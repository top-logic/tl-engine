/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.Validator;

/**
 * {@link ControlValidator} is a validator to revalidate
 * {@link AbstractControlBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ControlValidator implements Validator<AbstractControlBase> {

	public static final Validator<AbstractControlBase> INSTANCE = new ControlValidator();

	private ControlValidator() {
		// just one instance
	}

	@Override
	public void validate(DisplayContext context, UpdateQueue queue, AbstractControlBase control) {
		control.revalidate(context, queue);
	}

}
