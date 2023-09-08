/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.applet.AppletContext;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Factory for {@link AppletContext}-dependent {@link Filter}s identifying {@link FormMember}s.
 * 
 * @see DefaultFormMemberNaming.Name#getPath()
 * @see GlobalFormMemberNaming.Name#getMatchers()
 * @see FieldAnalyzer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldMatcher {

	/**
	 * Build a {@link AppletContext}-local {@link Filter} that can identify (
	 * {@link Filter#accept(Object)}) a {@link FormMember}.
	 */
	Filter<? super FormMember> createFilter(ActionContext context);

}
