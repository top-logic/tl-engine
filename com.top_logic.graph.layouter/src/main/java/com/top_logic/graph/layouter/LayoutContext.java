/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Context for graph layouting.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutContext {

	private LayoutDirection _direction;

	private LabelProvider _labelProvider;

	private boolean _showTableInterfaceTypes;

	/**
	 * Creates a {@link LayoutContext} with the {@link MetaResourceProvider} and
	 * {@link LayoutDirection} vertical from sink as default.
	 */
	public LayoutContext() {
		this(LayoutDirection.VERTICAL_FROM_SINK, MetaResourceProvider.INSTANCE, false);
	}

	/**
	 * Creates a {@link LayoutContext} with the given arguments.
	 */
	public LayoutContext(LayoutDirection direction, LabelProvider labelProvider, boolean showTableInterfaceTypes) {
		_direction = direction;
		_labelProvider = labelProvider;
		_showTableInterfaceTypes = showTableInterfaceTypes;
	}

	/**
	 * the layouting direction.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

	/**
	 * @see #getDirection()
	 */
	public void setDirection(LayoutDirection direction) {
		_direction = direction;
	}

	/**
	 * the label provider for resources.
	 */
	public LabelProvider getLabelProvider() {
		return _labelProvider;
	}

	/**
	 * @see #getLabelProvider()
	 */
	public void setLabelProvider(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	/**
	 * True if TableInterface types should be displayed, otherwise false.
	 */
	public boolean showTableInterfaceTypes() {
		return _showTableInterfaceTypes;
	}

	/**
	 * @see #showTableInterfaceTypes()
	 */
	public void setShowTableInterfaceTypes(boolean showTableInterfaceTypes) {
		_showTableInterfaceTypes = showTableInterfaceTypes;
	}

}
