/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.I18NConstants;
import com.top_logic.layout.TooltipProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * {@link TooltipProvider} displaying type and label.
 */
public class SimpleTooltipProvider implements TooltipProvider {

	/**
	 * Singleton {@link SimpleTooltipProvider} instance.
	 */
	public static final SimpleTooltipProvider INSTANCE = new SimpleTooltipProvider();

	private SimpleTooltipProvider() {
		// Singleton constructor.
	}

	@Override
	public String getTooltip(Object object) {
		return object == null ? null : Resources.getInstance().getString(getTooltipNonNull(object));
	}

	private ResKey getTooltipNonNull(Object object) {
		return I18NConstants.WRAPPER_TOOLTIP.fill(
			DefaultResourceProvider.quote(object),
			DefaultResourceProvider.quote(TLModelUtil.type(object)));
	}

}
