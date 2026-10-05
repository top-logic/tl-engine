/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;

/**
 * Two-way binding between the selection a selector displays and a {@link ViewChannel}.
 *
 * <p>
 * The channel is the shared selection: several selectors, a detail panel and a command's
 * executability can all be bound to the same one. A selector is therefore only one of its writers,
 * and it writes it in exactly two situations:
 * </p>
 *
 * <ul>
 * <li>The selection changed in the selector itself (the user picked an element): one selected key
 * is written as that object, several as the {@link Set} of keys, none as <code>null</code>.</li>
 * <li>A key this selector displayed as selected is gone after a {@link #refreshed() refresh} of its
 * elements - deleted, filtered away by a changed input, no longer matching the element criterion.
 * The value names something nobody can see any more, so the surviving selection is written in its
 * place: the one key that is left as that object, several as the {@link Set} of them, none as
 * <code>null</code>.</li>
 * </ul>
 *
 * <p>
 * The elements of a selector are its data, not the part of it the selector currently shows: an
 * element a filter hides, or one inside a collapsed group, is still an element of the selector. It
 * takes a channel value naming it as its selection, and keeps that selection as long as it is part
 * of the data - which it shows once it is visible again. Only an element gone from the data gives
 * the selection up.
 * </p>
 *
 * <p>
 * A value the selector merely does not have an element for means no more than "nothing selected
 * here": the selector shows no selection and leaves the channel alone. Clearing it would destroy
 * what another writer put there - the element a second selector over a different set of elements
 * selected, or the object a create command wrote before this selector's elements caught up with it.
 * </p>
 *
 * <p>
 * A value that is a {@link Collection} is the selection of several elements. A selector that
 * {@link #canDisplaySeveral() can show several} selected elements displays the ones it has for
 * those keys and ignores the rest - a collection none of whose keys it has is "nothing selected
 * here" like any other foreign value, and leaves the channel alone. A selector showing a single
 * selected element cannot display such a value at all: it shows no selection and leaves the channel
 * alone for the same reason.
 * </p>
 *
 * <p>
 * A selector that {@link #keepsUndisplayedSelection() keeps the undisplayed selection} - a diagram
 * showing only some of the objects another selector lists - treats the channel value as two parts:
 * the keys it has an element for, and the rest. It only ever changes the first part: a refresh
 * drops the vanished keys it displayed, and a selection made in it incrementally (a click with a
 * modifier key, see {@link #selectionChanged(Set, boolean)}) adds or removes its own keys. The rest
 * stays on the channel, unless it names a deleted object, and is only replaced by a selection that
 * replaces the whole selection (a plain click).
 * </p>
 *
 * <p>
 * The selector's own echo of an applied channel value never writes the channel back: a
 * {@link #selectionChanged(Set)} reported while the value is being applied is not a selection made
 * in the selector.
 * </p>
 *
 * <p>
 * A write the channel refuses - unsaved changes in a form the selection would replace - propagates
 * out of the selector's selection push, which restores the selection it displayed before. The
 * binding therefore records the displayed keys only once the write has gone through, so what it
 * remembers is what the selector shows.
 * </p>
 *
 * <p>
 * A subclass connects the binding to its selector: it stores the selector and registers its
 * selection listener in its constructor and calls {@link #attach()} as the last statement there -
 * the binding reads the selector from {@link #attach()} on, so the subclass' fields must hold it by
 * then. {@link #detach()} gives the listener back up.
 * </p>
 */
public abstract class SelectionChannelBinding {

	private final ViewChannel _channel;

	private final ViewChannel.ChannelListener _channelListener = (sender, oldValue, newValue) -> applyChannelValue();

	/**
	 * Whether a channel value is currently being applied, so the selector's echo is not written
	 * back.
	 */
	private boolean _applyingFromChannel;

	/** The keys the selector last displayed as selected. */
	private Set<Object> _displayedKeys = Set.of();

	/**
	 * Creates a {@link SelectionChannelBinding}.
	 *
	 * @param channel
	 *        The channel holding the selection.
	 */
	protected SelectionChannelBinding(ViewChannel channel) {
		_channel = channel;
	}

