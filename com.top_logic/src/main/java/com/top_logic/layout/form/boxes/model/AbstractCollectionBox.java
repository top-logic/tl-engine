/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Base class for {@link Box}es consisting of nested {@link Box}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCollectionBox extends AbstractBox {

	private final Set<Box> _children = new HashSet<>();

	@Override
	public Collection<Box> getChildren() {
		return _children;
	}

	/**
	 * Initializes the {@link Box#getParent()} relation of the given child box.
	 * 
	 * @param oldChild
	 *        The (optional) old child box that was replaced with a new child box.
	 * @param newChild
	 *        The (optional) new child of this {@link AbstractCollectionBox}.
	 * @return The given child for call chaining.
	 */
	protected <B extends Box> B replace(Box oldChild, B newChild) {
		resetParent(oldChild);
		initParent(newChild);
		return newChild;
	}

	private void resetParent(Box oldChild) {
		if (oldChild != null) {
			_children.remove(oldChild);
			oldChild.internals().setParent(null);
			notifyLayoutChange();
		}
	}

	private void initParent(Box newChild) {
		if (newChild != null) {
			newChild.internals().setParent(this);
			_children.add(newChild);
			notifyLayoutChange();
		}
	}

}
