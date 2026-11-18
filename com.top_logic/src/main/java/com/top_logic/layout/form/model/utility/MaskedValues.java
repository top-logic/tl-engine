/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.io.IOException;
import java.util.Comparator;

import com.top_logic.basic.col.Matcher;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.model.security.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Presentation of the values of a form field that hides the identity of the objects the current
 * user may not discover.
 *
 * <p>
 * A form field holds the stored value of a reference, which may contain objects the current user
 * may not discover, see {@link DiscoverRight#isMasked(Object)}. Such an object is shown by the
 * placeholder {@link I18NConstants#MASKED_VALUE}: without its label, tooltip, image, link and
 * context menu. In an order by label, it comes after all other values, so that its position does
 * not disclose its label.
 * </p>
 *
 * <p>
 * The decorators of this class take the presentation of the other values from the presentation
 * they decorate.
 * </p>
 */
public final class MaskedValues {

	private MaskedValues() {
		// Static utility.
	}

	/**
	 * The text shown instead of the label of a masked value.
	 */
	public static String placeholder() {
		return Resources.getInstance().getString(I18NConstants.MASKED_VALUE);
	}

	/**
	 * The given labels with masked values shown as {@link #placeholder()}, without tooltip, image,
	 * link, CSS class and type.
	 *
	 * @param labels
	 *        The presentation of all other values.
	 * @return A {@link ResourceProvider} masking undiscoverable values.
	 */
	public static ResourceProvider labels(LabelProvider labels) {
		if (labels instanceof MaskingResourceProvider masking) {
			return masking;
		}
		return new MaskingResourceProvider(labels);
	}

	/**
	 * The given order with all masked values after all other values.
	 *
	 * <p>
	 * Masked values are equal to each other, so that a stable sort keeps their order.
	 * </p>
	 *
	 * @param order
	 *        The order of all other values.
	 * @return An order placing masked values last.
	 */
	public static Comparator<Object> maskedLast(Comparator<Object> order) {
		if (order instanceof MaskedLastComparator) {
			return order;
		}
		return new MaskedLastComparator(order);
	}

	/**
	 * The given context menus without a context menu for a masked value.
	 *
	 * @param contextMenu
	 *        The context menus of all other values.
	 * @return A {@link ContextMenuProvider} offering no menu for an undiscoverable value.
	 */
	public static ContextMenuProvider contextMenu(ContextMenuProvider contextMenu) {
		if (contextMenu instanceof MaskingContextMenuProvider) {
			return contextMenu;
		}
		return new MaskingContextMenuProvider(contextMenu);
	}

	/**
	 * The given renderer, writing the {@link #placeholder()} for a masked value.
	 *
	 * @param renderer
	 *        The renderer of all other values.
	 * @return A {@link Renderer} masking undiscoverable values.
	 */
	public static Renderer<Object> renderer(Renderer<Object> renderer) {
		if (renderer instanceof MaskingRenderer) {
			return renderer;
		}
		return new MaskingRenderer(renderer);
	}

	/**
	 * {@link ResourceProvider} showing masked values as {@link MaskedValues#placeholder()}.
	 */
	private static final class MaskingResourceProvider implements ResourceProvider, Matcher {

		private final LabelProvider _labels;

		private final ResourceProvider _resources;

		MaskingResourceProvider(LabelProvider labels) {
			_labels = labels;
			_resources = LabelResourceProvider.toResourceProvider(labels);
		}

		@Override
		public String getLabel(Object object) {
			return DiscoverRight.isMasked(object) ? placeholder() : _resources.getLabel(object);
		}

		@Override
		public String getType(Object object) {
			return DiscoverRight.isMasked(object) ? null : _resources.getType(object);
		}

		@Override
		public String getTooltip(Object object) {
			return DiscoverRight.isMasked(object) ? null : _resources.getTooltip(object);
		}

		@Override
		public ThemeImage getImage(Object object, Flavor flavor) {
			return DiscoverRight.isMasked(object) ? null : _resources.getImage(object, flavor);
		}

		@Override
		public String getLink(DisplayContext context, Object object) {
			return DiscoverRight.isMasked(object) ? null : _resources.getLink(context, object);
		}

		@Override
		public String getCssClass(Object object) {
			return DiscoverRight.isMasked(object) ? null : _resources.getCssClass(object);
		}

		@Override
		public boolean match(String searchText, Object object) {
			return _labels instanceof Matcher matcher && !DiscoverRight.isMasked(object)
				&& matcher.match(searchText, object);
		}

	}

	/**
	 * {@link Comparator} placing masked values after all other values.
	 */
	private static final class MaskedLastComparator implements Comparator<Object> {

		private final Comparator<Object> _order;

		MaskedLastComparator(Comparator<Object> order) {
			_order = order;
		}

		@Override
		public int compare(Object o1, Object o2) {
			boolean masked1 = DiscoverRight.isMasked(o1);
			boolean masked2 = DiscoverRight.isMasked(o2);
			if (masked1 || masked2) {
				return Boolean.compare(masked1, masked2);
			}
			return _order.compare(o1, o2);
		}

	}

	/**
	 * {@link ContextMenuProvider} offering no menu for a masked value.
	 */
	private static final class MaskingContextMenuProvider implements ContextMenuProvider {

		private final ContextMenuProvider _contextMenu;

		MaskingContextMenuProvider(ContextMenuProvider contextMenu) {
			_contextMenu = contextMenu;
		}

		@Override
		public boolean hasContextMenu(Object obj) {
			return !DiscoverRight.isMasked(obj) && _contextMenu.hasContextMenu(obj);
		}

		@Override
		public Menu getContextMenu(Object directTarget, Object model) {
			if (DiscoverRight.isMasked(directTarget)) {
				return null;
			}
			return _contextMenu.getContextMenu(directTarget, model);
		}

	}

	/**
	 * {@link Renderer} writing {@link MaskedValues#placeholder()} for a masked value.
	 */
	private static final class MaskingRenderer implements Renderer<Object> {

		private final Renderer<Object> _renderer;

		MaskingRenderer(Renderer<Object> renderer) {
			_renderer = renderer;
		}

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			if (DiscoverRight.isMasked(value)) {
				out.writeText(placeholder());
			} else {
				_renderer.write(context, out, value);
			}
		}

	}

}
