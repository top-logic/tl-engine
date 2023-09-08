/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import static com.top_logic.config.xdiff.util.Utils.*;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.config.xdiff.util.Utils;

/**
 * An element {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Element extends FragmentBase implements Named {

	private List<Attribute> _orderedAttributes = Collections.emptyList();

	private QName _name;

	private boolean _empty;

	/* package protected */Element(QName name) {
		_name = name;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.ELEMENT;
	}

	/**
	 * This {@link Element}'s qualified name.
	 */
	public QName getQName() {
		return _name;
	}

	@Override
	public String getNamespace() {
		return _name.getNamespace();
	}

	@Override
	public String getLocalName() {
		return _name.getLocalName();
	}
	
	/**
	 * Whether this {@link Element} should be serialized as an empty element, if it has no
	 * {@link #getChildren()}.
	 */
	public boolean isEmpty() {
		return _empty;
	}

	/** @see #isEmpty() */
	public Element setEmpty(boolean empty) {
		_empty = empty;
		return this;
	}

	/**
	 * List of all {@link Attribute}s of this {@link Element} in {@link QNameOrder}.
	 */
	public List<Attribute> getOrderedAttributes() {
		return _orderedAttributes;
	}

	/**
	 * Setter for {@link #getOrderedAttributes()}.
	 * 
	 * @param newAttributes
	 *        The new set of attributes of this node. No reference to the given collection is kept.
	 *        The collection may contain <code>null</code> values, which are ignored.
	 * @return This instance of call chaining.
	 */
	public Element setAttributes(Attribute... newAttributes) {
		return setAttributes(list(newAttributes));
	}

	/**
	 * Setter for {@link #getOrderedAttributes()}.
	 * 
	 * @param newAttributes
	 *        The new set of attributes of this node. No reference to the given collection is kept.
	 *        The collection may contain <code>null</code> values, which are ignored.
	 * @return This instance of call chaining.
	 */
	public Element setAttributes(Collection<Attribute> newAttributes) {
		if (newAttributes.isEmpty()) {
			_orderedAttributes = Collections.emptyList();
		} else {
			ArrayList<Attribute> newAttributesOrdered = nonNullList(newAttributes);
			if (newAttributesOrdered.isEmpty()) {
				_orderedAttributes = Collections.emptyList();
			} else {
				Collections.sort(newAttributesOrdered, QNameOrder.INSTANCE);
				_orderedAttributes = newAttributesOrdered;
			}
		}
		return this;
	}

	/**
	 * The number of attributes in {@link #getOrderedAttributes()}.
	 */
	public int getAttributeCount() {
		return _orderedAttributes.size();
	}
	
	/**
	 * The {@link Attribute} at the given index in {@link #getOrderedAttributes()}.
	 */
	public Attribute getAttribute(int index) {
		return _orderedAttributes.get(index);
	}

	/**
	 * The value of the {@link Attribute} with the given namespace and name.
	 */
	public String getAttribute(String namespace, String localName) {
		return getAttribute(NodeFactory.qname(namespace, localName));
	}
	
	/**
	 * The value of the {@link Attribute} with the given name.
	 */
	public String getAttribute(QName name) {
		int index = Collections.binarySearch(_orderedAttributes, name, QNameOrder.INSTANCE);
		if (index < 0) {
			return null;
		} else {
			Attribute attribute = _orderedAttributes.get(index);
			return attribute.getValue();
		}
	}
	
	@Override
	protected int getLocalWeight() {
		return super.getLocalWeight() + _orderedAttributes.size();
	}
	
	@Override
	protected void updateWithLocalData(MessageDigest md5) {
		super.updateWithLocalData(md5);
		
		Utils.updateUTF8(md5, getNamespace());
		Utils.updateUTF8(md5, getLocalName());

		updateWithAttributes(md5);
	}

	private void updateWithAttributes(MessageDigest md5) {
		for (Attribute attribute : _orderedAttributes) {
			Utils.updateUTF8(md5, attribute.getNamespace());
			Utils.updateUTF8(md5, attribute.getLocalName());
			Utils.updateUTF8(md5, attribute.getValue());
		}
	}
	
	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitElement(this, arg);
	}

}