	/**
	 * Starts listening to the channel and displays its current value.
	 *
	 * <p>
	 * To be called as the last statement of the subclass constructor, once the selector is
	 * reachable through {@link #getSelectedKeys()}, {@link #displaySelection(Set)} and
	 * {@link #canDisplaySeveral()}.
	 * </p>
	 */
	protected final void attach() {
		_channel.addListener(_channelListener);

		applyChannelValue();
	}

	/**
	 * Re-establishes the binding after the selector's elements have been refreshed.
	 *
	 * <p>
	 * To be called by the owner of the selector directly after the refresh: the channel value is
	 * applied again, so an element that appears only now (the just-created object the channel
	 * already names) is selected; and when an element this selector displayed as selected is no
	 * longer among its elements, the surviving selection is written to the channel -
	 * <code>null</code> when nothing of it is left.
	 * </p>
	 */
	public void refreshed() {
		// The refresh has silently dropped vanished keys from the selector's selection, so what the
		// selector still holds tells which of the displayed elements survived. The refresh itself
		// does not report a selection change, hence the last reported selection is the one
		// displayed before it.
		Set<Object> displayedBefore = _displayedKeys;
		Set<Object> survivors = new LinkedHashSet<>(getSelectedKeys());
		boolean displayedElementGone = !displayedBefore.isEmpty() && !survivors.containsAll(displayedBefore);
		_displayedKeys = survivors;

		// The part of the value this selector did not display before is not its to adjust, unless
		// it names deleted objects.
		Set<Object> undisplayed = new LinkedHashSet<>();
		boolean deletedKept = false;
		if (keepsUndisplayedSelection()) {
			for (Object key : channelKeys()) {
				if (displayedBefore.contains(key)) {
					continue;
				}
				if (isDeleted(key)) {
					deletedKept = true;
				} else {
					undisplayed.add(key);
				}
			}
		}

		applyChannelValue();

		if (displayedElementGone || deletedKept) {
			// The value names elements nobody can see any more, so it is replaced by what is left
			// of the selection - which is no selection at all when every displayed element
			// vanished.
			undisplayed.addAll(survivors);
			writeSelection(undisplayed);
		}
	}

	/**
	 * Detaches from the selector and the channel.
	 *
	 * <p>
	 * To be called by the owner when the selector goes away.
	 * </p>
	 */
	public final void dispose() {
		_channel.removeListener(_channelListener);

		detach();
	}

	/**
	 * Reports a selection the selector now displays, writing it to the channel when it is a
	 * selection made in the selector.
	 *
	 * <p>
	 * To be called by the subclass from the selector's selection listener, also when the selection
	 * was pushed by this binding: the echo of an applied value is recognized here and not written
	 * back.
	 * </p>
	 *
	 * <p>
	 * A refused write leaves the method through the veto, so the recorded keys stay the ones the
	 * selector displays after it has restored the refused change.
	 * </p>
	 *
	 * @param selectedKeys
	 *        The keys the selector displays as selected.
	 */
	protected final void selectionChanged(Set<Object> selectedKeys) {
		selectionChanged(selectedKeys, true);
	}

	/**
	 * Reports a selection the selector now displays, telling whether it replaces the whole
	 * selection.
	 *
	 * <p>
	 * Like {@link #selectionChanged(Set)}, but for a selector that
	 * {@link #keepsUndisplayedSelection() keeps the undisplayed selection}: a change that does not
	 * replace the selection (a click with a modifier key, or no selection change at all) keeps the
	 * keys of the channel value the selector has no element for.
	 * </p>
	 *
	 * @param selectedKeys
	 *        The keys the selector displays as selected.
	 * @param replacing
	 *        Whether the user replaced the whole selection, so that also the keys of the channel
	 *        value the selector has no element for are dropped.
	 */
	protected final void selectionChanged(Set<Object> selectedKeys, boolean replacing) {
		if (!_applyingFromChannel) {
			if (replacing || !keepsUndisplayedSelection()) {
				writeSelection(selectedKeys);
			} else {
				writeSelection(withUndisplayedKeys(selectedKeys));
			}
		}
		_displayedKeys = new LinkedHashSet<>(selectedKeys);
	}

