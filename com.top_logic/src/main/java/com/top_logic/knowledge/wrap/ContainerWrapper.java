/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;

import com.top_logic.model.TLObject;

/**
 * Interface for persistent containers.
 * 
 * @author <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
public interface ContainerWrapper extends Wrapper {

	/**
	 * Whether this container is empty.
	 * 
	 * @return Whether {@link #getContentSize()} is zero.
	 */
	public boolean isEmpty();

	/**
	 * The number of content objects.
	 * 
	 * When counting the security will be honored.
	 * 
	 * @return The size of {@link #getContent()}.
	 */
	public int getContentSize();

	/**
	 * Whether the given object is contained in this container.
	 */
	public boolean contains(TLObject content);

	/**
	 * The objects contained in this container.
	 */
	public Collection<? extends TLObject> getContent();

	/**
	 * Appends the given object as child for the container.
	 * 
	 * One object can be added only once, so if this method has been called twice with the same
	 * object, it returns false.
	 * 
	 * @param newChild
	 *        The object to add
	 * @return Whether the given object was added to this container. <code>false</code>, if the
	 *         object was already part of this container.
	 */
	public boolean add(TLObject newChild);

	/**
	 * Removes the given object from this container.
	 * 
	 * @param oldChild
	 *        The object to be removed.
	 * @return true, if removing the object succeeded. <code>false</code>, if the object was not
	 *         part of this container.
	 */
	public boolean remove(TLObject oldChild);

	/**
	 * Removes all objects contained in this one.
	 */
	public void clear();

}

