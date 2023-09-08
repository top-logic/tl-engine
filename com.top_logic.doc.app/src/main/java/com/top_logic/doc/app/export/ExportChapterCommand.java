/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.export;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import com.lowagie.text.DocumentException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.doc.component.WithDocumentationLanguage;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * {@link CommandHandler} preparing an PDF export of the selected chapter of the documentation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportChapterCommand extends PreconditionCommandHandler {

	/**
	 * Configuration options for {@link ExportChapterCommand}.
	 */
	public interface Config extends PreconditionCommandHandler.Config, WithDocumentationLanguage {
		/**
		 * Whether to export with "draft" background.
		 */
		boolean isDraft();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link ExportChapterCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExportChapterCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Page)) {
			return new Failure(ExecutableState.NO_EXEC_NOT_SUPPORTED.getI18NReasonKey());
		}
		Page root = (Page) model;
		Locale locale = ((Config) getConfig()).resolveLanguage(component);

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				BinaryDataSource data = new BinaryDataSource() {
					@Override
					public String getName() {
						StringBuilder result = new StringBuilder();
						appendPageName(result, root);
						result.append("-");
						result.append(locale.toLanguageTag());
						result.append(new SimpleDateFormat("-yyyyMMdd").format(new Date()));
						result.append(".pdf");
						return result.toString();
					}

					private void appendPageName(StringBuilder result, Page page) {
						Page parent = page.getParent();
						if (parent != null) {
							appendPageName(result, parent);
							if (result.length() > 0) {
								result.append("-");
							}
							result.append(page.getName());
						}
					}

					@Override
					public long getSize() {
						return -1;
					}

					@Override
					public String getContentType() {
						return "application/pdf";
					}

					@Override
					public void deliverTo(OutputStream stream) throws IOException {
						boolean draft = ((Config) getConfig()).isDraft();
						ITextRenderer renderer = new DocumentPrinter(draft, locale).print(root);
						renderer.layout();
						try {
							renderer.createPDF(stream);
						} catch (DocumentException ex) {
							throw new IOException("PDF generation failed", ex);
						}
					}
				};
				
				context.getWindowScope().deliverContent(data);
			}
		};
	}

	static class DocumentPrinter {

		private final boolean _draft;

		private final Locale _locale;

		private final Resources _resources;

		TagWriter _out;

		final Map<String, BinaryData> _data = new HashMap<>();

		private int _nextId = 1;

		int _section[] = new int[20];

		/**
		 * Creates a {@link DocumentPrinter}.
		 */
		public DocumentPrinter(boolean draft, Locale locale) {
			_locale = locale;
			_draft = draft;
			_resources = Resources.getInstance(locale);
		}

		/**
		 * Prints the given page and all of its sub-pages.
		 */
		public ITextRenderer print(Page page) throws IOException {
			float dpi = 150.0f;
			float dpp = dpi / 72;
			ITextOutputDevice device = new ITextOutputDevice(dpp);
			ITextUserAgent ua = new ITextUserAgent(device) {
				@Override
				protected InputStream openStream(String uri) throws MalformedURLException, IOException {
					BinaryData data = _data.get(uri);
					if (data != null) {
						return data.getStream();
					} else {
						return null;
					}
				}

				@Override
				public String resolveURI(String uri) {
					// Do not use the URI of the current page as base, because all links must never
					// be relative to the current document.
					return uri;
				}
			};
			ITextRenderer renderer = new ITextRenderer(dpp, 1, device, ua);

			_out = new TagWriter();
			try {
				printDocument(page);

				String html = _out.toString();
				html = _out.toString();
				renderer.setDocumentFromString(html);
			} finally {
				_out = null;
			}

			return renderer;
		}

		String nextId() {
			return "res" + (_nextId++);
		}

		void printDocument(Page page) throws IOException {
			int level = level(page);
			Page ancestor = page;
			for (int n = level; n > 0; n--) {
				Page parent = ancestor.getParent();
				_section[n - 1] = parent.getChildren().indexOf(ancestor) + 1;
				ancestor = parent;
			}
			if (level > 0) {
				// Will be incremented when rendering the page.
				_section[level - 1]--;
			}

			_out.beginTag(HTML);
			_out.beginTag(HEAD);
			_out.beginBeginTag(META);
			_out.writeAttribute(HTTP_EQUIV_ATTR, "content-type");
			_out.writeAttribute(CONTENT_ATTR, CONTENT_TYPE_TEXT_HTML_UTF_8);
			_out.endEmptyTag();
			_out.beginTag(TITLE);
			_out.append("<i>TopLogic</i> Documentation");
			_out.endTag(TITLE);
			_out.beginBeginTag(STYLE_ELEMENT);
			_out.endBeginTag();

			try (InputStream cssStream = getClass().getResourceAsStream("export.css")) {
				try (Reader cssReader = new InputStreamReader(cssStream, "utf-8")) {
					StreamUtilities.copyReaderWriterContents(cssReader, _out);
				}
			}

			_out.append("@page {");
			_out.append("@top-left {");
			_out.append("font-family: Arial, Helvetica, sans-serif;");
			_out.append("content: '" + _resources.getString(I18NConstants.DOCUMENTATION_HEADER) + "';");
			_out.append("}");

			_out.append("@top-right {");
			_out.append("font-family: Arial, Helvetica, sans-serif;");
			_out.append("content: '\u00A9 " + new GregorianCalendar().get(Calendar.YEAR)
				+ " Business Operation Systems GmbH';");
			_out.append("}");

			_out.append("@bottom-left {");
			_out.append("font-family: Arial, Helvetica, sans-serif;");
			_out.append("content: 'https://www.top-logic.com/';");
			_out.append("}");
			_out.append("}");

			_out.endTag(STYLE_ELEMENT);
			_out.endTag(HEAD);
			_out.beginTag(BODY);

			if (_draft) {
				_out.beginBeginTag(IMG);
				_out.writeAttribute(ID_ATTR, "watermark");
				_out.beginAttribute(SRC_ATTR);
				_out.append("data:image/png;base64,");
				_out.append(Base64.getEncoder()
					.encodeToString(
						StreamUtilities.readStreamContents(getClass().getResourceAsStream("draft.png"))));
				_out.endAttribute();
				_out.endEmptyTag();
			}
			print(page, level);

			_out.endTag(BODY);
			_out.endTag(HTML);
		}

		private int level(Page page) {
			// Note: The technical root node is not show, therefore direct children of root start
			// with level 0.
			return page.getParent() == null ? -1 : level(page.getParent()) + 1;
		}

		private void print(Page page, int level) throws IOException {
			ResKey title = page.getTitle();

			String headingTag = headingTag(level);
			_out.beginTag(headingTag);
			if (level > 0) {
				appendSection(level);
			}
			_out.append(_resources.getString(title));
			_out.beginBeginTag(ANCHOR);
			_out.writeAttribute(NAME_ATTR, page.getUuid());
			_out.endBeginTag();
			_out.endTag(ANCHOR);
			_out.endTag(headingTag);

			I18NStructuredText content = page.getContent();
			if (content != null) {
				StructuredText contentText = content.localize(_locale);
				if (contentText != null) {
					String sourceCode = contentText.getSourceCode();
					if (!StringServices.isEmpty(sourceCode)) {
						sourceCode = sourceCode.replace("&nbsp;", HTMLConstants.NBSP);
						Document document = Jsoup.parse(sourceCode);
						new PagePrinter(contentText, level).copyContents(document);
					}
				}
			}

			for (Page child : page.getChildren()) {
				print(child, level + 1);
			}
		}

		void appendSection(int i) throws IOException {
			_section[i - 1]++;

			if (i < 4) {
				for (int n = 0; n < i; n++) {
					_out.writeInt(_section[n]);
					_out.append('.');
				}
				_out.append(' ');
			} else {
				_out.append((char) ('a' + (_section[3] - 1)));
				for (int n = 4; n < i; n++) {
					_out.append('.');
					_out.writeInt(_section[n]);
				}
				_out.append(") ");
			}

			Arrays.fill(_section, i, _section.length, 0);
		}

		final String headingTag(int headingLevel) {
			return "h" + Math.max(1, Math.min(6, headingLevel));
		}

		class PagePrinter {

			private final StructuredText _page;

			private final int _level;

			private int _offset;

			/**
			 * Creates a {@link PagePrinter}.
			 */
			public PagePrinter(StructuredText page, int level) {
				_page = page;
				_level = level;
			}

			private void print(Node node) throws IOException {
				if (node instanceof Element) {
					printElement((Element) node);
				} else if (node instanceof TextNode) {
					printText((TextNode) node);
				} else {
					copyContents(node);
				}
			}

			private void printText(TextNode node) {
				_out.writeText(node.getWholeText());
			}

			private void printElement(Element element) throws IOException {
				String tagName = element.tagName();
				if (tagName.equalsIgnoreCase(HEAD)) {
					// Drop.
				} else if (tagName.equalsIgnoreCase(HTML) || tagName.equalsIgnoreCase(BODY)) {
					// Descend.
					copyContents(element);
				} else if (tagName.equalsIgnoreCase(H1) ||
					tagName.equalsIgnoreCase(H2) ||
					tagName.equalsIgnoreCase(H3) ||
					tagName.equalsIgnoreCase(H4) ||
					tagName.equalsIgnoreCase(H5) ||
					tagName.equalsIgnoreCase(H6)) {

					int srcLevel = Integer.parseInt(tagName.substring(1));
					if (_offset == 0 || srcLevel - _offset < 1) {
						_offset = srcLevel - 1;
					}
					int headingLevel = _level + srcLevel - _offset;
					tagName = headingTag(headingLevel);

					startTag(element, tagName);
					{
						appendSection(headingLevel);

						copyContents(element);
					}
					endTag(tagName);
				} else {
					copyTag(element, tagName);
				}
			}

			private void copyTag(Element element, String tagName) throws IOException {
				startTag(element, tagName);
				{
					copyContents(element);
				}
				endTag(tagName);
			}

			private void startTag(Element element, String tagName) {
				_out.beginBeginTag(tagName);
				Attributes attributes = element.attributes();
				for (Attribute attribute : attributes) {
					writeAttribute(attribute);
				}
				if (!VOID_ELEMENTS.contains(tagName)) {
					_out.endBeginTag();
				}
			}

			private void writeAttribute(Attribute attribute) {
				String name = attribute.getKey();
				String value = attribute.getValue();
				if (name.equalsIgnoreCase(HREF_ATTR) && !StringServices.isEmpty(value)) {
					value = makeInternalLink(value);
				} else if (name.equalsIgnoreCase(SRC_ATTR) && !StringServices.isEmpty(value)) {
					if (value.startsWith("ref:")) {
						String resource = value.substring("ref:".length());
						BinaryData binaryData = _page.getImages().get(resource);
						if (binaryData != null) {
							String id = nextId() + "_" + resource;
							_data.put(id, binaryData);
							value = id;
						}
					}
				}
				_out.writeAttribute(name, value);
			}

			private String makeInternalLink(String url) {
				int queryIndex = url.lastIndexOf('?');
				if (queryIndex >= 0) {
					String query = url.substring(queryIndex + 1);

					String uuid = null;
					for (String param : query.split("&")) {
						int eqIndex = param.indexOf('=');
						if (eqIndex >= 0) {
							String paramName = param.substring(0, eqIndex);
							String paramValue = param.substring(eqIndex + 1);
							switch (paramName) {
								case "uuid":
									uuid = paramValue;
									break;
							}
						}
					}
					if (uuid != null) {
						url = "#" + uuid;
					}
				}
				return url;
			}

			private void endTag(String tagName) {
				if (VOID_ELEMENTS.contains(tagName)) {
					_out.endEmptyTag();
				} else {
					_out.endTag(tagName);
				}
			}

			void copyContents(Node node) throws IOException {
				for (Node child : node.childNodes()) {
					print(child);
				}
			}
		}
	}
}
