/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Base interface for {@link ApplicationAction}s operating on {@link FormField}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractFormAction extends ApplicationAction {

	/**
	 * Description of the {@link FormField} to operate on.
	 */
	ModelName getField();

	/**
	 * @see #getField()
	 */
	void setField(ModelName value);

}
