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
 * in the value's shape.
 *
 * <p>
 * Each list element is rendered as a collapsible {@link ReactFormGroupControl} with action buttons
 * (Move Up, Move Down, Remove) in the header and a nested {@link ConfigEditorControl} for the
 * element's properties - Move Up/Move Down only for a {@link ConfigCollectionValue#isReorderable() reorderable}
 * collection. An Add button at the bottom creates new elements.
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

	/**
	 * The edited collection, which is where every difference between a LIST, an ARRAY and a MAP
	 * property lives - see {@link ConfigCollectionValue}. This class works in element indices and
	 * never asks what shape holds them.
	 */
	private final ConfigCollectionValue _value;

	private final PolymorphicOptions.Choices _choices;

	private final List<ListenerRegistration> _listeners = new ArrayList<>();

	private record ListenerRegistration(ConfigurationItem item, PropertyDescriptor property,
			ConfigurationListener listener) {
	}

	/**
	 * The entries created by the add button that are not yet in the edited collection, in the
	 * order they were created.
	 *
	 * <p>
	 * A keyed collection is indexed by a property of its entries, so it cannot hold an entry whose
	 * key is empty or already taken. Such an entry therefore lives here until the user confirms it
	 * with a key the collection accepts - see {@link #commitPending(PendingEntry)}. More than one may be pending at once:
	 * each is a distinct object with its own key field, so nothing about holding several
	 * simultaneously is ambiguous - unlike the single {@code _pendingEntry} field this replaced,
	 * which came with a guard refusing a second one, on the mistaken belief that two pending
	 * entries could not be told apart.
	 * </p>
	 *
	 * <p>
	 * Rendered in {@link #rebuild(ConfigurationItem)} after every committed entry, in this list's
	 * order - a stable, predictable place: newly created entries always appear at the end, in the
	 * order the user pressed "+".
	 * </p>
	 */
	private final List<PendingEntry> _pendingEntries = new ArrayList<>();

	/**
	 * One entry from {@link #_pendingEntries}, paired with the {@link ConfigFieldModel} built for
	 * its key field.
	 *
	 * <p>
	 * The pairing itself is why this class exists: {@link #_entry} and {@link #_keyFieldModel}
	 * used to be two separate fields on {@link ConfigListEditorControl} that had to be set and
	 * cleared in lockstep by hand, for a single pending entry. Now that several can be pending
	 * simultaneously, that lockstep would have had to be repeated per list index across two
	 * parallel lists - keeping the pairing in one object removes the chance of the two ever
	 * drifting apart.
	 * </p>
	 *
	 * <p>
	 * Neither field is {@code final}. {@link #_entry} is replaced wholesale when the user picks a
	 * different type for a still-pending entry of a polymorphic collection - the holder is what
	 * every closure of the current render captured, so swapping the entry inside it keeps those
	 * closures pointing at the right thing.
	 * </p>
	 *
	 * <p>
	 * {@link #_keyFieldModel} is mutable because
	 * {@link #rebuild(ConfigurationItem)} discards and recreates all child controls - including
	 * the key field - on every cycle. {@link #createPendingElementGroup(PendingEntry)} refreshes
	 * it on this very object every time it (re)builds this entry's group (via
	 * {@link #createKeyField(ConfigurationItem, PropertyDescriptor, PendingEntry)}), so
	 * {@link #commitPending(PendingEntry)} never reports an error on a control that is no longer
	 * displayed.
	 * </p>
	 */
	private static final class PendingEntry {

		private ConfigurationItem _entry;

		private ConfigFieldModel _keyFieldModel;

		PendingEntry(ConfigurationItem entry) {
			_entry = entry;
		}
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
		_value = new ConfigCollectionValue(parentConfig, property);
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
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * <p>
	 * Every entry in {@link #_pendingEntries} is rendered too, in that list's order - after all
	 * the committed entries and before the add button, always expanded, with its key field
	 * editable. Every rebuild recreates all of them afresh (see
	 * {@link #createPendingElementGroup(PendingEntry)}), which is also why that method
	 * re-registers each entry's {@link PendingEntry#_keyFieldModel} and key-change listener every
	 * time, rather than relying on a registration made once when the entry was created: a rebuild
	 * triggered by something unrelated (moving a different entry, say) discards and recreates
	 * every pending group exactly like every other child.
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

		List<ConfigurationItem> items = _value.elements();
		if (items != null) {
			for (int i = 0; i < items.size(); i++) {
				ConfigurationItem item = items.get(i);
				addChild(createElementGroup(item, i, items.size(), item == expandItem));
			}
		}

		for (PendingEntry pending : _pendingEntries) {
			addChild(createPendingElementGroup(pending));
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
	 * Move Up/Move Down are added only for a {@link ConfigCollectionValue#isReorderable() reorderable} collection.
	 * </p>
	 */
	private ReactFormGroupControl createElementGroup(ConfigurationItem item, int index, int listSize, boolean expanded) {
		Label label = resolveElementLabel(item);

		List<ReactControl> headerActions = new ArrayList<>();
		if (_value.isReorderable()) {
			ReactButtonControl moveUpButton = new ReactButtonControl(_context, "\u25B2", ctx -> {
				moveUp(_value.indexOf(item));
				return HandlerResult.DEFAULT_RESULT;
			});
			moveUpButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
			moveUpButton.setDisabled(index == 0);
			headerActions.add(moveUpButton);

			ReactButtonControl moveDownButton = new ReactButtonControl(_context, "\u25BC", ctx -> {
				moveDown(_value.indexOf(item));
				return HandlerResult.DEFAULT_RESULT;
			});
			moveDownButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
			moveDownButton.setDisabled(index == listSize - 1);
			headerActions.add(moveDownButton);
		}

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			int currentIndex = _value.indexOf(item);
			if (currentIndex >= 0) {
				removeElement(currentIndex);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		headerActions.add(removeButton);

		PropertyDescriptor keyProperty = _value.keyProperty(item);
		List<ReactControl> bodyChildren = createBodyChildren(item, keyProperty, null);

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, !expanded, "subtle", true,
			headerActions, bodyChildren);
		group.setHeader(createHeaderControl(label));

		registerTitleListener(item, group);

		return group;
	}

	/**
	 * The group for one entry of {@link #_pendingEntries}, rendered separately from
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} because a pending entry
	 * differs from a committed one in more than one dimension at once: it has no position in the
	 * edited collection to move it among (so no Move Up/Move Down button - there is nothing to
	 * move it among), it carries a Confirm button that a committed entry has no use for (see
	 * {@link #commitPending(PendingEntry)}), its Remove button discards it (see
	 * {@link #discardPending(PendingEntry)}) instead of removing an entry from the collection, and
	 * its key field is editable rather than fixed. Folding all of that into
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} via extra parameters would
	 * have left that method's index/listSize-based Move Up/Move Down logic surrounded by
	 * conditionals that never apply to it; a sibling method keeps both readable, sharing the
	 * body/title-listener construction that does not differ (
	 * {@link #createBodyChildren(ConfigurationItem, PropertyDescriptor, PendingEntry)},
	 * {@link #registerTitleListener(ConfigurationItem, ReactFormGroupControl)}).
	 *
	 * <p>
	 * Always rendered expanded, and refreshes {@code pending}'s
	 * {@link PendingEntry#_keyFieldModel} every time it runs - see
	 * {@link #rebuild(ConfigurationItem)}.
	 * </p>
	 */
	private ReactFormGroupControl createPendingElementGroup(PendingEntry pending) {
		ConfigurationItem entry = pending._entry;
		Label label = resolveElementLabel(entry);

		ReactButtonControl confirmButton = new ReactButtonControl(_context, "\u2713", ctx -> {
			commitPending(pending);
			return HandlerResult.DEFAULT_RESULT;
		});
		confirmButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			discardPending(pending);
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		List<ReactControl> headerActions = List.of(confirmButton, removeButton);

		PropertyDescriptor keyProperty = _value.keyProperty(entry);
		List<ReactControl> bodyChildren = createBodyChildren(entry, keyProperty, pending);

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, false, "subtle", true,
			headerActions, bodyChildren);
		group.setHeader(createHeaderControl(label));

		registerTitleListener(entry, group);

		return group;
	}

	/**
	 * The body children shared by {@link #createElementGroup(ConfigurationItem, int, int, boolean)}
	 * and {@link #createPendingElementGroup(PendingEntry)}: the type selector (for a polymorphic
	 * collection with more than one choice), the key field (for a keyed collection), and the
	 * nested {@link ConfigEditorControl} over the entry's own properties (with the key property
	 * hidden from it, since this class already renders it). {@code pending} is passed straight
	 * through to {@link #createKeyField(ConfigurationItem, PropertyDescriptor, PendingEntry)} -
	 * non-{@code null} only for a pending entry's own group, which is what makes its key field
	 * editable there and fixed everywhere else.
	 */
	private List<ReactControl> createBodyChildren(ConfigurationItem item, PropertyDescriptor keyProperty,
			PendingEntry pending) {
		List<ReactControl> bodyChildren = new ArrayList<>();
		boolean polymorphic = _choices.hasOptions();
		if (polymorphic && _choices.options().size() > 1) {
			bodyChildren.add(createTypeSelector(item, pending));
		}
		if (keyProperty != null) {
			bodyChildren.add(createKeyField(item, keyProperty, pending));
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
	 * {@link #createPendingElementGroup(PendingEntry)}.
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
	 *        {@link ConfigCollectionValue#keyProperty(ConfigurationItem)} - the caller must pass the same
	 *        instance it also hides from the nested editor, since only identity (not mere
	 *        equality) is checked there.
	 * @param pending
	 *        The {@link PendingEntry} {@code entry} is one of, or {@code null} if it is already
	 *        committed. The key may still be changed only while pending - once the entry is in
	 *        the collection, changing it would re-index the collection under a new key. When
	 *        non-{@code null}, the built model is also kept as {@link PendingEntry#_keyFieldModel}.
	 */
	private ReactControl createKeyField(ConfigurationItem entry, PropertyDescriptor keyProperty,
			PendingEntry pending) {
		boolean editable = pending != null;
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(entry, keyProperty);
		model.setEditable(editable);
		if (pending != null) {
			pending._keyFieldModel = model;
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

	private ReactFormFieldChromeControl createTypeSelector(ConfigurationItem item, PendingEntry pending) {
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
				onTypeChanged(item, PolymorphicOptions.optionForKey(rawOptions, (String) newValue), pending);
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

	/**
	 * Replaces an entry with one of the newly picked type, carrying over what was already filled in.
	 *
	 * @param pending
	 *        The {@link PendingEntry} {@code oldItem} belongs to, or {@code null} if it is already
	 *        committed. A pending entry is not in the edited collection, so it cannot be found -
	 *        let alone replaced - by index there; its holder is where it has to be swapped instead.
	 *        Without that, picking a type for a pending entry would leave the select showing the new
	 *        type while the entry kept the old one, and Confirm would take an entry of a type nobody
	 *        chose.
	 */
	private void onTypeChanged(ConfigurationItem oldItem, Object selected, PendingEntry pending) {
		if (selected == null) {
			return;
		}
		ConfigurationItem replacement = (ConfigurationItem) _choices.mapping().toSelection(selected);
		ConfigCopier.copyContent(new DefaultInstantiationContext(ConfigListEditorControl.class),
			oldItem, replacement, true);

		if (pending != null) {
			pending._entry = replacement;
			rebuild(replacement);
			return;
		}

		int index = _value.indexOf(oldItem);
		if (index < 0) {
			return;
		}
		_value.replace(index, replacement);
		rebuild(replacement);
	}

	private boolean isTypeSelected(ConfigurationItem item) {
		return _choices.mapping() != null
			&& _choices.mapping().asOption(_choices.options(), item) != null;
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
	 * therefore joins {@link #_pendingEntries} instead, rendered by
	 * {@link #createPendingElementGroup(PendingEntry)} until {@link #commitPending(PendingEntry)}
	 * moves it into the collection. Any number of entries may be pending at once, independently -
	 * a keyed collection's constraint is on the entries actually in it, and a pending entry, by
	 * definition, is not.
	 * </p>
	 */
	private void addElement() {
		if (_value.isKeyed()) {
			PendingEntry pending = new PendingEntry(newEntry());
			_pendingEntries.add(pending);
			rebuild(pending._entry);
			return;
		}

		ConfigurationItem newItem = newEntry();
		_value.add(newItem);
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
		return _value.newElement();
	}

	/**
	 * Moves {@code pending}'s entry into the edited collection, on the user's explicit
	 * confirmation.
	 *
	 * <p>
	 * Confirming is a deliberate action of its own (the Confirm button of
	 * {@link #createPendingElementGroup(PendingEntry)}) rather than something inferred from the
	 * key field's value. Committing as soon as the key merely <em>looks</em> usable would take a
	 * half-typed key for the finished one - a key field reports what has been typed so far, so
	 * anyone not typing the whole key in one go would find the entry committed under a prefix of
	 * it, its key field by then already fixed. Committing when the field loses focus would be no
	 * better: leaving the field to fetch the value from somewhere else is exactly what one does
	 * while filling it in.
	 * </p>
	 *
	 * <p>
	 * So both an empty key and one that another entry already uses are reported at the key field,
	 * and the entry stays pending. Neither is silently tolerated: the user asked for this entry to
	 * be taken, and has to be told why it was not. Inserting a duplicate key would otherwise be
	 * rejected by {@link TypedConfiguration} with a message about the collection's index, which
	 * says nothing to whoever is editing the form.
	 * </p>
	 *
	 * <p>
	 * "Another entry" means a <em>committed</em> one - {@link ConfigCollectionValue#hasEntryWithKey(Object)} does not
	 * also consult {@link #_pendingEntries}, deliberately. A pending entry has claimed nothing
	 * yet: its key is still being written, and two entries being filled in side by side may well
	 * pass through the same intermediate text. Whichever is confirmed first takes the key; the
	 * other is then told the key is taken, at the moment it is confirmed - which is the moment its
	 * key is actually being claimed.
	 * </p>
	 */
	private void commitPending(PendingEntry pending) {
		ConfigurationItem entry = pending._entry;
		PropertyDescriptor keyProperty = _value.keyProperty(entry);
		Object key = entry.value(keyProperty);
		if (key == null || key.toString().isEmpty()) {
			pending._keyFieldModel.setError(
				I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(keyProperty, false)));
			return;
		}
		if (_value.hasEntryWithKey(key)) {
			pending._keyFieldModel.setError(I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE
				.fill(Labels.propertyLabel(keyProperty, false), key));
			return;
		}
		_pendingEntries.remove(pending);
		_value.add(entry);
		rebuild(entry);
	}

	/**
	 * Discards {@code pending} - that entry's own Remove action, which cannot remove it from the
	 * edited collection since it was never added there. The other entries of
	 * {@link #_pendingEntries}, if any, are left untouched.
	 */
	private void discardPending(PendingEntry pending) {
		_pendingEntries.remove(pending);
		rebuild(null);
	}

	/**
	 * Removes the element at the given index.
	 */
	private void removeElement(int index) {
		_value.remove(index);
		rebuild(null);
	}

	/**
	 * Moves the element at the given index one position up.
	 */
	private void moveUp(int index) {
		_value.move(index, -1);
		rebuild(null);
	}

	/**
	 * Moves the element at the given index one position down.
	 */
	private void moveDown(int index) {
		_value.move(index, 1);
		rebuild(null);
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
