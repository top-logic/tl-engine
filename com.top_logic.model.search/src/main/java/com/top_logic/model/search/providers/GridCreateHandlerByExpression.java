/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.create.ConstantCreateTypeOptions;
import com.top_logic.element.layout.create.CreateTypeOptions;
import com.top_logic.element.layout.grid.AbstractGridCreateHandler;
import com.top_logic.element.meta.gui.FormObjectCreation;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AbstractGridCreateHandler} that can be parameterized with TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = { "grid", "treegrid" })
@Label("New row")
public class GridCreateHandlerByExpression extends AbstractGridCreateHandler {

	/**
	 * Configuration options for {@link GridCreateHandlerByExpression}.
	 */
	public interface Config extends AbstractGridCreateHandler.Config {

		/**
		 * @see #getCreateContext()
		 */
		String CREATE_CONTEXT = "createContext";

		/**
		 * @see #getInitOperation()
		 */
		String INIT_OPERATION = "initOperation";

		/** Computes the types that can be instantiated. */
		@NonNullable
		@ItemDefault
		@ImplementationClassDefault(ConstantCreateTypeOptions.class)
		PolymorphicConfiguration<CreateTypeOptions> getTypeOptions();

		/**
		 * Optional function linking the new object to its context.
		 * 
		 * <p>
		 * The function takes three arguments. The first argument is the container object which is
		 * calculated in {@link #getCreateContext()}, the second argument is the newly created
		 * object and the third argument is the {@link #getTarget() target model} of the command.
		 * </p>
		 * 
		 * <p>
		 * If the result of the function is either a {@link ResKey} or {@link String} it is
		 * interpreted as error description and the allocation fails with this message.
		 * </p>
		 * 
		 * <p>
		 * When this operation is invoked, the field values entered in the UI have already been
		 * applied to the newly created object.
		 * </p>
		 */
		@Name(INIT_OPERATION)
		Expr getInitOperation();

		/**
		 * Function providing the container of the object to create.
		 * 
		 * <p>
		 * Typically, the container of an object has a composition reference that contains the
		 * object. During creation, the new object is not yet part of any composition reference, but
		 * it pretends to be part of the computed container object. This is useful, if the default
		 * value computations of attributes refer to the <code>tContainer()</code> function of the
		 * object being created. When providing a create context to a new object, the
		 * <code>tContainer()</code> function of that object being created will return the provided
		 * create context.
		 * </p>
		 * 
		 * <p>
		 * The function takes the {@link #getTarget()} model of the command as argument.
		 * </p>
		 * 
		 * <p>
		 * If not given, the component's model is used by default.
		 * </p>
		 */
		@Name(CREATE_CONTEXT)
		@Label("Container")
		Expr getCreateContext();

		@Override
		@FormattedDefault("class.com.top_logic.model.search.providers.I18NConstants.NEW_LINE")
		ResKey getResourceKey();

		@Override
		@FormattedDefault("theme:com.top_logic.layout.table.control.Icons.ADD_ROW")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:com.top_logic.layout.table.control.Icons.ADD_ROW_DISABLED")
		ThemeImage getDisabledImage();

		@Override
		@FormattedDefault(CommandHandlerFactory.CREATE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();
	}

	private final CreateTypeOptions _typeOptions;

	private final QueryExecutor _initOperation;

	private final QueryExecutor _container;

	/**
	 * Creates a {@link GridCreateHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GridCreateHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_typeOptions = context.getInstance(config.getTypeOptions());
		Expr initOperation = config.getInitOperation();
		_initOperation = QueryExecutor.compileOptional(initOperation);
		_container = QueryExecutor.compileOptional(config.getCreateContext());
	}

	@Override
	protected CreateTypeOptions getTypeOptions() {
		return _typeOptions;
	}

	@Override
	protected CreateFunction getCreateOperation() {
		if (_initOperation == null) {
			return FormObjectCreation.INSTANCE;
		} else {
			return new CreateFunctionByExpression(_initOperation);
		}
	}

	@Override
	protected TLObject getContainer(Object model, Object selection) {
		return _container == null ? asTLObject(model)
			: SearchExpression.asTLObject(_container.getSearch(), _container.execute(model));
	}

	private static TLObject asTLObject(Object model) {
		return model instanceof TLObject ? (TLObject) model : null;
	}

	@Override
	protected ContextPosition getContextPosition(Object model, Object selection) {
		return ContextPosition.END;
	}
}
