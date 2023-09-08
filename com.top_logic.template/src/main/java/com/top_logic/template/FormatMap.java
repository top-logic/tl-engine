/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template;

import java.text.Format;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.dob.MetaObject;

/**
 * A map for types to {@link Format}s.
 * <p>
 * As the template engine has to handle different kind of 'types', a normal {@link Map} isn't
 * usable. Instead, two mappings exist:
 * <ul>
 * <li>{@link MetaObject} -> {@link Format}</li>
 * <li>{@link Class} -> {@link Format}</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FormatMap {

	private final Map<MetaObject, Format> _formatByMetaObject = new HashMap<>();

	private final Map<Class<?>, Format> _formatByClass = new HashMap<>();

	/**
	 * Stores the given {@link Format} under the given {@link MetaObject} and {@link Class}.
	 */
	public void put(MetaObject metaObject, Class<?> javaClass, Format format) {
		_formatByMetaObject.put(metaObject, format);
		_formatByClass.put(javaClass, format);
	}

	/**
	 * The {@link Format} stored under the given {@link MetaObject} key.
	 */
	public Format get(MetaObject key) {
		return _formatByMetaObject.get(key);
	}

	/**
	 * The {@link Format} stored under the given {@link Class} key.
	 */
	public Format get(Class<?> key) {
		return _formatByClass.get(key);
	}

	/**
	 * An unmodifiable view of the internal used {@link Map} from {@link Class} to
	 *         {@link Format}.
	 */
	public Map<Class<?>, Format> getClassMap() {
		return Collections.unmodifiableMap(_formatByClass);
	}

	/**
	 * An unmodifiable view of the internal used {@link Map} from {@link MetaObject} to
	 *         {@link Format}.
	 */
	public Map<MetaObject, Format> getMetaObjectMap() {
		return Collections.unmodifiableMap(_formatByMetaObject);
	}

}
