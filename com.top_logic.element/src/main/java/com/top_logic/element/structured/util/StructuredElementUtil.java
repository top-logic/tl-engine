/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import static com.top_logic.basic.StringServices.debug;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.string.StringServicesShared.isEmpty;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * static Functions operating on  {@link StructuredElement} are found here.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class StructuredElementUtil {
    
    /**
     * Compute the smallest (lowest) common element that is in elements or parent for the elements.
     * 
     * @return null in case of empty or null elements and for elements in disjunct trees.
     */
    public static StructuredElement lowestCommonParent(Collection<StructuredElement> elements) {
        if (elements == null) {
            return null;
        }
        int size = elements.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return CollectionUtil.getFirst(elements);
        }
        
        Iterator<StructuredElement> theElements = elements.iterator();  
        
        // Create pathToParent for stl.
        StructuredElement stl = theElements.next();
        assert stl != null;
        ArrayList<StructuredElement> pathToParent = new ArrayList<>(); 
        while (stl != null) {
            pathToParent.add(stl);
            stl = stl.getParent();
        }
        
        while (theElements.hasNext()) {
            stl = theElements.next();
            assert stl != null;
            while (stl != null) {
                int indexInParentPath = pathToParent.indexOf(stl);
                if (indexInParentPath < 0) { // Not in common path
                    stl = stl.getParent();
                    continue;
                } 
                // remove all elements that are not common.
                while (indexInParentPath > 0) {
                    pathToParent.remove(0);
                    indexInParentPath--;
                }
                break;
            }
            if (stl == null) { 
                return null; // No Common parent
            }
        }
        assert !pathToParent.isEmpty();
        return pathToParent.get(0);
    }

    /**
     * Compute the Common Set of {@link StructuredElement#getStructureContext()}. for elements.
     * 
     * @return EMPTY_SET in case of empty or null elements the RootElement etc.
     *         the returned list may be immutable 
     */
    public static Set<StructuredElement> commonStructureContexts(Collection<StructuredElement> elements) {
        int size ;
        if ((elements == null) || 0 == (size = elements.size())) {
            return Collections.emptySet();
        }
        if (size == 1) {
            StructuredElement theElement = CollectionUtil.getFirst(elements);
            if (!theElement.isRoot()) {
                return Collections.singleton(theElement.getStructureContext());
            }
        }
        Iterator<StructuredElement> theElements = elements.iterator();  
        Set<StructuredElement>      result      = new HashSet<>();
        while (theElements.hasNext()) {
            StructuredElement stl = theElements.next();
            assert stl != null;
            if (stl.isRoot() || result.contains(stl)) {
                continue;
            }
            result.add(stl.getStructureContext());
        }
        return result;
    }

	/**
	 * Resolve the type in the given parent element.
	 * 
	 * @param parent
	 *        The parent under which the child would be. Is not allowed to be null.
	 * @param childType
	 *        The name of the type that the child would have.
	 * @return The resolved type.
	 */
	public static TLClass resolveChildType(StructuredElement parent, String childType) {
		ModelFactory factory = DynamicModelService.getFactoryFor(parent.getStructureName());
		return factory.getNodeType(parent, childType);
	}

	/**
	 * Find all concrete sub-classes of the given abstract class that are valid for instantiation in
	 * the context of the given object.
	 * 
	 * <p>
	 * This search respects context local classes (that are only valid within their contexts).
	 * </p>
	 * 
	 * @param self
	 *        The context object.
	 * @param type
	 *        The abstract base class.
	 * @return All concrete subclasses of the given class that can be instantiated in the given
	 *         context.
	 */
	public static Set<TLClass> getSubclassesForInstantiation(TLObject self, TLClass type) {
		Set<TLClass> result = set();
		collectPotentialInstantiationTypes(result, self, type);
		return result;
	}

	private static void collectPotentialInstantiationTypes(Set<TLClass> result, TLObject self, TLClass type) {
		TLClass localSubtype = getLocalSubtype(self, type);
		if (localSubtype != null) {
			result.add(localSubtype);
			return;
		}
		if (!type.isAbstract()) {
			result.add(type);
		}
		collectPotentialInstantiationSubTypes(result, self, type);
	}

	private static void collectPotentialInstantiationSubTypes(Set<TLClass> result, TLObject self, TLClass type) {
		for (TLClass subtype : type.getSpecializations()) {
			collectPotentialInstantiationTypes(result, self, subtype);
		}
	}

	private static TLClass getLocalSubtype(TLObject self, TLClass globalSuperType) {
		ScopeRef scopeRef = TLAnnotations.getAnnotation(globalSuperType, ScopeRef.class);
		if (scopeRef == null) {
			return null;
		}
		String scopeRefValue = scopeRef.getScopeRef();
		if (isEmpty(scopeRefValue)) {
			return null;
		}
		TLScope scope = HolderType.findScope(self, scopeRefValue);
		if (scope == null) {
			return null;
		}
		String createType = scopeRef.getCreateType();
		TLType localType = scope.getType(createType);
		if (localType == null) {
			logError(createMessageNoLocalType(createType, scope));
			return null;
		}
		if (!(localType instanceof TLClass)) {
			String message = createMessageNotATLClass(localType, scope);
			logError(message);
			return null;
		}
		TLClass localClass = (TLClass) localType;
		if (!isGeneralization(globalSuperType, localClass)) {
			logError(createMessageNotASubtype(localClass, globalSuperType, scope));
			return null;
		}
		return localClass;
	}

	private static String createMessageNoLocalType(String createType, TLScope scope) {
		return "Found no local type of name '" + createType + " in scope '" + debug(scope) + "'.";
	}

	private static String createMessageNotASubtype(TLClass localClass, TLClass globalSuperType, TLScope scope) {
		return "Local type " + qualifiedName(localClass) + " is not a subtype of what is supposed to be"
			+ " its abstract global supertype " + qualifiedName(globalSuperType) + ". Resolved scope: " + debug(scope);
	}

	private static String createMessageNotATLClass(TLType localType, TLScope scope) {
		return "Local type " + qualifiedName(localType) + " is not a TLClass. Resolved scope: " + debug(scope);
	}

	private static void logError(String message) {
		Logger.error(message, StructuredElementUtil.class);
	}

	/**
	 * The recursive list of parents.
	 * <p>
	 * The first element is the direct parent. The last element is the root. If the given element is
	 * the root, the list is therefore empty.
	 * </p>
	 * 
	 * @param element
	 *        Is not allowed to be null.
	 * @return Never null. A new, mutable and resizable {@link List}.
	 */
	public static List<StructuredElement> getParents(StructuredElement element) {
		List<StructuredElement> parents = list();
		StructuredElement parent = element.getParent();
		while (parent != null) {
			parents.add(parent);
			parent = parent.getParent();
		}
		return parents;
	}

}
