/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.model;

import static test.com.top_logic.config.xdiff.TestingUtil.*;

import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.DocumentSerializer;

/**
 * Test case for {@link DocumentSerializer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDocumentSerializer extends TestCase {

	public void testSerialization() throws XMLStreamException, FactoryConfigurationError {
		doCheckSerialization("<a><b a1='v1' a2='v2'/>text<!--comment--></a>");
	}

	public void testNamespaceInContext() throws XMLStreamException, FactoryConfigurationError {
		doCheckSerialization("<a xmlns='http://some.name.space/'><b xmlns='' a1='v1' a2='v2'/>text<!--comment--></a>");
	}

	public void testNamespaceInContent() throws XMLStreamException, FactoryConfigurationError {
		doCheckSerialization("<a><b xmlns='http://some.name.space/' a1='v1' a2='v2'/>text<!--comment--></a>");
	}

	private void doCheckSerialization(String xml) throws XMLStreamException {
		Document document = fixture(xml);
		StringWriter buffer = new StringWriter();
		DocumentSerializer.serialize(document, XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer));
		String serializedDocument = buffer.toString();

		assertTrue(document.equals(fixture(serializedDocument)));
	}

}
