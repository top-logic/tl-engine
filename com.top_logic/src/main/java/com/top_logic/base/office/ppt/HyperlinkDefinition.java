/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import com.top_logic.basic.StringServices;


/**
 * Definition of an hyper link for replacing in PowerPoint exports.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class HyperlinkDefinition {

    private String bookmark;

	private Integer slideNo;
    private String label;


    /**
     * Creates a new instance of this class.
     *
     * @param bookmark
     *        The target bookmark name for the hyper link.
     */
    public HyperlinkDefinition(String bookmark) {
        this(bookmark, null);
    }

    /**
     * Creates a new instance of this class.
     *
     * @param label
     *        The display label of the hyper link.
     * @param bookmark
     *        The target bookmark name for the hyper link.
     */
    public HyperlinkDefinition(String bookmark, String label) {
		if (StringServices.isEmpty(bookmark)) {
			throw new IllegalArgumentException("bookmark must not be null or empty");
		}
        this.bookmark = bookmark;
        this.label = label;
    }

    /**
     * Creates a new instance of this class.
     *
     * @param slideNumber
     *        The target slide for the hyper link.
     */
    public HyperlinkDefinition(int slideNumber) {
		this(slideNumber, null);
    }

    /**
     * Creates a new instance of this class.
     *
     * @param label
     *        The display label of the hyper link.
     * @param slideNumber
     *        The target slide for the hyper link.
     */
    public HyperlinkDefinition(int slideNumber, String label) {
		this.slideNo = slideNumber;
		this.label = label;
    }


    /**
     * This method sets the display label of the hyper link.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method sets the target bookmark name for the hyper link.
     */
    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    /**
     * This method sets the target slide for the hyper link.
     */
    public void setSlideNumber(int slideNumber) {
		setSlideNumber(Integer.valueOf(slideNumber));
	}

	/**
	 * This method sets the target slide for the hyper link.
	 */
	public void setSlideNumber(Integer slideNumber) {
		this.slideNo = slideNumber;
    }

	/**
	 * the slide number. May be <code>null</code>.
	 */
	public Integer getSlideNumber() {
		return this.slideNo;
	}

    /**
     * This method returns the display label of the hyper link.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the target bookmark name for the hyper link.
     */
    public String getBookmark() {
        return bookmark;
    }

    /**
     * Gets the display name of this hyper link.
     */
    public String getDisplayName() {
        String linkLabel = getLabel();
		linkLabel = StringServices.isEmpty(linkLabel) ? getBookmark() : linkLabel;
		return StringServices.isEmpty(linkLabel) ? (getSlideNumber().toString()) : linkLabel;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
