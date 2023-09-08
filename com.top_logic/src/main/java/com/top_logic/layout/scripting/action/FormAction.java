/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.List;

import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.layout.scripting.recorder.ref.misc.FieldValue;

/**
 * Contains the information necessary for every {@link FormAction}.
 * 
 * @deprecated Use {@link FormInput}, or {@link FormRawInput}.
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
@Deprecated
public interface FormAction extends ModelAction {

	@EntryTag("value")
	List<FieldValue> getFieldValues();
	void setFieldValues(List<? extends FieldValue> fieldValues);

}
