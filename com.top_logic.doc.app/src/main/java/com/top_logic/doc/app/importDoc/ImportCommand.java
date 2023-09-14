/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.importDoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.google.common.io.Files;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.doc.app.I18NConstants;
import com.top_logic.doc.component.DocumentationTreeComponent;
import com.top_logic.doc.misc.TLDocUtil;
import com.top_logic.doc.model.Page;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Command} to import selected HTML documents into the application.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class ImportCommand {

	/** File extension of .zip files */
	private static final String FILE_EXTENSION_ZIP = "zip";

	/** File extension of .html files */
	private static final String HTML_EXTENSION_ZIP = "html";

	/** The {@link Page} to add the imported documents to */
	private Page _page;

	private ImportSettings _settings;

	/** The language to import for. */
	private Locale _language;

	/**
	 * Creates an {@link ImportCommand}.
	 */
	public ImportCommand(Page page, Locale language, ImportSettings settings) {
		_page = page;
		_language = language;
		_settings = settings;
	}

	/**
	 * The {@link Page} where the files should be imported to.
	 */
	protected final Page getPage() {
		return _page;
	}

	/**
	 * Start the actual import.
	 */
	public HandlerResult processImport() {
		BinaryData data = _settings.getImportData();
		if (data == null) {
			return errorNoDocumentSelected();
		}
		return executeImport(Collections.singletonList(data));
	}

	/**
	 * Imports the selected files into the selected {@link Page}.
	 * 
	 * <p>
	 * If a zip file was selected this method imports all HTML files inside of the zip.
	 * </p>
	 * 
	 * @param data
	 *        Selected files to import below the {@link Page}.
	 */
	private HandlerResult executeImport(List<BinaryData> data) {
		TracWikiImporter formatter = new TracWikiImporter();
		for (BinaryData file : data) {
			try {
				String fileExtension = Files.getFileExtension(file.getName());
				if (fileExtension.equals(FILE_EXTENSION_ZIP)) {
					createZipFilePages(formatter, file);
				} else if (fileExtension.equals(HTML_EXTENSION_ZIP)) {
					createHtmlPage(formatter, file);
				}
			} catch (IOException ex) {
				String message = "Failed to read file '" + file.getName() + "'. Cause: " + ex.getMessage();
				throw new RuntimeException(message, ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a {@link Page} from a HTML file.
	 * 
	 * @param formatter
	 *        The {@link TracWikiImporter} to format Trac Wiki pages.
	 * @param file
	 *        The file to import.
	 */
	private void createHtmlPage(TracWikiImporter formatter, BinaryData file) throws IOException {
		String htmlContents = getHtmlContents(file.getStream());
		Document doc = formatter.getParsedDocument(htmlContents);
		String name = formatter.extractPageName(file.getName(), doc);
		if (formatter.hasImages(doc)) {
			String pagePath = getFuturePagePath(formatter.extractFullPath(doc));
			formatter.addImagesNotImportedMessage(pagePath);
			formatter.addImportErrorHintToImages(doc);
		}
		String pageName = getFuturePagePath(formatter.extractFullPath(doc));
		htmlContents = formatter.formatTracWiki(pageName, doc);
		Page newPage = createPage(getPage(), name, guessTitle(name, doc), htmlContents, null);
		resolveLinks(formatter, Collections.singletonList(newPage));
		formatter.showMessages();
	}

	/**
	 * Extracts HTML files of a zip and creates pages for all of them.
	 * 
	 * @param formatter
	 *        The {@link TracWikiImporter} to format Trac Wiki pages.
	 * @param file
	 *        The zip file as {@link BinaryData}.
	 */
	@SuppressWarnings("resource")
	private void createZipFilePages(TracWikiImporter formatter, BinaryData file) throws IOException {
		List<Page> pages = new ArrayList<>();
		File tmp = File.createTempFile("tmp", null, Settings.getInstance().getTempDir());
		OutputStream outputStream = new FileOutputStream(tmp);
		IOUtils.copy(file.getStream(), outputStream);
		ZipFile zipFile = new ZipFile(tmp);
		ZipInputStream zipStream = new ZipInputStream(file.getStream());
		ZipEntry zipEntry;

		while ((zipEntry = zipStream.getNextEntry()) != null) {
			if (!zipEntry.isDirectory()) {
				String fileName = zipEntry.getName();
				String contentType = new MimetypesFileTypeMap().getContentType(fileName);
				if (contentType.equals(HTMLConstants.CONTENT_TYPE_TEXT_HTML)) {
					String htmlContents = getHtmlContents(zipFile.getInputStream(zipEntry));
					Document doc = formatter.getParsedDocument(htmlContents);
					String name = formatter.extractPageName(fileName, doc);
					String pageName = getFuturePagePath(formatter.extractFullPath(doc));
					Map<String, BinaryData> htmlImages =
						formatter.getHtmlImages(pageName, doc, zipFile, zipEntry);

					String path = formatter.extractWikiPath(doc);
					Page parent = getParent(getPage(), path);
					htmlContents = formatter.formatTracWiki(pageName, doc);
					Page createPage = createPage(parent, name, guessTitle(name, doc), htmlContents, htmlImages);
					StructuredText structuredText = createPage.getContent().localize(getLanguage());
					if (structuredText != null) {
						String newPageCode = structuredText.getSourceCode();
						Document newPageDoc = formatter.getParsedDocument(newPageCode);
						formatter.removePathFromImageSrc(newPageDoc);
						I18NStructuredTextUtil.linkImageSources(newPageDoc, htmlImages);
						updateSourceCode(createPage, newPageDoc.body().html());
					}
					pages.add(createPage);
				}
			}
		}
		resolveLinks(formatter, pages);
		formatter.showMessages();
		tmp.deleteOnExit();
		zipFile.close();
		outputStream.close();
	}

	/**
	 * Calculates the full path of a not yet existing {@link Page} in the
	 * {@link DocumentationTreeComponent}.
	 * 
	 * @param importPath
	 *        The path of the {@link Page} to create starting from the {@link Page} where the new
	 *        {@link Page} will be imported.
	 * 
	 * @return Complete {@link Page} of the {@link DocumentationTreeComponent}
	 */
	private String getFuturePagePath(String... importPath) {
		String pagePath = TLDocUtil.pagePath(getPage());
		String pageName;
		String importPathAsString = StringServices.toString(importPath, TLDocUtil.PAGE_SEPARATOR);
		if (pagePath.equals("")) {
			pageName = importPathAsString;
		} else {
			pageName = pagePath + TLDocUtil.PAGE_SEPARATOR + importPathAsString;
		}
		return pageName;
	}

	/**
	 * Resolves Trac links.
	 * 
	 * @param formatter
	 *        The {@link TracWikiImporter} to format Trac pages.
	 * @param pages
	 *        {@link Page}s whose links will be resolved.
	 */
	private void resolveLinks(TracWikiImporter formatter, List<Page> pages) {
		for (Page page : pages) {
			String sourceCode = formatter.resolveLinks(getPage(), page, getLanguage());
			if (sourceCode != null) {
				updateSourceCode(page, sourceCode);
			}
		}
	}

	/**
	 * Updates source code of {@link Page}.
	 * 
	 * @param page
	 *        {@link Page} whose source code will be updated.
	 * @param sourceCode
	 *        New source code.
	 */
	private void updateSourceCode(Page page, String sourceCode) {
		I18NStructuredText structuredText =
			I18NStructuredTextUtil.updateSourceCode(page.getContent(), getLanguage(), sourceCode);
		KBUtils.inTransaction(() -> page.setContent(structuredText));
	}


	/**
	 * Creates an error message if an import was started without selection a document.
	 */
	static HandlerResult errorNoDocumentSelected() {
		HandlerResult error = new HandlerResult();
		error.addErrorMessage(I18NConstants.IMPORT_DOCUMENT_NO_DOCUMENT_SELECTED);
		return error;
	}

	/**
	 * Recursively calculates the parent {@link Page} of the document based on the given path.
	 * 
	 * <p>
	 * In case of a hierarchical structure of the imported files with sub documents the correct
	 * {@link Page} max not be {@link #_page}. If the sub page does not exist yet it will be
	 * created.
	 * </p>
	 * 
	 * @param page
	 *        Currently calculated parent {@link Page}.
	 * @param path
	 *        Wiki path of the document.
	 * @return Actual parent {@link Page} of the document.
	 */
	private Page getParent(Page page, String path) {
		if (path.equals("")) {
			return page;
		} else {
			String[] pathParts = path.split("/", 2);
			Page newPage = getOrCreatePage(page, pathParts[0]);
			if (pathParts.length == 1) {
				return getParent(newPage, "");
			} else {
				return getParent(newPage, pathParts[1]);
			}
		}
	}

	/**
	 * Looks for a {@link Page} with the given name in the parent {@link Page}. If this {@link Page}
	 * does not exist yet returns a newly created {@link Page}.
	 * 
	 * @param parent
	 *        Parent {@link Page} whose children are checked with the given name.
	 * @param name
	 *        Name of the {@link Page} to look for.
	 * @return A new or existing {@link Page}.
	 */
	private Page getOrCreatePage(Page parent, String name) {
		Page newPage = null;
		List<? extends Page> children = parent.getChildren();
		for (Page child : children) {
			String childTitle = Resources.getInstance(getLanguage()).getString(child.getTitle());
			if (childTitle.equalsIgnoreCase(TracWikiImporter.splitCamelCaseString(name))) {
				newPage = child;
			}
		}
		if (newPage == null) {
			newPage = createPage(parent, name, guessTitle(name, null), "", null);
		}
		return newPage;
	}

	/**
	 * Heuristic computing a page title from a page's technical name.
	 * 
	 * @param name
	 *        The technical name of the page.
	 * @param contents
	 *        The HTML contents, may be <code>null</code>.
	 */
	private ResKey guessTitle(String name, Document contents) {
		if (_settings.getExtractTitles()) {
			if (contents != null) {
				Element firstHeader = findFirstHeader(contents);
				if (firstHeader != null) {
					return ResKey.text(firstHeader.text());
				}
			}
			String split = TracWikiImporter.splitCamelCaseString(name);
			if (split.equals("")) {
				return null;
			}
			return ResKey.text(split.strip());
		} else {
			return ResKey.text(name.strip());
		}
	}

	/**
	 * Finds the first H1, H2, ... element in the given contents.
	 */
	private static Element findFirstHeader(Node contents) {
		if (contents instanceof Element) {
			Element element = (Element) contents;
			String tagName = element.tagName();
			if (tagName.length() == 2) {
				if (Character.toLowerCase(tagName.charAt(0)) == 'h' && Character.isDigit(tagName.charAt(1))) {
					return element;
				}
			}
		}

		for (Node child : contents.childNodes()) {
			Element childResult = findFirstHeader(child);
			if (childResult != null) {
				return childResult;
			}
		}

		return null;
	}

	/**
	 * Creates a new {@link Page} below the parent {@link Page}.
	 * 
	 * @param parent
	 *        Parent {@link Page} to add the new {@link Page} to.
	 * @param name
	 *        The technical name of the new {@link Page}.
	 * @param title
	 *        The page title.
	 * @param contents
	 *        HTML contents of the new {@link Page}.
	 * @param images
	 *        {@link Map} of images of the HTML page.
	 * @return Newly created {@link Page}.
	 */
	private Page createPage(Page parent, String name, ResKey title, String contents, Map<String, BinaryData> images) {
		return KBUtils.inTransaction(() -> createPageInTransaction(parent, name, title, contents, images));
	}

	/**
	 * Creates a {@link Page} in the database.
	 * 
	 * @param parent
	 *        {@link Page} to add the new {@link Page} as a child.
	 * @param name
	 *        The technical name of the new {@link Page}.
	 * @param title
	 *        The page title
	 * @param contents
	 *        Source code of the new {@link Page}.
	 * @param images
	 *        Images of the new {@link Page}.
	 * @return Newly created {@link Page}.
	 */
	private Page createPageInTransaction(Page parent, String name, ResKey title, String contents, Map<String, BinaryData> images) {
		Page newPage = (Page) parent.createChild(name.strip(), Page.PAGE_TYPE);
		Map<Locale, StructuredText> content =
			Collections.singletonMap(getLanguage(), new StructuredText(contents, images));
		I18NStructuredText structuredText = new I18NStructuredText(content);
		newPage.setContent(structuredText);
		if (title != null) {
			newPage.setTitle(title);
		}
		return newPage;
	}

	/**
	 * Language of the {@link DocumentationTreeComponent}.
	 * 
	 * @return Currently selected language.
	 */
	private Locale getLanguage() {
		return _language;
	}

	/**
	 * Extracts the HTML contents of a {@link BinaryData} into a String.
	 * 
	 * @param stream
	 *        File stream containing HTML.
	 * @return HTML in a {@link String}.
	 */
	private String getHtmlContents(InputStream stream) {
		try (Scanner scanner = new Scanner(stream, StringServices.UTF8).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

}
