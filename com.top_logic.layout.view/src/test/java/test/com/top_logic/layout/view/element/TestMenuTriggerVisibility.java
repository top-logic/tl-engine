/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.view.command.FakeCommandModelBase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.MenuRenderer;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.MenuRegionControl;
import com.top_logic.layout.view.command.ViewCommandSource;
import com.top_logic.layout.view.element.ContextMenuElement;
import com.top_logic.layout.view.element.MenuElement;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a {@link MenuElement} shows its trigger exactly while its menu offers an entry, and
 * that a {@link ContextMenuElement} keeps its content regardless.
 *
 * <p>
 * The elements are exercised through their public seam - a view read the way the application reads
 * it, a control created for a view context holding the channel the commands decide over, and the
 * visibility that control publishes once displayed.
 * </p>
 */
public class TestMenuTriggerVisibility extends TestCase {

	/** The channel the commands of the test views take as input. */
	private static final String FLAG_CHANNEL = "flag";

	/** A command hidden while {@link #FLAG_CHANNEL} is empty. */
	private static final String HIDDEN_WHILE_EMPTY = """
			<generic-command name="hiddenWhileEmpty" input="%s">
				<executability>
					<null-input-hidden/>
				</executability>
			</generic-command>
			""".formatted(FLAG_CHANNEL);

	/** A command disabled, but still shown, while {@link #FLAG_CHANNEL} is empty. */
	private static final String DISABLED_WHILE_EMPTY = """
			<generic-command name="disabledWhileEmpty" input="%s">
				<executability>
					<null-input-disabled/>
				</executability>
			</generic-command>
			""".formatted(FLAG_CHANNEL);

	/** A command without rules, offered always. */
	private static final String ALWAYS = """
			<generic-command name="always"/>
			""";

