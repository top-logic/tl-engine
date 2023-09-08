/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.util;

import java.util.Collection;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Utilities to add propeties to an existant {@link JavaScriptObject}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class JavaScriptObjectUtil {

	/**
	 * The object property for the given key.
	 */
	public static native JavaScriptObject get(JavaScriptObject object, String key) /*-{
		return object[key];
	}-*/;

	/**
	 * Add a property to the given {@link JavaScriptObject}.
	 */
	public static native void put(JavaScriptObject object, String key, Object value) /*-{
		object[key] = value;
	}-*/;

	/**
	 * Add an array property to the given {@link JavaScriptObject}.
	 */
	public static void put(JavaScriptObject object, String key, Collection<?> items) {
		put(object, key, getArray(items));
	}

	/**
	 * Copies all properties from source to target.
	 */
	public static native void assign(JavaScriptObject target, JavaScriptObject source) /*-{
		if (typeof Object.assign != 'function') {
			Object.defineProperty(Object, "assign", {
				value : function assign(target, varArgs) {
					'use strict';
					if (target == null) {
						throw new TypeError(
								'Cannot convert undefined or null to object');
					}

					var to = Object(target);

					for (var index = 1; index < arguments.length; index++) {
						var nextSource = arguments[index];

						if (nextSource != null) {
							for ( var nextKey in nextSource) {
								if (Object.prototype.hasOwnProperty.call(
										nextSource, nextKey)) {
									to[nextKey] = nextSource[nextKey];
								}
							}
						}
					}
					return to;
				},
				writable : true,
				configurable : true
			});
		}

		Object.assign(target, source);
	}-*/;

	/**
	 * Creates a {@link JavaScriptObject} array containing the given items.
	 */
	public static JavaScriptObject getArray(Collection<?> items) {
		JavaScriptObject array = JavaScriptObject.createArray();

		pushAll(array, items);

		return array;
	}

	private static void pushAll(JavaScriptObject object, Collection<?> items) {
		for (Object item : items) {
			push(object, item);
		}
	}

	private static native void push(JavaScriptObject object, Object item) /*-{
		object.push(item);
	}-*/;

	private static native void push(JavaScriptObject object, Collection<?> items) /*-{
		object.push(getArray(items));
	}-*/;
}
