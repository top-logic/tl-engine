/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.MOAttribute;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LayoutComponent} for editing an existing template.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateEditBuilder extends AbstractTemplateFormBuilder<Object> {

	/**
	 * Creates a {@link TemplateEditBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateEditBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected Class<? extends Object> getModelType() {
		return Object.class;
	}

	@Override
	protected void fillFormModel(TemplateEditModel formModel, Object businessModel) {
		TemplateResource templateResource = IsTemplateSelected.template(businessModel);

		Template template;
		if (templateResource == null) {
			return;
		} else {
			template = templateResource.tryLoad();
		}

		formModel.setName(stripLeadingSlash(templateResource.getResourceSuffix()));

		List<TemplateEditModel.Parameter> parameters = formModel.getParameters();
		for (MOAttribute attribute : template.getParameters()) {
			TemplateEditModel.Parameter parameter =
					TypedConfiguration.newConfigItem(TemplateEditModel.Parameter.class);

			parameter.setName(attribute.getName());
			parameter.setDefault((String) template.getDefaultValue(attribute));

			parameters.add(parameter);
		}

		try {
			formModel.setContent(template.getContent(config().getXMLDisplay()));
		} catch (IOException | SAXException ex) {
			throw new TopLogicException(I18NConstants.ERROR_TEMPLATE_LOADING_FAILED__NAME_ERROR.fill(
				templateResource.getName(), ex.getMessage()), ex);
		}
	}

	private String stripLeadingSlash(String resourceSuffix) {
		return stripLeading('/', resourceSuffix);
	}

	private String stripLeading(char ch, String s) {
		if (s != null && !s.isEmpty() && s.charAt(0) == ch) {
			return s.substring(1);
		}
		return s;
	}

	@Override
	protected boolean modelSupported(Object model, LayoutComponent component) {
		return model == null || IsTemplateSelected.isTemplate(model);
	}

}
