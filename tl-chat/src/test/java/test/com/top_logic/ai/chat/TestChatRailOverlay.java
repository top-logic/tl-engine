/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.ai.chat.view.ChatRailElement.SectionConfig;
import com.top_logic.ai.chat.view.ChatScopeElement;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Tests that a same-path overlay of the shipped chat page {@code tl-chat/albert.view.xml} addresses
 * the sections of its {@code <chat-rail>} by their name.
 *
 * <p>
 * The views are folded the way the application reads a view several modules provide, by
 * {@link ViewLoader#parseConfig(List)}.
 * </p>
 */
public class TestChatRailOverlay extends TestCase {

	/** The shipped page, read from the sources of this module. */
	private static final File ALBERT_VIEW = new File("src/main/webapp/WEB-INF/views/tl-chat/albert.view.xml");

	/** Adds a section between the shipped sections "sources" and "memories". */
	private static final String ADD_SECTION = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<chat-rail config:ref="rail">
					<section name="x" config:position="after" config:reference="sources">
						<label><en>X</en></label>
						<chat-sources/>
					</section>
				</chat-rail>
			</view>
			""";

	/** Changes the shipped section "progress". */
	private static final String UPDATE_SECTION = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<chat-rail config:ref="rail">
					<section name="progress" open="false"/>
				</chat-rail>
			</view>
			""";

	/** Removes the shipped section "memories". */
	private static final String REMOVE_SECTION = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<chat-rail config:ref="rail">
					<section name="memories" config:operation="remove"/>
				</chat-rail>
			</view>
			""";

	/** The shipped page alone. */
	public void testShippedSections() throws Exception {
		ChatRailElement.Config rail = rail(view());
		assertEquals(Arrays.asList("progress", "sources", "memories"), sectionNames(rail));
		assertTrue("The progress section of the shipped page starts open.", section(rail, "progress").getOpen());
	}

	/** A section with a new name is placed relative to a shipped section. */
	public void testAddSectionAfterSources() throws Exception {
		ChatRailElement.Config rail = rail(view(ADD_SECTION));
		assertEquals(Arrays.asList("progress", "sources", "x", "memories"), sectionNames(rail));
		assertEquals("The added section keeps its content.", 1, section(rail, "x").getContent().size());
	}

	/** A section with a shipped name updates that section and adds none. */
	public void testUpdateShippedSection() throws Exception {
		ChatRailElement.Config rail = rail(view(UPDATE_SECTION));
		assertEquals(Arrays.asList("progress", "sources", "memories"), sectionNames(rail));
		SectionConfig progress = section(rail, "progress");
		assertFalse("The overlay's value replaces the shipped one.", progress.getOpen());
		assertEquals("The shipped content of the section stays.", 1, progress.getContent().size());
		assertNotNull("The shipped icon of the section stays.", progress.getIcon());
	}

	/** A shipped section is removed by its name. */
	public void testRemoveShippedSection() throws Exception {
		ChatRailElement.Config rail = rail(view(REMOVE_SECTION));
		assertEquals(Arrays.asList("progress", "sources"), sectionNames(rail));
	}

	/** Two sections of one name are refused, because the name is the key of the section. */
	public void testDuplicateSectionNameIsRefused() throws IOException {
		String duplicate = """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<chat-rail config:ref="rail">
						<section name="x"><chat-sources/></section>
						<section name="x"><chat-sources/></section>
					</chat-rail>
				</view>
				""";
		try {
			view(duplicate);
			fail("Two sections named 'x' in one rail must be refused.");
		} catch (ConfigurationException | RuntimeException ex) {
			// Expected.
		}
	}

	private static ViewElement.Config view(String... fragments) throws ConfigurationException, IOException {
		List<CharacterContent> contents = new ArrayList<>();
		contents.add(CharacterContents.newContent(
			new String(Files.readAllBytes(ALBERT_VIEW.toPath()), StandardCharsets.UTF_8), ALBERT_VIEW.getPath()));
		for (int n = 0; n < fragments.length; n++) {
			contents.add(CharacterContents.newContent(fragments[n], "test-albert-fragment-" + n + ".view.xml"));
		}
		return ViewLoader.parseConfig(contents);
	}

	private static ChatRailElement.Config rail(ViewElement.Config view) {
		assertTrue("The page is one <chat>.", view.getContent() instanceof ChatScopeElement.Config);
		for (UIElement.Config child : ((ChatScopeElement.Config) view.getContent()).getChildren()) {
			if (child instanceof ChatRailElement.Config) {
				return (ChatRailElement.Config) child;
			}
		}
		fail("The page has no <chat-rail>.");
		return null;
	}

	private static SectionConfig section(ChatRailElement.Config rail, String name) {
		for (SectionConfig section : rail.getSections()) {
			if (name.equals(section.getName())) {
				return section;
			}
		}
		fail("No section '" + name + "'.");
		return null;
	}

	private static List<String> sectionNames(ChatRailElement.Config rail) {
		return rail.getSections().stream().map(SectionConfig::getName).collect(Collectors.toList());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the {@link ResourcesModule}.
	 */
	public static Test suite() throws ModuleException {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestChatRailOverlay.class,
			TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
