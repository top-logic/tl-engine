/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;

/**
 * The state behind an edit mode for a {@link ConfigurationItem}: the original, a working copy, and
 * the transitions between them.
 *
 * <p>
 * In view mode, {@link #edited()} is the {@link #original()} itself, so a change through it is
 * immediate. {@link #startEditing()} switches to edit mode by copying the root of the original's
 * configuration tree - found by {@link ConfigItemPath#to(ConfigurationItem)} - and resolving the
 * part corresponding to the original inside that copy - via
 * {@link ConfigItemPath#resolveIn(ConfigurationItem)}. From then on {@link #edited()} is that part
 * of the copy, and everything typed into it stays there until {@link #apply()} carries it back into
 * {@link #original()}, or {@link #cancelEditing()} drops it. Either way, {@link #edited()} is the
 * {@link #original()} again once edit mode ends.
 * </p>
 *
 * <p>
 * That one method, {@link #edited()}, is the whole trick: the editor built over it keeps writing
 * straight through to whatever it was handed, and this class decides what that was.
 * </p>
 */
public class ConfigFormModel {

	private final ConfigurationItem _original;

	/** The part inside the copied root that corresponds to {@link #_original}, while editing. */
	private ConfigurationItem _copy;

	private final List<Runnable> _listeners = new ArrayList<>();

	/**
	 * Creates a {@link ConfigFormModel} in view mode, working on the given item.
	 *
	 * @param original
	 *        The item to edit. See {@link #original()}.
	 */
	public ConfigFormModel(ConfigurationItem original) {
		_original = original;
	}

	/**
	 * The item this model was created for.
	 *
	 * <p>
	 * This is always the item {@link #apply()} writes back into, no matter whether the model is
	 * currently {@link #isEditMode() in edit mode}.
	 * </p>
	 */
	public ConfigurationItem original() {
		return _original;
	}

	/**
	 * The item an editor should be built over.
	 *
	 * <p>
	 * This is {@link #original()} in view mode, and while {@link #isEditMode() in edit mode} the
	 * part of the working copy that corresponds to it.
	 * </p>
	 */
	public ConfigurationItem edited() {
		return _copy != null ? _copy : _original;
	}

	/**
	 * Whether this model currently works on a copy - see {@link #edited()}.
	 */
	public boolean isEditMode() {
		return _copy != null;
	}

	/**
	 * Switches to edit mode: from now on, {@link #edited()} is a working copy of
	 * {@link #original()}, starting out as a snapshot of what {@link #original()} currently holds.
	 */
	public void startEditing() {
		ConfigItemPath path = ConfigItemPath.to(_original);
		ConfigurationItem rootCopy = TypedConfiguration.copy(path.root());
		_copy = path.resolveIn(rootCopy);
		notifyListeners();
	}

	/**
	 * Carries the content of {@link #edited()} back into {@link #original()}, and leaves edit
	 * mode.
	 *
	 * <p>
	 * Only the edited part's content is copied back - the rest of the working copy exists solely
	 * so the edited part can navigate out of itself, not to be written back.
	 * </p>
	 */
	public void apply() {
		InstantiationContext context = new DefaultInstantiationContext(ConfigFormModel.class);
		ConfigCopier.copyContent(context, _copy, _original);
		_copy = null;
		notifyListeners();
	}

	/**
	 * Drops the working copy without touching {@link #original()}, and leaves edit mode.
	 */
	public void cancelEditing() {
		_copy = null;
		notifyListeners();
	}

	/**
	 * Registers a listener to be {@link Runnable#run() run} whenever edit mode is entered or left.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes a listener previously registered with {@link #addListener(Runnable)}.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	public void removeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	private void notifyListeners() {
		for (Runnable listener : new ArrayList<>(_listeners)) {
			listener.run();
		}
	}

}
