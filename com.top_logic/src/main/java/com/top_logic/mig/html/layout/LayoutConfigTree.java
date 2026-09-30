/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeModel;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * {@link AbstractTLTreeNodeModel} based on {@link LayoutConfigTreeNode}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutConfigTree extends AbstractTLTreeNodeModel<LayoutConfigTreeNode> {

	private LayoutConfigTreeNode _root;

	final Filter<? super Config> _filter;

	private Mapping<CompoundSecurityLayout.Config, CompoundSecurityLayout.Config> _securityDefiningLayouts =
		name -> name;

	/**
	 * Creates a new {@link LayoutConfigTree}.
	 */
	public LayoutConfigTree(LayoutComponent.Config config) {
		this(FilterFactory.trueFilter(), config);
	}

	/**
	 * Creates a new {@link LayoutConfigTree}.
	 */
	public LayoutConfigTree(Filter<? super LayoutComponent.Config> filter, LayoutComponent.Config config) {
		_filter = filter;
		_root = new LayoutConfigRootNode(this, config);
	}

	@Override
	public LayoutConfigTreeNode getRoot() {
		return _root;
	}

	@Override
	public boolean childrenInitialized(LayoutConfigTreeNode parent) {
		return parent._children != null;
	}

	@Override
	public void resetChildren(LayoutConfigTreeNode parent) {
		parent._children = null;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	/**
	 * Determine the {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config} for
	 * the given {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config} the
	 * actually defines the security. name.
	 */
	public CompoundSecurityLayout.Config getSecurityDefiningLayout(CompoundSecurityLayout.Config name) {
		return _securityDefiningLayouts.map(name);
	}

	/**
	 * Sets the mapping for
	 * {@link #getSecurityDefiningLayout(com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config)}.
	 */
	public void setSecurityDefiningLayouts(
			Mapping<CompoundSecurityLayout.Config, CompoundSecurityLayout.Config> mapping) {
		_securityDefiningLayouts = mapping;
		
	}

	private static final class LayoutConfigRootNode extends LayoutConfigTreeNode {

		private LayoutConfigTree _model;

		LayoutConfigRootNode(LayoutConfigTree model, Config businessObject) {
			super(null, null, businessObject);
			_model = model;
		}

		@Override
		public LayoutConfigTree getModel() {
			return _model;
		}
	}

}

