/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CompiledExpression} representing the context of an {@link Expression}.
 * 
 * @see ExpressionFactory#context()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledContext extends CompiledExpression {

	/**
	 * Creates a new {@link CompiledContext}.
	 */
	public CompiledContext(MetaObject type) {
		super(type);
	}

	@Override
	public Expression buildExpression(EvalContext context) {
		return context();
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return item;
	}

	@Override
	public boolean needsEvalContext() {
		return false;
	}

	/**
	 * The query root is the object whose object-level visibility is enforced on the result by the
	 * consumer ({@link com.top_logic.model.search.expr.SearchExpression#filterSecurity}). Therefore
	 * its accesses may be compiled, unless the accessed attribute has an attribute-level read
	 * restriction, which the object-level result filtering does not cover.
	 */
	@Override
	protected boolean readCheckBypassedByCompilation(TLStructuredTypePart part) {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			// Model security is not active, e.g. while the model service compiles storage-algorithm
			// expressions during startup (the security service is a model service extension point and
			// starts afterwards). Without the security service there are no attribute-level read
			// restrictions to honour, so the access may be compiled.
			return false;
		}
		return !ModelAccessRights.getInstance().getAllowedRoles(part, SimpleBoundCommandGroup.READ).isEmpty();
	}

}

