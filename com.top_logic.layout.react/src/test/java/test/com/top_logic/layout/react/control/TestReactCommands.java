/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.form.FieldValueArguments;

/**
 * Tests the derivations from a typed {@link ReactCommand} item: the {@link
 * ReactCommands#arguments(ReactCommand) dispatch arguments} and the {@link
 * ReactCommand#getTarget() derived target name}.
 */
public class TestReactCommands extends TestCase {

	/**
	 * The dispatch arguments of an item are its argument properties without the address/name
	 * envelope — the same shape the {@code React} client sends.
	 */
	public void testArgumentsStripEnvelope() {
		FieldValueArguments item = TypedConfiguration.newConfigItem(FieldValueArguments.class);
		item.setAddress("/form/formField[members]/textInput");
		item.setName("valueChanged");
		item.setValue("alice");

		Map<String, Object> arguments = ReactCommands.arguments(item);
		assertEquals("alice", arguments.get(FieldValueArguments.VALUE));
		assertFalse(arguments.containsKey(ReactCommand.ADDRESS));
		assertFalse(arguments.containsKey(ReactCommand.NAME));
		assertFalse(arguments.containsKey(ReactCommand.TARGET));
	}

	/**
	 * An argument-less command is a bare {@link ReactCommand} item with an empty argument map.
	 */
	public void testBareCommandHasNoArguments() {
		ReactCommand item = TypedConfiguration.newConfigItem(ReactCommand.class);
		item.setAddress("/panel/button[Neu]");
		item.setName("click");

		assertTrue(ReactCommands.arguments(item).isEmpty());
	}

	/**
	 * The derived target name is the last {@code [name]} segment of the address — the
	 * container-supplied identity a label template renders a positional command with.
	 */
	public void testDerivedTargetName() {
		ReactCommand item = TypedConfiguration.newConfigItem(ReactCommand.class);

		item.setAddress("/form/formField[members]/textInput");
		assertEquals("members", item.getTarget());

		item.setAddress("/panel/button[Neu]");
		assertEquals("Neu", item.getTarget());

		item.setAddress("/appShell/sidebar");
		assertNull(item.getTarget());

		item.setAddress(null);
		assertNull(item.getTarget());
	}
}
