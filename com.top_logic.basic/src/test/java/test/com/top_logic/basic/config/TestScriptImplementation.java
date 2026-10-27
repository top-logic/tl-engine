/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementationProvider;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * Test case for configuration properties that are implemented either in Java or by a script of a
 * {@link ScriptImplementationProvider}.
 *
 * <p>
 * The test installs the {@link StubProvider} that implements interfaces declared in this test by
 * returning the script text from each method.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestScriptImplementation extends AbstractTypedConfigurationTestCase {

	private static final String JAVA_FUN = JavaFun.class.getName();

	private static final String SERVICE_FILE = "META-INF/services/" + ScriptImplementationProvider.class.getName();

	/**
	 * Functional interface implemented in Java or by script.
	 */
	public interface Fun {
		Object call(Object arg);

		default Object callTwice(Object arg) {
			return call(call(arg));
		}

		@Override
		String toString();
	}

	/**
	 * Interface with two abstract methods.
	 */
	public interface TwoMethods {
		Object first();

		Object second();
	}

	/**
	 * Sub interface of {@link PolymorphicConfiguration} that the {@link StubProvider} supports.
	 */
	public interface FunConfig extends PolymorphicConfiguration<Fun> {
		// Marker.
	}

	/**
	 * Sub interface of {@link PolymorphicConfiguration} that the {@link StubProvider} does not
	 * support.
	 */
	public interface OtherFunConfig extends PolymorphicConfiguration<Fun> {
		// Marker.
	}

	public interface A extends ConfigurationItem {
		String FUN = "fun";

		@Name(FUN)
		PolymorphicConfiguration<Fun> getFun();

		void setFun(PolymorphicConfiguration<Fun> value);

		PolymorphicConfiguration<? extends Fun> getWildcard();

		void setWildcard(PolymorphicConfiguration<? extends Fun> value);

		FunConfig getKnown();

		void setKnown(FunConfig value);

		OtherFunConfig getOther();

		PolymorphicConfiguration<TwoMethods> getTwo();

		void setTwo(PolymorphicConfiguration<TwoMethods> value);

		@ImplementationClassDefault(JavaFun.class)
		PolymorphicConfiguration<Fun> getWithDefault();
	}

	/**
	 * Property type declared before any provider is installed.
	 */
	public interface WithoutProvider extends ConfigurationItem {
		PolymorphicConfiguration<Fun> getFun();
	}

	/**
	 * Java implementation of {@link Fun}.
	 */
	public static class JavaFun extends AbstractConfiguredInstance<JavaFun.Config<?>> implements Fun {

		public interface Config<I extends JavaFun> extends PolymorphicConfiguration<I> {
			String getResult();

			void setResult(String value);

			int getCount();

			void setCount(int value);
		}

		public JavaFun(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Object call(Object arg) {
			return getConfig().getResult();
		}
	}

	/**
	 * Script implementation of the {@link StubProvider}.
	 */
	public static final class StubScript {

		public interface Config<T> extends PolymorphicConfiguration<T> {

			String TEXT = "text";

			String INTERFACE = "interface";

			@Override
			@ClassDefault(StubScript.class)
			Class<? extends T> getImplementationClass();

			@Name(TEXT)
			String getText();

			void setText(String value);

			@Name(INTERFACE)
			Class<?> getInterface();

			void setInterface(Class<?> value);
		}

		/**
		 * Script configuration for {@link FunConfig} properties.
		 */
		public interface FunScript extends Config<Fun>, FunConfig {
			// Combination.
		}

		private StubScript() {
			// Factory only.
		}

		/**
		 * Creates a proxy of {@link Config#getInterface()} whose methods return
		 * {@link Config#getText()}.
		 */
		public static Object newInstance(InstantiationContext context, Config<?> config) {
			Class<?> type = config.getInterface();
			return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				(proxy, method, args) -> invoke(config, proxy, method, args));
		}

		private static Object invoke(Config<?> config, Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.isDefault()) {
				return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
			}
			switch (method.getName()) {
				case "equals":
					return proxy == args[0];
				case "hashCode":
					return System.identityHashCode(proxy);
				case "toString":
					return "script(" + config.getText() + ")";
				default:
					return config.getText();
			}
		}
	}

	/**
	 * {@link ScriptImplementationProvider} for interfaces declared in
	 * {@link TestScriptImplementation}.
	 */
	public static class StubProvider implements ScriptImplementationProvider {

		@Override
		public boolean supports(PropertyDescriptor property) {
			Class<?> type = property.getType();
			return (type == PolymorphicConfiguration.class || type == FunConfig.class)
				&& property.getInstanceType().getEnclosingClass() == TestScriptImplementation.class;
		}

		@Override
		public PolymorphicConfiguration<?> parse(PropertyDescriptor property, CharSequence text)
				throws ConfigurationException {
			Class<?> configType = property.getType() == FunConfig.class ? StubScript.FunScript.class : StubScript.Config.class;
			StubScript.Config<?> result = (StubScript.Config<?>) TypedConfiguration.newConfigItem(
				configType.asSubclass(ConfigurationItem.class));
			result.setText(text.toString());
			result.setInterface(property.getInstanceType());
			return result;
		}

		@Override
		public boolean isScript(PolymorphicConfiguration<?> config) {
			return config instanceof StubScript.Config<?>;
		}

		@Override
		public String getSpecification(PolymorphicConfiguration<?> config) {
			return ((StubScript.Config<?>) config).getText();
		}

		@Override
		public ConfigBuilder createScriptBuilder(PropertyDescriptor property) throws ConfigurationException {
			ConfigBuilder result = TypedConfiguration.createConfigBuilder(StubScript.Config.class);
			result.update(result.descriptor().getProperty(StubScript.Config.INTERFACE), property.getInstanceType());
			return result;
		}
	}

	/**
	 * A second provider for testing that at most one provider may be registered.
	 */
	public static class OtherStubProvider extends StubProvider {
		// Second registration.
	}

	private ScriptImplementationProvider _installedBefore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_installedBefore = ScriptImplementations.installProvider(new StubProvider());
	}

	@Override
	protected void tearDown() throws Exception {
		ScriptImplementations.installProvider(_installedBefore);
		super.tearDown();
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return createDescriptorMap(A.class);
	}

	public void testAcceptsScript() {
		assertTrue(ScriptImplementations.acceptsScript(getProperty(A.class, A.FUN)));
		assertTrue(ScriptImplementations.acceptsScript(getProperty(A.class, "wildcard")));
		assertTrue(ScriptImplementations.acceptsScript(getProperty(A.class, "known")));
		assertTrue(ScriptImplementations.acceptsScript(getProperty(A.class, "two")));
		assertFalse(ScriptImplementations.acceptsScript(getProperty(A.class, "other")));

		assertNotNull("Functional interface has a text form.", getProperty(A.class, A.FUN).getValueProvider());
		assertNull("Unsupported property type.", getProperty(A.class, "other").getValueProvider());
		assertNull("No text form for interfaces with more than one method.",
			getProperty(A.class, "two").getValueProvider());
	}

	public void testClassNameAttribute() throws ConfigurationException {
		A a = read("<A fun='" + JAVA_FUN + "'/>");
		assertJava(a.getFun());
	}

	public void testClassAttributeUnchanged() throws ConfigurationException {
		A a = read("<A><fun class='" + JAVA_FUN + "' result='r1' count='2'/></A>");
		JavaFun.Config<?> config = assertJava(a.getFun());
		assertEquals("r1", config.getResult());
		assertEquals(2, config.getCount());
		assertEquals("r1", getInstance(a.getFun()).call(null));
	}

	public void testBaseConfigMergeElement() throws ConfigurationException {
		A a = read(
			"<A><fun class='" + JAVA_FUN + "' result='r1' count='2'/></A>",
			"<A><fun result='r2'/></A>");
		JavaFun.Config<?> config = assertJava(a.getFun());
		assertEquals("r2", config.getResult());
		assertEquals(2, config.getCount());
	}

	public void testBaseConfigMergeClassAttribute() throws ConfigurationException {
		A a = read(
			"<A><fun class='" + JAVA_FUN + "' result='r1' count='2'/></A>",
			"<A fun='" + JAVA_FUN + "'/>");
		JavaFun.Config<?> config = assertJava(a.getFun());
		assertEquals("r1", config.getResult());
		assertEquals(2, config.getCount());
	}

	public void testScriptOverridesJava() throws ConfigurationException {
		A a = read(
			"<A><fun class='" + JAVA_FUN + "' result='r1'/></A>",
			"<A fun='x -> 42'/>");
		assertScript("x -> 42", a.getFun());
	}

	public void testScriptAttribute() throws ConfigurationException {
		A a = read("<A fun='x -> 42' wildcard='y -> 13' known='z -> 7'/>");
		assertScript("x -> 42", a.getFun());
		assertScript("y -> 13", a.getWildcard());
		assertScript("z -> 7", a.getKnown());
		assertTrue("Script config must be an instance of the property type.",
			a.getKnown() instanceof StubScript.FunScript);
	}

	public void testScriptVariableIsNoClassName() throws ConfigurationException {
		A a = read("<A fun='$x.name'/>");
		assertScript("$x.name", a.getFun());
	}

	public void testScriptElementText() throws ConfigurationException {
		A a = read("<A><fun>x -> 42</fun><known><![CDATA[x -> $x < 2]]></known></A>");
		assertScript("x -> 42", a.getFun());
		assertScript("x -> $x < 2", a.getKnown());
	}

	public void testClassNameElementText() throws ConfigurationException {
		A a = read("<A><fun>" + JAVA_FUN + "</fun></A>");
		assertJava(a.getFun());
	}

	public void testConcreteDefaultImplementationClass() throws ConfigurationException {
		A java = read("<A><with-default result='r1'/></A>");
		assertEquals("r1", assertJava(java.getWithDefault()).getResult());

		A script = read("<A with-default='x -> 42'/>");
		assertScript("x -> 42", script.getWithDefault());
	}

	public void testClassLessElement() throws ConfigurationException {
		A a = read("<A><fun text='x -> 42'/><two><text>both</text></two></A>");
		assertScript("x -> 42", a.getFun());
		assertScript("both", a.getTwo());
		TwoMethods two = getInstance(a.getTwo());
		assertEquals("both", two.first());
		assertEquals("both", two.second());
	}

	public void testInstantiateScript() throws ConfigurationException {
		A a = read("<A fun='x -> 42'/>");
		Fun fun = getInstance(a.getFun());
		assertTrue(Proxy.isProxyClass(fun.getClass()));
		assertEquals("x -> 42", fun.call("arg"));
		assertEquals("x -> 42", fun.callTwice("arg"));
	}

	public void testXmlRoundTrip() throws Exception {
		A a = read("<A fun='x -> 42' known='z -> 7'>"
			+ "<wildcard class='" + JAVA_FUN + "' result='r1'/>"
			+ "<two text='both'/>"
			+ "</A>");
		String xml = toXML(a);
		assertTrue(xml, xml.contains("fun=\"x -&gt; 42\"") || xml.contains("fun=\"x -> 42\""));
		assertTrue(xml, xml.contains("<wildcard class=\"" + JAVA_FUN + "\""));

		A copy = (A) fromXML(xml);
		assertEquals(a, copy);
		assertScript("x -> 42", copy.getFun());
		assertScript("z -> 7", copy.getKnown());
		assertJava(copy.getWildcard());
		assertScript("both", copy.getTwo());
	}

	public void testXmlRoundTripSpecialCharacters() throws Exception {
		A a = read("<A><fun><![CDATA[x -> $x < 2]]></fun></A>");
		String xml = toXML(a);
		A copy = (A) fromXML(xml);
		assertEquals(a, copy);
		assertScript("x -> $x < 2", copy.getFun());
	}

	public void testJsonRoundTrip() throws Exception {
		A a = read("<A fun='x -> 42' known='z -> 7'>"
			+ "<wildcard class='" + JAVA_FUN + "' result='r1'/>"
			+ "</A>");
		ConfigurationDescriptor staticType = TypedConfiguration.getConfigurationDescriptor(A.class);
		StringWriter buffer = new StringWriter();
		new JsonConfigurationWriter(buffer).write(staticType, a);
		String json = buffer.toString();
		assertTrue(json, json.contains("\"x -> 42\""));

		A copy = (A) new JsonConfigurationReader(context, staticType)
			.setSource(CharacterContents.newContent(json))
			.read();
		assertEquals(a, copy);
		assertScript("x -> 42", copy.getFun());
		assertScript("z -> 7", copy.getKnown());
		assertJava(copy.getWildcard());
	}

	public void testJsonClassName() throws Exception {
		ConfigurationDescriptor staticType = TypedConfiguration.getConfigurationDescriptor(A.class);
		A a = (A) new JsonConfigurationReader(context, staticType)
			.setSource(CharacterContents.newContent("{\"fun\": \"" + JAVA_FUN + "\"}"))
			.read();
		assertJava(a.getFun());
	}

	public void testUnresolvableClassName() {
		assertIllegalXml("Class name that does not resolve.", "<A fun='no.such.Clazz'/>",
			Pattern.compile("no\\.such\\.Clazz"), A.class);
	}

	public void testUnsupportedSubInterface() {
		assertIllegalXml("Property type not supported by the provider.", "<A other='x -> 42'/>",
			Pattern.compile("x -> 42"), A.class);
	}

	public void testNoTextFormForMultipleMethods() {
		assertIllegalXml("No text form for interfaces with more than one method.", "<A two='x -> 42'/>",
			Pattern.compile("x -> 42"), A.class);
		assertIllegalXml("No text form for interfaces with more than one method.", "<A><two>x -> 42</two></A>",
			Pattern.compile("no format"), A.class);
	}

	public void testWithoutProvider() {
		ScriptImplementationProvider stub = ScriptImplementations.installProvider(null);
		try {
			assertNull(ScriptImplementations.getProvider());
			PropertyDescriptor property = getProperty(WithoutProvider.class, "fun");
			assertFalse(ScriptImplementations.acceptsScript(property));
			assertNull(property.getValueProvider());
			assertIllegalXml("Script text without provider.", "<WithoutProvider fun='x -> 42'/>",
				Pattern.compile("x -> 42"), WithoutProvider.class);
		} finally {
			ScriptImplementations.installProvider(stub);
		}
	}

	public void testIsClassName() {
		assertTrue(ScriptImplementations.isClassName("a.b.C"));
		assertTrue(ScriptImplementations.isClassName("a.b.C$D"));
		assertTrue(ScriptImplementations.isClassName(" a.b.C "));
		assertFalse(ScriptImplementations.isClassName("C"));
		assertFalse(ScriptImplementations.isClassName("a..C"));
		assertFalse(ScriptImplementations.isClassName("a.b."));
		assertFalse(ScriptImplementations.isClassName("$x.name"));
		assertFalse(ScriptImplementations.isClassName("x -> x.y"));
		assertFalse(ScriptImplementations.isClassName("a.b()"));
		assertFalse(ScriptImplementations.isClassName(""));
	}

	public void testServiceLoaderLookup() throws IOException {
		assertSame(StubProvider.class, lookup(StubProvider.class.getName()).getClass());
		assertNull(lookup());
		try {
			lookup(StubProvider.class.getName(), OtherStubProvider.class.getName());
			fail("At most one provider must be registered.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(OtherStubProvider.class.getName()));
		}
	}

	private ScriptImplementationProvider lookup(String... providerClasses) throws IOException {
		File dir = Files.createTempDirectory(TestScriptImplementation.class.getSimpleName()).toFile();
		try {
			if (providerClasses.length > 0) {
				File serviceFile = new File(dir, SERVICE_FILE);
				serviceFile.getParentFile().mkdirs();
				Files.write(serviceFile.toPath(), List.of(providerClasses), StandardCharsets.UTF_8);
			}
			ClassLoader parent = new ClassLoader(null) {
				// Hides the service registrations of the test class path.
				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					return TestScriptImplementation.class.getClassLoader().loadClass(name);
				}
			};
			try (URLClassLoader loader = new URLClassLoader(new URL[] { dir.toURI().toURL() }, parent)) {
				return ScriptImplementations.lookup(loader);
			}
		} finally {
			deleteRecursively(dir);
		}
	}

	private static void deleteRecursively(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteRecursively(child);
			}
		}
		file.delete();
	}

	public void testFactoryMethodReturningInterface() throws ConfigurationException {
		StubScript.Config<?> config = TypedConfiguration.newConfigItem(StubScript.Config.class);
		config.setText("t");
		config.setInterface(Fun.class);
		Object instance = getInstance(config);
		assertTrue(instance instanceof Fun);
		assertEquals("script(t)", instance.toString());
	}

	public void testResolveType() throws Exception {
		Type context = A.class.getMethod("getKnown").getGenericReturnType();
		Type implementationType = JavaTypeUtil.resolve(context, PolymorphicConfiguration.class.getTypeParameters()[0]);
		assertEquals(Fun.class, implementationType);

		@SuppressWarnings("unused")
		class Holder {
			UnaryOperator<List<String>> _op;
		}
		Type operatorType = Holder.class.getDeclaredField("_op").getGenericType();
		Type resultType =
			JavaTypeUtil.resolve(operatorType, Function.class.getMethod("apply", Object.class).getGenericReturnType());
		assertEquals("java.util.List<java.lang.String>", resultType.getTypeName());
	}

	public void testAbstractMethods() {
		assertEquals(1, JavaTypeUtil.getAbstractMethods(Fun.class).size());
		assertEquals(2, JavaTypeUtil.getAbstractMethods(TwoMethods.class).size());
		assertEquals(1, JavaTypeUtil.getAbstractMethods(UnaryOperator.class).size());
		assertEquals(1, JavaTypeUtil.getAbstractMethods(Comparable.class).size());
		assertEquals(1, JavaTypeUtil.getAbstractMethods(Runnable.class).size());
	}

	private static JavaFun.Config<?> assertJava(PolymorphicConfiguration<?> config) {
		assertNotNull(config);
		assertEquals(JavaFun.class, config.getImplementationClass());
		assertTrue(config instanceof JavaFun.Config<?>);
		return (JavaFun.Config<?>) config;
	}

	private static void assertScript(String expectedText, PolymorphicConfiguration<?> config) {
		assertTrue("Not a script: " + config, config instanceof StubScript.Config<?>);
		assertEquals(expectedText, ((StubScript.Config<?>) config).getText());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestScriptImplementation.class));
	}

}
