/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.InstanceFilter;
import com.top_logic.basic.util.Utils;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.filter.ModelFilter;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ModelBasedSearch.SearchConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Utils for working with {@link TLType}s in the search.
 * <p>
 * The search has for example some relaxed rules about what types are compatible. Those don't belong
 * into the {@link TLModelUtil} and are therefore collected here.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchTypeUtil {

	/**
	 * Whether the given {@link TLClass} has subtypes.
	 * <p>
	 * More specific: Whether it has (indirect) subtypes that are not filtered out by the
	 * {@link ModelFilter}.
	 * </p>
	 * <p>
	 * Checking just the direct subtypes is not enough, as they might have been filtered out by the
	 * {@link ModelFilter}.
	 * </p>
	 * 
	 * @param tlClass
	 *        Is not allowed to be null.
	 * @param searchName
	 *        Is not allowed to be null.
	 */
	public static boolean hasSubtypes(TLClass tlClass, String searchName) {
		return !applyModelFilter(TLModelUtil.getTransitiveSpecializations(tlClass), searchName).isEmpty();
	}

	/**
	 * The direct subtypes of the given {@link TLClass}.
	 * 
	 * @param tlClass
	 *        Is not allowed to be null.
	 * @param searchName
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Collection<TLClass> getDirectSubtypes(TLClass tlClass, String searchName) {
		return applyModelFilter(tlClass.getSpecializations(), searchName);
	}

	/**
	 * Returns all {@link TLType}s that are "compatible" with the given {@link TLType} when
	 * searching.
	 * <p>
	 * "Compatible" means for example, that all number types are compatible with one another.
	 * </p>
	 */
	public static Collection<? extends TLType> getCompatibleTypes(TLType contextType) {
		if (contextType instanceof TLPrimitive) {
			List<TLType> result = new ArrayList<>();
			for (TLPrimitive primitive : getAllPrimitiveTypes()) {
				if (SearchTypeUtil.areComparableWithEachOther(contextType, primitive)) {
					result.add(primitive);
				}
			}
			return result;
		}
		if (contextType instanceof TLClass) {
			return TLModelUtil.getReflexiveTransitiveGeneralizations((TLClass) contextType);
		}
		return singletonList(contextType);
	}

	private static List<TLPrimitive> getAllPrimitiveTypes() {
		return TLModelUtil.getDataTypes(ModelService.getApplicationModel());
	}

	/**
	 * Returns whether the given {@link TLType}s are compatible with each other for the search.
	 * <p>
	 * All number kinds are compatible with each other and boolean/tristate are compatible.
	 * </p>
	 */
	public static boolean areComparableWithEachOther(TLType expectedType, TLType actualType) {
		if (TLModelUtil.isCompatibleType(expectedType, actualType)) {
			return true;
		}
		if (!((expectedType instanceof TLPrimitive) && (actualType instanceof TLPrimitive))) {
			return false;
		}
		Kind expectedKind = ((TLPrimitive) expectedType).getKind();
		Kind actualKind = ((TLPrimitive) actualType).getKind();
		Set<TLPrimitive.Kind> bothKinds = EnumSet.of(expectedKind, actualKind);
		if (getBooleanLikeKinds().containsAll(bothKinds)) {
			return true;
		}
		if (getNumberLikeKinds().containsAll(bothKinds)) {
			return true;
		}
		return false;
	}

	/**
	 * The {@link TLPrimitive} {@link Kind}s for booleans.
	 */
	public static Set<TLPrimitive.Kind> getBooleanLikeKinds() {
		return EnumSet.of(TLPrimitive.Kind.BOOLEAN, TLPrimitive.Kind.TRISTATE);
	}

	/**
	 * The {@link TLPrimitive} {@link Kind}s for numbers.
	 */
	public static Set<TLPrimitive.Kind> getNumberLikeKinds() {
		return EnumSet.of(TLPrimitive.Kind.INT, TLPrimitive.Kind.FLOAT);
	}

	/**
	 * Whether the given {@link TLAssociation} implements a {@link TLReference}.
	 * 
	 * @param association
	 *        Is not allowed to be null.
	 */
	public static boolean isReferenceImpl(TLAssociation association) {
		for (TLAssociationPart part : association.getAssociationParts()) {
			if (SearchTypeUtil.isReferenceImpl(part)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether this {@link TLStructuredTypePart} is a {@link TLAssociationEnd} that implements a
	 * {@link TLReference}.
	 * <p>
	 * <b>Important:</b> Only the outgoing part of the TLReference is marked like this. The "self"
	 * part looks like a "real" association.
	 * </p>
	 * 
	 * @see TLAssociationEnd#getReference()
	 */
	public static boolean isReferenceImpl(TLStructuredTypePart part) {
		if (!(part instanceof TLAssociationEnd)) {
			return false;
		}
		return isReferenceImpl((TLAssociationEnd) part);
	}

	/**
	 * Whether this {@link TLAssociationEnd} implements a {@link TLReference}.
	 * <p>
	 * <b>Important:</b> Only the outgoing part of the TLReference is marked like this. The "self"
	 * part looks like a "real" association.
	 * </p>
	 * 
	 * @see TLAssociationEnd#getReference()
	 */
	public static boolean isReferenceImpl(TLAssociationEnd associationEnd) {
		return associationEnd.getReference() != null;
	}

	/**
	 * Whether there are {@link TLAssociationPart}s that point to the given {@link TLType}.
	 * 
	 * @param tlClass
	 *        Is not allowed to be null.
	 * @param searchName
	 *        Is not allowed to be null.
	 */
	public static boolean isAssociated(TLClass tlClass, String searchName) {
		return !getPartsPointingTo(tlClass, searchName).isEmpty();
	}

	/**
	 * The {@link TLAssociationPart}s that point to the given type.
	 * <p>
	 * The exact result depends on:
	 * {@link com.top_logic.model.search.ModelBasedSearch.SearchConfig#getIncludeSubtypeUsages()}
	 * </p>
	 */
	public static List<TLAssociationPart> getPartsPointingTo(TLClass type, String searchName) {
		List<TLAssociationPart> parts = new ArrayList<>();
		for (TLAssociation association : SearchTypeUtil.getAssociations(type.getModel(), searchName)) {
			parts.addAll(getPartsPointingTo(type, association, searchName));
		}
		return applyModelFilter(parts, searchName);
	}

	static Collection<TLAssociationPart> getPartsPointingTo(TLClass type, TLAssociation association,
			String searchName) {
		if (SearchTypeUtil.isReferenceImpl(association)) {
			return emptyList();
		}
		Collection<TLAssociationPart> matchingParts = new ArrayList<>();
		List<TLAssociationPart> supportedParts = SearchTypeUtil.getSupportedParts(association);
		for (TLAssociationPart part : supportedParts) {
			TLType targetType = part.getType();
			if (!(targetType instanceof TLClass)) {
				continue;
			}
			TLClass targetClass = (TLClass) targetType;
			if (isRelevant(type, targetClass, searchName)) {
				matchingParts.add(part);
			}
		}
		return matchingParts;
	}

	private static boolean isRelevant(TLClass type, TLClass targetClass, String searchName) {
		if (ModelBasedSearch.getInstance().getSearchConfig(searchName).getIncludeSubtypeUsages()) {
			for (TLClass subtype : TLModelUtil.getConcreteSpecializations(type)) {
				if (TLModelUtil.isGeneralization(targetClass, subtype)) {
					return true;
				}
			}
			return false;
		}
		return TLModelUtil.isGeneralization(targetClass, type);
	}

	/**
	 * All global {@link TLAssociation}s in the given {@link TLModel}.
	 */
	public static Collection<TLAssociation> getAssociations(TLModel model, String searchName) {
		Collection<TLAssociation> associations = new ArrayList<>();
		Collection<TLType> types = TLModelUtil.getAllGlobalTypes(model);
		for (TLType type : types) {
			if (type instanceof TLAssociation) {
				associations.add((TLAssociation) type);
			}
		}
		return applyModelFilter(associations, searchName);
	}

	static List<TLAssociationPart> getOtherEnds(TLAssociationPart incomingPart) {
		List<TLAssociationPart> result = new ArrayList<>();
		for (TLAssociationPart part : getSupportedParts(incomingPart.getOwner())) {
			if (Utils.equals(part, incomingPart)) {
				continue;
			}
			result.add(part);
		}
		return result;
	}

	static List<TLAssociationPart> getSupportedParts(TLAssociation association) {
		// Other TLAssociationParts are not yet supported.
		return FilterUtil.filterList(new InstanceFilter(TLAssociationEnd.class), association.getAssociationParts());
	}

	/**
	 * Whether the given {@link TLStructuredType} has {@link TLStructuredTypePart}s.
	 */
	public static Boolean hasParts(TLStructuredType type, String searchName) {
		if (type instanceof TLClass) {
			TLClass classType = (TLClass) type;
			List<? extends TLClassPart> allParts = classType.getAllClassParts();
			List<TLClassPart> filteredParts = applyModelFilter(allParts, searchName);
			return !filteredParts.isEmpty();
		}
		if (type instanceof TLAssociation) {
			TLAssociation linkType = (TLAssociation) type;
			Collection<TLAssociationPart> allParts = linkType.getAssociationParts();
			List<TLAssociationPart> filteredParts = applyModelFilter(allParts, searchName);
			return !(filteredParts.isEmpty());
		}
		throw unexpectedStructuredType(type);
	}

	/**
	 * Whether there are {@link TLReference}s that point to the given {@link TLType}.
	 * 
	 * @param tlType
	 *        Is not allowed to be null.
	 * @param searchName
	 *        Is not allowed to be null.
	 */
	public static boolean isReferenced(TLStructuredType tlType, String searchName) {
		return !getReferences(tlType, searchName).isEmpty();
	}

	/**
	 * The {@link TLReference}s that point to the given {@link TLType}.
	 * 
	 * @param tlType
	 *        Is not allowed to be null.
	 * @param searchName
	 *        Is not allowed to be null.
	 */
	public static List<TLReference> getReferences(TLStructuredType tlType, String searchName) {
		List<TLReference> result = new ArrayList<>();
		if (tlType instanceof TLClass) {
			TLClass tlClass = (TLClass) tlType;
			for (TLType relevantClass : getRelevantClasses(tlClass, searchName)) {
				addUsage(result, relevantClass);
			}
		} else if (tlType instanceof TLAssociation) {
			addUsage(result, tlType);
		} else {
			throw unexpectedStructuredType(tlType);
		}
		return applyModelFilter(result, searchName);
	}

	private static Set<TLClass> getRelevantClasses(TLClass tlClass, String searchName) {
		if (getSearchConfig(searchName).getIncludeSubtypeUsages()) {
			return TLModelUtil.getGeneralizationsOfConcreteSpecializations(tlClass.getModel(), tlClass);
		}
		return TLModelUtil.getReflexiveTransitiveGeneralizations(tlClass);
	}

	private static SearchConfig getSearchConfig(String configName) {
		return ModelBasedSearch.getInstance().getSearchConfig(configName);
	}

	private static void addUsage(List<TLReference> result, TLType contextType) {
		Collection<TLStructuredTypePart> usage = TLModelUtil.getUsage(ModelService.getApplicationModel(), contextType);
		for (TLStructuredTypePart part : usage) {
			if (TLModelUtil.isReference(part)) {
				result.add((TLReference) part);
			}
		}
	}

	static <T extends TLModelPart> List<T> applyModelFilter(Collection<? extends T> modelParts, String searchName) {
		return ModelBasedSearch.getInstance().filterModel(modelParts, searchName);
	}

	private static UnreachableAssertion unexpectedStructuredType(TLStructuredType type) throws UnreachableAssertion {
		throw new UnreachableAssertion(
			"Unexpected " + TLStructuredType.class.getSimpleName() + ": " + Utils.debug(type));
	}

}
