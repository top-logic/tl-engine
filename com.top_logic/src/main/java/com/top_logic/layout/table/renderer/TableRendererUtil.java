/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.control.TableControl;

/**
 * The class {@link TableRendererUtil} provides util methods for {@link TableRenderer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableRendererUtil {

	/**
	 * This method returns a style attribute for the displayed row.
	 * 
	 * @param tableRenderer
	 *            the {@link TableRenderer} to be asked.
	 * @param view
	 *            the {@link TableControl} used to get the correct style value
	 * @param displayedRow
	 *            the number of the row to display
	 * @return the desired style value. may be <code>null</code>.
	 */
	static String getRowStyle(TableRenderer<?> tableRenderer, TableControl view, int displayedRow) {
		String result = null;
		Map<Integer, String> rowTRStyles = tableRenderer.getRowTRStyles();
		if (rowTRStyles != null) {
			String configuredStyle = rowTRStyles.get(view.getViewModel().getApplicationModelRow(displayedRow));
			result = StringServices.checkOnNullAndTrim(configuredStyle);
		}
		if (StringServices.isEmpty(result)) {
			result = StringServices.checkOnNullAndTrim(view.getViewModel().getTableConfiguration().getRowStyle());
		}
		if (StringServices.isEmpty(result)) {
			return null;
		} else {
			return result;
		}
	}

	/**
	 * Instantiates the {@link TableRenderer} from the given table renderer configuration.
	 */
	public static ITableRenderer getInstance(InstantiationContext context,
			PolymorphicConfiguration<? extends ITableRenderer> config) {
		if (config == null) {
			return DefaultTableRenderer.newInstance();
		}

		return context.getInstance(config);
	}

}
