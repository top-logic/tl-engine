/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.util.TLModelUtil;

/**
 * Algorithm computing the module part of a qualified name.
 * 
 * <p>
 * The module part of a qualified name is <code>null</code>, if the name is not qualified.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleName extends Function1<String, String> {

	@Override
	public String apply(String qualifiedName) {
		if (qualifiedName == null) {
			return null;
		}
		int nameSepIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (nameSepIndex >= 0) {
			return qualifiedName.substring(0, nameSepIndex);
		} else {
			return null;
		}
	}

}