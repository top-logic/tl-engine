/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TileLayoutListener;

/**
 * A {@link TLTreeModel} that uses {@link TileBreadcrumbTreeNode}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class TileBreadcrumbTreeModel extends AbstractMutableTLTreeModel<TileBreadcrumbTreeNode>
		implements SelectionModelOwner {

	private final DefaultSingleSelectionModel _selectionModel;

	private boolean _disableIncomingEvents;

	private boolean _disableOutgoingEvents;

	private final List<Runnable> _listenerCleanups = list();

	/**
	 * Creates a {@link TileBreadcrumbTreeModel}.
	 * 
	 * @param builder
	 *        Is not allowed to be null.
	 * @param tileComponent
	 *        Is not allowed to be null.
	 */
	public TileBreadcrumbTreeModel(TreeBuilder<TileBreadcrumbTreeNode> builder,
			TileContainerComponent tileComponent) {
		super(requireNonNull(builder), requireNonNull(tileComponent));
		_selectionModel = new DefaultSingleSelectionModel(this);
		updateSelection();
		addNodeToTreeSelectionUpdater();
		addTreeToNodeSelectionUpdater();
		addTileUpdateListener(tileComponent);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	/**
	 * The {@link SingleSelectionModel} represents the innermost selection in the breadcrumb.
	 */
	public SingleSelectionModel getSingleSelectionModel() {
		return _selectionModel;
	}

	/** Useful for log messages and other debug output. */
	String getSelectionLabelPath() {
		List<String> selections = list();
		TileBreadcrumbTreeNode node = getRoot();
		while (node != null) {
			selections.add(label(node));
			node = node.getSelectedChild();
		}
		return String.join(" > ", selections);
	}

	private void updateSelection() {
		TileBreadcrumbTreeNode node = getRoot();
		while (node.getSelectedChild() != null) {
			node = node.getSelectedChild();
		}
		getSingleSelectionModel().setSingleSelection(node);
	}

	private void addNodeToTreeSelectionUpdater() {
		/* This listener is not deregistered, as it is only registered at the nodes of the tree.
		 * The listener is therefore garbage collected when the tree is. */
		new TileBreadcrumbNodeToTreeSelectionUpdater(this).addListenerRecursively(getRoot());
	}

	private void addTreeToNodeSelectionUpdater() {
		/* This listener is not deregistered, as it is only registered at the selection model of
		 * the tree. The listener is therefore garbage collected when the tree is. */
		getSingleSelectionModel().addSingleSelectionListener(
			(model, oldSelection, newSelection) -> processOutgoingEvent(
				() -> onSelectionChange(newSelection)));
	}

	private void onSelectionChange(Object newSelection) {
		if (newSelection == null) {
			clearSelection(getRoot());
			logTreeToNodeSelectionChange();
			return;
		}
		TileBreadcrumbTreeNode toSelect = (TileBreadcrumbTreeNode) newSelection;
		if (getRoot().equals(toSelect)) {
			clearSelection(getRoot());
			logTreeToNodeSelectionChange();
			return;
		}
		toSelect.getParent().getChildSelection().setSingleSelection(toSelect);
		clearSelection(toSelect);
		logTreeToNodeSelectionChange();
	}

	private void logTreeToNodeSelectionChange() {
		logDebug(() -> "Changing selection to: " + getSelectionLabelPath());
	}

	private void addTileUpdateListener(TileContainerComponent tileContainer) {
		tileContainer.addListener(TileLayoutListener.TILE_LAYOUT_CHANGED, this::layoutChanged);
	}

	private Bubble layoutChanged(TileContainerComponent sender, TileLayout tileOld, TileLayout tileNew) {
		processIncomingEvent(() -> onLayoutChange(sender, tileOld, tileNew));
		return Bubble.BUBBLE;
	}

	private void onLayoutChange(TileContainerComponent sender, TileLayout tileOld, TileLayout tileNew) {
		logTileSelectionChange(sender, tileOld, tileNew);
		updateTree();
	}

	private void logTileSelectionChange(TileContainerComponent sender, TileLayout tileOld, TileLayout tileNew) {
		logDebug(() -> "Selection of tile component '" + label(sender) + "' changed from '" + label(tileOld)
			+ "' to '" + label(tileNew) + "'.");
	}

	void onIncomingComponentSelection(ComponentChannel sender, Object oldValue, Object newValue) {
		if (Objects.equals(oldValue, newValue)) {
			return;
		}
		processIncomingEvent(() -> onIncomingComponentSelectionInternal(sender, oldValue, newValue));
	}

	private void onIncomingComponentSelectionInternal(ComponentChannel sender, Object oldValue, Object newValue) {
		logDebug(() -> "Selection for component '" + label(sender.getComponent()) + "' changed from '"
			+ label(oldValue) + "' to '" + label(newValue) + "'.");
		updateTree();
	}

	private static String label(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	private void clearSelection(TileBreadcrumbTreeNode node) {
		if (node == null) {
			return;
		}
		clearSelection(node.getSelectedChild());
		node.getChildSelection().setSingleSelection(null);
	}

	void addListenerCleanup(Runnable listenerCleanup) {
		_listenerCleanups.add(listenerCleanup);
	}

	void updateTree() {
		deregisterListeners();
		getRoot().updateNodeStructure();
		getRoot().updateChildSelectionRecursively();
	}

	private void deregisterListeners() {
		for (Runnable cleanup : _listenerCleanups) {
			cleanup.run();
		}
		_listenerCleanups.clear();
	}

	/**
	 * Disable listener on the components that apply changes those changes to the breadcrumb.
	 */
	private boolean getDisableIncomingEvents() {
		return _disableIncomingEvents;
	}

	private void setDisableIncomingEvents(boolean disableIncomingEvents) {
		_disableIncomingEvents = disableIncomingEvents;
	}

	/**
	 * Disable listener on the breadcrumb selection models that apply changes to the components.
	 */
	private boolean getDisableOutgoingEvents() {
		return _disableOutgoingEvents;
	}

	private void setDisableOutgoingEvents(boolean disableOutgoingEvents) {
		_disableOutgoingEvents = disableOutgoingEvents;
	}

	/**
	 * Disables the listeners of the opposite direction while the event is processed.
	 */
	void processIncomingEvent(Runnable eventHandler) {
		if (getDisableIncomingEvents()) {
			return;
		}
		boolean oldValue = getDisableOutgoingEvents();
		setDisableOutgoingEvents(true);
		try {
			/* Make sure the gui isn't destroyed by a bug in the breadcrumb. */
			ExceptionUtil.runAndLogErrors(eventHandler::run, TileBreadcrumbTreeModel.class);
		} finally {
			setDisableOutgoingEvents(oldValue);
		}
	}

	/**
	 * Disables the listeners of the opposite direction while the event is processed.
	 */
	void processOutgoingEvent(Runnable eventHandler) {
		if (getDisableOutgoingEvents()) {
			return;
		}
		boolean oldValue = getDisableIncomingEvents();
		setDisableIncomingEvents(true);
		try {
			/* Make sure the gui isn't destroyed by a bug in the breadcrumb. */
			ExceptionUtil.runAndLogErrors(eventHandler::run, TileBreadcrumbTreeModel.class);
		} finally {
			setDisableIncomingEvents(oldValue);
		}
	}

	private static void logDebug(Supplier<String> message) {
		Logger.debug(message, TileBreadcrumbTreeModel.class);
	}

}
