/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * {@link ObjectConstructor} that creates a new instance generically.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultObjectConstructor implements ObjectConstructor {

	/**
	 * Singleton {@link DefaultObjectConstructor} instance.
	 */
	public static final DefaultObjectConstructor INSTANCE = new DefaultObjectConstructor();

	private static final ValueProvider NONE = new ValueProvider() {
		@Override
		public Object getValue(String aKey) {
			return null;
		}

		@Override
		public void setValue(String aName, Object aValue) {
			throw new UnsupportedOperationException();
		}
	};

	private DefaultObjectConstructor() {
		// Singleton constructor.
	}

	@Override
	public TLObject newInstance(TLFormObject overlay) {
		return DynamicModelService.getInstance().createObject((TLClass) overlay.getType(), overlay.tContainer(),
			NONE);
	}
}