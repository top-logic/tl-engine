/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.element.meta.MetaElementUtil.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.doc.misc.TLDocUtil;
import com.top_logic.doc.model.Page;
import com.top_logic.doc.model.TLDocFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.objects.KnowledgeItem.State;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.help.HelpFinder;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.util.Resources;
import com.top_logic.util.TLResKeyUtil;

/**
 * Generates the documentation for the {@link LayoutComponent} tree.
 * <p>
 * Commits the changes as a single transaction.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DocumentationGenerator {

	private Map<String, Page> _pages = map();

	/**
	 * Generates documentation {@link Page}s for the application views.
	 * 
	 * @param mainLayout
	 *        Must not be null.
	 * @param rootPage
	 *        Must not be null.
	 * @return A mapping from
	 *         {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getHelpId()}s to the
	 *         corresponding documentation {@link Page}s. A new, mutable and resizable {@link Map}.
	 */
	public Map<String, Page> generate(MainLayout mainLayout, Page rootPage) {
		requireNonNull(mainLayout);
		requireNonNull(rootPage);
		addExistingPages();
		List<Page> createdPages = new ArrayList<>();
		KBUtils.inTransaction(() -> addMissingContent(mainLayout, rootPage, createdPages));
		if (createdPages.isEmpty()) {
			InfoService.showInfo(I18NConstants.NO_PAGES_GENERATED);
		} else {
			List<ResKey> createdPaths = new ArrayList<>();
			for (Page createdPage : createdPages) {
				createdPaths.add(ResKey.text(TLDocUtil.pagePath(createdPage)));
			}
			InfoService.showInfoList(I18NConstants.PAGES_GENERATED, createdPaths);
		}
		return map(_pages);
	}

	/**
	 * Registered all existing {@link Page}s, indexed by their {@link Page#getUuid()}.
	 * 
	 * @see #getPage(String)
	 */
	protected void addExistingPages() {
		for (Page page : getExistingPages()) {
			addPage(page.getUuid(), page);
		}
	}

	/** Returns the {@link Page}s that already exist in the database. */
	protected Collection<Page> getExistingPages() {
		return getAllInstancesOf(getPageType(), Page.class);
	}

	/** Adds the missing {@link Page} and updates the titles of existing ones. */
	protected void addMissingContent(MainLayout mainLayout, Page rootPage, List<Page> createdPages) {
		ComponentInstantiationContext ctx = new ComponentInstantiationContext(
			new DefaultInstantiationContext(DocumentationGenerator.class), mainLayout);
		Map<String, LayoutConfigTree> layoutConfigTrees = LayoutUtils.getLayoutConfigTreesByName(ctx);
		try {
			ctx.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		for (LayoutConfigTree tree : layoutConfigTrees.values()) {
			createPagesForChildren(null, rootPage, tree.getRoot(), createdPages);
		}
	}

	private Page createPagesForChildren(Page predecessor, Page parent, LayoutConfigTreeNode node,
			List<Page> createdPages) {
		List<? extends LayoutConfigTreeNode> children = node.getChildren();
		for (LayoutConfigTreeNode child : children) {
			if (node.getConfig() instanceof TabComponent.Config) {
				predecessor = createPageForTab(predecessor, parent, child, createdPages);
			} else {
				predecessor = createPageForComponent(predecessor, parent, child, createdPages);
			}
		}
		return predecessor;
	}

	private Page createPageForTab(Page predecessor, Page parent, LayoutConfigTreeNode child, List<Page> createdPages) {
		Config config = child.getConfig();
		if (config.getTabInfo() != null) {
			ResKey label = config.getTabInfo().getLabel();
			String name = pageNameForLabel(label, pageNameForComponent(config));
			if (HelpFinder.hasHelpID(config)) {
				Page newPage =
					getOrCreatePage(HelpFinder.getHelpID(config), name, label, predecessor, parent, createdPages);
				createPagesForChildren(null, newPage, child, createdPages);
				return newPage;
			} else {
				Page newPage = getOrCreatePage(null, name, label, predecessor, parent, createdPages);
				createPagesForChildren(null, newPage, child, createdPages);
				if (isNew(newPage) && newPage.getChildren().isEmpty()) {
					createdPages.remove(newPage);
					/* Page is not really needed, i.e. there is no child of the tab for which a page
					 * is needed. */
					newPage.tDelete();
					return predecessor;
				} else {
					return newPage;
				}
			}
		} else {
			return createPageForComponent(predecessor, parent, child, createdPages);
		}
	}

	private Page createPageForComponent(Page predecessor, Page parent, LayoutConfigTreeNode child,
			List<Page> createdPages) {
		Config config = child.getConfig();
		if (HelpFinder.hasHelpID(config)) {
			ResKey titleKey = Config.getEffectiveTitleKey(config);
			String name = pageNameForLabel(titleKey, pageNameForComponent(config));
			String helpID = HelpFinder.getHelpID(config);
			Page newPage = getOrCreatePage(helpID, name, titleKey, predecessor, parent, createdPages);
			createPagesForChildren(null, newPage, child, createdPages);
			return newPage;
		} else if (config.getDialogInfo() != null) {
			DialogInfo dialogInfo = config.getDialogInfo();
			String helpID = dialogInfo.getHelpId();
			if (helpID != null) {
				ResKey titleKey = TypedConfigUtil.createInstance(dialogInfo.getTitle()).getSimpleTitle(config);
				if (titleKey == null) {
					titleKey = ResKey.text(pageNameForComponent(config));
				}
				String name = pageNameForLabel(titleKey, pageNameForComponent(config));
				Page newPage = getOrCreatePage(helpID, name, titleKey, predecessor, parent, createdPages);
				createPagesForChildren(null, newPage, child, createdPages);
				return newPage;
			} else {
				return createPagesForChildren(predecessor, parent, child, createdPages);
			}
		} else {
			return createPagesForChildren(predecessor, parent, child, createdPages);
		}
	}

	private String pageNameForComponent(Config config) {
		return config.getName().localName();
	}

	private Page getOrCreatePage(String helpID, String name, ResKey titleKey, Page predecessor, Page parent,
			List<Page> createdPages) {
		Page newPage = getOrCreatePage(helpID, name, predecessor, parent);
		if (isNew(newPage)) {
			createdPages.add(newPage);
			newPage.setTitle(titleKey);
		}
		return newPage;
	}

	private boolean isNew(Page page) {
		return page.tHandle().getState() == State.NEW;
	}

	/**
	 * Creates a page name for the page from the given {@link ResKey}.
	 */
	private String pageNameForLabel(ResKey key, String fallback) {
		String label = Resources.getInstance(Locale.ENGLISH).getString(key, fallback);
		label = label.replaceAll("[^A-Za-z0-9]", " ");
		return StringServices.capitalizeString(StringServices.toCamelCase(label, "\\s+"));
	}

	/**
	 * Returns the page for the given UUID or the child of the given page with same name when
	 * exists, or creates a new child of the given {@link Page} with given name and UUID.
	 * 
	 * @param predecessor
	 *        Direct predecessor of the new page as child of parent. May be <code>null</code> or a
	 *        not a child of parent.
	 */
	protected Page getOrCreatePage(String uuid, String name, Page predecessor, Page parent) {
		if (uuid == null) {
			return getOrCreatePageByName(uuid, name, predecessor, parent);
		}
		Page existingPage = getPage(uuid);
		if (existingPage != null) {
			return existingPage;
		}
		return getOrCreatePageByName(uuid, name, predecessor, parent);
	}

	/**
	 * Returns the child of the given page with same name when exists, or creates a new child of the
	 * given {@link Page} with given name and UUID.
	 * 
	 * @param predecessor
	 *        Direct predecessor of the new page as child of parent. May be <code>null</code> or a
	 *        not a child of parent.
	 */
	protected Page getOrCreatePageByName(String uuid, String name, Page predecessor, Page parent) {
		for (Page child : parent.getChildren()) {
			if (Utils.equals(name, child.getName())) {
				return child;
			}
		}
		Page newPage = createEmptyPage(name, predecessor, parent);
		if (uuid != null) {
			String uuidStripped = uuid.strip();
			newPage.setUuid(uuidStripped);
			addPage(uuidStripped, newPage);
		}
		return newPage;
	}

	/**
	 * Creates a child of the given parent with the given child-predecessor.
	 * 
	 * @param predecessor
	 *        If <code>null</code> or not child of the parent, it is ignored; otherwise it will be
	 *        the direct children predecessor of the new child.
	 */
	protected Page createEmptyPage(String name, Page predecessor, Page parent) {
		List<StructuredElement> children = parent.getChildrenModifiable();
		Page newPage = createEmptyPage(name, parent);
		int newPageIndex = -1;
		int siblingIndex = -1;
		if (predecessor == null) {
			// new page must be first element
			for (int i = children.size() - 1; i >= 0; i--) {
				StructuredElement child = children.get(i);
				if (child == newPage) {
					newPageIndex = i;
					break;
				}
			}
		} else {
			for (int i = children.size() - 1; i >= 0; i--) {
				StructuredElement child = children.get(i);
				if (newPageIndex == -1 && child == newPage) {
					newPageIndex = i;
				}
				if (siblingIndex == -1 && child == predecessor) {
					siblingIndex = i;
				}
				if (siblingIndex > 0 && newPageIndex > 0) {
					break;
				}
			}
			if (siblingIndex == -1) {
				// Sibling is not a child of the new parent. No order necessary.
				return newPage;
			}
		}
		if (newPageIndex == siblingIndex + 1) {
			// Sibling already direct predecessor.
			return newPage;
		}
		StructuredElement removedPage = children.remove(newPageIndex);
		if (siblingIndex < newPageIndex) {
			children.add(siblingIndex + 1, removedPage);
		} else {
			/* Sibling was a successor. Removing the page has decreased the actual index of the
			 * sibling by one. */
			children.add(siblingIndex, removedPage);
		}
		return newPage;
	}
	
	/**
	 * Creates an empty {@link Page} under the given parent.
	 */
	protected Page createEmptyPage(String name, Page parent) {
		return (Page) parent.createChild(name.strip(), getPageType());
	}

	/** The {@link TLClass} representing the {@link Page} type. */
	protected TLClass getPageType() {
		return TLDocFactory.getPageType();
	}

	/**
	 * The {@link Page} registered under the given UUID.
	 * <p>
	 * Null, if there is none.
	 * </p>
	 */
	protected Page getPage(String uuid) {
		return _pages.get(uuid);
	}

	/**
	 * Adds the given {@link Page} under the given UUID to the {@link Map} of existing pages.
	 * <p>
	 * An existing page stored under the given UUID is replaced.
	 * </p>
	 * 
	 * @see #getPage(String)
	 */
	protected void addPage(String uuid, Page page) {
		_pages.put(uuid, page);
	}

	/** Updates the title of the given {@link Page} in the given translation. */
	protected void updateTitle(Page page, String title, Locale locale) {
		TLResKeyUtil.updateTranslation(page, Page.TITLE_ATTR, locale, title);
	}

}
