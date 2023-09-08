/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.importDoc;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.doc.app.I18NConstants;
import com.top_logic.doc.app.Icons;
import com.top_logic.doc.component.DocumentationTreeComponent;
import com.top_logic.doc.export.TLDocExportImportConstants;
import com.top_logic.doc.misc.TLDocUtil;
import com.top_logic.doc.model.Page;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Helper class to import HTML pages of the Trac Wiki exported by the Trac Wiki Exporter.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class TracWikiImporter {

	private static final String INDEX_SUFFIX = "/" + TLDocExportImportConstants.CONTENT_FILE_NAME;

	/** Regex to split camel case strings */
	private static final String SPLIT_CAMEL_CASE =
		// split between a small letter and a capital letter
		"(?<=[a-zäöü])(?=[A-ZÄÖÜ])"
			// split between a letter and a number
			+ "|(?<=[a-zA-ZäöüÄÖÜ])(?=[0-9])"
			// split between a number and a letter
			+ "|(?<=[0-9])(?=[a-zA-ZäöüÄÖÜ])";

	/** Beginning of tag to wrap {@link Element}s @see Element#wrap(String) */
	private static final String WRAP_TAG_BEGIN = "<";

	/** Middle of tag to wrap {@link Element}s @see Element#wrap(String) */
	private static final String WRAP_TAG_MIDDLE = "></";

	/** End of tag to wrap {@link Element}s @see Element#wrap(String) */
	private static final String WRAP_TAG_END = ">";

	/** CSS class of underlined texts in Trac Wiki */
	private static final String UNDERLINE = "underline";

	/** CSS class of closed tickets, milestones etc. */
	private static final String CLOSED = "closed";

	/** CSS class of codes */
	private static final String HLJS = "hljs";

	/** CSS class of inline codes in code tags */
	private static final String INLINE_CODE = "inlineCode";

	/** HTML tag name "del" */
	private static final String DEL = "del";

	/** Prefix of wiki paths */
	private static final String WIKI_PATH_PREFIX = "wiki:";

	/** ID of the contents of the wiki page */
	private static final String WIKIPAGE = "wikipage";

	/** ID of the wiki path which implements the hierarchy of the wiki pages */
	private static final String PAGEPATH = "pagepath";

	/** CSS class of the {@value #PAGEPATH} entries */
	private static final String PATHENTRY = "pathentry";

	/** Separator of paths */
	public static final String PATH_SEPARATOR = "/";

	/** Separator of paths for gotos inside of a {@link Page} */
	private static final String GOTO_SEPARATOR = "#";

	/** Value of target in <a> to create a new tab when clicking the link */
	private static final String NEW_TAB = "_blank";

	/** CSS class addition for the first part of the wiki path */
	private static final String FIRST = ".first";

	/** CSS class addition for the separator of wiki path entries */
	private static final String WIKI_PATH_SEPARATOR = ".sep";

	/** Base URL for TL websites */
	private static final String TRAC_URL = "http://tl";

	/** Prefix of Trac websites */
	private static final String TRAC_PREFIX = "/trac/";

	/** Prefix of Trac wiki websites */
	private static final String TRAC_WIKI_PREFIX = TRAC_PREFIX + "wiki/";

	/** CSS class of the <code>TracNav</code> */
	private static final String TRAC_NAV = "trac-nav";

	/** CSS class of Trac table of contents */
	private static final String WIKI_TOC = "wiki-toc";

	/** CSS class of missing sources on Trac Wiki */
	private static final String MISSING_SOURCE = "missing";

	/** CSS class of Trac wiki */
	private static final String WIKI = "wiki";

	/**
	 * CSS class of the import error icon. To mark the affected {@link Element} properly it has to
	 * be the next sibling of the element with this CSS class
	 */
	private static final String ICON_IMPORT_ERROR = "importErrorIcon";
	
	/** CSS class of the external link icon */
	private static final String EXTERNAL_LINK_ICON = "externalLinkIcon";

	/** Attribute to set content editable of DOM elements */
	private static final String CONTENTEDITABLE = "contenteditable";

	/** Tooltip attribute */
	private static final String DATA_TOOLTIP = "data-tooltip";

	/** CSS class of ticket tables */
	private static final String LISTING_TICKETS = "listing tickets";

	/** Size of borders of Trac Wiki tables */
	private static final String TABLE_BORDER_SIZE = "1";

	/** No border size of Trac Wiki tables */
	private static final String TABLE_NO_BORDER = "0";

	/** Cellpadding of Trac Wiki tables */
	private static final String TABLE_CELL_PADDING = "1";

	/** Cellspacing of Trac Wiki tables */
	private static final String TABLE_CELL_SPACING = "1";

	/** CSS class of Trac Wiki tables containing a listing */
	private static final String TABLE_LISTING = "listing";

	/** 100% width CSS style */
	private static final String FULL_WIDTH_STYLE = "width: 100%;";

	/** Text align left CSS style */
	private static final String TEXT_ALIGN_LEFT = "text-align:left;";

	/** CSS class of row number information */
	private static final String NUMROWS = "numrows";

	/** File extension of png files */
	private static final String PNG_FILE_EXTENSION = ".png";

	/** Mimetype of png files */
	private static final String PNG_MIME_TYPE = "image/png";

	/**
	 * Map of all upcoming errors, warnings and infos during the import orderd by the name of the
	 * html page.
	 */
	private Map<String, List<ResKey>> _messages;

	/**
	 * Creates an {@link TracWikiImporter}.
	 * 
	 */
	public TracWikiImporter() {
		_messages = new HashMap<>();
	}

	/**
	 * Parses s html string into a {@link Document}.
	 * 
	 * @param html
	 *        HTML page as a {@link String}.
	 * @return HTML page as {@link Document}
	 */
	public Document getParsedDocument(String html) {
		Document doc = Jsoup.parse(html);
		return doc;
	}

	/**
	 * Formats HTML pages that were extracted from the http://tl/trac.
	 * 
	 * <p>
	 * Only use this method if the HTML pages were extracted with the Trac Wiki Exporter
	 * (https://github.com/lsegal/trac-export-wiki). A correct and valid HTML output is not
	 * guaranteed for other HTML pages.
	 * </p>
	 * 
	 * @param pagePath
	 *        Complete path and name of the {@link Page} that will be created in the
	 *        {@link DocumentationTreeComponent} after formatting the source code.
	 * @param html
	 *        HTML contents of a file.
	 * @return Formatted HTML contents.
	 */
	public String formatTracWiki(String pagePath, Document html) {
		Element wikipage = html.getElementById(WIKIPAGE);
		if (wikipage == null) {
			wikipage = html.body();
		}
		removeTracNav(wikipage);
		removeMissingSourcesLinks(wikipage);
		removeWikiClasses(wikipage);
		removeTableOfContents(wikipage);
		formatCodeSnippets(wikipage);
		correctUnderline(wikipage);
		correctStrikeOut(wikipage);
		formatTables(pagePath, wikipage);
		convertDescriptionListsToTables(wikipage);
		strikeOutClosed(wikipage);
		unwrapBlockquotes(wikipage);
		unwrapDivs(wikipage);
		
		return cleanCode(wikipage.html());
	}

	/**
	 * Removes the <code>TracNav</code> table of contents of the HTML page.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void removeTracNav(Element wikipage) {
		Elements tracNav = wikipage.getElementsByClass(WIKI_TOC + " " + TRAC_NAV);
		for (Element element : tracNav) {
			removePreviousEmptyParagraphs(element);
			element.remove();
		}
	}

	/**
	 * Removes Trac Wiki links of missing sources.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void removeMissingSourcesLinks(Element wikipage) {
		wikipage.getElementsByClass(MISSING_SOURCE).unwrap();
	}

	/**
	 * Removes the unnecessary css class "wiki".
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void removeWikiClasses(Element wikipage) {
		wikipage.getElementsByClass(WIKI).removeClass(WIKI);
	}


	/**
	 * Removes the table of contents created by Trac Wiki.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void removeTableOfContents(Element wikipage) {
		wikipage.getElementsByClass(WIKI_TOC).remove();
		Elements hTags = wikipage.select("#Inhaltsverzeichnis, #Inhalt");
		if (hTags.isEmpty()) {
			return;
		}
		Element h = hTags.first();
		Element list = findTocList(h);
		if (list == null) {
			return;
		}
		list.remove();
		Element sibling = h.nextElementSibling();
		while (sibling != null && sibling.tagName().equals(list.tagName())) {
			sibling.remove();
			sibling = h.nextElementSibling();
		}
		removePreviousEmptyParagraphs(h);
		h.remove();

	}

	/**
	 * Removes all previous siblings of an {@link Element} that are paragraphs without text.
	 * 
	 * @param element
	 *        Paragraphs before this {@link Element}.
	 */
	private void removePreviousEmptyParagraphs(Element element) {
		Element prevSibling = element.previousElementSibling();
		while (prevSibling != null && prevSibling.tagName().equals(HTMLConstants.PARAGRAPH) && !prevSibling.hasText()) {
			prevSibling.remove();
			prevSibling = element.previousElementSibling();
		}
	}

	/**
	 * If an {@link Element} is list with ul or ol tag.
	 * 
	 * @param element
	 *        The {@link Element} to check.
	 * @return If {@link Element} is a list.
	 */
	private boolean isList(Element element) {
		return element.tagName().equals(HTMLConstants.OL) || element.tagName().equals(HTMLConstants.UL);
	}

	/**
	 * Returns the list of a table of contents.
	 * 
	 * <p>
	 * Checks the next siblings of a start {@link Element}. The first list {@link Element} will be
	 * returned. If there is a sibling that contains text but is not a list there does not exist a
	 * table of contents and <code>null</code> will be returend.
	 * </p>
	 * 
	 * @param start
	 *        The {@link Element} to find the next sibling list.
	 * @return The list {@link Element}. <code>null</code> if no list was found or if an
	 *         {@link Element} containing text occurred before a list {@link Element}.
	 */
	private Element findTocList(Element start) {
		Element sibling = start.nextElementSibling();
		while (sibling != null && !isList(sibling) && !sibling.hasText()) {
			sibling = sibling.nextElementSibling();
		}
		return sibling;
	}

	/**
	 * Formats code blocks and inline codes.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void formatCodeSnippets(Element wikipage) {
		Elements inlineCodes = wikipage.getElementsByTag(HTMLConstants.CODE);
		for (Element element : inlineCodes) {
			element.children().unwrap();
			element.addClass(HLJS);
			element.addClass(INLINE_CODE);
		}
		Elements codeBlocks = wikipage.getElementsByTag(HTMLConstants.PRE);
		for (Element element : codeBlocks) {
			element.children().unwrap();
			wrapElement(element, HTMLConstants.PRE);
			element.tagName(HTMLConstants.CODE);
		}
	}

	/**
	 * Use correct tag to underline text.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void correctUnderline(Element wikipage) {
		Elements underlined = wikipage.getElementsByClass(UNDERLINE);
		for (Element element : underlined) {
			element.removeAttr(HTMLConstants.CLASS_ATTR);
			element.tagName(HTMLConstants.U);
		}
	}

	/**
	 * Use correct tag to strike out text.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void correctStrikeOut(Element wikipage) {
		wikipage.getElementsByTag(DEL).tagName(HTMLConstants.S);
	}

	/**
	 * Formats Trac Wiki tables.
	 * 
	 * @param pagePath
	 *        Complete path and name of the {@link Page} that will be created in the
	 *        {@link DocumentationTreeComponent} after formatting the source code.
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void formatTables(String pagePath, Element wikipage) {
		Elements tables = wikipage.getElementsByTag(HTMLConstants.TABLE);
		for (Element table : tables) {
			if (table.className().contains(LISTING_TICKETS)) {
				addTicketTableFoundMessage(pagePath);
				addImportErrorHintToTable(table, I18NConstants.TICKET_TABLE_FOUND);
			}
			styleTable(table, TABLE_BORDER_SIZE);
			formatHeader(table);
			formatTh(table);
			combineTbody(table);
		}
	}

	/**
	 * Adds styling to a simple table with border.
	 * 
	 * @param table
	 *        The table {@link Element} to style.
	 * @param borderSize
	 *        Size of table border. #TABLE_NO_BORDER if no border wanted.
	 */
	private void styleTable(Element table, String borderSize) {
		if (table.className().contains(TABLE_LISTING)) {
			table.attr(HTMLConstants.STYLE_ATTR, FULL_WIDTH_STYLE);
		}
		if (!borderSize.equals(TABLE_NO_BORDER)) {
			table.attr(HTMLConstants.BORDER_ATTR, borderSize);
		}
		table.attr(HTMLConstants.CELLPADDING_ATTR, TABLE_CELL_PADDING);
		table.attr(HTMLConstants.CELLSPACING_ATTR, TABLE_CELL_SPACING);
	}

	/**
	 * Formats captions and headers of tables.
	 * 
	 * @param table
	 *        The table {@link Element} to format.
	 */
	private void formatHeader(Element table) {
		table.getElementsByClass(NUMROWS).remove();
		table.getElementsByTag(HTMLConstants.H2).attr(HTMLConstants.STYLE_ATTR, TEXT_ALIGN_LEFT);
	}

	/**
	 * Formats th tags of tables.
	 * 
	 * @param table
	 *        The table {@link Element} to format.
	 */
	private void formatTh(Element table) {
		Elements header = table.getElementsByTag(HTMLConstants.THEAD);
		for (Element head : header) {
			Elements thTags = head.getElementsByTag(HTMLConstants.TH);
			thTags.attr(HTMLConstants.SCOPE_ATTR, "col");
			thTags.removeAttr(HTMLConstants.TITLE);
		}
	}

	/**
	 * If a table consists of multiple tbody tags they will be combined to one.
	 * 
	 * @param table
	 *        The table {@link Element} to format.
	 */
	private void combineTbody(Element table) {
		Elements tbodies = table.getElementsByTag(HTMLConstants.TBODY);
		for (int i = 1; i < tbodies.size(); i++) {
			Element tbody = tbodies.get(i);
			tbodies.get(0).append(tbody.html());
			tbody.remove();
		}
	}

	/**
	 * Converts all description lists with dl tag to borderless tables.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void convertDescriptionListsToTables(Element wikipage) {
		Elements tables =
			wrapElements(wikipage.getElementsByTag(HTMLConstants.DL).tagName(HTMLConstants.TBODY), HTMLConstants.TABLE);
		for (Element element : tables) {
			styleTable(element.parent(), TABLE_NO_BORDER);
		}
		Elements dtTags = wikipage.getElementsByTag(HTMLConstants.DT);
		for (Element dt : dtTags) {
			dt.tagName(HTMLConstants.TH);
			Element dd = dt.nextElementSibling();
			dd.tagName(HTMLConstants.TD);
			wrapElement(dt, HTMLConstants.TR);
			dt.after(dd);
		}


	}
	
	/**
	 * Strike out all {@link Element}s with {@link #CLOSED} CSS class.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void strikeOutClosed(Element wikipage) {
		Elements closed = wikipage.getElementsByClass(CLOSED);
		wrapElements(closed, HTMLConstants.S);
	}

	/** 
	 * Wraps an {@link Element} into a new {@link Element} with the given tag.
	 * 
	 * @param element
	 * 		  The {@link Element} to wrap.
	 * @param tag
	 * 		  Tag name of the new wrapping {@link Element}.
	 * @return Wrapper of the given {@link Element}.
	 */
	private Element wrapElement(Element element, String tag) {
		return element.wrap(WRAP_TAG_BEGIN + tag + WRAP_TAG_MIDDLE + tag + WRAP_TAG_END);
	}

	/** 
	 * Wraps all {@link Elements} into a new {@link Element} with the given tag.
	 * 
	 * @param elements
	 * 		  The {@link Elements} to wrap.
	 * @param tag
	 * 		  Tag name of the new wrapping {@link Element}.
	 * @return Wrapper of the given {@link Element}.
	 */
	private Elements wrapElements(Elements elements, String tag) {
		return elements.wrap(WRAP_TAG_BEGIN + tag + WRAP_TAG_MIDDLE + tag + WRAP_TAG_END);
	}

	/**
	 * Unwrap blockquote tags.
	 * 
	 * @param wikipage
	 *        {@link Element} containing the relevant contents of the HTML page.
	 */
	private void unwrapBlockquotes(Element wikipage) {
		wikipage.getElementsByTag(HTMLConstants.BLOCKQUOTE).unwrap();
	}

	/**
	 * Unwraps all div tags except the wikipage.
	 * 
	 * @param wikipage
	 *        All divs inside of this {@link Element} will be unwrapped.
	 */
	private void unwrapDivs(Element wikipage) {
		Elements divs = wikipage.getElementsByTag(HTMLConstants.DIV);
		for (Element div : divs) {
			if (!div.equals(wikipage)) {
				div.unwrap();
			}
		}
	}

	/**
	 * Clean HTML code and remove unwanted tags and attributes.
	 * 
	 * @param html
	 *        HTML code to clean.
	 * @return clean HTML code.
	 */
	private String cleanCode(String html) {
		Safelist whitelist = getWhiteList();
		return Jsoup.clean(html, TRAC_URL, whitelist);
	}

	/**
	 * Creates a {@link Safelist} to clean HTML pages.
	 * 
	 * @return {@link Safelist} for HTML pages.
	 */
	private Safelist getWhiteList() {
		Safelist whiteList = Safelist.relaxed().preserveRelativeLinks(true);
		whiteList.addTags(HTMLConstants.S);
		whiteList.addAttributes(HTMLConstants.TABLE, HTMLConstants.STYLE_ATTR);
		whiteList.addAttributes(HTMLConstants.TABLE, HTMLConstants.BORDER_ATTR);
		whiteList.addAttributes(HTMLConstants.TABLE, HTMLConstants.CELLPADDING_ATTR);
		whiteList.addAttributes(HTMLConstants.TABLE, HTMLConstants.CELLSPACING_ATTR);
		whiteList.addAttributes(HTMLConstants.H2, HTMLConstants.STYLE_ATTR);
		whiteList.addAttributes(HTMLConstants.H1, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.H2, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.H3, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.H4, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.H5, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.H6, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.SPAN, HTMLConstants.CLASS_ATTR);
		whiteList.addAttributes(HTMLConstants.SPAN, HTMLConstants.ID_ATTR);
		whiteList.addAttributes(HTMLConstants.IMG, HTMLConstants.CLASS_ATTR);
		whiteList.addAttributes(HTMLConstants.SPAN, CONTENTEDITABLE);
		whiteList.addAttributes(HTMLConstants.SPAN, HTMLConstants.ALT_ATTR);
		whiteList.addAttributes(HTMLConstants.SPAN, HTMLConstants.TITLE);
		whiteList.addAttributes(HTMLConstants.SPAN, DATA_TOOLTIP);
		whiteList.addAttributes(HTMLConstants.ITALICS, HTMLConstants.CLASS_ATTR);
		whiteList.addAttributes(HTMLConstants.PARAGRAPH, HTMLConstants.CLASS_ATTR);
		whiteList.addAttributes(HTMLConstants.PARAGRAPH, CONTENTEDITABLE);
		whiteList.addAttributes(HTMLConstants.CODE, HTMLConstants.CLASS_ATTR);
		return whiteList;
	}

	/**
	 * Resolves Trac links.
	 * 
	 * <p>
	 * The Trac Wiki Exporter may not be able to recreate the Trac links. Links starting with
	 * {@link #TRAC_PREFIX} are missing the prefix of TL Trac and will be added. Links starting with
	 * {@link #TRAC_WIKI_PREFIX} lead to other Trac Wiki pages and have to be resolved after
	 * importing the Trac pages. External links will be ignored.
	 * </p>
	 * 
	 * @param selected
	 *        The selected {@link Page} where the Trac pages are imported to.
	 * @param currentPage
	 *        The {@link Page} whose links will be resolved.
	 * @param language
	 *        Currently set language of the {@link DocumentationTreeComponent}.
	 */
	public String resolveLinks(Page selected, Page currentPage, Locale language) {
		StructuredText localizedContent = currentPage.getContent().localize(language);
		if (localizedContent == null) {
			return null;
		}
		String sourceCode = localizedContent.getSourceCode();
		Document doc = getParsedDocument(sourceCode);
		Elements links = doc.select("[" + HTMLConstants.HREF_ATTR + "]");
		for (Element link : links) {
			String destinationPath = link.attr(HTMLConstants.HREF_ATTR);
			if (destinationPath.startsWith(TRAC_WIKI_PREFIX)) {
				resolveTracWikiLink(selected, currentPage, link, destinationPath);
			} else if (destinationPath.startsWith(TRAC_PREFIX)) {
				link.attr(HTMLConstants.HREF_ATTR, TRAC_URL + destinationPath);
				link.attr(HTMLConstants.TARGET_ATTR, NEW_TAB);
			} else if (destinationPath.startsWith(GOTO_SEPARATOR)) {
				// Gotos don't need any changes
				continue;
			} else {
				link.attr(HTMLConstants.TARGET_ATTR, NEW_TAB);
			}
		}

		return doc.toString();
	}

	/**
	 * Resolves Trac Wiki links as {@link TLObject}s if the corresponding {@link Page} was found and
	 * is not page.
	 * 
	 * @param selected
	 *        The selected {@link Page} where the Trac pages are imported to.
	 * @param currentPage
	 *        The {@link Page} whose links will be resolved.
	 * @param link
	 *        The {@link Element} containing the link.
	 * @param destinationPath
	 *        The path the link is leading to.
	 */
	private void resolveTracWikiLink(Page selected, Page currentPage, Element link, String destinationPath) {
		destinationPath = destinationPath.replace(TRAC_WIKI_PREFIX, StringServices.EMPTY_STRING);
		int index = destinationPath.lastIndexOf(GOTO_SEPARATOR);
		String goTo = null;
		if (index > -1) {
			goTo = destinationPath.substring(index + 1);
			destinationPath = destinationPath.substring(0, index);
		}
		Page destination = getLinkDestination(destinationPath, selected);
		if (destination != null) {
			if (destination.equals(currentPage)) {
				link.attr(HTMLConstants.HREF_ATTR, goTo);
				return;
			}
			String path = extractPath(destinationPath);
			String linkText = link.text().replace(path, StringServices.EMPTY_STRING);
			linkText = linkText.replace(WIKI_PATH_PREFIX, StringServices.EMPTY_STRING);
			linkText = splitCamelCaseString(linkText);
			String resolvedLink = TLObjectLinkUtil.getLink(destination, linkText, goTo);
			link.before(resolvedLink);
			link.remove();
		} else {
			addImportErrorHint(link, I18NConstants.TLOBJECT_NOT_FOUND_HINT);
			link.addClass(TLObjectLinkUtil.TL_OBJECT);
			link.attr(HTMLConstants.HREF_ATTR, "#");
			addObjectNotFountMessage(TLDocUtil.pagePath(currentPage), link, destinationPath);
		}
	}

	/**
	 * Looks for a {@link TLObject} in the tree structure.
	 * 
	 * @param path
	 *        Path of the {@link TLObject} in the {@link DocumentationTreeComponent}.
	 * @param page
	 *        {@link Page} in which the {@link TLObject} is searched for.
	 * @return {@link TLObject} of the link.
	 */
	private Page getLinkDestination(String path, Page page) {
		String[] pathParts = path.split(PATH_SEPARATOR, 2);
		String name = pathParts[0];
		List<? extends StructuredElement> children =
			page.getChildren(child -> child.getName().equalsIgnoreCase(name));
		page = (Page) CollectionUtil.getFirst(children);
		if (page == null) {
			return null;
		} else {
			if (pathParts.length == 1) {
				return page;
			} else {
				return getLinkDestination(pathParts[1], page);
			}
		}

	}

	/**
	 * Splits camel case strings and separates them with spaces.
	 * 
	 * @param camelCase
	 *        String as camel case.
	 * @return String with the camel case text separated with spaces.
	 */
	public static String splitCamelCaseString(String camelCase) {
		LinkedList<String> result = new LinkedList<>();
		for (String word : camelCase.split(SPLIT_CAMEL_CASE)) {
			result.add(word);
		}
		return String.join(" ", result);
	}

	/**
	 * Extracts the full wiki path of the HTML page including the title of the document.
	 * 
	 * @param html
	 *        HTML page.
	 * @return Path as String separated by "/"
	 */
	public String[] extractFullPath(Document html) {
		Element pagePath = html.getElementById(PAGEPATH);
		if (pagePath == null) {
			return ArrayUtil.EMPTY_STRING_ARRAY;
		}
		Elements pathentries = pagePath.getElementsByClass(PATHENTRY).not(WIKI_PATH_SEPARATOR).not(FIRST);
		return pathentries.stream().map(Element::text).toArray(String[]::new);
	}

	/**
	 * Extracts the title of the HTML page to use it as the page title in the doc app.
	 * 
	 * @param fileName
	 *        The file name (including '/' separated path) of the given HTML contents.
	 * @param html
	 *        HTML page.
	 * 
	 * @return Title of the document without the path.
	 */
	public String extractPageName(String fileName, Document html) {
		String[] fullPath = extractFullPath(html);
		String name;
		if (fullPath.length == 0) {
			name = StringServices.EMPTY_STRING;
		} else {
			name = fullPath[fullPath.length - 1];
		}

		if (StringServices.isEmpty(name)) {
			name = stripExtension(fileName);
		}

		return name;
	}

	/**
	 * Removes the file extension from the given file name.
	 */
	private static String stripExtension(String fileName) {
		if (fileName.endsWith(INDEX_SUFFIX)) {
			fileName = fileName.substring(0, fileName.length() - INDEX_SUFFIX.length());
		}

		int slashIndex = fileName.lastIndexOf('/');
		if (slashIndex >= 0) {
			fileName = fileName.substring(slashIndex + 1);
		}

		int sepIndex = fileName.lastIndexOf('.');
		if (sepIndex > 0) {
			return fileName.substring(0, sepIndex);
		}
		return fileName;
	}

	/**
	 * Extracts the wiki path of the HTML page without the title of the page.
	 * 
	 * @param html
	 *        HTML page.
	 * @return Path of the document without the title
	 */
	public String extractWikiPath(Document html) {
		String[] fullPath = extractFullPath(html);
		if (fullPath.length > 1) {
			return StringServices.toString(fullPath, 0, fullPath.length - 1, PATH_SEPARATOR);
		} else {
			return StringServices.EMPTY_STRING;
		}
	}

	/**
	 * Extracts the path without the name from a string.
	 * 
	 * @param fullPath
	 *        Full path with name
	 * @return Path without name
	 */
	private String extractPath(String fullPath) {
		int index = fullPath.lastIndexOf(PATH_SEPARATOR);
		if (index != -1) {
			return fullPath.substring(0, index + 1);
		}
		return StringServices.EMPTY_STRING;
	}

	/**
	 * Creates a {@link Map} containing all images of an HTML page as {@link BinaryData}.
	 * 
	 * <p>
	 * To find images the user has to upload a zip file containing the HTML page and its images in
	 * the same or in a sub directory. The path to an image has to be set in the src attribute of
	 * the img tag.
	 * 
	 * Example (the Trac Wiki Exporter (https://github.com/lsegal/trac-export-wiki) creates a
	 * directory structure like this):
	 * 
	 * HTML page: root/page.html; image: root/images/image.png; <img src="images/image.png"/>
	 * 
	 * Note that the directory of the HTML page is not given in the src attribute.
	 * </p>
	 * 
	 * @param pageName
	 *        Name of the {@link Page}.
	 * @param html
	 *        HTML page.
	 * @param zipFile
	 *        {@link ZipFile} containing the HTML page and its images.
	 * @param htmlFile
	 *        {@link ZipEntry} of the HTML page whose images have to be found.
	 * 
	 * @return Map of HTML page images as {@link BinaryData}
	 */
	public Map<String, BinaryData> getHtmlImages(String pageName, Document html, ZipFile zipFile, ZipEntry htmlFile) {
		Elements imageTags = html.getElementsByTag(HTMLConstants.IMG);
		Map<String, BinaryData> imageMap = new HashMap<>();
		for (Element imageTag : imageTags) {
			String imagePath = getImagePath(htmlFile, imageTag);
			if (imagePath == null) {
				continue;
			}
			ZipEntry image = zipFile.getEntry(imagePath);
			String imageName = getImageName(imagePath);
			if (image != null) {
				addImageToMap(imageMap, zipFile, image, imageName, imageTag);
			} else {
				addImportErrorHint(imageTag, I18NConstants.IMAGE_NOT_FOUND_HINT);
				addMissingImageMessage(pageName, imageTag, imagePath);
			}

		}
		return imageMap;
	}

	/**
	 * Converts {@link ZipEntry} images into {@link BinaryData} and adds them to a {@link Map}.
	 * 
	 * @param imageMap
	 *        {@link Map} of all images of a HTML page.
	 * @param zipFile
	 *        Uploaded {@link ZipFile}.
	 * @param image
	 *        {@link ZipEntry} of the image.
	 * @param imageName
	 *        Name of the image file.
	 * @param imageTag
	 * 		  {@link Element} containing the image.
	 * @return true if convert and add was successful
	 */
	private boolean addImageToMap(Map<String, BinaryData> imageMap, ZipFile zipFile, ZipEntry image,
			String imageName, Element imageTag) {
		try {
			String contentType = Files.probeContentType(Paths.get(imageName));
			if (contentType == null) {
				contentType = PNG_MIME_TYPE;
				imageName = imageName + PNG_FILE_EXTENSION;
				String src = imageTag.attr(HTMLConstants.SRC_ATTR);
				if (!src.equals(StringServices.EMPTY_STRING)) {
					imageTag.attr(HTMLConstants.SRC_ATTR, src + PNG_FILE_EXTENSION);
				}
			}
			InputStream imageInputStream = zipFile.getInputStream(image);
			BinaryData imageBinary =
				BinaryDataFactory.createBinaryData(imageInputStream, image.getSize(), contentType, imageName);
			imageMap.put(imageName, imageBinary);
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Complete image path inside of the {@link ZipFile}.
	 * 
	 * @param htmlFile
	 *        {@link ZipEntry} of the HTML page containing the image.
	 * @param imageTag
	 *        img tag in the HTML page.
	 * @return full path of the image
	 */
	private String getImagePath(ZipEntry htmlFile, Element imageTag) {
		String imageSrc = imageTag.attr(HTMLConstants.SRC_ATTR);
		if (imageSrc.equals(StringServices.EMPTY_STRING)) {
			return null;
		}
		String htmlFilePath = htmlFile.getName();
		int index = htmlFile.getName().lastIndexOf(PATH_SEPARATOR);
		if (index != -1) {
			htmlFilePath = htmlFilePath.substring(0, index + 1);
		}
		return htmlFilePath + imageSrc;
	}

	/**
	 * Name of the image without the path.
	 * 
	 * @param imagePath
	 *        Full path of the image
	 * @return image name
	 * @see #getImagePath(ZipEntry, Element)
	 */
	private String getImageName(String imagePath) {
		int index = imagePath.lastIndexOf(PATH_SEPARATOR);
		String imageName = imagePath;
		if (index != -1) {
			imageName = imagePath.substring(index + 1);
		}
		return imageName;
	}

	/**
	 * Removes the paths of all src attributes and the href attribute in img tags.
	 * 
	 * @param html
	 *        HTML page.
	 */
	public void removePathFromImageSrc(Document html) {
		Elements imageTags = html.getElementsByTag(HTMLConstants.IMG);
		for (Element imageTag : imageTags) {
			String imageSource = imageTag.attr(HTMLConstants.SRC_ATTR);
			String imageName = imageSource.substring(imageSource.lastIndexOf(PATH_SEPARATOR) + 1);
			imageTag.attr(HTMLConstants.SRC_ATTR, imageName);
			imageTag.removeAttr(HTMLConstants.HREF_ATTR);
			Element parent = imageTag.parent();
			if (parent.tagName().equals(HTMLConstants.ANCHOR)
				&& parent.attr(HTMLConstants.HREF_ATTR).contains(imageName)) {
				parent.unwrap();
			}
		}
	}

	/**
	 * If the HTML contains img tags.
	 * 
	 * @param html
	 *        HTML page.
	 */
	public boolean hasImages(Document html) {
		if (html.getElementsByTag(HTMLConstants.IMG).isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Add a warning icon to an {@link Element} to mark it as import error.
	 * 
	 * @param element
	 *        The {@link Element} that could not be imported correctly.
	 * @param hint
	 *        Tooltip text of the hint.
	 */
	private void addImportErrorHint(Element element, ResKey hint) {
		StringWriter writer = new StringWriter();
		String hintText = Resources.getInstance().getString(hint);
		writeErrorIcon(writer, hintText, ICON_IMPORT_ERROR);
		element.before(writer.toString());
		element.previousElementSibling().attr(CONTENTEDITABLE, Boolean.FALSE.toString());
	}

	/**
	 * Add a warning icon to an {@link Element} table to mark it as import error.
	 * 
	 * <p>
	 * The CKeditor automatically adds paragraphs around free code blocks. In case of tables the
	 * error icon would be a free code block and will be wrapped in the paragraph. As the CSS class
	 * {@link #ICON_IMPORT_ERROR} is only working properly if the affected {@link Element} is the
	 * sibling of the error icon, the icon has to be wrapped manually with a paragraph and the CSS
	 * class has to be added to the paragraph instead of the icon.
	 * </p>
	 * 
	 * @param element
	 *        The table that could not be imported correctly.
	 * @param hint
	 *        Tooltip text of the hint.
	 */
	private void addImportErrorHintToTable(Element element, ResKey hint) {
		StringWriter writer = new StringWriter();
		String hintText = Resources.getInstance().getString(hint);
		writeErrorIcon(writer, hintText, null);
		element.before(writer.toString());
		Element icon = element.previousElementSibling();
		Element paragraph = wrapElement(icon, HTMLConstants.PARAGRAPH).parent();
		paragraph.addClass(ICON_IMPORT_ERROR);
		paragraph.attr(CONTENTEDITABLE, Boolean.FALSE.toString());
	}

	/**
	 * Add import error hints to all img tags of the {@link Document}.
	 * 
	 * @param doc
	 *        HTML page.
	 */
	public void addImportErrorHintToImages(Document doc) {
		removePathFromImageSrc(doc);
		Elements images = doc.getElementsByTag(HTMLConstants.IMG);
		for (Element image : images) {
			addImportErrorHint(image, I18NConstants.IMAGE_NOT_FOUND_HINT);
		}
	}

	/**
	 * Writes a warning icon for import errors.
	 * 
	 * @param stringWriter
	 *        {@link StringWriter} containing the icon HTML.
	 * @param hintText
	 *        Tooltip text of the hint.
	 * @param cssClass
	 *        CSS class of the icon.
	 */
	private void writeErrorIcon(StringWriter stringWriter, String hintText, String cssClass) {
		try (TagWriter writer = new TagWriter(stringWriter)) {
			if (cssClass == null) {
				Icons.IMPORT_ERROR_ICON.writeWithTooltip(null, writer, hintText);
			} else {
				Icons.IMPORT_ERROR_ICON.writeWithCssTooltip(null, writer, cssClass,
					hintText);
			}
		} catch (IOException exception) {
			throw new TopLogicException(I18NConstants.WRITE_ICON_ERROR, exception);
		}
	}

	/**
	 * Validates the source code of the {@link StructuredText} of a {@link Page}.
	 * 
	 * <p>
	 * Creates an info message if unresolved {@link TLObject} links or images where found.
	 * </p>
	 * 
	 * @param page
	 *        The {@link Page} containing the {@link StructuredText}.
	 * @param doc
	 *        Source code of the {@link StructuredText}
	 * @return If errors where found.
	 */
	public boolean valid(Page page, Document doc) {
		boolean valid = true;
		Elements errors = doc.getElementsByClass(ICON_IMPORT_ERROR);
		if (errors.isEmpty()) {
			return true;
		}
		for (Element error : errors) {
			Element sibling = error.nextElementSibling();
			if (sibling == null) {
				return true;
			}
			String tagName = sibling.tagName();
			if (tagName.equals(HTMLConstants.ANCHOR)) {
				addObjectNotFountMessage(TLDocUtil.pagePath(page), error, error.attr(HTMLConstants.HREF_ATTR));
			} else if (tagName.equals(HTMLConstants.IMG)) {
				addMissingImageMessage(TLDocUtil.pagePath(page), error, error.attr(HTMLConstants.SRC_ATTR));
			} else if (tagName.equals(HTMLConstants.TABLE)) {
				addTicketTableFoundMessage(TLDocUtil.pagePath(page));
			}
			valid = false;
		}
		return valid;
	}

	/**
	 * Add a warning message that images are missing because they are not imported.
	 * 
	 * @param pageName
	 *        Name of the {@link Page}.
	 */
	public void addImagesNotImportedMessage(String pageName) {
		_messages.computeIfAbsent(pageName, k -> new ArrayList<>())
			.add(I18NConstants.IMAGES_NOT_IMPORTED);
	}

	/**
	 * Add a warning message that an image of the {@link Page} could not be resolved.
	 * 
	 * @param pageName
	 *        Name of the {@link Page}
	 * @param imageTag
	 *        img tag containing the missing image.
	 * @param imagePath
	 *        Path of the image that was not found.
	 */
	private void addMissingImageMessage(String pageName, Element imageTag, String imagePath) {
		_messages.computeIfAbsent(pageName, k -> new ArrayList<>())
			.add(I18NConstants.IMAGE_NOT_FOUND.fill(imagePath, imageTag.toString()));
	}

	/**
	 * Add a warning message that the {@link TLObject} of a {@link TLObject} link could not be
	 * found.
	 * 
	 * @param pageName
	 *        Name of the {@link Page}
	 * @param link
	 *        Anchor tag containing the link.
	 * @param path
	 *        Destination of the {@link TLObject} that could not be found.
	 */
	private void addObjectNotFountMessage(String pageName, Element link, String path) {
		_messages.computeIfAbsent(pageName, k -> new ArrayList<>())
			.add(I18NConstants.TLOBJECT_NOT_FOUND.fill(path, link.toString()));
	}

	/**
	 * Add a warning message that a table with a ticket list was found and may has to be removed
	 * manually.
	 * 
	 * @param pageName
	 *        Name of the {@link Page}
	 */
	private void addTicketTableFoundMessage(String pageName) {
		List<ResKey> pageErrors = _messages.computeIfAbsent(pageName, k -> new ArrayList<>());
		if (!pageErrors.contains(I18NConstants.TICKET_TABLE_FOUND)) {
			pageErrors.add(I18NConstants.TICKET_TABLE_FOUND);
		}
	}

	/**
	 * Shows all collected error messages of the import in the info area.
	 * 
	 * <p>
	 * All messages are grouped by the {@link Page} name and will be shown as lists.
	 * </p>
	 */
	public void showMessages() {
		if (_messages.isEmpty()) {
			return;
		}
		Tag[] fileMessages = new Tag[_messages.size()];
		int i = 0;
		for (Map.Entry<String, List<ResKey>> entry : _messages.entrySet()) {
			HTMLFragment text = p(message(I18NConstants.PAGE_ERRORS__FILE.fill(entry.getKey())));
			HTMLFragment list = messageList(entry.getValue());
			fileMessages[i] = (div(text, list));
			i++;
		}
		InfoService.showWarning(div(fileMessages));
		_messages.clear();
	}
}
