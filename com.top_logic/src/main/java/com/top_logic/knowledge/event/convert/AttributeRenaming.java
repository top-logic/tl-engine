/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * {@link EventRewriter} that changes the name of attributes in
 * {@link ItemChange} events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AttributeRenaming extends AbstractMappingRewriter
		implements ConfiguredInstance<AttributeRenaming.Config> {

	/**
	 * Configuration of the {@link AttributeRenaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<AttributeRenaming>, TypesConfig {

		/**
		 * The names of all types to rename attributes.
		 * 
		 * <p>
		 * If the given set is not empty, only the attributes of the types with that names are
		 * renamed.
		 * </p>
		 */
		@Override
		Set<String> getTypeNames();

		/**
		 * Whether not just for the given types, but also for all sub types the attribute must be
		 * renamed.
		 */
		boolean getIncludeSubtypes();

		/**
		 * The attribute name to rename.
		 */
		String getSourceAttribute();

		/**
		 * Setter for {@link #getSourceAttribute()}.
		 */
		void setSourceAttribute(String attributeName);

		/**
		 * The attribute name to rename to.
		 */
		String getTargetAttribute();

		/**
		 * Setter for {@link #getTargetAttribute()}.
		 */
		void setTargetAttribute(String attributeName);
	}

	private Config _config;

	private Set<String> _types;

	/**
	 * Creates a {@link AttributeRenaming} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeRenaming(InstantiationContext context, Config config) {
		_config = config;
		_types = config.getTypeNames();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Initialises the types to process.
	 */
	@Inject
	public void initMORepository(MORepository typeSystem) {
		if (getConfig().getIncludeSubtypes()) {
			HashSet<String> newTypes = new HashSet<>(_types);
			typeSystem.getMetaObjects().forEach(mo -> includeSubTypeName(newTypes, mo));
			_types = newTypes;
		}
	}

	private void includeSubTypeName(Set<String> newTypes, MetaObject mo) {
		if (newTypes.contains(mo.getName())) {
			return;
		}
		if (!(mo instanceof MOClass)) {
			return;
		}
		MOClass superType = ((MOClass) mo).getSuperclass();
		if (superType == null) {
			return;
		}
		includeSubTypeName(newTypes, superType);
		if (newTypes.contains(superType.getName())) {
			newTypes.add(mo.getName());
		}
	}

	@Override
	protected ChangeSet mapChangeSet(ChangeSet input) {
		for (ItemDeletion deletion : input.getDeletions()) {
			modify(deletion);
		}
		for (ObjectCreation creation : input.getCreations()) {
			modify(creation);
		}
		for (ItemUpdate update : input.getUpdates()) {
			modify(update);
		}
		return input;
	}

	private void modify(ItemChange input) {
		Set<String> typeNames = _types;
		if (!typeNames.isEmpty()) {
			if (!typeNames.contains(input.getObjectType().getName())) {
				// Do not rename because type does not match.
				return;
			}
		}
		Map<String, Object> values = input.getValues();
		String sourceName = getConfig().getSourceAttribute();
		if (values.containsKey(sourceName)) {
			Object b2Value = values.remove(sourceName);
			values.put(getConfig().getTargetAttribute(), b2Value);
		}
	}

	/**
	 * Creates an {@link AttributeRenaming} with given values.
	 */
	public static AttributeRenaming newAttributeRenaming(String typeName, String sourceName, String targetName) {
		Config newConfigItem = TypedConfiguration.newConfigItem(AttributeRenaming.Config.class);
		newConfigItem.setTypeNames(Collections.singleton(typeName));
		newConfigItem.setSourceAttribute(sourceName);
		newConfigItem.setTargetAttribute(targetName);
		return TypedConfigUtil.createInstance(newConfigItem);
	}

}