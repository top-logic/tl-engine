/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * The {@link DropTarget} of a declared table: it accepts what the table's drops declare, and applies
 * a drop by running the action chain of the drop that matches it.
 *
 * <p>
 * Acceptance is decided over the model here, where the model is known, and reaches the client as a
 * plain set of type tags: a declared type contributes its own qualified name and those of all its
 * subtypes, so the client's comparison of tags accepts a subtype exactly as this side does. The
 * client thereby offers only a drop that can apply, and the drop that arrives is matched against the
 * same tags again - per declared drop this time, to find the one that applies.
 * </p>
 *
 * @see DropTargetMode
 */
public class TableDropBinding implements DropTarget {

	/**
	 * One declared drop of the table.
	 *
	 * @param acceptedTags
	 *        The qualified names of the accepted types and of their subtypes.
	 * @param mode
	 *        Whether the table or a single row is the target.
	 * @param targetChannel
	 *        The channel the target row is written to before the actions run, or {@code null} if
	 *        the drop declares none. A {@link DropTargetMode#TABLE} drop writes {@code null}.
	 * @param actions
	 *        The action chain applying the drop, with the dropped objects as its input.
	 */
	public record Drop(Set<String> acceptedTags, DropTargetMode mode, ViewChannel targetChannel,
			List<ViewAction> actions) {
		// Value type.
	}

	private final ReactContext _context;

	private final List<Drop> _drops;

	private final Set<String> _acceptedTags;

	private final boolean _dropOnRows;

	/**
	 * Creates a {@link TableDropBinding}.
	 *
	 * @param context
	 *        The context the action chains run in.
	 * @param drops
	 *        The declared drops, in declaration order - which is the order a drop is matched
	 *        against them in.
	 */
	public TableDropBinding(ReactContext context, List<Drop> drops) {
		_context = context;
		_drops = drops;
		Set<String> tags = new LinkedHashSet<>();
		boolean onRows = false;
		for (Drop drop : drops) {
			tags.addAll(drop.acceptedTags());
			onRows |= drop.mode() == DropTargetMode.ROW;
		}
		_acceptedTags = tags;
		_dropOnRows = onRows;
	}

	/**
	 * The qualified names of the accepted types and of their subtypes, over all declared drops.
	 */
	@Override
	public Collection<String> acceptedTypes() {
		return _acceptedTags;
	}

	@Override
	public boolean dropOnRows() {
		return _dropOnRows;
	}

	/**
	 * Applies the drop through the first declared drop that matches it.
	 *
	 * <p>
	 * A drop made on a row is offered to the {@link DropTargetMode#ROW row} drops first, and falls
	 * back to a {@link DropTargetMode#TABLE table} drop when none of them accepts the drag - a table
	 * whose rows are targets for one kind of object still accepts another kind as a whole, wherever
	 * the pointer happened to be. A drop nothing accepts does nothing.
	 * </p>
	 */
	@Override
	public void onDrop(DropEvent event) {
		String tag = draggedType(event);
		if (tag == null) {
			return;
		}
		List<?> objects = event.objects();
		if (event.target() != null) {
			Drop rowDrop = matching(DropTargetMode.ROW, tag);
			if (rowDrop != null) {
				apply(rowDrop, objects, event.target());
				return;
			}
		}
		Drop tableDrop = matching(DropTargetMode.TABLE, tag);
		if (tableDrop != null) {
			apply(tableDrop, objects, null);
		}
	}

	private Drop matching(DropTargetMode mode, String tag) {
		for (Drop drop : _drops) {
			if (drop.mode() == mode && drop.acceptedTags().contains(tag)) {
				return drop;
			}
		}
		return null;
	}

	/**
	 * The type tag of what was dropped, or {@code null} if the drop says nothing about it.
	 *
	 * <p>
	 * The control the objects were dragged out of is the authority on what it drags, so its declared
	 * tag is what the drops are matched against - the very tag the client offered the drop by. A drop
	 * replayed from a recorded script names no such control; there the objects say what they are, and
	 * they have to agree on it, so that a drag of several rows is never applied in part.
	 * </p>
	 */
	private static String draggedType(DropEvent event) {
		if (event.source() instanceof DragSourceControl source && source.dragType() != null) {
			return source.dragType();
		}
		String tag = null;
		for (Object object : event.objects()) {
			if (!(object instanceof TLObject model)) {
				return null;
			}
			String objectTag = TLModelUtil.qualifiedName(model.tType());
			if (tag == null) {
				tag = objectTag;
			} else if (!tag.equals(objectTag)) {
				return null;
			}
		}
		return tag;
	}

	private void apply(Drop drop, List<?> objects, Object target) {
		ViewChannel targetChannel = drop.targetChannel();
		if (targetChannel != null) {
			targetChannel.set(target);
		}
		ViewActionChain.run(_context, drop.actions(), objects, null);
	}

	/**
	 * The tags a declared type is accepted under: its own qualified name and those of its subtypes,
	 * so that the client accepts a subtype by comparing tags.
	 *
	 * @param types
	 *        The declared accepted types.
	 */
	public static Set<String> tagsOf(Collection<? extends TLType> types) {
		Set<String> tags = new LinkedHashSet<>();
		for (TLType type : types) {
			tags.add(TLModelUtil.qualifiedName(type));
			if (type instanceof TLClass generalization) {
				for (TLClass specialization : TLModelUtil.getTransitiveSpecializations(generalization)) {
					tags.add(TLModelUtil.qualifiedName(specialization));
				}
			}
		}
		return tags;
	}

}
