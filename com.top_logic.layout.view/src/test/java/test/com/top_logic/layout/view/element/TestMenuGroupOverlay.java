/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.AbstractMenuElement.Config.CommandGroup;
import com.top_logic.layout.view.element.MenuElement;

/**
 * Tests that a same-path overlay of a view extends the {@link CommandGroup groups} of a
 * {@link MenuElement} it does not define, addressing them by their {@link CommandGroup#getId() ID}.
 *
 * <p>
 * The views are read the way the application reads a view that several modules provide: the base
 * first, the contributions after it, folded into one configuration by
 * {@link ViewLoader#parseConfig(List)}.
 * </p>
 */
public class TestMenuGroupOverlay extends TestCase {

	/** The development menu as tl-layout-view ships it, read from this module's web application. */
	private static final String DEV_MENU = "src/main/webapp/WEB-INF/views/dev-menu.view.xml";

	/** A menu with two groups, the base the overlays of the tests extend. */
	private static final String BASE = """
			<view>
				<menu>
					<text/>
					<groups>
						<group id="first">
							<generic-command name="a"/>
						</group>
						<group id="second">
							<generic-command name="b"/>
						</group>
					</groups>
				</menu>
			</view>
			""";

	/** An overlay naming an existing group adds its entries at the end of that group. */
	public void testOverlayAddsEntryToGroup() throws Exception {
		MenuElement.Config menu = menu(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<menu>
						<groups>
							<group id="second" config:operation="update">
								<generic-command name="c"/>
							</group>
						</groups>
					</menu>
				</view>
				""");

		List<CommandGroup> groups = menu.getGroups();
		assertEquals("The overlay adds no group.", List.of("first", "second"), ids(groups));
		assertEquals(List.of("a"), names(groups.get(0)));
		assertEquals("The contributed entry follows the ones of the base.", List.of("b", "c"),
			names(groups.get(1)));
	}

	/** An overlay naming a group the base does not have adds it after the existing ones. */
	public void testOverlayAddsGroup() throws Exception {
		MenuElement.Config menu = menu(BASE, """
				<view>
					<menu>
						<groups>
							<group id="third">
								<generic-command name="c"/>
							</group>
						</groups>
					</menu>
				</view>
				""");

		List<CommandGroup> groups = menu.getGroups();
		assertEquals(List.of("first", "second", "third"), ids(groups));
		assertEquals("A group of the base stays as it is.", List.of("b"), names(groups.get(1)));
		assertEquals(List.of("c"), names(groups.get(2)));
	}

	/** An update of a group the base does not have fails rather than adding entries nowhere. */
	public void testUpdateOfMissingGroupFails() {
		try {
			menu(BASE, """
					<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
						<menu>
							<groups>
								<group id="renamed" config:operation="update">
									<generic-command name="c"/>
								</group>
							</groups>
						</menu>
					</view>
					""");
			fail("Updating a group that does not exist must fail.");
		} catch (ConfigurationException ex) {
			// Expected: the contribution names a group the menu does not have.
		}
	}

	/** Two groups of one menu cannot share an ID, since an overlay could not tell them apart. */
	public void testDuplicateGroupIdFails() {
		try {
			menu("""
					<view>
						<menu>
							<text/>
							<groups>
								<group id="same">
									<generic-command name="a"/>
								</group>
								<group id="same">
									<generic-command name="b"/>
								</group>
							</groups>
						</menu>
					</view>
					""");
			fail("Two groups with the same ID must be rejected.");
		} catch (ConfigurationException ex) {
			// Expected: duplicate key.
		}
	}

	/**
	 * The development menu shipped with this module takes an entry contributed into its automation
	 * group the way the agent access module contributes one.
	 */
	public void testDevMenuTakesContribution() throws Exception {
		String base = Files.readString(new File(DEV_MENU).toPath(), StandardCharsets.UTF_8);
		MenuElement.Config menu = menu(base, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<menu>
						<groups>
							<group id="automation" config:operation="update">
								<generic-command name="contributed" image="css:bi bi-robot">
									<executability>
										<authenticated-only/>
									</executability>
									<open-dialog dialog-view="agent-access.view.xml"/>
								</generic-command>
							</group>
						</groups>
					</menu>
				</view>
				""");

		List<CommandGroup> groups = menu.getGroups();
		assertEquals(List.of("page", "automation"), ids(groups));
		assertEquals("Designer and UI inspector.", 2, groups.get(0).getCommands().size());

		List<PolymorphicConfiguration<? extends ViewCommand>> automation = groups.get(1).getCommands();
		assertEquals("The script recorder and the contributed entry.", 2, automation.size());
		assertEquals("The contribution follows the entry of the base.", "contributed",
			((ViewCommand.Config) automation.get(1)).getName());

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestMenuGroupOverlay.class);
		assertNotNull(context.getInstance(menu));
		context.checkErrors();
	}

	/** The development menu shipped with this module can be read on its own. */
	public void testDevMenuAlone() throws ConfigurationException, IOException {
		String base = Files.readString(new File(DEV_MENU).toPath(), StandardCharsets.UTF_8);
		List<CommandGroup> groups = menu(base).getGroups();
		assertEquals(List.of("page", "automation"), ids(groups));
		assertEquals("The script recorder.", 1, groups.get(1).getCommands().size());
	}

	private static MenuElement.Config menu(String... sources) throws ConfigurationException {
		List<CharacterContent> contents = new ArrayList<>();
		for (int n = 0; n < sources.length; n++) {
			contents.add(CharacterContents.newContent(sources[n], "test-menu-" + n + ".view.xml"));
		}
		ViewElement.Config config = ViewLoader.parseConfig(contents);
		PolymorphicConfiguration<?> content = config.getContent();
		assertTrue("The view shows a menu.", content instanceof MenuElement.Config);
		return (MenuElement.Config) content;
	}

	private static List<String> ids(List<CommandGroup> groups) {
		return groups.stream().map(CommandGroup::getId).collect(Collectors.toList());
	}

	private static List<String> names(CommandGroup group) {
		return group.getCommands().stream()
			.map(command -> ((ViewCommand.Config) command).getName())
			.collect(Collectors.toList());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestMenuGroupOverlay.class, TypeIndex.Module.INSTANCE);
	}

}
