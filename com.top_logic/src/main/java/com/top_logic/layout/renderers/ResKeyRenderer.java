/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.provider.ResKeyResourceProvider;

/**
 * {@link Renderer} for {@link ResKey} instances displaying the internationalized text with an
 * optional tooltip.
 * 
 * @see ResKey#tooltip()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyRenderer {

	/**
	 * Singleton {@link ResKeyRenderer} instance.
	 */
	public static final Renderer<? super ResKey> INSTANCE =
		ResourceRenderer.newResourceRenderer(ResKeyResourceProvider.INSTANCE);

}
