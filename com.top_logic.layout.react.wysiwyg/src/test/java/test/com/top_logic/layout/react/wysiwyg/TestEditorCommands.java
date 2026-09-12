/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.layout.react.wysiwyg.WysiwygControlProvider;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewCommand;

/**
 * Tests the commands an editor is configured with: the toolbar they are offered in, the channels
 * they see, and the insertion of what they write into the edited text.
 *
 * @see ReactWysiwygControl#insertAtCursor(String)
 */
public class TestEditorCommands extends TestCase {

	/** State holding the toolbar of configured commands. */
	private static final String TOOLBAR = "toolbar";

	/** State asking the client to insert markup at the cursor. */
	private static final String INSERT = "insert";

	/** Field of {@link #INSERT} counting the insertions. */
	private static final String INSERT_SEQ = "seq";

	/** Field of {@link #INSERT} holding the markup to insert. */
	private static final String INSERT_HTML = "html";

	/** The command the client sends when the text of the editor changed. */
	private static final String CMD_VALUE_CHANGED = ReactFormFieldControl.CMD_VALUE_CHANGED;

	/** The {@link #CMD_VALUE_CHANGED} argument holding the text. */
	private static final String ARG_VALUE = "value";

	/** The name the editor's insertion channel is declared under. */
	private static final String INSERT_CHANNEL = "insert";

	/** A channel of the surrounding view, which the editor's commands see. */
	private static final String VIEW_CHANNEL = "ticket";

	/** A view declaring an editor with commands of its own. */
	private static final String VIEW_FILE = "test-editor-commands.view.xml";

