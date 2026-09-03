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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;
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
import com.top_logic.layout.react.control.layout.LabelPosition;
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
 * collection. An Add button at the bottom creates new elements. None of these actions - Add, Move
 * Up, Move Down, Remove - are rendered at all while this control was built {@code editable = false}
 * (see the five-argument constructor); the elements themselves are still shown, just without
 * anything that could change the collection.
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

	/**
	 * The edited collection, which is where every difference between a LIST, an ARRAY and a MAP
	 * property lives - see {@link ConfigCollectionValue}. This class works in element indices and
	 * never asks what shape holds them.
	 */
	private final ConfigCollection _value;

	private final ConfigurationItem _formModel;

	private final PolymorphicOptions.Choices _choices;

	/**
	 * The entries created here that the edited collection cannot hold yet - see
	 * {@link ConfigPendingEntries}. This class renders them and wires their buttons; when an entry
	 * may join the collection, and what happens if it may not, is decided there.
	 */
	private final ConfigPendingEntries _pending;

	private final ConfigFieldIndex _index;

	/**
	 * Whether an add/remove/reorder button is rendered at all, and whether every entry's own
	 * fields and nested collections accept input.
	 *
	 * <p>
	 * {@code false} while the enclosing {@link ConfigFormControl}'s own edit mode is off - see
	 * {@link ConfigEditorControl#_editable}, which this mirrors and which threads it here. Unlike a
	 * field, a collection action has no {@link ConfigFieldModel} to call
	 * {@link ConfigFieldModel#setEditable(boolean)} on, so the only way to keep it from being
	 * offered is to not create it: {@link #rebuild(ConfigurationItem)} skips the add button and
	 * every pending entry, and {@link #createElementGroup(ConfigurationItem, int, int, boolean)}
	 * skips every header action (move up, move down, remove), whenever this is {@code false}.
	 * </p>
	 */
	private final boolean _editable;

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
		this(context, parentConfig, property, null);
	}

	/**
	 * Creates a {@link ConfigListEditorControl}, reporting every field it builds - including each
	 * entry's own fields and its key field - to the given {@link ConfigFieldIndex}.
	 *
	 * <p>
	 * Editable - see the five-argument constructor for one that is not.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param parentConfig
	 *        The parent configuration item owning the LIST property.
	 * @param property
	 *        The LIST property descriptor.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
			PropertyDescriptor property, ConfigFieldIndex index) {
		this(context, parentConfig, property, index, true);
	}

	/**
	 * Creates a {@link ConfigListEditorControl}, reporting every field it builds to the given
	 * {@link ConfigFieldIndex}, and deciding whether any action on the collection is offered.
	 *
	 * @param context
	 *        The React context.
	 * @param parentConfig
	 *        The parent configuration item owning the LIST property.
	 * @param property
	 *        The LIST property descriptor.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 * @param editable
	 *        Whether add/remove/reorder are offered, and every entry's own fields accept input -
	 *        see {@link #_editable}.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
			PropertyDescriptor property, ConfigFieldIndex index, boolean editable) {
		this(context, parentConfig, property, index, editable, parentConfig);
	}

	/**
	 * Creates a {@link ConfigListEditorControl} that knows what is being edited as a whole.
	 *
	 * @param formModel
	 *        The root of the configuration under edit, handed down to every field and nested editor -
	 *        see {@link ConfigEditorControl#ConfigEditorControl(ReactContext, ConfigurationItem, Set,
	 *        boolean, ConfigFieldIndex, boolean, ConfigurationItem)}.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigurationItem parentConfig,
			PropertyDescriptor property, ConfigFieldIndex index, boolean editable,
			ConfigurationItem formModel) {
		this(context, new ConfigCollectionValue(parentConfig, property),
			PolymorphicOptions.compute(parentConfig, property), index, editable, formModel);
	}

	/**
	 * Creates a {@link ConfigListEditorControl} over any {@link ConfigCollection}.
	 *
	 * <p>
	 * The general constructor the others end in. Nothing below this point knows whether the rows
	 * come from a property of a surrounding configuration or from a form field - which is the point
	 * of {@link ConfigCollection}. A caller that has no property must also say which types an entry
	 * may have, since {@link PolymorphicOptions} reads that off a property's options annotation.
	 * </p>
	 *
	 * @param value
	 *        The collection to edit.
	 * @param choices
	 *        The types an entry may be given, or {@link PolymorphicOptions.Choices#NONE} if the
	 *        collection is not polymorphic.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 * @param editable
	 *        Whether add/remove/reorder are offered, and every entry's own fields accept input -
	 *        see {@link #_editable}.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigCollection value,
			PolymorphicOptions.Choices choices, ConfigFieldIndex index, boolean editable) {
		this(context, value, choices, index, editable, null);
	}

	/**
	 * Creates a {@link ConfigListEditorControl} over any collection, knowing what is being edited as
	 * a whole.
	 *
	 * @param formModel
	 *        The root of the configuration under edit, or {@code null} when there is none - a
	 *        collection held by a form field has no surrounding configuration.
	 */
	public ConfigListEditorControl(ReactContext context, ConfigCollection value,
			PolymorphicOptions.Choices choices, ConfigFieldIndex index, boolean editable,
			ConfigurationItem formModel) {
		super(context);
		_context = context;
		_index = index;
		_editable = editable;
		_formModel = formModel;
		_value = value;
		_choices = choices;
		_pending = new ConfigPendingEntries(_value, this::rebuild);

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
	 * The form model this editor was told about, or the given entry when it was told none - an entry
	 * of a collection that is nobody's property is the best answer available.
	 */
	private ConfigurationItem formModelOr(ConfigurationItem entry) {
		return _formModel != null ? _formModel : entry;
	}

	/**
	 * Clears all children and rebuilds them from the current list state.
	 *
	 * <p>
	 * Every entry in {@link ConfigPendingEntries} is rendered too, in that list's order - after all
	 * the committed entries and before the add button, always expanded, with its key field
	 * editable. Every rebuild recreates all of them afresh (see
	 * {@link #createPendingElementGroup(PendingEntry)}), which is also why that method
	 * re-registers each entry's {@link PendingEntry#setKeyFieldModel(ConfigFieldModel)} and key-change listener every
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

		if (_editable) {
			for (PendingEntry pending : _pending.entries()) {
				addChild(createPendingElementGroup(pending));
			}

			// Now that every pending entry has the key field of this render cycle, put the standing
			// complaints back on them. A rebuild replaces those field models, so a complaint placed
			// on the previous cycle's field would otherwise disappear with it - most visibly right
			// after confirming one of two entries that were being given the same key.
			_pending.checkKeys();

			// Add button at the bottom. Not rendered at all while !_editable - the requirement is
			// that no collection action is offered in view mode, not merely a disabled one.
			ReactButtonControl addButton =
				new ReactButtonControl(_context, "+ " + _value.label(),
					ctx -> {
						addElement();
						return HandlerResult.DEFAULT_RESULT;
					});
			addChild(addButton);
		}

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
		if (_editable) {
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
		}

		PropertyDescriptor keyProperty = _value.keyProperty(item);
		List<ReactControl> bodyChildren = createBodyChildren(item, keyProperty, null);

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, !expanded, "subtle", true,
			headerActions, bodyChildren);
		ReactControl header = createEntryHeader(item, keyProperty, null, label);
		group.setHeader(header);

		registerTitleListener(item, group, header instanceof ReactTextControl);

		return group;
	}

	/**
	 * The group for one entry of {@link ConfigPendingEntries}, rendered separately from
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} because a pending entry
	 * differs from a committed one in more than one dimension at once: it has no position in the
	 * edited collection to move it among (so no Move Up/Move Down button - there is nothing to
	 * move it among), it carries a Confirm button that a committed entry has no use for (see
	 * {@link ConfigPendingEntries#confirm(PendingEntry)}), its Remove button discards it (see
	 * {@link ConfigPendingEntries#discard(PendingEntry)}) instead of removing an entry from the collection, and
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
	 * {@link PendingEntry#setKeyFieldModel(ConfigFieldModel)} every time it runs - see
	 * {@link #rebuild(ConfigurationItem)}.
	 * </p>
	 */
	private ReactFormGroupControl createPendingElementGroup(PendingEntry pending) {
		ConfigurationItem entry = pending.entry();
		Label label = resolveElementLabel(entry);

		ReactButtonControl confirmButton = new ReactButtonControl(_context, "\u2713", ctx -> {
			ResKey refusal = _pending.confirm(pending);
			if (refusal == null) {
				return HandlerResult.DEFAULT_RESULT;
			}
			// Said out loud, not only at the key field: a type-keyed collection has no such field,
			// and the button would otherwise appear to do nothing.
			HandlerResult result = new HandlerResult();
			result.setErrorTitle(refusal);
			result.addErrorMessage(refusal);
			return result;
		});
		confirmButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		ReactButtonControl removeButton = new ReactButtonControl(_context, "\u2715", ctx -> {
			_pending.discard(pending);
			return HandlerResult.DEFAULT_RESULT;
		});
		removeButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		List<ReactControl> headerActions = List.of(confirmButton, removeButton);

		PropertyDescriptor keyProperty = _value.keyProperty(entry);
		List<ReactControl> bodyChildren = createBodyChildren(entry, keyProperty, pending);

		if (keyProperty != null) {
			// Re-examine every pending key on each keystroke, so that a key already spoken for is
			// complained about while it is being typed instead of only when the entry is confirmed.
			// All of them, not just this one: renaming one end of a clash has to clear the
			// complaint at the other end too.
			ConfigurationListener keyListener = change -> _pending.checkKeys();
			entry.addConfigurationListener(keyProperty, keyListener);
			_listeners.add(new ListenerRegistration(entry, keyProperty, keyListener));
		}

		ReactFormGroupControl group = new ReactFormGroupControl(
			_context, null, true, false, "subtle", true,
			headerActions, bodyChildren);
		ReactControl header = createEntryHeader(entry, keyProperty, pending, label);
		group.setHeader(header);

		registerTitleListener(entry, group, header instanceof ReactTextControl);

		if (_index != null) {
			// The form above cannot see a pending entry - it is not in the configuration - so it is
			// told here, and untold when this group goes away, whether because the entry was
			// confirmed, discarded, or merely re-rendered.
			_index.registerPending(pending);
			group.addCleanupAction(() -> _index.unregisterPending(pending));
		}

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
		// Only where the type is not the key. Where it is, it is the entry's key control and
		// belongs in the header with every other key - see #createEntryHeader.
		if (polymorphic && _choices.options().size() > 1 && !isTypeKeyed(item)) {
			bodyChildren.add(createTypeSelector(item, pending, false));
		}
		if (!polymorphic || isTypeSelected(item)) {
			bodyChildren.add(new ConfigEditorControl(_context, item,
				keyProperty == null ? Collections.emptySet() : Collections.singleton(keyProperty), false, _index,
				_editable, formModelOr(item)));
		}
		return bodyChildren;
	}

	/**
	 * Registers the dynamic label update on {@code item}'s title property, shared by
	 * {@link #createElementGroup(ConfigurationItem, int, int, boolean)} and
	 * {@link #createPendingElementGroup(PendingEntry)}.
	 */
	private void registerTitleListener(ConfigurationItem item, ReactFormGroupControl group,
			boolean headerIsTitle) {
		PropertyDescriptor titleProp = resolveTitleProperty(item);
		// Only where the header actually shows a title. Where the collection has a key, the header
		// is the key control itself, which shows the key without being told; swapping it for a text
		// would be wrong twice over, since the title property is the very key being typed into it -
		// every keystroke would fire this listener, and
		// ReactFormGroupControl#setHeader(ReactControl) disposes the control it replaces, tearing
		// the field out from under the caret.
		if (headerIsTitle && titleProp != null) {
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
	 *        non-{@code null}, the built model is also kept as {@link PendingEntry#setKeyFieldModel(ConfigFieldModel)}.
	 */
	private ReactControl createKeyField(ConfigurationItem entry, PropertyDescriptor keyProperty,
			PendingEntry pending) {
		// Pending already implies _editable in practice - a pending entry cannot exist while
		// !_editable, since that is exactly the state in which the add button that creates one is
		// never rendered (see #rebuild). Still combined explicitly rather than relied upon, so this
		// stays correct even if that invariant ever changes.
		boolean editable = _editable && pending != null;
		ConfigFieldModel model =
			ConfigControlService.getInstance().createModel(entry, keyProperty, formModelOr(entry));
		model.setEditable(editable);
		index(entry, keyProperty, model);
		if (pending != null) {
			pending.setKeyFieldModel(model);
		}
		ReactControl input = ConfigControlService.getInstance().createControl(_context, model);

		String label = Labels.propertyLabel(keyProperty, false);
		ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(_context, label,
			model.isMandatory(), false, null, null, LabelPosition.HIDDEN, false, true, input);
		// Detach when this specific field is disposed (on the next rebuild(), or when this whole
		// control is cleaned up) - the same lifecycle ConfigEditorControl#addCleanupAction(Runnable)
		// ties its own field models to, just anchored on the field's own chrome control instead of
		// on "this", since a fresh key field (and model) is built on every rebuild() cycle.
		chrome.addCleanupAction(model::detach);
		// ... and take the index registration back with it, so a discarded key field (an entry
		// removed, a pending entry given up on) stops answering for a row that is gone.
		if (_index != null) {
			chrome.addCleanupAction(() -> _index.unregister(entry, keyProperty));
		}

		String tooltip = Resources.getInstance().getString(keyProperty.labelKey(null).tooltip(), null);
		if (tooltip != null && !tooltip.isEmpty()) {
			chrome.setTooltip(tooltip, label, true);
		}
		return chrome;
	}

	/**
	 * Reports a field to the {@link ConfigFieldIndex} this editor was given, if it was given one -
	 * the counterpart of {@link ConfigEditorControl}'s own registration point, used here for the
	 * key field, which {@link ConfigEditorControl} never sees since it is rendered by this class
	 * and hidden from the nested editor over the entry's own properties.
	 */
	private void index(ConfigurationItem item, PropertyDescriptor property, ConfigFieldModel model) {
		if (_index != null) {
			_index.register(item, property, model);
		}
	}

	/**
	 * The control heading an entry's group: its key, where the collection has one, and its title
	 * otherwise.
	 *
	 * <p>
	 * The key names the entry, so it belongs where the entry is named - the group's own header row,
	 * the place a title would otherwise occupy. It is editable only while the entry is pending,
	 * because the key decides where the entry sits in the collection and re-keying a committed
	 * entry would re-index the collection under it. Once committed it stays, immutable, and an
	 * immutable field renders as plain text - so the header goes on reading as the title it also
	 * is. The classic declarative form settles the key the same way, in a dialog before the entry
	 * is added ({@code ListEditor}'s {@code AddDialog}), and renders it immutable afterwards.
	 * </p>
	 *
	 * <p>
	 * The key control is labelled for the tooltip but rendered {@link LabelPosition#HIDDEN}: the
	 * header is a single row shared with the collapse toggle and the Confirm and Remove buttons,
	 * which a label stacked above the input would push apart.
	 * </p>
	 */
	private ReactControl createEntryHeader(ConfigurationItem item, PropertyDescriptor keyProperty,
			PendingEntry pending, Label label) {
		if (keyProperty != null) {
			if (isTypeKeyed(item)) {
				// A key that is the entry's type is picked, not typed: offering it as text would
				// invite writing a class name by hand.
				if (_choices.hasOptions() && _choices.options().size() > 1) {
					return createTypeSelector(item, pending, true);
				}
			} else {
				return createKeyField(item, keyProperty, pending);
			}
		}
		return createHeaderControl(label);
	}

	private ReactTextControl createHeaderControl(Label label) {
		return new ReactTextControl(_context, label.text(), label.placeholder() ? PLACEHOLDER_CSS : null);
	}

	/**
	 * The entry's own type selector - a {@link SimpleSelectFieldModel}, disabled via
	 * {@link SimpleSelectFieldModel#setEditable(boolean)} rather than left out while {@link #_editable}
	 * is {@code false}, the same way {@link PolymorphicItemControl}'s top-level counterpart is: the
	 * currently chosen type must stay legible to the reader even where it may not be changed.
	 */
	private ReactFormFieldChromeControl createTypeSelector(ConfigurationItem item, PendingEntry pending,
			boolean inHeader) {
		List<Object> options = _choices.options();
		Object own = ownOptionIfMissing(item);

		// Two lists, because a key is a position and the two orders must not be the same one. What
		// a key resolves against only ever grows at the end, so every option keeps the key it had
		// no matter whether this entry contributes one of its own - a key is quoted in recorded
		// scripts and sent back by the client, and must mean the same thing for every entry of the
		// collection. What is displayed puts the entry's own type first.
		List<Object> resolution = new ArrayList<>(options);
		List<String> keys = new ArrayList<>(options.size() + 1);
		if (own != null) {
			resolution.add(own);
			keys.add(PolymorphicOptions.keyFor(options.size()));
		}
		for (int i = 0; i < options.size(); i++) {
			keys.add(PolymorphicOptions.keyFor(i));
		}
		String currentKey = PolymorphicOptions.keyForItem(resolution, _choices.mapping(), item);
		SimpleSelectFieldModel typeModel = new SimpleSelectFieldModel(currentKey, keys, false);
		typeModel.setMandatory(true);
		typeModel.setNullable(false);
		// In the header the type is the entry's key, so it follows the key's rule: settled before
		// the entry joins the collection, fixed afterwards. Elsewhere it is an ordinary property of
		// the entry and may be changed for as long as the form is editable.
		typeModel.setEditable(inHeader ? _editable && pending != null : _editable);

		LabelProvider labelProvider = PolymorphicOptions.indexLabelProvider(resolution);

		typeModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				onTypeChanged(item, PolymorphicOptions.optionForKey(resolution, (String) newValue), pending);
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
		return new ReactFormFieldChromeControl(_context, "Type", false, false, null, null,
			inHeader ? LabelPosition.HIDDEN : null, false, true, typeSelect);
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
			_pending.replaceEntry(pending, replacement);
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
	//
	// None of addElement/removeElement/moveUp/moveDown below carry their own !_editable guard, the
	// way a field's ReactFormFieldControl#acceptsClientValue() does. That is deliberate, not an
	// oversight: every one of them is private, reachable only from a button #rebuild and
	// #createElementGroup stop creating in the first place while !_editable (see their own
	// `if (_editable)` guards) - there is no rendered control, hence no command surface, for a
	// stale or forged client message to reach them through. If a future change ever offers one of
	// these operations through a control that stays rendered while !_editable (unlike every button
	// here today), that control needs its own guard - the same lookup for the type selector is
	// exactly why #createTypeSelector calls SimpleSelectFieldModel#setEditable(boolean) rather than
	// omitting itself.

	/**
	 * Adds a new element with default values.
	 *
	 * <p>
	 * For a keyed collection, the new element cannot be added directly: a keyed collection is
	 * indexed by a property of its entries, so {@link TypedConfiguration} would reject an entry
	 * whose key is empty (as every freshly constructed entry's is) with a technical
	 * {@link IllegalArgumentException} the moment a second one collided with it. The new entry
	 * therefore joins {@link ConfigPendingEntries} instead, rendered by
	 * {@link #createPendingElementGroup(PendingEntry)} until {@link ConfigPendingEntries#confirm(PendingEntry)}
	 * moves it into the collection. Any number of entries may be pending at once, independently -
	 * a keyed collection's constraint is on the entries actually in it, and a pending entry, by
	 * definition, is not.
	 * </p>
	 */
	private void addElement() {
		if (_value.isKeyed()) {
			_pending.start(newEntry());
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
		if (isTypeKeyed(item)) {
			// The key is the entry's own type, so the type is what it should be called - and by the
			// name the user knows it under, the same label the type selector shows, not the tag
			// name the XML uses. Falling through would read the key property, find it empty - a
			// configuration interface is not stored text - and settle for "<tag name> (empty)",
			// which says the wrong thing about an entry that is perfectly complete.
			return new Label(PolymorphicOptions.labelFor(item.descriptor().getConfigurationInterface()), false);
		}
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
	 * Whether the collection is keyed by the entry's own configuration interface.
	 *
	 * <p>
	 * Such a key is no input of the user's - it is the type, which a polymorphic collection already
	 * offers a selector for, and which every {@code annotations} property is keyed by. It is
	 * therefore neither shown as a field nor read as a title.
	 * </p>
	 */
	private boolean isTypeKeyed(ConfigurationItem item) {
		PropertyDescriptor keyProperty = _value.keyProperty(item);
		return keyProperty != null
			&& ConfigurationItem.CONFIGURATION_INTERFACE_NAME.equals(keyProperty.getPropertyName());
	}

	/**
	 * The option standing for the type the given entry actually has, where the collection does not
	 * offer that type itself - {@code null} where it does, or where no option can be made of it.
	 *
	 * <p>
	 * Options say what may be chosen for a <em>new</em> entry, and that set can be narrower than
	 * what exists - an annotation not marked for in-app use is left out of it. An entry that already
	 * carries such a type must still show it: a selector whose value is not among its options shows
	 * nothing at all, which says something false about an entry that is perfectly well typed, and
	 * invites losing the value on the next write.
	 * </p>
	 */
	private Object ownOptionIfMissing(ConfigurationItem item) {
		List<Object> options = _choices.options();
		if (_choices.mapping() == null
			|| PolymorphicOptions.keyForItem(options, _choices.mapping(), item) != null) {
			return null;
		}
		// May still be null - then there is nothing the selector could match the entry against, and
		// an empty selector is better than an option it would never select.
		return _choices.mapping().asOption(options, item);
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
		// 1. What the collection itself declares.
		PropertyDescriptor declared = _value.titleProperty(item);
		if (declared != null) {
			return declared;
		}

		// 2. @TitleProperty on the element type.
		TitleProperty titleOnType = item.descriptor().getConfigurationInterface().getAnnotation(TitleProperty.class);
		if (titleOnType != null && !titleOnType.name().isEmpty()) {
			PropertyDescriptor prop = item.descriptor().getProperty(titleOnType.name());
			if (prop != null) {
				return prop;
			}
		}

		// 3. The key property - asked of the collection, so it is the entry's own instance, the
		// same one every other key lookup here uses.
		PropertyDescriptor keyProp = _value.keyProperty(item);
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
