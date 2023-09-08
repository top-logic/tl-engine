/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapper;

/**
 * {@link RowTransformer} that replaces source {@link TLID} attribute values by corresponding target
 * {@link TLID}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IDMappingTransformer implements RowTransformer {

	/**
	 * Configuration of a {@link IDMappingTransformer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<RowTransformer> {

		/**
		 * Configuration of the attributes whose values are {@link TLID}.
		 */
		@MapBinding(valueFormat = CommaSeparatedStringSet.class, key = "type", attribute = "attributes")
		Map<String, Set<String>> getMappings();

	}

	private Map<String, Set<String>> _mappings;

	private IdMapper _mapper;

	/**
	 * Creates a new {@link IDMappingTransformer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link IDMappingTransformer}.
	 */
	public IDMappingTransformer(InstantiationContext context, Config config) {
		_mappings = config.getMappings();
	}

	/**
	 * Initialises this {@link IDMappingTransformer} with the given {@link IdMapper}.
	 */
	@Inject
	public void initIdMapper(IdMapper mapper) {
		_mapper = mapper;
	}

	@Override
	public void transform(RowValue row, RowWriter out) {
		Set<String> idAttributes = _mappings.get(row.getTable().getName());
		if (idAttributes != null) {
			Map<String, Object> values = row.getValues();
			for (String idAttribute : idAttributes) {
				TLID id = (TLID) values.get(idAttribute);
				if (id != null) {
					values.put(idAttribute, _mapper.mapOrCreateId(id));
				}
			}
		}
		out.write(row);
	}

}

