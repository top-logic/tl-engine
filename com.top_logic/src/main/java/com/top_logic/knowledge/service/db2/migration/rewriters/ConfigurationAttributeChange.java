/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * {@link ItemChangeRewriter} changing the value of an configuration values attribute
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationAttributeChange extends ConfiguredItemChangeRewriter<ConfigurationAttributeChange.Config> {

	/**
	 * Typed configuration interface definition for {@link ConfigurationAttributeChange}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("configuration-change")
	public interface Config extends PolymorphicConfiguration<ConfigurationAttributeChange>, AttributeConfig {
		
		/**
		 * Configuration of the mapping from the old value to the new value.
		 * 
		 * <p>
		 * The {@link Mapping} must fulfil the following conditions.
		 * </p>
		 * 
		 * <ul>
		 * <li>The {@link Mapping} returns the same {@link ConfigurationItem} (the same java
		 * {@link Object}) to indicate that the value must not be changed.</li>
		 * <li>The given value must no be modified.</li>
		 * <li>The {@link Mapping} returns a new {@link ConfigurationItem} (or <code>null</code>)
		 * which is the new value for the attribute.</li>
		 * </ul>
		 */
		PolymorphicConfiguration<Mapping<? super ConfigurationItem, Object>> getMapping();

	}

	private final Mapping<? super ConfigurationItem, Object> _mapping;

	/**
	 * Create a {@link ConfigurationAttributeChange}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfigurationAttributeChange(InstantiationContext context, Config config) {
		super(context, config);
		_mapping = context.getInstance(config.getMapping());
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		ConfigurationItem currentValue = (ConfigurationItem) values.get(attribute);
		Object mappedValue = _mapping.map(currentValue);
		if (mappedValue != currentValue) {
			ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);
			update.setValue(attribute, event.getOldValue(attribute), mappedValue);
			event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
		}
	}

}

