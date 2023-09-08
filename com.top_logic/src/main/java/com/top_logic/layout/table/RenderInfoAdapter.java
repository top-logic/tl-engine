/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.control.TableControl;

/**
 * Adapter for {@link TableRenderer.RenderInfo}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RenderInfoAdapter implements TableRenderer.RenderInfo {

	@Override
	public TableControl getView() {
		return impl().getView();
	}

	@Override
	public TableViewModel getModel() {
		return impl().getModel();
	}

	/**
	 * Return the {@link TableRenderer.RenderInfo} to delegate to.
	 */
	protected abstract TableRenderer.RenderInfo impl();

}

