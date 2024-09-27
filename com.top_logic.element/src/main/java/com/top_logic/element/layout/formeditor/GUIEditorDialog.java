/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.Command.CommandChain;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.declarative.DirectFormDisplay;
import com.top_logic.layout.form.declarative.DirectFormDisplay.Config;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.FlexibleFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.mig.html.layout.Layout.LayoutResizeMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormDialogBase} to edit a {@link FormDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GUIEditorDialog extends AbstractFormDialogBase {

	private boolean _inEditMode;

	private TLStructuredType _type;

	private FormDefinition _formDefinition;

	private Supplier<? extends List<FormDefinitionTemplate>> _templateProvider = () -> Collections.emptyList();

	private Command _acceptCommand;

	private List<CommandModel> _additionalCommands = new ArrayList<>();

	private final LayoutComponent _contextComponent;

	/**
	 * Creates a new {@link GUIEditorDialog} with default behavior.
	 * 
	 * @param contextComponent
	 *        The component for which a from is currently edited, <code>null</code> if the edited
	 *        form does not belong to a unique component.
	 */
	public GUIEditorDialog(LayoutComponent contextComponent) {
		super(DefaultDialogModel.dialogModel(I18NConstants.FORM_EDITOR_DIALOG, DisplayDimension.FIFTY_PERCENT, DisplayDimension.FIFTY_PERCENT));
		_contextComponent = contextComponent;
		// Open the dialog maximized
		getDialogModel().setMaximized(true);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		FormDefinition formDefinition = getFormDefinition();
		Objects.requireNonNull(formDefinition, "No FormDefinition to edit given.");
		DisplayFormEditorBuilder formBuilder = new DisplayFormEditorBuilder(_contextComponent);
		formBuilder.setInEditMode(openInEditMode());
		formBuilder.setTemplateProvider(getTemplateProvider());
		formBuilder.fillFormContext(getType(), I18NConstants.FORM_EDITOR_DIALOG, context, formDefinition);
	}

	@Override
	protected HTMLFragment createLayout() {
		FlexibleFlowLayoutControl layout =
			new FlexibleFlowLayoutControl(LayoutResizeMode.INSTANT, Orientation.HORIZONTAL);
		layout.addChild(
			createMediaControl(DisplayDimension.HUNDERED_PERCENT, DisplayDimension.HUNDERED_PERCENT, editorView()));
		layout.addChild(createMediaControl(DisplayDimension.dim(300, DisplayUnit.PIXEL),
			DisplayDimension.HUNDERED_PERCENT, attributesView()));
		return layout;
	}

	@Override
	protected HTMLFragment createView() {
		return null;
	}

	@Override
	public HandlerResult open(WindowScope windowScope) {
		DialogWindowControl dialog = new DialogWindowControl(getDialogModel());
		ToolBar toolbar = dialog.getDialogModel().getToolbar();
		ArrayList<CommandModel> buttons = new ArrayList<>();
		fillButtons(buttons);
		if (!buttons.isEmpty()) {
			ToolBarGroup grp = toolbar.defineGroup("basic-edit");
			grp.addButtons(buttons);
		}

		dialog.setChildControl(new LayoutControlAdapter(createLayout()));
		windowScope.openDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

	private HTMLFragment editorView() {
		return createFormDisplay("rf_editor", DisplayFormEditorBuilder.EDITOR_FORM_CONTEXT);
	}

	private HTMLFragment attributesView() {
		return createFormDisplay("rf_dropTarget", DisplayFormEditorBuilder.ATTRIBUTE_FORM_CONTEXT);
	}

	private HTMLFragment createFormDisplay(String cssClass, String member) {
		Config formDisplayConfig = TypedConfiguration.newConfigItem(DirectFormDisplay.Config.class);
		formDisplayConfig.setCssClass(cssClass);
		formDisplayConfig.setDisplayedMember(member);
		DirectFormDisplay formDisplay = new DirectFormDisplay(formDisplayConfig);
		return formDisplay.createView(this, null, noModelKey());
	}

	@Override
	public Command getApplyClosure() {
		Command applyClosure;
		if (getAcceptCommand() != null) {
			Command close = getDialogModel().getCloseAction();
			applyClosure = new CommandChain(checkContextCommand(), getAcceptCommand(), close);
		} else {
			applyClosure = super.getApplyClosure();
		}
		return applyClosure;
	}

	private ResKey noModelKey() {
		return ResKey.fallback(I18NConstants.FORM_EDITOR_DIALOG.key(FormTag.DEFAULT_NO_MODEL_KEY_SUFFIX),
			com.top_logic.layout.form.tag.I18NConstants.NO_MODEL);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		if (_inEditMode) {
			Command applyClosure = getApplyClosure();
			if (applyClosure != null) {
				buttons.add(applyCommandModel(applyClosure));
			}
			buttons.add(cancelCommandModel(getDiscardClosure()));
		}

		buttons.addAll(_additionalCommands);
	}

	private CommandModel cancelCommandModel(Command command) {
		return MessageBox.button(ButtonType.CANCEL, command);
	}

	private CommandModel applyCommandModel(Command command) {
		CommandModel apply = MessageBox.button(ButtonType.OK, command);
		apply.setLabel(com.top_logic.layout.form.component.I18NConstants.SAVE);
		return apply;
	}

	/**
	 * The command to execute when the user accepts the modification. May be <code>null</code>.
	 */
	public Command getAcceptCommand() {
		return _acceptCommand;
	}

	/**
	 * Setter for {@link #getAcceptCommand()}.
	 */
	public void setAcceptCommand(Command acceptCommand) {
		_acceptCommand = acceptCommand;

	}

	/**
	 * Whether the dialog is opened in edit mode.
	 */
	public boolean openInEditMode() {
		return _inEditMode;
	}

	/**
	 * Setter for {@link #openInEditMode()}.
	 */
	public void setOpenInEditMode(boolean inEditMode) {
		_inEditMode = inEditMode;
	}

	/**
	 * The {@link TLStructuredType} to design form for.
	 */
	public TLStructuredType getType() {
		return _type;
	}

	/**
	 * Setter for {@link #getType()}.
	 */
	public void setType(TLStructuredType type) {
		_type = Objects.requireNonNull(type);
		
	}

	/**
	 * The {@link FormDefinition} to modify.
	 */
	public FormDefinition getFormDefinition() {
		return _formDefinition;
	}

	/**
	 * Setter for {@link #getFormDefinition()}.
	 */
	public void setFormDefinition(FormDefinition formDefinition) {
		_formDefinition = formDefinition;
	}

	/**
	 * Sets a copy of the given {@link FormDefinition} or a new {@link FormDefinition} if given
	 * {@link FormDefinition} is <code>null</code>.
	 */
	public void setFormDefinitionCopy(FormDefinition formDefinition) {
		FormDefinition copy;
		if (formDefinition != null) {
			copy = TypedConfiguration.copy(formDefinition);
		} else {
			copy = TypedConfiguration.newConfigItem(FormDefinition.class);
		}
		setFormDefinition(copy);
	}

	/**
	 * The {@link FormDefinitionTemplate}s to offer.
	 */
	public Supplier<? extends List<FormDefinitionTemplate>> getTemplateProvider() {
		return _templateProvider;
	}

	/**
	 * Setter for {@link #getTemplateProvider()}.
	 */
	public void setTemplateProvider(Supplier<? extends List<FormDefinitionTemplate>> templateProvider) {
		_templateProvider = Objects.requireNonNull(templateProvider);
		
	}

	/**
	 * Adds an additional command.
	 * 
	 * @see #fillButtons(List)
	 */
	public void addAdditionalCommand(CommandModel command) {
		_additionalCommands.add(command);
	}

}

