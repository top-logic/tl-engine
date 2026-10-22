/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;

/**
 * All indexed types, and which configuration applies to an object.
 *
 * <p>
 * An object is indexed by the configuration of its own class, or of the nearest super class that
 * has one. So one configuration of an abstract type covers all its concrete types, and a concrete
 * type may still have its own.
 * </p>
 */
public final class IndexedTypes {

	private final Map<TLClass, IndexedType> _byType = new LinkedHashMap<>();

	/**
	 * Compiles the given configurations.
	 */
	public IndexedTypes(Collection<IndexedTypeConfig> configs) {
		for (IndexedTypeConfig config : configs) {
			IndexedType type = new IndexedType(config);
			if (_byType.put(type.type(), type) != null) {
				throw new IllegalArgumentException("The type '" + config.getName() + "' is configured twice.");
			}
		}
	}

	/**
	 * The configured types.
	 */
	public Collection<IndexedType> all() {
		return _byType.values();
	}

	/**
	 * The configuration that applies to the given object, or <code>null</code> when it is not
	 * indexed.
	 */
	public IndexedType forObject(TLObject object) {
		TLStructuredType type = object.tType();
		return type instanceof TLClass clazz ? forClass(clazz) : null;
	}

	/**
	 * The configuration that applies to objects of the given class, or <code>null</code>.
	 */
	public IndexedType forClass(TLClass clazz) {
		IndexedType own = _byType.get(clazz);
		if (own != null) {
			return own;
		}
		for (TLClass general : TLModelUtil.getTransitiveGeneralizations(clazz)) {
			IndexedType inherited = _byType.get(general);
			if (inherited != null) {
				return inherited;
			}
		}
		return null;
	}

	/**
	 * Every concrete class whose objects are indexed.
	 */
	public Set<TLClass> concreteClasses() {
		Set<TLClass> result = new LinkedHashSet<>();
		for (TLClass type : _byType.keySet()) {
			for (TLClass concrete : TLModelUtil.getConcreteSpecializations(type)) {
				if (forClass(concrete) != null) {
					result.add(concrete);
				}
			}
		}
		return result;
	}

	/**
	 * The qualified names of the configured types, as a type filter of a search over all of them.
	 */
	public List<String> names() {
		return _byType.keySet().stream().map(TLModelUtil::qualifiedName).toList();
	}

}
