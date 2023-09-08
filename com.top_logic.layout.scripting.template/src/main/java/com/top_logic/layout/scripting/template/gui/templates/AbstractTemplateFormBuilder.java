/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;


import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} allowing to edit templates.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTemplateFormBuilder<T> extends DeclarativeFormBuilder<T, TemplateEditModel> {

	/**
	 * Configuration options for {@link AbstractTemplateFormBuilder}.
	 */
	public interface Config extends DeclarativeFormBuilder.Config {

		/**
		 * {@link XMLPrettyPrinter} configuration for displaying the template body.
		 */
		@ItemDefault
		XMLPrettyPrinter.Config getXMLDisplay();

		/**
		 * {@link XMLPrettyPrinter} configuration for saving the template.
		 */
		@ItemDefault
		XMLPrettyPrinter.Config getXMLStorage();

	}

	/**
	 * Creates a {@link AbstractTemplateFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractTemplateFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TemplateEditModel> getFormType(Object contextModel) {
		return TemplateEditModel.class;
	}

}
