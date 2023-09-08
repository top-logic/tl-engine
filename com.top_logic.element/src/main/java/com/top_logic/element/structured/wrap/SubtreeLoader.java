/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import com.top_logic.element.layout.tree.StructuredElementStructureView;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.export.PreloadContext;

/**
 * Utility to preload whole {@link StructuredElement} structures with all relevant information such
 * as types, flex data, and association caches.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubtreeLoader extends SubtreeLoaderBase<StructuredElement> {

	/**
	 * Creates a {@link SubtreeLoader}.
	 */
	public SubtreeLoader() {
		super(StructuredElementStructureView.INSTANCE);
	}

	/**
	 * Creates a {@link SubtreeLoader}.
	 */
	public SubtreeLoader(PreloadContext context) {
		super(StructuredElementStructureView.INSTANCE, context);
	}

	/**
	 * Load the whole sub-tree rooted at the given node.
	 * 
	 * <p>
	 * The preload covers all nodes, their types and attributes, flex data, and association caches.
	 * </p>
	 * 
	 * @param root
	 *        The root of the sub-tree.
	 * @return A {@link PreloadContext} that must be keept until the traversal operation has
	 *         finished.
	 */
	public static PreloadContext load(StructuredElement root) {
		PreloadContext context = new PreloadContext();

		// Loader is not closed to keep the returned context valid.
		@SuppressWarnings("resource")
		SubtreeLoader loader = new SubtreeLoader(context);
		loader.loadTree(root);
		loader.loadValues();
		loader.clearLoadedNodes();
		return context;
	}


}