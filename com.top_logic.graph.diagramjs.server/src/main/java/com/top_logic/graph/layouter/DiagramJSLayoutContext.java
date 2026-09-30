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
 * Extended {@link LayoutContext} for diagram.js server-side layouting with TLModel-specific
 * properties.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F&ouml;rster</a>
 */
public class DiagramJSLayoutContext extends LayoutContext {

	private final LabelProvider _labelProvider;

	private final Collection<Object> _hiddenElements;

	private final Collection<TLType> _hiddenGeneralizations;

	/**
	 * Creates a {@link DiagramJSLayoutContext} with the given arguments.
	 */
	public DiagramJSLayoutContext(LayoutDirection direction, LabelProvider labelProvider,
			Collection<Object> hiddenElements, Collection<TLType> hiddenGeneralizations) {
		super(direction);
		_labelProvider = labelProvider;
		_hiddenElements = hiddenElements;
		_hiddenGeneralizations = hiddenGeneralizations;
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
