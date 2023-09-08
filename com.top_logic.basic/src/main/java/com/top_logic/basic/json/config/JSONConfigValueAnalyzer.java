/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ValueAnalyzer;

/**
 * Strategy for determining the {@link JSON} encoding for a {@link JSONObject} configuration.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class JSONConfigValueAnalyzer implements ValueAnalyzer {

	/**
	 * Singleton instance of {@link JSONConfigValueAnalyzer}.
	 */
	public static final ValueAnalyzer INSTANCE = new JSONConfigValueAnalyzer();

	@Override
	public int getType(Object value) {
		if (value instanceof JSONObject) {
			return MAP_TYPE;
		} else if (value instanceof JSONList) {
			return LIST_TYPE;
		} else if (value instanceof JSONString) {
			return STRING_TYPE;
		} else if(value instanceof JSONProperty) {
			return getType(((JSONProperty) value).getValue());
		} else if (value instanceof JSONInteger) {
			return INTEGER_TYPE;
		} else if (value instanceof JSONFloat) {
			return FLOAT_TYPE;
		} else if (value instanceof JSONTrue) {
			return TRUE_TYPE;
		} else if (value instanceof JSONFalse) {
			return FALSE_TYPE;
		} else if (value instanceof JSONBoolean) {
			return ((JSONBoolean) value).getValue() ? TRUE_TYPE : FALSE_TYPE;
		} else if (value instanceof JSONNull) {
			return NULL_TYPE;
		} else {
			return UNSUPPORTED_TYPE;
		}

	}

	@Override
	public String getString(Object object) {
		if (object instanceof JSONProperty) {
			return getString(((JSONProperty) object).getValue());
		} else if (object instanceof JSONString) {
			return ((JSONString) object).getValue();
		}

		return null;
	}

	@Override
	public Date getDateValue(Object object) {
		return null;
	}

	@Override
	public Iterator<Entry<String, JSONProperty>> getMapIterator(Object map) {
		if (map instanceof JSONObject) {
			return ((JSONObject) map).getProperties().entrySet().iterator();
		} else if (map instanceof JSONProperty) {
			return getMapIterator(((JSONProperty) map).getValue());
		}

		return null;
	}

	@Override
	public Iterator<JSONValue> getListIterator(Object list) {
		if (list instanceof JSONList) {
			return ((JSONList) list).getContent().iterator();
		} else if (list instanceof JSONProperty) {
			return getListIterator(((JSONProperty) list).getValue());
		}

		return null;
	}

	@Override
	public Number getNumber(Object object) {
		if (object instanceof JSONProperty) {
			return getNumber(((JSONProperty) object).getValue());
		} else if (object instanceof JSONFloat) {
			return ((JSONFloat) object).getValue();
		} else if (object instanceof JSONInteger) {
			return ((JSONInteger) object).getValue();
		}

		return null;
	}

}
