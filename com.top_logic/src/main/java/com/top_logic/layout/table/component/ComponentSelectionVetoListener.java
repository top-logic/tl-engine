/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ComponentSelectionVetoListener} is a
 * {@link SelectionVetoListener} to avoid a {@link TableViewModel} from
 * switching to a new row. The {@link CheckScope checked scope} is determined by
 * the given {@link CheckScopeProvider} and the given {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ComponentSelectionVetoListener implements SelectionVetoListener {

	private final LayoutComponent component;
	private CheckScopeProvider checkScopeProvider;

	public ComponentSelectionVetoListener(CheckScopeProvider checkScopeProvider, LayoutComponent component) {
		this.component = component;
		this.checkScopeProvider = checkScopeProvider;
	}

	@Override
	public void checkVeto(SelectionModel selectionModel, Object newSelectedRow, SelectionType selectionType)
			throws VetoException {
		DirtyHandling.checkVeto(checkScopeProvider.getCheckScope(component));
	}

}
