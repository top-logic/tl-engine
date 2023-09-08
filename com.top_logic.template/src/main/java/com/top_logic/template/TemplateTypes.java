/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.template.util.SimpleBooleanFormat;
import com.top_logic.template.util.SimpleCharSequenceFormat;
import com.top_logic.template.util.SimpleDoubleFormat;
import com.top_logic.template.util.SimpleLongFormat;

/**
 * Service methods and constants around the predefined data types in the template language.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateTypes {

	public static final String TYPE_NAME_ROOT = "_object";

	/**
	 * Name of the implicit 'this' type.
	 */
	public static final String IMPLICIT_THIS_TYPE = "_this";

	public static final String TYPE_NAME_STRING = "string";

	public static final String TYPE_NAME_XML = "xml";

	public static final String TYPE_NAME_BOOLEAN = "boolean";

	public static final String TYPE_NAME_INTEGER = "integer";

	public static final String TYPE_NAME_FLOAT = "float";

	public static final String TYPE_NAME_DATE = "date";

	public static LinkedHashMap<String, MetaObject> buildTypesMap() {
		LinkedHashMap<String, MetaObject> types = new LinkedHashMap<>();
		types.put(TYPE_NAME_STRING, MOPrimitive.STRING);
		types.put(TYPE_NAME_XML, MOPrimitive.CLOB);
		types.put(TYPE_NAME_BOOLEAN, MOPrimitive.BOOLEAN);
		types.put(TYPE_NAME_INTEGER, MOPrimitive.LONG);
		types.put(TYPE_NAME_FLOAT, MOPrimitive.DOUBLE);
		types.put(TYPE_NAME_DATE, MOPrimitive.DATE);
		types.put(TYPE_NAME_ROOT, new MOClassImpl(TYPE_NAME_ROOT));
		return types;
	}

	public static FormatMap buildFormatMap() {
		FormatMap formatMap = new FormatMap();
		// Normally, there is no format needed to "parse" or "format" a String.
		// But then everything using these maps would need extra code to handle strings separately
		// and not ask the map for a Format for parsing and formating.
		formatMap.put(MOPrimitive.STRING, String.class, new SimpleCharSequenceFormat());
		formatMap.put(MOPrimitive.CLOB, String.class, new SimpleCharSequenceFormat());
		formatMap.put(MOPrimitive.BOOLEAN, Boolean.class, new SimpleBooleanFormat());
		formatMap.put(MOPrimitive.LONG, Long.class, new SimpleLongFormat());
		formatMap.put(MOPrimitive.DOUBLE, Double.class, new SimpleDoubleFormat());
		formatMap.put(MOPrimitive.DATE, Date.class, XmlDateTimeFormat.INSTANCE);
		return formatMap;
	}

	public static void buildEmptyThisType(Map<String, MetaObject> typeMap) {
		MOClassImpl newType = new MOClassImpl(IMPLICIT_THIS_TYPE);
		newType.setSuperclass((MOClass) typeMap.get(TYPE_NAME_ROOT));
		newType.freeze();
		typeMap.put(IMPLICIT_THIS_TYPE, newType);
	}

}
