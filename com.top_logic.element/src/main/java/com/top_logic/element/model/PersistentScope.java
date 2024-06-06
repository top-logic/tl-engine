/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.ConcatenatedIterable;
import com.top_logic.dob.DataObjectException;
import com.top_logic.element.meta.MetaElementException;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.util.model.ElementModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.util.TLPrimitiveColumns;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.internal.PersistentType;

/**
 * Base class for persistent implementations of {@link TLScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentScope extends DynamicModelPart implements MetaElementHolder {

	/**
	 * Query to find all {@link TLClass}s defined in some scope.
	 */
	private static final IndexedLinkQuery<String, TLClass> META_ELEMENTS_ATTR =
		IndexedLinkQuery.indexedLinkQuery(new NamedConstant("metaelements"), TLClass.class, KBBasedMetaElement.META_ELEMENT_KO,
			PersistentType.SCOPE_REF, KBBasedMetaElement.META_ELEMENT_TYPE, String.class, null, true);

	private static final IndexedLinkQuery<String, TLPrimitive> DATATYPES_ATTR =
		IndexedLinkQuery.indexedLinkQuery(new NamedConstant("datatypes"), TLPrimitive.class, TLPrimitiveColumns.OBJECT_TYPE,
			PersistentType.SCOPE_REF, PersistentDatatype.NAME_ATTR, String.class, null, true);

	private static final IndexedLinkQuery<String, TLClass> CLASSES_ATTR =
		IndexedLinkQuery.indexedLinkQuery(new NamedConstant("classes"), TLClass.class, KBBasedMetaElement.META_ELEMENT_KO,
			PersistentType.SCOPE_REF, KBBasedMetaElement.NAME_ATTR, String.class,
			Collections.singletonMap(TLStructuredTypeColumns.META_ELEMENT_IMPL, TLStructuredTypeColumns.CLASS_TYPE), true);

	private static final IndexedLinkQuery<String, TLAssociation> ASSOCIATIONS_ATTR =
		IndexedLinkQuery.indexedLinkQuery(new NamedConstant("associations"), TLAssociation.class, KBBasedMetaElement.META_ELEMENT_KO,
			PersistentType.SCOPE_REF, KBBasedMetaElement.NAME_ATTR, String.class,
			Collections.singletonMap(TLStructuredTypeColumns.META_ELEMENT_IMPL, TLStructuredTypeColumns.ASSOCIATION_TYPE), true);

	private static final IndexedLinkQuery<String, TLEnumeration> ENUMERATIONS_ATTR =
		IndexedLinkQuery.indexedLinkQuery(new NamedConstant("enumerations"), TLEnumeration.class, FastList.OBJECT_NAME,
			PersistentType.MODULE_REF, FastList.NAME_ATTRIBUTE, String.class, null, true);

	/**
	 * Implementation of {@link #getTypes()}.
	 * 
	 * <p>
	 * Note: The method {@link PersistentScope#getTypes(TLScope)} actually loads values of
	 * {@link #getClasses()}, {@link #getEnumerations()}, {@link #getAssociations()}, and
	 * {@link #getDatatypes()}. When this is not done a preload of these values in
	 * {@link ElementModelService} is advisable.
	 * </p>
	 */
	private volatile Collection<TLType> _types;

	/**
	 * Creates a {@link PersistentScope}.
	 */
	@CalledByReflection
	public PersistentScope(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public void addMetaElement(TLClass aMetaElement) throws IllegalArgumentException {
		PersistentScope.addMetaElement(this, aMetaElement);
	}

	@Override
	public void removeMetaElement(TLClass aMetaElement) throws IllegalArgumentException {
		PersistentScope.removeMetaElement(this, aMetaElement);
	}

	@Override
	public TLClass getMetaElement(String aMetaElementType) throws IllegalArgumentException {
		return PersistentScope.getMetaElement(this, aMetaElementType);
	}

	@Override
	public Set<TLClass> getMetaElements() {
		return PersistentScope.getMetaElements(this);
	}

	@Override
	public Collection<TLAssociation> getAssociations() {
		return PersistentScope.getAssociations(this);
	}

	@Override
	public Collection<TLEnumeration> getEnumerations() {
		return PersistentScope.getEnumerations(this);
	}

	@Override
	public Collection<TLPrimitive> getDatatypes() {
		return PersistentScope.getDatatypes(this);
	}

	@Override
	public Collection<TLClass> getClasses() {
		return PersistentScope.getClasses(this);
	}

	@Override
	public Collection<TLType> getTypes() {
		Collection<TLType> types = _types;
		if (types == null) {
			types = PersistentScope.getTypes(this);
			_types = types;
		}
		return types;
	}

	@Override
	public TLType getType(String name) {
		return PersistentScope.getType(this, name);
	}

	/**
	 * Implementation of {@link TLScope#getType(String)}.
	 */
	public static TLType getType(TLScope self, String name) {
		TLType result = getClass(self, name);
		if (result != null) {
			return result;
		}
		result = getDataype(self, name);
		if (result != null) {
			return result;
		}
		result = getAssociation(self, name);
		if (result != null) {
			return result;
		}
		result = getEnumeration(self, name);
		if (result != null) {
			return result;
		}
		return null;
	}

	private static TLClass getClass(TLScope self, String name) {
		return classesIndex(self).get(name);
	}

	private static TLPrimitive getDataype(TLScope self, String name) {
		return datatypesIndex(self).get(name);
	}

	private static TLAssociation getAssociation(TLScope self, String name) {
		return associationsIndex(self).get(name);
	}

	private static TLEnumeration getEnumeration(TLScope self, String name) {
		return enumerationsIndex(self).get(name);
	}

	/**
	 * Implementation of {@link TLScope#getTypes()}.
	 */
	public static Collection<TLType> getTypes(final TLScope self) {
		return new AbstractCollection<>() {

			/**
			 * Note: This fields actually loads the association caches for the classes, datatypes,
			 * enumerations, and associations. When the vallue of this field is changed, such this
			 * is not longer done, it is advisable to preload the values for the scope in
			 * {@link ElementModelService}.
			 */
			private final Collection<Collection<? extends TLType>> _parts = Arrays.asList(
				getClasses(self),
				getDatatypes(self),
				getEnumerations(self),
				getAssociations(self));

			@Override
			public Iterator<TLType> iterator() {
				return ConcatenatedIterable.concat(_parts).iterator();
			}

			@Override
			public int size() {
				int result = 0;
				for (Collection<?> part : _parts) {
					result += part.size();
				}
				return result;
			}

			@Override
			public boolean add(TLType type) {
				TLScope oldScope = type.getScope();
				if (oldScope == self) {
					return false;
				}
				if (oldScope != null) {
					throw new IllegalStateException("Type is already part of another scope: " + oldScope);
				}
				try {
					type.tHandle().setAttributeValue(PersistentType.SCOPE_REF, self.tHandle());
				} catch (DataObjectException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				}
				return true;
			}

		};
	}

	/**
	 * Implementation of {@link TLScope#getClasses()}.
	 */
	public static Collection<TLClass> getClasses(TLScope self) {
		return classesIndex(self).values();
	}

	private static BidiMap<String, TLClass> classesIndex(TLScope self) {
		return AbstractWrapper.resolveLinks(self, PersistentScope.CLASSES_ATTR);
	}

	/**
	 * Implementation of {@link TLScope#getAssociations()}.
	 */
	public static Collection<TLAssociation> getAssociations(TLScope self) {
		return associationsIndex(self).values();
	}

	private static BidiMap<String, TLAssociation> associationsIndex(TLScope self) {
		return AbstractWrapper.resolveLinks(self, PersistentScope.ASSOCIATIONS_ATTR);
	}

	/**
	 * Implementation of {@link TLScope#getEnumerations()}.
	 */
	public static Collection<TLEnumeration> getEnumerations(TLScope self) {
		return enumerationsIndex(self).values();
	}

	private static BidiMap<String, TLEnumeration> enumerationsIndex(TLScope self) {
		return AbstractWrapper.resolveLinks(self, PersistentScope.ENUMERATIONS_ATTR);
	}

	/**
	 * Implementation of {@link TLScope#getDatatypes()}.
	 */
	public static Set<TLPrimitive> getDatatypes(TLScope self) {
		return datatypesIndex(self).values();
	}

	private static BidiMap<String, TLPrimitive> datatypesIndex(TLScope self) {
		return AbstractWrapper.resolveLinks(self, PersistentScope.DATATYPES_ATTR);
	}

	/**
	 * Implementation of {@link MetaElementHolder#getMetaElements()}.
	 */
	public static Set<TLClass> getMetaElements(TLObject self) {
		return metaelementsIndex(self).values();
	}

	private static BidiMap<String, TLClass> metaelementsIndex(TLObject self) {
		return AbstractWrapper.resolveLinks(self, PersistentScope.META_ELEMENTS_ATTR);
	}

	/**
	 * Implementation of {@link MetaElementHolder#getMetaElement(String)}.
	 */
	public static TLClass getMetaElement(TLObject self, String name) {
		return metaelementsIndex(self).get(name);
	}

	/**
	 * @see MetaElementHolder#removeMetaElement(TLClass)
	 */
	public static void removeMetaElement(TLObject self, TLClass aMetaElement) {
		try {
			KnowledgeItem scopeKO = self.tHandle();
			KnowledgeItem typeKO = aMetaElement.tHandle();
	
			Object currentScope = typeKO.getAttributeValue(KBBasedMetaElement.SCOPE_REF);
			if (currentScope != scopeKO && currentScope != null) {
				throw new IllegalArgumentException("Meta element '" + aMetaElement + "' is attached to scope '"
					+ currentScope + "' and cannot be removed from '" + self + "'.");
			}
			typeKO.setAttributeValue(KBBasedMetaElement.SCOPE_REF, null);
		} catch (DataObjectException ex) {
			throw new MetaElementException("Failed to remove the meta element '" + aMetaElement + "' from " + self, ex);
		}
	}

	/**
	 * @see MetaElementHolder#addMetaElement(TLClass)
	 */
	public static void addMetaElement(TLObject self, TLClass aMetaElement) {
		try {
			KnowledgeItem typeKO = aMetaElement.tHandle();
			Object currentScope = typeKO.getAttributeValue(KBBasedMetaElement.SCOPE_REF);
			if (currentScope != null) {
				throw new IllegalArgumentException("Meta element '" + aMetaElement + "' belongs already to the scope '"
					+ self + "'.");
			}
		
			KnowledgeItem scopeKO = self.tHandle();
			typeKO.setAttributeValue(KBBasedMetaElement.SCOPE_REF, scopeKO);
		} catch (DataObjectException ex) {
			throw new MetaElementException("Failed to add meta element '" + aMetaElement + "' to scope '" + self
				+ "'.", ex);
		}
	}

}
