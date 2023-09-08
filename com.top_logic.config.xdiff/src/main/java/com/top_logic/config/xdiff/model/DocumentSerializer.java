/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Serialization implementation of {@link Node} hierarchies.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentSerializer implements NodeVisitor<Void, XMLStreamWriter> {

	private static final Void none = null;

	/**
	 * Creates a {@link DocumentSerializer}.
	 */
	public static DocumentSerializer documentSerializer() {
		return new DocumentSerializer();
	}
	
	private DocumentSerializer() {
		// Singleton constructor.
	}

	/**
	 * Transforms the given {@link Node} into events passed to the given writer.
	 */
	public static void serialize(Node node, XMLStreamWriter out) throws XMLStreamException {
		documentSerializer().serializeTo(out, node);
	}

	/**
	 * Serializes the given node to the given stream.
	 * 
	 * @param out
	 *        The stream to write to.
	 * @param node
	 *        The {@link Node} to write.
	 * @throws XMLStreamException
	 *         If serialization fails.
	 */
	public void serializeTo(XMLStreamWriter out, Node node) throws XMLStreamException {
		try {
			node.visit(this, out);
		} catch (SerializationFailure ex) {
			throw ex.getCause();
		}
	}

	@Override
	public Void visitDocument(Document node, XMLStreamWriter out) {
		try {
			out.writeStartDocument();
			descendFragmentBase(node, out);
			out.writeEndDocument();
			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	@Override
	public Void visitElement(Element node, XMLStreamWriter out) {
		try {
			String elementNamespace = node.getNamespace();

			boolean isEmpty = node.getChildren().size() == 0 && node.isEmpty();
			if (isEmpty) {
				// Note: Writing empty elements triggers a problem in the
				// StAX API with namespace scopes in empty elements. See
				// http://jira.codehaus.org/browse/STAX-61
				// This problem is fixed with the Java 6 version of javax.stream.
				if (elementNamespace == null) {
					out.writeEmptyElement(node.getLocalName());
				} else {
					out.writeEmptyElement(elementNamespace, node.getLocalName());
				}
			} else {
				if (elementNamespace == null) {
					out.writeStartElement(node.getLocalName());
				} else {
					out.writeStartElement(elementNamespace, node.getLocalName());
				}
			}

			for (Attribute attribute : node.getOrderedAttributes()) {
				String attributeNamespace = attribute.getNamespace();
				if (attributeNamespace == null) {
					out.writeAttribute(
						attribute.getLocalName(),
						attribute.getValue());
				} else {
					out.writeAttribute(
						attributeNamespace,
						attribute.getLocalName(),
						attribute.getValue());
				}
			}
			descendFragmentBase(node, out);
			if (!isEmpty) {
				out.writeEndElement();
			}

			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	@Override
	public Void visitText(Text node, XMLStreamWriter out) {
		try {
			out.writeCharacters(node.getContents());
			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	@Override
	public Void visitCData(CData node, XMLStreamWriter out) {
		try {
			out.writeCData(node.getContents());
			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	@Override
	public Void visitFragment(Fragment node, XMLStreamWriter out) {
		return descendFragmentBase(node, out);
	}

	@Override
	public Void visitComment(Comment node, XMLStreamWriter out) {
		try {
			out.writeComment(node.getContents());
			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	@Override
	public Void visitEntityReference(EntityReference node, XMLStreamWriter out) {
		try {
			out.writeEntityRef(node.getName());
			return none;
		} catch (XMLStreamException ex) {
			throw error(ex);
		}
	}

	private Void descendFragmentBase(FragmentBase node, XMLStreamWriter out) {
		for (Node child : node.getChildren()) {
			child.visit(this, out);
		}
		return none;
	}

	private RuntimeException error(XMLStreamException ex) {
		throw new SerializationFailure(ex);
	}

	private static class SerializationFailure extends RuntimeException {

		public SerializationFailure(XMLStreamException cause) {
			super(cause);
		}

		@Override
		public XMLStreamException getCause() {
			return (XMLStreamException) super.getCause();
		}

	}

}
