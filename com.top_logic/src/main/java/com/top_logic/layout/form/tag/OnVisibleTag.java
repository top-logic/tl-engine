/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * {@link Tag} for conditionally rendering parts of a form.
 * 
 * <p>
 * The visibility of the body of this view depends on the visibility status of
 * the referenced {@link FormMember}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OnVisibleTag extends CompositeControlTag {

	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	protected Control createControl() {
		final FormMember targetMember = 
			FormContext.getMemberByRelativeName(FormTagUtil.findParentFormContainer(this), name);

		AbstractCompositeControl<?> result = DefaultFormFieldControlProvider.createOnVisibleControl(targetMember);
		result.setRenderer(mkRenderer());
		return result;
	}

	private DefaultSimpleCompositeControlRenderer mkRenderer() {
		String localName = getLocalName();
		String cssClass = getCssClass();
		if (cssClass == null) {
			if (localName == null || SPAN.equals(localName)) {
				return DefaultSimpleCompositeControlRenderer.SPAN_INSTANCE;
			} else if (DIV.equals(localName)) {
				return DefaultSimpleCompositeControlRenderer.DIV_INSTANCE;
			} else {
				return new DefaultSimpleCompositeControlRenderer(localName);
			}
		} else {
			return DefaultSimpleCompositeControlRenderer.withCSSClass(localName, cssClass);
		}
	}

	
}
