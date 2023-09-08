/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml.fixtures;

import java.io.InputStream;

import org.w3c.dom.Document;

import test.com.top_logic.basic.xml.AbstractXMLFixtures;

/**
 * Test fixtures.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Fixtures extends AbstractXMLFixtures {

	public static Document getDocument(String name) {
		return getDocument(Fixtures.class, name);
	}

	public static InputStream getStream(String name) {
		return getStream(Fixtures.class, name);
	}

}
