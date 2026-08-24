/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
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

/**
 * A {@link ReactControl} that renders a full editor for a LIST, an ARRAY, or a MAP property of a
 * {@link ConfigurationItem} - the same sequence-of-elements editor for all three, differing only
 * in the value's shape and, for a MAP, in being unordered (see {@link PropertyDescriptor#isOrdered()}).
 *
 * <p>
 * Each list element is rendered as a collapsible {@link ReactFormGroupControl} with action buttons
 * (Move Up, Move Down, Remove) in the header and a nested {@link ConfigEditorControl} for the
 * element's properties - Move Up/Move Down only for an ordered collection. An Add button at the
 * bottom creates new elements.
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
	 * An entry created by the add button that is not yet in the edited collection.
	 *
	 * <p>
	 * A keyed collection is indexed by a property of its entries, so it cannot hold an entry
	 * whose key is empty or already taken. Such an entry therefore lives here until its key makes
	 * it acceptable - see {@link #commitPending()}.
	 * </p>
	 */
	private ConfigurationItem _pendingEntry;

	/**
	 * The field model this class built for {@link #_pendingEntry}'s key field, most recently by
	 * {@link #createKeyField(ConfigurationItem, PropertyDescriptor, boolean)}.
	 *
	 * <p>
	 * {@link #commitPending()} reports a duplicate key here directly, rather than searching the
	 * rebuilt tree for it. Held in a field rather than a local variable because
	 * {@link #rebuild(ConfigurationItem)} discards and recreates all child controls - including
	 * this one - on every cycle, so this reference is refreshed together with it every time the
	 * pending entry's group is (re)built, and would otherwise go stale.
	 * </p>
	 */
	private ConfigFieldModel _pendingKeyFieldModel;

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
	 *
	 * <p>
	 * A {@link PropertyKind#MAP} property's own value is, under the hood, exactly as directly
	 * mutable as a LIST's - {@link Map#put(Object, Object)}/{@link Map#remove(Object)} on it take
	 * effect immediately, the same as {@link List#add(Object)}/{@link List#remove(Object)} do on a
	 * LIST's live list. But there is no live list to hand out here: what this method returns is
	 * rows to render and address by index, and a {@link Map} has no index of its own. So, like
	 * ARRAY, this is a detached copy - the map's values, in the map's current iteration order -
	 * that reaches the configuration only through {@link #storeElements(List)} rebuilding the map
	 * from scratch.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private List<ConfigurationItem> elements() {
		Object value = _parentConfig.value(_property);
		if (_property.kind() == PropertyKind.ARRAY) {
			return value == null ? new ArrayList<>()
				: new ArrayList<>((List<ConfigurationItem>) PropertyDescriptorImpl.arrayAsList(value));
		}
		if (_property.kind() == PropertyKind.MAP) {
			Map<?, ?> map = (Map<?, ?>) value;
			return map == null ? new ArrayList<>() : new ArrayList<>((Collection<ConfigurationItem>) map.values());
		}
		return (List<ConfigurationItem>) value;
	}

	/**
	 * Writes back the elements obtained from {@link #elements()}, for a property shape that
	 * cannot be mutated in place.
	 *
	 * <p>
	 * A {@link PropertyKind#MAP} property is rebuilt wholesale into a fresh {@link LinkedHashMap},
	 * as {@code MapFormGroupBuilder} (the classic form's equivalent) does, so the order the user
	 * sees stays stable - keyed by each entry's own key property value, resolved by
	 * {@link #resolveKeyProperty(ConfigurationItem)} rather than {@link #_property}'s declared one,
	 * for the same polymorphism reason that method exists for. A duplicate key cannot reach this
	 * method: {@link #commitPending()} already refused one before committing, which matters here
	 * in a way it does not for a keyed LIST - {@link Map#put(Object, Object)} on an existing key
	 * silently overwrites it, unlike inserting a duplicate key into a keyed LIST, which
	 * TypedConfiguration rejects with an {@link IllegalArgumentException}.
	 * </p>
	 */
	private void storeElements(List<ConfigurationItem> elements) {
		if (_property.kind() == PropertyKind.ARRAY) {
			_parentConfig.update(_property, PropertyDescriptorImpl.listAsArray(_property, elements));
			return;
		}
		if (_property.kind() == PropertyKind.MAP) {
			Map<Object, ConfigurationItem> newMap = new LinkedHashMap<>();
			for (ConfigurationItem element : elements) {
				newMap.put(element.value(resolveKeyProperty(element)), element);
			}
			_parentConfig.update(_property, newMap);
			return;
		}
		// A LIST property was mutated in place and needs no write-back.
	}

	/**
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * <p>
	 * If {@link #_pendingEntry} is set, its group is rendered too - after the committed entries
	 * and before the add button, always expanded, with its key field editable. Every rebuild
	 * recreates it afresh (see {@link #createPendingElementGroup(ConfigurationItem)}), which is
	 * also why that method re-registers {@link #_pendingKeyFieldModel} and the key-change listener
	 * every time, rather than relying on a registration made once when the entry was created: a
	 * rebuild triggered by something unrelated (moving a different entry, say) discards and
	 * recreates this pending group exactly like every other child.
	 * </p>
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

		if (_pendingEntry != null) {
			addChild(createPendingElementGroup(_pendingEntry));
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

	/**
	 * The group for a committed element, with action buttons (Move Up, Move Down, Remove) in the
	 * header and a nested {@link ConfigEditorControl} for the element's properties in the body.
	 *
	 * <p>
	 * Move Up/Move Down are added only when {@link #_property} {@link PropertyDescriptor#isOrdered()
	 * is ordered}: {@code setKindMap} sets a MAP property's {@code ordered} flag to {@code false},
	 * since a {@link java.util.Map} has no position of its own for an entry to move to - unlike a
	 * keyed LIST, which stays ordered and keeps both buttons.
	 * </p>
	 */
	private ReactFormGroupControl createElementGroup(ConfigurationItem item, int index, int listSize, boolean expanded) {
		Label label = resolveElementLabel(item);

		List<ReactControl> headerActions = new ArrayList<>();
		if (_property.isOrdered()) {
			ReactButtonControl moveUpButton = new ReactButtonControl(_context, "\u25B2", ctx -> {
				moveUp(indexOf(item));
				return HandlerResult.DEFAULT_RESULT;
			});
			moveUpButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
			moveUpButton.setDisabled(index == 0);
			headerActions.add(moveUpButton);

			ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
				moveDown(indexOf(item));
				return HandlerResult.DEFAULT_RESULT;
			});
			moveDownButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
			moveDownButton.setDisabled(index == listSize - 1);
			headerActions.add(moveDownButton);
		}

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			int currentIndex = indexOf(item);
			if (currentIndex >= 0) {
				removeElement(currentIndex);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		headerActions.add(removeButton);

		PropertyDescriptor keyProperty = resolveKeyProperty(item);
		List<ReactControl> bodyChildren = createBodyChildren(item, keyProperty, false);

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, !expanded, "subtle", true,
			headerActions, bodyChildren);
		group.setHeader(createHeaderControl(label));

		registerTitleListener(item, group);

		return group;
	}

	/**
	 * The group for {@link #_pendingEntry}, rendered separately from
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} because a pending entry
	 * differs from a committed one in more than one dimension at once: it has no position in the
	 * edited collection to move it among (so no Move Up/Move Down button - there is nothing to
	 * move it among), its Remove button discards it (see {@link #discardPending()}) instead of
	 * removing an entry from the collection, and its key field is editable rather than fixed.
	 * Folding all of that into {@link #createElementGroup(ConfigurationItem, int, int, boolean)}
	 * via extra parameters would have left that method's index/listSize-based Move Up/Move Down
	 * logic surrounded by conditionals that never apply to it; a sibling method keeps both
	 * readable, sharing the body/title-listener construction that does not differ (
	 * {@link #createBodyChildren(ConfigurationItem, PropertyDescriptor, boolean)},
	 * {@link #registerTitleListener(ConfigurationItem, ReactFormGroupControl)}).
	 *
	 * <p>
	 * Always rendered expanded, and re-registers {@link #_pendingKeyFieldModel} and the
	 * key-change listener every time it runs - see {@link #rebuild(ConfigurationItem)}.
	 * </p>
	 */
	private ReactFormGroupControl createPendingElementGroup(ConfigurationItem entry) {
		Label label = resolveElementLabel(entry);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			discardPending();
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		List<ReactControl> headerActions = List.of(removeButton);

		PropertyDescriptor keyProperty = resolveKeyProperty(entry);
		List<ReactControl> bodyChildren = createBodyChildren(entry, keyProperty, true);

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, false, "subtle", true,
			headerActions, bodyChildren);
		group.setHeader(createHeaderControl(label));

		registerTitleListener(entry, group);
		listenForKey(entry);

		return group;
	}

	/**
	 * The body children shared by {@link #createElementGroup(ConfigurationItem, int, int, boolean)}
	 * and {@link #createPendingElementGroup(ConfigurationItem)}: the type selector (for a
	 * polymorphic collection with more than one choice), the key field (for a keyed collection),
	 * and the nested {@link ConfigEditorControl} over the entry's own properties (with the key
	 * property hidden from it, since this class already renders it). {@code keyEditable} is
	 * passed straight through to
	 * {@link #createKeyField(ConfigurationItem, PropertyDescriptor, boolean)} - {@code true} only
	 * for the pending entry.
	 */
	private List<ReactControl> createBodyChildren(ConfigurationItem item, PropertyDescriptor keyProperty,
			boolean keyEditable) {
		List<ReactControl> bodyChildren = new ArrayList<>();
		boolean polymorphic = _choices.hasOptions();
		if (polymorphic && _choices.options().size() > 1) {
			bodyChildren.add(createTypeSelector(item));
		}
		if (keyProperty != null) {
			bodyChildren.add(createKeyField(item, keyProperty, keyEditable));
		}
		if (!polymorphic || isTypeSelected(item)) {
			bodyChildren.add(new ConfigEditorControl(_context, item,
				keyProperty == null ? Collections.emptySet() : Collections.singleton(keyProperty)));
		}
		return bodyChildren;
	}

	/**
	 * Registers the dynamic label update on {@code item}'s title property, shared by
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} and
	 * {@link #createPendingElementGroup(ConfigurationItem)}.
	 */
	private void registerTitleListener(ConfigurationItem item, ReactFormGroupControl group) {
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		if (titleProp != null) {
			ConfigurationListener listener = change -> {
				group.setHeader(createHeaderControl(resolveElementLabel(item)));
			};
			item.addConfigurationListener(titleProp, listener);
			_listeners.add(new ListenerRegistration(item, titleProp, listener));
		}
	}

	/**
	 * The key property of the given entry, resolved against the entry's own
	 * {@link ConfigurationItem#descriptor() descriptor} rather than
	 * {@link #_property}'s declared element type.
	 *
	 * <p>
	 * For a polymorphic keyed collection - which this class supports via
	 * {@link #createTypeSelector(ConfigurationItem)} - an entry's actual type is typically a
	 * genuine subtype of the collection's declared element type, and its own
	 * {@link ConfigurationDescriptor} creates a fresh {@link PropertyDescriptor} instance for
	 * every property, including one inherited unchanged from a super interface.
	 * {@link PropertyDescriptor} has no {@link Object#equals(Object) equals}/
	 * {@link Object#hashCode() hashCode} override, so
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} hiding the key from the
	 * nested editor via {@link java.util.Set#contains(Object) Set.contains} only works if both
	 * call sites use the very same instance - the one the entry's own descriptor hands out, not
	 * {@link PropertyDescriptor#getKeyProperty()}'s declared-type instance.
	 * </p>
	 *
	 * @param entry
	 *        The entry whose key property is resolved.
	 * @return {@code null} if {@link #_property} is not keyed.
	 */
	private PropertyDescriptor resolveKeyProperty(ConfigurationItem entry) {
		PropertyDescriptor declaredKeyProperty = _property.getKeyProperty();
		if (declaredKeyProperty == null) {
			return null;
		}
		return entry.descriptor().getProperty(declaredKeyProperty.getPropertyName());
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
	 * @param keyProperty
	 *        The entry's own key property, as resolved by
	 *        {@link #resolveKeyProperty(ConfigurationItem)} - the caller must pass the same
	 *        instance it also hides from the nested editor, since only identity (not mere
	 *        equality) is checked there.
	 * @param editable
	 *        Whether the key may still be changed, i.e. whether the entry is pending. When
	 *        {@code true}, the built model is also kept as {@link #_pendingKeyFieldModel} - the
	 *        only entry whose key field is ever editable is {@link #_pendingEntry}.
	 */
	private ReactControl createKeyField(ConfigurationItem entry, PropertyDescriptor keyProperty, boolean editable) {
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(entry, keyProperty);
		model.setEditable(editable);
		if (editable) {
			_pendingKeyFieldModel = model;
		}
		ReactControl input = ConfigControlService.getInstance().createControl(_context, model);

		String label = Labels.propertyLabel(keyProperty, false);
		ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(_context, label,
			model.isMandatory(), false, null, null, null, false, true, input);
		// Detach when this specific field is disposed (on the next rebuild(), or when this whole
		// control is cleaned up) - the same lifecycle ConfigEditorControl#addCleanupAction(Runnable)
		// ties its own field models to, just anchored on the field's own chrome control instead of
		// on "this", since a fresh key field (and model) is built on every rebuild() cycle.
		chrome.addCleanupAction(model::detach);

		String tooltip = Resources.getInstance().getString(keyProperty.labelKey(null).tooltip(), null);
		if (tooltip != null && !tooltip.isEmpty()) {
			chrome.setTooltip(tooltip, label, true);
		}
		return chrome;
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
	 * Adds a new element with default values.
	 *
	 * <p>
	 * For a keyed collection, the new element cannot be added directly: a keyed collection is
	 * indexed by a property of its entries, so {@link TypedConfiguration} would reject an entry
	 * whose key is empty (as every freshly constructed entry's is) with a technical
	 * {@link IllegalArgumentException} the moment a second one collided with it. The new entry
	 * therefore becomes {@link #_pendingEntry} instead, rendered by
	 * {@link #createPendingElementGroup(ConfigurationItem)} until {@link #commitPending()} moves
	 * it into the collection.
	 * </p>
	 */
	private void addElement() {
		if (_property.getKeyProperty() != null) {
			if (_pendingEntry != null) {
				// One unfinished entry at a time: a second one could not be told apart from the
				// first, both having no key yet.
				return;
			}
			_pendingEntry = newEntry();
			rebuild(_pendingEntry);
			return;
		}

		List<ConfigurationItem> items = elements();
		ConfigurationItem newItem = newEntry();
		items.add(newItem);
		storeElements(items);
		rebuild(newItem);
	}

	/**
	 * Creates a new element of this property's element type, honouring a polymorphic collection's
	 * first option just as the pre-pending-entry {@link #addElement()} always did.
	 */
	private ConfigurationItem newEntry() {
		if (_choices.hasOptions()) {
			return (ConfigurationItem) _choices.mapping().toSelection(_choices.options().get(0));
		}
		return TypedConfiguration.newConfigItem(resolveNewElementType());
	}

	/**
	 * Registers the {@link ConfigurationListener} on {@code pendingEntry}'s key property that
	 * calls {@link #commitPending()} whenever the key changes.
	 *
	 * <p>
	 * Tracked in {@link #_listeners} like every other listener this class holds, so
	 * {@link #removeListeners()} unregisters it too - which is exactly why
	 * {@link #createPendingElementGroup(ConfigurationItem)} calls this again on every
	 * {@link #rebuild(ConfigurationItem)} cycle rather than {@link #addElement()} calling it once:
	 * a rebuild triggered by something else entirely (moving a different entry, changing another
	 * entry's type, ...) would otherwise silently drop this registration, leaving the pending
	 * entry stuck - editable, but no longer able to commit itself.
	 * </p>
	 */
	private void listenForKey(ConfigurationItem pendingEntry) {
		PropertyDescriptor keyProperty = resolveKeyProperty(pendingEntry);
		ConfigurationListener listener = change -> commitPending();
		pendingEntry.addConfigurationListener(keyProperty, listener);
		_listeners.add(new ListenerRegistration(pendingEntry, keyProperty, listener));
	}

	/**
	 * Moves {@link #_pendingEntry} into the edited collection once its key is usable.
	 *
	 * <p>
	 * An empty key means the user has not finished typing, so the entry simply stays pending. A
	 * key that another entry already uses is reported at the key field: inserting would be
	 * rejected by {@link TypedConfiguration} with a message about the collection's index, which
	 * says nothing to whoever is editing the form.
	 * </p>
	 */
	private void commitPending() {
		PropertyDescriptor keyProperty = resolveKeyProperty(_pendingEntry);
		Object key = _pendingEntry.value(keyProperty);
		if (key == null || key.toString().isEmpty()) {
			return;
		}
		if (hasEntryWithKey(key)) {
			_pendingKeyFieldModel.setError(I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE
				.fill(Labels.propertyLabel(keyProperty, false), key));
			return;
		}
		ConfigurationItem entry = _pendingEntry;
		_pendingEntry = null;
		_pendingKeyFieldModel = null;
		List<ConfigurationItem> items = elements();
		items.add(entry);
		storeElements(items);
		rebuild(entry);
	}

	/**
	 * Whether some entry already committed to the edited collection carries the given key.
	 *
	 * <p>
	 * Resolves each existing entry's key via {@link #resolveKeyProperty(ConfigurationItem)}, not
	 * {@link #_property}'s declared key property, for the same reason every other key lookup in
	 * this class does - see that method's own JavaDoc. This is also what makes a duplicate key
	 * impossible for a {@link PropertyKind#MAP} property: unlike a keyed LIST, where
	 * TypedConfiguration itself rejects a colliding insert, {@link Map#put(Object, Object)} on an
	 * already-used key would silently overwrite the existing entry - so this check, not the
	 * underlying structure, is the only thing standing in the way.
	 * </p>
	 *
	 * @param key
	 *        The candidate key. Never {@code null} or empty - {@link #commitPending()} only calls
	 *        this once {@link #_pendingEntry}'s own key has cleared that check.
	 */
	private boolean hasEntryWithKey(Object key) {
		List<ConfigurationItem> items = elements();
		if (items == null) {
			return false;
		}
		for (ConfigurationItem existing : items) {
			if (key.equals(existing.value(resolveKeyProperty(existing)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Discards {@link #_pendingEntry} - the pending entry's own Remove action, which cannot
	 * remove it from the edited collection since it was never added there.
	 */
	private void discardPending() {
		_pendingEntry = null;
		_pendingKeyFieldModel = null;
		rebuild(null);
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
