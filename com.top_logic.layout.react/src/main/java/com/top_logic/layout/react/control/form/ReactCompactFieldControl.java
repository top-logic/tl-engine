/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.common.TextOverflow;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A field displayed in little room: a one-line preview of its value and a button opening the full
 * editor of the value in a dialog.
 *
 * <p>
 * For a field whose editor needs more room than a {@link FieldSpec#isCompact() compact} display
 * offers - a multi-line text in a table cell, the list of the values of a multi-valued field. The
 * preview is truncated with an ellipsis where it is wider than the available space and follows
 * every change of the field's value.
 * </p>
 *
 * <p>
 * What the dialog edits and what its buttons do is an {@link EditorSession}, opened by an
 * {@link Editing} each time the dialog opens: OK {@link EditorSession#apply() applies} the edit and
 * keeps the dialog open as long as the edit is not valid, Cancel and the close button of the
 * window {@link EditorSession#revert() revert} it. A field that may not be edited is shown
 * read-only in the dialog, which then only offers to close it. The preview is updated when the
 * dialog closes.
 * </p>
 *
 * <p>
 * A field that may not be edited and holds no value - what counts as none is decided per kind of
 * value, an HTML text without text and images for instance - has nothing to show in a dialog: its
 * opener is hidden until the field holds a value or becomes editable.
 * </p>
 *
 * <p>
 * The {@link #ReactCompactFieldControl(ReactContext, FieldModel, String, Function, Predicate, EditorFactory)
 * editing of a value} works on a copy of the value. OK writes the copy to the field, Cancel
 * discards it, so the field changes once, when the user confirms. OK keeps the dialog open as long
 * as the copy is not valid: while the editor reports an error, or while a mandatory field is
 * empty.
 * </p>
 *
 * <p>
 * Composed from existing controls: a {@code TLStack} row of a {@code TLText} preview and an
 * icon-only {@code TLButton}, and a {@code TLWindow} dialog holding the editor.
 * </p>
 */
public class ReactCompactFieldControl extends ReactStackControl {

	/**
	 * Creates the editor displayed in the dialog of a {@link ReactCompactFieldControl}.
	 */
	@FunctionalInterface
	public interface EditorFactory {

		/**
		 * Creates the editor of the given value.
		 *
		 * @param context
		 *        The context to create the editor in.
		 * @param model
		 *        Holds the edited copy of the value.
		 * @return The editor control.
		 */
		ReactControl createEditor(ReactContext context, FieldModel model);

	}

	/**
	 * The edit taking place in the dialog of a {@link ReactCompactFieldControl}, from opening the
	 * dialog until it is closed.
	 */
	public interface EditorSession {

		/**
		 * Creates the editor displayed in the dialog.
		 *
		 * @param context
		 *        The context to create the editor in.
		 * @param closeDialog
		 *        Closes the dialog without applying or reverting anything, for an edit that ends
		 *        while the dialog is open.
		 * @return The editor control.
		 */
		ReactControl createEditor(ReactContext context, Runnable closeDialog);

		/**
		 * Applies the edit, when the user confirms the dialog.
		 *
		 * <p>
		 * An edit that is not valid is not applied: the editor displays what is wrong, and the
		 * dialog stays open for the user to correct it.
		 * </p>
		 *
		 * @return Whether the edit was valid and is applied, so the dialog closes.
		 */
		boolean apply();

		/**
		 * Discards the edit, when the user cancels or closes the dialog.
		 */
		void revert();

	}

	/**
	 * Opens the {@link EditorSession} of a dialog of a {@link ReactCompactFieldControl}.
	 */
	@FunctionalInterface
	public interface Editing {

		/**
		 * Opens the edit taking place in a dialog that is about to open.
		 *
		 * @param model
		 *        The field model holding the displayed value.
		 * @param editable
		 *        Whether the value may be edited; otherwise the editor displays the value only.
		 * @return The session of the dialog.
		 */
		EditorSession open(FieldModel model, boolean editable);

	}

	/** The width the dialog opens with. */
	private static final DisplayDimension DIALOG_WIDTH = DisplayDimension.px(640);

	private final FieldModel _model;

	private final String _label;

	private final Function<Object, String> _previewText;

	private final Predicate<Object> _isEmpty;

	private final Editing _editing;

	private final ReactTextControl _preview;

	private final ReactButtonControl _opener;

	private FieldModelListener _modelListener;

	/**
	 * Creates a {@link ReactCompactFieldControl}.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param model
	 *        Holds the displayed value.
	 * @param label
	 *        The label of the field, naming the dialog, or {@code null} for a generic title.
	 * @param previewText
	 *        Produces the single line of text standing for a value of the field.
	 * @param isEmpty
	 *        Whether a value of the field has no content to display.
	 * @param editorFactory
	 *        Creates the editor of a copy of the value displayed in the dialog.
	 */
	public ReactCompactFieldControl(ReactContext context, FieldModel model, String label,
			Function<Object, String> previewText, Predicate<Object> isEmpty, EditorFactory editorFactory) {
		this(context, model, label, previewText, isEmpty, bufferedEditing(editorFactory, isEmpty));
	}

	/**
	 * Creates a {@link ReactCompactFieldControl} whose dialog edits what the given
	 * {@link Editing} opens.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param model
	 *        Holds the displayed value.
	 * @param label
	 *        The label of the field, naming the dialog, or {@code null} for a generic title.
	 * @param previewText
	 *        Produces the single line of text standing for a value of the field.
	 * @param isEmpty
	 *        Whether a value of the field has no content to display. A field that may not be
	 *        edited offers no dialog for such a value.
	 * @param editing
	 *        Opens the edit taking place in the dialog.
	 */
	public ReactCompactFieldControl(ReactContext context, FieldModel model, String label,
			Function<Object, String> previewText, Predicate<Object> isEmpty, Editing editing) {
		super(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false, List.of());
		_model = model;
		_label = label;
		_previewText = previewText;
		_isEmpty = isEmpty;
		_editing = editing;

		_preview = new ReactTextControl(context, previewText.apply(model.getValue()));
		_preview.setOverflow(TextOverflow.ELLIPSIS);

		Resources resources = Resources.getInstance();
		_opener = new ReactButtonControl(context, resources.getString(I18NConstants.COMPACT_FIELD_OPEN_BUTTON),
			ctx -> {
				if (!isOpenerShown()) {
					// A click from a client whose display lags behind: there is nothing to open.
					return HandlerResult.DEFAULT_RESULT;
				}
				openEditor(ctx);
				return HandlerResult.DEFAULT_RESULT;
			});
		_opener.setImage(Icons.COMPACT_FIELD_OPEN);
		_opener.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		updateOpener();

		setChildren(List.of(_preview, _opener));
		setGrowFirst(true);

		_modelListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_preview.setText(_previewText.apply(newValue));
				updateOpener();
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				updateOpener();
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		};
		_model.addListener(_modelListener);
	}

	/**
	 * The field model holding the displayed value.
	 */
	public FieldModel getFieldModel() {
		return _model;
	}

	/**
	 * Whether the button opening the dialog is offered.
	 *
	 * <p>
	 * It is, unless the field may not be edited and holds no value.
	 * </p>
	 */
	public boolean isOpenerShown() {
		return !_opener.isHidden();
	}

	/**
	 * Offers the opener where the dialog has something to show: a value to look at, or the editor
	 * entering one.
	 */
	private void updateOpener() {
		_opener.setHidden(!_model.isEditable() && _isEmpty.test(_model.getValue()));
	}

	/**
	 * The text currently standing for the value of the field.
	 */
	public String getPreviewText() {
		return _preview.getText();
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		if (_modelListener != null) {
			_model.removeListener(_modelListener);
			_modelListener = null;
		}
	}

	/**
	 * Opens the dialog displaying the full editor of the field's value.
	 *
	 * <p>
	 * Does nothing without a {@link ReactContext#getDialogManager() dialog manager}.
	 * </p>
	 *
	 * @param context
	 *        The context to open the dialog in.
	 */
	public void openEditor(ReactContext context) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			return;
		}
		boolean editable = _model.isEditable();
		EditorSession session = _editing.open(_model, editable);

		Runnable close = () -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			updatePreview();
		};
		Runnable cancel = () -> {
			session.revert();
			close.run();
		};
		ReactControl editor = session.createEditor(context, close);

		ReactWindowControl window = new ReactWindowControl(context, dialogTitle(), DIALOG_WIDTH, cancel);
		window.setResizable(true);
		// The window body is flush; the editor keeps the page inset from its border.
		window.setChild(new ReactInsetControl(context, editor));

		List<ReactControl> actions = new ArrayList<>();
		if (editable) {
			actions.add(MessageButtons.cancel(context, ctx -> {
				cancel.run();
				return HandlerResult.DEFAULT_RESULT;
			}));
			// Not the Enter-default: the editors displayed here are multi-line, where Enter
			// inserts a line break.
			actions.add(MessageButtons.ok(context, ctx -> {
				if (!session.apply()) {
					// The editor displays the error; the dialog stays open for the user to correct
					// it.
					return HandlerResult.DEFAULT_RESULT;
				}
				dialogManager.closeTopDialog(DialogResult.ok(null));
				updatePreview();
				return HandlerResult.DEFAULT_RESULT;
			}));
		} else {
			actions.add(MessageButtons.close(context, ctx -> {
				cancel.run();
				return HandlerResult.DEFAULT_RESULT;
			}));
		}
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Nothing to do on close: the buttons applied or reverted the edit.
		});
	}

	/**
	 * Shows the preview of the current value, which an edit in the dialog may have changed without
	 * a change of the field's value being announced.
	 */
	private void updatePreview() {
		_preview.setText(_previewText.apply(_model.getValue()));
		updateOpener();
	}

	/**
	 * The {@link Editing} of a copy of the field's value, which OK writes to the field.
	 *
	 * @param editorFactory
	 *        Creates the editor of the copy.
	 * @param isEmpty
	 *        Whether a value of the field has no content, which a
	 *        {@link FieldModel#isMandatory() mandatory} field must not be confirmed with.
	 */
	public static Editing bufferedEditing(EditorFactory editorFactory, Predicate<Object> isEmpty) {
		return (model, editable) -> new BufferedEditorSession(model, editable, editorFactory, isEmpty);
	}

	/**
	 * {@link EditorSession} editing a copy of the value of a field.
	 */
	private static final class BufferedEditorSession implements EditorSession {

		private final FieldModel _model;

		private final AbstractFieldModel _buffer;

		private final EditorFactory _editorFactory;

		private final Predicate<Object> _isEmpty;

		BufferedEditorSession(FieldModel model, boolean editable, EditorFactory editorFactory,
				Predicate<Object> isEmpty) {
			_model = model;
			_editorFactory = editorFactory;
			_isEmpty = isEmpty;
			_buffer = new AbstractFieldModel(copyValue(model.getValue()));
			_buffer.setMandatory(model.isMandatory());
			_buffer.setNullable(model.isNullable());
			_buffer.setEditable(editable);
			_buffer.addListener(new FieldModelListener() {
				@Override
				public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
					if (_buffer.isRevealed()) {
						// Once reported, the missing value is reported as long as it is missing.
						validateMandatory(_buffer, _isEmpty);
					}
				}

				@Override
				public void onEditabilityChanged(FieldModel source, boolean isEditable) {
					// Ignored.
				}

				@Override
				public void onValidationChanged(FieldModel source) {
					// Ignored.
				}
			});
		}

		@Override
		public ReactControl createEditor(ReactContext context, Runnable closeDialog) {
			return _editorFactory.createEditor(context, _buffer);
		}

		/**
		 * Writes the edited copy to the field, when it is valid.
		 *
		 * <p>
		 * Nothing is written to a field that may not be edited (any more), however the request to
		 * confirm the dialog arrived.
		 * </p>
		 */
		@Override
		public boolean apply() {
			if (!isValid(_buffer, _isEmpty)) {
				return false;
			}
			if (_model.isEditable()) {
				_model.setValue(_buffer.getValue());
			}
			return true;
		}

		@Override
		public void revert() {
			// The copy is dropped with the session.
		}

	}

	/**
	 * Whether the edited copy may be written to the field.
	 *
	 * <p>
	 * Not while the editor reports an error, an input it cannot parse for instance, and not while
	 * a {@link FieldModel#isMandatory() mandatory} field is empty. Revealing the errors of the copy
	 * makes the editor display them.
	 * </p>
	 */
	private static boolean isValid(AbstractFieldModel buffer, Predicate<Object> isEmpty) {
		validateMandatory(buffer, isEmpty);
		buffer.setRevealed(true);
		return !buffer.hasError();
	}

	/**
	 * Reports a missing value of a {@link FieldModel#isMandatory() mandatory} field as an error of
	 * the given copy, and withdraws the report once a value is given.
	 *
	 * @param isEmpty
	 *        Whether a value of the field has no content.
	 */
	private static void validateMandatory(AbstractFieldModel buffer, Predicate<Object> isEmpty) {
		boolean missing = buffer.isMandatory() && isEmpty.test(buffer.getValue());
		buffer.setModelValidationError(missing ? I18NConstants.COMPACT_FIELD_ERROR_VALUE_REQUIRED : null);
	}

	/** The dialog title, naming the field when its label is known. */
	private String dialogTitle() {
		if (_label == null || _label.isEmpty()) {
			return Resources.getInstance().getString(I18NConstants.COMPACT_FIELD_TITLE);
		}
		return _label;
	}

	/**
	 * A copy of the given value that the dialog may change without changing the field: a
	 * collection is copied, any other value is immutable or replaced as a whole when edited.
	 */
	private static Object copyValue(Object value) {
		if (value instanceof Set<?> set) {
			return new LinkedHashSet<>(set);
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return value;
	}

}
