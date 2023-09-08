/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.awt.Font;
import java.util.HashMap;

import com.top_logic.reporting.report.util.ReportConstants;

/**
 * The title has an i18n text, a visibility and an align.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class Title implements ReportConstants {

    /**
     * The map contains the titles messages.
     * The keys are languages (e.g. 'de' or 'en').
     * The values are strings.
     */
    private HashMap messages;
    private boolean visible;
    private String  align;
    private Font    font;

    /** 
     * Creates a {@link Title}.
     */
    public Title() {
        this(INVISIBLE, ALIGN_CENTER, new HashMap());
    }
    
    /**
     * Creates a {@link Title}.
     * 
     * @param someMessages
     *        See {@link #messages}.
     */
    public Title(HashMap someMessages) {
        this(INVISIBLE, ALIGN_CENTER, someMessages);
    }
    
    /** 
     * Creates a {@link Title}.
     * 
     * @param isVisible See {@link #isVisible()}.
     * @param anAlign   Sie {@link #getAlign()}.
     */
    public Title(boolean isVisible, String anAlign, HashMap someMessages) {
        this.messages   = someMessages;
        this.visible    = isVisible;
        this.align      = anAlign;
        this.font       = null;
    }

    /**
     * This method returns an title message for the given language (e.g. 'de' or
     * 'en'). Never <code>null</code> but maybe an empty string.
     */
    public String getMessage(String aLanguage) {
        String theMessage = (String) this.messages.get(aLanguage);
        
        return theMessage != null ? theMessage : "";
    }
    
    public void setMessage(String aLanguage, String aText) {
        this.messages.put(aLanguage, aText);
    }
   
    /**
     * This method returns <code>true</code> if the title is visible,
     * <code>false</code> otherwise. Please use the constants of
     * {@link ReportConstants}.
     */
    public boolean isVisible() {
        return this.visible;
    }
    
    /** See {@link #isVisible()}. */
    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }

    /** 
     * This method returns the align of this title. 
     * Please use the constants of {@link ReportConstants}.
     */
    public String getAlign() {
        return this.align;
    }

    /** See {@link #getAlign()}. */
    public void setAlign(String aAlign) {
        this.align = aAlign;
    }

    /**
     * This method returns the font of this title. The font can be
     * <code>null</code>.
     */
    public Font getFont() {
        return this.font;
    }
    
    /** See {@link #getFont()}. */
    public void setFont(Font aFont) {
        this.font = aFont;
    }
    
}

