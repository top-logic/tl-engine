/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * TL-Script function {@code revertChanges(changes)}.
 *
 * <p>
 * Reverts every given {@code tl.changelog:ChangeSet} in descending revision order within the
 * <strong>caller's</strong> active transaction. The function does not open or commit a
 * transaction; commit handling must be done by the surrounding TL-Script code.
 * </p>
 *
 * <p>
 * Returns the aggregated list of problems ({@code ResKey}s) reported by the individual revert
 * operations. An empty list means the revert was clean.
 * </p>
 */
public class RevertChanges extends GenericMethod {

	/**
	 * Creates a {@link RevertChanges}.
	 */
	protected RevertChanges(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RevertChanges(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		// ResKey has no TLType representation; leave the result type open.
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input == null) {
			return Collections.emptyList();
		}

		List<ChangeSet> changes = new ArrayList<>();
		if (input instanceof Collection<?> coll) {
			for (Object element : coll) {
				if (element instanceof ChangeSet cs) {
					changes.add(cs);
				}
			}
		} else if (input instanceof ChangeSet cs) {
			changes.add(cs);
		}

		if (changes.isEmpty()) {
			return Collections.emptyList();
		}

		List<ResKey> problems = ChangeSetReverter.revertAll(changes);
		return problems;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link RevertChanges}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RevertChanges> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("changes")
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
		public RevertChanges build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new RevertChanges(getConfig().getName(), args);
		}
	}

}
