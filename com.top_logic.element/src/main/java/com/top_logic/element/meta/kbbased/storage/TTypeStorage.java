/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageImplementation} for the built-in <code>tType</code> attribute of all
 * {@link TLObject}s.
 */
public class TTypeStorage extends ReadOnlyForeignKeyStorage<TTypeStorage.Config<?>> {

	/**
	 * Configuration options for {@link TTypeStorage}.
	 */
	public interface Config<I extends TTypeStorage> extends ReadOnlyForeignKeyStorage.Config<I> {
		@Override
		@StringDefault("tType")
		String getStorageAttribute();

		@Override
		@StringDefault("TLObject")
		String getStorageType();
	}

	/**
	 * Creates a {@link TTypeStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TTypeStorage(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Object getFormValue(TLFormObjectBase formObject, TLStructuredTypePart part) {
		// Must use short-cut to prevent failing ID lookup.
		return formObject.tType();
	}

}
