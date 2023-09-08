/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.renderer;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.renderer.TableRendererUtil;

/**
 * Tests the {@link TableRendererUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTableRendererUtil extends BasicTestCase {

	public void testNoFixedColumnsConfigured() {
		TableRenderer.Config<?> tableRendererConf = newTestingTableRendererConfig();

		ITableRenderer renderer =
			TableRendererUtil.getInstance(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, tableRendererConf);
		assertSame(TestingTableRenderer.INSTANCE, renderer);
	}

	@SuppressWarnings("unchecked")
	private TableRenderer.Config<?> newTestingTableRendererConfig() {
		@SuppressWarnings("rawtypes")
		TableRenderer.Config tableRendererConf = TypedConfiguration.newConfigItem(TestingTableRenderer.Config.class);
		tableRendererConf.setImplementationClass(TestingTableRenderer.class);
		return tableRendererConf;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTableRendererUtil}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestTableRendererUtil.class);
	}

}
