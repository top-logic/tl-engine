/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.unit;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * This {@link CommandHandler} deletes a {@link Unit} instance if permissible.
 * It contains the corresponding {@link ExecutabilityRule} as an inner class.
 * 
 * @author     <a href="mailto:TEH@top-logic.com">Tobias Ehrler</a>
 */
public class DeleteUnitCommandHandler extends AbstractDeleteCommandHandler {

	/** ID of this handler. */
    public static final String COMMAND_ID = "deleteUnit";

    public DeleteUnitCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
	
	@Override
	protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Unit)) {
			throw failCannotDelete();
		}

		Unit theUnit = (Unit) model;
		theUnit.tDelete();
	}

}
