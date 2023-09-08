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
 * The Axis has i18n messages, a visibility, a gridline and a font.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class Axis {

    /**
     * The map contains the axis messages. 
     * The keys are languages (e.g. 'de' or 'en'). 
     * The values are strings.
     */
    private HashMap messages;

    private boolean visible;

    private boolean gridline;

    private Font    font;
    
    private Font    valueFont;

    /** 
     * Creates a {@link Axis}.
     */
    public Axis() {
        this(new HashMap());
    }
    
    /** 
     * Creates a {@link Axis}.
     * 
     * @param someMessages See {@link #messages}.
     */
    public Axis(HashMap someMessages) {
        this.messages  = someMessages;
        this.visible   = false;
        this.gridline  = false;
        this.font      = null;
        this.valueFont = null;
    }

    /**
     * This method returns an axis message for the given language (e.g. 'de' or
     * 'en'). Never <code>null</code> but maybe an empty string.
     */
    public String getMessage(String aLanguage) {
        String theMessage = (String) this.messages.get(aLanguage);
        
        return theMessage != null ? theMessage : "";
    }
    
    public void setMessage(String aLanguage, String aText) {
        this.messages.put(aLanguage, aText);
    }
    
    /** See {@link #messages}. */
    public HashMap getMessages() {
        return this.messages;
    }

    /** See {@link #messages}. */
    public void setMessages(HashMap aMessages) {
        this.messages = aMessages;
    }

    /**
     * This method returns the font of this axis. The font can be
     * <code>null</code>.
     */
    public Font getFont() {
        return this.font;
    }
    
    /** See {@link #getFont()}. */
    public void setFont(Font aFont) {
        this.font = aFont;
    }
    
    /**
     * This method returns the font for the values of this axis. The font can be
     * <code>null</code>.
     */
    public Font getValueFont() {
        return this.valueFont;
    }
    
    /** See {@link #getValueFont()}. */
    public void setValueFont(Font aFont) {
        this.valueFont = aFont;
    }
    
    /**
     * This method returns <code>true</code> if the grid line for this axis is
     * visible, <code>false</code> otherwise.
     */
    public boolean hasGridline() {
        return this.gridline;
    }
    
    /** See {@link #hasGridline()}. */
    public void setGridline(boolean aGridline) {
        this.gridline = aGridline;
    }
    
    /**
     * This method returns <code>true</code> if the axis is visible,
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
    
}
