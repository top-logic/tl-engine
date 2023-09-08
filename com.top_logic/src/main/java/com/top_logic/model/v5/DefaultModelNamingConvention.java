/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5;

import static com.top_logic.basic.generate.CodeUtil.*;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.generate.CodeUtil;

/**
 * Default {@link ModelNamingConvention}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultModelNamingConvention implements ModelNamingConvention {

	private static final String STRUCTURE_TYPE_SUFFIX = "Structure";
	/**
	 * Singleton {@link DefaultModelNamingConvention} instance.
	 */
	public static final DefaultModelNamingConvention INSTANCE = new DefaultModelNamingConvention();

	private DefaultModelNamingConvention() {
		// Singleton constructor.
	}

	@Override
	public String getMetaObjectTypeName(String name) {
		return name;
	}

	@Override
	public  String getNodeTypeName(String nodeName) {
		return toUpperCaseStart(nodeName);
	}

	@Override
	public String getStructureTypeName(String structureName) {
		String typeName = CodeUtil.toUpperCaseStart(structureName);
		if (typeName.endsWith(STRUCTURE_TYPE_SUFFIX)) {
			return typeName;
		} else {
			return typeName + STRUCTURE_TYPE_SUFFIX;
		}
	}

	@Override
	public String getElementTypeName(String name) {
		return CodeUtil.toCamelCase(name);
	}

	@Override
	public String getReferenceSourceName(String elementTypeName) {
		return "memberOf" + CodeUtil.toPluralName(elementTypeName);
	}

	@Override
	public String getEnumName(String fastListName) {
		return CodeUtil.toCamelCase(removeTLPrefix(fastListName));
	}
	
	@Override
	public String getClassifierName(String listName, String listElementName) {
		String simpleListName = removeTLPrefix(listName);
		String classifierName = removeTLPrefix(listElementName);
		
		// Make classifier name local.
		if (classifierName.startsWith(simpleListName)) {
			classifierName = classifierName.substring(simpleListName.length());
			if (StringServices.startsWithChar(classifierName, '.')) {
				classifierName = classifierName.substring(1);
			}
		}
		classifierName = toLowerCaseStart(toCamelCase(classifierName));
		return classifierName;
	}

	private static String removeTLPrefix(String name) {
		if (name.startsWith("tl.")) {
			name = name.substring(3);
		}
		return name;
	}

}