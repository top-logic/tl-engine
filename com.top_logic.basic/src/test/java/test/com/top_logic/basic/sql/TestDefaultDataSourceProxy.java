/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import static com.top_logic.basic.sql.DefaultDataSourceProxy.*;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.sql.DataSourceProxy;
import com.top_logic.basic.sql.DefaultDataSourceProxy;
import com.top_logic.basic.sql.SQLH;

/**
 * Test case for {@link DefaultDataSourceProxy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDefaultDataSourceProxy extends TestCase {

	private static final String VALUE_1_PROPERTY = "value1";

	private static final String VALUE_2_PROPERTY = "value2";

	public static class AProxy extends DefaultDataSourceProxy {

		public final String _value1;

		public final String _value2;

		public AProxy(Properties config) throws SQLException, ConfigurationException {
			super(CProxy.class.getName(), config);

			_value1 = config.getProperty(VALUE_1_PROPERTY);
			_value2 = config.getProperty(VALUE_2_PROPERTY);
		}

		DataSource accessImpl() {
			return impl();
		}
	}

	public static class BProxy extends DefaultDataSourceProxy {

		public final String _value1;

		public final String _value2;

		public BProxy(Properties config) throws SQLException, ConfigurationException {
			super(CProxy.class.getName(), config);

			_value1 = config.getProperty(VALUE_1_PROPERTY);
			_value2 = config.getProperty(VALUE_2_PROPERTY);
		}

		DataSource accessImpl() {
			return impl();
		}
	}

	public static class CProxy extends DataSourceProxy {

		public final String _value1;

		public final String _value2;

		public CProxy(Properties config) throws SQLException, ConfigurationException {
			_value1 = config.getProperty(VALUE_1_PROPERTY);
			_value2 = config.getProperty(VALUE_2_PROPERTY);
		}

		@Override
		protected DataSource impl() {
			throw new UnsupportedOperationException();
		}

	}

	public void testDefaultClass() throws SQLException {
		Properties config = new Properties();
		config.setProperty(SQLH.DATA_SOURCE_PROPERTY, AProxy.class.getName());
		config.setProperty(VALUE_1_PROPERTY, "localvalue");
		config.setProperty(VALUE_2_PROPERTY, "globalvalue");

		AProxy a = (AProxy) SQLH.createDataSource(config);
		assertEquals("localvalue", a._value1);
		assertEquals("globalvalue", a._value2);
		assertEquals(CProxy.class, a.accessImpl().getClass());
	}

	public void testNestingProxies() throws SQLException {
		Properties config = new Properties();
		config.setProperty(SQLH.DATA_SOURCE_PROPERTY, AProxy.class.getName());
		config.setProperty(VALUE_1_PROPERTY, "localvalue1");
		config.setProperty(VALUE_2_PROPERTY, "globalvalue");

		config.setProperty(IMPL_CLASS_PROPERTY, BProxy.class.getName());
		config.setProperty(INNER_CONFIGURATION_PREFIX + VALUE_1_PROPERTY, "localvalue2");

		config.setProperty(INNER_CONFIGURATION_PREFIX + IMPL_CLASS_PROPERTY, AProxy.class.getName());
		config.setProperty(INNER_CONFIGURATION_PREFIX + INNER_CONFIGURATION_PREFIX + VALUE_1_PROPERTY, "localvalue3");
		
		AProxy a = (AProxy) SQLH.createDataSource(config);
		assertEquals("localvalue1", a._value1);
		assertEquals("globalvalue", a._value2);
		assertEquals(BProxy.class, a.accessImpl().getClass());

		BProxy b = (BProxy) a.accessImpl();
		assertEquals("localvalue2", b._value1);
		assertEquals("globalvalue", b._value2);
		assertEquals(AProxy.class, b.accessImpl().getClass());

		AProxy a2 = (AProxy) b.accessImpl();
		assertEquals("localvalue3", a2._value1);
		assertEquals("globalvalue", a2._value2);
		assertEquals(CProxy.class, a2.accessImpl().getClass());
	}

}
