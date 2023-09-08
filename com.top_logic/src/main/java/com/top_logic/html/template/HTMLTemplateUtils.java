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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.html.template.config.ConfiguredTemplate;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.html.template.parser.HTMLTemplateParser;
import com.top_logic.html.template.parser.ParseException;
import com.top_logic.html.template.parser.TokenMgrError;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for parsing {@link HTMLTemplateFragment}s.
 */
public class HTMLTemplateUtils {

	/**
	 * Parses the given template from its textual form (HTML with embedded expressions).
	 *
	 * @param htmlTemplate
	 *        The template source code.
	 * @return The parsed template.
	 */
	public static HTMLTemplateFragment parse(String htmlTemplate) {
		HTMLTemplateParser parser = new HTMLTemplateParser(new StringReader(htmlTemplate));
		return new ConfiguredTemplate(internalParse(parser), htmlTemplate);
	}

	/**
	 * Reads the template with the ginve name as resource relative to the given class.
	 */
	public static HTMLTemplateFragment parse(Class<?> resourceClass, String resourceName) throws IOException {
		return parse(resourceClass.getResourceAsStream(resourceName));
	}

	/**
	 * Parses the given template from its textual form (HTML with embedded expressions) read from
	 * the given stream in UTF-8 encoding.
	 *
	 * @param in
	 *        The template source code.
	 * @return The parsed template.
	 */
	public static HTMLTemplateFragment parse(InputStream in) throws IOException {
		if (in == null) {
			return null;
		}
		return parse(StreamUtilities.readAllFromStream(in, "utf-8"));
	}

	private static HTMLTemplateFragment internalParse(HTMLTemplateParser parser) {
		try {
			RawTemplateFragment raw = parser.html();
			return checkStructure(raw);
		} catch (ParseException | TokenMgrError ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_TEMPLATE_SYNTAX_ERROR__DESC.fill(ex.getLocalizedMessage()), ex);
		}
	}

	private static HTMLTemplateFragment checkStructure(HTMLTemplateFragment raw) {
		if (raw instanceof RawTemplateFragment) {
			Stack<TagContext> stack = new Stack<>();
			TagContext context = new TagContext(null);
			stack.push(context);
			((RawTemplateFragment) raw).visit(StructureBuilder.INSTANCE, stack);

			StartTagTemplate top = stack.peek().getStart();
			if (top != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_MISSING_END_TAG__NAME_LINE_COL.fill(top.getName(), top.getLine(),
						top.getColumn()));
			}
			return context.toContent();
		} else {
			return raw;
		}
	}

	/**
	 * Temporary data structure for analyzing the contents of an open tag.
	 */
	private static class TagContext {

		private final StartTagTemplate _start;

		private final List<HTMLTemplateFragment> _contents = new ArrayList<>();

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

			if (template instanceof StringLiteral) {
				// Join with potentially preceding string literal.
				if (!_contents.isEmpty()) {
					HTMLTemplateFragment last = _contents.get(_contents.size() - 1);
					if (last instanceof StringLiteral) {
						StringLiteral lastString = (StringLiteral) last;
						StringLiteral templateString = (StringLiteral) template;
						lastString.setText(lastString.getText() + templateString.getText());
						return;
					}
				}
			}

			_contents.add(template);
		}
	}

	/**
	 * Transformer of a sequence of {@link RawTemplateFragment}s building a tree of well-formed
	 * tags.
	 */
	static class StructureBuilder implements RawTemplateFragment.Visitor<Void, Stack<TagContext>> {

		/**
		 * Singleton {@link HTMLTemplateUtils.StructureBuilder} instance.
		 */
		public static final StructureBuilder INSTANCE = new StructureBuilder();

		private StructureBuilder() {
			// Singleton constructor.
		}

		@Override
		public Void visit(StartTagTemplate template, Stack<TagContext> stack) {
			processStart(template, stack);

			if (template.isEmpty() && !HTMLConstants.VOID_ELEMENTS.contains(template.getName())) {
				throw new TopLogicException(I18NConstants.NO_VALID_VOID_ELEMENT__NAME_LINE_COL
					.fill(template.getName(), template.getLine(), template.getColumn()));
			}

			return null;
		}

		@Override
		public Void visit(SpecialStartTag template, Stack<TagContext> stack) {
			processAttributes(template);

			if (template.isEmpty()) {
				append(stack, template.getBuilder().build(template.getInner()));
			} else {
				push(stack, template);
			}

			return null;
		}

		private void processStart(StartTagTemplate template, Stack<TagContext> stack) {
			processAttributes(template);
			push(stack, template);
		}

		private void processAttributes(StartTagTemplate template) {
			List<TagAttributeTemplate> attributes = template.getAttributes();
			if (attributes.isEmpty()) {
				return;
			}

			Set<String> names = new HashSet<>();
			for (TagAttributeTemplate attr : attributes) {
				boolean ok = names.add(attr.getName());
				if (!ok) {
					throw new TopLogicException(I18NConstants.ERROR_DUPLICATE_ATTRIBUTE__NAME_LINE_COL
						.fill(attr.getName(), attr.getLine(), attr.getColumn()));
				}
				HTMLTemplateFragment content = attr.getContent();
				if (content instanceof RawTemplateFragment) {
					((RawTemplateFragment) content).visit(CheckNoStructure.INSTANCE, null);
				}
			}
		}

		private void push(Stack<TagContext> stack, StartTagTemplate template) {
			if (template.isEmpty()) {
				append(stack, template);
			} else {
				stack.push(new TagContext(template));
			}
		}

		@Override
		public Void visit(TagAttributeTemplate template, Stack<TagContext> stack) {
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
			template.setThenFragment(checkStructure(template.getThenFragment()));
			template.setElseFragment(checkStructure(template.getElseFragment()));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(ForeachTemplate template, Stack<TagContext> stack) {
			template.setContent(checkStructure(template.getContent()));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(DefineTemplate template, Stack<TagContext> stack) {
			template.setContent(checkStructure(template.getContent()));
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(ExpressionTemplate template, Stack<TagContext> stack) {
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(VariableTemplate template, Stack<TagContext> stack) {
			append(stack, template);
			return null;
		}

		@Override
		public Void visit(StringLiteral template, Stack<TagContext> stack) {
			append(stack, template);
			return null;
		}

		private void append(Stack<TagContext> stack, HTMLTemplateFragment template) {
			stack.peek().add(template);
		}
	}

	private static class CheckNoStructure extends DescendingTemplateVisitor<String> {

		/**
		 * Singleton {@link CheckNoStructure} instance.
		 */
		public static final CheckNoStructure INSTANCE = new CheckNoStructure();

		private CheckNoStructure() {
			// Singleton constructor.
		}

		@Override
		public Void visit(StartTagTemplate template, String attr) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__NAME_LINE_COL.fill(attr,
				template.getLine(), template.getColumn()));
		}

		@Override
		public Void visit(TagAttributeTemplate template, String attr) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__NAME_LINE_COL.fill(attr,
				template.getLine(), template.getColumn()));
		}

		@Override
		public Void visit(EndTagTemplate template, String attr) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__NAME_LINE_COL.fill(attr,
				template.getLine(), template.getColumn()));
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
}
