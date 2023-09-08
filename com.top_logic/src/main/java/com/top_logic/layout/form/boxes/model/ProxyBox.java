/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.Collection;

import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * {@link Box} that consists of a single other {@link Box}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProxyBox extends AbstractBox {

	private Box _impl;

	/**
	 * Creates a {@link ProxyBox}.
	 * 
	 * @param impl
	 *        See {@link #getImpl()}.
	 */
	public ProxyBox(Box impl) {
		setImpl(impl);
	}

	/**
	 * The content {@link Box}.
	 * 
	 * @return The content {@link Box} or <code>null</code>, in which case, this {@link Box} becomes
	 *         invisible.
	 */
	public Box getImpl() {
		return _impl;
	}

	/**
	 * @see #getImpl()
	 */
	public void setImpl(Box impl) {
		if (_impl != null) {
			_impl.internals().setParent(null);
		}
		_impl = impl;
		if (_impl != null) {
			_impl.internals().setParent(this);
		}
		notifyLayoutChange();
	}

	@Override
	public Collection<Box> getChildren() {
		return CollectionUtilShared.singletonOrEmptySet(_impl);
	}

	@Override
	protected void localLayout() {
		if (_impl == null) {
			setDimension(0, 0);
		} else {
			_impl.layout();

			setDimension(_impl.getColumns(), _impl.getRows());
		}
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		if (_impl != null) {
			_impl.enter(x, y, availableColumns, availableRows, table);
		}
	}

}
