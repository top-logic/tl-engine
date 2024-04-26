/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.layout.LabelProvider;

/**
 * Display the content of a collection using the {@link LabelProvider inner provider}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionLabelProvider implements LabelProvider {

	/** The label provider for the objects in the collection. */
	private final LabelProvider _inner;

	/** The separator to be displayed between two objects. */
	private final String _separator;

	/**
	 * Creates a {@link CollectionLabelProvider} which uses <code>", "</code> as separator and the
	 * {@link MetaResourceProvider}.
	 */
	public CollectionLabelProvider() {
		this(MetaResourceProvider.INSTANCE, ", ");
	}

	/**
	 * Creates a {@link CollectionLabelProvider}.
	 * 
	 * @param inner
	 *        The inner label provider for writing the object in the collection, must not be
	 *        <code>null</code>.
	 * @param separator
	 *        The separator to divide the objects in the collection, must not be <code>null</code>.
	 */
	public CollectionLabelProvider(LabelProvider inner, String separator) {
		_inner = Objects.requireNonNull(inner);
		_separator = Objects.requireNonNull(separator);
	}

	@Override
	public String getLabel(Object object) {
		Collection<?> collection = (Collection<?>) object;
		if (collection == null || collection.isEmpty()) {
			return "";
		}
		boolean writeSeparator = false;
		StringBuilder result = new StringBuilder();
		if (!isOrdered(collection)) {
			collection = sort(collection);
		}
		for (Object value : collection) {
			if (writeSeparator) {
				result.append(_separator);
			} else {
				writeSeparator = true;
			}
			result.append(_inner.getLabel(value));
		}
		return (result.toString());
	}

	private boolean isOrdered(Collection<?> collection) {
		return collection instanceof List
			|| collection instanceof LinkedHashSet
			|| collection instanceof TreeSet;
	}

	private List<?> sort(Collection<?> collection) {
		List<?> list = list(collection);
		list.sort(ComparableComparator.INSTANCE);
		return list;
	}

}
