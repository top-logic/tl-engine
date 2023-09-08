/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.filters.HasValueFilter;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.MigrationValueParser;
import com.top_logic.util.Utils;

/**
 * {@link Filter} of {@link RowValue} matching the {@link RowValue} whose attributes have given
 * values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MatcherByValue extends AbstractConfiguredInstance<MatcherByValue.Config> implements Filter<RowValue> {

	/**
	 * Typed configuration interface definition for {@link MatcherByValue}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<MatcherByValue> {

		/**
		 * Definition of the attribute value that a {@link RowValue} must have to be skipped.
		 * 
		 * <p>
		 * If any attribute name matching a pattern and the corresponding value equals the given
		 * value, this filter accepts the value.
		 * </p>
		 */
		@MapBinding(key = "pattern", keyFormat = RegExpValueProvider.class, attribute = "value", valueFormat = DumpValueSpec.Format.class)
		Map<Pattern, DumpValueSpec> getAttributes();

		/**
		 * If <code>true</code>, all attributes matching any any pattern must have the corresponding
		 * value. Otherwise the filter does not match.
		 */
		boolean getNeedMatchAll();
	}

	private Map<Pattern, Object> _attributes;

	/**
	 * Create a {@link MatcherByValue}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public MatcherByValue(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Initialises the {@link MORepository} of this {@link HasValueFilter}.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		_attributes = new HashMap<>();
		MigrationValueParser parser = new MigrationValueParser(types);
		getConfig().getAttributes().entrySet().forEach(entry -> {
			_attributes.put(entry.getKey(), entry.getValue().resolve(parser));
		});
	}

	@Override
	public boolean accept(RowValue anObject) {
		Map<String, Object> values = anObject.getValues();
		if (getConfig().getNeedMatchAll()) {
			for (Entry<Pattern, Object> attributeMatch : _attributes.entrySet()) {
				Pattern pattern = attributeMatch.getKey();
				for (Entry<String, Object> value : values.entrySet()) {
					if (pattern.matcher(value.getKey()).matches()) {
						if (!Utils.equals(value.getValue(), attributeMatch.getValue())) {
							return true;
						}
					}
				}
			}
			return true;
		} else {
			for (Entry<Pattern, Object> attributeMatch : _attributes.entrySet()) {
				Pattern pattern = attributeMatch.getKey();
				for (Entry<String, Object> value : values.entrySet()) {
					if (pattern.matcher(value.getKey()).matches()) {
						if (Utils.equals(value.getValue(), attributeMatch.getValue())) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}
}