	/**
	 * The given keys the selector displays, together with the keys of the channel value it has no
	 * element for.
	 */
	private Set<Object> withUndisplayedKeys(Set<Object> selectedKeys) {
		Set<Object> result = new LinkedHashSet<>();
		if (canDisplaySeveral() || selectedKeys.isEmpty()) {
			for (Object key : channelKeys()) {
				if (!hasElement(key) && !isDeleted(key)) {
					result.add(key);
				}
			}
		}
		// Else a displayed key replaces an undisplayed one, since the selector shows only one.
		result.addAll(selectedKeys);
		return result;
	}

	/**
	 * The keys of the current channel value.
	 */
	private Set<Object> channelKeys() {
		Object value = _channel.get();
		if (value instanceof Collection<?> keys) {
			return new LinkedHashSet<>(keys);
		}
		return value == null ? Set.of() : Set.of(value);
	}

	private static boolean isDeleted(Object key) {
		return key instanceof TLObject obj && !obj.tValid();
	}

	/**
	 * Writes the given keys to the channel: one as that object, several as their {@link Set}, none
	 * as <code>null</code>.
	 */
	private void writeSelection(Set<Object> selectedKeys) {
		if (selectedKeys.size() == 1) {
			_channel.set(selectedKeys.iterator().next());
		} else if (selectedKeys.isEmpty()) {
			_channel.set(null);
		} else {
			_channel.set(new LinkedHashSet<>(selectedKeys));
		}
	}

	/**
	 * Displays the channel's current value as the selector's selection, showing nothing where the
	 * selector has no such element.
	 */
	private void applyChannelValue() {
		Object value = _channel.get();
		if (isDisplayed(value)) {
			// The selector already shows this selection: it is the selector's own, echoed back
			// through the channel. Re-applying it would reduce a multiple selection to nothing and
			// scroll to the element the user just picked.
			return;
		}
		_applyingFromChannel = true;
		try {
			displaySelection(requestedKeys(value));
		} finally {
			_applyingFromChannel = false;
		}
	}

	/**
	 * The keys the given channel value asks the selector to display as selected.
	 */
	private Set<?> requestedKeys(Object value) {
		if (value instanceof Collection<?> keys) {
			// A selector showing one selected element at a time has no way of displaying a
			// selection of several.
			return canDisplaySeveral() ? new LinkedHashSet<Object>(keys) : Set.of();
		}
		return value == null ? Set.of() : Set.of(value);
	}

	/**
	 * Whether the selector's current selection is exactly the given channel value.
	 */
	private boolean isDisplayed(Object value) {
		Set<Object> selectedKeys = getSelectedKeys();
		if (value == null) {
			return selectedKeys.isEmpty();
		}
		if (value instanceof Collection<?> keys) {
			return selectedKeys.equals(new LinkedHashSet<>(keys));
		}
		return selectedKeys.size() == 1 && selectedKeys.contains(value);
	}

	/**
	 * The keys of the elements the selector currently displays as selected.
	 */
	protected abstract Set<Object> getSelectedKeys();

	/**
	 * Displays exactly the elements the selector has for the given keys as its selection, and no
	 * others.
	 *
	 * @param keys
	 *        The keys to display as selected, empty to display no selection. Keys the selector has
	 *        no element for are left out.
	 */
	protected abstract void displaySelection(Set<?> keys);

	/**
	 * Whether the selector can display more than one selected element at a time.
	 */
	protected abstract boolean canDisplaySeveral();

	/**
	 * Gives up the selection listener the subclass registered with the selector.
	 */
	protected abstract void detach();

	/**
	 * Whether keys of the channel value that this selector has no element for are kept, when the
	 * selection changes in the selector or the selector's elements are refreshed.
	 *
	 * <p>
	 * By default, a selection made in the selector and a refresh dropping a displayed element
	 * write only the keys the selector displays. A selector showing only some of the objects of a
	 * shared selection - a diagram rendering a part of the objects a table lists - returns
	 * <code>true</code> here, reports its selection changes through
	 * {@link #selectionChanged(Set, boolean)} and answers {@link #hasElement(Object)}.
	 * </p>
	 */
	protected boolean keepsUndisplayedSelection() {
		return false;
	}

	/**
	 * Whether the selector has an element for the given key.
	 *
	 * <p>
	 * Only asked when the selector {@link #keepsUndisplayedSelection() keeps the undisplayed
	 * selection}.
	 * </p>
	 */
	protected boolean hasElement(Object key) {
		return true;
	}

}
