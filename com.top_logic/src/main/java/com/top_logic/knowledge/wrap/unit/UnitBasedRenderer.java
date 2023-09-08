/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.unit;

import java.io.IOException;
import java.text.NumberFormat;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Render a number value based on the unit the instance has been initialized with.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UnitBasedRenderer implements Renderer<Number> {

    /** The unit to be used for formatting. */
    private final Unit unit;

    /** Flag, if zero values have to be ignored. */
    private final boolean ignoreZero;

    /** 
     * Creates a {@link UnitBasedRenderer}.
     * 
     * @param    aUnit    The unit to be used for formatting, must not be <code>null</code>.
     */
    public UnitBasedRenderer(Unit aUnit) {
        this(aUnit, false);
    }
    
    /** 
     * Creates a {@link UnitBasedRenderer}.
     * 
     * @param    aUnit         The unit to be used for formatting, must not be <code>null</code>.
     * @param    ignoreZero    Flag, if null values should be ignored in rendering.
     */
    public UnitBasedRenderer(Unit aUnit, boolean ignoreZero) {
        if (aUnit == null) {
            throw new IllegalArgumentException("Given unit is null");
        }
        
        this.unit       = aUnit;
        this.ignoreZero = ignoreZero;
    }

    @Override
	public void write(DisplayContext aContext, TagWriter anOut, Number aValue) throws IOException {
		if (aValue == null) {
			return;
        }

		if (this.ignoreZero && aValue.doubleValue() == 0.0d) {
			return;
        }

		NumberFormat format = this.unit.getFormat(aContext.getResources().getLocale());
		anOut.writeText(format.format(aValue));
    }
}

