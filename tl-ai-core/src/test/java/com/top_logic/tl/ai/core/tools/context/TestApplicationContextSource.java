/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.agent.SystemPromptContributor;
import com.top_logic.tl.ai.core.agent.TextPromptContributor;
import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * Tests for {@link ApplicationContextSource}: the application context is written into the
 * application's source configuration as an entry of {@code LLMService}'s prompt contributors, the
 * engine reads it from there, and nothing else of the file changes (Gitea #667).
 */
public class TestApplicationContextSource extends TestCase {

	/** Boots the module the typed configuration and the resources need. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestApplicationContextSource.class));
	}

	private static final List<String> LANGUAGES = List.of("de", "en");

	/** Markdown with what XML would otherwise read as markup, including the end of a CDATA section. */
	private static final String CONTEXT = "# Application context\n\n"
		+ "## Rules\n"
		+ "- An order is late when `$due < now()` & it is not shipped.\n"
		+ "  - Nested: keep the indentation ]]> as written.";

	/** An application's own LLMService file, as the service editor or a developer leaves it. */
	private static final String APPLICATION = """
		<?xml version="1.0" encoding="utf-8" ?>

		<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
			xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
			<services>
				<!-- The application names its own model. -->
				<config service-class="com.top_logic.tl.ai.core.provider.LLMService"
					config:override="true"
				>
					<instance class="com.top_logic.tl.ai.core.provider.LLMService">
						<agent-execution default-model="app-model"/>

						<environment>
							<application>
								<en>Demo</en>
							</application>
						</environment>
					</instance>
				</config>
			</services>
		</application>
		""";

	/** What a lower layer - tl-ai's own configuration - sets for the service. */
	private static final String SHIPPED = """
		<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
			xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
			<services>
				<config service-class="com.top_logic.tl.ai.core.provider.LLMService">
					<instance class="com.top_logic.tl.ai.core.provider.LLMService">
						<agent-execution default-model="shipped-model"/>
						<prompt-contributors>
							<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
								<text>
									<en>House rule.</en>
								</text>
							</contributor>
						</prompt-contributors>
					</instance>
				</config>
			</services>
		</application>
		""";

	private Path _webapp;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_webapp = Files.createTempDirectory("app-source").resolve("src/main/webapp");
		Files.createDirectories(_webapp.resolve(ApplicationContextSource.AUTOCONF));
	}

	@Override
	protected void tearDown() throws Exception {
		try (Stream<Path> files = Files.walk(_webapp.getParent().getParent().getParent())) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(file);
			}
		}
		super.tearDown();
	}

	/**
	 * The application's file gets the entry, every other character of it stays, and the engine reads
	 * the text back exactly - in every language.
	 */
	public void testTheEntryIsAddedToTheApplicationsFile() throws Exception {
		Path file = write("com.top_logic.tl.ai.core.provider.LLMService.config.xml", APPLICATION);

		ApplicationContextSource.Saved saved = source().save(CONTEXT, LANGUAGES);

		assertEquals(List.of(file), saved.files());
		assertFalse(saved.created());
		String written = read(file);
		assertOneInsertion(APPLICATION, written);

		LLMService.Config<?> service = serviceConfig(written);
		assertEquals("The rest of the configuration is the application's.", "app-model",
			service.getAgentExecution().getDefaultModel());
		TextPromptContributor context = applicationContext(service);
		assertEquals(CONTEXT, context.text(Locale.GERMAN));
		assertEquals(CONTEXT, context.text(Locale.ENGLISH));
		assertEquals(CONTEXT, source().read().texts().get("de"));
	}

	/**
	 * A second save replaces the text of the one entry, and keeps the agents a developer named for it.
	 */
	public void testASecondSaveReplacesTheEntry() throws Exception {
		Path file = write("com.top_logic.tl.ai.core.provider.LLMService.config.xml", APPLICATION);
		source().save("First version.", LANGUAGES);
		String afterFirst = read(file);
		Files.writeString(file, afterFirst.replace("name=\"application-context\"",
			"name=\"application-context\"\n\t\t\t\t\t\t\tagents=\"TL_SingleAgent\""), StandardCharsets.UTF_8);

		ApplicationContextSource.Saved saved = source().save(CONTEXT, LANGUAGES);

		assertFalse(saved.added());
		String written = read(file);
		assertEquals(1, occurrences(written, "name=\"application-context\""));
		assertFalse(written.contains("First version."));
		LLMService.Config<?> service = serviceConfig(written);
		assertEquals(1, service.getPromptContributors().size());
		TextPromptContributor context = applicationContext(service);
		assertEquals(CONTEXT, context.text(Locale.ENGLISH));
		assertEquals(List.of("TL_SingleAgent"), context.getConfig().getAgents());
		assertEquals("Outside the entry, the file is as it was after the first save.",
			afterFirst.substring(0, afterFirst.indexOf("<contributor")),
			written.substring(0, written.indexOf("<contributor")));
	}

	/**
	 * An application that configures the service in no file of its own gets a file that adds the
	 * entry and replaces nothing: what tl-ai configured stays.
	 */
	public void testWithoutAFileOfItsOwnTheEntryIsAddedOnTopOfTheShippedConfiguration() throws Exception {
		ApplicationContextSource.Saved saved = source().save(CONTEXT, LANGUAGES);

		assertTrue(saved.created());
		Path created = _webapp.resolve(ApplicationContextSource.AUTOCONF).resolve(ApplicationContextSource.NEW_FILE);
		assertEquals(List.of(created), saved.files());
		String written = read(created);
		assertFalse("The file adds to the service's configuration rather than replacing it.",
			written.contains("config:override=\""));
		assertTrue("Read before a snapshot the service editor would write, which then carries the entry.",
			ApplicationContextSource.NEW_FILE.compareTo("com.top_logic.tl.ai.core.provider.LLMService.config.xml") < 0);

		LLMService.Config<?> service = serviceConfig(SHIPPED, written);
		assertEquals("shipped-model", service.getAgentExecution().getDefaultModel());
		List<SystemPromptContributor> contributors = instances(service);
		assertEquals(2, contributors.size());
		assertEquals("House rule.", ((TextPromptContributor) contributors.get(0)).text(Locale.ENGLISH));
		assertEquals(CONTEXT, applicationContext(service).text(Locale.GERMAN));

		source().save("Revised.", LANGUAGES);
		assertEquals("The next save finds the entry in the file it created.", "Revised.",
			applicationContext(serviceConfig(SHIPPED, read(created))).text(Locale.ENGLISH));
	}

	/**
	 * The entry goes into the file that is read last: when that one replaces the service's
	 * configuration, an entry in an earlier file would be dropped.
	 */
	public void testTheEntryGoesIntoTheFileReadLast() throws Exception {
		Path earlier = write("app.llm.config.xml", SHIPPED.replace("shipped-model", "earlier-model"));
		Path last = write("com.top_logic.tl.ai.core.provider.LLMService.config.xml", APPLICATION);

		source().save(CONTEXT, LANGUAGES);

		assertEquals(SHIPPED.replace("shipped-model", "earlier-model"), read(earlier));
		assertEquals(CONTEXT, applicationContext(serviceConfig(read(earlier), read(last))).text(Locale.ENGLISH));
	}

	private ApplicationContextSource source() {
		return new ApplicationContextSource(_webapp);
	}

	private Path write(String name, String content) throws IOException {
		Path file = _webapp.resolve(ApplicationContextSource.AUTOCONF).resolve(name);
		Files.writeString(file, content, StandardCharsets.UTF_8);
		return file;
	}

	private static String read(Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	/**
	 * Fails unless the changed text is the original with one piece inserted.
	 */
	private static void assertOneInsertion(String original, String changed) {
		int prefix = 0;
		while (prefix < original.length() && original.charAt(prefix) == changed.charAt(prefix)) {
			prefix++;
		}
		int suffix = 0;
		while (suffix < original.length() - prefix
			&& original.charAt(original.length() - 1 - suffix) == changed.charAt(changed.length() - 1 - suffix)) {
			suffix++;
		}
		assertEquals("Everything outside the inserted entry is unchanged.", original.length(), prefix + suffix);
	}

	/**
	 * The service's configuration as the engine reads it from the given layers, the first one lowest.
	 */
	private static LLMService.Config<?> serviceConfig(String... layers) throws ConfigurationException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("application", descriptor));
		List<CharacterContent> sources = new ArrayList<>();
		for (String layer : layers) {
			sources.add(CharacterContents.newContent(layer));
		}
		reader.setSources(sources);
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.read();
		return (LLMService.Config<?>) config.getServices().get(LLMService.class).getInstance();
	}

	private static List<SystemPromptContributor> instances(LLMService.Config<?> service) {
		List<SystemPromptContributor> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends SystemPromptContributor> contributor : service.getPromptContributors()) {
			result.add(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(contributor));
		}
		return result;
	}

	private static TextPromptContributor applicationContext(LLMService.Config<?> service) {
		TextPromptContributor result = null;
		for (SystemPromptContributor contributor : instances(service)) {
			if (contributor instanceof TextPromptContributor text
				&& TextPromptContributor.Config.APPLICATION_CONTEXT.equals(text.getConfig().getName())) {
				assertNull("One entry holds the application context.", result);
				result = text;
			}
		}
		assertNotNull("An entry holds the application context.", result);
		return result;
	}

	private static int occurrences(String text, String part) {
		int count = 0;
		for (int index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1)) {
			count++;
		}
		return count;
	}
}
