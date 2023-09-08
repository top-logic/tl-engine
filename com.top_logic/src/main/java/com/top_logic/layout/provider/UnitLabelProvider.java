/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.layout.ResPrefix;

/**
 * A LabelProvider for UnitWrappers that knows about Currencies.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class UnitLabelProvider extends ResourcedLabelProvider {

	/** I18N prefix used to fetch internationalisation. */
	public static final ResPrefix I18N_PREFIX = ResPrefix.legacyString(Unit.DISPLAY_NAME_ATTRIBUTE_KEY + '.');

	/** 
     * Creates a new UnitLabelProvider based on WrapperResourceProvider and prefix "unit.".
     */
    public UnitLabelProvider() {
		super(WrapperResourceProvider.INSTANCE, I18N_PREFIX);
    }

    /**
     * Use name for Currency, ID for Unit and "" for null.
     */
    @Override
	public String getLabel(Object anUnit) {
        return (anUnit == null) ? "" : super.getLabel(anUnit);
    }
}

