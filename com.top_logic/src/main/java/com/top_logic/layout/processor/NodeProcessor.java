/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.layout.processor.Expansion.Buffer;

/**
 * Callback interface announcing a {@link LayoutModelConstants#INCLUDE_ELEMENT} to be processed
 * during layout template {@link Expansion}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NodeProcessor {

	/**
	 * Requests including a referenced template layout.
	 * 
	 * @param reference
	 *        The {@link LayoutModelConstants#INCLUDE_ELEMENT} element.
	 * @param contextExpansion
	 *        The {@link Expansion} being currently active.
	 * @param out
	 *        The target {@link Buffer} to write to.
	 * @return The top-level {@link Node}s created.
	 */
	List<? extends Node> expandReference(Element reference, Expansion contextExpansion, Buffer out)
			throws ElementInAttributeContextError;

}
