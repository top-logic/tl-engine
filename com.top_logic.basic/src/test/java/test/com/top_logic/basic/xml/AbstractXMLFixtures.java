/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Base class for XML fixture loading facades.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AbstractXMLFixtures {

	/**
	 * Loads an XML document with the given name relative to the given class.
	 */
	public static Document getDocument(final Class<?> self, String name) throws AssertionFailedError {
		return getDocument(self, DOMUtil.getDocumentBuilder(), name);
	}

	/**
	 * Load an XML document with the given name relative to the given class.
	 * 
	 * @param builder
	 *        The {@link DocumentBuilder} for creating the document.
	 */
	public static Document getDocument(final Class<?> self, DocumentBuilder builder, String name)
			throws AssertionFailedError {
		try {
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					// Absurd hack to get rid of the automatic "absolutification" of relative system
					// IDs.
					try {
						URI uri = new URI(systemId);
						if (uri.isAbsolute()) {
							if ("file".equals(uri.getScheme())) {
								String path = uri.getPath();

								String prefix =
									new File(".").getCanonicalFile().getPath().replace(File.separatorChar, '/') + "/";
								int index = path.indexOf(prefix);
								if (index >= 0 && (index <= 1)) {
									// Note: The URI always path starts with a '/' character, while
									// the canonical path on Windows does not ("C:/....").
									systemId = path.substring(index + prefix.length());
								}
							}
						}
					} catch (URISyntaxException ex) {
						// Ignore.
					}
					return new InputSource(getStream(self, systemId));
				}
			});
			return builder.parse(getStream(self, name));
		} catch (SAXException ex) {
			throw (AssertionFailedError) new AssertionFailedError("Parsing of fixture '" + name + "' failed.")
				.initCause(ex);
		} catch (IOException ex) {
			throw (AssertionFailedError) new AssertionFailedError("Canot read fixture '" + name + "' failed.")
				.initCause(ex);
		}
	}

	/**
	 * Opens an {@link InputStream} to the text fixture.
	 * 
	 * @param self
	 *        The class to load the text fixture for.
	 * @param name
	 *        The local name of the resource.
	 * @return A stream to the specified resource.
	 */
	public static InputStream getStream(Class<?> self, String name) {
		String resourceName = getResourceName(self, name);
		InputStream stream = self.getResourceAsStream(resourceName);
		TestCase.assertNotNull("Fixture '" + name + "' not found.", stream);
		return stream;
	}

	protected static File getFile(Class<?> self, String name) {
		return new File(new File(ModuleLayoutConstants.SRC_TEST_DIR), getResourceName(self, name));
	}
	
	protected static String getResourceName(Class<?> self, String name) {
		return "/" + self.getPackage().getName().replace('.', '/') + "/" + name;
	}

}
