/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.form.DefaultFormMemberNaming;

/**
 * {@link ModelName} for {@link FormMember}s that don't need any special treatment.
 * 
 * @see FormMemberNamingScheme
 * @deprecated Use {@link DefaultFormMemberNaming}
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface FormMemberName extends FormMemberBasedName {
	// Pure marker interface.
}