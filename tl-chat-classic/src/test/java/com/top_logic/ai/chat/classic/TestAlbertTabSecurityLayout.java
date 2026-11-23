/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.classic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTreeView;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.util.Resources;

/**
 * The security nodes the Albert tab contributes to the role profiles.
 *
 * <p>
 * The tab is one node: rights for the chat are granted at the tab, and every node has a label.
 * </p>
 */
public class TestAlbertTabSecurityLayout extends BasicTestCase {

	private static final String TAB_LAYOUT = "tl-chat/chat/tabLayout.layout.xml";

	/**
	 * The tab, including the chat component it references, contains exactly one
	 * {@link CompoundSecurityLayout}: the tab itself.
	 */
	public void testSingleSecurityLayout() throws Exception {
		List<CompoundSecurityLayout.Config> securityLayouts = new ArrayList<>();
		for (LayoutComponent.Config component : components()) {
			if (component instanceof CompoundSecurityLayout.Config) {
				securityLayouts.add((CompoundSecurityLayout.Config) component);
			}
		}

		assertEquals("Security layouts: " + securityLayouts, 1, securityLayouts.size());
		assertEquals(TAB_LAYOUT + "#TabLayout", securityLayouts.get(0).getName().qualifiedName());
	}

	/**
	 * Every component of the tab has a title in every supported language, so no role profile
	 * shows a missing resource key.
	 */
	public void testLabelsResolved() throws Exception {
		for (Locale locale : new Locale[] { Locale.ENGLISH, Locale.GERMAN }) {
			Resources resources = Resources.getInstance(locale);
			for (LayoutComponent.Config component : components()) {
				ResKey titleKey = LayoutComponent.Config.getEffectiveTitleKey(component);
				assertNotNull("No title for '" + component.getName() + "' in '" + locale + "': " + titleKey,
					resources.getString(titleKey, null));
			}
		}
	}

	private static List<LayoutComponent.Config> components() throws Exception {
		LayoutComponent.Config tab =
			LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), null, TAB_LAYOUT);
		assertNotNull("Layout not found: " + TAB_LAYOUT, tab);

		List<LayoutComponent.Config> result = new ArrayList<>();
		collect(result, LayoutUtils.inlineLayoutReferences(tab));
		return result;
	}

	private static void collect(List<LayoutComponent.Config> result, LayoutComponent.Config component) {
		result.add(component);
		for (Iterator<? extends LayoutComponent.Config> it =
			LayoutConfigTreeView.INSTANCE.getChildIterator(component); it.hasNext();) {
			collect(result, it.next());
		}
	}

	/**
	 * The test with the services that load layouts.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestAlbertTabSecurityLayout.class, LayoutStorage.Module.INSTANCE));
	}
}
