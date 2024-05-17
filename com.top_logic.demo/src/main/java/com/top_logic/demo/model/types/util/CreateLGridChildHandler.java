/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.model.plain.DemoPlainA;
import com.top_logic.demo.model.plain.DemoPlainFactory;
import com.top_logic.demo.model.types.DemoTypesL;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.meta.gui.FormObjectCreation;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} that creates {@link DemoPlainA} child objects of {@link DemoTypesL} nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateLGridChildHandler extends AbstractCommandHandler {

	/**
	 * {@link CreateFunction} associating linking created {@link DemoPlainA}s with their context.
	 */
	public static final class CreatePlainA extends FormObjectCreation {

		/**
		 * Singleton {@link CreatePlainA} instance.
		 */
		@SuppressWarnings("hiding")
		public static final CreatePlainA INSTANCE = new CreatePlainA();

		@Override
		protected void initContainer(TLObject container, TLObject newObject, Object createContext) {
			super.initContainer(container, newObject, createContext);

			DemoPlainA child = (DemoPlainA) newObject;
			DemoTypesL parent = (DemoTypesL) container;
			parent.getPlainChildrenModifiable().add(child);
		}
	}

	private static final ExecutabilityRule EXECUTABILITY = new ExecutabilityRule() {
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			GridComponent grid = (GridComponent) aComponent;
			Object item = grid.getSelectedSingletonOrNull();
			if (item instanceof DemoTypesL) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}
	};

	/**
	 * Creates a new {@link CreateLGridChildHandler}.
	 */
	public CreateLGridChildHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		GridComponent grid = (GridComponent) aComponent;
		DemoTypesL context = (DemoTypesL) grid.getSelectedSingletonOrNull();
		
		TLClass plainAType = DemoPlainFactory.getAType();
		grid.startCreation(plainAType.getName(), plainAType, CreatePlainA.INSTANCE, ContextPosition.END, context,
			model);

		return HandlerResult.DEFAULT_RESULT;
	}
	
	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(EXECUTABILITY, super.intrinsicExecutability());
	}

}
