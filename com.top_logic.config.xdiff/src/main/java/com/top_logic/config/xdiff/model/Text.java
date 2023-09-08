/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import java.security.MessageDigest;

import com.top_logic.config.xdiff.util.Utils;

/**
 * Text {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Text extends Node {
	
	private String contents;
	
	/* package protected */Text(String contents) {
		setContents(contents);
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.TEXT;
	}

	/**
	 * The text content of this {@link Text} node.
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Setter for {@link #getContents()}.
	 */
	public void setContents(String contents) {
		assert contents != null : "Text contents may not be null.";
		this.contents = contents;
	}
	
	@Override
	protected void updateWithLocalData(MessageDigest md5) {
		super.updateWithLocalData(md5);
		
		Utils.updateUTF8(md5, contents);
	}
	
	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitText(this, arg);
	}
}
