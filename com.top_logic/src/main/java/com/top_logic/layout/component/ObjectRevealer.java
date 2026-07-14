/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

/**
 * A component that can make a given business object visible to the user.
 *
 * <p>
 * Revealing an object means bringing it into view without necessarily changing the selection: in a
 * tree, the ancestors of the object are expanded so that it is displayed; in a tree or table, the
 * row of the object is scrolled into the viewport. This works even if the object is already
 * selected, where setting the selection again would be a no-op.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectRevealer {

	/**
	 * Makes the given object visible to the user.
	 *
	 * @param businessObject
	 *        The object to reveal.
	 * @return Whether the object is part of this component and could be revealed.
	 */
	boolean revealObject(Object businessObject);

}
