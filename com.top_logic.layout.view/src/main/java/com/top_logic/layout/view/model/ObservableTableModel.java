/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Companion to an {@link ObjectTableModel} that observes persistent object changes via
 * {@link ModelScope} and input changes via {@link ViewChannel}, translating them into
 * {@link TableModelEvent}s on the inner model.
 *
 * <p>
 * This class lives in the view module and bridges the gap between the TopLogic model layer
 * and the model-free React control layer. The wrapped {@link ObjectTableModel} and the
 * {@link com.top_logic.layout.react.control.table.ReactTableControl} remain unchanged.
 * </p>
 *
 * <h3>Two trigger paths:</h3>
 * <ol>
 * <li><b>Channel change</b> (new input data): Re-evaluates the row function with the new
 * input, calls {@link ObjectTableModel#setRowObjects(List)} on the inner model (which fires
 * {@link TableModelEvent#INVALIDATE}), and re-registers object listeners.</li>
 * <li><b>ModelScope event</b> (object changes): Removes deleted rows via
 * {@link ObjectTableModel#removeRowObject(Object)}, and re-evaluates the row function for
 * updates and creates (firing {@link TableModelEvent#INVALIDATE} via
 * {@link ObjectTableModel#setRowObjects(List)}).</li>
 * </ol>
 *
 * <h3>Lifecycle:</h3>
 * <ul>
 * <li>{@link #attach(ModelScope)} - registers listeners (called on first render)</li>
 * <li>{@link #detach()} - removes all listeners (called on cleanup)</li>
 * </ul>
 */
public class ObservableTableModel implements ModelListener, ViewChannel.ChannelListener {

	private final ObjectTableModel _inner;

	private final Function<Object[], Collection<?>> _rowFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private Set<ObjectKey> _observedKeys = new HashSet<>();

	private ModelScope _modelScope;

	private boolean _attached;

	/**
	 * Creates a new {@link ObservableTableModel}.
	 *
	 * @param inner
	 *        The wrapped table model.
	 * @param rowFunction
	 *        Function that takes channel values and returns row objects. Used for
	 *        re-evaluation on channel change and create detection.
	 * @param observedTypes
	 *        Types to observe for create events. Empty means no create detection.
	 * @param inputChannels
	 *        The input channels whose values are passed to the row function.
	 */
	public ObservableTableModel(ObjectTableModel inner,
			Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes,
			List<ViewChannel> inputChannels) {
		_inner = inner;
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
	}

	/**
	 * The wrapped {@link ObjectTableModel}.
	 *
	 * <p>
	 * The {@link com.top_logic.layout.react.control.table.ReactTableControl} uses this as
	 * its table model and registers its {@link com.top_logic.layout.table.model.TableModelListener}
	 * on it directly.
	 * </p>
	 */
	public ObjectTableModel getTableModel() {
		return _inner;
	}

	/**
	 * Registers listeners on the given {@link ModelScope} and input channels.
	 *
	 * <p>
	 * Called lazily on first render (via {@code addBeforeWriteAction}).
	 * </p>
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_modelScope = scope;

		registerObjectListeners();
		registerTypeListeners();
		registerChannelListeners();
	}

	/**
	 * Removes all listeners and releases references.
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;

		deregisterObjectListeners();
		deregisterTypeListeners();
		deregisterChannelListeners();

		_modelScope = null;
		_observedKeys.clear();
	}

	// --- ModelListener ---

	@Override
	public void notifyChange(ModelChangeEvent event) {
		boolean hasDeletes = handleDeletes(event);
		boolean hasUpdates = hasUpdatesForCurrentRows(event);
		boolean hasCreates = hasRelevantCreates(event);

		if (hasUpdates || hasCreates) {
			// Re-evaluate row function and update inner model. This fires INVALIDATE
			// via setRowObjects(), causing ReactTableControl.buildFullState().
			reEvaluateRows();
		}
	}

	private boolean handleDeletes(ModelChangeEvent event) {
		List<TLObject> toRemove = new ArrayList<>();
		event.getDeleted().forEach(obj -> {
			ObjectKey key = key(obj);
			if (key != null && _observedKeys.contains(key)) {
				toRemove.add(obj);
				_observedKeys.remove(key);
				_modelScope.removeModelListener(obj, this);
			}
		});
		for (TLObject obj : toRemove) {
			_inner.removeRowObject(obj);
		}
		return !toRemove.isEmpty();
	}

	private boolean hasUpdatesForCurrentRows(ModelChangeEvent event) {
		return event.getUpdated().anyMatch(obj -> {
			ObjectKey key = key(obj);
			return key != null && _observedKeys.contains(key);
		});
	}

	private boolean hasRelevantCreates(ModelChangeEvent event) {
		if (_observedTypes.isEmpty()) {
			return false;
		}
		for (TLStructuredType type : _observedTypes) {
			if (event.getCreated(type).findAny().isPresent()) {
				return true;
			}
		}
		return false;
	}

	// --- ChannelListener ---

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		reEvaluateRows();
	}

	// --- Re-evaluation ---

	private void reEvaluateRows() {
		deregisterObjectListeners();
		Object[] channelValues = readChannelValues();
		Collection<?> newRows = _rowFunction.apply(channelValues);
		_inner.setRowObjects(new ArrayList<>(newRows));
		registerObjectListeners();
	}

	// --- Listener registration helpers ---

	@SuppressWarnings("unchecked")
	private void registerObjectListeners() {
		_observedKeys.clear();
		for (Object row : (Collection<Object>) _inner.getAllRows()) {
			if (row instanceof TLObject tlObj) {
				ObjectKey key = key(tlObj);
				if (key != null) {
					_observedKeys.add(key);
					_modelScope.addModelListener(tlObj, this);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void deregisterObjectListeners() {
		for (Object row : (Collection<Object>) _inner.getAllRows()) {
			if (row instanceof TLObject tlObj) {
				_modelScope.removeModelListener(tlObj, this);
			}
		}
		_observedKeys.clear();
	}

	private void registerTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.addModelListener(type, this);
		}
	}

	private void deregisterTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.removeModelListener(type, this);
		}
	}

	private void registerChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.addListener(this);
		}
	}

	private void deregisterChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.removeListener(this);
		}
	}

	// --- Utility ---

	private Object[] readChannelValues() {
		Object[] values = new Object[_inputChannels.size()];
		for (int i = 0; i < _inputChannels.size(); i++) {
			values[i] = _inputChannels.get(i).get();
		}
		return values;
	}

	private static ObjectKey key(TLObject obj) {
		KnowledgeItem item = obj.tHandle();
		return item != null ? item.tId() : null;
	}
}
