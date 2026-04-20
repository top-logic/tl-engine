/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * TL-Script function {@code redo([obj [, maxEntries [, includeSubtree]]])}.
 *
 * <p>
 * Re-applies the most recent revert within the considered change-log window by reverting the
 * revert ChangeSet itself. Parameters mirror {@link ChangeLog#eval(Object[], EvalContext)};
 * passing {@code null}/omitting {@code obj} operates on the global application change log.
 * </p>
 *
 * <p>
 * The function runs inside the caller's active transaction; it does not open or commit one. It
 * returns the aggregated problem list from the revert (empty when clean or when no revertable
 * undo was found).
 * </p>
 */
public class Redo extends GenericMethod {

	/**
	 * Creates a {@link Redo}.
	 */
	protected Redo(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Redo(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject obj = arguments[0] == null ? null : asTLObject(arguments[0]);
		int maxEntries = arguments.length > 1 && arguments[1] != null ? asInt(arguments[1]) : 0;
		boolean includeSubtree = arguments.length > 2 && arguments[2] != null ? asBoolean(arguments[2]) : true;

		return ChangeSetReverter.redoLast(obj, maxEntries, includeSubtree);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link Redo}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Redo> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("object")
			.optional("maxEntries")
			.optional("includeSubtree")
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
		public Redo build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Redo(getConfig().getName(), args);
		}
	}

}
