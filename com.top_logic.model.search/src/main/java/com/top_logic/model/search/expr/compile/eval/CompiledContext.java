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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
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
	public Expression buildIsNull(EvalContext context) {
		return ExpressionFactory.literal(Boolean.FALSE);
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
	 * consumer ({@link com.top_logic.model.search.expr.SearchExpression#filterSecurity}), which keeps
	 * the <em>discoverable</em> objects. Compiling an access on the root into the database query
	 * therefore applies no read check at all. This is only equivalent to the interpreted evaluation if
	 * the result filter implies the read check, i.e. if no object of the accessed type can be
	 * discoverable without being readable. The access is compiled only if
	 * <ul>
	 * <li>the accessed attribute has no attribute-level read restriction,</li>
	 * <li>the attribute is not a reference (the interpreted access filters undiscoverable targets,
	 * which a comparison on the stored reference would not),</li>
	 * <li>for the declaring class and all its concrete specializations, the roles granted Discover
	 * are a subset of the roles granted Read, and</li>
	 * <li>none of these classes has an access parent (readability would depend on another
	 * object), and</li>
	 * <li>no singleton of these classes has rights of its own (they replace the class rights the
	 * subset check relies on).</li>
	 * </ul>
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
		ModelAccessRights rights = ModelAccessRights.getInstance();
		if (rights.hasGrant(part, SimpleBoundCommandGroup.READ)) {
			return true;
		}
		if (part instanceof TLReference) {
			return true;
		}
		if (!(part.getOwner() instanceof TLClass owner)) {
			return true;
		}
		for (TLClass type : TLModelUtil.getConcreteReflexiveTransitiveSpecializations(owner)) {
			if (rights.isWithoutSecurity(type)) {
				continue;
			}
			if (rights.getAccessParent(type) != null) {
				return true;
			}
			if (rights.hasSingletonRights(type)) {
				return true;
			}
			if (!rights.getAllowedRoles(type, SimpleBoundCommandGroup.READ)
				.containsAll(rights.getAllowedRoles(type, SimpleBoundCommandGroup.DISCOVER))) {
				return true;
			}
		}
		return false;
	}

}
