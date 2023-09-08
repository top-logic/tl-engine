/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import com.top_logic.basic.col.StructureView;
import com.top_logic.mig.html.ModelBuilder;

/**
 * {@link ModelBuilder} that provides tree-structured models to components.
 * 
 * <p>
 * In a structure model,
 * </p>
 * 
 * <ul>
 * <li>the parent relation only depends on the node model, not on the context, and</li>
 * <li>each non-root node has an unique parent.</li>
 * </ul>
 * 
 * @see TreeModelBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface StructureModelBuilder<N> extends TreeBuilderBase<N>, StructureView<N> {

	// Pure sum interface.

}
