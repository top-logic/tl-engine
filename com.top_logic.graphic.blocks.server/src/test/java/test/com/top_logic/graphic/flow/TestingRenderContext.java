/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetrics;
import com.top_logic.graphic.blocks.svg.TextMetricsImpl;

/**
 * {@link RenderContext} for testing only.
 */
final class TestingRenderContext implements RenderContext {
	@Override
	public TextMetrics measure(String text) {
		return new TextMetricsImpl(text.length() * 12, 12, 10);
	}
}