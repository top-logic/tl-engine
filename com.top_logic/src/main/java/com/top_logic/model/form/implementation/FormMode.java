/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.TLVisibility;

/**
 * Mode where a form is displayed. Can be used to calculate the {@link TLVisibility} of the
 * {@link TLModelPart}s of a form.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public enum FormMode {
	/** In create mode, the form is not filled with an existing model. */
	CREATE,
	/** The form is filled with an existing model. */
	EDIT,
	/** Form is designed in a form editor. */
	DESIGN;
}
