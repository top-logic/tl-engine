/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} that grants (or revokes) access on all visible
 * {@link BooleanField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetAccessCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration of the {@link SetAccessCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Whether the access should be granted or revoked.
		 */
		boolean getAccess();
	}

	/**
	 * Creates a new {@link SetAccessCommandHandler}.
	 */
	public SetAccessCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> aSomeArguments) {
		boolean access = ((Config) getConfig()).getAccess();
		EditSecurityProfileComponent component = (EditSecurityProfileComponent) aComponent;
		TableViewModel viewModel = component.getTableField().getViewModel();
		int rowCount = viewModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			for (String name : viewModel.getColumnNames()) {
				Object valueAt = viewModel.getValueAt(i, name);
				if (valueAt instanceof BooleanField) {
					((BooleanField) valueAt).setValue(access);
				}
			}
		}
		return DefaultHandlerResult.DEFAULT_RESULT;
	}

}