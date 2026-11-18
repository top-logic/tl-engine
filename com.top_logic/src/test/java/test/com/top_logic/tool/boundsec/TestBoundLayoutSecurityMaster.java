/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.ModelSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * Test for the choice of the security master of a {@link BoundLayout}.
 */
public class TestBoundLayoutSecurityMaster extends BasicTestCase {

	/**
	 * A layout without security object of its own delegates to its single child.
	 */
	public void testSingleChildIsSecurityMaster() throws IOException, ConfigurationException {
		BoundLayout layout = (BoundLayout) createLayout("testSingleChildIsSecurityMaster.xml");

		assertProvider(ModelSecurityObjectProvider.class, layout);
	}

	/**
	 * A security object configured at a layout with a single child is not ignored.
	 */
	public void testOwnSecurityObjectWinsOverSingleChild() throws IOException, ConfigurationException {
		BoundLayout layout = (BoundLayout) createLayout("testOwnSecurityObjectWinsOverSingleChild.xml");

		assertProvider(ModelSecurityObjectProvider.class, layout);
	}

	/**
	 * A layout marked as security master provides the security object of its parent, although the
	 * parent has more than one child.
	 */
	public void testLayoutAsSecurityMaster() throws IOException, ConfigurationException {
		BoundLayout outer = (BoundLayout) createLayout("testLayoutAsSecurityMaster.xml");
		assertEquals(2, outer.getChildCount());
		BoundLayout master = (BoundLayout) outer.getChild(0);

		assertSame(master.getSecurityObjectProvider(), outer.getSecurityObjectProvider());
		assertProvider(ModelSecurityObjectProvider.class, outer);
	}

	/**
	 * A layout marked as security master checks access on its own security object (here the
	 * default security root), neither on an inner security master nor on its single child. The
	 * inner security master is not reported beyond the layout.
	 */
	public void testSecurityMasterLayoutUsesOwnSecurityObject() throws IOException, ConfigurationException {
		BoundLayout outer = (BoundLayout) createLayout("testSecurityMasterLayoutUsesOwnSecurityObject.xml");
		BoundLayout master = (BoundLayout) outer.getChild(0);

		assertProvider(SecurityRootObjectProvider.class, master);
		assertSame(master.getSecurityObjectProvider(), outer.getSecurityObjectProvider());
	}

	/**
	 * A security object configured at a dialog is used, although the dialog template always
	 * creates exactly one child (the content layout).
	 */
	public void testDialogSecurityObject() throws IOException, ConfigurationException {
		BoundLayout dialog = (BoundLayout) createLayout("testDialogSecurityObject.layout.xml");
		assertEquals(1, dialog.getChildCount());

		assertProvider(ModelSecurityObjectProvider.class, dialog);
	}

	/**
	 * A dialog without security object of its own delegates to its content layout.
	 */
	public void testDialogWithoutSecurityObject() throws IOException, ConfigurationException {
		BoundLayout dialog = (BoundLayout) createLayout("testDialogWithoutSecurityObject.layout.xml");
		assertEquals(1, dialog.getChildCount());

		assertSame(((BoundLayout) dialog.getChild(0)).getSecurityObjectProvider(),
			dialog.getSecurityObjectProvider());
		assertProvider(SecurityRootObjectProvider.class, dialog);
	}

	private static void assertProvider(Class<? extends SecurityObjectProvider> expectedType, BoundLayout layout) {
		SecurityObjectProvider provider = layout.getSecurityObjectProvider();
		assertTrue("Expected provider of type " + expectedType.getName() + " but got: " + provider,
			expectedType.isInstance(provider));
	}

	private static LayoutComponent createLayout(String fileNameSuffix) throws IOException, ConfigurationException {
		File layoutFile = ComponentTestUtils.getLayoutFile(TestBoundLayoutSecurityMaster.class, fileNameSuffix);
		CreateComponentParameter arguments = ComponentTestUtils.createComponentArgs(layoutFile);

		AssertProtocol protocol = new AssertProtocol();
		arguments.setLayoutResolver(
			CompileTimeApplication.createCompileTimeApplication(new File(ModuleLayoutConstants.PATH_TO_MODULE_ROOT),
				new File(ModuleLayoutConstants.WEBAPP_DIR)).createLayoutResolver(protocol, ThemeFactory.getTheme()));
		TLLayout config = LayoutStorage.createLayout(new DefaultInstantiationContext(protocol), arguments,
			Collections.emptyList());

		return ComponentTestUtils.createComponent(layoutFile, config.get());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestBoundLayoutSecurityMaster.class,
			CommandHandlerFactory.Module.INSTANCE,
			LayoutStorage.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
