/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Test test {@link LayoutReference} in XML files.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestLayoutReference extends BasicTestCase {

	/**
	 * Test for
	 * {@link MainLayout#replaceComponent(String, com.top_logic.mig.html.layout.LayoutComponent.Config)}
	 * when a component is replaced that references a component that is not replaced.
	 */
	public void testReplaceRefererComponent() throws ConfigurationException, IOException {
		MainLayout mainLayout = ComponentTestUtils.createMainLayout(fileName("testReplaceComp_main.layout.xml"));
		ComponentName refererName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp_referer.layout.xml", "comp");
		ReferencingComp referer = (ReferencingComp) mainLayout.getComponentByName(refererName);
		assertNotNull(referer);
		assertEquals("origContent", referer.getConfig().getContent());
		LayoutComponent reference = mainLayout.getComponentByName(
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp_reference.layout.xml",
				"comp"));
		assertNotNull(reference);
		assertEquals(reference, referer.getOther());

		ReferencingComp.Config newConfig = TypedConfiguration.copy(referer.getConfig());
		newConfig.setContent("newContent");

		mainLayout.replaceComponent("test.com.top_logic/TestLayoutReference.testReplaceComp_referer.layout.xml",
			newConfig);

		LayoutComponent reInstalledReferrer = mainLayout.getComponentByName(refererName);
		assertNotNull(reInstalledReferrer);
		assertNotEquals(referer, reInstalledReferrer);
		assertInstanceof(reInstalledReferrer, ReferencingComp.class);
		assertEquals("newContent", ((ReferencingComp) reInstalledReferrer).getConfig().getContent());
		assertEquals(reference, ((ReferencingComp) reInstalledReferrer).getOther());
	}

	/**
	 * Test for
	 * {@link MainLayout#replaceComponent(String, com.top_logic.mig.html.layout.LayoutComponent.Config)}
	 * when a component is replaced that is referred by a component that is not replaced.
	 */
	public void testReplaceReferenceComponent() throws ConfigurationException, IOException {
		MainLayout mainLayout = ComponentTestUtils.createMainLayout(fileName("testReplaceComp_main.layout.xml"));
		ComponentName refererName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp_referer.layout.xml", "comp");
		ReferencingComp referer = (ReferencingComp) mainLayout.getComponentByName(refererName);
		assertNotNull(referer);
		assertEquals("origContent", referer.getConfig().getContent());
		ComponentName referenceName = ComponentName
			.newName("test.com.top_logic/TestLayoutReference.testReplaceComp_reference.layout.xml", "comp");
		SimpleComponent reference = (SimpleComponent) mainLayout.getComponentByName(referenceName);
		assertNotNull(reference);
		assertEquals(reference, referer.getOther());

		SimpleComponent.Config newConfig = (SimpleComponent.Config) TypedConfiguration.copy(reference.getConfig());
		newConfig.setContent("newContent");

		mainLayout.replaceComponent("test.com.top_logic/TestLayoutReference.testReplaceComp_reference.layout.xml",
			newConfig);

		LayoutComponent reInstalledReference = mainLayout.getComponentByName(referenceName);
		assertNotNull(reInstalledReference);
		assertNotEquals(reference, reInstalledReference);
		assertInstanceof(reInstalledReference, SimpleComponent.class);
		assertEquals("newContent", ((SimpleComponent.Config) reInstalledReference.getConfig()).getContent());

		/* Fetch the reference again. It is (or may be) regenerated. */
		ReferencingComp refetchedReferer = (ReferencingComp) mainLayout.getComponentByName(refererName);
		assertNotNull(refetchedReferer);
		assertEquals(reInstalledReference, refetchedReferer.getOther());
	}

	public void testReplaceReferenceComponent2() throws ConfigurationException, IOException {
		MainLayout mainLayout = ComponentTestUtils.createMainLayout(fileName("testReplaceComp2_main.layout.xml"));

		ComponentName replacedComponentName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp2_a.layout.xml",
				"replaced");
		Layout.Config newLayoutConf =
			(Layout.Config) TypedConfiguration.copy(mainLayout.getComponentByName(replacedComponentName).getConfig());

		ComponentName referencedName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp2_a.layout.xml",
				"referenced");
		ComponentName refererName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp2_c.layout.xml",
				"referer");
		ComponentName unrelatedName =
			ComponentName.newName("test.com.top_logic/TestLayoutReference.testReplaceComp2_b.layout.xml",
				"unrelated");

		LayoutComponent oldReference = mainLayout.getComponentByName(referencedName);
		LayoutComponent oldReferer = mainLayout.getComponentByName(refererName);
		LayoutComponent oldUnrelated = mainLayout.getComponentByName(unrelatedName);

		mainLayout.replaceComponent("test.com.top_logic/TestLayoutReference.testReplaceComp2_a.layout.xml",
			newLayoutConf);

		LayoutComponent newReference = mainLayout.getComponentByName(referencedName);
		assertNotEquals("Component is contained in replaced layout.", oldReference, newReference);
		NamedConstant newModel = new NamedConstant("new model");
		newReference.setModel(newModel);

		LayoutComponent newUnrelated = mainLayout.getComponentByName(unrelatedName);
		assertEquals("Unrelated component is not replaced, only a child is replaced whihc occurs inline.", oldUnrelated,
			newUnrelated);

		LayoutComponent newReferer = mainLayout.getComponentByName(refererName);
		assertSame(mainLayout, newReferer.getMainLayout());
		assertNotEquals("Referer was replaced, as referenced component is replaced.", oldReferer, newReferer);
		assertEquals("Components have the same model channel.", newModel, newReferer.getModel());
	}

	private static String fileName(String layoutNamesuffix) {
		return fileName(TestLayoutReference.class, layoutNamesuffix);
	}

	/**
	 * Creates the name of a file in the layouts folder folder relative to given base class.
	 *
	 * @implSpec Layout file to resolve is: "test.com.top_logic/" + baseClass.getSimpleName() + "."
	 *           + layoutNamesuffix
	 */
	public static String fileName(Class<?> baseClass, String layoutNamesuffix) {
		return "test.com.top_logic/" + baseClass.getSimpleName() + "." + layoutNamesuffix;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestLayoutReference}.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestLayoutReference.class,
			CommandHandlerFactory.Module.INSTANCE,
			SecurityComponentCache.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE,
			LayoutStorage.Module.INSTANCE);
		return KBSetup.getSingleKBTest(test);
	}

	/**
	 * {@link SimpleComponent} refering another component.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@SuppressWarnings("javadoc")
	public static class ReferencingComp extends SimpleComponent {

		public interface Config extends SimpleComponent.Config {

			@Mandatory
			ComponentName getOther();
		}

		private LayoutComponent _other;

		public ReferencingComp(InstantiationContext ctx, Config config) throws ConfigurationException {
			super(ctx, config);
			ctx.resolveReference(config.getOther(), LayoutComponent.class, this::setOther);
		}

		@Override
		public Config getConfig() {
			return (Config) super.getConfig();
		}

		private void setOther(LayoutComponent other) {
			_other = other;
		}

		LayoutComponent getOther() {
			return _other;
		}

	}

}
