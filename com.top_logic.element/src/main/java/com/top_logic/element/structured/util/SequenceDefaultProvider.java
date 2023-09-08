/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.sql.SQLException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DefaultProvider} that creates a default value for an integer attribute from a sequence.
 * 
 * <p>
 * The created identifiers are guaranteed to be continuous. Those identifiers can only be created
 * during the commit of a transaction. Therefore, the attribute that uses this default provider must
 * be hidden in create context.
 * </p>
 * 
 * @see NumberHandler
 * @see NumberHandlerDefaultProvider For string-valued IDs with built-in formatting.
 */
@Label("Continuous ID")
@TargetType(TLTypeKind.INT)
public class SequenceDefaultProvider extends AbstractConfiguredInstance<SequenceDefaultProvider.Config>
		implements DefaultProvider {

	/**
	 * Configuration of a {@link SequenceDefaultProvider}.
	 */
	@DisplayOrder({
		Config.SEQUENCE_NAME,
		Config.DYNAMIC_SEQUENCE_NAME,
	})
	public static interface Config
			extends PolymorphicConfiguration<SequenceDefaultProvider> {

		/** Name of the configuration option configure {@link #getSequenceName()}. */
		String SEQUENCE_NAME = "sequence-name";

		/** @see #getDynamicSequenceName() */
		String DYNAMIC_SEQUENCE_NAME = "dynamic-sequence-name";

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

		/**
		 * {@link DynamicSequenceName Function} computing a dynamic sequence name based on the
		 * context, in which a new object is created.
		 * 
		 * <p>
		 * If given, a new sequence of numbers is started for each unique value computed from the
		 * creation context of the object.
		 * </p>
		 */
		@Name(DYNAMIC_SEQUENCE_NAME)
		PolymorphicConfiguration<? extends DynamicSequenceName> getDynamicSequenceName();

		/**
		 * @see #getDynamicSequenceName()
		 */
		void setDynamicSequenceName(PolymorphicConfiguration<? extends DynamicSequenceName> value);
	}

	private static final SequenceManager SEQUENCE_MANAGER = new RowLevelLockingSequenceManager();

	private static final int RETRY_COUNT = 5;

	private String _sequenceName;

	private DynamicSequenceName _dynamicSequence;

	/**
	 * Creates a new {@link SequenceDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SequenceDefaultProvider}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public SequenceDefaultProvider(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_sequenceName = config.getSequenceName();
		_dynamicSequence = context.getInstance(config.getDynamicSequenceName());
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		if (createForUI) {
			// Number must only be generated during object creation.
			return null;
		}
		try {
			StringBuilder sequenceNameBuilder =
				new StringBuilder(_sequenceName).append(SequenceIdGenerator.SEQUENCE_SUFFIX);

			DynamicSequenceName labelProvider = _dynamicSequence;
			if (labelProvider != null) {
				Object contextName = labelProvider.getSequenceName(context);
				SequenceIdGenerator.addNames(sequenceNameBuilder, contextName);
			}

			KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
			PooledConnection connection = ((CommitHandler) kb).createCommitContext().getConnection();
			long nextId = SEQUENCE_MANAGER.nextSequenceNumber(
				connection.getSQLDialect(), connection, RETRY_COUNT, sequenceNameBuilder.toString());

			return Long.valueOf(nextId);
		} catch (SQLException ex) {
			ResKey reason = I18NConstants.ERROR_CREATE_NUMBER_HANDLER_DEFAULT__NUMBER_HANDLER__ATTRIBUTE
				.fill(getConfig().getSequenceName(), attribute.getName());
			throw new TopLogicException(reason, ex);
		}
	}

}

