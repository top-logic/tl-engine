/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.TLMainLayout;
import com.top_logic.layout.AbstractCompositeContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.LayoutLinker;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.RuntimeApplication;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutList;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.PageComponent;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.util.TLContextManager;

/**
 * Utilities for testing {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentTestUtils {

	static class DummyHandler extends AbstractCompositeContentHandler {
	
		@Override
		public URLBuilder getURL(DisplayContext context) {
			return URLPathBuilder.newLayoutServletURLBuilder(context);
		}
	
		@Override
		public void internalHandleContent(DisplayContext context, URLParser url) throws IOException {
			throw new UnsupportedOperationException();
		}
	
	}

	/**
	 * Creates a test component by loading its definition relative to the given test class.
	 * 
	 * @param testClass
	 *        The context class.
	 * @param fileNameSuffix
	 *        The name to append to the name of the test class to build the layout resource to load.
	 * 
	 * @return The instantiated test component.
	 * 
	 * @see ComponentTestUtils#createMainLayout(File)
	 */
	public static LayoutComponent createComponent(Class<? extends Test> testClass, String fileNameSuffix)
			throws IOException, ConfigurationException {
		return createComponent(getLayoutFile(testClass, fileNameSuffix));
	}

	/**
	 * Creates a {@link MainLayout} by loading its definition relative to the given test class.
	 * 
	 * @param layoutFile
	 *        Layout file relative to the given test class.
	 * 
	 * @see ComponentTestUtils#createComponent(Class, String)
	 */
	public static LayoutComponent createMainLayout(File layoutFile) throws IOException, ConfigurationException {
		return createMainLayout(getTestLayoutScope(layoutFile));
	}

	/**
	 * Fetches the layout file relative to the given test class. It is expected that the required
	 * file has name "&lt;test-class-name>_&lt;fileNameSuffix>"
	 */
	public static File getLayoutFile(Class<? extends Test> testClass, String fileNameSuffix) {
		return FileManager.getInstance().getIDEFileOrNull(createPath(testClass, fileNameSuffix));
	}

	/**
	 * Creates a path to the file with the given filename suffix next to the given test class.
	 */
	public static String createPath(Class<? extends Test> testClass, String suffix) {
		return CustomPropertiesDecorator.createFileName(testClass, testClass.getSimpleName() + "_" + suffix);
	}

	/**
	 * Creates a test component by loading its definition from the given file.
	 * 
	 * @param f
	 *        The file holding the component to instantiate.
	 * 
	 * @return The instantiated test component.
	 */
	public static LayoutComponent createComponent(File f) throws IOException, ConfigurationException {
		return createComponent(getTestLayoutScope(f));
	}

	/**
	 * Creates a test component by loading its definition from the given file.
	 * 
	 * @param f
	 *        The file holding the component to instantiate.
	 * @param config
	 *        layout component configuration to instantiate.
	 * 
	 * @return The instantiated test component.
	 */
	public static LayoutComponent createComponent(File f, LayoutComponent.Config config) throws IOException, ConfigurationException {
		return createComponent(getTestLayoutScope(f), config);
	}

	/**
	 * Creates a {@link CreateComponentParameter} from the given file.
	 */
	public static CreateComponentParameter createComponentArgs(File f) throws IOException {
		return CreateComponentParameter.newParameter(getTestLayoutScope(f));
	}

	/**
	 * Layout component scope in tests.
	 */
	public static String getTestLayoutScope(File file) throws IOException {
		/* Result is resolved by LayoutResolver using the FileManager (without test prefix). To
		 * ensure that the file is not resolving relative to the web application, mark the file as
		 * "direct". */
		return LayoutResolver.TEST_SCHEME_PREFIX + FileManager.markDirect(file.getCanonicalPath());
	}

	/**
	 * Creates a {@link LayoutComponent} read from the given
	 * {@link CreateComponentParameter#getLayoutName() layout}.
	 * 
	 * <p>
	 * It is expected that the defined component is not a {@link MainLayout}.
	 * </p>
	 *
	 * @see ComponentTestUtils#createMainLayout(String)
	 */
	public static LayoutComponent createComponent(String layoutKey) throws IOException, ConfigurationException {
		LayoutComponent.Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(layoutKey);

		return createComponent(layoutKey, config);
	}

	private static LayoutComponent createComponent(String layoutKey, LayoutComponent.Config config) throws IOException, ConfigurationException {
		InstantiationContext context = new DefaultInstantiationContext(new AssertProtocol());
		LayoutComponent result = LayoutUtils.createComponentFromXML(context, newMainLayout(), layoutKey, true, config);
		Assert.assertFalse(
			"Component read from " + layoutKey + " is a MainLayout. Call #createMainLayout(...) instead.",
			result instanceof MainLayout);
		result.acceptVisitorRecursively(new LayoutLinker(new AssertProtocol()));
		return result;
	}

	/**
	 * Creates a {@link MainLayout} read from the given
	 * {@link CreateComponentParameter#getLayoutName() layout}.
	 * 
	 * <p>
	 * It is expected that the defined component is a {@link MainLayout}.
	 * </p>
	 *
	 * @see ComponentTestUtils#createComponent(String)
	 */
	public static MainLayout createMainLayout(String layoutKey) throws IOException, ConfigurationException {
		InstantiationContext context = new DefaultInstantiationContext(new AssertProtocol());
		LayoutComponent.Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(layoutKey);
		LayoutComponent result = LayoutUtils.createComponentFromXML(context, null, layoutKey, false, config);
		Assert.assertTrue("Component read from " + layoutKey
			+ " is not a MainLayout. Call #createComponent(...) instead.",
			result instanceof MainLayout);
		MainLayout newML = (MainLayout) result;
		newML.setLocation(layoutKey);
		initMainLayout(context, newML);
		return newML;
	}

	private static void ensureResolver(CreateComponentParameter createArg) {
		if (createArg.getLayoutResolver() == null) {
			Theme theme = ThemeFactory.getTheme();
			AssertProtocol protocol = new AssertProtocol();
			LayoutResolver resolver = RuntimeApplication.INSTANCE.createLayoutResolver(protocol, theme);
			createArg.setLayoutResolver(resolver);
		}
	}

	public static SimpleComponent newSimpleComponent(String name, String header, String content) {
		return newSimpleComponent(ComponentName.newName(name), header, content);
	}

	public static SimpleComponent newSimpleComponent(ComponentName name, String header, String content)
			throws AssertionFailedError {
		try {
			SimpleComponent.Config config = newSimpleComponentConfig(header, content);
			config.setName(name);
			return new SimpleComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		} catch (ConfigurationException ex) {
			throw BasicTestCase.fail("Cannot instantiate component.", ex);
		}
	}

	public static SimpleComponent newSimpleComponent(String header, String content) {
		return newSimpleComponent("", header, content);
	}

	public static SimpleComponent.Config newSimpleComponentConfig(String header, String content) {
		SimpleComponent.Config config = TypedConfiguration.newConfigItem(SimpleComponent.Config.class);
		config.setHeader(header);
		config.setContent(content);
		return config;
	}

	public static PageComponent newPageComponent(String page) {
		try {
			PageComponent.Config config = TypedConfiguration.newConfigItem(PageComponent.Config.class);
			config.setPage(page);
			return new PageComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		} catch (ConfigurationException ex) {
			throw BasicTestCase.fail("Cannot instantiate component.", ex);
		}
	}

	public static Layout newLayout(boolean horizontal) {
		Layout.Config config = newLayoutConfig(horizontal);
		return (Layout) TypedConfigUtil.createInstance(config);
	}

	public static Layout.Config newLayoutConfig(boolean horizontal) {
		Layout.Config config = TypedConfiguration.newConfigItem(Layout.Config.class);
		config.setHorizontal(horizontal);
		config.setImplementationClass(Layout.class);
		return config;
	}

	public static MainLayout newMainLayout() {
		return newMainLayout("id", "title", false);
	}

	public static MainLayout newMainLayout(String id, String title, boolean horizontal) {
		TLMainLayout.Config config = TypedConfiguration.newConfigItem(TLMainLayout.Config.class);
		config.setName(ComponentName.newName(id));
		config.setImplementationClass(TLMainLayout.class);
		config.setTitleKey(ResKey.forTest(title));
		config.setHorizontal(horizontal);
		try {
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			MainLayout result = (MainLayout) context.getInstance(config);
			initMainLayout(context, result);

			return result;
		} catch (RuntimeException ex) {
			throw BasicTestCase.fail("Cannot instantiate component.", ex);
		}
	}

	private static void initMainLayout(InstantiationContext instantiationContext, MainLayout mainLayout) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		TLSubSessionContext subSession = ensureSubSession(displayContext);
		SubsessionHandler subSessionHandler = (SubsessionHandler) ensureLayoutContext(subSession);
		MainLayout.initializeMainLayout(instantiationContext, displayContext, mainLayout, subSessionHandler, subSession);
	}

	public static void setChildren(LayoutList comp, List<LayoutComponent> newChildren) {
		ReflectionUtils.executeMethod(comp, "setChildren", new Class[] { List.class }, new Object[] { newChildren });
	}

	/**
	 * Creates a new {@link ComponentName} for tests.
	 */
	public static ComponentName newComponentName(File layout, String name) throws IOException {
		return ComponentName.newName(getTestLayoutScope(layout), name);
	}

	/**
	 * Creates a dummy {@link SubsessionHandler}.
	 */
	public static SubsessionHandler newDummySubsessionHandler() {
		return new SubsessionHandler(new ComponentTestUtils.DummyHandler(), null, null, null);
	}

	/**
	 * Ensures that the given {@link TLInteractionContext} has a
	 * {@link TLInteractionContext#getSubSessionContext() sub session}. If it already has a
	 * {@link TLSubSessionContext}, nothing is done.
	 */
	public static TLSubSessionContext ensureSubSession(TLInteractionContext context) {
		if (context.getSubSessionContext() != null) {
			return context.getSubSessionContext();
		}
		TLSubSessionContext subSession = newSubSession();
		context.installSubSessionContext(subSession);
		TLContextManager.getManager().registerSubSessionInSessionForInteraction(context);
		return subSession;
	}

	/**
	 * Creates a new {@link TLSubSessionContext}.
	 */
	public static TLSubSessionContext newSubSession() {
		ThreadContextManager factory = TLContextManager.getManager();
		TLSubSessionContext subSession = (TLSubSessionContext) factory.newSubSessionContext();
		TLSessionContext session = (TLSessionContext) factory.newSessionContext();
		subSession.setSessionContext(session);
		return subSession;
	}

	/**
	 * Ensures that the given {@link TLSubSessionContext} contains a
	 * {@link TLSubSessionContext#getLayoutContext() layout context}. If it already contains a
	 * {@link LayoutContext}, nothing is done.
	 */
	public static LayoutContext ensureLayoutContext(TLSubSessionContext subSession) {
		if (subSession.getLayoutContext() != null) {
			return subSession.getLayoutContext();
		}
		SubsessionHandler dummyLayoutContext = newDummySubsessionHandler();
		TLContextManager.initLayoutContext(subSession, dummyLayoutContext);
		return dummyLayoutContext;
	}

}
