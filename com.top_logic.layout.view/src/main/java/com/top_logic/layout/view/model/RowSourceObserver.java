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
import java.util.function.Consumer;
import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.impl.ListRowSource;

/**
 * Keeps a displayed element list in sync with the persistent model: observes object changes via
 * {@link ModelScope} and input changes via {@link ViewChannel}, and on any relevant change re-runs
 * the element function and pushes the fresh elements into a sink (typically a
 * {@link ListRowSource}, but any consumer of the element list works).
 *
 * <p>
 * The green-field analog of the legacy observable {@code ObjectTableModel} wrapper. Listeners are
 * registered only for the observed types (to catch
 * creates), the currently displayed objects (to catch their updates / deletes) and the input
 * channels, so any received notification simply means "re-evaluate".
 * </p>
 *
 * @param <R>
 *        The element business object type.
 */
public class RowSourceObserver<R> implements ModelListener, ViewChannel.ChannelListener {

	private final Function<Object[], Collection<?>> _rowFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private final Consumer<List<R>> _sink;

	private final Set<ObjectKey> _observedKeys = new HashSet<>();

	/** The currently displayed elements, whose updates / deletes are observed. */
	private List<R> _elements;

	private ModelScope _scope;

	private boolean _attached;

	/**
	 * Creates a {@link RowSourceObserver} pushing fresh elements into a {@link ListRowSource}.
	 *
	 * @param source
	 *        The row source to refresh.
	 * @param rowFunction
	 *        Computes the row objects from the input channel values.
	 * @param observedTypes
	 *        Types whose creates trigger a refresh (empty disables create detection).
	 * @param inputChannels
	 *        Input channels whose changes trigger a refresh.
	 * @param onChange
	 *        Run after the source has been refreshed (e.g. to rebuild the control's state).
	 */
	public RowSourceObserver(ListRowSource<R> source, Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels, Runnable onChange) {
		this(new ArrayList<>(source.elements()), rowFunction, observedTypes, inputChannels,
			elements -> {
				source.setElements(elements);
				onChange.run();
			});
	}

	/**
	 * Creates a {@link RowSourceObserver} pushing fresh elements into an arbitrary sink.
	 *
	 * @param initialElements
	 *        The elements displayed at construction time, whose updates / deletes are observed
	 *        until the first re-evaluation.
	 * @param rowFunction
	 *        Computes the element objects from the input channel values.
	 * @param observedTypes
	 *        Types whose creates trigger a refresh (empty disables create detection).
	 * @param inputChannels
	 *        Input channels whose changes trigger a refresh.
	 * @param sink
	 *        Receives the fresh element list after each re-evaluation.
	 */
	public RowSourceObserver(List<R> initialElements, Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels, Consumer<List<R>> sink) {
		_elements = new ArrayList<>(initialElements);
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
		_sink = sink;
	}

	/**
	 * Registers all listeners on the given {@link ModelScope} (called on first render).
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_scope = scope;
		registerObjectListeners();
		registerTypeListeners();
		registerChannelListeners();
	}

	/**
	 * Removes all listeners (called on cleanup).
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		deregisterObjectListeners();
		deregisterTypeListeners();
		deregisterChannelListeners();
		_scope = null;
		_observedKeys.clear();
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		reEvaluate();
	}

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		reEvaluate();
	}

	private void reEvaluate() {
		deregisterObjectListeners();
		Collection<?> rows = _rowFunction.apply(readChannelValues());
		List<R> elements = new ArrayList<>(rows.size());
		for (Object row : rows) {
			@SuppressWarnings("unchecked")
			R element = (R) row;
			elements.add(element);
		}
		_elements = elements;
		registerObjectListeners();
		_sink.accept(elements);
	}

	private void registerObjectListeners() {
		_observedKeys.clear();
		for (Object row : _elements) {
			if (row instanceof TLObject object) {
				ObjectKey key = key(object);
				if (key != null && _observedKeys.add(key)) {
					_scope.addModelListener(object, this);
				}
			}
		}
	}

	private void deregisterObjectListeners() {
		for (Object row : _elements) {
			if (row instanceof TLObject object) {
				_scope.removeModelListener(object, this);
			}
		}
		_observedKeys.clear();
	}

	private void registerTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_scope.addModelListener(type, this);
		}
	}

	private void deregisterTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_scope.removeModelListener(type, this);
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

	private Object[] readChannelValues() {
		Object[] values = new Object[_inputChannels.size()];
		for (int n = 0; n < _inputChannels.size(); n++) {
			values[n] = _inputChannels.get(n).get();
		}
		return values;
	}

	private static ObjectKey key(TLObject object) {
		KnowledgeItem item = object.tHandle();
		return item != null ? item.tId() : null;
	}

}
