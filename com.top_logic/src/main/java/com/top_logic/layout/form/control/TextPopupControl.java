/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.ButtonBar;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlViewAdapter;
import com.top_logic.layout.basic.HTMLFragmentView;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Multi-line text input that is usable in fixed-size table rows.
 * 
 * <p>
 * From a multi-line text, only the first line is displayed with a tool-tip showing the complete
 * text. Editing the text is possible by opening a pop-up showing an editor.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextPopupControl extends PopupEditControl {

	private boolean _immutable;

	private int _rows;

	private int _columns;

	/**
	 * {@link ControlProvider} creating {@link TextPopupControl}s.
	 */
	public static class CP extends PopupEditControl.CP {

		/**
		 * Configuration options for {@link CP}s.
		 */
		public interface Config extends PopupEditControl.CP.Config<CP> {

			/** Name of the annotation {@link Config#isImmutable()} */
			String IMMUTABLE = "immutable";

			/**
			 * Configuration if the text field should be immutable.
			 */
			@Name(IMMUTABLE)
			@BooleanDefault(false)
			boolean isImmutable();

			/**
			 * Rows of the text field displayed in the pop-up.
			 */
			@Name("rows")
			@IntDefault(20)
			int getRows();

			/**
			 * Columns of the text field displayed in the pop-up.
			 */
			@Name("columns")
			@IntDefault(80)
			int getColumns();

		}

		private boolean _immutable;

		private int _rows;

		private int _columns;

		/**
		 * Creates a {@link TextPopupControl.CP} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public CP(InstantiationContext context, Config config) {
			super(context, config);

			_immutable = config.isImmutable();
			_rows = config.getRows();
			_columns = config.getColumns();
		}

		/**
		 * Singleton {@link TextPopupControl.CP} instance.
		 */
		public static final CP DEFAULT_INSTANCE =
			new CP(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				TypedConfiguration.newConfigItem(Config.class));

		@Override
		public Control createControl(Object model, String style) {
			return new TextPopupControl(getSettings(), _rows, _columns, _immutable, (FormField) model);
		}
	}

	/**
	 * Creates a {@link TextPopupControl}.
	 */
	public TextPopupControl(Settings settings, int rows, int columns, boolean immutable, FormField model) {
		super(settings, model);
		_rows = rows;
		_columns = columns;
		_immutable = immutable;
	}

	@Override
	protected HandlerResult openPopup(DisplayContext commandContext) {
		LayoutData layout = getLayout();
		final PopupDialogModel editorModel = new DefaultPopupDialogModel(layout);

		PopupDialogControl editorPopup = getEditorPopup(editorModel);
		final AbstractFormField originalField = (AbstractFormField) getFieldModel();

		final FormField editField = createEditField(originalField);

		Command cancel = getCancelCommand(editorModel);
		Control contents = createContents(editorModel, originalField, editField, cancel);
		editorPopup.setContent(contents);

		commandContext.getWindowScope().openPopupDialog(editorPopup);

		return HandlerResult.DEFAULT_RESULT;
	}

	private Control createContents(final PopupDialogModel editorModel, AbstractFormField originalField,
			FormField editField, Command cancel) {
		if (!_immutable || originalField.isImmutable()) {
			final Command apply = getApplyCommand(editorModel, originalField, editField);
			HTMLFragment okButton = getApplyButton(apply);
			HTMLFragment cancelButton = getCancelButton(cancel);

			initEditField(originalField, editField, apply);
			addSourceConstraints(originalField, editField);
			TextInputControl editor = getTextEditorControl(editField);

			return getContent(editor, cancelButton, okButton);
		} else {
			HTMLFragment closeButton = getCloseButton(cancel);

			editField.setImmutable(true);
			initEditField(originalField, editField, null);
			addSourceConstraints(originalField, editField);
			TextInputControl editor = getTextEditorControl(editField);

			return getContent(editor, closeButton);

		}
	}

	private ControlViewAdapter getContent(TextInputControl editor, HTMLFragment cancelButton,
			HTMLFragment okButton) {
		return new ControlViewAdapter(new HTMLFragmentView(
			div("cTextPopupEditor",
				div(editor),
				div(ButtonBar.DEFAULT_CSS_CLASS, okButton, cancelButton))));
	}

	private ControlViewAdapter getContent(TextInputControl editor, HTMLFragment cancelButton) {
		return new ControlViewAdapter(new HTMLFragmentView(
			div("cTextPopupEditor",
				div(editor),
				div(ButtonBar.DEFAULT_CSS_CLASS, cancelButton))));
	}

	private void initEditField(final AbstractFormField originalField, final FormField editField,
			final Command apply) {
		ScriptingRecorder.annotateAsDontRecord(editField);
		editField.setLabel(ResKey.EMPTY_TEXT);
		editField.initializeField(originalField.getValue());
		if (!originalField.isImmutable()) {
			editField.addKeyListener(new KeyEventListener(CTRL_RETURN) {
				@Override
				public HandlerResult handleKeyEvent(DisplayContext context, KeyEvent event) {
					return apply.executeCommand(context);
				}
			});
		}
	}

	private void addSourceConstraints(final AbstractFormField originalField, final FormField editField) {
		for (Constraint constraint : originalField.getConstraints()) {
			editField.addConstraint(constraint);
		}
	}

	private TextInputControl getTextEditorControl(final FormField editField) {
		TextInputControl editor = new TextInputControl(editField);
		editor.setMultiLine(true);
		editor.setRows(_rows);
		editor.setColumns(_columns);
		return editor;
	}

	private PopupDialogControl getEditorPopup(final PopupDialogModel editorModel) {
		String placementId = getID();
		return new PopupDialogControl(getFrameScope(), editorModel, placementId);
	}

	private Command getCancelCommand(final PopupDialogModel editorModel) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				editorModel.setClosed();
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	private DefaultLayoutData getLayout() {
		return new DefaultLayoutData(
			DisplayDimension.dim(0, DisplayUnit.PIXEL), 100,
			DisplayDimension.dim(0, DisplayUnit.PIXEL), 100,
			Scrolling.NO);
	}

	private Command getApplyCommand(final PopupDialogModel editorModel, final AbstractFormField originalField,
			final FormField editField) {
		final Command apply = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final Object newValue = editField.getValue();
				try {
					FormFieldInternals.setValue(originalField, newValue);
					editorModel.setClosed();
				} catch (VetoException ex) {
					if (ScriptingRecorder.isRecordingActive()) {
						ScriptingRecorder.recordFieldInput(originalField, newValue);
					}

					ex.setContinuationCommand(new Command() {
						@Override
						public HandlerResult executeCommand(DisplayContext continuationContext) {
							originalField.setValue(newValue);
							originalField.checkWithAllDependencies();
							editorModel.setClosed();
							return HandlerResult.DEFAULT_RESULT;
						}
					});
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		return apply;
	}

	@Override
	protected HTMLFragment getCancelButton(Command cancel) {
		CommandModel cancelModel = MessageBox.button(ButtonType.CANCEL, cancel);
		ScriptingRecorder.annotateAsDontRecord(cancelModel);
		return new ButtonControl(cancelModel, ButtonComponentButtonRenderer.INSTANCE);
	}

	private HTMLFragment getCloseButton(Command close) {
		CommandModel closeModel = MessageBox.button(ButtonType.CLOSE, close);
		ScriptingRecorder.annotateAsDontRecord(closeModel);
		return new ButtonControl(closeModel, ButtonComponentButtonRenderer.INSTANCE);
	}

	@Override
	protected HTMLFragment getApplyButton(final Command apply) {
		CommandModel okModel = MessageBox.button(ButtonType.OK, apply);
		ScriptingRecorder.annotateAsDontRecord(okModel);
		return new ButtonControl(okModel, ButtonComponentButtonRenderer.INSTANCE);
	}

}
