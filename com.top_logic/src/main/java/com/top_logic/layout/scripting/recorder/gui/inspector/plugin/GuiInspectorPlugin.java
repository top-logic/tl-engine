/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin;

import org.w3c.dom.Document;

import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProviderWithColon;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateControl;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldAssertionPlugin;

/**
 * A {@link GuiInspectorPlugin} provides a piece of information for the GuiInspector. It is rendered
 * in one or more table rows.
 * 
 * @param <M>
 *        The inspected model type.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class GuiInspectorPlugin<M> {

	private final M _model;

	private final String internalName;

	/**
	 * @param internalName
	 *        The internal name of the FormGroup, in which all UI elements of this
	 *        {@link AssertionPlugin} are contained.
	 */
	public GuiInspectorPlugin(M model, String internalName) {
		_model = model;
		assert internalName != null : "The internal name must not be null!";
		this.internalName = internalName;
	}

	/**
	 * The inspected model element.
	 */
	public M getModel() {
		return _model;
	}

	/**
	 * Register the top-level {@link FormMember}s of this {@link FieldAssertionPlugin} in the given
	 * {@link FormContainer}.
	 */
	public void registerFormMembers(FormContainer container) {
		FormGroup group = createGroup();
		container.addMember(group);
		initGuiElements(group);
	}

	protected abstract void initGuiElements(FormContainer group);

	private FormGroup createGroup() {
		FormGroup group = new FormGroup(internalName, getI18nPrefix());
		group.setControlProvider(createControlProviderForGroup());
		return group;
	}

	/**
	 * Creates the {@link ControlProvider} for the {@link FormGroup} of this
	 * {@link GuiInspectorPlugin}. Override in subclasses that need a different
	 * {@link ControlProvider}.
	 */
	protected ControlProvider createControlProviderForGroup() {
		return new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				FormTemplate formTemplate =
					new FormTemplate(getI18nPrefix(), DefaultFormFieldControlProviderWithColon.INSTANCE, true,
						getFormTemplateDocument());
				return new FormTemplateControl((FormContainer) model, formTemplate);

			}
		};
	}

	/**
	 * Provides the {@link Document} for the {@link FormTemplate} that is used to create the
	 * {@link FormTemplateControl} for this {@link GuiInspectorPlugin}. Override in subclasses that
	 * have another UI structure.
	 */
	protected abstract Document getFormTemplateDocument();

	/**
	 * The resource prefix to use for this plugin.
	 */
	protected abstract ResPrefix getI18nPrefix();

}
