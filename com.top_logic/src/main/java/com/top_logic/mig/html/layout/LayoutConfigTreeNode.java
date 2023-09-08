/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.AbstractTLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * {@link AbstractTLTreeNode} based on {@link LayoutComponent.Config configuration} of
 * {@link LayoutComponent} as user objects.
 * 
 * <p>
 * The children of a {@link LayoutComponent.Config} are the
 * {@link LayoutContainer.Config#getChildConfigurations() actual children} and the
 * {@link LayoutComponent.Config#getDialogs() dialogs}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutConfigTreeNode extends AbstractTLTreeNode<LayoutConfigTreeNode> {

	private Set<BoundCommandGroup> _commandGroups;

	/**
	 * Whether {@link #_securityComponent} has been initialized.
	 */
	private boolean _securityComponentInitialized;

	/**
	 * Value resolved by {@link #getSecurityComponent()}, may be <code>null</code>, even if
	 * {@link #_securityComponentInitialized}.
	 */
	private PersBoundComp _securityComponent;

	List<LayoutConfigTreeNode> _children;
	
	private final LayoutConfigTreeNode _dialogParent;

	LayoutConfigTreeNode(LayoutConfigTreeNode parent, LayoutConfigTreeNode dialogParent,
			LayoutComponent.Config businessObject) {
		super(parent, businessObject);
		_dialogParent = dialogParent;
	}

	private Filter<? super Config> getFilter() {
		return getModel()._filter;
	}

	/**
	 * {@link LayoutConfigTree} of this node.
	 */
	public LayoutConfigTree getModel() {
		return getParent().getModel();
	}

	/**
	 * Root not of {@link #getModel()}.
	 */
	public final LayoutConfigTreeNode getRoot() {
		return getModel().getRoot();
	}

	/**
	 * The represented {@link LayoutComponent.Config configuration}.
	 */
	public final LayoutComponent.Config getConfig() {
		return (LayoutComponent.Config) getBusinessObject();
	}

	/**
	 * Returns the "dialog parent" of this config.
	 * 
	 * <p>
	 * If the configuration lives in a dialog, this method returns the configuration which declares
	 * the top level {@link Config} as dialog.
	 * </p>
	 * 
	 * @return Returns the {@link LayoutConfigTreeNode} for the next {@link Config "dialog parent"},
	 *         or <code>null</code> when the represented configuration is not within a dialog of the
	 *         {@link #getRoot() root} configuration.
	 */
	public LayoutConfigTreeNode getDialogParent() {
		return _dialogParent;
	}

	@Override
	public List<? extends LayoutConfigTreeNode> getChildren() {
		if (_children == null) {
			_children = initChildren();
		}
		return _children;
	}

	private List<LayoutConfigTreeNode> initChildren() {
		List<LayoutConfigTreeNode> children = new ArrayList<>();
		fillChildren(this, children, getConfig());
		return Collections.unmodifiableList(children);
	}


	private void fillChildren(LayoutConfigTreeNode parent, Collection<LayoutConfigTreeNode> treeChildren,
			LayoutComponent.Config config) {
		fillChildren(parent, parent, treeChildren, config.getDialogs());
		if (config instanceof LayoutContainer.Config) {
			List<? extends Config> children = ((LayoutContainer.Config) config).getChildConfigurations();
			fillChildren(parent, parent.getDialogParent(), treeChildren, children);
		}
	}

	private void fillChildren(LayoutConfigTreeNode parent, LayoutConfigTreeNode dialogParent,
			Collection<LayoutConfigTreeNode> treeChildren, Collection<? extends LayoutComponent.Config> configs) {
		for (LayoutComponent.Config config : configs) {
			if (getFilter().accept(config)) {
				treeChildren.add(new LayoutConfigTreeNode(parent, dialogParent, config));
			} else {
				fillChildren(parent, treeChildren, config);
			}
		}
	}
	
	/**
	 * The set of all {@link BoundCommandGroup} for all {@link CommandHandler} in the represented
	 * {@link LayoutComponent.Config}.
	 * 
	 * <p>
	 * The returned value is not <code>null</code>, unmodifiable, and cached.
	 * </p>
	 */
	public Set<BoundCommandGroup> getCommandGroups() {
		Set<BoundCommandGroup> groups = _commandGroups;
		if (groups == null) {
			groups = findCommandGroups();
			_commandGroups = groups;
		}
		return groups;

	}

	private Set<BoundCommandGroup> findCommandGroups() {
		Set<BoundCommandGroup> out = new HashSet<>();
		LayoutUtils.addCommandGroups(out, getConfig());
		return Collections.unmodifiableSet(out);
	}

	/**
	 * Returns the {@link PersBoundComp} for the represented {@link LayoutComponent.Config}.
	 * 
	 * @return May be <code>null</code>, if there is none.
	 */
	public PersBoundComp getSecurityComponent() {
		if (!_securityComponentInitialized) {
			_securityComponent = SecurityComponentCache.getSecurityComponent(getConfig());
			_securityComponentInitialized = true;
		}
		return _securityComponent;

	}

}

