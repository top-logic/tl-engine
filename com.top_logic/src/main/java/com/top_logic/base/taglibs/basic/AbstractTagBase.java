/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.tag.AbstractFormTag;
import com.top_logic.layout.form.tag.util.TagAttribute;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tag.JSPErrorUtil;

/**
 * Base class for all tags that render HTML through a {@link TagWriter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTagBase extends TagSupport {

	/**
	 * Internal field that is used to make sure that all subclasses, which overwrite
	 * {@link #setup()} and {@link #teardown()} call the respective super implementations.
	 */
	private boolean internalCallSequenceCheck;

	/** Writer to enforce generation of well-formed XHTML. */
	private TagWriter out;

	private int tagDepth;

	private boolean _startElementSucceeded;

	/**
	 * Convenient access to the context path of the application.
	 */
	protected String getContextPath() {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		return request.getContextPath();
	}

	protected DisplayContext getDisplayContext() {
		return DefaultDisplayContext.getDisplayContext(pageContext);
	}

	protected void setup() throws JspException {
		// Enforces to call super in overridden setup() methods.
		this.internalCallSequenceCheck = true;
	}

	/**
	 * This method sets a new {@link Writer} to the TagWriter associated with this tag. The new
	 * underlying {@link Writer} is given by the output writer of the pageContext.
	 */
	protected final void installCorrectTagWriter() {
		out.setOut(pageContext.getOut());
	}

	/**
	 * Hook for performing cleanup work immediately after rendering.
	 * 
	 * <p>
	 * Subclasses that overwrite this method must call the super implementation.
	 * </p>
	 * 
	 * @see TagAttribute#reset() for resetting the attribute states.
	 */
	protected void teardown() {
		this.out = null;

		// Enforces to call super in overridden setup() methods.
		this.internalCallSequenceCheck = true;
	}

	/**
	 * @see AbstractFormTag for an explanation of the call sequence that is enforced by this
	 *      implementation.
	 * 
	 * @return The value returned from {@link #startElement()}.
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	@Override
	public final int doStartTag() throws JspException {
		_startElementSucceeded = false;
		try {
			out = MainLayout.getTagWriter(pageContext);
			tagDepth = out.getDepth();
			internalCallSequenceCheck = false;
			setup();
			assert internalCallSequenceCheck : "setup() must call its super implementation.";
			int result = startElement();

			_startElementSucceeded = true;

			// Make sure to flush all buffers local to TagWriter to the underlying stream, since
			// native JSP code will directly write to the underlying stream. Without flushing
			// local
			// buffers, JSP output overtakes TagWriter output.
			out.flushBuffer();

			return result;
		} catch (Throwable throwable) {
			try {
				doCatch(throwable, tagDepth + 1);
			} catch (Throwable ex) {
				Logger.error("Failsafe handling of tag failed", ex, this);
			}
			return SKIP_BODY;
		}
	}

	/**
	 * Code executed at the beginning of this JSP element.
	 * 
	 * @return The value that is returned from {@link Tag#doStartTag()}.
	 */
	protected abstract int startElement() throws JspException, IOException;

	/**
	 * @see AbstractFormTag for an explanation of the call sequence that is enforced by this
	 *      implementation.
	 * 
	 * @return The value returned from {@link #endElement()}.
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	@Override
	public final int doEndTag() throws JspException {
		try {
			int returnValue;

			if (_startElementSucceeded) {
				returnValue = endElement();
			} else {
				// Otherwise, the setup crashed and startElement() has not been called/completed
				// successfully.
				out.endAll(tagDepth);
				returnValue = EVAL_PAGE;
			}

			out.flushBuffer();
			internalCallSequenceCheck = false;
			teardown();
			assert internalCallSequenceCheck : "teardown() must call its super implementation.";

			return returnValue;
		} catch (Throwable throwable) {
			try {
				doCatch(throwable, tagDepth);
			} catch (Throwable ex) {
				Logger.error("Failsafe handling of tag failed", ex, this);
			}
			return EVAL_PAGE;
		} finally {
			doFinally();
		}
	}

	/**
	 * Code executed at the end of this JSP element.
	 * 
	 * @return The value that is returned from {@link Tag#doEndTag()}.
	 */
	protected abstract int endElement() throws IOException, JspException;

	/**
	 * The {@link TagWriter} to write to.
	 */
	public TagWriter getOut() {
		return out;
	}

	/**
	 * Convenience shortcut for {@link TagWriter#beginBeginTag(String)} with <code>false</code> as
	 * second argument.
	 */
	protected void beginBeginTag(String elementName) throws IOException {
		out.beginBeginTag(elementName);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#beginScript()}.
	 */
	protected void beginScript() throws IOException {
		out.beginScript();
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeAttribute(String, CharSequence)}.
	 */
	protected void writeAttribute(String attributeName, String value) throws IOException {
		out.writeAttribute(attributeName, value);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeAttribute(String, char)}.
	 */
	protected void writeAttribute(String attributeName, char value) throws IOException {
		out.writeAttribute(attributeName, value);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeAttribute(String, boolean)}.
	 */
	protected void writeAttribute(String attributeName, boolean value) throws IOException {
		out.writeAttribute(attributeName, value);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeAttribute(String, int)}.
	 */
	protected void writeAttribute(String attributeName, int value) throws IOException {
		out.writeAttribute(attributeName, value);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#endBeginTag()}.
	 */
	protected void endBeginTag() throws IOException {
		out.endBeginTag();
	}

	/**
	 * Convenience shortcut for {@link TagWriter#endEmptyTag()}.
	 */
	protected void endEmptyTag() throws IOException {
		out.endEmptyTag();
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeText(CharSequence)}.
	 */
	protected void writeText(String text) throws IOException {
		out.writeText(text);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#append(CharSequence)}.
	 */
	protected void append(String text) throws IOException {
		out.append(text);
	}
	
	/**
	 * Convenience shortcut for {@link TagWriter#endTag(String)} with <code>false</code> as second
	 * argument.
	 */
	protected void endTag(String elementName) throws IOException {
		out.endTag(elementName);
	}

	/**
	 * Convenience shortcut for {@link TagWriter#endScript()}.
	 */
	protected void endScript() throws IOException {
		out.endScript();
	}

	/**
	 * Convenience shortcut for {@link TagWriter#writeContent(CharSequence)}.
	 */
	protected void writeContent(String content) throws IOException {
		out.writeContent(content);
	}

	/**
	 * Convenience shortcut for reporting errors.
	 * 
	 * @returns JspException Created from the given argument values.
	 */
	protected JspException reportError(String message, Exception ex) throws JspException {
		if (ex instanceof JspException) {
			return (JspException) ex;
		} else {
			return new JspException(message, ex);
		}
	}

	protected final TagWriter out() {
		return out;
	}

	private void doCatch(Throwable throwable, int closeTagDepth) throws Throwable {
		if (out != null) {
			out.endAll(closeTagDepth);
			JSPErrorUtil.produceErrorOutput(pageContext, getErrorMessage(), throwable, this);
		} else {
			Logger.error(getErrorMessage(), throwable, this);
		}
	}

	/**
	 * Message, that will be logged and shown at GUI, if an error during rendering occured.
	 */
	protected String getErrorMessage() {
		return "Error occured during rendering of tag '" + getClass() + "'.";
	}

	private void doFinally() {
		// Make sure to flush all buffers local to TagWriter to the underlying stream, since
		// native JSP code will directly write to the underlying stream. Without flushing
		// local
		// buffers, JSP output overtakes TagWriter output.
		try {
			if (out != null) {
				out.flushBuffer();
			}
		} catch (Throwable throwable) {
			Logger.error("Teardown of tag failed!", throwable, this);
		}
	}

}
