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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.element.meta.kbbased.AbstractElementFactory;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.element.model.generate.WrapperGenerator;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.element.structured.wrap.StructuredElementWrapperFactory;
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

	private static final String ATTRIBUTED_WRAPPER = AttributedWrapper.class.getName();

	private static final String STRUCTURED_WRAPPER = AttributedStructuredElementWrapper.class.getName();

	private static final String STRUCTURED_FACTORY = StructuredElementWrapperFactory.class.getName();

	private static final String ELEMENT_FACTORY = AbstractElementFactory.class.getName();

	private static final String MODULE_WITH_SINGLETON = "ticket29806.withSingleton";

	private static final String MODULE_WITHOUT_SINGLETON = "ticket29806.withoutSingleton";

	private static final String PACKAGE_WITH_SINGLETON = "test/ticket29806/singleton";

	private static final String PACKAGE_WITHOUT_SINGLETON = "test/ticket29806/plain";

	/**
	 * Matches the class declaration of a generated class and captures the optional
	 * {@code abstract} modifier (group 1) and its super class (group 2).
	 */
	private static final Pattern EXTENDS_PATTERN =
		Pattern.compile("^public (abstract )?class \\w+ extends ([\\w.]+)", Pattern.MULTILINE);

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

	/**
	 * The implementation class of a type extends the implementation class of the primary
	 * generalization, independent of whether the module declares singletons (Ticket #29806).
	 */
	public void testImplementationBaseFollowsModelType() throws Exception {
		File root = BasicTestCase.createdCleanTestDir("TestWrapperGeneratorImplementationBase");
		File app1 = app1();

		generate(root, app1, MODULE_WITH_SINGLETON);
		generate(root, app1, MODULE_WITHOUT_SINGLETON);
		compileSources(root);

		String singletonImpl = PACKAGE_WITH_SINGLETON + "/impl/";
		assertEquals(ATTRIBUTED_WRAPPER, superClass(root, singletonImpl + "PlainImpl"));
		assertEquals(ATTRIBUTED_WRAPPER, superClass(root, singletonImpl + "RootImpl"));
		assertEquals(ATTRIBUTED_WRAPPER, superClass(root, singletonImpl + "MarkerImpl"));
		assertEquals(STRUCTURED_WRAPPER, superClass(root, singletonImpl + "NodeImpl"));
		assertEquals("NodeImpl", superClass(root, singletonImpl + "MarkedNodeImpl"));
		assertTrue("Implementation of an interface type must be abstract.",
			isAbstract(root, singletonImpl + "MarkerImpl"));
		assertFalse(isAbstract(root, singletonImpl + "PlainImpl"));
		assertFalse(isAbstract(root, singletonImpl + "NodeImpl"));

		String plainImpl = PACKAGE_WITHOUT_SINGLETON + "/impl/";
		assertEquals(STRUCTURED_WRAPPER, superClass(root, plainImpl + "LeafImpl"));
		assertEquals(ATTRIBUTED_WRAPPER, superClass(root, plainImpl + "OtherImpl"));

		String singletonFactory = source(factoryFile(root, PACKAGE_WITH_SINGLETON));
		assertEquals(STRUCTURED_FACTORY, superClassOf(singletonFactory));
		assertTrue("Missing singleton lookup in factory.", singletonFactory.contains(" getRootSingleton("));

		assertEquals(ELEMENT_FACTORY, superClassOf(source(factoryFile(root, PACKAGE_WITHOUT_SINGLETON))));
	}

	private File app1() {
		File pkg = new File(ModuleLayoutConstants.SRC_TEST_DIR,
			TestWrapperGenerator.class.getPackage().getName().replace('.', '/'));
		return new File(pkg, "app1/" + ModuleLayoutConstants.WEBAPP_DIR);
	}

	private void generate(File root, File app, String moduleName) throws Exception {
		String[] args = {
			WrapperGenerator.DEPLOY_OPTION, app.getAbsolutePath(),
			WrapperGenerator.OUTPUT_DIR_OPTION, root.getAbsolutePath(),
			WrapperGenerator.MODULE_OPTION, moduleName,
			WrapperGenerator.LANG_OPTION, ""
		};
		WrapperGenerator.main(args);
	}

	private static File factoryFile(File root, String packagePath) {
		File[] factories = new File(root, packagePath).listFiles((dir, name) -> name.endsWith("Factory.java"));
		assertNotNull("No package generated: " + packagePath, factories);
		assertEquals("Expected exactly one factory in " + packagePath, 1, factories.length);
		return factories[0];
	}

	private static String superClass(File root, String classPath) throws IOException {
		return superClassOf(source(root, classPath));
	}

	private static boolean isAbstract(File root, String classPath) throws IOException {
		return classDeclaration(source(root, classPath)).group(1) != null;
	}

	private static String source(File root, String classPath) throws IOException {
		File file = new File(root, classPath + ".java");
		assertTrue("Not generated: " + file, file.exists());
		return source(file);
	}

	private static String source(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	private static String superClassOf(String source) {
		return classDeclaration(source).group(2);
	}

	private static Matcher classDeclaration(String source) {
		Matcher matcher = EXTENDS_PATTERN.matcher(source);
		assertTrue("No class declaration with super class found.", matcher.find());
		return matcher;
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
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
			Boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();
			if (!Boolean.TRUE.equals(success)) {
				StringBuilder message = new StringBuilder("Generated sources do not compile:");
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					message.append('\n').append(diagnostic);
				}
				fail(message.toString());
			}
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
