/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.tableForm;

import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface RowFormGroupGenerator {
    public void       addFormFields(ControlComponent aLC, FormGroup aFormGroup, Object aRowObject);
    public String     getRowGroupName(Object aRowObject);
    public void       handleResult(ControlComponent aComponent, FormGroup aFormGroup, FormField aTableAttribute, Object aModel);

	ControlProvider getControlProvider();
}
