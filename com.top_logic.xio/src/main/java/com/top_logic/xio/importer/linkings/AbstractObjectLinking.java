/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.linkings;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ObjectLinking;
import com.top_logic.xio.importer.handlers.ConfiguredImportPart;

/**
 * Base class for {@link ObjectLinking} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractObjectLinking<C extends AbstractObjectLinking.Config<?>>
		extends AbstractConfiguredInstance<C> implements ObjectLinking, ConfiguredImportPart<C> {

	/**
	 * Configuration options for {@link AbstractObjectLinking}.
	 */
	@Abstract
	public interface Config<I extends AbstractObjectLinking<?>> extends PolymorphicConfiguration<I> {
		/**
		 * Whether a property in the newly created object is referencing the context object instead
		 * of vice versa.
		 */
		boolean getReverse();
	}

	/**
	 * Creates a {@link AbstractObjectLinking} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractObjectLinking(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final void linkOrElse(ImportContext context, Object scope, Object target, Runnable continuation) {
		Object self;
		Object value;
		if (getConfig().getReverse()) {
			self = target;
			value = scope;
		} else {
			self = scope;
			value = target;
		}
		link(context, self, value, continuation);
	}

	/**
	 * Actually links the given value to the given object <code>self</code>.
	 *
	 * @param context
	 *        See {@link #link(ImportContext, Object, Object, Runnable)}.
	 * @param self
	 *        The context object to link the value to.
	 * @param value
	 *        The value to link to the context object.
	 * @param continuation
	 *        See {@link ObjectLinking#linkOrElse(ImportContext, Object, Object, Runnable)}.
	 */
	protected abstract void link(ImportContext context, Object self, Object value, Runnable continuation);

}
