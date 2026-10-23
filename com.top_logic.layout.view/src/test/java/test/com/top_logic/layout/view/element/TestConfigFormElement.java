/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.element.ConfigFormElement;
import com.top_logic.layout.view.element.ConfigFormStateHandler;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for {@link ConfigFormElement} - the {@code <config-form>} view element.
 *
 * <p>
 * Exercises what the element decides: where the item to edit comes from, what is shown when there is
 * none, and what happens when the input channel is given a different one.
 * </p>
 */
public class TestConfigFormElement extends TestCase {

	/** The configuration the element under test edits. */
	public interface EditedConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/**
	 * {@link ViewAction} recording the value it is run over, failing on request.
	 */
	public static class RecordingAction implements ViewAction {

		/** Configuration for {@link RecordingAction}. */
		public interface Config extends PolymorphicConfiguration<RecordingAction> {

			/** Configuration name for {@link #getFail()}. */
			String FAIL = "fail";

			@Override
			@ClassDefault(RecordingAction.class)
			Class<? extends RecordingAction> getImplementationClass();

			/** Whether the action fails instead of recording. */
			@Name(FAIL)
			@BooleanDefault(false)
			boolean getFail();

			/** @see #getFail() */
			void setFail(boolean value);
		}

		/** The values the actions of this class were run over, in order. */
		static final List<Object> INPUTS = new ArrayList<>();

		private final boolean _fail;

		/** Creates a {@link RecordingAction} from configuration. */
		@CalledByReflection
		public RecordingAction(InstantiationContext context, Config config) {
			_fail = config.getFail();
		}

