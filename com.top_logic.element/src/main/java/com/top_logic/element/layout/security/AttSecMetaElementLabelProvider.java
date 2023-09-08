/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} that provides an additional label for the global domain
 * {@link AttributeClassifierRolesComponent#GLOBAL_DOMAIN}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class AttSecMetaElementLabelProvider implements LabelProvider {

	/** Singleton {@link AttSecMetaElementLabelProvider} instance. */
	public static final AttSecMetaElementLabelProvider INSTANCE = new AttSecMetaElementLabelProvider();

	/**
	 * Creates a new {@link AttSecMetaElementLabelProvider}.
	 */
	protected AttSecMetaElementLabelProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		if (object == AttributeClassifierRolesComponent.GLOBAL_DOMAIN) {
			return Resources.getInstance().getString(I18NConstants.GLOBAL_DOMAIN);
		} else {
			return MetaLabelProvider.INSTANCE.getLabel(object);
		}
	}

}
