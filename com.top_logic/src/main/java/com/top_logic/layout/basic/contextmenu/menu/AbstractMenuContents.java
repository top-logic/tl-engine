/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.menu;

import static com.google.common.collect.Iterables.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.basic.CommandModel;

/**
 * Base class for composite contents of a {@link Menu}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMenuContents implements MenuPart, Iterable<MenuItem> {

	private final List<MenuItem> _contents = new ArrayList<>();

	/**
	 * The contents of this menu item.
	 */
	public List<MenuItem> getContents() {
		return _contents;
	}

	/**
	 * Adds the given item at the end.
	 */
	public void add(MenuItem item) {
		_contents.add(item);
	}

	/**
	 * Compatibility API, equivalent to {@link #add(MenuItem)} with a {@link CommandItem}.
	 */
	public void add(CommandModel button) {
		_contents.add(CommandItem.create(button));
	}

	/**
	 * Add all given {@link CommandModel}s
	 * 
	 * @see #add(CommandModel)
	 */
	public void addAll(Collection<CommandModel> buttons) {
		_contents.addAll(buttons.stream().map(button -> CommandItem.create(button)).collect(Collectors.toSet()));
	}

	/**
	 * Inserts the given {@link MenuItem} at the given position.
	 */
	public void insert(int i, MenuItem item) {
		_contents.add(i, item);
	}

	/**
	 * Whether this group has no entries (independent of their current visibility).
	 * 
	 * @see #hasVisibleEntries()
	 */
	public boolean isEmpty() {
		return _contents.isEmpty();
	}

	/**
	 * Whether some of the {@link #getContents()} is currently visible.
	 * 
	 * @see MenuItem#isVisible()
	 * @see #isEmpty()
	 */
	public boolean hasVisibleEntries() {
		for (MenuItem item : _contents) {
			if (item.isVisible()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<MenuItem> iterator() {
		return _contents.iterator();
	}

	@Override
	public Iterable<CommandModel> buttons() {
		return concat(transform(_contents, item -> item.buttons()));
	}

}
