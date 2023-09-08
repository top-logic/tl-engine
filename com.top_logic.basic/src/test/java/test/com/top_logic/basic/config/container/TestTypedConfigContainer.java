/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.container;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.A;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.AS;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.BZ;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerList;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerListMulti;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerMulti;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerOfDefaultItem;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerOfDefaultItemList;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeDerivedViaContainerEntry;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeDerivedViaContainerParent;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeEntry;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.copy.ConfigCopier;

/**
 * Tests for the {@link Container} annotation on {@link ConfigPart}s.
 * <p>
 * Tests for the {@link ConfigPart#container()} method are in:
 * {@link TestTypedConfigContainerMethod} <br/>
 * Tests for container properties are in: {@link TestTypedConfigContainerProperty} <br/>
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigContainer extends AbstractTypedConfigurationTestCase {

	public void testMultipleContainerReferences() {
		A as = newAS("as");
		BZ b = newBZ("bz");

		as.setB(b);

		assertEquals(as, b.container());
		assertEquals(as, b.getContext());
		assertEquals(as, b.getParent());
	}

	private AS newAS(String name) {
		AS result = TypedConfiguration.newConfigItem(AS.class);
		result.setValue(name);
		return result;
	}

	private BZ newBZ(String name) {
		BZ result = TypedConfiguration.newConfigItem(BZ.class);
		result.setName(name);
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public void testAddToAnotherContainer() {
		ScenarioTypeContainer firstContainer = create(ScenarioTypeContainer.class);
		ScenarioTypeContainer secondContainer = create(ScenarioTypeContainer.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);

		firstContainer.setEntry(entry);
		try {
			secondContainer.setEntry(entry);
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Failed for the wrong reason.",
				"Item is already an entry in another container.", ex.getMessage());
			return; // Okay
		}
		fail("Setting a configItem into multiple containers does not fail.");
	}

	public void testAddToAnotherContainerList() {
		ScenarioTypeContainerList firstContainer = create(ScenarioTypeContainerList.class);
		ScenarioTypeContainerList secondContainer = create(ScenarioTypeContainerList.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);

		firstContainer.setEntries(Collections.singletonList(entry));
		try {
			secondContainer.setEntries(Collections.singletonList(entry));
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Failed for the wrong reason.",
				"Item is already an entry in another container.", ex.getMessage());
			return; // Okay
		}
		fail("Setting a configItem into multiple containers has to fail.");
	}

	public void testAddToSameContainerTwice() {
		ScenarioTypeContainerMulti container = create(ScenarioTypeContainerMulti.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);

		container.setLeftEntry(entry);
		try {
			container.setRightEntry(entry);
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Failed for the wrong reason.",
				"Item is already an entry in another container.", ex.getMessage());
			return; // Okay
		}
		fail("Setting a configItem into multiple properties of the same container has to fail.");
	}

	public void testAddToSameContainerTwiceSameList() {
		ScenarioTypeContainerList container = create(ScenarioTypeContainerList.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);
		ArrayList<ScenarioTypeEntry> entries = new ArrayList<>();
		entries.add(entry);
		entries.add(entry);

		try {
			container.setEntries(entries);
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Failed for the wrong reason.",
				"Item is already an entry in another container.", ex.getMessage());
			return; // Okay
		}
		fail("Setting a list with a duplicate configItem has to fail.");
	}

	public void testAddToSameContainerTwiceDifferentList() {
		ScenarioTypeContainerListMulti container = create(ScenarioTypeContainerListMulti.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);

		container.setLeftEntries(Collections.singletonList(entry));
		try {
			container.setRightEntries(Collections.singletonList(entry));
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Item is already an entry in another container.", ex.getMessage());
			return; // Okay
		}
		fail("Setting a configItem into multiple properties of the same container has to fail.");
	}

	public void testReplaceWithNull() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);

		container.setEntry(entry);
		assertEquals(container, entry.container());
		container.setEntry(null);
		assertEquals(null, entry.container());
	}

	public void testReplaceWithOther() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypeEntry firstEntry = create(ScenarioTypeEntry.class);
		ScenarioTypeEntry secondEntry = create(ScenarioTypeEntry.class);

		container.setEntry(firstEntry);
		assertEquals(container, firstEntry.container());
		container.setEntry(secondEntry);
		assertEquals(null, firstEntry.container());
		assertEquals(container, secondEntry.container());
	}

	public void testReplaceWithSelf() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypeEntry firstEntry = create(ScenarioTypeEntry.class);

		container.setEntry(firstEntry);
		assertEquals(container, firstEntry.container());
		try {
			container.setEntry(firstEntry);
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase.fail("Ticket #11603: Replacing a ConfigPart with itself has to work, but fails.", ex);
		}
		assertEquals(container, firstEntry.container());
	}

	public void testIllegalValueViaConfigReader() {
		String message = "Reading a configuration where the container type is illegal for its entries has to fail.";
		String configXml = "<ScenarioTypeContainer xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "'>"
			+ "<entry config:interface='"
			+ "test.com.top_logic.basic.config.container.ScenarioContainerReference$ScenarioTypeEntrySpecificContainer"
			+ "' /></ScenarioTypeContainer>";
		assertIllegalXml(message, configXml, ScenarioTypeContainer.class);
	}

	public void testDerivedViaContainer() {
		ScenarioTypeDerivedViaContainerEntry entry = create(ScenarioTypeDerivedViaContainerEntry.class);
		ScenarioTypeDerivedViaContainerParent parent = create(ScenarioTypeDerivedViaContainerParent.class);
		parent.setEntry(entry);
		assertEquals(ScenarioTypeDerivedViaContainerParent.DEFAULT_VALUE_SOURCE, entry.getDerived());
		int explicitValue = 7;
		parent.setSource(explicitValue);
		assertEquals(explicitValue, entry.getDerived());
		parent.setEntry(null);
		assertEquals(0, entry.getDerived());
		int explicitValue2 = 13;
		parent.setSource(explicitValue2);
		assertEquals(0, entry.getDerived());
		parent.setEntry(entry);
		assertEquals(explicitValue2, entry.getDerived());
	}

	public void testCopyContainerOfDefaultItem() {
		ScenarioTypeContainerOfDefaultItem container = create(ScenarioTypeContainerOfDefaultItem.class);
		try {
			ConfigCopier.copy(container);
		} catch (Exception ex) {
			BasicTestCase.fail("Ticket #21663: Copy of Container with ConfigPart-Default failed.", ex);
		}
	}

	public void testContainerOfDefaultItem() {
		ScenarioTypeContainerOfDefaultItem container = create(ScenarioTypeContainerOfDefaultItem.class);
		ConfigPart entry = container.getEntry();
		assertSame(container, entry.container());
	}

	public void testContainerOfDefaultItemList() {
		ScenarioTypeContainerOfDefaultItemList container = create(ScenarioTypeContainerOfDefaultItemList.class);
		ConfigPart entry = container.getEntries().get(0);
		assertSame(container, entry.container());
	}

	private void assertIllegalXml(String message, String configXml, Class<? extends ConfigurationItem> globalDescriptor) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(globalDescriptor);
		Map<String, ConfigurationDescriptor> descriptorMap = singletonMap(globalDescriptor.getSimpleName(), descriptor);
		try {
			read(descriptorMap, configXml);
			context.checkErrors();
		} catch (Throwable error) {
			String expectedPart =
				"ScenarioTypeEntrySpecificContainer is only allowed to be put into containers that implement all the types";
			BasicTestCase.assertContains("Failed for the wrong reason.", expectedPart, error.getMessage());
			return;
		}
		fail(message);
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigContainer.class);
	}

}
