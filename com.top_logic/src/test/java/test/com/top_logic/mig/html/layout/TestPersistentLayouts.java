/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.SimpleComponent;

/**
 * Test for persistency of layouts.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPersistentLayouts extends BasicTestCase {

	private static final String BAR_CONTENT_VALUE = "bar";

	private static final String FOO_CONTENT_VALUE = "foo";

	private Person _firstPerson;

	private Person _secondPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_firstPerson = TestPerson.createPerson("firstPerson");
		_secondPerson = TestPerson.createPerson("secondPerson");
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_secondPerson);
		TestPerson.deletePersonAndUser(_firstPerson);

		LayoutStorage.deleteLayoutsFromDatabase(null);
		LayoutStorage.deleteLayoutsFromDatabase(_firstPerson);
		LayoutStorage.deleteLayoutsFromDatabase(_secondPerson);

		super.tearDown();
	}

	public void testPersistUserLayoutTemplate() throws ConfigurationException {
		String layoutName = "layoutName1";
		DynamicComponentDefinition definition = createTestDefinition();
		ConfigurationItem config = createSimpleComponentConfig(definition, FOO_CONTENT_VALUE);

		LayoutStorage.storeLayout(_firstPerson, layoutName, definition.definitionFile(), config);

		assertSimplePersistentTemplateLayout(_firstPerson, layoutName, FOO_CONTENT_VALUE);

		assertEmptyPersistentTemplateLayout(_secondPerson, layoutName);
		assertEmptyPersistentTemplateLayout(null, layoutName);
	}

	private void assertSimplePersistentTemplateLayout(Person person, String layoutName, String contentValue) throws ConfigurationException {
		Config persistentLayout =
			LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), person, layoutName);

		assertSimpleComponentConfig(persistentLayout, contentValue);
	}

	private void assertEmptyPersistentTemplateLayout(Person person, String layoutName) throws ConfigurationException {
		assertEquals(null, LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), person, layoutName));
	}

	private void assertSimpleComponentConfig(Config persistentLayout, String contentValue) {
		assertInstanceof("Layout config has not correct type.", persistentLayout, SimpleComponent.Config.class);
		assertEquals("Property value is different.", contentValue,
			((SimpleComponent.Config) persistentLayout).getContent());
	}

	private ConfigurationItem createSimpleComponentConfig(DynamicComponentDefinition definition, String propertyValue) {
		ConfigurationDescriptor descriptor = definition.descriptor();
		ConfigurationItem config = descriptor.factory().createNew();

		config.update(descriptor.getProperty("content"), propertyValue);

		return config;
	}

	private DynamicComponentDefinition createTestDefinition() {
		Optional<DynamicComponentDefinition> templateDefinition =
			LayoutTemplateUtils.getTemplateComponentDefinition("test.com.top_logic/test.template.xml");

		assertEquals("Typed layout template could not be read.", templateDefinition.isPresent(), true);

		return templateDefinition.get();
	}

	public void testPersistGlobalLayoutTemplate() throws ConfigurationException {
		String layoutName = "layoutName2";
		DynamicComponentDefinition definition = createTestDefinition();
		ConfigurationItem config = createSimpleComponentConfig(definition, FOO_CONTENT_VALUE);

		LayoutTemplateUtils.storeLayout(layoutName, definition.definitionFile(), config);

		assertSimplePersistentTemplateLayout(null, layoutName, FOO_CONTENT_VALUE);
		assertSimplePersistentTemplateLayout(_secondPerson, layoutName, FOO_CONTENT_VALUE);
		assertSimplePersistentTemplateLayout(_firstPerson, layoutName, FOO_CONTENT_VALUE);
	}

	public void testReleaseUserLayoutTemplates() throws ConfigurationException {
		String layoutName = "layoutName3";
		DynamicComponentDefinition definition = createTestDefinition();

		ConfigurationItem fooConfig = createSimpleComponentConfig(definition, FOO_CONTENT_VALUE);
		ConfigurationItem barConfig = createSimpleComponentConfig(definition, BAR_CONTENT_VALUE);

		LayoutStorage.storeLayout(_firstPerson, layoutName, definition.definitionFile(), fooConfig);
		LayoutStorage.storeLayout(_secondPerson, layoutName, definition.definitionFile(), barConfig);

		assertSimplePersistentTemplateLayout(_firstPerson, layoutName, FOO_CONTENT_VALUE);
		assertSimplePersistentTemplateLayout(_secondPerson, layoutName, BAR_CONTENT_VALUE);

		assertEmptyPersistentTemplateLayout(null, layoutName);

		LayoutStorage.getInstance().releaseLayouts(_firstPerson);

		assertSimplePersistentTemplateLayout(null, layoutName, FOO_CONTENT_VALUE);
		assertSimplePersistentTemplateLayout(_firstPerson, layoutName, FOO_CONTENT_VALUE);
		assertSimplePersistentTemplateLayout(_secondPerson, layoutName, BAR_CONTENT_VALUE);
	}

	public void testLayoutsRemovedOnPersonRemove() throws ConfigurationException {
		Person person = TestPerson.createPerson("testRemovePerson");
		String layoutName = "layoutName4";
		DynamicComponentDefinition definition = createTestDefinition();

		ConfigurationItem config = createSimpleComponentConfig(definition, FOO_CONTENT_VALUE);

		LayoutStorage.storeLayout(person, layoutName, definition.definitionFile(), config);

		assertSimplePersistentTemplateLayout(person, layoutName, FOO_CONTENT_VALUE);

		TestPerson.deletePersonAndUser(person);

		assertEquals(false, LayoutStorage.getInstance().getLayoutFromDatabase(ThemeFactory.getTheme(), person, layoutName) != null);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPersistentLayouts}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestPersistentLayouts.class);

		test = ServiceTestSetup.createSetup(test, LayoutStorage.Module.INSTANCE);
		test = ServiceTestSetup.createSetup(test, DynamicComponentService.Module.INSTANCE);
		test = PersonManagerSetup.createPersonManagerSetup(test);

		return test;
	}

}

