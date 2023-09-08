/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.literal;

import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;

/**
 * {@link LiteralPrimitiveValue} describing a literal integer value.
 * <p>
 * It uses {@link Long} instead of {@link Integer} to cover as many integer number types as
 * possible.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(LiteralPrimitiveValue.Display.class)
public interface LiteralIntegerValue extends LiteralPrimitiveValue {

	@LongDefault(0)
	@Override
	Long getCompareValue();

}
