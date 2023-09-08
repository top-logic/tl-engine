/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Code common to the {@link TLTypePart} implementations.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLTypePartCommon {

	/**
	 * Updates the {@link TLStructuredTypePart#getDefinition()}s of the given
	 * {@link TLStructuredType} and its subclasses, recursively.
	 * <p>
	 * Strictly speaking, this does not belong here but to "TLTypeCommon". But to avoid creating yet
	 * another new class just for this single method, it is here and not in "TLTypeCommon".
	 * </p>
	 */
	public static void updatePartDefinitions(TLStructuredType owner) {
		if (owner instanceof TLAssociation) {
			return;
		}
		TLClass ownerClass = (TLClass) owner;
		for (TLClassPart parts : TLModelUtil.getLocalParts(ownerClass)) {
			parts.updateDefinition();
		}
		for (TLClass subClasses : ownerClass.getSpecializations()) {
			updatePartDefinitions(subClasses);
		}
	}

	/**
	 * Re-calculate the {@link TLStructuredTypePart#getDefinition()} of the given part and its
	 * overrides.
	 * 
	 * @param part
	 *        Is not allowed to be null.
	 */
	public static void updateDefinition(TLStructuredTypePart part) {
		if ((part.getOwner() == null) || (part instanceof TLAssociationPart)) {
			/* "null" can happen during the creation of a part, before it is assigned to an owner. */
			part.setDefinition(part);
			return;
		}
		TLClassPart classPart = (TLClassPart) part;
		TLClassPart newDefinition = calcDefinition(classPart);
		part.setDefinition(newDefinition);
		propagateNewDefinition(classPart, newDefinition);
	}

	/**
	 * Calculates {@link TLClassPart#getDefinition()}.
	 * 
	 * @param part
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	private static TLClassPart calcDefinition(TLClassPart part) {
		/* This algorithm is based on the assumption, that there is exactly one definition and every
		 * way leads there. */
		for (TLClass superOwner : part.getOwner().getGeneralizations()) {
			TLStructuredTypePart overriddenPart = superOwner.getPart(part.getName());
			if (overriddenPart != null) {
				return calcDefinition((TLClassPart) overriddenPart);
			}
		}
		// This part is not overriding another part.
		return part;
	}

	private static void propagateNewDefinition(TLClassPart part, TLClassPart newDefinition) {
		for (TLClassPart overridingPart : TLModelUtil.getOverridingParts(part)) {
			overridingPart.setDefinition(newDefinition);
			propagateNewDefinition(overridingPart, newDefinition);
		}
	}

}
