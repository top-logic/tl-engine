/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Alternative in-memory representation of XML documents for difference computation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Node {

	private int weigth;
	
	private byte[] hash;
	
	/**
	 * The {@link NodeType} of this node.
	 */
	public abstract NodeType getNodeType();

	/**
	 * The number of descendant nodes of this {@link Node}.
	 */
	public int getWeight() {
		return weigth;
	}
	
	/**
	 * Initialization of {@link #getWeight()} and {@link #equalsNode(Node)}.
	 */
	public final void init() {
		MessageDigest md5 = createMD5();
		preprocess(md5);
	}

	private void preprocess(MessageDigest md5) {
		preprocessDescendents(md5);
		computeHash(md5);
		computeWeight();
	}

	private MessageDigest createMD5() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw (AssertionError) new AssertionError("No MD5 implementation available.").initCause(ex);
		}
	}

	/**
	 * Calls {@link #preprocess(MessageDigest)} recursively on all child nodes.
	 */
	private void preprocessDescendents(MessageDigest md5) {
		for (Node child : getChildren()) {
			child.preprocess(md5);
		}
	}
	
	private void computeHash(MessageDigest md5) {
		md5.reset();
		updateWithLocalData(md5);
		updateWithDirectChildrenHashes(md5);
		hash = md5.digest();
	}

	/**
	 * Hook for sub-classes to update the given digest with local data.
	 * 
	 * <p>
	 * A subclass must not the digest with data from any {@link #getChildren()} nodes.
	 * </p>
	 */
	protected void updateWithLocalData(MessageDigest md5) {
		md5.update((byte) getNodeType().ordinal());
	}

	/**
	 * Calls {@link #updateWithHash(MessageDigest)} on all direct child nodes. 
	 */
	private void updateWithDirectChildrenHashes(MessageDigest md5) {
		for (Node child : getChildren()) {
			child.updateWithHash(md5);
		}
	}
	
	/**
	 * Updates the given digest with the hash value of this local node.
	 */
	private void updateWithHash(MessageDigest md5) {
		assert isInitialized();
		md5.update(hash);
	}
	
	private void computeWeight() {
		weigth = getLocalWeight() + getSumOfChildrenWeight();
	}

	/**
	 * Hook for sub-classes computing the local {@link #getWeight()} of a node (exclusive the weight
	 * of any {@link #getChildren()}).
	 */
	protected int getLocalWeight() {
		return 1;
	}

	/**
	 * The sum of calls to {@link #getWeight()} on all direct children.
	 */
	private int getSumOfChildrenWeight() {
		int sum = 0;
		for (Node child : getChildren()) {
			sum += child.getWeight();
		}
		return sum;
	}

	/**
	 * The direct children of this {@link Node}.
	 */
	public List<Node> getChildren() {
		// No children by default.
		return Collections.emptyList();
	}

	/**
	 * The child {@link Node} at the given index in the {@link #getChildren()} list.
	 */
	public Node getChild(int index) {
		return getChildren().get(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Node)) {
			return false;
		} else {
			return equalsNode((Node) obj);
		}
	}

	/**
	 * Whether this node is structurally equal to the given node.
	 */
	public boolean equalsNode(Node other) {
		assert isInitialized();
		return Arrays.equals(hash, other.hash);
	}
	
	@Override
	public int hashCode() {
		assert isInitialized();
		return Arrays.hashCode(hash);
	}

	/**
	 * Whether {@link #init()} was called.
	 */
	private boolean isInitialized() {
		return hash != null;
	}

	/**
	 * Visits this node with the given {@link NodeVisitor}.
	 */
	public abstract <R, A> R visit(NodeVisitor<R, A> v, A arg);

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		try {
			XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
			DocumentSerializer.serialize(this, out);
			out.flush();
		} catch (XMLStreamException ex) {
			buffer.append("[error:");
			buffer.append(ex.getMessage());
			buffer.append("]");
		}
		return buffer.toString();
	}
}
