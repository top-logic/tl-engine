/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;

/**
 * Pure man's servlet 3.0 support creating a monolithic <code>web.xml</code> from multiple
 * <code>web-fragment.xml</code> files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebXmlBuilder {

	private static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee";

	private static final String WEB_XML_PATH = "WEB-INF/web.xml";

	private final DocumentBuilder _builder;

	private Document _result;

	/**
	 * Whether a <code>metadata-complete</code> attribute has been read so far from any merged
	 * web.xml or fragment.
	 */
	private boolean _metadataComplete;

	/**
	 * Creates a {@link WebXmlBuilder}.
	 *
	 */
	public WebXmlBuilder() throws ParserConfigurationException {
		_builder = DOMUtil.newDocumentBuilderNamespaceAware();
		_result = createEmptyWebXml();
	}

	/**
	 * Creates a combined <code>web.xml</code> from the initial <code>web.xml</code> found in the
	 * given web application root and adding all fragments found in the class path.
	 */
	public static WebXmlBuilder createFromClassPath(File webApp) {
		WebXmlBuilder result;
		try {
			result = tryCreateWebXml(webApp);
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	private static WebXmlBuilder tryCreateWebXml(File webApp)
			throws IOException, SAXException, ParserConfigurationException {
		WebXmlBuilder builder = new WebXmlBuilder();
		builder.addFromWebApp(webApp);
		builder.addClassPathFragments();
		return builder;
	}

	/**
	 * Adds <code>web-fragment.xml</code> resources found on the class path.
	 * 
	 * @return The given {@link File} for call chaining.
	 */
	public WebXmlBuilder addClassPathFragments() throws IOException, SAXException {
		if (isMetadataComplete()) {
			// A `metadata-complete` attribute was set in the web.xml, therefore do not process
			// fragments.
			return this;
		}
		Enumeration<URL> resources =
			Thread.currentThread().getContextClassLoader().getResources("META-INF/web-fragment.xml");
		while (resources.hasMoreElements()) {
			URL fragmentUrl = resources.nextElement();
			try (InputStream in = fragmentUrl.openStream()) {
				addFragment(in);
			}
		}
		return this;
	}

	private boolean isMetadataComplete() {
		return _metadataComplete;
	}

	/**
	 * Adds a <code>web.xml</code> from the given web application root.
	 * 
	 * @return The given {@link File} for call chaining.
	 */
	public WebXmlBuilder addFromWebApp(File webApp) throws SAXException, IOException {
		File local = new File(webApp, WEB_XML_PATH);
		if (local.exists()) {
			addFragment(local);
		}
		return this;
	}

	/**
	 * The resulting <code>web.xml</code> {@link Document}.
	 */
	public Document getWebXml() {
		return _result;
	}

	private Document parse(File local) throws SAXException, IOException, FileNotFoundException {
		Document doc;
		try (InputStream in = new FileInputStream(local)) {
			doc = parse(in);
		}
		return doc;
	}

	private Document parse(InputStream in) throws SAXException, IOException {
		Document doc;
		doc = _builder.parse(in);
		return doc;
	}

	private Document createEmptyWebXml() {
		Document doc;
		doc = _builder.newDocument();
		Element root = doc.createElementNS(JAVAEE_NS, "web-app");
		root.setAttributeNS(null, "version", "3.0");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
			"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
		root.setAttribute("metadata-complete", "true");
		{
			Element ordering = doc.createElementNS(JAVAEE_NS, "absolute-ordering");
			ordering.appendChild(doc.createComment("Complete web.xml."));
			root.appendChild(ordering);
		}
		doc.appendChild(root);
		
		return doc;
	}

	/**
	 * Reads and adds the given web-fragment.xml to this builder.
	 */
	public WebXmlBuilder addFragment(InputStream webFragment) throws SAXException, IOException {
		return addFragment(parse(webFragment));
	}

	/**
	 * Reads and adds the given web-fragment.xml to this builder.
	 */
	public WebXmlBuilder addFragment(File webFragment) throws SAXException, IOException {
		return addFragment(parse(webFragment));
	}

	/**
	 * Adds the given web-fragment.xml to this builder.
	 */
	public WebXmlBuilder addFragment(Document webFragment) {
		Element source = webFragment.getDocumentElement();
		Element target = _result.getDocumentElement();

		mergeInto(target, source);
		return this;
	}

	private void mergeInto(Element target, Element source) {
		_metadataComplete = _metadataComplete || "true".equals(source.getAttribute("metadata-complete"));

		for (Element content : DOMUtil.elements(source)) {
			String localName = content.getLocalName();
			if (localName.equals("name")) {
				// Only the top-level web.xml defines the name.
				continue;
			}

			Iterable<Element> existing =
				DOMUtil.elementsNS(target, content.getNamespaceURI(), localName, true);
			Iterator<Element> it = existing.iterator();
			boolean targetHasElement = it.hasNext();

			if (localName.equals("jsp-config") && targetHasElement) {
				// Element must exist only once in the resulting document.

				Element correspondence = it.next();
				for (Element inner : DOMUtil.elements(content)) {
					correspondence.appendChild(_result.importNode(inner, true));
				}
			} else {
				Element anchor;
				if (targetHasElement) {
					Element correspondence = it.next();
					anchor = DOMUtil.getNextElementSibling(correspondence);
				} else {
					anchor = null;
				}

				target.insertBefore(_result.importNode(content, true), anchor);
			}

		}
	}

	/**
	 * Dumps the generated <code>web.xml</code> to the given {@link File}.
	 * 
	 * @return The given {@link File} for call chaining.
	 */
	public File dumpTo(File webXml) throws IOException {
		try (OutputStream out = new FileOutputStream(webXml)) {
			XMLPrettyPrinter.dump(out, getWebXml());
		}
		return webXml;
	}

}
