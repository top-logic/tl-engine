/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * One node in a role rule path.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PathElement {

	/**
	 * The part that is used by this {@link PathElement}.
	 */
	TLStructuredTypePart getMetaAttribute();

	boolean isInverse();

	/**
	 * Traverses this path element starting from the given base object and returns the reached
	 * objects.
	 *
	 * <p>
	 * For a non-inverse path element, the reference value(s) of {@code base} are returned. For an
	 * inverse path element, all objects that refer to {@code base} via the reference are returned.
	 * </p>
	 *
	 * @param base
	 *        The object to start the traversal from. Must not be <code>null</code>.
	 * @return The objects reached by following this path element. Never <code>null</code>.
	 * 
	 * @see #getSources(TLObject)
	 */
	Collection<? extends TLObject> getValues(TLObject base);

	/**
	 * Returns the source objects that reach the given destination object when this path element is
	 * traversed.
	 *
	 * <p>
	 * This is the inverse operation of {@link #getValues(TLObject)}: for a non-inverse path
	 * element, all objects that refer to {@code destination} via the reference are returned; for an
	 * inverse path element, the reference value(s) of {@code destination} are returned.
	 * </p>
	 *
	 * @param destination
	 *        The object to determine the sources for. Must not be <code>null</code>.
	 * @return The objects that reach {@code destination} by following this path element. Never
	 *         <code>null</code>.
	 * 
	 * @see #getValues(TLObject)
	 */
	Collection<? extends TLObject> getSources(TLObject destination);

	/**
	 * Appends a machine-readable identifier for this path element to the given output.
	 *
	 * <p>
	 * The produced string is used as part of a unique ID for the enclosing {@link RoleRule} and
	 * must therefore be stable and unambiguous across all path element types.
	 * </p>
	 *
	 * @param out
	 *        The output to append to. Must not be <code>null</code>.
	 * @throws IOException
	 *         If writing to {@code out} fails.
	 */
	void appendId(Appendable out) throws IOException;

	/**
	 * Appends an HTML-encoded, human-readable description of this path element to the given output.
	 *
	 * <p>
	 * The produced string is used inside a tooltip that describes the path of a {@link RoleRule} to
	 * the user.
	 * </p>
	 *
	 * @param out
	 *        The output to append to. Must not be <code>null</code>.
	 * @throws IOException
	 *         If writing to {@code out} fails.
	 */
	void appendForTooltip(Appendable out) throws IOException;
}

