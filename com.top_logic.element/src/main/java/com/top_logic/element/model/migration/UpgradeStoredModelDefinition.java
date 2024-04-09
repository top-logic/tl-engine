/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.migration.model.MigrationUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;

/**
 * {@link MigrationPostProcessor} that patches the stored model XML to be compatible with the
 * current schema.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpgradeStoredModelDefinition implements MigrationPostProcessor {

	@Override
	public void afterMigration(Log log, KnowledgeBase kb) {
		PooledConnection connection = KBUtils.getCurrentContext(kb).getConnection();
		MigrationUtils.modifyTLModel(log, connection, document -> removeModuleContents(log, document));
	}

	private boolean removeModuleContents(Log log, Document tlModel) {
		if (tlModel == null) {
			return false;
		}
		boolean changed = false;
		changed |= removeModuleContent(log, tlModel, "roles");
		changed |= removeModuleContent(log, tlModel, "factory");
		return changed;
	}

	private boolean removeModuleContent(Log log, Document document, String tagName) {
		boolean changed = false;
		NodeList nodes = document.getElementsByTagName(tagName);

		List<Node> nodeList = new ArrayList<>();
		for (int n = nodes.getLength() - 1; n >= 0; n--) {
			Node node = nodes.item(n);
			nodeList.add(node);
		}
		for (Node node : nodeList) {
			Node parentNode = node.getParentNode();
			if (parentNode instanceof Element && ((Element) parentNode).getTagName().equals("module")) {
				log.info(
					"Removed '" + tagName + "' from module '" + ((Element) parentNode).getAttribute("name") + "'.");

				parentNode.removeChild(node);
				changed = true;
			}
		}
		return changed;
	}

}
