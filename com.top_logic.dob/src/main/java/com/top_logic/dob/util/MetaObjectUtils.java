/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOPart;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.storage.CachedComputedAttributeStorage;
import com.top_logic.dob.attr.storage.MOAttributeStorageImpl;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOStructureImpl;

/**
 * Commonly used utility functions for {@link MetaObject}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaObjectUtils {

	/** Mapping from an {@link MOPart} to its {@link MOPart#getName() name}. */
	public static Mapping<MOPart, String> NAME_MAPPING = new Mapping<>() {

		@Override
		public String map(MOPart input) {
			return input.getName();
		}
	};

	/**
	 * Check, whether the given {@link MetaObject} is an association type.
	 * 
	 * <p>
	 * The only potentially association types are {@link MOClass}es.
	 * </p>
	 */
	public static boolean isAssociation(MetaObject aMO) {
		return MetaObjectUtils.isClass(aMO) && ((MOClass) aMO).isAssociation();
	}

	/**
	 * Check, whether the given {@link MetaObject} is abstract.
	 * 
	 * <p>
	 * The only potentially abstract types are {@link MOClass}es.
	 * </p>
	 */
	public static boolean isAbstract(MetaObject aMO) {
		boolean abstractClass = MetaObjectUtils.isClass(aMO) && ((MOClass) aMO).isAbstract();
		if (abstractClass) {
			return true;
		}
		return isAlternative(aMO);
	}

	/**
	 * Whether the given type kind is {@link Kind#primitive}.
	 */
	public static boolean isPrimitive(MetaObject metaObject) {
		return metaObject.getKind() == Kind.primitive;
	}

	/**
	 * Whether the given type kind is {@link Kind#struct}.
	 */
	public static boolean isStructure(MetaObject metaObject) {
		return metaObject.getKind() == Kind.struct;
	}

	/**
	 * Whether the given type kind is {@link Kind#item}.
	 */
	public static boolean isClass(MetaObject metaObject) {
		return metaObject.getKind() == Kind.item;
	}

	/**
	 * Whether the given type kind is {@link Kind#alternative}.
	 */
	public static boolean isAlternative(MetaObject metaObject) {
		return metaObject.getKind() == Kind.alternative;
	}

	/**
	 * Whether the given type kind is {@link Kind#collection}.
	 */
	public static boolean isCollection(MetaObject metaObject) {
		return metaObject.getKind() == Kind.collection;
	}

	/**
	 * {@link MOStructure#getAttribute(String)} for a {@link MOStructure} type,
	 * {@link NoSuchAttributeException} otherwise.
	 */
	public static MOAttribute getAttribute(MetaObject metaObject, String attrName) throws NoSuchAttributeException {
		if (! (metaObject instanceof MOStructure)) {
			throw new NoSuchAttributeException("Not a structured type: " + metaObject.getName());
		}
		return ((MOStructure) metaObject).getAttribute(attrName);
	}

	/**
	 * {@link MOStructure#getAttribute(String)} and cast to {@link MOReference} for a
	 * {@link MOStructure} type, {@link NoSuchAttributeException} otherwise.
	 */
	public static MOReference getReference(MetaObject metaObject, String attrName) throws NoSuchAttributeException {
		return (MOReference) getAttribute(metaObject, attrName);
	}

	/**
	 * {@link MOStructure#hasAttribute(String)} for a {@link MOStructure} type, <code>false</code>
	 * otherwise.
	 */
	public static boolean hasAttribute(MetaObject metaObject, String attrName) {
		if (! (metaObject instanceof MOStructure)) {
			return false;
		}
		return ((MOStructure) metaObject).hasAttribute(attrName);
	}

	/**
	 * {@link MOStructure#getAttributes()} for a {@link MOStructure} type,
	 * {@link Collections#emptyList()} otherwise.
	 */
	public static List<? extends MOAttribute> getAttributes(MetaObject metaObject) {
		if (! (metaObject instanceof MOStructure)) {
			return Collections.emptyList();
		}
		return ((MOStructure) metaObject).getAttributes();
	}

	/**
	 * {@link MOStructure#getReferenceAttributes()} for a {@link MOStructure} type,
	 * {@link Collections#emptyList()} otherwise.
	 */
	public static List<? extends MOReference> getReferences(MetaObject metaObject) {
		if (! (metaObject instanceof MOStructure)) {
			return Collections.emptyList();
		}
		return ((MOStructure) metaObject).getReferenceAttributes();
	}
	
	/**
	 * {@link MOStructure#getAttributeNames()} for a {@link MOStructure} type,
	 * <code>null</code> otherwise.
	 */
	public static String[] getAttributeNames(MetaObject metaObject) {
		if (! (metaObject instanceof MOStructure)) {
			return null;
		}
		return ((MOStructure) metaObject).getAttributeNames();
	}

	/**
	 * {@link MOStructure#getCacheSize()} for a {@link MOStructure} type,
	 * <code>0</code> otherwise.
	 */
	public static int getCacheSize(MetaObject metaObject) {
		if (! (metaObject instanceof MOStructure)) {
			return 0;
		}
		return ((MOStructure) metaObject).getCacheSize();
	}

	/**
	 * Creates a new {@link List} containing all {@link MOReference}s in the
	 * given attributes list.
	 * 
	 * @param attributes
	 *        The list to filter. Must not be <code>null</code>.
	 */
	public static List<MOReference> filterReferences(List<? extends MOAttribute> attributes) {
		List<MOReference> references = new ArrayList<>();
		for (MOAttribute attribute : attributes) {
			if (attribute instanceof MOReference) {
				references.add((MOReference) attribute);
			}
		}
		return references;
	}

	/**
	 * Whether the {@link MetaObject} represents an item type
	 */
	public static boolean isItemType(MetaObject type) {
		switch (type.getKind()) {
			case item:
			case alternative:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns the list of "cache attributes" in the giveb type.
	 * 
	 * @param mo
	 *        The type to get "cache attributes" for.
	 * 
	 * @see #isCacheAttribute(MOAttribute)
	 */
	public static List<? extends MOAttribute> getCacheAttributes(MetaObject mo) {
		if (!(mo instanceof MOStructure)) {
			return Collections.emptyList();
		}
		if (mo instanceof MOStructureImpl) {
			return ((MOStructureImpl) mo).getCacheAttributes();
		}
		return filterCachedAttributes(((MOStructure) mo).getAttributes());
	}

	/**
	 * Whether the given attribute is a "cache attributes".
	 * 
	 * <p>
	 * A cache attribute has no database representation. The value for an object is computed by a
	 * certain algorithm and the value is stored within the object. As the computation of the value
	 * may depend on the environment (the changed object, a different attribute, ...) the cache
	 * value may be adapted when some value of an object changes.
	 * </p>
	 */
	public static boolean isCacheAttribute(MOAttribute attr) {
		return attr.getStorage() instanceof CachedComputedAttributeStorage;
	}

	public static void resetCacheAttribute(MOAttribute cacheAttribute, Object[] storage, DataObject changedObject, MOAttribute changedAttribute) {
		CachedComputedAttributeStorage attrStorage = (CachedComputedAttributeStorage) cacheAttribute.getStorage();
		attrStorage.resetCacheValue(cacheAttribute, storage, changedObject, changedAttribute);
	}

	public static <T extends MOAttribute> List<T> filterCachedAttributes(List<T> attributes) {
		List<T> result = new ArrayList<>();
		for (T attribute : attributes) {
			if (isCacheAttribute(attribute)) {
				result.add(attribute);
			}
		}
		return result;
	}

	/**
	 * Checks whether the given type is a versioned {@link #isClass(MetaObject) class}.
	 * 
	 * @param type
	 *        The type to check.
	 * @return <code>true</code> iff the given type is a {@link #isClass(MetaObject) class} and
	 *         {@link MOClass#isVersioned() versioned}.
	 */
	public static boolean isVersioned(MetaObject type) {
		return MetaObjectUtils.isClass(type) && ((MOClass) type).isVersioned();
	}

	/**
	 * Returns the default {@link AttributeStorage} when storing attributes of the given type.
	 * 
	 * @see MOAttribute#getStorage()
	 * @see MOAttribute#getMetaObject()
	 */
	public static AttributeStorage getDefaultStorage(MetaObject attributeType) {
		if (attributeType instanceof MOPrimitive) {
			return ((MOPrimitive) attributeType).getDefaultStorage();
		}
		return MOAttributeStorageImpl.INSTANCE;
	}

}
