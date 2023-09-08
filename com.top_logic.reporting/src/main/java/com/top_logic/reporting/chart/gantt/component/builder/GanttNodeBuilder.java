/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.reporting.chart.gantt.model.GanttNode;
import com.top_logic.reporting.chart.gantt.model.GanttRow;
import com.top_logic.reporting.chart.gantt.ui.GanttChartCreatorFields;
import com.top_logic.util.Utils;

/**
 * The GanttNodeBuilder creates the {@link GanttRow}s shown in the Gantt tree and the {@link GanttNode}
 * shown in the chart.
 * 
 * @author <a href=mailto:jes@top-logic.com>Jens Schäfer</a>
 */
public abstract class GanttNodeBuilder {

	/**
	 * Builds the tree to show in the gantt chart.
	 *
	 * @param cf
	 *        the {@link GanttChartCreatorFields} to fill the nodes list for
	 * @param rootNode
	 *        the root node of the tree to show.
	 */
	public void build(GanttChartCreatorFields cf, DefaultMutableTLTreeNode rootNode) {
		PreloadContext context = new PreloadContext();
		preloadData(cf, context, rootNode);
		// Cache to find depending nodes easier while building the tree.
		Map<Object, List<GanttNode>> dependingNodeDatas = new HashMap<>();
		build(cf, dependingNodeDatas, rootNode, cf.getMaxDepth(), 0, 0, 0);
		fillDependencyData(cf, dependingNodeDatas);
	}

	/**
	 * Builds the (sub)tree to show in the gantt chart.
	 */
	protected void build(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas, DefaultMutableTLTreeNode treeNode,
			int maxDepth, int currentDepth, int nodeIndex, int lastNodeIndex) {
		boolean isFinished = isFinished(treeNode);
		boolean isRoot = treeNode.equals(treeNode.getModel().getRoot());
		boolean isStartElement = Utils.equals(treeNode.getBusinessObject(), cf.getFilterSettings().getModel());
		if (cf.hideFinishedElements() && isFinished && !isRoot && !isStartElement) {
			// Don't handle finished elements if configured so.
			return;
		}
		// Handle this node
		boolean showRoot = cf.showRoot();
		if (showRoot || !isRoot) {
			int visibleDepth = showRoot ? currentDepth : (currentDepth - 1);
			boolean isFirst = nodeIndex == 0;
			boolean isLast = nodeIndex == lastNodeIndex;
			cf.getRows().add(createNode(cf, dependingNodeDatas, treeNode, isFinished, visibleDepth, isFirst, isLast));
		}
		// Handle children recursively
		if (currentDepth < maxDepth) {
			List<DefaultMutableTLTreeNode> children = treeNode.getChildren();
			int childNodeIndex = 0, lastChildIndex = children.size() - 1;
			for (DefaultMutableTLTreeNode child : children) {
				build(cf, dependingNodeDatas, child, maxDepth, currentDepth + 1, childNodeIndex++, lastChildIndex);
			}
		}
	}

	/**
	 * Creates a {@link GanttRow} for the given business object.
	 */
	protected GanttRow createNode(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas, DefaultMutableTLTreeNode treeNode,
			boolean isFinished, int depth, boolean isFirst, boolean isLast) {
		Object nodeBO = treeNode.getBusinessObject();
		GanttRow node = createNode(nodeBO);
		node.setDepth(depth);
		node.setFirst(isFirst);
		node.setLast(isLast);
		node.setDisabled(isFinished);
		ResourceProvider resourceProvider = getResourceProvider();
		node.setName(resourceProvider.getLabel(nodeBO));
		node.setGoto(resourceProvider, nodeBO);
		node.setNodeImagePath(resourceProvider.getImage(nodeBO, Flavor.DEFAULT));
		node.setTooltip(resourceProvider.getTooltip(nodeBO));
		handleDates(cf, dependingNodeDatas, node);
		return node;
	}

	/**
	 * Creates an additional collision avoiding {@link GanttRow} from the given original row.
	 */
	public GanttRow createCollisionAvoidingNode(GanttRow originalRow) {
		GanttRow newRow = new GanttRow(originalRow.getBusinessObject());
		newRow.setProgrammatic(true);
		newRow.setDepth(originalRow.getDepth());
		newRow.setLast(originalRow.isLast());
		originalRow.setLast(false);
		newRow.setFirst(false);
		newRow.setDisabled(originalRow.isDisabled());
		newRow.setName(StringServices.EMPTY_STRING);
		return newRow;
	}

	/**
	 * Gets the {@link ResourceProvider} for generating links, labels and tooltips for the given object.
	 */
	protected ResourceProvider getResourceProvider() {
		return MetaResourceProvider.INSTANCE;
	}

	/**
	 * Pre-loads all relevant data to get faster access to all information shown by the gantt chart.
	 */
	protected abstract void preloadData(GanttChartCreatorFields cf, PreloadContext preloadContext, DefaultMutableTLTreeNode rootNode);

	/**
	 * Fills the dependency data of NodeDatas.
	 */
	protected abstract void fillDependencyData(GanttChartCreatorFields cf,
			Map<Object, List<GanttNode>> dependingNodeDatas);

	/**
	 * Checks whether the given business object is finished or not.
	 */
	protected abstract boolean isFinished(DefaultMutableTLTreeNode node);

	/**
	 * Creates {@link GanttNode}s for each date element of the given object and adds them into the given nodes nodeDates list.
	 */
	public abstract void handleDates(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas, GanttRow node);

	/**
	 * Create a node representing the given business object.
	 * 
	 * @param businessObject
	 *        The business object to create the node for, must not be <code>null</code>.
	 * @return The requested node, never <code>null</code>.
	 */
	public GanttRow createNode(Object businessObject) {
		return new GanttRow(businessObject);
	}

	/**
	 * Creates a {@link GanttNode} for the given node and the given business object.
	 */
	protected GanttNode createNodeData(GanttRow node, Object businessObject) {
		return new GanttNode(node, businessObject);
	}

}
