/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.text.Format;
import java.util.Date;

import com.top_logic.basic.config.FormatValueProvider;
import com.top_logic.basic.config.XmlDateTimeFormat;

/**
 * {@link FormatValueProvider} adapting the {@link XmlDateTimeFormat}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateValueProvider extends FormatValueProvider {

	/**
	 * Singleton {@link DateValueProvider} instance.
	 */
	public static final DateValueProvider INSTANCE = new DateValueProvider();

	private DateValueProvider() {
		super(Date.class);
	}

	@Override
	protected Format format() {
		return XmlDateTimeFormat.INSTANCE;
	}

}
