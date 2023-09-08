/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.util.GenerateNumberException;
import com.top_logic.element.structured.util.NumberHandler;
import com.top_logic.element.structured.util.NumberHandlerFactory;
import com.top_logic.knowledge.wrap.MapValueProvider;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} to create a {@link TLObject} with a
 * {@link AbstractWrapperResolver#createNewWrapper(String) certain type name}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DefaultCreateAttributedCommandHandler extends AbstractCreateAttributedCommandHandler {

	/**
	 * Command handler may need information when not called by
	 * {@link DefaultCreateAttributedComponent}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends AbstractCreateAttributedCommandHandler.Config {

		/**
		 * Configuration name for the value of {@link Config#getUniqueID()}.
		 */
		String UNIQUE_ID_NAME = "unique-id";

		/**
		 * Configuration name for the value of {@link Config#getNumberHandler()}.
		 */
		String NUMBER_HANDLER_NAME = "number-handler";

		/**
		 * Configuration name for the value of {@link Config#getContextAttribute()}.
		 */
		String CONTEXT_ATTRIBUTE_NAME = "context-attribute";

		/**
		 * Optional name of the attribute of the newly created object where the the
		 * {@link #createNewObject(Map, Wrapper) context object} (model of the create component)
		 * should be stored.
		 * 
		 * <p>
		 * If not given, the context object of the create component is not used.
		 * </p>
		 */
		@Name(CONTEXT_ATTRIBUTE_NAME)
		String getContextAttribute();

		/**
		 * Optional name of an ID attribute in the newly created object, the generated ID from
		 * {@link #getNumberHandler()} should be stored in.
		 * 
		 * <p>
		 * This value is only used, if {@link #getNumberHandler()} is given, too.
		 * </p>
		 * 
		 * @see #getNumberHandler()
		 */
		@Name(UNIQUE_ID_NAME)
		String getUniqueID();

		/**
		 * Optional name of a {@link NumberHandler} to generate a unique ID for the newly created
		 * object.
		 * 
		 * <p>
		 * If given, {@link #getUniqueID()} must be configured, too.
		 * </p>
		 * 
		 * @see #getUniqueID()
		 */
		@Name(NUMBER_HANDLER_NAME)
		String getNumberHandler();
	}

	/** Unique ID of this command handler. */
	public static final String COMMAND_ID = "createAttributed";

	/**
	 * Creates a new {@link DefaultCreateAttributedCommandHandler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultCreateAttributedCommandHandler}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DefaultCreateAttributedCommandHandler(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			@SuppressWarnings("rawtypes") Map someValues) {

		Map<String, Object> theMap = extractValues(formContainer, (Wrapper) createContext);

		theMap.putAll(someValues);

		return createNewObjectFromMap(theMap, (Wrapper) createContext);
	}

	@Override
	public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {

		extendContextInformation(someValues, aModel);

		Wrapper newObject = createObject(someValues);

		extendUniqueIDInformation(someValues, newObject);

		return newObject;
	}

	private Wrapper createObject(Map<String, Object> someValues) {
		Object structureName = someValues.get(DefaultCreateAttributedComponent.STRUCT_NAME.getName());
		if (structureName instanceof String) {
			String[] theInfo = StringServices.split((String) structureName, '.');
			String moduleName = theInfo[0];
			String elementName = theInfo[1];
			AbstractWrapperResolver factory = (AbstractWrapperResolver) getFactory(moduleName);
			return factory.createNewWrapper(elementName);
		} else {
			TLClass targetType = getMetaElement(someValues);
			TLObject context = null;
			ValueProvider initialValues = new MapValueProvider();
			return (Wrapper) DynamicModelService.getInstance().createObject(targetType, context, initialValues);
		}
	}

	@Override
	protected Map<String, Object> extractValues(FormContainer aContainer, Wrapper anAttributed) {
		Map<String, Object> values = super.extractValues(aContainer, anAttributed);

		if (aContainer.hasMember(DefaultCreateAttributedComponent.ELEMENT_TYPE)) {
			FormField theField = aContainer.getField(DefaultCreateAttributedComponent.ELEMENT_TYPE);
			Object theStruct = theField.get(DefaultCreateAttributedComponent.STRUCT_NAME);

			if (theStruct != null) {
				values.put(DefaultCreateAttributedComponent.STRUCT_NAME.getName(), theStruct);
			}
		}

		return values;
	}

	/**
	 * Return the meta element of the new object to be created.
	 * 
	 * @param someValues
	 *        The values given by the command handler.
	 * @return The requested meta element, never <code>null</code>.
	 */
	protected TLClass getMetaElement(Map<?, ?> someValues) {
		return (TLClass) someValues.get(DefaultCreateAttributedComponent.ELEMENT_TYPE);
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * Extends the given map of values by a context information (when
	 * {@link Config#getContextAttribute()} is defined).
	 * 
	 * @param someValues
	 *        The map to be extended.
	 * @param model
	 *        The context model the create handler has been called from.
	 */
	protected void extendContextInformation(Map<String, Object> someValues, Wrapper model) {
		String contextAttribute = config().getContextAttribute();

		if (!contextAttribute.isEmpty() && model != null) {
			someValues.put(contextAttribute, model);
		}
	}

	/**
	 * Extend the given map of values by a context information (when {@link Config#getUniqueID()}
	 * and {@link Config#getNumberHandler()} are defined).
	 * 
	 * @param someValues
	 *        The map to be extended.
	 * @param newObject
	 *        The newly created object.
	 */
	protected void extendUniqueIDInformation(Map<String, Object> someValues, Wrapper newObject) {
		String uniqueIDAttr = config().getUniqueID();

		if (uniqueIDAttr.isEmpty()) {
			return;
		}

		String numberHandlerName = config().getNumberHandler();
		NumberHandler numberHandler = getNumberHandler(numberHandlerName);
		if (numberHandler == null) {
			return;
		}
		try {
			someValues.put(uniqueIDAttr, numberHandler.generateId(newObject));
		} catch (GenerateNumberException ex) {
			ResKey reason =
				I18NConstants.ERROR_CREATE_UNIQUEID__NUMBER_HANDLER__ATTRIBUTED.fill(numberHandlerName, newObject);
			throw new TopLogicException(reason, ex);
		}

	}

	private NumberHandler getNumberHandler(String numberHandler) {
		if (!StringServices.isEmpty(numberHandler)) {
			return NumberHandlerFactory.getInstance().getNumberHandler(numberHandler);
		}
		return null;
	}
}
