/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.util.Resources;

/**
 * {@link Comparator} that compares objects according to the
 * {@link DisplayValue#get(DisplayContext) localized text} of some
 * {@link DisplayValue} aspect.
 * 
 * @see #getLocalizable(Object) Looking up the {@link DisplayValue} aspect of the
 *      objects to compare.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractLocalizableComparator implements Comparator {
	
	private final DisplayContext context;
	private final Collator collator;

	public AbstractLocalizableComparator(DisplayContext context) {
		this.context = context;
		this.collator = Collator.getInstance(Resources.getInstance().getLocale());
	}

	@Override
	public final int compare(Object o1, Object o2) {
		DisplayValue i1 = getLocalizable(o1);
		DisplayValue i2 = getLocalizable(o2);
		
		return compareLocalizable(i1, i2);
	}

	/**
	 * Looks up the {@link DisplayValue} aspect of the given object that is used
	 * for comparison.
	 * 
	 * @param object
	 *        The object to compare with this comparator.
	 * @return Some {@link DisplayValue} aspect of the given object.
	 */
	protected abstract DisplayValue getLocalizable(Object object);

	/**
	 * Performs the actual comparison of the given localizable aspects.
	 * 
	 * @param i1
	 *        {@link DisplayValue} aspect of the first object.
	 * @param i2
	 *        {@link DisplayValue} aspect of the second object.
	 * @return See {@link Comparator#compare(Object, Object)}.
	 */
	protected final int compareLocalizable(DisplayValue i1, DisplayValue i2) {
		return collator.compare(i1.get(context), i2.get(context));
	}

}
