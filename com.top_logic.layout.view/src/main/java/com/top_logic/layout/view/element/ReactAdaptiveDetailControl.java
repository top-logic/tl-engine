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

import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Anchor;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Position;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Size;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.responsive.DisplayClass;
import com.top_logic.layout.responsive.DisplayClassModel;
import com.top_logic.layout.responsive.DisplayClassModel.DisplayClassListener;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.VetoForwarder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.AdaptiveDetailElement.Config;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Server-side control of {@link AdaptiveDetailElement}.
 *
 * <p>
 * Renders one of three presentations of the shared {@code <selector>}/{@code <detail>}
 * configuration, chosen from the subsession's {@link DisplayClassModel} and the element's
 * {@link DetailDisplay}:
 * </p>
 * <ul>
 * <li>{@link DisplayClass#REGULAR} with {@link DetailDisplay#SPLIT} - a horizontal
 * {@link ReactSplitPanelControl split} with the selector and the detail side by side.</li>
 * <li>{@link DisplayClass#REGULAR} with {@link DetailDisplay#DRAWER} - the selector at full width,
 * overlaid by a {@link ReactDrawerControl drawer} holding the detail. The drawer is anchored in
 * this control's own area, so the surrounding chrome stays visible; it opens while the selection
 * channel holds a value, carries that value's label as its title, and clears the selection when it
 * is dismissed.</li>
 * <li>{@link DisplayClass#COMPACT} - the selector full-bleed, replaced by the detail while the
 * selection channel holds a value.</li>
 * </ul>
 *
 * <p>
 * A presentation builds its selector and its detail once and keeps them across selection changes:
 * both are bound to the selection channel and follow it themselves. Only the compact presentation,
 * which exchanges one for the other, rebuilds.
 * </p>
 *
 * <p>
 * In compact mode the <em>outermost</em> (coordinator) control renders a single breadcrumb spanning
 * all nested levels (home + one crumb per selected object down the chain); tapping a crumb clears
 * the selections from that level down. Nested controls render no breadcrumb of their own.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReactAdaptiveDetailControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLAdaptiveDetail";

	private static final String CONTENT = "content";

	private static final String OVERLAY = "overlay";

	private static final String BREADCRUMB = "breadcrumb";

	private static final String DEFAULT_HOME_LABEL = "‹";

	private final ViewContext _context;

	private final ViewContext _selectorContext;

	private final ViewContext _detailContext;

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

	private final DetailDisplay _detailDisplay;

	private final int _detailSize;

	private ReactControl _currentChild;

	/** The drawer holding the detail, while the drawer presentation is displayed. */
	private ReactDrawerControl _drawer;

	private boolean _disposed;

	/**
	 * Creates a new {@link ReactAdaptiveDetailControl}.
	 *
	 * @param context
	 *        The {@link ViewContext} used to build the selector and detail child controls.
	 * @param element
	 *        The configured element this control displays, addressed when the selector or the
	 *        detail is to be brought into view.
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
	 * @param detailDisplay
	 *        How the detail is presented beside the selector on a wide viewport.
	 * @param detailSize
	 *        The width in pixels of the drawer the detail is displayed in.
	 */
	public ReactAdaptiveDetailControl(ViewContext context, AdaptiveDetailElement element,
			List<UIElement> selector, List<UIElement> detail,
			ViewChannel selectionChannel, List<ViewChannel> resetOn, boolean coordinator, List<ViewChannel> chain,
			String homeLabel, DetailDisplay detailDisplay, int detailSize) {
		super(context, null, REACT_MODULE);
		_context = context;
		RevealPath here = RevealPath.of(context);
		_selectorContext = context.withScope(RevealPath.class, here.append(element, Config.SELECTOR));
		_detailContext = context.withScope(RevealPath.class, here.append(element, Config.DETAIL));
		_selector = selector;
		_detail = detail;
		_selectionChannel = selectionChannel;
		_coordinator = coordinator;
		_chain = chain;
		_homeLabel = homeLabel;
		_detailDisplay = detailDisplay;
		_detailSize = detailSize;
		_displayModel = DisplayClassModel.forCurrentSubSession();

		_displayListener = (sender, oldValue, newValue) -> renderPresentation();
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

			// The reset discards the detail's selection, so the unsaved changes blocking it are
			// reported when the master is asked, before the master is written.
			addCleanupAction(VetoForwarder.forward(master, _selectionChannel));
		}

		// Keep the breadcrumb in sync when a deeper selection changes (the own selection is handled
		// by the selection listener via renderPresentation). The coordinator control is stable, so
		// these listeners never fire on a torn-down control.
		if (coordinator && chain != null) {
			ChannelListener breadcrumbListener = (sender, oldValue, newValue) -> updateBreadcrumb();
			for (int i = 1; i < chain.size(); i++) {
				ViewChannel deeper = chain.get(i);
				deeper.addListener(breadcrumbListener);
				addCleanupAction(() -> deeper.removeListener(breadcrumbListener));
			}
		}

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			addCleanupAction(registry.registerContainer(element, here, this));
		}

		renderPresentation();
	}

	/**
	 * Brings the selector or the detail side into view.
	 *
	 * <p>
	 * On a wide viewport the selector is displayed anyway, beside the detail or underneath its
	 * drawer. On a narrow one the selector returns by dropping the selection, exactly as the
	 * breadcrumb's home crumb does. The detail, in every presentation, appears as soon as the
	 * selection channel holds the object it displays.
	 * </p>
	 */
	@Override
	public void revealChild(String key) {
		if (Config.SELECTOR.equals(key)) {
			if (_displayModel.getDisplayClass() == DisplayClass.COMPACT) {
				_selectionChannel.set(null);
			}
		} else if (!Config.DETAIL.equals(key)) {
			throw new IllegalArgumentException("A master-detail element has no side '" + key + "'.");
		}
	}

	private void onSelectionChanged() {
		// In REGULAR the detail reacts to the channel itself; only COMPACT toggles selector vs.
		// detail and therefore needs a rebuild. A drawer presentation keeps its detail as well and
		// only follows the selection with the panel it is shown in.
		if (_displayModel.getDisplayClass() == DisplayClass.COMPACT) {
			renderPresentation();
		} else if (_drawer != null) {
			updateDrawer(_drawer);
		}
	}

	/**
	 * Opens the given drawer on the object now selected (titled with its label) and closes it when
	 * the selection is dropped.
	 *
	 * <p>
	 * The title of a closing drawer is left untouched: it stays readable while the panel slides
	 * out, and the next opening replaces it.
	 * </p>
	 *
	 * @param drawer
	 *        The drawer to bring in line with the selection.
	 */
	private void updateDrawer(ReactDrawerControl drawer) {
		Object selection = _selectionChannel.get();
		if (selection == null) {
			drawer.close();
		} else {
			drawer.setTitle(MetaLabelProvider.INSTANCE.getLabel(selection));
			drawer.open();
		}
	}

	private void renderPresentation() {
		if (_disposed) {
			// A nested control may still receive a display-class event from the stack-copy of
			// listeners after its parent rebuilt and tore it down; ignore it.
			return;
		}
		boolean compact = _displayModel.getDisplayClass() == DisplayClass.COMPACT;
		boolean hasSelection = _selectionChannel.get() != null;

		ReactControl built;
		ReactDrawerControl drawer;
		if (compact && hasSelection) {
			built = buildDetail();
			drawer = null;
		} else if (compact) {
			built = buildSelector();
			drawer = null;
		} else if (_detailDisplay == DetailDisplay.DRAWER) {
			built = buildSelector();
			drawer = buildDetailDrawer();
		} else {
			built = buildRegularSplit();
			drawer = null;
		}

		ReactControl oldContent = _currentChild;
		ReactDrawerControl oldDrawer = _drawer;
		_currentChild = built;
		_drawer = drawer;
		Object token = beginUpdate();
		putState(CONTENT, built);
		putState(OVERLAY, drawer);
		putState(BREADCRUMB, buildBreadcrumb());
		commitUpdate(token);
		if (oldContent != null && oldContent != built) {
			ContentControls.retire(oldContent);
		}
		if (oldDrawer != null && oldDrawer != drawer) {
			// The drawer owns the detail it holds, so retiring it retires the detail with it.
			ContentControls.retire(oldDrawer);
		}
	}

	private void updateBreadcrumb() {
		putState(BREADCRUMB, buildBreadcrumb());
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
		split.addChild(buildSelector(), new ChildConstraint(32f, DisplayUnit.PERCENT, 240, Scrolling.AUTO));
		split.addChild(buildDetail(), new ChildConstraint(68f, DisplayUnit.PERCENT, 320, Scrolling.AUTO));
		return split;
	}

	/**
	 * Builds the drawer the detail is displayed in, showing the object currently selected.
	 *
	 * <p>
	 * Dismissing the drawer drops the selection, which is what closes it - and what lets the same
	 * object be selected again to bring it back. A detail refusing to be left (unsaved changes)
	 * vetoes that channel write, and the drawer stays open.
	 * </p>
	 */
	private ReactDrawerControl buildDetailDrawer() {
		ReactDrawerControl drawer = new ReactDrawerControl(_context, null, Position.RIGHT, Size.MEDIUM,
			Anchor.CONTAINER, () -> _selectionChannel.set(null));
		drawer.setWidth(_detailSize);
		drawer.setChild(buildDetail());
		updateDrawer(drawer);
		return drawer;
	}

	private ReactControl buildSelector() {
		return ContentControls.toControl(_selector, _selectorContext);
	}

	private ReactControl buildDetail() {
		return ContentControls.toControl(_detail, _detailContext);
	}

	/**
	 * Navigates to a breadcrumb crumb: clears the chain selections from the crumb's depth down,
	 * returning the drill-in to that level.
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
		for (int j = depth; j < _chain.size(); j++) {
			_chain.get(j).set(null);
		}
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}
