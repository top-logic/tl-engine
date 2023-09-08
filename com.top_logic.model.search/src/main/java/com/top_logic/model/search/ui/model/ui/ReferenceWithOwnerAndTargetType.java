/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.ui;

import static com.top_logic.model.search.ui.model.ui.TLNamedPartResourceProvider.*;

import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link LabelProvider} for {@link TLReference}s that also displays the target type of the
 * reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceWithOwnerAndTargetType extends StructuredTypePartWithOwnerType {

	/**
	 * Singleton {@link ReferenceWithOwnerAndTargetType} instance.
	 */
	@SuppressWarnings("hiding")
	public static final ReferenceWithOwnerAndTargetType INSTANCE = new ReferenceWithOwnerAndTargetType();

	private ReferenceWithOwnerAndTargetType() {
		// Singleton constructor.
	}

	@Override
	protected void appendLabel(StringBuilder buffer, TLStructuredTypePart part) {
		super.appendLabel(buffer, part);

		buffer.append(" : ");
		buffer.append(getInternationalizedName(part.getType()));
	}

}
