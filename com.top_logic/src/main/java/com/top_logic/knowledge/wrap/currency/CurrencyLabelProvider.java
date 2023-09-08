/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.provider.ResourcedLabelProvider;

/**
 * {@link LabelProvider} for {@link Currency} instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CurrencyLabelProvider extends ResourcedLabelProvider {

	private static final ResPrefix CURRENCY_RESPREFIX = I18NConstants.UNITS;

	public static LabelProvider INSTANCE = new CurrencyLabelProvider();

	/**
	 * Creates a {@link CurrencyLabelProvider}.
	 */
	private CurrencyLabelProvider() {
		super(WrapperResourceProvider.INSTANCE, CURRENCY_RESPREFIX);
	}

	@FrameworkInternal
	public static ResKey labelKey(Wrapper anObject) {
		return CURRENCY_RESPREFIX.key(anObject.getName());
	}

}
