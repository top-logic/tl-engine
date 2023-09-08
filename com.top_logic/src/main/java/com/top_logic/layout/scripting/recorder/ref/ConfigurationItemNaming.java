/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link AbstractModelNamingScheme} for {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationItemNaming
		extends AbstractModelNamingScheme<ConfigurationItem, ConfigurationItemNaming.Name> {

	/**
	 * {@link ModelName} for a {@link ConfigurationItem}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * The {@link ConfigurationItem} this is a name for.
		 */
		@Mandatory
		@Subtypes({})
		ConfigurationItem getItem();

		/**
		 * Setter for {@link #getItem()}.
		 */
		void setItem(ConfigurationItem item);

	}

	/**
	 * Creates a {@link ConfigurationItemNaming}.
	 * 
	 * @see AbstractModelNamingScheme#AbstractModelNamingScheme(Class, Class)
	 */
	public ConfigurationItemNaming() {
		super(ConfigurationItem.class, Name.class);
	}

	@Override
	protected void initName(Name name, ConfigurationItem model) {
		name.setItem(model);
	}

	@Override
	public ConfigurationItem locateModel(ActionContext context, Name name) {
		return name.getItem();
	}

}

