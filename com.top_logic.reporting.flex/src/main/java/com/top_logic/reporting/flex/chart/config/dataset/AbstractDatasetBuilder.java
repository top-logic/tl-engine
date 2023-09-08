/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Default-implementation of {@link DatasetBuilder}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractDatasetBuilder<D extends Dataset> implements DatasetBuilder<D>,
		ConfiguredInstance<AbstractDatasetBuilder.Config> {

	/**
	 * Base config-interface for {@link AbstractDatasetBuilder}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface Config extends PolymorphicConfiguration<AbstractDatasetBuilder<?>> {
		// base interface
	}

	private final Config _config;

	private final Map<Comparable<?>, UniqueName> _map;

	/**
	 * Config-Constructor for {@link AbstractDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractDatasetBuilder(InstantiationContext context, Config config) {
		_config = config;
		_map = new HashMap<>();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public D getDataset(ChartTree tree) {
		_map.clear();
		return internalCreateDataset(tree);
	}

	/**
	 * @param tree
	 *        the {@link ChartTree} to create the {@link Dataset} for.
	 * @return the {@link Dataset} to be used in a {@link JFreeChart}.
	 */
	protected abstract D internalCreateDataset(ChartTree tree);

	/**
	 * Use this method to get a unique instance of {@link UniqueName} for a given comparable.
	 * 
	 * @param comp
	 *        the comparable to get a UniqueName for.
	 * @return the UniqueName for the given key - lazy initialized.
	 */
	protected UniqueName getUniqueName(Comparable<?> comp) {
		if (_map.containsKey(comp)) {
			return _map.get(comp);
		}
		UniqueName res = new UniqueName(comp);
		_map.put(comp, res);
		return res;
	}

}
