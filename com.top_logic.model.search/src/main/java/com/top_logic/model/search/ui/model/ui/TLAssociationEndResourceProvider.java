/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.ui;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;

/**
 * {@link ResourceProvider} for {@link TLReference} using cardinalities for image types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLAssociationEndResourceProvider extends TLStructuredTypePartResourceProvider {

	/**
	 * Singleton {@link TLAssociationEndResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TLAssociationEndResourceProvider INSTANCE = new TLAssociationEndResourceProvider();

	/**
	 * Creates a {@link TLAssociationEndResourceProvider}.
	 */
	protected TLAssociationEndResourceProvider() {
		super();
	}

	@Override
	protected String suffix(Object object) {
		TLAssociationEnd end = (TLAssociationEnd) object;
		boolean containment = end.isComposite();
		boolean mandatory = end.isMandatory();
		boolean multiple = end.isMultiple();
		return suffix(containment, "@containment") + suffix(mandatory, "@mandatory") + suffix(multiple, "@multiple");
	}

}
