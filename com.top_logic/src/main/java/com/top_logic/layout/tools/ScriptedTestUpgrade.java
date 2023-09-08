/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

/**
 * {@link XMLTransformer} applying <code>ScriptedTestUpgrade.xslt</code> to given files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptedTestUpgrade extends XMLTransformer {

	/**
	 * Creates a {@link ScriptedTestUpgrade}.
	 */
	public ScriptedTestUpgrade() throws TransformerConfigurationException {
		super();
	}

	@Override
	protected Source txSource() {
		return new StreamSource(getClass().getResourceAsStream("ScriptedTestUpgrade.xslt"));
	}

	public static void main(String[] args) throws Exception {
		new ScriptedTestUpgrade().runMain(args);
	}

}
