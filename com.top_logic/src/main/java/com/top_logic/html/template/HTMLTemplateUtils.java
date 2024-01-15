/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.html.template.expr.AddExpression;
import com.top_logic.html.template.expr.AndExpression;
import com.top_logic.html.template.expr.BinaryExpression;
import com.top_logic.html.template.expr.DivExpression;
import com.top_logic.html.template.expr.EqExpression;
import com.top_logic.html.template.expr.GeExpression;
import com.top_logic.html.template.expr.GtExpression;
import com.top_logic.html.template.expr.LiteralExpression;
import com.top_logic.html.template.expr.LiteralText;
import com.top_logic.html.template.expr.ModExpression;
import com.top_logic.html.template.expr.MulExpression;
import com.top_logic.html.template.expr.NegExpression;
import com.top_logic.html.template.expr.NotExpression;
import com.top_logic.html.template.expr.NullExpression;
import com.top_logic.html.template.expr.OrExpression;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.html.template.expr.SubExpression;
import com.top_logic.html.template.expr.TestExpression;
import com.top_logic.html.template.expr.UnaryExpression;
import com.top_logic.html.template.expr.VariableExpression;
import com.top_logic.html.template.parser.HTMLTemplateParser;
import com.top_logic.html.template.parser.ParseException;
import com.top_logic.html.template.parser.TokenMgrError;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for parsing {@link HTMLTemplateFragment}s.
 */
public class HTMLTemplateUtils {

	/**
	 * Parses the given template from its textual form (HTML with embedded expressions).
	 * 
	 * @param templateName
	 *        The name of the template to parse for error reporting.
	 * @param htmlTemplate
	 *        The template source code.
	 *
	 * @return The parsed template.
	 * @throws ConfigurationException
	 *         If parsing fails.
	 */
	public static HTMLTemplate parse(String templateName, String htmlTemplate) throws ConfigurationException {
		HTMLTemplateParser parser = new HTMLTemplateParser(new StringReader(htmlTemplate));

		HTMLTemplateFragment template;
		Set<String> variables;
		try {
			RawTemplateFragment raw = parser.html();
			StructureBuilder builder = new StructureBuilder();
			template = builder.build(raw);
			variables = builder.getVariables();
		} catch (ParseException | TokenMgrError ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_TEMPLATE_SYNTAX_ERROR__NAME_DESC.fill(templateName, ex.getLocalizedMessage()),
				templateName, htmlTemplate, ex);
		}

