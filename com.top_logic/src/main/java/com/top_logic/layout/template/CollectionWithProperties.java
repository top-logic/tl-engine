/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.util.AbstractCollection;
import java.util.Collection;

/**
 * {@link Collection} implementing {@link WithProperties}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CollectionWithProperties<T> extends AbstractCollection<T> implements WithProperties {

	/**
	 * See {@link #size()}.
	 */
	public static final String SIZE = "size";

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case SIZE:
				return size();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

}
