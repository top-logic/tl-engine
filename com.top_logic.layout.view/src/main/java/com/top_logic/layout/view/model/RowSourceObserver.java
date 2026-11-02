/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.top_logic.basic.listener.Registration;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.react.control.ReactControl;
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
 * Listeners are registered only for the observed types (to catch creates), the currently displayed
 * persistent objects (to catch their updates / deletes) and the input channels, so any received
 * notification simply means "re-evaluate".
 * </p>
 *
 * <p>
 * What an input change triggers depends on the channel:
 * </p>
 * <ul>
 * <li>A change of a {@link #withRowIndependentInputs(Collection) row-independent input} - a channel
 * the element function receives but whose value cannot change the elements, such as the selection of
 * the very table displaying them - re-evaluates nothing; it only runs the
 * {@link #onInputsChanged(Collection, Runnable) inputs-changed handler}.</li>
 * <li>A change of an input {@link #onInputsChanged(Collection, Runnable) declared to the handler}
 * re-evaluates the elements; elements equal to the ones at hand are not delivered, the handler runs
 * instead.</li>
 * <li>A change of any other input re-evaluates the elements and delivers them to the sink, even
 * where they equal the ones at hand: what the display builds from the elements may read that
 * input.</li>
 * <li>A model change re-evaluates the elements and delivers them in any case, even where they equal
 * the ones at hand: the displayed objects themselves may have changed.</li>
 * </ul>
 *
 * <p>
 * The element function receives the values of all input channels in their declared order, the
 * row-independent ones included.
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

	/** @see #withRowIndependentInputs(Collection) */
	private Set<ViewChannel> _rowIndependentInputs = Set.of();

	/** @see #onInputsChanged(Collection, Runnable) */
	private Set<ViewChannel> _handledInputs = Set.of();

	/** @see #onInputsChanged(Collection, Runnable) */
	private Runnable _onInputsChanged;

	/**
	 * The registrations observing the persistent objects among {@link #_elements}, one per object.
	 */
	private final Map<ObjectKey, Registration> _objectRegistrations = new HashMap<>();

	/**
	 * The registrations on the {@link #_observedTypes} and the {@link #_inputChannels}.
	 */
	private final List<Registration> _registrations = new ArrayList<>();

	/** The currently displayed elements, whose updates / deletes are observed. */
	private List<R> _elements;

	private ModelScope _scope;

	private boolean _attached;

	/** Whether the observation was stopped and has not begun again. */
	private boolean _suspended;

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
	 * Declares input channels whose changes cannot change the elements.
	 *
	 * <p>
	 * A change of such a channel does not re-evaluate the element function and leaves the elements
	 * as they are; it only runs the {@link #onInputsChanged(Collection, Runnable) inputs-changed
	 * handler}, so it counts among the channels declared to the handler in any case. The
	 * element function still receives the channel's value at its declared position. A channel that
	 * is not among the input channels is ignored.
	 * </p>
	 *
	 * @param channels
	 *        Input channels the elements do not depend on, e.g. the selection channel of the table
	 *        displaying the elements.
	 * @return This instance, for call chaining.
	 */
	public RowSourceObserver<R> withRowIndependentInputs(Collection<? extends ViewChannel> channels) {
		_rowIndependentInputs = new HashSet<>(channels);
		return this;
	}

	/**
	 * Sets what to run when one of the given inputs changed but the elements did not.
	 *
	 * <p>
	 * The handler runs after a change of a {@link #withRowIndependentInputs(Collection)
	 * row-independent input}, and after a change of one of the given channels that yields elements
	 * equal to the ones at hand - which are then not delivered to the sink. It takes up what
	 * depends on the inputs apart from the elements themselves, e.g. criteria of filters or CSS
	 * classes of rows. A change of any other input delivers the elements in any case, equal or not:
	 * the given channels are the ones nothing the display builds from the elements reads.
	 * </p>
	 *
	 * @param channels
	 *        The input channels whose change with equal elements only runs the handler. A channel
	 *        that is not among the input channels is ignored.
	 * @param handler
	 *        What to run, {@code null} for nothing.
	 * @return This instance, for call chaining.
	 */
	public RowSourceObserver<R> onInputsChanged(Collection<? extends ViewChannel> channels, Runnable handler) {
		_handledInputs = new HashSet<>(channels);
		_onInputsChanged = handler;
		return this;
	}

	/**
	 * Begins observing on the given {@link ModelScope}, and re-reads the elements.
	 *
	 * <p>
	 * The elements are re-read because what happened before the observation began is unknown here: an
	 * object created, an input channel written - a channel bound to the URL taking up the value a
	 * deep link carries, for instance - between the construction of the observer and this call
	 * reached no listener, so the elements at hand describe a state that may already be gone.
	 * </p>
	 *
	 * <p>
	 * An observation that resumes - one that was stopped by {@link #detach()} - delivers the elements
	 * it read in any case, even where they are the ones at hand: the objects in that list are the
	 * ones nobody followed while the display was suspended, so what they carry is unknown and the
	 * display rebuilds from them.
	 * </p>
	 *
	 * @param scope
	 *        The scope the model listeners are registered on.
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
		catchUp();
	}

	/**
	 * Observes while the given control is displayed: begins the observation on the
	 * {@link com.top_logic.layout.react.ReactContext#getModelScope() model scope} of the control
	 * whenever it is {@link ReactControl#addAttachListener(Runnable) attached}, and stops it whenever
	 * it is {@link ReactControl#addDetachListener(Runnable) detached}.
	 *
	 * <p>
	 * A control that is not displayed - the inactive child of a tab bar, a field of a hidden part of
	 * a form - does not follow the model, and catches up with it as soon as it is displayed again,
	 * see {@link #attach(ModelScope)}.
	 * </p>
	 *
	 * @param control
	 *        The control displaying the elements.
	 * @return This instance, for call chaining.
	 */
	public RowSourceObserver<R> observeWhileAttached(ReactControl control) {
		control.addAttachListener(() -> attach(control.getReactContext().getModelScope()));
		control.addDetachListener(this::detach);
		return this;
	}

	/**
	 * Removes all listeners (called on cleanup).
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		_suspended = true;
		deregisterObjectListeners();
		for (Registration registration : _registrations) {
			registration.dispose();
		}
		_registrations.clear();
		_scope = null;
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		reEvaluate();
	}

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		if (_rowIndependentInputs.contains(sender)) {
			inputsChanged();
			return;
		}
		List<R> elements = readElements();
		if (_handledInputs.contains(sender) && elements.equals(_elements)) {
			inputsChanged();
			return;
		}
		replaceElements(elements);
		_sink.accept(elements);
	}

	/**
	 * Runs the {@link #onInputsChanged(Collection, Runnable) inputs-changed handler}, if any.
	 */
	private void inputsChanged() {
		if (_onInputsChanged != null) {
			_onInputsChanged.run();
		}
	}

	private void reEvaluate() {
		List<R> elements = readElements();
		replaceElements(elements);
		_sink.accept(elements);
	}

	/**
	 * Re-reads the elements and delivers them where they differ from the ones at hand, and in any case
	 * where the observation resumes.
	 *
	 * <p>
	 * Silent for an observation beginning on an unchanged list: nothing was missed then, and a display
	 * that was just built from those elements has nothing to rebuild. A resumed observation says
	 * nothing about the objects in the list, though - every change they saw while nobody followed them
	 * passed unnoticed - so their list is delivered as it is read.
	 * </p>
	 */
	private void catchUp() {
		boolean resumed = _suspended;
		_suspended = false;
		List<R> elements = readElements();
		if (!resumed && elements.equals(_elements)) {
			return;
		}
		replaceElements(elements);
		_sink.accept(elements);
	}

	/**
	 * The elements the element function yields for the current input values.
	 */
	private List<R> readElements() {
		Collection<?> rows = _rowFunction.apply(readChannelValues());
		List<R> elements = new ArrayList<>(rows.size());
		for (Object row : rows) {
			@SuppressWarnings("unchecked")
			R element = (R) row;
			elements.add(element);
		}
		return elements;
	}

	/**
	 * Makes the given elements the observed ones.
	 */
	private void replaceElements(List<R> elements) {
		deregisterObjectListeners();
		_elements = elements;
		registerObjectListeners();
	}

	private void registerObjectListeners() {
		for (Object row : _elements) {
			if (isObservable(row)) {
				TLObject object = (TLObject) row;
				ObjectKey key = key(object);
				if (key != null && !_objectRegistrations.containsKey(key)) {
					_objectRegistrations.put(key, _scope.addModelListener(object, this));
				}
			}
		}
	}

	private void deregisterObjectListeners() {
		for (Registration registration : _objectRegistrations.values()) {
			registration.dispose();
		}
		_objectRegistrations.clear();
	}

	/**
	 * Whether changes of the given element can be observed: it is a persistent object.
	 *
	 * <p>
	 * A transient object has no identity in the persistency layer and therefore no changes anyone
	 * could be notified of - it changes only where the display that holds it changes it.
	 * </p>
	 */
	private static boolean isObservable(Object row) {
		return row instanceof TLObject object && !object.tTransient();
	}

	private void registerTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_registrations.add(_scope.addModelListener(type, this));
		}
	}

	private void registerChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			_registrations.add(channel.addListener(this));
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
