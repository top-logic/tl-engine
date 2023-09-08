/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.common.tree.TreeAxis;
import com.top_logic.reporting.common.tree.TreeInfo;
import com.top_logic.reporting.common.tree.TreeInfoNode;
import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.TreeAxisTimeSeriesChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon.KeyProvider;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.datasource.DataContext;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;

/**
 * {@link ChartDataSource} for status report chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class TreeInfoDataSource implements ChartDataSource<ComponentDataContext> {

	/** Resource prefix for UI elements. */
	public static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(TreeInfoDataSource.class);

	/**
	 * Config interface for {@link TreeInfoDataSource}.
	 */
	public interface Config extends PolymorphicConfiguration<TreeInfoDataSource> {

		/** @see #getTreeDepth() */
		public String TREE_DEPTH = "tree-depth";

		/**
		 * Gets the data source.
		 */
		@InstanceFormat
		public ChartDataSource<DataContext> getDataSource();

		@Override
		@ClassDefault(TreeInfoDataSource.class)
		public Class<TreeInfoDataSource> getImplementationClass();

		/**
		 * Gets the maximum element count
		 */
		@IntDefault(400)
		public int getMaxElementCount();

		/**
		 * Flag whether to suppress name.
		 * 
		 * NOTE: please set this only to true if only status report of ONE element is shown. E.g.
		 * Project overview information. Then the name is suppressed.
		 */
		@BooleanDefault(false)
		public boolean getSuppressName();

		/**
		 * Gets the icon count.
		 */
		@IntDefault(4)
		public int getIconCount();

		/**
		 * Gets the tree depth.
		 */
		@IntDefault(2)
		@Name(TREE_DEPTH)
		public int getTreeDepth();

		/**
		 * @see #getTreeDepth()
		 */
		public void setTreeDepth(int depth);

		/**
		 * Gets the tree filter.
		 */
		@InstanceFormat
		@InstanceDefault(TrueFilter.class)
		public Filter<? super StructuredElement> getTreeFilter();

		/**
		 * @see #getTreeFilter()
		 */
		public void setTreeFilter(Filter<? super StructuredElement> filter);

		/**
		 * Gets the max tree depth.
		 */
		@IntDefault(-1)
		public int getMaxTreeDepth();

		/**
		 * @see #getMaxTreeDepth()
		 */
		public void setMaxTreeDepth(int depth);

		/**
		 * Gets the visible number of objects.
		 */
		@IntDefault(10)
		public int getVisibleNumberOfObjects();
	}

	private final Config _config;

	private int _nodeCount;

	/**
	 * Config-Constructor for {@link TreeInfoDataSource}.
	 */
	public TreeInfoDataSource(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	/**
	 * Gets the configuration.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * Return an icon index for the given information.
	 * 
	 * @param aCurrent
	 *        The structured element to be displayed, must not be <code>null</code>.
	 * @param aDepth
	 *        The depth of the represented node.
	 * @return The requested image index (to be used in array returned from
	 *         {@link TreeAxisTimeSeriesChartBuilder#getIcons()}).
	 */
	protected int getIconIndex(StructuredElement aCurrent, int aDepth) {
		return 1;
	}

	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {
		Collection<? extends Object> rawData = getConfig().getDataSource().getRawData(context);
		if (rawData.isEmpty()) {
			return Collections.emptyList();
		}
		assert rawData.size() == 1;
		StructuredElement model = (StructuredElement) CollectionUtil.getFirst(rawData);
		return Arrays.asList(getTreeInfos(model));
	}

	/**
	 * This method returns a array of tree infos. The tree infos are needed to draw the tree axis.
	 */
	private TreeInfo[] getTreeInfos(StructuredElement anElement) {
		TreeInfoNode info = getTreeInfoNode(anElement);

		if (info == null) {
			return new TreeInfo[0];
		}

		TreeInfo[] infos = info.getReverseArray();
		int maxCount = getConfig().getMaxElementCount();

		if ((maxCount > -1) && (infos.length > maxCount)) {
			TreeInfo[] result = new TreeInfo[maxCount + 1];
			int diff = infos.length - maxCount;

			System.arraycopy(infos, diff, result, 1, maxCount);

			for (int pos = 1; pos <= maxCount; pos++) {
				result[pos].setParent(1 + result[pos].getParent() - diff);
			}

			result[0] = new TreeInfo("...", 3);
			result[0].setDepth(1);
			result[0].setParent(maxCount);

			return result;
		} else {
			return infos;
		}
	}

	private TreeInfoNode getTreeInfoNode(StructuredElement anElement) {
		TreeInfoNode root = null;

		int treeDepth = getConfig().getTreeDepth();
		Filter<? super StructuredElement> filter = getConfig().getTreeFilter();

		if (anElement != null) {
			String url = "";
			String tooltip = "";
			String name = anElement.getName();
			int index = 0;
			StructuredElement element = anElement;

			if (getConfig().getSuppressName()) {
				name = "";
			}

			while ((index < 4) && !(element.getParent() == null)) {
				index++;
				element = element.getParent();
			}

			root =
				new TreeInfoNode(new TreeInfo(name, getIconIndex(anElement, index), tooltip, url,
					tooltip, url, anElement));
			_nodeCount = 1;

			root.getInfo().setDepth(0);

			convertTree(root, anElement, treeDepth, filter);
		}

		return root;
	}

	/**
	 * This method converts the structured tree into a representation which the {@link TreeAxis} can
	 * understand.
	 */
	public void convertTree(TreeInfoNode aNode, StructuredElement aElement, int treeDepth,
			Filter<? super StructuredElement> filter) {
		if (aNode.getDepth() < treeDepth) {
			int maxIcons = getConfig().getIconCount();

			for (Object object : aElement.getChildren(filter)) {
				StructuredElement current = (StructuredElement) object;
				String url = "";
				String tooltip = "";
				TreeInfoNode node =
					new TreeInfoNode(new TreeInfo(current.getName(), getIconIndex(current,
						Math.min(treeDepth + 1, maxIcons)), tooltip, url, tooltip, url, current));

				node.getInfo().setDepth(aNode.getDepth() + 1);

				if (_nodeCount < ValueAxis.MAXIMUM_TICK_COUNT) {
					aNode.addChild(node);
					_nodeCount++;
				}

				convertTree(node, current, treeDepth, filter);
			}
		}
	}

	public static class DemoKeyProvider extends KeyProvider {

		@Override
		public Object getKey(ChartNode node) {
			if (node == null) {
				return null;
			}
			List<?> objects = node.getObjects();
			Object first = CollectionUtil.getFirst(objects);
			if (first == null) {
				return null;
			}
			if (first instanceof Wrapper) {
				Object floatValue = ((Wrapper) first).getValue("float");
				if (floatValue instanceof Number) {
					double value = ((Number) floatValue).doubleValue();
					if (value > 0.5) {
						return Integer.valueOf(1);
					}
				}
			}
			return Integer.valueOf(0);
		}
	}

}

