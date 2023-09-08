/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.Iterator;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * Default implementation of {@link UnversionedCreations} based on a given list of
 * {@link ObjectCreation}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultUnversionedCreations implements UnversionedCreations {

	private Iterable<ObjectCreation> _items;

	private MetaObject _type;

	/**
	 * Creates a new {@link DefaultUnversionedCreations}.
	 */
	public DefaultUnversionedCreations(MetaObject type, Iterable<ObjectCreation> items) {
		super();
		_items = items;
		_type = type;
	}

	@Override
	public MetaObject getType() {
		return _type;
	}

	@Override
	public Iterator<ObjectCreation> getItems() {
		return _items.iterator();
	}

	@Override
	public String toString() {
		return "UnversionedCreations: type: " + _type + ", items: " + _items;
	}

}

