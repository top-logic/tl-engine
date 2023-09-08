/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * {@link RowTransformer} that applies to specified types.
 * 
 * @see AbstractRowTransformer.Config#getTypeNames()
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractRowTransformer<C extends AbstractRowTransformer.Config<?>>
		extends AbstractConfiguredInstance<C> implements RowTransformer {

	/**
	 * Configuration of an {@link AbstractRowTransformer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends AbstractRowTransformer<?>> extends PolymorphicConfiguration<I>, TypesConfig {

		// sum interface

	}

	private Set<String> _typeNames;

	/**
	 * Creates a new {@link AbstractRowTransformer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractRowTransformer}.
	 */
	public AbstractRowTransformer(InstantiationContext context, C config) {
		super(context, config);
		_typeNames = config.getTypeNames();
	}

	@Override
	public void transform(RowValue row, RowWriter out) {
		if (_typeNames.contains(row.getTable().getName())) {
			internalTransform(row, out);
		} else {
			out.write(row);
		}
	}

	/**
	 * Actual {@link #transform(RowValue, RowWriter)} implementation for rows that belong to one of
	 * the configured types.
	 */
	protected abstract void internalTransform(RowValue row, RowWriter out);

}

