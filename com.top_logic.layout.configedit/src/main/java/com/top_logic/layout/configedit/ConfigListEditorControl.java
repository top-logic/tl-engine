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
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.provider.label.ClassLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
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

	/**
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * @param expandItem
	 *        Element whose group should be rendered expanded; all others stay collapsed. May be
	 *        {@code null}.
	 */
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

	@SuppressWarnings("unchecked")
	private void rebuild(ConfigurationItem expandItem) {
		removeListeners();

		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();

		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
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
		moveUpButton.setIconOnly(true);
		moveUpButton.setDisabled(index == 0);

		ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
			moveDown(indexOf(item));
			return HandlerResult.DEFAULT_RESULT;
		});
		moveDownButton.setIconOnly(true);
		moveDownButton.setDisabled(index == listSize - 1);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			int currentIndex = indexOf(item);
			if (currentIndex >= 0) {
				removeElement(currentIndex);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setIconOnly(true);

		List<ReactControl> headerActions = List.of(moveUpButton, moveDownButton, removeButton);

		List<ReactControl> bodyChildren = new ArrayList<>();
		if (_choices.hasOptions()) {
			bodyChildren.add(createTypeSelector(item));
		}
		bodyChildren.add(new ConfigEditorControl(_context, item));
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
		Class<?> currentClass = PolymorphicOptions.fromConfig(_choices.mapping(), _choices.options(), item);
		List<String> options = _choices.options().stream().map(Class::getName).toList();
		String currentFqcn = currentClass != null ? currentClass.getName() : null;
		SimpleSelectFieldModel typeModel = new SimpleSelectFieldModel(currentFqcn, options, false);

		LabelProvider labelProvider = new FqcnClassLabelProvider(_choices.options());

		typeModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				onTypeChanged(item, resolveClass(_choices.options(), (String) newValue));
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

	@SuppressWarnings("unchecked")
	private void onTypeChanged(ConfigurationItem oldItem, Class<?> selected) {
		if (selected == null) {
			return;
		}
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		int index = items.indexOf(oldItem);
		if (index < 0) {
			return;
		}
		ConfigurationItem replacement = PolymorphicOptions.toConfig(_choices.mapping(), selected);
		items.set(index, replacement);
		rebuild(replacement);
	}

	static Class<?> resolveClass(List<Class<?>> options, String fqcn) {
		if (fqcn == null) {
			return null;
		}
		for (Class<?> cls : options) {
			if (cls.getName().equals(fqcn)) {
				return cls;
			}
		}
		return null;
	}

	/**
	 * {@link LabelProvider} that receives a fully-qualified class name and delegates to
	 * {@link ClassLabelProvider} once the {@link Class} has been resolved against a known option
	 * list.
	 */
	static final class FqcnClassLabelProvider implements LabelProvider {

		private final List<Class<?>> _options;

		private final ClassLabelProvider _delegate = new ClassLabelProvider();

		FqcnClassLabelProvider(List<Class<?>> options) {
			_options = options;
		}

		@Override
		public String getLabel(Object object) {
			if (!(object instanceof String fqcn) || fqcn.isEmpty()) {
				return "";
			}
			Class<?> cls = resolveClass(_options, fqcn);
			return cls != null ? _delegate.getLabel(cls) : fqcn;
		}
	}

	@SuppressWarnings("unchecked")
	private int indexOf(ConfigurationItem item) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		return items != null ? items.indexOf(item) : -1;
	}

	// --- Operations ---

	/**
	 * Adds a new element with default values to the end of the list.
	 */
	@SuppressWarnings("unchecked")
	private void addElement() {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		Class<? extends ConfigurationItem> elementType = resolveNewElementType();
		ConfigurationItem newItem = TypedConfiguration.newConfigItem(elementType);
		items.add(newItem);
		rebuild(newItem);
	}

	/**
	 * Removes the element at the given index.
	 */
	@SuppressWarnings("unchecked")
	private void removeElement(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index >= 0 && index < items.size()) {
			items.remove(index);
			rebuild(null);
		}
	}

	/**
	 * Moves the element at the given index one position up.
	 */
	@SuppressWarnings("unchecked")
	private void moveUp(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index > 0 && index < items.size()) {
			ConfigurationItem item = items.remove(index);
			items.add(index - 1, item);
			rebuild(null);
		}
	}

	/**
	 * Moves the element at the given index one position down.
	 */
	@SuppressWarnings("unchecked")
	private void moveDown(int index) {
		List<ConfigurationItem> items = (List<ConfigurationItem>) _parentConfig.value(_property);
		if (items != null && index >= 0 && index < items.size() - 1) {
			ConfigurationItem item = items.remove(index);
			items.add(index + 1, item);
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
