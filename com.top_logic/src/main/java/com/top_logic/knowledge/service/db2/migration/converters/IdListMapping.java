/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.google.inject.Inject;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapper;

/**
 * {@link Mapping} that updates comma-separated list of identifiers with target identifiers.
 * 
 * @see IdMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IdListMapping implements Mapping<String, String> {

	/**
	 * Configuration of an {@link IdListMapping}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<IdListMapping> {

		/** Configuration name of the value {@link #isTypedIds()}. */
		String TYPED_IDS_NAME = "typed-ids";

		/**
		 * Whether the ids are in the form &lt;type&gt;:&lt;id&gt;. In this case the type is not
		 * converted, only the id.
		 */
		@Name(TYPED_IDS_NAME)
		boolean isTypedIds();

	}

	private IdMapper _idMapper;

	private final Config _config;

	/**
	 * Creates a new {@link IdListMapping} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link IdListMapping}.
	 */
	public IdListMapping(InstantiationContext context, Config config) {
		_config = config;
	}

	/**
	 * Initialises the {@link IdMapper} for this {@link IdListMapping}.
	 */
	@Inject
	public void initIdMapper(IdMapper idMapper) {
		_idMapper = idMapper;
	}

	@Override
	public String map(String input) {
		if (StringServices.isEmpty(input)) {
			return input;
		}

		boolean typedIds = _config.isTypedIds();

		StringBuilder buffer = new StringBuilder();

		int pos = 0;
		boolean append = false;
		while (pos < input.length()) {
			int nextIndex = input.indexOf(',', pos);
			String dumpIdString;
			if (nextIndex < 0) {
				dumpIdString = input.substring(pos).trim();
				pos = input.length();
			} else {
				dumpIdString = input.substring(pos, nextIndex).trim();
				pos = nextIndex + 1;
			}

			if (append) {
				buffer.append(",");
			}

			String typeName;
			if (typedIds) {
				int typeSep = dumpIdString.indexOf(':');
				typeName = dumpIdString.substring(0, typeSep).trim();
				dumpIdString = dumpIdString.substring(typeSep + 1).trim();
			} else {
				typeName = null;
			}

			TLID dumpId = StringID.fromExternalForm(dumpIdString);
			TLID targetId = _idMapper.mapId(dumpId);
			if (targetId != null) {
				if (typeName != null) {
					buffer.append(typeName);
					buffer.append(':');
				}
				buffer.append(IdentifierUtil.toExternalForm(targetId));
				append = true;
			}
		}

		return buffer.toString();
	}

}
