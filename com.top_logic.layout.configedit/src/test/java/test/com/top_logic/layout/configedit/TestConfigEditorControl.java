/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.I18NConstants;
import com.top_logic.layout.configedit.PolymorphicItemControl;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;

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

		/** Property name for {@link #getIndex()}. */
		String INDEX = "index";

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

		/**
		 * A {@link PropertyKind#MAP} property, edited by the same
		 * {@link com.top_logic.layout.configedit.ConfigListEditorControl} as a keyed
		 * {@link ListItem} LIST or ARRAY property - differing only in the value's shape (a
		 * {@link Map}, keyed by each entry's {@link ListItem#getName()}).
		 */
		@Name(INDEX)
		@Key(ListItem.NAME)
		Map<String, ListItem> getIndex();
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

	/**
	 * Common polymorphic, keyed configuration for
	 * {@link TestConfigEditorControl#testPolymorphicKeyedEntryRendersKeyOnce()} - unlike
	 * {@link HandlerConfig}, this one declares a {@link Key}-annotated property once, inherited
	 * unchanged by both {@link KeyedHandlerAConfig} and {@link KeyedHandlerBConfig}: the ordinary
	 * shape of a polymorphic keyed collection, where an entry's actual type is a genuine subtype
	 * of the collection's declared element type.
	 */
	public interface KeyedHandlerConfig extends PolymorphicConfiguration<Handler> {

		/** Property name for {@link #getEntryKey()}. */
		String ENTRY_KEY = "entryKey";

		@Name(ENTRY_KEY)
		String getEntryKey();

		void setEntryKey(String value);
	}

	/** Concrete keyed handler config A - inherits {@link KeyedHandlerConfig#getEntryKey()} unchanged. */
	public interface KeyedHandlerAConfig extends KeyedHandlerConfig {

		/** Property name for {@link #getNameA()}. */
		String NAME_A = "nameA";

		@Name(NAME_A)
		String getNameA();

		void setNameA(String value);
	}

	/** Concrete keyed handler config B - inherits {@link KeyedHandlerConfig#getEntryKey()} unchanged. */
	public interface KeyedHandlerBConfig extends KeyedHandlerConfig {

		/** Property name for {@link #getValueB()}. */
		String VALUE_B = "valueB";

		@Name(VALUE_B)
		int getValueB();

		void setValueB(int value);
	}

	/** Concrete {@link Handler} implementation selected through {@link KeyedHandlerAConfig}. */
	public static class KeyedHandlerA implements Handler {
		/** Creates a {@link KeyedHandlerA} from configuration. */
		public KeyedHandlerA(InstantiationContext context, KeyedHandlerAConfig config) {
			// No state required for the test.
		}
	}

	/** Concrete {@link Handler} implementation selected through {@link KeyedHandlerBConfig}. */
	public static class KeyedHandlerB implements Handler {
		/** Creates a {@link KeyedHandlerB} from configuration. */
		public KeyedHandlerB(InstantiationContext context, KeyedHandlerBConfig config) {
			// No state required for the test.
		}
	}

	/**
	 * Test config with a LIST property that is both polymorphic (more than one
	 * {@link KeyedHandlerConfig} implementation) and keyed - the combination
	 * {@link ConfigListEditorControl#createElementGroup(ConfigurationItem, int, int, boolean)}
	 * must get right by hiding an entry's key using the entry's own
	 * {@link com.top_logic.basic.config.PropertyDescriptor}, not the collection's declared element
	 * type's.
	 */
	public interface KeyedPolymorphicTestConfig extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(KeyedHandlerConfig.ENTRY_KEY)
		java.util.List<KeyedHandlerConfig> getItems();
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

		TestableConfigEditorControl(ReactContext context, ConfigurationItem config,
				Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index) {
			super(context, config, hiddenProperties, skipTreeProperties, index);
		}

		TestableConfigEditorControl(ReactContext context, ConfigurationItem config,
				Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index,
				boolean editable) {
			super(context, config, hiddenProperties, skipTreeProperties, index, editable);
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
		protected ConfigEditorControl newEditor(ReactContext context, ConfigurationItem config,
				Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index,
				boolean editable) {
			return new TestableConfigEditorControl(context, config, hiddenProperties, skipTreeProperties, index,
				editable);
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

		TestableConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
				PropertyDescriptor property, ConfigFieldIndex index) {
			super(context, parentConfig, property, index);
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
	 * "\u25BC", or remove "\u2715" icon) inside the given element group, or {@code null} if no
	 * such button is rendered - a miss is a legitimate outcome for a collection that offers no
	 * such action, so this does not fail on it, the way {@link #findKeyFieldModel(ReactControl)}
	 * deliberately does not either.
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
	 * Finds the field model of the key field rendered directly by the given element group -
	 * reached the same way as {@link #findTypeFieldModel(ReactControl)}, via
	 * {@link ReactControl#scriptingChildren()} and {@link ReactControl#scriptingScalarState()}.
	 *
	 * <p>
	 * Unlike the "Type" selector, the key field carries no fixed label (its label is the key
	 * property's own, which varies by fixture), so it is identified by elimination among the
	 * group's direct children: not the "Type" selector, and not the nested
	 * {@link com.top_logic.layout.configedit.ConfigEditorControl} over the entry, which carries
	 * no {@code "label"} scripting state of its own. A header action button (move up/down,
	 * remove) also carries a non-{@code null} {@code "label"} and reaches this far, but is
	 * eliminated one step later: it has no nested {@link ReactControl} of its own, so its
	 * {@link ReactControl#scriptingChildren()} is empty and the inner loop below simply moves on.
	 * </p>
	 */
	/**
	 * The error text the chrome around an entry's key field shows - the state that actually
	 * reaches the browser, as opposed to the field model's own error the chrome is meant to
	 * mirror.
	 */
	private String keyFieldChromeError(ReactControl elementGroup) {
		for (ReactControl child : elementGroup.scriptingChildren()) {
			Object label = child.scriptingScalarState().get("label");
			if (label == null || "Type".equals(label)) {
				continue;
			}
			return (String) child.scriptingScalarState().get("error");
		}
		return null;
	}

	private FieldModel findKeyFieldModel(ReactControl elementGroup) {
		for (ReactControl child : elementGroup.scriptingChildren()) {
			Object label = child.scriptingScalarState().get("label");
			if (label == null || "Type".equals(label)) {
				continue;
			}
			for (ReactControl field : child.scriptingChildren()) {
				return (FieldModel) field.getModel();
			}
		}
		return null;
	}

	/**
	 * Counts the fields bound to {@code keyProperty} anywhere under the given control - whether
	 * rendered directly by the element group (the key field added by
	 * {@link ConfigListEditorControl}) or by the nested editor over the same entry.
	 *
	 * <p>
	 * Walks the full {@link ReactControl#scriptingChildren()} tree rather than just the direct
	 * children {@link #findKeyFieldModel(ReactControl)} looks at, because a duplicate rendering
	 * would come from the nested editor, several levels down.
	 * </p>
	 *
	 * <p>
	 * Compares by {@link PropertyDescriptor#identifier()}, not by reference: a polymorphic
	 * entry's own {@link com.top_logic.basic.config.ConfigurationDescriptor} hands out a distinct
	 * {@link PropertyDescriptor} instance for a property inherited unchanged from the collection's
	 * declared element type, so the two given here (one resolved against the entry, one against
	 * the declared type) never share identity - only the identifier, which is exactly what the
	 * framework's own consistency checks (e.g.
	 * {@code PropertyDescriptorImpl.ensureConsistentKeyProperty}) compare by for the same reason.
	 * </p>
	 */
	private int countKeyFields(ReactControl control, PropertyDescriptor keyProperty) {
		int count = control.getModel() instanceof ConfigFieldModel fieldModel
			&& fieldModel.getProperty().identifier() == keyProperty.identifier() ? 1 : 0;
		for (ReactControl child : control.scriptingChildren()) {
			count += countKeyFields(child, keyProperty);
		}
		return count;
	}

	/**
	 * The key of an entry that is already in the collection cannot be edited: changing it would
	 * silently re-index the collection under the new key. The classic editor renders the key
	 * field immutable for the same reason.
	 */
	public void testKeyFieldOfExistingEntryIsReadOnly() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		first.setName("Apple");
		config.getKeyedItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		FieldModel keyModel = findKeyFieldModel(elementGroups(editor).get(0));
		assertNotNull("The entry group must render the key field itself.", keyModel);
		assertFalse("The key of a committed entry must not be editable.", keyModel.isEditable());
	}

	/**
	 * The key field is rendered once, by the group - not a second time by the nested editor over
	 * the same entry.
	 */
	public void testKeyPropertyIsNotRenderedTwice() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		first.setName("Apple");
		config.getKeyedItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		assertEquals("The key must appear exactly once in the entry group.", 1,
			countKeyFields(elementGroups(editor).get(0), property.getKeyProperty()));
	}

	/**
	 * A LIST property without a key property must be entirely unaffected by the group's own key
	 * field: no key field is rendered, and the nested editor still shows the "name" property it
	 * always did (unlike {@link #testKeyPropertyIsNotRenderedTwice()}, nothing is hidden from it).
	 */
	public void testUnkeyedListRendersNoKeyField() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		first.setName("Apple");
		config.getPlainItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.PLAIN_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		assertNull("An unkeyed list's entry group must render no key field.",
			findKeyFieldModel(elementGroups(editor).get(0)));

		PropertyDescriptor nameProperty = first.descriptor().getProperty(ListItem.NAME);
		assertEquals("The nested editor must still render the entry's \"name\" property.", 1,
			countKeyFields(elementGroups(editor).get(0), nameProperty));
	}

	/**
	 * A collection that is both polymorphic and keyed - the combination this class's type
	 * selector and key handling both need to support at once - must still render its key field
	 * exactly once.
	 *
	 * <p>
	 * {@code entry}'s actual type ({@link KeyedHandlerAConfig}) is a genuine subtype of the
	 * collection's declared element type ({@link KeyedHandlerConfig}), so its own
	 * {@link com.top_logic.basic.config.PropertyDescriptor} for the inherited key property is a
	 * different instance from {@code property.getKeyProperty()}'s declared-type one - exactly
	 * the mismatch that let the key property escape being hidden from the nested editor when the
	 * hiding was checked by reference instead of by
	 * {@link com.top_logic.basic.config.PropertyDescriptor#identifier()}.
	 * </p>
	 */
	public void testPolymorphicKeyedEntryRendersKeyOnce() {
		KeyedPolymorphicTestConfig config = TypedConfiguration.newConfigItem(KeyedPolymorphicTestConfig.class);
		KeyedHandlerAConfig entry = TypedConfiguration.newConfigItem(KeyedHandlerAConfig.class);
		entry.setEntryKey("first");
		entry.setNameA("value");
		config.getItems().add(entry);

		PropertyDescriptor property = config.descriptor().getProperty(KeyedPolymorphicTestConfig.ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		assertEquals("The key must appear exactly once in a polymorphic, keyed entry's group.", 1,
			countKeyFields(elementGroups(editor).get(0), property.getKeyProperty()));
	}

	/**
	 * Picking a type for a still-pending entry actually changes that entry's type.
	 *
	 * <p>
	 * A pending entry is not in the edited collection, so it cannot be found there by index. Left
	 * unhandled, the type change was silently dropped: the select showed the new type while the
	 * entry kept the old one, and confirming took an entry of a type nobody had chosen.
	 * </p>
	 *
	 * <p>
	 * Switches to whichever option is <em>not</em> the one a new entry starts out as, rather than
	 * to a named type: the option order comes from the type index and is not guaranteed, so naming
	 * the target type would make this test pass for the wrong reason whenever a new entry happened
	 * to start out as that very type.
	 * </p>
	 */
	public void testChangingTheTypeOfAPendingEntryTakesEffect() {
		KeyedPolymorphicTestConfig config = TypedConfiguration.newConfigItem(KeyedPolymorphicTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(KeyedPolymorphicTestConfig.ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		FieldModel typeModel = findTypeFieldModel(elementGroups(editor).get(0));
		String startType = (String) typeModel.getValue();
		String otherType = otherTypeOption(config, property, startType);

		typeModel.setValue(otherType);
		nameAndConfirm(editor, 0, "first");

		List<KeyedHandlerConfig> items = config.getItems();
		assertEquals("The confirmed entry must be in the collection.", 1, items.size());
		assertEquals("The entry must carry the type that was picked while it was pending.",
			expectedInterface(config, property, otherType),
			items.get(0).descriptor().getConfigurationInterface());
		assertEquals("The key typed after the type change must survive it.",
			"first", items.get(0).getEntryKey());
	}

	/**
	 * What was already filled in survives a type change of a pending entry - the same carry-over a
	 * committed entry gets - and the type change on its own never commits the entry.
	 */
	public void testChangingAPendingEntrysTypeKeepsTheKeyAndStaysPending() {
		KeyedPolymorphicTestConfig config = TypedConfiguration.newConfigItem(KeyedPolymorphicTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(KeyedPolymorphicTestConfig.ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		findKeyFieldModel(elementGroups(editor).get(0)).setValue("typed before");
		FieldModel typeModel = findTypeFieldModel(elementGroups(editor).get(0));
		typeModel.setValue(otherTypeOption(config, property, (String) typeModel.getValue()));

		assertEquals("The key entered before the type change must be carried over.",
			"typed before", findKeyFieldModel(elementGroups(editor).get(0)).getValue());
		assertEquals("The entry must still be pending - a type change never commits it.", 0,
			config.getItems().size());
	}

	/** The option key of some type option other than the given one. */
	private String otherTypeOption(ConfigurationItem config, PropertyDescriptor property, String currentKey) {
		List<Object> options =
			com.top_logic.layout.configedit.PolymorphicOptions.compute(config, property).options();
		assertTrue("Needs more than one type option to switch between.", options.size() > 1);
		for (int n = 0; n < options.size(); n++) {
			String key = com.top_logic.layout.configedit.PolymorphicOptions.keyFor(n);
			if (!key.equals(currentKey)) {
				return key;
			}
		}
		fail("No option other than the current one.");
		return null;
	}

	/** The configuration interface an entry of the given type option has. */
	private Class<?> expectedInterface(ConfigurationItem config, PropertyDescriptor property, String optionKey) {
		com.top_logic.layout.configedit.PolymorphicOptions.Choices choices =
			com.top_logic.layout.configedit.PolymorphicOptions.compute(config, property);
		List<Object> options = choices.options();
		Object option = com.top_logic.layout.configedit.PolymorphicOptions.optionForKey(options, optionKey);
		ConfigurationItem sample = (ConfigurationItem) choices.mapping().toSelection(option);
		return sample.descriptor().getConfigurationInterface();
	}

	/**
	 * Confirms the pending entry rendered as the given element group, by pressing its Confirm
	 * button - the only thing that moves a pending entry into the collection.
	 */
	private void confirmPending(TestableConfigListEditorControl editor, int groupIndex) {
		ReactButtonControl confirm = findHeaderButton(elementGroups(editor).get(groupIndex), "\u2713");
		assertNotNull("A pending entry must offer a Confirm button.", confirm);
		click(confirm);
	}

	/** Types a key into the pending entry rendered as the given element group, then confirms it. */
	private void nameAndConfirm(TestableConfigListEditorControl editor, int groupIndex, String key) {
		findKeyFieldModel(elementGroups(editor).get(groupIndex)).setValue(key);
		confirmPending(editor, groupIndex);
	}

	/**
	 * Adding a second element to a keyed list always succeeds, even while every existing element
	 * already has a real key: the "+" never inserts directly any more, so there is nothing left
	 * to collide with the existing "Apple" key. Naming and confirming the new (pending) entry then
	 * adds it.
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
		assertTrue("Adding a second element must never be refused", result.isSuccess());

		nameAndConfirm(editor, 1, "Banana");

		assertEquals("The named second element should have been added", 2, config.getKeyedItems().size());
	}

	/**
	 * Pressing "+" on a keyed collection creates an entry that is not yet in the collection: it
	 * has no key, and a keyed collection cannot hold it.
	 */
	public void testAddingToKeyedCollectionCreatesAPendingEntry() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);

		assertEquals("The pending entry must not be in the collection yet.", 0,
			config.getKeyedItems().size());
		assertEquals("The pending entry must be rendered.", 1, elementGroups(editor).size());
	}

	/**
	 * A key that merely looks usable does not commit the entry - only confirming does.
	 *
	 * <p>
	 * The key field reports what has been typed so far: {@code TLTextInput} pushes its value 300 ms
	 * after the last keystroke, and again when the field loses focus, so every pause in typing and
	 * every trip away from the field to fetch the value from elsewhere arrives here as a value
	 * change. Committing on such a change would take "fir" for the finished key and fix the field
	 * on it.
	 * </p>
	 */
	public void testTypingAKeyDoesNotCommitTheEntry() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);
		clickAddButton(editor);

		FieldModel keyModel = findKeyFieldModel(elementGroups(editor).get(0));
		keyModel.setValue("fir");
		keyModel.setValue("first");

		assertEquals("An entry must not be committed under a half-typed key.", 0,
			config.getKeyedItems().size());
		assertTrue("The key field must stay editable while the entry is pending.",
			findKeyFieldModel(elementGroups(editor).get(0)).isEditable());
	}

	/** Confirming the named pending entry moves it into the collection. */
	public void testConfirmingTheNamedPendingEntryCommitsIt() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);
		clickAddButton(editor);

		nameAndConfirm(editor, 0, "first");

		List<ListItem> items = config.getKeyedItems();
		assertEquals("The confirmed entry must be in the collection.", 1, items.size());
		assertEquals("first", items.get(0).getName());
	}

	/**
	 * Confirming an entry that has no key at all reports that at the key field, rather than
	 * quietly doing nothing - the user asked for the entry to be taken and has to be told why it
	 * was not.
	 */
	public void testConfirmingWithoutAKeyReportsItAtTheField() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);
		clickAddButton(editor);

		confirmPending(editor, 0);

		assertEquals("An entry without a key must stay out of the collection.", 0,
			config.getKeyedItems().size());
		assertEquals("The entry must stay pending.", 1, elementGroups(editor).size());
		ResKey error = findKeyFieldModel(elementGroups(editor).get(0)).getError();
		assertNotNull("The missing key must be reported at the key field.", error);
		assertEquals(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY, error.plain());
	}

	/**
	 * A key that is already taken leaves the entry pending and reports the clash at the field,
	 * rather than letting TypedConfiguration reject it with a technical exception.
	 *
	 * <p>
	 * Asserts the reported {@link ResKey}'s identity and its arguments' order, not just that some
	 * error is present: a mere {@code assertNotNull} cannot see the property label and the
	 * clashing value landing in the wrong slots, which is exactly the mistake the message template
	 * ({@code "An entry with {0} \"{1}\" already exists."}) and the
	 * {@link I18NConstants#ERROR_DUPLICATE_KEY__PROPERTY_VALUE} constant's own name agree on: the
	 * property label first, the clashing value second. Resolved text is not compared - nothing in
	 * this module's tests resolves a {@link ResKey} through {@link com.top_logic.util.Resources}
	 * to a rendered string, and setting that up for this one assertion would be inventing
	 * machinery the module does not otherwise need.
	 * </p>
	 */
	public void testDuplicateKeyLeavesTheEntryPendingWithAnError() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem existing = TypedConfiguration.newConfigItem(ListItem.class);
		existing.setName("first");
		config.getKeyedItems().add(existing);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);
		clickAddButton(editor);

		nameAndConfirm(editor, 1, "first");

		assertEquals("The clashing entry must stay out of the collection.", 1,
			config.getKeyedItems().size());
		ResKey error = findKeyFieldModel(elementGroups(editor).get(1)).getError();
		assertNotNull("The clash must be reported at the key field.", error);
		assertEquals("The clash must carry the duplicate-key message.",
			I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE, error.plain());
		assertEquals(
			"The property label must be the first argument and the clashing value the second - "
				+ "the order the message template and the constant's own name agree on.",
			Arrays.asList(Labels.propertyLabel(property.getKeyProperty(), false), "first"),
			Arrays.asList(error.arguments()));
	}

	/**
	 * A second entry can be started while the first is still unnamed - the limitation the old
	 * add-guard imposed is gone. Walks the fuller lifecycle: both are pending at once and the
	 * collection holds neither; confirming the first commits only it, leaving the second still
	 * pending beside it; confirming the second afterwards commits it too, and both land in the
	 * collection in commit order.
	 */
	public void testASecondEntryCanBeStartedBeforeTheFirstIsNamed() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		clickAddButton(editor);

		assertEquals("Two pending entries must be able to coexist.", 2, elementGroups(editor).size());
		assertEquals("Neither pending entry is in the collection yet.", 0,
			config.getKeyedItems().size());

		nameAndConfirm(editor, 0, "first");

		assertEquals("Confirming the first entry must commit only it.", 1, config.getKeyedItems().size());
		assertEquals("The second entry is still pending beside the newly committed first.", 2,
			elementGroups(editor).size());

		nameAndConfirm(editor, 1, "second");

		List<ListItem> items = config.getKeyedItems();
		assertEquals("The confirmed second entry must also have been committed.", 2, items.size());
		assertEquals("The first-committed entry must come first.", "first", items.get(0).getName());
		assertEquals("The second-committed entry must come second.", "second", items.get(1).getName());
	}

	/**
	 * The second pending entry can be named and confirmed before the first one is - the entries
	 * are addressed by object, not by their position among the pending ones.
	 */
	public void testTheSecondPendingEntryCanBeConfirmedFirst() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		clickAddButton(editor);

		nameAndConfirm(editor, 1, "second");

		List<ListItem> items = config.getKeyedItems();
		assertEquals("Exactly the confirmed entry must have been committed.", 1, items.size());
		assertEquals("second", items.get(0).getName());
		assertEquals("The other entry must still be pending beside the committed one.", 2,
			elementGroups(editor).size());
	}

	/**
	 * Discarding one pending entry through its own Remove action leaves any other pending entry
	 * untouched - it must remove exactly the entry whose Remove button was pressed, not every
	 * pending entry, and it must not touch the collection, which it was never part of.
	 */
	public void testDiscardingOnePendingEntryLeavesTheOtherPending() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		clickAddButton(editor);

		click(findHeaderButton(elementGroups(editor).get(0), "\u2715"));

		assertEquals("Discarding one pending entry must leave exactly the other one pending.", 1,
			elementGroups(editor).size());
		assertEquals("The collection must stay empty - discarding a pending entry never touches it, "
			+ "and neither entry was ever named.", 0, config.getKeyedItems().size());
	}

	/**
	 * Two pending entries may carry the same key while both are still being filled in - neither
	 * has claimed it yet. Confirming decides: the first confirmed takes the key, and confirming
	 * the second then clashes with what is by then a committed entry, reported at its own key
	 * field - the same duplicate-key path as colliding with any other pre-existing entry, not a
	 * special case for two pending entries racing each other.
	 */
	public void testSecondPendingEntryConfirmedWithTheSameKeyClashesWithTheCommittedFirst() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		clickAddButton(editor);

		findKeyFieldModel(elementGroups(editor).get(0)).setValue("dup");
		findKeyFieldModel(elementGroups(editor).get(1)).setValue("dup");

		assertEquals("Two pending entries may hold the same key while neither is confirmed.", 0,
			config.getKeyedItems().size());

		confirmPending(editor, 0);

		assertEquals("The entry confirmed first must have committed.", 1,
			config.getKeyedItems().size());

		confirmPending(editor, 1);

		assertEquals("The second entry must not also have been committed under the same key.", 1,
			config.getKeyedItems().size());
		assertNotNull("The clash must be reported at the second entry's own key field.",
			findKeyFieldModel(elementGroups(editor).get(1)).getError());
	}

	/**
	 * Typing a key an existing entry already holds is complained about at the field right away,
	 * without the entry having to be confirmed first.
	 */
	public void testTypingATakenKeyIsReportedAtTheFieldImmediately() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem first = TypedConfiguration.newConfigItem(ListItem.class);
		first.setName("Apple");
		config.getKeyedItems().add(first);

		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		FieldModel keyField = findKeyFieldModel(elementGroups(editor).get(1));
		keyField.setValue("Apple");

		assertEquals("The taken key must be complained about while it is typed, not on confirmation.",
			I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE, keyField.getError().plain());

		keyField.setValue("Banana");

		assertNull("Typing a free key clears the complaint.", keyField.getError());
	}

	/**
	 * Two pending entries given the same key are told about each other while typing, at both
	 * fields - neither of the two is the one at fault.
	 */
	public void testTwoPendingEntriesTypingTheSameKeyAreBothTold() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		clickAddButton(editor);
		findKeyFieldModel(elementGroups(editor).get(0)).setValue("dup");
		findKeyFieldModel(elementGroups(editor).get(1)).setValue("dup");

		assertEquals(I18NConstants.ERROR_DUPLICATE_PENDING_KEY__PROPERTY_VALUE,
			findKeyFieldModel(elementGroups(editor).get(0)).getError().plain());
		assertEquals(I18NConstants.ERROR_DUPLICATE_PENDING_KEY__PROPERTY_VALUE,
			findKeyFieldModel(elementGroups(editor).get(1)).getError().plain());

		confirmPending(editor, 0);

		assertEquals("The entry left behind now really does clash with a committed entry.",
			I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE,
			findKeyFieldModel(elementGroups(editor).get(1)).getError().plain());
	}

	/**
	 * The order the user actually works in: name the first new entry, only then add the second and
	 * name it too - so the second entry's group is built by a rebuild that happens while the first
	 * one already carries the key.
	 */
	public void testAddingTheSecondEntryAfterNamingTheFirstStillReportsTheClash() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		clickAddButton(editor);
		findKeyFieldModel(elementGroups(editor).get(0)).setValue("eee");

		clickAddButton(editor);
		findKeyFieldModel(elementGroups(editor).get(1)).setValue("eee");

		assertNotNull("The entry named second must be told the key is already being used.",
			findKeyFieldModel(elementGroups(editor).get(1)).getError());
		assertNotNull("The entry named first must be told as well.",
			findKeyFieldModel(elementGroups(editor).get(0)).getError());

		// The field models carry the complaint; what decides whether the user ever sees it is
		// whether the chrome around each field mirrored it into the state sent to the browser.
		assertNotNull("The complaint must have reached the second field's chrome.",
			keyFieldChromeError(elementGroups(editor).get(1)));
		assertNotNull("...and the first field's chrome.",
			keyFieldChromeError(elementGroups(editor).get(0)));
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

	/** A map property is rendered by the collection editor. */
	public void testMapPropertyIsRendered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		assertTrue("A MAP property must be rendered by the collection editor.",
			rendersProperty(config, TestConfig.INDEX));
	}

	/** Its entries are shown, one group each. */
	public void testMapEntriesAreRendered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.getIndex().put("Apple", newListItem("Apple"));
		config.getIndex().put("Banana", newListItem("Banana"));

		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.INDEX);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		assertEquals("Both map entries must be rendered, one group each.", 2, elementGroups(editor).size());
	}

	/** Confirming a named pending entry puts it into the map under its key. */
	public void testConfirmingThePendingEntryPutsItIntoTheMap() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.INDEX);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);
		clickAddButton(editor);

		nameAndConfirm(editor, 0, "alpha");

		Map<?, ?> index = (Map<?, ?>) config.value(property);
		assertEquals(1, index.size());
		assertTrue("The map must be keyed by the entry's key value.", index.containsKey("alpha"));
	}

	/**
	 * A map's entries can be reordered, even though a MAP property is not
	 * {@link PropertyDescriptor#isOrdered() ordered}.
	 *
	 * <p>
	 * The flag only says a {@link Map} has no positional index of its own; the value a MAP
	 * property holds is backed by a {@link java.util.LinkedHashMap}, so it does have an iteration
	 * order - the one the editor renders and a configuration is written out in.
	 * </p>
	 */
	public void testMapEntriesCanBeReordered() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.getIndex().put("Apple", newListItem("Apple"));
		config.getIndex().put("Banana", newListItem("Banana"));

		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.INDEX);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		ReactButtonControl moveUp = findHeaderButton(elementGroups(editor).get(1), "▲");
		assertNotNull("A map's iteration order is stable, so its entries can be reordered.", moveUp);
		click(moveUp);

		Map<?, ?> index = (Map<?, ?>) config.value(property);
		assertEquals("Reordering must not lose or add entries.", 2, index.size());
		assertEquals("The moved entry must come first now.",
			Arrays.asList("Banana", "Apple"), new ArrayList<>(index.keySet()));
	}

	/** The first entry of a map cannot be moved further up. */
	public void testFirstMapEntryCannotMoveUp() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.getIndex().put("Apple", newListItem("Apple"));
		config.getIndex().put("Banana", newListItem("Banana"));

		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.INDEX);
		TestableConfigListEditorControl editor =
			new TestableConfigListEditorControl(createTestContext(), config, property);

		ReactButtonControl moveUp = findHeaderButton(elementGroups(editor).get(0), "▲");
		assertNotNull("A map entry must offer the button at all.", moveUp);
		assertEquals("The first entry has nowhere to move up to.",
			Boolean.TRUE, moveUp.scriptingScalarState().get("disabled"));
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
	 * The editor reports every field it builds to the index it was given, including the fields of a
	 * nested item - which is where a violation would otherwise have nowhere to go.
	 */
	public void testTheEditorFillsTheFieldIndex() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		InnerConfig inner = TypedConfiguration.newConfigItem(InnerConfig.class);
		inner.setTitle("t");
		config.setInner(inner);
		ConfigFieldIndex index = new ConfigFieldIndex();

		new TestableConfigEditorControl(createTestContext(), config, Collections.emptySet(), false, index);

		assertNotNull("The top-level field must be indexed.",
			index.lookup(config, config.descriptor().getProperty(TestConfig.COUNT)));
		assertNotNull("A nested item's field must be indexed under that item.",
			index.lookup(inner, inner.descriptor().getProperty(InnerConfig.TITLE)));
	}

	/** A field of a collection entry is indexed under that entry. */
	public void testTheIndexReachesIntoCollectionEntries() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem entry = TypedConfiguration.newConfigItem(ListItem.class);
		entry.setName("a");
		config.getPlainItems().add(entry);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.PLAIN_ITEMS);
		ConfigFieldIndex index = new ConfigFieldIndex();

		new TestableConfigListEditorControl(createTestContext(), config, property, index);

		assertNotNull("The entry's own field must be indexed under the entry.",
			index.lookup(entry, entry.descriptor().getProperty(ListItem.NAME)));
	}

	/**
	 * A violation on a keyed collection entry's key must land on that entry's own key field - the
	 * field {@link ConfigListEditorControl#createKeyField} builds and renders itself, separately
	 * from the nested editor over the entry's other properties.
	 */
	public void testTheIndexReachesIntoAnEntrysKeyField() {
		ListTestConfig config = TypedConfiguration.newConfigItem(ListTestConfig.class);
		ListItem entry = TypedConfiguration.newConfigItem(ListItem.class);
		entry.setName("a");
		config.getKeyedItems().add(entry);
		PropertyDescriptor property = config.descriptor().getProperty(ListTestConfig.KEYED_ITEMS);
		ConfigFieldIndex index = new ConfigFieldIndex();

		new TestableConfigListEditorControl(createTestContext(), config, property, index);

		assertNotNull("The entry's key field must be indexed under the entry.",
			index.lookup(entry, entry.descriptor().getProperty(ListItem.NAME)));
	}

	/** Without an index nothing is collected and nothing breaks - every existing caller's case. */
	public void testTheEditorWorksWithoutAnIndex() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		TestableConfigEditorControl editor = new TestableConfigEditorControl(createTestContext(), config);

		assertFalse("The editor must still render its fields.", editor.getChildrenList().isEmpty());
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
