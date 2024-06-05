/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeySelector;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.Command.CommandChain;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlCommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.component.WarningsDialog;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.FieldCopier;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Control displaying a value with a popup button opening a popup dialog for editing this value.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F?rster</a>
 */
public class PopupEditControl extends AbstractFormFieldControl {

	/** The suffix for {@link FormField} names in a popup. */
	public static final String POPUP_SUFFIX = "_Popup";

	/**
	 * {@link KeySelector} for directly accepting the text input from the popup editor.
	 */
	public static final KeySelector CTRL_RETURN = new KeySelector(KeyCode.RETURN, KeySelector.CTRL_MASK);

	private static final ControlCommand POPUP_COMMAND = new OpenPopup("openEditPopup");

	/**
	 * Commands of {@link PopupEditControl}.
	 */
	@SuppressWarnings("hiding")
	protected static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(AbstractFormFieldControl.COMMANDS, POPUP_COMMAND);

	/**
	 * {@link ControlProvider} creating {@link PopupEditControl}s.
	 */
	public static class CP extends AbstractFormFieldControlProvider implements ConfiguredInstance<CP.Config<?>> {

		/**
		 * Configuration options for {@link PopupEditControl.CP}.
		 */
		public interface Config<I extends CP> extends PolymorphicConfiguration<I>, ConfiguredFirstLineRenderer.Options {
			/** @see #getEditControlProvider() */
			String EDIT_CONTROL_PROVIDER = "editControlProvider";

			/**
			 * {@link ControlProvider} that is responsible for creating the edit display shown in
			 * the popup dialog.
			 */
			@Name(EDIT_CONTROL_PROVIDER)
			PolymorphicConfiguration<? extends ControlProvider> getEditControlProvider();

			/**
			 * @see #getEditControlProvider()
			 */
			void setEditControlProvider(PolymorphicConfiguration<? extends ControlProvider> value);

			/**
			 * Width of the opened dialog.
			 */
			@Name("dialogWidth")
			DisplayDimension getDialogWidth();

			/**
			 * @see #getDialogWidth()
			 */
			void setDialogWidth(DisplayDimension value);

			/**
			 * Height of the opened dialog.
			 */
			@Name("dialogHeight")
			DisplayDimension getDialogHeight();

			/**
			 * @see #getDialogHeight()
			 */
			void setDialogHeight(DisplayDimension value);
		}

		private final Config<?> _config;

		private final Settings _settings;

		/**
		 * Creates a {@link CP} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public CP(InstantiationContext context, Config<?> config) {
			_config = config;

			_settings = new Settings()
				.setFirstLineRenderer(ConfiguredFirstLineRenderer.createFirstLineRenderer(context, config))
				.setEditControlProvider(context, config.getEditControlProvider())
				.setDialogWidth(config.getDialogWidth())
				.setDialogHeight(config.getDialogHeight());
		}

		@Override
		public Config<?> getConfig() {
			return _config;
		}

		@Override
		protected Control createInput(FormMember member) {
			return new PopupEditControl(getSettings(), (FormField) member);
		}

		/**
		 * The {@link Settings} to create the control from.
		 */
		protected Settings getSettings() {
			return _settings;
		}
	}

	private final CommandModel _openEditor;

	private Settings _settings;

	/**
	 * Settings for creating a {@link PopupEditControl}.
	 * 
	 * @see PopupEditControl#PopupEditControl(Settings, FormField)
	 */
	public static class Settings {

		private static final DefaultLayoutData DEFAULT_LAYOUT =
			new DefaultLayoutData(DisplayDimension.px(720), 100, DisplayDimension.px(380), 100, Scrolling.AUTO);

		private Renderer<Object> _firstLineRenderer = FirstLineRenderer.DEFAULT_INSTANCE;

		private ControlProvider _editControlProvider = DefaultFormFieldControlProvider.INSTANCE;

		private LayoutData _dialogLayout = DEFAULT_LAYOUT;

