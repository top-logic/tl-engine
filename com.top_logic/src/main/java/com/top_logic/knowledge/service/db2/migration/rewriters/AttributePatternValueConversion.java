/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.convert.EventRewriter;

/**
 * {@link EventRewriter} that changes values of attributes in {@link ItemChange}
 * events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributePatternValueConversion extends Rewriter {

	/**
	 * Configuration options for {@link AttributePatternValueConversion}.
	 */
	public interface Config extends Rewriter.Config {

		/** Configuration name for value of {@link #getValueMapping()}. */
		String VALUE_MAPPING_NAME = "value-mapping";

		/** Configuration name for value of {@link #getAttributePattern()}. */
		String ATTRIBUTE_PATTERN_NAME = "attribute-pattern";

		/**
		 * Pattern matching the name of an attribute to apply the conversion to.
		 */
		@Name(ATTRIBUTE_PATTERN_NAME)
		@Mandatory
		String getAttributePattern();

		/**
		 * Setter for {@link #getAttributePattern()}
		 */
		void setAttributePattern(String value);

		/**
		 * Conversion operation to process the source value with.
		 */
		@Name(VALUE_MAPPING_NAME)
		@Mandatory
		PolymorphicConfiguration<Mapping<Object, ?>> getValueMapping();

		/**
		 * Setter for {@link #getValueMapping()}
		 */
		void setValueMapping(PolymorphicConfiguration<Mapping<Object, ?>> value);

	}

	private final Pattern _attributePattern;

	private final Mapping<Object, ?> _valueMapping;

	/**
	 * Creates a {@link AttributePatternValueConversion} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributePatternValueConversion(InstantiationContext context, Config config) {
		super(context, config);

		_attributePattern = Pattern.compile(config.getAttributePattern());
		_valueMapping = context.getInstance(config.getValueMapping());
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		rewriteValues(event.getValues());
		return APPLY_EVENT;
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			rewriteValues(oldValues);
		}
		return super.processUpdate(event);
	}

	private void rewriteValues(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			String attributeName = entry.getKey();
			if (_attributePattern.matcher(attributeName).matches()) {
				Object oldValue = entry.getValue();
				Object newValue = _valueMapping.map(oldValue);
				entry.setValue(newValue);
			}
		}
	}

}