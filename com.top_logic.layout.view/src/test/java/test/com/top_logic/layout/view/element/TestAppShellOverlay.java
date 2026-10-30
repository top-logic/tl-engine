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
import java.util.Locale;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.element.AppBarElement;
import com.top_logic.layout.view.element.AppShellElement;
import com.top_logic.layout.view.element.SectionItemElement;
import com.top_logic.layout.view.element.SidebarElement;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemConfig;
import com.top_logic.layout.view.element.TextElement;

/**
 * Tests that an application extends the {@link AppShellElement shell} tl-layout-view ships as
 * {@code WEB-INF/views/app.view.xml} with a same-path overlay, and that an application with a
 * shell of its own replaces it.
 *
 * <p>
 * The views are read the way the application reads a view that several modules provide: the base
 * first, the contributions after it, folded into one configuration by
 * {@link ViewLoader#parseConfig(List)}.
 * </p>
 */
public class TestAppShellOverlay extends TestCase {

	/** The application shell as tl-layout-view ships it, read from this module's web application. */
	private static final String APP_VIEW = "src/main/webapp/WEB-INF/views/app.view.xml";

	/** The development menu, which the shell places in the trailing area of its app bar. */
	private static final String DEV_MENU = "dev-menu.view.xml";

	/** The account area, which closes the app bar of the shell. */
	private static final String USER_MENU = "user-menu.view.xml";

	/** The section of the shell an application fills with its items. */
	private static final String APPLICATION_SECTION = "application";

	/** The section of the shell holding the system pages. */
	private static final String SYSTEM_SECTION = "system";

	/** The administration item of the shell. */
	private static final String ADMINISTRATION = "administration";

	/**
	 * An application overlay: names the application, sets the start item, adds a channel, places
	 * items into the application section of the shell - the first of them a section of its own - and
	 * one item after the system section.
	 */
	private static final String OVERLAY = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<channels>
					<derived-channel name="count" expr="42"/>
				</channels>
				<app-shell>
					<content>
						<sidebar active-item="home">
							<header config:override="true">
								<text>
									<label>
										<en>My App</en>
									</label>
								</text>
							</header>
							<items>
								<section id="application">
									<section id="pages">
										<label><en>Pages</en></label>
										<nav-item id="home" badge="count">
											<view-ref view="home.view.xml"/>
											<label><en>Home</en></label>
										</nav-item>
									</section>
									<nav-item id="start">
										<view-ref view="start.view.xml"/>
										<label><en>Start</en></label>
									</nav-item>
								</section>
								<nav-item id="about">
									<view-ref view="about.view.xml"/>
									<label><en>About</en></label>
								</nav-item>
							</items>
						</sidebar>
					</content>
					<header>
						<app-bar>
							<title>
								<en>My App</en>
							</title>
						</app-bar>
					</header>
				</app-shell>
			</view>
			""";

	/**
	 * A complete shell of an application, marked as a replacement of the one tl-layout-view ships.
	 */
	private static final String REPLACEMENT = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<app-shell config:override="true">
					<notices>
						<maintenance-notice/>
					</notices>
					<content>
						<sidebar>
							<items>
								<nav-item id="home">
									<view-ref view="home.view.xml"/>
									<label><en>Home</en></label>
								</nav-item>
							</items>
						</sidebar>
					</content>
					<header>
						<app-bar>
							<title>
								<en>Own shell</en>
							</title>
							<trailing>
								<view-ref view="user-menu.view.xml"/>
							</trailing>
						</app-bar>
					</header>
				</app-shell>
			</view>
			""";

	/** The shell shipped with this module can be read on its own and instantiated. */
	public void testBaseAlone() throws Exception {
		ViewElement.Config view = view(base());
		AppShellElement.Config shell = shell(view);

		List<SidebarItemConfig> items = sidebar(shell).getItems();
		assertEquals(List.of(APPLICATION_SECTION, SYSTEM_SECTION), ids(items));
		assertEquals("The application section is left to the application.", List.of(),
			ids(section(items.get(0)).getItems()));
		assertEquals(List.of(ADMINISTRATION), ids(section(items.get(1)).getItems()));
		assertEquals(text("TopLogic", "TopLogic"), appBar(shell).getTitle());
		assertEquals(List.of(DEV_MENU, USER_MENU), views(appBar(shell).getTrailing()));
		assertEquals("Maintenance and session timeout notice.", 2, shell.getNotices().size());

		assertInstantiates(view);
	}

