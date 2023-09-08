/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.PieDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Dataset-builder for {@link PieDataset}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class PieDatasetBuilder extends AbstractDatasetBuilder<PieDataset> {

	/**
	 * Enum describing options for TableOrder used when mapping a {@link CategoryDataset} to a
	 * {@link PieDataset}.
	 */
	public enum Order {

		/**
		 * <code>COLUM</code> corresponding to {@link TableOrder#BY_COLUMN}
		 */
		COLUMN(TableOrder.BY_COLUMN),
		/**
		 * <code>ROW</code> corresponding to {@link TableOrder#BY_ROW}
		 */
		ROW(TableOrder.BY_ROW);

		private final TableOrder _order;

		private Order(TableOrder order) {
			_order = order;
		}

		/**
		 * the corresponding JFreeChart {@link TableOrder} for this enum-element.
		 */
		public TableOrder getTableOrder() {
			return _order;
		}
	}

	/**
	 * Config-interface for {@link PieDatasetBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		@Override
		@ClassDefault(PieDatasetBuilder.class)
		public Class<PieDatasetBuilder> getImplementationClass();

		/**
		 * see {@link CategoryToPieDataset}
		 * 
		 * @return the order that indicates if the data is extracted from rows or columns when
		 *         mapping an {@link CategoryDataset} to a {@link PieDataset}.
		 */
		public Order getTableOrder();

		/**
		 * see {@link CategoryToPieDataset}
		 * 
		 * @return the row or column index to map a {@link CategoryDataset} to a {@link PieDataset}.
		 */
		@IntDefault(0)
		public int getIndex();

	}

	/**
	 * Config-Constructor for {@link PieDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public PieDatasetBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	private TableOrder getTableOrder() {
		Order order = getConfig().getTableOrder();
		return order.getTableOrder();
	}

	@Override
	public Class<PieDataset> getDatasetType() {
		return PieDataset.class;
	}

	@Override
	protected PieDataset internalCreateDataset(ChartTree tree) {
		TableOrder order = getTableOrder();
		return new CategoryToPieDataset(CategoryDatasetBuilder.generateCategoryDataset(tree, this), order, getConfig()
			.getIndex());
	}

}
