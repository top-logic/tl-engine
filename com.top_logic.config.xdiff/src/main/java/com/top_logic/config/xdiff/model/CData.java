/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * {@link Text} that is specially escaped during output.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CData extends Text {

	CData(String contents) {
		super(contents);
	}

	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitCData(this, arg);
	}

}
