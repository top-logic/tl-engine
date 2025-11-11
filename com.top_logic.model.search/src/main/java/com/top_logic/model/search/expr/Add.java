/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * {@link GenericMethod} to add a value to a collection-valued property.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Add extends GenericMethod {

	/**
	 * Creates a {@link Add}.
	 */
	protected Add(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Add(getName(), arguments);
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
		int oldSize = oldValue.size();

		int index;
		Collection<?> insertion;
		Object secondArg = arguments[2];
		if (arguments.length < 4) {
			index = oldSize;
			insertion = asCollection(secondArg);
		} else {
			index = asInt(secondArg);
			insertion = asCollection(arguments[3]);
		}

		int insertLength = insertion.size();
		if (insertLength > 0) {
			Collection<?> effectiveInsertion = insertion;

			if (!part.isBag()) {
				// Only filter duplicates if the reference does not allow duplicates
				Set<Object> existingElements = new HashSet<>(oldValue);

				// Filter out duplicates from insertion collection
				List<Object> filtered = null;
				for (Object item : insertion) {
					// returns true only if item was not already present
					if (existingElements.add(item)) {
						if (filtered == null) {
							filtered = new ArrayList<>();
						}
						filtered.add(item);
					}
				}

				// Only proceed if there are elements to add
				if (filtered == null || filtered.isEmpty()) {
					return null;
				}

				effectiveInsertion = filtered;
				insertLength = filtered.size();
			}

			// Build the new list with the elements to insert
			List<Object> newValue = new ArrayList<>(oldSize + insertLength);
			newValue.addAll(oldValue.subList(0, index));
			newValue.addAll(effectiveInsertion);
			newValue.addAll(oldValue.subList(index, oldSize));

			obj.tUpdate(part, newValue);
		}

		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link Add}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Add> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Add build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 3, 4);
			return new Add(getConfig().getName(), args);
		}

	}
}
