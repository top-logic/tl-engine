/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.gantt;

import java.util.Date;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.reporting.chart.gantt.component.builder.GanttNodeBuilder;
import com.top_logic.reporting.chart.gantt.model.GanttRow;
import com.top_logic.reporting.chart.gantt.ui.AbstractGanttChartCreator;
import com.top_logic.reporting.chart.gantt.ui.GanttChartCreatorFields;

/**
 * Demo gantt chart creator.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DemoGanttChartCreator extends AbstractGanttChartCreator {

	@Override
	protected GanttNodeBuilder getNodeBuilder() {
		return new DemoGanttNodeBuilder();
	}

	@Override
	protected TreeBuilder<DefaultMutableTLTreeNode> getTreeBuilder(GanttChartCreatorFields cf) {
		return new DemoGanttTreeBuilder();
	}

	@Override
	protected Date createTargetDate(GanttChartCreatorFields cf) {
		return null;
	}

	@Override
	protected Object getDateLinkObjectFromNode(GanttRow aNode, boolean isStartDateColumn) {
		return null;
	}

	@Override
	protected Object getResponsibleFromBusinessObject(Object aBO) {
		if (aBO instanceof DemoTypesA) {
			return ((DemoTypesA) aBO).getPerson();
		}
		return null;
	}

	@Override
	protected FastListElement getStateFromBusinessObject(Object businessObject) {
		if (businessObject instanceof DemoTypesA) {
			return (FastListElement) CollectionUtil.getSingleValueFrom(((DemoTypesA) businessObject)
				.getChecklistSingle());
		}
		return null;
	}

	@Override
	protected Object getStateLinkObjectFromBusinessObject(Object aBO) {
		return aBO;
	}

}
