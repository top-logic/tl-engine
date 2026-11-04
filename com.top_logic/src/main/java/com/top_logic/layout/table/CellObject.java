/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.layout.basic.ThemeImage;

/**
 * This class adds some flavors to an object.<br/>
 * Currently supported flavors: tooltip, image, style.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class CellObject implements Comparable {

    /** The proper value. */
    private Object value;

    /** The tooltip for the value. */
    private String tooltip;

	/** The CSS class for the value. */
	private String cssClass;

    /** The style for the value. */
    private String style;

    /** The image for use. */
	private ThemeImage image;

    /** Flag indicating whether to disable the link display of the wrapped value. */
    private boolean disableLink = false;

    /** Flag indicating whether to disable the image display of the wrapped value. */
    private boolean disableImage = false;

    /** Flag indicating whether to disable the tooltip display of the wrapped value. */
    private boolean disableTooltip = false;

    // tooltip or image of the wrapped value is disabled automatically if a custom tooltip or image is set.



    /**
     * Creates a {@link CellObject}.
     */
    public CellObject() {
        this(null, null, null, null);
    }

    /**
     * Creates a new {@link CellObject} around the given object and no tooltip.
     *
     * @param aValue
     *            the object to encapsulate
     */
    public CellObject(Object aValue) {
        this(aValue, null, null, null);
    }

    /**
     * Creates a new {@link CellObject} around the given object with the given tooltip.
     *
     * @param aValue
     *            the object to encapsulate
     * @param aTooltip
     *            the tooltip to set
     */
    public CellObject(Object aValue, String aTooltip) {
        this(aValue, aTooltip, null, null);
    }

    /**
     * Creates a new {@link CellObject} around the given object with the given tooltip.
     *
     * @param aValue
     *            the object to encapsulate
     * @param aTooltip
     *            the tooltip to set
     * @param aStyle
     *            the style to set
     */
    public CellObject(Object aValue, String aTooltip, String aStyle) {
        this(aValue, aTooltip, aStyle, null);
    }

    /**
     * Creates a new {@link CellObject} around the given object with the given tooltip.
     *
     * @param aValue
     *            the object to encapsulate
     * @param aTooltip
     *            the tooltip to set
     * @param aStyle
     *            the style to set
     * @param aImage
     *            the image to set
     */
	public CellObject(Object aValue, String aTooltip, String aStyle, ThemeImage aImage) {
        this.value = aValue;
        this.tooltip = aTooltip;
        this.style = aStyle;
        this.image = aImage;
    }

    /**
     * Creates a {@link CellObject} with the values of the given {@link CellObject}.
     *
     * @param aCellObject
     *        the {@link CellObject} to copy the values from
     */
    public CellObject(CellObject aCellObject) {
        this(aCellObject.value, aCellObject.tooltip, aCellObject.style, aCellObject.image);
        this.disableLink = aCellObject.disableLink;
        this.disableImage = aCellObject.disableImage;
        this.disableTooltip = aCellObject.disableTooltip;
    }



    /** The wrapped value. */
    public Object getValue() {
        return value;
    }

    /** Setter for {@link #getValue()}. */
    public void setValue(Object aValue) {
        value = aValue;
    }

    /** The tooltip for the value. */
    public String getTooltip() {
        return tooltip;
    }

    /** Setter for {@link #getTooltip()}. */
    public void setTooltip(String aTooltip) {
        tooltip = aTooltip;
    }

	/** The CSS class for the value. */
	public String getCssClass() {
		return cssClass;
	}

	/** Setter for {@link #getCssClass()}. */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

    /** The style for the value. */
    public String getStyle() {
        return style;
    }

    /** Setter for {@link #getStyle()}. */
    public void setStyle(String aStyle) {
        style = aStyle;
    }

	/** The image to display for the value. */
	public ThemeImage getImage() {
        return image;
    }

	/** Setter for {@link #getImage()}. */
	public void setImage(ThemeImage aImage) {
        image = aImage;
    }

    /** Whether the link display of the wrapped value is disabled. */
    public boolean isLinkDisabled() {
        return disableLink;
    }

    /** Setter for {@link #isLinkDisabled()}. */
    public void setDisableLink(boolean disableLink) {
        this.disableLink = disableLink;
    }

    /** Whether the image display of the wrapped value is disabled. */
    public boolean isImageDisabled() {
        return disableImage;
    }

    /** Setter for {@link #isImageDisabled()}. */
    public void setDisableImage(boolean disableImage) {
        this.disableImage = disableImage;
    }

    /** Whether the tooltip display of the wrapped value is disabled. */
    public boolean isTooltipDisabled() {
        return disableTooltip;
    }

    /** Setter for {@link #isTooltipDisabled()}. */
    public void setDisableTooltip(boolean disableTooltip) {
        this.disableTooltip = disableTooltip;
    }



    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellObject:\n");
        sb.append("Value: ").append(getValue()).append('\n');
        sb.append("Tooltip: ").append(getTooltip()).append('\n');
		sb.append("CssClass: ").append(getCssClass()).append('\n');
        sb.append("Style: ").append(getStyle()).append('\n');
        sb.append("Image: ").append(getImage()).append('\n');
        sb.append("disableLink: ").append(isLinkDisabled()).append('\n');
        sb.append("disableImage: ").append(isImageDisabled()).append('\n');
        sb.append("disableTooltip: ").append(isTooltipDisabled()).append('\n');
        return sb.toString();
    }

    @Override
	public int compareTo(Object aObject) {
        return ComparableComparator.INSTANCE.compare(this.value, ((CellObject)aObject).value);
    }

}
