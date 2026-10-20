/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

/**
 * Asserts that every test class in this repository is one that a build actually runs.
 *
 * <p>
 * Which classes surefire executes is decided by the {@code <includes>} of the aggregator pom (see
 * #369 for why they look the way they do). Nothing used to check that those includes still cover
 * the files on disk, so a test class in a package nobody scans was simply never run - green in the
 * IDE, invisible to the build, and no louder about it than a test that passes. This test closes
 * that gap: it reads the includes out of the poms, walks every module's {@code src/test/java}, and
 * fails naming each test class that no include reaches.
 * </p>
 *
 * <p>
 * It deliberately reads the configuration rather than repeating it. Changing the include patterns
 * changes what this test accepts, so the two cannot drift apart; what it does not accept is a test
 * file that no pattern covers.
 * </p>
 *
 * <p>
 * The check spans the whole repository, not just the module it lives in, because the invariant is a
 * repository-level one: the patterns are configured once, in the aggregator pom. That means a run
 * of a single other module ({@code mvn -pl tl-chat test}) does not perform it - a full reactor
 * build, which is what CI runs, does.
 * </p>
 *
 * @see #REACHABLE_NAME the file-name rule a test class additionally has to satisfy.
 */
public class TestSuiteReachability extends TestCase {

	/**
	 * Names a test class must have to be collected, whichever route collects it.
	 *
	 * <p>
	 * Both routes apply this rule: surefire's own default and the engine's
	 * {@code DirectoryLocalTestCollector} agree on it. A class called {@code ChatScopeChecks} is
	 * therefore not a test to either of them, however many test methods it has.
	 * </p>
	 */
	static final Pattern REACHABLE_NAME = Pattern.compile("Test[A-Z0-9].*|.*Test");

	/**
	 * Files the engine's collector skips by name, because they are the suite rather than a test in
	 * it.
	 */
	private static final List<String> SUITE_FILE_NAMES = List.of("TestAll.java", "TestAllLive.java");

	/** The engine's generic suite, as a source path below {@code src/test/java}. */
	private static final String ENGINE_SUITE_SOURCE = "test/TestAll.java";

	/** Marks an include as a regular expression, rather than an Ant path pattern. */
	private static final String REGEX_PREFIX = "%regex[";

	/** Source root every module keeps its tests in. */
	private static final String TEST_SOURCE_DIR = "src/test/java";

	/** The artifact the aggregator pom is recognised by. */
	private static final String AGGREGATOR_ARTIFACT = "tl-ai-parent";

	/**
	 * A class body that only forwards to a test class somewhere else.
	 *
	 * <p>
	 * Before #369 this was how a test outside the scanned packages got run at all: a stub in the
	 * {@code test} package whose {@code suite()} returned the real class's suite. Now that the real
	 * class is scanned directly, such a stub runs its cases a second time.
	 * </p>
	 */
	private static final Pattern DELEGATING_SUITE = Pattern.compile(
		"public\\s+static\\s+(?:junit\\.framework\\.)?Test\\s+suite\\(\\)\\s*\\{\\s*return\\s+"
			+ "(?:new\\s+TestSuite\\(\\s*)?(com\\.top_logic\\.[\\w.]*\\.(?:Test[A-Z0-9]\\w*|\\w*Test))\\b");

	/** Every test class on disk is reached by an include of the module it lives in. */
	public void testEveryTestClassIsReachable() {
		List<String> unreachable = new ArrayList<>();
		for (Module module : modules()) {
			for (Path test : module.testClasses()) {
				String path = module.relativePath(test);
				if (!module.reaches(path)) {
					unreachable.add(module.name() + "/" + TEST_SOURCE_DIR + "/" + path);
				}
			}
		}
		assertTrue("These test classes are never run: no <include> of their module reaches them."
			+ " Move them below one of the scanned package roots, or widen the <includes> in the"
			+ " aggregator pom (and in tl-ai-demo's, which does not inherit them):"
			+ list(unreachable), unreachable.isEmpty());
	}

	/** Every test class on disk is named so that the collector recognises it as one. */
	public void testEveryTestClassIsNamedLikeATest() {
		List<String> misnamed = new ArrayList<>();
		for (Module module : modules()) {
			for (Path test : module.testClasses()) {
				String fileName = test.getFileName().toString();
				String className = fileName.substring(0, fileName.length() - ".java".length());
				if (!REACHABLE_NAME.matcher(className).matches()) {
					misnamed.add(module.name() + "/" + TEST_SOURCE_DIR + "/" + module.relativePath(test));
				}
			}
		}
		assertTrue("These classes declare tests but are not named like a test, so no collector picks"
			+ " them up. Rename them to Test<Something> or <Something>Test:" + list(misnamed),
			misnamed.isEmpty());
	}

