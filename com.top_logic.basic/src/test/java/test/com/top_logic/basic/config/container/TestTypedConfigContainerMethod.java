/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.container;

import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.A;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.B;
import test.com.top_logic.basic.config.container.ScenarioContainerReference.ScenarioTypeContainerMethodOverride;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Tests for the {@link ConfigPart#container()} method.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigContainerMethod extends AbstractTypedConfigurationTestCase {

	public void testReflectiveContainer() {
		A a = newA("a");
		B b1 = newB("b1");

		assertNull(a.container());
		assertNull(b1.container());

		a.setB(b1);

		assertNull(a.container());
		assertEquals(a, b1.container());

		B b2 = newB("b2");
		assertNull(b2.container());
		a.setB(b2);

		assertNull(a.container());
		assertNull(b1.container());
		assertEquals(a, b2.container());

		a.setB(null);

		assertNull(a.container());
		assertNull(b1.container());
		assertNull(b2.container());
	}

	public void testReflectiveContainerList() {
		A a = newA("a");
		B b1 = newB("b1");

		assertNull(a.container());
		assertNull(b1.container());

		a.getBList().add(b1);

		assertNull(a.container());
		assertEquals(a, b1.container());

		B b2 = newB("b2");
		assertNull(b2.container());
		a.getBList().add(b2);

		assertNull(a.container());
		assertEquals(a, b1.container());
		assertEquals(a, b2.container());

		a.getBList().remove(b1);

		assertNull(a.container());
		assertNull(b1.container());
		assertEquals(a, b2.container());

		a.getBList().clear();

		assertNull(a.container());
		assertNull(b1.container());
		assertNull(b2.container());
	}

	public void testReflectiveContainerMap() {
		A a = newA("a");
		B b1 = newB("b1");

		assertNull(a.container());
		assertNull(b1.container());

		putAMap(a, b1);

		assertNull(a.container());
		assertEquals(a, b1.container());

		B b2 = newB("b2");
		assertNull(b2.container());
		putAMap(a, b2);

		assertNull(a.container());
		assertEquals(a, b1.container());
		assertEquals(a, b2.container());

		removeBMap(a, b1);

		assertNull(a.container());
		assertNull(b1.container());
		assertEquals(a, b2.container());

		a.getBMap().clear();

		assertNull(a.container());
		assertNull(b1.container());
		assertNull(b2.container());
	}

	public void testReadTopLevel() throws ConfigurationException {
		B b = read("<b name='b1'/>");
		assertNull(b.container());
	}

	public void testReadContainer() throws ConfigurationException {
		A a = read("<a><b name='b1'/></a>");
		assertEquals(a, a.getB().container());
	}

	public void testReadContainerList() throws ConfigurationException {
		A a = read("<a><b-list><b name='b1'/></b-list></a>");
		assertEquals(a, a.getBList().get(0).container());
	}

	public void testReadContainerMap() throws ConfigurationException {
		A a = read("<a><b-map><b name='b1'/></b-map></a>");
		assertEquals(a, a.getBMap().get("b1").container());
	}

	public void testReadFallbackContainer() throws ConfigurationException {
		A a1 = read("<a name='a1'><b name='b1'/></a>");
		assertEquals("a1", a1.getName());
		try {
			A a2 = read(a1, "<a name='a2'></a>");
			assertEquals("a1", a1.getName());
			assertEquals("a2", a2.getName());
			assertEquals(a1, a1.getB().container());
			assertEquals(a2, a2.getB().container());
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase.fail("Ticket #12554: Unexpected error:" + ex.getMessage(), ex);
		}
	}

	public void testReadFallbackContainerList() throws ConfigurationException {
		A a1 = read("<a name='a1'><b-list><b name='b1'/></b-list></a>");
		assertEquals("a1", a1.getName());
		try {
			A a2 = read(a1, "<a name='a2'></a>");
			assertEquals("a1", a1.getName());
			assertEquals("a2", a2.getName());
			assertEquals(a1, a1.getBList().get(0).container());
			assertEquals(a2, a2.getBList().get(0).container());
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase.fail("Ticket #12554: Unexpected error:" + ex.getMessage(), ex);
		}
	}

	public void testReadFallbackContainerMap() throws ConfigurationException {
		A a1 = read("<a name='a1'><b-map><b name='b1'/></b-map></a>");
		assertEquals("a1", a1.getName());
		try {
			A a2 = read(a1, "<a name='a2'></a>");
			assertEquals("a1", a1.getName());
			assertEquals("a2", a2.getName());
			assertEquals(a1, a1.getBMap().get("b1").container());
			assertEquals(a2, a2.getBMap().get("b1").container());
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase.fail("Ticket #12554: Unexpected error:" + ex.getMessage(), ex);
		}
	}

	private void putAMap(A a, B b1) {
		a.getBMap().put(b1.getName(), b1);
	}

	private void removeBMap(A a, B b1) {
		a.getBMap().remove(b1.getName());
	}

	private A newA(String name) {
		A result = TypedConfiguration.newConfigItem(A.class);
		result.setName(name);
		return result;
	}

	private B newB(String name) {
		B result = TypedConfiguration.newConfigItem(B.class);
		result.setName(name);
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return new MapBuilder<String, ConfigurationDescriptor>()
			.put("a", TypedConfiguration.getConfigurationDescriptor(A.class))
			.put("b", TypedConfiguration.getConfigurationDescriptor(B.class))
			.toMap();
	}

	public void testContainerMethodOverride() {
		String message = "Overriding the container method has to be forbidden, but is not.";
		String expectedReasonPart = "Overriding method 'container' is not allowed.";
		assertIllegal(message, expectedReasonPart, ScenarioTypeContainerMethodOverride.class);
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigContainerMethod.class);
	}

}
