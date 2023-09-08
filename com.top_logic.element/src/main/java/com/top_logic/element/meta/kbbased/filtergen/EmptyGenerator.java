/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collections;
import java.util.List;

import com.top_logic.element.meta.form.EditContext;

/**
 * Use in case you are unable to create the range of allowed Objects statically.
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class EmptyGenerator extends ListGeneratorAdaptor {

	@Override
	public List<?> generateList(EditContext editContext) {
		return Collections.EMPTY_LIST;
	}

}
