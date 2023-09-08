/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder.CategoryDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * This dataset-builder creates column- and row-keys by grouping a configured set out of all
 * possible keys. Example: {@link ChartTree} with a depth of 3 and keys A, B, C on level one, X, Y,
 * Z on level two and 1, 2, 3 on level three. By grouping leven one and two as column-key and using
 * level three as row-key we can reduce the size to the number of dimensions e.g. to fit a
 * bar-chart:
 * 
 * <pre>
 * Column-keys: (A,X), (A,Y), (A,Z), (B,X), (B,Y), (B,Z), (C,X), (C,Y), (C,Z). 
 * Row-keys: 1, 2, 3
 * </pre>
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class GenericCategoryDatasetBuilder extends AbstractDatasetBuilder<CategoryDataset> {

	/**
	 * Config-interface for {@link GenericCategoryDatasetBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config, ConfigPart {

		@Override
		@ClassDefault(GenericCategoryDatasetBuilder.class)
		public Class<GenericCategoryDatasetBuilder> getImplementationClass();

		/**
		 * the indexes of the keys to use as column-key
		 */
		@Format(CommaSeparatedStrings.class)
		@Name("column-keys")
		public List<String> getColKeyIndexes();

		/**
		 * the indexes of the keys to use as row-key
		 */
		@Format(CommaSeparatedStrings.class)
		@Name("row-keys")
		public List<String> getRowKeyIndexes();
	}

	/**
	 * Config-Constructor for {@link GenericCategoryDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public GenericCategoryDatasetBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<CategoryDataset> getDatasetType() {
		return CategoryDataset.class;
	}

	@Override
	protected CategoryDataset internalCreateDataset(ChartTree tree) {
		return generateCategoryDataset(tree);
	}

	private CategoryDataset generateCategoryDataset(ChartTree tree) {

		Comparable<?>[] array = new Comparable[tree.getDepth()];

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		ChartNode root = tree.getRoot();
		parseCategoryChildren(dataset, root.getChildren(), 0, array);
		return dataset;
	}

	private void parseCategoryChildren(DefaultCategoryDataset result, List<ChartNode> children, int level,
			Comparable<?>[] keys) {
		for (int i = 0; i < children.size(); i++) {
			ChartNode child = children.get(i);
			Comparable<?> key = child.getKey();
			keys[level] = key;
			if (child.isLeaf()) {

				UniqueName rowKey = getRowKey(keys);
				UniqueName colKey = getColumnKey(keys);

				CategoryDataKey dataKey = CategoryDatasetBuilder.createDataKey(rowKey, colKey);
				CategoryDatasetBuilder.addValueAndRegisterNode(result, dataKey, child);
			} else {
				List<ChartNode> theChildren = child.getChildren();
				parseCategoryChildren(result, theChildren, level + 1, keys);
			}
		}
	}

	private UniqueName getColumnKey(Comparable<?>[] keys) {
		return getKey(getConfig().getColKeyIndexes(), keys, this);
	}

	private UniqueName getRowKey(Comparable<?>[] keys) {
		return getKey(getConfig().getRowKeyIndexes(), keys, this);
	}

	/**
	 * Util method to create a (column or row)-key based on the given indexes in the given array of
	 * {@link Comparable}s.
	 * 
	 * @param indexes
	 *        list of numbers indicating the indexes that should be used for the key
	 * @param keys
	 *        array of all keys that can be used to create a part-key
	 * @param builder
	 *        the Dataset-builder providing unique instances used as key (see
	 *        {@link AbstractDatasetBuilder#getUniqueName(Comparable)}
	 * @return new {@link UniqueName} based on the keys from the indexes
	 */
	public static UniqueName getKey(List<String> indexes, Comparable<?>[] keys, AbstractDatasetBuilder<?> builder) {
		assert !indexes.isEmpty();
		int size = indexes.size();
		if (size == 1) {
			int pos = Integer.valueOf(CollectionUtil.getFirst(indexes)).intValue();
			UniqueName res = builder.getUniqueName(keys[pos]);
			return res;
		}
		List<Comparable<?>> keyList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int pos = Integer.valueOf(indexes.get(i)).intValue();
			Comparable<?> comparable = keys[pos];
			keyList.add(comparable);
		}
		Tuple tuple = TupleFactory.newTuple(keyList);
		return builder.getUniqueName(tuple);
	}

}
