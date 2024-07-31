/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.util.css.CssUtil;

/**
 * {@link DefaultTableRenderer} that uses a special header layout.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class TileCockpitTableRenderer extends DefaultTableRenderer {

	/**
	 * Creates a new {@link TileCockpitTableRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param atts
	 *        the configuration object to be used for instantiation
	 */
	public TileCockpitTableRenderer(InstantiationContext context, Config atts) {
		super(context, atts);
	}

	@Override
	public String computeTHClass(Column column) {
		String thClass = TABLE_HEADER_CELL_CSS_CLASS;
		String cssClasses = addGroupThClasses(column, thClass);

		String bpeTHClass = "bpeTileCockpitTH";
		cssClasses = CssUtil.joinCssClasses(cssClasses, bpeTHClass);

		return cssClasses;
	}

	private static String addGroupThClasses(Column column, String cellClass) {
		return CssUtil.joinCssClasses(cellClass, column.getCssHeaderClasses());
	}

}
