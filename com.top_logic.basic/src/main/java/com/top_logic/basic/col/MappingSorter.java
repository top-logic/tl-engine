/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ObjectMapping.UnwrapMapping;


/**
 * Helper class for efficient sorting according to a substitution value produces
 * by a {@link Mapping}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappingSorter {

	/**
	 * Sort the given collection of objects according to the mapped values
	 * produced by the given {@link Mapping} compared with the given
	 * {@link Comparator}.
	 * 
	 * @param objects
	 *        The objects to sort.
	 * @param mapping
	 *        A {@link Mapping} that maps the given objects to values that are
	 *        compared to each other with the given comparator.
	 * @param valueComparator
	 *        The comparator that is used to compare the mapped values.
	 * @return The list of objects sorted according to the given mapping and
	 *         comparator. For the result, the following holds for all
	 *         <code>n</code>:
	 *         <code>comparator.compare(mapping.map(result.get(n)), mapping.map(result.get(n+1))) <= 0</code>
	 * 
	 * @see MappingSorter#sortByMapping(Collection, Mapping, Comparator,
	 *      Comparator) Sorting with an additional global order that is used, if
	 *      the given value comparator is not able to distinguish objects.
	 */
	public static <S, D> List<S> sortByMapping(Collection<S> objects, Mapping<? super S, ? extends D> mapping, Comparator<? super D> valueComparator) {
		return MappingSorter.<S, D>sortByMapping(objects, mapping, valueComparator, Equality.INSTANCE);
	}

	/**
	 * Same as {@link #sortByMapping(Collection, Mapping, Comparator)} with an
	 * additional Comparator to establish a total order.
	 * 
	 * @param globalOrder
	 *        A {@link Comparator} that is used, if the given value comparator
	 *        applied to the mapping values is not able to distinguish objects.
	 */
	public static <S, D> List<S> sortByMapping(Collection<? extends S> objects, Mapping<? super S, ? extends D> mapping, Comparator<? super D> valueComparator, Comparator<? super S> globalOrder) {
		List<ObjectMapping<S, D>> wrappedObjects = Mappings.map(new ObjectMapping.WrapMapping<>(mapping), objects);
		
		Collections.sort(wrappedObjects,
			new ObjectMapping.SubstitutionComparator<>(valueComparator, globalOrder));
		
		return Mappings.mapInline(UnwrapMapping.<S>unwrapMapping(), wrappedObjects);
	}
	
	/**
	 * Sort the given list of objects according to the mapped values produced by
	 * the given {@link Mapping} compared with the given {@link Comparator}.
	 * 
	 * <p>
	 * The given list of objects is modified inline. After the call returns, the
	 * following holds for the given objects list for all <code>n</code>:
	 * <code>comparator.compare(mapping.map(result.get(n)), mapping.map(result.get(n+1))) <= 0</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The given list of objects must allow to store arbitrary
	 * objects, because its memory is temporarily used to store placeholder
	 * objects.
	 * </p>
	 * 
	 * @param objects
	 *        The objects to sort.
	 * @param mapping
	 *        A {@link Mapping} that maps the given objects to values that are
	 *        compared to each other with the given comparator.
	 * @param valueComparator
	 *        The comparator that is used to compare the mapped values.
	 */
	public static <S, D> List<S> sortByMappingInline(List<S> objects, Mapping<? super S, ? extends D> mapping, Comparator<? super D> valueComparator){
		return sortByMappingInline(objects, mapping, valueComparator, Equality.INSTANCE);
	}

	/**
	 * Same as {@link #sortByMappingInline(List, Mapping, Comparator)} with an
	 * additional Comparator to establish a total order.
	 * 
	 * @param globalOrder
	 *        A {@link Comparator} that is used, if the given value comparator
	 *        applied to the mapping values is not able to distinguish objects.
	 */
	public static <S, D> List<S> sortByMappingInline(List<S> objects, Mapping<? super S, ? extends D> mapping, Comparator<? super D> valueComparator, Comparator<? super S> globalOrder) {
		// Do mapping, which is undone in case of exception
		List<ObjectMapping<S, D>> wrappedObjects =
			Mappings.mapInlineRestorable(new ObjectMapping.WrapMapping<>(mapping), objects);
		
		List<S> result;
		try {
			// Sort according to the mapping (in case of exception Collections.sort(..) (jdk 1.5) assures original order)
			Collections.sort(wrappedObjects,
				new ObjectMapping.SubstitutionComparator<>(valueComparator, globalOrder));
		} catch (RuntimeException e) {
			Logger.error("Could not perform sort of mapped list elements, because of following reason: " + e.getMessage(), e, MappingSorter.class);
			throw(e);
		} finally {
			// Undo mapping, ignoring, whether sort was successful, or not
			result = Mappings.mapInline(UnwrapMapping.<S>unwrapMapping(), wrappedObjects);
		}
		
		return result;
	}
}
