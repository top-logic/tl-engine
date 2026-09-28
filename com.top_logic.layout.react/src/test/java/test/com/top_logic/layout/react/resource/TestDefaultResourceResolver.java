/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.resource;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.resource.DefaultResourceResolver;
import com.top_logic.layout.react.resource.ModuleScriptConfig;
import com.top_logic.layout.react.resource.ResourceConfig;
import com.top_logic.layout.react.resource.ScriptConfig;
import com.top_logic.layout.react.resource.StyleSheetConfig;
import com.top_logic.layout.react.resource.UnbundledResourceProvider;
import com.top_logic.layout.servlet.CacheControlFilter;

/**
 * Test case for {@link DefaultResourceResolver}: content versions of emitted client resource URLs.
 */
@SuppressWarnings("javadoc")
public class TestDefaultResourceResolver extends TestCase {

	private static final String VERSION_PREFIX = CacheControlFilter.VERSION_PARAMETER + "=";

	private static final Pattern VERSIONED =
		Pattern.compile("^(.*)[?&]" + Pattern.quote(VERSION_PREFIX) + "([0-9a-f]+)$");

	private FileManager _fileManager;

	private Path _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_fileManager = FileManager.getInstance();
		_root = Files.createTempDirectory(TestDefaultResourceResolver.class.getSimpleName());
		FileManager.setInstance(MultiFileManager.createMultiFileManager(_root));
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_fileManager);
		FileUtilities.deleteR(_root.toFile());
		super.tearDown();
	}

	public void testSameContentSameVersion() throws IOException {
		write("/script/a.js", "alert(1);");
		write("/script/b.js", "alert(1);");

		String a1 = resolve(new DefaultResourceResolver(), module("a", "/script/a.js"));
		String a2 = resolve(new DefaultResourceResolver(), module("a", "/script/a.js"));
		String b = resolve(new DefaultResourceResolver(), module("b", "/script/b.js"));

		assertEquals("Version must be stable across resolver instances.", a1, a2);
		assertTrue(a1.startsWith("/script/a.js?" + VERSION_PREFIX));
		assertEquals(version(a1), version(b));
		assertEquals(DefaultResourceResolver.VERSION_LENGTH, version(a1).length());
	}

	public void testDifferentContentDifferentVersion() throws IOException {
		write("/style/a.css", "body { color: red; }");
		write("/style/b.css", "body { color: blue; }");

		DefaultResourceResolver resolver = new DefaultResourceResolver();
		String a = resolve(resolver, styleSheet("a", "/style/a.css"));
		String b = resolve(resolver, styleSheet("b", "/style/b.css"));

		assertFalse(version(a).equals(version(b)));
	}

	public void testChangedContentAfterRestart() throws IOException {
		write("/script/a.js", "alert(1);");
		String before = resolve(new DefaultResourceResolver(), script("a", "/script/a.js"));

		write("/script/a.js", "alert(2);");
		String after = resolve(new DefaultResourceResolver(), script("a", "/script/a.js"));

		assertFalse(version(before).equals(version(after)));
	}

	public void testExistingQuery() throws IOException {
		write("/script/a.js", "alert(1);");

		String url = resolve(new DefaultResourceResolver(), script("a", "/script/a.js?mode=x"));

		assertTrue(url, url.startsWith("/script/a.js?mode=x&" + VERSION_PREFIX));
		assertEquals(DefaultResourceResolver.VERSION_LENGTH, version(url).length());
	}

	public void testMissingFile() {
		DefaultResourceResolver resolver = new DefaultResourceResolver();
		assertEquals("/script/missing.js", resolve(resolver, script("m", "/script/missing.js")));
		assertEquals("/script/missing.js", resolve(resolver, script("m", "/script/missing.js")));
	}

	public void testAbsoluteUrl() {
		DefaultResourceResolver resolver = new DefaultResourceResolver();
		assertEquals("https://cdn.example.com/x.js",
			resolve(resolver, script("x", "https://cdn.example.com/x.js")));
		assertEquals("//cdn.example.com/x.js", resolve(resolver, script("y", "//cdn.example.com/x.js")));
	}

	public void testScriptTagAndImportMapIdentical() throws IOException {
		write("/script/bridge.js", "export const x = 1;");
		ModuleScriptConfig bridge = module("bridge", "/script/bridge.js");
		update(bridge, ModuleScriptConfig.SPECIFIER, "tl-bridge");

		DefaultResourceResolver resolver = new DefaultResourceResolver();
		String expected = "/ctx" + resolve(resolver, bridge);

		UnbundledResourceProvider provider = new UnbundledResourceProvider(List.of(bridge), resolver);
		StringWriter buffer = new StringWriter();
		TagWriter out = new TagWriter(buffer);
		provider.writeScriptRefs(out, "/ctx");
		out.flushBuffer();
		String html = buffer.toString();

		Matcher src = Pattern.compile("src=\"([^\"]*)\"").matcher(html);
		assertTrue(html, src.find());
		assertEquals(expected, src.group(1).replace("&amp;", "&"));

		Matcher mapping = Pattern.compile("\"tl-bridge\"\\s*:\\s*\"([^\"]*)\"").matcher(html);
		assertTrue(html, mapping.find());
		assertEquals(expected, unescapeJson(mapping.group(1)));
	}

	private static String unescapeJson(String value) {
		Matcher escape = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(value);
		StringBuilder result = new StringBuilder();
		while (escape.find()) {
			escape.appendReplacement(result,
				Matcher.quoteReplacement(String.valueOf((char) Integer.parseInt(escape.group(1), 16))));
		}
		escape.appendTail(result);
		return result.toString().replace("\\/", "/");
	}

	private void write(String path, String content) throws IOException {
		Path file = _root.resolve(path.substring(1));
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
	}

	private static String version(String url) {
		Matcher matcher = VERSIONED.matcher(url);
		assertTrue("Not versioned: " + url, matcher.matches());
		return matcher.group(2);
	}

	private static String resolve(DefaultResourceResolver resolver, ResourceConfig resource) {
		List<String> urls = resolver.resolve(resource);
		assertEquals(1, urls.size());
		return urls.get(0);
	}

	private static ModuleScriptConfig module(String name, String resource) {
		return resource(ModuleScriptConfig.class, name, ModuleScriptConfig.RESOURCE, resource);
	}

	private static ScriptConfig script(String name, String resource) {
		return resource(ScriptConfig.class, name, ScriptConfig.RESOURCE, resource);
	}

	private static StyleSheetConfig styleSheet(String name, String resource) {
		return resource(StyleSheetConfig.class, name, StyleSheetConfig.RESOURCE, resource);
	}

	private static <T extends ResourceConfig> T resource(Class<T> type, String name, String resourceProperty,
			String resource) {
		T result = TypedConfiguration.newConfigItem(type);
		result.setName(name);
		update(result, resourceProperty, resource);
		return result;
	}

	private static void update(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestDefaultResourceResolver.class);
	}

}
