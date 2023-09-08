/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured;

import java.io.IOException;
import java.util.Collection;
import java.util.MissingResourceException;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ElementConfigUtil;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.model.config.ScopeConfig;

/**
 * Test case for parsing {@link ModelConfig} configurations.
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
@SuppressWarnings("javadoc")
public class TestStructuredElementFactoryConfiguration extends TestCase {

	private final static String FACTORY_CONFIG_FILE_1 = "/WEB-INF/xml/model1/projElement.model.xml";

	private final static String FACTORY_CONFIG_FILE_2 = "/WEB-INF/xml/model2/prodElement.model.xml";

	private final static String[] FACTORY_CONFIG_FILE_3 = {
		"/WEB-INF/xml/model3/prodElement.model.xml",
		"/WEB-INF/xml/model3/projElement.model.xml",
	};

	private final static String FACTORY_CONFIG_FILE_4 = "/WEB-INF/xml/model4/projElement.model.xml";

	private final static String FACTORY_CONFIG_FILE_5 = "/WEB-INF/xml/model5/projElement.model.xml";

	public TestStructuredElementFactoryConfiguration(String name) {
		super(name);
	}
	
	public void testMergeWithConfig2() throws Exception {
		final ModelConfig config1 = config1();
    	
		// check initial config
		ScopeConfig projectModule = config1.getModule("projElement");
		assertNotNull(projectModule);
		assertNotNull(ElementConfigUtil.getObjectType(projectModule, "Test.Global"));
		assertNotNull(ElementConfigUtil.getObjectType(projectModule, "Test.Global.Sub"));
    	
		Collection<ModuleConfig> structuresBefore = config1.getModules();
		assertEquals(1, structuresBefore.size());
		ModuleConfig projectStructureBefore = ElementConfigUtil.getStructureOrFail(config1, "projElement");
		assertNotNull(projectStructureBefore);
		assertEquals(5, countClasses(projectStructureBefore));
		assertEquals("projElement", projectStructureBefore.getName());
		assertEquals("ProjectRoot", ElementConfigUtil.getDefaultSingletonType(projectStructureBefore));
		ClassConfig projectRootBefore = ElementConfigUtil.getClassType(projectStructureBefore, "ProjectRoot");
		assertNotNull(projectRootBefore);
		ClassConfig sideProjectBefore = ElementConfigUtil.getClassType(projectStructureBefore, "Sideproject");
		assertNull(sideProjectBefore);
    	
		// config1 can be merged with config2 (two disjunct structures)
		ElementConfigUtil.merge(config1, config2());
    	
		ScopeConfig productModule = config1.getModule("prodElement");
		assertNotNull(productModule);
		assertNull(ElementConfigUtil.getObjectType(productModule, "Test.Global"));
		assertNull(ElementConfigUtil.getObjectType(productModule, "Test.Global.Sub"));
		assertNotNull(ElementConfigUtil.getObjectType(productModule, "Test.Global.SubSub"));

		Collection<ModuleConfig> structuresAfter = config1.getModules();
		assertEquals(2, structuresAfter.size());
    	// old structure not changed
		ModuleConfig projectStructureAfter = ElementConfigUtil.getStructureOrFail(config1, "projElement");
		assertNotNull(projectStructureAfter);
		assertEquals(5, countClasses(projectStructureAfter));
		assertEquals("projElement", projectStructureAfter.getName());
		assertEquals("ProjectRoot", ElementConfigUtil.getDefaultSingletonType(projectStructureAfter));
		ClassConfig projectRootAfter = ElementConfigUtil.getClassType(projectStructureAfter, "ProjectRoot");
		assertNotNull(projectRootAfter);
		ClassConfig sideProjectAfter = ElementConfigUtil.getClassType(projectStructureAfter, "Sideproject");
		assertNull(sideProjectAfter);
    	
    	// new structure 
		ModuleConfig productStructureAfter = ElementConfigUtil.getStructureOrFail(config1, "prodElement");
		assertNotNull(productStructureAfter);
		assertEquals(4, countClasses(productStructureAfter));
		assertEquals("prodElement", productStructureAfter.getName());
		assertEquals("RootElement", ElementConfigUtil.getDefaultSingletonType(productStructureAfter));
		ClassConfig productRootAfter = ElementConfigUtil.getClassType(productStructureAfter, "RootElement");
		assertNotNull(productRootAfter);
		ClassConfig sidePrjectAfter = ElementConfigUtil.getClassType(productStructureAfter, "Sideproject");
		assertNull(sidePrjectAfter);
	}

	public void testMergeWithConfig3() throws Exception {
    	// config can NOT be merged with config3
		assertMergeFails(config1(), config3());
	}

	public void testMergeWithConfig4() throws Exception {
		ModelConfig config1 = config1();

		// config can be merged with config4 (disjunct elements in structure)
		ElementConfigUtil.merge(config1, config4());
    	
		ScopeConfig theGMEC3;
		theGMEC3 = config1.getModule("projElement");
		assertNotNull(theGMEC3);
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC3, "Test.Global"));
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC3, "Test.Global.Sub"));
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC3, "Test.Global.Side"));
    	
		Collection<ModuleConfig> theStructures = config1.getModules();
    	assertEquals(1, theStructures.size());
    	// old structure not changed
		ModuleConfig theStructure = ElementConfigUtil.getStructureOrFail(config1, "projElement");
    	assertNotNull(theStructure);
		assertEquals(6, countClasses(theStructure));
    	assertEquals("projElement", theStructure.getName());
    	assertEquals("ProjectRoot", ElementConfigUtil.getDefaultSingletonType(theStructure));
		ClassConfig projectRoot = ElementConfigUtil.getClassType(theStructure, "ProjectRoot");
		assertNotNull(projectRoot);
		ClassConfig sideProject = ElementConfigUtil.getClassType(theStructure, "Sideproject");
		assertNotNull(sideProject);
	}

	public void testMergeWithConfig5() throws Exception {
		ModelConfig config1 = config1();
		// config can be merged with config5 (but the root type is NOT changed, i.e. the root type
		// definition in the source config is ignored)
		ElementConfigUtil.merge(config1, config5());
    	
		ScopeConfig theGMEC4;
		theGMEC4 = config1.getModule("projElement");
		assertNotNull(theGMEC4);
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC4, "Test.Global"));
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC4, "Test.Global.Sub"));
		assertNotNull(ElementConfigUtil.getObjectType(theGMEC4, "Test.Global.Side"));
    	
		Collection<ModuleConfig> theStructures = config1.getModules();
    	assertEquals(1, theStructures.size());
    	// old structure not changed
		ModuleConfig theStructure = ElementConfigUtil.getStructureOrFail(config1, "projElement");
    	assertNotNull(theStructure);
		assertEquals(6, countClasses(theStructure));
    	assertEquals("projElement", theStructure.getName());
    	assertEquals("ProjectRoot", ElementConfigUtil.getDefaultSingletonType(theStructure)); // still the old one, not changed by merge
		ClassConfig projectRoot = ElementConfigUtil.getClassType(theStructure, "ProjectRoot");
		assertNotNull(projectRoot);
		ClassConfig sideProject = ElementConfigUtil.getClassType(theStructure, "Sideproject");
		assertNotNull(sideProject);
	}

	private ModelConfig config1() throws IOException {
		return loadConfig(FACTORY_CONFIG_FILE_1);
	}

	private ModelConfig config2() throws IOException {
		return loadConfig(FACTORY_CONFIG_FILE_2);
	}

	private ModelConfig config3() throws IOException {
		return loadConfig(FACTORY_CONFIG_FILE_3);
	}

	private ModelConfig config4() throws IOException {
		return loadConfig(FACTORY_CONFIG_FILE_4);
	}

	private ModelConfig config5() throws IOException {
		return loadConfig(FACTORY_CONFIG_FILE_5);
	}

	private ModelConfig loadConfig(String... fileNames) throws IOException {
		boolean resolve = false;
		ModelConfig result = null;
		for (String fileName : fileNames) {
			BinaryData file = FileManager.getInstance().getDataOrNull(fileName);
			if (result == null) {
				result = DefinitionReader.readElementConfig(file, null, resolve);
			} else {
				result = DefinitionReader.readElementConfig(file, result, resolve);
			}
		}
		return result;
	}

	private void assertMergeFails(ModelConfig base, ModelConfig aConfig2) {
		/* Suppress the logger output as it looks like a failing test */
		Logger.configureStdout("FATAL");
		try {
			ElementConfigUtil.merge(base, aConfig2);
	    	
	    	fail("Configs should fail to be merged.");
		} catch (IllegalArgumentException e) {
			// as expected
		} catch (MissingResourceException e) {
			// as expected
		} finally {
			/* Reset logging */
			Logger.configureStdout();
		}
	}

	private void writeConfig(ModelConfig aConfig) {
		Logger.info(aConfig.toString(), this);
	}
	
	private static int countClasses(ModuleConfig module) {
		int count = 0;
		for (@SuppressWarnings("unused") ClassConfig config : ElementConfigUtil.getClassTypes(module)) {
			count++;
		}
		return count;
	}

	/** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return ModuleLicenceTestSetup.setupModule(ServiceTestSetup.createSetup(
			TestStructuredElementFactoryConfiguration.class, AttributeSettings.Module.INSTANCE));
    }

}
