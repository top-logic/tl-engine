/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.InternalExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;

/**
 * {@link CompiledExpression} representing the access to an attribute in the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledAttributeAccess extends CompiledExpression {

	private final CompiledValue _base;

	private final MOAttribute _attribute;

	private final TLStructuredTypePart _part;

	/**
	 * Creates a new {@link CompiledAttributeAccess}.
	 */
	public CompiledAttributeAccess(TLStructuredTypePart part, MOAttribute attr, CompiledValue base) {
		super(attr.getMetaObject());
		_part = part;
		_attribute = attr;
		_base = base;
	}

	@Override
	public boolean needsEvalContext() {
		return _base.needsEvalContext();
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		Expression contextExpression = _base.buildExpression(context);
		switch (_part.getModelKind()) {
			case PROPERTY: {
				return InternalExpressionFactory.attributeTyped(contextExpression, _attribute);
			}
			case REFERENCE: {
				return InternalExpressionFactory.referenceTyped(contextExpression, (MOReference) _attribute);
			}
			default: {
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		if (item == null) {
			return null;
		}
		return item.tValue(_part);
	}

}
