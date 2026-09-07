/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket29221;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.layout.tools.XMLTransformer;

/**
 * Tool for removing security domain in layout configurations.
 */
public class RemoveSecurityDomain extends XMLTransformer {

	/**
	 * Creates a {@link RemoveSecurityDomain}.
	 */
	public RemoveSecurityDomain() throws TransformerConfigurationException {
		super();
	}

	@Override
	protected Source txSource() {
		return new StreamSource(
			getClass().getResourceAsStream("/com/top_logic/layout/tools/ticket29221/RemoveSecurityDomain.xslt"));
	}

	/**
	 * Entry-point of the {@link RemoveSecurityDomain} tool.
	 */
	public static void main(String[] args) throws Exception {
		new RemoveSecurityDomain().runMain(args);
	}

}
