/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.template.TemplateExpression.Alternative;
import com.top_logic.basic.config.template.TemplateExpression.Choice;
import com.top_logic.basic.config.template.TemplateExpression.CollectionAccess;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.FunctionCall;
import com.top_logic.basic.config.template.TemplateExpression.GlobalConfig.FunctionDef;
import com.top_logic.basic.config.template.TemplateExpression.LiteralInt;
import com.top_logic.basic.config.template.TemplateExpression.LiteralText;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;
import com.top_logic.basic.config.template.TemplateExpression.SelfAccess;
import com.top_logic.basic.config.template.TemplateExpression.VariableAccess;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.util.Utils;

/**
 * Evaluation visitor for {@link ConfigExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Eval implements ConfigExpressionVisitor<Object, Eval.IContext, RuntimeException> {

	/**
	 * Exception thrown, if an expression cannot be evaluated.
	 */
	public static class EvalException extends RuntimeException {

		/**
		 * Creates a {@link EvalException}.
		 * 
		 * @param message
		 *        See {@link RuntimeException#RuntimeException(String)}.
		 */
		public EvalException(String message) {
			super(message);
		}

		/**
		 * Creates a {@link EvalException} with a linked cause.
		 * 
		 * @param message
		 *        See {@link RuntimeException#RuntimeException(String, Throwable)}.
		 * @param cause
		 *        See {@link RuntimeException#RuntimeException(String, Throwable)}.
		 */
		public EvalException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * The context an {@link Eval evaluation} operates on.
	 */
	public interface IContext {

		/**
		 * The implicit <code>this</code> object that is displayed by the evaluated
		 * {@link TemplateExpression}.
		 */
		Object getBaseObject();

		/**
		 * Lookup of the value of an additional variable with the given name available from the
		 * evaluation context.
		 */
		Object getVariableValue(VariableAccess variable);

		/**
		 * Creates a new context defining the variable with the given name.
		 * 
		 * <p>
		 * The initial value of the new variable is <code>null</code> but can be
		 * {@link IContextOverlay#assign(Object) assigned} later on.
		 * </p>
		 *
		 * @param varName
		 *        The name of the newly created variable.
		 * @return A new context defining the variable with the given name.
		 */
		default IContextOverlay scope(String varName) {
			return new ContextOverlay(this, varName);
		}

	}

	/**
	 * A context defining a certain variable.
	 * 
	 * @see #assign(Object)
	 */
	public interface IContextOverlay extends IContext {

		/**
		 * Assigns a new value to the variable defined by this context.
		 *
		 * @param newValue
		 *        The new value of the context variable.
		 */
		void assign(Object newValue);

	}

	/**
	 * {@link IContext} implementation.
	 */
	public static class EvalContext implements IContext {

		private Object _context;

		private final Map<String, ?> _variables;

		/**
		 * Creates a {@link Eval.EvalContext}.
		 *
		 * @param context
		 *        The context object (implicit parameter), see {@link #getBaseObject()}.
		 * @param variableBindings
		 *        Number of explicit variable bindings.
		 */
		public EvalContext(Object context, Map<String, ?> variableBindings) {
			_context = context;
			_variables = variableBindings;
		}

		/**
		 * The context object (implicit parameter for the template expansion).
		 */
		@Override
		public Object getBaseObject() {
			return _context;
		}

		@Override
		public Object getVariableValue(VariableAccess variable) {
			String variableName = variable.getVariableName();
			Object result = _variables.get(variableName);
			if (result == null && !_variables.containsKey(variableName)) {
				throw new EvalException(
					"There is no binding for the variable '" + variableName + "'" + variable.location() + ".");
			}
			return result;
		}
	}

	/**
	 * Variable defining context implementation.
	 */
	protected static class ContextOverlay implements IContextOverlay {
		private IContext _outer;

		private String _varName;

		private Object _value;

		/**
		 * Creates a {@link ContextOverlay}.
		 */
		public ContextOverlay(IContext outer, String varName) {
			_outer = outer;
			_varName = varName;
		}

		@Override
		public Object getVariableValue(VariableAccess variable) {
			if (Utils.equals(_varName, variable.getVariableName())) {
				return _value;
			} else {
				return _outer.getVariableValue(variable);
			}
		}

		@Override
		public Object getBaseObject() {
			if (_varName == null) {
				return _value;
			} else {
				return _outer.getBaseObject();
			}
		}

		@Override
		public void assign(Object newValue) {
			_value = newValue;
		}
	}

	private final ModelAccess _modelAccess;

	private final Map<String, FunctionDef> _functions;

	/**
	 * Creates a {@link Eval}.
	 */
	public Eval(ModelAccess modelAccess) {
		_modelAccess = modelAccess;
		_functions = ApplicationConfig.getInstance().getConfig(TemplateExpression.GlobalConfig.class).getFunctions();
	}

	@Override
	public Object visitLiteralText(LiteralText expr, Eval.IContext arg) {
		return expr.getText();
	}

	@Override
	public Object visitLiteralInt(LiteralInt expr, Eval.IContext arg) {
		return expr.getValue();
	}

	@Override
	public Object visitPropertyAccess(PropertyAccess expr, Eval.IContext arg) {
		Object targetValue = expr.getTarget().visitEvaluator(this, arg);
		if (targetValue == null) {
			return null;
		}
		return _modelAccess.getProperty(targetValue, expr);
	}

	@Override
	public Object visitVariableAccess(VariableAccess expr, Eval.IContext arg) {
		return arg.getVariableValue(expr);
	}

	@Override
	public Object visitFunctionCall(FunctionCall expr, Eval.IContext arg) {
		GenericFunction<?> fun = getFunction(expr);
		List<ConfigExpression> arguments = expr.getArgs();
		int argumentCount = arguments.size();
		if (fun.hasVarArgs()) {
			if (argumentCount < fun.getArgumentCount()) {
				throw new EvalException("Function '" + expr.getName() + "' requires at least "
					+ fun.getArgumentCount() + " arguments" + expr.location() + ": " + expr);
			}
		} else {
			if (argumentCount != fun.getArgumentCount()) {
				throw new EvalException("Function '" + expr.getName() + "' requires exactly "
					+ fun.getArgumentCount() + " arguments" + expr.location() + ": " + expr);
			}
		}

		Object[] values = new Object[argumentCount];
		for (int n = 0; n < argumentCount; n++) {
			values[n] = arguments.get(n).visitEvaluator(this, arg);
		}
		try {
			return fun.invoke(values);
		} catch (RuntimeException ex) {
			throw new EvalException(
				"Function evaluation '" + expr + "' failed" + expr.location() + ": " + ex.getMessage(), ex);
		}
	}

	private GenericFunction<?> getFunction(FunctionCall expr) {
		String functionName = expr.getName();
		FunctionDef def = _functions.get(functionName);
		if (def == null) {
			throw new EvalException("There is no function '" + functionName + "'" + expr.location() + ".");
		}
		return def.getImpl();
	}

	@Override
	public Object visitCollectionAccess(CollectionAccess expr, Eval.IContext arg) {
		ConfigExpression collectionExpr = expr.getExpr();
		Object collection = collectionExpr.visitEvaluator(this, arg);
		if (collection == null) {
			return null;
		}
		ConfigExpression indexExpr = expr.getIndex();
		Object indexValue = indexExpr.visitEvaluator(this, arg);
		return EvalUtil.accessCollection(collectionExpr, indexExpr, collection, indexValue);
	}

	@Override
	public Object visitAlternative(Alternative expr, Eval.IContext arg) {
		ConfigExpression testExpr = expr.getExpr();
		Object testValue = testExpr.visitEvaluator(this, arg);
		if (EvalUtil.isNonEmpty(testExpr, testValue)) {
			return testValue;
		} else {
			TemplateExpression fallbackExpr = expr.getFallback();
			return evalTemplate(fallbackExpr, arg);
		}
	}

	@Override
	public Object visitChoice(Choice expr, Eval.IContext arg) {
		ConfigExpression testExpr = expr.getTest();
		Object testValue = testExpr.visitEvaluator(this, arg);
		if (EvalUtil.isTrue(testExpr, testValue)) {
			return evalTemplate(expr.getPositive(), arg);
		} else {
			return evalTemplate(expr.getNegative(), arg);
		}
	}

	private Object evalTemplate(TemplateExpression expr, Eval.IContext arg) {
		if (expr instanceof ConfigExpression) {
			return ((ConfigExpression) expr).visitEvaluator(this, arg);
		} else {
			return expr;
		}
	}

	@Override
	public Object visitSelfAccess(SelfAccess expr, Eval.IContext arg) {
		return arg.getBaseObject();
	}

}
