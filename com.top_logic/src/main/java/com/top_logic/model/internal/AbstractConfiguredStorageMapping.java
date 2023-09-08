/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.model.access.StorageMapping;

/**
 * Base class for configured {@link StorageMapping}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfiguredStorageMapping<C extends PolymorphicConfiguration<?>, T>
		extends AbstractConfiguredInstance<C> implements StorageMapping<T> {

	/**
	 * Creates a {@link AbstractConfiguredStorageMapping}.
	 */
	public AbstractConfiguredStorageMapping(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final int hashCode() {
		return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(getConfig());
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractConfiguredStorageMapping<?, ?> other = (AbstractConfiguredStorageMapping<?, ?>) obj;
		if (getConfig() == null) {
			if (other.getConfig() != null)
				return false;
		} else if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(getConfig(), other.getConfig()))
			return false;
		return true;
	}

}
