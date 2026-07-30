/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.element.SwitchElement.SwitchCase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Server-side control of {@link SwitchElement}: shows the content of the first {@code <case>} whose
 * predicate matches the input channel's current value (or the {@code <default>} content), and swaps
 * to a different case's content when the matching case changes.
 *
 * <p>
 * Re-evaluation is triggered by a new input channel value, and by a change of the input object
 * itself: a predicate typically decides by one of its attributes, which can be edited while the
 * channel keeps pointing to the same object. The input object is therefore observed in the
 * {@link ModelScope}, along with the optionally configured
 * {@link SwitchElement.Config#getObservedTypes() observed types}.
 * </p>
 *
 * <p>
 * Unlike a caching deck, the previously shown content is <em>disposed</em> on a case change rather
 * than kept alive. This keeps the enclosing scope free of a hidden case's contributions (e.g. a
 * form's edit/save/cancel commands), which a cached-but-detached case would otherwise leave behind
 * as duplicate toolbar buttons. Within a single case, the content is bound to its own channels and
 * updates itself - the switch does not rebuild while the matching case stays the same.
 * </p>
 *
 * <p>
 * The React component {@code TLDeckPane} renders the single {@code activeChild}.
 * </p>
 */
public class ReactSwitchControl extends ReactControl implements ModelListener {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_CHILD = "activeChild";

	private final ViewContext _context;

	private final ViewChannel _input;

	private final List<SwitchCase> _cases;

	private final List<UIElement> _default;

	private final Set<TLStructuredType> _observedTypes;

	private final ChannelListener _inputListener;

	private ModelScope _scope;

	/** The input objects currently observed for changes, see {@link #observeInput()}. */
	private Set<TLObject> _observedObjects = Set.of();

	private ReactControl _current;

	private int _activeIndex = -1;

	private boolean _disposed;

	/**
	 * Creates a new {@link ReactSwitchControl}.
	 *
	 * @param context
	 *        The context used to build case content controls.
	 * @param input
	 *        The channel whose value selects the visible case.
	 * @param cases
	 *        The cases in evaluation order.
	 * @param defaultContent
	 *        The content shown when no case matches (may be empty).
	 * @param observedTypes
	 *        Types whose object changes trigger a re-evaluation in addition to the input object
	 *        (may be empty).
	 */
	ReactSwitchControl(ViewContext context, ViewChannel input, List<SwitchCase> cases,
			List<UIElement> defaultContent, Set<TLStructuredType> observedTypes) {
		super(context, null, REACT_MODULE);
		_context = context;
		_input = input;
		_cases = cases;
		_default = defaultContent;
		_observedTypes = observedTypes;

		_inputListener = (sender, oldValue, newValue) -> {
			observeInput();
			renderActive();
		};
		_input.addListener(_inputListener);
		addCleanupAction(() -> _input.removeListener(_inputListener));

		addBeforeWriteAction(this::attachModelListeners);
		addCleanupAction(this::detachModelListeners);

		renderActive();
	}

	/**
	 * Starts observing the {@link SwitchElement.Config#getObservedTypes() observed types} and the
	 * current input object.
	 */
	private void attachModelListeners() {
		if (_scope != null) {
			return;
		}
		_scope = _context.getModelScope();
		for (TLStructuredType type : _observedTypes) {
			_scope.addModelListener(type, this);
		}
		observeInput();
	}

	private void detachModelListeners() {
		if (_scope == null) {
			return;
		}
		for (TLStructuredType type : _observedTypes) {
			_scope.removeModelListener(type, this);
		}
		removeObjectListeners();
		_scope = null;
	}

	/**
	 * Points the object listeners at the current input value, so that editing an attribute the case
	 * tests re-evaluates the switch although the channel value stays the same.
	 */
	private void observeInput() {
		if (_scope == null) {
			// Not rendered yet; the listeners are registered on the first write.
			return;
		}

		Set<TLObject> objects = inputObjects();
		if (objects.equals(_observedObjects)) {
			return;
		}

		removeObjectListeners();
		_observedObjects = objects;
		for (TLObject object : objects) {
			_scope.addModelListener(object, this);
		}
	}

	private void removeObjectListeners() {
		for (TLObject object : _observedObjects) {
			_scope.removeModelListener(object, this);
		}
		_observedObjects = Set.of();
	}

	/**
	 * The {@link TLObject}s in the current input channel value, which may hold a single object or -
	 * for a multi-selection - a collection of them.
	 *
	 * @return The objects to observe as a set: their order carries no meaning for observation, and a
	 *         value listing the same object twice must not be registered (or removed) twice.
	 */
	private Set<TLObject> inputObjects() {
		Object value = _input.get();
		if (value instanceof TLObject object) {
			return Set.of(object);
		}
		if (value instanceof Collection<?> values) {
			Set<TLObject> result = new HashSet<>();
			for (Object element : values) {
				if (element instanceof TLObject object) {
					result.add(object);
				}
			}
			return result;
		}
		return Set.of();
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		renderActive();
	}

	private void renderActive() {
		if (_disposed) {
			return;
		}
		if (hasDeletedInput()) {
			// A deleted object is still in the channel: the deleting command notifies the model change
			// before writing the channel. Evaluating a test against it would fail, so keep the current
			// content until the channel write arrives.
			return;
		}
		int index = selectIndex(_input.get());
		if (index == _activeIndex && _current != null) {
			// The matching case is unchanged; its content is bound to the channel and updates itself.
			return;
		}
		_activeIndex = index;

		ReactControl built = buildContent(index);
		ReactControl old = _current;
		_current = built;

		Object token = beginUpdate();
		putState(ACTIVE_CHILD, built);
		commitUpdate(token);

		if (isAttached()) {
			built.attach();
		}
		if (old != null && old != built) {
			// The rebuild typically runs from inside the input channel's listener notification, where
			// the old content's controls may still be pending in the listener snapshot. Disposing them
			// synchronously would let those listeners run on a torn-down control, so disposal is
			// deferred until the notification has unwound (mirrors ReactAdaptiveDetailControl).
			ChannelNotificationScope.current().afterNotification(old::cleanupTree);
		}
	}

	private boolean hasDeletedInput() {
		for (TLObject object : inputObjects()) {
			if (!object.tValid()) {
				return true;
			}
		}
		return false;
	}

	private int selectIndex(Object value) {
		for (int i = 0; i < _cases.size(); i++) {
			if (Boolean.TRUE.equals(_cases.get(i)._test().execute(value))) {
				return i;
			}
		}
		return _cases.size();
	}

	private ReactControl buildContent(int index) {
		List<UIElement> elements = index < _cases.size() ? _cases.get(index)._content() : _default;
		ViewContext childContext = _context.childContext("switch").withChildSlotPath(String.valueOf(index));
		return ContentControls.toControl(elements, childContext);
	}

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (_current != null) {
			_current.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_current != null) {
			_current.detach();
		}
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

	@Override
	protected void cleanupChildren() {
		if (_current != null) {
			_current.cleanupTree();
			_current = null;
		}
	}
}
