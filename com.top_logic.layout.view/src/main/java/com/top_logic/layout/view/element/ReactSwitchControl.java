/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.SwitchElement.SwitchCase;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;

/**
 * Server-side control of {@link SwitchElement}: shows the content of the first {@code <case>} whose
 * predicate matches the input channel's current value (or the {@code <default>} content), and swaps
 * to a different case's content when the matching case changes.
 *
 * <p>
 * Re-evaluation is triggered by a new input channel value, and by a change of the input object
 * itself: a predicate typically decides by one of its attributes, which can be edited while the
 * channel keeps pointing to the same object. A {@link ChannelObjectObserver} therefore observes the
 * input object in the {@link ModelScope}, along with the optionally configured
 * {@link SwitchElement.Config#getObservedTypes() observed types}. While the input object is
 * deleted, the observer reports no change, so no case predicate is evaluated over it: the shown
 * case stays until the channel delivers its next value.
 * </p>
 *
 * <p>
 * Unlike a caching deck, the previously shown content is <em>disposed</em> on a case change rather
 * than kept alive. This keeps the enclosing scope free of a hidden case's contributions (e.g. a
 * form's edit/save/cancel commands), which a cached-but-detached case would otherwise leave behind
 * as duplicate toolbar buttons. Within a single case, the content is bound to its own channels and
 * updates itself - the switch does not rebuild while the matching case stays the same, unless it
 * is configured to rebuild for every new input value.
 * </p>
 *
 * <p>
 * The React component {@code TLDeckPane} renders the single {@code activeChild}. While the chosen
 * content is empty - no case matches and there is no {@code <default>} - the switch is
 * {@link #setHidden(boolean) hidden}, so that it takes no place in its container.
 * </p>
 */
public class ReactSwitchControl extends ReactControl {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_CHILD = "activeChild";

	private final ViewContext _context;

	private final ViewChannel _input;

	private final List<SwitchCase> _cases;

	private final List<UIElement> _default;

	private final ChannelObjectObserver _inputObserver;

	private final boolean _rebuild;

	private ReactControl _current;

	/** The input value the {@link #_current} content was built for. */
	private Object _shownValue;

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
	 * @param rebuild
	 *        Whether the content is built anew for every new input value, see
	 *        {@link SwitchElement.Config#getRebuild()}.
	 */
	ReactSwitchControl(ViewContext context, ViewChannel input, List<SwitchCase> cases,
			List<UIElement> defaultContent, Set<TLStructuredType> observedTypes, boolean rebuild) {
		super(context, null, REACT_MODULE);
		_rebuild = rebuild;
		_context = context;
		_input = input;
		_cases = cases;
		_default = defaultContent;
		_inputObserver = new ChannelObjectObserver(List.of(input), observedTypes, this::renderActive);

		Registration inputRegistration = _input.addListener((sender, oldValue, newValue) -> renderActive());
		addCleanupAction(inputRegistration::dispose);

		addAttachListener(() -> _inputObserver.attach(_context.getModelScope()));
		addDetachListener(_inputObserver::detach);

		renderActive();
	}

	private void renderActive() {
		if (_disposed) {
			return;
		}
		Object value = _input.get();
		int index = selectIndex(value);
		if (index == _activeIndex && _current != null && !(_rebuild && value != _shownValue)) {
			// The matching case is unchanged; its content is bound to the channel and updates itself.
			return;
		}
		_activeIndex = index;
		_shownValue = value;

		List<UIElement> content = content(index);
		ReactControl built = buildContent(index, content);
		ReactControl old = _current;
		_current = built;

		Object token = beginUpdate();
		putState(ACTIVE_CHILD, built);
		// With nothing to show, the switch takes no place among its siblings - no gap of the
		// enclosing container on either side of it.
		setHidden(content.isEmpty());
		commitUpdate(token);

		if (isAttached()) {
			built.attach();
		}
		if (old != null && old != built) {
			ContentControls.retire(old);
		}
	}

	private int selectIndex(Object value) {
		for (int i = 0; i < _cases.size(); i++) {
			if (Boolean.TRUE.equals(_cases.get(i)._test().execute(value))) {
				return i;
			}
		}
		return _cases.size();
	}

	private List<UIElement> content(int index) {
		return index < _cases.size() ? _cases.get(index)._content() : _default;
	}

	private ReactControl buildContent(int index, List<UIElement> elements) {
		ViewContext childContext = _context.childContext("switch").withChildSlotPath(String.valueOf(index));
		return ContentControls.toControl(elements, childContext);
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}
