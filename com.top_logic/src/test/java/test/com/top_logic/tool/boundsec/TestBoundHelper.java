/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceStarter;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * The class {@link TestBoundHelper} tests {@link BoundHelper}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBoundHelper extends BasicTestCase {

	private final ServiceStarter _startBoundHelper = new ServiceStarter(BoundHelper.Module.INSTANCE);

	/**
	 * Bound object used in layout file for this test.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Child1BoundObject extends SimpleBoundObject {
		// subclass to have different types in tests
	}

	/**
	 * Bound object used in layout file for this test.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Child2BoundObject extends SimpleBoundObject {
		// subclass to have different types in tests
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_startBoundHelper.startService();
	}

	@Override
	protected void tearDown() throws Exception {
		_startBoundHelper.stopService();
		super.tearDown();
	}

	/**
	 * Tests that fetching {@link BoundChecker} for concrete component does not corrupt result for
	 * {@link MainLayout}.
	 * 
	 * @see #testBoundHandlerCaches2() The other way around
	 */
	public void testBoundHandlerCaches() throws IOException, ConfigurationException {
		File mainLayoutFile = getLayoutFile("mainLayout.xml");
		LayoutComponent mainLayout = getLayoutComponent(mainLayoutFile);
		LayoutComponent container2 =
			mainLayout.getComponentByName(ComponentTestUtils.newComponentName(mainLayoutFile, "child_2"));
		LayoutComponent defaultForChild1Object =
			mainLayout.getComponentByName(ComponentTestUtils.newComponentName(mainLayoutFile, "child_11"));
		SimpleBoundObject anObject = new Child1BoundObject();

		Collection<BoundChecker> boundHandlers =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) container2, null);
		assertTrue(
			"Container2 does not contain a default component for object of type '" + Child1BoundObject.class.getName()
				+ "'.", boundHandlers.isEmpty());
		Collection<BoundChecker> refetchedHandlers =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) container2, null);
		assertEquals("Repeated refetch must not return different values.", boundHandlers, refetchedHandlers);

		Collection<BoundChecker> boundHandlers2 =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout, null);
		assertTrue("Ticket #12930: MainLayout contains a default component for object of type '"
			+ Child1BoundObject.class.getName()
			+ "'.", boundHandlers2.contains(defaultForChild1Object));
		Collection<BoundChecker> refetchedHandlers2 =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout, null);
		assertEquals("Repeated refetch must not return different values.", boundHandlers2, refetchedHandlers2);
	}

	/**
	 * Tests that fetching {@link BoundChecker} for {@link MainLayout} does not corrupt result for
	 * concrete component.
	 * 
	 * @see #testBoundHandlerCaches() The other way around
	 */
	public void testBoundHandlerCaches2() throws IOException, ConfigurationException {
		File mainLayoutFile = getLayoutFile("mainLayout.xml");
		LayoutComponent mainLayout = getLayoutComponent(mainLayoutFile);
		LayoutComponent container2 =
			mainLayout.getComponentByName(ComponentTestUtils.newComponentName(mainLayoutFile, "child_2"));
		LayoutComponent defaultForChild1Object =
			mainLayout.getComponentByName(ComponentTestUtils.newComponentName(mainLayoutFile, "child_11"));
		SimpleBoundObject anObject = new Child1BoundObject();

		Collection<BoundChecker> boundHandlers2 =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout, null);
		assertTrue("MainLayout contains a default component for object of type '" + Child1BoundObject.class.getName()
			+ "'.", boundHandlers2.contains(defaultForChild1Object));
		Collection<BoundChecker> refetchedHandlers2 =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout, null);
		assertEquals("Repeated refetch must not return different values.", boundHandlers2, refetchedHandlers2);

		Collection<BoundChecker> boundHandlers =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) container2, null);
		assertTrue("Ticket #12930: Container2 does not contain a default component for object of type '"
			+ Child1BoundObject.class.getName() + "'.", boundHandlers.isEmpty());
		Collection<BoundChecker> refetchedHandlers =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) container2, null);
		assertEquals("Repeated refetch must not return different values.", boundHandlers, refetchedHandlers);
	}

	/**
	 * Tests that BoundHelper can be used with different {@link MainLayout}.
	 */
	public void testMultipleMainLayouts() throws IOException, ConfigurationException {
		File mainLayout1File = getLayoutFile("mainLayout.xml");
		LayoutComponent mainLayout1 = getLayoutComponent(mainLayout1File);
		LayoutComponent defaultForChild1Object =
			mainLayout1.getComponentByName(ComponentTestUtils.newComponentName(mainLayout1File, "child_11"));
		SimpleBoundObject anObject = new Child1BoundObject();

		Collection<BoundChecker> boundHandlers =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout1, null);
		assertTrue(
			"First MainLayout contains a default component for object of type '" + Child1BoundObject.class.getName()
				+ "'.", boundHandlers.contains(defaultForChild1Object));

		File mainLayout2File = getLayoutFile("mainLayout2.xml");
		LayoutComponent mainLayout2 = getLayoutComponent(mainLayout2File);
		assertNotEquals(mainLayout1, mainLayout2);
		Collection<BoundChecker> boundHandlers2 =
			BoundHelper.getInstance().getBoundCheckers(anObject, (BoundChecker) mainLayout2, null);
		assertFalse(
			"Ticket #13281: Second MainLayout contains default for object of type '"
				+ Child1BoundObject.class.getName() + "'.", boundHandlers2.isEmpty());
		assertFalse("Ticket #13281: No result from foreign layout tree.",
			boundHandlers2.contains(defaultForChild1Object));

	}

	private static LayoutComponent getLayoutComponent(File layoutFile) throws IOException, ConfigurationException {
		return ComponentTestUtils.createMainLayout(layoutFile);
	}

	private static File getLayoutFile(String fileNameSuffix) throws IOException {
		return ComponentTestUtils.getLayoutFile(TestBoundHelper.class, fileNameSuffix);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestBoundHelper}.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestBoundHelper.class,
			SecurityObjectProviderManager.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE,
			SecurityComponentCache.Module.INSTANCE,
			LayoutStorage.Module.INSTANCE);
		return KBSetup.getSingleKBTest(test);
	}

}
