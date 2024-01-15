/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.MapBasedXMLAttributes;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.layout.tabbar.TabSwitchCommandModel;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link TabSwitchCommandModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTabSwitchCommandModel extends BasicTestCase {

	private static final Attributes EMPTY_ATTRIBUTES = new MapBasedXMLAttributes(Collections.emptyMap());

	private static final ResKey CARD1_ID = ResKey.forTest("card1");

	private static final ResKey CARD2_ID = ResKey.forTest("card2");
	
	private TabComponent tabComponent;
	private Card firstTab;
	private Card secondTab;
	private TabSwitchCommandModel firstTabSwitchCommandModel;
	private TabSwitchCommandModel secondTabSwitchCommandModel;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext.getContext().setCurrentPerson(PersonManager.getManager().getRoot());

		tabComponent =
			new TabComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				TypedConfiguration.newConfigItem(TabComponent.Config.class));
		tabComponent.setTabs(createTwoTabs());
		createTabSwitchCommandModelsForTabs(tabComponent);
	}

	private List<Card> createTwoTabs() {
		firstTab = createTab(tabComponent, CARD1_ID);
		secondTab = createTab(tabComponent, CARD2_ID);
		List<Card> cards = new ArrayList<>(2);
		cards.add(firstTab);
		cards.add(secondTab);
		return cards;
	}

	private Card createTab(TabComponent tabComponent, ResKey tabId) {
		TabInfo tabInfo = TabInfo.newTabInfo(tabId);
		try {
			return new TabbedLayoutComponent(
				new DummyBoundComponent(), tabInfo);
		} catch (ConfigurationException ex) {
			throw fail("Cannot instantiate component.", ex);
		}
	}

	private void createTabSwitchCommandModelsForTabs(TabComponent tabComponent) {
		firstTabSwitchCommandModel = createTabSwitchCommandModel(tabComponent, CARD1_ID);
		secondTabSwitchCommandModel = createTabSwitchCommandModel(tabComponent, CARD2_ID);
	}

	private TabSwitchCommandModel createTabSwitchCommandModel(TabComponent tabComponent, ResKey tabId) {
		TabSwitchCommandModel tabSwitchCommandModel = TabSwitchCommandModel.newInstance(tabComponent, tabId.getKey());
		tabSwitchCommandModel.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE,
			GenericPropertyListener.IGNORE_EVENTS);
		return tabSwitchCommandModel;
	}

	public void testSelectedTabIsNotExecutable() {
		tabComponent.setSelectedIndex(1);

		assertFalse("Selected tab must not be executable!", secondTabSwitchCommandModel.isExecutable());
	}

	public void testSelectedTabIsVisible() {
		tabComponent.setSelectedIndex(1);

		assertTrue("Selected tab must be visible!", secondTabSwitchCommandModel.isVisible());
	}

	public void testNonSelectedTabIsExecutable() {
		tabComponent.setSelectedIndex(1);

		assertTrue("Not selected tab must be executable!", firstTabSwitchCommandModel.isExecutable());
	}

	public void testNonSelectedTabIsVisible() {
		tabComponent.setSelectedIndex(1);

		assertTrue("Not selected tab must be visible!", firstTabSwitchCommandModel.isVisible());
	}

	public void testBeforeAndAfterExistentTabVisibleAfterSettingNewTabs() {
		TabSwitchCommandModel beforeAndAfterExistingCommandModel = secondTabSwitchCommandModel;
		Card beforeAndAfterExistingTab = createTab(tabComponent, CARD2_ID);

		tabComponent.setTabs(Collections.singletonList(beforeAndAfterExistingTab));

		assertTrue("Tab, which was existent and visible before and still is existent must still be visible!",
			beforeAndAfterExistingCommandModel.isVisible());
	}

	public void testOnlyBeforeExistentTabInvisibleAfterSettingNewTabs() {
		TabSwitchCommandModel beforeExistingCommandModel = firstTabSwitchCommandModel;
		Card beforeAndAfterExistingTab = createTab(tabComponent, CARD2_ID);

		tabComponent.setTabs(Collections.singletonList(beforeAndAfterExistingTab));

		assertFalse("Tab, which was existent before only, must not be visible now!",
			beforeExistingCommandModel.isVisible());
	}

	public void testOnlyBeforeExistentTabNotExecutableAfterSettingNewTabs() {
		TabSwitchCommandModel beforeExistingCommandModel = firstTabSwitchCommandModel;
		Card beforeAndAfterExistingTab = createTab(tabComponent, CARD2_ID);

		tabComponent.setTabs(Collections.singletonList(beforeAndAfterExistingTab));

		assertFalse("Tab, which was existent before only, must not be executable now!",
			beforeExistingCommandModel.isExecutable());
	}

	public void testCardIsInvisibleAfterHasBeenMadeInactive() {
		TabSwitchCommandModel beforeActiveCommandModel = secondTabSwitchCommandModel;
		assertTrue("Active card must be visible!", beforeActiveCommandModel.isVisible());

		DummyBoundComponent componentSecondTab = (DummyBoundComponent) secondTab.getContent();
		componentSecondTab.setInvisibleAndDisallowsAll();
		revalidateTabComponent(componentSecondTab);

		assertFalse("Inactive card must be invisible!", beforeActiveCommandModel.isVisible());
	}

	public void testCardIsNotExecutableAfterHasBeenMadeInactive() {
		TabSwitchCommandModel beforeActiveCommandModel = secondTabSwitchCommandModel;
		assertTrue("Active card must be executable!", beforeActiveCommandModel.isExecutable());

		DummyBoundComponent componentSecondTab = (DummyBoundComponent) secondTab.getContent();
		componentSecondTab.setInvisibleAndDisallowsAll();
		revalidateTabComponent(componentSecondTab);

		assertFalse("Inactive card must be executable!", beforeActiveCommandModel.isExecutable());
	}

	public void testCardIsVisibleAfterHasBeenMadeActive() {
		DummyBoundComponent componentSecondTab = (DummyBoundComponent) secondTab.getContent();
		componentSecondTab.setInvisibleAndDisallowsAll();
		revalidateTabComponent(componentSecondTab);

		TabSwitchCommandModel afterActiveCommandModel = secondTabSwitchCommandModel;
		assertFalse("Inactive card must not be visible!", afterActiveCommandModel.isVisible());

		componentSecondTab.setVisibleAndAllowsAll();
		revalidateTabComponent(componentSecondTab);

		assertTrue("Active card must be visible!", afterActiveCommandModel.isVisible());
	}

	public void testCardIsExecutableAfterHasBeenMadeActive() {
		DummyBoundComponent componentSecondTab = (DummyBoundComponent) secondTab.getContent();
		componentSecondTab.setInvisibleAndDisallowsAll();
		revalidateTabComponent(componentSecondTab);

		TabSwitchCommandModel afterActiveCommandModel = secondTabSwitchCommandModel;
		assertFalse("Inactive card must not be executable!", afterActiveCommandModel.isExecutable());

		componentSecondTab.setVisibleAndAllowsAll();
		revalidateTabComponent(componentSecondTab);

		assertTrue("Active card must be executable!", afterActiveCommandModel.isExecutable());
	}

	private void revalidateTabComponent(LayoutComponent changedSecurityComponent) {
		invalidateTabComponentModel(changedSecurityComponent);
		tabComponent.validateModel(DummyDisplayContext.newInstance());
	}

	private void invalidateTabComponentModel(LayoutComponent changedSecurityComponent) {
		tabComponent.handleModelEvent(tabComponent.getModel(), changedSecurityComponent,
			ModelEventListener.SECURITY_CHANGED);
	}

	public static Test suite() {
		Test t = new TestSuite(TestTabSwitchCommandModel.class);
		t = ServiceTestSetup.createSetup(t, BoundHelper.Module.INSTANCE);
		t = ServiceTestSetup.createSetup(t, SecurityObjectProviderManager.Module.INSTANCE);
		t = ServiceTestSetup.createSetup(t, CommandHandlerFactory.Module.INSTANCE);
		t = TestPersonSetup.wrap(t);
		t = PersonManagerSetup.createPersonManagerSetup(t);
		return t;
	}
}
