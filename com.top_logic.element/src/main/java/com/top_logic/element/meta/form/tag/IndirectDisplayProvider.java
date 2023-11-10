/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link DisplayProvider} creating the display by delegating to an external
 * {@link #getControlProvider(EditContext) control provider}.
 *
 * <p>
 * Note: This class can be used as base class for retro-fitting legacy "tag providers". In versions
 * before <i>TopLogic 7.5</i>, rendering form inputs was always redirected through a JSP tag (even
 * if the form was not rendered by a JSP). Those tags used to implement also the
 * {@link ControlProvider} interface, therefore, the tag created by a legacy tag provider can easily
 * be used as implementation of the {@link #getControlProvider(EditContext)} method.
 * </p>
 * 
 * @deprecated directly implement {@link DisplayProvider}
 */
@Deprecated
public abstract class IndirectDisplayProvider implements DisplayProvider {

	/**
	 * Creates a rendering tag for the given edit location.
	 */
	@Deprecated
	@Override
	public abstract ControlProvider getControlProvider(EditContext editContext);

	@Override
	public final Control createDisplay(EditContext editContext, FormMember member) {
		return getControlProvider(editContext).createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	@Override
	public final HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return getControlProvider(editContext).createFragment(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

}
