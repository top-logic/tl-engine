/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Based on a given {@link ControlRepresentable}, it provides the table's internal
 * {@link TableData}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ControlBasedTableDataProvider implements TableDataProvider {
	
	private ControlRepresentable controlProvider;
	
	/**
	 * Create a new {@link ControlBasedTableDataProvider}
	 */
	public ControlBasedTableDataProvider(ControlRepresentable controlProvider) {
		this.controlProvider = controlProvider;
	}
	
	@Override
	public TableData getTableData() {
		Control renderingControl = controlProvider.getRenderingControl();
		if (renderingControl instanceof TableControl) {
			return ((TableControl) renderingControl).getTableData();
		} else {
			Object name;
			if (controlProvider instanceof LayoutComponent) {
				name = ((LayoutComponent) controlProvider).getName();
			} else {
				name = controlProvider.getClass().getName();
			}
			throw new IllegalStateException(
				"Component '" + name + "' does not provide necessary '" + TableControl.class.getSimpleName() + "'.");
		}
	}
}
