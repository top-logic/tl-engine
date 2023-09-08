/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.ui;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLReference;

/**
 * {@link ResourceProvider} for {@link TLReference} using cardinalities for image types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLReferenceResourceProvider extends TLStructuredTypePartResourceProvider {

	/**
	 * Singleton {@link TLReferenceResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TLReferenceResourceProvider INSTANCE = new TLReferenceResourceProvider();

	/**
	 * Creates a {@link TLReferenceResourceProvider}.
	 */
	protected TLReferenceResourceProvider() {
		super();
	}

	@Override
	protected String suffix(Object object) {
		TLReference reference = (TLReference) object;
		boolean mandatory = reference.isMandatory();
		boolean multiple = reference.getEnd().isMultiple();
		boolean containment = reference.getEnd().isComposite();
		return suffix(containment, "@containment") + suffix(mandatory, "@mandatory") + suffix(multiple, "@multiple");
	}

}
