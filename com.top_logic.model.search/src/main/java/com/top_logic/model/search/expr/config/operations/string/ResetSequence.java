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
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * A {@link GenericMethod} implementation that resets a sequence to a specified value. The sequence
 * is identified the same way as in {@link GenerateSequenceId}: The context parameter can be any
 * value (same as {@link GenerateSequenceId}).
 * 
 * Standard value for reset is 0.
 * 
 * Returns true if reset was successful, false if it failed. Note: This method modifies the database
 * and should not be used in read-only queries.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ResetSequence extends GenericMethod {

	/**
	 * Technical suffix for the sequence actually used in sequence table to ensure that no clash is
	 * produced with internal sequences.
	 */
	public static final String SEQUENCE_SUFFIX = "_SequenceId";

	/**
	 * Creates a {@link ResetSequence} expression.
	 */
	protected ResetSequence(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ResetSequence(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
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

		// extract the newValue from the arguments
		long newValue = arguments[2] != null ? asLong(arguments[2])-1 : 0L;

		// reset the sequence
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		PooledConnection connection = ((CommitHandler) kb).createCommitContext().getConnection();

		try {
			return RowLevelLockingSequenceManager.resetSequence(connection, sequenceIdentifierBuilder.toString(),
				newValue);
		} catch (SQLException ex) {
			return false;
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link ResetSequence} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ResetSequence> {

		/** Description of parameters for a {@link ResetSequence}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("sequence")
			.optional("context")
			.optional("newValue")
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
		public ResetSequence build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ResetSequence(getConfig().getName(), args);
		}

	}

}