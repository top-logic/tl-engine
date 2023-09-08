/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link DemoCarrier} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoCarrierLabelProvider implements LabelProvider {

	public static final LabelProvider INSTANCE = new DemoCarrierLabelProvider();

	private DemoCarrierLabelProvider() {
	    // cannot be constrcuted, use INSTANCE
    }

	@Override
	public String getLabel(Object object) {
		DemoCarrier theCarrier = (DemoCarrier) object;
		return theCarrier.getName();
	}
	
}
