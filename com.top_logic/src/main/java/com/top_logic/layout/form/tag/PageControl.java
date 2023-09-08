/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * {@link Control} rendering a page with title and content area.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PageControl extends AbstractConstantControl {

	private final PageRenderer _renderer;

	private HTMLFragment _titleContent = Fragments.empty();

	private HTMLFragment _subtitleContent = Fragments.empty();

	private HTMLFragment _iconBarContent = Fragments.empty();

	private HTMLFragment _bodyContent = Fragments.empty();

	/**
	 * Creates a {@link PageControl}.
	 * 
	 * @param renderer
	 *        See {@link #getRenderer()}.
	 */
	public PageControl(PageRenderer renderer) {
		_renderer = renderer;
	}

	/**
	 * The {@link PageRenderer} to use.
	 */
	public PageRenderer getRenderer() {
		return _renderer;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		_renderer.writePage(context, out, this);
	}

	/**
	 * The title content of the page.
	 */
	public HTMLFragment getTitleContent() {
		return _titleContent;
	}

	/**
	 * @see #getTitleContent()
	 */
	public void setTitleContent(HTMLFragment titleContent) {
		_titleContent = titleContent;

		requestRepaint();
	}

	/**
	 * The sub-title content of the page.
	 */
	public HTMLFragment getSubtitleContent() {
		return _subtitleContent;
	}

	/**
	 * @see #getSubtitleContent()
	 */
	public void setSubtitleContent(HTMLFragment subtitleContent) {
		_subtitleContent = subtitleContent;

		requestRepaint();
	}

	/**
	 * The icon bar content of the title.
	 */
	public HTMLFragment getIconBarContent() {
		return _iconBarContent;
	}

	/**
	 * @see #getIconBarContent()
	 */
	public void setIconBarContent(HTMLFragment iconBarContent) {
		_iconBarContent = iconBarContent;

		requestRepaint();
	}

	/**
	 * The body content of the page.
	 */
	public HTMLFragment getBodyContent() {
		return _bodyContent;
	}

	/**
	 * @see #getBodyContent()
	 */
	public void setBodyContent(HTMLFragment bodyContent) {
		_bodyContent = bodyContent;

		requestRepaint();
	}
}
