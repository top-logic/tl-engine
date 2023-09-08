/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Common base class for {@link Tag} implementations referencing component configured views.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractViewTag extends AbstractTagBase {

	private String _name;

	/**
	 * The name of the configured component view to display.
	 * 
	 * @see LayoutComponent#getConfiguredViews()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * The configured view referenced by this tag.
	 */
	protected HTMLFragment getView() {
		LayoutComponent component = MainLayout.getComponent(pageContext);
		if (component != null) {
			return component.getConfiguredViews().get(_name);
		} else {
			return null;
		}
	}

	/**
	 * Whether the given {@link HTMLFragment} is visible (including the special case of a
	 * {@link View} implementation).
	 */
	protected final boolean isVisible(HTMLFragment view) {
		return Fragments.isVisible(view);
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		_name = null;
		super.teardown();
	}
}
