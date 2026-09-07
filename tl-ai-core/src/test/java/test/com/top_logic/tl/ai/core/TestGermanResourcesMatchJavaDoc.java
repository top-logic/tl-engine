/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import junit.framework.TestCase;

/**
 * Guards that the German text a developer wrote next to a resource constant is the German text the
 * application ships.
 *
 * <p>
 * The JavaDoc of an {@code I18NConstants} field is where its texts are authored. The build's doclet
 * exports the {@code @en} tag to {@code META-INF/messages_en.properties}, and
 * {@code tl-maven-plugin:translate} then seeds the missing German by machine translation from that
 * English string alone. The {@code @de} tag sitting right next to {@code @en} is read by no
 * generator - not by the doclet, not by the translation goal (Gitea #331) - so nothing compares the
 * German that ships with the German that was written, and a machine translation that means
 * something else reaches the user in a green build.
 * </p>
 *
 * <p>
 * This test is that comparison. It found six wrong texts on its first run, among them
 * <em>"Klare Modelle"</em> ("lucid models") on the button that clears the model allowlist. German
 * resources are maintained by hand - the fix for a failure here is to correct
 * {@code messages_de.properties}, not to reword the tag.
 * </p>
 *
 * <p>
 * It stays useful after the generator learns to honour {@code @de}: it is that fix's acceptance
 * test, green the moment the doclet writes the authored German and green afterwards. That matters
 * here because tl-ai tracks {@code 8.0.0-alpha*} snapshots of the engine, so the behaviour of the
 * generators can change under it between two builds.
 * </p>
 */
/*
 * Three things about this test are unlike its neighbours, and each of them is deliberate.
 *
 * It reads the Java sources rather than the classpath, because JavaDoc is not in the bytecode: the
 * authored half of the comparison exists nowhere else. TestConfigPresentation, which checks the
 * generated texts themselves, can and does read the classpath instead.
 *
 * It lives in the "test" package root, which is where the engine's test.TestAll collects a module's
 * tests from - so it runs in a plain "mvn test -DskipTests=false". The rest of tl-ai's tests sit
 * outside that root and therefore run only when a -Dtest argument names them, which is not enough
 * for a check whose job is to notice something nobody is looking at. It is also where the engine
 * keeps its own resource check, TestI18NConstantsTranslations, which asks the complementary
 * question: whether a translation exists at all.
 *
 * It walks every module of the reactor rather than only its own. Surefire runs a test with its
 * module directory as the working directory, so a per-module check would be the smaller step - but
 * it would need a shared base class and a subclass that somebody has to remember for a module added
 * later, and a check whose coverage can silently shrink is the very failure mode this test exists
 * to close. Walking the reactor covers a new module the moment it exists.
 */
public class TestGermanResourcesMatchJavaDoc extends TestCase {

	/**
	 * Identifies the POM at the root of the tl-ai reactor.
	 *
	 * <p>
	 * The artifact name alone does not: every module names the same artifact as its parent. Only the
	 * root POM names it and is itself an aggregator.
	 * </p>
	 */
	private static final List<String> REACTOR_POM_MARKERS =
		List.of("<artifactId>tl-ai-parent</artifactId>", "<packaging>pom</packaging>");

	/** Where a module keeps the Java sources the doclet reads. */
	private static final String SOURCE_FOLDER = "src/main/java";

	/** Where the build writes the German resources of a module. */
	private static final String GERMAN_BUNDLE = SOURCE_FOLDER + "/META-INF/messages_de.properties";

	/**
	 * A documented resource constant: its JavaDoc block, optional plain comments in between, and the
	 * field declaration the resource key is derived from.
	 *
	 * <p>
	 * The plain comments have to be part of the pattern. A block comment may sit between the JavaDoc
	 * and the declaration to say something to the reader of the code rather than to the reader of the
	 * user interface, and a pattern that does not allow for it pairs a JavaDoc block with the wrong
	 * field.
	 * </p>
	 */
	private static final Pattern DOCUMENTED_CONSTANT = Pattern.compile(
		"/\\*\\*((?:(?!\\*/).)*?)\\*/(?:\\s*/\\*(?:(?!\\*/).)*\\*/)*"
			+ "\\s*public\\s+static\\s+(?:final\\s+)?ResKey\\d*\\s+(\\w+)\\s*;",
		Pattern.DOTALL);

	/** The {@code @de} tag inside a JavaDoc block, up to the next tag or the end of the block. */
	private static final Pattern GERMAN_TAG = Pattern.compile(
		"^\\s*\\*\\s*@de\\s+(.*?)(?=^\\s*\\*\\s*@|\\z)", Pattern.DOTALL | Pattern.MULTILINE);

	/**
	 * Every line that opens a {@code @de} tag, whatever it documents.
	 *
	 * <p>
	 * Like the tag pattern itself, this expects the tag on a line of its own - which is how every
	 * JavaDoc tag in this project is written.
	 * </p>
	 */
	private static final Pattern GERMAN_TAG_LINE =
		Pattern.compile("^\\s*\\*\\s*@de\\s", Pattern.MULTILINE);

