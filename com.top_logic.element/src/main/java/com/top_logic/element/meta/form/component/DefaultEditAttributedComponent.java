/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.gui.layout.form.DeleteSimpleWrapperCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Simple implementation of {@link EditAttributedComponent}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultEditAttributedComponent extends EditAttributedComponent {

	/**
	 * Configuration of the {@link DefaultEditAttributedComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends EditAttributedComponent.Config {

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "attributedEditor";

		/** Name of {@link #getMetaElementName()} property. */
		String META_ELEMENT_NAME_PROPERTY_NAME = "metaElementName";

		@Override
		@ClassDefault(DefaultEditAttributedComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		/**
		 * Configuration of the name of the {@link TLClass} an object must have to be edited by
		 * a {@link DefaultEditAttributedComponent}.
		 */
		@Name(META_ELEMENT_NAME_PROPERTY_NAME)
		@Mandatory
		String getMetaElementName();

		/**
		 * Name of the {@link CommandHandler} used to delete the edited object.
		 */
		@Override
		@StringDefault(DeleteSimpleWrapperCommandHandler.COMMAND)
		String getDeleteCommand();

		/**
		 * Name of the {@link CommandHandler} used to apply changes to the edited object.
		 */
		@Override
		@StringDefault(DefaultApplyAttributedCommandHandler.COMMAND_ID)
		String getApplyCommand();

	}


	private final String _metaElementName;

	/**
	 * Creates a new {@link DefaultEditAttributedComponent} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultEditAttributedComponent}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DefaultEditAttributedComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_metaElementName = config.getMetaElementName();
	}

	@Override
	protected String getMetaElementName() {
		return _metaElementName;
	}

}

