/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.func.Function1;

/**
 * {@link Function1} computing a {@link ComponentName#isLocalName() local component name} be the
 * local name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentNameByLocalName extends Function1<ComponentName, String> {

	@Override
	public ComponentName apply(String localName) {
		if (StringServices.isEmpty(localName)) {
			return null;
		}
		return ComponentName.newName(localName);
	}

}
