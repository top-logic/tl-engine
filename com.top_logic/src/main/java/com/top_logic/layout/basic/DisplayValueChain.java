/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * {@link DisplayValue} that simply appends more than one {@link DynamicText}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayValueChain extends AbstractDisplayValue {

	/**
	 * Creates a {@link DisplayValue} which the same result as first
	 * {@link DynamicText#append(DisplayContext, Appendable) append} <code>first</code> and than
	 * <code>second</code>.
	 * 
	 * @param first
	 *        The fist {@link DynamicText} to append.
	 * @param second
	 *        The second {@link DynamicText} to append.
	 * @return A {@link DisplayValue} appending <code>first</code> and <code>second</code>.
	 */
	public static DisplayValue newChain(DynamicText first, DynamicText second) {
		return new DisplayValueChain(first, second);
	}

	/**
	 * Creates a {@link DisplayValue} which the same result as appending the given
	 * {@link DynamicText}s in the given order.
	 * 
	 * @param values
	 *        The {@link DynamicText}s to append.
	 */
	public static DisplayValue newChain(DynamicText... values) {
		DynamicText result;
		switch (values.length) {
			case 0:
				result = ConstantDisplayValue.EMPTY_STRING;
				break;
			default:
				result = values[0];
				for (int i = 1; i < values.length; i++) {
					result = newChain(result, values[i]);
				}
		}
		return Fragments.toDisplayValue(result);
	}

	/**
	 * Creates a {@link DisplayValue} which the same result as appending the given
	 * {@link DynamicText}s in the order of the {@link Collection#iterator() collection}.
	 * 
	 * @param values
	 *        The {@link DynamicText}s to append.
	 */
	public static DisplayValue newChain(Collection<? extends DynamicText> values) {
		DynamicText result;
		switch (values.size()) {
			case 0:
				result = ConstantDisplayValue.EMPTY_STRING;
				break;
			default:
				if (values instanceof RandomAccess) {
					result = newChainFromList((List<? extends DynamicText>) values);
				} else {
					result = newChainFromIterator(values.iterator());
				}
		}
		return Fragments.toDisplayValue(result);
	}

	private static DisplayValue newChainFromIterator(Iterator<? extends DynamicText> iterator) {
		DynamicText result = iterator.next();
		while (iterator.hasNext()) {
			result = newChain(result, iterator.next());
		}
		return Fragments.toDisplayValue(result);
	}

	private static DisplayValue newChainFromList(List<? extends DynamicText> values) {
		DynamicText result = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			result = newChain(result, values.get(i));
		}
		return Fragments.toDisplayValue(result);
	}

	private final DynamicText _first;

	private final DynamicText _second;

	private DisplayValueChain(DynamicText first, DynamicText second) {
		_first = first;
		_second = second;
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		_first.append(context, out);
		_second.append(context, out);
	}

}

