/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * {@link FormMember}, whereby a opener of a popup menu containing {@link CommandModel}s can be
 * displayed.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class PopupMenuField extends AbstractButtonField implements PopupMenuModel {

	public PopupMenuField(String formMemberName) {
		super(formMemberName);
	}

	@Override
	public Object visit(FormMemberVisitor visitor, Object arg) {
		return visitor.visitPopupMenuField(this, arg);
	}

}
