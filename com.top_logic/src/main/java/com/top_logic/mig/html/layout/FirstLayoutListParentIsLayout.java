/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.func.Function1;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Dynamic {@link FieldMode} computation of {@link LayoutReference#getLayoutInfo()}.
 * 
 * Field is only active and visible if the first parent of type {@link LayoutList} is a
 * {@link Layout}.
 * 
 * In particular {@link LayoutInfo} inside {@link TabComponent}s are not visible.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class FirstLayoutListParentIsLayout extends Function1<FieldMode, ComponentName> {

	@Override
	public FieldMode apply(ComponentName name) {
		LayoutComponent component = MainLayout.getDefaultMainLayout().getComponentForLayoutKey(name.toString());

		if (component != null) {
			LayoutList firstLayoutList = LayoutUtils.findFirstParent(component.getParent(), LayoutList.class);

			if (firstLayoutList instanceof Layout) {
				return FieldMode.ACTIVE;
			}
		}

		return FieldMode.INVISIBLE;
	}

}
