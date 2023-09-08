/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.ms.fixtures;

import java.io.File;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.AssertionFailedError;

import org.w3c.dom.Document;

import test.com.top_logic.basic.xml.AbstractXMLFixtures;

import com.top_logic.config.xdiff.model.DocumentBuilder;

/**
 * Facade that delivers fixtures.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Fixtures extends AbstractXMLFixtures {

	public static com.top_logic.config.xdiff.model.Document getDocumentX(final String name) {
		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
			inputFactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", true);
			inputFactory.setXMLResolver(new XMLResolver() {
				@Override
				public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
						throws XMLStreamException {
					return Fixtures.getStream(systemID);
				}
			});
			XMLStreamReader in = inputFactory.createXMLStreamReader(Fixtures.getStream(name));
			com.top_logic.config.xdiff.model.Document result = DocumentBuilder.parseDocument(true, in);
			return result;
		} catch (XMLStreamException ex) {
			throw (AssertionFailedError) new AssertionFailedError("Canot read fixture '" + name + "' failed.")
				.initCause(ex);
		}
	}

	public static Document getDocument(String name) {
		Document result = getDocument(Fixtures.class, name);
		return result;
	}

	public static InputStream getStream(String name) {
		return getStream(Fixtures.class, name);
	}

	public static File getFile(String name) {
		return getFile(Fixtures.class, name);
	}

}
