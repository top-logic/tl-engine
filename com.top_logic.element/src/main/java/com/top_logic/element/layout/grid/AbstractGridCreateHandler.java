/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.layout.create.CreateTypeOptions;
import com.top_logic.element.meta.schema.ElementSchema;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.PositionStrategy;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.SelfCheckProvider;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.SimpleSelectDialog;
import com.top_logic.layout.provider.label.TLTypeLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * Base class for {@link CommandHandler}s starting a create operation in a {@link GridComponent}.
 * 
 * @see #getTypeOptions()
 * @see #getCreateOperation()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractGridCreateHandler extends PreconditionCommandHandler {

	/**
	 * Configuration options for {@link AbstractGridCreateHandler}.
	 */
	public interface Config extends PreconditionCommandHandler.Config {

		/**
		 * Whether the user can choose from concrete sub-types of the create type.
		 * 
		 * <p>
		 * The value of this option is only relevant for a concrete types. If the type is abstract,
		 * a type chooser is always shown.
		 * </p>
		 */
		@Name("typeChooser")
		boolean getTypeChooser();

		@Override
		@ImplementationClassDefault(SelfCheckProvider.class)
		@ItemDefault
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
	}

	/**
	 * Creates a {@link AbstractGridCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractGridCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		GridComponent grid = (GridComponent) component;

		List<TLClass> possibleTypes = getTypeOptions().getPossibleTypes(model);
		if (possibleTypes.isEmpty()) {
			return new Failure(I18NConstants.NO_NEW_OBJECT_FOR_THIS_CONTEXT__CONTEXT.fill(model));
		}
		if (possibleTypes.size() == 1) {
			return successSingleOption(grid, model, possibleTypes.get(0));
		}
		TLClass defaultType = getTypeOptions().getDefaultType(model, possibleTypes);
		if (openTypeChooser()) {
			return successMultipleOptions(grid, model, arguments, possibleTypes, defaultType);
		}
		if (defaultType != null) {
			return successSingleOption(grid, model, defaultType);
		}
		return successMultipleOptions(grid, model, arguments, possibleTypes, defaultType);
	}

	/**
	 * Legacy hook to select the container for the creation.
	 * 
	 * @param model
	 *        The model of the grid. May be <code>null</code>.
	 * @param selection
	 *        The object currently selected in the grid. May be <code>null</code>.
	 */
	protected TLObject getContainer(Object model, Object selection) {
		return (TLObject) model;
	}

	/**
	 * Whether to open a sub-type chooser, even if
	 * {@link CreateTypeOptions#getDefaultType(Object, List) the default type} is concrete.
	 */
	protected boolean openTypeChooser() {
		return config().getTypeChooser();
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * Computes which types the user can create, based on the context.
	 * <p>
	 * The context is the {@link #getTargetModel(LayoutComponent, Map) target model} of this
	 * command.
	 * </p>
	 */
	protected abstract CreateTypeOptions getTypeOptions();

	/**
	 * The operation that finally performs the object creation.
	 */
	protected abstract CreateFunction getCreateOperation();

	private Success successSingleOption(GridComponent grid, Object model, TLClass concreteType) {
		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				// Actual operation in commit.
			}

			@Override
			protected HandlerResult doCommit(DisplayContext context) {
				TLObject container = getContainer(model, grid.getSelected());
				return startCreation(grid, model, concreteType, container);
			}
		};
	}

	private Success successMultipleOptions(GridComponent grid, Object model, Map<String, Object> arguments,
			Collection<TLClass> concreteTypes, TLClass defaultOption) {
		assert !concreteTypes.isEmpty() : "Concretes types must not be empty";
		return new Success() {

			private static final String CONCRETE_TYPE_SELECTION = "concreteType";

			@Override
			protected void doExecute(DisplayContext context) {
				// Actual operation in commit.
			}

			@Override
			protected HandlerResult doCommit(DisplayContext context) {
				TLClass selectedConcreteType = (TLClass) arguments.get(CONCRETE_TYPE_SELECTION);
				if (selectedConcreteType == null) {
					HandlerResult suspended = HandlerResult.suspended();
					openSelectDialog(context, suspended, concreteTypes);
					return suspended;
				} else {
					TLObject container = getContainer(model, grid.getSelected());
					return startCreation(grid, model, selectedConcreteType, container);
				}
			}

			private void openSelectDialog(DisplayContext aContext, HandlerResult suspended,
					Collection<TLClass> concreteSpecializations) {
				new SimpleSelectDialog<>(I18NConstants.SELECT_CONCRETE_TYPE_DIALOG, concreteSpecializations)
					.setDefaultOption(defaultOption)
					.setLabels(new TLTypeLabelProvider())
					.setSelectionHandler((context, selection) -> suspended.resume(context,
						Collections.singletonMap(CONCRETE_TYPE_SELECTION, selection)))
					.open(aContext);
			}

		};
	}

	HandlerResult startCreation(GridComponent grid, Object model, TLClass createType, TLObject container) {
		HandlerResult checkResult = checkFormContext(grid.getFormContext());

		if (!checkResult.isSuccess()) {
			return checkResult;
		}
		ContextPosition position = getContextPosition(model, grid.getSelected());
		grid.startCreation(getElementType(createType), createType, getCreateOperation(), position, container, model);
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult checkFormContext(FormContext context) {
		if (!context.checkAll()) {
			HandlerResult error = new HandlerResult();

			AbstractApplyCommandHandler.fillHandlerResultWithErrors(context, error);
			return error;
		} else {
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Select the position where the new row should appear.
	 * 
	 * @param model
	 *        The model of the grid. May be <code>null</code>.
	 * @param selection
	 *        The object currently selected in the grid. May be <code>null</code>.
	 * 
	 * @return The context position to place the empty row.
	 */
	protected ContextPosition getContextPosition(Object model, Object selection) {
		if (selection != null) {
			return ContextPosition.position(PositionStrategy.AFTER, selection);
		} else {
			return ContextPosition.START;
		}
	}

	private String getElementType(TLType type) {
		return ElementSchema.getElementType(type.getModule().getName(), type.getName());
	}
}
