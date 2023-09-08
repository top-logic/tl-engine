/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import static test.com.top_logic.ComponentTestUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleComponent;

/**
 * Tests for {@link LayoutUtils}.
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@SuppressWarnings("javadoc")
public class TestLayoutUtils extends BasicTestCase {

	public interface TestIncrementComponentConfig extends SimpleComponent.Config {

		SimpleComponent.Config getInnerComponent();

		void setInnerComponent(SimpleComponent.Config component);

		ThingWithComponent getThingWithComponent();

		void setThingWithComponent(ThingWithComponent thing);

		TestIncrementComponentConfig[] getComponentsArray();

		void setComponentsArray(TestIncrementComponentConfig[] array);

		List<LayoutComponent.Config> getComponentsList();

		@Key(LayoutComponent.Config.NAME)
		Map<ComponentName, LayoutComponent.Config> getComponentsMap();

	}

	public interface ThingWithComponent extends ConfigurationItem {
		LayoutComponent.Config getComponent();

		void setComponent(LayoutComponent.Config component);
	}

	public static class TestIncrementComponent extends SimpleComponent {

		public TestIncrementComponent(InstantiationContext context, TestIncrementComponentConfig config)
				throws ConfigurationException {
			super(context, config);
		}
	}

    /** Test finding the common parent */
	public void testCommonParent() {
        // Create a Layout to search in
		SimpleComponent l0000 = newSimpleComponent("ignore", "ignore");
		SimpleComponent r0001 = newSimpleComponent("ignore", "ignore");
		Layout l000 = newLayout(Layout.VERTICAL);
        l000.addComponent(l0000);
        l000.addComponent(r0001);

		SimpleComponent r001 = newSimpleComponent("ignore", "ignore");
		Layout l00 = newLayout(Layout.HORIZONTAL);
        l00.addComponent(l000);
        l00.addComponent(r001);

		SimpleComponent r01 = newSimpleComponent("ignore", "ignore");
		Layout l0 = newLayout(Layout.VERTICAL);
        l0.addComponent(l00);
        l0.addComponent(r01);

		SimpleComponent r1 = newSimpleComponent("ignore", "ignore");
		Layout l = newLayout(Layout.HORIZONTAL);
        l.addComponent(l0);
        l.addComponent(r1);

		MainLayout main = newMainLayout();

        main.addComponent(l);
        
        assertSame(l000, LayoutUtils.findCommonParent(l0000,l0000));
        assertSame(l000, LayoutUtils.findCommonParent(l0000,r0001));
        assertSame(main, LayoutUtils.findCommonParent(l0000,l    ));
        assertSame(l,    LayoutUtils.findCommonParent(l0000,l0   ));

    }

	public static interface QualifyNameTestConfig extends ConfigurationItem {
		String COMP_NAME = "compName";

		List<QualifyNameTestConfig> getList();

		@Key(QualifyNameTestConfig.COMP_NAME)
		List<QualifyNameTestConfig> getIndexedList();

		QualifyNameTestConfig getItem();

		void setItem(QualifyNameTestConfig item);

		@Name(COMP_NAME)
		@Mandatory
		ComponentName getName();

		void setName(ComponentName name);

		@ListBinding(tag = "entry", attribute = "name")
		List<ComponentName> getNameList();

		void setNameList(List<ComponentName> value);

		@Key(QualifyNameTestConfig.COMP_NAME)
		Map<ComponentName, QualifyNameTestConfig> getMap();

		QualifyNameTestConfig[] getArray();

		void setArray(QualifyNameTestConfig[] array);

	}

	public void testQualifyComponentName() {
		ComponentName unqualifiedName = ComponentName.newName("testName");
		assertTrue(unqualifiedName.isLocalName());
		LayoutComponent.Config conf = newSimpleComponent(unqualifiedName, "header", "content").getConfig();
		LayoutUtils.qualifyComponentNames("scope", conf);
		ComponentName qualifiedName = conf.getName();
		assertFalse(qualifiedName.isLocalName());
		assertEquals(ComponentName.newName("scope", unqualifiedName.localName()), qualifiedName);
		LayoutUtils.qualifyComponentNames("scope2", conf);
		assertSame("Qualify a qualified name is a noop.", qualifiedName, conf.getName());
	}

	public void testQualifyComponentNameRecursive() {
		QualifyNameTestConfig config = newConf("name1");
		QualifyNameTestConfig item = newConf("item");
		item.setItem(newConf("innerItem"));
		config.setItem(item);
		config.getList().add(newConf("scope1", "name2"));
		config.getList().add(newConf("duplicateName"));
		config.getList().add(newConf("duplicateName"));
		config.setNameList(list(toName("scope1", "name2"), toName("duplicateName"), toName("duplicateName")));
		config.getIndexedList().add(newConf("scope1", "name2"));
		config.getIndexedList().add(newConf("name3"));
		QualifyNameTestConfig mapConf1 = newConf("scope1", "map1");
		QualifyNameTestConfig mapConf2 = newConf("map2");
		config.getMap().put(mapConf1.getName(), mapConf1);
		config.getMap().put(mapConf2.getName(), mapConf2);
		config.setArray(new QualifyNameTestConfig[] { newConf("name2"), newConf("scope1", "name2") });

		LayoutUtils.qualifyComponentNames("scope", config);
		assertEquals(toName("scope", "name1"), config.getName());
		assertEquals("Not descended in item property.", toName("scope", "item"), config.getItem().getName());
		assertEquals("Not recursivly descended in item property.", toName("scope", "innerItem"),
			config.getItem().getItem().getName());
		assertEquals(3, config.getList().size());
		assertEquals("Qualified name changed.", toName("scope1", "name2"), config.getList().get(0).getName());
		assertEquals(toName("scope", "duplicateName"), config.getList().get(1).getName());
		assertEquals(toName("scope", "duplicateName"), config.getList().get(2).getName());
		assertEquals(
			list(toName("scope1", "name2"), toName("scope", "duplicateName"), toName("scope", "duplicateName")),
			config.getNameList());
		assertEquals(2, config.getIndexedList().size());
		assertEquals("Qualified name changed.", toName("scope1", "name2"), config.getIndexedList().get(0).getName());
		assertEquals(toName("scope", "name3"), config.getIndexedList().get(1).getName());
		assertEquals(2, config.getMap().size());
		assertEquals(toName("scope1", "map1"), config.getMap().get(toName("scope1", "map1")).getName());
		assertEquals(toName("scope", "map2"), config.getMap().get(toName("scope", "map2")).getName());
		assertEquals(new ComponentName[] { toName("scope", "name2"), toName("scope1", "name2") },
			Arrays.stream(config.getArray()).map(arrayConfig -> arrayConfig.getName()).toArray());
	}

	private QualifyNameTestConfig newConf(String name) {
		return newConf(toName(name));
	}

	private QualifyNameTestConfig newConf(String scope, String name) {
		return newConf(toName(scope, name));
	}

	private ComponentName toName(String scope, String name) {
		return ComponentName.newName(scope, name);
	}

	private ComponentName toName(String name) {
		return ComponentName.newName(name);
	}

	private QualifyNameTestConfig newConf(ComponentName name) {
		QualifyNameTestConfig config = TypedConfiguration.newConfigItem(QualifyNameTestConfig.class);
		config.setName(name);
		return config;
	}

	public void testOverlay() {
		String content = "content";
		String header = "header";
		SimpleComponent.Config baseConf = newSimpleComponentConfig(header, content);
		baseConf.setName(ComponentName.newName("compName"));
		assertEquals(content, baseConf.getContent());
		assertEquals(header, baseConf.getHeader());
		SimpleComponent.Config incremented = (SimpleComponent.Config) modify(baseConf,
			Collections.singletonMap(baseConf.getName(), "testOverlaySetContent.xml"));
		assertEquals("newContent", incremented.getContent());
		assertEquals(header, incremented.getHeader());
	}

	LayoutComponent.Config modify(LayoutComponent.Config base, Map<ComponentName, String> incrementFiles) {
		return LayoutUtils.modify(base, config -> {
			String incrementFile = incrementFiles.get(config.getName());
			if (incrementFile == null) {
				return config;
			}
			ConfigurationReader reader = new ConfigurationReader(
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, Collections.singletonMap("component",
					TypedConfiguration.getConfigurationDescriptor(LayoutComponent.Config.class)));
			reader.setBaseConfig(config);
			reader.setSource(testScript(incrementFile));
			try {
				return (LayoutComponent.Config) reader.read();
			} catch (ConfigurationException ex) {
				throw fail("Unable to increment", ex);
			}
		});
	}

	private ClassRelativeBinaryContent testScript(String suffix) {
		return ClassRelativeBinaryContent.withSuffix(getClass(), suffix);
	}

	public void testIncrementChangeComponentClass() {
		SimpleComponent.Config baseConf = newSimpleComponentConfig("header", "content");
		baseConf.setName(ComponentName.newName("compName"));
		SimpleComponent.Config incremented =
			(SimpleComponent.Config) modify(baseConf, Collections.singletonMap(baseConf.getName(),
				"testOverlayChangeClass.xml"));
		assertEquals(TestIncrementComponent.class, incremented.getImplementationClass());
	}

	public void testOverlayDeepChange() {
		ComponentName baseName = ComponentName.newName("base");
		ComponentName child1Name = ComponentName.newName("child1");
		ComponentName child2Name = ComponentName.newName("child2");
		ComponentName child3Name = ComponentName.newName("child3");
		ComponentName child4Name = ComponentName.newName("child4");
		ComponentName child5Name = ComponentName.newName("child5");
		TestIncrementComponentConfig base = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		base.setName(baseName);
		TestIncrementComponentConfig child1 = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		child1.setName(child1Name);
		TestIncrementComponentConfig child2 = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		child2.setName(child2Name);
		TestIncrementComponentConfig child3 = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		child3.setName(child3Name);
		TestIncrementComponentConfig child4 = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		child4.setName(child4Name);
		TestIncrementComponentConfig child5 = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		child5.setName(child5Name);

		base.setInnerComponent(child1);
		base.getComponentsList().add(child2);
		base.getComponentsMap().put(child3.getName(), child3);
		base.setComponentsArray(new TestIncrementComponentConfig[] { child4 });
		ThingWithComponent thing = TypedConfiguration.newConfigItem(ThingWithComponent.class);
		thing.setComponent(child5);
		base.setThingWithComponent(thing);

		assertEquals("", base.getInnerComponent().getContent());
		assertEquals("", ((SimpleComponent.Config) base.getComponentsList().get(0)).getContent());
		assertEquals("", ((SimpleComponent.Config) base.getComponentsMap().get(child3Name)).getContent());
		assertEquals("", base.getComponentsArray()[0].getContent());
		assertEquals("", ((SimpleComponent.Config) base.getThingWithComponent().getComponent()).getContent());
		Map<ComponentName, String> overlays = new MapBuilder<ComponentName, String>()
			.put(child1.getName(), "testOverlaySetContent.xml")
			.put(child2.getName(), "testOverlaySetContent.xml")
			.put(child3.getName(), "testOverlaySetContent.xml")
			.put(child4.getName(), "testOverlaySetContent.xml")
			.put(child5.getName(), "testOverlaySetContent.xml")
				.toMap();
		TestIncrementComponentConfig incremented = (TestIncrementComponentConfig) modify(base, overlays);
		assertSame(base, incremented);
		assertEquals("newContent", base.getInnerComponent().getContent());
		assertEquals("newContent", ((SimpleComponent.Config) base.getComponentsList().get(0)).getContent());
		assertEquals("newContent", ((SimpleComponent.Config) base.getComponentsMap().get(child3Name)).getContent());
		assertEquals("newContent", base.getComponentsArray()[0].getContent());
		assertEquals("newContent", ((SimpleComponent.Config) base.getThingWithComponent().getComponent()).getContent());
	}

	public void testOverlayRecursively() {
		ComponentName baseName = ComponentName.newName("base");
		ComponentName child1Name = ComponentName.newName("child1");
		TestIncrementComponentConfig base = TypedConfiguration.newConfigItem(TestIncrementComponentConfig.class);
		base.setName(baseName);
		assertNull(base.getInnerComponent());

		MapBuilder<ComponentName, String> overlays = new MapBuilder<ComponentName, String>()
			.put(baseName, "testOverlayAddInner.xml");
		TestIncrementComponentConfig incremented1 = (TestIncrementComponentConfig) modify(base, overlays.toMap());
		assertNotNull(incremented1.getInnerComponent());
		assertEquals("", ((TestIncrementComponentConfig) incremented1.getInnerComponent()).getContent());

		overlays.put(child1Name, "testOverlaySetContent.xml");
		TestIncrementComponentConfig incremented2 = (TestIncrementComponentConfig) modify(base, overlays.toMap());
		assertNotNull(incremented2.getInnerComponent());
		assertEquals("newContent", ((TestIncrementComponentConfig) incremented2.getInnerComponent()).getContent());
	}

    /**
     * the suite of Tests to execute 
     */
    public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestLayoutUtils.class, RequestLockFactory.Module.INSTANCE));
    }

    /**
     * main function for direct testing.
     */
    public static void main(String[] argv) {
        junit.textui.TestRunner.run(suite());
    }
}
