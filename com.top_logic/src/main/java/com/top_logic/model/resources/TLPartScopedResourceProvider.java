/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLScope;
import com.top_logic.model.visit.LabelVisitor;

/**
 * {@link TLPartResourceProvider} appending the scope label to local types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLPartScopedResourceProvider extends TLPartResourceProvider {

	/** Singleton {@link TLPartScopedResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final TLPartScopedResourceProvider INSTANCE = new TLPartScopedResourceProvider();

	/**
	 * Creates a new {@link TLPartScopedResourceProvider}.
	 * 
	 */
	protected TLPartScopedResourceProvider() {
		// singleton instance
	}

	@Override
	protected LabelVisitor createLabelVisitor() {
		return new LabelVisitor() {
			@Override
			public String visitClass(TLClass model, Void arg) {
				String result = super.visitClass(model, arg);

				TLScope scope = model.getScope();
				if (scope != model.getModule()) {
					result = result + " (" + MetaLabelProvider.INSTANCE.getLabel(scope) + ")";
				}
				return result;
			}
		};
	}

}
