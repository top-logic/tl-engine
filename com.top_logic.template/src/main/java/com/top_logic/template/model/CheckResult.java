/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.List;
import java.util.Map;

import com.top_logic.template.tree.TemplateNode;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class CheckResult {
	
	private final TemplateNode node;
	
	private final List<String> references;
	
	private final Map<String, String> ids;

	private final Map<String, TemplateArea> templateAreas;
	
	public CheckResult(TemplateNode aNode, List<String> someReferences, Map<String, String> someIds, Map<String, TemplateArea> someAreas) {
		this.node = aNode;
		this.references = someReferences;
		this.ids = someIds;
		this.templateAreas = someAreas;
	}

	public TemplateNode getNode() {
		return this.node;
	}
	
	public Map<String, String> getIds() {
		return this.ids;
	}
	
	public List<String> getReferences() {
		return this.references;
	}

	public Map<String, TemplateArea> getTemplateAreas() {
		return templateAreas;
	}
}
