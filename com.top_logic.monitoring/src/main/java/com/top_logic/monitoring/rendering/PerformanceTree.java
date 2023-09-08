/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.execution.I18NConstants;

/**
 * {@link TreeComponent} with nodes representing {@link CompoundSecurityLayout} with performance
 * information
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class PerformanceTree extends TreeComponent {

	/**
	 * Configuration for a {@link PerformanceTree}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends TreeComponent.Config {

		@Override
		@ItemDefault
		@ImplementationClassDefault(ViewPerformanceTreeBuilder.class)
		public PolymorphicConfiguration<TreeModelBuilder<Object>> getModelBuilder();
	}

	public PerformanceTree(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected void becomingInvisible() {
		DefaultTreeUINodeModel treeModel = getTreeModel();
		if (treeModel != null) {
			Object rootNode = treeModel.getRoot().getBusinessObject();
			if (rootNode instanceof LayoutComponentNode) {
				resetNode((LayoutComponentNode) rootNode);
			}
		} else {
			/* This may happen in rare case that the component was made visible and then
			 * invisible without validation, e.g. a programmatic call of "make visible" first on
			 * this component and then on a different one. */
		}
		super.becomingInvisible();
	}

	@Override
	public ResKey hideReason(Object potentialModel) {
		if (!PerformanceMonitor.isEnabled()) {
			return I18NConstants.ERROR_FUNCTIONALITY_DISABLED;
		}
		return super.hideReason(potentialModel);
	}

	/** 
	 * Reset node and children
	 * 
	 * @param aNode the node
	 */
	protected void resetNode(LayoutComponentNode aNode) {
		aNode.resetReady();
		for (LayoutComponentNode node : aNode.getChildren()) {
			this.resetNode(node);
		}
	}
}