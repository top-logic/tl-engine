/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.component.ComponentModelNamingScheme.ComponentModelName;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.AbstractStaticInfoPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.DebugResourceProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SingleValueAssertionPlugin} to display details for the model of a {@link LayoutComponent}
 * and create an assertion for the model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentModelAssertionPlugin extends SingleValueAssertionPlugin<LayoutComponent, SelectField> {

	/**
	 * Creates a new {@link ComponentModelAssertionPlugin}.
	 * 
	 * @param model
	 *        The component holding the model.
	 */
	public ComponentModelAssertionPlugin(LayoutComponent model) {
		super(model, false, "componentModel");
	}

	@Override
	protected void initAssertionContents(FormContainer group) {
		super.initAssertionContents(group);

		CommandField inspectButton =
			AbstractStaticInfoPlugin.createInspectButton(getI18nPrefix(), getModel().getMainLayout(),
				getModel().getModel());
		if (inspectButton != null) {
			// Inspect button should be visible
			inspectButton.setInheritDeactivation(false);
			group.addMember(inspectButton);
			getExpectedValueField().setControlProvider(new ControlProvider() {

				@Override
				public Control createControl(Object model, String style) {
					BlockControl blockControl = new BlockControl();
					blockControl.addChild(new SelectionControl((SelectField) model));
					blockControl.addChild(new ButtonControl(inspectButton));
					return blockControl;
				}
			});
		}

	}

	@Override
	protected SelectField createValueField(String name) {
		SelectField field =
			FormFactory.newSelectField(name, componentsModel(), !FormFactory.MULTIPLE, FormFactory.IMMUTABLE);
		field.setOptionLabelProvider(DebugResourceProvider.INSTANCE);
		field.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		return field;
	}

	@Override
	protected Object getInitialValue() {
		return componentsModel();
	}

	private List<Object> componentsModel() {
		return Collections.singletonList(getModel().getModel());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.COMPONENT_MODEL;
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ComponentModelName componentModel = TypedConfiguration.newConfigItem(ComponentModelName.class);
		componentModel.setComponent(ModelResolver.buildModelName(getModel()));
		Object expectedModel = getExpectedValueField().getSingleSelection();
		return ActionFactory.equalsCheck(expectedModel, componentModel, getComment());
	}
}