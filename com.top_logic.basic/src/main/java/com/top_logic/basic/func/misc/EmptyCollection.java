/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IFunction0;

/**
 * {@link IFunction0} returning always an empty {@link Collection}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class EmptyCollection extends Function0<Collection<?>> {

	@Override
	public Collection<?> apply() {
		return Collections.emptySet();
	}

}
