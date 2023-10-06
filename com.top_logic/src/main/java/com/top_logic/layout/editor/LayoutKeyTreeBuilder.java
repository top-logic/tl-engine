/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Builds a tree describing the component layout by its layout keys.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutKeyTreeBuilder extends DefaultTreeTableBuilder {

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		String parentLayoutKey = (String) node.getBusinessObject();

		try {
			List<DefaultTreeTableNode> children = new ArrayList<>();

			for (String layoutKey : getReferencedLayoutKeys(parentLayoutKey)) {
				children.add(createNode(node.getModel(), node, layoutKey));
			}

			return children;
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.LAYOUT_RESOLVE_ERROR.fill(parentLayoutKey), exception);
		}
	}

	private Collection<String> getReferencedLayoutKeys(String layoutKey) throws ConfigurationException {
		PersonManager r = PersonManager.getManager();
		Person currentPerson = TLContext.currentUser();

		return LayoutTemplateUtils.getReferencedLayoutKeys(currentPerson, layoutKey);
	}

}
