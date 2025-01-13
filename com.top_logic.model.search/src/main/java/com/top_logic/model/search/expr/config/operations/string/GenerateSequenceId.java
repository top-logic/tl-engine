/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.element.structured.util.SequenceIdGenerator;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * A {@link GenericMethod} implementation that generates unique sequential numbers. The sequence is
 * identified by combining:
 * 
 * 1. A base sequence identifier (mandatory) 2. Optional context value(s) that create separate
 * sequences for each unique context
 * 
 * The context parameter can be any value: - String/primitive values - TLObjects (using their ID) -
 * Collections (combining all elements)
 * 
 * Each sequence maintains its own counter. If no sequence exists for the given identifier and
 * context combination, a new sequence will be automatically created starting from 0.
 * 
 * Note: This method modifies the database and should not be used in read-only queries.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class GenerateSequenceId extends GenericMethod {

	/**
	 * Technical suffix for the sequence actually used in sequence table to ensure that no clash is
	 * produced with internal sequences.
	 */
	public static final String SEQUENCE_SUFFIX = "_SequenceId";

	private static final SequenceManager SEQUENCE_MANAGER = new RowLevelLockingSequenceManager();

	/**
	 * Number of retry attempts used in case of non-fatal database failures. The operation will be
	 * retried this many times before failing permanently.
	 */
	public static final int RETRY_COUNT = 3;

	/**
	 * Creates a {@link GenerateSequenceId} expression.
	 */
	protected GenerateSequenceId(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new GenerateSequenceId(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		// extract the sequence Identifier from the arguments
		String sequenceId = asString(arguments[0]);

		// if no sequence Identifier was entered do not continue with the evaluation
		if (sequenceId == null || sequenceId.equals("")) {
			return null;
		}

		StringBuilder sequenceIdentifierBuilder = new StringBuilder(sequenceId);

		// extract the context from the arguments
		Object contextArg = arguments[1];

		// add the optional Context to the sequence Identifier
		if (contextArg != null) {
			SequenceIdGenerator.addNames(sequenceIdentifierBuilder, contextArg);
		}

		// append the technical suffix after all context has been added
		sequenceIdentifierBuilder.append(SEQUENCE_SUFFIX);

		// get the next number
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		PooledConnection connection = ((CommitHandler) kb).createCommitContext().getConnection();
		long nextId;
		try {
			nextId = SEQUENCE_MANAGER.nextSequenceNumber(
				connection.getSQLDialect(), connection, RETRY_COUNT, sequenceIdentifierBuilder.toString());
		} catch (SQLException ex) {
			return null;
		}

		return asDouble(nextId);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link GenerateSequenceId} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<GenerateSequenceId> {

		/** Description of parameters for a {@link GenerateSequenceId}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("sequence")
			.optional("context")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public GenerateSequenceId build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new GenerateSequenceId(getConfig().getName(), args);
		}

	}

}