/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.ui;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.DateTimeLabelProvider;
import com.top_logic.layout.provider.ProxyLabelProvider;

/**
 * {@link LabelProvider} that labels an object with its modified date.
 * 
 * <p>
 * Note: This {@link LabelProvider} must not be used for sortable table columns, because the sort
 * order would not be the date
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModifiedDateLabelProvider extends ProxyLabelProvider {

	/**
	 * Singleton {@link ModifiedDateLabelProvider} instance.
	 */
	public static final ModifiedDateLabelProvider INSTANCE = new ModifiedDateLabelProvider();

	/**
	 * Creates a {@link ModifiedDateLabelProvider}.
	 */
	public ModifiedDateLabelProvider() {
		this(DateTimeLabelProvider.INSTANCE);
	}

	/**
	 * Creates a {@link ModifiedDateLabelProvider}.
	 * 
	 * @param dateLabelProvider
	 *        The {@link LabelProvider} to lable the modfied date.
	 */
	public ModifiedDateLabelProvider(LabelProvider dateLabelProvider) {
		super(dateLabelProvider);
	}

	@Override
	public String getLabel(Object object) {
		return super.getLabel(((Wrapper) object).getModified());
	}

}
