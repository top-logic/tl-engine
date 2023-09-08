/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket25923;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;
import com.top_logic.layout.tools.XMLTransformer;

/**
 * Adjusts model labels in scripted tests.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AdjustModelLabelsInTestCases extends XMLTransformer {

	/**
	 * Creates a {@link AdjustModelLabelsInTestCases}.
	 */
	public AdjustModelLabelsInTestCases() throws TransformerConfigurationException {
		super();
	}

	@Override
	protected Source txSource() {
		return new StreamSource(getClass().getResourceAsStream("upgrade-test-scripts.xsl"));
	}

	@Override
	protected Config prettyPrinterConfig() {
		Config config = XMLPrettyPrinter.newConfiguration();
		config.setPreserveWhitespace(true);
		return config;
	}

	/**
	 * Entry-point of the {@link AdjustModelLabelsInTestCases} tool.
	 */
	public static void main(String[] args) throws Exception {
		new AdjustModelLabelsInTestCases().runMain(args);
	}

}
