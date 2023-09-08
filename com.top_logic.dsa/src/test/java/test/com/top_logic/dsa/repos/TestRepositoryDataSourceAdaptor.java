/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.repos;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dsa.repos.file.FileRepository;

/**
 * Test case for the {@link com.top_logic.dsa.repos.RepositoryDataSourceAdaptor}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestRepositoryDataSourceAdaptor extends AbstractTestRepositoryDataSourceAdaptor {

    /**
     * Default constructor.
     *
     * @param name of test to execute.
     */
    public TestRepositoryDataSourceAdaptor (String name) {
        super (name);
    }

	@Override
	public void doCleanup() {
		File   theTest  = new File(new File("tmp"), "test");
		File[] theFiles = theTest.listFiles();

		if (theFiles != null) {
			for (File theFile : theFiles) {
				if (theFile.getName().startsWith("p")) {
					FileUtilities.deleteR(theFile);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected PolymorphicConfiguration<?> getImplementationConfiguration() {
		String theBase = "tmp/" + TestRepositoryDataSourceAdaptor.class.getSimpleName() + "/p"
			+ StringServices.randomUUID().replace("-", "") + '/';
		FileRepository.Config theConfig = TypedConfiguration.newConfigItem(FileRepository.Config.class);

		theConfig.setImplementationClass(FileRepository.class);
		theConfig.setPath(theBase + "repository2");
		theConfig.setWorkarea(theBase + "workarea2");
		theConfig.setAttic(theBase + "attic2");

		return theConfig;
	}

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
		TestSuite suite = new TestSuite();

		suite.addTest(new TestSuite(TestRepositoryDataSourceAdaptor.class));
		suite.addTest(new TestRepositoryDataSourceAdaptor("doCleanup"));

        return DSATestSetup.createDSATestSetup(suite);
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        Logger.configureStdout("ERROR");
        junit.textui.TestRunner.run (suite ());
    }

}
