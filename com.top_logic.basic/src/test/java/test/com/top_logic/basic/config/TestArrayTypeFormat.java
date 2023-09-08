/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.time.Month;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;

/**
 * Test for array types in typed configurations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestArrayTypeFormat extends AbstractTypedConfigurationTestCase {

	public enum TestEnum {
		enum1, enum2, enum3;
	}

	public interface TestArrayFormatConfig extends ConfigurationItem {

		String INT_ARRAY_NAME = "int-array";

		@Name(INT_ARRAY_NAME)
		int[] getIntArray();

		String INT_WRAPPER_ARRAY_NAME = "int-wrapper-array";

		@Name(INT_WRAPPER_ARRAY_NAME)
		Integer[] getIntWrapperArray();

		String LONG_ARRAY_NAME = "long-array";

		@Name(LONG_ARRAY_NAME)
		long[] getLongArray();

		String LONG_WRAPPER_ARRAY_NAME = "long-wrapper-array";

		@Name(LONG_WRAPPER_ARRAY_NAME)
		Long[] getLongWrapperArray();

		String DOUBLE_ARRAY_NAME = "double-array";

		@Name(DOUBLE_ARRAY_NAME)
		double[] getDoubleArray();

		String DOUBLE_WRAPPER_ARRAY_NAME = "double-wrapper-array";

		@Name(DOUBLE_WRAPPER_ARRAY_NAME)
		Double[] getDoubleWrapperArray();

		String FLOAT_ARRAY_NAME = "float-array";

		@Name(FLOAT_ARRAY_NAME)
		float[] getFloatArray();

		String FLOAT_WRAPPER_ARRAY_NAME = "float-wrapper-array";

		@Name(FLOAT_WRAPPER_ARRAY_NAME)
		Float[] getFloatWrapperArray();

		String SHORT_ARRAY_NAME = "short-array";

		@Name(SHORT_ARRAY_NAME)
		short[] getShortArray();

		String SHORT_WRAPPER_ARRAY_NAME = "short-wrapper-array";

		@Name(SHORT_WRAPPER_ARRAY_NAME)
		Short[] getShortWrapperArray();

		String BYTE_ARRAY_NAME = "byte-array";

		@Name(BYTE_ARRAY_NAME)
		byte[] getByteArray();

		String BYTE_WRAPPER_ARRAY_NAME = "byte-wrapper-array";

		@Name(BYTE_WRAPPER_ARRAY_NAME)
		Byte[] getByteWrapperArray();

		String BOOLEAN_ARRAY_NAME = "boolean-array";

		@Name(BOOLEAN_ARRAY_NAME)
		boolean[] getBooleanArray();

		String BOOLEAN_WRAPPER_ARRAY_NAME = "boolean-wrapper-array";

		@Name(BOOLEAN_WRAPPER_ARRAY_NAME)
		Boolean[] getBooleanWrapperArray();

		String GENERIC_ENUM_ARRAY_NAME = "generic-enum-array";

		@Name(GENERIC_ENUM_ARRAY_NAME)
		Enum<?>[] getGenericEnumArray();

		String STRING_ARRAY_NAME = "string-array";

		@Name(STRING_ARRAY_NAME)
		String[] getStringArray();

		String CLASS_ARRAY_NAME = "class-array";

		@Name(CLASS_ARRAY_NAME)
		Class<?>[] getClassArray();

		String DATE_ARRAY_NAME = "date-array";

		@Name(DATE_ARRAY_NAME)
		Date[] getDateArray();

		String CONCRETE_ENUM_ARRAY_NAME = "concrete-enum-array";

		@Name(CONCRETE_ENUM_ARRAY_NAME)
		TestEnum[] getConcreteEnumArray();

	}

	private TestArrayFormatConfig _configItem;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_configItem = TypedConfiguration.newConfigItem(TestArrayFormatConfig.class);
	}

	@Override
	protected void tearDown() throws Exception {
		_configItem = null;

		super.tearDown();
	}

	public void testFillConfiguration() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		values.put(TestArrayFormatConfig.BOOLEAN_ARRAY_NAME, "true,false, false");
		values.put(TestArrayFormatConfig.BOOLEAN_WRAPPER_ARRAY_NAME, "true,false, ,false");
		values.put(TestArrayFormatConfig.BYTE_ARRAY_NAME, "0,1, 2");
		values.put(TestArrayFormatConfig.BYTE_WRAPPER_ARRAY_NAME, "0,,1, 2");
		values.put(TestArrayFormatConfig.SHORT_ARRAY_NAME, "0,1, 2");
		values.put(TestArrayFormatConfig.SHORT_WRAPPER_ARRAY_NAME, "0,,1, 2");
		values.put(TestArrayFormatConfig.INT_ARRAY_NAME, "0,1, 2");
		values.put(TestArrayFormatConfig.INT_WRAPPER_ARRAY_NAME, "0,,1, 2");
		values.put(TestArrayFormatConfig.LONG_ARRAY_NAME, "0 , 1, 2147483648");
		values.put(TestArrayFormatConfig.LONG_WRAPPER_ARRAY_NAME, "0 , , 1, 2147483648");
		values.put(TestArrayFormatConfig.DOUBLE_ARRAY_NAME, "0d , 0.123d, 3E-7");
		values.put(TestArrayFormatConfig.DOUBLE_WRAPPER_ARRAY_NAME, "0d , 0.123d, , 3E-7");
		values.put(TestArrayFormatConfig.FLOAT_ARRAY_NAME, "0d , 0.123d, 3E-7");
		values.put(TestArrayFormatConfig.FLOAT_WRAPPER_ARRAY_NAME, "0d , 0.123d, , 3E-7");
		values.put(TestArrayFormatConfig.STRING_ARRAY_NAME, "a,b, ,c");
		values.put(TestArrayFormatConfig.CLASS_ARRAY_NAME, "java.lang.String , java.util.Map, ,java.lang.Integer");
		values.put(TestArrayFormatConfig.GENERIC_ENUM_ARRAY_NAME,
			"test.com.top_logic.basic.config.TestArrayTypeFormat$TestEnum:enum1 ,, java.time.Month:JANUARY");
		values.put(TestArrayFormatConfig.CONCRETE_ENUM_ARRAY_NAME, "enum1 , enum3, enum2  ");
		values.put(TestArrayFormatConfig.DATE_ARRAY_NAME, "2019-11-08T08:51:00.003Z , , 1978-01-19T02:34:10.235Z ");
		TypedConfiguration.fillValues(_configItem, values.entrySet());

		BasicTestCase.assertEquals(new boolean[] { true, false, false }, _configItem.getBooleanArray());
		BasicTestCase.assertEquals(new Boolean[] { Boolean.TRUE, Boolean.FALSE, null, Boolean.FALSE },
			_configItem.getBooleanWrapperArray());
		BasicTestCase.assertEquals(new byte[] { 0, 1, 2 }, _configItem.getByteArray());
		BasicTestCase.assertEquals(new Byte[] { 0, null, 1, 2 }, _configItem.getByteWrapperArray());
		BasicTestCase.assertEquals(new short[] { 0, 1, 2 }, _configItem.getShortArray());
		BasicTestCase.assertEquals(new Short[] { 0, null, 1, 2 }, _configItem.getShortWrapperArray());
		BasicTestCase.assertEquals(new int[] { 0, 1, 2 }, _configItem.getIntArray());
		BasicTestCase.assertEquals(new Integer[] { 0, null, 1, 2 }, _configItem.getIntWrapperArray());
		BasicTestCase.assertEquals(new long[] { 0, 1, 2147483648L }, _configItem.getLongArray());
		BasicTestCase.assertEquals(new Long[] { 0L, null, 1L, 2147483648L }, _configItem.getLongWrapperArray());
		BasicTestCase.assertEquals(new double[] { 0, 0.123D, 3E-7 }, _configItem.getDoubleArray());
		BasicTestCase.assertEquals(new Double[] { 0D, 0.123D, null, 3E-7 }, _configItem.getDoubleWrapperArray());
		BasicTestCase.assertEquals(new float[] { 0, 0.123F, 3E-7F }, _configItem.getFloatArray());
		BasicTestCase.assertEquals(new Float[] { 0F, 0.123F, null, 3E-7F }, _configItem.getFloatWrapperArray());
		BasicTestCase.assertEquals(new String[] { "a", "b", "", "c" }, _configItem.getStringArray());
		BasicTestCase.assertEquals(new Class[] { String.class, Map.class, null, Integer.class },
			_configItem.getClassArray());
		BasicTestCase.assertEquals(new Enum[] { TestEnum.enum1, null, Month.JANUARY },
			_configItem.getGenericEnumArray());
		BasicTestCase.assertEquals(new TestEnum[] { TestEnum.enum1, TestEnum.enum3, TestEnum.enum2 },
			_configItem.getConcreteEnumArray());
		Calendar cal = CalendarUtil.createCalendar(TimeZones.UTC, Locale.GERMANY);
		cal.set(2019, Calendar.NOVEMBER, 8, 8, 51, 0);
		cal.set(Calendar.MILLISECOND, 3);
		Date date1 = cal.getTime();
		cal.set(1978, Calendar.JANUARY, 19, 2, 34, 10);
		cal.set(Calendar.MILLISECOND, 235);
		Date date2 = cal.getTime();
		BasicTestCase.assertEquals(new Date[] { date1, null, date2 }, _configItem.getDateArray());
	}

	public void testEmptyConfiguration() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		TypedConfiguration.fillValues(_configItem, values.entrySet());

		BasicTestCase.assertEquals(new boolean[] {}, _configItem.getBooleanArray());
		BasicTestCase.assertEquals(new Boolean[] {}, _configItem.getBooleanWrapperArray());
		BasicTestCase.assertEquals(new byte[] {}, _configItem.getByteArray());
		BasicTestCase.assertEquals(new Byte[] {}, _configItem.getByteWrapperArray());
		BasicTestCase.assertEquals(new short[] {}, _configItem.getShortArray());
		BasicTestCase.assertEquals(new Short[] {}, _configItem.getShortWrapperArray());
		BasicTestCase.assertEquals(new int[] {}, _configItem.getIntArray());
		BasicTestCase.assertEquals(new Integer[] {}, _configItem.getIntWrapperArray());
		BasicTestCase.assertEquals(new long[] {}, _configItem.getLongArray());
		BasicTestCase.assertEquals(new Long[] {}, _configItem.getLongWrapperArray());
		BasicTestCase.assertEquals(new double[] {}, _configItem.getDoubleArray());
		BasicTestCase.assertEquals(new Double[] {}, _configItem.getDoubleWrapperArray());
		BasicTestCase.assertEquals(new float[] {}, _configItem.getFloatArray());
		BasicTestCase.assertEquals(new Float[] {}, _configItem.getFloatWrapperArray());
		BasicTestCase.assertEquals(new String[] {}, _configItem.getStringArray());
		BasicTestCase.assertEquals(new Class[] {}, _configItem.getClassArray());
		BasicTestCase.assertEquals(new Enum[] {}, _configItem.getGenericEnumArray());
		BasicTestCase.assertEquals(new TestEnum[] {}, _configItem.getConcreteEnumArray());
		BasicTestCase.assertEquals(new Date[] {}, _configItem.getDateArray());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config",
			TypedConfiguration.getConfigurationDescriptor(TestArrayFormatConfig.class));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestArrayTypeFormat}.
	 */
	public static Test suite() {
		return suite(TestArrayTypeFormat.class);
	}

}
