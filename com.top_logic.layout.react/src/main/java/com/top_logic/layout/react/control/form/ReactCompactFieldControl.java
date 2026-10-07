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
 * The dialog edits a copy of the value. OK writes the copy to the field, Cancel discards it, so the
 * field changes once, when the user confirms. OK keeps the dialog open as long as the copy is not
 * valid: while the editor reports an error, or while a mandatory field is empty. A field that may not be edited is shown read-only in
 * the dialog, which then only offers to close it.
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

	/** The width the dialog opens with. */
	private static final DisplayDimension DIALOG_WIDTH = DisplayDimension.px(640);

	private final FieldModel _model;

	private final String _label;

	private final Function<Object, String> _previewText;

	private final EditorFactory _editorFactory;

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
	 * @param editorFactory
	 *        Creates the editor displayed in the dialog.
	 */
	public ReactCompactFieldControl(ReactContext context, FieldModel model, String label,
			Function<Object, String> previewText, EditorFactory editorFactory) {
		super(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false, List.of());
		_model = model;
		_label = label;
		_previewText = previewText;
		_editorFactory = editorFactory;

		_preview = new ReactTextControl(context, previewText.apply(model.getValue()));
		_preview.setOverflow(TextOverflow.ELLIPSIS);

		Resources resources = Resources.getInstance();
		_opener = new ReactButtonControl(context, resources.getString(I18NConstants.COMPACT_FIELD_OPEN_BUTTON),
			ctx -> {
				openEditor(ctx);
				return HandlerResult.DEFAULT_RESULT;
			});
		_opener.setImage(Icons.COMPACT_FIELD_OPEN);
		_opener.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		setChildren(List.of(_preview, _opener));
		setGrowFirst(true);

		_modelListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_preview.setText(_previewText.apply(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The dialog shows the value either way, editable or read-only.
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

		AbstractFieldModel buffer = new AbstractFieldModel(copyValue(_model.getValue()));
		buffer.setMandatory(_model.isMandatory());
		buffer.setNullable(_model.isNullable());
		buffer.setEditable(editable);
		buffer.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				if (buffer.isRevealed()) {
					// Once reported, the missing value is reported as long as it is missing.
					validateMandatory(buffer);
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

		ReactControl editor = _editorFactory.createEditor(context, buffer);

		ReactWindowControl window = new ReactWindowControl(context, dialogTitle(), DIALOG_WIDTH,
			() -> dialogManager.closeTopDialog(DialogResult.cancelled()));
		window.setResizable(true);
		// The window body is flush; the editor keeps the page inset from its border.
		window.setChild(new ReactInsetControl(context, editor));

		List<ReactControl> actions = new ArrayList<>();
		if (editable) {
			actions.add(MessageButtons.cancel(context, ctx -> {
				dialogManager.closeTopDialog(DialogResult.cancelled());
				return HandlerResult.DEFAULT_RESULT;
			}));
			// Not the Enter-default: the editors displayed here are multi-line, where Enter
			// inserts a line break.
			actions.add(MessageButtons.ok(context, ctx -> {
				if (!isValid(buffer)) {
					// The editor displays the error at its field; the dialog stays open for the
					// user to correct it.
					return HandlerResult.DEFAULT_RESULT;
				}
				apply(buffer);
				dialogManager.closeTopDialog(DialogResult.ok(null));
				return HandlerResult.DEFAULT_RESULT;
			}));
		} else {
			actions.add(MessageButtons.close(context, ctx -> {
				dialogManager.closeTopDialog(DialogResult.cancelled());
				return HandlerResult.DEFAULT_RESULT;
			}));
		}
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Nothing to do on close: OK already applied the value, Cancel discards it.
		});
	}

	/**
	 * Writes the edited copy to the field.
	 *
	 * <p>
	 * Refused for a field that may not be edited (any more), however the request to confirm the
	 * dialog arrived.
	 * </p>
	 */
	private void apply(FieldModel buffer) {
		if (!_model.isEditable()) {
			return;
		}
		_model.setValue(buffer.getValue());
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
	private static boolean isValid(AbstractFieldModel buffer) {
		validateMandatory(buffer);
		buffer.setRevealed(true);
		return !buffer.hasError();
	}

	/**
	 * Reports a missing value of a {@link FieldModel#isMandatory() mandatory} field as an error of
	 * the given copy, and withdraws the report once a value is given.
	 */
	private static void validateMandatory(AbstractFieldModel buffer) {
		boolean missing = buffer.isMandatory() && isEmpty(buffer.getValue());
		buffer.setModelValidationError(missing ? I18NConstants.COMPACT_FIELD_ERROR_VALUE_REQUIRED : null);
	}

	/**
	 * Whether the given value is no value: {@code null}, an empty text, or an empty collection.
	 */
	private static boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof CharSequence text) {
			return text.length() == 0;
		}
		if (value instanceof Collection<?> collection) {
			return collection.isEmpty();
		}
		return false;
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
