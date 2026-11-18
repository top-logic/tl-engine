/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.migration.model.CreateTLClassProcessor;
import com.top_logic.element.model.migration.model.MigrationUtils;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of the model baseline entry written by {@link CreateTLClassProcessor}.
 */
@SuppressWarnings("javadoc")
public class TestCreateClassBaseline extends BasicTestCase {

	private static final String MODULE_NAME = "my.module";

	private static final String CLASS_NAME = "Base";

	public void testAbstract() throws ConfigurationException, MigrationException {
		Element classType = createClass("abstract=\"true\" without-primary-generalization=\"true\"");

		assertEquals(ClassConfig.TAG_NAME, classType.getTagName());
		assertEquals("true", classType.getAttribute(ClassConfig.ABSTRACT));
		assertFalse(classType.hasAttribute(ClassConfig.FINAL));

		TLClass type = loadClass(classType);
		assertTrue(type.isAbstract());
		assertFalse(type.isFinal());
	}

	public void testFinal() throws ConfigurationException, MigrationException {
		Element classType = createClass("final=\"true\" without-primary-generalization=\"true\"");

		assertEquals(ClassConfig.TAG_NAME, classType.getTagName());
		assertFalse(classType.hasAttribute(ClassConfig.ABSTRACT));
		assertEquals("true", classType.getAttribute(ClassConfig.FINAL));

		TLClass type = loadClass(classType);
		assertFalse(type.isAbstract());
		assertTrue(type.isFinal());
	}

	public void testConcrete() throws ConfigurationException, MigrationException {
		Element classType = createClass("without-primary-generalization=\"true\"");

		assertEquals(ClassConfig.TAG_NAME, classType.getTagName());
		assertFalse(classType.hasAttribute(ClassConfig.ABSTRACT));
		assertFalse(classType.hasAttribute(ClassConfig.FINAL));

		TLClass type = loadClass(classType);
		assertFalse(type.isAbstract());
		assertFalse(type.isFinal());
	}

	public void testAbstractFinalIsError() throws ConfigurationException {
		CreateTLClassProcessor.Config config =
			readProcessorConfig("abstract=\"true\" final=\"true\" without-primary-generalization=\"true\"");

		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		context.getInstance(config);

		assertTrue("Abstract and final class must be reported as configuration error.", log.hasErrors());
		Throwable problem = log.getFirstProblem();
		assertInstanceof(problem, ConfigurationException.class);
		assertEquals(CreateTLClassProcessor.Config.FINAL, ((ConfigurationException) problem).getPropertyName());
	}

	/**
	 * Reads a <code>create-class</code> processor with the given attributes from a migration file,
	 * applies it to a model baseline as {@link CreateTLClassProcessor} does, and returns the created
	 * type element.
	 */
	private Element createClass(String attributes) throws ConfigurationException, MigrationException {
		CreateTLClassProcessor.Config config = readProcessorConfig(attributes);

		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		context.getInstance(config);
		context.checkErrors();

		Document baseline = DOMUtil.parse(TypedConfiguration.toStringRaw(newBaseline()));
		Element classType = MigrationUtils.createClassType(log, baseline, config.getName(), config.isAbstract(),
			MigrationUtils.nullIfUnset(config, CreateTLClassProcessor.Config.FINAL), config);
		log.checkErrors();
		return classType;
	}

	private TLClass loadClass(Element classType) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		TLModel model = DynamicModelService.loadTransientModel(log, DOMUtil.toString(classType.getOwnerDocument()));
		TLClass type = (TLClass) TLModelUtil.findType(model, MODULE_NAME + ":" + CLASS_NAME);
		assertNotNull(type);
		return type;
	}

	private static ModelConfig newBaseline() {
		ModelConfig model = TypedConfiguration.newConfigItem(ModelConfig.class);
		ModuleConfig module = TypedConfiguration.newConfigItem(ModuleConfig.class);
		module.setName(MODULE_NAME);
		model.getModules().add(module);
		return model;
	}

	private static CreateTLClassProcessor.Config readProcessorConfig(String attributes)
			throws ConfigurationException {
		String migrationXML =
			"<" + MigrationUtil.ROOT_TAG_NAME + ">"
				+ "<version name=\"test\" module=\"test\"/>"
				+ "<processors>"
				+ "<create-class name=\"" + MODULE_NAME + ":" + CLASS_NAME + "\" " + attributes + "/>"
				+ "</processors>"
				+ "</" + MigrationUtil.ROOT_TAG_NAME + ">";

		InstantiationContext context = new DefaultInstantiationContext(TestCreateClassBaseline.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		MigrationConfig migration = (MigrationConfig) new ConfigurationReader(context,
			Collections.singletonMap(MigrationUtil.ROOT_TAG_NAME, descriptor))
				.setSource(CharacterContents.newContent(migrationXML)).read();
		context.checkErrors();

		List<PolymorphicConfiguration<? extends MigrationProcessor>> processors = migration.getProcessors();
		assertEquals(1, processors.size());
		return (CreateTLClassProcessor.Config) processors.get(0);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCreateClassBaseline.class, ResourcesModule.Module.INSTANCE));
	}

}
