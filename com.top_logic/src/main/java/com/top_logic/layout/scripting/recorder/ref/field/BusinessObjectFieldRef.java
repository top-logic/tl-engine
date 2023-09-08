/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * {@link FieldRef} that uses the {@link FormMember#getStableIdSpecialCaseMarker() business object}
 * behind the field as its name.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public interface BusinessObjectFieldRef extends FieldRef {

	public ModelName getBusinessObject();

	public void setBusinessObject(ModelName businessObject);

}
