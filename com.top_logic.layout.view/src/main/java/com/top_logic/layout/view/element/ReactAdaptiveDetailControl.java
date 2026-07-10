/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.responsive.DisplayClass;
import com.top_logic.layout.responsive.DisplayClassModel;
import com.top_logic.layout.responsive.DisplayClassModel.DisplayClassListener;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Server-side control of {@link AdaptiveDetailElement}.
 *
 * <p>
 * Renders one of two presentations of the shared {@code <selector>}/{@code <detail>} configuration,
 * chosen from the subsession's {@link DisplayClassModel}:
 * </p>
 * <ul>
 * <li>{@link DisplayClass#REGULAR} - a horizontal {@link ReactSplitPanelControl split} with the
 * selector and the detail side by side.</li>
 * <li>{@link DisplayClass#COMPACT} - the selector full-bleed, replaced by the detail while the
 * selection channel holds a value.</li>
 * </ul>
 *
 * <p>
 * In compact mode the <em>outermost</em> (coordinator) control renders a single breadcrumb spanning
 * all nested levels (home + one crumb per selected object down the chain); tapping a crumb clears
 * the selections from that level down. Nested controls render no breadcrumb of their own.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReactAdaptiveDetailControl extends ReactControl {

	private static final String REACT_MODULE = "TLAdaptiveDetail";

	private static final String CONTENT = "content";

	private static final String BREADCRUMB = "breadcrumb";

	private static final String DEFAULT_HOME_LABEL = "‹";

	private final ViewContext _context;

	private final List<UIElement> _selector;

	private final List<UIElement> _detail;

	private final ViewChannel _selectionChannel;

	private final DisplayClassModel _displayModel;

	private final DisplayClassListener _displayListener;

	private final ChannelListener _selectionListener;

	/** Whether this is the outermost element (renders the unified breadcrumb). */
	private final boolean _coordinator;

	/** The chain of selection channels (this level first, then nested), only set for a coordinator. */
	private final List<ViewChannel> _chain;

	private final String _homeLabel;

	private ReactControl _currentChild;

	private boolean _disposed;

	/**
	 * Subsession-shared coordinator that defers disposal of replaced presentations until the
	 * triggering channel notification has unwound (see {@link AdaptiveDetailDisposal}).
	 */
	private final AdaptiveDetailDisposal _disposal;

	/**
	 * Creates a new {@link ReactAdaptiveDetailControl}.
	 *
	 * @param context
	 *        The {@link ViewContext} used to build the selector and detail child controls.
	 * @param selector
	 *        The master content elements (write the selection channel).
	 * @param detail
	 *        The detail content elements (bound to the selection channel).
	 * @param selectionChannel
	 *        The shared selection channel.
	 * @param resetOn
	 *        Channels whose change resets {@code selectionChannel} to {@code null} (an upstream
	 *        master selection this selection depends on); may be empty.
	 * @param coordinator
	 *        Whether this is the outermost element that renders the unified breadcrumb.
	 * @param chain
	 *        The ordered chain of selection channels (this level first, then nested); only relevant
	 *        for a coordinator, otherwise {@code null}.
	 * @param homeLabel
	 *        Label of the breadcrumb's home crumb, or {@code null} for a default.
	 */
	public ReactAdaptiveDetailControl(ViewContext context, List<UIElement> selector, List<UIElement> detail,
			ViewChannel selectionChannel, List<ViewChannel> resetOn, boolean coordinator, List<ViewChannel> chain,
			String homeLabel) {
		super(context, null, REACT_MODULE);
		_context = context;
		_selector = selector;
		_detail = detail;
		_selectionChannel = selectionChannel;
		_coordinator = coordinator;
		_chain = chain;
		_homeLabel = homeLabel;
		_displayModel = DisplayClassModel.forCurrentSubSession();
		_disposal = AdaptiveDetailDisposal.forCurrentSubSession();

		_displayListener = (sender, oldValue, newValue) -> renderPresentation(false);
		_displayModel.addListener(DisplayClassModel.DISPLAY_CLASS, _displayListener);
		addCleanupAction(() -> _displayModel.removeListener(DisplayClassModel.DISPLAY_CLASS, _displayListener));

		_selectionListener = (sender, oldValue, newValue) -> onSelectionChanged();
		_selectionChannel.addListener(_selectionListener);
		addCleanupAction(() -> _selectionChannel.removeListener(_selectionListener));

		// Reset this selection whenever a master selection it depends on changes, so a stale value
		// cannot resurface under a different master (the selector that would prune it may not be
		// rendered in compact mode).
		ChannelListener resetListener = (sender, oldValue, newValue) -> _selectionChannel.set(null);
		for (ViewChannel master : resetOn) {
			master.addListener(resetListener);
			addCleanupAction(() -> master.removeListener(resetListener));
		}

		// Keep the breadcrumb in sync when a deeper selection changes (the own selection is handled
		// by the selection listener via renderPresentation). The coordinator control is stable, so
		// these listeners never fire on a torn-down control.
		if (coordinator && chain != null) {
			ChannelListener breadcrumbListener = (sender, oldValue, newValue) -> updateBreadcrumb(false);
			for (int i = 1; i < chain.size(); i++) {
				ViewChannel deeper = chain.get(i);
				deeper.addListener(breadcrumbListener);
				addCleanupAction(() -> deeper.removeListener(breadcrumbListener));
			}
		}

		renderPresentation(true);
	}

	private void onSelectionChanged() {
		// In REGULAR the split's detail reacts to the channel itself; only COMPACT toggles selector
		// vs. detail and therefore needs a rebuild.
		if (_displayModel.getDisplayClass() == DisplayClass.COMPACT) {
			renderPresentation(false);
		}
	}

	private void renderPresentation(boolean initial) {
		if (_disposed) {
			// A nested control may still receive a display-class event from the stack-copy of
			// listeners after its parent rebuilt and tore it down; ignore it.
			return;
		}
		boolean compact = _displayModel.getDisplayClass() == DisplayClass.COMPACT;
		boolean hasSelection = _selectionChannel.get() != null;

		ReactControl built;
		if (compact && hasSelection) {
			built = buildContent(_detail);
		} else if (compact) {
			built = buildContent(_selector);
		} else {
			built = buildRegularSplit();
		}

		ReactControl old = _currentChild;
		_currentChild = built;
		if (initial) {
			putStateSilent(CONTENT, built);
			putStateSilent(BREADCRUMB, buildBreadcrumb());
		} else {
			Object token = beginUpdate();
			putState(CONTENT, built);
			putState(BREADCRUMB, buildBreadcrumb());
			commitUpdate(token);
			if (old != null && old != built) {
				if (_disposal.isDeferring()) {
					_disposal.defer(old);
				} else {
					old.cleanupTree();
				}
			}
		}
	}

	private void updateBreadcrumb(boolean initial) {
		Object items = buildBreadcrumb();
		if (initial) {
			putStateSilent(BREADCRUMB, items);
		} else {
			putState(BREADCRUMB, items);
		}
	}

	/**
	 * Builds the breadcrumb items for the coordinator in compact mode: a home crumb plus one crumb
	 * per selected object down the chain. Returns {@code null} when no breadcrumb should be shown
	 * (not a coordinator, not compact, or nothing selected yet).
	 */
	private List<Map<String, Object>> buildBreadcrumb() {
		if (!_coordinator || _chain == null || _displayModel.getDisplayClass() != DisplayClass.COMPACT) {
			return null;
		}
		List<Map<String, Object>> items = new ArrayList<>();
		for (int i = 0; i < _chain.size(); i++) {
			Object value = _chain.get(i).get();
			if (value == null) {
				break;
			}
			items.add(crumb(i + 1, MetaLabelProvider.INSTANCE.getLabel(value)));
		}
		if (items.isEmpty()) {
			// Nothing selected: showing the selector full-bleed, no breadcrumb needed.
			return null;
		}
		items.add(0, crumb(0, _homeLabel != null ? _homeLabel : DEFAULT_HOME_LABEL));
		return items;
	}

	private static Map<String, Object> crumb(int depth, String label) {
		Map<String, Object> entry = new HashMap<>(2);
		entry.put("depth", Integer.valueOf(depth));
		entry.put("label", label);
		return entry;
	}

	private ReactControl buildRegularSplit() {
		ReactSplitPanelControl split = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		split.addChild(buildContent(_selector), new ChildConstraint(32f, DisplayUnit.PERCENT, 240, Scrolling.AUTO));
		split.addChild(buildContent(_detail), new ChildConstraint(68f, DisplayUnit.PERCENT, 320, Scrolling.AUTO));
		return split;
	}

	private ReactControl buildContent(List<UIElement> elements) {
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(_context);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(_context))
			.collect(Collectors.toList());
		return new ReactStackControl(_context, children);
	}

	/**
	 * Navigates to a breadcrumb crumb: clears the chain selections from the crumb's depth down,
	 * returning the drill-in to that level.
	 *
	 * <p>
	 * Clearing runs (re-entrantly) inside {@link ViewChannel#set} while channels notify their
	 * listener snapshots, so disposal of the replaced presentation is deferred until the
	 * notifications have unwound (see {@link #_disposal}).
	 * </p>
	 *
	 * @param arguments
	 *        Command arguments; {@code "depth"} is the target chain depth to clear from.
	 */
	@ReactCommandHandler("navigate")
	void handleNavigate(Map<String, Object> arguments) {
		if (_chain == null) {
			return;
		}
		Object depthArg = arguments.get("depth");
		if (!(depthArg instanceof Number)) {
			return;
		}
		int depth = ((Number) depthArg).intValue();
		if (depth < 0 || depth >= _chain.size()) {
			return;
		}
		_disposal.run(() -> {
			for (int j = depth; j < _chain.size(); j++) {
				_chain.get(j).set(null);
			}
		});
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

	@Override
	protected void cleanupChildren() {
		if (_currentChild != null) {
			_currentChild.cleanupTree();
			_currentChild = null;
		}
	}

}
