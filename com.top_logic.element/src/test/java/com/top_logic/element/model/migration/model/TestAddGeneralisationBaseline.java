/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of {@link MigrationUtils#addGeneralisation(com.top_logic.basic.Log, Document, QualifiedTypeName, QualifiedTypeName)}
 * in the model baseline, especially that adding an existing generalisation changes nothing.
 */
@SuppressWarnings("javadoc")
public class TestAddGeneralisationBaseline extends BasicTestCase {

	private static final String MODULE_NAME = "my.module";

	private static final String OTHER_MODULE_NAME = "my.other";

	public void testAddTwiceSameModule() throws MigrationException {
		Document baseline = newBaseline();
		BufferingProtocol log = new BufferingProtocol();

		QualifiedTypeName special = typeName(MODULE_NAME, "Special");
		QualifiedTypeName base = typeName(MODULE_NAME, "Base");

		assertTrue(MigrationUtils.addGeneralisation(log, baseline, special, base));
		assertFalse(MigrationUtils.addGeneralisation(log, baseline, special, base));
		log.checkErrors();

		assertGeneralisations(baseline, "Base");
	}

	public void testAddTwiceOtherModule() throws MigrationException {
		Document baseline = newBaseline();
		BufferingProtocol log = new BufferingProtocol();

		QualifiedTypeName special = typeName(MODULE_NAME, "Special");
		QualifiedTypeName base = typeName(OTHER_MODULE_NAME, "OtherBase");

		assertTrue(MigrationUtils.addGeneralisation(log, baseline, special, base));
		assertFalse(MigrationUtils.addGeneralisation(log, baseline, special, base));
		log.checkErrors();

		assertGeneralisations(baseline, OTHER_MODULE_NAME + ":OtherBase");
	}

	public void testAddDifferent() throws MigrationException {
		Document baseline = newBaseline();
		BufferingProtocol log = new BufferingProtocol();

		QualifiedTypeName special = typeName(MODULE_NAME, "Special");

		assertTrue(MigrationUtils.addGeneralisation(log, baseline, special, typeName(MODULE_NAME, "Base")));
		assertTrue(
			MigrationUtils.addGeneralisation(log, baseline, special, typeName(OTHER_MODULE_NAME, "OtherBase")));
		log.checkErrors();

		assertGeneralisations(baseline, "Base", OTHER_MODULE_NAME + ":OtherBase");
	}

	private static void assertGeneralisations(Document baseline, String... expectedTypes) {
		Element special = findClass(baseline, "Special");
		NodeList extensions = special.getElementsByTagName(ExtendsConfig.TAG_NAME);
		assertEquals(expectedTypes.length, extensions.getLength());
		for (int i = 0; i < expectedTypes.length; i++) {
			assertEquals(expectedTypes[i], ((Element) extensions.item(i)).getAttribute(ExtendsConfig.TYPE));
		}
	}

	private static Element findClass(Document baseline, String name) {
		NodeList classes = baseline.getElementsByTagName(ClassConfig.TAG_NAME);
		for (int i = 0; i < classes.getLength(); i++) {
			Element type = (Element) classes.item(i);
			if (name.equals(type.getAttribute("name"))) {
				return type;
			}
		}
		fail("No class '" + name + "' in baseline.");
		return null;
	}

	private static QualifiedTypeName typeName(String module, String type) {
		QualifiedTypeName result = TypedConfiguration.newConfigItem(QualifiedTypeName.class);
		result.setName(TLModelUtil.qualifiedName(module, type));
		return result;
	}

	private static Document newBaseline() {
		ModelConfig model = TypedConfiguration.newConfigItem(ModelConfig.class);
		model.getModules().add(newModule(MODULE_NAME, "Special", "Base"));
		model.getModules().add(newModule(OTHER_MODULE_NAME, "OtherBase"));
		return DOMUtil.parse(TypedConfiguration.toStringRaw(model));
	}

	private static ModuleConfig newModule(String name, String... classNames) {
		ModuleConfig module = TypedConfiguration.newConfigItem(ModuleConfig.class);
		module.setName(name);
		for (String className : classNames) {
			ClassConfig type = TypedConfiguration.newConfigItem(ClassConfig.class);
			type.setName(className);
			module.getTypes().add(type);
		}
		return module;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAddGeneralisationBaseline.class, ResourcesModule.Module.INSTANCE));
	}

}
