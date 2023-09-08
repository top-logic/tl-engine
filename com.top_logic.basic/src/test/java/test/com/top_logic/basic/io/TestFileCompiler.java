/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.FileManagerTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileCompiler;
import com.top_logic.basic.io.FileUtilities;

/**
 * Test the {@link FileCompiler}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestFileCompiler extends BasicTestCase {

    /** Path to a File used for Testing */
	static final String TARGET = "Target.txt";
    
    /** Path to a File used for Testing */
	static final String DYNAMIC_RESOURCE = "DynamicResource.txt";

    /**
     * Test Actual compilation of files.
     */
	public void testCompileFiles() throws IOException {
		File tmpDir = mkBaseDir();
        
		File dynamicSrc = new File(tmpDir, DYNAMIC_RESOURCE);
		File target = new File(tmpDir, TARGET);

		FileUtilities.writeStringToFile("", dynamicSrc);
        // Do this first otherwise checkDirty will trigger a re-compile.

		final File rootDir = new File("test/fixtures/TestFileCompiler");
		TestedFileCompiler.Config newConfig = newConfig("baseDir/", "file://" + target.getAbsolutePath());
		List<ResourceDeclaration> files = newConfig.getFiles();
		files.add(resourceDeclaration("a.txt"));
		files.add(resourceDeclaration("subDir/b.txt"));
		files.add(resourceDeclaration("/c.txt"));
		files.add(resourceDeclaration("/extDir/d.txt"));
		files.add(resourceDeclaration("file://" + dynamicSrc.getAbsolutePath()));
        
		FileUtilities.writeStringToFile("", target);

		assertEquals(0L, target.length());
		assertEquals(0L, dynamicSrc.length());
        
		FileManager.setInstance(new DefaultFileManager(rootDir));
		FileCompiler fc = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(newConfig);
		((TestedFileCompiler) fc).startUpAccessible();

		assertEquals("abcd", FileUtilities.readFileToString(target));
        provokeCheck(fc);
		assertEquals("abcd", FileUtilities.readFileToString(target));
		FileUtilities.writeStringToFile("x", dynamicSrc);
        provokeCheck(fc);
		assertEquals("abcdx", FileUtilities.readFileToString(target));

		((TestedFileCompiler) fc).shutDownAccessible();
    }

	private void provokeCheck(FileCompiler fc) {
		try {
			// On linux timestamp of files may be computed in seconds instead of milliseconds.
			// Therefore must wait to next second.
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
		fc.getTarget(); // provoke (noop) checkDirty ...
	}

    /** 
     * Test Compilation with missing files.
     */
	public void testBrokenInput() throws IOException {
        File baseDir = mkBaseDir();
        
		File target2 = new File(baseDir, DYNAMIC_RESOURCE);
        FileUtilities.writeStringToFile("", target2);
        // Do this first otherwise checkDirty will trigger a re-compile.

		TestedFileCompiler.Config newConfig = newConfig("file://" + baseDir.getPath(), TARGET);
		String nonExistingResource = "IsNichDa";
		newConfig.getFiles().add(resourceDeclaration(nonExistingResource));
		newConfig.getFiles().add(resourceDeclaration(DYNAMIC_RESOURCE));
        
		File target1 = new File(baseDir, TARGET);
        FileUtilities.writeStringToFile("", target1);

		BufferingProtocol p = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(p);
		FileCompiler fc = context.getInstance(newConfig);
		((TestedFileCompiler) fc).startUpAccessible();
		assertTrue(p.hasErrors());
		assertContains("Resource does not exist", p.getError());
		assertContains(nonExistingResource, p.getError());

        assertEquals(0L, target1.length());
        provokeCheck(fc); 
        assertEquals(0L, target1.length());
        FileUtilities.writeStringToFile("A String of length 21", target2);
        provokeCheck(fc);
        assertEquals(21, target1.length());

		((TestedFileCompiler) fc).shutDownAccessible();
    }

	private ResourceDeclaration resourceDeclaration(String resource) {
		ResourceDeclaration decl = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
		decl.setResource(resource);
		return decl;
	}

	private TestedFileCompiler.Config newConfig() {
		TestedFileCompiler.Config result = TypedConfiguration.newConfigItem(TestedFileCompiler.Config.class);
		result.setImplementationClass(TestedFileCompiler.class);
		return result;
	}

	private TestedFileCompiler.Config newConfig(String baseDir, String target) {
		TestedFileCompiler.Config newConfig = newConfig();
		newConfig.setAlwaysCheck(true);
		newConfig.setCheckInterval(0);
		newConfig.setDeployed(false);
		newConfig.setBaseDir(baseDir);
		newConfig.setTarget(target);
		return newConfig;
	}

	public static final class TestedFileCompiler extends FileCompiler {

		public static interface Config extends FileCompiler.Config {

			@Override
			@StringDefault(TARGET)
			String getTarget();
		}

		/**
		 * Creates a new {@link TestFileCompiler.TestedFileCompiler} from the given configuration.
		 * 
		 * @param context
		 *        {@link InstantiationContext} to instantiate sub configurations.
		 * @param config
		 *        Configuration for this {@link TestFileCompiler.TestedFileCompiler}.
		 */
		public TestedFileCompiler(InstantiationContext context, Config config) {
			super(context, config);
		}

		public void startUpAccessible() {
			startUp();
		}

		public void shutDownAccessible() {
			shutDown();
		}
    
    }

    private static File mkBaseDir() {
		return createdCleanTestDir("TestFileCompiler");
	}
    
    /**
     * Return the suite of tests to execute.
     */
    public static Test suite () {
		Test suite = new FileManagerTestSetup(new TestSuite(TestFileCompiler.class));

        // Test suite = TestFileCompiler("testIsDeployed");
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }

}

