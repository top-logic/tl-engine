/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.I18NConstants;
import com.top_logic.layout.configedit.PolymorphicItemControl;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for {@link ConfigEditorControl}.
 */
public class TestConfigEditorControl extends TestCase {

	/** Nested configuration used as an ITEM property value. */
	public interface InnerConfig extends ConfigurationItem {

		/** Property name for {@link #getTitle()}. */
		String TITLE = "title";

		@Name(TITLE)
		String getTitle();

		void setTitle(String value);
	}

	/**
	 * A minimal {@code ConfigurationValueBinding} with no accompanying {@code @Format} - stands
	 * in for the framework's real "binding-only" bindings ({@code AbstractListBinding},
	 * {@code MapAttributeBinding}, {@code XMLFragmentString}), none of which pair with a
	 * {@code ConfigurationValueProvider}. Never actually exercised for XML I/O by these tests.
	 */
	public static class NoFormatBinding extends AbstractConfigurationValueBinding<List<String>> {
		@Override
		public void saveConfigItem(XMLStreamWriter out, List<String> item) throws XMLStreamException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}

		@Override
		public List<String> loadConfigItem(XMLStreamReader in, List<String> baseValue)
				throws XMLStreamException, ConfigurationException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}
	}

	/** Test configuration with a mix of property types. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isEnabled()}. */
		String ENABLED = "enabled";

		/** Property name for {@link #getInner()}. */
		String INNER = "inner";

		/** Property name for {@link #getBindingOnly()}. */
		String BINDING_ONLY = "bindingOnly";

		/** Property name for {@link #getItemArray()}. */
		String ITEM_ARRAY = "itemArray";

		/**
		 * A {@link ResKey} property is {@link PropertyKind#COMPLEX} - its type carries both a
		 * {@code @Format} and a {@code ConfigurationValueBinding}, and a binding decides the kind
		 * before the value provider is considered. It has a value provider, so it can be edited as
		 * text and must appear in the form.
		 */
		@Name(LABEL)
		@Mandatory
		ResKey getLabel();

		void setLabel(ResKey value);

		/**
		 * A {@link PropertyKind#COMPLEX} property with only a value binding and no format has no
		 * way to become text, so it must stay skipped.
		 */
		@Name(BINDING_ONLY)
		@Binding(NoFormatBinding.class)
		List<String> getBindingOnly();

		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		void setCount(int value);

		@Name(ENABLED)
		boolean isEnabled();

		void setEnabled(boolean value);

		@Name(INNER)
		InnerConfig getInner();

		void setInner(InnerConfig value);

		/**
		 * An {@link PropertyKind#ARRAY} property of configuration items, edited by the same
		 * {@link com.top_logic.layout.configedit.ConfigListEditorControl} as a {@link ListItem} LIST
		 * property - only the value's shape (array vs. {@link List}) differs.
		 */
		@Name(ITEM_ARRAY)
		ListItem[] getItemArray();

		void setItemArray(ListItem[] value);
	}

	/** Common instance type for the polymorphic handler implementations. */
	public interface Handler {
		// Marker interface.
	}

	/** Base polymorphic configuration for testing. */
	public interface HandlerConfig extends PolymorphicConfiguration<Handler> {
		// Marker interface.
	}

	/** Concrete handler config A with a string property. */
	public interface HandlerAConfig extends HandlerConfig {

		/** Property name for {@link #getNameA()}. */
		String NAME_A = "nameA";

		@Name(NAME_A)
		String getNameA();

		void setNameA(String value);
	}

	/** Concrete handler config B with an int property. */
	public interface HandlerBConfig extends HandlerConfig {

		/** Property name for {@link #getValueB()}. */
		String VALUE_B = "valueB";

		@Name(VALUE_B)
		int getValueB();

		void setValueB(int value);
	}

	/**
	 * Concrete {@link Handler} implementation selected through {@link HandlerAConfig}.
	 *
	 * <p>
	 * The polymorphic options of a {@link PolymorphicConfiguration} property are its instantiable
	 * implementation classes (here {@link HandlerA} and {@link HandlerB}), discovered via the
	 * {@link com.top_logic.basic.reflect.TypeIndex}.
	 * </p>
	 */
	public static class HandlerA implements Handler {
		/** Creates a {@link HandlerA} from configuration. */
		public HandlerA(InstantiationContext context, HandlerAConfig config) {
			// No state required for the test.
		}
	}

	/** Concrete {@link Handler} implementation selected through {@link HandlerBConfig}. */
	public static class HandlerB implements Handler {
		/** Creates a {@link HandlerB} from configuration. */
		public HandlerB(InstantiationContext context, HandlerBConfig config) {
			// No state required for the test.
		}
	}

	/** Test config with a polymorphic ITEM property and a polymorphic ARRAY property. */
	public interface PolymorphicTestConfig extends ConfigurationItem {

		/** Property name for {@link #getHandler()}. */
		String HANDLER = "handler";

		/** Property name for {@link #getHandlerArray()}. */
		String HANDLER_ARRAY = "handlerArray";

		@Name(HANDLER)
		HandlerConfig getHandler();

		void setHandler(HandlerConfig value);

		/**
		 * A {@link PropertyKind#ARRAY} property whose element type ({@link HandlerConfig}) is
		 * itself a {@link PolymorphicConfiguration} with more than one implementation
		 * ({@link HandlerA}, {@link HandlerB}) - unlike {@link TestConfig#getItemArray()}, this
		 * array property's per-element type selector actually has more than one option, so it is
		 * rendered.
		 */
		@Name(HANDLER_ARRAY)
		HandlerConfig[] getHandlerArray();

		void setHandlerArray(HandlerConfig[] value);
	}

	/** Element type of {@link ListTestConfig#getKeyedItems()} and {@link ListTestConfig#getPlainItems()}. */
	public interface ListItem extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		void setName(String value);
	}

	/** Test config with a keyed and an unkeyed LIST property, for {@link ConfigListEditorControl} tests. */
	public interface ListTestConfig extends ConfigurationItem {

		/** Property name for {@link #getKeyedItems()}. */
		String KEYED_ITEMS = "keyedItems";

		/** Property name for {@link #getPlainItems()}. */
		String PLAIN_ITEMS = "plainItems";

		@Name(KEYED_ITEMS)
		@Key(ListItem.NAME)
		java.util.List<ListItem> getKeyedItems();

		@Name(PLAIN_ITEMS)
		java.util.List<ListItem> getPlainItems();
	}

	/**
	 * Test subclass that bypasses {@link com.top_logic.layout.form.values.edit.Labels} to avoid
	 * requiring Resources/ThreadContextManager in unit tests.
	 */
	static class TestableConfigEditorControl extends ConfigEditorControl {

		TestableConfigEditorControl(ReactContext context, ConfigurationItem config) {
			super(context, config);
		}

		TestableConfigEditorControl(ReactContext context, ConfigurationItem config,
				Set<PropertyDescriptor> hiddenProperties) {
			super(context, config, hiddenProperties, false);
		}

		@Override
		protected String resolveLabel(PropertyDescriptor property) {
			return property.getPropertyName();
		}

		@Override
		protected String resolveTooltip(PropertyDescriptor property) {
			// Bypass Resources/ThreadContextManager in unit tests.
			return null;
		}

		@Override
		protected ConfigEditorControl createNestedEditor(ReactContext context, ConfigurationItem nested) {
			return new TestableConfigEditorControl(context, nested);
		}

		@Override
		protected PolymorphicItemControl createPolymorphicGroup(ReactContext context, String label,
				ConfigurationItem parentConfig, PropertyDescriptor property) {
			return new PolymorphicItemControl(context, label, parentConfig, property, this::createNestedEditor);
		}

		int getChildCount() {
			return getChildren().size();
		}

		java.util.List<ReactControl> getChildrenList() {
			return getChildren();
		}
	}

	/**
	 * Test subclass exposing the protected {@code getChildren()} list, so a test can reach the "+"
	 * {@link ReactButtonControl} and drive it as a click would.
	 */
	static class TestableConfigListEditorControl extends ConfigListEditorControl {

		TestableConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
				PropertyDescriptor property) {
			super(context, parentConfig, property);
		}

		java.util.List<ReactControl> getChildrenList() {
			return getChildren();
		}
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Whether {@code propertyName} contributes a rendered child to a {@link ConfigEditorControl}
	 * built over {@code config}.
	 *
	 * <p>
	 * Determined by comparing the child count of a control built with nothing hidden against one
	 * that hides exactly this property (the {@code hiddenProperties} constructor parameter
	 * {@link ConfigEditorControl} already offers): a property that was actually rendered
	 * contributes exactly one child, so hiding it removes exactly one. This is the same
	 * before/after comparison {@link #testNullItemPropertySkipped()} already relies on to prove
	 * that a skipped property adds nothing.
	 * </p>
	 */
	private boolean rendersProperty(ConfigurationItem config, String propertyName) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);

		int shown = new TestableConfigEditorControl(createTestContext(), config).getChildCount();
		int hidden =
			new TestableConfigEditorControl(createTestContext(), config, Set.of(property)).getChildCount();

		return shown - hidden == 1;
	}

	/**
	 * Finds the "+" add button among the given {@link ConfigListEditorControl}'s children (the only
	 * top-level {@link ReactButtonControl} - the move/remove buttons live inside each element's
	 * group header, not as direct children).
	 */
	private ReactButtonControl findAddButton(TestableConfigListEditorControl editor) {
		for (ReactControl child : editor.getChildrenList()) {
			if (child instanceof ReactButtonControl button) {
				return button;
			}
		}
		fail("Should have an add button");
		return null;
	}

	/**
	 * Simulates clicking the given button and returns the command's {@link HandlerResult}.
	 */
	private HandlerResult click(ReactButtonControl button) {
		return button.executeCommand("click", java.util.Map.of());
	}

	/**
	 * Simulates clicking the given editor's "+" add button, as an end user would.
	 */
	private void clickAddButton(TestableConfigListEditorControl editor) {
		click(findAddButton(editor));
	}

	/**
	 * Creates a {@link ListItem} with the given name, for array/list element fixtures.
	 */
	private ListItem newListItem(String name) {
		ListItem item = TypedConfiguration.newConfigItem(ListItem.class);
		item.setName(name);
		return item;
	}

	/**
	 * The element groups rendered by the given editor, in list order (the "+" button at the end is
	 * not one of them).
	 */
	private List<ReactControl> elementGroups(TestableConfigListEditorControl editor) {
		List<ReactControl> groups = new ArrayList<>();
		for (ReactControl child : editor.getChildrenList()) {
			if (child instanceof ReactFormGroupControl) {
				groups.add(child);
			}
		}
		return groups;
	}

	/**
	 * Finds the header action button carrying the given label (the move-up "\u25B2", move-down
	 * "\u25BC", or remove "\u2715" icon) inside the given element group.
	 *
	 * <p>
	 * Reached through {@link ReactControl#scriptingChildren()} and
	 * {@link ReactControl#scriptingScalarState()} - the same headless projection the scripted-test
	 * player uses to address a control's nested controls and their state - rather than through a
	 * new accessor, because {@link com.top_logic.layout.react.control.layout.ReactFormGroupControl}
	 * is outside this module and already exposes exactly this.
	 * </p>
	 */
	private ReactButtonControl findHeaderButton(ReactControl elementGroup, String label) {
		for (ReactControl child : elementGroup.scriptingChildren()) {
			if (child instanceof ReactButtonControl button
				&& label.equals(button.scriptingScalarState().get("label"))) {
				return button;
			}
		}
		fail("Should have a \"" + label + "\" header button");
		return null;
	}

	/**
	 * Finds the field model of the "Type" selector rendered inside the given element group -
	 * reached the same way as {@link #findHeaderButton(ReactControl, String)}, via
	 * {@link ReactControl#scriptingChildren()} and {@link ReactControl#scriptingScalarState()},
	 * plus the public {@link ReactControl#getModel()} of the field control found that way.
	 */
	private FieldModel findTypeFieldModel(ReactControl elementGroup) {
		for (ReactControl child : elementGroup.scriptingChildren()) {
			if ("Type".equals(child.scriptingScalarState().get("label"))) {
				for (ReactControl field : child.scriptingChildren()) {
					return (FieldModel) field.getModel();
				}
			}
		}
		fail("Should have a \"Type\" selector field in the element group");
		return null;
	}

	/**
	 * Adding to a keyed list works as long as every existing element already has a real key -
	 * distinct real names never collide.
	 */
	public void testAddElementToKeyedListWithAllKeysSetWorks() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		first.setName("Apple");
		config.getKeyedItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		HandlerResult result = click(findAddButton(editor));

		assertTrue("Adding a second element must succeed while every existing key is set",
			result.isSuccess());
		assertEquals("A second element should have been added", 2, config.getKeyedItems().size());
	}

	/**
	 * Adding a second element to a keyed list is refused - with the new user-facing message, not
	 * the technical {@link IllegalArgumentException} - while the first element still has no name.
	 *
	 * <p>
	 * The command dispatch ({@link com.top_logic.layout.react.control.ReactCommandInvoker}) never
	 * lets a {@link TopLogicException} escape a {@code @ReactCommandHandler} method: it is caught
	 * and wrapped into an {@link com.top_logic.basic.exception.I18NRuntimeException} carried by the
	 * returned {@link HandlerResult} (see {@code ReactCommandInvoker#invoke}), rather than
	 * propagating as a thrown exception - so the refusal is observed through
	 * {@link HandlerResult#getException()}, not a {@code catch} block.
	 * </p>
	 */
	public void testAddElementToKeyedListWithUnsetKeyIsRefused() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		// Name deliberately left unset.
		config.getKeyedItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		HandlerResult result = click(findAddButton(editor));

		assertFalse("Adding a second element while the first has no name must be refused",
			result.isSuccess());
		assertNotNull("The refusal must be reported as an exception", result.getException());
		assertEquals("The refusal must carry the new user-facing message",
			I18NConstants.ERROR_LIST_ELEMENT_KEY_MISSING__PROPERTY,
			result.getException().getErrorKey().plain());
		assertEquals("No element should have been added", 1, config.getKeyedItems().size());
	}

	/**
	 * A LIST property without a key property (no {@link Key} annotation) is unaffected by the new
	 * guard, even though its elements never carry a name at all.
	 */
	public void testAddElementToUnkeyedListIsUnaffected() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		// Name deliberately left unset - there is no key property to collide on.
		config.getPlainItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.PLAIN_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		HandlerResult result = click(findAddButton(editor));

		assertTrue("Adding a second element to an unkeyed list must never be blocked",
			result.isSuccess());
		assertEquals("A second element should have been added", 2, config.getPlainItems().size());
	}

	/**
	 * Tests that the editor creates child controls for PLAIN/REF/COMPLEX-with-format properties.
	 *
	 * <p>
	 * TestConfig has 2 PLAIN properties (count, enabled), a COMPLEX property with a format
	 * (label), and a binding-only COMPLEX property (bindingOnly, which stays skipped), plus the
	 * inherited "configuration-interface" PLAIN property. With no ITEM set, the child count
	 * should be at least 3.
	 * </p>
	 */
	public void testChildControlsCreated() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// ConfigurationItem declares "configuration-interface" as a PLAIN property too.
		// 3 rendered (count, enabled, label) + 1 inherited = at least 4 children (each wrapped in
		// chrome); bindingOnly contributes none.
		assertTrue("Should have at least 3 child controls", editor.getChildCount() >= 3);
	}

	/**
	 * Tests that the editor correctly reflects the React module name.
	 */
	public void testReactModule() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		assertEquals("TLFormLayout", editor.getReactModule());
	}

	/**
	 * Tests that cleanup detaches all field models so config listeners no longer fire.
	 */
	public void testCleanup() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		// Create field models directly to verify detach behavior.
		PropertyDescriptor labelProp = config.descriptor().getProperty(TestConfig.LABEL);
		ConfigFieldModel labelModel = new ConfigFieldModel(config, labelProp);

		int[] callCount = {0};
		labelModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				callCount[0]++;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		// Before detach, changing config fires through the model.
		config.setLabel(ResKey.text("before"));
		assertEquals("Listener should fire before detach", 1, callCount[0]);

		labelModel.detach();

		// After detach, config changes are no longer propagated.
		config.setLabel(ResKey.text("after"));
		assertEquals("Listener should not fire after detach", 1, callCount[0]);

		// Now verify that the editor's cleanup triggers detach via cleanup actions.
		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);
		assertTrue("Editor should have children", editor.getChildCount() >= 3);

		editor.cleanupTree();

		// After cleanupTree, the editor's children list is still there but SSE is detached.
		// The cleanup actions (model::detach) have run.
	}

	/**
	 * A {@link ResKey} property is {@link PropertyKind#COMPLEX} - its type carries a
	 * {@code @Format} and a {@code ConfigurationValueBinding}, and a binding decides the kind
	 * before the value provider is considered. It has a value provider, so it can be edited as
	 * text and must appear in the form.
	 */
	public void testComplexPropertyWithFormatIsDisplayed() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		assertTrue("A ResKey property must be rendered, not skipped.",
			rendersProperty(config, TestConfig.LABEL));
	}

	/**
	 * A {@link PropertyKind#COMPLEX} property with only a value binding and no format has no way
	 * to become text, so it stays skipped - rendering it would hand the service a property it
	 * rejects.
	 */
	public void testComplexPropertyWithoutFormatIsSkipped() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		assertFalse("A binding-only property has no text form and must stay skipped.",
			rendersProperty(config, TestConfig.BINDING_ONLY));
	}

	/**
	 * An array property is a sequence like a list and gets the same editor.
	 */
	public void testArrayPropertyIsRenderedByListEditor() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		assertTrue("An array property must be rendered by the list editor.",
			rendersProperty(config, TestConfig.ITEM_ARRAY));
	}

	/**
	 * Adding to an array property stores a new array, not a list: the configuration rejects a
	 * value of the wrong shape, so this is what makes the editor usable at all.
	 */
	public void testAddingToArrayPropertyStoresAnArray() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEM_ARRAY);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);

		Object value = config.value(property);
		assertNotNull("The array must have been stored.", value);
		assertTrue("An array property must hold an array, not a list.", value.getClass().isArray());
		assertEquals("One element must have been added.", 1, java.lang.reflect.Array.getLength(value));
	}

	/**
	 * Removing an element from an array property leaves the remaining ones, in order, and writes
	 * the result back as an array, not a list - the same shape check as adding, now for removal.
	 */
	public void testRemovingFromArrayPropertyStoresAnArray() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setItemArray(new ListItem[] { newListItem("Apple"), newListItem("Banana"), newListItem("Cherry") });
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEM_ARRAY);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		click(findHeaderButton(elementGroups(editor).get(1), "\u2715"));

		Object value = config.value(property);
		assertTrue("An array property must still hold an array after removal.", value.getClass().isArray());
		ListItem[] remaining = (ListItem[]) value;
		assertEquals("One element must have been removed.", 2, remaining.length);
		assertEquals("The remaining elements must keep their order.", "Apple", remaining[0].getName());
		assertEquals("The remaining elements must keep their order.", "Cherry", remaining[1].getName());
	}

	/**
	 * Moving an element up in an array property reorders it and writes the result back as an
	 * array, not a list.
	 */
	public void testMovingUpInArrayPropertyStoresAnArray() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setItemArray(new ListItem[] { newListItem("Apple"), newListItem("Banana"), newListItem("Cherry") });
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEM_ARRAY);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		click(findHeaderButton(elementGroups(editor).get(1), "\u25B2"));

		Object value = config.value(property);
		assertTrue("An array property must still hold an array after reordering.", value.getClass().isArray());
		ListItem[] reordered = (ListItem[]) value;
		assertEquals("Three elements must remain.", 3, reordered.length);
		assertEquals("Banana", reordered[0].getName());
		assertEquals("Apple", reordered[1].getName());
		assertEquals("Cherry", reordered[2].getName());
	}

	/**
	 * Moving an element down in an array property reorders it and writes the result back as an
	 * array, not a list.
	 */
	public void testMovingDownInArrayPropertyStoresAnArray() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setItemArray(new ListItem[] { newListItem("Apple"), newListItem("Banana"), newListItem("Cherry") });
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.ITEM_ARRAY);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		click(findHeaderButton(elementGroups(editor).get(1), "\u25BC"));

		Object value = config.value(property);
		assertTrue("An array property must still hold an array after reordering.", value.getClass().isArray());
		ListItem[] reordered = (ListItem[]) value;
		assertEquals("Three elements must remain.", 3, reordered.length);
		assertEquals("Apple", reordered[0].getName());
		assertEquals("Cherry", reordered[1].getName());
		assertEquals("Banana", reordered[2].getName());
	}

	/**
	 * Changing an element's type in a polymorphic array property changes the element's type and
	 * writes the result back as an array, not a list.
	 *
	 * <p>
	 * Unlike {@link TestConfig#getItemArray()} (whose element type, {@link ListItem}, has no
	 * second implementation), {@link PolymorphicTestConfig#getHandlerArray()}'s element type
	 * ({@link HandlerConfig}) has two ({@link HandlerA}, {@link HandlerB}), so its per-element type
	 * selector genuinely has more than one option and is rendered - the type selector only renders
	 * when {@code options().size() > 1}
	 * ({@code ConfigListEditorControl.createElementGroup}), which does not depend on the property
	 * being LIST or ARRAY.
	 * </p>
	 */
	public void testChangingTypeInPolymorphicArrayPropertyStoresAnArray() {
		PolymorphicTestConfig config = TypedConfiguration.newConfigItem(PolymorphicTestConfig.class);
		HandlerAConfig handlerA = TypedConfiguration.newConfigItem(HandlerAConfig.class);
		handlerA.setNameA("first");
		config.setHandlerArray(new HandlerConfig[] { handlerA });
		PropertyDescriptor property = config.descriptor().getProperty(PolymorphicTestConfig.HANDLER_ARRAY);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		List<Object> options =
			com.top_logic.layout.configedit.PolymorphicOptions.compute(config, property).options();
		int handlerBIndex = options.indexOf(HandlerB.class);
		assertTrue("HandlerB must be an available option for the array property too", handlerBIndex >= 0);

		FieldModel typeModel = findTypeFieldModel(elementGroups(editor).get(0));
		typeModel.setValue(com.top_logic.layout.configedit.PolymorphicOptions.keyFor(handlerBIndex));

		Object value = config.value(property);
		assertTrue("An array property must still hold an array after a type change.", value.getClass().isArray());
		HandlerConfig[] result = (HandlerConfig[]) value;
		assertEquals("One element must remain.", 1, result.length);
		assertEquals("The element's type must have changed to HandlerBConfig.",
			HandlerBConfig.class, result[0].descriptor().getConfigurationInterface());
	}

	/**
	 * Tests that ITEM properties produce a child control (form group).
	 */
	public void testItemPropertyRendered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		inner.setTitle("Hello");
		config.setInner(inner);

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// With an ITEM property set, the editor should have more children than without.
		// PLAIN/REF children (at least 3) + 1 form group for the ITEM property.
		assertTrue("Should have at least 4 children with ITEM", editor.getChildCount() >= 4);
	}

	/**
	 * Tests that a null ITEM property value is skipped (no group created).
	 */
	public void testNullItemPropertySkipped() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		// inner is null by default

		int childCountWithoutItem = new TestableConfigEditorControl(createTestContext(), config)
			.getChildCount();

		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		config.setInner(inner);

		int childCountWithItem = new TestableConfigEditorControl(createTestContext(), config)
			.getChildCount();

		assertEquals("Null ITEM should not add a child", childCountWithItem, childCountWithoutItem + 1);
	}

	/**
	 * Tests that a polymorphic ITEM property creates a {@link PolymorphicItemControl}.
	 */
	public void testPolymorphicPropertyCreatesTypeSelector() {
		PolymorphicTestConfig config = TypedConfiguration.newConfigItem(PolymorphicTestConfig.class);

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		// Should have children for: configuration-interface (PLAIN) + handler (polymorphic ITEM).
		boolean hasPolymorphicChild = false;
		for (ReactControl child : editor.getChildrenList()) {
			if (child instanceof PolymorphicItemControl) {
				hasPolymorphicChild = true;
			}
		}
		assertTrue("Should have a PolymorphicItemControl child", hasPolymorphicChild);
	}

	/**
	 * Tests that changing the type in a polymorphic control rebuilds the nested editor.
	 */
	public void testPolymorphicTypeChange() {
		PolymorphicTestConfig config = TypedConfiguration.newConfigItem(PolymorphicTestConfig.class);
		HandlerAConfig handlerA = TypedConfiguration.newConfigItem(HandlerAConfig.class);
		handlerA.setNameA("test");
		config.setHandler(handlerA);

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		PolymorphicItemControl polyControl = findPolymorphicControl(editor);
		assertNotNull("Should have PolymorphicItemControl", polyControl);
		assertNotNull("Should have nested editor for HandlerAConfig", polyControl.getNestedEditor());

		// Change type to the HandlerB implementation. The type selector uses index-based option
		// keys, so resolve HandlerB's key from the computed options.
		PropertyDescriptor handlerProp = config.descriptor().getProperty(PolymorphicTestConfig.HANDLER);
		java.util.List<Object> options =
			com.top_logic.layout.configedit.PolymorphicOptions.compute(config, handlerProp).options();
		int handlerBIndex = options.indexOf(HandlerB.class);
		assertTrue("HandlerB must be an available option", handlerBIndex >= 0);
		polyControl.getTypeModel()
			.setValue(com.top_logic.layout.configedit.PolymorphicOptions.keyFor(handlerBIndex));

		// Verify the config was updated.
		assertNotNull("Handler should be set", config.getHandler());
		assertEquals("Handler should be HandlerBConfig",
			HandlerBConfig.class, config.getHandler().descriptor().getConfigurationInterface());
		assertNotNull("Should have a new nested editor", polyControl.getNestedEditor());
	}

	/**
	 * Tests that a null polymorphic value shows only the type selector (no nested editor).
	 */
	public void testPolymorphicNullValue() {
		PolymorphicTestConfig config = TypedConfiguration.newConfigItem(PolymorphicTestConfig.class);
		// handler is null by default.

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		PolymorphicItemControl polyControl = findPolymorphicControl(editor);
		assertNotNull("Should have PolymorphicItemControl", polyControl);
		assertNull("Should have no nested editor for null value", polyControl.getNestedEditor());
	}

	/**
	 * Tests that the type options include all non-abstract specializations.
	 */
	public void testPolymorphicTypeOptions() {
		PolymorphicTestConfig config = TypedConfiguration.newConfigItem(PolymorphicTestConfig.class);
		PropertyDescriptor handlerProp = config.descriptor().getProperty(PolymorphicTestConfig.HANDLER);

		java.util.List<Object> options =
			com.top_logic.layout.configedit.PolymorphicOptions.compute(config, handlerProp).options();

		assertTrue("Options should contain HandlerA", options.contains(HandlerA.class));
		assertTrue("Options should contain HandlerB", options.contains(HandlerB.class));
	}

	private PolymorphicItemControl findPolymorphicControl(TestableConfigEditorControl editor) {
		for (ReactControl child : editor.getChildrenList()) {
			if (child instanceof PolymorphicItemControl) {
				return (PolymorphicItemControl) child;
			}
		}
		return null;
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the polymorphic type selector's class-label resolution.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigEditorControl.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
