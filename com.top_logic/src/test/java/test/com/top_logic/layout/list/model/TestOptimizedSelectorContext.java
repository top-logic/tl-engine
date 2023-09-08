/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import junit.framework.Test;
import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.OptimizedSelectorContext;
import com.top_logic.layout.form.selection.SelectorContext;
import com.top_logic.layout.provider.DefaultLabelProvider;

/**
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class TestOptimizedSelectorContext extends AbstractSelectorContextTest {

	@Override
	protected SelectorContext createFormContext(SelectField targetSelectField) {
		return new OptimizedSelectorContext(targetSelectField, DefaultLabelProvider.INSTANCE, OPTIONS_PER_PAGE, 
		                                    Command.DO_NOTHING, false);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestOptimizedSelectorContext.class);
	}

}
