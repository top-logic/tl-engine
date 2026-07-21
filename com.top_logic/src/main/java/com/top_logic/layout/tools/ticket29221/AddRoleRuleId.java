/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket29221;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.tools.XMLRewriter;

/**
 * Migration tool assigning a stable {@code id} to role-rule configurations that do not have one
 * yet (ticket #29221).
 *
 * <p>
 * Since ticket #29221 the {@code id} of a role rule is a mandatory configuration property and the
 * key of the enclosing role-rules configuration (see
 * {@code com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig}). Existing
 * configurations computed the id at runtime and therefore did not specify it. This tool walks all
 * XML files below the given directory and adds a freshly generated {@link UUID} as {@code id}
 * attribute to every role-rule element that lacks one.
 * </p>
 *
 * <p>
 * A role-rule element is a {@code <rule>}, {@code <inheritance-rule>} or {@code <singleton-rule>}
 * whose parent element is a {@code <rules>}, {@code <role-rules>} or {@code <security-parents>}
 * container. The {@code <security-parents>} container holds the same role-rule configurations (see
 * {@code com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig}, ticket #29088)
 * and therefore needs the same {@code id}. Elements that already have an {@code id} are left
 * untouched, and files without any change are not rewritten.
 * </p>
 */
public class AddRoleRuleId extends XMLRewriter {

	private static final Set<String> RULE_TAGS = Set.of("rule", "inheritance-rule", "singleton-rule");

	private static final Set<String> CONTAINER_TAGS = Set.of("rules", "role-rules", "security-parents");

	private static final String ID_ATTRIBUTE = "id";

	@Override
	public void handleFile(String fileName) throws Exception {
		handleFile(new File(fileName));
	}

	private void handleFile(File file) throws Exception {
		if (!file.exists()) {
			System.err.println("File does not exist: " + file.getPath());
			return;
		}
		if (file.isDirectory()) {
			for (File child : FileUtilities.listFiles(file)) {
				handleFile(child);
			}
		} else {
			process(file);
		}
	}

	private void process(File file) {
		if (!file.getName().endsWith(".xml")) {
			return;
		}

		try {
			Document document = parse(file);
			boolean changed = addIds(document.getDocumentElement());
			if (changed) {
				dump(document, file);
			}
		} catch (Exception ex) {
			System.err.println("ERROR processing file: " + file);
			ex.printStackTrace(System.err);
		}
	}

	private boolean addIds(Element element) {
		boolean changed = false;
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element childElement) {
				changed |= addIds(childElement);
			}
		}
		if (isRoleRule(element) && !element.hasAttribute(ID_ATTRIBUTE)) {
			element.setAttribute(ID_ATTRIBUTE, UUID.randomUUID().toString());
			changed = true;
		}
		return changed;
	}

	private static boolean isRoleRule(Element element) {
		if (!RULE_TAGS.contains(element.getTagName())) {
			return false;
		}
		if (element.getParentNode() instanceof Element parent) {
			return CONTAINER_TAGS.contains(parent.getTagName());
		}
		return false;
	}

	/**
	 * Entry-point of the {@link AddRoleRuleId} tool.
	 */
	public static void main(String[] args) throws Exception {
		new AddRoleRuleId().runMain(args);
	}

}
