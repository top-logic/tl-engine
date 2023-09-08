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
     * Creates a new CellObject around the given object and no tooltip.
     *
     * @param aValue
     *            the object to encapsulate
     */
    public CellObject(Object aValue) {
        this(aValue, null, null, null);
    }

    /**
     * Creates a new CellObject around the given object with the given tooltip.
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
     * Creates a new CellObject around the given object with the given tooltip.
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
     * Creates a new CellObject around the given object with the given tooltip.
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
     * Creates a {@link CellObject} with the values of the given CellObject.
     *
     * @param aCellObject
     *        the CellObject to copy the values from
     */
    public CellObject(CellObject aCellObject) {
        this(aCellObject.value, aCellObject.tooltip, aCellObject.style, aCellObject.image);
        this.disableLink = aCellObject.disableLink;
        this.disableImage = aCellObject.disableImage;
        this.disableTooltip = aCellObject.disableTooltip;
    }



    public Object getValue() {
        return value;
    }

    public void setValue(Object aValue) {
        value = aValue;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String aTooltip) {
        tooltip = aTooltip;
    }

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

    public String getStyle() {
        return style;
    }

    public void setStyle(String aStyle) {
        style = aStyle;
    }

	public ThemeImage getImage() {
        return image;
    }

	public void setImage(ThemeImage aImage) {
        image = aImage;
    }

    public boolean isLinkDisabled() {
        return disableLink;
    }

    public void setDisableLink(boolean disableLink) {
        this.disableLink = disableLink;
    }

    public boolean isImageDisabled() {
        return disableImage;
    }

    public void setDisableImage(boolean disableImage) {
        this.disableImage = disableImage;
    }

    public boolean isTooltipDisabled() {
        return disableTooltip;
    }

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
