/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.Drag;
import com.top_logic.layout.Drop;
import com.top_logic.layout.tree.TreeControl;

/**
 * Legacy support of Drag'n'Drop for {@link TreeControl}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface LegacyTreeDrag extends Drag<List<?>> {

	/**
	 * Adds the given object as potential {@link Drop} target.
	 */
	void addDropTarget(Drop<? super List<?>> target);

	/**
	 * Removes the given object from the potential {@link Drop} targets.
	 * 
	 * @see #addDropTarget(Drop)
	 */
	void removeDropTarget(Drop<? super List<?>> target);

	/**
	 * all drop targets, registered to this {@link LegacyTreeDrag}.
	 */
	Collection<Drop<? super List<?>>> getDropTargets();

}
