/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Graphics2D;

/**
 * Output areas for a pagable chart.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphicsContext {

	/**
	 * Header area.
	 */
	public interface HeaderContext {

		/**
		 * Output for the header.
		 */
		Graphics2D header();

	}

	/**
	 * Content area.
	 */
	public interface ContentContext {

		/**
		 * Output for the content.
		 */
		Graphics2D content();

	}

	/**
	 * Footer area.
	 */
	interface FooterContext {

		/**
		 * Output for the footer.
		 */
		Graphics2D footer();

	}

}
