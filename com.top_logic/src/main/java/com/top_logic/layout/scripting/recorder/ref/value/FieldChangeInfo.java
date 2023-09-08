/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.decorator.CompareInfo;

/**
 * {@link FieldAspect} for {@link FormField}s that have a {@link CompareInfo} evaluating to
 * {@link CompareInfo#getChangeInfo() the field's change info}.
 * 
 * @see CellChangeInfo
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Deprecated
public interface FieldChangeInfo extends FieldAspect {

	// Nothing needed but the type itself and the properties of the supertypes.

}