		return new HTMLTemplate(template, variables, htmlTemplate);
	}

	/**
	 * Reads the template with the ginve name as resource relative to the given class.
	 * 
	 * @throws ConfigurationException
	 *         If reading or parsing the template fails.
	 */
	public static HTMLTemplateFragment parse(Class<?> resourceClass, String resourceName)
			throws ConfigurationException {
		return parse(resourceClass.getPackageName() + "/" + resourceName,
			resourceClass.getResourceAsStream(resourceName));
	}

	/**
	 * Parses the given template from its textual form (HTML with embedded expressions) read from
	 * the given stream in UTF-8 encoding.
	 * 
	 * @param templateName
	 *        The name of the template for error reporting.
	 * @param in
	 *        The template source code.
	 *
	 * @return The parsed template.
	 * @throws ConfigurationException
	 *         If parsing fails.
	 */
	public static HTMLTemplateFragment parse(String templateName, InputStream in)
			throws ConfigurationException {
		if (in == null) {
			return null;
		}
		try {
			return parse(templateName, StreamUtilities.readAllFromStream(in, "utf-8"));
		} catch (IOException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_TEMPLATE_READING_FAILED__NAME.fill(templateName),
				templateName, null, ex);
		}
	}

	/**
	 * Temporary data structure for analyzing the contents of an open tag.
	 */
	private static class TagContext {

		private final StartTagTemplate _start;

		private final List<HTMLTemplateFragment> _contents = new ArrayList<>();

		private String _defaultNamespace;

		private Map<String, String> _namespaces = Collections.emptyMap();

		private String _namespace;

		private TagContext _parent;

		/**
		 * Creates a {@link HTMLTemplateUtils.TagContext}.
		 */
		public TagContext(StartTagTemplate start) {
			_start = start;
		}

		/**
		 * The tag name.
		 */
		public StartTagTemplate getStart() {
			return _start;
		}

		public HTMLTemplateFragment toContent() {
			return toTemplate(_contents);
		}

		public void add(HTMLTemplateFragment template) {
			if (template == EmptyTemplate.INSTANCE) {
				return;
			}

			if (template instanceof LiteralText) {
				// Join with potentially preceding string literal.
				if (!_contents.isEmpty()) {
					HTMLTemplateFragment last = _contents.get(_contents.size() - 1);
					if (last instanceof LiteralText) {
						LiteralText lastString = (LiteralText) last;
						LiteralText templateString = (LiteralText) template;
						lastString.setText(lastString.getText() + templateString.getText());
						return;
					}
				}
			}

			_contents.add(template);
		}

		public TagContext init(TagContext parent) {
			_parent = parent;
			_defaultNamespace = parent._defaultNamespace;

			if (_start != null) {
				Map<String, String> namespaces = null;
				for (TagAttributeTemplate attribute : _start.getAttributes()) {
					String name = attribute.getName();
					if (name.equals("xmlns")) {
						String value = getConstantValue(attribute);
						_defaultNamespace = value;
					} else if (name.startsWith("xmlns:")) {
						String prefix = name.substring("xmlns:".length());
						String value = getConstantValue(attribute);
						if (namespaces == null) {
							namespaces = new HashMap<>();
						}
						namespaces.put(prefix, value);
					}
				}
				if (namespaces != null) {
					_namespaces = namespaces;
				}

				String name = _start.getName();
				int nsSep = name.indexOf(':');
				if (nsSep < 0) {
					_namespace = _defaultNamespace;
				} else {
					String prefix = name.substring(0, nsSep);
					_namespace = getNameSpace(prefix);
				}
			} else {
				_namespace = _defaultNamespace;
			}

			return this;
		}

		protected String getNameSpace(String prefix) {
			String result = _namespaces.get(prefix);
			if (result != null) {
				return result;
			}

			return _parent == null ? null : _parent.getNameSpace(prefix);
		}

		/**
		 * The namespace of this element.
		 */
		public String getNamespace() {
			return _namespace;
		}

		private String getConstantValue(TagAttributeTemplate attribute) {
			HTMLTemplateFragment content = attribute.getContent();
			String value;
			if (content instanceof LiteralText) {
				value = ((LiteralText) content).getText();
			} else if (content instanceof EmptyTemplate) {
				value = "";
			} else {
				throw new TopLogicException(I18NConstants.ERROR_NAMESPACE_MUST_BE_CONSTANT__LINE_COL
					.fill(attribute.getLine(), attribute.getColumn()));
			}
			return value;
		}
	}

	/**
	 * Transformer of a sequence of {@link RawTemplateFragment}s building a tree of well-formed
	 * tags.
	 */
	static class StructureBuilder
			implements RawTemplateFragment.Visitor<Void, Stack<TagContext>>, TemplateExpression.Visitor<Void, Void> {

		private final Set<String> _variables = new HashSet<>();

		private boolean _inAttribute;

		public StructureBuilder() {
			// Singleton constructor.
		}

		/**
		 * Names of all used variables in the analyzed template.
		 */
		public Set<String> getVariables() {
			return _variables;
		}

		public HTMLTemplateFragment build(RawTemplateFragment raw) {
			Stack<TagContext> stack = new Stack<>();
			TagContext context = new TagContext(null);
			stack.push(context);
			raw.visit(this, stack);

			StartTagTemplate top = stack.peek().getStart();
			if (top != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_MISSING_END_TAG__NAME_LINE_COL.fill(top.getName(), top.getLine(),
						top.getColumn()));
			}
			return context.toContent();
		}

		private HTMLTemplateFragment descend(HTMLTemplateFragment fragment, Stack<TagContext> stack) {
			if (fragment instanceof RawTemplateFragment) {
				return descendRaw((RawTemplateFragment) fragment, stack);
			} else {
				return fragment;
			}
		}

		private HTMLTemplateFragment descendRaw(RawTemplateFragment raw, Stack<TagContext> stack) {
			if (stack == null) {
				// In attribute context.
				raw.visit(this, stack);
				return raw;
			} else {
				TagContext context = new TagContext(null).init(stack.peek());
				stack.push(context);
				raw.visit(this, stack);
				TagContext after = stack.pop();
				if (after != context) {
					StartTagTemplate top = after.getStart();
					throw new TopLogicException(
						I18NConstants.ERROR_MISSING_END_TAG__NAME_LINE_COL.fill(top.getName(), top.getLine(),
							top.getColumn()));
				}
				return context.toContent();
			}
		}

		@Override
		public Void visit(StartTagTemplate template, Stack<TagContext> stack) {
			if (_inAttribute) {
				throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__LINE_COL.fill(
					template.getLine(), template.getColumn()));
			}
			TagContext context = processStart(template, stack);

			if (context.getNamespace() == null) {
				// Looks like HTML.
				if (template.isEmpty()) {
					if (!HTMLConstants.VOID_ELEMENTS.contains(template.getName())) {
						throw new TopLogicException(I18NConstants.NO_VALID_VOID_ELEMENT__NAME_LINE_COL
							.fill(template.getName(), template.getLine(), template.getColumn()));
					}
				} else {
					if (HTMLConstants.VOID_ELEMENTS.contains(template.getName())) {
						throw new TopLogicException(I18NConstants.MUST_BE_VOID_ELEMENT__NAME_LINE_COL
							.fill(template.getName(), template.getLine(), template.getColumn()));
					}
				}
			}

			return null;
		}

		@Override
		public Void visit(SpecialStartTag template, Stack<TagContext> stack) {
			checkAttributes(template);

			if (template.isEmpty()) {
				append(stack, template.getBuilder().build(template.getInner()));
			} else {
				push(stack, template);
			}

			return null;
		}

		private TagContext processStart(StartTagTemplate template, Stack<TagContext> stack) {
			checkAttributes(template);
			return push(stack, template);
		}

		private void checkAttributes(StartTagTemplate template) {
			List<TagAttributeTemplate> attributes = template.getAttributes();
			if (attributes.isEmpty()) {
				return;
			}

			_inAttribute = true;
			try {
				Set<String> names = new HashSet<>();
				for (TagAttributeTemplate attr : attributes) {
					boolean ok = names.add(attr.getName());
					if (!ok) {
						throw new TopLogicException(I18NConstants.ERROR_DUPLICATE_ATTRIBUTE__NAME_LINE_COL
							.fill(attr.getName(), attr.getLine(), attr.getColumn()));
					}
					HTMLTemplateFragment content = attr.getContent();
					if (content instanceof RawTemplateFragment) {
						((RawTemplateFragment) content).visit(this, null);
					}
				}
			} finally {
				_inAttribute = false;
			}
		}

		private TagContext push(Stack<TagContext> stack, StartTagTemplate template) {
			TagContext context = new TagContext(template).init(stack.peek());
			if (template.isEmpty()) {
				append(stack, template);
			} else {
				stack.push(context);
			}
			return context;
		}

		@Override
		public Void visit(TagAttributeTemplate template, Stack<TagContext> stack) {
			if (_inAttribute) {
				throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__LINE_COL.fill(
					template.getLine(), template.getColumn()));
			}

			append(stack, template);
			return null;
		}

		@Override
		public Void visit(TemplateSequence template, Stack<TagContext> stack) {
			for (HTMLTemplateFragment content : template.getContents()) {
				StructureBuilder visitor = this;
				if (content instanceof RawTemplateFragment) {
					((RawTemplateFragment) content).visit(visitor, stack);
				} else {
					append(stack, content);
				}
			}
			return null;
		}

		@Override
		public Void visit(EndTagTemplate template, Stack<TagContext> stack) {
			if (_inAttribute) {
				throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__LINE_COL.fill(
					template.getLine(), template.getColumn()));
			}

			String foundTagName = template.getName();
			TagContext context = stack.pop();
			StartTagTemplate start = context.getStart();
			if (start == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_MATCHING_OPENING_TAG__NAME_LINE_COL
					.fill(foundTagName, template.getLine(), template.getColumn()));
			}
			String expectedTagName = start.getName();
			if (expectedTagName.equals(foundTagName)) {
				if (start instanceof SpecialStartTag) {
					SpecialStartTag specialStart = (SpecialStartTag) start;
					TagTemplate inner = new TagTemplate(specialStart.getInner(), context.toContent());
					HTMLTemplateFragment tag = specialStart.getBuilder().build(inner);
					append(stack, tag);
				} else {
					append(stack, new TagTemplate(start, context.toContent()));
				}
				return null;
			} else {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_NESTING__EXPECTED_FOUND_LINE_COL.fill(expectedTagName, foundTagName,
						template.getLine(), template.getColumn()));
			}
		}

		@Override
		public Void visit(EmptyTemplate template, Stack<TagContext> stack) {
			return null;
		}

		@Override
		public Void visit(ConditionalTemplate template, Stack<TagContext> stack) {
			template.getTest().visit(this, null);
			template.setThenFragment(descend(template.getThenFragment(), stack));
			template.setElseFragment(descend(template.getElseFragment(), stack));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(ForeachTemplate template, Stack<TagContext> stack) {
			template.setContent(descend(template.getContent(), stack));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(DefineTemplate template, Stack<TagContext> stack) {
			template.setContent(descend(template.getContent(), stack));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(ExpressionTemplate template, Stack<TagContext> stack) {
			template.getExpression().visit(this, null);
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(VariableTemplate template, Stack<TagContext> stack) {
			_variables.add(template.getName());
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(LiteralText template, Stack<TagContext> stack) {
			append(stack, template);
			return null;
		}

		private void append(Stack<TagContext> stack, HTMLTemplateFragment template) {
			if (stack != null) {
				stack.peek().add(template);
			}
		}

		@Override
		public Void visit(NotExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(AddExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(AndExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(DivExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(EqExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(GeExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(GtExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(ModExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(MulExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(NegExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(OrExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(SubExpression expr, Void arg) {
			return descend(expr, arg);
		}

		@Override
		public Void visit(LiteralExpression expr, Void arg) {
			return null;
		}

		@Override
		public Void visit(NullExpression expr, Void arg) {
			return null;
		}

		@Override
		public Void visit(StringLiteral expr, Void arg) {
			return null;
		}

		protected Void descend(UnaryExpression expr, Void arg) {
			expr.getExpr().visit(this, arg);
			return null;
		}

		protected Void descend(BinaryExpression expr, Void arg) {
			expr.getLeft().visit(this, arg);
			expr.getRight().visit(this, arg);
			return null;
		}

		@Override
		public Void visit(TestExpression expr, Void arg) {
			expr.getTest().visit(this, arg);
			expr.getThen().visit(this, arg);
			expr.getElse().visit(this, arg);
			return null;
		}

		@Override
		public Void visit(VariableExpression expr, Void arg) {
			_variables.add(expr.getName());
			return null;
		}
	}

	private static HTMLTemplateFragment toTemplate(List<HTMLTemplateFragment> contents) {
		switch (contents.size()) {
			case 0:
				return EmptyTemplate.INSTANCE;
			case 1:
				return contents.get(0);
			default:
				return new TemplateSequence(contents);
		}
	}

	/**
	 * Logs the {@link Throwable}, displays an {@link InfoService} error message and produces a
	 * placeholder error fragment.
	 */
	public static void renderError(DisplayContext context, TagWriter out, Throwable exception) throws IOException {
		ResKey resKey = I18NConstants.ERROR_RENDERING_TEMPLATE_FAILED;
		String message = exception.getMessage();
		RenderErrorUtil.produceErrorOutput(context, out, resKey, message, exception, HTMLTemplateUtils.class);
	}

}
