/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.RowArrangement;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.Column;
import com.top_logic.table.RowSource;
import com.top_logic.table.impl.TreeRowSource;

/**
 * {@link RowArrangement} of a {@link RowSetTableControl} whose rows form a tree: the rows are the
 * nodes of a {@link RowTree}, keyed by the stored objects of their nodes.
 *
 * <p>
 * The edit session reads no rows when it starts. A row joins it when the table shows it: while the
 * session runs, the row object of a node is the row the session edits its stored object through,
 * which the session {@link RowSetEditSession#adoptRow(TLObject) adopts} the first time the row is
 * computed - so opening a row in edit mode adopts its children. Outside the session the row object
 * is the stored object itself.
 * </p>
 *
 * <p>
 * Every table the control builds - on each switch between view and edit mode - displays the same
 * tree: its rows are expanded where the rows of the table before were, and the selection channel is
 * applied to it again, so the place the user works at survives the switch. While the session runs,
 * the tree is frozen: it is not observed, so it changes neither with the model nor with the inputs.
 * Outside the session, the tree follows them through the observation of the {@link RowTree}, which
 * takes up what changed while it was frozen when it observes again.
 * </p>
 *
 * <p>
 * The selection is that of a tree, see {@link TreeSelectionBinding}. While the session runs, the
 * channel holds the rows the session edits - as the selection of a flat row-editing table does -,
 * outside it the stored objects; either is found in the tree.
 * </p>
 */
public class TreeRowArrangement implements RowArrangement {

	private final RowTree _tree;

	private final Set<TLStructuredType> _observedTypes;

	private final Collection<? extends ViewChannel> _rowIndependentInputs;

	private final Collection<? extends ViewChannel> _cellInputs;

	/**
	 * The source created last, {@code null} before
	 * {@link #createSource(List, RowSetEditSession, List)}.
	 */
	private TreeRowSource<DefaultTreeUINode, TLObject> _source;

	/** The edit session the rows of {@link #_source} are edited in, {@code null} outside one. */
	private RowSetEditSession _session;

	/** What the tree reports its changes to, {@code null} while it is not observed for a table. */
	private Display _display;

	/** Whether the observation of the {@link #_tree} was set up. */
	private boolean _observing;

	/**
	 * Creates a {@link TreeRowArrangement}.
	 *
	 * @param tree
	 *        The tree whose nodes are the rows.
	 * @param observedTypes
	 *        Types whose created or changed objects are placed in the tree even while it has no node
	 *        for them, see {@link RowTree#observe(ObservableTreeModel.Display, Set, Collection, Collection)}.
	 * @param rowIndependentInputs
	 *        The input channels whose changes cannot change the tree.
	 * @param cellInputs
	 *        Channels the cells read that are no input channels of the tree.
	 */
	public TreeRowArrangement(RowTree tree, Set<TLStructuredType> observedTypes,
			Collection<? extends ViewChannel> rowIndependentInputs, Collection<? extends ViewChannel> cellInputs) {
		_tree = tree;
		_observedTypes = observedTypes;
		_rowIndependentInputs = rowIndependentInputs;
		_cellInputs = cellInputs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The rows are the nodes of the tree, the given rows are not used. The source is expanded where
	 * the source created before was.
	 * </p>
	 */
	@Override
	public RowSource<TLObject> createSource(List<? extends TLObject> rows, RowSetEditSession session,
			List<Column<TLObject, ?>> columns) {
		List<Object> expanded = _source == null ? List.of() : new ArrayList<>(_source.expandedKeys());
		_session = session;
		_source = _tree.createSource(columns, this::rowObject, expanded);
		return _source;
	}

	/**
	 * The row object of a node with the given business object: the row the running session edits
	 * the object through, the object itself outside a session.
	 *
	 * <p>
	 * The rows of a row-editing tree are model objects.
	 * </p>
	 */
	private TLObject rowObject(Object businessObject) {
		TLObject stored = (TLObject) businessObject;
		RowSetEditSession session = _session;
		if (stored != null && session != null && session.isRunning()) {
			TLObject row = session.adoptRow(stored);
			if (row != null) {
				return row;
			}
		}
		return stored;
	}

	@Override
	public void bindSelection(TableViewControl<TLObject> table, ViewChannel channel) {
		if (channel == null) {
			return;
		}
		_tree.bindSelection(table, channel, new TreeSelectionBinding.ChannelObjects() {
			@Override
			public Object toChannel(Object businessObject) {
				RowSetEditSession session = _session;
				if (session != null && businessObject instanceof TLObject stored) {
					TLObject row = session.findRow(stored);
					if (row != null) {
						return row;
					}
				}
				return businessObject;
			}

			@Override
			public Object fromChannel(Object channelObject) {
				return storedObject(channelObject);
			}
		});
	}

	@Override
	public void observe(Display display) {
		_display = display;
		if (_observing) {
			return;
		}
		_observing = true;
		_tree.observe(new ObservableTreeModel.Display() {
			@Override
			public void invalidateNode(DefaultTreeUINode node) {
				// Every invalidation is followed by nodesChanged(), which renders all rows anew.
			}

			@Override
			public void nodesChanged() {
				rowsChanged();
			}

			@Override
			public void treeReplaced(DefaultTreeUINodeModel treeModel) {
				rowsChanged();
			}

			@Override
			public void inputsChanged() {
				if (_display != null) {
					_display.inputsChanged();
				}
			}
		}, _observedTypes, _rowIndependentInputs, _cellInputs);
	}

	/**
	 * Computes the rows of the current source again for the changed tree, and tells the table.
	 */
	private void rowsChanged() {
		if (_display == null) {
			return;
		}
		_source.refresh();
		_display.rowsReplaced();
	}

	@Override
	public void attach(ModelScope scope) {
		if (_display != null) {
			_tree.attach(scope);
		}
	}

	@Override
	public void detach() {
		_tree.detach();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The observation of the tree is suspended, not dropped: the tree stays as it is, and catches up
	 * on what changed meanwhile when it is {@link #observe(Display) observed} for a table again.
	 * </p>
	 */
	@Override
	public void release() {
		_display = null;
		_tree.detach();
		_tree.releaseSelection();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The rows are the nodes of the tree, so the rows of the current source are computed again from
	 * them.
	 * </p>
	 */
	@Override
	public void setRows(List<? extends TLObject> rows) {
		if (_source != null) {
			_source.refresh();
		}
	}

	@Override
	public Object key(TLObject row) {
		return storedObject(row);
	}

	@Override
	public TLObject row(Object key) {
		RowSetEditSession session = _session;
		if (!(key instanceof TLObject stored)) {
			return null;
		}
		if (session != null && session.isRunning()) {
			return session.findRow(stored);
		}
		return stored;
	}

	/**
	 * The stored object the given object stands for: the object a buffer edits, the object itself
	 * otherwise.
	 */
	private static Object storedObject(Object object) {
		return object instanceof TLObjectOverlay overlay ? overlay.getBase() : object;
	}

}
