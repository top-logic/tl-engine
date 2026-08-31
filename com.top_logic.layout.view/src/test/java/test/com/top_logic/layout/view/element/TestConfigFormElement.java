/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ConfigFormElement;

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

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue()));
	}

	/** Builds the element from a configuration with the given property values. */
	private ConfigFormElement element(Object... propertyValuePairs) {
		ConfigFormElement.Config config = TypedConfiguration.newConfigItem(ConfigFormElement.Config.class);
		for (int i = 0; i < propertyValuePairs.length; i += 2) {
			PropertyDescriptor property = config.descriptor().getProperty((String) propertyValuePairs[i]);
			config.update(property, propertyValuePairs[i + 1]);
		}
		return new ConfigFormElement(null, config);
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

	/** Suite requiring the services the configuration editor builds its fields with. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFormElement.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
