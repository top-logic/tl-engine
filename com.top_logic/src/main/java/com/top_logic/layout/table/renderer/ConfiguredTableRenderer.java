/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;
import com.top_logic.layout.table.control.access.RowDisplay;
import com.top_logic.layout.table.control.access.RowRef;
import com.top_logic.layout.template.ConfiguredRenderer;

/**
 * {@link ITableRenderer} that can be parameterized with templates for the body and a row.
 * 
 * @see ConfiguredTableRenderer.Config#getTemplate()
 * @see ConfiguredTableRenderer.Config#getRowTemplate()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredTableRenderer extends ConfiguredRenderer<TableControl, ConfiguredTableRenderer.Config<?>>
		implements ITableRenderer {

	/**
	 * Configuration options for {@link ConfiguredTableRenderer}.
	 */
	public interface Config<I extends ConfiguredTableRenderer> extends ConfiguredRenderer.Config<I> {

		/**
		 * The template for rendering the {@link AbstractButtonControl}.
		 */
		TemplateExpression getRowTemplate();

	}

	/**
	 * Creates a {@link ConfiguredTableRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredTableRenderer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Creates a client-side ID for a row element.
	 *
	 * @param table
	 *        The {@link TableControl} being displayed.
	 * @param rowObject
	 *        The application object of the displayed row.
	 * @return A HTML ID to be used e.g. for the <code>tr</code> element.
	 * 
	 * @see #getRow(String)
	 */
	public String getRowId(TableControl table, Object rowObject) {
		return getRowId(table, table.getViewModel().getRowOfObject(rowObject));
	}

	private String getRowId(TableControl table, int rowNum) {
		return table.getID() + "." + rowNum;
	}

	@Override
	public int getRow(String rowId) {
		return Integer.parseInt(rowId.substring(rowId.indexOf('.') + 1));
	}

	@Override
	public void updateRows(TableControl view, List<UpdateRequest> updateRequests, UpdateQueue actions) {
		RowDisplay rowDisplay = new RowDisplay();
		for (UpdateRequest update : updateRequests) {
			int startRow = update.getFirstAffectedRow();
			int stopRow = update.getLastAffectedRow();
			String startId = getRowId(view, startRow);
			String stopId = getRowId(view, stopRow);
			actions.add(new RangeReplacement(startId, stopId, (context, out) -> {
				for (int rowNum = startRow; rowNum <= stopRow; rowNum++) {
					Object rowObject = view.getViewModel().getRowObject(rowNum);
					rowDisplay.init(view, rowObject).write(context, out);
				}
			}));
		}
	}

	/**
	 * Renders a single row.
	 */
	public void renderRow(DisplayContext context, TagWriter out, RowRef rowView) throws IOException {
		render(context, out, getConfig().getRowTemplate(), rowView);
	}

}
