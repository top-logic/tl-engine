/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl.util;

import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Copies all characteristics from a source {@link TLClassPart} to a destination
 * {@link TLClassPart}, exception name and type.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLCharacteristicsCopier {

	/**
	 * Copy all characteristics from the source {@link TLClassPart} to the override.
	 * 
	 * @param overridden
	 *        Part the is overridden.
	 * @param part
	 *        Part that overrides <code>overridden</code>.
	 */
	public static void copyOverrideCharacteristics(TLStructuredTypePart overridden, TLStructuredTypePart part) {
		internalCopyCharacteristics(overridden, part, true);
	}

	/**
	 * Copy all characteristics from the source {@link TLClassPart} to the destination
	 * {@link TLClassPart}, exception name and type.
	 * 
	 * @param source
	 *        Is not allowed to be null.
	 * @param destination
	 *        Is not allowed to be null.
	 */
	public static void copyCharacteristics(TLStructuredTypePart source, TLStructuredTypePart destination) {
		internalCopyCharacteristics(source, destination, false);
	}

	private static void internalCopyCharacteristics(TLStructuredTypePart source, TLStructuredTypePart destination,
			boolean isOverride) {
		assert destination.getName().equals(source.getName());
		copyPartCharacteristics(source, destination, isOverride);
		assert TLModelUtil.isReference(destination) == TLModelUtil.isReference(source);
		boolean isReference = TLModelUtil.isReference(destination) && TLModelUtil.isReference(source);
		if (isReference) {
			TLAssociationEnd destinationEnd = ((TLReference) destination).getEnd();
			TLAssociationEnd sourceEnd = ((TLReference) source).getEnd();
			copyPartCharacteristics(sourceEnd, destinationEnd, isOverride);
			destinationEnd.setAggregate(sourceEnd.isAggregate());
			destinationEnd.setComposite(sourceEnd.isComposite());
			destinationEnd.setNavigate(sourceEnd.canNavigate());
			destinationEnd.setHistoryType(sourceEnd.getHistoryType());
		}
	}

	private static void copyPartCharacteristics(TLStructuredTypePart source, TLStructuredTypePart destination,
			boolean isOverride) {
		destination.setBag(source.isBag());
		destination.setMandatory(source.isMandatory());
		if (!isOverride) {
			/* Abstract must be re-declared. Otherwise the first concrete implementation must
			 * declare abstract="false", which is be unexpected. */
			destination.setAbstract(source.isAbstract());
		}
		destination.setMultiple(source.isMultiple());
		destination.setOrdered(source.isOrdered());
	}

}
