/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.ValidationQueue;

/**
 * Base class for {@link TestCase} implementations requiring a {@link DisplayContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractLayoutTest extends BasicTestCase {

	private AbstractDisplayContext _displayContext;

	/**
	 * Creates a {@link AbstractLayoutTest}.
	 */
	public AbstractLayoutTest() {
		super();
	}

	/**
	 * Creates a {@link AbstractLayoutTest} with explicit name.
	 */
	public AbstractLayoutTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		MainLayout ml = ComponentTestUtils.newMainLayout();
		ContextComponent component = new ContextComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			TypedConfiguration.newConfigItem(LayoutComponent.Config.class));
		ml.addComponent(component);
		
		TLSubSessionContext subSession = ComponentTestUtils.newSubSession();
		
		ContentHandlersRegistry urlContext = new ContentHandlersRegistry();
		WindowId windowId = new WindowId("test");
		SubsessionHandler layoutContext = new SubsessionHandler(urlContext, windowId, null, null) {
			@Override
			protected void errorStateModification() {
				Assert.fail("State modification during rendering.");
			}
		};
		TLContextManager.initLayoutContext(subSession, layoutContext);

		// Simulate initial rendering to set up URL contexts.
		ml.getEnclosingFrameScope().setUrlContext(layoutContext);
		AbstractDisplayContext initialDisplayContext = createDisplayContext(subSession, ml);
		LayoutControl windowControl = ml.getLayoutFactory().createLayout(ml);
		windowControl.write(initialDisplayContext, new TagWriter());
		DefaultDisplayContext.teardownDisplayContext(null);
		
		_displayContext  = createDisplayContext(subSession, component);

		enableUpdates();
	}

	private AbstractDisplayContext createDisplayContext(TLSubSessionContext subSession, LayoutComponent component) {
		ControlSupport controlSupport = new ControlSupport(component);
		AbstractDisplayContext displayContext = DummyDisplayContext.forScope(controlSupport);
		DefaultDisplayContext.setupDisplayContext(null, displayContext);

		displayContext.installSubSessionContext(subSession);
		TLContextManager.getManager().registerSubSessionInSessionForInteraction(displayContext);
		return displayContext;
	}

	/**
	 * Enables model updates in the session.
	 *
	 * @return The state before, should be passed to {@link #resetUpdatesEnabled(boolean)} later on
	 *         (ideally in a finally clause).
	 */
	protected boolean enableUpdates() {
		return setUpdatesEnabled(true);
	}

	/**
	 * Sets the "updates-enabled" state of the session.
	 *
	 * @param enabled
	 *        Whether updates should be enabled.
	 * @return The session state before. Must be passed to {@link #resetUpdatesEnabled(boolean)}
	 *         later on (ideally in a finally clause).
	 */
	protected boolean setUpdatesEnabled(boolean enabled) {
		SubsessionHandler layoutContext = (SubsessionHandler) TLContext.getContext().getLayoutContext();
		return layoutContext.enableUpdate(enabled);
	}

	/**
	 * Resets the "updates-enabled" state of the session.
	 * 
	 * @param before
	 *        The value returned from {@link #setUpdatesEnabled(boolean)} called before.
	 * 
	 * @see #setUpdatesEnabled(boolean)
	 */
	protected void resetUpdatesEnabled(boolean before) {
		SubsessionHandler layoutContext = (SubsessionHandler) TLContext.getContext().getLayoutContext();
		layoutContext.processActions();
		layoutContext.enableUpdate(before);
	}

	@Override
	protected void tearDown() throws Exception {
		resetUpdatesEnabled(false);

		DefaultDisplayContext.teardownDisplayContext(null);
		_displayContext = null;
		super.tearDown();
	}

	/**
	 * @see ValidationQueue#runValidation(DisplayContext)
	 */
	protected void runValidation() {
		DisplayContext displayContext = displayContext();
		displayContext.getLayoutContext().runValidation(displayContext);
	}

	/**
	 * The {@link DisplayContext} for test.
	 */
	protected final DisplayContext displayContext() {
		return _displayContext;
	}

	/**
	 * @deprecated Should be replaced with {@link FrameScope} implementation.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	@Deprecated
	protected static final class ContextComponent extends LayoutComponent {

		public ContextComponent(InstantiationContext context, Config atts) throws ConfigurationException {
			super(context, atts);
		}

		@Override
		protected boolean supportsInternalModel(Object object) {
			return false;
		}

	}

	public static Test suite(Class<? extends Test> testClass) {
		return suite(new TestSuite(testClass));
	}

	public static Test suite(Test test) {
		Test setupModules = ServiceTestSetup.createSetup(test,
			RequestLockFactory.Module.INSTANCE,
			MimeTypes.Module.INSTANCE,
			SecurityObjectProviderManager.Module.INSTANCE,
			CommandHandlerFactory.Module.INSTANCE,
			TableConfigurationFactory.Module.INSTANCE,
			SecurityComponentCache.Module.INSTANCE,
			SafeHTML.Module.INSTANCE);
		return KBSetup.getSingleKBTest(setupModules);
	}

}
