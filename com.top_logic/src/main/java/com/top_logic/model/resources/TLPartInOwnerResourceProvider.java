/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.visit.LabelVisitor;

/**
 * {@link TLPartResourceProvider} labeling a {@link TLStructuredTypePart} together with the type
 * owning it, for a list that offers the parts of several types side by side.
 * <p>
 * The label of a part is followed by the label of its owner in parentheses, so that the reference
 * of one type is told apart from the equally named reference of another.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLPartInOwnerResourceProvider extends TLPartResourceProvider {

	/** Singleton {@link TLPartInOwnerResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final TLPartInOwnerResourceProvider INSTANCE = new TLPartInOwnerResourceProvider();

	/**
	 * Creates a new {@link TLPartInOwnerResourceProvider}.
	 */
	protected TLPartInOwnerResourceProvider() {
		// singleton instance
	}

	@Override
	protected LabelVisitor createLabelVisitor() {
		return new LabelVisitor() {
			@Override
			public String visitReference(TLReference model, Void arg) {
				return inOwner(model, super.visitReference(model, arg));
			}

			@Override
			public String visitProperty(TLProperty model, Void arg) {
				return inOwner(model, super.visitProperty(model, arg));
			}

			private String inOwner(TLStructuredTypePart part, String label) {
				return label + " (" + part.getOwner().visit(this, null) + ")";
			}
		};
	}

}
