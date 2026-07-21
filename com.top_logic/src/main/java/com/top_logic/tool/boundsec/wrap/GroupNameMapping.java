/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.tool.boundsec.wrap;

import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} for {@link Group}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GroupNameMapping implements OptionMapping {

	@Override
	public Object toSelection(Object option) {
		if (option instanceof Group group) {
			return group.getName();
		}
		return null;
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		return Group.getGroupByName((String) selection);
	}

}

