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
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * {@link ObjectConstructor} that creates a new instance generically.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultObjectConstructor implements ObjectConstructor {

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

	@Override
	public TLObject newInstance(TLFormObject overlay) {
		return factory().createObject((TLClass) overlay.getType(), overlay.tContainer(), NONE);
	}

	/**
	 * The factory used to allocated objects.
	 */
	protected abstract TLFactory factory();

	/**
	 * Singleton {@link DefaultObjectConstructor} instance.
	 */
	private static final ObjectConstructor PERSISTENT_INSTANCE = new DefaultObjectConstructor() {
		@Override
		protected TLFactory factory() {
			return DynamicModelService.getInstance();
		}
	};

	private static final ObjectConstructor TRANSIENT_INSTANCE = new DefaultObjectConstructor() {
		@Override
		protected TLFactory factory() {
			return TransientObjectFactory.INSTANCE;
		}
	};

	private DefaultObjectConstructor() {
		// Singleton constructor.
	}

	/**
	 * {@link ObjectConstructor} using {@link DynamicModelService} for allocation.
	 */
	public static ObjectConstructor getPersistentInstance() {
		return PERSISTENT_INSTANCE;
	}

	/**
	 * {@link ObjectConstructor} using {@link TransientObjectFactory} for allocation.
	 */
	public static ObjectConstructor getTransientInstance() {
		return TRANSIENT_INSTANCE;
	}
}