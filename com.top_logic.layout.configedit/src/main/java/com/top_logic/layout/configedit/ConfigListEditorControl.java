/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ReactControl} that renders a full editor for a LIST or an ARRAY property of a
 * {@link ConfigurationItem} - the same sequence-of-elements editor for both, differing only in the
 * value's shape.
 *
 * <p>
 * Each list element is rendered as a collapsible {@link ReactFormGroupControl} with action buttons
 * (Move Up, Move Down, Remove) in the header and a nested {@link ConfigEditorControl} for the
 * element's properties. An Add button at the bottom creates new elements.
 * </p>
 *
 * <p>
 * Group labels are dynamic: a
 * {@link com.top_logic.basic.config.ConfigurationListener ConfigurationListener} on the title
 * property updates the header when the user edits the identifying value.
 * </p>
 */
public class ConfigListEditorControl extends ReactFormLayoutControl {

	private static final String PLACEHOLDER_CSS = "tlText--placeholder";

	private record Label(String text, boolean placeholder) {
	}

	private final ReactContext _context;

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	private final PolymorphicOptions.Choices _choices;

	private final List<ListenerRegistration> _listeners = new ArrayList<>();

	private record ListenerRegistration(ConfigurationItem item, PropertyDescriptor property,
			ConfigurationListener listener) {
	}

	/**
	 * Creates a {@link ConfigListEditorControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param parentConfig
	 *        The parent configuration item owning the LIST property.
	 * @param property
	 *        The LIST property descriptor.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
			PropertyDescriptor property) {
		super(context);
		_context = context;
		_parentConfig = parentConfig;
		_property = property;
		_choices = PolymorphicOptions.compute(parentConfig, property);

		rebuild(null);
	}

	@Override
	protected void onCleanup() {
		removeListeners();
		super.onCleanup();
	}

	private void removeListeners() {
		for (ListenerRegistration reg : _listeners) {
			reg.item().removeConfigurationListener(reg.property(), reg.listener());
		}
		_listeners.clear();
	}

	/**
	 * The elements currently held by the edited property.
	 *
	 * <p>
	 * For a {@link PropertyKind#LIST} property this is the configuration's own live list, so a
	 * mutation of it takes effect directly - which is what lets TypedConfiguration reject a
	 * duplicate key at the moment of the change. For a {@link PropertyKind#ARRAY} property it is a
	 * detached copy that only reaches the configuration through {@link #storeElements(List)}.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private List<ConfigurationItem> elements() {
		Object value = _parentConfig.value(_property);
		if (_property.kind() == PropertyKind.ARRAY) {
			return value == null ? new ArrayList<>()
				: new ArrayList<>((List<ConfigurationItem>) PropertyDescriptorImpl.arrayAsList(value));
		}
		return (List<ConfigurationItem>) value;
	}

	/**
	 * Writes back the elements obtained from {@link #elements()}, for a property shape that
	 * cannot be mutated in place.
	 */
	private void storeElements(List<ConfigurationItem> elements) {
		if (_property.kind() == PropertyKind.ARRAY) {
			_parentConfig.update(_property, PropertyDescriptorImpl.listAsArray(_property, elements));
		}
		// A LIST property was mutated in place and needs no write-back.
	}

	/**
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * @param expandItem
	 *        Element whose group should be rendered expanded; all others stay collapsed. May be
	 *        {@code null}.
	 */
	private void rebuild(ConfigurationItem expandItem) {
		removeListeners();

		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();

		List<ConfigurationItem> items = elements();
		if (items != null) {
			for (int i = 0; i < items.size(); i++) {
				ConfigurationItem item = items.get(i);
				addChild(createElementGroup(item, i, items.size(), item == expandItem));
			}
		}

		// Add button at the bottom.
		ReactButtonControl addButton = new ReactButtonControl(_context, "+ " + Labels.propertyLabel(_property, false),
			ctx -> {
				addElement();
				return HandlerResult.DEFAULT_RESULT;
			});
		addChild(addButton);

		putState("children", getChildren());
	}