	/** No test class is collected twice, through a stub that forwards to another one. */
	public void testNoTestIsCollectedTwice() {
		List<String> duplicates = new ArrayList<>();
		for (Module module : modules()) {
			for (Path test : module.testClasses()) {
				Matcher delegation = DELEGATING_SUITE.matcher(module.code(test));
				if (!delegation.find()) {
					continue;
				}
				String target = delegation.group(1);
				if (module.reaches(target.replace('.', '/') + ".java")) {
					duplicates.add(module.name() + "/" + TEST_SOURCE_DIR + "/" + module.relativePath(test)
						+ " forwards to " + target + ", which the build already runs on its own");
				}
			}
		}
		assertTrue("These stubs make a test class run twice. Delete them - the class they forward to"
			+ " is collected directly:" + list(duplicates), duplicates.isEmpty());
	}

	/** The modules of this repository, each with the includes that apply to it. */
	private List<Module> modules() {
		File root = repositoryRoot();
		List<String> inherited = surefireIncludes(new File(root, "pom.xml"));
		assertFalse("The aggregator pom configures no surefire <includes>, so this test cannot tell"
			+ " what a build runs. Restore them, or delete this test with the reason.", inherited.isEmpty());

		List<Module> modules = new ArrayList<>();
		File[] candidates = root.listFiles();
		assertNotNull("Cannot read the repository root: " + root, candidates);
		for (File dir : sorted(candidates)) {
			File pom = new File(dir, "pom.xml");
			if (!new File(dir, TEST_SOURCE_DIR).isDirectory() || !pom.isFile()) {
				continue;
			}
			List<String> own = surefireIncludes(pom);
			modules.add(new Module(dir, own.isEmpty() ? inherited : own));
		}
		assertFalse("Found no module with a " + TEST_SOURCE_DIR + " below " + root, modules.isEmpty());
		return modules;
	}

	/**
	 * The directory holding the aggregator pom, found by walking up from the module under test.
	 *
	 * <p>
	 * Recognised by the pom's <em>own</em> artifactId, not by the text appearing anywhere: every
	 * module's pom names the aggregator in its {@code <parent>}, so a search for the name would
	 * stop at the module itself.
	 * </p>
	 */
	private File repositoryRoot() {
		File start = new File(".").getAbsoluteFile();
		File dir = start;
		while (dir != null) {
			File pom = new File(dir, "pom.xml");
			if (pom.isFile() && AGGREGATOR_ARTIFACT.equals(ownArtifactId(pom))) {
				return dir;
			}
			dir = dir.getParentFile();
		}
		throw new AssertionError("No pom.xml with artifactId '" + AGGREGATOR_ARTIFACT + "' above " + start
			+ ". This test reads the surefire configuration from it and only runs inside the repository.");
	}

	/** The {@code artifactId} the given pom declares for itself, or {@code null} if it declares none. */
	private static String ownArtifactId(File pom) {
		for (Element artifactId : children(parse(pom), "artifactId")) {
			return artifactId.getTextContent().trim();
		}
		return null;
	}

	/** The {@code <include>} entries configured for the surefire plugin in the given pom. */
	private static List<String> surefireIncludes(File pom) {
		Element plugin = surefirePlugin(pom);
		if (plugin == null) {
			return List.of();
		}
		List<String> includes = new ArrayList<>();
		for (Element configuration : children(plugin, "configuration")) {
			for (Element includeList : children(configuration, "includes")) {
				for (Element include : children(includeList, "include")) {
					includes.add(include.getTextContent().trim());
				}
			}
		}
		return includes;
	}

