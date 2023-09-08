/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.top_logic.basic.io.WrappedIOException;
import com.top_logic.basic.xml.DefaultNamespaceContext;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QName;

/**
 * Algorithm to transform an {@link XPathNode} into a textual representation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XPathSerializer implements XPathVisitor<Void, Appendable> {

	private static final XPathSerializer DEFAULT_INSTANCE = new XPathSerializer(new DefaultNamespaceContext());

	private static final Void none = null;

	private final NamespaceContext _context;

	/**
	 * Creates a {@link XPathSerializer}.
	 * 
	 * @param context
	 *        A binding of namespaces to prefixes.
	 */
	public XPathSerializer(NamespaceContext context) {
		this._context = context;
	}

	/**
	 * Serializes the given XPath with the default {@link NamespaceContext} defined by XML.
	 */
	public static String toString(XPathNode path) {
		StringBuilder buffer = new StringBuilder();
		path.visit(DEFAULT_INSTANCE, buffer);
		return buffer.toString();
	}

	@Override
	public Void visitLocationPath(LocationPath path, Appendable out) {
		try {
			if (path.isAbsolute()) {
				out.append('/');
			}

			boolean first = true;
			for (Step step : path.getSteps()) {
				if (first) {
					first = false;
				} else {
					out.append('/');
				}
				step.visit(this, out);
			}
			return none;
		} catch (IOException ex) {
			throw wrap(ex);
		}
	}

	@Override
	public Void visitNameTest(NameTest path, Appendable out) {
		try {
			QName name = path.getName();
			appendPrefix(out, name);
			out.append(name.getLocalName());
			return none;
		} catch (IOException ex) {
			throw wrap(ex);
		}
	}

	private void appendPrefix(Appendable out, QName name) throws IOException {
		String prefix = prefix(name.getNamespace());
		if (prefix == null || !prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			out.append(prefix);
			out.append(':');
		}
	}

	@Override
	public Void visitTypeTest(TypeTest path, Appendable out) {
		try {
			NodeType type = path.getType();
			switch (type) {
				case COMMENT:
					out.append("comment()");
					break;
				case TEXT:
					out.append("text()");
					break;
				default:
					throw new IllegalArgumentException("Invalid type test:_" + type);
			}
			return none;
		} catch (IOException ex) {
			throw wrap(ex);
		}
	}

	@Override
	public Void visitStep(Step path, Appendable out) {
		try {
			Axis axis = path.getAxis();
			switch (axis) {
				case CHILD:
					// Default.
					break;
				case FOLLOWING_SIBLING:
					out.append("following-sibling::");
					break;
				default:
					throw new UnsupportedOperationException("Unimplemented axis: " + axis);
			}
			path.getNodeTest().visit(this, out);
			out.append("[");
			out.append(Integer.toString(path.getPosition()));
			out.append("]");
			return none;
		} catch (IOException ex) {
			throw wrap(ex);
		}
	}

	private String prefix(String namespace) {
		if (namespace == null) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}
		if (namespace.equals(XMLConstants.NULL_NS_URI)) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}
		return _context.getPrefix(namespace);
	}

	private WrappedIOException wrap(IOException ex) {
		return new WrappedIOException(ex);
	}

}
