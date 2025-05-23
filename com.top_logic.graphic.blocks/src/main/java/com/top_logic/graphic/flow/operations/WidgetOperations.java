/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Custom operations defined for a {@link Widget}.
 */
public interface WidgetOperations extends Drawable, MapLike {

	@Override
	Widget self();

}