	/** Markup a command writes to the insertion channel. */
	private static final String MARKUP = "<a href=\"?id=42\" class=\"tlObject\">Some object</a>";

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue());
		_context = new DefaultViewContext(reactContext);
		_context.registerChannel(VIEW_CHANNEL, new DefaultViewChannel(VIEW_CHANNEL));
	}

	/** A command placed in a toolbar is offered in one. */
	public void testConfiguredCommandsAreOfferedInAToolbar() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), null);

		assertTrue("The editor offers no toolbar.", hasToolbar(editor));
	}

	/** An editor without commands has no toolbar of its own. */
	public void testWithoutCommandsThereIsNoToolbar() {
		ReactWysiwygControl editor = editor(List.of(), null);

		assertFalse(hasToolbar(editor));
		assertNull("There is nothing that could write text.", editor.getInsertChannel());
	}

	/** A command placed elsewhere is not rendered in the editor's toolbar. */
	public void testACommandPlacedElsewhereIsNotOffered() {
		ViewCommand.Config command = TypedConfiguration.newConfigItem(GenericViewCommand.Config.class);
		set(command, ViewCommand.Config.PLACEMENT, CommandPlacement.NONE);

		ReactWysiwygControl editor = editor(List.of(command), null);

		assertFalse(hasToolbar(editor));
	}

	/** A command says nothing about where it goes, so it goes where the editor offers commands. */
	public void testACommandGoesToTheToolbarByDefault() {
		ViewCommand.Config command = TypedConfiguration.newConfigItem(GenericViewCommand.Config.class);

		ReactWysiwygControl editor = editor(List.of(command), null);

		assertTrue(hasToolbar(editor));
	}

	/** Text written to the insertion channel is asked of the client. */
	public void testWrittenTextIsInsertedAtTheCursor() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);

		editor.getInsertChannel().set(MARKUP);

		Map<String, Object> insert = insertState(editor);
		assertNotNull("Nothing was asked of the client.", insert);
		assertEquals(MARKUP, insert.get(INSERT_HTML));
		assertEquals(Integer.valueOf(1), insert.get(INSERT_SEQ));
	}

	/** Writing the same text again inserts it again. */
	public void testTheSameTextCanBeInsertedAgain() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);

		editor.getInsertChannel().set(MARKUP);
		editor.getInsertChannel().set(MARKUP);

		assertEquals(Integer.valueOf(2), insertState(editor).get(INSERT_SEQ));
	}

	/** A command that wrote nothing inserts nothing. */
	public void testWritingNothingInsertsNothing() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);

		editor.getInsertChannel().set(null);

		assertNull(insertState(editor));
	}

	/** Only markup is inserted; anything else is not text the editor could show. */
	public void testWritingSomethingThatIsNoMarkupInsertsNothing() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);

		editor.getInsertChannel().set(Integer.valueOf(42));

		assertNull(insertState(editor));
	}

	/** A carried-out insertion is taken back, so that a client mounting anew does not repeat it. */
	public void testACarriedOutInsertionIsTakenBack() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);
		editor.getInsertChannel().set(MARKUP);

		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(ARG_VALUE, "<p>" + MARKUP + "</p>"));

		assertNull("The insertion stands although the client carried it out.", insertState(editor));
	}

	/** Ordinary typing leaves nothing behind. */
	public void testTextArrivingWithoutAnInsertionLeavesNothingBehind() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), INSERT_CHANNEL);

		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(ARG_VALUE, "<p>typed</p>"));

		assertNull(insertState(editor));
	}

	/** The commands see the insertion channel under the name it is declared with. */
	public void testTheCommandsSeeTheInsertionChannel() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(INSERT_CHANNEL)), INSERT_CHANNEL);

		assertNotNull("The command resolved the channel it takes its input from.",
			editor.getInsertChannel());
	}

	/** Without the declaration there is no insertion channel for a command to work on. */
	public void testWithoutTheDeclarationThereIsNoInsertionChannel() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(null)), null);

		assertNull(editor.getInsertChannel());

		try {
			editor(List.of(toolbarCommand(INSERT_CHANNEL)), null);
			fail("A command cannot work on a channel that was never declared.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(INSERT_CHANNEL));
		}
	}

	/** The commands see the channels of the view the edited field is displayed in. */
	public void testTheCommandsSeeTheChannelsOfTheView() {
		ReactWysiwygControl editor = editor(List.of(toolbarCommand(VIEW_CHANNEL)), INSERT_CHANNEL);

		assertTrue("The command resolved the view's channel.", hasToolbar(editor));
	}

	/** A view names the editor's commands and its insertion channel where the field is shown. */
	public void testAViewDeclaresTheEditorsCommands() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestEditorCommands.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestEditorCommands.class, VIEW_FILE));
		ViewElement.Config view = (ViewElement.Config) reader.read();
		context.checkErrors();

		FormElement.Config form = (FormElement.Config) view.getContent();
		FieldElement.Config field = (FieldElement.Config) form.getChildren().get(0);
		WysiwygControlProvider.Config editor = (WysiwygControlProvider.Config) field.getInputControl();

		assertEquals(INSERT_CHANNEL, editor.getInsertChannel());
		assertEquals(1, editor.getCommands().size());
		assertEquals(VIEW_CHANNEL, editor.getCommands().get(0).getInput().getChannelName());
	}

	/**
	 * A command rendered in the editor's toolbar, taking its input from the given channel.
	 */
	private static ViewCommand.Config toolbarCommand(String input) {
		GenericViewCommand.Config command = TypedConfiguration.newConfigItem(GenericViewCommand.Config.class);
		set(command, ViewCommand.Config.PLACEMENT, CommandPlacement.TOOLBAR);
		if (input != null) {
			set(command, ViewCommand.Config.INPUT, new ChannelRef(input));
		}
		return command;
	}

	private static void set(ViewCommand.Config command, String property, Object value) {
		command.update(command.descriptor().getProperty(property), value);
	}

	/**
	 * An editor offering the given commands, created as the provider creates them.
	 */
	private ReactWysiwygControl editor(List<ViewCommand.Config> commandConfigs, String insertChannel) {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestEditorCommands.class);
		List<ViewCommand> commands = commandConfigs.stream()
			.<ViewCommand> map(instantiation::getInstance)
			.toList();
		return new ReactWysiwygControl(_context, new AbstractFieldModel(null), commands, commandConfigs,
			insertChannel);
	}

	/**
	 * Whether the editor offers the toolbar of its commands, under the state name the client
	 * renders it from.
	 */
	private static boolean hasToolbar(ReactWysiwygControl editor) {
		boolean displayed = editor.displayedChildren().stream()
			.anyMatch(child -> child instanceof ReactToolbarControl);
		assertEquals("The toolbar reaches the client under '" + TOOLBAR + "'.",
			displayed, editor.stateAsJSON().contains("\"" + TOOLBAR + "\""));
		return displayed;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> insertState(ReactWysiwygControl editor) {
		return (Map<String, Object>) editor.scriptingScalarState().get(INSERT);
	}

	/**
	 * Test suite requiring the session resources a command's presentation is composed from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestEditorCommands.class, ThreadContextManager.Module.INSTANCE,
				TypeIndex.Module.INSTANCE));
	}

}
