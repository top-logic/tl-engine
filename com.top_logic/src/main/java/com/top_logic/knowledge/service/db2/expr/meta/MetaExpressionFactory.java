/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

/**
 * BHU: this class
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public class MetaExpressionFactory {

	public static MetaSet metaSet(String name) {
		return new MetaSet(name);
	}

	public static MetaValue metaValue(String name) {
		return new MetaValue(name);
	}

}
