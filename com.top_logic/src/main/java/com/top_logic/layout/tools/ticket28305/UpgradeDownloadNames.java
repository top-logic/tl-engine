/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket28305;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.layout.tools.XMLTransformer;
import com.top_logic.layout.tools.ticket25923.AdjustModelLabelsInTestCases;

/**
 * Tool for upgrading command confirmations in layout configurations.
 */
public class UpgradeDownloadNames extends XMLTransformer {

	/**
	 * Creates a {@link UpgradeDownloadNames}.
	 */
	public UpgradeDownloadNames() throws TransformerConfigurationException {
		super();
	}

	@Override
	protected Source txSource() {
		return new StreamSource(
			getClass().getResourceAsStream("/com/top_logic/layout/tools/ticket28305/UpgradeDownloadNames.xslt"));
	}

	/**
	 * Entry-point of the {@link AdjustModelLabelsInTestCases} tool.
	 */
	public static void main(String[] args) throws Exception {
		new UpgradeDownloadNames().runMain(args);
	}

}