		/**
		 * The {@link Renderer} to display the value in line.
		 */
		public Renderer<Object> getFirstLineRenderer() {
			return _firstLineRenderer;
		}

		/**
		 * Updates {@link #getFirstLineRenderer()} with a {@link Renderer} instantiated from
		 * configuration.
		 */
		public Settings setFirstLineRenderer(InstantiationContext context,
				PolymorphicConfiguration<? extends Renderer<?>> value) {
			if (value == null) {
				return this;
			}

			return setFirstLineRenderer(context.getInstance(value).generic());
		}

		/**
		 * @see #getFirstLineRenderer()
		 */
		public Settings setFirstLineRenderer(Renderer<Object> value) {
			if (value != null) {
				_firstLineRenderer = value;
			}
			return this;
		}

		/**
		 * The {@link ControlProvider} to display the value in the popup dialog.
		 */
		public ControlProvider getEditControlProvider() {
			return _editControlProvider;
		}

		/**
		 * Updates {@link #getEditControlProvider()} with a {@link ControlProvider} instantiated
		 * from configuration.
		 */
		public Settings setEditControlProvider(InstantiationContext context,
				PolymorphicConfiguration<? extends ControlProvider> value) {
			if (value == null) {
				return this;
			}
			return setEditControlProvider(context.getInstance(value));
		}

		/**
		 * @see #getEditControlProvider()
		 */
		public Settings setEditControlProvider(ControlProvider instance) {
			if (instance != null) {
				_editControlProvider = instance;
			}
			return this;
		}

		/**
		 * {@link LayoutData} defining the dialog to open for editing the value.
		 */
		public LayoutData getDialogLayout() {
			return _dialogLayout;
		}

		/**
		 * Updates the dialog width.
		 * 
		 * @see #getDialogLayout()
		 */
		public Settings setDialogWidth(DisplayDimension dim) {
			if (dim != null) {
				_dialogLayout = _dialogLayout.resized(dim, _dialogLayout.getHeightDimension());
			}
			return this;
		}

		/**
		 * Updates the dialog height.
		 * 
		 * @see #getDialogLayout()
		 */
		public Settings setDialogHeight(DisplayDimension dim) {
			if (dim != null) {
				_dialogLayout = _dialogLayout.resized(_dialogLayout.getWidthDimension(), dim);
			}
			return this;
		}
	}

	/**
	 * Creates a {@link PopupEditControl}.
	 * 
	 * @param model
	 *        The {@link FormField} model.
	 */
	public PopupEditControl(Settings settings, FormField model) {
		super(model, COMMANDS);

		_settings = settings;
		_openEditor = createOpenEditorButton();
	}

	private CommandModel createOpenEditorButton() {
		ControlCommandModel result = new ControlCommandModel(POPUP_COMMAND, this);
		result.setImage(Icons.OPEN_TEXT_EDITOR);
		result.setNotExecutableImage(Icons.OPEN_TEXT_EDITOR_DISABLED);
		result.setTooltip(Resources.getInstance().getString(I18NConstants.TEXT_POPUP_OPEN));
		ScriptingRecorder.annotateAsDontRecord(result);
		updateOpenerExecutability(result);
		return result;
	}

	private void updateOpenerExecutability(CommandModel opener) {
		boolean executable = !isDisabled();
		if (executable) {
			opener.setExecutable();
		} else {
			// Field is currently disabled
			opener.setNotExecutable(com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED);
		}
	}

	@Override
	protected String getTypeCssClass() {
		return isImmutable() ? "cTextPopupView" : "cTextPopup";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		writeContent(context, out, true);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		writeContent(context, out, false);
	}