	private static Element parse(File pom) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			return factory.newDocumentBuilder().parse(pom).getDocumentElement();
		} catch (Exception ex) {
			throw new AssertionError("Cannot read " + pom + ": " + ex.getMessage(), ex);
		}
	}

	private static Element surefirePlugin(File pom) {
		Element project = parse(pom);
		for (Element build : children(project, "build")) {
			for (Element plugins : children(build, "plugins")) {
				for (Element plugin : children(plugins, "plugin")) {
					if (hasText(plugin, "artifactId", "maven-surefire-plugin")) {
						return plugin;
					}
				}
			}
		}
		return null;
	}

	private static boolean hasText(Element parent, String child, String expected) {
		for (Element element : children(parent, child)) {
			if (expected.equals(element.getTextContent().trim())) {
				return true;
			}
		}
		return false;
	}

	private static List<Element> children(Element parent, String name) {
		List<Element> result = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for (int n = 0; n < nodes.getLength(); n++) {
			Node node = nodes.item(n);
			if (node instanceof Element element && name.equals(element.getTagName())) {
				result.add(element);
			}
		}
		return result;
	}

	private static List<File> sorted(File[] files) {
		List<File> result = new ArrayList<>(List.of(files));
		result.sort(Comparator.comparing(File::getName));
		return result;
	}

	private static String list(List<String> entries) {
		return entries.isEmpty() ? "" : "\n  " + String.join("\n  ", entries);
	}

	private static String read(Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException("Cannot read " + file, ex);
		}
	}

	/**
	 * One module of this repository, and what its surefire {@code <includes>} reach.
	 */
	private static final class Module {

		private final File _dir;

		private final List<String> _includes;

		private final Map<Path, String> _code = new LinkedHashMap<>();

		Module(File dir, List<String> includes) {
			_dir = dir;
			_includes = includes;
		}

		String name() {
			return _dir.getName();
		}

		String relativePath(Path test) {
			return testSourceDir().relativize(test).toString().replace(File.separatorChar, '/');
		}

		private Path testSourceDir() {
			return new File(_dir, TEST_SOURCE_DIR).toPath();
		}

		/**
		 * The source files below {@code src/test/java} that declare JUnit tests.
		 */
		List<Path> testClasses() {
			try (Stream<Path> files = Files.walk(testSourceDir())) {
				return files
					.filter(file -> file.getFileName().toString().endsWith(".java"))
					.filter(file -> !SUITE_FILE_NAMES.contains(file.getFileName().toString()))
					.filter(this::declaresTests)
					.sorted()
					.toList();
			} catch (IOException ex) {
				throw new UncheckedIOException("Cannot walk " + testSourceDir(), ex);
			}
		}

		/**
		 * Whether the given file declares JUnit tests, as opposed to helping others declare them.
		 *
		 * <p>
		 * Read from the source rather than from the compiled class, so that the answer does not
		 * depend on what happens to be on the class path of the module running this test.
		 * </p>
		 */
		private boolean declaresTests(Path file) {
			String code = code(file);
			if (code.contains("abstract class ") || code.contains("interface ") || code.contains("enum ")) {
				return false;
			}
			return code.contains("extends TestCase")
				|| code.contains("extends BasicTestCase")
				|| code.contains("@RunWith(")
				|| code.contains("public static Test suite()")
				|| code.contains("public static junit.framework.Test suite()");
		}

		/**
		 * The given file's source with its comments blanked out.
		 *
		 * <p>
		 * The gate classes next to this one describe {@code @RunWith} in their JavaDoc without ever
		 * being a test, so a check reading the raw text would classify them as one.
		 * </p>
		 */
		String code(Path file) {
			return _code.computeIfAbsent(file, f -> stripComments(read(f)));
		}

		/**
		 * Whether an include of this module reaches the test source at the given path, relative to
		 * {@code src/test/java}.
		 */
		boolean reaches(String sourcePath) {
			for (String include : _includes) {
				if (reaches(include, sourcePath)) {
					return true;
				}
			}
			return false;
		}

		private boolean reaches(String include, String sourcePath) {
			if (!include.startsWith(REGEX_PREFIX) || !include.endsWith("]")) {
				throw new AssertionError("Unsupported surefire include in " + name() + ": '" + include
					+ "'. This test knows %regex[...] includes only: surefire 3.6.0 runs no class at all when"
					+ " Ant and %regex[...] includes are mixed. See the comment in the aggregator pom,"
					+ " and teach this test the new form if surefire changed.");
			}
			Pattern regex = Pattern.compile(include.substring(REGEX_PREFIX.length(), include.length() - 1));
			if (runs(regex, ENGINE_SUITE_SOURCE) && sourcePath.startsWith("test/")) {
				// The engine's suite collects, recursively, what lies below the "test" package.
				return true;
			}
			return runs(regex, sourcePath);
		}

		/**
		 * Whether surefire runs the class compiled from the given source under the given
		 * {@code %regex[...]} include.
		 *
		 * <p>
		 * The plugin selects a class by its class file path. Surefire 3.6.0 then matches the
		 * selected class by its class name in the test JVM and drops it when that fails. A class
		 * therefore runs only if the include matches both spellings.
		 * </p>
		 */
		private static boolean runs(Pattern regex, String sourcePath) {
			String path = sourcePath.replaceAll("\\.java$", "");
			return regex.matcher(path + ".class").matches()
				&& regex.matcher(path.replace('/', '.')).matches();
		}
	}

	/**
	 * The given Java source with every comment replaced by spaces, so that positions are kept but
	 * prose cannot be mistaken for code.
	 */
	static String stripComments(String source) {
		StringBuilder result = new StringBuilder(source.length());
		int index = 0;
		while (index < source.length()) {
			char current = source.charAt(index);
			if (current == '"' || current == '\'') {
				index = copyLiteral(source, index, result);
			} else if (source.startsWith("//", index)) {
				index = skip(source, index, indexAfter(source, "\n", index + 2), result);
			} else if (source.startsWith("/*", index)) {
				index = skip(source, index, indexAfter(source, "*/", index + 2), result);
			} else {
				result.append(current);
				index++;
			}
		}
		return result.toString();
	}

	private static int copyLiteral(String source, int start, StringBuilder result) {
		char quote = source.charAt(start);
		result.append(quote);
		int index = start + 1;
		while (index < source.length()) {
			char current = source.charAt(index);
			result.append(current);
			index++;
			if (current == '\\' && index < source.length()) {
				result.append(source.charAt(index));
				index++;
			} else if (current == quote) {
				break;
			}
		}
		return index;
	}

	private static int skip(String source, int start, int end, StringBuilder result) {
		for (int index = start; index < end; index++) {
			result.append(source.charAt(index) == '\n' ? '\n' : ' ');
		}
		return end;
	}

	private static int indexAfter(String source, String token, int from) {
		int found = source.indexOf(token, from);
		return found < 0 ? source.length() : found + token.length();
	}

}
