/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ErrorDisplay;
import com.top_logic.layout.form.control.IconErrorRenderer;
import com.top_logic.layout.form.control.ListErrorRenderer;
import com.top_logic.layout.form.model.FormContext;

/**
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ErrorsTag extends AbstractControlTag {

	private String names;
	private boolean icon = true;
	
	private LinkedHashSet fields;
	
	public void setNames(String names) {
		this.names = names;
	}
	
	public void setIcon(boolean value) {
		this.icon = value;
	}
	
	@Override
	protected void setup() throws JspException {
		super.setup();
		
		if (this.fields == null) {
			if (this.names == null) {
				throw new IllegalArgumentException(
					"Either field names or field references must be given.");
			}
			
			FormContainer parentFormContainer = FormTagUtil.findParentFormContainer(this);
			
			// Initialize from the 
			this.fields = new LinkedHashSet();
			for (StringTokenizer tokens = new StringTokenizer(names); tokens.hasMoreTokens(); ) {
				String name = tokens.nextToken();
				
				FormMember member = FormContext.getMemberByRelativeName(parentFormContainer, name);
				if (member instanceof FormContainer) {
					for (Iterator it = ((FormContainer) member).getDescendantFields(); it.hasNext(); ) {
						fields.add(it.next());
					}
				} else {
					fields.add(member);
				}
			}
		} else {
			if (names != null) {
				throw new IllegalArgumentException(
					"Either field names or field references must be given.");
			}
			
			// Fields are already initialized.
		}
	}
	
	@Override
	protected void teardown() {
		this.fields = null;
		this.names = null;
		this.icon = true;
		
		super.teardown();
	}
	
	@Override
	protected Control createControl() {
		ErrorDisplay display = new ErrorDisplay(fields, icon ? IconErrorRenderer.INSTANCE : ListErrorRenderer.INSTANCE);
		return display;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		ControlTagUtil.writeControl(this, pageContext, getControl());
		return SKIP_BODY;
	}
	
	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}

}
