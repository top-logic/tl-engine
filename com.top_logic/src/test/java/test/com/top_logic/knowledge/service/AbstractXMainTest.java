/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMain;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.ComputationEx2;

/**
 * Testcase {@link XMain}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractXMainTest extends BasicTestCase {

	protected File _metaConfFile;

	private FileManager _testFileManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_testFileManager = FileManager.getInstance();
		InputStream metaConf = AbstractXMainTest.class.getResourceAsStream(ModuleLayoutConstants.META_CONF_NAME);
		File tmp = File.createTempFile(AbstractXMainTest.class.getSimpleName() + "_metaConf", ".txt");
		try {
			FileUtilities.copyToFile(metaConf, tmp);
		} finally {
			metaConf.close();
		}
		_metaConfFile = tmp;
	}

	@Override
	protected void tearDown() throws Exception {
		_metaConfFile.delete();
		// Reinstall test fileManager because test may install a different one.
		FileManager.setInstance(_testFileManager);
		super.tearDown();
	}

	/**
	 * Enhanced arguments be prepending "-m &lt;metaConf.txt-file&gt;" to the given array.
	 */
	protected String[] prependMetaConf(String... args) throws IOException {
		String[] enhanced = new String[args.length + 2];
		int index = 0;
		enhanced[index++] = "-m";
		enhanced[index++] = _metaConfFile.getCanonicalPath();
		System.arraycopy(args, 0, enhanced, index, args.length);
		return enhanced;
	}

	protected <T, E1 extends Throwable, E2 extends Throwable> T withTestFilemanager(
			ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		FileManager current = FileManager.getInstance();
		FileManager.setInstance(_testFileManager);
		try {
			return computation.run();
		} finally {
			FileManager.setInstance(current);
		}
	}

	protected static Test suite(Class<? extends AbstractXMainTest> testClass) {
		return TestUtils.doNotMerge(TLTestSetup.createTLTestSetup(testClass));
	}

}