	/** The prefix the doclet gives a resource key it derived from a Java constant. */
	private static final String KEY_PREFIX = "class.";

	/** The package declaration a resource key is qualified with. */
	private static final Pattern PACKAGE =
		Pattern.compile("^package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

	/** A German text a developer wrote, and where they wrote it. */
	private record AuthoredText(String key, String german, String source) {
		/* Pure data. */
	}

	/** What the sources hold: the authored texts, and what it took to find them. */
	private record Authored(Map<String, AuthoredText> texts, int tags, int files) {
		/* Pure data. */
	}

	/**
	 * Every {@code @de} tag holds the text the application ships.
	 */
	public void testEveryAuthoredGermanTextIsShipped() throws IOException {
		Map<String, String> shipped = shippedGermanTexts();
		List<String> wrong = new ArrayList<>();
		for (AuthoredText authored : authored().texts().values()) {
			String actual = shipped.get(authored.key());
			if (actual != null && !actual.equals(authored.german())) {
				wrong.add(authored.key()
					+ "\n      @de     : " + authored.german()
					+ "\n      shipped : " + actual
					+ "\n      source  : " + authored.source());
			}
		}

		assertEquals("These German texts are not the text their @de tag documents - the build's machine"
			+ " translation wrote them and nobody corrected them. Set each to its @de text in the"
			+ " module's src/main/java/META-INF/messages_de.properties, which is maintained by hand."
			+ " Rewording the tag alone changes nothing: no generator reads it (Gitea #331).\n  "
			+ String.join("\n  ", wrong), List.of(), wrong);
	}

	/**
	 * Every {@code @de} tag documents a resource that exists in German.
	 *
	 * <p>
	 * A key missing here has no German text at all, so the application falls back to the English one.
	 * That happens to a key added by a build without a translation API key - which is what a build
	 * outside a developer machine is - and it is invisible in the source: the tag is there, and it
	 * looks like the German is too.
	 * </p>
	 */
	public void testEveryAuthoredGermanTextReachesTheBundle() throws IOException {
		Map<String, String> shipped = shippedGermanTexts();
		List<String> missing = new ArrayList<>();
		for (AuthoredText authored : authored().texts().values()) {
			if (!shipped.containsKey(authored.key())) {
				missing.add(authored.key()
					+ "\n      @de     : " + authored.german()
					+ "\n      source  : " + authored.source());
			}
		}

		assertEquals("These constants carry a German text that no German bundle holds, so the user reads"
			+ " the English one. Either the constant has no @en tag - the doclet writes no resource at all"
			+ " for that - or the bundle was generated without the text and never regenerated. Add the"
			+ " German to the module's messages_de.properties:\n  "
			+ String.join("\n  ", missing), List.of(), missing);
	}

	/**
	 * Every {@code @de} tag in the sources was read by this check.
	 *
	 * <p>
	 * The tags are found by pairing a JavaDoc block with the {@code ResKey} field below it. A tag
	 * written anywhere else - on a configuration property, on a method - documents German that this
	 * check cannot compare with anything, so it would be missed in silence, which is how the whole
	 * problem stayed invisible in the first place.
	 * </p>
	 */
	public void testEveryGermanTagIsAccountedFor() throws IOException {
		Authored authored = authored();

		assertEquals("Found " + authored.tags() + " @de tags in the sources but could attribute only "
			+ authored.texts().size() + " of them to a resource constant. A tag that is not on a"
			+ " 'public static ResKey<n> NAME;' field documents a text nothing can check - move it onto"
			+ " the constant, or teach DOCUMENTED_CONSTANT about the declaration it sits on.",
			authored.tags(), authored.texts().size());
	}

	/**
	 * Precondition: the sources and bundles this test compares were found.
	 *
	 * <p>
	 * Without it a moved source folder, a renamed bundle or a working directory this test did not
	 * expect would turn every check above into one that compares nothing and passes.
	 * </p>
	 */
	public void testTheSourcesAndBundlesAreThere() throws IOException {
		Path root = reactorRoot();
		Authored authored = authored();
		int bundles = germanBundles().size();
		int shipped = shippedGermanTexts().size();

		assertTrue("Only " + authored.files() + " Java sources found under " + root + "/*/"
			+ SOURCE_FOLDER + ". Either the reactor root was located wrongly or the modules moved.",
			authored.files() > 300);
		assertTrue("Only " + bundles + " German bundles found. The build writes one per module that has"
			+ " resources; either the doclet did not run or " + GERMAN_BUNDLE + " moved.", bundles >= 5);
		assertTrue("Only " + shipped + " German resources found in those bundles.", shipped > 200);
		assertTrue("Only " + authored.texts().size() + " constants carrying a @de tag found. The tags are"
			+ " the whole input of this test; if they are gone, it checks nothing.",
			authored.texts().size() > 100);
	}

	/** The German texts the developers of this project wrote, by resource key. */
	private static Authored authored() throws IOException {
		Map<String, AuthoredText> texts = new TreeMap<>();
		int tags = 0;
		int files = 0;
		for (Path module : modules()) {
			try (Stream<Path> sources = Files.walk(module.resolve(SOURCE_FOLDER))) {
				for (Path source : sources.filter(TestGermanResourcesMatchJavaDoc::isJavaSource)
					.sorted().toList()) {
					files++;
					String text = Files.readString(source, StandardCharsets.UTF_8);
					int found = count(GERMAN_TAG_LINE, text);
					if (found == 0) {
						continue;
					}
					tags += found;
					collectAuthoredTexts(source, text, texts);
				}
			}
		}
		return new Authored(texts, tags, files);
	}

	private static void collectAuthoredTexts(Path source, String text,
			Map<String, AuthoredText> result) {
		String packageName = packageOf(text);
		if (packageName == null) {
			return;
		}
		String className = source.getFileName().toString().replaceFirst("\\.java$", "");
		Matcher constants = DOCUMENTED_CONSTANT.matcher(text);
		while (constants.find()) {
			Matcher tag = GERMAN_TAG.matcher(constants.group(1));
			if (!tag.find()) {
				continue;
			}
			String key = KEY_PREFIX + packageName + "." + className + "." + constants.group(2);
			String where = source + ":" + lineOf(text, constants.start());
			result.put(key, new AuthoredText(key, unwrap(tag.group(1)), where));
		}
	}

	/** The German texts the application reads, merged over every module of the reactor. */
	private static Map<String, String> shippedGermanTexts() throws IOException {
		Map<String, String> result = new TreeMap<>();
		for (Path bundle : germanBundles()) {
			Properties properties = new Properties();
			try (InputStream in = Files.newInputStream(bundle);
					Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			for (String key : properties.stringPropertyNames()) {
				result.put(key, properties.getProperty(key));
			}
		}
		return result;
	}

	/** The generated German bundles of every module of the reactor. */
	private static List<Path> germanBundles() throws IOException {
		List<Path> result = new ArrayList<>();
		for (Path module : modules()) {
			Path bundle = module.resolve(GERMAN_BUNDLE);
			if (Files.isRegularFile(bundle)) {
				result.add(bundle);
			}
		}
		return result;
	}

	/** The modules of the reactor that have Java sources. */
	private static List<Path> modules() throws IOException {
		try (Stream<Path> candidates = Files.list(reactorRoot())) {
			return candidates.filter(candidate -> Files.isDirectory(candidate.resolve(SOURCE_FOLDER)))
				.sorted(Comparator.comparing(Path::toString)).toList();
		}
	}

	/**
	 * The directory holding the POM of the reactor this test runs in.
	 *
	 * <p>
	 * Surefire runs a test with the directory of its own module as the working directory, and the
	 * modules of this project are directories next to the reactor POM - so walking upwards finds the
	 * root from any of them, and from a worktree as well as from the main checkout.
	 * </p>
	 */
	private static Path reactorRoot() throws IOException {
		for (Path candidate = Path.of("").toAbsolutePath(); candidate != null; candidate =
			candidate.getParent()) {
			Path pom = candidate.resolve("pom.xml");
			if (Files.isRegularFile(pom) && isReactorPom(Files.readString(pom, StandardCharsets.UTF_8))) {
				return candidate;
			}
		}
		throw new IOException("No POM containing " + REACTOR_POM_MARKERS + " found at or above "
			+ Path.of("").toAbsolutePath() + ". This test needs the sources of the reactor it belongs to.");
	}

	private static boolean isReactorPom(String pom) {
		for (String marker : REACTOR_POM_MARKERS) {
			if (!pom.contains(marker)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isJavaSource(Path candidate) {
		return Files.isRegularFile(candidate) && candidate.getFileName().toString().endsWith(".java");
	}

	private static String packageOf(String text) {
		Matcher declaration = PACKAGE.matcher(text);
		return declaration.find() ? declaration.group(1) : null;
	}

	/**
	 * The text of a JavaDoc tag as a resource value: one line, without the comment decoration a
	 * continuation line carries.
	 */
	private static String unwrap(String tag) {
		List<String> parts = new ArrayList<>();
		for (String line : tag.strip().split("\n")) {
			String part = line.strip().replaceFirst("^\\*", "").strip();
			if (!part.isEmpty()) {
				parts.add(part);
			}
		}
		return String.join(" ", parts);
	}

	private static int count(Pattern pattern, String text) {
		int result = 0;
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			result++;
		}
		return result;
	}

	private static int lineOf(String text, int position) {
		int result = 1;
		for (int n = 0; n < position; n++) {
			if (text.charAt(n) == '\n') {
				result++;
			}
		}
		return result;
	}

}
