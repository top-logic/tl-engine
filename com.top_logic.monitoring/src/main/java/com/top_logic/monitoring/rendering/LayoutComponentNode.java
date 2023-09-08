/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.admin.component.DescendantComponentFinder;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryAggregated;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Node representing a CompoundSecurityLayout
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class LayoutComponentNode extends AbstractMutableTLTreeNode<LayoutComponentNode> {
	private long maxlocalTime;

	private long maxSubTime;

	private long sumTimes;

	boolean isReady;

	/**
	 * Constructs a new {@link DefaultMutableTLTreeNode}.
	 * 
	 * @param model
	 *        The model of this node. See {@link #getModel()}.
	 * @param parent
	 *        The parent node. See {@link #getParent()}.
	 * @param businessObject
	 *        The user object of this node. See {@link #getBusinessObject()}.
	 * 
	 * @see AbstractMutableTLTreeNode#AbstractMutableTLTreeNode(AbstractMutableTLTreeModel, AbstractMutableTLTreeNode, Object)
	 */
	public LayoutComponentNode(AbstractMutableTLTreeModel<LayoutComponentNode> model,
			LayoutComponentNode parent,
			Object businessObject) {
		super(model, parent, businessObject);
	}

	/** 
	 * Reset timing info
	 */
	public void resetReady() {
		this.isReady = false;
	}

	/**
	 * Look up the first (button or grid) component which is a child of the given component.
	 * 
	 * @param aComponent
	 *        The component to start the search at.
	 * @return The found component or <code>null</code>.
	 */
	public LayoutComponent findButtonOrDirectComponent(LayoutComponent aComponent, boolean acceptFormComp) {
		return DescendantComponentFinder.findButtonOrDirectComponent(aComponent, acceptFormComp);
	}

	/** 
	 * Collect the info for the node
	 */
	protected void getReady() {
		if (!isReady) {
			// Get local base data
			LayoutComponent theComp = this.getComponent();
			LayoutComponent theButton = findButtonOrDirectComponent(theComp, false);
			if (theButton == null) {
				theButton = findButtonOrDirectComponent(theComp, true);
			}

			if (theButton != null) {
				LayoutComponent theRealComp = theButton;
				if (theRealComp != null) {
					theComp = theRealComp;
				}
			}

			ComponentName compName = theComp.getName();
			Map<Long, Set<PerformanceDataEntryAggregated>> performanceData =
				PerformanceMonitor.getInstance().getPerformanceData(null, null, null, null, null, null, null,
					Collections.singletonList(compName.qualifiedName()));
			Map<Long, Set<PerformanceDataEntryAggregated>> performanceDataGroupedBy =
				PerformanceMonitor.getInstance().getPerformanceDataGroupedBy(performanceData, false, false, true,
					true);

			// Find local max
			this.maxlocalTime = 0;
			this.sumTimes  = 0;
			Long theCurrentInterval = PerformanceMonitor.getInstance().getCurrentInterval();
			for (Long theInterval : performanceDataGroupedBy.keySet()) {
				long theIntervalDiff = theInterval - theCurrentInterval;
				if (theIntervalDiff > -PerformanceMonitor.getMaxCacheIntervals()) {
					for (PerformanceDataEntryAggregated performanceDataEntry : performanceDataGroupedBy
						.get(theInterval)) {
						long maxTime = performanceDataEntry.getMaxTime();
						if (maxTime > this.maxlocalTime) {
							this.maxlocalTime = maxTime;
						}
						
						this.sumTimes += performanceDataEntry.getTotalTime();
					}
				}
			}
			
			// Handle children
			this.maxSubTime  = 0;
			for (LayoutComponentNode node : this.getChildren()) {
				long subMax = node.getMaxSubTime();
				if (subMax > this.maxSubTime) {
					this.maxSubTime = subMax;
				}
				subMax = node.getMaxLocalTime();
				if (subMax > this.maxSubTime) {
					this.maxSubTime = subMax;
				}
				
				this.sumTimes += node.getSumTimes();
			}
		}

		isReady = true;
	}


	/** 
	 * the max time of this
	 */
	public long getMaxLocalTime() {
		getReady();

		return maxlocalTime;
	}

	/** 
	 * the max time of children
	 */
	public long getMaxSubTime() {
		getReady();

		return maxSubTime;
	}

	/** 
	 * the summed time
	 */
	public long getSumTimes() {
		getReady();

		return sumTimes;
	}

	/** 
	 * the layout
	 */
	public LayoutComponent getComponent() {
		return (LayoutComponent) this.getBusinessObject();
	}
}