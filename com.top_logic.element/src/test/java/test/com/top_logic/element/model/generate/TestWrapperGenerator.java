/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.generate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

import org.apache.commons.io.output.TeeOutputStream;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.element.model.generate.WrapperGenerator;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * Test case for {@link WrapperGenerator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestWrapperGenerator extends TestCase {

	private static final String MESSAGE_VALUE_INPUT = "This is \n a text with \r\n new lines";
	private static final String MESSAGE_VALUE = "This is \n a text with \n new lines";

	public void testGenerate() throws Exception {
		File root = BasicTestCase.createdCleanTestDir("TestWrapperGenerator");
		File app1 = app1();

		TLModelImpl model = new TLModelImpl();
		TLModule foo = model.addModule(model, "foo");
		TLModule bar = model.addModule(model, "bar");
		String fooResources =
			app1.getAbsolutePath() + "/WEB-INF/conf/resources/" + TLModelNamingConvention.resourcesFileName(foo, "de");

		generateWrappers(root, app1, foo);
		changeProperties(fooResources);
		generateWrappers(root, app1, foo);
		checkProperties(fooResources);
		generateWrappers(root, app1, bar);
	}

	public void testGenerateWithResourceStack() throws Exception {
		File root = BasicTestCase.createdCleanTestDir("TestWrapperGeneratorStack");
		File app1 = app1();

		List<Path> stack = new ArrayList<>();
		stack.add(app1.toPath());
		stack.addAll(Workspace.applicationModules());

		WrapperGenerator.generate(stack, generatorArgs(root, "foo"));

		assertTrue("Interface of type 'A' not generated.", new File(root, "test/foo/A.java").isFile());
		assertTrue("Factory of module 'foo' not generated.", new File(root, "test/foo/FooFactory.java").isFile());
		compileSources(root);
	}

	public void testResourceStackWithoutModule() throws Exception {
		File root = BasicTestCase.createdCleanTestDir("TestWrapperGeneratorStackMissing");

		List<Path> stack = Workspace.applicationModules();

		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintStream origErr = System.err;
		System.setErr(new PrintStream(new TeeOutputStream(origErr, errBuffer), true, StandardCharsets.UTF_8));
		try {
			WrapperGenerator.generate(stack, generatorArgs(root, "foo"));
			fail("Generation of a module missing in the resource stack must fail.");
		} catch (AbortExecutionException ex) {
			// Expected.
		} finally {
			System.setErr(origErr);
		}

		String errors = errBuffer.toString(StandardCharsets.UTF_8);
		assertTrue("Missing module not reported: " + errors, errors.contains("No module with name 'foo' defined."));
		assertTrue("Wrappers generated for a missing module.", listJavaFiles(root).isEmpty());
	}

	private static File app1() {
		File pkg = new File(ModuleLayoutConstants.SRC_TEST_DIR,
			TestWrapperGenerator.class.getPackage().getName().replace('.', '/'));
		return new File(pkg, "app1/" + ModuleLayoutConstants.WEBAPP_DIR);
	}

	private static String[] generatorArgs(File root, String moduleName) {
		return new String[] {
			WrapperGenerator.OUTPUT_DIR_OPTION, root.getAbsolutePath(),
			WrapperGenerator.MODULE_OPTION, moduleName,
			WrapperGenerator.LANG_OPTION, "de"
		};
	}

	private void generateWrappers(File root, File app, TLModule module) throws Exception, IOException {
		String[] args = {
			WrapperGenerator.DEPLOY_OPTION, app.getAbsolutePath(),
			WrapperGenerator.OUTPUT_DIR_OPTION, root.getAbsolutePath(),
			WrapperGenerator.MODULE_OPTION, module.getName(),
			WrapperGenerator.LANG_OPTION, "de"
		};
		WrapperGenerator.main(args);

		compileSources(root);
	}

	private void changeProperties(String path) throws FileNotFoundException, IOException {
		Properties properties = new Properties();

		try (FileInputStream fis =
			new FileInputStream(path)) {
			properties.load(fis);

			properties.forEach((k, v) -> {
				properties.setProperty((String) k, MESSAGE_VALUE_INPUT);
			});

		}
		try (FileOutputStream fos =
			new FileOutputStream(path);) {

			properties.store(fos, null);
		}
	}

	private void checkProperties(String path) throws FileNotFoundException, IOException {
		Properties properties = new Properties();

		try (FileInputStream fis =
			new FileInputStream(path)) {
			properties.load(fis);

			properties.forEach((k, v) -> {
				assertEquals(MESSAGE_VALUE, v);
			});

		}
	}

	private void compileSources(File root) throws IOException {
		Iterable<? extends File> sourceFiles = listJavaFiles(root);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
			compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
		}
	}

	private List<File> listJavaFiles(File root) throws IOException {
		ArrayList<File> result = new ArrayList<>();
		addJavaFiles(result, root);
		return result;
	}

	private void addJavaFiles(ArrayList<File> result, File root) throws IOException {
		for (File file : FileUtilities.listFiles(root)) {
			if (file.isDirectory()) {
				addJavaFiles(result, file);
			} else if (file.getName().endsWith(".java")) {
				result.add(file);
			}
		}
	}

}
