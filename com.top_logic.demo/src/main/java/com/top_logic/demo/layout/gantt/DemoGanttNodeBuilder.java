/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.gantt;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.demo.model.types.DemoTypesCAll;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.reporting.chart.gantt.component.builder.GanttNodeBuilder;
import com.top_logic.reporting.chart.gantt.model.GanttNode;
import com.top_logic.reporting.chart.gantt.model.GanttRow;
import com.top_logic.reporting.chart.gantt.ui.GanttChartCreatorFields;
import com.top_logic.util.Utils;

/**
 * Demo {@link GanttNodeBuilder}. This uses some attributes of {@link DemoTypesA} in the following way:
 *
 * boolean: isFinished
 * date: start date
 * date2: end date
 * person: responsible
 * beacon: state
 * typedWrapper: following {@link DemoTypesA} (implicit dependency "this.date2 --> this.typedWrapper.date")
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DemoGanttNodeBuilder extends GanttNodeBuilder {

	@Override
	protected void preloadData(GanttChartCreatorFields cf, PreloadContext preloadContext, DefaultMutableTLTreeNode rootNode) {
		// nothing to preload
	}

	@Override
	protected boolean isFinished(DefaultMutableTLTreeNode node) {
		Object businessObject = node.getBusinessObject();
		if (businessObject instanceof DemoTypesA) {
			return Utils.getbooleanValue(((DemoTypesA) businessObject).getBoolean());
		}
		return false;
	}


	@Override
	public void handleDates(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas, GanttRow node) {
		Date maxDate = node.setStartDate(DateUtil.createDate(2100, 11, 31));
		node.setEndDate(DateUtil.createDate(1900, 0, 0));
		if (node.getBusinessObject() instanceof DemoTypesA) {
			DemoTypesA businessObject = (DemoTypesA) node.getBusinessObject();
			ResourceProvider res = getResourceProvider();
			boolean hideStartEndLabel = cf.hideStartEndLabel();
			ThemeImage image = Icons.MILESTONE;
			Date start = businessObject.getDate();
			Date end = businessObject.getDate2();
			if (start != null) {
				DemoMilestone ms = new DemoMilestone("Start", businessObject);
				GanttNode startNode = createNodeData(node, ms);
				startNode.setName(hideStartEndLabel ? "" : ms.getName());
				startNode.setDate(start);
				startNode.setGoto(res, businessObject);
				startNode.setTooltip(res.getLabel(start));
				startNode.setImagePath(image);
				node.getNodes().add(startNode);
				MapUtil.addObject(dependingNodeDatas, ms, startNode);
				node.adjustNodeDates(start);
			}

			for (StructuredElement child : businessObject.getChildren()) {
				if (!(child instanceof DemoTypesCAll)) {
					continue;
				}

				Date date = ((DemoTypesCAll) child).getDependentDate();
				if (date == null) {
					continue;
				}

				GanttNode data = createNodeData(node, child);
				data.setName(child.getName());
				data.setDate(date);
				data.setGoto(res, child);
				data.setTooltip(res.getLabel(date));
				data.setImagePath(image);
				node.getNodes().add(data);
				MapUtil.addObject(dependingNodeDatas, child, data);
			}
			if (end != null) {
				DemoMilestone ms = new DemoMilestone("Ende", businessObject);
				GanttNode endNode = createNodeData(node, ms);
				endNode.setName(hideStartEndLabel ? "" : ms.getName());
				endNode.setDate(end);
				endNode.setGoto(res, businessObject);
				endNode.setTooltip(res.getLabel(end));
				endNode.setImagePath(image);
				node.getNodes().add(endNode);
				MapUtil.addObject(dependingNodeDatas, ms, endNode);
				node.adjustNodeDates(end);
			}
		}
		finalAdjustNodeDate(node, maxDate);
	}

	/**
	 * Checks whether the node date is in visible area.
	 */
	protected void finalAdjustNodeDate(GanttRow node, Date maxDate) {
		if (maxDate == null || maxDate.equals(node.getStartDate())) {
			node.setEndDate(node.setStartDate(null));
		}
	}

	@Override
	protected void fillDependencyData(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas) {
		for (GanttRow node : cf.getRows()) {
			for (GanttNode nodeData : node.getNodes()) {
				if (nodeData.getBusinessObject() instanceof DemoMilestone) {
					fillDependencies(cf, dependingNodeDatas, nodeData);
				}
			}
		}
	}

	private void fillDependencies(GanttChartCreatorFields cf, Map<Object, List<GanttNode>> dependingNodeDatas, GanttNode nodeData) {
		DemoMilestone ms = (DemoMilestone) nodeData.getBusinessObject();
		Object holder = ms.getHolder();
		if (holder instanceof DemoTypesA && "Ende".equals(ms.getName())) {
			Object successor = ((DemoTypesA) holder).getTypedWrapper();
			if (successor instanceof DemoTypesA && !successor.equals(holder)) {
				Date date = ((DemoTypesA) successor).getDate();
				if (date != null) {
					List<GanttNode> successorNodes = dependingNodeDatas.get(new DemoMilestone("Start", successor));
					if (CollectionUtil.isEmptyOrNull(successorNodes)) {
						nodeData.setHasInvisibleMSDeps(true);
					}
					else
						for (GanttNode successorNode : successorNodes) {
							nodeData.getNodeDependencies().add(successorNode);
						}
				}
			}
		}
	}

}
