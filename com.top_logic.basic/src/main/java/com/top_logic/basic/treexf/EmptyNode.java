/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Base class for implementing {@link Node}s without children.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class EmptyNode extends AbstractNode {

	@Override
	public final int getSize() {
		return 0;
	}

	@Override
	public final Node getChild(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final void setChild(int index, Node child) {
		throw new IndexOutOfBoundsException();
	}

}