	/**
	 * An overlay extends the sidebar and the app bar of the shell instead of placing second ones
	 * beside them, and fills the application section of the shell, which stands before the system
	 * section.
	 */
	public void testOverlayExtendsShell() throws Exception {
		ViewElement.Config view = view(base(), OVERLAY);
		AppShellElement.Config shell = shell(view);

		assertEquals("The overlay adds its channel.", List.of("count"),
			view.getChannels().stream().map(ChannelConfig::getName).collect(Collectors.toList()));

		assertEquals("The overlay extends the one sidebar of the shell.", 1, shell.getContent().size());
		SidebarElement.Config sidebar = sidebar(shell);
		List<SidebarItemConfig> items = sidebar.getItems();
		assertEquals("The sections of the shell first, the item written beside them after them.",
			List.of(APPLICATION_SECTION, SYSTEM_SECTION, "about"), ids(items));
		SectionItemElement.Config application = section(items.get(0));
		assertEquals("The overlay's items are placed into the section addressed by its id.",
			List.of("pages", "start"), ids(application.getItems()));
		assertEquals(List.of("home"), ids(section(application.getItems().get(0)).getItems()));
		assertEquals(text("Pages"), section(application.getItems().get(0)).getLabel());
		assertEquals("The system section keeps its item.", List.of(ADMINISTRATION),
			ids(section(items.get(1)).getItems()));
		assertEquals("home", sidebar.getActiveItem());
		assertEquals("The base keeps the drawer toggle.", "appbar-leading", sidebar.getDrawerOpenSlotName());

		assertEquals("The marked header replaces the one of the shell.", 1, sidebar.getHeader().size());
		assertEquals(text("My App"), ((TextElement.Config) sidebar.getHeader().get(0)).getLabel());
		assertEquals("The unmarked collapsed header is the one of the shell.", 1,
			sidebar.getHeaderCollapsed().size());

		assertEquals("The overlay extends the one app bar of the shell.", 1, shell.getHeader().size());
		AppBarElement.Config appBar = appBar(shell);
		assertEquals(text("My App"), appBar.getTitle());
		assertEquals("The trailing area is the one of the shell.", List.of(DEV_MENU, USER_MENU),
			views(appBar.getTrailing()));
		assertEquals(1, appBar.getLeading().size());
		assertEquals(1, appBar.getChildren().size());
		assertEquals("The notices of the shell.", 2, shell.getNotices().size());

		assertInstantiates(view);
	}

	/** A shell marked as replacement takes the place of the one shipped as a whole. */
	public void testReplacementTakesPlaceOfShell() throws Exception {
		ViewElement.Config merged = view(base(), REPLACEMENT);
		ViewElement.Config alone = view(REPLACEMENT);

		assertTrue("The merged view is the replacement alone.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(alone, merged));

		assertFalse("The shell differs from the replacement.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(view(base()), merged));

		AppShellElement.Config shell = shell(merged);
		assertEquals(1, shell.getNotices().size());
		assertEquals(List.of("home"), ids(sidebar(shell).getItems()));
		assertEquals(List.of(USER_MENU), views(appBar(shell).getTrailing()));
		assertEquals(text("Own shell"), appBar(shell).getTitle());
		assertNull("The base's drawer toggle is gone with the rest of it.",
			nullIfEmpty(sidebar(shell).getDrawerOpenSlotName()));

		assertInstantiates(merged);
	}

	/** An overlay naming an item the shell does not have as position reference fails. */
	public void testUnknownReferenceFails() throws IOException {
		try {
			view(base(), """
					<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
						<app-shell>
							<content>
								<sidebar>
									<nav-item id="home"
										config:position="before" config:reference="no-such-item">
										<view-ref view="home.view.xml"/>
									</nav-item>
								</sidebar>
							</content>
						</app-shell>
					</view>
					""");
			fail("A reference to an item the shell does not have must fail.");
		} catch (ConfigurationException ex) {
			// Expected: dangling position reference.
		}
	}

	/** Two elements of one kind in a slot of the shell are rejected. */
	public void testDuplicateKindInSlotFails() {
		try {
			view("""
					<view>
						<app-shell>
							<content>
								<text/>
								<text/>
							</content>
						</app-shell>
					</view>
					""");
			fail("Two elements of the same kind in one slot must be rejected.");
		} catch (ConfigurationException ex) {
			// Expected: duplicate key.
		}
	}

	private static String base() throws IOException {
		return Files.readString(new File(APP_VIEW).toPath(), StandardCharsets.UTF_8);
	}

	private static ViewElement.Config view(String... sources) throws ConfigurationException {
		List<CharacterContent> contents = new ArrayList<>();
		for (int n = 0; n < sources.length; n++) {
			contents.add(CharacterContents.newContent(sources[n], "test-app-" + n + ".view.xml"));
		}
		return ViewLoader.parseConfig(contents);
	}

	private static AppShellElement.Config shell(ViewElement.Config view) {
		PolymorphicConfiguration<?> content = view.getContent();
		assertTrue("The view shows an app shell.", content instanceof AppShellElement.Config);
		return (AppShellElement.Config) content;
	}

	private static SidebarElement.Config sidebar(AppShellElement.Config shell) {
		assertEquals("One element in the content slot.", 1, shell.getContent().size());
		return (SidebarElement.Config) shell.getContent().get(0);
	}

	private static AppBarElement.Config appBar(AppShellElement.Config shell) {
		assertEquals("One element in the header slot.", 1, shell.getHeader().size());
		return (AppBarElement.Config) shell.getHeader().get(0);
	}

	private static SectionItemElement.Config section(SidebarItemConfig item) {
		assertTrue("A section: " + item, item instanceof SectionItemElement.Config);
		return (SectionItemElement.Config) item;
	}

	private static List<String> ids(List<SidebarItemConfig> items) {
		return items.stream().map(SidebarItemConfig::getId).collect(Collectors.toList());
	}

	private static List<String> views(List<UIElement.Config> elements) {
		return elements.stream()
			.map(element -> ((ReferenceElement.Config) element).getView().getPath())
			.collect(Collectors.toList());
	}

	/** The literal of an English text, and of a German one where given. */
	private static ResKey text(String english, String... german) {
		ResKey.Builder builder = ResKey.builder().add(Locale.ENGLISH, english);
		for (String translation : german) {
			builder.add(Locale.GERMAN, translation);
		}
		return builder.build();
	}

	private static String nullIfEmpty(String value) {
		return value == null || value.isEmpty() ? null : value;
	}

	private static void assertInstantiates(ViewElement.Config view) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAppShellOverlay.class);
		assertNotNull(context.getInstance(view));
		context.checkErrors();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestAppShellOverlay.class, TypeIndex.Module.INSTANCE);
	}

}
