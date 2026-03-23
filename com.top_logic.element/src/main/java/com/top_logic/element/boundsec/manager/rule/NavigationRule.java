/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * A navigation rule that describes how to traverse from a source object of a given {@link TLClass}
 * type to a set of target objects by following a sequence of {@link PathElement} steps.
 *
 * <p>
 * Navigation rules are used in the security system to determine which objects are reachable from a
 * given object via a configured path. The rule can be applied in both forward direction
 * ({@link #getContent}) and backward direction ({@link #getContentBackwards}).
 * </p>
 *
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigationRule {

	/** The meta element type of objects the role applies to */
	private final TLClass _type;

	/** Indicates that the role is to be applied to sub types too */
	private final boolean _inherit;

	/**
	 * A list of {@link PathElement}s representing the navigation steps of this rule.
	 */
	private final List<PathElement> _path;

	/**
	 * Creates a {@link NavigationRule}.
	 *
	 * @param type
	 *        The type of objects this rule applies to. Must not be <code>null</code>.
	 * @param inherit
	 *        Whether this rule also applies to sub types of {@code type}.
	 * @param path
	 *        The sequence of steps to navigate from the source to the target objects.
	 */
	public NavigationRule(TLClass type, boolean inherit, List<PathElement> path) {
		_type = Objects.requireNonNull(type);
		_inherit = inherit;
		_path = path;
	}

	/**
	 * The type of objects this rule applies to.
	 */
	public TLClass getMetaElement() {
		return (_type);
	}

	/**
	 * Indicates, that the rule also applies to the sub types (in case of a set {@link TLClass})
	 */
	public boolean isInherit() {
		return (_inherit);
	}

	/**
	 * The sequence of {@link PathElement} steps to navigate from the source to the target objects.
	 */
	public List<PathElement> getPath() {
		return (_path);
	}

	/**
	 * Collects all objects reachable from {@code aNode} by following this rule's path backwards,
	 * i.e. starting from the last path step and working towards the first.
	 *
	 * <p>
	 * At each step, {@link PathElement#getSources(TLObject)} is used to traverse the path element
	 * in its source direction.
	 * </p>
	 *
	 * @param aNode
	 *        The object to start the backwards traversal from.
	 * @param out
	 *        Receives the collected objects reached at the start of the path.
	 */
	public void getContentBackwards(TLObject aNode, Set<? super TLObject> out) {
		getContentBackwards(aNode, _path.size() - 1, out);
	}

	/**
	 * Recursive implementation of {@link #getContentBackwards(TLObject, Set)}.
	 *
	 * @param aNode
	 *        The current object being traversed.
	 * @param aPosition
	 *        The current path index, starting at the last step and decremented towards zero.
	 * @param out
	 *        Receives the collected objects once the first path step (index 0) is reached.
	 */
	protected void getContentBackwards(TLObject aNode, int aPosition, Set<? super TLObject> out) {
		PathElement thePE = _path.get(aPosition);
		Collection<? extends TLObject> theNodeElements = thePE.getSources(aNode);
		if (aPosition == 0) {
			out.addAll(theNodeElements);
		} else {
			int theChildPosition = aPosition - 1;
			for (Iterator<? extends TLObject> theIt = theNodeElements.iterator(); theIt.hasNext();) {
				TLObject theElement = theIt.next();
				this.getContentBackwards(theElement, theChildPosition, out);
			}
		}
	}

	/**
	 * Collects all objects reachable from {@code aNode} by following this rule's path in forward
	 * direction.
	 *
	 * <p>
	 * Starting from {@code aNode}, the path is traversed step by step using
	 * {@link PathElement#getValues(TLObject)} at each step. Objects reached at the end of the path
	 * are added to {@code out}.
	 * </p>
	 *
	 * @param aNode
	 *        The source object to navigate from.
	 * @param out
	 *        Receives the collected target objects.
	 */
	public void getContent(TLObject aNode, Set<? super TLObject> out) {
		getContent(aNode, 0, out);
	}

	/**
	 * Recursive implementation of {@link #getContent(TLObject, Set)}.
	 *
	 * @param aNode
	 *        The current object being traversed.
	 * @param aPosition
	 *        The current index in the path, incremented with each recursive step.
	 * @param out
	 *        Receives the collected target objects once the end of the path is reached.
	 */
	protected void getContent(TLObject aNode, int aPosition, Set<? super TLObject> out) {
		if (aNode == null || !aNode.tValid())
			return;

		PathElement thePE = _path.get(aPosition);
		Collection<? extends TLObject> theNodeElements = thePE.getValues(aNode);
		if (_path.size() == aPosition + 1) {
			out.addAll(theNodeElements);
		} else {
			int theChildPosition = aPosition + 1;
			for (Iterator<? extends TLObject> theIt = theNodeElements.iterator(); theIt.hasNext();) {
				TLObject theElement = theIt.next();
				this.getContent(theElement, theChildPosition, out);
			}
		}
	}

}

