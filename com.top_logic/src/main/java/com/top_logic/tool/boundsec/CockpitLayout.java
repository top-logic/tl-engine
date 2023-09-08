/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * With a CockpitLayout you can build portlets.
 * 
 * A CockpitLayout may contain any kind of component. On all contained
 * components, that are <code>instanceof</code>
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras </a>
 */
public class CockpitLayout extends BoundLayout {

	/**
	 * Creates a {@link CockpitLayout} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param atts
	 *        The configuration.
	 */
	@CalledByReflection
	public CockpitLayout(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

}
