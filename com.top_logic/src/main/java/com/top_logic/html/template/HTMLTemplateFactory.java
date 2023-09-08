/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.html.template.entities.Entities;
import com.top_logic.html.template.expr.AddExpression;
import com.top_logic.html.template.expr.AndExpression;
import com.top_logic.html.template.expr.DivExpression;
import com.top_logic.html.template.expr.EqExpression;
import com.top_logic.html.template.expr.GeExpression;
import com.top_logic.html.template.expr.GtExpression;
import com.top_logic.html.template.expr.LiteralExpression;
import com.top_logic.html.template.expr.ModExpression;
import com.top_logic.html.template.expr.MulExpression;
import com.top_logic.html.template.expr.NegExpression;
import com.top_logic.html.template.expr.NotExpression;
import com.top_logic.html.template.expr.NullExpression;
import com.top_logic.html.template.expr.OrExpression;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.html.template.expr.SubExpression;
import com.top_logic.html.template.expr.TestExpression;
import com.top_logic.html.template.expr.VariableExpression;
import com.top_logic.html.template.parser.HTMLTemplateParser;
import com.top_logic.html.template.parser.Token;
import com.top_logic.mig.html.HTMLConstants;

import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.server.io.ReaderAdapter;

/**
 * Factory of {@link RawTemplateFragment}s used while {@link HTMLTemplateParser parsing} templates.
 */
@FrameworkInternal
public class HTMLTemplateFactory {

	private static final Map<String, String> ENTITIES;

