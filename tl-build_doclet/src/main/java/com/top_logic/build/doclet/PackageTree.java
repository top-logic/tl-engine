/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Collection of Java package names organized as tree structure.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PackageTree {

	private Set<String> _contents = new HashSet<>();

	/**
	 * Adds the package with the given name to the package tree.
	 *
	 * @param name
	 *        Name of the package to add.
	 */
	public void add(String name) {
		_contents.add(name);
	}

	void fromXML(Document packageDoc) {
		Element list = packageDoc.getDocumentElement();
		NodeList packageList = list.getElementsByTagName("package");
		for (int n = 0, cnt = packageList.getLength(); n < cnt; n++) {
			Element packageRef = (Element) packageList.item(n);
			add(packageRef.getAttribute("name"));
		}
	}

	void save(XMLStreamWriter out) throws XMLStreamException {
		List<String> contents = new ArrayList<>(_contents);
		Collections.sort(contents);

		out.writeStartElement("list");
		{
			nl(out);
			out.writeStartElement("packages");
			for (int n = 0, cnt = contents.size(); n < cnt;) {
				n = writeSubtree(out, contents, n);
			}
			out.writeEndElement();
		}
		out.writeEndElement();
	}

	private int writeSubtree(XMLStreamWriter out, List<String> contents, int n) throws XMLStreamException {
		String root = contents.get(n++);

		nl(out);
		out.writeStartElement("package");
		out.writeAttribute("name", root);

		if (n < contents.size() && isSubPackage(contents.get(n), root)) {
			nl(out);
			out.writeStartElement("packages");
			do {
				n = writeSubtree(out, contents, n);
			} while (n < contents.size() && isSubPackage(contents.get(n), root));
			out.writeEndElement();
		}

		out.writeEndElement();
		return n;
	}

	private void nl(XMLStreamWriter out) throws XMLStreamException {
		out.writeCharacters("\n");
	}

	private boolean isSubPackage(String pkg, String parent) {
		int parentLength = parent.length();

		if (!(pkg.length() > parentLength)) {
			return false;
		}

		if (!pkg.startsWith(parent)) {
			return false;
		}

		return pkg.charAt(parentLength) == '.';
	}

}
