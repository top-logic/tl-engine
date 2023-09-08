/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;


import com.top_logic.dob.MetaObject;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} that decides upon its implementation based on the first object it should
 * provide a label for.
 * 
 * <p>
 * Uses {@link LabelProviderService} for looking up its {@link LabelProvider} implementation. As key
 * to the registry, it either uses the {@link MetaObject} or the {@link Class} of the object it
 * should provide a label for.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link MetaLabelProvider} instance.
	 */
	public static final MetaLabelProvider INSTANCE = new MetaLabelProvider();
	
	/**
	 * Singleton constructor.
	 */
	private MetaLabelProvider() {
		super();
	}
	
	@Override
	public String getLabel(Object object) {
		return LabelProviderService.getLabel(object);
	}

}
