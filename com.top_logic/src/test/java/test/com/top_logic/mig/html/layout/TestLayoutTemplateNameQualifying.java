/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutStorage;

/**
 * Test component name qualifying for instantiated layout templates.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TestLayoutTemplateNameQualifying extends BasicTestCase {

	private Person _person;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_person = TestPerson.createPerson("person");
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_person);

		LayoutStorage.deleteLayoutsFromDatabase(null);
		LayoutStorage.deleteLayoutsFromDatabase(_person);

		super.tearDown();
	}

	/**
	 * Tests instantiated layout template name qualifying from database.
	 */
	public void testPersistentLayoutTemplateNameQualifying() throws ConfigurationException {
		String layoutName1 = "testQualifying1";
		String layoutName2 = "testQualifying2";

		DynamicComponentDefinition definition = createTestDefinition();

		ConfigurationItem config1 = createSimpleComponentConfig(definition, "foo");
		ConfigurationItem config2 = createSimpleComponentConfig(definition, "bar");

		LayoutStorage.storeLayout(_person, layoutName1, definition.definitionFile(), config1);
		LayoutStorage.storeLayout(_person, layoutName2, definition.definitionFile(), config2);

		Config persistentLayout1 =
			LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), _person, layoutName1);
		Config persistentLayout2 =
			LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), _person, layoutName2);

		ComponentName name1 = persistentLayout1.getName();
		ComponentName name2 = persistentLayout2.getName();

		assertEquals(name1.toString(), "testQualifying1#SimpleComponent");
		assertEquals(name2.toString(), "testQualifying2#SimpleComponent");
		assertEquals(name1.localName(), "SimpleComponent");
		assertEquals(name2.localName(), "SimpleComponent");
	}

	/**
	 * Tests instantiated layout template name qualifying from file.
	 */
	public void testLayoutTemplateNameQualifying() throws ConfigurationException {
		String layoutName1 = "testQualifying1";
		String layoutName2 = "testQualifying2";

		DynamicComponentDefinition definition = createTestDefinition();

		ConfigurationItem config1 = createSimpleComponentConfig(definition, "foo");
		ConfigurationItem config2 = createSimpleComponentConfig(definition, "bar");

		Config layout1 =
			LayoutTemplateUtils.getInstantiatedLayoutTemplate(definition.definitionFile(), config1, layoutName1);
		Config layout2 =
			LayoutTemplateUtils.getInstantiatedLayoutTemplate(definition.definitionFile(), config2, layoutName2);

		ComponentName name1 = layout1.getName();
		ComponentName name2 = layout2.getName();

		assertEquals(name1.toString(), "testQualifying1#SimpleComponent");
		assertEquals(name2.toString(), "testQualifying2#SimpleComponent");
		assertEquals(name1.localName(), "SimpleComponent");
		assertEquals(name2.localName(), "SimpleComponent");
	}

	private ConfigurationItem createSimpleComponentConfig(DynamicComponentDefinition definition, String propertyValue) {
		ConfigurationDescriptor descriptor = definition.descriptor();
		ConfigurationItem config = descriptor.factory().createNew();

		config.update(descriptor.getProperty("content"), propertyValue);

		return config;
	}

	private DynamicComponentDefinition createTestDefinition() {
		Optional<DynamicComponentDefinition> templateDefinition =
			LayoutTemplateUtils.getTemplateComponentDefinition("test.com.top_logic/namedSimpleComponent.template.xml");

		assertEquals("Typed layout template could not be read.", templateDefinition.isPresent(), true);

		return templateDefinition.get();
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestPersistentLayouts}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestLayoutTemplateNameQualifying.class);

		test = ServiceTestSetup.createSetup(test, LayoutStorage.Module.INSTANCE);
		test = ServiceTestSetup.createSetup(test, DynamicComponentService.Module.INSTANCE);
		test = PersonManagerSetup.createPersonManagerSetup(test);

		return test;
	}

}
