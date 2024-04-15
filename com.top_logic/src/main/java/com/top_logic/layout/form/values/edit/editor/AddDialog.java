/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.Iterator;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Utility for opening a dialog for filling a configuration.
 * 
 * <p>
 * Dialog-based editing is used, when a configuration must be (partially) completed before adding to
 * the main configuration. This is especially the case for configurations indexed in maps and
 * configurations used for instantiating instances.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class AddDialog extends AbstractFormDialogBase {

	private final EditorFactory _editorFactory;

	private final CreateHandler _createHandler;

	private final PropertyDescriptor _property;

	private final ConfigurationItem _elementModel;

	public AddDialog(EditorFactory editorFactory, CreateHandler createHandler, PropertyDescriptor property,
			ConfigurationItem elementModel) {
		super(createDialogModel(property));

		_editorFactory = editorFactory;
		_createHandler = createHandler;
		_property = property;
		_elementModel = elementModel;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		/**
		 * Create a copy of the editor factory, since the factory caches all fields. The fields of
		 * the add dialog must go out of scope, if the dialog is closed.
		 */
		EditorFactory editorFactory = _editorFactory.copy();
		editorFactory.initEditorGroup(context, _elementModel);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		Command addOperation = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext addContext) {
				FormContext group = getFormContext();
				boolean ok = group.checkAll();
				if (!ok) {
					return AbstractApplyCommandHandler.createErrorResult(group);
				}
				addNewElement();
				return getDialogModel().getCloseAction().executeCommand(addContext);
			}
		};

		CommandModel okButton = MessageBox.button(ButtonType.OK, addOperation);
		buttons.add(okButton);
		getDialogModel().setDefaultCommand(okButton);
		buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
	}

	@Override
	protected HTMLFragment createView() {
		return Fragments.div(FormConstants.FORM_BODY_CSS_CLASS,
			DefaultFormFieldControlProvider.INSTANCE.createControl(getFormContext()));
	}

	@Override
	public HandlerResult open(WindowScope windowScope) {
		boolean hasInput = false;
		for (Iterator<? extends FormField> it = getFormContext().getDescendantFields(); it.hasNext();) {
			FormField field = it.next();
			if (field.isActive()) {
				hasInput = true;
				break;
			}
		}

		if (!hasInput) {
			// Short-cut, do not open the dialog, but immediately add the new value.
			addNewElement();
			return HandlerResult.DEFAULT_RESULT;
		}

		return super.open(windowScope);
	}

	void addNewElement() {
		ConfigurationItem newConfig = TypedConfiguration.copy(_elementModel);
		Object newEntry;
		if (_property.isInstanceValued()) {
			newEntry =
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
					.getInstance((PolymorphicConfiguration<?>) newConfig);
		} else {
			newEntry = newConfig;
		}
		_createHandler.addElement(newEntry, newConfig);
	}

	private static DefaultDialogModel createDialogModel(PropertyDescriptor property) {
		LayoutData dialogLayout =
			new DefaultLayoutData(
				DisplayDimension.dim(50, DisplayUnit.PERCENT), 100,
				DisplayDimension.dim(70, DisplayUnit.PERCENT), 100,
				Scrolling.AUTO);

		DisplayValue dialogTitle =
			new ResourceText(I18NConstants.ADD_ELEMENT__PROPERTY.fill(Labels.propertyLabelKey(property)));

		final DefaultDialogModel dialogModel =
			new DefaultDialogModel(dialogLayout, dialogTitle, true, true, null);
		return dialogModel;
	}

}