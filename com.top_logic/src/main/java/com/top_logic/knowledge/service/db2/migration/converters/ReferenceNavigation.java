/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.service.db2.migration.Migration;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;

/**
 * Mapping of an {@link ObjectKey} to the value of an attribute of the corresponding object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceNavigation implements Mapping<ObjectKey, Object> {

	/**
	 * Configuration for a {@link ReferenceNavigation}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<Mapping<?, ?>>, TypesConfig {

		/** Name of {@link #getTargetAttribute()}. */
		String TARGET_ATTRIBUTE = "target-attribute";

		/**
		 * Names of the attribute whose value is the result of the mapping.
		 */
		@Name(TARGET_ATTRIBUTE)
		String getTargetAttribute();
	}

	private Set<String> _targetTypes;

	private String _targetAttribute;

	private Map<String, Index> _indexByTargetType;

	private Migration _migration;

	/**
	 * Creates a {@link ReferenceNavigation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReferenceNavigation(InstantiationContext context, Config config) {
		_targetTypes = config.getTypeNames();
		_targetAttribute = config.getTargetAttribute();
	}

	/**
	 * Initialises this {@link ReferenceNavigation} with the {@link Indexer} to use.
	 */
	@Inject
	public void setIndexer(Indexer indexer) {
		_indexByTargetType = new HashMap<>();
		List<String> keyAttributes = Collections.singletonList(Indexer.SELF_ATTRIBUTE);
		List<String> valueAttributes = Collections.singletonList(_targetAttribute);
		for (String targetType : _targetTypes) {
			Index index = indexer.register(targetType, keyAttributes, valueAttributes);
			_indexByTargetType.put(targetType, index);
		}
	}

	/**
	 * Initialises this {@link ReferenceNavigation} with the {@link Migration} which uses it.
	 */
	@Inject
	public void setMigration(Migration migration) {
		_migration = migration;
	}

	@Override
	public Object map(ObjectKey input) {
		if (input == null) {
			return null;
		}
		Index index = _indexByTargetType.get(input.getObjectType().getName());
		if (index == null) {
			_migration.error("Reference to unexpected type in navigation to attribute '" + _targetAttribute
				+ "', expected one of '" + _targetTypes + "', got: " + input);
			return null;
		}
		return index.getValue(input);
	}

}
