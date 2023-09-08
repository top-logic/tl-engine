/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.format;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;

/**
 * Test case for {@link MemorySizeFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMemorySizeFormat extends AbstractTypedConfigurationTestCase {
	
	public interface A extends ConfigurationItem {
		@Format(MemorySizeFormat.class)
		long getX();

		@LongDefault(13L)
		@Format(MemorySizeFormat.class)
		long getY();

		@FormattedDefault("13k")
		@Format(MemorySizeFormat.class)
		long getZ();
	}

	public void testFormat() throws ConfigurationException {
		assertEquals(42L, MemorySizeFormat.INSTANCE.getValue("", "42").longValue());
		assertEquals(42L * 1024, MemorySizeFormat.INSTANCE.getValue("", "42k").longValue());
		assertEquals(42L * 1024 * 1024, MemorySizeFormat.INSTANCE.getValue("", "42m").longValue());
		assertEquals(42L * 1024 * 1024 * 1024, MemorySizeFormat.INSTANCE.getValue("", "42g").longValue());
		assertEquals(42L * 1024 * 1024 * 1024 * 1024, MemorySizeFormat.INSTANCE.getValue("", "42t").longValue());
		assertEquals(42L * 1024 * 1024 * 1024 * 1024 * 1024, MemorySizeFormat.INSTANCE.getValue("", "42p").longValue());
	}

	public void testSerialize() {
		assertEquals("0", MemorySizeFormat.INSTANCE.getSpecification(0L));
		assertEquals("42", MemorySizeFormat.INSTANCE.getSpecification(42L));
		assertEquals("42k", MemorySizeFormat.INSTANCE.getSpecification(42L * 1024));
		assertEquals("42m", MemorySizeFormat.INSTANCE.getSpecification(42L * 1024 * 1024));
		assertEquals("42g", MemorySizeFormat.INSTANCE.getSpecification(42L * 1024 * 1024 * 1024));
		assertEquals("42t", MemorySizeFormat.INSTANCE.getSpecification(42L * 1024 * 1024 * 1024 * 1024));
		assertEquals("42p", MemorySizeFormat.INSTANCE.getSpecification(42L * 1024 * 1024 * 1024 * 1024 * 1024));
	}
	
	public void testParse_WithoutDefault_NotMentioned() throws ConfigurationException {
		assertEquals(0L, fromXML("<a/>").getX());
	}

	public void testParse_WithoutDefault_EmptyString() {
		String messagePrimitivePropertyNull = "Setting a primitive property to null has to fail.";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");

		assertIllegalXml(messagePrimitivePropertyNull, "<A x=''/>", errorPattern, A.class);
	}

	public void testParse_WithoutDefault_0() throws ConfigurationException {
		assertEquals(0L, fromXML("<a x='0'/>").getX());
	}

	public void testParse_WithoutDefault_42() throws ConfigurationException {
		assertEquals(42L, fromXML("<a x='42'/>").getX());
	}

	public void testParse_WithoutDefault_42k() throws ConfigurationException {
		assertEquals(42L * 1024, fromXML("<a x='42k'/>").getX());
	}

	public void testParse_WithLongDefault_NotMentioned() throws ConfigurationException {
		assertEquals(13L, fromXML("<a/>").getY());
	}

	public void testParse_WithLongDefault_EmptyString() {
		String messagePrimitivePropertyNull = "Setting a primitive property to null has to fail.";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");

		assertIllegalXml(messagePrimitivePropertyNull, "<A y=''/>", errorPattern, A.class);
	}

	public void testParse_WithLongDefault_0() throws ConfigurationException {
		assertEquals(0L, fromXML("<a y='0'/>").getY());
	}

	public void testParse_WithLongDefault_42() throws ConfigurationException {
		assertEquals(42L, fromXML("<a y='42'/>").getY());
	}

	public void testParse_WithFormattedDefault_NotMentioned() throws ConfigurationException {
		assertEquals(13L * 1024, fromXML("<a/>").getZ());
	}

	public void testParse_WithFormattedDefault_EmptyString() {
		String messagePrimitivePropertyNull = "Setting a primitive property to null has to fail.";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");

		assertIllegalXml(messagePrimitivePropertyNull, "<A z=''/>", errorPattern, A.class);
	}

	public void testParse_WithFormattedDefault_0() throws ConfigurationException {
		assertEquals(0L, fromXML("<a z='0'/>").getZ());
	}

	public void testParse_WithFormattedDefault_42() throws ConfigurationException {
		assertEquals(42L, fromXML("<a z='42'/>").getZ());
	}

	@Override
	protected A fromXML(String xml) throws ConfigurationException {
		return (A) super.fromXML(xml);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

}
