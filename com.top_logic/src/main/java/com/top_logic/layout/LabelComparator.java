/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;

import com.top_logic.layout.basic.LabelSorter;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.util.TLContext;

/**
 * {@link Comparator} comparing object by their labels.
 * 
 * <p>
 * Since sorting requires multiple compare operations, labels are cached once generated. Therefore,
 * instances of this class should not be reused over a long time. Especially they must not be stored
 * in static members to avoid a memory leak.
 * </p>
 * 
 * <p>
 * For better efficiency, use {@link LabelSorter#sortByLabelInline(java.util.List, LabelProvider)}
 * with the list and {@link MetaResourceProvider#INSTANCE} as {@link LabelProvider} instead of
 * <code>{@link Collections}.sort(list, new LabelComparator()</code>).
 * </p>
 * 
 * <p>
 * In tables, use <code>sortKeyProvider="{@link com.top_logic.layout.LabelMapping}"</code> in
 * combination with <code>comparator="com.top_logic.util.TLCollator"</code> instead of
 * <code>comparator="{@link LabelComparator}"</code>.
 * </p>
 * 
 * @author <a href="mailto:CBR@top-logic.com">Christian Braun</a>
 */
public class LabelComparator implements Comparator<Object> {

	/**
	 * Creates a {@link LabelComparator} using {@link MetaResourceProvider} to generate labels.
	 * 
	 * <p>
	 * Note: Generated labels are cached for the life-time of this comparator. Do not keep a
	 * reference to this comparator for a long time to avoid a memory leak.
	 * </p>
	 */
	public static LabelComparator newCachingInstance() {
		return newCachingInstance(MetaResourceProvider.INSTANCE);
	}

	/**
	 * Creates a {@link LabelComparator} using {@link MetaResourceProvider} to generate labels.
	 * 
	 * <p>
	 * Note: Generated labels are cached for the life-time of this comparator. Do not keep a
	 * reference to this comparator for a long time to avoid a memory leak.
	 * </p>
	 */
	public static LabelComparator newCachingInstance(int initialCacheSize) {
		return newCachingInstance(MetaResourceProvider.INSTANCE, initialCacheSize);
	}

	/**
	 * Creates a {@link LabelComparator} using a custom {@link LabelProvider} to generate labels.
	 * 
	 * <p>
	 * Note: Generated labels are cached for the life-time of this comparator. Do not keep a
	 * reference to this comparator for a long time to avoid a memory leak.
	 * </p>
	 */
	public static LabelComparator newCachingInstance(LabelProvider labelProvider) {
		return newCachingInstance(labelProvider, 16);
	}

	/**
	 * Creates a {@link LabelComparator} using a custom {@link LabelProvider} to generate labels and
	 * a given initial cache size.
	 * 
	 * <p>
	 * Note: Generated labels are cached for the life-time of this comparator. Do not keep a
	 * reference to this comparator for a long time to avoid a memory leak.
	 * </p>
	 */
	public static LabelComparator newCachingInstance(LabelProvider labelProvider, int initialCacheSize) {
		return newInstance(CachedLabelProvider.newInstance(labelProvider, initialCacheSize));
	}

	/**
	 * Creates a {@link LabelComparator} using directly the given {@link LabelProvider} without
	 * caching generated labels.
	 */
	public static LabelComparator newInstance(LabelProvider labelProvider) {
		return newInstance(labelProvider, Collator.getInstance(TLContext.getLocale()));
	}

	/**
	 * Creates a {@link LabelComparator} using directly the given {@link LabelProvider} without
	 * caching generated labels.
	 * 
	 * @param labelProvider
	 *        {@link LabelProvider} that actually computes the labels.
	 * @param collator
	 *        The {@link Collator} that is used to compare labels.
	 */
	public static LabelComparator newInstance(LabelProvider labelProvider, Collator collator) {
		return new LabelComparator(labelProvider, collator);
	}

	private final Collator _collator;

	private final LabelProvider _labelProvider;

	/**
	 * Create a new CachedLabelComparator
	 * 
	 * @param labelProvider
	 *        The {@link LabelProvider} to generate labels with.
	 * @param collator
	 *        The {@link Collator} to compare labels with.
	 * 
	 * @see #newCachingInstance(LabelProvider)
	 * @see #newInstance(LabelProvider)
	 */
	protected LabelComparator(LabelProvider labelProvider, Collator collator) {
		_labelProvider = labelProvider;
		_collator = collator;
	}

	/**
	 * Compares objects via their labels
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}

		if (o1 == null) {
			return -1;
		}

		if (o2 == null) {
			return 1;
		}

		String label1 = _labelProvider.getLabel(o1);
		String label2 = _labelProvider.getLabel(o2);

		if (label1 == null && label2 == null) {
			return 0;
		}

		if (label1 == null) {
			return -1;
		}

		if (label2 == null) {
			return 1;
		}

		return _collator.compare(label1, label2);
	}

}
