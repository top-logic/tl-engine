/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.DynamicViewElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.ReactSwitchControl;
import com.top_logic.layout.view.element.SwitchElement;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests the datatype {@code tl.view:ViewDefinition} storing view configurations and the element
 * {@link DynamicViewElement &lt;dynamic-view&gt;} showing them.
 */
public class TestDynamicViewElement extends BasicTestCase {

	/**
	 * A stored view whose {@code <switch>} shows content only while its channel {@code item} holds
	 * {@code "x"}: the switch is shown exactly when the channel bound from the enclosing view is
	 * visible in the stored view.
	 */
	private static final String VIEW_WITH_SWITCH = """
		<view>
			<channels>
				<channel name="item"/>
			</channels>
			<switch input="item">
				<case test="$item == 'x'">
					<stack/>
				</case>
			</switch>
		</view>
		""";

	private static final String VIEW_WITH_STACK = """
		<view>
			<stack/>
		</view>
		""";

	private static final String DYNAMIC_VIEW = """
		<dynamic-view inputs="value" view="$value">
			<bind channel="item" to="item"/>
		</dynamic-view>
		""";

	/**
	 * Tests that a view configuration survives storing and loading through the storage of the
	 * datatype {@code tl.view:ViewDefinition}, as declared in its model file.
	 */
	public void testStorageRoundTrip() throws Exception {
		StorageMapping<?> mapping = viewDefinitionStorage();
		assertEquals(ViewElement.Config.class, mapping.getApplicationType());

		ViewElement.Config view = parseView(VIEW_WITH_SWITCH);
		Object stored = mapping.getStorageObject(view);
		assertTrue("Stored as text.", stored instanceof String);

		Object loaded = mapping.getBusinessObject(stored);
		assertTrue("Loaded as view configuration.", loaded instanceof ViewElement.Config);
		ViewElement.Config loadedView = (ViewElement.Config) loaded;
		assertEquals(1, loadedView.getChannels().size());
		assertEquals("item", loadedView.getChannels().get(0).getName());
		assertTrue(loadedView.getContent() instanceof SwitchElement.Config);
		assertEquals("Storing again yields the same text.", stored, mapping.getStorageObject(loadedView));

		// The loaded configuration can be shown.
		assertNotNull(ViewLoader.instantiateView(loadedView));
	}

	/**
	 * Tests that a {@code <dynamic-view>} shows the view its expression yields with the bound channel
	 * visible inside, switches to another view when the input changes, and shows nothing for
	 * {@code null}.
	 */
	public void testShowsViewOfInput() throws Exception {
		ViewElement.Config viewWithSwitch = parseView(VIEW_WITH_SWITCH);
		ViewElement.Config viewWithStack = parseView(VIEW_WITH_STACK);

		ViewContext context = createContext();
		DefaultViewChannel value = new DefaultViewChannel("value");
		DefaultViewChannel item = new DefaultViewChannel("item");
		context.registerChannel("value", value);
		context.registerChannel("item", item);
		item.set("x");
		value.set(viewWithSwitch);

		ReactControl control = (ReactControl) dynamicView().createControl(context);
		assertFalse(control.isHidden());
		ReactControl firstContent = content(control);
		ReactSwitchControl embeddedSwitch = find(firstContent, ReactSwitchControl.class);
		assertNotNull("The stored view is shown.", embeddedSwitch);
		assertFalse("The bound channel is visible in the stored view.", embeddedSwitch.isHidden());

		item.set("y");
		assertTrue("The stored view follows the bound channel.", embeddedSwitch.isHidden());

		value.set(viewWithStack);
		assertFalse(control.isHidden());
		ReactControl secondContent = content(control);
		assertNotSame("Another view replaces the content.", firstContent, secondContent);
		assertNull("The switch of the replaced view is gone.", find(secondContent, ReactSwitchControl.class));

		value.set(null);
		assertTrue("Without a view, nothing is shown.", control.isHidden());
		assertNull(find(content(control), ReactSwitchControl.class));

		value.set(viewWithSwitch);
		assertFalse(control.isHidden());
		assertNotNull("The view is shown again.", find(content(control), ReactSwitchControl.class));
	}

	/**
	 * Tests that a view configuration that cannot be instantiated is shown as error placeholder.
	 */
	public void testDefectiveView() throws Exception {
		ViewElement.Config viewWithoutContent = TypedConfiguration.newConfigItem(ViewElement.Config.class);

		ViewContext context = createContext();
		DefaultViewChannel value = new DefaultViewChannel("value");
		context.registerChannel("value", value);
		context.registerChannel("item", new DefaultViewChannel("item"));
		value.set(viewWithoutContent);

		ReactControl control = (ReactControl) dynamicView().createControl(context);
		assertNotNull("A defective view renders the error placeholder.",
			find(content(control), ReactAlertControl.class));
	}

	private static StorageMapping<?> viewDefinitionStorage() throws Exception {
		ModelConfig model = DefinitionReader.readElementConfig(
			FileManager.getInstance().getData("/WEB-INF/model/tl.view.model.xml"));
		for (TypeConfig type : model.getModule("tl.view").getTypes()) {
			if (type instanceof DatatypeConfig datatype && "ViewDefinition".equals(datatype.getName())) {
				DefaultInstantiationContext context = new DefaultInstantiationContext(TestDynamicViewElement.class);
				StorageMapping<?> result = context.getInstance(datatype.getStorageMapping());
				context.checkErrors();
				return result;
			}
		}
		throw new AssertionError("No datatype 'tl.view:ViewDefinition'.");
	}

	private static ViewElement.Config parseView(String xml) throws Exception {
		return ViewLoader.parseConfig(List.of(CharacterContents.newContent(xml)));
	}

	private static UIElement dynamicView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDynamicViewElement.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(
			DynamicViewElement.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(DynamicViewElement.Config.class)));
		reader.setSource(CharacterContents.newContent(DYNAMIC_VIEW));
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) reader.read();
		context.checkErrors();
		UIElement result = (UIElement) context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static ViewContext createContext() {
		// No object is observed, so the window reports no model scope - which spares the test the
		// knowledge base one is built from.
		return new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return null;
			}
		});
	}

	private static ReactControl content(ReactControl dynamicView) {
		List<ReactControl> children = dynamicView.displayedChildren();
		assertEquals(1, children.size());
		return children.get(0);
	}

	private static <T extends ReactControl> T find(ReactControl control, Class<T> type) {
		if (type.isInstance(control)) {
			return type.cast(control);
		}
		for (ReactControl child : control.displayedChildren()) {
			T result = find(child, type);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Test suite with a knowledge base, in which the expressions are evaluated.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDynamicViewElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
