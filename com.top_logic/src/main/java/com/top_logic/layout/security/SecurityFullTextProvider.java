/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.ProxyLabelProvider;
import com.top_logic.util.Resources;

/**
 * Full text provider for protected column.
 * 
 * <p>
 * This {@link LabelProvider} gets values returned by the sort key provider of the column. This is
 * by default a mapping that maps {@link ProtectedValue} wither to the actual value or to a
 * {@link ProtectedValueReplacement}. For such a value the default
 * {@link ProtectedValueRenderer#getBlockedText(Resources) blocked text} is returned.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityFullTextProvider extends ProxyLabelProvider {

	/**
	 * Creates a new {@link SecurityFullTextProvider}.
	 * 
	 * @param impl
	 *        The {@link LabelProvider} to dispatch {@link #getLabel(Object)} for objects that are
	 *        not {@link ProtectedValueReplacement}.
	 */
	public SecurityFullTextProvider(LabelProvider impl) {
		super(impl);
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof ProtectedValueReplacement) {
			return ProtectedValueRenderer.getBlockedText(Resources.getInstance());
		}
		return super.getLabel(object);
	}

}

