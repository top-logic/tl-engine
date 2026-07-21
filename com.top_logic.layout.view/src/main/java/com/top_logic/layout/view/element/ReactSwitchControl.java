/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.element.SwitchElement.SwitchCase;

/**
 * Server-side control of {@link SwitchElement}: shows the content of the first {@code <case>} whose
 * predicate matches the input channel's current value (or the {@code <default>} content), and swaps
 * to a different case's content when the matching case changes.
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
public class ReactSwitchControl extends ReactControl {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_CHILD = "activeChild";

	private final ViewContext _context;

	private final ViewChannel _input;

	private final List<SwitchCase> _cases;

	private final List<UIElement> _default;

	private final ChannelListener _inputListener;

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
	 */
	ReactSwitchControl(ViewContext context, ViewChannel input, List<SwitchCase> cases,
			List<UIElement> defaultContent) {
		super(context, null, REACT_MODULE);
		_context = context;
		_input = input;
		_cases = cases;
		_default = defaultContent;

		_inputListener = (sender, oldValue, newValue) -> renderActive();
		_input.addListener(_inputListener);
		addCleanupAction(() -> _input.removeListener(_inputListener));

		renderActive();
	}

	private void renderActive() {
		if (_disposed) {
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