		@Override
		public Object execute(ReactContext context, Object input) {
			if (_fail) {
				throw new TopLogicException(com.top_logic.layout.view.I18NConstants.CONFIG_FORM_NO_MODEL);
			}
			INPUTS.add(input);
			return input;
		}
	}

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")));
		RecordingAction.INPUTS.clear();
	}

	/** Builds the element from a configuration with the given property values. */
	private ConfigFormElement element(Object... propertyValuePairs) {
		ConfigFormElement.Config config = TypedConfiguration.newConfigItem(ConfigFormElement.Config.class);
		for (int i = 0; i < propertyValuePairs.length; i += 2) {
			PropertyDescriptor property = config.descriptor().getProperty((String) propertyValuePairs[i]);
			config.update(property, propertyValuePairs[i + 1]);
		}
		return new ConfigFormElement(new DefaultInstantiationContext(TestConfigFormElement.class), config);
	}

	/** Builds an element over the given channel whose Save runs a {@link RecordingAction}. */
	private ConfigFormElement savingElement(boolean failing) {
		ConfigFormElement.Config config = TypedConfiguration.newConfigItem(ConfigFormElement.Config.class);
		config.update(config.descriptor().getProperty(ConfigFormElement.Config.INPUT), new ChannelRef("configuration"));
		RecordingAction.Config action = TypedConfiguration.newConfigItem(RecordingAction.Config.class);
		action.setFail(failing);
		config.getSaveActions().add(action);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestConfigFormElement.class);
		ConfigFormElement result = new ConfigFormElement(context, config);
		assertFalse("The element must instantiate its actions.", context.hasErrors());
		return result;
	}

	/** The command of the given name the displayed form offers. */
	private CommandModel command(IReactControl holder, String name) {
		for (CommandModel command : ((ConfigFormControl) displayed(holder)).commands()) {
			if (name.equals(command.getName())) {
				return command;
			}
		}
		fail("No command named '" + name + "'.");
		return null;
	}

	/** Starts editing the displayed form and changes the name of the edited item. */
	private void edit(IReactControl holder, String name) {
		command(holder, ConfigFormControl.EDIT_COMMAND).executeCommand(_context);
		fieldOf(displayed(holder), EditedConfig.NAME).setValue(name);
	}

	private static FieldModel fieldOf(ReactControl control, String propertyName) {
		if (control.getModel() instanceof ConfigFieldModel field
			&& propertyName.equals(field.getProperty().getPropertyName())) {
			return field;
		}
		for (ReactControl child : control.scriptingChildren()) {
			FieldModel found = fieldOf(child, propertyName);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static EditedConfig named(String name) {
		EditedConfig result = TypedConfiguration.newConfigItem(EditedConfig.class);
		result.setName(name);
		return result;
	}

	/** The single control the element currently displays. */
	private ReactControl displayed(IReactControl holder) {
		List<ReactControl> children = ((ReactControl) holder).scriptingChildren();
		assertEquals("The element shows exactly one thing at a time.", 1, children.size());
		return children.get(0);
	}

	private ViewChannel channelWith(Object value) {
		ViewChannel channel = new DefaultViewChannel("configuration");
		channel.set(value);
		_context.registerChannel("configuration", channel);
		return channel;
	}

	/**
	 * With a type and no channel, the element edits a fresh item of that type - the case an
	 * ordinary form cannot serve, since there is no model object anywhere.
	 */
	public void testATypeAloneIsEnoughToEdit() {
		IReactControl holder = element(ConfigFormElement.Config.TYPE, EditedConfig.class).createControl(_context);

		assertTrue("A form must be shown, not the no-model message.",
			displayed(holder) instanceof ConfigFormControl);
	}

	/** Without a channel value and without a type there is nothing to edit, and the element says so. */
	public void testNothingToEditShowsTheMessage() {
		IReactControl holder = element().createControl(_context);

		assertTrue("The no-model message must stand in for the form.",
			displayed(holder) instanceof ReactTextControl);
	}

	/** The item on the input channel is what gets edited. */
	public void testTheChannelValueIsEdited() {
		channelWith(TypedConfiguration.newConfigItem(EditedConfig.class));

		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);

		assertTrue(displayed(holder) instanceof ConfigFormControl);
	}

	/**
	 * A channel value of some other kind counts as no value: the channel is shared, and a
	 * selection that is not a configuration is not this form's business to complain about.
	 */
	public void testAForeignChannelValueCountsAsNone() {
		channelWith("not a configuration");

		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);

		assertTrue(displayed(holder) instanceof ReactTextControl);
	}

	/** A new item on the channel replaces the form. */
	public void testANewItemReplacesTheForm() {
		ViewChannel channel = channelWith(TypedConfiguration.newConfigItem(EditedConfig.class));
		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);
		ReactControl first = displayed(holder);

		channel.set(TypedConfiguration.newConfigItem(EditedConfig.class));

		assertNotSame("The form must be built afresh over the new item.", first, displayed(holder));
		assertTrue(displayed(holder) instanceof ConfigFormControl);
	}

	/** The channel losing its value takes the form with it. */
	public void testAnEmptiedChannelShowsTheMessageAgain() {
		ViewChannel channel = channelWith(TypedConfiguration.newConfigItem(EditedConfig.class));
		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);
		assertTrue(displayed(holder) instanceof ConfigFormControl);

		channel.set(null);

		assertTrue("Nothing to edit any more, so the message is what belongs here.",
			displayed(holder) instanceof ReactTextControl);
	}

	/**
	 * With an edit mode, the form offers its commands for the enclosing toolbar; without one, it
	 * writes straight through and has no commands to offer.
	 */
	public void testEditModeDecidesWhetherThereAreCommands() {
		IReactControl withMode = element(ConfigFormElement.Config.TYPE, EditedConfig.class).createControl(_context);
		assertFalse("An edit mode is offered through the toolbar.",
			((ConfigFormControl) displayed(withMode)).commands().isEmpty());

		IReactControl withoutMode = element(
			ConfigFormElement.Config.TYPE, EditedConfig.class,
			ConfigFormElement.Config.WITH_EDIT_MODE, Boolean.FALSE).createControl(_context);
		assertTrue("Writing straight through leaves nothing to command.",
			((ConfigFormControl) displayed(withoutMode)).commands().isEmpty());
	}

	/**
	 * Save runs the save actions over the edited working copy, and once they ran through the edits
	 * are carried over and the form leaves edit mode.
	 */
	public void testSaveActionsRunOverTheWorkingCopy() {
		EditedConfig item = named("before");
		channelWith(item);
		IReactControl holder = savingElement(false).createControl(_context);
		edit(holder, "after");

		command(holder, ConfigFormControl.SAVE_COMMAND).executeCommand(_context);

		assertEquals(1, RecordingAction.INPUTS.size());
		EditedConfig saved = (EditedConfig) RecordingAction.INPUTS.get(0);
		assertNotSame("The chain works on the working copy.", item, saved);
		assertEquals("after", saved.getName());
		assertEquals("The edits became the displayed state.", "after", item.getName());
		assertFalse(((ConfigFormControl) displayed(holder)).isEditMode());
	}

	/** A failing save chain leaves the form in edit mode, with the edits intact. */
	public void testAFailingSaveKeepsEditing() {
		EditedConfig item = named("before");
		channelWith(item);
		IReactControl holder = savingElement(true).createControl(_context);
		edit(holder, "after");

		try {
			command(holder, ConfigFormControl.SAVE_COMMAND).executeCommand(_context);
			fail("The failure must be reported.");
		} catch (TopLogicException ex) {
			// Expected.
		}

		ConfigFormControl form = (ConfigFormControl) displayed(holder);
		assertTrue(form.isEditMode());
		assertEquals("after", fieldOf(form, EditedConfig.NAME).getValue());
		assertEquals("before", item.getName());
	}

	/**
	 * A form holding unsaved changes blocks another item on its input channel, naming itself, and
	 * lets it through once the changes are discarded.
	 */
	public void testUnsavedChangesVetoTheInputChannel() {
		ViewChannel channel = channelWith(named("before"));
		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);
		EditedConfig other = named("other");

		channel.set(named("replaced while clean"));
		ReactControl form = displayed(holder);
		edit(holder, "after");

		List<StateHandler> handlers;
		try {
			channel.set(other);
			fail("Unsaved changes must not be dropped silently.");
			return;
		} catch (ChannelVetoException ex) {
			handlers = ex.getDirtyHandlers().stream().map(StateHandler.class::cast).toList();
		}
		assertEquals(1, handlers.size());
		assertSame(form, ((ConfigFormStateHandler) handlers.get(0)).getForm());
		assertSame("The refused write keeps the form.", form, displayed(holder));

		handlers.get(0).executeDiscard();
		channel.set(other);

		assertNotSame(form, displayed(holder));
	}

	/**
	 * A form holding unsaved changes reports them to the enclosing dirty-tracked scope, and takes
	 * them back when they are saved.
	 */
	public void testUnsavedChangesReachTheEnclosingScope() {
		DirtyChannel scope = new DirtyChannel();
		_context.setDirtyChannel(scope);
		EditedConfig item = named("before");
		channelWith(item);
		IReactControl holder = savingElement(false).createControl(_context);

		edit(holder, "after");
		assertEquals(1, scope.getDirtyHandlers().size());
		StateHandler handler = (StateHandler) scope.getDirtyHandlers().get(0);
		assertTrue(handler.isDirty());

		handler.executeSave();

		assertEquals("after", item.getName());
		assertEquals("Saving runs the save chain.", 1, RecordingAction.INPUTS.size());
		assertEquals(List.of(), scope.getDirtyHandlers());
	}

	/** A form that is disposed takes its unsaved changes out of the enclosing scope. */
	public void testADisposedFormLeavesTheScope() {
		DirtyChannel scope = new DirtyChannel();
		_context.setDirtyChannel(scope);
		channelWith(named("before"));
		IReactControl holder = element(ConfigFormElement.Config.INPUT, new ChannelRef("configuration"))
			.createControl(_context);
		edit(holder, "after");
		assertEquals(1, scope.getDirtyHandlers().size());

		((ReactControl) holder).cleanupTree();

		assertEquals(List.of(), scope.getDirtyHandlers());
	}

	/** Suite requiring the services the configuration editor builds its fields with. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFormElement.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
