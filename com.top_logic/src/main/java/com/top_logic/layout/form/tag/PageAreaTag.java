/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.layout.View;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Tag} rendering a page with styled header area (including title and subtitle).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PageAreaTag extends AbstractTag implements BodyTag {

	private PageRenderer _renderer;

	private BodyContent _bodyContent;

	private boolean _hasTitle;

	private boolean _hasSubTitle;

	private boolean _hasIconBar;

	private View _titleView;

	private View _subTitleView;

	private View _iconBarView;

	private View _bodyView;

	/**
	 * The {@link PageRenderer} for writing the page structure (default provided by the
	 * {@link Theme}).
	 */
	@CalledFromJSP
	public void setPageRenderer(PageRenderer renderer) {
		_renderer = renderer;
	}

	/**
	 * The {@link View} to render into the title area.
	 */
	@CalledFromJSP
	public void setTitleView(View titleView) {
		_titleView = titleView;
	}

	/**
	 * The {@link View} to render into the subtitle area.
	 */
	@CalledFromJSP
	public void setSubTitleView(View subTitleView) {
		_subTitleView = subTitleView;
	}

	/**
	 * The {@link View} to render into the icon bar area.
	 */
	@CalledFromJSP
	public void setIconBarView(View iconBarView) {
		_iconBarView = iconBarView;
	}

	/**
	 * The {@link View} to render into the body area.
	 */
	@CalledFromJSP
	public void setBodyView(View contentView) {
		_bodyView = contentView;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		TagWriter out = getOut();
		_renderer.beginPage(out);
		_renderer.beginHeader(out);

		if (hasSynthesizedBody()) {
			// Note: In that case, the form:title tags can also not be evaluated. A page with a
			// synthesized body must specify all contents as tag attributes.
			return SKIP_BODY;
		} else {
			return EVAL_BODY_BUFFERED;
		}
	}

	@Override
	protected int endElement() throws IOException, JspException {
		installCorrectTagWriter();
		TagWriter out = getOut();

		completeBody();
		_renderer.endPage(out);

		flushBuffer();

		return EVAL_PAGE;
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		_bodyContent = bodyContent;
	}

	@Override
	public void doInitBody() throws JspException {
		installCorrectTagWriter();
	}

	@Override
	public int doAfterBody() throws JspException {
		return SKIP_BODY;
	}

	private void completeBody() throws IOException {
		// Now write directly to the underlying writer.
		completeIconBar();
		TagWriter out = getOut();

		_renderer.endHeader(out);

		boolean hasBodyContent = hasBodyContent();
		if (hasBodyContent || hasBodyView()) {
			_renderer.beginBody(out, getComponent().getName().qualifiedName(),
				getComponent().shallResetScrollPosition());
			if (hasBodyContent) {
				flushBody();
			} else {
				writeDefaultBodyContent();
			}
			_renderer.endBody(out);
		}
	}

	private boolean hasBodyContent() {
		if (_bodyContent != null) {
			return hasNonWhitespaceCharacter();
		} else {
			return false;
		}
	}

	private boolean hasNonWhitespaceCharacter() {
		String content = _bodyContent.getString();
		for (int i = 0; i < content.length(); i++) {
			if (!Character.isWhitespace(content.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	int beginTitle() throws IOException {
		assert !_hasTitle : "Title has already been rendered.";

		flushBuffer();
		dropContent();

		popBody();
		internalBeginTitle();
		int result;
		if (writeAttributeTitleContent()) {
			// Content specified by attributes takes precedence.
			result = SKIP_BODY;
		} else {
			result = EVAL_BODY_INCLUDE;
		}
		pushBody();

		return result;
	}

	private void internalBeginTitle() throws IOException {
		_hasTitle = true;
		_renderer.beginTitle(getOut());
	}

	void endTitle() throws IOException {
		flushBody();

		popBody();
		internalEndTitle();
		pushBody();
	}

	private void internalEndTitle() throws IOException {
		_renderer.endTitle(getOut());
	}

	int beginSubTitle() throws IOException {
		assert !_hasSubTitle : "Sub-title has already been rendered.";

		flushBuffer();
		dropContent();

		popBody();
		internalBeginSubTitle();
		pushBody();

		return EVAL_BODY_INCLUDE;
	}

	private void internalBeginSubTitle() throws IOException {
		completeTitle();
		_hasSubTitle = true;
		_renderer.beginSubTitle(getOut());
	}

	void endSubTitle() throws IOException {
		flushBody();

		popBody();
		internalEndSubTitle();
		pushBody();
	}

	private void internalEndSubTitle() throws IOException {
		_renderer.endSubTitle(getOut());
	}

	int beginIconBar() throws IOException {
		assert !_hasIconBar : "Icon bar has already been rendered.";

		flushBuffer();
		dropContent();

		popBody();
		internalBeginIconBar();
		pushBody();

		return EVAL_BODY_INCLUDE;
	}

	private void internalBeginIconBar() throws IOException {
		completeSubTitle();
		_hasIconBar = true;
		_renderer.beginIconBar(getOut());
	}

	void endIconBar() throws IOException {
		flushBody();

		popBody();
		internalEndIconBar();
		pushBody();
	}

	private void internalEndIconBar() throws IOException {
		_renderer.endIconBar(getOut());
	}

	private void completeTitle() throws IOException {
		if (_hasTitle) {
			return;
		}

		internalBeginTitle();
		internalWriteDefaultTitleContent();
		internalEndTitle();
	}

	private void completeSubTitle() throws IOException {
		if (_hasSubTitle) {
			return;
		}

		internalBeginSubTitle();
		internalWriteDefaultSubTitleContent();
		internalEndSubTitle();
	}

	private void completeIconBar() throws IOException {
		if (_hasIconBar) {
			return;
		}

		internalBeginIconBar();
		writeDefaultIconBarContent();
		internalEndIconBar();
	}

	private void flushBody() throws IOException {
		flushBuffer();
		_bodyContent.writeOut(_bodyContent.getEnclosingWriter());
		_bodyContent.clear();
	}

	private void popBody() throws IOException {
		flushBuffer();
		pageContext.popBody();
		installCorrectTagWriter();
	}

	private void pushBody() throws IOException {
		flushBuffer();
		pageContext.pushBody();
		installCorrectTagWriter();
	}

	private void dropContent() throws IOException {
		assert _bodyContent.getString().trim().length() == 0 : "Must not start structuring sub-tag after content has been produced.";
		_bodyContent.clear();
	}

	private void flushBuffer() throws IOException {
		out().flushBuffer();
	}

	private void internalWriteDefaultTitleContent() throws IOException {
		if (!writeAttributeTitleContent()) {
			writeDefaultTitleContent();
		}
	}

	/**
	 * Writes the title area content based on explicitly set attributes on this tag.
	 * 
	 * @return Whether some title content was rendered.
	 */
	protected boolean writeAttributeTitleContent() throws IOException {
		if (_titleView != null) {
			_titleView.write(getDisplayContext(), getOut());
			return true;
		}
		return false;
	}

	/**
	 * Writes default title area content if no content was specified at all.
	 */
	protected void writeDefaultTitleContent() throws IOException {
		// No title by default.
		writeText(HTMLConstants.NBSP);
	}

	private void internalWriteDefaultSubTitleContent() throws IOException {
		if (!writeAttributeSubTitleContent()) {
			writeDefaultSubTitleContent();
		}
	}

	/**
	 * Writes the subtitle area content based on explicitly set attributes on this tag.
	 * 
	 * @return Whether some subtitle content was rendered.
	 */
	protected boolean writeAttributeSubTitleContent() throws IOException {
		if (_subTitleView != null) {
			_subTitleView.write(getDisplayContext(), getOut());
			return true;
		}
		return false;
	}

	/**
	 * Writes default subtitle area content if no content was specified at all.
	 */
	protected void writeDefaultSubTitleContent() throws IOException {
		// No subtitle by default.
		writeText(HTMLConstants.NBSP);
	}

	/**
	 * Writes default subtitle area content if explicit form:iconbar subtag was specified.
	 */
	protected void writeDefaultIconBarContent() throws IOException {
		if (_iconBarView != null) {
			_iconBarView.write(getDisplayContext(), getOut());
		}
	}

	/**
	 * Whether rendering the tags body content should be prevented.
	 */
	protected boolean hasSynthesizedBody() {
		return hasBodyView();
	}

	/**
	 * Writes a replacement for the body content of this tag (which is regularly rendered into the
	 * page body).
	 */
	protected void writeDefaultBodyContent() throws IOException {
		if (hasBodyView()) {
			_bodyView.write(getDisplayContext(), getOut());
		}
	}

	private boolean hasBodyView() {
		return _bodyView != null;
	}

	@Override
	protected void setup() throws JspException {
		super.setup();

		if (_renderer == null) {
			_renderer = PageRenderer.getThemeInstance();
		}
	}

	@Override
	protected void teardown() {
		_renderer = null;
		_bodyContent = null;

		_hasTitle = false;
		_hasSubTitle = false;
		_hasIconBar = false;

		_titleView = null;
		_subTitleView = null;
		_iconBarView = null;
		_bodyView = null;

		super.teardown();
	}

}

