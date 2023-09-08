/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.linkings;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ObjectLinking;
import com.top_logic.xio.importer.binding.PartRef;

/**
 * {@link ObjectLinking} that assigns a reference of the context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RefLinking<C extends RefLinking.Config<?>> extends AbstractObjectLinking<C> {

	/**
	 * Configuration options for {@link RefLinking}.
	 */
	@TagName("ref-linking")
	public interface Config<I extends RefLinking<?>> extends AbstractObjectLinking.Config<I> {
		/**
		 * The name of the refernce to assign to.
		 */
		PartRef getName();
	}

	/**
	 * Creates a {@link RefLinking} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RefLinking(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void link(ImportContext context, Object self, Object value, Runnable continuation) {
		context.isInstanceOf(self, getConfig().getName().getType(),
			() -> context.setReference(this, self, getConfig().getName().getPart(), value),
			continuation);
	}

}
