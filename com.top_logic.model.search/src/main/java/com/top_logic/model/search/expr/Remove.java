/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * {@link GenericMethod} to remove a value from a collection-valued property.
 *
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class Remove extends GenericMethod {

	/**
	 * Creates a {@link Remove}.
	 */
	protected Remove(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Remove(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject obj = asTLObjectNonNull(arguments[0]);
		TLStructuredTypePart part = asTypePart(getArguments()[1], arguments[1]);

		List<?> oldValue = asList(obj.tValue(part));
		Collection<?> toRemove = asCollection(arguments[2]);

		if (!toRemove.isEmpty()) {
			Set<Object> removeSet = new HashSet<>(toRemove);
			List<Object> newValue = new ArrayList<>(oldValue.size());
			for (Object item : oldValue) {
				if (!removeSet.contains(item)) {
					newValue.add(item);
				}
			}
			obj.tUpdate(part, newValue);
		}

		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link Remove}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Remove> {

		/** Description of parameters for a {@link Remove}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("object")
			.mandatory("property")
			.mandatory("toRemove")
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
		public Remove build(Expr expr, SearchExpression[] args) {
			return new Remove(getConfig().getName(), args);
		}

	}
}