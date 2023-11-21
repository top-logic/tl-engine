/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Context for graph layouting.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutContext {

	private final LayoutDirection _direction;

	private final LabelProvider _labelProvider;

	private final Collection<Object> _hiddenElements;

	/**
	 * Creates a {@link LayoutContext} with the {@link MetaResourceProvider} and
	 * {@link LayoutDirection} vertical from sink as default.
	 */
	public LayoutContext() {
		this(LayoutDirection.VERTICAL_FROM_SINK, MetaResourceProvider.INSTANCE, Collections.emptySet());
	}

	/**
	 * Creates a {@link LayoutContext} with the given arguments.
	 */
	public LayoutContext(LayoutDirection direction, LabelProvider labelProvider, Collection<Object> hiddenElements) {
		_direction = direction;
		_labelProvider = labelProvider;
		_hiddenElements = hiddenElements;
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

}
