/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.CollapsibleFormMember;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * @author <a href="mailto:cwo@top-logic.com">cwo</a>
 */
public class ExpandableStringField extends StringField implements CollapsibleFormMember {

	@Inspectable
	private boolean _collapsed = true;

	/**
	 * @see StringField#StringField(String, boolean, boolean, Constraint)
	 */
	protected ExpandableStringField(String aName, boolean aMandatory, boolean aImmutable, Constraint aConstraint) {
        super(aName, aMandatory, aImmutable, aConstraint);
    }

    @Override
	public Object visit(FormMemberVisitor v, Object arg) {
        return v.visitExpandableStringField(this, arg);
    }
    
    public void toggleExpandedState() {
		this.setCollapsed(!this._collapsed);
    }

	@Override
	public boolean isCollapsed() {
		return _collapsed;
	}

	@Override
	public void setCollapsed(boolean value) {
		boolean hasChanged = value != this._collapsed;

		this._collapsed = value;

		if (hasChanged) {
			this.firePropertyChanged(Collapsible.COLLAPSED_PROPERTY, self(), Boolean.valueOf(!value),
				Boolean.valueOf(value));
		}
	}

	@Override
	protected ExpandableStringField self() {
		return this;
	}
}
