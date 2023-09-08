/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOAlternativeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;

/**
 * Abstract implementation of {@link MOFactory}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMOFactory implements MOFactory {

	@Override
	public MOClass createMOClass(String name) {
	    return new MOClassImpl(name);
	}

	@Override
	public MOAttribute createMOAttribute(String name, MetaObject type) {
	    return new MOAttributeImpl(name, type);
	}

	@Override
	public MOAlternativeBuilder createAlternativeBuilder(String name) {
		return new MOAlternativeImpl(name);
	}

}
