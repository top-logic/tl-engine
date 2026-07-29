/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
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
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ReactControl} that renders a full editor for a LIST property of a
 * {@link ConfigurationItem}.
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

	private final ConfigChildren _children;

	private final PolymorphicOptions.Choices _choices;

	private final List<ListenerRegistration> _listeners = new ArrayList<>();

	/** Menu offering the element types, created on first use. */
	private ReactMenuControl _typeMenu;

	/** The anchor the {@link #_typeMenu} is positioned at. */
	private String _addButtonId;

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
		_children = ConfigChildren.create(parentConfig, property);
		_choices = _children.allowedTypes();

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
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * @param expandItem
	 *        Element whose group should be rendered expanded; all others stay collapsed. May be
	 *        {@code null}.
	 */
	@SuppressWarnings("unchecked")
	private void rebuild(ConfigurationItem expandItem) {
		removeListeners();

		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();

		// The type menu is one of the disposed children; a later choice creates a fresh one.
		_typeMenu = null;

		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null) {
			for (int i = 0; i < items.size(); i++) {
				ConfigurationItem item = items.get(i);
				addChild(createElementGroup(item, i, items.size(), item == expandItem));
			}
		}

		// Add button at the bottom. When the property accepts several element types, the button opens
		// a menu to choose from instead of silently picking one.
		ReactButtonControl addButton = new ReactButtonControl(_context, "+ " + Labels.propertyLabel(_property, false),
			ctx -> {
				ConfigTypeChoice types = ConfigTypeChoice.of(_children);
				if (types.isUnique()) {
					addElement(types.single());
				} else {
					openTypeMenu(types);
				}
				return HandlerResult.DEFAULT_RESULT;
			});
		addChild(addButton);
		_addButtonId = addButton.getID();

		putState("children", getChildren());
	}

	private ReactFormGroupControl createElementGroup(ConfigurationItem item, int index, int listSize, boolean expanded) {
		Label label = resolveElementLabel(item);

		// Action buttons: Move Up, Move Down, Remove.
		ReactButtonControl moveUpButton = new ReactButtonControl(_context, "\u25B2", ctx -> {
			move(item, -1);
			return HandlerResult.DEFAULT_RESULT;
		});
		moveUpButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		moveUpButton.setDisabled(index == 0);

		ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
			move(item, 1);
			return HandlerResult.DEFAULT_RESULT;
		});
		moveDownButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		moveDownButton.setDisabled(index == listSize - 1);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			removeElement(item);
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		List<ReactControl> headerActions = List.of(moveUpButton, moveDownButton, removeButton);

		List<ReactControl> bodyChildren = new ArrayList<>();
		boolean polymorphic = _choices.hasOptions();
		if (polymorphic && _choices.options().size() > 1) {
			bodyChildren.add(createTypeSelector(item));
		}
		if (!polymorphic || isTypeSelected(item)) {
			bodyChildren.add(new ConfigEditorControl(_context, item));
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
		ConfigurationItem replacement = _children.newElement(selected);
		ConfigCopier.copyContent(new DefaultInstantiationContext(ConfigListEditorControl.class),
			oldItem, replacement, true);
		if (_children.replace(oldItem, replacement)) {
			rebuild(replacement);
		}
	}

	private boolean isTypeSelected(ConfigurationItem item) {
		return _choices.mapping() != null
			&& _choices.mapping().asOption(_choices.options(), item) != null;
	}

	private int indexOf(ConfigurationItem item) {
		return _children.indexOf(item);
	}

	// --- Operations ---

	/**
	 * Opens the menu offering the given element types below the Add button.
	 */
	private void openTypeMenu(ConfigTypeChoice types) {
		List<ConfigTypeChoice.Choice> choices = types.choices();
		List<ReactMenuControl.MenuEntry> entries = new ArrayList<>();
		for (int i = 0; i < choices.size(); i++) {
			entries.add(ReactMenuControl.MenuEntry.item(Integer.toString(i), choices.get(i).label()));
		}

		_typeMenu = new ReactMenuControl(_context, _addButtonId, entries,
			itemId -> addElement(choices.get(Integer.parseInt(itemId)).option()),
			() -> {
				// Nothing to do when dismissed without a choice.
			});
		addChild(_typeMenu);
		putState("children", getChildren());
		_typeMenu.open();
	}

	/**
	 * Adds a new element of the given type to the end of the list.
	 *
	 * @param typeOption
	 *        The element type to create, one of {@link ConfigChildren#allowedTypes()}, or
	 *        {@code null} for the property's default type.
	 */
	private void addElement(Object typeOption) {
		ConfigurationItem newItem = _children.newElement(typeOption);
		if (_children.add(newItem)) {
			rebuild(newItem);
		}
	}

	/**
	 * Removes the given element.
	 */
	private void removeElement(ConfigurationItem item) {
		if (_children.remove(item)) {
			rebuild(null);
		}
	}

	/**
	 * Moves the given element by the given number of positions.
	 */
	private void move(ConfigurationItem item, int delta) {
		if (_children.move(item, delta)) {
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
}
