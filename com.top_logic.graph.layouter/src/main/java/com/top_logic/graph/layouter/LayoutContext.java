/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

import java.util.Collection;

import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLType;

/**
 * Context for graph layouting.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutContext {

	private final LayoutDirection _direction;

	private final LabelProvider _labelProvider;

	private final Collection<Object> _hiddenElements;

	private final Collection<TLType> _hiddenGeneralizations;

	/**
	 * Creates a {@link LayoutContext} with the given arguments.
	 */
	public LayoutContext(LayoutDirection direction, LabelProvider labelProvider, Collection<Object> hiddenElements,
			Collection<TLType> hiddenGeneralizations) {
		_direction = direction;
		_labelProvider = labelProvider;
		_hiddenElements = hiddenElements;
		_hiddenGeneralizations = hiddenGeneralizations;
	}

	/**
	 * the layouting direction.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

	/**
	 * the label provider for resources.
	 */
	public LabelProvider getLabelProvider() {
		return _labelProvider;
	}

	/**
	 * Collection of objects that should be hidden.
	 */
	public Collection<Object> getHiddenElements() {
		return _hiddenElements;
	}

	/**
	 * Collection of objects that should be hidden as generalizations.
	 * 
	 * <p>
	 * References to this set of types are still displayed.
	 * </p>
	 */
	public Collection<TLType> getHiddenGeneralizations() {
		return _hiddenGeneralizations;
	}

}
