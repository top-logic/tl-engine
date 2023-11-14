/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.LocationImpl;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.AttributeChecker;
import com.top_logic.basic.html.HTMLChecker;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.search.expr.CompareKind;
import com.top_logic.model.search.expr.config.ExprPrinter;
import com.top_logic.model.search.expr.config.dom.Expr.Add;
import com.top_logic.model.search.expr.config.dom.Expr.And;
import com.top_logic.model.search.expr.config.dom.Expr.Apply;
import com.top_logic.model.search.expr.config.dom.Expr.Arg;
import com.top_logic.model.search.expr.config.dom.Expr.Args;
import com.top_logic.model.search.expr.config.dom.Expr.Assign;
import com.top_logic.model.search.expr.config.dom.Expr.At;
import com.top_logic.model.search.expr.config.dom.Expr.Attribute;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContent;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContents;
import com.top_logic.model.search.expr.config.dom.Expr.Block;
import com.top_logic.model.search.expr.config.dom.Expr.Cmp;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Div;
import com.top_logic.model.search.expr.config.dom.Expr.EmbeddedExpression;
import com.top_logic.model.search.expr.config.dom.Expr.EndTag;
import com.top_logic.model.search.expr.config.dom.Expr.Eq;
import com.top_logic.model.search.expr.config.dom.Expr.False;
import com.top_logic.model.search.expr.config.dom.Expr.Html;
import com.top_logic.model.search.expr.config.dom.Expr.HtmlContent;
import com.top_logic.model.search.expr.config.dom.Expr.Method;
import com.top_logic.model.search.expr.config.dom.Expr.Mod;
import com.top_logic.model.search.expr.config.dom.Expr.ModuleLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Mul;
import com.top_logic.model.search.expr.config.dom.Expr.Not;
import com.top_logic.model.search.expr.config.dom.Expr.Null;
import com.top_logic.model.search.expr.config.dom.Expr.NumberLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Operation;
import com.top_logic.model.search.expr.config.dom.Expr.Or;
import com.top_logic.model.search.expr.config.dom.Expr.PartLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.QName;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyLiteral.LangStringConfig;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyReference;
import com.top_logic.model.search.expr.config.dom.Expr.SingletonLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.StartTag;
import com.top_logic.model.search.expr.config.dom.Expr.StringLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Sub;
import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.Expr.True;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple.Coord;
import com.top_logic.model.search.expr.config.dom.Expr.TypeLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Var;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.Token;
import com.top_logic.util.error.TopLogicException;

