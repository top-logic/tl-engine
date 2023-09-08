/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import com.top_logic.basic.Logger;

/**
 * Proxy for a tag, which is created dynamically.
 * 
 * The creation of the proxied tag may can be controlled by attributes set on
 * this tag (see {@link #createImplementation()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractProxyTag implements BodyTag, TryCatchFinally {

	Tag impl;
	
	/**
	 * Called from {@link #doStartTag()} and triggers the creation of the
	 * implementation tag (see {@link #createImplementation()}).
	 * 
	 * Subclasses may override this method but must call the super
	 * implementation.
	 */
	protected void setup() {
		this.impl = createImplementation();

		this.impl.setParent(parent);
		this.impl.setPageContext(pageContext);
	}

	/**
	 * Called immediately after the {#link {@link #doEndTag()}} call from the
	 * implementation tag returns.
	 * 
	 * Subclasses may override this method but must call the super
	 * implementation.
	 */
	protected void teardown() {
	    this.impl.release();
	    this.impl = null;
	}
	
	/**
	 * Creates the implementation tag, for which this tag is a proxy for. The
	 * creation is initiated just before the it is needed in
	 * {@link #doStartTag()}. All calls of the {@link Tag},
	 * {@link IterationTag}, {@link BodyTag}, and {@link TryCatchFinally}
	 * interfaces are forwarded to the implementation tag (if the implementation
	 * tag actually implements these interfaces).
	 * 
	 * All calls that occur on this tag before {@link #doStartTag()} are cached
	 * and forwarded to the implementation tag after it has been created. These
	 * calls are {@link #setParent(Tag)} and
	 * {@link #setPageContext(PageContext)}.
	 * 
	 * @return The new tag, for which this tag is a proxy for.
	 */
	protected abstract Tag createImplementation();

	
	/**
	 * Cached parent value. This value is passed to the implementation tag,
	 * after {@link #setup()} returns.
	 */
	Tag parent;

	/**
	 * Cached page context value. This value is passed to the implementation
	 * tag, after {@link #setup()} returns.
	 */
	PageContext pageContext;

	/** @see #createImplementation() */
	@Override
	public void setParent(Tag parent) {
		this.parent = parent; 
	}

	@Override
	public Tag getParent() {
		return parent;
	}
    
    /** @see #createImplementation() */
	public PageContext getPageContext() {
        return this.pageContext;
    }
    
    /** @see #createImplementation() */
    @Override
	public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

	/** @see #createImplementation() */
	@Override
	public int doStartTag() throws JspException {
		setup();
		return impl.doStartTag();
	}

	@Override
	public int doEndTag() throws JspException {
		int result = impl.doEndTag();
		teardown();
		return result;
	}

	@Override
	public void release() {
	    if (impl != null) {
	        Logger.warn("Failure in Lifecyle, tearDown was not called?", this);
	    }
	}

	@Override
	public int doAfterBody() throws JspException {
		if (impl instanceof IterationTag) {
			IterationTag bodyTagImpl = (IterationTag) impl;
			return bodyTagImpl.doAfterBody();
		}
		return SKIP_BODY;
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		if (impl instanceof BodyTag) {
			BodyTag bodyTagImpl = (BodyTag) impl;
			bodyTagImpl.setBodyContent(bodyContent);
		}
	}

	@Override
	public void doInitBody() throws JspException {
		if (impl instanceof BodyTag) {
			BodyTag bodyTagImpl = (BodyTag) impl;
			bodyTagImpl.doInitBody();
		}
	}

	@Override
	public void doCatch(Throwable ex) throws Throwable {
		if (impl instanceof TryCatchFinally) {
			TryCatchFinally tryCatchFinallyImpl = (TryCatchFinally) impl;
			tryCatchFinallyImpl.doCatch(ex);
		} else {
            throw ex;
		}
	}

	@Override
	public void doFinally() {
		if (impl instanceof TryCatchFinally) {
			TryCatchFinally tryCatchFinallyImpl = (TryCatchFinally) impl;
			tryCatchFinallyImpl.doFinally();
		}
	}
	
	protected Tag getImplTag() {
	    return this.impl;
	}
}
