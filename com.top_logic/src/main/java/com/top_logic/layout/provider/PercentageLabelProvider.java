/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * LabelProvider to display percentage values. You may add pre- and sufix. You can also
 * provide a special prefix for 100%.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PercentageLabelProvider implements LabelProvider {

    private String prefix;

    private String lastprefix;

    private String suffix;

    /**
     * Contsructs a new {@link PercentageLabelProvider}
     */
    public PercentageLabelProvider() {
        this("", "");
    }

    /**
     * Contsructs a new {@link PercentageLabelProvider}
     * 
     * @param    aPrefix    Used for all values.
     * @param    aSuffix    Used for all values.
     */
    public PercentageLabelProvider(String aPrefix, String aSuffix) {
        this(aPrefix, aPrefix, aSuffix);
    }

    /**
     * Contsructs a new {@link PercentageLabelProvider}
     * 
     * @param    aPrefix        Used for all values except 100%.
     * @param    aLastPrefix    Prefix used for 100%.
     * @param    aSuffix         Used for all values.
     */
    public PercentageLabelProvider(String aPrefix, String aLastPrefix, String aSuffix) {
        this.setPrefix(aPrefix);
        this.setLastprefix(aLastPrefix);
        this.setSufix(aSuffix);
    }

    /**
     * Values should be between 0 and 1.
     * 
     * @param    anObject    The float value.
     * @return   A string like ">=90%".
     * @see     com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        double theDouble = ((Number) anObject).doubleValue();

        if (theDouble >= 1) {
            return this.lastprefix + HTMLFormatter.getInstance().formatPercent(theDouble) + this.suffix;
        }
        else { 
            return this.prefix + HTMLFormatter.getInstance().formatPercent(theDouble) + this.suffix;
        }
    }

    private void setPrefix(String aPrefix) {
        this.prefix = (aPrefix == null) ? "" : aPrefix;
    }

    /**
     * @param    aLastprefix    The lastprefix to set.
     */
    private void setLastprefix(String aLastprefix) {
        this.lastprefix = (aLastprefix == null) ? "" : aLastprefix;
    }

    /**
     * @param     aSuffix    The sufix to set.
     */
    private void setSufix(String aSuffix) {
        this.suffix = (aSuffix == null) ? "" : aSuffix;
    }
}
