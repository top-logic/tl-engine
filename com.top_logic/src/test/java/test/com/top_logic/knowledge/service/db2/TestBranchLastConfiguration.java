/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.module.ModuleException;

/**
 * Test that the configuration file {@value #BRANCH_LAST_CONFIG} switches on
 * {@link SchemaConfiguration#isBranchLast()} for the default type system of an application.
 *
 * <p>
 * The file is added to the application configuration the way a <code>metaConf.txt</code> entry
 * adds it, e.g. the one of the <code>with-h2</code> profile of the application parent POM.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchLastConfiguration extends BasicTestCase {

	/**
	 * Name of the configuration file switching on {@link SchemaConfiguration#isBranchLast()}.
	 */
	public static final String BRANCH_LAST_CONFIG = "top-logic.branchLast.config.xml";

	private static final String CONF_DIR = "/WEB-INF/conf/";

	/**
	 * With {@value #BRANCH_LAST_CONFIG}, the branch column is placed last, and the other settings
	 * of the default type system are kept.
	 */
	public void testBranchLastConfig() throws ModuleException {
		SchemaConfiguration before = defaultSchema();

		XMLPropertiesConfig oldConfig = XMLProperties.Module.INSTANCE.config();
		XMLPropertiesConfig newConfig = oldConfig.clone();
		newConfig.pushAdditionalContent(FileManager.getInstance().getData(CONF_DIR + BRANCH_LAST_CONFIG));
		XMLProperties.restartXMLProperties(newConfig);
		try {
			SchemaConfiguration schema = defaultSchema();
			assertTrue(schema.isBranchLast());
			assertEquals(before.hasMultipleBranches(), schema.hasMultipleBranches());
			assertEquals(before.getDeclarations().size(), schema.getDeclarations().size());
			assertEquals(before.getSchemas().size(), schema.getSchemas().size());
		} finally {
			XMLProperties.restartXMLProperties(oldConfig);
		}
		assertEquals(before.isBranchLast(), defaultSchema().isBranchLast());
	}

	private static SchemaConfiguration defaultSchema() {
		ApplicationTypes types = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
		return types.getTypeSystem(SchemaConfiguration.DEFAULT_NAME).getConfig();
	}

	/**
	 * The suite of this test.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestBranchLastConfiguration.class);
	}

}
