/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.control;

import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.SelectionChannelBinding;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.SelectableBox;

/**
 * {@link SelectionChannelBinding} for the selection of a {@link FlowDiagramControl}.
 *
 * <p>
 * The keys of the binding are the user objects the diagram elements carry, so a value of the
 * channel selects the elements built for that object - however many of them there are, and in
 * whichever diagram element type ({@link SelectableBox} node, edge). An element without a user
 * object is not part of the shared selection at all: nothing names it, and it names nothing.
 * </p>
 *
 * <p>
 * A diagram displays several selected elements when it is a
 * {@link Diagram#isMultiSelect() multi-select} one. Whichever elements are selected, the client
 * learns about the marking through the diagram patch the control pushes.
 * </p>
 *
 * <p>
 * A diagram whose empty area stands for an object - the one it displays - writes that object when
 * the user leaves no element selected: a click or a context menu on the empty area, or a click with
 * {@code Ctrl} giving up the last selected element, see {@link #noSelectionValue()}.
 * </p>
 */
public class DiagramSelectionBinding extends SelectionChannelBinding {

	private final FlowDiagramControl _control;

	private final FlowDiagramControl.SelectionListener _selectionListener = this::selectionChanged;

	private final FlowDiagramControl.ModelListener _modelListener = this::refreshed;

	private final Supplier<?> _emptyAreaSelection;

	/**
	 * Creates a {@link DiagramSelectionBinding} whose empty area selects nothing, and applies the
	 * channel's current value to the diagram.
	 *
	 * @param control
	 *        The diagram whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public DiagramSelectionBinding(FlowDiagramControl control, ViewChannel channel) {
		this(control, channel, () -> null);
	}

	/**
	 * Creates a {@link DiagramSelectionBinding} and applies the channel's current value to the
	 * diagram.
	 *
	 * @param control
	 *        The diagram whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 * @param emptyAreaSelection
	 *        The object the empty area of the diagram stands for, asked whenever the user leaves no
	 *        element selected; it may answer <code>null</code> for no object.
	 */
	public DiagramSelectionBinding(FlowDiagramControl control, ViewChannel channel,
			Supplier<?> emptyAreaSelection) {
		super(channel);
		_control = control;
		_emptyAreaSelection = emptyAreaSelection;

		control.addSelectionListener(_selectionListener);
		control.addModelListener(_modelListener);

		attach();
	}

	@Override
	protected Set<Object> getSelectedKeys() {
		return _control.getSelectedUserObjects();
	}

	@Override
	protected void displaySelection(Set<?> keys) {
		_control.selectUserObjects(keys);
	}

	@Override
	protected boolean canDisplaySeveral() {
		return _control.isMultiSelect();
	}

	/**
	 * A diagram often displays only some of the objects of a shared selection - the others are
	 * listed by another selector only. Those are kept, unless the user replaces the selection by a
	 * plain click in the diagram.
	 */
	@Override
	protected boolean keepsUndisplayedSelection() {
		return true;
	}

	@Override
	protected boolean hasElement(Object key) {
		return _control.hasElementFor(key);
	}

	/**
	 * The object the empty area of the diagram stands for, <code>null</code> if none.
	 */
	@Override
	protected Object noSelectionValue() {
		return _emptyAreaSelection.get();
	}

	@Override
	protected void detach() {
		_control.removeSelectionListener(_selectionListener);
		_control.removeModelListener(_modelListener);
	}

}
