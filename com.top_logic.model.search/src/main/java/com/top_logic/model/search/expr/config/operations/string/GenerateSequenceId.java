/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} that generates the next number in a sequence based on a sequence identifier
 * and optional context. The sequence identifier is combined with the context (if provided) to
 * create a unique sequence key. Each sequence maintains its own counter. If no sequence exists for
 * the given identifier and context combination, a new sequence will be automatically created
 * starting from 0.
 * 
 * 
 * Example usage: - generateSequenceId("invoice", null) -> uses sequence "invoice_SequenceId" -
 * generateSequenceId("invoice", "dept_A") -> uses sequence "invoice_dept_A_SequenceId"
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan H�sing</a>
 */
public class GenerateSequenceId extends GenericMethod {

	/**
	 * Technical suffix for the sequence actually used in sequence table to ensure that no clash is
	 * produced with internal sequences.
	 */
	public static final String SEQUENCE_SUFFIX = "_SequenceId";

	private static final SequenceManager SEQUENCE_MANAGER = new RowLevelLockingSequenceManager();

	/**
	 * Technical suffix for the sequence actually used in sequence table to ensure that no clash is
	 * produced with internal sequences.
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

		StringBuilder sequenceIdentifierBuilder = new StringBuilder(sequenceId);

		// if no sequence Identifier was entered do not continue with the evaluation
		if (sequenceId == null || sequenceId.equals("")) {
			return null;
		}

		// extract the context from the arguments
		Object contextArg = arguments[1];

		// add the optional Context to the sequence Identifier
		if (contextArg != null) {
			addNames(sequenceIdentifierBuilder, contextArg);
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
	 * Adds the given sequence name identifier to the given sequence name builder.
	 */
	public static void addNames(StringBuilder sequenceIdentifierBuilder, Object id) {
		if (id instanceof Collection<?>) {
			for (Object element : (Collection<?>) id) {
				addNames(sequenceIdentifierBuilder, element);
			}
		} else if (id instanceof TLObject) {
			addNameSeparator(sequenceIdentifierBuilder);
			sequenceIdentifierBuilder.append(((TLObject) id).tId().asString());
		} else if (id == null) {
			return;
		} else {
			addNameSeparator(sequenceIdentifierBuilder);
			sequenceIdentifierBuilder.append(MetaLabelProvider.INSTANCE.getLabel(id));
		}
	}

	private static void addNameSeparator(StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append('_');
		}
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