/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 * The German is not written once and then left alone, either: the translation goal re-translates a
 * key whose English changed since the last build, so rewording an {@code @en} text throws away the
 * hand-written German of that key silently. A corrected text is therefore not corrected for good,
 * which is the second reason this comparison has to run on every build rather than once per key.
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
 * Comparing is not enough on its own: a constant that carries no {@code @de} has nothing to compare
 * against, and that is precisely the case the machine translation ships unread. So the German is
 * required, not optional - a constant documented with {@code @en} must carry {@code @de} as well.
 * The machine translation survives only where nobody authored a German text at all, which is the
 * configuration labels and tooltips the doclet derives from prose rather than from a tag. Together
 * with the placeholder check below, that is what leaves no generated German text unread.
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

	/** Where the build writes the English resources of a module. */
	private static final String ENGLISH_BUNDLE = SOURCE_FOLDER + "/META-INF/messages_en.properties";

	/** The bundles the build generates, in the order a failure should report them. */
	private static final List<String> BUNDLES = List.of(ENGLISH_BUNDLE, GERMAN_BUNDLE);

	/**
	 * What the i18n generator writes as the text of a key it cannot resolve.
	 *
	 * <p>
	 * Regenerating a bundle reads it back in and writes it out again, so such a placeholder turns
	 * from a missing key into a present one whose value is the placeholder - which resolves, and
	 * therefore reaches the user. That is how the unregistered bundle of issue #229 stayed invisible
	 * until PR #323. {@code TestModelResources} rejects it for the model resources; here it is
	 * rejected for the generated message bundles, in both languages.
	 * </p>
	 */
	private static final String GENERATOR_PLACEHOLDER = "[missing:";

	/** A module of the reactor, as its POM declares it. */
	private static final Pattern DECLARED_MODULE = Pattern.compile("<module>\\s*([^<\\s]+)\\s*</module>");

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
	 * The {@code @en} tag inside a JavaDoc block.
	 *
	 * <p>
	 * Its presence is what makes the doclet write a resource for the constant at all, so it is also
	 * what makes a German text mandatory. The text itself is of no interest here - the English is
	 * generated from the tag and therefore cannot disagree with it.
	 * </p>
	 */
	private static final Pattern ENGLISH_TAG_LINE =
		Pattern.compile("^\\s*\\*\\s*@en\\s", Pattern.MULTILINE);

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

	/**
	 * What the sources hold: the authored texts, the constants that need one, and what it took to
	 * find them.
	 *
	 * @param texts
	 *        The German text of every constant that carries a {@code @de} tag, by resource key.
	 * @param documented
	 *        Where every constant that carries an {@code @en} tag is declared, by resource key -
	 *        that is, every constant the doclet writes a resource for.
	 */
	private record Authored(Map<String, AuthoredText> texts, Map<String, String> documented, int tags,
			int files) {
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
	 * Every constant the doclet writes a resource for carries an authored German text.
	 *
	 * <p>
	 * This is the check that closes Gitea #331 from this side. Without it the two above compare only
	 * what somebody already bothered to write down: a new constant carrying {@code @en} alone passes
	 * them silently, its German written by {@code tl-maven-plugin:translate} out of the English
	 * string and read by nobody. That is how <em>"Stornierung beantragen"</em> ("request a
	 * cancellation") reached the button that cancels a run.
	 * </p>
	 *
	 * <p>
	 * A tag alone does not ship the text - no generator reads {@code @de} - so a constant added here
	 * needs both: the tag, and the same text in the module's {@code messages_de.properties}. The
	 * check above then holds the two together.
	 * </p>
	 */
	public void testEveryDocumentedConstantAuthorsItsGerman() throws IOException {
		Authored authored = authored();
		List<String> untranslated = new ArrayList<>();
		for (Map.Entry<String, String> documented : authored.documented().entrySet()) {
			if (!authored.texts().containsKey(documented.getKey())) {
				untranslated.add(documented.getKey() + "\n      source  : " + documented.getValue());
			}
		}

		assertEquals("These constants are documented with @en but not with @de, so their German is"
			+ " whatever the build's machine translation made of the English - nobody has read it."
			+ " Write the German as a @de tag next to the @en one, and put the same text into the"
			+ " module's src/main/java/META-INF/messages_de.properties, which is maintained by hand.\n  "
			+ String.join("\n  ", untranslated), List.of(), untranslated);
	}

	/**
	 * No generated bundle ships the generator's placeholder as a text.
	 *
	 * <p>
	 * A placeholder resolves, so nothing at runtime tells it apart from a translation, and a
	 * regeneration keeps it: this is what PR #323 found in the {@code tl.ai.htmlpages} bundle, as the
	 * value of every key, in both languages. Both languages are checked because that is how it
	 * appeared.
	 * </p>
	 */
	public void testNoShippedTextIsAGeneratorPlaceholder() throws IOException {
		List<String> placeholders = new ArrayList<>();
		for (Path module : modules()) {
			for (String bundleName : BUNDLES) {
				Path bundle = module.resolve(bundleName);
				if (!Files.isRegularFile(bundle)) {
					continue;
				}
				Map<String, String> texts = load(bundle);
				for (Map.Entry<String, String> text : texts.entrySet()) {
					if (text.getValue().startsWith(GENERATOR_PLACEHOLDER)) {
						placeholders.add(bundle + "\n      " + text.getKey() + " = " + text.getValue());
					}
				}
			}
		}

		assertEquals("These resources hold the i18n generator's placeholder as their text, so the user"
			+ " reads it. The key the placeholder names could not be resolved when the bundle was"
			+ " generated - register the bundle that defines it, or write the text by hand - and then"
			+ " regenerate, because a placeholder left in the file survives the next generation.\n  "
			+ String.join("\n  ", placeholders), List.of(), placeholders);
	}

	/**
	 * Every module of the reactor is covered by the checks above.
	 *
	 * <p>
	 * They walk the directories next to the reactor POM that have Java sources, which covers a module
	 * added later without anybody remembering this test. The converse is what needs asserting: a
	 * module whose sources move out of {@value #SOURCE_FOLDER} drops out of the walk, and the checks
	 * stay green while covering less.
	 * </p>
	 */
	public void testEveryDeclaredModuleIsChecked() throws IOException {
		Path root = reactorRoot();
		List<String> checked = modules().stream().map(module -> module.getFileName().toString()).toList();
		List<String> missed = new ArrayList<>();
		String pom = Files.readString(root.resolve("pom.xml"), StandardCharsets.UTF_8);
		Matcher declared = DECLARED_MODULE.matcher(pom);
		while (declared.find()) {
			String module = declared.group(1);
			if (!checked.contains(module)) {
				missed.add(module);
			}
		}

		assertEquals("These modules are declared in " + root + "/pom.xml but hold no " + SOURCE_FOLDER
			+ ", so nothing of theirs is checked. The module keeps its Java sources somewhere else -"
			+ " teach this test where, or the resources it generates go unwatched.\n  "
			+ String.join("\n  ", missed), List.of(), missed);
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
		assertTrue("Only " + authored.documented().size() + " constants carrying an @en tag found. They"
			+ " are what testEveryDocumentedConstantAuthorsItsGerman holds to a German text; if they are"
			+ " gone, it holds nothing.", authored.documented().size() > 100);

		int bundleFiles = 0;
		for (Path module : modules()) {
			for (String bundleName : BUNDLES) {
				if (Files.isRegularFile(module.resolve(bundleName))) {
					bundleFiles++;
				}
			}
		}
		assertTrue("Only " + bundleFiles + " generated bundles found over both languages. They are what"
			+ " testNoShippedTextIsAGeneratorPlaceholder reads; either " + BUNDLES + " no longer names"
			+ " them or the doclet did not run.", bundleFiles >= 2 * bundles);
	}

	/** The German texts the developers of this project wrote, by resource key. */
	private static Authored authored() throws IOException {
		Map<String, AuthoredText> texts = new TreeMap<>();
		Map<String, String> documented = new TreeMap<>();
		int tags = 0;
		int files = 0;
		for (Path module : modules()) {
			try (Stream<Path> sources = Files.walk(module.resolve(SOURCE_FOLDER))) {
				for (Path source : sources.filter(TestGermanResourcesMatchJavaDoc::isJavaSource)
					.sorted().toList()) {
					files++;
					String text = Files.readString(source, StandardCharsets.UTF_8);
					tags += count(GERMAN_TAG_LINE, text);
					collectAuthoredTexts(source, text, texts, documented);
				}
			}
		}
		return new Authored(texts, documented, tags, files);
	}

	private static void collectAuthoredTexts(Path source, String text,
			Map<String, AuthoredText> result, Map<String, String> documented) {
		String packageName = packageOf(text);
		if (packageName == null) {
			return;
		}
		String className = source.getFileName().toString().replaceFirst("\\.java$", "");
		Matcher constants = DOCUMENTED_CONSTANT.matcher(text);
		while (constants.find()) {
			String comment = constants.group(1);
			String key = KEY_PREFIX + packageName + "." + className + "." + constants.group(2);
			String where = source + ":" + lineOf(text, constants.start());
			if (ENGLISH_TAG_LINE.matcher(comment).find()) {
				documented.put(key, where);
			}
			Matcher tag = GERMAN_TAG.matcher(comment);
			if (tag.find()) {
				result.put(key, new AuthoredText(key, unwrap(tag.group(1)), where));
			}
		}
	}

	/** The German texts the application reads, merged over every module of the reactor. */
	private static Map<String, String> shippedGermanTexts() throws IOException {
		Map<String, String> result = new TreeMap<>();
		for (Path bundle : germanBundles()) {
			result.putAll(load(bundle));
		}
		return result;
	}

	/** The texts of a single generated bundle, by resource key. */
	private static Map<String, String> load(Path bundle) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = Files.newInputStream(bundle);
				Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		Map<String, String> result = new TreeMap<>();
		for (String key : properties.stringPropertyNames()) {
			result.put(key, properties.getProperty(key));
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
