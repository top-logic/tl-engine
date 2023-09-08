/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.meta.gui.FormObjectCreation;
import com.top_logic.element.meta.schema.ElementSchema;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link CommandHandler} that creates new grid row object of the type given as the grid's model
 * (which is expected to be of type {@link TLClass}).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateInstanceHandler extends PreconditionCommandHandler {

	/**
	 * Creates a {@link CreateInstanceHandler} from configuration.
	 */
	public CreateInstanceHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model == null) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL);
		}
		
		if (!(model instanceof TLClass)) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_MODEL_NOT_SUPPORTED);
		}

		TLClass type = (TLClass) component.getModel();
		if (type.isAbstract()) {
			return new Failure(I18NConstants.ERROR_CANNOT_INSTANTIATE_ABSTRACT_TYPE);
		}
		
		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				GridComponent grid = (GridComponent) component;

				grid.startCreation(
					ElementSchema.getElementType(type.getModule().getName(), type.getName()), type,
					createHandler(type), ContextPosition.END, null, model);
			}
		};
	}

	final CreateFunction createHandler(TLClass type) {
		return FormObjectCreation.INSTANCE;
	}
}
