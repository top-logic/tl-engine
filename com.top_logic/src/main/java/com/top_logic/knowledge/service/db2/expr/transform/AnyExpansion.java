/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.TypeSystemDependent;
import com.top_logic.knowledge.search.Union;

/**
 * Replaces all {@link AnyOf} by a {@link Union} of {@link AllOf} for all concrete sub types of the
 * declared type of the {@link AnyOf} expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AnyExpansion extends InlineExpressionTransformer<TypeSystem> {

	/**
	 * Singleton {@link AnyExpansion} instance.
	 */
	public static final AnyExpansion INSTANCE = new AnyExpansion();

	private AnyExpansion() {
		// Singleton constructor.
	}

	/**
	 * Replaces all {@link AnyOf} by {@link Union} of {@link AllOf}. It is expected that the
	 * {@link TypeSystemDependent} already have passed through type binding operations.
	 * 
	 * @param typeSystem
	 *        the system to decide which types are the concrete sub types of the type of the
	 *        {@link AnyOf}.
	 * @param expr
	 *        the expression to transform
	 * 
	 * @return the transformed expression
	 * 
	 * @see TypeSystemDependent#hasTypeBinding()
	 */
	public static SetExpression expandAny(TypeSystem typeSystem, SetExpression expr) {
		return expr.visitSetExpr(INSTANCE, typeSystem);
	}
	
	@Override
	public SetExpression visitAnyOf(AnyOf expr, TypeSystem arg) {
		List<? extends MetaObject> concreteSubtypes = arg.getConcreteSubtypes(expr.getDeclaredType());
		
		int typeCnt = concreteSubtypes.size();
		if (typeCnt == 0) {
			return none();
		} else {
			SetExpression result = allOfTyped((MOClass) concreteSubtypes.get(0));
			for (int n = 1; n < typeCnt; n++) {
				result = union(result, allOfTyped((MOClass) concreteSubtypes.get(n)));
			}
			return result;
		}
	}
	
	@Override
	public SetExpression visitSetLiteral(SetLiteral expr, TypeSystem arg) {
		Collection<? extends Object> values = expr.getValues();
		HashMap<MetaObject, List<Object>> partition = new HashMap<>();
		
		for (Object obj : values) {
			if (obj instanceof DataObject) {
				MetaObject concreteType = ((DataObject) obj).tTable();
				List<Object> monomorphicObjects = partition.get(concreteType);
				if (monomorphicObjects == null) {
					monomorphicObjects = new ArrayList<>();
					partition.put(concreteType, monomorphicObjects);
				}
				monomorphicObjects.add(obj);
			} else {
				return super.visitSetLiteral(expr, arg);
			}
		}
		
		Iterator<Entry<MetaObject, List<Object>>> it = partition.entrySet().iterator();
		SetExpression result = setLiteral(it.next().getValue());
		while (it.hasNext()) {
			result = union(result, setLiteral(it.next().getValue()));
		}
		
		return result;
	}
	
	@Override
	protected Expression processInstanceOf(InstanceOf expr, Expression context, TypeSystem arg) {
		if (false) {
			// Split into or(hasType(),...)
			MetaObject declaredType = expr.getDeclaredType();
			List<? extends MetaObject> subtypes = arg.getConcreteSubtypes(declaredType);
			Expression result = null;
			for (int n = 0, cnt = subtypes.size(); n < cnt; n++) {
				MetaObject concreteType = subtypes.get(n);
				Expression hasType = hasTypeTyped(concreteType);
				if (result == null) {
					result = hasType;
				} else {
					result = or(result, hasType);
				}
			}
			if (result == null) {
				return literal(false);
			}
			return result;
		} else {
			return super.processInstanceOf(expr, context, arg);
		}
	}
	
}
