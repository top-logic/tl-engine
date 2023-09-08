/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.util.ResKey;
import com.top_logic.reporting.chart.gantt.model.GanttObject;


/**
 * Holds the generated graphic in a memory saving pseudo vector format.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class GraphicData {

	private GraphicsContextImpl _context = new GraphicsContextImpl();

	/** The width of the graphic. */
	private int _width;

	/** The header height of the graphic. */
	private int _headerHeight;

	/** The width of the description area of the graphic. */
	private int _treeWidth;

	/** The content height of the graphic. */
	private int _contentHeight;

	/** The footer height of the graphic. */
	private int _footerHeight;

	/** The clickable links of the graphic. */
	private Collection<GanttObject> _links = Collections.emptyList();

	/** The page count of this graphics. */
	private int _pageCount = 1;

	/** Message key if graphic could not get created. */
	private ResKey _messageKey;

	/** The number of nodes to be shown on one page. */
	private int _nodesPerPage;

	/**
	 * The graphic output parts to render to.
	 */
	public GraphicsContextImpl getContext() {
		return _context;
	}

	/**
	 * Getter for {@link #_width}.
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * Setter for {@link #_width}.
	 */
	public void setWidth(int width) {
		_width = width;
	}

	/** The width of the description area of the graphic. */
	public int getTreeWidth() {
		return _treeWidth;
	}

	/**
	 * @see #getTreeWidth()
	 */
	public void setTreeWidth(int width) {
		_treeWidth = width;
	}

	/**
	 * Getter for {@link #_nodesPerPage}.
	 */
	public int getNodesPerPage() {
		return _nodesPerPage;
	}

	/**
	 * Setter for {@link #_nodesPerPage}.
	 */
	public void setNodesPerPage(int nodesPerPage) {
		_nodesPerPage = nodesPerPage;
	}

	/**
	 * Getter for {@link #_headerHeight}.
	 */
	public int getHeaderHeight() {
		return _headerHeight;
	}

	/**
	 * Setter for {@link #_headerHeight}.
	 */
	public void setHeaderHeight(int headerHeight) {
		_headerHeight = headerHeight;
	}

	/**
	 * Getter for {@link #_contentHeight}.
	 */
	public int getContentHeight() {
		return _contentHeight;
	}

	/**
	 * Setter for {@link #_contentHeight}.
	 */
	public void setContentHeight(int contentHeight) {
		_contentHeight = contentHeight;
	}

	/**
	 * Getter for {@link #_footerHeight}.
	 */
	public int getFooterHeight() {
		return _footerHeight;
	}

	/**
	 * Setter for {@link #_footerHeight}.
	 */
	public void setFooterHeight(int footerHeight) {
		_footerHeight = footerHeight;
	}

	/**
	 * Getter for {@link #_links}.
	 */
	public Collection<GanttObject> getLinks() {
		return _links;
	}

	/**
	 * Setter for {@link #_links}.
	 */
	public void setLinks(Collection<GanttObject> links) {
		_links = links;
	}

	/**
	 * Checks whether this GraphicData has an (error) message.
	 */
	public boolean hasMessage() {
		return getMessageKey() != null;
	}

	/**
	 * Getter for {@link #_messageKey}.
	 */
	public ResKey getMessageKey() {
		return _messageKey;
	}

	/**
	 * Setter for {@link #_messageKey}.
	 */
	public void setMessageKey(ResKey messageKey) {
		_messageKey = messageKey;
	}

	/**
	 * Getter for {@link #_pageCount}.
	 */
	public int getPageCount() {
		return _pageCount;
	}

	/**
	 * Setter for {@link #_pageCount}.
	 */
	public void setPageCount(int pageCount) {
		_pageCount = pageCount;
	}

}
