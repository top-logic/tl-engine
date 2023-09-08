/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import static test.com.top_logic.ComponentTestUtils.*;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CheckingProtocol;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.channel.BidirectionalTransformLinking;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.Provider;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.processor.Application;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleCommandRegistry;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Test case for {@link LayoutComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLayoutComponent extends BasicTestCase {

	public static final Property<String> TEST_VALUE = TypedAnnotatable.property(String.class, "testValue");

	private static final Object initialModel = new NamedConstant("initialModel");

	public static class TestModelProvider implements ModelProvider {
		@Override
		public Object getBusinessModel(LayoutComponent businessComponent) {
			return initialModel;
		}
	}

	public void testInvalidModel() throws ConfigurationException {
		AtomicReference<Object> receivedNewModel = new AtomicReference<>();

		MainLayout ml = newMainLayout();
		SimpleComponent.Config config = TypedConfiguration.newConfigItem(SimpleComponent.Config.class);
		Provider provider = TypedConfiguration.newConfigItem(Provider.class);
		PolymorphicConfiguration<ModelProvider> providerImpl =
			TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		providerImpl.setImplementationClass(TestModelProvider.class);
		provider.setImpl(providerImpl);
		config.setModelSpec(provider);
		LayoutComponent sc = new SimpleComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		sc.modelChannel().addListener(new ChannelListener() {
			@Override
			public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
				receivedNewModel.set(newValue);
			}
		});
		ml.addComponent(sc);
		ml.setupRelations(new AssertProtocol());

		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		TLSubSessionContext subSession = ComponentTestUtils.ensureSubSession(context);
		subSession.setPerson(PersonManager.getManager().getRoot());
		SubsessionHandler layoutContext = (SubsessionHandler) ComponentTestUtils.ensureLayoutContext(subSession);

		assertEquals(initialModel, sc.getModel());
		assertEquals("Model and listeners are updated synchronously.", sc.getModel(), receivedNewModel.get());

		layoutContext.enableUpdate(true);
		Person newPerson = TestPerson.createPerson("newPerson");
		sc.setModel(newPerson);
		ml.globallyValidateModel(context);
		layoutContext.enableUpdate(false);

		assertEquals(newPerson, sc.getModel());
		assertEquals("Model and listeners are updated synchronously.", sc.getModel(), receivedNewModel.get());

		layoutContext.enableUpdate(true);
		TestPerson.deletePersonAndUser(newPerson);
		ml.globallyValidateModel(context);
		layoutContext.enableUpdate(false);

		assertEquals("Component resets the model to initial model.", initialModel, sc.getModel());
		assertEquals("Model and listeners are updated synchronously.", sc.getModel(), receivedNewModel.get());
	}

	public void testIntrinsicCommands() throws ConfigurationException, IOException {
		LayoutComponent layoutComponent = getLayoutComponent("testIntrinsicCommands.xml");
		LayoutComponent.Config config = layoutComponent.getConfig();
		
		SimpleCommandRegistry registry = new SimpleCommandRegistry();
		config.modifyIntrinsicCommands(registry);
		assertTrue(registry.getCommands().contains(ComponentWithIntrinsicCommand.INTRINSIC_COMMAND_NAME));
		
		assertNotNull(layoutComponent.getCommandById(ComponentWithIntrinsicCommand.INTRINSIC_COMMAND_NAME));
	}

	/**
	 * Test for Ticket #19890.
	 */
	public void testRemovePropertyListener() throws ConfigurationException {
		final LayoutComponent c =
			new SimpleComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				TypedConfiguration.newConfigItem(SimpleComponent.Config.class));

		class TestingVisibilityListener implements VisibilityListener {

			Boolean _newVisibility = null;

			@Override
			public Bubble handleVisibilityChange(final Object sender, final Boolean oldVisibility,
					final Boolean newVisibility) {
				_newVisibility = newVisibility;
				return Bubble.BUBBLE;
			}
		}

		final TestingVisibilityListener listener = new TestingVisibilityListener();

		c.addListener(LayoutComponent.VISIBILITY_EVENT, listener);
		c.setVisible(true);
		assertEquals(Boolean.TRUE, listener._newVisibility);

		c.removeListener(LayoutComponent.VISIBILITY_EVENT, listener);
		c.setVisible(false);

		assertEquals("Listener must not be called again after removal.", Boolean.TRUE, listener._newVisibility);
	}

	public void testCommandSequence() throws IOException, ConfigurationException {
		LayoutComponent layoutComponent = getLayoutComponent("testCommandSequence.xml");
		CommandHandler handler = layoutComponent.getCommandById("testCommandSequence");
		assertNotNull(handler);
		HandlerResult result = CommandHandlerUtil.handleCommand(handler, DummyDisplayContext.newInstance(), layoutComponent, Collections.<String, Object> emptyMap());
		assertTrue(result.isSuccess());
		assertEquals("value1|value2", layoutComponent.get(TEST_VALUE));
	}

	public void testCommandSequenceFailure() throws IOException, ConfigurationException {
		LayoutComponent layoutComponent = getLayoutComponent("testCommandSequenceFailure.xml");
		CommandHandler handler = layoutComponent.getCommandById("testCommandSequenceFailure");
		assertNotNull(handler);
		HandlerResult result = CommandHandlerUtil.handleCommand(handler, DummyDisplayContext.newInstance(), layoutComponent, Collections.<String, Object> emptyMap());
		assertFalse(result.isSuccess());
		assertEquals("command1.failed", result.getEncodedErrors().get(0).getKey());
		assertEquals("value1", layoutComponent.get(TEST_VALUE));
	}

	public void testCallCommand() throws IOException, ConfigurationException {
		LayoutComponent layoutComponent = getLayoutComponent("testCallCommand.xml");
		CommandHandler command1 = layoutComponent.getCommandById("command1");
		assertNotNull(command1);
		assertEquals(SimpleBoundCommandGroup.READ, command1.getCommandGroup());
		CommandHandler command2 = layoutComponent.getCommandById("command2");
		assertEquals(SimpleBoundCommandGroup.WRITE, command2.getCommandGroup());
		assertNotNull(command2);
		HandlerResult result = CommandHandlerUtil.handleCommand(command2, DummyDisplayContext.newInstance(), layoutComponent, Collections.<String, Object> emptyMap());
		assertTrue(result.isSuccess());
		assertEquals("value1", layoutComponent.get(TEST_VALUE));
	}

	public void testLayoutConstraint() throws IOException, ConfigurationException {
		ComponentWithConstraint constraintComponent =
			(ComponentWithConstraint) getLayoutComponent("testConstraint.xml");
		assertSame(75, constraintComponent.getPositiveNumber());
		try {
			LayoutComponent component = getLayoutComponent("testConstraintFailed.xml");
			fail(component + " is configured with negative number. It must not be instantiated.");
		} catch (AssertionFailedError err) {
			assertContains("Name of the file containing the misconfigured component must be contained in message.",
				"TestLayoutComponent_testConstraintFailed.xml", err.getMessage());
			assertContains("Name of misconfigured property must be contained in message.",
				"positive-number", err.getMessage());
		}
	}

	public void testCorrectDialogParent() throws ConfigurationException, IOException {
		File layoutFile = getLayoutFile("testDialogParent.xml");
		Layout comp = (Layout) getLayoutComponent(layoutFile);
		MainLayout main = comp.getMainLayout();
		assertNotNull(main);
		Layout oldParent = (Layout) comp.getChild(0);
		Layout newParent = (Layout) comp.getChild(1);

		LayoutComponent child = oldParent.getChild(0);
		Layout dialog = (Layout) child.getDialogs().get(0);
		LayoutComponent dialogContent = dialog.getChild(0);
		assertEquals(child, dialog.getDialogParent());
		assertEquals(dialog, dialog.getDialogTopLayout());
		assertEquals(child, dialogContent.getDialogParent());
		assertEquals(dialog, dialogContent.getDialogTopLayout());
		assertEquals(main, dialog.getMainLayout());
		assertEquals(main, dialogContent.getMainLayout());

		oldParent.removeComponent(child);
		assertEquals(list(), oldParent.getChildList());
		newParent.addComponent(child);
		assertEquals(list(child), newParent.getChildList());
		assertEquals(newParent, child.getParent());

		assertEquals(child, dialog.getDialogParent());
		assertEquals(dialog, dialog.getDialogTopLayout());
		assertEquals(child, dialogContent.getDialogParent());
		assertEquals(dialog, dialogContent.getDialogTopLayout());
		assertEquals(main, dialog.getMainLayout());
		assertEquals(main, dialogContent.getMainLayout());


		Layout.Config child2Conf = ComponentTestUtils.newLayoutConfig(Layout.HORIZONTAL);
		child2Conf.setName(ComponentTestUtils.newComponentName(layoutFile, "child2"));
		Layout.Config dialog2Conf = ComponentTestUtils.newLayoutConfig(Layout.HORIZONTAL);
		child2Conf.setName(ComponentTestUtils.newComponentName(layoutFile, "dialog2"));
		dialog2Conf.setDialogInfo(TypedConfiguration.newConfigItem(DialogInfo.class));
		child2Conf.getDialogs().add(dialog2Conf);

		LayoutComponent child2 = TypedConfigUtil.createInstance(child2Conf);
		child2.createSubComponents(
			new ComponentInstantiationContext(new DefaultInstantiationContext(TestLayoutComponent.class), main));
		LayoutComponent dialog2 = child2.getDialogs().get(0);

		newParent.addComponent(child2);
		assertEquals(newParent, child2.getParent());
		assertEquals(child2, dialog2.getParent());
		assertEquals(main, child2.getMainLayout());
		assertEquals(main, dialog2.getMainLayout());

	}

	public void testChildVisibility() {
		MainLayout mainLayout = ComponentTestUtils.newMainLayout();
		Layout child1 = ComponentTestUtils.newLayout(Layout.HORIZONTAL);
		mainLayout.addComponent(child1);

		mainLayout.setVisible(true);

		assertTrue(mainLayout.isVisible());
		assertTrue(child1.isVisible());

		Layout child2 = ComponentTestUtils.newLayout(Layout.HORIZONTAL);
		assertFalse(child2.isVisible());
		mainLayout.addComponent(child2);
		assertTrue(child2.isVisible());

		Layout child3 = ComponentTestUtils.newLayout(Layout.HORIZONTAL);
		Layout child4 = ComponentTestUtils.newLayout(Layout.HORIZONTAL);
		ComponentTestUtils.setChildren(mainLayout, list(child3, child4));
		assertTrue(child3.isVisible());
		assertTrue(child4.isVisible());
	}

	public void testSimpleMaster() throws IOException, ConfigurationException {
		LayoutComponent root = getLayoutComponent("testSimpleMaster.xml");
		LayoutComponent master = root.getComponentByName(ComponentName.newName(root.getName().scope(), "master"));
		LayoutComponent slave = root.getComponentByName(ComponentName.newName(root.getName().scope(), "slave"));
		
		assertEquals(master, slave.getMaster());
		assertEquals(Collections.singleton(slave), master.getSlaves());

		master.setModel("Hello world!");
		assertEquals("Hello world!", slave.getModel());
	}

	public void testTransformedMaster() throws IOException, ConfigurationException {
		LayoutComponent root = getLayoutComponent("testTransformedMaster.xml");
		LayoutComponent master = root.getComponentByName(ComponentName.newName(root.getName().scope(), "master"));
		LayoutComponent slave = root.getComponentByName(ComponentName.newName(root.getName().scope(), "slave"));
		
		assertEquals(master, slave.getMaster());
		assertEquals(Collections.singleton(slave), master.getSlaves());

		master.setModel("Hello world!");
		assertEquals("transformed(Hello world!)", slave.getModel());

		master.setModel("special-a");
		assertEquals("SPECIAL", slave.getModel());

		// Master does not change, if slave is forced to a new model.
		slave.setModel("new-value");
		assertEquals("special-a", master.getModel());
		assertEquals("new-value", slave.getModel());

		// If master changes its model to a new value, the slave is updated, even if it was forced
		// to display another model and the resulting slave model is the same as it had before the
		// forced update.
		master.setModel("special-b");
		assertEquals("SPECIAL", slave.getModel());
	}

	public void testCombinedMasters() throws IOException, ConfigurationException {
		LayoutComponent root = getLayoutComponent("testCombinedMasters.xml");
		LayoutComponent master1 = root.getComponentByName(ComponentName.newName(root.getName().scope(), "master1"));
		LayoutComponent master2 = root.getComponentByName(ComponentName.newName(root.getName().scope(), "master2"));
		LayoutComponent slave = root.getComponentByName(ComponentName.newName(root.getName().scope(), "slave"));

		assertEquals(set(master1, master2), slave.getMasters());
		assertEquals(Collections.singleton(slave), master1.getSlaves());
		assertEquals(Collections.singleton(slave), master2.getSlaves());

		master1.setModel("Hello");
		master2.setModel("world");
		assertEquals(list("Hello", "world"), slave.getModel());
	}

	public void testBidirectionalTransformation() throws IOException, ConfigurationException {
		LayoutComponent root = getLayoutComponent("testBidirectionalTransformation.xml");
		LayoutComponent component1 =
			root.getComponentByName(ComponentName.newName(root.getName().scope(), "component1"));
		LayoutComponent component2 =
			root.getComponentByName(ComponentName.newName(root.getName().scope(), "component2"));

		component1.setModel("special");
		assertEquals("SPECIAL", component2.getModel());
		component2.setModel("transformed_foobar");
		assertEquals("foobar", component1.getModel());
	}

	public static class TestModelTransform extends AbstractTransformLinking<AbstractTransformLinking.Config> {

		/**
		 * Creates a {@link TestModelTransform}.
		 */
		public TestModelTransform(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void appendTo(StringBuilder result) {
			result.append("TestModelTransform(");
			input().appendTo(result);
			result.append(")");
		}

		@Override
		protected BiFunction<Object, Object, ?> transformation() {
			return (x, y) -> x != null && x.toString().startsWith("special") ? "SPECIAL" : "transformed(" + x + ")";
		}

	}

	public static class TestBidirectionalModelTransform
			extends BidirectionalTransformLinking<AbstractTransformLinking.Config> {

		private static final String TRANSFORMED_PREFIX = "transformed_";

		/**
		 * Creates a {@link TestModelTransform}.
		 */
		public TestBidirectionalModelTransform(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void appendTo(StringBuilder result) {
			result.append("TestBidirectionalModelTransform(");
			input().appendTo(result);
			result.append(")");
		}

		@Override
		protected BiFunction<Object, Object, ?> transformation() {
			return (x, y) -> x != null && x.toString().startsWith("special") ? "SPECIAL" : TRANSFORMED_PREFIX + x;
		}

		@Override
		protected Function<Object, ?> inverseTransformation() {
			return x -> x != null && x.toString().startsWith(TRANSFORMED_PREFIX)
				? x.toString().substring(TRANSFORMED_PREFIX.length())
				: "special";
		}

	}

	public void testMixedLayoutOverlayTypes() throws IOException, ConfigurationException {
		String argumentOverlay1 = "argumentOverlay1.layout.overlay.xml";
		String argumentOverlay2 = "argumentOverlay2.layout.overlay.xml";
		String argumentOverlay3 = "argumentOverlay3.layout.overlay.xml";
		String componentOverlay1 = "componentOverlay1.layout.overlay.xml";
		String componentOverlay2 = "componentOverlay2.layout.overlay.xml";
		String baseConfig = "baseConfig.layout.xml";

		String baseUrl = LayoutResolver.TEST_SCHEME_PREFIX + createDifferentOverlayLayoutFilename(baseConfig);
		CreateComponentParameter parameters = CreateComponentParameter.newParameter(baseUrl);

		BinaryData overlay1 = getDifferentOverlayLayoutData(argumentOverlay1);
		BinaryData overlay2 = getDifferentOverlayLayoutData(argumentOverlay2);
		BinaryData overlay3 = getDifferentOverlayLayoutData(componentOverlay1);
		BinaryData overlay4 = getDifferentOverlayLayoutData(argumentOverlay3);
		BinaryData overlay5 = getDifferentOverlayLayoutData(componentOverlay2);

		List<BinaryData> overlays = Arrays.asList(overlay1, overlay2, overlay3, overlay4, overlay5);

		TabComponent.Config tabbar = (TabComponent.Config) createLayoutConfig(parameters, overlays).get();
		List<LayoutComponent.Config> components = tabbar.getComponents();

		assertEquals(5, components.size());
		assertEquals(baseConfig, components.get(0).getName().localName());
		assertEquals(argumentOverlay1, components.get(1).getName().localName());
		assertEquals(argumentOverlay2, components.get(2).getName().localName());
		assertEquals(componentOverlay1, components.get(3).getName().localName());
		assertEquals(componentOverlay2, components.get(4).getName().localName());
	}

	public void testLayoutResPrefix() throws ConfigurationException, IOException {
		LayoutComponent comp1 =
			ComponentTestUtils.createComponent(
				TestLayoutReference.fileName(TestLayoutComponent.class, "testLayoutResPrefix1.layout.xml"));
		assertEquals("layouts.test.com.top_logic.TestLayoutComponent.testLayoutResPrefix1.",
			((SimpleComponent.Config) comp1.getConfig()).getContent());
		LayoutComponent comp2 =
			ComponentTestUtils.createComponent(
				TestLayoutReference.fileName(TestLayoutComponent.class, "testLayoutResPrefix2.layout.xml"));
		assertEquals("layouts.test.com.top_logic.TestLayoutComponent.testLayoutResPrefix2.",
			((SimpleComponent.Config) comp2.getConfig()).getContent());
	}

	private static TLLayout createLayoutConfig(CreateComponentParameter parameters, List<BinaryData> overlays)
			throws IOException, ConfigurationException {
		CheckingProtocol protocol = new AssertProtocol();
		LayoutResolver resolver = createLayoutResolver(protocol);
		DefaultInstantiationContext context = new DefaultInstantiationContext(protocol);
		parameters.setLayoutResolver(resolver);

		return LayoutStorage.createLayout(context, parameters, overlays);
	}

	private BinaryData getDifferentOverlayLayoutData(String suffix) {
		return FileManager.getInstance().getDataOrNull(createDifferentOverlayLayoutFilename(suffix));
	}

	private String createDifferentOverlayLayoutFilename(String suffix) {
		Class<TestLayoutComponent> clazz = TestLayoutComponent.class;
		String filenameSuffix = clazz.getSimpleName() + "_testDifferentOverlays_" + suffix;
		return CustomPropertiesDecorator.createFileName(clazz, filenameSuffix);
	}

	private static LayoutResolver createLayoutResolver(Protocol log) throws IOException {
		Application application =
			CompileTimeApplication.createCompileTimeApplication(new File(ModuleLayoutConstants.PATH_TO_MODULE_ROOT),
				new File(ModuleLayoutConstants.WEBAPP_DIR));
		Theme theme = ThemeFactory.getTheme();
		return application.createLayoutResolver(log, theme);
	}

	private static LayoutComponent getLayoutComponent(String fileNameSuffix) throws IOException, ConfigurationException {
		return getLayoutComponent(getLayoutFile(fileNameSuffix));
	}

	private static File getLayoutFile(String fileNameSuffix) throws IOException {
		return ComponentTestUtils.getLayoutFile(TestLayoutComponent.class, fileNameSuffix);
	}

	private static LayoutComponent getLayoutComponent(File layoutFile) throws IOException, ConfigurationException {
		CreateComponentParameter arguments = ComponentTestUtils.createComponentArgs(layoutFile);
		TLLayout config = createLayoutConfig(arguments, Collections.emptyList());

		return ComponentTestUtils.createComponent(layoutFile, config.get());
	}

	public static class TestingCommand extends AbstractCommandHandler {

		public interface Config extends AbstractCommandHandler.Config {

			String getTestValue();

			ResKey getFailure();

		}

		private String _testValue;

		private ResKey _failure;

		/**
		 * Creates a {@link TestLayoutComponent.TestingCommand} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestingCommand(InstantiationContext context, Config config) {
			super(context, config);

			_testValue = config.getTestValue();
			_failure = config.getFailure();
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			String value = aComponent.get(TEST_VALUE);
			if (value == null) {
				value = _testValue;
			} else {
				value = value + "|" + _testValue;
			}
			aComponent.set(TEST_VALUE, value);

			if (_failure != null) {
				HandlerResult error = new HandlerResult();
				error.addError(_failure);
				return error;
			} else {
				return HandlerResult.DEFAULT_RESULT;
			}
		}

	}

	public static class ComponentWithConstraint extends SimpleComponent {

		/**
		 * Typed configuration interface definition for {@link ComponentWithConstraint}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config extends SimpleComponent.Config {

			@Constraint(value = NonNegative.class)
			@Mandatory
			int getPositiveNumber();

		}

		/**
		 * Create a {@link TestLayoutComponent.ComponentWithConstraint}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the new object in
		 * @param config
		 *        the configuration object to be used for instantiation
		 */
		public ComponentWithConstraint(InstantiationContext context, Config config)
				throws ConfigurationException {
			super(context, config);
		}

		int getPositiveNumber() {
			return ((Config) getConfig()).getPositiveNumber();
		}

	}

	public static class ComponentWithIntrinsicCommand extends SimpleComponent {
		
		public static final String INTRINSIC_COMMAND_NAME = "TestIntrinsicCommands";

		/**
		 * Typed configuration interface definition for {@link ComponentWithIntrinsicCommand}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config extends SimpleComponent.Config {
			
			/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
			Lookup LOOKUP = MethodHandles.lookup();

			@Override
			default void modifyIntrinsicCommands(CommandRegistry registry) {
				SimpleComponent.Config.super.modifyIntrinsicCommands(registry);
				registry.registerCommand(INTRINSIC_COMMAND_NAME);
			}

		}
		
		/**
		 * Creates a new {@link ComponentWithIntrinsicCommand}.
		 */
		public ComponentWithIntrinsicCommand(InstantiationContext context, Config atts) throws ConfigurationException {
			super(context, atts);
		}

	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestLayoutComponent.class,
			CommandHandlerFactory.Module.INSTANCE			, 
			LayoutStorage.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE
			);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