/**
 * Factory methods for {@link Expr} instances.
 * 
 * <p>
 * Facade used by the {@link SearchExpressionParser}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class ExprFactory {

	private SearchExpressionParser _parser;

	/**
	 * Creates a {@link ExprFactory}.
	 *
	 */
	public ExprFactory(SearchExpressionParser parser) {
		_parser = parser;
	}

	/** @see Null */
	public Expr nullLiteral() {
		return node(Null.class);
	}

	/** @see True */
	public Expr trueLiteral() {
		return node(True.class);
	}

	/** @see False */
	public Expr falseLiteral() {
		return node(False.class);
	}

	/** @see And */
	public Expr and(Expr left, Expr right) {
		if (left instanceof And) {
			((And) left).getOperands().add(right);
			return left;
		}
		And result = node(And.class);
		List<Expr> operands = result.getOperands();
		operands.add(left);
		operands.add(right);
		return result;
	}

	/** @see Or */
	public Expr or(Expr left, Expr right) {
		if (left instanceof Or) {
			((Or) left).getOperands().add(right);
			return left;
		}
		Or result = node(Or.class);
		List<Expr> operands = result.getOperands();
		operands.add(left);
		operands.add(right);
		return result;
	}

	/** @see Not */
	public Expr not(Expr expr) {
		Not result = node(Not.class);
		result.setExpr(expr);
		return result;
	}

	/** Creates a negated expression */
	public Expr neg(Expr expr) {
		if (expr instanceof NumberLiteral) {
			return numberLiteral(-((NumberLiteral) expr).getValue());
		} else {
			Sub result = node(Sub.class);
			List<Expr> operands = result.getOperands();
			operands.add(numberLiteral(0));
			operands.add(expr);
			return result;
		}
	}

	/**
	 * Creates an if-then-else expression.
	 */
	public Expr ifExpr(Expr test, Expr thenExpr, Expr elseExpr) {
		return staticMethod("if", args(test, thenExpr, elseExpr));
	}

	/** @see Eq */
	public Expr eq(Expr left, Expr right) {
		Eq result = node(Eq.class);
		List<Expr> operands = result.getOperands();
		operands.add(left);
		operands.add(right);
		return result;
	}

	/**
	 * @see Not
	 * @see Eq
	 */
	public Expr neq(Expr left, Expr right) {
		return not(eq(left, right));
	}

	/** @see Add */
	public Expr add(Expr left, Expr right) {
		return op(Add.class, left, right);
	}

	/** @see Sub */
	public Expr sub(Expr left, Expr right) {
		return op(Sub.class, left, right);
	}

	/** @see Mul */
	public Expr mul(Expr left, Expr right) {
		return op(Mul.class, left, right);
	}

	/** @see Div */
	public Expr div(Expr left, Expr right) {
		return op(Div.class, left, right);
	}

	/** @see Mod */
	public Expr mod(Expr left, Expr right) {
		return op(Mod.class, left, right);
	}

	/** @see Cmp */
	public Expr gt(Expr left, Expr right) {
		return cmp(CompareKind.GT, left, right);
	}

	/** @see Cmp */
	public Expr ge(Expr left, Expr right) {
		return cmp(CompareKind.GE, left, right);
	}

	/** @see Cmp */
	public Expr lt(Expr left, Expr right) {
		return cmp(CompareKind.LT, left, right);
	}

	/** @see Cmp */
	public Expr le(Expr left, Expr right) {
		return cmp(CompareKind.LE, left, right);
	}

	private Cmp cmp(CompareKind kind, Expr left, Expr right) {
		Cmp result = op(Cmp.class, left, right);
		result.setKind(kind);
		return result;
	}

	/** @see StringLiteral */
	public Expr stringLiteral(String value) {
		StringLiteral result = node(StringLiteral.class);
		result.setValue(unQuoteString(value));
		return result;
	}

	/** @see Html */
	public Html html() {
		return node(Html.class);
	}

	/**
	 * Transforms the given {@link Html} so that it is well-formed and does not contain unsafe
	 * content.
	 */
	public Html checkHtml(Html html) {
		if (!SafeHTML.Module.INSTANCE.isActive()) {
			/* This may happen e.g. during data migration, but not in normal application mode.
			 * Therefore the HTML is not given by user and is regarded as safe. */
			Logger.warn(
				"Module " + SafeHTML.class.getSimpleName() + " not started. The given HTML is not checked: "
					+ ExprPrinter.toString(html),
				ExprFactory.class);
			return html;
		}
		List<HtmlContent> safeContent = new ArrayList<>();
		Stack<String> opened = new Stack<>();
		HTMLChecker safeHtml = SafeHTML.getInstance();
		for (HtmlContent content : html.getContents()) {
			try {
				checkContent(safeHtml, opened, content, safeContent);
			} catch (I18NException ex) {
				throw wrap(content, ex);
			}
		}
		while (!opened.isEmpty()) {
			safeContent.add(syntheticEndTag(opened.pop()));
		}
		html.getContents().clear();
		html.getContents().addAll(safeContent);
		return html;
	}

	private TopLogicException wrap(ConfigurationItem node, I18NException ex) {
		ResKey errorKey = ex.getErrorKey();
		ResKey errorKeyWithLocation;
		if (Location.hasLocation(errorKey)) {
			errorKeyWithLocation = errorKey;
		} else {
			errorKeyWithLocation = node.location().withLocation(errorKey);
		}
		throw new TopLogicException(errorKeyWithLocation, ex).initSeverity(ErrorSeverity.WARNING);
	}

	private void checkContent(HTMLChecker safeHtml, Stack<String> opened, HtmlContent content,
			List<HtmlContent> safeContent) throws I18NException {
		if (content instanceof StartTag) {
			StartTag start = (StartTag) content;
			String tag = start.getTag();
			if (!isTLScriptTag(start, tag)) {
				safeHtml.checkTag(tag);
			}
			
			for (Iterator<Attribute> attributes = start.getAttributes().iterator(); attributes.hasNext(); ) {
				Attribute attribute = attributes.next();
				checkAttribute(safeHtml, tag, attribute);
			}
			
			if (start.isEmpty()) {
				if (HTMLConstants.VOID_ELEMENTS.contains(tag)) {
					safeContent.add(content);
				} else {
					start.reset(start.descriptor().getProperty(StartTag.EMPTY));
					safeContent.add(content);
					safeContent.add(syntheticEndTag(tag));
				}
			} else {
				if (HTMLConstants.VOID_ELEMENTS.contains(tag)) {
					start.setEmpty(true);
				} else {
					opened.push(tag);
				}
				safeContent.add(content);
			}
		} else if (content instanceof EndTag) {
			String tag = ((EndTag) content).getTag();

			if (!HTMLConstants.VOID_ELEMENTS.contains(tag)) {
				checkEndTag:
				{
					while (!opened.isEmpty()) {
						String last = opened.pop();
						if (last.equals(tag)) {
							// Matching opening tag found, keep end tag.
							safeContent.add(content);
							break checkEndTag;
						} else {
							safeContent.add(syntheticEndTag(last));
						}
					}

					throw new TopLogicException(
						content.location().withLocation(I18NConstants.ERROR_NO_MATCHING_START_TAG__NAME.fill(tag)));
				}
			}
		} else {
			safeContent.add(content);
		}
	}

	private boolean isTLScriptTag(StartTag start, String tag) {
		if (!SafeHTML.SCRIPT_TAG.equals(tag)) {
			return false;
		}
		
		AttributeContent typeValue = start.getAttribute(SafeHTML.TYPE_ATTR);
		if (typeValue == null) {
			return false;
		}
		
		if (!(typeValue instanceof TextContent)) {
			// Not a literal value.
			return false;
		}

		String value = ((TextContent) typeValue).getValue();
		return SafeHTML.TEXT_TLSSCRIPT_TYPE.equals(value);
	}

	private void checkAttribute(HTMLChecker safeHtml, String tag, Attribute attribute) {
		String attributeName = attribute.getName();

		try {
			safeHtml.checkAttributeName(tag, attributeName);
		} catch (I18NException ex) {
			throw wrap(attribute, ex);
		}

		AttributeChecker checker = safeHtml.getAttributeChecker(attributeName);
		if (checker != null) {
			AttributeContent value = attribute.getValue();
			try {
				checkAttributeValue(checker, value);
			} catch (I18NException ex) {
				throw wrap(value, ex);
			}
		}
	}

	private void checkAttributeValue(AttributeChecker checker, AttributeContent value) throws I18NException {
		if (value instanceof TextContent) {
			checker.check(((TextContent) value).getValue());
		} else {
			// Writing of attribute will be checked dynamically.
		}
	}

	/** @see TextContent */
	public TextContent textContent(String text) {
		TextContent result = node(TextContent.class);
		result.setValue(unQuoteHtmlText(text));
		return result;
	}

	/**
	 * Creates a {@link StringLiteral} from a text part of a text with embedded expressions.
	 */
	public StringLiteral plainText(String text) {
		StringLiteral result = node(StringLiteral.class);
		result.setValue(unQuoteHtmlText(text));
		return result;
	}

	/**
	 * Creates a string concatenation of the evaluation of the given expression parts.
	 */
	public Expr textContent(List<Expr> parts) {
		return staticMethod("toString", args(parts));
	}

	/**
	 * Removes `\` quoting characters from HTML text content.
	 */
	private String unQuoteHtmlText(String text) {
		int quoteIndex = text.indexOf('\\');
		if (quoteIndex < 0) {
			return text;
		}

		int last = 0;
		StringBuilder result = new StringBuilder();
		while (true) {
			result.append(text.subSequence(last, quoteIndex));

			int quotedIndex = quoteIndex + 1;
			if (quotedIndex < text.length()) {
				result.append(text.charAt(quotedIndex));
			} else {
				// Backslash at the end without character to quote.
				result.append('\\');
				break;
			}

			last = quotedIndex + 1;
			quoteIndex = text.indexOf('\\', last);
			if (quoteIndex < 0) {
				result.append(text.subSequence(last, text.length()));
				break;
			}
		}
		return result.toString();
	}

	/** @see StartTag */
	public StartTag startTag(String name) {
		StartTag result = node(StartTag.class);
		result.setTag(name.substring(1));
		return result;
	}

	/** @see EndTag */
	public EndTag endTag(String name) {
		return syntheticEndTag(name.substring(2, name.length() - 1));
	}

	private EndTag syntheticEndTag(String tag) {
		EndTag result = node(EndTag.class);
		result.setTag(tag);
		return result;
	}

	/** @see EmbeddedExpression */
	public EmbeddedExpression embeddedExpr(Expr expr) {
		EmbeddedExpression result = node(EmbeddedExpression.class);
		result.setExpr(expr);
		return result;
	}

	/** @see Attribute */
	public Attribute attribute(String name, AttributeContent value) {
		Attribute result = node(Attribute.class);
		result.setName(name);
		result.setValue(value);
		return result;
	}

	/** @see AttributeContents */
	public AttributeContents concat() {
		AttributeContents result = node(AttributeContents.class);
		return result;
	}

	/** @see ResKeyReference */
	public Expr reskeyLiteral(String value) {
		ResKeyReference result = node(ResKeyReference.class);
		result.setKey(unQuoteString(value.substring(1)));
		return result;
	}

	/** A floating point value. */
	public Expr doubleLiteral(String value) {
		return numberLiteral(Double.parseDouble(value));
	}

	/** An integer value. */
	public Expr longLiteral(String value) {
		return numberLiteral(Long.parseLong(value.replace("_", "")));
	}

	private Expr numberLiteral(double num) {
		NumberLiteral result = node(NumberLiteral.class);
		result.setValue(num);
		return result;
	}

	/** @see Var */
	public Expr var(String name) {
		return internalVar(name.substring(1));
	}

	final Expr internalVar(String varName) {
		Var result = node(Var.class);
		result.setName(varName);
		return result;
	}

	/** @see ModuleLiteral */
	public Expr moduleLiteral(String token) {
		assertModelLiteral(token);

		ModuleLiteral result = node(ModuleLiteral.class);
		result.setName(modelLiteralContents(token));
		return result;
	}

	private String modelLiteralContents(String token) {
		return token.substring(1, token.length() - 1);
	}

	private void assertModelLiteral(String token) {
		assert token.startsWith("`");
		assert token.endsWith("`");
	}

	/** @see SingletonLiteral */
	public Expr singletonLiteral(String token) {
		assertModelLiteral(token);
		SingletonLiteral result = node(SingletonLiteral.class);
		int partSepIdx = partSepIdx(token);

		result.setModule(token.substring(1, partSepIdx));
		result.setName(token.substring(partSepIdx + 1, token.length() - 1));
		return result;
	}

	private int partSepIdx(String token) {
		int result = token.indexOf('#');
		assert result >= 0;
		return result;
	}

	/** @see TypeLiteral */
	public Expr typeLiteral(String token) {
		assertModelLiteral(token);

		int typeSepIndex = typeSepIdx(token);

		TypeLiteral result = node(TypeLiteral.class);
		result.setModule(token.substring(1, typeSepIndex));
		result.setName(token.substring(typeSepIndex + 1, token.length() - 1));
		return result;
	}

	private int typeSepIdx(String token) {
		int result = token.indexOf(':');
		assert result >= 0;
		return result;
	}

	/** @see PartLiteral */
	public Expr partLiteral(String token) {
		assertModelLiteral(token);

		int typeSepIndex = typeSepIdx(token);
		int partSepIndex = partSepIdx(token);

		PartLiteral result = node(PartLiteral.class);
		result.setModule(token.substring(1, typeSepIndex));
		result.setType(token.substring(typeSepIndex + 1, partSepIndex));
		result.setName(token.substring(partSepIndex + 1, token.length() - 1));
		return result;
	}
	
	/** @see Apply */
	public Expr apply(Expr fun, Expr arg) {
		Apply result = node(Apply.class);
		result.setFun(fun);
		result.setArg(arg);
		return result;
	}

	/** @see Define */
	public Expr lambda(String var, Expr expr) {
		Define result = node(Define.class);
		result.setName(var);
		result.setExpr(expr);
		return result;
	}

	/** @see Assign */
	public Expr assign(String var, Expr expr) {
		Assign result = node(Assign.class);
		result.setName(var);
		result.setExpr(expr);
		return result;
	}

	/**
	 * @see At
	 */
	public Expr at(Expr self, Expr index) {
		At result = node(At.class);
		result.setSelf(self);
		result.setIndex(index);
		return result;
	}

	/** @see Tuple */
	public Expr tuple(List<Coord> coords) {
		Tuple result = node(Tuple.class);
		result.getCoords().addAll(coords);
		return result;
	}

	/** @see Coord */
	public Coord coord(String name, boolean optional, Expr expr) {
		Coord result = node(Coord.class);
		result.setName(name);
		result.setOptional(optional);
		result.setExpr(expr);
		return result;
	}

	/** @see Method */
	public Expr method(String name, Expr self, List<Arg> args) {
		Method result = method(name, self);
		result.getArgs().addAll(args);
		return result;
	}

	/** @see Method */
	public Method method(String name, Expr self) {
		Method result = node(Method.class);
		result.setName(name);
		if (self != null) {
			result.getArgs().add(arg(self));
		}
		return result;
	}

	/** @see Method */
	public Expr staticMethod(String name, List<Arg> args) {
		Method result = node(Method.class);
		result.setName(name);
		result.setArgs(args);
		return result;
	}

	/**
	 * @see Block
	 */
	public Expr block(List<Expr> contents) {
		if (contents.isEmpty()) {
			return nullLiteral();
		}
		if (contents.size() == 1) {
			return contents.get(0);
		}

		Block result = node(Block.class);
		result.setContents(contents);
		return result;
	}

	/**
	 * Creates a list constructor from an expression list.
	 */
	public Expr listConstructor(List<Expr> contents) {
		return staticMethod("list", args(contents));
	}

	/**
	 * Creates a dictionary builder.
	 */
	public DictBuilder dictBuilder() {
		return new DictBuilder();
	}

	/**
	 * Builder creating a dictionary constructor.
	 */
	public class DictBuilder {

		private final List<Expr> _keys = new ArrayList<>();

		private final List<Expr> _values = new ArrayList<>();

		/**
		 * Adds a key/value expression parir.
		 */
		public void put(Expr key, Expr value) {
			_keys.add(key);
			_values.add(value);
		}

		/**
		 * Creates the final expression.
		 */
		public Expr toExpr() {
			return method("newStruct", staticMethod("structType", args(_keys)), args(_values));
		}
	}

	/**
	 * Temporary builder for parsing switch blocks.
	 */
	public class SwitchBuilder {
		class Case {
			private final Expr test;

			private final Expr result;

			/**
			 * Creates a {@link Case}.
			 */
			public Case(Expr test, Expr result) {
				this.test = test;
				this.result = result;
			}

			/**
			 * The test expression that guards the {@link #getResult()}.
			 */
			public Expr getTest() {
				return test;
			}

			/**
			 * The expression to evaluate, if {@link #getTest()} evaluates to true.
			 */
			public Expr getResult() {
				return result;
			}
		}

		private Expr _value;

		private final List<Case> _cases = new ArrayList<>();

		private Expr _defaultResult;

		/**
		 * Sets value to compare with the test expressions.
		 */
		public void setValue(Expr value) {
			_value = value;
		}

		/**
		 * Adds a case branch to a {@link SwitchBuilder}.
		 */
		public void addCase(Expr test, Expr result) {
			_cases.add(new Case(test, result));
		}

		/**
		 * Sets the default result of a {@link SwitchBuilder}.
		 */
		public void setDefault(Expr defaultResult) {
			_defaultResult = defaultResult;
		}

		/**
		 * Creates the resulting expression.
		 */
		public Expr toExpr() {
			String var = "<synthetic>";

			Expr result = _defaultResult == null ? nullLiteral() : _defaultResult;

			for (int n = _cases.size() - 1; n >= 0; n--) {
				Case current = _cases.get(n);
				Args args = args();
				args.add(arg(current.getResult()));
				args.add(arg(result));
				Expr test;
				if (_value == null) {
					test = current.getTest();
				} else {
					test = eq(internalVar(var), current.getTest());
				}
				result = method("ifElse", test, args);
			}

			if (_value == null) {
				return result;
			} else {
				Expr lambda = lambda(var, result);
				return apply(lambda, _value);
			}
		}

	}

	/**
	 * Creates a {@link SwitchBuilder}.
	 */
	public SwitchBuilder switchBlock() {
		return new SwitchBuilder();
	}

	/**
	 * Creates a list buffer for parsing multiple {@link Expr}s.
	 */
	public List<Expr> list() {
		return new ArrayList<>();
	}

	/**
	 * Creates a list buffer for parsing multiple {@link Coord}s.
	 */
	public List<Expr.Tuple.Coord> coords() {
		return new ArrayList<>();
	}

	/** @see Args */
	public Args args() {
		return new Args();
	}

	/** @see Args */
	public Args args(Expr... exprs) {
		Args result = args();
		for (Expr expr : exprs) {
			addArg(result, null, expr);
		}
		return result;
	}

	/** @see QName */
	public QName qname() {
		return new QName();
	}

	/**
	 * Adds the given expression to the given {@link Args} list.
	 * 
	 * @param name
	 *        The name of the parameter that is bound. A value of <code>null</code> means that a
	 *        positional argument was given.
	 */
	public Args addArg(Args result, String name, Expr expr) {
		result.add(arg(name, expr));
		return result;
	}

	private List<Arg> args(List<Expr> parts) {
		ArrayList<Arg> result = new ArrayList<>(parts.size());
		for (Expr expr : parts) {
			result.add(arg(expr));
		}
		return result;
	}

	private Arg arg(Expr expr) {
		return arg(null, expr);
	}

	private Arg arg(String name, Expr expr) {
		Arg result = TypedConfiguration.newConfigItem(Arg.class);
		result.setName(name);
		result.setValue(expr);
		return result;
	}

	private <T extends Operation> T op(Class<T> type, Expr left, Expr right) {
		T result = node(type);
		List<Expr> operands = result.getOperands();
		operands.add(left);
		operands.add(right);
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T extends ConfigurationItem> T node(Class<T> type) {
		Token token = _parser.token;
		Location location = LocationImpl.location(null, token.beginLine, token.beginColumn);
		return (T) TypedConfiguration.getConfigurationDescriptor(type).factory().createNew(location);
	}

	/**
	 * A builder for {@link ResKeyLiteral}s.
	 */
	public I18NBuilder i18nBuilder() {
		return new I18NBuilder();
	}

	/**
	 * Builder creating a {@link ResKeyLiteral} expression.
	 * 
	 * @see ExprFactory#i18nBuilder() Factory for creating a builder.
	 * @see #build() Creating the final literal expression.
	 */
	public static class I18NBuilder {
	
		private Expr.ResKeyLiteral _value = TypedConfiguration.newConfigItem(Expr.ResKeyLiteral.class);

		private Map<String, I18NBuilder> _suffixBuilder = new HashMap<>();
	
		/**
		 * Adds a language tagged string
		 */
		public I18NBuilder add(String value, String lang) {
			Locale locale = Locale.forLanguageTag(lang.substring(1));
			LangStringConfig string =
				TypedConfiguration.newConfigItem(Expr.ResKeyLiteral.LangStringConfig.class);
			string.setText(unQuoteString(value));
			string.setLang(locale);
			_value.getValues().put(locale, string);
			return this;
		}
	
		/**
		 * Creates the result.
		 */
		public Expr.ResKeyLiteral build() {
			for (Entry<String, I18NBuilder> entry : _suffixBuilder.entrySet()) {
				ResKeyLiteral suffixLiteral = entry.getValue().build();
				suffixLiteral.setKey(entry.getKey());
				_value.getSuffixes().put(suffixLiteral.getKey(), suffixLiteral);
			}
			return _value;
		}

		/**
		 * Creates an {@link I18NBuilder} for a suffix {@link ResKeyLiteral} with the given suffix.
		 * 
		 * @param suffix
		 *        Name of the suffix to get builder for.
		 */
		public I18NBuilder suffix(String suffix) {
			I18NBuilder suffixBuilder = _suffixBuilder.get(suffix);
			if (suffixBuilder == null) {
				suffixBuilder = new I18NBuilder();
				_suffixBuilder.put(suffix, suffixBuilder);
			}
			return suffixBuilder;
		}
	
	}

	static String unQuoteString(String value) {
		return value.substring(1, value.length() - 1).replaceAll("\\\\(.)", "$1");
	}

}
