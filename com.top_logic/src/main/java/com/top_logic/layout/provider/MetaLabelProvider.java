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
 * Computes labels for objects using implementations registered in the application configuration for
 * object types.
 * 
 * <p>
 * Uses {@link LabelProviderService} for looking up its {@link LabelProvider} implementation. As key
 * to the registry, it either uses the {@link TLClass dynamic type} or the {@link Class Java class}
 * of the object to find an appropriate implementation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Application configuration lookup")
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
