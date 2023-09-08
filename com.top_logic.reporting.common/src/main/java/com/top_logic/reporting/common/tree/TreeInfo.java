/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.tree;

/**
 * The TreeInfo contains all information which is needed from the 
 * {@link com.top_logic.reporting.common.tree.TreeAxis}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TreeInfo {

    /** The parent node. */
    private int    parent;
    /** The tree node depth. */
    private int    depth;
    /** The label of the tree node. */
    private String label;
    /** The index of the icon for the tree node. */
    private int    iconIndex;
    /** The icon tooltip. */
    private String iconTooltip;
    /**  The url behind the icon. */
    private String iconUrl;
    /** The tooltip for the label. */
    private String labelTooltip;
    /** The url behind the icon. */
    private String labelUrl;
    /** An object. */
    private Object object;
    
    /** 
     * Creates a {@link TreeInfo} with the
     * given parameters.
     * 
     * @param aLabel     The label of this node. Must NOT be <code>null</code>.
     * @param aIconIndex The icon index of this node (0..n). 
     */
    public TreeInfo(String aLabel, int aIconIndex) {
        this(aLabel, aIconIndex, "", "", "", "");
    }

    /** 
     * Creates a {@link TreeInfo} with the
     * given parameters.
     * 
     * @param anObject      The object.
     * @param aLabel     The label of this node. Must NOT be <code>null</code>.
     * @param aIconIndex The icon index of this node (0..n). 
     */
    public TreeInfo(String aLabel, int aIconIndex, Object anObject) {
        this(aLabel, aIconIndex, "", "", "", "");
    }
    
    /** 
     * Creates a {@link TreeInfo} with the
     * given parameters.
     * 
     * @param aLabel        The label of this node. Must NOT be <code>null</code>.
     * @param aIconIndex    The icon index of this node (0..n).
     * @param aIconTooltip  The icon tooltip.
     * @param aIconUrl      The icon url.
     * @param aLabelTooltip The label tooltip.
     * @param aLabelUrl     The label url.
     */
    public TreeInfo(String aLabel, int aIconIndex, String aIconTooltip, String aIconUrl,
                    String aLabelTooltip, String aLabelUrl) {
        this(aLabel, aIconIndex, aIconTooltip, aIconUrl, aLabelTooltip, aLabelUrl, null);
    }
    
    /** 
     * Creates a {@link TreeInfo} with the
     * given parameters.
     * 
     * @param anObject      The object.
     * @param aLabel        The label of this node. Must NOT be <code>null</code>.
     * @param aIconIndex    The icon index of this node (0..n).
     * @param aIconTooltip  The icon tooltip.
     * @param aIconUrl      The icon url.
     * @param aLabelTooltip The label tooltip.
     * @param aLabelUrl     The label url.
     */
    public TreeInfo(String aLabel, int aIconIndex, String aIconTooltip, String aIconUrl,
                    String aLabelTooltip, String aLabelUrl, Object anObject) {
        this.object       = anObject;
        this.parent       = 0;
        this.depth        = 0;
        this.label        = aLabel;
        this.iconIndex    = aIconIndex;
        this.iconTooltip  = aIconTooltip;
        this.iconUrl      = aIconUrl;
        this.labelTooltip = aLabelTooltip;
        this.labelUrl     = aLabelUrl;
    }
    
    /**
     * This method returns the depth.
     * 
     * @return Returns the depth.
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * This method sets the depth.
     *
     * @param aDepth The depth to set.
     */
    public void setDepth(int aDepth) {
        this.depth = aDepth;
    }
    
    /**
     * This method returns the iconIndex.
     * 
     * @return Returns the iconIndex.
     */
    public int getIconIndex() {
        return this.iconIndex;
    }

    /**
     * This method sets the iconIndex.
     *
     * @param aIconIndex The iconIndex to set.
     */
    public void setIconIndex(int aIconIndex) {
        this.iconIndex = aIconIndex;
    }

    /**
     * This method returns the iconTooltip.
     * 
     * @return Returns the iconTooltip.
     */
    public String getIconTooltip() {
        return this.iconTooltip;
    }
    
    /**
     * This method sets the iconTooltip.
     *
     * @param aIconTooltip The iconTooltip to set.
     */
    public void setIconTooltip(String aIconTooltip) {
        this.iconTooltip = aIconTooltip;
    }
    
    /**
     * This method returns the iconUrl.
     * 
     * @return Returns the iconUrl.
     */
    public String getIconUrl() {
        return this.iconUrl;
    }
    
    /**
     * This method sets the iconUrl.
     *
     * @param aIconUrl The iconUrl to set.
     */
    public void setIconUrl(String aIconUrl) {
        this.iconUrl = aIconUrl;
    }
    
    /**
     * This method returns the label.
     * 
     * @return Returns the label.
     */
    public String getLabel() {
        return this.label;
    }
    
    /**
     * This method sets the label.
     *
     * @param aLabel The label to set.
     */
    public void setLabel(String aLabel) {
        this.label = aLabel;
    }

    /**
     * This method returns the labelTooltip.
     * 
     * @return Returns the labelTooltip.
     */
    public String getLabelTooltip() {
        return this.labelTooltip;
    }
    
    /**
     * This method sets the labelTooltip.
     *
     * @param aLabelTooltip The labelTooltip to set.
     */
    public void setLabelTooltip(String aLabelTooltip) {
        this.labelTooltip = aLabelTooltip;
    }
    
    /**
     * This method returns the labelUrl.
     * 
     * @return Returns the labelUrl.
     */
    public String getLabelUrl() {
        return this.labelUrl;
    }
    
    /**
     * This method sets the labelUrl.
     *
     * @param aLabelUrl The labelUrl to set.
     */
    public void setLabelUrl(String aLabelUrl) {
        this.labelUrl = aLabelUrl;
    }

    /**
     * This method returns the parent.
     * 
     * @return Returns the parent.
     */
    public int getParent() {
        return this.parent;
    }
    
    /**
     * This method sets the parent.
     *
     * @param aParent The parent to set.
     */
    public void setParent(int aParent) {
        this.parent = aParent;
    }
    
    /**
     * This method returns the object.
     * 
     * @return Returns the object.
     */
    public Object getObject() {
        return this.object;
    }
    
    /**
     * This method sets the object.
     *
     * @param aObject The object to set.
     */
    public void setObject(Object aObject) {
        this.object = aObject;
    }
    
}

