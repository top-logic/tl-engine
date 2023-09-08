/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import org.w3c.dom.Document;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Information about a resolved layout definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutDefinition {

	/**
	 * The resolver that resolved this {@link LayoutDefinition}.
	 */
	LayoutResolver getResolver();

	/**
	 * The name of this {@link LayoutDefinition} relative to <code>/WEB-INF/layouts</code>.
	 */
	String getLayoutName();

	/**
	 * The {@link BinaryData} from which this {@link LayoutDefinition} is loaded.
	 */
	BinaryData getLayoutData();

	/**
	 * The parsed layout definition document.
	 */
	Document getLayoutDocument();

	Application getApplication();

	Protocol getProtocol();

}
