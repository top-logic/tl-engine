/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A cell control that displays a business object using a {@link ResourceProvider}.
 *
 * <p>
 * Resolves label, icon, CSS class, tooltip, and link availability from the provider and sends them
 * as flat state to the {@code TLResourceCell} React component, together with the
 * {@link ReactValueColor#COLOR color} the displayed value carries in the model.
 * </p>
 *
 * <p>
 * The cell is a link when something can act on a click: a {@link GotoListener} set via
 * {@link #setGotoListener(GotoListener)}, or - where the cell is allowed to link at all - the
 * {@link ObjectNavigator} of the context, which leads to the place the application displays the
 * value at. The listener takes precedence, so a display that knows where its own values lead keeps
 * deciding that itself.
 * </p>
 */
public class ReactResourceCellControl extends ReactControl implements TooltipProvider {

	/**
	 * Listener that is notified when the user clicks a linked resource cell.
	 */
	public interface GotoListener {

		/**
		 * Called when the user requests navigation to the given object.
		 *
		 * @param context
		 *        The current view display context.
		 * @param target
		 *        The business object to navigate to.
		 * @return The handler result.
		 */
		HandlerResult handleGoto(ReactContext context, Object target);
	}

	// -- State keys --

	private static final String LABEL = "label";

	private static final String ICON_CSS = "iconCss";

	private static final String ICON_SRC = "iconSrc";

	private static final String CSS_CLASS = "cssClass";

	private static final String HAS_TOOLTIP = "hasTooltip";

	/** Key expected by {@link #getTooltipContent(String)}. */
	private static final String TOOLTIP_KEY = "tooltip";

	private static final String HAS_LINK = "hasLink";

	// -- Commands --

	/** Command sent when the user follows the link of the displayed value. */
	private static final String CMD_GOTO = "goto";

	// -- Configuration --

	private static final String CSS_PREFIX = "css:";

	private static final String COLORED_CSS_PREFIX = "colored:";

	// -- Fields --

	private final ResourceProvider _provider;

	private final boolean _useImage;

	private final boolean _useLabel;

	private final boolean _useLink;

	private Object _rowObject;

	private String _tooltipHtml;

	private GotoListener _gotoListener;

	/**
	 * Creates a new {@link ReactResourceCellControl}.
	 *
	 * @param value
	 *        The value to display.
	 * @param provider
	 *        The {@link ResourceProvider} to resolve display data from.
	 * @param useImage
	 *        Whether to resolve and display the type icon.
	 * @param useLabel
	 *        Whether to resolve and display the label text.
	 * @param useLink
	 *        Whether the cell may become a link. A cell that may link does so as soon as something
	 *        can act on the click.
	 */
	public ReactResourceCellControl(ReactContext context, Object value, ResourceProvider provider, boolean useImage, boolean useLabel,
			boolean useLink) {
		super(context, null, "TLResourceCell");
		_provider = provider;
		_useImage = useImage;
		_useLabel = useLabel;
		_useLink = useLink;
		_rowObject = value;
		resolveState(value);
	}

	/**
	 * Convenience constructor with all display options enabled.
	 */
	public ReactResourceCellControl(ReactContext context, Object value, ResourceProvider provider) {
		this(context, value, provider, true, true, true);
	}

	/**
	 * Sets the listener that handles goto navigation when the user clicks a linked resource.
	 *
	 * @param listener
	 *        The goto listener, or {@code null} to disable navigation.
	 */
	public void setGotoListener(GotoListener listener) {
		_gotoListener = listener;
		updateLinkState();
	}

	/**
	 * Updates the displayed value.
	 */
	public void update(Object value) {
		_rowObject = value;
		resolveState(value);
	}

	private void resolveState(Object value) {
		if (_useLabel) {
			String label = value != null ? _provider.getLabel(value) : null;
			putState(LABEL, label != null ? label : "");
		}

		if (_useImage && value != null) {
			resolveIcon(value);
		}

		putState(ReactValueColor.COLOR, ReactValueColor.cssColorOf(value));

		if (value != null) {
			String cssClass = _provider.getCssClass(value);
			if (cssClass != null) {
				putState(CSS_CLASS, cssClass);
			}

			String tooltip = _provider.getTooltip(value);
			_tooltipHtml = (tooltip == null || tooltip.isEmpty()) ? null : tooltip;
		} else {
			_tooltipHtml = null;
		}
		putState(HAS_TOOLTIP, _tooltipHtml != null);

		updateLinkState();
	}

	/**
	 * Tells the client whether the displayed value is a link.
	 */
	private void updateLinkState() {
		putState(HAS_LINK, Boolean.valueOf(hasLink()));
	}

	/**
	 * Whether a click on the displayed value leads anywhere.
	 */
	private boolean hasLink() {
		Object value = _rowObject;
		if (value == null) {
			return false;
		}
		if (_gotoListener != null) {
			return true;
		}
		return _useLink && canShow(value);
	}

	private boolean canShow(Object value) {
		ObjectNavigator navigator = navigator();
		return navigator != null && navigator.canShow(value);
	}

	private ObjectNavigator navigator() {
		ReactContext context = getReactContext();
		return context == null ? null : context.getObjectNavigator();
	}

	@Override
	public TooltipContent getTooltipContent(String key) {
		if (!TOOLTIP_KEY.equals(key) || _tooltipHtml == null) {
			return null;
		}
		return new TooltipContent(_tooltipHtml, null, true);
	}

	private void resolveIcon(Object value) {
		ThemeImage image = _provider.getImage(value, Flavor.DEFAULT);
		if (image == null) {
			return;
		}
		ThemeImage resolved = image.resolve();
		if (resolved == ThemeImage.none()) {
			return;
		}
		String encoded = resolved.toEncodedForm();
		if (encoded.startsWith(CSS_PREFIX)) {
			putState(ICON_CSS, encoded.substring(CSS_PREFIX.length()));
		} else if (encoded.startsWith(COLORED_CSS_PREFIX)) {
			putState(ICON_CSS, encoded.substring(COLORED_CSS_PREFIX.length()));
		} else {
			// Resource image path.
			putState(ICON_SRC, encoded);
		}
	}

	/**
	 * Follows the link of the displayed value: to where the {@link GotoListener} leads if one is
	 * set, and otherwise to the place the application displays the value at.
	 */
	@ReactCommandHandler(CMD_GOTO)
	HandlerResult handleGoto(ReactContext context) {
		Object target = _rowObject;
		if (target == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		GotoListener listener = _gotoListener;
		if (listener != null) {
			return listener.handleGoto(context, target);
		}
		ObjectNavigator navigator = navigator();
		if (navigator != null && navigator.canShow(target)) {
			navigator.show(context, target);
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
