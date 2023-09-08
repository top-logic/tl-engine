/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.awt.Font;

import com.top_logic.reporting.report.util.ReportConstants;

/**
 * The Legend has an alignment, a visibility and maybe a font.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class Legend implements ReportConstants {

    /** See {@link #getAlign()}. */
    private String  align;
    /** See {@link #isVisible()}. */
    private boolean visible;
    /** See {@link #getFont()}. */
    private Font    font;

    /** 
     * Creates a {@link Legend}.
     */
    public Legend() {
        this(ALIGN_BOTTOM, false, null);
    }
    
    /** 
     * Creates a {@link Legend}.
     * 
     * @param aAlign    See {@link #getAlign()}.
     * @param isVisible See {@link #isVisible()}.
     */
    public Legend(String aAlign, boolean isVisible) {
        this(aAlign, isVisible, null);
    }
    
    /** 
     * Creates a {@link Legend}.
     * 
     * @param aAlign    See {@link #getAlign()}.
     * @param isVisible See {@link #isVisible()}.
     * @param aFont     See {@link #getFont()}.
     */
    public Legend(String aAlign, boolean isVisible, Font aFont) {
        this.align   = aAlign;
        this.visible = isVisible;
        this.font    = aFont;
    }

    /** 
     * This method returns the alignment of this legend.
     * 
     * Please, use one of the constant of the interface {@link ReportConstants}.
     */
    public String getAlign() {
        return this.align;
    }

    /** See {@link #getAlign()}. */
    public void setAlign(String aAlign) {
        this.align = aAlign;
    }

    /** 
     * This method returns the font of the legend. 
     * The font is maybe <code>null</code>.
     */
    public Font getFont() {
        return this.font;
    }
    
    /** See {@link #getFont()}. */
    public void setFont(Font aFont) {
        this.font = aFont;
    }
    
    /**
     * This method returns <code>true</code> if the legend is visible,
     * <code>false</code> otherwise.
     */
    public boolean isVisible() {
        return this.visible;
    }
    
    /** See {@link #isVisible()}. */
    public void setVisible(boolean aVisible) {
        this.visible = aVisible;
    }
    
}

