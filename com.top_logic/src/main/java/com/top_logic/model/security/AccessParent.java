/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * The relation leading from an object to its access parent: the object whose access definition
 * decides access to it.
 * <p>
 * An object with an access parent has no grants and no roles of its own. Whether a user may
 * perform an operation on it is whether the user may perform the corresponding operation on its
 * access parent, and that parent may delegate further. The relation is one of three:
 * </p>
 * <ul>
 * <li>{@link #container() The container}: the object holding the object in a composition, whichever
 * composition that is. This is the relation a composition part gets by default.</li>
 * <li>{@link #backward(TLReference) A composition navigated backwards}: the container, provided that
 * it holds the object through the named composition.</li>
 * <li>{@link #forward(TLReference) A to-one reference navigated forwards}: the object the reference
 * of the object points to.</li>
 * </ul>
 * 
 * @param reference
 *        The reference the relation navigates, <code>null</code> for the container through any
 *        composition.
 * @param inverse
 *        Whether the reference is navigated backwards, from the contained object to the container
 *        holding it.
 * @param explicit
 *        Whether the relation is configured for the type, in contrast to the container relation a
 *        composition part gets by default.
 * 
 * @see ModelAccessRights#getAccessParent(com.top_logic.model.TLClass)
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessParent()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record AccessParent(TLReference reference, boolean inverse, boolean explicit) {

	private static final AccessParent CONTAINER = new AccessParent(null, true, false);

	/**
	 * The relation to the container of an object, whichever composition holds it.
	 */
	public static AccessParent container() {
		return CONTAINER;
	}

	/**
	 * The relation to the container holding an object through the given composition.
	 * 
	 * @param composition
	 *        The composition navigated backwards. The type of its values is the type of the objects
	 *        delegating.
	 */
	public static AccessParent backward(TLReference composition) {
		return new AccessParent(composition, true, true);
	}

	/**
	 * The relation to the object a to-one reference of the delegating object points to.
	 * 
	 * @param reference
	 *        The reference navigated forwards. Its owner is the type of the objects delegating.
	 */
	public static AccessParent forward(TLReference reference) {
		return new AccessParent(reference, false, true);
	}

	/**
	 * Whether this is the {@link #container()} relation.
	 */
	public boolean isContainer() {
		return reference == null;
	}

	/**
	 * The access parent of the given object.
	 * 
	 * @param object
	 *        The object delegating its access decision.
	 * @return The object deciding, <code>null</code> when the relation leads nowhere: the object is
	 *         in no container, its container holds it through another composition than the one
	 *         navigated, or its reference is empty. An object without access parent is not
	 *         accessible.
	 */
	public TLObject resolve(TLObject object) {
		if (reference == null) {
			return object.tContainer();
		}
		if (inverse) {
			TLReference via = object.tContainerReference();
			if (via == null || !via.getDefinition().equals(reference.getDefinition())) {
				return null;
			}
			return object.tContainer();
		}
		Object value = object.tValue(reference);
		return value instanceof TLObject parent ? parent : null;
	}

}
