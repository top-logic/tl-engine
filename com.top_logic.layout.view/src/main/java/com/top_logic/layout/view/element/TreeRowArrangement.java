/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.DeliveryScope;
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
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.Pointer;
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
 * the tree is not observed, so it changes neither with the model nor with the inputs. Outside the
 * session, the tree follows them through the observation of the {@link RowTree}, which takes up what
 * changed while it was not observed when it observes again.
 * </p>
 *
 * <p>
 * While the session runs, the tree shows the edited state: the children of a node are computed in
 * a {@link TracingScope} over the {@link RowSetEditSession#overlays() overlays} of the form, so the
 * children function reads the buffers of the session - a new row inserted into the buffered
 * composition of its parent is a child, keyed by its new object -, and a row
 * {@link RowSetEditSession#isRemoved(TLObject) removed} in the session is no child. The attributes
 * each computation of a child list read are recorded; when a value is written into one of them
 * ({@link RowSetEditSession.Listener#onValueWritten(RowSetEditSession, TLObject, TLStructuredTypePart)}),
 * the child lists that read it are computed again once the interaction has completed
 * ({@link DeliveryScope#afterInteraction(Runnable)}), keeping the nodes of the children that stay.
 * When the session ends, the child lists are computed from the stored objects again.
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
	 * The running session the child lists of the tree are computed over, {@code null} while they are
	 * computed from the stored objects.
	 */
	private RowSetEditSession _followed;

	/** What the tree reports its changes to while {@link #_followed a session} runs. */
	private Display _editDisplay;

	/**
	 * For each node whose children were computed over the buffers of the {@link #_followed session},
	 * the attribute values the computation read; the objects of the values are the objects that are
	 * edited, never their buffers.
	 */
	private final Map<DefaultTreeUINode, Set<Pointer>> _dependencies = new IdentityHashMap<>();

	/** The nodes whose children are computed again when the current interaction has completed. */
	private final Set<DefaultTreeUINode> _stale = Collections.newSetFromMap(new IdentityHashMap<>());

	/** Whether the tree was changed while the {@link #_followed session} ran. */
	private boolean _changedInSession;

	/**
	 * The session followed last, which may know the stored objects of its new rows, see
	 * {@link RowSetEditSession#savedObject(TLObject)}; {@code null} while one is followed.
	 */
	private RowSetEditSession _ended;

	private final RowSetEditSession.Listener _sessionListener = new RowSetEditSession.Listener() {
		@Override
		public void onRowsChanged(RowSetEditSession session) {
			// Followed through setRows().
		}

		@Override
		public void onRowSetValidationChanged(RowSetEditSession session) {
			// Not displayed by the arrangement.
		}

		@Override
		public void onValueWritten(RowSetEditSession session, TLObject buffer, TLStructuredTypePart part) {
			valueWritten(buffer, part);
		}
	};

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
	 * the source created before was; the first one where the tree was
	 * {@link RowTree#initiallyExpanded() opened when it was built}.
	 * </p>
	 */
	@Override
	public RowSource<TLObject> createSource(List<? extends TLObject> rows, RowSetEditSession session,
			List<Column<TLObject, ?>> columns) {
		Collection<?> expandedBefore = _source == null ? _tree.initiallyExpanded() : _source.expandedKeys();
		_session = session;
		_editDisplay = null;
		follow(session != null && session.isRunning() ? session : null);
		List<Object> expanded = new ArrayList<>(expandedBefore.size());
		for (Object key : expandedBefore) {
			expanded.add(savedKey(key));
		}
		_source = _tree.createSource(columns, this::rowObject, expanded);
		return _source;
	}

	/**
	 * Computes the child lists of the tree over the buffers of the given session from now on, or
	 * from the stored objects again.
	 *
	 * <p>
	 * When a session begins to be followed, the child lists computed already are computed again,
	 * which records what they read; they hold the same children, since nothing is edited yet. When
	 * it ends, the child lists are computed from the stored objects again where the session changed
	 * the tree: the new rows it added are stored or discarded, and the rows it removed are deleted
	 * or back. A new row that was saved is then keyed by its stored object, which takes over its
	 * expansion and its selection, see {@link #savedKey(Object)}.
	 * </p>
	 */
	private void follow(RowSetEditSession session) {
		if (session == _followed) {
			return;
		}
		if (_followed != null) {
			_followed.removeListener(_sessionListener);
		}
		_ended = _followed;
		_dependencies.clear();
		_stale.clear();
		_followed = session;
		if (session != null) {
			session.addListener(_sessionListener);
			_tree.setChildList(this::childrenOverBuffers);
			_changedInSession = false;
			_tree.reconcileAll();
		} else {
			_tree.setChildList(null);
			if (_changedInSession) {
				_changedInSession = false;
				_tree.reconcileAll();
			}
		}
	}

	/**
	 * Computes the children of the given node over the buffers of the {@link #_followed session},
	 * recording what the computation read.
	 *
	 * @return The objects the children stand for - the stored object of an existing row, the new
	 *         object of a new row - without the rows removed in the session.
	 */
	private Collection<?> childrenOverBuffers(DefaultTreeUINode node, Function<Object, Collection<?>> function) {
		RowSetEditSession session = _followed;
		Set<Pointer> dependencies = new HashSet<>();
		Collection<?> children;
		try (TracingScope scope = TracingScope.open(
			pointer -> dependencies.add(Pointer.create(editedObject(pointer.object()), pointer.attribute())),
			session.overlays())) {
			children = function.apply(node.getBusinessObject());
		}
		_dependencies.put(node, dependencies);

		List<Object> result = new ArrayList<>(children.size());
		for (Object child : children) {
			Object object = storedObject(child);
			if (object instanceof TLObject childObject && session.isRemoved(childObject)) {
				continue;
			}
			result.add(object);
		}
		return result;
	}

	/**
	 * Marks the nodes whose child lists read the written value, and computes them again when the
	 * interaction has completed.
	 */
	private void valueWritten(TLObject buffer, TLStructuredTypePart part) {
		Pointer changed = Pointer.create(editedObject(buffer), part);
		boolean scheduled = !_stale.isEmpty();
		for (Map.Entry<DefaultTreeUINode, Set<Pointer>> entry : _dependencies.entrySet()) {
			if (entry.getValue().contains(changed)) {
				_stale.add(entry.getKey());
			}
		}
		if (!scheduled && !_stale.isEmpty()) {
			DeliveryScope.afterInteraction(this::reconcileStale);
		}
	}

	private void reconcileStale() {
		List<DefaultTreeUINode> stale = new ArrayList<>(_stale);
		_stale.clear();
		if (_followed == null) {
			return;
		}
		reconcile(stale);
	}

	/**
	 * Computes the children of the given nodes again while the {@link #_followed session} runs, and
	 * tells the table when the tree changed.
	 */
	private void reconcile(Collection<DefaultTreeUINode> nodes) {
		if (!_tree.reconcile(nodes)) {
			return;
		}
		_changedInSession = true;
		if (_source != null) {
			_source.refresh();
		}
		if (_editDisplay != null) {
			_editDisplay.rowsReplaced();
		}
	}

	/**
	 * Computes the children of the node of the given object again over the buffers of the running
	 * session: the object whose composition received a new row, say.
	 *
	 * @param parent
	 *        The stored or new object of a row, a buffer of one, or the {@link RowTree#rootObject()
	 *        object the tree is built from}.
	 */
	public void childrenChanged(Object parent) {
		if (_followed == null) {
			return;
		}
		DefaultTreeUINode node = _tree.findNode(storedObject(parent));
		if (node != null) {
			reconcile(List.of(node));
		}
	}

	/**
	 * The object the tree is built from, whose children are the top-level rows.
	 */
	public Object rootObject() {
		return _tree.rootObject();
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
		TreeSelectionBinding.ChannelObjects channelObjects = new TreeSelectionBinding.ChannelObjects() {
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
				return savedKey(storedObject(channelObject));
			}
		};
		_tree.bindSelection(table, channel, channelObjects);

		// A new row that was saved is selected as its stored object, which the channel names from
		// now on.
		Object selected = channel.get();
		if (selected instanceof TLObject) {
			Object saved = channelObjects.fromChannel(selected);
			if (saved != storedObject(selected)) {
				channel.set(channelObjects.toChannel(saved));
			}
		}
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
			public void treeReplaced(DefaultTreeUINodeModel treeModel, List<DefaultTreeUINode> opened,
					boolean rootChanged) {
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
	public void followSession(Display display) {
		_editDisplay = display;
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
		RowSetEditSession session = _followed;
		if (session != null) {
			// A removed row leaves its parent.
			List<DefaultTreeUINode> holders = new ArrayList<>();
			for (DefaultTreeUINode node : _tree.computedNodes()) {
				for (DefaultTreeUINode child : node.getChildren()) {
					if (child.getBusinessObject() instanceof TLObject object && session.isRemoved(object)) {
						holders.add(node);
						break;
					}
				}
			}
			if (_tree.reconcile(holders)) {
				_changedInSession = true;
			}
		}
		if (_source != null) {
			_source.refresh();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The rows above the row of the given object in the tree, from the top-level row down.
	 * </p>
	 */
	@Override
	public Collection<?> ancestorKeys(TLObject row) {
		DefaultTreeUINode node = _tree.findNode(storedObject(row));
		if (node == null) {
			return List.of();
		}
		List<Object> result = new ArrayList<>();
		for (DefaultTreeUINode parent = node.getParent(); parent != null && parent.getParent() != null;
				parent = parent.getParent()) {
			result.add(0, parent.getBusinessObject());
		}
		return result;
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
	 * The stored object the given object stands for: the object a buffer edits - a stored object or
	 * the new object of a new row -, the object itself otherwise.
	 */
	private static Object storedObject(Object object) {
		return object instanceof TLObjectOverlay overlay ? overlay.getBase() : object;
	}

	/**
	 * The key a row has now that had the given key before the session followed last ended: the
	 * stored object of a new row that was saved, the given key otherwise.
	 */
	private Object savedKey(Object key) {
		RowSetEditSession ended = _ended;
		if (_followed == null && ended != null && key instanceof TLObject object && object.tTransient()) {
			TLObject saved = ended.savedObject(object);
			if (saved != null) {
				return saved;
			}
		}
		return key;
	}

	private static TLObject editedObject(TLObject object) {
		return object instanceof TLObjectOverlay overlay ? overlay.getBase() : object;
	}

}
