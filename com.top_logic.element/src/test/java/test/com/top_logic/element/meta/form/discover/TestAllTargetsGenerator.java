/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.discover;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Java {@link Generator} offering all objects of the value type of the edited attribute, without
 * any access check of its own.
 */
public class TestAllTargetsGenerator implements Generator {

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		TLClass type = (TLClass) editContext.getValueType();
		return new DefaultListOptionModel<>(MetaElementUtil.getAllInstancesOf(type, TLObject.class));
	}

}
