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
 * {@link ObjectLinking} that adds the value to a multi-reference of it's context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListLinking<C extends ListLinking.Config<?>> extends AbstractObjectLinking<C> {

	/**
	 * Configuration options for {@link ListLinking}.
	 */
	@TagName("list-linking")
	public interface Config<I extends ListLinking<?>> extends AbstractObjectLinking.Config<I> {
		/**
		 * The multi-reference of the context object to add the value to.
		 */
		PartRef getName();
	}

	/**
	 * Creates a {@link ListLinking} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ListLinking(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void link(ImportContext context, Object self, Object value, Runnable continuation) {
		PartRef partRef = getConfig().getName();
		context.isInstanceOf(self, partRef.getType(),
			() -> context.addValue(this, self, partRef.getPart(), value),
			continuation);
	}

}
