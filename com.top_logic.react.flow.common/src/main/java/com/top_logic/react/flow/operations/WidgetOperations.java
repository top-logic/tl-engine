/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.model.Drawable;
import com.top_logic.react.flow.data.Widget;

/**
 * Custom operations defined for a {@link Widget}.
 */
public interface WidgetOperations extends Drawable, MapLike {

	@Override
	Widget self();

}
