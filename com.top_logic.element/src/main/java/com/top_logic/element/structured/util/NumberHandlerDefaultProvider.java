/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DefaultProvider} that creates unique identifiers with some built-in formatting.
 * 
 * <p>
 * The created sequence number within the identifier is guaranteed to be continuous. Therefore,
 * those identifiers can only be created during the commit of a transaction. Therefore, the
 * attribute that uses this default provider must be hidden in create context.
 * </p>
 * 
 * @see NumberHandler The source of identifiers.
 * @see SequenceDefaultProvider Corresponding {@link DefaultProvider} for integer attributes.
 */
@Label("Generated ID")
@TargetType(TLTypeKind.STRING)
public class NumberHandlerDefaultProvider extends AbstractConfiguredInstance<NumberHandlerDefaultProvider.Config>
		implements DefaultProvider {

	/**
	 * Configuration of a {@link NumberHandlerDefaultProvider}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		Config.SEQUENCE_NAME,
		Config.PATTERN,
		Config.NUMBER_PATTERN,
		Config.DATE_PATTERN,
		Config.DYNAMIC_SEQUENCE_NAME,
	})
	public static interface Config
			extends PolymorphicConfiguration<NumberHandlerDefaultProvider>, ConfiguredNumberHandler.UIConfig {

		/** Name of the configuration option configure {@link #getSequenceName()}. */
		String SEQUENCE_NAME = "sequence-name";

		/**
		 * Identifier for the sequence of numbers.
		 * 
		 * <p>
		 * The same value in {@link #getSequenceName()} in the default specification of multiple
		 * attributes guarantee that the generated numbers for all those attributes are unique.
		 * Using different {@link #getSequenceName()}s result in the same number to be assigned to
		 * different attributes with such a default specification.
		 * </p>
		 * 
		 * @see NumberHandler.NumberHandlerConfig#getName()
		 */
		@Name(SEQUENCE_NAME)
		@Mandatory
		String getSequenceName();

	}

	private final NumberHandler _generator;

	/**
	 * Creates a new {@link NumberHandlerDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NumberHandlerDefaultProvider}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public NumberHandlerDefaultProvider(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		String sequenceName = config.getSequenceName();
		String idPattern = config.getPattern();
		String numberPattern = config.getNumberPattern();
		String datePattern = config.getDatePattern();
		DynamicSequenceName dynamicSequence = context.getInstance(config.getDynamicSequenceName());
		_generator =
			new SequenceIdGenerator(sequenceName, idPattern, numberPattern, datePattern, dynamicSequence, 3);
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		if (createForUI) {
			// Number must only be generated during object creation.
			return null;
		}
		try {
			return _generator.generateId(context);
		} catch (GenerateNumberException ex) {
			ResKey reason = I18NConstants.ERROR_CREATE_NUMBER_HANDLER_DEFAULT__NUMBER_HANDLER__ATTRIBUTE
				.fill(getConfig().getSequenceName(), attribute.getName());
			throw new TopLogicException(reason, ex);
		}
	}

}

