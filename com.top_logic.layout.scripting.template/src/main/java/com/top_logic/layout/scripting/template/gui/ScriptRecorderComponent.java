/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import static com.top_logic.layout.scripting.template.gui.ScriptRecorderTree.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionField;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.util.Utils;

/**
 * Displays an {@link ApplicationAction} as XML. If the user edits the action, the changes are
 * parsed and the new {@link ApplicationAction} is displayed.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptRecorderComponent extends FormComponent {

	/**
	 * Configuration options for {@link ScriptRecorderComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * {@link XMLPrettyPrinter} configuration for displaying actions as XML.
		 */
		@ItemDefault
		XMLPrettyPrinter.Config getXMLDisplay();

	}

	/** Called from ScriptRecorderComponent.jsp */
	@CalledFromJSP
	public static final String FIELD_NAME_EDIT_ACTION = "editAction";

	/**
	 * CSS class for the multi-line text area for XML source display.
	 */
	public static final String ACTION_FIELD_CSS_CLASS = "scriptRecorderActionXmlField";

	private final ValueListener editActionListener = new ValueListener() {

		@Override
		@SuppressWarnings("synthetic-access")
		public void valueChanged(FormField editActionFieldViaParameter, Object oldActionXml, Object newActionXml) {
			reactOnNewActionXml((ApplicationAction) newActionXml);
		}

	};

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptRecorderComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptRecorderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(this);
		FormField editableActionField =
			ActionField.newActionField(FIELD_NAME_EDIT_ACTION, false, false, null, ((Config) _config).getXMLDisplay());
		editableActionField.setControlProvider(new CodeEditorControl.CP(CodeEditorControl.MODE_XML));
		formContext.addMember(editableActionField);
		updateActionFields(editableActionField);
		editableActionField.addValueListener(editActionListener);
		editableActionField.setCssClasses(ACTION_FIELD_CSS_CLASS);
		return formContext;
	}

	private void reactOnNewActionXml(ApplicationAction newAction) {
		MutableTLTreeNode<?> model = (MutableTLTreeNode<?>) getModel();
		if (Utils.equals(action(model), newAction)) {
			return;
		}

		model.setBusinessObject(newAction);
		fireModelModifiedEvent(model, this);
	}

	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
		boolean result = super.receiveModelChangedEvent(aModel, changedBy);

		if (aModel == getModel()) {
			removeFormContext();
		}
		return result;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof TLTreeNode<?>;
	}

	@Override
	protected void updateForm() {
		// Do not remove form context to prevent flicker.
		// super.updateForm();

		if (hasFormContext()) {
			updateActionFields(getFormContext().getField(FIELD_NAME_EDIT_ACTION));
		}
	}

	@Override
	protected void handleNewModel(Object newModel) {
		// Do not invalidate form.
		//
		// super.handleNewModel(newModel);
	}

	private void updateActionFields(FormField editableActionField) {
		ApplicationAction newAction = ScriptRecorderTree.action(getModel());
		editableActionField.setValue(newAction);
		if (newAction == null) {
			editableActionField.setImmutable(true);
		} else {
			if (ScriptRecorderTree.isDerived((TLTreeNode<?>) getModel())) {
				editableActionField.setDisabled(true);
			} else {
				editableActionField.setDisabled(false);
			}
		}
	}

}
