/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormContext;

/**
 * Abstract base class for all tags that contribute in rendering the contents of
 * a form.
 * 
 * <p>
 * This class provides convenient access to the {@link FormTag} (see
 * {@link #getFormTag()}), which renders the top-level view of a form.
 * </p>
 * 
 * <p>
 * With {@link #getParentFormContainer()}, this class provides convenient
 * access to the {@link FormContainer} model of the nearest ancestor
 * {@link AbstractFormMemberTag}.
 * </p>
 * 
 * <p>
 * This tag implementation enforces the following call sequence of its methods.
 * For each view that is rendered by this tag, the following methods are called
 * in exactly the described order:
 * </p>
 * 
 * <ol>
 * <li>{@link #setup()}</li>
 * <li>{@link #startFormMember()}</li>
 * <li>{@link #subscribePropertyEvents()}</li>
 * <li>Rendering of descendant views (only if the result of
 * {@link #startFormMember()} was {@link Tag#EVAL_BODY_INCLUDE}).</li>
 * <li>{@link #endFormMember()}</li>
 * <li>{@link #teardown()}</li>
 * </ol>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormTag extends AbstractTag {
	
	/**
	 * @see #getFormTag()
	 */
	private FormTag formTag;
	
	/**
	 * @see #getParentFormContainer()
	 */
	private FormContainer parentFormContainer;


	/**
	 * Returns the {@link FormTag}, which renders the top-level view of the
	 * current form.
	 */
	public FormTag getFormTag() {
		return this.formTag;
	}
	
	/**
	 * Convenient access to the top-level {@link FormContext} model of the
	 * current form.
	 */
	protected final FormContext getFormContext() {
		return formTag.getFormContext();
	}

	/**
	 * Return the parent form tag's model (which must be a {@link FormContainer}).
	 */
	public FormContainer getParentFormContainer() {
		return parentFormContainer;
	}

	@Override
	protected final int startElement() throws JspException {
		int result;
		try {
			result = this.startFormMember();
		} catch (IOException ex) {
			Logger.error("Error writing form tag.", ex, this);
			throw new JspException(ex);
		}
		
		return result;
	}
	
	@Override
	protected final int endElement() throws IOException, JspException {
		return this.endFormMember();
	}
	
	/**
	 * Hook for performing setup work immediately before rendering.
	 * 
	 * <p>
	 * Subclasses that overwrite this method must call the super implementation.
	 * </p>
	 * 
	 * @see #startFormMember() for the next step.
	 */
	@Override
	protected void setup() throws JspException {
		super.setup();
		
		this.formTag = FormTagUtil.findFormTag(this);

		if (formTag != null) {
			this.parentFormContainer = FormTagUtil.findParentFormContainer(this);
		}
	}

	@Override
	protected void teardown() {
		super.teardown();
		this.formTag = null;
		this.parentFormContainer = null;
	}
	
	/**
	 * Renders the "opening" part of the view. Called before descendant views
	 * are rendered.
	 * 
	 * @return Either {@link Tag#EVAL_BODY_INCLUDE} or {@link Tag#SKIP_BODY}.
	 */
	protected abstract int startFormMember() throws IOException, JspException;

	/**
	 * Renders the "closing" part of the view. Called after all descendant views
	 * have been rendered.
	 * 
	 * @return {@link Tag#EVAL_PAGE} or {@link Tag#SKIP_PAGE}.
	 */
	protected abstract int endFormMember() throws IOException, JspException;

	/**
	 * @deprecated Method no longer required, must no longer be called.
	 */
	@Deprecated
	protected final void subscribePropertyEvents() {
		throw new UnsupportedOperationException();
	}

}
