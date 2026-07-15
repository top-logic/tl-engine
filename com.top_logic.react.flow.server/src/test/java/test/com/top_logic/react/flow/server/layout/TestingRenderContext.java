/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.TextMetrics;
import com.top_logic.react.flow.svg.TextMetricsImpl;

/**
 * {@link RenderContext} for testing only.
 *
 * <p>
 * Reports deterministic text metrics (width proportional to the character count) independent of
 * font, size and weight, so that layout geometry in tests is reproducible.
 * </p>
 */
final class TestingRenderContext implements RenderContext {

	private static TextMetrics metrics(String text) {
		return new TextMetricsImpl(text.length() * 12, 12, 10);
	}

	@Override
	public TextMetrics measure(String text) {
		return metrics(text);
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, double fontSize) {
		return metrics(text);
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, double fontSize, String fontWeight) {
		return metrics(text);
	}
}
