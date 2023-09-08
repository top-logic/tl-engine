/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.FilteredIterator;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.ConfiguredAttributeImpl;
import com.top_logic.element.meta.kbbased.MetaElementPreload;
import com.top_logic.element.model.PersistentModule;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.Preloader;
import com.top_logic.model.impl.util.TLCharacteristicsCopier;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Static helpers for querying properties of {@link TLClass}s.
 * 
 * @see AttributeOperations
 * @see AttributeUtil
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaElementUtil {

	/**
	 * Check, whether the given super type candidate is a super type of the given type.
	 * 
	 * @param superTypeCandidate
	 *        A type that is checked for being a super type of the given sub type candidate.
	 * @param subTypeCandidate
	 *        The type that is checked for being a sub type of the given super type candidate.
	 * @return <code>true</code>, if the given super type candidate is the same type or a super type
	 *         of the given sub type candidate, <code>false</code>, otherwise.
	 */
	public static boolean isSuperType(TLStructuredType superTypeCandidate, TLStructuredType subTypeCandidate) {
		return hasGeneralization(subTypeCandidate, superTypeCandidate);
	}

	/**
	 * Check, whether the given super type candidate is a super type of the
	 * given type.
	 * 
	 * @param superTypeCandidate
	 *        A type that is checked for being a super type of the given sub
	 *        type candidate.
	 * @param subTypeCandidate
	 *        The type that is checked for being a sub type of the given super
	 *        type candidate.
	 * @return <code>true</code>, if the given super type candidate is the same
	 *         type or a super type of the given sub type candidate,
	 *         <code>false</code>, otherwise.
	 */
	public static boolean isSuperType(TLClass superTypeCandidate, TLClass subTypeCandidate) {
		return hasGeneralization(subTypeCandidate, superTypeCandidate);
	}

    /**
	 * Copies all attribute values from the given source object to the given target object. Only
	 * attributes, which are existing in both objects are copied (only name matching, not type
	 * matching). Attributes in the given excludeAttributeNames list and technical attributes (see
	 * {@link KBUtils#KA_SYSTEM_ATTRIBUTES}) are not copied.
	 * 
	 * @param aSource
	 *        the source object to copy attribute values from
	 * @param aTarget
	 *        the target object to copy attribute values to
	 * @param excludeAttributeNames
	 *        the attributes which shall not be copied
	 */
	public static void copyMetaAttritutes(Wrapper aSource, Wrapper aTarget,
			Collection<String> excludeAttributeNames) {
		Collection<? extends TLStructuredTypePart> sourceAttributes = aSource.tType().getAllParts();
		Collection<? extends TLStructuredTypePart> targetAttributes = aTarget.tType().getAllParts();
		Set<TLStructuredTypePart> attributes = new HashSet<>(sourceAttributes);
        attributes.retainAll(targetAttributes);
		for (TLStructuredTypePart metaAttribute : attributes) {
            String attributeName = metaAttribute.getName();
			if (metaAttribute.isDerived()) {
				continue;
			}
			if (KBUtils.KA_SYSTEM_ATTRIBUTES.contains(attributeName)) {
				continue;
			}
			if (excludeAttributeNames.contains(attributeName)) {
				continue;
            }
			aTarget.setValue(attributeName, aSource.getValue(attributeName));
        }
    }

    /**
	 * Gets all objects which are from the given meta element type or a sub meta element type.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation 
	 * 
	 * @param aMetaElement
	 *        the desired super meta element
	 * @return a set of all objects which are from the given meta element type or a sub meta element
	 *         type.
	 * @deprecated Use {@link #getAllInstancesOf(TLClass, Class)}
	 */
	@Deprecated
	public static List<Wrapper> getAllInstancesOf(TLClass aMetaElement) {
		return getAllInstancesOf(aMetaElement, Wrapper.class);
    }
    
    /**
     * Gets all objects which are from the given meta element type or a sub meta element
     * type.
     * 
     * @param aMetaElement
     *        the desired super meta element
     * @return a set of all objects which are from the given meta element type or a sub meta
     *         element type.
     */
	public static <T extends TLObject> List<T> getAllInstancesOf(TLClass aMetaElement, Class<T> expectedType) {
		return AttributeOperations.allInstances(aMetaElement, expectedType);
    }

	/**
     * Gets all objects which are from the given meta element type.
     * 
     * TODO #6121: Delete TL 5.8.0 deprecation 
     * 
     * @param metaElement
     *        the desired meta element
     * @return a set of all objects which are from the given meta element type.
     * @deprecated Use {@link #getAllDirectInstancesOf(TLClass, Class)}.
     */
	@Deprecated
	public static List<Wrapper> getAllDirectInstancesOf(TLClass metaElement) {
		return getAllDirectInstancesOf(metaElement, Wrapper.class);
	}
	
	/**
	 * Gets all objects which are from the given meta element type.
	 * 
	 * @param metaElement
	 *        the desired meta element
	 * @return a set of all objects which are from the given meta element type.
	 */
	public static <T extends TLObject> List<T> getAllDirectInstancesOf(TLClass metaElement, Class<T> expectedType) {
		List<T> result = new ArrayList<>();
		try (CloseableIterator<T> iterator = MetaElementUtil.iterateDirectInstances(metaElement, expectedType)) {
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
		}
		return result;
    }

    /**
	 * Iterator through all direct instances of the given type.
	 * 
	 * <p>
	 * <b>Note:</b> The resulting iterator must be closed after iteration to prevent resource leaks!
	 * </p>
	 * 
	 * @param type
	 *        The type to find direct instances of.
	 * @return {@link CloseableIterator} of instances.
	 */
	public static <T extends TLObject> CloseableIterator<T> iterateDirectInstances(TLClass type, Class<T> expectedType) {
		return AttributeOperations.allDirectInstances(type, expectedType);
	}

	/**
	 * Gets the type with the given name.
	 *
	 * @param aMetaElementType
	 *        the type type name of the type to search, must not be <code>null</code>
	 * @return the requested type or <code>null</code>, if there exists no type with the given type.
	 */
    public static TLClass getMetaElement(String aMetaElementType) {
        for (Iterator it = MetaElementFactory.getInstance().getAllMetaElements().iterator(); it.hasNext();) {
            TLClass me = (TLClass)it.next();
            if (aMetaElementType.equals(me.getName())) return me;
        }
        return null;
    }

	/**
	 * Loads the meta elements of the objects contained in the given collection.
	 * 
	 * @see KnowledgeBase#fillCaches(Iterable, AbstractAssociationQuery)
	 */
	public static void preloadMetaElements(PreloadContext context, Collection<? extends TLObject> baseObjects) {
		MetaElementPreload.INSTANCE.prepare(context, baseObjects);
	}

	/**
	 * Loads the {@link TLClass} and value of the given {@link TLStructuredTypePart} for the
	 * given {@link TLObject}s.
	 * 
	 * @param baseObjects
	 *        The wrapper to load the attribute. Must not be <code>null</code>.
	 * @param attributes
	 *        the attributes to pre-load. Must not be <code>null</code>.
	 */
	public static void preloadAttributes(PreloadContext context, Collection<? extends TLObject> baseObjects,
			TLStructuredTypePart... attributes) {
		preloadMetaElements(context, baseObjects);
		for (TLStructuredTypePart attribute : attributes) {
			preloadAttribute(context, baseObjects, attribute);
		}
	}

	/**
	 * Whether the given type is declared in global or pseudo-global (local to a structure root)
	 * scope.
	 * 
	 * @see TLModelUtil#isGlobal(com.top_logic.model.TLType)
	 */
	public static boolean isGlobal(TLClass me) {
		TLScope scope = me.getScope();
		if (scope.tHandle().tTable().getName().equals(PersistentModule.OBJECT_TYPE)) {
			return true;
		} else if (scope instanceof StructuredElement) {
			StructuredElement structuredElement = (StructuredElement) scope;
			boolean isRoot = structuredElement.equals(structuredElement.getRoot());
	
			return isRoot;
		} else {
			return false;
		}
	}

	/**
	 * Whether the given {@link TLClass} or one of its super types has the given name.
	 * 
	 * @param expectedTypeName
	 *        The name to search for.
	 * @param type
	 *        The type to check.
	 * @return Whether the given type has a generalization (reflexive) with the given name.
	 */
	public static boolean isSubtype(String expectedTypeName, TLClass type) {
		return isSubtype(null, expectedTypeName, type);
	}

	/**
	 * Whether the given {@link TLClass} or one of its super types has the given name in the
	 * given module.
	 * 
	 * @param expectedModuleName
	 *        The name of the module to search for. May be <code>null</code> which means equality of
	 *        the module name is ignored.
	 * @param expectedTypeName
	 *        The name to search for.
	 * @param type
	 *        The type to check.
	 * @return Whether the given type has a generalization (reflexive) with the given name in the
	 *         given module.
	 */
	public static boolean isSubtype(String expectedModuleName, String expectedTypeName, TLClass type) {
		for (TLClass generalization : TLModelUtil.getReflexiveTransitiveGeneralizations(type)) {
			if (generalization.getName().equals(expectedTypeName)) {
				if (expectedModuleName == null || expectedModuleName.equals(generalization.getModule().getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static StructuredElement getRoot(TLStructuredTypePart attribute) {
		String structureType = ((TLClass) attribute.getType()).getModule().getName();
		return StructuredElementFactory.getInstanceForStructure(structureType).getRoot();
	}

	public static List<String> getContainmentNodeTypes(String moduleName, String typeName) {
		return getContainmentNodeNames(getGlobalType(moduleName, typeName));
	}

	public static TLClass getGlobalType(String moduleName, String typeName) {
		if (moduleName == null) {
			throw new IllegalArgumentException("Module name must not be null.");
		}
		if (typeName == null) {
			throw new IllegalArgumentException("Type must not be null.");
		}
		return MetaElementFactory.getInstance().getGlobalMetaElement(moduleName, typeName);
	}

	public static List<String> getContainmentNodeNames(TLClass type) {
		ArrayList<String> result = new ArrayList<>();
		for (TLStructuredTypePart attribute : getContainments(type)) {
			if (AttributeOperations.isComposition(attribute)) {
				result.add(attribute.getType().getName());
			}
		}
	
		return result;
	}

	public static boolean hasGeneralization(TLStructuredType type, TLStructuredType potentialGeneralization) {
		if (type.getModelKind() == ModelKind.CLASS && potentialGeneralization.getModelKind() == ModelKind.CLASS) {
			return hasGeneralization((TLClass) type, (TLClass) potentialGeneralization);
		} else {
			return type == potentialGeneralization;
		}
	}

	public static boolean hasGeneralization(TLStructuredType type, Set<? extends TLClass> potentialGeneralizations) {
		for (TLClass searchType : potentialGeneralizations) {
			if (hasGeneralization(type, searchType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasGeneralization(TLClass type, TLClass potentialGeneralization) {
		return TLModelUtil.isGeneralization(potentialGeneralization, type);
	}

	public static boolean hasGeneralization(TLStructuredType type, String generalizationName) {
		if (type.getModelKind() == ModelKind.CLASS) {
			return hasGeneralization((TLClass) type, generalizationName);
		} else {
			return type.getName().equals(generalizationName);
		}
	}

	public static boolean hasGeneralization(TLClass type, String generalizationName) {
		for (TLClass tlClass : TLModelUtil.getReflexiveTransitiveGeneralizations(type)) {
			if (tlClass.getName().equals(generalizationName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Loads values of the given {@link TLStructuredTypePart} for the given {@link TLObject}s.
	 * 
	 * @param baseObjects
	 *        The wrapper to load the attribute. Must not be <code>null</code>.
	 * @param attrName
	 *        the name of the attribute to pre-load. Must not be <code>null</code>.
	 */
	public static void preloadAttribute(PreloadContext context, List<? extends TLObject> baseObjects, String attrName) {
		if (baseObjects.isEmpty()) {
			return;
		}
		TLObject example = baseObjects.get(0);
		TLClass type = (TLClass) example.tType();
		TLStructuredTypePart attribute = type.getPart(attrName);
		if (attribute == null) {
			return;
		}
		preloadAttribute(context, baseObjects, attribute);
	}

	/**
	 * Loads values of the given {@link TLStructuredTypePart} for the given {@link TLObject}s.
	 * 
	 * @param baseObjects
	 *        The wrapper to load the attribute. Must not be <code>null</code>.
	 * @param attribute
	 *        the attribute to pre-load. Must not be <code>null</code>.
	 * 
	 * @see StorageImplementation#getPreload()
	 * @see #reversePreloadAttribute(PreloadContext, Collection, TLStructuredTypePart)
	 */
	public static void preloadAttribute(PreloadContext context, Collection<? extends TLObject> baseObjects,
			TLStructuredTypePart attribute) {
		StorageImplementation storageImplementation = (StorageImplementation) AttributeOperations.getStorageImplementation(attribute);
		if (storageImplementation == null) {
			Logger.error("Cannot preload attribute '" + attribute + "' without storage implementation.",
				MetaElementUtil.class);
			return;
		}
		PreloadContribution contribution = storageImplementation.getPreload();
		Preloader preloader = new Preloader();
		contribution.contribute(preloader);
		preloader.prepare(context, baseObjects);
	}

	/**
	 * Loads reverse values of the given {@link TLStructuredTypePart} for the given
	 * {@link TLObject}s.
	 * 
	 * @param baseObjects
	 *        The wrapper to load the attribute. Must not be <code>null</code>.
	 * @param attribute
	 *        the attribute to pre-load. Must not be <code>null</code>.
	 * 
	 * @see StorageImplementation#getReversePreload()
	 * @see #preloadAttribute(PreloadContext, Collection, TLStructuredTypePart)
	 */
	public static void reversePreloadAttribute(PreloadContext context, Collection<? extends TLObject> baseObjects,
			TLStructuredTypePart attribute) {
		StorageImplementation storageImplementation = (StorageImplementation) attribute.getStorageImplementation();
		if (storageImplementation == null) {
			Logger.error("Cannot preload attribute '" + attribute + "' without storage implementation.",
				MetaElementUtil.class);
			return;
		}
		PreloadContribution contribution = storageImplementation.getReversePreload();
		Preloader preloader = new Preloader();
		contribution.contribute(preloader);
		preloader.prepare(context, baseObjects);
	}

	/**
	 * Sets {@link TLClass#getGeneralizations()} to the singleton list containing the argument, or
	 * the empty list, if the argument is <code>null</code>.
	 */
	public static void setSuperMetaElement(TLClass self, TLClass generalization) {
		List<TLClass> generalizations;
		if (generalization == null) {
			generalizations = Collections.emptyList();
		} else {
			generalizations = Collections.<TLClass> singletonList(generalization);
		}
		TLModelUtil.setGeneralizations(self, generalizations);
	}

	/**
	 * All containment references of the given type.
	 */
	public static Iterable<TLStructuredTypePart> getContainments(TLClass type) {
		final Collection<TLStructuredTypePart> attributes = TLModelUtil.getMetaAttributes(type);
		return new Iterable<>() {
			@Override
			public Iterator<TLStructuredTypePart> iterator() {
				return new FilteredIterator<>(attributes.iterator()) {
					@Override
					protected boolean test(TLStructuredTypePart value) {
						if (!TLModelUtil.isReference(value)) {
							return false;
						}

						if (!((TLReference) value).getEnd().isComposite()) {
							return false;
						}

						return true;
					}
				};
			}
		};
	}

	/**
	 * Whether {@link TLClass#getLocalClassParts()} contains one with the given name.
	 */
	public static boolean hasLocalMetaAttribute(TLClass metaElement, String name) {
		return MetaElementUtil.getLocalMetaAttributeOrNull(metaElement, name) != null;
	}

	/**
	 * Whether {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)} contains one with the given
	 * name.
	 */
	public static boolean hasMetaAttributeInHierarchy(TLClass metaElement, String name) {
		return getMetaAttributeInHierarchyOrNull(metaElement, name) != null;
	}

	/**
	 * Retrieve the {@link TLStructuredTypePart} with the given name from
	 * {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)} of the given meta element.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param metaElement
	 *        The type that builds the root of the hierarchy.
	 * @param name
	 *        The name of the attribute to retrieve.
	 * @return The {@link TLStructuredTypePart} with the given name from the hierarchy.
	 * @throws NoSuchAttributeException
	 *         If no attribute with the given name is found.
	 * 
	 * @see TLModelUtil#getMetaAttributesInHierarchy(TLClass)
	 * 
	 * @deprecated The result of this method is random, if there are multiple subtypes that define
	 *             an attribute with the given name, or if the attribute is overridden. Use
	 *             {@link TLClass#getPart(String)} or {@link TLClass#getLocalParts()} instead, as
	 *             they don't include subtypes, or use a method that returns a list of attributes
	 *             like {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)}, as they can
	 *             return all matching attributes, not just one.
	 */
	@Deprecated
	public static TLStructuredTypePart getMetaAttributeInHierarchy(TLClass metaElement, String name)
			throws NoSuchAttributeException {
		TLStructuredTypePart result = getMetaAttributeInHierarchyOrNull(metaElement, name);
		if (result == null) {
			throw new NoSuchAttributeException("No such attribute '" + name + "' in the type hierarchy rooted at '"
				+ metaElement.getName() + "'.");
		}
		return result;
	}

	/**
	 * Retrieve the {@link TLStructuredTypePart} with the given name from
	 * {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)} of the given meta element.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param metaElement
	 *        The type that builds the root of the hierarchy.
	 * @param name
	 *        The name of the attribute to retrieve.
	 * @return The {@link TLStructuredTypePart} with the given name from the hierarchy.
	 * 
	 * @see TLModelUtil#getMetaAttributesInHierarchy(TLClass)
	 * 
	 * @deprecated The result of this method is random, if there are multiple subtypes that define
	 *             an attribute with the given name, or if the attribute is overridden. Use
	 *             {@link TLClass#getPart(String)} or {@link TLClass#getLocalParts()} instead, as
	 *             they don't include subtypes, or use a method that returns a list of attributes
	 *             like {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)}, as they can
	 *             return all matching attributes, not just one.
	 */
	@Deprecated
	public static TLStructuredTypePart getMetaAttributeInHierarchyOrNull(TLClass metaElement, String name) {
		TLStructuredTypePart ma = MetaElementUtil.getMetaAttributeOrNull(metaElement, name);
		if (ma != null) {
			return ma;
		}
		for (TLClass specialization : TLModelUtil.getTransitiveSpecializations(metaElement)) {
			ma = MetaElementUtil.getLocalMetaAttributeOrNull(specialization, name);
			if (ma != null) {
				return ma;
			}
		}
		return null;
	}

	/**
	 * Same as {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)} but excluding inherited
	 * {@link TLStructuredTypePart}s of the root type.
	 * <p>
	 * All definitions in sub-types of the given {@link TLClass} are returned. If an attribute is
	 * not defined in the given {@link TLClass}, the definitions from all subtypes are returned,
	 * even if they have no common root and define for example incompatible types.
	 * </p>
	 * 
	 * @param metaElement
	 *        The root type.
	 * @return All {@link TLStructuredTypePart}s in the hierarchy excluding inherited attributes of
	 *         the root type. The {@link List} is potentially unmodifiable.
	 * 
	 * @see TLModelUtil#getMetaAttributesInHierarchy(TLClass)
	 */
	public static List<TLStructuredTypePart> getMetaAttributesInHierarchyExcludingInherited(TLClass metaElement) {
		return TLModelUtil.getMetaAttributes(metaElement, false, true);
	}

	/**
	 * Get all MetaAttributes of the given type. If specified the MetaAttributes of the the super
	 * and sub MetaElements will be added as well.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param aMetaAttributeType
	 *        the legacy attribute type to be returned.
	 * @param includeSuperMetaElements
	 *        if <code>true</code> search in super MetaAttributes as well
	 * @param includeSubMetaElements
	 *        if <code>true</code> search in sub MetaAttributes as well
	 * @return a set of all MetaAttributes that match the params. May be empty but must not be
	 *         <code>null</code>.
	 * 
	 * @deprecated Use query based on {@link TLStructuredTypePart#getType()} /
	 *             {@link ConfiguredAttributeImpl#TYPE_REF}.
	 */
	@Deprecated
	public static List<TLStructuredTypePart> getMetaAttributes(TLClass metaElement, int aMetaAttributeType,
			boolean includeSuperMetaElements, boolean includeSubMetaElements) {

		List<TLStructuredTypePart> theAttList = new ArrayList<>();
		for (TLStructuredTypePart theAttCheck : TLModelUtil.getMetaAttributes(metaElement,
			includeSuperMetaElements,
			includeSubMetaElements)) {
			// some special handling for calculated meta attribute here necessary
			if (LegacyTypeCodes.TYPE_CALCULATED == aMetaAttributeType && AttributeOperations.isReadOnly(theAttCheck)) {
				theAttList.add(theAttCheck);
			} else if (AttributeOperations.getMetaAttributeType(theAttCheck) == aMetaAttributeType) {
				theAttList.add(theAttCheck);
			}
		}

		return theAttList;
	}

	/**
	 * Whether the given {@link TLStructuredTypePart} represents a reference of one object to (potential
	 * multiple) other items.
	 * 
	 * @param attribute
	 *        The {@link TLStructuredTypePart} to check.
	 * 
	 * @return Whether the {@link TLStructuredTypePart} references to other items.
	 */
	public static boolean isReferenceMetaAttribute(TLStructuredTypePart attribute) {
		return AttributeOperations.getStorageImplementation(attribute) instanceof AssociationStorage;
	}

	/**
	 * Check whether this type has a {@link TLStructuredTypePart} with the given name (either locally
	 * defined or inherited).
	 */
	public static boolean hasMetaAttribute(TLStructuredType metaElement, String aName) {
		return metaElement.getPart(aName) != null;
	}

	/**
	 * Retrieve the attribute of this type with the given name (either locally defined, or
	 * inherited).
	 * 
	 * @param aName
	 *        The {@link TLStructuredTypePart#getName()} of the attribute to retrieve.
	 * @return The attribute with the given name.
	 * @throws NoSuchAttributeException
	 *         If no such attribute exists.
	 */
	public static TLStructuredTypePart getMetaAttribute(TLStructuredType metaElement, String aName)
			throws NoSuchAttributeException {
		return metaElement.getPartOrFail(aName);
	}

	/**
	 * Same as {@link TLClass#getPart(String)} but returns <code>null</code> instead of throwing an
	 * exception.
	 */
	public static TLStructuredTypePart getMetaAttributeOrNull(TLClass metaElement, String aName) {
		return metaElement.getPart(aName);
	}

	/**
	 * Retrieve the locally defined attribute of this type with the given name (excluding inherited
	 * attributes).
	 * 
	 * @param aName
	 *        The {@link TLStructuredTypePart#getName()} of the attribute to retrieve.
	 * @return The attribute with the given name.
	 * @throws NoSuchAttributeException
	 *         If no such attribute exists.
	 */
	public static TLStructuredTypePart getLocalMetaAttribute(TLClass metaElement, String aName)
			throws NoSuchAttributeException {
		TLStructuredTypePart result = metaElement.getPartOrFail(aName);
		if (result.getOwner() != metaElement) {
			throw new NoSuchAttributeException("Not locally defined on '" + metaElement + "': " + result);
		}
		return (TLStructuredTypePart) result;
	}

	/**
	 * Same as {@link TLClass#getPart(String)} but returns <code>null</code> if the part is not
	 * locally defined.
	 * 
	 * @see TLClass#getLocalParts()
	 */
	public static TLStructuredTypePart getLocalMetaAttributeOrNull(TLClass metaElement, String aName) {
		TLStructuredTypePart result = metaElement.getPart(aName);
		if (result == null || result.getOwner() != metaElement) {
			return null;
		}
		return result;
	}

	/**
	 * Get the attribute with a given name. If specified the attribute is searched in the super and
	 * sub types as well.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param aName
	 *        the name of the attribute
	 * @param includeSuperMetaElements
	 *        if <code>true</code> search in super MetaAttributes as well
	 * @param includeSubMetaElements
	 *        if <code>true</code> search in sub MetaAttributes as well
	 * @return the attribute
	 * @throws NoSuchAttributeException
	 *         if the attribute does not exist.
	 * 
	 * @deprecated The result of this method is random, if the attributes of subtypes are included
	 *             and there are multiple subtypes that define an attribute with the given name, or
	 *             if the attribute is overridden. Use {@link TLClass#getPart(String)} or
	 *             {@link TLClass#getLocalParts()} instead, as they don't include subtypes, or use a
	 *             method that returns a list of attributes like
	 *             {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)}, as they can return all
	 *             matching attributes, not just one.
	 */
	@Deprecated
	public static TLStructuredTypePart getMetaAttribute(TLClass metaElement, String aName,
			boolean includeSuperMetaElements, boolean includeSubMetaElements) throws NoSuchAttributeException {
		TLStructuredTypePart attribute =
			MetaElementUtil.getMetaAttributeOrNull(metaElement, aName, includeSuperMetaElements, includeSubMetaElements);
		if (attribute == null) {
			throw new NoSuchAttributeException(
				"No attribute '" + aName + "' in type '" + metaElement.getModule().getName() + ":"
					+ metaElement.getName() + "'.");
		}
		return attribute;
	}

	/**
	 * Same as {@link TLClass#getPart(String)}.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated The result of this method is random, if the attributes of subtypes are included
	 *             and there are multiple subtypes that define an attribute with the given name, or
	 *             if the attribute is overridden. Use {@link TLClass#getPart(String)} or
	 *             {@link TLClass#getLocalParts()} instead, as they don't include subtypes, or use a
	 *             method that returns a list of attributes like
	 *             {@link TLModelUtil#getMetaAttributesInHierarchy(TLClass)}, as they can return all
	 *             matching attributes, not just one.
	 */
	@Deprecated
	public static TLStructuredTypePart getMetaAttributeOrNull(TLClass metaElement, String aName,
			boolean includeSuperMetaElements, boolean includeSubMetaElements) {
		// Note: The absurd combinations of sub and super are no longer supported.
		return metaElement.getPart(aName);
	}

	/**
	 * Get the MetaElementHolder.
	 * 
	 * @return the MetaElementHolder. May be <code>null</code>.
	 * @throws MetaElementException
	 *         is getting the holder fails
	 */
	public static MetaElementHolder getMetaElementHolder(TLClass metaElement) throws MetaElementException {
		return (MetaElementHolder) metaElement.getScope();
	}

	/**
	 * Get the type name of given type.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @return the type name of given type.
	 * 
	 * @deprecated Use {@link TLClass#getName()}
	 */
	@Deprecated
	public static String getMetaElementType(TLClass metaElement) {
		return metaElement.getName();
	}

	/**
	 * Add an attribute to the given type.
	 * 
	 * @param aMetaAttribute
	 *        the attribute. Must not be <code>null</code>.
	 * @throws DuplicateAttributeException
	 *         if the given type has a attribute with the same name.
	 * @throws IllegalArgumentException
	 *         if aMetaAttribute is <code>null</code> or cannot be added to the given type.
	 * @throws AttributeException
	 *         if adding fails for other reasons
	 */
	public static void addMetaAttribute(TLClass metaElement, TLStructuredTypePart aMetaAttribute)
			throws DuplicateAttributeException, IllegalArgumentException, AttributeException {
		addMetaAttribute(metaElement, TLModelUtil.getLocalMetaAttributes(metaElement).size(), aMetaAttribute);
	}

	/**
	 * Add a attribute to the given type.
	 * 
	 * @param aMetaAttribute
	 *        the attribute. Must not be <code>null</code>.
	 * @param index
	 *        The index in {@link TLClass#getLocalParts()} where the new attribute should be added.
	 * @throws DuplicateAttributeException
	 *         if the given type has a attribute with the same name.
	 * @throws IllegalArgumentException
	 *         if aMetaAttribute is <code>null</code> or cannot be added to the given type.
	 * @throws AttributeException
	 *         if adding fails for other reasons
	 */
	public static void addMetaAttribute(TLClass metaElement, int index, TLStructuredTypePart aMetaAttribute)
			throws DuplicateAttributeException, IllegalArgumentException, AttributeException {
		switch (metaElement.getModelKind()) {
			case ASSOCIATION:
				addAssociationPart((TLAssociation) metaElement, index, aMetaAttribute);
				break;
			case CLASS:
				addClassPart(metaElement, index, aMetaAttribute);
				break;
			default:
				throw new UnsupportedOperationException("Type: " + metaElement);
		}
	}

	private static void addClassPart(TLClass clazz, int index, TLStructuredTypePart part) {
		if (!(part instanceof TLClassPart)) {
			throw new IllegalArgumentException("Expected instance of " + TLClassPart.class.getSimpleName()
				+ " but got: " + Utils.debug(part));
		}
		TLClassPart classPart = (TLClassPart) part;

		TLStructuredTypePart existingPart = clazz.getPart(classPart.getName());
		if (existingPart != null) {
			// Is override.
			if (existingPart.getOwner() == clazz) {
				throw new TopLogicException(I18NConstants.DUPLICATE_ATTRIBUTE__ID.fill(classPart.getName()));
			}
			copyCharacteristics((TLClassPart) existingPart, classPart);
		}

		clazz.getLocalClassParts().add(index, classPart);
		classPart.updateDefinition();
	}

	private static void copyCharacteristics(TLClassPart source, TLClassPart destination) {
		TLCharacteristicsCopier.copyCharacteristics(source, destination);
	}

	private static void addAssociationPart(TLAssociation association, int index, TLStructuredTypePart part) {
		if (!(part instanceof TLAssociationPart)) {
			throw new IllegalArgumentException("Expected instance of ");
		}
		// Add attribute
		association.getAssociationParts().add(index, (TLAssociationPart) part);
	}

	/**
	 * Remove an attribute from the given type. Note: this does not delete the attribute!
	 * 
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @throws NoSuchAttributeException
	 *         if the given attribute does not belong to the given type.
	 * @throws IllegalArgumentException
	 *         if aMetaAttribute is <code>null</code>.
	 * @throws AttributeException
	 *         if removing fails
	 */
	public static void removeMetaAttribute(TLClass kbBasedMetaElement, TLStructuredTypePart attribute) {
		// Check existence
		TLStructuredType owner = attribute.getOwner();
		if (owner != kbBasedMetaElement) {
			throw new NoSuchAttributeException(
				"Attribute '" + attribute + "' not owned by '" + kbBasedMetaElement + "', but '"
					+ owner + "'.");
		}

		// Remove
		attribute.tSetData(ApplicationObjectUtil.META_ELEMENT_ATTR, null);
		updateDefinitionsOfOverrides(kbBasedMetaElement, attribute);
	}

	private static void updateDefinitionsOfOverrides(TLClass kbBasedMetaElement,
			TLStructuredTypePart aMetaAttribute) {
		if (kbBasedMetaElement.getModelKind() == ModelKind.ASSOCIATION) {
			/* Workaround for the hack that TLAssociations are TLClasses, too, in the persistent
			 * model. */
			return;
		}
		Set<TLClassPart> overridingParts = TLModelUtil.getOverridingParts(kbBasedMetaElement, aMetaAttribute.getName());
		for (TLClassPart overridingPart : overridingParts) {
			overridingPart.updateDefinition();
		}
	}

	public static final String STATE_PREFIX = "element.state.";
	public static final String STATE_WHITE = STATE_PREFIX + "white";
	public static final String STATE_GREEN = STATE_PREFIX + "green";
	public static final String STATE_YELLOW = STATE_PREFIX + "yellow";
	public static final String STATE_RED = STATE_PREFIX + "red";
	/** The possible states of a supplier. */
	public static final String[] STATE_MAP = new String[] { STATE_RED, STATE_YELLOW, STATE_GREEN, STATE_WHITE };

}
