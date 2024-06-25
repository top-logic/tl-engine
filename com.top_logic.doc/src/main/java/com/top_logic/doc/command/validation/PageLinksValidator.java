/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command.validation;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.util.ResKey;
import com.top_logic.doc.PageBookmarkHandler;
import com.top_logic.doc.misc.TLDocUtil;
import com.top_logic.doc.model.Page;
import com.top_logic.doc.model.TLDocFactory;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;

/**
 * {@link PageValidator} validating the links used in the {@link Page}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PageLinksValidator implements PageValidator {

	private class Link {

		private String _name;

		private String _uuid;

		private String _display;

		public Link() {
		}

		/**
		 * Whether both, {@link #name()} and {@link #uuid()} are <code>null</code>.
		 */
		public boolean isEmpty() {
			return name() == null && uuid() == null;
		}

		/**
		 * {@link Page#getName()} of the linked page.
		 */
		public String name() {
			return _name;
		}

		/**
		 * Setter for {@link #name()}.
		 */
		public void setName(String name) {
			_name = name;
		}

		/**
		 * {@link Page#getUuid()} of the linked page.
		 */
		public String uuid() {
			return _uuid;
		}

		/**
		 * Setter for {@link #uuid()}
		 */
		public void setUuid(String uuid) {
			_uuid = uuid;
		}

		/**
		 * Text on the GUI that represents the link.
		 */
		public String linkDisplay() {
			return _display;
		}

		/**
		 * Setter for {@link #linkDisplay()}.
		 */
		public void setLinkDisplay(String display) {
			_display = display;
		}

	}

	private static final String LINK_SELECTOR =
		ANCHOR + "[" + HREF_ATTR + "][" + CLASS_ATTR + "*=" + TLObjectLinkUtil.TL_OBJECT + "]";

	@Override
	public void validatePage(DisplayContext context, Page page, Locale language, boolean recursive) {
		List<ResKey> errors = new ArrayList<>();
		findBrokenLinks(page, errors, language, recursive);
		if (!errors.isEmpty()) {
			InfoService.showErrorList(I18NConstants.ERROR_VALIDATING_LINKS, errors);
		} else {
			InfoService.showInfo(I18NConstants.INFO_ALL_LINKS_VALID);
		}

	}

	private void findBrokenLinks(Page page, List<ResKey> errors, Locale language, boolean recursive) {
		Map<Page, Set<Link>> linksByPage = linksByPage(page, recursive, language);
		
		KnowledgeBase kb = page.tKnowledgeBase();

		Map<String, Page> linkTargetsByName = indexLinksByName(kb, linksByPage, language, errors);
		Map<String, Page> linkTargetsByUUID = indexLinksByUUID(kb, linksByPage, language, errors);

		Iterator<Entry<Page, Set<Link>>> entries = linksByPage.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<Page, Set<Link>> entry = entries.next();
			Set<Link> value = entry.getValue();
			Iterator<Link> targetLinks = value.iterator();
			while (targetLinks.hasNext()) {
				boolean invalidName = false;
				boolean invalidUUID = false;
				boolean inconsistentLink = false;

				Link targetLink = targetLinks.next();
				Page nameTarget;
				if (targetLink.name() != null) {
					nameTarget = linkTargetsByName.get(targetLink.name());
					if (nameTarget == null) {
						invalidName = true;
					}
				} else {
					nameTarget = null;
				}
				Page uuidTarget;
				if (targetLink.uuid() != null) {
					uuidTarget = linkTargetsByUUID.get(targetLink.uuid());
					if (uuidTarget == null) {
						invalidUUID = true;
					} else if (nameTarget != null && !nameTarget.equals(uuidTarget)) {
						inconsistentLink = true;
					}
				} else {
					uuidTarget = null;
				}

				if (invalidUUID) {
					if (invalidName) {
						errors.add(errorInvalidUUIDAndNameInLink(toString(entry.getKey(), language), targetLink));
					} else {
						errors.add(errorInvalidUUIDInLink(toString(entry.getKey(), language), targetLink));
					}
				} else if (invalidName) {
					errors.add(errorInvalidNameInLink(toString(entry.getKey(), language), targetLink));
				}
				if (inconsistentLink) {
					errors.add(errorInconsistentLink(toString(entry.getKey(), language), targetLink));
				}
			}
		}
	}

	private static ResKey errorInconsistentLink(String pageName, Link targetLink) {
		return I18NConstants.ERROR_INCONSISTENT_LINK__PAGE__LINK_NAME__LINK_UUID__LINK_DISPLAY.fill(
			pageName,
			targetLink.name(),
			targetLink.uuid(),
			targetLink.linkDisplay());
	}

	private static ResKey errorInvalidNameInLink(String pageName, Link targetLink) {
		return I18NConstants.ERROR_INVALID_LINK__PAGE__LINK_NAME__LINK_DISPLAY.fill(
			pageName,
			targetLink.name(),
			targetLink.linkDisplay());
	}

	private static ResKey errorInvalidUUIDInLink(String pageName, Link targetLink) {
		return I18NConstants.ERROR_INVALID_LINK__PAGE__LINK_UUID__LINK_DISPLAY.fill(
			pageName,
			targetLink.uuid(),
			targetLink.linkDisplay());
	}

	private static ResKey errorInvalidUUIDAndNameInLink(String pageName, Link targetLink) {
		return I18NConstants.ERROR_INVALID_LINK__PAGE__LINK_UUID__LINK_NAME__LINK_DISPLAY.fill(
			pageName,
			targetLink.uuid(),
			targetLink.name(),
			targetLink.linkDisplay());
	}

	private Map<String, Page> indexLinksByUUID(KnowledgeBase kb, Map<Page, Set<Link>> linksByPage, Locale language, List<ResKey> errors) {
		Set<String> referencedUUIDs = linksByPage.values()
			.stream()
			.flatMap(Set::stream)
			.map(Link::uuid)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (referencedUUIDs.isEmpty()) {
			return new HashMap<>();
		}
		Expression hasUUID = inLiteralSet(attribute(TLDocFactory.KO_NAME_PAGE, Page.UUID_ATTR), referencedUUIDs);
		List<Page> items = kb.search(queryResolved(filter(allOf(TLDocFactory.KO_NAME_PAGE), hasUUID), Page.class));

		Map<String, Page> linkTargetsByUUID = new HashMap<>();
		for (Page target : items) {
			Page clash = linkTargetsByUUID.put(target.getUuid(), target);
			if (clash != null) {
				errors.add(
					I18NConstants.ERROR_AMBIGUOUS_LINK_UUID__UUID__O1__O2.fill(
						target.getUuid(),
						toString(target, language),
						toString(clash, language)));
			}
		}
		return linkTargetsByUUID;
	}

	private Map<String, Page> indexLinksByName(KnowledgeBase kb, Map<Page, Set<Link>> linksByPage, Locale language,
			List<ResKey> errors) {
		Set<String> referencedNames = linksByPage.values()
			.stream()
			.flatMap(Set::stream)
			.map(Link::name)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (referencedNames.isEmpty()) {
			return new HashMap<>();
		}
		Expression hasName = inLiteralSet(attribute(TLDocFactory.KO_NAME_PAGE, Page.NAME_ATTR),referencedNames);
		List<Page> items = kb.search(queryResolved(filter(allOf(TLDocFactory.KO_NAME_PAGE), hasName), Page.class));

		Map<String, Page> linkTargetsByName = new HashMap<>();
		for (Page target : items) {
			Page clash = linkTargetsByName.put(target.getName(), target);
			if (clash != null) {
				errors.add(I18NConstants.ERROR_AMBIGUOUS_LINK_NAME__NAME__O1__O2.fill(
					target.getName(),
					toString(target, language),
					toString(clash, language)));
			}
		}
		return linkTargetsByName;
	}

	private static String toString(Page page, Locale language) {
		return TLDocUtil.pagePath(page, language);
	}

	private Map<Page, Set<Link>> linksByPage(Page page, boolean recursive, Locale language) {
		Map<Page, Set<Link>> linksByPage = new LinkedHashMap<>();
		if (recursive) {
			addLinksRecursive(linksByPage, page, language);
		} else {
			addLinksOnPage(linksByPage, page, language);
		}

		return linksByPage;
	}

	private void addLinksRecursive(Map<Page, Set<Link>> linksByPage, Page page, Locale language) {
		addLinksOnPage(linksByPage, page, language);
		List<? extends Page> children = page.getChildren();
		for (Page child : children) {
			addLinksRecursive(linksByPage, child, language);
		}
	}

	private void addLinksOnPage(Map<Page, Set<Link>> linksByPage, Page page, Locale language) {
		StructuredText contents = page.getContent().localize(language);
		if (contents == null) {
			return;
		}
		String sourceCode = contents.getSourceCode();

		Document doc = Jsoup.parse(sourceCode);
		Elements links = doc.select(LINK_SELECTOR);
		for (Element linkElement : links) {
			String href = linkElement.attr(HREF_ATTR);
			Link link = parseLink(href);
			if (link.isEmpty()) {
				continue;
			}
			link.setLinkDisplay(linkElement.text());
			MultiMaps.add(linksByPage, page, link);
		}
	}

	private Link parseLink(String href) {
		Link result = new Link();
		Map<String, Object> objectArgs = TLObjectLinkUtil.parseLinkArguments(href);
		result.setName((String) objectArgs.get(PageBookmarkHandler.COMMAND_PARAM_PAGE));
		result.setUuid((String) objectArgs.get(PageBookmarkHandler.COMMAND_PARAM_UUID));
		return result;
	}

}

