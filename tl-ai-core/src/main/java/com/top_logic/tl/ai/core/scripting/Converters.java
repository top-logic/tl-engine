/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * Data conversion utilities for TL-Script.
 */
@ScriptPrefix("convert")
public class Converters extends TLScriptFunctions {

	private static final MutableDataSet DEFAULT_OPTIONS = createDefaultOptions();

	/**
	 * Converts <i>Markdown</i> to HTML.
	 *
	 * @param markdown
	 *        Text in <i>Markdown</i> format.
	 * @return HTML fragment that can be rendered.
	 */
	public static HTMLFragment markdownToHtml(@Mandatory String markdown) {
		return Fragments.htmlSource(markdownToHtmlString(markdown));
	}

	/**
	 * Converts <i>Markdown</i> to an HTML string.
	 *
	 * @param markdown
	 *        Text in <i>Markdown</i> format.
	 * @return HTML string.
	 */
	public static String markdownToHtmlString(@Mandatory String markdown) {
		MutableDataSet options = DEFAULT_OPTIONS;

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options)
			.attributeProviderFactory(NEW_TAB_LINK_FACTORY)
			.build();

		Node document = parser.parse(markdown);
		return renderer.render(document);
	}

	/**
	 * Forces every rendered link to open in a new tab. The chat UI is a single-page TopLogic
	 * application, so following a link in the same tab would discard chat state.
	 */
	private static final AttributeProviderFactory NEW_TAB_LINK_FACTORY = new IndependentAttributeProviderFactory() {
		@Override
		public AttributeProvider apply(LinkResolverContext context) {
			return NewTabLinkAttributeProvider.INSTANCE;
		}
	};

	private static class NewTabLinkAttributeProvider implements AttributeProvider {

		static final NewTabLinkAttributeProvider INSTANCE = new NewTabLinkAttributeProvider();

		@Override
		public void setAttributes(Node node, AttributablePart part, MutableAttributes attributes) {
			if (part == AttributablePart.LINK) {
				attributes.replaceValue("target", "_blank");
				attributes.replaceValue("rel", "noopener noreferrer");
			}
		}
	}

	/**
	 * Converts HTML to <i>Markdown</i>.
	 *
	 * @param html
	 *        Text in HTML format.
	 * @return Markdown formatted text.
	 */
	public static String htmlToMarkdown(@Mandatory String html) {
		return FlexmarkHtmlConverter.builder(DEFAULT_OPTIONS).build().convert(html);
	}

	private static MutableDataSet createDefaultOptions() {
		MutableDataSet options = new MutableDataSet();

		// Enable extensions for better HTML conversion
		options.set(Parser.EXTENSIONS, Arrays.asList(
			TablesExtension.create(),
			StrikethroughExtension.create(),
			EmojiExtension.create(),
			InsExtension.create(),
			SuperscriptExtension.create(),
			NestedMarkdownExtension.create()
		));

		// Escape raw HTML embedded in markdown instead of passing it through. The markdown
		// rendered in the chat UI is LLM/tool output and may carry attacker-controlled markup
		// (prompt injection via fetched web content or MCP tool results), so raw HTML must
		// never reach the browser unescaped.
		options.set(HtmlRenderer.ESCAPE_HTML, true);

		// Configure the HTML to Markdown converter for cleaner output
		options.set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false); // Use ATX headings (# style) instead of setext (underlined)
		options.set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false); // Don't output id attributes
		options.set(FlexmarkHtmlConverter.WRAP_AUTO_LINKS, false); // Don't wrap auto-links in angle brackets
		options.set(FlexmarkHtmlConverter.EXTRACT_AUTO_LINKS, true); // Extract links from anchor tags
		options.set(FlexmarkHtmlConverter.DOT_ONLY_NUMERIC_LISTS, true); // Use . for numeric lists
		options.set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false); // <br> as space, not blank line
		options.set(FlexmarkHtmlConverter.LISTS_END_ON_DOUBLE_BLANK, false); // End lists on single blank
		options.set(FlexmarkHtmlConverter.DIV_AS_PARAGRAPH, true); // Treat divs as paragraphs
		options.set(FlexmarkHtmlConverter.SKIP_HEADING_1, false); // Include H1 headings
		options.set(FlexmarkHtmlConverter.SKIP_HEADING_2, false); // Include H2 headings
		options.set(FlexmarkHtmlConverter.SKIP_ATTRIBUTES, true); // Skip HTML attributes
		return options;
	}

	/**
	 * Flexmark extension that renders fenced code blocks with language "markdown" or "md"
	 * as actual rendered markdown instead of literal code blocks.
	 * <p>
	 * AI models sometimes return markdown content wrapped in markdown code blocks,
	 * which would normally be displayed as literal code. This extension detects such
	 * blocks and renders their content as HTML.
	 * </p>
	 */
	static class NestedMarkdownExtension implements HtmlRenderer.HtmlRendererExtension {

		@Override
		public void rendererOptions(MutableDataHolder options) {
			// No custom options needed
		}

		@Override
		public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
			rendererBuilder.nodeRendererFactory(new NestedMarkdownNodeRendererFactory());
		}

		static NestedMarkdownExtension create() {
			return new NestedMarkdownExtension();
		}
	}

	/**
	 * Factory for creating the nested markdown node renderer.
	 */
	static class NestedMarkdownNodeRendererFactory implements NodeRendererFactory {

		@Override
		public NodeRenderer apply(DataHolder options) {
			return new NestedMarkdownNodeRenderer(options);
		}
	}

	/**
	 * Node renderer that handles fenced code blocks with "markdown" or "md" language.
	 */
	static class NestedMarkdownNodeRenderer implements NodeRenderer {

		private final Parser _parser;

		private final HtmlRenderer _renderer;

		NestedMarkdownNodeRenderer(DataHolder options) {
			// Create parser and renderer for nested markdown content
			_parser = Parser.builder(options).build();
			_renderer = HtmlRenderer.builder(options).build();
		}

		@Override
		public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
			Set<NodeRenderingHandler<?>> handlers = new HashSet<>();
			handlers.add(new NodeRenderingHandler<>(FencedCodeBlock.class, this::render));
			return handlers;
		}

		private void render(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
			String language = node.getInfo().toString().trim().toLowerCase();

			// Check if this is a markdown code block
			if ("markdown".equals(language) || "md".equals(language)) {
				// Extract the content and parse it as markdown
				String markdownContent = node.getContentChars().toString();

				// Parse and render the nested markdown
				Node document = _parser.parse(markdownContent);
				String renderedHtml = _renderer.render(document);

				// Write the rendered HTML directly
				html.raw(renderedHtml);
			} else {
				// For all other languages, use default rendering
				context.delegateRender();
			}
		}
	}

}