	private ReactFormGroupControl createElementGroup(ConfigurationItem item, int index, int listSize, boolean expanded) {
		Label label = resolveElementLabel(item);

		// Action buttons: Move Up, Move Down, Remove.
		ReactButtonControl moveUpButton = new ReactButtonControl(_context, "\u25B2", ctx -> {
			moveUp(indexOf(item));
			return HandlerResult.DEFAULT_RESULT;
		});
		moveUpButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		moveUpButton.setDisabled(index == 0);

		ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
			moveDown(indexOf(item));
			return HandlerResult.DEFAULT_RESULT;
		});
		moveDownButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		moveDownButton.setDisabled(index == listSize - 1);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			int currentIndex = indexOf(item);
			if (currentIndex >= 0) {
				removeElement(currentIndex);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		List<ReactControl> headerActions = List.of(moveUpButton, moveDownButton, removeButton);

		List<ReactControl> bodyChildren = new ArrayList<>();
		boolean polymorphic = _choices.hasOptions();
		if (polymorphic && _choices.options().size() > 1) {
			bodyChildren.add(createTypeSelector(item));
		}
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		if (keyProperty != null) {
			bodyChildren.add(createKeyField(item, false));
		}
		if (!polymorphic || isTypeSelected(item)) {
			bodyChildren.add(new ConfigEditorControl(_context, item,
				keyProperty == null ? Collections.emptySet() : Collections.singleton(keyProperty)));
		}
		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, !expanded, "subtle", true,
			headerActions, bodyChildren);
		group.setHeader(createHeaderControl(label));

		// Register dynamic label update on the title property.
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		if (titleProp != null) {
			ConfigurationListener listener = change -> {
				group.setHeader(createHeaderControl(resolveElementLabel(item)));
			};
			item.addConfigurationListener(titleProp, listener);
			_listeners.add(new ListenerRegistration(item, titleProp, listener));
		}

		return group;
	}

	/**
	 * The field for an entry's key property, rendered by this control rather than by the nested
	 * editor over the entry.
	 *
	 * <p>
	 * The key decides where the entry sits in the collection, so it is editable only while the
	 * entry is still pending - once it is in the collection, changing it would re-index the
	 * collection under a new key. The classic declarative form renders it immutable for the same
	 * reason.
	 * </p>
	 *
	 * @param entry
	 *        The entry whose key is edited.
	 * @param editable
	 *        Whether the key may still be changed, i.e. whether the entry is pending.
	 */
	private ReactControl createKeyField(ConfigurationItem entry, boolean editable) {
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(entry, keyProperty);
		model.setEditable(editable);
		ReactControl input = ConfigControlService.getInstance().createControl(_context, model);
		return new ReactFormFieldChromeControl(_context, Labels.propertyLabel(keyProperty, false),
			model.isMandatory(), false, null, null, null, false, true, input);
	}

	private ReactTextControl createHeaderControl(Label label) {
		return new ReactTextControl(_context, label.text(), label.placeholder() ? PLACEHOLDER_CSS : null);
	}

	private ReactFormFieldChromeControl createTypeSelector(ConfigurationItem item) {
		List<Object> rawOptions = _choices.options();
		List<String> keys = new ArrayList<>(rawOptions.size());
		for (int i = 0; i < rawOptions.size(); i++) {
			keys.add(PolymorphicOptions.keyFor(i));
		}
		String currentKey = PolymorphicOptions.keyForItem(rawOptions, _choices.mapping(), item);
		SimpleSelectFieldModel typeModel = new SimpleSelectFieldModel(currentKey, keys, false);
		typeModel.setMandatory(true);
		typeModel.setNullable(false);

		LabelProvider labelProvider = PolymorphicOptions.indexLabelProvider(rawOptions);

		typeModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				onTypeChanged(item, PolymorphicOptions.optionForKey(rawOptions, (String) newValue));
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

		ReactSelectFormFieldControl typeSelect =
			new ReactSelectFormFieldControl(_context, typeModel, labelProvider);
		return new ReactFormFieldChromeControl(_context, "Type", typeSelect);
	}

	private void onTypeChanged(ConfigurationItem oldItem, Object selected) {
		if (selected == null) {
			return;
		}
		List<ConfigurationItem> items = elements();
		int index = items.indexOf(oldItem);
		if (index < 0) {
			return;
		}
		ConfigurationItem replacement = (ConfigurationItem) _choices.mapping().toSelection(selected);
		ConfigCopier.copyContent(new DefaultInstantiationContext(ConfigListEditorControl.class),
			oldItem, replacement, true);
		items.set(index, replacement);
		storeElements(items);
		rebuild(replacement);
	}

	private boolean isTypeSelected(ConfigurationItem item) {
		return _choices.mapping() != null
			&& _choices.mapping().asOption(_choices.options(), item) != null;
	}

	private int indexOf(ConfigurationItem item) {
		List<ConfigurationItem> items = elements();
		return items != null ? items.indexOf(item) : -1;
	}

	// --- Operations ---

	/**
	 * Adds a new element with default values to the end of the list.
	 */
	private void addElement() {
		List<ConfigurationItem> items = elements();
		ConfigurationItem newItem;
		if (_choices.hasOptions()) {
			newItem = (ConfigurationItem) _choices.mapping().toSelection(_choices.options().get(0));
		} else {
			newItem = TypedConfiguration.newConfigItem(resolveNewElementType());
		}
		checkKeyAvailable(items, newItem);
		items.add(newItem);
		storeElements(items);
		rebuild(newItem);
	}

	/**
	 * Ensures that adding the given element will not collide with an existing element's key.
	 *
	 * <p>
	 * A keyed LIST property ({@link PropertyDescriptor#getKeyProperty()}) is indexed by the key
	 * property's value; {@link TypedConfiguration} refuses two elements with an equal key with a
	 * technical {@link IllegalArgumentException}. Both a freshly constructed element (the
	 * {@code else} branch above) and a {@code Class}-valued polymorphic option (see
	 * {@link com.top_logic.layout.form.values.ItemOptionMapping} /
	 * {@link com.top_logic.layout.form.values.ImplOptionMapping}) have their key property unset,
	 * i.e. they evaluate to the same "empty" key as any other unset element - so adding a second
	 * such element while an existing one is still unset would trigger exactly that collision. An
	 * option-provided element that already carries a real key (e.g. a domain-specific
	 * {@code @Options} function returning pre-filled templates via an identity mapping) is not
	 * affected: this check only ever fires for a {@code newItem} whose own key is still unset.
	 * </p>
	 *
	 * @param items
	 *        The list's current elements.
	 * @param newItem
	 *        The element about to be added.
	 *
	 * @throws TopLogicException
	 *         If {@code newItem}'s key is unset and some existing element's key is unset, too.
	 */
	private void checkKeyAvailable(List<ConfigurationItem> items, ConfigurationItem newItem) {
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		if (keyProperty == null || !isKeyUnset(newItem, keyProperty)) {
			return;
		}
		for (ConfigurationItem existing : items) {
			if (isKeyUnset(existing, keyProperty)) {
				throw new TopLogicException(I18NConstants.ERROR_LIST_ELEMENT_KEY_MISSING__PROPERTY
					.fill(Labels.propertyLabel(keyProperty, false)));
			}
		}
	}

	/**
	 * Whether the given element's key property currently evaluates to the same "no value entered
	 * yet" value that {@link #resolveElementLabel(ConfigurationItem)} already treats as "no title
	 * entered yet": {@code null} or an empty {@link String}.
	 *
	 * <p>
	 * Deliberately not {@link ConfigurationItem#valueSet(PropertyDescriptor)}: that flag only
	 * records whether a setter was ever called, not whether the value it left behind is actually
	 * meaningful. A name typed and then deleted again leaves {@code valueSet} {@code true} while
	 * the value is back to the empty string - the same value an element whose name was never
	 * touched carries - so both would still collide in the same way when
	 * {@link com.top_logic.basic.config.PropertyDescriptorImpl#checkCorrectListValues} compares the
	 * actual key values.
	 * </p>
	 */
	private boolean isKeyUnset(ConfigurationItem item, PropertyDescriptor keyProperty) {
		Object value = item.value(keyProperty);
		return value == null || (value instanceof String s && s.isEmpty());
	}

	/**
	 * Removes the element at the given index.
	 */
	private void removeElement(int index) {
		List<ConfigurationItem> items = elements();
		if (items != null && index >= 0 && index < items.size()) {
			items.remove(index);
			storeElements(items);
			rebuild(null);
		}
	}

	/**
	 * Moves the element at the given index one position up.
	 */
	private void moveUp(int index) {
		List<ConfigurationItem> items = elements();
		if (items != null && index > 0 && index < items.size()) {
			ConfigurationItem item = items.remove(index);
			items.add(index - 1, item);
			storeElements(items);
			rebuild(null);
		}
	}

	/**
	 * Moves the element at the given index one position down.
	 */
	private void moveDown(int index) {
		List<ConfigurationItem> items = elements();
		if (items != null && index >= 0 && index < items.size() - 1) {
			ConfigurationItem item = items.remove(index);
			items.add(index + 1, item);
			storeElements(items);
			rebuild(null);
		}
	}

	// --- Label resolution ---

	/**
	 * Resolves the display label for a list element.
	 *
	 * <p>
	 * Uses the title property value if available and non-empty. Otherwise marks the label as a
	 * placeholder showing the type name plus an "(empty)" hint.
	 * </p>
	 */
	private Label resolveElementLabel(ConfigurationItem item) {
		String typeName = ConfigTagName.of(item);
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		if (titleProp == null) {
			return new Label(typeName, false);
		}
		Object value = item.value(titleProp);
		if (value instanceof String s && !s.isEmpty()) {
			return new Label(s, false);
		}
		String label = Resources.getInstance()
			.getString(I18NConstants.LIST_ELEMENT_EMPTY_TITLE__TYPE.fill(typeName));
		return new Label(label, true);
	}

	/**
	 * Resolves the title property for a list element.
	 *
	 * <p>
	 * Resolution order: {@link TitleProperty} on the LIST property, {@link TitleProperty} on the
	 * element type, key property of the LIST property, common names ("name", "id").
	 * </p>
	 */
	private PropertyDescriptor resolveTitleProperty(ConfigurationItem item) {
		// 1. @TitleProperty on the LIST property itself.
		TitleProperty titleOnList = _property.getAnnotation(TitleProperty.class);
		if (titleOnList != null && !titleOnList.name().isEmpty()) {
			PropertyDescriptor prop = item.descriptor().getProperty(titleOnList.name());
			if (prop != null) {
				return prop;
			}
		}

		// 2. @TitleProperty on the element type.
		TitleProperty titleOnType = item.descriptor().getConfigurationInterface().getAnnotation(TitleProperty.class);
		if (titleOnType != null && !titleOnType.name().isEmpty()) {
			PropertyDescriptor prop = item.descriptor().getProperty(titleOnType.name());
			if (prop != null) {
				return prop;
			}
		}

		// 3. Key property.
		PropertyDescriptor keyProp = _property.getKeyProperty();
		if (keyProp != null) {
			return keyProp;
		}

		// 4. Common names.
		for (String name : new String[] { "name", "id" }) {
			PropertyDescriptor prop = item.descriptor().getProperty(name);
			if (prop != null && prop.kind() == PropertyKind.PLAIN) {
				return prop;
			}
		}

		return null;
	}

	/**
	 * Resolves the configuration interface to instantiate for new list elements.
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends ConfigurationItem> resolveNewElementType() {
		return (Class<? extends ConfigurationItem>) _property.getDefaultDescriptor().getConfigurationInterface();
	}
}
