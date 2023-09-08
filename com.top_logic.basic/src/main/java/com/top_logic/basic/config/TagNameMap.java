/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Protocol;

/**
 * Result of resolving tag names for configuration interfaces or implementation classes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class TagNameMap {

	public static final TagNameMap EMPTY = new TagNameMap() {
		{
			// Make unmodifiable.
			_descriptorByTag = Collections.emptyMap();
		}
	};

	/**
	 * Mapping from tag name to {@link ConfigurationDescriptor} or {@link List} of
	 * {@link ConfigurationDescriptor} in case of a non unique tag mapping.
	 */
	Map<String, ConfigurationDescriptor> _descriptorByTag = new HashMap<>();

	public Iterable<Entry<String, ConfigurationDescriptor>> apply(final Protocol log, final PropertyDescriptor property,
			final Set<String> ignoreTags) {
		return new Iterable<>() {
			@Override
			public Iterator<Entry<String, ConfigurationDescriptor>> iterator() {
				return _descriptorByTag.entrySet().iterator();
			}
		};
	}

	void put(String tagName, ConfigurationDescriptor elementDescriptor) {
		_descriptorByTag.put(tagName, elementDescriptor);
	}

	public boolean isEmpty() {
		return _descriptorByTag.isEmpty();
	}

}
