/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.InternalExpressionFactory;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.QueryParameter;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.TypedExpression;
import com.top_logic.knowledge.search.TypedSetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;

/**
 * Resolves type names to types in {@link SetExpression}s by looking up types in
 * a {@link TypeSystem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeBinding extends DefaultDescendingQueryVisitor<Void, Void, Void, Void, Void, Map<String, MetaObject>> {
	
	private static final Void none = null;
	
	private final TypeSystem typeSystem;
	private final ExpressionCompileProtocol log;

	/**
	 * Applies a {@link TypeBinding} to the given expression.
	 * 
	 * @param typeSystem
	 *        The system to lookup types from.
	 * @param log
	 *        The {@link Protocol} to report errors to.
	 * @param expr
	 *        The expression to bind types in.
	 */
	public static void bindTypes(TypeSystem typeSystem, ExpressionCompileProtocol log, QueryPart expr) {
		expr.visitQuery(new TypeBinding(typeSystem, log), new HashMap<>());
	}

	private TypeBinding(TypeSystem typeSystem, ExpressionCompileProtocol log) {
		this.typeSystem = typeSystem;
		this.log = log;
	}

	@Override
	public Void visitParameterDeclaration(ParameterDeclaration expr, Map<String, MetaObject> arg) {
		try {
			expr.setDeclaredType(typeSystem.getType(expr.getTypeName()));
		} catch (UnknownTypeException ex) {
			error(expr, "Unknown type ''{0}''.", expr.getTypeName());
		}
		arg.put(expr.getName(), expr.getDeclaredType());
		return none;
	}
	
	@Override
	public Void visitAllOf(AllOf expr, Map<String, MetaObject> arg) {
		Void result = super.visitAllOf(expr, arg);
		
		if (expr.hasTypeBinding()) {
			MetaObject declaredType = expr.getDeclaredType();
			if (MetaObjectUtils.isAbstract(declaredType)) {
				error(expr, "Concrete item lookup of abstract type ''{0}''.", expr.getTypeName());
			}
		}
		
		return result;
	}
	
	@Override
	public Void visitParameter(Parameter expr, Map<String, MetaObject> arg) {
		visitQueryParameter(expr, arg);
		return super.visitParameter(expr, arg);
	}
	
	@Override
	public Void visitSetParameter(SetParameter expr, Map<String, MetaObject> arg) {
		visitQueryParameter(expr, arg);
		return super.visitSetParameter(expr, arg);
	}

	private void visitQueryParameter(QueryParameter expr, Map<String, MetaObject> arg) {
		MetaObject parameterType = arg.get(expr.getName());
		if (parameterType == null) {
			error((QueryPart) expr, "Undeclared parameter.");
		} else {
			expr.setDeclaredType(parameterType);
		}
	}
	
	@Override
	protected Void processTypedExpression(TypedExpression expr, Void context, Map<String, MetaObject> arg) {
		try {
			MetaObject type = typeSystem.getType(expr.getTypeName());
			expr.setDeclaredType(type);
		} catch (UnknownTypeException ex) {
			error(expr, "Unknown type.");
		}
		return super.processTypedExpression(expr, context, arg);
	}
	
	@Override
	protected Void processAttribute(Attribute expr, Void context, Map<String, MetaObject> arg) {
		try {
			MetaObject ownerType = typeSystem.getType(expr.getOwnerTypeName());
			MOAttribute attribute = MetaObjectUtils.getAttribute(ownerType, expr.getAttributeName());
			if (attribute instanceof MOReference) {
				error(expr, "Attribute ''{0}'' is a " + MOReference.class.getSimpleName()
						+ ". Use `reference(typeName, referenceName)` for a reference attribute.",
					attribute);
			} else {
				InternalExpressionFactory.setAttributeImpl(expr, attribute);
			}
		} catch (NoSuchAttributeException ex) {
			error(expr, "No such attribute ''{0}'' in context type ''{1}''.", expr.getAttributeName(), expr.getOwnerTypeName());
		} catch (UnknownTypeException ex) {
			error(expr, "No such type ''{0}''.", expr.getOwnerTypeName());
		}
		return super.processAttribute(expr, context, arg);
	}
	
	@Override
	protected Void processReference(Reference expr, Void context, Map<String, MetaObject> arg) {
		try {
			MetaObject ownerType = typeSystem.getType(expr.getOwnerTypeName());
			try {
				MOAttribute attributeImpl = MetaObjectUtils.getAttribute(ownerType, expr.getAttributeName());
				if (attributeImpl instanceof MOReference) {
					InternalExpressionFactory.setReferenceAttributeImpl(expr, (MOReference) attributeImpl);
				} else {
					error(expr,
						"Attribute ''{0}'' is not a " + MOReference.class.getSimpleName()
								+ ". Use `attribute(typeName, attributeName)` for a primitive attribute.",
						attributeImpl);
				}
			} catch (NoSuchAttributeException ex) {
				error(expr, "No such attribute ''{0}'' in context type ''{1}''.", expr.getAttributeName(),
					expr.getOwnerTypeName());
			}
		} catch (UnknownTypeException ex) {
			error(expr, "No such type ''{0}''.", expr.getOwnerTypeName());
		}
		return super.processReference(expr, context, arg);
	}

	@Override
	protected Void processFlex(Flex expr, Void context, Map<String, MetaObject> arg) {
		MOPrimitive declaredType = MOPrimitive.getPrimitive(expr.getTypeName());
		if (declaredType == null) {
			error(expr, "Not a primitive type.");
		} else {
			expr.setDeclaredType(declaredType);
		}
		return none;
	}

	@Override
	protected Void visitTypedSetExpression(TypedSetExpression expr, Map<String, MetaObject> arg) {
		MetaObject declaredType;
		try {
			declaredType = typeSystem.getType(expr.getTypeName());
		} catch (UnknownTypeException ex) {
			error(expr, "Unknown type ''{0}''.", expr.getTypeName());
			return none;
		}
		
		if (!MetaObjectUtils.isItemType(declaredType)) {
			error(expr, "''{0}'' is ot of item type.", declaredType);
			return none;
		}

		expr.setDeclaredType(declaredType);
		return none;
	}
	
	private void error(QueryPart expr, String message, Object... args) {
		log.error(expr, message, args);
	}

}
