/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;

/**
 * What a declared drop is: its {@link DropMode}, and the {@link DropReference objects} its location
 * refers to, in the order the {@link DropConfig#getRefuseIf() refusal function} receives them after
 * the dragged objects.
 *
 * <p>
 * The element declaring a drop assigns its signature: the mode decides where the drop applies, the
 * references are what the drop's script is told about the place it applies at. A refusal function
 * of a signature with the references {@code r1, ..., rn} has the form
 * {@code objects -> r1 -> ... -> rn -> reason}, and the drop may declare a channel for each of its
 * references.
 * </p>
 *
 * @param mode
 *        The mode the drop applies in.
 * @param references
 *        The objects a location of {@code mode} refers to, in argument order.
 */
public record DropSignature(DropMode mode, List<DropReference> references) {

	/**
	 * A drop on the control as a whole: it refers to the {@link DropReference#TARGET target}, which
	 * is {@code null} there.
	 */
	public static final DropSignature CONTROL = new DropSignature(DropMode.CONTROL, List.of(DropReference.TARGET));

	/**
	 * A drop onto a single item: it refers to that item as its {@link DropReference#TARGET target}.
	 */
	public static final DropSignature ONTO = new DropSignature(DropMode.ONTO, List.of(DropReference.TARGET));

	/**
	 * An insertion into a flat list of items: it refers to the item the dropped objects are inserted
	 * {@link DropReference#BEFORE before}, {@code null} for an insertion at the end.
	 */
	public static final DropSignature ORDERED_LIST =
		new DropSignature(DropMode.ORDERED, List.of(DropReference.BEFORE));

	/**
	 * An insertion into a tree: it refers to the object the dropped objects are inserted under as
	 * their {@link DropReference#PARENT parent}, and to the object they are inserted
	 * {@link DropReference#BEFORE before} among its children, {@code null} for an insertion as the
	 * last children.
	 */
	public static final DropSignature ORDERED_TREE =
		new DropSignature(DropMode.ORDERED, List.of(DropReference.PARENT, DropReference.BEFORE));

	/**
	 * The objects the given location refers to, in the order of the {@link #references()}.
	 *
	 * @param location
	 *        A location of this signature's {@link #mode()}.
	 */
	public List<Object> valuesAt(DropLocation location) {
		List<Object> result = new ArrayList<>(references.size());
		for (DropReference reference : references) {
			result.add(reference.value().apply(location));
		}
		return result;
	}

}
