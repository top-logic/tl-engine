/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.graphic.flow.data.Widget;

/**
 * Base class for implementing {@link RenderContext}s.
 */
public abstract class AbstractRenderContext implements RenderContext {

	private Map<Widget, Object> _infos = new HashMap<>();

	@Override
	public void setRenderInfo(Widget widget, Object info) {
		_infos.put(widget, info);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getRenderInfo(Widget widget) {
		return (T) _infos.get(widget);
	}
}
