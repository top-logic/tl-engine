/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;

/**
 * Base class for completely mutable lists.
 * 
 * <p>
 * {@link AbstractList} that redeclares necessary methods to override for a mutable list as
 * abstract. This allows an implementation with minimal methods to implement to have a complete
 * mutable list.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MutableList<E> extends AbstractList<E> {

	@Override
	public abstract E remove(int index);

	@Override
	public abstract E set(int index, E element);

	@Override
	public abstract void add(int index, E element);

}

