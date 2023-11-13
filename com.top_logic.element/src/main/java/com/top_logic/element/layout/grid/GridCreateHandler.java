/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.layout.create.ConstantCreateTypeOptions;
import com.top_logic.element.layout.create.CreateTypeOptions;
import com.top_logic.element.meta.gui.DefaultCreateAttributedCommandHandler;
import com.top_logic.element.model.util.TypeReferenceConfig;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.Hide;

/**
 * Button to create a new {@link Wrapper} in the {@link GridComponent}.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GridCreateHandler extends AbstractGridCreateHandler {

	/**
	 * Configuration options for {@link GridCreateHandler}.
	 */
	public interface Config extends AbstractGridCreateHandler.Config, TypeReferenceConfig {

		/** Name of the configuration option to configure {@link Config#getCreateHandler()}. */
		String CREATE_HANDLER = "create-handler";

		/** Name of the configuration option to configure {@link Config#getCreateHandlerId()}. */
		String CREATE_HANDLER_ID = "create-handler-id";

		/**
		 * Configuration of the create handler used to create the persistent object from the
		 * transient object.
		 * 
		 * <p>
		 * Either {@link #getCreateHandler()} or {@link #getCreateHandlerId()} must be given.
		 * </p>
		 */
		@Name(CREATE_HANDLER)
		PolymorphicConfiguration<? extends AbstractCreateCommandHandler> getCreateHandler();

		/**
		 * Command ID of the create handler to be used.
		 * 
		 * <p>
		 * Either {@link #getCreateHandler()} or {@link #getCreateHandlerId()} must be given.
		 * </p>
		 */
		@StringDefault(DefaultCreateAttributedCommandHandler.COMMAND_ID)
		@Name(CREATE_HANDLER_ID)
		String getCreateHandlerId();

		/**
		 * Element types on which it is allowed to create a new object.
		 * 
		 * <p>
		 * If empty the model of the component is used as create context, otherwise the selection.
		 * </p>
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getParentTypes();

	}

	/** Unique ID of this command handler. */
	public static final String COMMAND_ID = "createAttributedGrid";

	private final TLClass _metaElement;

	private final AbstractCreateCommandHandler _createHander;

	private final CreateTypeOptions _typeOptions;

	/**
	 * Creates a new {@link GridCreateHandler}.
	 */
	public GridCreateHandler(InstantiationContext context, Config aConfig) throws ConfigurationException {
		super(context, aConfig);

		_metaElement = TypeReferenceConfig.Resolver.getMetaElement(aConfig);
		_createHander = createCreateHandler(context, aConfig);
		_typeOptions = initTypeOptions(_metaElement);
	}

	private static AbstractCreateCommandHandler createCreateHandler(InstantiationContext context, Config config) {
		AbstractCreateCommandHandler configuredHandler = context.getInstance(config.getCreateHandler());
		if (configuredHandler != null) {
			return configuredHandler;
		}
		String referencedID = config.getCreateHandlerId();
		if (referencedID.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Either ");
			errorMessage.append(Config.CREATE_HANDLER);
			errorMessage.append(" or ");
			errorMessage.append(Config.CREATE_HANDLER_ID);
			errorMessage.append(" in config ");
			errorMessage.append(config);
			errorMessage.append("(");
			errorMessage.append(config.location());
			errorMessage.append(") must be given.");
			context.error(errorMessage.toString());
		}

		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler(referencedID);
		if (handler == null) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Configured handler ");
			errorMessage.append(referencedID);
			errorMessage.append(" in config ");
			errorMessage.append(config);
			errorMessage.append("(");
			errorMessage.append(config.location());
			errorMessage.append(") does not exist.");
			context.error(errorMessage.toString());
		}
		return (AbstractCreateCommandHandler) handler;
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		Config createConfig = config();
		if (createConfig.getCreateHandler() == null && createConfig.getCreateHandlerId().isEmpty()) {
			return new Hide();
		}
		List<String> parentTypes = createConfig.getParentTypes();
		if (!parentTypes.isEmpty()) {
			Object selection = ((Selectable) component).getSelected();
			if (selection == null) {
				return new Failure(I18NConstants.NO_SELECTION);
			} else if (selection instanceof Wrapper) {
				TLStructuredType selectionType = ((Wrapper) selection).tType();
				if (!parentTypes.contains(TLModelUtil.qualifiedName(selectionType))) {
					return new Failure(I18NConstants.WRONG_SELECTION);
				}
			}
		}

		GridComponent grid = (GridComponent) component;
		Object createContext = getContainer(model, grid.getSelected());
		if (isChild() && (createContext == null)) {
			return new Failure(I18NConstants.NO_SELECTION);
		}

		return super.prepare(component, model, arguments);
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * Whether the type of the object to create is a "child type"
	 */
	protected boolean isChild() {
		return !config().getParentTypes().isEmpty();
	}

	@Override
	protected CreateFunction getCreateOperation() {
		return _createHander;
	}

	@Override
	protected ContextPosition getContextPosition(Object model, Object selection) {
		if (isChild()) {
			return ContextPosition.END;
		} else {
			return super.getContextPosition(model, selection);
		}
	}

	@Override
	protected TLObject getContainer(Object model, Object selection) {
		return isChild() ? (TLObject) selection : (TLObject) model;
	}

	@Override
	protected CreateTypeOptions getTypeOptions() {
		return _typeOptions;
	}

	/**
	 * Creates the {@link CreateTypeOptions} that computes which types the user can instantiate.
	 * <p>
	 * This is called at the end of the constructor. Therefore, if this is overridden in subclasses,
	 * it must not access any fields as they might not have been initialized.
	 * </p>
	 * 
	 * @param createType
	 *        The user can choose between this type and all its concrete subtypes, recursively.
	 *        Abstract types are filtered out.
	 */
	protected CreateTypeOptions initTypeOptions(TLClass createType) {
		@SuppressWarnings("unchecked")
		ConstantCreateTypeOptions.Config<ConstantCreateTypeOptions<?>> config =
			newConfigItem(ConstantCreateTypeOptions.Config.class);
		setProperty(config, TypeReferenceConfig.TYPE, TLModelPartRef.ref(createType));
		return TypedConfigUtil.createInstance(config);
	}

	/** @see Config#getType() */
	protected final TLClass getCreateType() {
		return _metaElement;
	}

}
