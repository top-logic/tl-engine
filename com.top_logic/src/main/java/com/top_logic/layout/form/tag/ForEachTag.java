/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.IterationTag;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.util.ShadowedAttribute;

/**
 * Tag that allows iterating over the members of a {@link FormContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ForEachTag extends AbstractFormTag implements IterationTag {
	private String varName;
	private Iterator members;
    
    private Filter filter;
    private Comparator comparator;
	
	private final ShadowedAttribute shadow = new ShadowedAttribute(); 
	
	public void setMember(String varName) {
		assert this.varName == null;
		this.varName = varName;
	}
    
    public void setComparator(Comparator aComparator) {
        this.comparator = aComparator;
    }
    
    public void setFilter(Filter aFilter) {
        this.filter = aFilter;
    }

	@Override
	protected int startFormMember() throws IOException {
		this.members = getParentFormContainer().getMembers();
        
        if (this.filter != null) {
            Collection theRealMembers;
            if (this.comparator != null) {
                theRealMembers = new TreeSet(this.comparator);
            } else {
                theRealMembers = new ArrayList();
            }
            for (; this.members.hasNext();) {
                Object theElement = this.members.next();
                if (this.filter.accept(theElement)) {
                    theRealMembers.add(theElement);
                }
            }
            this.members = theRealMembers.iterator();
        } else if (this.comparator != null) {
            Collection theRealMembers = new TreeSet(this.comparator);
            for (; this.members.hasNext();) {
                Object theElement = this.members.next();
                theRealMembers.add(theElement);
            }
            this.members = theRealMembers.iterator();
        }

		if (this.members.hasNext()) {
			shadow.backupShadowedAttribute(pageContext, varName);

			updateState();
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doAfterBody() throws JspException {
		if (members.hasNext()) {
			updateState();
			return EVAL_BODY_AGAIN;
		} else {
			pageContext.removeAttribute(varName);
			return EVAL_PAGE;
		}
	}

	private void updateState() {
		FormMember currentMember = (FormMember) members.next();
		pageContext.setAttribute(varName, currentMember.getName());
	}

	@Override
	protected int endFormMember() throws IOException {
		shadow.restoreShadowedAttribute(pageContext, varName);
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		super.teardown();
		this.shadow.reset();
		this.varName    = null;
		this.members    = null;
        this.filter     = null;
        this.comparator = null;
	}
	
}
