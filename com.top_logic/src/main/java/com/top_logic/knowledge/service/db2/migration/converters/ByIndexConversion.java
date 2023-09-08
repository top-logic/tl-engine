/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import java.util.Collections;

import com.google.inject.Inject;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;

/**
 * {@link AbstractReferenceConversion} that find the referenced key by lookup in an index.
 * 
 * <p>
 * It is expected that an item is uniquely identified by an attribute value. This
 * {@link AbstractReferenceConversion} expects that the dump value is the value of such an object.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ByIndexConversion extends AbstractReferenceConversion {

	/**
	 * Configuration of a {@link ByIndexConversion}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ReferenceConversion> {

		/** Name of {@link #getTypeName()} attribute. */
		String TYPE = "type";

		/** Name of {@link #getAttribute()} attribute. */
		String ATTRIBUTE = "attribute";

		/**
		 * Name of the type whose {@link #getAttribute() key attribute} uniquely identifies the
		 * object.
		 */
		@Name(TYPE)
		String getTypeName();

		/**
		 * Name of the attribute whose value uniquely identifies the object.
		 */
		@Name(ATTRIBUTE)
		String getAttribute();
	}

	private final String _typeName;

	private final String _keyAttribute;

	private Index _index;

	/**
	 * Creates a new {@link ByIndexConversion} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ByIndexConversion}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ByIndexConversion(InstantiationContext context, Config config) throws ConfigurationException {
		_typeName = config.getTypeName();
		_keyAttribute = config.getAttribute();
	}

	/**
	 * Initialises this {@link ByIndexConversion} with the {@link Indexer} to use.
	 */
	@Inject
	public void initIndexer(Indexer indexer) {
		_index = indexer.register(_typeName, Collections.singletonList(_keyAttribute),
			Collections.singletonList(Indexer.SELF_ATTRIBUTE));
	}

	@Override
	public ObjectKey convertReference(ItemChange event, String dumpValue) {
		return (ObjectKey) _index.getValue(dumpValue);
	}

}

