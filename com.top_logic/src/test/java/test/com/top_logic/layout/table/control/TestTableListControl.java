/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.control;

import junit.framework.Test;

import test.com.top_logic.layout.table.TableTestStructure.TableControlProvider;

import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableListControl;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * The class {@link TestTableListControl} tests methods in {@link TableListControl}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestTableListControl extends TestTableControl {
	
	@Override
	protected TableControlProvider getTableControlProvider() {
		return new TableControlProvider() {

			@Override
			public TableControl createTableControl(TableData model) {
				ITableRenderer tableRenderer = DefaultTableRenderer.newInstance();
				RowObjectCreator rowObjectCreator = null;
				RowObjectRemover rowObjectRemover = null;
				boolean isSortable = false;
				TableControl tableControl =
					TableListControl.createTableListControl(model, tableRenderer,
						rowObjectCreator, rowObjectRemover, /* selectable */true, true, /* createButtons */
						isSortable);

				tableControl.setSelectable(true);
				return tableControl;
			}
		};
	}

	/**
	 * Test for #5749: Control creates potentially fires model events during rendering
	 */
	public void testNoUpdateCreationDuringRendering() {
		TableControl ctrl = tableTestStructure.tableControl;

		// ensure no selection is given
		ctrl.getTableData().getSelectionModel().clear();
		writeControl(ctrl);
		// will not fail also with old implementation as workaround for #5415 drops all updates
		// during rendering
		assertFalse("#5749: No updates must be produced during rendering", ctrl.hasUpdates());

	}

	public static Test suite() {
		return TestTableControl.suite(TestTableListControl.class);
	}
}

