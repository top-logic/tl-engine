/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.Application;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Test case ensuring that {@link LayoutComponent#handleNewModel(Object)} is called, even if the
 * initial model of the component is <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestInitialNullModel extends AbstractLayoutTest {

	private ApplicationSession _session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Application application = ApplicationTestSetup.getApplication();
		Person root = PersonManager.getManager().getRoot();
		_session = application.login(root, layoutName());
	}

	static String layoutName() {
		return TestInitialNullModel.class.getName() + ".xml";
	}

	@Override
	protected void tearDown() throws Exception {
		_session.invalidate();
		_session = null;
		super.tearDown();
	}

	public void testInitialNullModel() {
		_session.process(TypedConfiguration.newConfigItem(TestAction.class));
	}

	public interface TestAction extends ApplicationAction {

		@Override
		@ClassDefault(Op.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		class Op<C extends TestAction> extends AbstractApplicationActionOp<C> {

			/**
			 * Creates a {@link TestInitialNullModel.TestAction} from configuration.
			 * 
			 * @param context
			 *        The context for instantiating sub configurations.
			 * @param config
			 *        The configuration.
			 */
			@CalledByReflection
			public Op(InstantiationContext context, C config) {
				super(context, config);
			}

			@Override
			protected Object processInternal(ActionContext context, Object argument) throws Throwable {
				LayoutComponent component = context.getMainLayout()
					.getComponentByName(ComponentName.newName(layoutName(), "testInitialNullModel"));
				assertTrue(((TestingComponent) component).isOk());
				return argument;
			}

		}
	}

	public static class TestingComponent extends LayoutComponent {

		private boolean _ok;

		/**
		 * Creates a {@link TestingComponent} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestingComponent(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		@Override
		protected void handleNewModel(Object newModel) {
			super.handleNewModel(newModel);

			_ok = true;
		}

		public boolean isOk() {
			return _ok;
		}

	}

	public static Test suite() {
		return ApplicationTestSetup.setupApplication(TestInitialNullModel.class);
//		return ApplicationTestSetup
//			.setupApplication(ServiceTestSetup.createSetup(null, TestInitialNullModel.class,
//				PersonManager.Module.INSTANCE, RequestLockFactory.Module.INSTANCE, SessionService.Module.INSTANCE,
//				Login.Module.INSTANCE, LayoutStorage.Module.INSTANCE, JSFileCompiler.Module.INSTANCE));
	}

}