	private ViewChannel _flag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_flag = new DefaultViewChannel(FLAG_CHANNEL);
	}

	/** A menu whose only entry is hidden by its rule shows no trigger. */
	public void testOnlyEntryHiddenHidesTrigger() throws Exception {
		MenuRegionControl menu = menu(HIDDEN_WHILE_EMPTY, "");

		assertFalse("The menu has nothing to offer.", menu.offersEntry());
		assertTrue("A menu without entries shows no trigger.", menu.isHidden());
	}

	/** A menu with an entry its rules allow shows its trigger. */
	public void testVisibleEntryShowsTrigger() throws Exception {
		MenuRegionControl menu = menu(HIDDEN_WHILE_EMPTY + ALWAYS, "");

		assertTrue(menu.offersEntry());
		assertFalse("A menu offering an entry shows its trigger.", menu.isHidden());
	}

	/** A disabled entry is still offered - as a disabled entry - so the trigger stays. */
	public void testDisabledEntryShowsTrigger() throws Exception {
		MenuRegionControl menu = menu(DISABLED_WHILE_EMPTY, "");

		assertTrue("A disabled entry is offered.", menu.offersEntry());
		assertFalse("A menu offering only a disabled entry shows its trigger.", menu.isHidden());
	}

	/** The trigger follows the entry's executability while the menu is displayed. */
	public void testTriggerFollowsEntryState() throws Exception {
		MenuRegionControl menu = menu(HIDDEN_WHILE_EMPTY, "");
		assertTrue(menu.isHidden());

		_flag.set("on");
		assertFalse("The entry becoming visible brings the trigger back.", menu.isHidden());

		_flag.set(null);
		assertTrue("The entry being hidden again takes the trigger away.", menu.isHidden());
	}

	/** An entry in a configured group counts like one written directly in the menu. */
	public void testGroupEntriesCount() throws Exception {
		String group = "<groups><group>%s</group></groups>";

		MenuRegionControl hiddenGroup = menu(HIDDEN_WHILE_EMPTY, group.formatted(HIDDEN_WHILE_EMPTY));
		assertTrue("A group offering nothing leaves the menu empty.", hiddenGroup.isHidden());

		_flag.set("on");
		assertFalse("A group entry becoming visible brings the trigger back.", hiddenGroup.isHidden());

		_flag.set(null);
		MenuRegionControl offeringGroup = menu(HIDDEN_WHILE_EMPTY, group.formatted(ALWAYS));
		assertFalse("A group entry is offered like any other.", offeringGroup.isHidden());
	}

	/** The commands of a source count as entries of the menu. */
	public void testSourceEntriesCount() throws Exception {
		String source = "<command-sources><command-source class='" + Source.class.getName()
			+ "' offer='%s'/></command-sources>";

		assertTrue("A source offering nothing leaves the menu empty.",
			menu(HIDDEN_WHILE_EMPTY, source.formatted(false)).isHidden());
		assertFalse("A source offering a command shows the trigger.",
			menu(HIDDEN_WHILE_EMPTY, source.formatted(true)).isHidden());
	}

	/**
	 * The region of a context menu is content in its own right, so it stays while its menu offers
	 * nothing.
	 */
	public void testContextMenuKeepsContent() throws Exception {
		MenuRegionControl region = region("""
				<context-menu>
					<text/>
					<commands>
						<generic-command name="hiddenWhileEmpty" input="%s" placement="CONTEXT_MENU">
							<executability>
								<null-input-hidden/>
							</executability>
						</generic-command>
					</commands>
				</context-menu>
				""".formatted(FLAG_CHANNEL), ContextMenuElement.Config.class);

		assertFalse("The context menu has nothing to offer.", region.offersEntry());
		assertFalse("The content carrying an empty context menu stays.", region.isHidden());
	}

	/** The displayed control of a {@code <menu>} with the given commands and further contents. */
	private MenuRegionControl menu(String commands, String further) throws Exception {
		return region("""
				<menu>
					<text/>
					<commands>
						%s
					</commands>
					%s
				</menu>
				""".formatted(commands, further), MenuElement.Config.class);
	}

	/** The displayed control of the given menu element. */
	private MenuRegionControl region(String elementXml, Class<?> expectedConfig) throws ConfigurationException {
		DefaultInstantiationContext instantiationContext =
			new DefaultInstantiationContext(TestMenuTriggerVisibility.class);
		UIElement element = instantiationContext.getInstance(content(elementXml, expectedConfig));
		instantiationContext.checkErrors();

		ViewContext context = new DefaultViewContext(new PageContext())
				.withContextMenuOpener(new ContextMenuOpener(new NoRenderer()));
		context.registerChannel(FLAG_CHANNEL, _flag);

		MenuRegionControl region = (MenuRegionControl) element.createControl(context);
		// Displaying the region lets its commands follow their input.
		region.attach();
		return region;
	}

	private static PolymorphicConfiguration<? extends UIElement> content(String elementXml, Class<?> expectedConfig)
			throws ConfigurationException {
		String view = "<view>" + elementXml + "</view>";
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, "test-menu.view.xml")));
		PolymorphicConfiguration<? extends UIElement> content = config.getContent();
		assertTrue("Unexpected content: " + content, expectedConfig.isInstance(content));
		return content;
	}

	/**
	 * {@link ViewCommandSource} offering a single command, or none.
	 */
	public static class Source implements ViewCommandSource {

		/**
		 * Configuration for {@link Source}.
		 */
		public interface Config extends ViewCommandSource.Config<Source> {

			/** Configuration name for {@link #getOffer()}. */
			String OFFER = "offer";

			@Override
			@ClassDefault(Source.class)
			Class<? extends Source> getImplementationClass();

			/**
			 * Whether the source offers its command.
			 */
			@Name(OFFER)
			boolean getOffer();
		}

		private final boolean _offer;

		/**
		 * Creates a {@link Source} from configuration.
		 */
		public Source(InstantiationContext context, Config config) {
			_offer = config.getOffer();
		}

		@Override
		public ResKey getLabel() {
			return null;
		}

		@Override
		public List<CommandModel> getCommands(ViewContext context) {
			return _offer ? List.of(new FakeCommandModelBase("sourced")) : List.of();
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * Nothing displayed here is an object others observe, so the page reports no
	 * {@link ModelScope}, and the commands follow their input channel alone - which spares the test
	 * the knowledge base a scope is built from.
	 * </p>
	 */
	private static final class PageContext extends DefaultReactContext {

		PageContext() {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * {@link MenuRenderer} for a frame nobody looks at.
	 */
	private static final class NoRenderer implements MenuRenderer {

		@Override
		public void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			// Nothing displayed.
		}

		@Override
		public void hide() {
			// Nothing displayed.
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestMenuTriggerVisibility.class, TypeIndex.Module.INSTANCE);
	}
}
