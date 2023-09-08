/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
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
		File pkg = new File(ModuleLayoutConstants.SRC_TEST_DIR,
			TestWrapperGenerator.class.getPackage().getName().replace('.', '/'));
		File app1 = new File(pkg, "app1/" + ModuleLayoutConstants.WEBAPP_DIR);

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

	private Iterable<? extends File> listJavaFiles(File root) throws IOException {
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
