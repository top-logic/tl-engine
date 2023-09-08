/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.config.template.TemplateExpression.Alternative;
import com.top_logic.basic.config.template.TemplateExpression.Choice;
import com.top_logic.basic.config.template.TemplateExpression.CollectionAccess;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.Foreach;
import com.top_logic.basic.config.template.TemplateExpression.FunctionCall;
import com.top_logic.basic.config.template.TemplateExpression.LiteralInt;
import com.top_logic.basic.config.template.TemplateExpression.LiteralText;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;
import com.top_logic.basic.config.template.TemplateExpression.SelfAccess;
import com.top_logic.basic.config.template.TemplateExpression.Tag;
import com.top_logic.basic.config.template.TemplateExpression.Template;
import com.top_logic.basic.config.template.TemplateExpression.TemplateReference;
import com.top_logic.basic.config.template.TemplateExpression.VariableAccess;
import com.top_logic.basic.config.template.parser.ParseException;
import com.top_logic.basic.config.template.parser.Token;

/**
 * Collection of factory method for {@link TemplateExpression}s.
 * 
 * <p>
 * Used as facade of the {@link TemplateExpression} hierarchy for the generated parser code.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExprFactory {

	/**
	 * Creates a {@link TemplateReference}.
	 */
	public static TemplateReference reference(Token ref, ConfigExpression templateName) {
		return new TemplateReference(range(ref, templateName), templateName);
	}

	static TextRange range(Token start, TemplateExpression stop) {
		return range(start, stop.getRange());
	}

	static TextRange range(Token start, TextRange stop) {
		return range(start.beginLine, start.beginColumn, stop.endLine(), stop.endColumn());
	}

	static TextRange range(Token token) {
		return range(token.beginLine, token.beginColumn, token.endLine, token.endColumn);
	}

	static TextRange range(Token start, Token stop) {
		return range(start.beginLine, start.beginColumn, stop.endLine, stop.endColumn);
	}

	static TextRange range(TemplateExpression start, Token stop) {
		return range(start.getRange(), stop);
	}

	static TextRange range(TemplateExpression start, TemplateExpression stop) {
		return range(start.getRange(), stop.getRange());
	}

	static TextRange range(TextRange start, TextRange stop) {
		return range(start.beginLine(), start.beginColumn(), stop.endLine(), stop.endColumn());
	}

	static TextRange rangeCopy(TemplateExpression expr) {
		return range(expr.getRange(), expr.getRange());
	}

	static TextRange range(TextRange start, Token stop) {
		return range(start.beginLine(), start.beginColumn(), stop.endLine, stop.endColumn);
	}

	static TextRange rangeStart(Token token) {
		return range(token.beginLine, token.beginColumn, token.beginLine, token.beginColumn);
	}

	static TextRange range(int beginLine, int beginColumn, int endLine, int endColumn) {
		return new TextRange(beginLine, beginColumn, endLine, endColumn);
	}

	/**
	 * Creates a {@link LiteralText}.
	 */
	public static LiteralText literalText(Token text) {
		return literalTextQuoted(range(text), text.image);
	}

	/**
	 * Creates a {@link LiteralText}.
	 */
	public static LiteralText stringLiteral(Token literal) {
		String image = literal.image;
		return literalTextQuoted(range(literal), image.substring(1, image.length() - 1));
	}

	/**
	 * Creates a {@link LiteralText}.
	 */
	public static LiteralText literalTextQuoted(TextRange range, String text) {
		return literalTextDirect(range, text.replaceAll("!(.)", "$1"));
	}

	/**
	 * Creates a {@link LiteralText}.
	 */
	public static LiteralText literalTextDirect(TextRange range, String text) {
		return new LiteralText(range, text);
	}

	/**
	 * Creates a {@link LiteralInt}.
	 */
	public static LiteralInt numberLiteral(Token value) {
		return new LiteralInt(range(value), Integer.parseInt(value.image));
	}

	/**
	 * Creates a {@link SelfAccess}.
	 */
	public static SelfAccess self(Token token) {
		return self(range(token));
	}

	private static SelfAccess self(TextRange range) {
		return new SelfAccess(range);
	}

	/**
	 * Creates a {@link PropertyAccess}.
	 */
	public static PropertyAccess propertyAccess(Token propertyName) throws ParseException {
		return propertyAccess(self(rangeStart(propertyName)), propertyName);
	}

	/**
	 * Creates a {@link PropertyAccess}.
	 */
	public static PropertyAccess propertyAccess(TemplateExpression expr, Token propertyName) throws ParseException {
		return new PropertyAccess(range(expr, propertyName), toExpression(expr), propertyName.image);
	}

	/**
	 * Creates a {@link VariableAccess}.
	 */
	public static VariableAccess variable(Token name) {
		return new VariableAccess(range(name), name.image.substring(1));
	}

	/**
	 * Creates a {@link FunctionCall}.
	 */
	public static FunctionCall function(Token name, List<ConfigExpression> arguments, Token end) {
		return new FunctionCall(range(name, end), name.image.substring(1), arguments);
	}

	/**
	 * Creates a {@link CollectionAccess}.
	 */
	public static CollectionAccess collectionAccess(ConfigExpression expr, ConfigExpression index, Token end) {
		return new CollectionAccess(range(expr, end), expr, index);
	}

	private static ConfigExpression toExpression(TemplateExpression template) throws ParseException {
		if (!(template instanceof ConfigExpression)) {
			throw new ParseException("Expression expected, found '" + template + "'" + location(template) + ".");
		}
		return (ConfigExpression) template;
	}

	/**
	 * Creates a {@link Choice}.
	 */
	public static Choice choice(TemplateExpression expr, TemplateExpression positive,
			TemplateExpression negative) throws ParseException {
		return new Choice(range(expr, negative != null ? negative : positive), toExpression(expr), positive, negative);
	}

	/**
	 * Creates an {@link Alternative}.
	 */
	public static Alternative alternative(TemplateExpression expr, TemplateExpression alternative)
			throws ParseException {
		return new Alternative(range(expr, alternative), toExpression(expr), alternative);
	}

	/**
	 * Creates a {@link Foreach}.
	 */
	public static Foreach foreach(Token open, Token local, TemplateExpression collection,
			TemplateExpression separator,
			TemplateExpression iterator,
			TemplateExpression start, TemplateExpression stop, Token close) throws ParseException {
		return new Foreach(range(open, close),
			local != null ? local.image : null,
			toExpression(collection),
			iterator == null ? self(collection.getRange()) : iterator,
			optionalTempate(separator),
			optionalTempate(start),
			optionalTempate(stop));
	}

	private static TemplateExpression optionalTempate(TemplateExpression stop) {
		return stop == null ? new LiteralText(TextRange.UNDEFINED, "") : stop;
	}

	/**
	 * Creates a list.
	 */
	public static List<ConfigExpression> expressions() {
		return new ArrayList<>();
	}

	/**
	 * Creates a new {@link Tag} expression.
	 *
	 * @param tagName
	 *        The tag name, see {@link Tag#getName()}
	 */
	public static Tag startTag(Token tagName) {
		return new Tag(range(tagName), tagName.image.substring(1));
	}

	/**
	 * Assigns the attribute with the given name and value to the given {@link Tag}.
	 * 
	 * @throws ParseException
	 *         If an attribute with the same name was already defined.
	 */
	public static void setAttribute(Tag tag, Token attribute, TemplateExpression value) throws ParseException {
		String attributeName = attribute.image;
		TemplateExpression clash = tag.setAttribute(attributeName, value);
		if (clash != null) {
			throw new ParseException(
				"Multiple attributes with name '" + attributeName + "'" + location(attribute) + ".");
		}
	}

	/**
	 * Creates a {@link TemplateBuilder} for constructing a {@link TemplateExpression}.
	 */
	public static TemplateBuilder builder() {
		return new TemplateBuilder();
	}

	static String location(TemplateExpression expr) {
		return expr.getRange().location();
	}

	static String location(Token token) {
		return range(token).location();
	}

	/**
	 * Builder for {@link TemplateExpression}s.
	 */
	public static class TemplateBuilder {

		private Template _base;

		private Stack<Tag> _open;

		/**
		 * Creates a {@link ExprFactory.TemplateBuilder}.
		 *
		 */
		public TemplateBuilder() {
			super();
		}

		/**
		 * Adds the given {@link TemplateExpression} to the created template.
		 */
		public void append(TemplateExpression expr) {
			if (_open == null || _open.isEmpty()) {
				if (_base == null) {
					_base = new Template(rangeCopy(expr));
				}
				_base.append(expr);
			} else {
				_open.peek().append(expr);
			}
		}

		/**
		 * Adds a new {@link Tag} to the created template.
		 * 
		 * <p>
		 * New {@link #append(TemplateExpression) contents} is created within this new tag until
		 * {@link #endTag(Token)} is called.
		 * </p>
		 */
		public void startTag(Tag start) {
			append(start);
			if (!start.isEmpty()) {
				if (_open == null) {
					_open = new ArrayStack<>();
				}
				_open.push(start);
			}
		}

		/**
		 * Closes the last {@link Tag} started with {@link #startTag(TemplateExpression.Tag)}.
		 *
		 * @param nameToken
		 *        The name of the {@link Tag} to close.
		 * @throws ParseException
		 *         If tags are not nested properly.
		 */
		public void endTag(Token nameToken) throws ParseException {
			String name = nameToken.image.substring(2);
			if (_open == null || _open.isEmpty()) {
				throw new ParseException(
					"Unexpected end tag '" + name + "', no tags are open" + location(nameToken) + ".");
			} else {
				Tag tag = _open.pop();
				String expected = tag.getName();
				if (!name.equals(expected)) {
					throw new ParseException(
						"Unexpected end tag '" + name + "', the correspondig open tag is '" + expected + "'"
							+ location(nameToken) + ".");
				}
				tag.setRange(range(tag.getRange(), nameToken));
			}
		}

		/**
		 * Finally creates the built template.
		 * 
		 * @throws ParseException
		 *         If tags are not nested properly.
		 */
		public TemplateExpression build() throws ParseException {
			if (_open != null && !_open.isEmpty()) {
				Tag tag = _open.pop();
				throw new ParseException(
					"Missing end of open tag: '" + tag.getName() + "'" + tag.getRange().location() + ".");
			}
			return _base;
		}

	}

}
