/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Visitor for DOM {@link Node}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DOMVisitor {
	
	public final Object visit(Node node, Object arg) {
		short nodeType = node.getNodeType();
		switch (nodeType) {
		case Node.ATTRIBUTE_NODE: {
			return visitAttribute((Attr) node, arg);
		}
		case Node.CDATA_SECTION_NODE: {
			return visitCData((CharacterData) node, arg);
		}
		case Node.COMMENT_NODE: {
			return visitComment((Comment) node, arg);
		}
		case Node.DOCUMENT_FRAGMENT_NODE: {
			return visitFragment((DocumentFragment) node, arg);
		}
		case Node.DOCUMENT_NODE: {
			return visitDocument((Document) node, arg);
		}
		case Node.DOCUMENT_TYPE_NODE: {
			return visitDocument((DocumentType) node, arg);
		}
		case Node.ELEMENT_NODE: {
			return visitElement((Element) node, arg);
		}
		case Node.ENTITY_NODE: {
			return visitEntity((Entity) node, arg);
		}
		case Node.ENTITY_REFERENCE_NODE: {
			return visitEntityReference((EntityReference) node, arg);
		}
		case Node.NOTATION_NODE: {
			return visitNotation((Notation) node, arg);
		}
		case Node.PROCESSING_INSTRUCTION_NODE: {
			return visitProcessingInstruction((ProcessingInstruction) node, arg);
		}
		case Node.TEXT_NODE: {
			return visitText((Text) node, arg);
		}
		default: {
			throw new UnreachableAssertion("Unknown node type: " + nodeType);
		}
		}
	}

	protected Object visitProcessingInstruction(ProcessingInstruction node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitNotation(Notation node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitEntityReference(EntityReference node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitEntity(Entity node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitElement(Element node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitDocument(DocumentType node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitDocument(Document node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitFragment(DocumentFragment node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitComment(Comment node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitCData(CharacterData node, Object arg) {
		return visitNode(node, arg);
	}

	protected Object visitText(Text node, Object arg) {
		return visitCData(node, arg);
	}

	protected Object visitAttribute(Attr node, Object arg) {
		return visitNode(node, arg);
	}

	protected abstract Object visitNode(Node node, Object arg);
}
