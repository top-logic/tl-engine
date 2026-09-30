/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;


import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLClass;

/**
 * Generates labels for objects using implementations registered in the application configuration.
 * 
 * <p>
 * Implementations can be registered in the {@link LabelProviderService}. As key to the registry,
 * either the {@link TLClass dynamic type} or the {@link Class Java class} of the object is used..
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Default label")
@InApp
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
