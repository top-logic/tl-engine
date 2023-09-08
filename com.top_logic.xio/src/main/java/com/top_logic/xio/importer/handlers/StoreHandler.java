/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * Base class for {@link Handler}s assigning a value to a model element.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class StoreHandler<C extends StoreHandler.Config<?>> extends DispatchingImporter<C> {

	/**
	 * Configuration options for {@link StoreHandler}.
	 */
	public interface Config<I extends StoreHandler<?>> extends DispatchingImporter.Config<I> {

		/**
		 * Name of the {@link ImportContext#getVar(String) variable} that contains the object whose
		 * {@link #getName() property} is assigned.
		 */
		@StringDefault(ImportContext.THIS_VAR)
		@Nullable
		String getTargetVar();

		/**
		 * The name of the model property to assign.
		 */
		@Mandatory
		String getName();

	}

	/**
	 * Creates a {@link StoreHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StoreHandler(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Stores the given value as property to the configured location.
	 */
	protected void storeProperty(ImportContext context, Object value) {
		context.setProperty(this, var(context), name(), value);
	}

	/**
	 * Stores the given value as reference to the configured location.
	 */
	protected void storeReference(ImportContext context, Object value) {
		context.setReference(this, var(context), name(), value);
	}

	/**
	 * The object to store values to.
	 */
	protected final Object var(ImportContext context) {
		return context.getVar(getConfig().getTargetVar());
	}

	private String name() {
		return getConfig().getName();
	}

}
