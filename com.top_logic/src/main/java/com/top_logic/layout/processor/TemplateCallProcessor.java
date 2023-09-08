/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.layout.processor.Expansion.Buffer;

/**
 * Callback interface to expand a {@link LayoutModelConstants#TEMPLATE_CALL_ELEMENT} element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TemplateCallProcessor {

	/**
	 * Creates a list of nodes to use instead the given template call in the given
	 * {@link Expansion}.
	 */
	List<? extends Node> expandElement(Expansion expansion, Element templateCall, Buffer out)
			throws ElementInAttributeContextError;

}

