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
 * {@link LabelProvider} for {@link TLReference}s that displays a qualified name (with the owner
 * type).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTypePartWithOwnerType implements LabelProvider {

	/**
	 * Singleton {@link StructuredTypePartWithOwnerType} instance.
	 */
	public static final StructuredTypePartWithOwnerType INSTANCE = new StructuredTypePartWithOwnerType();

	/**
	 * Creates a {@link StructuredTypePartWithOwnerType}.
	 */
	protected StructuredTypePartWithOwnerType() {
		// Singleton constructor.
	}

	@Override
	public final String getLabel(Object object) {
		TLStructuredTypePart part = (TLStructuredTypePart) object;
		StringBuilder buffer = new StringBuilder();
		appendLabel(buffer, part);
		return buffer.toString();
	}

	/**
	 * Implementation of {@link #getLabel(Object)}.
	 */
	protected void appendLabel(StringBuilder buffer, TLStructuredTypePart part) {
		buffer.append(getInternationalizedName(part.getOwner()));
		buffer.append(".");
		buffer.append(getInternationalizedName(part));
	}

}
