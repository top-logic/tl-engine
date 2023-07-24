/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.LayoutControlAdapterRenderer;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControl} that dynamically sets CSS classes depending on the available width of its
 * content area.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MediaQueryControl extends LayoutControlAdapter {

	/**
	 * Default CSS class prefix to classify the available space into a size range.
	 * 
	 * <p>
	 * Depending on the available space, the {@link MediaQueryControl} adds CSS classes
	 * <code>dflColumns[X]</code> that can be used to influence the layout of its contents.
	 * </p>
	 * 
	 * <p>
	 * The dynamic class <code>dflColumns[X]</code> suggests using <code>X</code> column layout for
	 * the current document size. <code>X</code> usually ranges from <code>0</code> to
	 * <code>3</code>. Where <code>1</code> means that there is enough space of a whole column, but
	 * a value of <code>0</code> suggests to break contents even within a column if possible.
	 * </p>
	 */
	public static final String COLUMNS_CSS = "dflColumns";

	/**
	 * Default maximum columns.
	 * 
	 * @see Layout.Config#getMaxColumns()
	 */
	public static final int DEFAULT_MAX_COLUMNS = 3;

	/**
	 * {@link LayoutControlProvider} creating a {@link ContentControl} wrapped in a
	 * {@link MediaQueryControl}.
	 */
	public static class Layout extends DecoratingLayoutControlProvider<Layout.Config> {

		/** One instance of {@link Layout}. */
		public static final Layout INSTANCE;
		static {
			Config config = TypedConfiguration.newConfigItem(Layout.Config.class);
			INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		}

		/**
		 * Configuration options for {@link Layout}.
		 */
		public interface Config extends PolymorphicConfiguration<Layout> {

			/**
			 * The prefix of the dynamically added CSS class.
			 */
			@StringDefault(COLUMNS_CSS)
			String getCssPrefix();

			/**
			 * The maximum number of columns to use.
			 * 
			 * <p>
			 * This setting can be used to limit the maximum number of columns to a value smaller
			 * than 3, without explicitly specifying column widths using {@link #getSizes()}.
			 * </p>
			 */
			@IntDefault(DEFAULT_MAX_COLUMNS)
			int getMaxColumns();

			/**
			 * The minimum column sizes in ascending order.
			 * 
			 * <p>
			 * If the available content width is smaller than the value at position <code>X</code>,
			 * the CSS class <code>dflColumns[X]</code> is set.
			 * </p>
			 */
			@ListBinding
			List<Integer> getSizes();

			/**
			 * {@link LayoutControlProvider} for creating the component's content layout.
			 * 
			 * <p>
			 * Note: Since CSS rules work only within a single document, the content layout must be
			 * rendered inline.
			 * </p>
			 */
			@ItemDefault
			@ImplementationClassDefault(InlineLayoutControlProvider.class)
			PolymorphicConfiguration<? extends LayoutControlProvider> getContentLayout();

		}

		private final String _sizesConst;

		private final LayoutControlProvider _contentLayout;

		/**
		 * Creates a {@link Layout} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Layout(InstantiationContext context, Config config) {
			super(context, config);
			_sizesConst = jsArrayLiteral(config.getSizes());
			_contentLayout = context.getInstance(config.getContentLayout());
		}

		private String jsArrayLiteral(List<Integer> sizes) {
			if (sizes == null || sizes.isEmpty()) {
				return "null";
			} else {
				StringBuilder buffer = new StringBuilder();
				buffer.append('[');
				boolean first = true;
				for (Integer size : sizes) {
					if (first) {
						first = false;
					} else {
						buffer.append(',');
					}
					buffer.append(size.intValue());
				}
				buffer.append(']');
				return buffer.toString();
			}
		}

		@Override
		public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
			LayoutControl contentControl = _contentLayout.createLayoutControl(strategy, component);
			String css = getConfig().getCssPrefix();
			int maxColumns = getConfig().getMaxColumns();
			return new MediaQueryControl(contentControl, css, maxColumns, _sizesConst);
		}

	}

	/**
	 * Default {@link LayoutControlRenderer} for {@link MediaQueryControl}s.
	 */
	public static final class Renderer extends LayoutControlAdapterRenderer {

		/**
		 * Singleton {@link Renderer} instance.
		 */
		@SuppressWarnings("hiding")
		public static final Renderer INSTANCE = new Renderer();

		private Renderer() {
			// Singleton constructor.
		}

		@Override
		protected void writeControlTagAttributes(DisplayContext context, TagWriter out, LayoutControlAdapter control)
				throws IOException {
			super.writeControlTagAttributes(context, out, control);

			((MediaQueryControl) control).writeResizeFunction(context, out);
		}

		@Override
		public void appendControlCSSClasses(Appendable out, LayoutControlAdapter control) throws IOException {
			super.appendControlCSSClasses(out, control);
			out.append("cMediaQuery");
		}

	}

	private final String _cssPrefix;

	private final int _maxColumns;

	private final String _sizesConst;

	/**
	 * Creates a {@link MediaQueryControl} with default settings.
	 * 
	 * @param view
	 *        The actual content view.
	 */
	public MediaQueryControl(HTMLFragment view) {
		this(view, COLUMNS_CSS, DEFAULT_MAX_COLUMNS, "null");
	}

	/**
	 * Creates a {@link MediaQueryControl}.
	 * 
	 * @param view
	 *        The actual content view.
	 * @param cssPrefix
	 *        See {@link Layout.Config#getCssPrefix()}.
	 * @param maxColumns
	 *        See {@link Layout.Config#getMaxColumns()}.
	 * @param sizesConst
	 *        Optional minimum column sizes as JavaScript array literal, see
	 *        {@link Layout.Config#getSizes()}.
	 */
	public MediaQueryControl(HTMLFragment view, String cssPrefix, int maxColumns, String sizesConst) {
		super(view);
		_cssPrefix = cssPrefix;
		_maxColumns = maxColumns;
		_sizesConst = sizesConst;
	}

	/**
	 * Writes the resize function for this {@link MediaQueryControl}.
	 * 
	 * @param context
	 *        The current rendering context.
	 * @param out
	 *        The writer to write to.
	 */
	protected void writeResizeFunction(DisplayContext context, TagWriter out) throws IOException {
		LayoutControlRenderer.writeLayoutResizeFunction(out,
			"services.form.MediaQueryControl.onResize('" + getID() + "', '" + _cssPrefix + "', " + _maxColumns + ", "
				+ _sizesConst + ")");
	}

//	@Override
//	public Object visit(LayoutControlVisitor aVisitor, Object aParamObject) {
//		return aVisitor.visitMediaQueryControl(this, aParamObject);
//	}

	@Override
	protected ControlRenderer<? super LayoutControlAdapter> createDefaultRenderer() {
		return Renderer.INSTANCE;
	}

	@Override
	public MediaQueryControl self() {
		return this;
	}

}
