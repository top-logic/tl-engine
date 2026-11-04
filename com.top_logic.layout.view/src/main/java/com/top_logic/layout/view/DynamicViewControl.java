/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Server-side control of {@link DynamicViewElement}: shows the view whose configuration the
 * element's expression currently yields, and replaces it when the expression yields another
 * configuration.
 *
 * <p>
 * The expression is evaluated again on a new value of an input channel and on a change of an input
 * object (observed by a {@link ChannelObjectObserver}). The embedded view is rebuilt only when the
 * resulting configuration is another one than the shown one: a stored configuration is not changed
 * in place, an edit of the attribute holding it stores a new one. The previously shown view is
 * {@link ContentControls#retire(ReactControl) retired}, so that its disposal waits until the
 * channel notification that caused the switch has unwound.
 * </p>
 *
 * <p>
 * The React component {@code TLDeckPane} renders the single {@code activeChild}. While the
 * expression yields {@code null}, the control is {@link #setHidden(boolean) hidden}, so that it
 * takes no place in its container.
 * </p>
 */
public class DynamicViewControl extends ReactControl {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_CHILD = "activeChild";

	private final ViewContext _context;

	private final List<ViewChannel> _inputs;

	private final QueryExecutor _view;

	private final List<ChannelBindingConfig> _bindings;

	private final String _source;

	private ReactControl _current;

	/** The configuration the {@link #_current} content was built for. */
	private Object _shownView;

	private boolean _disposed;

	/**
	 * Creates a new {@link DynamicViewControl}.
	 *
	 * @param context
	 *        The context of the element; the embedded view gets an isolated context derived from it.
	 * @param inputs
	 *        The channels whose values the expression reads.
	 * @param view
	 *        The expression computing the configuration of the embedded view from the input values.
	 * @param bindings
	 *        The channels shared with the embedded view, see {@link ViewBindings#getBindings()}.
	 * @param observedTypes
	 *        Types whose object changes trigger a new evaluation in addition to the input objects
	 *        (may be empty).
	 * @param source
	 *        The name of the element in a message about a view that cannot be shown.
	 */
	public DynamicViewControl(ViewContext context, List<ViewChannel> inputs, QueryExecutor view,
			List<ChannelBindingConfig> bindings, Set<TLStructuredType> observedTypes, String source) {
		super(context, null, REACT_MODULE);
		_context = context;
		_inputs = inputs;
		_view = view;
		_bindings = bindings;
		_source = source;

		ChannelListener inputListener = (sender, oldValue, newValue) -> update();
		for (ViewChannel input : inputs) {
			Registration registration = input.addListener(inputListener);
			addCleanupAction(registration::dispose);
		}

		ChannelObjectObserver observer = new ChannelObjectObserver(inputs, observedTypes, this::update);
		addAttachListener(() -> observer.attach(_context.getModelScope()));
		addDetachListener(observer::detach);

		update();
	}

	private void update() {
		if (_disposed) {
			return;
		}
		Object view = _view.execute(ChannelInputs.arguments(_inputs));
		if (_current != null && view == _shownView) {
			// The same configuration is shown; its content updates itself from its channels.
			return;
		}
		_shownView = view;

		ReactControl built = build(view);
		ReactControl old = _current;
		_current = built;

		Object token = beginUpdate();
		putState(ACTIVE_CHILD, built);
		setHidden(view == null);
		commitUpdate(token);

		if (isAttached()) {
			built.attach();
		}
		if (old != null) {
			ContentControls.retire(old);
		}
	}

	private ReactControl build(Object view) {
		if (view == null) {
			return ContentControls.combine(_context, List.of());
		}
		if (!(view instanceof ViewElement.Config config)) {
			return ViewLoadError.createControl(_context, _source,
				new IllegalArgumentException("Not a view configuration: " + view));
		}

		ViewElement element;
		try {
			element = ViewLoader.instantiateView(config);
		} catch (ConfigurationException | ConfigurationError ex) {
			// A defective view replaces only itself, the enclosing view keeps rendering.
			Logger.error("Rendering a placeholder for the view that could not be instantiated: " + _source, ex,
				DynamicViewControl.class);
			return ViewLoadError.createControl(_context, _source, ex);
		}
		ViewContext childContext = ViewBindings.embeddedContext(_context, _bindings);
		return (ReactControl) element.createControl(childContext);
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}