	static {
		Map<String, String> entityMap;
		String entitiesDefinition = "entities.json";
		try (InputStream in = Entities.class.getResourceAsStream(entitiesDefinition)) {
			if (in == null) {
				Logger.error("Cannot find HTML5 named entities table",
					new NullPointerException("No resource '" + entitiesDefinition + "' found next to class "
							+ Entities.class.getName()),
					HTMLTemplateFactory.class);
				entityMap = Collections.emptyMap();
			} else {
				JsonReader json = new JsonReader(new ReaderAdapter(new InputStreamReader(in, StandardCharsets.UTF_8)));
				json.setLenient(true);
				Entities entities = Entities.readEntities(json);
				entityMap = entities.getEntities().entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getCharacters()));
			}
		} catch (IOException ex) {
			Logger.error("Cannot read HTML5 named entities table", ex, HTMLTemplateFactory.class);
			entityMap = Collections.emptyMap();
		}
		ENTITIES = entityMap;
	}

	/**
	 * Creates a concatenated fragment.
	 */
	public RawTemplateFragment template(List<RawTemplateFragment> sequence) {
		switch (sequence.size()) {
			case 0:
				return EmptyTemplate.INSTANCE;
			case 1:
				return sequence.get(0);
			default:
				return new TemplateSequence(sequence);
		}
	}

	/**
	 * Creates a literal text fragment with trimmed white-space.
	 */
	public RawTemplateFragment text(Token text) {
		String chars = text.image;
		if (chars.isBlank()) {
			return EmptyTemplate.INSTANCE;
		}

		chars = unquote(chars);

		int start = 0;
		while (Character.isWhitespace(chars.charAt(start))) {
			start++;
		}
		int length = chars.length();
		int stop = length;
		while (Character.isWhitespace(chars.charAt(stop - 1))) {
			stop--;
		}
		if (start == 0 && stop == length) {
			return new StringLiteral(text.beginLine, text.beginColumn, chars);
		} else {
			StringBuilder buffer = new StringBuilder();
			if (start > 0) {
				buffer.append(" ");
			}
			buffer.append(chars.subSequence(start, stop));
			if (stop < length) {
				buffer.append(" ");
			}
			return new StringLiteral(text.beginLine, text.beginColumn, buffer.toString());
		}
	}

	/**
	 * Creates a literal text fragment.
	 */
	public RawTemplateFragment rawText(Token text) {
		String chars = text.image;
		chars = unquote(chars);
		return new StringLiteral(text.beginLine, text.beginColumn, chars);
	}

	private String unquote(String chars) {
		chars = chars.replaceAll("\\\\(.)", "$1");
		return chars;
	}

	/**
	 * Creates a literal text fragment from a character reference.
	 */
	public StringLiteral charRef(Token text) {
		String ref = text.image;
		int codePoint = Integer.parseInt(ref, 2, ref.length() - 1, 10);
		return new StringLiteral(text.beginLine, text.beginColumn, Character.toString(codePoint));
	}

	/**
	 * Creates a literal text fragment from a hexadecimal character reference.
	 */
	public StringLiteral charRefHex(Token text) {
		String ref = text.image;
		int codePoint = Integer.parseInt(ref, 3, ref.length() - 1, 16);
		return new StringLiteral(text.beginLine, text.beginColumn, Character.toString(codePoint));
	}

	/**
	 * Creates a literal text fragment form a HTML5 entity reference.
	 */
	public StringLiteral entityRef(Token text) {
		String ref = text.image;
		String chars = ENTITIES.get(ref);
		if (chars == null) {
			chars = ref;
		}
		return new StringLiteral(text.beginLine, text.beginColumn, chars);
	}

	/**
	 * Creates a literal string expression .
	 */
	public StringLiteral string(Token text) {
		return new StringLiteral(text.beginLine, text.beginColumn, text.image.substring(1, text.image.length() - 1));
	}

	/**
	 * Creates a start tag.
	 */
	public StartTagTemplate startTag(Token name) {
		return new StartTagTemplate(name.beginLine, name.beginColumn, name.image.substring(1));
	}

	/**
	 * Creates a wrapped start tag with a condition annotation.
	 */
	public StartTagTemplate conditionalTag(StartTagTemplate inner, TemplateExpression test) {
		return new SpecialStartTag(inner, new ConditionBuilder(test));
	}

	/**
	 * Creates a looping tag.
	 */
	public StartTagTemplate foreachTag(StartTagTemplate inner, String var, TemplateExpression expression) {
		return new SpecialStartTag(inner, new ForeachBuilder(var, expression));
	}

	/**
	 * Creates a tag attribute.
	 */
	public TagAttributeTemplate attribute(Token nameToken, List<RawTemplateFragment> content) {
		String name = nameToken.image;
		if (HTMLConstants.CLASS_ATTR.equals(name)) {
			return new CssTagAttributeTemplate(nameToken.beginLine, nameToken.beginColumn, name, template(content));
		} else {
			return new TagAttributeTemplate(nameToken.beginLine, nameToken.beginColumn, name, template(content));
		}
	}

	/**
	 * Creates an end tag.
	 */
	public EndTagTemplate endTag(Token name) {
		return new EndTagTemplate(name.beginLine, name.beginColumn, name.image.substring(2, name.image.length() - 1));
	}

	/**
	 * Creates a variable access expression.
	 */
	public VariableExpression variable(Token name) {
		return new VariableExpression(name.beginLine, name.beginColumn, name.image);
	}

	/**
	 * Creates a conditional template that evaluates to the empty fragment, if the condition does
	 * not hold.
	 */
	public TemplateExpression ifThenExpression(TemplateExpression test, TemplateExpression thenExpression) {
		return ifThenExpression(test, thenExpression, null);
	}

	/**
	 * Creates a ternary operator expression.
	 */
	public TemplateExpression ifThenExpression(TemplateExpression test, TemplateExpression thenExpression,
			TemplateExpression elseExpression) {
		if (elseExpression == null) {
			elseExpression = NullExpression.INSTANCE;
		}

		return new TestExpression(test, thenExpression, elseExpression);
	}

	/**
	 * Creates an if tag.
	 */
	public ConditionalTemplate ifTag(TemplateExpression test, RawTemplateFragment content) {
		return ifTag(test, content, EmptyTemplate.INSTANCE);
	}

	/**
	 * Creates an if tag with else branch.
	 */
	public ConditionalTemplate ifTag(TemplateExpression test, RawTemplateFragment content,
			RawTemplateFragment elseContent) {
		return new ConditionalTemplate(test, content, elseContent);
	}

	/**
	 * Creates a foreach loop tag.
	 *
	 * @param var
	 *        The local variable to define.
	 * @param expression
	 *        The expression computing a list of elements to iterate.
	 * @param content
	 *        The content template to evaluate for each element.
	 */
	public RawTemplateFragment foreachTag(String var, TemplateExpression expression, RawTemplateFragment content) {
		return new ForeachTemplate(var, expression, content);
	}

	/**
	 * Creates a variable binding tag.
	 */
	public RawTemplateFragment withTag(String var, TemplateExpression expression, RawTemplateFragment content) {
		return new DefineTemplate(var, expression, content);
	}

	/**
	 * Creates a boolean <code>and</code> operator.
	 */
	public TemplateExpression and(TemplateExpression leftExpression, TemplateExpression rightExpression) {
		return new AndExpression(leftExpression, rightExpression);
	}

	/**
	 * Creates a boolean <code>or</code> operator.
	 */
	public TemplateExpression or(TemplateExpression leftExpression, TemplateExpression rightExpression) {
		return new OrExpression(leftExpression, rightExpression);
	}

	/**
	 * Creates a boolean <code>not</code> operator.
	 */
	public TemplateExpression not(TemplateExpression expression) {
		return new NotExpression(expression);
	}

	/**
	 * Creates a builder for a sequence of {@link RawTemplateFragment}s
	 */
	public List<RawTemplateFragment> builder() {
		return new ArrayList<>();
	}

	/**
	 * Marks the given expression as being directly rendered.
	 */
	public RawTemplateFragment renderExpression(TemplateExpression parsedExpression) {
		return parsedExpression.toFragment();
	}

	/**
	 * Creates a <code>div</code> expression.
	 */
	public TemplateExpression div(TemplateExpression left, TemplateExpression right) {
		return new DivExpression(left, right);
	}

	/**
	 * Creates an equals check expression.
	 */
	public TemplateExpression eq(TemplateExpression left, TemplateExpression right) {
		return new EqExpression(left, right);
	}

	/**
	 * Creates a not-equals test.
	 */
	public TemplateExpression neq(TemplateExpression left, TemplateExpression right) {
		return not(eq(left, right));
	}

	/**
	 * Creates a greater-or-equal comparison.
	 */
	public TemplateExpression ge(TemplateExpression left, TemplateExpression right) {
		return new GeExpression(left, right);
	}

	/**
	 * Creates a greater comparison.
	 */
	public TemplateExpression gt(TemplateExpression left, TemplateExpression right) {
		return new GtExpression(left, right);
	}

	/**
	 * Creates a lower-or-equal comparison.
	 */
	public TemplateExpression le(TemplateExpression left, TemplateExpression right) {
		return not(gt(left, right));
	}

	/**
	 * Creates a lower-than comparison.
	 */
	public TemplateExpression lt(TemplateExpression left, TemplateExpression right) {
		return not(ge(left, right));
	}

	/**
	 * Creates an addition.
	 */
	public TemplateExpression add(TemplateExpression left, TemplateExpression right) {
		return new AddExpression(left, right);
	}

	/**
	 * Creates a subtraction.
	 */
	public TemplateExpression sub(TemplateExpression left, TemplateExpression right) {
		return new SubExpression(left, right);
	}

	/**
	 * Creates a multiplication.
	 */
	public TemplateExpression mul(TemplateExpression left, TemplateExpression right) {
		return new MulExpression(left, right);
	}

	/**
	 * Creates a modulo operation.
	 */
	public TemplateExpression mod(TemplateExpression left, TemplateExpression right) {
		return new ModExpression(left, right);
	}

	/**
	 * Creates a negation.
	 */
	public TemplateExpression neg(TemplateExpression expr) {
		return new NegExpression(expr);
	}

	/**
	 * Creates a boolean <code>true</code> or <code>false</code>.
	 * 
	 * @param token
	 *        The parser token passing the location of the literal.
	 */
	public TemplateExpression boolenLiteral(Token token, boolean value) {
		return new LiteralExpression(token.beginLine, token.beginColumn, Boolean.valueOf(value));
	}

	/**
	 * Creates a <code>null</code> literal.
	 * 
	 * @param token
	 *        The parser token passing the location of the literal.
	 */
	public TemplateExpression nullLiteral(Token token) {
		return NullExpression.INSTANCE;
	}

	/**
	 * Creates a number literal.
	 */
	public TemplateExpression numberLiteral(Token token) {
		String num = token.image;
		boolean isDouble = num.indexOf('.') >= 0;
		return new LiteralExpression(token.beginLine, token.beginColumn, isDouble
			? (Object) Double.valueOf(Double.parseDouble(num))
			: (Object) Long.valueOf(Long.parseLong(num)));
	}

}
