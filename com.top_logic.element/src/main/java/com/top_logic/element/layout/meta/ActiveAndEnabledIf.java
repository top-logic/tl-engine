/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Decides the {@link FieldMode} depending on two conditions.
 * 
 * <p>
 * The fist condition must be true for showing fields (not immutable). The second condition must be
 * true for the fields being active (not disabled).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActiveAndEnabledIf extends Function2<FieldMode, Boolean, Boolean> {
	@Override
	public FieldMode apply(Boolean edit, Boolean enabled) {
		if (edit == null || enabled == null) {
			return FieldMode.ACTIVE;
		}
		if (!edit.booleanValue()) {
			return FieldMode.IMMUTABLE;
		}
		if (!enabled.booleanValue()) {
			return FieldMode.DISABLED;
		}
		return FieldMode.ACTIVE;
	}
}