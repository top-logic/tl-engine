/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.security.SecurityFilter;
import com.top_logic.model.TLObject;

/**
 * Filter that handles the security for the selection of an {@link TreeComponent}.
 * 
 * <p>
 * This filter decides whether a (business) node of the {@link TreeComponent} can be
 * selected (or not) for security reason.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecuritySelectionFilter extends SecurityFilter {

	/**
	 * Configuration of a {@link SecuritySelectionFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends SecurityFilter.Config<SecuritySelectionFilter> {

		// nothing special here. Currently only for defining implementation class.

	}

	/** @see #getTree() */
	private TreeComponent _tree;

	/**
	 * Creates a new {@link SecuritySelectionFilter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SecuritySelectionFilter}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public SecuritySelectionFilter(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	void setTree(TreeComponent tree) {
		_tree = tree;
	}

	/**
	 * Returns the tree this {@link SecuritySelectionFilter} is created for.
	 */
	protected final TreeComponent getTree() {
		if (_tree == null) {
			throw new IllegalStateException("Tree is not yet initialized.");
		}
		return _tree;
	}

	/**
	 * Handles deletion of the given models.
	 * 
	 * @param models
	 *        see AbstractTreeComponent#receiveModelDeletedEvent(Object, Object)
	 * @param changedBy
	 *        see AbstractTreeComponent#receiveModelDeletedEvent(Object, Object)
	 * 
	 * @see TreeComponent#receiveModelDeletedEvent(Set, Object)
	 */
	protected void handleModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		// nothing to do here
	}

	/**
	 * Handles internal change of the given model.
	 * 
	 * @param model
	 *        see AbstractTreeComponent#receiveModelChangedEvent(Object, Object)
	 * @param changedBy
	 *        see AbstractTreeComponent#receiveModelChangedEvent(Object, Object)
	 * 
	 * @see TreeComponent#receiveModelChangedEvent(Object, Object)
	 */
	protected void handleModelChangedEvent(Object model, Object changedBy) {
		// nothing to do here
	}

	/**
	 * Handles creation of the given model.
	 * 
	 * @param model
	 *        see AbstractTreeComponent#receiveModelCreatedEvent(Object, Object)
	 * @param changedBy
	 *        see AbstractTreeComponent#receiveModelCreatedEvent(Object, Object)
	 * 
	 * @see TreeComponent#receiveModelCreatedEvent(Object, Object)
	 */
	protected void handleModelCreatedEvent(Object model, Object changedBy) {
		// nothing to do here
	}
}

