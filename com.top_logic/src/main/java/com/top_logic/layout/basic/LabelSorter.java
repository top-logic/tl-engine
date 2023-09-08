/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.ObjectMapping;
import com.top_logic.basic.col.ObjectMapping.UnwrapMapping;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelMapping;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.util.TLContext;

/**
 * Helper class for efficient sorting according to object labels.
 * 
 * <p>
 * This is a short-cut for using {@link MappingSorter} with a
 * {@link LabelMapping}.
 * </p>
 * 
 * <p>
 * Note: Computing the label through the {@link MetaResourceProvider} chain is a
 * non-trivial operation. Even for a small number of objects,
 * {@link LabelSorter#sortByLabelInline(List, LabelProvider)} is by a factor of
 * 10 faster than directly using {@link Collections#sort(List, Comparator)} with
 * {@link LabelComparator}.
 * </p>
 * 
 * @see MappingSorter
 * @see LabelMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelSorter {

	/**
	 * Sort the given collection of objects according to their labels provided
	 * by the given {@link LabelProvider}.
	 * 
	 * @param objects
	 *        The list of objects to sort.
	 * @param labelProvider
	 *        The {@link LabelProvider} that delivers the labels according to
	 *        which the objects are sorted.
	 * @return The sorted list of objects.
	 */
	public static <T> List<T> sortByLabel(Collection<T> objects, LabelProvider labelProvider) {
		List<ObjectMapping<T, String>> sortedObjects = Mappings.map(new ObjectLabelMapping<>(labelProvider), objects);
		Collections.sort(sortedObjects,
			new StringSubstitutionComparator(Collator.getInstance(TLContext.getLocale())));
		return Mappings.mapInline(UnwrapMapping.<T>unwrapMapping(), sortedObjects);
	}

	/**
	 * Sort the given list of objects according to their labels provided by the
	 * given {@link LabelProvider}.
	 * 
	 * <p>
	 * The given list of objects contains the result of the sorting after this
	 * call returns.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The given list of objects must allow to store arbitrary
	 * objects, because its memory is temporarily used to store placeholder
	 * objects.
	 * </p>
	 * 
	 * @param objects
	 *        The list of objects to sort.
	 * @param labelProvider
	 *        The {@link LabelProvider} that delivers the labels according to
	 *        which the objects are sorted.
	 */
	public static <T> void sortByLabelInline(List<T> objects, LabelProvider labelProvider) {
		List<ObjectMapping<T, String>> wrappedObjects =
			Mappings.mapInlineRestorable(new ObjectLabelMapping<>(labelProvider), objects);
		Collections.sort(wrappedObjects,
			new StringSubstitutionComparator(Collator.getInstance(TLContext.getLocale())));
		Mappings.mapInline(UnwrapMapping.unwrapMapping(), wrappedObjects);
	}

	/**
	 * {@link Mapping} that wraps any object into a {@link ObjectMapping} by creating the label
	 * through a {@link LabelProvider}.
	 * 
	 * <p>
	 * This is a short-cut for {@link com.top_logic.basic.col.ObjectMapping.WrapMapping} using a
	 * delegate {@link LabelMapping}.
	 * </p>
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class ObjectLabelMapping<S> implements Mapping<S, ObjectMapping<S, String>> {
		private final LabelProvider	labelProvider;

		public ObjectLabelMapping(LabelProvider labelProvider) {
			this.labelProvider = labelProvider;
		}

		@Override
		public ObjectMapping<S, String> map(S input) {
			return new ObjectMapping<>(input, labelProvider.getLabel(input));
		}
	}

	/**
	 * {@link Comparator} that compares {@link ObjectMapping}s according to their
	 * {@link ObjectMapping#getSubstitution()} assuming that the substitution is
	 * of type {@link String}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class StringSubstitutionComparator implements Comparator<ObjectMapping<?, String>> {
		
		private final Collator collator;
	
		/**
		 * {@link LabelSorter.StringSubstitutionComparator} that compares
		 * {@link ObjectMapping} values according to their
		 * {@link ObjectMapping#getSubstitution() substitution values} and the
		 * given {@link Collator}.
		 * 
		 * <p>
		 * It is assumed that the substitution values are of type {@link String}.
		 * </p>
		 */
		public StringSubstitutionComparator(Collator collator) {
			this.collator = collator;
		}
	
		@Override
		public int compare(ObjectMapping<?, String> o1, ObjectMapping<?, String> o2) {
			return collator.compare(o1.getSubstitution(), o2.getSubstitution());
		}
	}
	
}
