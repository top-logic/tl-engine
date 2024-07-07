/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.svg;

/**
 * Context for rendering SVG.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RenderContext {

	/**
	 * Measures the given text.
	 * 
	 * @see TextMetrics
	 */
	TextMetrics measure(String text);

}
