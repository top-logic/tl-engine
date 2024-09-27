/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.css.CssUtil;

/**
 * {@link CellRenderer} that renders table rows with a row type image.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowTypeCellRenderer extends AbstractCellRenderer
		implements ConfiguredInstance<RowTypeCellRenderer.Config> {

	/**
	 * Configuration options for {@link RowTypeCellRenderer}.
	 */
	public interface Config extends PolymorphicConfiguration<RowTypeCellRenderer> {

		/**
		 * @see #getResourceProvider()
		 */
		String RESOURCE_PROVIDER_PROPERTY = "resourceProvider";

		/**
		 * @see #getContentRenderer()
		 */
		String CONTENT_RENDERER_PROPERTY = "contentRenderer";


		/**
		 * A {@link ResourceProvider} to use for the given {@link Cell}.
		 * 
		 * <p>
		 * <em>Important:</em> This {@link ResourceProvider} will be applied to the row object, not
		 * the {@link Accessor} value of the cell.
		 * </p>
		 * 
		 * <p>
		 * The same effect cannot be achieved with a simple {@link ResourceProvider} for the column,
		 * because this inner {@link ResourceProvider} is applied to the row object which gives
		 * context information from the row that may not be available from the cell value alone.
		 * This context information is necessary to write e.g. a type image on a "name" column
		 * (whose value is a string) to make it look like a "self" column (whose value is the row
		 * object).
		 * </p>
		 * 
		 * @see RowTypeCellRenderer#getResourceProvider()
		 */
		@Name(RESOURCE_PROVIDER_PROPERTY)
		@ItemDefault(MetaResourceProvider.class)
		PolymorphicConfiguration<? extends ResourceProvider> getResourceProvider();
		
		/**
		 * Setter for {@link #getResourceProvider()}.
		 */
		void setResourceProvider(PolymorphicConfiguration<? extends ResourceProvider> provider);

		/**
		 * A delegate cell renderer that writes the value part of the cell (after the type image).
		 */
		@Name(CONTENT_RENDERER_PROPERTY)
		@ItemDefault(DefaultCellRenderer.class)
		PolymorphicConfiguration<? extends CellRenderer> getContentRenderer();

		/**
		 * Setter for {@link #getContentRenderer()}.
		 */
		void setContentRenderer(PolymorphicConfiguration<? extends CellRenderer> contentRenderer);

    }
    
	/**
	 * @see Config#getResourceProvider()
	 */
	private ResourceProvider _resourceProvider;

	/**
	 * @see Config#getContentRenderer()
	 */
	private CellRenderer _contentRenderer;

	private Config _config;

	/**
	 * Creates a {@link RowTypeCellRenderer} from configuration.
	 */
	public RowTypeCellRenderer(InstantiationContext context, Config config) {
		_resourceProvider = context.getInstance(config.getResourceProvider());
		_contentRenderer = context.getInstance(config.getContentRenderer());
		_config = config;
	}

	/**
	 * Creates a {@link RowTypeCellRenderer}.
	 */
	protected RowTypeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer) {
		_resourceProvider = resourceProvider;
		_contentRenderer = contentRenderer;
	}

	/**
	 * @see Config#getResourceProvider()
	 */
	public final ResourceProvider getResourceProvider() {
		return _resourceProvider;
	}

	/**
	 * @see #getResourceProvider()
	 */
	public void setResourceProvider(ResourceProvider resourceProvider) {
		_resourceProvider = resourceProvider;
	}

	/**
	 * See {@link Config#getContentRenderer()}
	 */
	public CellRenderer getContentRenderer() {
		return _contentRenderer;
	}

	/**
	 * @see #getContentRenderer()
	 */
	public void setContentRenderer(CellRenderer contentRenderer) {
		_contentRenderer = contentRenderer;
	}

	/**
	 * Creates a {@link RowTypeCellRenderer}.
	 * 
	 * @param resourceProvider
	 *        The {@link ResourceProvider} to take the icon from.
	 */
	public RowTypeCellRenderer(Class<? extends ResourceProvider> resourceProvider) throws ConfigurationException {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, toConfig(resourceProvider));
	}

	private static Config toConfig(Class<? extends ResourceProvider> resourceProvider) throws ConfigurationException {
		Config config = TypedConfiguration.newConfigItem(Config.class);

		config.setResourceProvider(TypedConfiguration.createConfigItemForImplementationClass(resourceProvider));
		return config;
	}

	@Override
	public Config getConfig() {
		ensureConfig();
		return _config;
	}

	private void ensureConfig() {
		if (_config == null) {
			try {
				_config = createConfig();
			} catch (ConfigurationException ex) {
				Logger.error("Cannot syntesize configuation.", ex, RowTypeCellRenderer.class);
			}
		}
	}

	/**
	 * Creates a configuration for serialization, if this instance was created programatically.
	 * 
	 * @see RowTypeCellRenderer#RowTypeCellRenderer(ResourceProvider, CellRenderer)
	 */
	protected Config createConfig() throws ConfigurationException {
		Config result = (Config) TypedConfiguration.createConfigItemForImplementationClass(getClass());
		result.setResourceProvider(
			TypedConfiguration.createConfigItemForImplementationClass(_resourceProvider.getClass()));
		result.setContentRenderer(
			TypedConfiguration.createConfigItemForImplementationClass(_contentRenderer.getClass()));
		return result;
	}

	@Override
	public final void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		out.beginBeginTag(SPAN);
		writeCellAttributes(context, out, cell);
		out.endBeginTag();
		int currentDepth = out.getDepth();
		try {
			writeDecoration(context, out, cell);
			writeValue(context, out, cell);
		} catch (Throwable throwable) {
			try {
				out.endAll(currentDepth);
				RuntimeException renderingError = ExceptionUtil.createException(
					"Error occured during rendering of cell (Row " + cell.getRowIndex()
					+ ", Column '" + cell.getColumnName() + "') of table '"
					+ cell.getModel().getTableConfiguration().getTableName() + "'.",
					Collections.singletonList(throwable));

				cell.getView().produceErrorOutput(context, out, renderingError);
			} catch (Throwable inner) {
				// In the rare case of catastrophe better throw the original.
				throw throwable;
			}
		}
		
		out.endTag(SPAN);
	}

	/**
	 * Writes the HTML attribute for the {@link HTMLConstants#SPAN} tag of the cell.
	 * 
	 * @param context
	 *        Current interaction.
	 * @param out
	 *        The output stream
	 * @param cell
	 *        The written cell.
	 * @throws IOException
	 *         iff given {@link TagWriter} throws one.
	 */
	protected void writeCellAttributes(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		out.writeAttribute(CLASS_ATTR, "cDecoratedCell");
	}

	private void writeDecoration(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		out.beginBeginTag(SPAN);
		CssUtil.writeCombinedCssClasses(out,
			FormConstants.FIXED_LEFT_CSS_CLASS,
			getResourceProvider().getCssClass(cell.getRowObject()));
		out.endBeginTag();

		writeDecorationContent(context, out, cell);

		out.endTag(SPAN);
	}

	/**
	 * Writes the content of the fixed decoration box.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writeDecorationContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object rowObject = cell.getRowObject();
		ResKey tooltip = ResKey.text(getResourceProvider().getTooltip(rowObject));
		ThemeImage image = getResourceProvider().getImage(rowObject, Flavor.DEFAULT);

		if (image != null) {
			writeImageTag(context, out, rowObject, image, tooltip);
		}
	}

	private void writeValue(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
		out.endBeginTag();

		writeValueContent(context, out, cell);

		out.endTag(SPAN);
	}

	/**
	 * Writes the content of the flexible value box.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writeValueContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		_contentRenderer.writeCell(context, out, cell);
	}

	private void writeImageTag(DisplayContext context, TagWriter out, Object node, ThemeImage image, ResKey tooltip)
			throws IOException {
		XMLTag tag = image.toIcon();
		tag.beginBeginTag(context, out);
		out.writeAttribute(CLASS_ATTR, FormConstants.TREE_TYPE_IMAGE_CSS_CLASS);
		out.writeAttribute(ALT_ATTR, StringServices.nonNull(getResourceProvider().getLabel(node)));
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING); // Avoid popup for IE
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
		}
		tag.endEmptyTag(context, out);
	}

}
