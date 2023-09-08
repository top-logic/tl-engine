/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.AbstractDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.util.KeyCompare;

/**
 * Dataset-builder for {@link CategoryDataset}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class CategoryDatasetBuilder extends AbstractDatasetBuilder<CategoryDataset> {

	/**
	 * Config-interface for {@link AbstractDatasetBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		@Override
		@ClassDefault(CategoryDatasetBuilder.class)
		public Class<CategoryDatasetBuilder> getImplementationClass();
	}

	/**
	 * Config-Constructor for {@link CategoryDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public CategoryDatasetBuilder(InstantiationContext context, Config config) {
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
		return generateCategoryDataset(tree, this);
	}

	/**
	 * Util method to create a CategoryDataset from a ChartTree. A maximum depth of 3 is supported.
	 * 
	 * @param tree
	 *        the universal chart-model to be transformed into a JFreeChart-Dataset
	 * @param builder
	 *        the Dataset-builder providing unique instances used as key (see
	 *        {@link AbstractDatasetBuilder#getUniqueName(Comparable)}
	 * @return a new CategoryDataset initialized with the values from the tree
	 */
	public static CategoryDataset generateCategoryDataset(ChartTree tree, AbstractDatasetBuilder<?> builder) {

		if (tree.getDepth() == 1) {
			return createDataset1D(tree, builder);
		} else {
			return createDataset(tree, builder);
		}
	}

	/**
	 * Util-method that adds the value from a ChartNode to the dataset and also registers this node
	 * so it can be found when generating tooltips or urls.
	 * 
	 * @param dataset
	 *        the {@link Dataset} to add the value to
	 * @param dataKey
	 *        The key to identify the given node in the {@link Dataset}.
	 * @param node
	 *        the ChartNode to be registered for the row- and column-key
	 */
	public static void addValueAndRegisterNode(DefaultCategoryDataset dataset, CategoryDataKey dataKey,
			ChartNode node) {
		Number value = getValue(node);
		dataset.addValue(value, dataKey.getRow(), dataKey.getColumn());
		String id = node.getID();
		node.getTree().registerDataKey(dataKey, id);
	}

	/**
	 * {@link DataKey}-implementation used for {@link CategoryDataset}
	 */
	public static class CategoryDataKey extends AbstractDataKey<Comparable<?>, Comparable<?>> {

		/**
		 * Creates a new {@link CategoryDataKey}
		 * 
		 * @param rowKey
		 *        the row-key in a {@link CategoryDataset}
		 * @param colKey
		 *        the column-key in a {@link CategoryDataset}
		 */
		public CategoryDataKey(Comparable<?> rowKey, Comparable<?> colKey) {
			super(rowKey, colKey);
		}

		/**
		 * The row key.
		 * 
		 * @see DefaultCategoryDataset#addValue(Number, Comparable, Comparable)
		 */
		public final Comparable<?> getRow() {
			return super.getFirst();
		}

		/**
		 * The column key.
		 * 
		 * @see DefaultCategoryDataset#addValue(Number, Comparable, Comparable)
		 */
		public final Comparable<?> getColumn() {
			return super.getSecond();
		}

	}

	/**
	 * a new {@link CategoryDataKey} for the given row- and column-key.
	 */
	public static CategoryDataKey createDataKey(Comparable<?> rowKey, Comparable<?> colKey) {
		return new CategoryDataKey(rowKey, colKey);
	}

	/**
	 * a new {@link CategoryDataKey} identifying the data represented in the given dataset
	 *         by the given series and category.
	 */
	public static CategoryDataKey toDataKey(CategoryDataset dataset, int series, int category) {
		Comparable<?> colKey = dataset.getColumnKey(category);
		Comparable<?> rowKey = dataset.getRowKey(series);
		return createDataKey(rowKey, colKey);
	}

	private static Number getValue(ChartNode node) {
		if (node.isLeaf()) {
			return node.getValue();
		}
		List<ChartNode> children = node.getChildren();
		int count = children.size();
		assert count <= 1;
		if (count == 0) {
			return null;
		} else {
			ChartNode child = CollectionUtil.getFirst(children);
			return child.getValue();
		}
	}

	private static CategoryDataset createDataset1D(ChartTree tree, AbstractDatasetBuilder<?> builder) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		ChartNode root = tree.getRoot();
		Criterion criterion = tree.getCriterion(2);
		if (criterion == null) {
			criterion = tree.getCriterion(1);
		}
		UniqueName columnKey = builder.getUniqueName(criterion.getLabel());
		List<ChartNode> l1Children = new ArrayList<>(root.getChildren());
		Collections.sort(l1Children);
		for (int i = 0; i < l1Children.size(); i++) {
			ChartNode l1Child = l1Children.get(i);

			UniqueName rowKey = builder.getUniqueName(l1Child.getKey());

			CategoryDataKey dataKey = createDataKey(rowKey, columnKey);

			addValueAndRegisterNode(dataset, dataKey, l1Child);
		}
		return dataset;
	}

	private static CategoryDataset createDataset(ChartTree tree, AbstractDatasetBuilder<?> builder) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<List<Comparable<?>>> sortedKeysByLevel = collectSortedKeysByLevel(tree, builder);

		insertCoordinates(dataset, sortedKeysByLevel, builder);
		insertValues(builder, tree.getRoot(), 0, new Comparable<?>[sortedKeysByLevel.size()], dataset);
		
		return dataset;
	}

	public static List<List<Comparable<?>>> collectSortedKeysByLevel(ChartTree tree,
			AbstractDatasetBuilder<?> builder) {
		List<Set<Comparable<?>>> keysByLevel = collectKeys(tree, builder);
		List<List<Comparable<?>>> sortedKeysByLevel = sortSets((List) keysByLevel);
		return sortedKeysByLevel;
	}

	private static void insertCoordinates(DefaultCategoryDataset dataset, List<List<Comparable<?>>> sortedKeysByLevel,
			AbstractDatasetBuilder<?> builder) {
		int levelCnt = sortedKeysByLevel.size();
		Comparable<?>[] zeroCoordinate = new Comparable<?>[levelCnt];
		for (int level = 0; level < levelCnt; level++) {
			List<Comparable<?>> keysByLevel = sortedKeysByLevel.get(level);
			if (keysByLevel.isEmpty()) {
				return;
			}
			zeroCoordinate[level] = keysByLevel.get(0);
		}

		Comparable<?>[] coordinate = new Comparable<?>[levelCnt];
		for (int level = 0; level < levelCnt; level++) {
			System.arraycopy(zeroCoordinate, 0, coordinate, 0, levelCnt);

			List<Comparable<?>> keys = sortedKeysByLevel.get(level);
			for (Comparable<?> key : keys) {
				coordinate[level] = key;

				insertCoordinate(dataset, coordinate, builder);
			}
		}
	}

	private static void insertCoordinate(DefaultCategoryDataset dataset, Comparable<?>[] coordinate,
			AbstractDatasetBuilder<?> builder) {
		CategoryDataKey dataKey = createDataKey(builder, coordinate);
		dataset.addValue(null, dataKey.getRow(), dataKey.getColumn());
	}

	private static void insertValues(AbstractDatasetBuilder<?> builder, ChartNode parent, int level,
			Comparable<?>[] coordinate, DefaultCategoryDataset dataset) {
		
		int nextLevel = level + 1;
		for (ChartNode child : parent.getChildren()) {
			coordinate[level] = child.getKey();
			
			if (nextLevel == coordinate.length) {
				CategoryDataKey dataKey = createDataKey(builder, coordinate);
				addValueAndRegisterNode(dataset, dataKey, child);
			} else {
				insertValues(builder, child, nextLevel, coordinate, dataset);
			}
		}
	}

	private static CategoryDataKey createDataKey(AbstractDatasetBuilder<?> builder, Comparable<?>[] coordinate) {
		Comparable<?> row;
		Comparable<?> column;

		if (coordinate.length <= 1) {
			throw new IllegalArgumentException("Cannot insert coordinates for less than two dimensions.");
		} else {
			column = coordinate[0];
			if (coordinate.length == 2) {
				row = coordinate[1];
			} else {
				row = new Pair<Comparable<?>, Comparable<?>>(coordinate[1], coordinate[2]);
			}
		}

		UniqueName rowKey = builder.getUniqueName(row);
		UniqueName columnKey = builder.getUniqueName(column);

		return createDataKey(rowKey, columnKey);
	}

	private static <T extends Comparable<T>> List<List<T>> sortSets(List<Set<T>> sets) {
		ArrayList<List<T>> result = new ArrayList<>(sets.size());
		for (Set<T> set : sets) {
			result.add(sort(set));
		}
		return result;
	}

	private static <T extends Comparable<T>> List<T> sort(Collection<T> set) {
		ArrayList<T> list = new ArrayList<>(set);
		Collections.sort(list, KeyCompare.INSTANCE);
		return list;
	}

	/**
	 * Create a list with keys for each level in the given {@link ChartTree}.
	 * 
	 * @return A list with keys for each level in the tree. The list at position zero contains a set
	 *         with all keys of level 0.
	 */
	private static List<Set<Comparable<?>>> collectKeys(ChartTree tree, AbstractDatasetBuilder<?> builder) {
		List<Set<Comparable<?>>> keys = new ArrayList<>();
		collectKeysForChildren(tree.getRoot(), builder, keys, 0);
		return keys;
	}
	
	private static void collectKeysForChildren(ChartNode node, AbstractDatasetBuilder<?> builder,
			List<Set<Comparable<?>>> keysByLevel, int level) {
		if (node.isLeaf()) {
			return;
		}

		if (level >= keysByLevel.size()) {
			keysByLevel.add(new HashSet<>());
		}
		Set<Comparable<?>> keys = keysByLevel.get(level);

		List<ChartNode> children = node.getChildren();
		for (int n = 0; n < children.size(); n++) {
			ChartNode child = children.get(n);
			keys.add(child.getKey());

			collectKeysForChildren(child, builder, keysByLevel, level + 1);
		}
	}

}
