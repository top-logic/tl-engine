/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.control;

import java.util.Set;

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
 */
public class DiagramSelectionBinding extends SelectionChannelBinding {

	private final FlowDiagramControl _control;

	private final FlowDiagramControl.SelectionListener _selectionListener = this::selectionChanged;

	private final FlowDiagramControl.ModelListener _modelListener = this::refreshed;

	/**
	 * Creates a {@link DiagramSelectionBinding} and applies the channel's current value to the
	 * diagram.
	 *
	 * @param control
	 *        The diagram whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public DiagramSelectionBinding(FlowDiagramControl control, ViewChannel channel) {
		super(channel);
		_control = control;

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

	@Override
	protected void detach() {
		_control.removeSelectionListener(_selectionListener);
		_control.removeModelListener(_modelListener);
	}

}
