/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.form.FormMember;

/**
 * Providers for input elements in {@link FormGroup}s corresponding to tree
 * nodes in {@link FormTree}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NodeGroupInitializer {
	
	/**
	 * A {@link NodeGroupInitializer} which does nothing.
	 */
	public static final NodeGroupInitializer EMPTY_GROUP_INITIALIZER = new NodeGroupInitializer() {
		
		@Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
            // nothing is done here
		}
	}; 

	/**
	 * Create members in the given {@link FormGroup} that corresponds to the
	 * given tree node.
	 * 
	 * TODO rename to populate FormGroup or such.
	 * 
	 * @param nodeGroup
	 *        The group to initialize with {@link FormMember}s
	 * @param node
	 *        a node in the {@link FormTree#getTreeModel()}
	 *        
	 *        
	 */
	void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node);

}
