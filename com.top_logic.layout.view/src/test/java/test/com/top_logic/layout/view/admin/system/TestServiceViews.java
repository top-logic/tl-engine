/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the views of the services administration are instantiated: their elements are
 * created and every script in them compiles against the variables it sees and the functions it
 * calls.
 */
public class TestServiceViews extends BasicTestCase {

	/** The drill-down holding the two frames. */
	private static final String SERVICES = "admin/system/services.view.xml";

	/** The initial frame, the table of the services. */
	private static final String OVERVIEW = "admin/system/services/overview.view.xml";

	/** The frame of a single service. */
	private static final String DETAIL = "admin/system/services/service-detail.view.xml";

	/** The drill-down view is instantiated. */
	public void testServices() throws Exception {
		assertInstantiated(SERVICES);
	}

	/** The overview frame is instantiated, including the scripts of its table columns. */
	public void testOverview() throws Exception {
		assertInstantiated(OVERVIEW);
	}

	/** The detail frame is instantiated, including the scripts of its channels and commands. */
	public void testDetail() throws Exception {
		assertInstantiated(DETAIL);
	}

	private static void assertInstantiated(String viewRef) throws Exception {
		ViewElement view = ViewLoader.loadView(ViewLoader.fullPath(viewRef));
		assertNotNull(viewRef, view);
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestServiceViews.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
