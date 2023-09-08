/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.tooltip;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * The WrapperCategoryTooltipGenerator generates tooltips for {@link Wrapper}s.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class WrapperCategoryTooltipGenerator extends ExtendedCategoryTooltipGenerator {

    /** 
     * @see com.top_logic.base.chart.generator.tooltip.ExtendedCategoryTooltipGenerator#getTooltipFor(java.lang.Object)
     */
    @Override
	public String getTooltipFor(Object anObject) {
        return  MetaResourceProvider.INSTANCE.getTooltip(anObject);
    }

}

