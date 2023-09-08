/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Representation of a JavaScript object literal.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class JSObject extends AbstractJSValue {

	private Map<String, JSValue> properties;

	/**
	 * Creates a {@link JSObject}.
	 */
	public JSObject() {
		this(new HashMap<>());
	}

	/**
	 * Creates a {@link JSObject} with the given values map.
	 */
	public JSObject(Map<String, JSValue> properties) {
		assert properties != null :
			"Properties map must not be null, use empty map, or no-arg constructor.";
		this.properties = properties;
	}

	/**
	 * Creates a {@link JSObject} with a single given key/value pair.
	 */
	public JSObject(String name, JSValue value) {
		this();
		addProperty(name, value);
	}

	/**
	 * Show properties for debugging.
	 */
	@Override
	public String toString() {
	    return "JSObject " + properties;
	}

	public boolean hasProperty(String propertyName) {
		return properties.containsKey(propertyName);
	}

	public JSObject addProperty(String propertyName, JSValue propertyValue) {
		properties.put(propertyName, propertyValue);
		return this;
	}

	public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

	@Override
	public JSValue eval(JSEnvironment env) {
		HashMap<String, JSValue> evaluatedProperties = new HashMap<>();

		// Evaluate all properties of this object.
		for (Entry<String, JSValue> entry : properties.entrySet()) {
			evaluatedProperties.put(
				entry.getKey(), ((JSExpression) entry.getValue()).eval(env));
		}

		return new JSObject(evaluatedProperties);
	}

	@Override
	public void eval(Appendable out) throws IOException {
		out.append('{');
		for (Iterator<Entry<String, JSValue>> it = properties.entrySet().iterator(); it.hasNext();) {
			Entry<String, JSValue> entry = it.next();

			String propertyName = entry.getKey();
			JSValue propertyValue = entry.getValue();
			assert propertyName != null;

			out.append(propertyName);
			out.append(':');
			propertyValue.eval(out);

			if (it.hasNext()) {
				out.append(',');
			}
		}
		out.append('}');
	}

	@Override
	public Map<String, Object> toObject() {
		HashMap<String, Object> result = new HashMap<>(properties.size());
		for (Iterator<Entry<String, JSValue>> it = properties.entrySet().iterator(); it.hasNext();) {
			Entry<String, JSValue> entry = it.next();

			String propertyName = entry.getKey();
			JSValue value = entry.getValue();
			Object propertyObject;
			if (value != null) {
				propertyObject = value.toObject();
			} else {
				propertyObject = value;
			}

			result.put(propertyName, propertyObject);
		}
		return result;
	}

	public final Map<String, Object> getPropertiesToObject() {
		return toObject();
	}

}
