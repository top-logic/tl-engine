/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.func.GenericFunction;

/**
 * Abstract root node of rendering templates.
 * 
 * <p>
 * {@link TemplateExpression}s define the rendering for model objects.
 * </p>
 * 
 * <p>
 * The template syntax is defined as follows:
 * </p>
 * 
 * <dl>
 * <dt>Literal text</dt>
 * <dd>
 * <p>
 * All text is interpreted as literal (internationalized) text, except for embedded template
 * expressions, see blow.
 * </p>
 * </dd>
 * 
 * <dt>Template expression</dt>
 * <dd>
 * <p>
 * A template expression is surrounded with braces <code>{ [expr] }</code>.
 * </p>
 * </dd>
 * 
 * <dt>Literal XML/HTML</dt>
 * <dd>
 * <p>
 * XML/HTML structure can be output literally with embedded expressions in attribute values and tag
 * content: <code>&lt;div class="myclass {$other}">Some {$value}.&lt;/div></code>.
 * </p>
 * </dd>
 * 
 * <dt>Template reference</dt>
 * <dd>
 * <p>
 * In a template expression, another template can be referenced by its name
 * <code>{-> [other-template]}</code>. In the output, a template reference is replaced by the
 * expansion of the referenced template based on the same configuration instance.
 * </p>
 * </dd>
 * 
 * <dt>Conditional evaluation</dt>
 * <dd>
 * <p>
 * A template expression can consist of a conditional evaluation operator
 * <code>{ [test] ? [if] : [else] }</code>. When evaluated, the conditional expression evaluates to
 * the expansion of the <code>[if]</code> expression, if the expression <code>[test]</code>
 * evaluates to a non-<code>null</code>, non-zero, or non-empty value. Otherwise, the
 * <code>[else]</code> expression is evaluated.
 * </p>
 * </dd>
 * 
 * <dt>Alternative evaluation</dt>
 * <dd>
 * <p>
 * To create an alternative representation for an otherwise empty value, a short-cut form for
 * <code>{ [alternative1] ? [alternative1] : [alternative2] }</code> is possible through an
 * alternative expression <code>{ [alternative1] | [alternative2] }</code>.
 * </p>
 * </dd>
 * 
 * <dt>Embedded template text</dt>
 * <dd>
 * <p>
 * Within a template expression, the parser mode can be switched back to literal text (with
 * potentially embedded template expressions) by surrounding the literal text again with braces
 * <code>{ [test] ? {some literal text} : {some other literal text} }</code>
 * </p>
 * </dd>
 * 
 * <dt>Property access</dt>
 * <dd>
 * <p>
 * Access to the value of a model property of the underlying model object is done by using the
 * property name <code>{my-property}</code>.
 * </p>
 * 
 * <p>
 * Accessing a property that is not defined by the underlying model results in an error.
 * </p>
 * 
 * <p>
 * By default, properties of {@link ConfigurationItem} models can be accessed using their property
 * name. When using a <code>com.top_logic.layout.template.TLModelAccess</code> for evaluation,
 * persistent objects (<code>com.top_logic.model.TLObject</code>) and arbitrary objects implementing
 * the <code>com.top_logic.layout.template.WithProperties</code> interface can also be accessed.
 * </p>
 * </dd>
 * 
 * <dt>Variable access</dt>
 * <dd>
 * <p>
 * In the actual evaluation context, a set of variables may be defined (as key value mapping). Such
 * variable can be accessed by its name by prepending a <code>$</code> sign. A variable with name
 * <code>my-var</code> may be accessed with <code>$my-var</code>.
 * </p>
 * </dd>
 * 
 * <dt>Function call</dt>
 * <dd>
 * <p>
 * Globally defined functions can be invoked from template expressions. A function is referenced
 * through a <code>#</code> prefix to its name. The function <code>sublist</code> is e.g. invoked
 * through the expression <code>#sublist($l, 2)</code>, where the first argument is expected to
 * reference a list value and the second gives the start offset as integer value. For all defined
 * functions, see the configuration of {@link GlobalConfig#getFunctions()}.
 * </p>
 * </dd>
 * 
 * <dt>Iteration</dt>
 * <dd>
 * <p>
 * If an accessed value is list-valued, all values may be iterated with e.g.
 * <code>{foreach(x : $my-var, '; ', {"{$x}"})}</code>. This binds each value of the list value
 * <code>$my-var</code> to the locally-defined variable <code>$x</code> and evaluates the template
 * <code>"{$x}"</code> for each list element (effectively dumping each element surrounded by double
 * quotes). Between two evaluations, the separator expression <code>'; '</code> expands to a
 * semicolon followed by a space. If the value of <code>$my-var</code> is the list of numbers
 * <code>1, 2, 3</code>, the final evaluation result would be <code>"1"; "2"; "3"</code>.
 * </p>
 * </dd>
 * 
 * <dt>Indexed access</dt>
 * <dd>
 * <p>
 * If an accessed value is list-valued, an element at some index may be accessed with
 * <code>$my-var[42]</code>.
 * </p>
 * 
 * <p>
 * Accessing an out-of range index results in a <code>null</code> value. This allows e.g. for
 * testing, whether a collection contains a certain number of entries through
 * <code>{list[1] ? ...}</code>.
 * </p>
 * 
 * <p>
 * Accessing with a negative index has the semantics of accessing with an index relative to the end
 * of the list. For a list with size <code>5</code>, the expression <code>{list[-2]}</code>
 * evaluates to the element at index <code>3</code>.
 * </p>
 * </dd>
 * 
 * <dt>Map access</dt>
 * <dd>
 * <p>
 * If an accessed value is map-valued with string keys, an element with some key may be accessed
 * with <code>$my-var['some-key']</code>.
 * </p>
 * </dd>
 * 
 * <dt>Reference access</dt>
 * <dd>
 * <p>
 * If an accessed value is a configuration value itself, a specific property may be accessed using a
 * dot-separated path expression <code>{$my-var.some-property}</code>.
 * </p>
 * </dd>
 * 
 * <dt>Literal string</dt>
 * <dd>
 * <p>
 * A literal string (without the possibility of embedding further expressions) is written as
 * <code>'some text'</code>, or <code>"some text"</code>.
 * </p>
 * </dd>
 * 
 * <dt>Literal numbers</dt>
 * <dd>
 * <p>
 * A literal integral value is simply written as number <code>42</code>.
 * </p>
 * </dd>
 * 
 * <dt>Access to the underlying model object</dt>
 * <dd>
 * <p>
 * The underlying model as whole can be referenced by the keyword <code>this</code>.
 * </p>
 * </dd>
 * 
 * <dt>HTML output</dt>
 * <dd>
 * <p>
 * When output is generated through a <code>com.top_logic.layout.template.TemplateWriter</code>, the
 * template can contain embedded HTML syntax. The HTML may contain embedded expressions within
 * attribute or element content:
 * </p>
 * 
 * <p>
 * <code>
 * &lt;div class="{my-class}">{my-text}&lt;/div>
 * </code>
 * </p>
 * </dd>
 * </dl>
 * 
 * @see "com.top_logic.layout.form.values.edit.ConfigLabelProvider"
 * @see "com.top_logic.layout.template.TemplateWriter"
 * @see "com.top_logic.layout.template.WithProperties"
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(TemplateFormat.class)
public abstract class TemplateExpression {

	private TextRange _range;

	/**
	 * Creates a {@link TemplateExpression}.
	 *
	 */
	public TemplateExpression(TextRange range) {
		setRange(range);
	}
	
	/**
	 * The {@link TextRange} this expression occupies in its source code.
	 */
	public TextRange getRange() {
		return _range;
	}

	/**
	 * @see #getRange()
	 */
	public void setRange(TextRange range) {
		_range = range;
	}

	/**
	 * Suffix to an (error) text describing the source code location.
	 */
	public String location() {
		if (_range != null) {
			return _range.location();
		} else {
			return "";
		}
	}

	/**
	 * Global configuration options for evaluation.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * @see #getFunctions()
		 */
		String FUNCTIONS = "functions";

		/**
		 * Definitions of functions accessible from templates.
		 * 
		 * @see FunctionDef
		 */
		@Name(FUNCTIONS)
		@Key(FunctionDef.NAME_ATTRIBUTE)
		Map<String, FunctionDef> getFunctions();

		/**
		 * Definition of a single {@link #getName() named} function.
		 * 
		 * @see #getImpl()
		 */
		interface FunctionDef extends NamedConfigMandatory {
			String IMPL = "impl";

			/**
			 * The function implementation.
			 */
			@InstanceFormat
			@Mandatory
			@NonNullable
			@Name(IMPL)
			GenericFunction<?> getImpl();
		}

	}

	/**
	 * Visits this expression with the given {@link TemplateVisitor}
	 * 
	 * @param v
	 *        The visitor to call.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	public abstract <R, A, E extends Throwable> R visit(TemplateVisitor<R, A, E> v, A arg) throws E;

	@Override
	public String toString() {
		return visit(new ToString(), new StringBuilder()).toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(_range);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateExpression other = (TemplateExpression) obj;
		return Objects.equals(_range, other._range);
	}

	/**
	 * Base class for template structure.
	 */
	public static abstract class TemplateStructure extends TemplateExpression {

		/**
		 * Creates a {@link TemplateStructure}.
		 */
		public TemplateStructure(TextRange range) {
			super(range);
		}

		/**
		 * Visits this expression with the given {@link ConfigExpressionVisitor}
		 * 
		 * @param v
		 *        The visitor to call.
		 * @param arg
		 *        The argument to the visit.
		 * @return The result of the visit.
		 */
		public abstract <R, A, E extends Throwable> R visitStructure(TemplateStructureVisitor<R, A, E> v, A arg)
				throws E;

		@Override
		public final <R, A, E extends Throwable> R visit(TemplateVisitor<R, A, E> v, A arg) throws E {
			return visitStructure(v, arg);
		}
	}

	/**
	 * Base class for expressions.
	 */
	public static abstract class ConfigExpression extends TemplateExpression {

		/**
		 * Creates a {@link TemplateExpression.ConfigExpression}.
		 *
		 */
		public ConfigExpression(TextRange range) {
			super(range);
		}

		/**
		 * Visits this expression with the given {@link ConfigExpressionVisitor}
		 * 
		 * @param v
		 *        The visitor to call.
		 * @param arg
		 *        The argument to the visit.
		 * @return The result of the visit.
		 */
		public abstract <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg)
				throws E;

		@Override
		public final <R, A, E extends Throwable> R visit(TemplateVisitor<R, A, E> v, A arg) throws E {
			return visitEvaluator(v, arg);
		}
	}

	/**
	 * A text constant.
	 * 
	 * <p>
	 * Syntax: <code>"[string-contents]"</code>, or '[string-contents]'
	 * </p>
	 * 
	 * <p>
	 * Within <code>string-contents</code>, backslash escape syntax is accepted to quote special
	 * characters: \", \', \r, \n.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class LiteralText extends ConfigExpression {

		private final String _text;

		/**
		 * Creates a {@link LiteralText}.
		 * 
		 * @param text
		 *        See {@link #getText()}.
		 */
		public LiteralText(TextRange range, String text) {
			super(range);
			_text = text;
		}

		/**
		 * The actual constant value.
		 */
		public String getText() {
			return _text;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitLiteralText(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_text);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			LiteralText other = (LiteralText) obj;
			return Objects.equals(_text, other._text);
		}

	}

	/**
	 * An integer constant.
	 * 
	 * <p>
	 * Syntax: A number in decimal representation.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class LiteralInt extends ConfigExpression {

		private final int _value;

		/**
		 * Creates a {@link LiteralInt}.
		 * 
		 * @param value
		 *        See {@link #getValue()}.
		 */
		public LiteralInt(TextRange range, int value) {
			super(range);
			_value = value;
		}

		/**
		 * The actual constant value.
		 */
		public int getValue() {
			return _value;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitLiteralInt(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			LiteralInt other = (LiteralInt) obj;
			return _value == other._value;
		}

	}

	/**
	 * Access to a model property.
	 * 
	 * <p>
	 * Syntax: <code>[expr].[property-name]</code>
	 * </p>
	 * 
	 * <p>
	 * First evaluates <code>expr</code> to a model value. Then accesses the property named
	 * <code>property-name</code> from this model and returns its value as result.
	 * </p>
	 * 
	 * <p>
	 * Alternative syntax: <code>[property-name]</code>
	 * </p>
	 * 
	 * <p>
	 * Evaluates to the property value of the context model with the given
	 * <code>property-name</code>.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class PropertyAccess extends ConfigExpression {

		private final ConfigExpression _target;

		private final String _propertyName;

		/**
		 * Creates a {@link PropertyAccess}.
		 * 
		 * @param target
		 *        See {@link #getTarget()}.
		 * @param propertyName
		 *        See {@link #getPropertyName()}.
		 */
		public PropertyAccess(TextRange range, ConfigExpression target, String propertyName) {
			super(range);
			_target = target;
			_propertyName = propertyName;
		}

		/**
		 * The expression that produces the model to access.
		 */
		public ConfigExpression getTarget() {
			return _target;
		}

		/**
		 * The name of the configuration property to access.
		 */
		public String getPropertyName() {
			return _propertyName;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitPropertyAccess(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_propertyName, _target);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyAccess other = (PropertyAccess) obj;
			return Objects.equals(_propertyName, other._propertyName) && Objects.equals(_target, other._target);
		}

	}

	/**
	 * Access to a global variable.
	 * 
	 * <p>
	 * Syntax: <code>$[variable-name]</code>
	 * </p>
	 * 
	 * <p>
	 * Evaluates to the variable with the given name in the current context. Variables are either
	 * globally defined or locally bound e.g. in {@link Foreach}.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class VariableAccess extends ConfigExpression {

		private final String _variableName;

		/**
		 * Creates a {@link VariableAccess}.
		 * 
		 * @param variableName
		 *        See {@link #getVariableName()}.
		 */
		public VariableAccess(TextRange range, String variableName) {
			super(range);
			_variableName = variableName;
		}

		/**
		 * The name of the accessed variable.
		 */
		public String getVariableName() {
			return _variableName;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitVariableAccess(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_variableName);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			VariableAccess other = (VariableAccess) obj;
			return Objects.equals(_variableName, other._variableName);
		}
	}

	/**
	 * Evaluation of a function.
	 * 
	 * <p>
	 * Syntax: <code>#[function-name]([arg1], [arg1],... [argN])</code>
	 * </p>
	 */
	public static class FunctionCall extends ConfigExpression {

		private final String _name;

		private final List<ConfigExpression> _args;

		/**
		 * Creates a {@link VariableAccess}.
		 * 
		 * @param name
		 *        See {@link #getName()}.
		 */
		public FunctionCall(TextRange range, String name, List<ConfigExpression> args) {
			super(range);
			_name = name;
			_args = args;
		}

		/**
		 * The name of the accessed variable.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * The function arguments.
		 */
		public List<ConfigExpression> getArgs() {
			return _args;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitFunctionCall(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_args, _name);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			FunctionCall other = (FunctionCall) obj;
			return Objects.equals(_args, other._args) && Objects.equals(_name, other._name);
		}
	}

	/**
	 * Access to the element of an indexed collection.
	 * 
	 * <p>
	 * Syntax: <code>[collection-expr] '[' [index-expr] ']'</code>
	 * </p>
	 * 
	 * <p>
	 * First evaluates <code>collection-expr</code> and expects that it evaluates to a {@link List}
	 * or {@link Map} value. Then it evaluates <code>index-expr</code> to an index value. The result
	 * is either the element at the specified index, or the map value with the specified key.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class CollectionAccess extends ConfigExpression {

		private final ConfigExpression _expr;

		private final ConfigExpression _index;

		/**
		 * Creates a {@link CollectionAccess}.
		 * 
		 * @param expr
		 *        See {@link #getExpr()}.
		 * @param index
		 *        See {@link #getIndex()}.
		 */
		public CollectionAccess(TextRange range, ConfigExpression expr, ConfigExpression index) {
			super(range);
			_expr = expr;
			_index = index;
		}

		/**
		 * The expression that evaluates to a collection value.
		 */
		public ConfigExpression getExpr() {
			return _expr;
		}

		/**
		 * The expression that evaluates to an index value for access to the {@link #getExpr()
		 * collection}.
		 */
		public ConfigExpression getIndex() {
			return _index;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitCollectionAccess(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_expr, _index);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CollectionAccess other = (CollectionAccess) obj;
			return Objects.equals(_expr, other._expr) && Objects.equals(_index, other._index);
		}
	}

	/**
	 * An expression with a fallback.
	 * 
	 * <p>
	 * Syntax: <code>[expr-1] | [expr-2] | [expr-3] | ...</code>
	 * </p>
	 * 
	 * <p>
	 * Evaluates the given expressions in the order given until the first one evaluates to a
	 * <code>true</code> or non-empty result, which is also the result of the whole alternative
	 * expression.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class Alternative extends ConfigExpression {

		private final ConfigExpression _expr;

		private final TemplateExpression _fallback;

		/**
		 * Creates a {@link Alternative}.
		 * 
		 * @param expr
		 *        See {@link #getExpr()}.
		 * @param fallback
		 *        See {@link #getFallback()}.
		 */
		public Alternative(TextRange range, ConfigExpression expr, TemplateExpression fallback) {
			super(range);
			_expr = expr;
			_fallback = fallback;
		}

		/**
		 * The that is evaluated.
		 */
		public ConfigExpression getExpr() {
			return _expr;
		}

		/**
		 * The alternative expression that is evaluated, if {@link #getExpr()} produces an empty
		 * result.
		 */
		public TemplateExpression getFallback() {
			return _fallback;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitAlternative(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_expr, _fallback);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Alternative other = (Alternative) obj;
			return Objects.equals(_expr, other._expr) && Objects.equals(_fallback, other._fallback);
		}

	}

	/**
	 * An if-then-else expression.
	 * 
	 * <p>
	 * Syntax: <code>[expression] ? [template-1] : [template-2] </code>
	 * </p>
	 * 
	 * Evaluates <code>expression</code>. If the result is <code>true</code> or non-empty,
	 * <code>template-1</code> is expanded, <code>template-2</code>, otherwise.
	 * 
	 * @see TemplateExpression
	 */
	public static class Choice extends ConfigExpression {

		private final ConfigExpression _test;

		private final TemplateExpression _positive;

		private final TemplateExpression _negative;

		/**
		 * Creates a {@link Choice}.
		 * 
		 * @param test
		 *        See {@link #getTest()}.
		 * @param positive
		 *        See {@link #getPositive()}.
		 * @param negative
		 *        See {@link #getNegative()}.
		 */
		public Choice(TextRange range, ConfigExpression test, TemplateExpression positive,
				TemplateExpression negative) {
			super(range);
			_test = test;
			_positive = positive;
			_negative = negative;
		}

		/**
		 * The expression that is interpreted as boolean value.
		 */
		public ConfigExpression getTest() {
			return _test;
		}

		/**
		 * The result expression that is used, if {@link #getTest()} is evaluated to
		 * <code>true</code>.
		 */
		public TemplateExpression getPositive() {
			return _positive;
		}

		/**
		 * The result expression that is used, if {@link #getTest()} is evaluated to
		 * <code>false</code>.
		 */
		public TemplateExpression getNegative() {
			return _negative;
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitChoice(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_negative, _positive, _test);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Choice other = (Choice) obj;
			return Objects.equals(_negative, other._negative) && Objects.equals(_positive, other._positive)
				&& Objects.equals(_test, other._test);
		}
	}

	/**
	 * A loop template.
	 * 
	 * <p>
	 * Syntax: <code>foreach([var] : [collection], [separator], [iterator], [start], [stop]) </code>
	 * </p>
	 * 
	 * <p>
	 * Evaluates <code>collection</code> and expects a {@link Collection} value. It the assigns each
	 * element of this collection to a local variable named <code>var</code>. In the context of this
	 * assignment, the <code>iterator</code> template is expanded. After each iteration, the
	 * template <code>separator</code> is expanded, if the current iteration was not the last one.
	 * The whole expansion is surrounded with the evaluation of the <code>start</code> and
	 * <code>stop</code> templates.
	 * </p>
	 * 
	 * <p>
	 * If <code>var</code> is not given (alternative syntax <code>foreach([collection], ...)</code>
	 * ), the binding of the current context object is changed during the evaluation of the
	 * <code>iterator</code> template.
	 * </p>
	 * 
	 * <p>
	 * The parameters <code>separator</code>, <code>iterator</code>, <code>start</code>, and
	 * <code>stop</code> are optional. The parameter <code>iterator</code> defaults to
	 * {@link SelfAccess}, while all other parameters default to the literal empty string. If one
	 * optional parameter is given, all other parameters before it must also be given.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class Foreach extends TemplateStructure {
	
		private final ConfigExpression _collection;

		private final TemplateExpression _iterator;

		private final TemplateExpression _separator;

		private final TemplateExpression _start;

		private final TemplateExpression _stop;

		private final String _varName;

		/**
		 * Creates a {@link Foreach}.
		 * 
		 * @param varName
		 *        See {@link #getVarName()}
		 * @param collection
		 *        See {@link #getCollection()}.
		 * @param iterator
		 *        See {@link #getIterator()}.
		 * @param separator
		 *        See {@link #getSeparator()}.
		 * @param start
		 *        See {@link #getStart()}.
		 * @param stop
		 *        See {@link #getStop()}.
		 */
		public Foreach(TextRange range, String varName, ConfigExpression collection, TemplateExpression iterator,
				TemplateExpression separator, TemplateExpression start,
				TemplateExpression stop) {
			super(range);
			_varName = varName;
			_collection = collection;
			_iterator = iterator;
			_separator = separator;
			_start = start;
			_stop = stop;
		}

		/**
		 * Name of the local variable to temporarily assign collection elements to.
		 * 
		 * @return Local variable name, or <code>null</code> to use the context for iterating.
		 */
		public String getVarName() {
			return _varName;
		}

		/**
		 * The expression that evaluates to a collection to iterate over.
		 */
		public ConfigExpression getCollection() {
			return _collection;
		}

		/**
		 * The template that is expanded for each element of the collection.
		 * 
		 * <p>
		 * The element value itself is accessed through the {@link SelfAccess} expression.
		 * </p>
		 */
		public TemplateExpression getIterator() {
			return _iterator;
		}

		/**
		 * The template that is expanded between each expansion of elements.
		 */
		public TemplateExpression getSeparator() {
			return _separator;
		}

		/**
		 * The template that is expanded before the first element of the collection.
		 */
		public TemplateExpression getStart() {
			return _start;
		}

		/**
		 * The template that is expanded after the last element of the collection.
		 */
		public TemplateExpression getStop() {
			return _stop;
		}

		@Override
		public <R, A, E extends Throwable> R visitStructure(TemplateStructureVisitor<R, A, E> v, A arg) throws E {
			return v.visitForeach(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_collection, _iterator, _separator, _start, _stop, _varName);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Foreach other = (Foreach) obj;
			return Objects.equals(_collection, other._collection) && Objects.equals(_iterator, other._iterator)
				&& Objects.equals(_separator, other._separator) && Objects.equals(_start, other._start)
				&& Objects.equals(_stop, other._stop) && Objects.equals(_varName, other._varName);
		}
	
	}

	/**
	 * Access the evaluation context object.
	 * 
	 * <p>
	 * Syntax: <code>this</code>
	 * </p>
	 * 
	 * <p>
	 * Evaluates to the context object of the current evaluation. Especially relevant in
	 * {@link Foreach}.
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class SelfAccess extends ConfigExpression {

		/**
		 * Creates a {@link SelfAccess}.
		 */
		public SelfAccess(TextRange range) {
			super(range);
		}

		@Override
		public <R, A, E extends Throwable> R visitEvaluator(ConfigExpressionVisitor<R, A, E> v, A arg) throws E {
			return v.visitSelfAccess(this, arg);
		}

	}

	/**
	 * Reference to another template.
	 * 
	 * <p>
	 * Syntax: <code>-> [name of template to expand]</code>
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class TemplateReference extends TemplateStructure {

		private final ConfigExpression _templateName;

		/**
		 * Creates a {@link TemplateReference}.
		 * 
		 * @param templateName
		 *        See {@link #getTemplateName()}.
		 */
		public TemplateReference(TextRange range, ConfigExpression templateName) {
			super(range);
			_templateName = templateName;
		}

		/**
		 * Expression evaluating to the name of the template to expand in the current context.
		 */
		public ConfigExpression getTemplateName() {
			return _templateName;
		}

		@Override
		public <R, A, E extends Throwable> R visitStructure(TemplateStructureVisitor<R, A, E> v, A arg) throws E {
			return v.vistTemplateReference(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_templateName);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TemplateReference other = (TemplateReference) obj;
			return Objects.equals(_templateName, other._templateName);
		}
	}

	/**
	 * A literal XML/HTML tag to create in the output.
	 */
	public static class Tag extends TemplateSequence {

		private String _name;

		private final Map<String, TemplateExpression> _attributes = new LinkedHashMap<>();

		private boolean _empty;

		/**
		 * Creates a {@link Tag}.
		 */
		public Tag(TextRange range, String name) {
			super(range);
			_name = name;
		}

		/**
		 * The {@link Tag} name.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * Whether the tag is empty (cannot have {@link #getExprs() contents}).
		 */
		public boolean isEmpty() {
			return _empty;
		}

		/**
		 * @see #isEmpty()
		 */
		public void setEmpty(boolean empty) {
			_empty = empty;
		}

		/**
		 * The XML/HTML attributes of the tag.
		 */
		public Map<String, TemplateExpression> getAttributes() {
			return _attributes;
		}

		/**
		 * The value of the XML/HTML attribute with the given name.
		 * 
		 * @see #getAttributes()
		 */
		public TemplateExpression getAttribute(String name) {
			return _attributes.get(name);
		}

		/**
		 * Updates/creates an attribute with the given name and value.
		 * 
		 * @param name
		 *        The name of the attribute.
		 * @param value
		 *        The expression delivering the value of the attribute or <code>null</code> to
		 *        delete the attribute.
		 * 
		 * @return The value previously assigned to the attribute with the given name, or
		 *         <code>null</code> if the attribute did not exist before.
		 */
		public TemplateExpression setAttribute(String name, TemplateExpression value) {
			if (value == null) {
				return _attributes.remove(name);
			} else {
				return _attributes.put(name, value);
			}
		}

		@Override
		public <R, A, E extends Throwable> R visitStructure(TemplateStructureVisitor<R, A, E> v, A arg) throws E {
			return v.visitTag(this, arg);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_attributes, _empty, _name);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tag other = (Tag) obj;
			return Objects.equals(_attributes, other._attributes) && _empty == other._empty
				&& Objects.equals(_name, other._name);
		}

	}

	/**
	 * Base class for {@link TemplateExpression}s that contain a sequence of other expressions.
	 */
	public abstract static class TemplateSequence extends TemplateStructure {

		private final List<TemplateExpression> _exprs = new ArrayList<>();

		/**
		 * Creates a {@link TemplateExpression.TemplateSequence}.
		 */
		public TemplateSequence(TextRange range) {
			super(range);
		}

		/**
		 * Appends the given {@link TemplateExpression} to the {@link #getExprs()}.
		 */
		public TemplateSequence append(TemplateExpression expr) {
			if (expr instanceof Template) {
				_exprs.addAll(((Template) expr).getExprs());
			} else {
				_exprs.add(expr);
			}
			return this;
		}

		/**
		 * The templates to expand.
		 */
		public List<TemplateExpression> getExprs() {
			return _exprs;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(_exprs);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TemplateSequence other = (TemplateSequence) obj;
			return Objects.equals(_exprs, other._exprs);
		}

	}

	/**
	 * A concatenation of {@link TemplateExpression}s.
	 * 
	 * <p>
	 * Syntax: <code>{ [template-1] [template-2] ... }</code>
	 * </p>
	 * 
	 * @see TemplateExpression
	 */
	public static class Template extends TemplateSequence {

		/**
		 * Creates a {@link TemplateExpression.Template}.
		 */
		public Template(TextRange range) {
			super(range);
		}

		@Override
		public <R, A, E extends Throwable> R visitStructure(TemplateStructureVisitor<R, A, E> v, A arg) throws E {
			return v.visitTemplate(this, arg);
		}
	}

}
