/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.dob.MOAttribute;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.template.model.TemplateMOAttribute;
import com.top_logic.util.Resources;

/**
 * {@link FormComponent} that displays all parameters of a template and allows to create template
 * argument values.
 * 
 * @see TemplateInstantiationCommand
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateParameterComponent extends FormComponent {

	private Template _template;

	/**
	 * Creates a {@link TemplateParameterComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateParameterComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		if (_template == null) {
			return null;
		}

		FormContext result = new FormContext(this);
		List<MOAttribute> attributes = _template.getType().getAttributes();
		if (attributes.isEmpty()) {
			result.addMember(FormFactory.newDisplayField("info", I18NConstants.NO_TEMPLATE_PARAMETERS));
		} else {
			for (MOAttribute attribute : attributes) {
				StringField field = FormFactory.newStringField(attribute.getName());

				String label = CodeUtil.toUpperCaseStart(attribute.getName());
				if (((TemplateMOAttribute) attribute).hasDefaultValue()) {
					Object defaultValue = _template.getDefaultValue(attribute);
					if (StringServices.isEmpty(defaultValue)) {
						label = Resources.getInstance().getString(I18NConstants.EMPLTY_DEFAULT_LABEL__LABEL.fill(label));
					} else {
						label = Resources.getInstance().getString(I18NConstants.DEFAULT_LABEL__LABEL_DEFAULT.fill(label, defaultValue));
					}
				} else {
					field.setMandatory(true);
				}
				field.setLabel(label);

				result.addMember(field);
			}
		}
		
		return result;
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);

		removeFormContext();
		loadTemplate(newModel);
	}

	private void loadTemplate(Object model) {
		TemplateResource templateResource = IsTemplateSelected.template(model);
		if (templateResource == null) {
			_template = null;
		} else {
			_template = templateResource.tryLoad();
		}
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return IsTemplateSelected.isTemplate(object);
	}

}
