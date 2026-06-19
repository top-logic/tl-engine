/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommand;
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
 * <li>{@link DisplayClass#COMPACT} - the selector full-bleed, replaced by the detail (with a back
 * affordance) while the selection channel holds a value.</li>
 * </ul>
 *
 * <p>
 * The control rebuilds its presentation when the display class changes (always) or when the
 * selection changes (only while {@code COMPACT}; in {@code REGULAR} the detail reacts to the channel
 * itself). The selection lives in the channel, so a display-class flip preserves it.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReactAdaptiveDetailControl extends ReactControl {

	private static final String REACT_MODULE = "TLAdaptiveDetail";

	private static final String CONTENT = "content";

	private static final String SHOW_BACK = "showBack";

	private static final String BAR_LABEL = "barLabel";

	private final ViewContext _context;

	private final List<UIElement> _selector;

	private final List<UIElement> _detail;

	private final ViewChannel _selectionChannel;

	private final DisplayClassModel _displayModel;

	private final DisplayClassListener _displayListener;

	private final ChannelListener _selectionListener;

	private ReactControl _currentChild;

	private boolean _disposed;

	/**
	 * While {@code true}, the old presentation is not torn down synchronously but collected in
	 * {@link #_deferredDisposal} and disposed once the triggering channel notification has unwound.
	 *
	 * <p>
	 * Needed because the old presentation's children (the detail's form, the dependent selector
	 * table) are themselves listeners of the very channel whose {@code set()} triggered the rebuild.
	 * The channel notifies a snapshot of its listeners, so those children are still pending in the
	 * current notification; disposing them mid-notification would let them run on a control whose
	 * SSE queue / model scope is already gone.
	 * </p>
	 */
	private boolean _deferDisposal;

	private final List<ReactControl> _deferredDisposal = new ArrayList<>();

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
	 */
	public ReactAdaptiveDetailControl(ViewContext context, List<UIElement> selector, List<UIElement> detail,
			ViewChannel selectionChannel) {
		super(context, null, REACT_MODULE);
		_context = context;
		_selector = selector;
		_detail = detail;
		_selectionChannel = selectionChannel;
		_displayModel = DisplayClassModel.forCurrentSubSession();

		_displayListener = (sender, oldValue, newValue) -> renderPresentation(false);
		_displayModel.addListener(DisplayClassModel.DISPLAY_CLASS, _displayListener);
		addCleanupAction(() -> _displayModel.removeListener(DisplayClassModel.DISPLAY_CLASS, _displayListener));

		_selectionListener = (sender, oldValue, newValue) -> onSelectionChanged();
		_selectionChannel.addListener(_selectionListener);
		addCleanupAction(() -> _selectionChannel.removeListener(_selectionListener));

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
		boolean showBack;
		String barLabel;
		if (compact && hasSelection) {
			built = buildContent(_detail);
			showBack = true;
			barLabel = MetaLabelProvider.INSTANCE.getLabel(_selectionChannel.get());
		} else if (compact) {
			built = buildContent(_selector);
			showBack = false;
			barLabel = null;
		} else {
			built = buildRegularSplit();
			showBack = false;
			barLabel = null;
		}

		ReactControl old = _currentChild;
		_currentChild = built;
		if (initial) {
			putStateSilent(CONTENT, built);
			putStateSilent(SHOW_BACK, Boolean.valueOf(showBack));
			putStateSilent(BAR_LABEL, barLabel);
		} else {
			Object token = beginUpdate();
			putState(CONTENT, built);
			putState(SHOW_BACK, Boolean.valueOf(showBack));
			putState(BAR_LABEL, barLabel);
			commitUpdate(token);
			if (old != null && old != built) {
				if (_deferDisposal) {
					_deferredDisposal.add(old);
				} else {
					old.cleanupTree();
				}
			}
		}
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
	 * Clears the selection, returning the compact presentation to the selector.
	 *
	 * <p>
	 * The rebuild runs (re-entrantly) inside {@link com.top_logic.layout.view.channel.ViewChannel#set}
	 * while the channel is still notifying its listener snapshot, so disposal of the old presentation
	 * is deferred until the notification has fully unwound (see {@link #_deferDisposal}).
	 * </p>
	 */
	@ReactCommand("back")
	void handleBack() {
		_deferDisposal = true;
		try {
			_selectionChannel.set(null);
		} finally {
			_deferDisposal = false;
			for (ReactControl old : _deferredDisposal) {
				old.cleanupTree();
			}
			_deferredDisposal.clear();
		}
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
