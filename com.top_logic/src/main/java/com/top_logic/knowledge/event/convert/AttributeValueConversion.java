/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * {@link EventRewriter} that changes values of attributes in {@link ItemChange}
 * events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeValueConversion extends AbstractMappingRewriter {

	/**
	 * Configuration options for {@link AttributeValueConversion}.
	 */
	@TagName("attribute-value-rewrite")
	public interface Config<I extends AttributeValueConversion> extends PolymorphicConfiguration<I>, TypesConfig {

		/**
		 * Filter of types.
		 * 
		 * <p>
		 * Only events targeting those types are processed.
		 * </p>
		 */
		@Override
		Set<String> getTypeNames();
		
		/**
		 * The source attribute to read the conversion source value from.
		 */
		@Name("source-attribute")
		@Mandatory
		@NonNullable
		String getSourceAttribute();

		/**
		 * The target attribute to store the converted value to.
		 */
		@Name("target-attribute")
		@Mandatory
		@NonNullable
		String getTargetAttribute();

		/**
		 * Conversion operation to process the source value with.
		 */
		@Name("value-mapping")
		@DefaultContainer
		@Mandatory
		PolymorphicConfiguration<Mapping<Object, ?>> getValueMapping();

	}

	/** Constant to use to indicate that the attribute of any type must be renamed. */
	public static final String ANY_TYPE = null;

	private final String sourceName;
	private final String targetName;
	private final Mapping<Object, ?> valueMapping;

	/**
	 * Type names to rewrite.
	 * 
	 * An empty set is used to indicate that all types should be rewritten.
	 */
	private final Set<String> typeNames;

	/**
	 * Creates a {@link AttributeValueConversion} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeValueConversion(InstantiationContext context, Config<?> config) {
		typeNames = config.getTypeNames();
		sourceName = config.getSourceAttribute();
		targetName = config.getTargetAttribute();
		valueMapping = context.getInstance(config.getValueMapping());
	}

	/**
	 * Creates a {@link AttributeValueConversion}.
	 * 
	 * @param typeName
	 *        The type to rename the attribute from, or {@link #ANY_TYPE} to rename the attribute of
	 *        any type.
	 * @param valueMapping
	 *        The {@link Mapping} to apply to all values of the attribute identified by the given
	 *        name.
	 * @param attributeName
	 *        The attribute name, the mapping should be applied to.
	 */
	public AttributeValueConversion(String typeName, String attributeName, Mapping<Object, ?> valueMapping) {
		this(typeName, attributeName, attributeName, valueMapping);
	}

	/**
	 * Creates a {@link AttributeValueConversion}.
	 * 
	 * @param typeName
	 *        The type to rename the attribute from, or {@link #ANY_TYPE} to rename the attribute of
	 *        any type.
	 * @param sourceName
	 *        The attribute name, the source values should be taken from.
	 * @param targetName
	 *        The attribute name, the mapped values should be stored in.
	 * @param valueMapping
	 *        The {@link Mapping} to apply to all values of the attribute identified by the given
	 *        source name.
	 */
	public AttributeValueConversion(String typeName, String sourceName, String targetName, Mapping<Object, ?> valueMapping) {
		this.typeNames = typeName == ANY_TYPE ? Collections.<String> emptySet() : Collections.singleton(typeName);
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.valueMapping = valueMapping;
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
		if (!typeNames.isEmpty()) {
			if (!typeNames.contains(input.getObjectType().getName())) {
				// Do not change value because type does not match.
				return;
			}
		}
		Map<String, Object> values = input.getValues();
		Object oldValue = values.get(sourceName);
		if (oldValue != null || values.containsKey(sourceName)) {
			Object newValue = valueMapping.map(oldValue);
			values.put(targetName, newValue);
		}
	}

}