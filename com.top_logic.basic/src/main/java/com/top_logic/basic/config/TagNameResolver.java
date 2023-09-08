/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * Algorithm finding custom tag names for configuration types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class TagNameResolver {

	private Protocol _log;

	public TagNameResolver(Protocol log) {
		_log = log;
	}

	public TagNameMap polymorphicSubtypes(Class<?> instanceType) {
		return index(TypeIndex.getInstance().implTagNames(instanceType));
	}

	public TagNameMap subtypes(Class<?> rootInterface) {
		return index(TypeIndex.getInstance().configTagNames(rootInterface));
	}

	private TagNameMap index(Map<String, Class<?>> tagNames) {
		if (tagNames.isEmpty()) {
			return TagNameMap.EMPTY;
		}

		TagNameMap result = new TagNameMap();
		for (Entry<String, Class<?>> entry : tagNames.entrySet()) {
			addTag(result, entry.getKey(), entry.getValue());
		}
		return result;
	}

	private void addTag(TagNameMap result, String tagName, Class<?> subInterface) {
		ConfigurationDescriptor elementDescriptor;
		try {
			elementDescriptor = ConfigDescriptionResolver.getDescriptor(_log, subInterface);
		} catch (TypeNotPresentException | NoClassDefFoundError ex) {
			handleMissingClass(subInterface, ex);
			return;
		} catch (ConfigurationException ex) {
			_log.error("Cannot resolve descriptor for '" + subInterface.getName() + "'.", ex);
			return;
		}

		result.put(tagName, elementDescriptor);
	}

	private void handleMissingClass(Class<?> subInterface, Throwable ex) {
		// It is possible that an indexed class cannot be loaded, because not all of its
		// dependencies are available on the class path. This might happen in a
		// micro-service environment where e.g. the servlet API classes are not present, but
		// classes depending on these APIs may be loaded as potential subclasses of actually
		// used classes.
		_log.info("Type '" + subInterface.getName() + "' cannot be loaded: " + ex.getClass().getName() + ": "
			+ ex.getMessage());
	}

}