/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.ListOrderedMap;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.layout.provider.icon.ProxyIconProvider;
import com.top_logic.layout.provider.icon.StaticIconProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.InstancePresentation;
import com.top_logic.model.annotate.TLSortOrder;
import com.top_logic.model.annotate.ui.TLDynamicIcon;
import com.top_logic.model.util.TLModelUtil;

/**
 * Performs operations on a {@link TLModel} like calculating of subclasses of a given
 * {@link TLClass}.
 * <p>
 * This class computes the data every time anew. The subclass {@link TLModelCacheEntry} is used by
 * the {@link TLModelCacheService} to cache the data.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLModelOperations {

	/** The {@link TLModelOperations} instance. */
	public static final TLModelOperations INSTANCE = new TLModelOperations();

	/**
	 * The recursive super classes.
	 * <p>
	 * Includes the given {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is depth-first sorted. Only the first occurrence of a {@link TLClass} in the super
	 * class graph is relevant.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getSuperClasses(TLClass tlClass) {
		return computeSuperClasses(tlClass);
	}

	/** Computes the data for {@link #getSuperClasses(TLClass)}. */
	protected LinkedHashSet<TLClass> computeSuperClasses(TLClass tlClass) {
		LinkedHashSet<TLClass> result = linkedSet();
		result.add(tlClass);
		for (TLClass superClass : tlClass.getGeneralizations()) {
			result.addAll(getSuperClasses(superClass));
		}
		return result;
	}

	/**
	 * The recursive subclasses.
	 * <p>
	 * Includes the given {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is unsorted as subclasses have no order.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getSubClasses(TLClass tlClass) {
		return computeSubClasses(tlClass);
	}

	/** Computes the data for {@link #getSubClasses(TLClass)}. */
	protected Set<TLClass> computeSubClasses(TLClass tlClass) {
		Set<TLClass> result = set();
		result.add(tlClass);
		return addSpecializationsRecursively(result, tlClass);
	}

	private <T extends Set<TLClass>> T addSpecializationsRecursively(T result, TLClass tlClass) {
		for (TLClass subClass : tlClass.getSpecializations()) {
			result.addAll(getSubClasses(subClass));
		}
		return result;
	}

	/**
	 * The tables in which instances of the given {@link TLClass} can be stored.
	 * <p>
	 * This includes tables in which instances of subclasses are stored.
	 * </p>
	 * <p>
	 * The result is unsorted, as the tables are computed from the subclasses. And as subclasses
	 * have no order their tables too have no order.
	 * </p>
	 * 
	 * @return The {@link Map} can be unmodifiable, if it was returned from the cache.
	 */
	public Map<String, ? extends Set<TLClass>> getPotentialTables(TLClass tlClass) {
		return computePotentialTables(tlClass);
	}

	/** Computes the data for {@link #getPotentialTables(TLClass)}. */
	protected Map<String, Set<TLClass>> computePotentialTables(TLClass tlClass) {
		return TLModelUtil.potentialTablesUncached(tlClass, false);
	}

	/**
	 * The {@link TLStructuredTypePart} of the {@link TLClass} with the given
	 * {@link TLStructuredType#getName() name}.
	 * <p>
	 * Searches in the super classes, too, recursively.
	 * </p>
	 * 
	 * @return The requested part, or <code>null</code> if neither this type nor one of its super
	 *         classes has a part with that name.
	 */
	public TLStructuredTypePart getAttribute(TLClass tlClass, String name) {
		/* Don't use "getAllAttributes(TLClass)" here. This way the method returns early without
		 * having to collect every attribute first. */
		for (TLClass superClass : getSuperClasses(tlClass)) {
			for (TLStructuredTypePart attribute : superClass.getLocalParts()) {
				if (attribute.getName().equals(name)) {
					return attribute;
				}
			}
		}
		return null;
	}

	/**
	 * The {@link TLStructuredTypePart}s of the given {@link TLClass} and its super classes,
	 * recursively.
	 * 
	 * @return The {@link List} is ordered by the {@link TLSortOrder}. The {@link List} can be
	 *         unmodifiable, if it was returned from the cache.
	 */
	public List<? extends TLStructuredTypePart> getAllAttributes(TLClass tlClass) {
		if (tlClass.getModelKind() == ModelKind.CLASS) {
			return TLModelUtil.calcAllPartsUncached(tlClass);
		}
		return tlClass.getLocalClassParts();
	}

	/**
	 * Computes the data for {@link #getAllAttributes(TLClass)} and
	 * {@link #getAttribute(TLClass, String)}.
	 */
	protected ListOrderedMap<String, TLStructuredTypePart> computeAllAttributes(TLClass tlClass) {
		if (tlClass.getModelKind() == ModelKind.CLASS) {
			return toMap(TLModelUtil.calcAllPartsUncached(tlClass));
		}
		return toMap(tlClass.getLocalClassParts());
	}

	private ListOrderedMap<String, TLStructuredTypePart> toMap(Iterable<TLClassPart> metaAttributes) {
		ListOrderedMap<String, TLStructuredTypePart> map = new ListOrderedMap<>();
		for (TLStructuredTypePart attribute : metaAttributes) {
			map.put(attribute.getName(), attribute);
		}
		return map;
	}

	/**
	 * The {@link TLClassPart}s of subclasses.
	 * <p>
	 * Does not include the attributes from the {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is unsorted, as subclasses have no order, and therefore their attributes have no
	 * order, too.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClassPart> getAttributesOfSubClasses(TLClass tlClass) {
		return computeAttributesOfSubClasses(tlClass);
	}

	/** Computes the data for {@link #getAttributesOfSubClasses(TLClass)}. */
	protected Set<TLClassPart> computeAttributesOfSubClasses(TLClass tlClass) {
		Set<TLClassPart> result = set();
		Set<TLClass> subClasses = addSpecializationsRecursively(set(), tlClass);
		for (TLClass subClass : subClasses) {
			result.addAll(subClass.getLocalClassParts());
		}
		return result;
	}

	/**
	 * The global {@link TLClass}es in the given {@link TLModel}.
	 * <p>
	 * "Global" means, it is either defined directly in the scope of a {@link TLModule} or
	 * {@link TLModule#getSingletons() singleton}.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getGlobalClasses(TLModel tlModel) {
		return computeGlobalClasses(tlModel);
	}

	/** Computes the data for {@link #getGlobalClasses(TLModel)}. */
	protected Set<TLClass> computeGlobalClasses(TLModel tlModel) {
		return TLModelUtil.getAllGlobalClassesUncached(tlModel);
	}

	/**
	 * Retrieves the {@link IconProvider} for a given {@link TLType}.
	 * 
	 * @see TLDynamicIcon#getIconProvider()
	 */
	public IconProvider getIconProvider(TLType type) {
		return computeIconProvider(type);
	}

	/**
	 * Looks up the first {@link TLDynamicIcon} annotation in the primary generalization hierarchy
	 * and builds an {@link IconProvider} for the given type.
	 * 
	 * @see #getIconProvider(TLType)
	 */
	protected IconProvider computeIconProvider(TLType type) {
		while (type != null) {
			TLDynamicIcon dynamicPresentation = type.getAnnotation(TLDynamicIcon.class);
			if (dynamicPresentation != null) {
				IconProvider dynamic = TypedConfigUtil.createInstance(dynamicPresentation.getIconProvider());
				IconProvider fallback = StaticIconProvider.getInstance(getStaticPresentation(type));
				return new ProxyIconProvider(dynamic, fallback);
			}
			InstancePresentation staticPresentation = type.getAnnotation(InstancePresentation.class);
			if (staticPresentation != null) {
				// Defining static icons on a sub-class overrides a potential dynamic annotation on
				// a super class.
				return StaticIconProvider.getInstance(staticPresentation);
			}

			type = TLModelUtil.getPrimaryGeneralization(type);
		}

		return IconProvider.NONE;
	}

	private InstancePresentation getStaticPresentation(TLType type) {
		while (type != null) {
			InstancePresentation annotation = type.getAnnotation(InstancePresentation.class);
			if (annotation != null) {
				return annotation;
			}

			type = TLModelUtil.getPrimaryGeneralization(type);
		}
		return null;
	}

}
