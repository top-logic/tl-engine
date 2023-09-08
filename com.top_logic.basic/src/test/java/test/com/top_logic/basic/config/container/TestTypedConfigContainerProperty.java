/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.container;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.A;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.AS;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.BX;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.BY;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeGetContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeItemA;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeItemAB;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeItemB;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeItemCommon;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeLocalAndInheritedContainerProperties;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeMoreSpecificContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeMultipleInheritedContainerProperties;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeMultipleLocalContainerProperties;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeNoConfigPartButContainerAnnotation;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeProperty;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertyEntryMoreSpecificContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertyEntrySpecificContainer;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertySetter;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertySub;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertySubNotMentioned;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypePropertySubSpecific;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeSpecificContainer;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Tests for container properties on {@link ConfigPart}s.
 * <p>
 * "Container properties" are properties annotated with {@link Container}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigContainerProperty extends AbstractTypedConfigurationTestCase {

	public void testTypedContainer() {
		A a = newA("a");
		BX bx = newBX("bx");

		assertNull(bx.getParent());

		a.setB(bx);
		assertEquals(a, bx.getParent());
	}

	public void testTypedContainerSubclass() {
		A a = newA("a");
		A as = newAS("as");
		BY b = newBY("by");

		assertNull(b.getContext());

		try {
			a.setB(b);
			fail("Ticket #11603: Must not allow using BY in context of A, since the container is expected to be of type AS.");
		} catch (IllegalArgumentException error) {
			String expectedPart = "BY is only allowed to be put into containers that implement";
			BasicTestCase.assertContains("Failed for the wrong reason.", expectedPart, error.getMessage());
			return;
		}

		as.setB(b);

		assertEquals(as, b.getContext());
	}

	private A newA(String name) {
		A result = TypedConfiguration.newConfigItem(A.class);
		result.setName(name);
		return result;
	}

	private AS newAS(String name) {
		AS result = TypedConfiguration.newConfigItem(AS.class);
		result.setValue(name);
		return result;
	}

	private BX newBX(String name) {
		BX result = TypedConfiguration.newConfigItem(BX.class);
		result.setName(name);
		return result;
	}

	private BY newBY(String name) {
		BY result = TypedConfiguration.newConfigItem(BY.class);
		result.setName(name);
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public void testReflectiveAccess() {
		ScenarioTypeProperty entry = create(ScenarioTypeProperty.class);
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		container.setEntry(entry);

		PropertyDescriptor containerProperty = entry.descriptor().getProperty("parent");
		assertEquals(container, entry.value(containerProperty));
	}

	public void testToString() {
		ScenarioTypeProperty entry = create(ScenarioTypeProperty.class);
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		container.setEntry(entry);
		try {
			assertNotNull(entry.toString());
			assertNotNull(container.toString());
		} catch (StackOverflowError ex) {
			String message =
				"When a ConfigItem has a container property, the toString() method causes a stackoverflow.";
			BasicTestCase.fail(message, ex);
		}
	}

	public void testSetContainerViaSetter() {
		ScenarioTypePropertySetter item = create(ScenarioTypePropertySetter.class);
		ConfigPart child = create(ConfigPart.class);
		try {
			item.setParent(child);
		} catch (UnsupportedOperationException ex) {
			// Good
			return;
		}
		fail("Setting the value of a container property with a setter has to fail.");
	}

	public void testSetContainerViaReflection() {
		ScenarioTypeProperty child = create(ScenarioTypeProperty.class);
		ScenarioTypeContainer parent = create(ScenarioTypeContainer.class);
		try {
			setValue(child, ScenarioTypeProperty.PARENT, parent);
		} catch (UnsupportedOperationException ex) {
			// Good
			return;
		}
		fail("Setting the value of a container property with reflection has to fail.");
	}

	public void testPropertyIsContainer() {
		ScenarioTypeProperty entry = create(ScenarioTypeProperty.class);
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		container.setEntry(entry);

		PropertyDescriptor containerProperty = entry.descriptor().getProperty("parent");
		assertTrue(containerProperty.hasContainerAnnotation());
	}

	public void testSub() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypePropertySub entry = create(ScenarioTypePropertySub.class);

		container.setEntry(entry);
		assertEquals(container, entry.getParent());
	}

	public void testSubNotMentioned() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypePropertySubNotMentioned entry = create(ScenarioTypePropertySubNotMentioned.class);

		container.setEntry(entry);
		assertEquals(container, entry.getParent());
	}

	public void testSubSpecific() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypePropertySubSpecific entry = create(ScenarioTypePropertySubSpecific.class);

		container.setEntry(entry);
		assertEquals(container, entry.getParent());
	}

	public void testSpecificContainer() {
		ScenarioTypeSpecificContainer container = create(ScenarioTypeSpecificContainer.class);
		ScenarioTypePropertyEntrySpecificContainer entry = create(ScenarioTypePropertyEntrySpecificContainer.class);

		container.setEntry(entry);
		assertEquals(container, entry.getParent());
	}

	public void testMoreSpecificContainer() {
		ScenarioTypeMoreSpecificContainer container = create(ScenarioTypeMoreSpecificContainer.class);
		ScenarioTypePropertyEntryMoreSpecificContainer entry =
			create(ScenarioTypePropertyEntryMoreSpecificContainer.class);

		container.setEntry(entry);
		assertEquals(container, entry.getParent());
	}

	public void testSpecificContainerViolation() {
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		ScenarioTypePropertyEntrySpecificContainer entry = create(ScenarioTypePropertyEntrySpecificContainer.class);

		try {
			container.setEntry(entry);
		} catch (IllegalArgumentException error) {
			String expectedPart =
				"ScenarioTypePropertyEntrySpecificContainer is only allowed to be put into containers that implement";
			BasicTestCase.assertContains("Failed for the wrong reason.", expectedPart, error.getMessage());
			return;
		}
		fail("Ticket #11603: Container is incompatible with the result type of the entries container property,"
			+ " but the setter did not fail.");
	}

	public void testGetContainer() {
		ScenarioTypeGetContainer entry = create(ScenarioTypeGetContainer.class);
		ScenarioTypeContainer container = create(ScenarioTypeContainer.class);
		container.setEntry(entry);
		assertEquals(container, entry.getContainer());
	}

	public void testNoConfigPartButContainerAnnotation() {
		assertIllegal("Must fail because @Container annotation is only valid on ConfigPart properties.",
			"Annotating a property with @Container is only allowed in subinterfaces of ConfigPart",
			ScenarioTypeNoConfigPartButContainerAnnotation.class);
	}

	public void testMultipleLocalContainerProperties() {
		ScenarioTypeMultipleLocalContainerProperties item = create(ScenarioTypeMultipleLocalContainerProperties.class);
		String situation = "Situation: Multiple local container properties.";
		assertIllegalContainer(situation, item, create(ScenarioTypeItemCommon.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemA.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemB.class));
		create(ScenarioTypeItemAB.class).setChild(item);
	}

	public void testMultipleInheritedContainerProperties() {
		ScenarioTypeMultipleInheritedContainerProperties item =
			create(ScenarioTypeMultipleInheritedContainerProperties.class);
		String situation = "Situation: Multiple inherited container properties.";
		assertIllegalContainer(situation, item, create(ScenarioTypeItemCommon.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemA.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemB.class));
		create(ScenarioTypeItemAB.class).setChild(item);
	}

	public void testLocalAndInheritedContainerProperties() {
		ScenarioTypeLocalAndInheritedContainerProperties item =
			create(ScenarioTypeLocalAndInheritedContainerProperties.class);
		String situation = "Situation: A local and an inherited container property.";
		assertIllegalContainer(situation, item, create(ScenarioTypeItemCommon.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemA.class));
		assertIllegalContainer(situation, item, create(ScenarioTypeItemB.class));
		create(ScenarioTypeItemAB.class).setChild(item);
	}

	private void assertIllegalContainer(String situation, ConfigurationItem item, ScenarioTypeItemCommon container) {
		try {
			container.setChild(item);
		} catch (IllegalArgumentException ex) {
			String message = situation + " Items of type " + item.getConfigurationInterface().getName()
					+ " are not allowed to be stored in containers of type "
					+ container.getConfigurationInterface().getName()
				+ ", but it failed for the wrong reason.";
			String itemClassName = item.getConfigurationInterface().getSimpleName();
			Pattern pattern = Pattern.compile(
				itemClassName + " is only allowed to be put into containers that implement");
			BasicTestCase.assertErrorMessage(message, pattern, ex);
			// good
			return;
		}
		fail(situation + " Items of type " + item.getConfigurationInterface().getName()
			+ " are not allowed to be stored in containers of type " + container.getConfigurationInterface().getName()
			+ ", but that did not fail.");
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigContainerProperty.class);
	}

}