	private void writeContent(DisplayContext context, TagWriter out, boolean editable) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			_settings.getFirstLineRenderer().write(context, out, getFieldModel().getValue());
			if (editable) {
				writeEditorButton(context, out);
			}
		}
		out.endTag(SPAN);
	}

	private void writeEditorButton(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_LEFT_CSS_CLASS);
		out.endBeginTag();
		{
			new ButtonControl(_openEditor).write(context, out);
		}
		out.endTag(SPAN);
	}

	private boolean isDisabled() {
		return getModel().isDisabled();
	}

	private boolean isImmutable() {
		return getModel().isImmutable();
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		updateOpenerExecutability(_openEditor);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	/**
	 * Opens the popup dialog when the user requests editing the value.
	 */
	protected HandlerResult openPopup(DisplayContext commandContext) {
		FormField originalField = (FormField) getModel();

		FormField editField = createEditFieldInternal(originalField);
		Control editControl = createEditControl(editField);

		// Insert into dummy form context to get default check for errors and warning when
		// pressing OK.
		FormContext context = new FormContext(editField.getName() + "_formContext", ResPrefix.GLOBAL);
		context.addMember(editField);

		DefaultDialogModel dialogModel = new DefaultDialogModel(getDialogLayout(editField),
			getDialogTitle(editField), true, true, null);
		DialogWindowControl dialogControl = new DialogWindowControl(dialogModel);
		FixedFlowLayoutControl layout = new FixedFlowLayoutControl(Orientation.VERTICAL);

		LayoutControlAdapter editlayout = new LayoutControlAdapter(editControl);
		editlayout.setConstraint(
			new DefaultLayoutData(
				DisplayDimension.percent(100), 100,
				DisplayDimension.percent(100), 100, Scrolling.AUTO));
		layout.addChild(editlayout);

		Command closeAction = dialogModel.getCloseAction();
		CommandChain apply = new CommandChain(getApplyCommand(originalField, editField), closeAction);
		dialogModel.setDefaultCommand(apply);
		LayoutControlAdapter buttonsLayout = new LayoutControlAdapter(getButtonsTag(apply, closeAction));
		buttonsLayout.setConstraint(
			new DefaultLayoutData(
				DisplayDimension.percent(100), 100,
				DisplayDimension.px(40), 100, Scrolling.NO));
		layout.addChild(buttonsLayout);

		dialogControl.setChildControl(layout);
		commandContext.getWindowScope().openDialog(dialogControl);
		return HandlerResult.DEFAULT_RESULT;
	}

	private FormField createEditFieldInternal(FormField originalField) {
		FormField editField = createEditField(originalField);
		editField.setLabel(originalField.getLabel());
		editField.initializeField(originalField.getValue());
		editField.setControlProvider(originalField.getControlProvider());
		copyFieldConstraints(originalField, editField);
		copyFieldAnnotations(originalField, editField);
		ScriptingRecorder.annotateAsDontRecord(editField);
		return editField;
	}

	/**
	 * Creates a copy of the original field for showing in the dialog.
	 */
	protected FormField createEditField(FormField originalField) {
		return (FormField) originalField.visit(FieldCopier.INSTANCE, originalField.getName() + "_popupEditor");
	}

	/**
	 * Creates the control for the edit field to show in dialog.
	 */
	protected Control createEditControl(FormField editField) {
		return _settings.getEditControlProvider().createControl(editField, null);
	}

	/**
	 * Gets the dialog layout.
	 * 
	 * @param editField
	 *        The {@link FormField} displayed in the edit popup, see
	 *        {@link #createEditField(FormField)}.
	 */
	protected LayoutData getDialogLayout(FormField editField) {
		return getConfiguredDialogLayout();
	}

	/**
	 * The {@link LayoutData} as provided by the configuration.
	 */
	protected final LayoutData getConfiguredDialogLayout() {
		return _settings.getDialogLayout();
	}

	/**
	 * Gets the dialog title.
	 */
	protected HTMLFragment getDialogTitle(FormField editField) {
		return new ResourceText(I18NConstants.TEXT_POPUP_TITLE.fill(editField.getLabel()));
	}

	/**
	 * Gets the apply command for the popup dialog.
	 */
	protected Command getApplyCommand(final FormField originalField, final FormField editField) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {

				// check for errors before applying changes
				FormContext formContext = (FormContext) editField.getParent();
				if (!formContext.checkAll()) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}

				// display warnings
				if (!AbstractFormCommandHandler.warningsDisabledTemporarily() && formContext.hasWarnings()) {
					HandlerResult suspended = HandlerResult.suspended();
					WarningsDialog.openWarningsDialog(context.getWindowScope(),
						com.top_logic.layout.form.component.I18NConstants.APPLY, formContext,
						AbstractFormCommandHandler.resumeContinuation(suspended));
					return suspended;
				}

				Object newValue = editField.getValue();
				try {
					if (originalField instanceof AbstractFormField) {
						FormFieldInternals.setValue((AbstractFormField) originalField, newValue);
					} else {
						originalField.setValue(newValue);
						originalField.checkWithAllDependencies();

						if (ScriptingRecorder.isRecordingActive()) {
							ScriptingRecorder.recordFieldInput(originalField, newValue);
						}
					}
				} catch (VetoException ex) {
					if (ScriptingRecorder.isRecordingActive()) {
						ScriptingRecorder.recordFieldInput(originalField, newValue);
					}
					ex.setContinuationCommand(getContinuationCommand(originalField, newValue));
				}
				return HandlerResult.DEFAULT_RESULT;
			}

			private Command getContinuationCommand(final FormField field, final Object newValue) {
				return new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext continuationContext) {
						field.setValue(newValue);
						field.checkWithAllDependencies();
						return HandlerResult.DEFAULT_RESULT;
					}
				};
			}
		};
	}

	/**
	 * Gets the apply button as {@link HTMLFragment}.
	 */
	protected HTMLFragment getApplyButton(Command apply) {
		CommandModel okModel = MessageBox.button(ButtonType.OK, apply);
		ScriptingRecorder.annotateAsDontRecord(okModel);
		return new ButtonControl(okModel, ButtonComponentButtonRenderer.INSTANCE);
	}

	/**
	 * Gets the cancel button as {@link HTMLFragment}.
	 */
	protected HTMLFragment getCancelButton(Command cancel) {
		CommandModel cancelModel = MessageBox.button(ButtonType.CANCEL, cancel);
		ScriptingRecorder.annotateAsDontRecord(cancelModel);
		return new ButtonControl(cancelModel, ButtonComponentButtonRenderer.INSTANCE);
	}

	/**
	 * Gets the buttons as {@link HTMLFragment}.
	 */
	protected Tag getButtonsTag(Command apply, Command cancel) {
		HTMLFragment applyButton = getApplyButton(apply);
		HTMLFragment cancelButton = getCancelButton(cancel);
		return Fragments.div(ButtonComponent.DEFAULT_CSS_CLASS, applyButton, cancelButton);
	}

	/**
	 * Util method to copy all constraints from the original field to the edit field.
	 */
	protected void copyFieldConstraints(FormField originalField, FormField editField) {
		for (Constraint constraint : originalField.getConstraints()) {
			editField.addConstraint(constraint);
		}
	}

	@SuppressWarnings("unchecked")
	private void copyFieldAnnotations(FormField originalField, FormField editField) {
		if (originalField instanceof AbstractFormField) {
			for (Entry<Property<?>, Object> entry : ((AbstractFormField) originalField)
				.internalAccessLazyPropertiesStore().toMap().entrySet()) {
				@SuppressWarnings("rawtypes")
				Property property = entry.getKey();
				editField.set(property, property.externalize(originalField, entry.getValue()));
			}
		}
	}

	/**
	 * {@link ControlCommand} opening the popup dialog.
	 * 
	 * @see PopupEditControl#openPopup(DisplayContext)
	 */
	public static class OpenPopup extends ControlCommand {
		/**
		 * Creates a new {@link OpenPopup}.
		 */
		public OpenPopup(String aCommand) {
			super(aCommand);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			PopupEditControl displayControl = (PopupEditControl) control;
			return displayControl.openPopup(commandContext);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TEXT_POPUP_OPEN;
		}
	}

}
