/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The checks of the {@link SimpleBoundCommandGroup#DELETE Delete} right: on a deleted object, and on
 * a part taken out of a composition.
 *
 * <p>
 * A part taken out of a composition is lost to its container, so its remove needs the right to
 * delete the part, as a delete of the part does. TL-Script and both user interfaces use these
 * checks, so that a remove is refused with the same message everywhere. Whether a single object may
 * be deleted is decided by {@link ModelAccessRights#isAllowed(TLObject, com.top_logic.tool.boundsec.BoundCommandGroup)}.
 * </p>
 *
 * @see DiscoverRight
 */
public final class DeleteRight {

	private DeleteRight() {
		// Static utility.
	}

	/**
	 * Whether the current user may delete the given object.
	 */
	@FrameworkInternal
	public static boolean mayDelete(TLObject object) {
		return ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), object, SimpleBoundCommandGroup.DELETE);
	}

	/**
	 * Checks that the current user may delete the given object.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#DELETE_PERMISSION_DENIED__OBJECT}, when the user may not
	 *         delete it.
	 */
	public static void checkDeletePermission(TLObject object) {
		if (!mayDelete(object)) {
			throw new TopLogicException(I18NConstants.DELETE_PERMISSION_DENIED__OBJECT.fill(object));
		}
	}

	/**
	 * Checks that the current user may delete each part that the given new value takes out of the
	 * given composition.
	 *
	 * <p>
	 * A reference that is no composition takes out no part. Each operation with security calls this
	 * before it changes the composition.
	 * </p>
	 *
	 * <p>
	 * The new value may take out a part the user may not discover: a secured script and a dialog
	 * see only the parts the user may discover. The refusal for such a part names the container and
	 * the attribute only, so that it does not reveal the part.
	 * </p>
	 *
	 * @param container
	 *        The object whose attribute changes.
	 * @param attribute
	 *        The changed attribute of the container.
	 * @param newValue
	 *        The new value of the attribute: an object, a collection of objects, or {@code null}.
	 *        An object stands for itself, it is compared by identity with the parts the container
	 *        holds now.
	 * @throws TopLogicException
	 *         With the {@link #removeRefusal(TLObject) refusal} of the first part that the new value
	 *         takes out and that the user may not delete.
	 */
	@FrameworkInternal
	public static void checkRemovedParts(TLObject container, TLStructuredTypePart attribute, Object newValue) {
		if (!(attribute instanceof TLReference reference) || !reference.isComposite()) {
			return;
		}
		Set<Object> kept = Collections.newSetFromMap(new IdentityHashMap<>());
		kept.addAll(DiscoverRight.asCollection(newValue));
		for (Object part : DiscoverRight.asCollection(container.tValue(attribute))) {
			if (part instanceof TLObject removed && !kept.contains(removed)) {
				ResKey refusal = removeRefusal(container, attribute, removed);
				if (refusal != null) {
					throw new TopLogicException(refusal);
				}
			}
		}
	}

	/**
	 * Why the current user may not take the given part out of the composition that holds it.
	 *
	 * <p>
	 * A user interface uses it to disable the remove of a part: the reason is the message the save
	 * of the remove would be refused with.
	 * </p>
	 *
	 * @param part
	 *        A part of a composition.
	 * @return {@code null}, when the user may delete the part. Otherwise
	 *         {@link I18NConstants#DELETE_PERMISSION_DENIED__OBJECT} naming the part, or
	 *         {@link I18NConstants#REMOVE_PERMISSION_DENIED__OBJECT_ATTRIBUTE} naming its container
	 *         and composition, when the user may not discover the part either.
	 */
	@FrameworkInternal
	public static ResKey removeRefusal(TLObject part) {
		return removeRefusal(part.tContainer(), part.tContainerReference(), part);
	}

	private static ResKey removeRefusal(TLObject container, TLStructuredTypePart attribute, TLObject part) {
		if (mayDelete(part)) {
			return null;
		}
		if (ModelAccessRights.getInstance().isDiscoverAllowed(part)) {
			return I18NConstants.DELETE_PERMISSION_DENIED__OBJECT.fill(part);
		}
		return I18NConstants.REMOVE_PERMISSION_DENIED__OBJECT_ATTRIBUTE.fill(container, attribute);
	}

}
