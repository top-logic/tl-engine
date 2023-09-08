/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.FieldProvider;

/**
 * The {@link ModifyClipboardControlProvider} creates a command field that displays a
 * {@link ModifyClipboardExecutable}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ModifyClipboardControlProvider extends AbstractFieldProvider {

	/** Sole instance of a {@link ModifyClipboardControlProvider} */
	public static final FieldProvider INSTANCE = new ModifyClipboardControlProvider();

	@Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
		if (!(aModel instanceof Wrapper)) {
			return null;
		}

		return this.createCommandField((Wrapper) aModel, aProperty);
	}

	private FormMember createCommandField(Wrapper aWrapper, String aProperty) {
		CommandField modifyClipboard = ModifyClipboardExecutable.createField(aWrapper, aProperty);
		modifyClipboard.setInheritDeactivation(false);
		return modifyClipboard;
	}
}
