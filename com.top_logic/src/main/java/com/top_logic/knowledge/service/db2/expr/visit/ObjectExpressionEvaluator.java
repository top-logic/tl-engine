/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.Map;

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.TypeCheck;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;

/**
 * BHU: This class
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ObjectExpressionEvaluator<EC extends ObjectExpressionEvaluator.ExpressionContext> extends AbstractExpressionEvaluator<EC> {

	/**
	 * Context object used as visitor argument.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected static class ExpressionContext extends AbstractExpressionEvaluator.ExpressionContext {

		private final Object contextObject;

		private final Map<String, ?> parameterValues;

		private final ObjectContext _objectContext;

		private final Revision _evaluationRevision;
		
		public ExpressionContext(Revision evaluationRevision, ObjectContext objectContext, Object contextObject,
				Map<String, ?> parameterValues) {
			_evaluationRevision = evaluationRevision;
			_objectContext = objectContext;
			this.contextObject = contextObject;
			this.parameterValues = parameterValues;
		}
		
		/**
		 * Creates a new {@link ExpressionContext}.
		 */
		public ExpressionContext(Revision evaluationRevision, DataObject baseObject, Map<String, ?> parameterValues) {
			this(evaluationRevision, (ObjectContext) baseObject, baseObject, parameterValues);
		}

		public Object getContextObject() {
			return contextObject;
		}
		
		public ObjectContext getObjectContext() {
			return _objectContext;
		}

		/**
		 * {@link Revision} in which the expression is evaluated.
		 */
		public Revision getEvaluationRevision() {
			return _evaluationRevision;
		}

		@Override
		public Object getParameterValue(String name) {
			Object object = parameterValues.get(name);
			if (object == null) {
				throw new IllegalStateException("No value for parameter '" + name + "' given.");
			}
			return object;
		}

		protected Map<String, ?> getParameterValues() {
			return parameterValues;
		}
	}

	protected EC createContext(EC parent, Object contextObject) {
		return createContext(parent, parent.getObjectContext(), contextObject);
	}

	protected abstract EC createContext(EC parent, ObjectContext objectContext, Object contextObject);
	
	@Override
	public Object visitAttribute(Attribute expr, EC arg) {
		String attributeName = expr.getAttributeName();
		Object result = getAttributeValue(expr, attributeName, arg);
		if (result == null) {
			/* if the context object is a new object (not already committed) then the internal life
			 * cycle attributes are not set */
			if (attributeName.equals(BasicTypes.REV_MAX_ATTRIBUTE_NAME)
				|| attributeName.equals(BasicTypes.REV_MIN_ATTRIBUTE_NAME)) {
				result = Revision.CURRENT_REV_WRAPPER;
			}
		}
		return result;
	}

	private Object getAttributeValue(ContextExpression expr, String attributeName, EC arg) {
		DataObject baseObject = resolveContextObject(expr, arg);
		try {
			return baseObject.getAttributeValue(attributeName);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Unable to retrieve attribute '" + attributeName + "' from type '"
				+ baseObject.tTable() + "'", ex);
		}
	}

	private DataObject resolveContextObject(ContextExpression expr, EC arg) {
		Object contextResult = expr.getContext().visit(this, arg);
		if (contextResult instanceof ObjectKey) {
			return (DataObject) arg.getObjectContext().resolveObject((ObjectKey) contextResult);
		} else {
			return (DataObject) contextResult;
		}
	}
	
	private ObjectKey resolveContextObjectKey(ContextExpression expr, EC arg) {
		Object contextResult = expr.getContext().visit(this, arg);
		if (contextResult instanceof IdentifiedObject) {
			return ((IdentifiedObject) contextResult).tId();
		} else {
			return (ObjectKey) contextResult;
		}
	}

	@Override
	public Object visitFlex(Flex expr, EC arg) {
		return getAttributeValue(expr, expr.getAttributeName(), arg);
	}
	
	@Override
	public Object visitGetEntry(GetEntry expr, EC arg) {
		return ((Tuple) arg.getContextObject()).get(expr.getIndex());
	}
	
	@Override
	public Object visitReference(Reference expr, EC arg) {
		DataObject baseObject = resolveContextObject(expr, arg);
		final ReferencePart accessPart = expr.getAccessType();
		final MOReference attribute = expr.getAttribute();
		final ObjectKey key = baseObject.getReferencedKey(attribute);

		if (key == null) {
			// no reference set for that attribute
			return null;
		}
		if (accessPart == null) {
			return key;
		} else {
			switch (accessPart) {
				case branch:
					return Long.valueOf(key.getBranchContext());
				case revision:
					return key.getHistoryContext();
				case name:
					return key.getObjectName();
				case type:
					return key.getObjectType().getName();
			}
			throw ReferencePart.noSuchPart(accessPart);
		}
	}
	
	@Override
	public Object visitEval(Eval expr, EC arg) {
		EC newContext = createNewContextForDescending(expr, arg);
		return expr.getExpr().visit(this, newContext);
	}

	/**
	 * Creates a new context from the context of the expression to descend into other parts of the
	 * {@link Expression}.
	 */
	protected EC createNewContextForDescending(ContextExpression expr, EC arg) {
		Object newContextObject = expr.getContext().visit(this, arg);
		return createContext(arg, newContextObject);
	}
	
	@Override
	public Object visitContext(ContextAccess expr, EC arg) {
		return arg.getContextObject();
	}

	@Override
	public Object visitHasType(HasType expr, EC arg) {
		MetaObject baseObject = getContextObjectType(expr, arg);
		if (baseObject == null) {
			return Boolean.FALSE;
		}
		return baseObject.equals(expr.getDeclaredType());
	}

	@Override
	public Object visitInstanceOf(InstanceOf expr, EC arg) {
		MetaObject baseObject = getContextObjectType(expr, arg);
		if (baseObject == null) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(baseObject.isSubtypeOf(expr.getDeclaredType()));
	}
	
	private MetaObject getContextObjectType(TypeCheck expr, EC arg) {
		ObjectKey contextObjectKey = resolveContextObjectKey(expr, arg);
		if (contextObjectKey == null) {
			return null;
		}
		return contextObjectKey.getObjectType();
	}
	
	@Override
	public Object visitBranch(EC arg) {
		Object object = arg.getArgument(0).visit(this, arg);
		return ObjectKey.GET_BRANCH.map(object);
	}

	@Override
	public Object visitRevision(EC arg) {
		Object object = arg.getArgument(0).visit(this, arg);
		return ObjectKey.GET_HISTORY_CONTEXT.map(object);
	}

	@Override
	public Object visitHistoryContext(EC arg) {
		Object object = arg.getArgument(0).visit(this, arg);
		return ObjectKey.GET_HISTORY_CONTEXT.map(object);
	}

	@Override
	public Object visitIdentifier(EC arg) {
		Object object = arg.getArgument(0).visit(this, arg);
		return ObjectKey.GET_OBJECT_NAME_MAPPING.map(object);
	}

	@Override
	public Object visitType(EC arg) {
		Object object = arg.getArgument(0).visit(this, arg);
		return ObjectKey.GET_TYPE_NAME.map(object);
	}

	@Override
	public Object visitIsCurrent(IsCurrent expr, EC arg) {
		ObjectKey contextObjectKey = resolveContextObjectKey(expr, arg);
		return Boolean.valueOf(HistoryUtils.isCurrent(contextObjectKey));
	}

	@Override
	public Object visitRequestedHistoryContext(RequestedHistoryContext expr, EC arg) {
		return arg.getEvaluationRevision().getCommitNumber();
	}
}

