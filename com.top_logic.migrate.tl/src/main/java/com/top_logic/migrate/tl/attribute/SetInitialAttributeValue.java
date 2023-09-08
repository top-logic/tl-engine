/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.service.db2.migration.filters.HasValueFilter;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.MigrationValueParser;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;

/**
 * Rewriter setting the configured values as initial values in {@link ObjectCreation} of the regared
 * types.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetInitialAttributeValue extends Rewriter {

	/**
	 * Configuration of {@link SetInitialAttributeValue}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/**
		 * The configured initial values.
		 */
		@MapBinding(key = "attribute")
		Map<String, DumpValueSpec> getInitialValues();

		/**
		 * Setter for {@link #getInitialValues()}
		 */
		void setInitialValues(Map<String, DumpValueSpec> values);

	}

	private Map<String, Object> _valueMapping;

	/**
	 * Creates a new {@link SetInitialAttributeValue}.
	 */
	public SetInitialAttributeValue(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Initialises the {@link MORepository} of this {@link HasValueFilter}.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		_valueMapping = new HashMap<>();
		MigrationValueParser migration = new MigrationValueParser(types);
		for (Entry<String, DumpValueSpec> entry : getConfig().getInitialValues().entrySet()) {
			_valueMapping.put(entry.getKey(), entry.getValue().resolve(migration));
		}
	}

	@Override
	protected Object processCreateObject(ObjectCreation event) {
		event.getValues().putAll(_valueMapping);
		return super.processCreateObject(event);
	}

}

