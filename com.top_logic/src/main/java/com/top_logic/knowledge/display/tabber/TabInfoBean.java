/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.display.tabber;

import java.util.ArrayList;

/**
 * Storage bean to handle tabbed panes using html framesets.
 *
 * @author    <a href=mailto:jco@top-logic.com>J&ouml;rg Connotte</a>
 */
public class TabInfoBean extends ArrayList {

    /** Index of the currently selected tab, <code>0</code> for the default. */
    protected int currentTabIndex;
    /** Height (in px) of the tab bar. */
    protected int barHeight;

    /** Name of the tab style. */
    protected String tabStyle;
    /** CSS class for displaying the tab text. */
    protected String textClass  = "";

    /**
     * Default, empty Constructor
     */
    public TabInfoBean() {
    }

    /**
     * Constructor with style.
     */
    public TabInfoBean(String aStyleName) {
        tabStyle = aStyleName;
    }

    /**
     * Returns a String[][] array containing tabber names and
     * URLs in position [][0] and [][1] respectively.
     */
    public String[][] getTabTable() {
        int size = size();
        String[][] tabs = new String[size][3];
        for (int i = 0; i < size; i++) {
            TabEntry entry = (TabEntry) get(i);
            tabs[i][0] = entry.getTabName();
            tabs[i][1] = entry.getTabLink();
            tabs[i][2] = entry.getTabTip();
        }

        return tabs;
    }

    /**
     * Sets the internal table holding tabber names and URLs to
     * the array provided.
     */
    public void setTabTable(String[][] aTable) {
        this.clear();
        for (int i = 0; i < aTable.length; i++) {
            add(new TabEntry(aTable[i][0], aTable[i][1], aTable[i][2]));
        }
    }


    /**
     * Sets the style used for the tabs. <pre>aStyleName</pre> should be the
     * name of a directory below /jsp/images/tabber. The images located in this
     * directory are used for drawing the tab bars.
     */
    public void setTabStyle(String aStyleName) {
        tabStyle = aStyleName;
    }
    
    /**
     * The style used for the tabs.
     *
     * @see #setTabStyle(String)
     */
    public String getTabStyle() {
        return tabStyle;
    }

    
    /**
     * Sets the (CSS) class used for displaying the tabber text.
     */
    public void setTextClass(String aClassName) {
        textClass = aClassName;
    }
    
    /**
     * The (CSS) class used for displaying the tab text.
     *
     * @see #setTextClass(String)
     */
    public String getTextClass() {
        return textClass;
    }
    
    /**
     * Sets the height (in px) used for the tabber bar.
     */
    public void setBarHeight(int aHeight) {
        barHeight = aHeight;
    }
    
    /**
     * The height (in px) of the tab bar.
     *
     * @see #setBarHeight(int)
     */
    public int getBarHeight() {
        return barHeight;
    }

    /**
     * Returns the index of the currently selected tabber.
     */
    public int getCurrentTabIndex() {
        if (currentTabIndex != 0) {
            return currentTabIndex;
        } else {
            return 1;
        }
    }

    /**
     * Sets the currently selected tabber to the index position given.
     */
    public void setCurrentTabIndex(int anIndex) {
        if (anIndex <= size())
            currentTabIndex = anIndex;
    }

    /**
     * Returns the name of the tabber at the given index position.
     */
    public String getTabName(int index) {
        try {
            return ((TabEntry) get(index)).getTabName();
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Returns the {@link TabEntry} at the given index position.
     */
    public TabEntry getTabEntry(int index) {
        try {
            return (TabEntry) get(index);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Returns the URL of the tabber at the given index position.
     */
    public String getTabLink(int index) {
        try {
            return ((TabEntry) get(index)).getTabLink();
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Adds a tabber.
     */
    public void addTabEntry(String aTabName, String aTabLink) {
        add(new TabEntry(aTabName, aTabLink));
    }

    /**
     * Adds a tabber.
     */
    public void addTabEntry(String aTabName, String aTabLink, String aTabTip) {
        add(new TabEntry(aTabName, aTabLink, aTabTip));
    }

    /**
     * Removes the tabber at the given index position. Nothing happens
     * if index is out of range.
     */
    public void removeTabEntry(int index) {
        try { // do we remove the currently selected tabber?
            remove(index);
            if (getCurrentTabIndex() >= index) {
                setCurrentTabIndex(0);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // do nothing
        }
    }

    /**
     * Returns the {@link TabEntry} for the given tabber name. Returns null
     * if not found.
     */
    public TabEntry getTabEntryForName(String aTabName) {
        int size = size();
        for (int i = 0; i < size; i++) {
            TabEntry theEntry = (TabEntry) get(i);
            if (theEntry.getTabName().equals(aTabName))
                return theEntry;
        }
        return null;
    }

    /**
     * Returns the {@link TabEntry} for the given tabber URL. Returns null
     * if not found.
     */
    public TabEntry getTabEntryForLink(String aTabLink) {

        int size = size();
        for (int i = 0; i < size; i++) {
            TabEntry theEntry = (TabEntry) get(i);
            if (theEntry.getTabLink().equals(aTabLink))
                return theEntry;
        }
        return null;

    }

    /**
     * Sets the URL of the tabber with given name to given URL.
     */
    public void changeTabEntryLink(String aTabName, String aTabLink) {
        getTabEntryForName(aTabName).setTabLink(aTabLink);
    }

    /**
     * Sets the URL of the tabber with given URL to given name.
     */
    public void changeTabEntryName(String aTabLink, String aTabName) {
        getTabEntryForLink(aTabLink).setTabName(aTabName);
    }

    // Package methodes
    // Protected methodes
    // Private methodes    

    /**
     * Inner class, encapsulating name, alt-text and URL of a tabber.
     */
    public static final class TabEntry {
        
        private String tabName;
        private String tabLink;
        private String tabTip;
        
        /**
         * Creates a {@link TabEntry} using the tab name as tool tip.
         */
        public TabEntry (String aTabName, String aTabLink) {
            this (aTabName, aTabLink, aTabName);
            
        }
        
        public TabEntry (String aTabName, String aTabLink, String aTabTip) {
            tabName = aTabName;
            tabLink = aTabLink;
            tabTip  = aTabTip;
        }
        
    
        /** Setter for {@link #getTabName()}. */
        public final void setTabName(String aTabName) {
            tabName = aTabName;
        }
        
        /** Setter for {@link #getTabLink()}. */
        public final void setTabLink(String aTabLink) {
            tabLink = aTabLink;
        }
        
        /** Setter for {@link #getTabTip()}. */
        public final void setTabTip (String aTabTip) {
            tabTip = aTabTip;
        }
        /** The name of the tab. */
        public final String getTabName() {
            return tabName;
        }
        
        /** The URL displayed in the tab. */
        public final String getTabLink() {
            return tabLink;
        }
        
        /** The tool tip of the tab. */
        public final String getTabTip () {
            return tabTip;
        }
        
    }
    // Static methodes
}
