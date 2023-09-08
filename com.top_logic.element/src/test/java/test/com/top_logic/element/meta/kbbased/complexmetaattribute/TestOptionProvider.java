/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.complexmetaattribute;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.meta.ListOptionProvider;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.element.meta.form.EditContext;

/**
 * Test Implementation of an {@link OptionProvider}
 * 
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class TestOptionProvider implements ListOptionProvider {

	private List<TestBusinessObject> staticOptions;
    
    @Override
	public List<?> getOptionsList(EditContext editContext) {
        if (staticOptions == null) {
			staticOptions = new ArrayList<>(3);
			staticOptions.add(TestBusinessObject.SEPPL);
			staticOptions.add(TestBusinessObject.HUGO);
			staticOptions.add(TestBusinessObject.EGON);
        }
        return staticOptions;
    }

}

