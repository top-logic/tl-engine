/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * The class {@link FormContainerTag} is an interface which can be implemented from
 * {@link FormMemberTag} if there {@link FormMember} is a {@link FormContainer}. That allows
 * descendants to access their member without using the qualified name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FormContainerTag extends FormMemberTag {

	/**
	 * This method must return the member of this {@link FormMemberTag} if it is an
	 * {@link FormContainer}.
	 */
	FormContainer getFormContainer();

}
