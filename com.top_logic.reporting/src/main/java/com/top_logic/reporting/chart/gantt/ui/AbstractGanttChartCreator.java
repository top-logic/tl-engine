/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.AllocationService;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.core.wrap.WrapperTLElement;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLClassifier;
import com.top_logic.reporting.chart.gantt.I18NConstants;
import com.top_logic.reporting.chart.gantt.component.GanttComponent;
import com.top_logic.reporting.chart.gantt.component.builder.GanttNodeBuilder;
import com.top_logic.reporting.chart.gantt.component.builder.GanttTreeBuilder;
import com.top_logic.reporting.chart.gantt.model.GanttChartConstants;
import com.top_logic.reporting.chart.gantt.model.GanttChartSettings;
import com.top_logic.reporting.chart.gantt.model.GanttEvent;
import com.top_logic.reporting.chart.gantt.model.GanttNode;
import com.top_logic.reporting.chart.gantt.model.GanttObject;
import com.top_logic.reporting.chart.gantt.model.GanttObject.Goto;
import com.top_logic.reporting.chart.gantt.model.GanttRow;
import com.top_logic.reporting.chart.gantt.model.TimeGranularity;
import com.top_logic.reporting.chart.gantt.ui.GanttChartConfig.ColorConfig;
import com.top_logic.reporting.chart.gantt.ui.GanttChartConfig.GridLineType;
import com.top_logic.reporting.chart.gantt.ui.GraphicsContext.ContentContext;
import com.top_logic.reporting.chart.gantt.ui.GraphicsContext.FooterContext;
import com.top_logic.reporting.chart.gantt.ui.GraphicsContext.HeaderContext;
import com.top_logic.tool.export.ExportUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Helper class to create the gantt chart.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class AbstractGanttChartCreator implements GanttChartConstants {

	/**
	 * Configuration options for {@link AbstractGanttChartCreator}
	 */
	public interface Config extends PolymorphicConfiguration<AbstractGanttChartCreator> {

		/**
		 * Display configuration.
		 */
		@ItemDefault
		GanttChartConfig getChartConfig();

	}

	/** Default color type name. */
	public static final String DEFAULT_COLOR_TYPE = "Default";

	/** Disabled color type name. */
	public static final String DISABLED_COLOR_TYPE = "Disabled";

	/**
	 * @see #getFont()
	 */
	private final Font _font;

	/**
	 * @see #fontHeight()
	 */
	private final int _fontHeight;

	/**
	 * @see #fontMetrics()
	 */
	private final FontMetrics _fontMetrics;

	private final GanttChartConfig _chartConfig;

	private final Graphics2D _dummyContext = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).createGraphics();

	/**
	 * The actual configuration options in use.
	 */
	public final GanttChartConfig getChartConfig() {
		return _chartConfig;
	}

	/**
	 * Creates a {@link AbstractGanttChartCreator}.
	 */
	public AbstractGanttChartCreator() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration.newConfigItem(Config.class));
	}

	/**
	 * Creates a {@link AbstractGanttChartCreator} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractGanttChartCreator(InstantiationContext context, Config config) {
		_chartConfig = createChartConfig(context, config);

		Font font = createDefaultFont();
		FontMetrics fontMetrics = fontMetrics(font);

		_font = font;
		_fontMetrics = fontMetrics;
		_fontHeight = fontMetrics.getHeight();
	}

	private GanttChartConfig createChartConfig(InstantiationContext context, Config config) {
		ConfigBuilder builder = createChartConfigBuilder(config);
		TypedConfiguration.applyFallback(config.getChartConfig(), builder, context);
		TypedConfiguration.applyFallback(getDefaultChartConfig(), builder, context);
		return (GanttChartConfig) builder.createConfig(context);
	}

	private ConfigBuilder createChartConfigBuilder(Config config) {
		return TypedConfiguration.createConfigBuilder(config.getChartConfig().descriptor());
	}

	private GanttChartConfig getDefaultChartConfig() {
		return ApplicationConfig.getInstance().getConfig(GanttChartConfig.class);
	}

	/**
	 * Creates the default font to use;
	 */
	protected Font createDefaultFont() {
		Theme theme = ThemeFactory.getTheme();
		String fontName = _chartConfig.getFont();
		int fontSize = _chartConfig.getFontSize();
		if (theme != null) {
			String configuredFamily = theme.getValue(Icons.TEXT_FAMILY);
			if (configuredFamily != null) {
				fontName = configuredFamily;
			}
			int configuredSize = theme.getValue(Icons.FONT_SIZE_IMAGE);
			if (configuredSize > 0) {
				fontSize = configuredSize;
			}
		}
		return new Font(fontName, Font.PLAIN, fontSize);
	}

	/** The default font to use. */
	public Font getFont() {
		return _font;
	}

	/**
	 * The {@link Font} to use for the given row.
	 *
	 * <p>
	 * If the row defines no specialized font, the default font is used.
	 * </p>
	 * 
	 * @see #getFont()
	 */
	protected Font getNodeFont(GanttRow row) {
		return (row.getNodeFont() != null) ? row.getNodeFont() : getFont();
	}

	/**
	 * The standard height of {@link #getFont()} obtained from {@link FontMetrics}.
	 *
	 * @see FontMetrics#getHeight()
	 * */
	public final int fontHeight() {
		return _fontHeight;
	}

	/**
	 * The {@link FontMetrics} of the default {@link #getFont()} for font size calculations.
	 *
	 * @see #fontMetrics(Font)
	 */
	public final FontMetrics fontMetrics() {
		return _fontMetrics;
	}

	/**
	 * Computes the {@link FontMetrics} of a given {@link Font}.
	 *
	 * @see #fontMetrics()
	 */
	public final FontMetrics fontMetrics(Font font) {
		return _dummyContext.getFontMetrics(font);
	}

	/**
	 * Gets the resource prefix for I18N.
	 */
	public final ResPrefix resPrefix() {
		ResPrefix result = _chartConfig.getResourcePrefix();
		if (result == null) {
			return I18NConstants.GENERATOR;
		}
		return result;
	}

	/**
	 * Gets the default line height.
	 */
	public int rowHeight() {
		return (int) (_chartConfig.getRowHeightFactor() * fontHeight());
	}

	/**
	 * Gets the height of a date line description row.
	 */
	public int dateRowHeight() {
		return _chartConfig.getVerticalTextSpace() + fontHeight();
	}

	/**
	 * Gets the height of a header calendar row in the chart.
	 */
	public int headerRowHeight() {
		return _chartConfig.getFrameSize() + 2 * _chartConfig.getVerticalTextSpace() + fontHeight() - 1;
	}

	/**
	 * Height of the footer in the chart.
	 */
	protected int footerHeight() {
		return 2 * _chartConfig.getFrameSize() + 2 * _chartConfig.getVerticalTextSpace() + 2 * dateRowHeight();
	}

	/**
	 * Gets the state of the given business object.
	 *
	 * @param businessObject
	 *        The business object to get the state from, must not be <code>null</code>.
	 * @return The beacon state of the businessObject (0 - 3) or -1, if no state is available
	 */
	protected abstract TLClassifier getStateFromBusinessObject(Object businessObject);

	/**
	 * Gets the object to get linked in the state column; may be <code>null</code> for no link.
	 *
	 * @param businessObject
	 *        The business object to get the state from, must not be <code>null</code>.
	 * @return The requested link.
	 */
	protected abstract Object getStateLinkObjectFromBusinessObject(Object businessObject);

	/**
	 * Gets the NodeBuilder to use to build the nodes.
	 */
	protected abstract GanttNodeBuilder getNodeBuilder();

	/**
	 * Maximum number of nodes to show per page.
	 */
	public int getMaxNodesPerPage(GanttChartCreatorFields settings) {
		return settings.getNodesPerPage();
	}

	/**
	 * Maximum page height.
	 */
	public int getMaxPageHeight(GanttChartSettings settings, int maxNodesPerPage) {
		return maxNodesPerPage * getNodeHeight(settings);
	}

	/**
	 * Gets the height of a node (line) in the chart.
	 */
	public int getNodeHeight(GanttChartSettings filterSettings) {
		return 2 * _chartConfig.getVerticalTextSpace() + rowHeight();
	}

	/**
	 * Gets the height of the header in the chart.
	 */
	public int getHeaderHeight(GanttChartSettings settings) {
		int headerHeight = 2 * headerRowHeight() + _chartConfig.getFrameSize();
		if (settings.getBoolean(PROPERTY_SHOW_DURATION_TO_MS)) {
			headerHeight += headerRowHeight(); // additional header row
		}
		return headerHeight;
	}

	/**
	 * Gets the height of the footer in the chart.
	 */
	public int getFooterHeight(GanttChartSettings filterSettings) {
		return footerHeight();
	}

	/**
	 * Hook for subclasses to use custom GanttChartCreatorFields.
	 */
	protected GanttChartCreatorFields createGanttChartCreatorFields() {
		return new GanttChartCreatorFields();
	}

	/**
	 * Creates the gantt chart image. Entry method for computing the chart.
	 */
	public GraphicData createGraphic(GanttChartSettings filterSettings, int windowWidth, int nodesPerPage) {
		try {
			if (filterSettings == null) {
				return drawEmptyGraphic(I18NConstants.ERROR_NO_DATA);
			}
			GanttChartCreatorFields cf = setupChartCreatorFields(filterSettings, windowWidth);
			cf.setNodesPerPage(nodesPerPage);
			return draw(cf);
		} catch (Exception e) {
			Logger.error("Error creating Gantt chart. ", e, GanttComponent.class);
			return drawEmptyGraphic(I18NConstants.ERROR_CREATION_FAILED);
		}
	}

	/**
	 * Creates and initializes the {@link GanttChartCreatorFields}.
	 */
	protected GanttChartCreatorFields setupChartCreatorFields(GanttChartSettings filterSettings, int windowWidth) {
		GanttChartCreatorFields cf = createGanttChartCreatorFields();
		cf.setFilterSettings(filterSettings);
		cf.setWindowWidth(windowWidth <= 0 ? _chartConfig.getDefaultImageWidth() : windowWidth);
		cf.setChartWidth(_chartConfig.getMaxImageWidth());
		cf.setTimeFrom(DateUtil.adjustToDayBegin(filterSettings.getDate(PROPERTY_START_DATE)));
		cf.setTimeTo(DateUtil.adjustToDayEnd(filterSettings.getDate(PROPERTY_END_DATE)));
		setupNodeList(cf);
		setupScalingAndWidth(cf);
		addCollisionAvoidingRows(cf);
		acceptChartWidth(cf);
		acceptChartHeight(cf);
		return cf;
	}

	/**
	 * Returns the width of the gantt chart.
	 */
	public int getImageWidth(GanttChartSettings filterSettings, int windowWidth) {
		return setupChartCreatorFields(filterSettings, windowWidth).getChartWidth();
	}

	/**
	 * Creates the gantt chart {@link ImageData} for the given page from the given
	 * {@link GraphicData}.
	 */
	public ImageData createPageImage(GraphicData graphicData, int pageNumber, int windowWidth, boolean showHeader,
			boolean showFooter, GanttChartSettings filterSettings) throws Exception {
		if (windowWidth <= 0)
			windowWidth = _chartConfig.getDefaultImageWidth();
		if (graphicData == null) {
			return drawEmptyImage(I18NConstants.ERROR_NO_DATA, windowWidth);
		}
		if (graphicData.hasMessage()) {
			return drawEmptyImage(graphicData.getMessageKey(), windowWidth);
		}
		if (pageNumber < 0 || pageNumber >= graphicData.getPageCount()) {
			return drawEmptyImage(I18NConstants.ERROR_PAGE_OUT_OF_RANGE, windowWidth);
		}

		int maxPageHeigth = getMaxPageHeight(filterSettings, graphicData.getNodesPerPage());
		int pageHeight = maxPageHeigth;
		if (pageNumber == graphicData.getPageCount() - 1) {
			pageHeight = graphicData.getContentHeight() - pageNumber * pageHeight;
		}
		int headerHeight = 0;
		if (showHeader) {
			headerHeight = graphicData.getHeaderHeight();
		}

		int footerHeight = 0;
		if (showFooter) {
			footerHeight = graphicData.getFooterHeight();
		}

		final int height = headerHeight + pageHeight + footerHeight;
		final int width = graphicData.getWidth();

		BufferedImage image = AllocationService.getInstance().allocateConditionally(
			getBufferSize(width, height),
			new Computation<BufferedImage>() {
				@Override
				public BufferedImage run() {
					return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				}
			});
		if (image == null) {
			return drawEmptyImage(I18NConstants.ERROR_MEMORY_OUT_OF_RANGE, windowWidth);
		}

		int pageTranslation = -(pageNumber == graphicData.getPageCount() - 1 ?
			graphicData.getContentHeight() - pageHeight : maxPageHeigth * pageNumber);

		Dimension dim = new Dimension(width, height);
		Graphics2D imgGraphics = image.createGraphics();
		imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		buildImage(graphicData.getContext(), imgGraphics, width, headerHeight, pageHeight, footerHeight, pageTranslation,
			showHeader, showFooter);
		// Collect ImageData
		ImageData imageData = new ImageData();
		imageData.setImage(image);
		imageData.setDimension(dim);
		imageData.setHeaderHeight(graphicData.getHeaderHeight());
		imageData.setTreeWidth(graphicData.getTreeWidth());

		// Create goto links and tooltips
		int shapeTranslation = pageTranslation + headerHeight;
		int dateDescTranslation = headerHeight + pageHeight;
		EntityCollection areaData = imageData.getAreaData();
		for (GanttObject node : graphicData.getLinks()) {
			if (node instanceof GanttEvent) {
				// date line
				Rectangle shape = new Rectangle(node.getXMin(), headerHeight, node.getWidth() + 1, pageHeight);
				areaData.add(newChartEntity(shape, node, imageData));
				// date line description
				GanttEvent line = (GanttEvent) node;
				shape = new Rectangle(line.getXminT(), line.getYminT(), line.xlengthT(), line.ylengthT());
				shape.translate(0, dateDescTranslation);
				areaData.add(newChartEntity(shape, node, imageData));
			}
			else {
				// for image map skip nodes outside of page
				int nodestartOnPage = node.getYMin() + pageTranslation;
				if (nodestartOnPage < 0 || nodestartOnPage > pageHeight) {
					continue;
				}
				Rectangle shape = new Rectangle(node.getXMin(), node.getYMin(), node.getWidth() + 1, node.getHeight() + 1);
				shape.translate(0, shapeTranslation);
				areaData.add(newChartEntity(shape, node, imageData));
			}
		}

		return imageData;
	}

	protected ChartEntity newChartEntity(Rectangle shape, GanttObject node, ImageData imageData) {
		String tooltip = node.getTooltip();
		Goto gotoArgument = node.getGoto();
		if (gotoArgument != null) {
			return new ChartEntity(shape, tooltip, imageData.getLinkPlaceholder(gotoArgument));
		} else {
			return new ChartEntity(shape, tooltip);
		}
	}

	/**
	 * Draws an page image for the given parameters.
	 */
	protected void buildImage(GraphicsContextImpl context, Graphics2D imgGraphics, int width, int headerHeight,
			int pageHeight, int footerHeight, int pageTranslation, boolean showHeader,
			boolean showFooter) {

		// draw header
		if (showHeader) {
			imgGraphics.setClip(0, 0, width, headerHeight);
			context.replayHeader(imgGraphics);
		}

		// draw content page
		imgGraphics.translate(0, headerHeight);
		imgGraphics.setClip(0, 0, width, pageHeight);
		imgGraphics.translate(0, pageTranslation);
		context.replayContent(imgGraphics);
		imgGraphics.translate(0, -pageTranslation);

		// draw footer
		if (showFooter) {
			imgGraphics.translate(0, pageHeight);
			imgGraphics.setClip(0, 0, width, footerHeight);
			context.replayFooter(imgGraphics);
		}
	}

	/**
	 * Creates a {@link GraphicData} with an error message.
	 *
	 * @param messageKey
	 *        the i18n key for the error message.
	 */
	public GraphicData drawEmptyGraphic(ResKey messageKey) {
		GraphicData graphicData = new GraphicData();
		graphicData.setMessageKey(messageKey);
		return graphicData;
	}

	/**
	 * Creates an image with an error message.
	 *
	 * @param messageKey
	 *        the i18n key for the error message.
	 * @param imageWidth
	 *        the width of the image
	 */
	public ImageData drawEmptyImage(ResKey messageKey, int imageWidth) throws Exception {
		// Calculate height and width
		FontMetrics fontMetrics = fontMetrics();
		int fontHeight = fontMetrics.getHeight();
		int rowHeight = (50 + fontHeight) / 2 - fontMetrics.getDescent();
		String message = Resources.getInstance().getString(messageKey);
		String[] messageLines = message.split("\n");
		int calculatedHeight = rowHeight * (messageLines.length + 1);
		int imageHeight = Math.max(_chartConfig.getMinErrorImageHeight(), calculatedHeight);

		// define size of image to draw in
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setFont(getFont());

		// draw background color of image
		graphics.setColor(Color.WHITE);
		Rectangle backgroundRect = new Rectangle(0, 0, imageWidth, imageHeight);
		graphics.fill(backgroundRect);
		graphics.setColor(Color.BLACK);

		// write messages
		int rowCount = 1;
		for (String messagePart : messageLines) {
			graphics.drawString(messagePart, 0, rowHeight * rowCount);
			rowCount++;
		}

		graphics.dispose();

		// define the size of output image
		Dimension dim = new Dimension(imageWidth, imageHeight);

		// Collect ImageData
		ImageData imgData = new ImageData();
		imgData.setImage(image);
		imgData.setDimension(dim);
		return imgData;
	}

	/**
	 * Starting method for drawing the gantt chart.
	 */
	protected GraphicData draw(GanttChartCreatorFields cf) {
		if (!cf.isScalingWidthAccepted()) {
			if (SCALING_OPTION_AUTO.equals(cf.scalingOption())) {
				return drawEmptyGraphic(I18NConstants.ERROR_WIDTH_OUT_OF_RANGE_AUTOMATIC);
			}
			else {
				return drawEmptyGraphic(I18NConstants.ERROR_WIDTH_OUT_OF_RANGE_MANUAL);
			}
		}
		else if (!cf.isScalingHeightAccepted()) {
			return drawEmptyGraphic(I18NConstants.ERROR_HEIGHT_OUT_OF_RANGE);
		}
		else {
			int pageCount = getPageCount(cf);
			if (pageCount < 1) {
				return drawEmptyGraphic(I18NConstants.ERROR_EMPTY_CHART);
			}
			GraphicData graphicData = new GraphicData();

			GraphicsContextImpl context = graphicData.getContext();

			init(context);

			drawHeader(context, cf);
			graphicData.setHeaderHeight(getHeaderHeight(cf.getFilterSettings()));
			graphicData.setTreeWidth(cf.getTreeWidth());

			drawContent(context, cf);
			graphicData.setContentHeight(getContentHeight(cf));

			drawFooter(context, cf);
			graphicData.setFooterHeight(getFooterHeight(cf.getFilterSettings()));

			graphicData.setWidth(cf.getChartWidth());
			graphicData.setPageCount(pageCount);
			graphicData.setNodesPerPage(cf.getNodesPerPage());
			graphicData.setLinks(getLinks(cf));
			return graphicData;
		}
	}

	/**
	 * Initializes the given graphics context.
	 */
	protected void init(GraphicsContextImpl context) {
		init(context.header());
		init(context.content());
		init(context.footer());
	}

	/**
	 * Initializes the given graphics.
	 */
	protected void init(Graphics2D graphics) {
		graphics.setFont(getFont());
		graphics.setColor(_chartConfig.getForegroundColor());
		graphics.setBackground(_chartConfig.getBackgroundColor());
	}

	/**
	 * Gets the clickable links of the chart.
	 */
	protected Collection<GanttObject> getLinks(GanttChartCreatorFields cf) {
		List<GanttObject> links = new ArrayList<>(cf.getEntities());
		for (GanttRow row : cf.getRows()) {
			links.add(row);
			for (GanttNode nodeData : row.getNodes()) {
				links.add(nodeData);
			}
		}
		links.addAll(cf.getDateLines());
		return links;
	}

	/**
	 * Gets the page count of the graphic.
	 */
	protected int getPageCount(GanttChartCreatorFields cf) {
		int contentHeight = getContentHeight(cf);
		int maxPageHeight = getMaxPageHeight(cf.getFilterSettings(), getMaxNodesPerPage(cf));
		int pageCount = contentHeight / maxPageHeight;
		if (contentHeight % maxPageHeight > 0)
			pageCount++;
		return pageCount;
	}

	/**
	 * Gets the height of the complete content (without header and footer).
	 */
	protected int getContentHeight(GanttChartCreatorFields cf) {
		return cf.getRows().size() * getNodeHeight(cf.getFilterSettings());
	}

	/**
	 * Gets the height of the complete graphic inclusive header and footer.
	 */
	protected int getChartHeight(GanttChartCreatorFields cf) {
		return getHeaderHeight(cf.getFilterSettings()) + getContentHeight(cf) + getFooterHeight(cf.getFilterSettings());
	}

	/**
	 * Draws the main background and border of the header.
	 */
	protected void drawHeaderBorder(HeaderContext context, GanttChartCreatorFields cf) {
		int frameSize = _chartConfig.getFrameSize();
		int headerHeight = getHeaderHeight(cf.getFilterSettings());
		context.header().setPaint(_chartConfig.getForegroundColor());
		context.header().fill(new Rectangle(0, 0, cf.getChartWidth(), headerHeight));
		context.header().setPaint(_chartConfig.getBackgroundColor());
		context.header().fill(
			new Rectangle(frameSize, frameSize, cf.getChartWidth() - 2 * frameSize, headerHeight - 2 * frameSize));
		context.header().setColor(_chartConfig.getForegroundColor());
		context.header().setBackground(_chartConfig.getBackgroundColor());
	}

	/**
	 * Draws the main background and border of the content.
	 */
	protected void drawContentBorder(ContentContext context, GanttChartCreatorFields cf) {
		int frameSize = _chartConfig.getFrameSize();
		int contentHeight = getContentHeight(cf);
		context.content().setPaint(_chartConfig.getForegroundColor());
		context.content().fill(new Rectangle(0, 0, cf.getChartWidth(), contentHeight));
		context.content().setPaint(_chartConfig.getBackgroundColor());
		context.content().fill(new Rectangle(frameSize, 0, cf.getChartWidth() - 2 * frameSize, contentHeight));
		context.content().setColor(_chartConfig.getForegroundColor());
		context.content().setBackground(_chartConfig.getBackgroundColor());
	}

	/**
	 * Draws the main background and border of the footer.
	 */
	protected void drawFooterBorder(FooterContext context, GanttChartCreatorFields cf) {
		int frameSize = _chartConfig.getFrameSize();
		int footerHeight = getFooterHeight(cf.getFilterSettings());
		context.footer().setPaint(_chartConfig.getForegroundColor());
		context.footer().fill(new Rectangle(0, 0, cf.getChartWidth(), footerHeight));
		context.footer().setPaint(_chartConfig.getBackgroundColor());
		context.footer().fill(
			new Rectangle(frameSize, frameSize, cf.getChartWidth() - 2 * frameSize, footerHeight - 2 * frameSize));
		context.footer().setColor(_chartConfig.getForegroundColor());
		context.footer().setBackground(_chartConfig.getBackgroundColor());
	}

	/**
	 * Draws the chart header (which will be repeated on top of all pages later).
	 */
	protected void drawHeader(HeaderContext context, GanttChartCreatorFields cf) {
		drawHeaderBorder(context, cf);
		drawTreeHeader(context, cf);
		drawCalendar(context, cf);
	}

	/**
	 * Draws the complete chart content (which will be parted into pages later).
	 */
	protected void drawContent(ContentContext context, GanttChartCreatorFields cf) {
		drawContentBorder(context, cf);
		cf.getBlockingInfo().addAll(drawTree(context, cf));
		drawSeparatorLines(context, cf);
		drawNodeDateRanges(context, cf);
		drawDateLines(context, cf);
		drawCurrentDateLine(context, cf);
		drawNodeDatas(context, cf);
		if (cf.showDependencies()) {
			drawDependencies(context, cf);
		}
		if (cf.showMilestoneIcons()) {
			// Draw dependencies first, then add milestone legend.
			// This is to make sure, the dependencies cannot overwrite the legend text.
			drawMilestonesLegend(context, cf);
		}
		if (cf.drawBlockingInfos()) {
			drawBlockingInfos(context, cf);
		}
	}

	/**
	 * Draws the vertical separator lines (grid lines).
	 */
	protected void drawSeparatorLines(ContentContext context, GanttChartCreatorFields cf) {
		GridLineType gridLineType = _chartConfig.getGridLineType();
		if (GridLineType.NONE != gridLineType) {
			context.content().setColor(_chartConfig.getPeriodSeparatorLineColor());
			context.content().setStroke(getStrokeForSeparatorLines());
			TimeGranularity granularity = GridLineType.INTERVAL == gridLineType ? cf.gettInterval() : cf.gettSubInterval();
			for (ColumnIterator column = new ColumnIterator(_chartConfig, cf, granularity); column.next();) {
				int stop = column.getStop();
				context.content().drawLine(stop, 0, stop, getContentHeight(cf));
			}
			context.content().setStroke(new BasicStroke());
		}
	}

	/**
	 * Gets the stroke for the separator lines.
	 */
	protected Stroke getStrokeForSeparatorLines() {
		return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1, 2 }, 0);
	}

	/**
	 * Draws the chart footer (which will be repeated on top of all pages later).
	 */
	protected void drawFooter(FooterContext context, GanttChartCreatorFields cf) {
		drawFooterBorder(context, cf);
		cf.getDateLineDescriptions().replay(context.footer());
	}

	/**
	 * Builds the list of nodes (business object lines) which shall be shown in the gantt charts.
	 */
	protected void setupNodeList(GanttChartCreatorFields cf) {
		cf.setRows(new ArrayList<>());
		cf.setDateLines(new TreeSet<>(new Comparator<GanttEvent>() {
			@Override
			public int compare(GanttEvent o1, GanttEvent o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		}));
		cf.setEntities(new ArrayList<>());
		cf.setBlockingInfo(new ArrayList<>());
		cf.setDateLineDescriptions(new InstructionGraphics2D());
		cf.getDateLineDescriptions().setColor(_chartConfig.getForegroundColor());

		DefaultMutableTLTreeNode treeRoot = createTreeModel(cf).getRoot();
		getNodeBuilder().build(cf, treeRoot);
		cf.setTargetDate(createTargetDate(cf));
	}

	/**
	 * Creates the tree model of the gantt chart.
	 */
	protected DefaultMutableTLTreeModel createTreeModel(GanttChartCreatorFields cf) {
		Object startElement = getRoot(cf);
		boolean showParentElements = cf.showParentElements();
		TreeBuilder<DefaultMutableTLTreeNode> treeBuilder = getTreeBuilder(cf);
		if (showParentElements) {
			Map<Object, Object> fixedChildMap = new HashMap<>();
			Object rootElement = fillFixedChildMap(fixedChildMap, startElement);
			cf.setParentElementCount(fixedChildMap.size());
			return new DefaultMutableTLTreeModel(new GanttTreeBuilder(treeBuilder, fixedChildMap), rootElement);
		}
		return new DefaultMutableTLTreeModel(treeBuilder, startElement);
	}

	/**
	 * Get the root element to be used for the gantt tree.
	 */
	protected Object getRoot(GanttChartCreatorFields cf) {
		return cf.getFilterSettings().getModel();
	}

	/**
	 * Gets the tree builder to build up the tree.
	 */
	protected abstract TreeBuilder<DefaultMutableTLTreeNode> getTreeBuilder(GanttChartCreatorFields cf);

	/**
	 * Fills a child map for additional parent elements of the given start element for the
	 * {@link GanttTreeBuilder} and returns the real root to show.
	 */
	protected Object fillFixedChildMap(Map<Object, Object> fixedChildMap, Object startElement) {
		if (startElement instanceof StructuredElement) {
			StructuredElement element = (StructuredElement) startElement;
			StructuredElement parent = element.getParent();
			while (parent != null && !parent.isRoot()) {
				fixedChildMap.put(parent, element);
				element = parent;
				parent = element.getParent();
			}
			return element;
		}
		return startElement;
	}

	/**
	 * Gets the target date for the {@link GanttChartConstants#PROPERTY_SHOW_DURATION_TO_MS} option.
	 */
	protected abstract Date createTargetDate(GanttChartCreatorFields cf);

	/**
	 * Sets ups the scaling, timeValues and width of the image.<br/>
	 * Must not be called before the nodes are computed in
	 * {@link #setupNodeList(GanttChartCreatorFields)} method.
	 */
	protected void setupScalingAndWidth(GanttChartCreatorFields cf) {
		cf.setTreeColumnWidth(computeTreeColumnWidth(cf));
		cf.setAdditionalColumnsWidth(computeAdditionalColumnsWidth(cf));
		cf.setTreeWidth(cf.getTreeColumnWidth() + cf.getAdditionalColumnsWidth());
		String scalingOption = cf.scalingOption();
		cf.setScalingWidthAccepted(true);
		cf.setScalingHeightAccepted(true);
		if (SCALING_OPTION_AUTO.equals(scalingOption) || SCALING_OPTION_WINDOW.equals(scalingOption)) {
			setScalingAutomatical(cf);
		} else {
			setScalingManual(cf);
		}
		acceptChartWidth(cf);
		acceptChartHeight(cf);
	}

	/**
	 * Checks whether the graphic width has not exceeded.
	 */
	protected void acceptChartWidth(GanttChartCreatorFields cf) {
		cf.setScalingWidthAccepted(cf.isScalingWidthAccepted()
			&& (cf.getChartWidth() <= _chartConfig.getMaxImageWidth()));
	}

	/**
	 * Checks whether the graphic height has not exceeded.
	 */
	protected void acceptChartHeight(GanttChartCreatorFields cf) {
		cf.setScalingHeightAccepted(cf.isScalingHeightAccepted()
			&& (getChartHeight(cf) <= _chartConfig.getMaxImageHeight()));
	}

	/**
	 * Sets the time values for automatic scaling.
	 */
	protected void setScalingAutomatical(GanttChartCreatorFields cf) {
		double timediffDays = getTimeDiffDays(cf);
		double timediffMonths = getTimeDiffMonths(timediffDays);
		cf.settInterval(TimeGranularity.YEAR);
		cf.settSubInterval(TimeGranularity.MONTH);
		cf.setChartWidth(cf.getWindowWidth());

		// compute space available for chart
		int chartSpace = cf.getChartWidth() - cf.getTreeWidth();

		// compute number of time columns that could be shown in available chart space
		double maxNumberTimeColumns = (double) chartSpace / _chartConfig.getMinColumnWidth();

		// check which scaling to use. Start with finest granularity!
		if (maxNumberTimeColumns > timediffDays) {
			cf.settSubInterval(TimeGranularity.DAY);
			cf.settInterval(TimeGranularity.MONTH);
		}
		else if (maxNumberTimeColumns > getTimeDiffWeeks(timediffDays)) {
			cf.settSubInterval(TimeGranularity.WEEK);
			cf.settInterval(TimeGranularity.MONTH);
		}
		else if (maxNumberTimeColumns > timediffMonths) {
			cf.settSubInterval(TimeGranularity.MONTH);
			cf.settInterval(TimeGranularity.YEAR);
		}
		else if (maxNumberTimeColumns > getTimeDiffQuarters(timediffMonths)) {
			cf.settSubInterval(TimeGranularity.QUARTER);
			cf.settInterval(TimeGranularity.YEAR);
		}
		else {
			if (SCALING_OPTION_AUTO.equals(cf.scalingOption())) {
				cf.settSubInterval(TimeGranularity.QUARTER);
				cf.settInterval(TimeGranularity.YEAR);
				setChartWidthManual(cf);
			}
			else {
				cf.setScalingWidthAccepted(false);
			}
		}
	}

	/**
	 * Sets the time values for manual scaling according to the filter settings.
	 */
	protected void setScalingManual(GanttChartCreatorFields cf) {
		// set scaling to use from filter
		cf.settSubInterval(cf.scalingGranularity());
		if (TimeGranularity.QUARTER.equals(cf.gettSubInterval()) || TimeGranularity.MONTH.equals(cf.gettSubInterval())) {
			cf.settInterval(TimeGranularity.YEAR);
		}
		else {
			cf.settInterval(TimeGranularity.MONTH);
		}
		setChartWidthManual(cf);
	}

	/**
	 * Sets the chart width for manual scaling.
	 */
	protected void setChartWidthManual(GanttChartCreatorFields cf) {
		int infoColumnsWidth = cf.getTreeWidth();
		cf.setChartWidth(getManualChartWidth(cf) + infoColumnsWidth);
	}

	/**
	 * Return the difference in years.
	 */
	protected double getTimeDiffYears(double timediffMonths) {
		return timediffMonths / 12.0;
	}

	/**
	 * Return the difference in quarters.
	 */
	protected double getTimeDiffQuarters(double timediffMonths) {
		return timediffMonths / 3.0;
	}

	/**
	 * Return the difference in month.
	 */
	protected double getTimeDiffMonths(double timediffDays) {
		return timediffDays / 30.0;
	}

	/**
	 * Return the difference in weeks.
	 */
	protected double getTimeDiffWeeks(double timediffDays) {
		return timediffDays / 7.0;
	}

	/**
	 * Return the difference in days.
	 */
	protected double getTimeDiffDays(GanttChartCreatorFields cf) {
		return (cf.getTimeTo().getTime() - cf.getTimeFrom().getTime()) / 1000.0 / 60.0 / 60.0 / 24.0;
	}

	/**
	 * Gets the memory size to allocate as buffer for images with the given size.
	 */
	protected long getBufferSize(int sizeX, int sizeY) {
		// Image size multiplied by 4 bytes for the alpha, red, green and blue channel.
		return 4L * sizeX * sizeY;
	}

	/**
	 * Computes the width of the gantt chart.
	 */
	protected int getManualChartWidth(GanttChartCreatorFields cf) {
		double timediffDays = getTimeDiffDays(cf);
		double timediffMonths = getTimeDiffMonths(timediffDays);
		int graphicsWidth = 0;
		int minColumnWidth = _chartConfig.getMinColumnWidth();
		switch (cf.gettSubInterval()) {
			case DAY:
				graphicsWidth = (int) (timediffDays * minColumnWidth);
				break;
			case WEEK:
				graphicsWidth = (int) (getTimeDiffWeeks(timediffDays) * minColumnWidth);
				break;
			case MONTH:
				graphicsWidth = (int) (timediffMonths * minColumnWidth);
				break;
			case QUARTER:
				graphicsWidth = (int) (getTimeDiffQuarters(timediffMonths) * minColumnWidth);
				break;
			case YEAR:
				graphicsWidth = (int) (getTimeDiffYears(timediffMonths) * minColumnWidth);
				break;
		}
		return graphicsWidth;
	}

	/**
	 * Computes the width of the tree column of the tree.
	 */
	protected int computeTreeColumnWidth(GanttChartCreatorFields cf) {
		int result = 0;
		Map<String, BufferedImage> imageCache = new HashMap<>();
		for (GanttRow row : cf.getRows()) {
			int xTreeStart = _chartConfig.getFrameSize() + _chartConfig.getHorizontalTextSpace();
			row.setXMin(xTreeStart + row.getDepth() * _chartConfig.getIndentWidthPerDepth());

			FontMetrics fontMetrics = fontMetrics(getNodeFont(row));
			int fontWidth = computeStringWidth(getTreeNodeName(row), fontMetrics);
			row.setXMax(row.getXMin() + fontWidth + _chartConfig.getHorizontalTextSpace() - 1);

			try {
				BufferedImage nodeImg = getImage(row, imageCache);
				if (nodeImg != null) {
					row.setXMax(row.getXMax() + (nodeImg.getWidth() + _chartConfig.getHorizontalTextSpace()));
				}
			} catch (IOException ex) {
				// ignore
			}

			if (row.getXMax() > result) {
				result = row.getXMax();
			}
		}
		result += _chartConfig.getVerticalTextSpace();
		result += _chartConfig.getFrameSize();

		return result;
	}

	/**
	 * Computes the length of the given string while considering line breaks.
	 */
	protected int computeStringWidth(String string, FontMetrics fontMetrics) {
		int maxSubstringLength = 0;

		// Split by "\n" and not by "\r\n". Assuming that "\r" has a width of "0"
		String[] lines = string.split(StringServices.LINE_BREAK);

		for (int i = 0; i < lines.length; i++) {
			int l = fontMetrics.stringWidth(lines[i]);
			if (l > maxSubstringLength) {
				maxSubstringLength = l;
			}
		}

		return maxSubstringLength;
	}

	/**
	 * Computes the width of the additional columns.
	 */
	protected int computeAdditionalColumnsWidth(GanttChartCreatorFields cf) {
		int result = 0;
		if (cf.showAdditionalColumns()) {
			for (String columnName : cf.additionalColumns()) {
				result += _chartConfig.getHorizontalTextSpace();
				result += getColumnWidth(cf, columnName);
				result += _chartConfig.getFrameSize();
			}
		}
		return result;
	}

	/**
	 * Gets the width of the additional column with the given name.
	 */
	protected int getColumnWidth(GanttChartCreatorFields cf, String columnName) {
		return Utils.getintValue(COLUMN_MAP.get(columnName));
	}


	/**
	 * Compute whether there are overlapping NodeDatas (milestones) and adds additional rows to
	 * avoid overlapping.
	 */
	protected void addCollisionAvoidingRows(GanttChartCreatorFields cf) {
		if (cf.addCollisionAvoidingRows()) {
			Map<String, Image> imageCache = new HashMap<>();

			// Dummy context for simulating and computing collisions
			GraphicsContextImpl context = new GraphicsContextImpl();
			init(context);

			// Call is required as exact positions of GanttRows will be computed and set as side
			// effect of this method.
			drawTree(context, cf);

			List<GanttRow> originalRows = cf.getRows();
			List<GanttRow> newRows = new ArrayList<>(originalRows.size());

			boolean showMilestoneIcons = cf.showMilestoneIcons();
			int num = 0;
			RowColorIndexer indexer = RowColorIndexer.newInstanceToogleRowGroups();
			for (GanttRow row : originalRows) {
				boolean legendDown = true;
				Map<Object, CollisionNodeGroup> nodeGroups = new LinkedHashMap<>();
				Color bgColor = getRowBackgroundColor(row, num, indexer);
				// Collect elements belonging together
				List<GanttNode> nodes = new ArrayList<>(row.getNodes());
				for (GanttNode node : nodes) {
					Set<BlockingInfo> blockingInfos = new HashSet<>();
					int xPos = getXPos(cf, node.getDate(), false);
					blockingInfos.addAll(node.draw(context, cf, this, xPos, imageCache));
					if (showMilestoneIcons && xPos >= 0&& !StringServices.isEmpty(node.getName())) {
						blockingInfos.addAll(drawMSLegendForDefaultSymbol(context, cf, row, node,
							legendDown, xPos, bgColor));
						legendDown = !legendDown;
					}
					addToNodeGroups(nodeGroups, node, blockingInfos);
				}

				checkForCollisionsAndAddRows(cf, newRows, row, nodeGroups);
				num++;
			}

			cf.setRows(newRows);
		}
	}

	/**
	 * Comparator for {@link CollisionNodeGroup}s which sorts more important {@link CollisionNodeGroup}s smaller than
	 * less important ones. Per default all nodes are equal important.
	 */
	protected Comparator<? super CollisionNodeGroup> getNodeImportanceComparator() {
		return CollisionNodeGroupImportanceComparator.INSTANCE;
	}

	/**
	 * Adds the given node with the given blocking infos to the given node group map.
	 */
	protected void addToNodeGroups(Map<Object, CollisionNodeGroup> nodeGroups, GanttNode node, Set<BlockingInfo> blockingInfos) {
		Object key = getCollisionGroupKey(node);
		CollisionNodeGroup nodeGroup = nodeGroups.get(key);
		if (nodeGroup == null) {
			nodeGroup = new CollisionNodeGroup(key);
			nodeGroups.put(key, nodeGroup);
		}
		nodeGroup.addNode(node);
		nodeGroup.addBlockingInfos(blockingInfos);
	}

	/**
	 * Gets a key representing a collision group for the given node. By default the key is the node
	 * itself. Subclasses may override this method to group nodes which belongs together and can be
	 * moved into another row only together.
	 */
	protected Object getCollisionGroupKey(GanttNode node) {
		return node;
	}

	/**
	 * Checks the given nodeGroups for overlapping and adds programmatic rows for the overlapping
	 * groups.
	 */
	protected void checkForCollisionsAndAddRows(GanttChartCreatorFields cf, List<GanttRow> newRows, GanttRow ganttRow, Map<Object, CollisionNodeGroup> nodeGroups) {
		List<List<CollisionNodeGroup>> rows = new ArrayList<>();
		List<CollisionNodeGroup> sortedNodeGroups = new ArrayList<>(nodeGroups.values());
		Collections.sort(sortedNodeGroups, getNodeImportanceComparator());
		
		// Find a matching row for all nodeGroups
		for (CollisionNodeGroup nodeGroup : sortedNodeGroups) {

			// Search in all existing rows
			boolean foundRow = false;
			for (List<CollisionNodeGroup> row : rows) {

				// Check all existing nodeGroups in the row for conflicts
				boolean conflict = false;
				for (CollisionNodeGroup otherGroup : row) {
					if (collide(nodeGroup, otherGroup)) {
						// found conflict in current row - check next row
						conflict = true;
						break;
					}
				}
				if (!conflict) {
					// found collision free row - finished for this nodeGroup
					foundRow = true;
					row.add(nodeGroup);
					break;
				}
			}

			if (!foundRow) {
				// no collision free row found for current nodeGroup
				if (rows.size() > getChartConfig().getMaxCollisionAvoidingRows()) {
					// maximum rows are reached - add remaining nodes to last row
					CollectionUtil.getLast(rows).add(nodeGroup);
				} else {
					// add new row
					List<CollisionNodeGroup> row = new ArrayList<>();
					row.add(nodeGroup);
					rows.add(row);
				}
			}
		}

		// resolve result
		if (rows.size() <= 1) {
			newRows.add(ganttRow);
		}
		else {
			GanttNodeBuilder nodeBuilder = getNodeBuilder();
			List<GanttNode> originalNodes = new ArrayList<>(ganttRow.getNodes());
			ganttRow.getNodes().clear();

			// Create additional rows for conflicting nodes
			for (int i = 0, length = rows.size(); i < length; i++) {
				List<CollisionNodeGroup> row = rows.get(i);
				GanttRow currentRow = (i == 0 ? ganttRow : nodeBuilder.createCollisionAvoidingNode(CollectionUtil.getLast(newRows)));
				List<GanttNode> newNodes = new ArrayList<>();
				for (CollisionNodeGroup nodeGroup : row) {
					Set<GanttNode> nodes = nodeGroup.getNodes();
					for (GanttNode node : nodes) {
						node.setRow(currentRow);
						newNodes.add(node);
					}
				}
				// Keep original node order
				List<GanttNode> currentNodes = new ArrayList<>(originalNodes);
				currentNodes.retainAll(newNodes);
				currentRow.getNodes().addAll(currentNodes);
				newRows.add(currentRow);
				if (i > 0) { // first row is the original row, therefore there was no move
					afterNodeMove(cf, ganttRow, currentRow, currentNodes);
				}
			}
		}
	}

	/**
	 * Hook for subclasses to react on node move to an additional new row.
	 */
	protected void afterNodeMove(GanttChartCreatorFields cf, GanttRow oldRow, GanttRow newRow, List<GanttNode> currentNodes) {
		// Nothing to do here
	}

	/**
	 * Checks whether the given node groups collides with each other.
	 */
	protected boolean collide(CollisionNodeGroup nodeGroup, CollisionNodeGroup otherGroup) {
		for (BlockingInfo info : nodeGroup.getBlockingInfos()) {
			for (BlockingInfo otherInfo : otherGroup.getBlockingInfos()) {
				if (overlaps(info, otherInfo)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Draws the header above the tree.
	 */
	protected void drawTreeHeader(HeaderContext context, GanttChartCreatorFields cf) {
		int frameSize = _chartConfig.getFrameSize();
		context.header().setColor(_chartConfig.getForegroundColor());
		context.header().fillRect(cf.getTreeColumnWidth() - frameSize, frameSize, frameSize,
			getHeaderHeight(cf.getFilterSettings()) - 2 * frameSize);
		drawAdditionalColumsHeader(context, cf);
	}

	/**
	 * Draws the header above the additional columns.
	 */
	protected void drawAdditionalColumsHeader(HeaderContext context, GanttChartCreatorFields cf) {
		if (cf.showAdditionalColumns()) {
			Resources resources = Resources.getInstance();
			int horizontalTextSpace = _chartConfig.getHorizontalTextSpace();
			int frameSize = _chartConfig.getFrameSize();
			int currentXPos = cf.getTreeColumnWidth();
			for (String columnName : cf.additionalColumns()) {
				int width = getColumnWidth(cf, columnName);
				drawColumnHeaderText(context, cf, currentXPos,
					resources.getString(RES_PREFIX_FOR_COLUMNS.key(columnName)), width);
				currentXPos += horizontalTextSpace + width + frameSize;
			}
			drawColumnBorders(context.header(), cf, getHeaderHeight(cf.getFilterSettings()));
		}
	}

	/**
	 * Draws the column header text
	 */
	protected void drawColumnHeaderText(HeaderContext context, GanttChartCreatorFields cf, int currentXPos,
			String columnHeader, int colWidth) {
		FontMetrics fontMetrics = fontMetrics();
		int headerWidth = fontMetrics.stringWidth(columnHeader);
		int xPosition = currentXPos + _chartConfig.getHorizontalTextSpace() + colWidth / 2 - headerWidth / 2;
		context.header().drawString(columnHeader, xPosition,
			getHeaderHeight(cf.getFilterSettings()) / 2 + fontMetrics.getHeight() / 4);
	}

	/**
	 * Draws the column borders of the additional columns to the tree.
	 */
	protected void drawColumnBorders(Graphics2D graphics, GanttChartCreatorFields cf, int lineHeight) {
		graphics.setColor(_chartConfig.getForegroundColor());
		int horizontalTextSpace = _chartConfig.getHorizontalTextSpace();
		int frameSize = _chartConfig.getFrameSize();
		int xPosition = cf.getTreeColumnWidth() - frameSize;
		for (String columnName : cf.additionalColumns()) {
			int width = getColumnWidth(cf, columnName);
			xPosition += frameSize + horizontalTextSpace + width;
			graphics.drawLine(xPosition, 0, xPosition, lineHeight);
		}
	}

	/**
	 * Draws the additional columns into the chart.
	 */
	protected void drawAdditionalColumns(ContentContext context, GanttChartCreatorFields cf) {
		if (cf.showAdditionalColumns()) {
			int horizontalTextSpace = _chartConfig.getHorizontalTextSpace();
			int frameSize = _chartConfig.getFrameSize();
			int currentXPos = cf.getTreeColumnWidth();
			for (String columnName : cf.additionalColumns()) {
				int width = getColumnWidth(cf, columnName);
				drawAdditionalColumn(context, cf, columnName, currentXPos, width);
				currentXPos += horizontalTextSpace + width + frameSize;
			}
			drawColumnBorders(context.content(), cf, getContentHeight(cf));
		}
	}

	/**
	 * Draws the additional column with the given name into the chart.
	 */
	protected void drawAdditionalColumn(ContentContext context, GanttChartCreatorFields cf, String columnName,
			int currentXPos, int width) {
		if (COLUMN_START_DATE.equals(columnName)) {
			drawStartEndDateColumn(context, cf, currentXPos, true, width);
		}
		else if (COLUMN_END_DATE.equals(columnName)) {
			drawStartEndDateColumn(context, cf, currentXPos, false, width);
		}
		else if (COLUMN_RESPONSIBLE.equals(columnName)) {
			drawResponsibleColumn(context, cf, currentXPos, width);
		}
		else if (COLUMN_STATE.equals(columnName)) {
			drawStateColumn(context, cf, currentXPos, width);
		}
	}

	/**
	 * The date format to show the date.
	 */
	protected DateFormat getDateFormat() {
		return HTMLFormatter.getInstance().getDateFormat();
	}

	/**
	 * Draws the start column or the end column of the tree.
	 */
	protected void drawStartEndDateColumn(ContentContext context, GanttChartCreatorFields cf, int xPos,
			boolean isStartDateColumn, int colWidth) {
		for (GanttRow row : cf.getRows()) {
			if (row.isProgrammatic()) {
				continue;
			}
			String dateString = getDateStringFromNode(row, isStartDateColumn);
			if (dateString != null) {
				Font font = getNodeFont(row);
				FontMetrics fontMetrics = fontMetrics(font);
				int dateStringWidth = fontMetrics.stringWidth(dateString);
				int xPosition = xPos + _chartConfig.getHorizontalTextSpace() + colWidth / 2 - dateStringWidth / 2;
				// draw start/end date
				context.content().setColor(getTreeTextColor(cf, row));
				context.content().setFont(font);
				context.content().drawString(dateString, xPosition, row.getYMax() - fontMetrics.getDescent());
				// create tooltips and goto links
				createDatesLinkAndTooltip(cf, row, dateStringWidth, xPosition, isStartDateColumn);
			}
		}
	}

	/**
	 * Gets the date string from the given business object.
	 *
	 * @param isStartDateColumn
	 *        Flag whether to get the start date (true) or the end date (false)
	 */
	protected String getDateStringFromNode(GanttRow row, boolean isStartDateColumn) {
		DateFormat dateFormat = getDateFormat();
		Date date = isStartDateColumn ? row.getStartDate() : row.getEndDate();
		return date != null ? dateFormat.format(date) : null;
	}

	/**
	 * Creates a DrawingData link for the given node for the image map.
	 */
	protected void createDatesLinkAndTooltip(GanttChartCreatorFields cf, GanttRow row, int shapeWidth, int xPosition,
			boolean isStartDateColumn) {
		GanttObject entity = new GanttObject(xPosition, row.getYMin(), xPosition + shapeWidth, row.getYMax());
		Object linkObject = getDateLinkObjectFromNode(row, isStartDateColumn);
		if (linkObject != null) {
			ResourceProvider resourceProvider = _chartConfig.getResourceProvider();
			entity.setName(resourceProvider.getLabel(linkObject));
			entity.setTooltip(resourceProvider.getTooltip(linkObject));
			entity.setGoto(resourceProvider, linkObject);
			cf.getEntities().add(entity);
		}
	}

	/**
	 * Gets the object to get linked in the start / end column; may be <code>null</code> for no
	 * link.
	 */
	protected abstract Object getDateLinkObjectFromNode(GanttRow row, boolean isStartDateColumn);

	/**
	 * Draws the responsible column of the tree.
	 */
	protected void drawResponsibleColumn(ContentContext context, GanttChartCreatorFields cf, int xPos, int colWidth) {
		for (GanttRow row : cf.getRows()) {
			if (row.isProgrammatic()) {
				continue;
			}
			Object responsible = getResponsibleFromBusinessObject(row.getBusinessObject());
			if (responsible != null) {
				// draw responsible
				String responsibleName = MetaLabelProvider.INSTANCE.getLabel(responsible);
				responsibleName = cutResponsibleName(responsibleName);
				Font font = getNodeFont(row);
				FontMetrics fontMetrics = fontMetrics(font);
				int responsibleWidth = fontMetrics.stringWidth(responsibleName);
				int xPosition = xPos + _chartConfig.getHorizontalTextSpace() + colWidth / 2 - responsibleWidth / 2;
				context.content().setColor(getTreeTextColor(cf, row));
				context.content().setFont(font);
				context.content().drawString(responsibleName, xPosition, row.getYMax() - fontMetrics.getDescent());
				// create tooltips and goto links
				createResponsibleLinkAndTooltip(cf, row, responsibleWidth, xPosition, responsible);
			}
		}
	}

	/**
	 * Creates a DrawingData link for the given node for the image map.
	 */
	protected void createResponsibleLinkAndTooltip(GanttChartCreatorFields cf, GanttRow row, int shapeWidth,
			int xPosition, Object responsible) {
		GanttObject entity = new GanttObject(xPosition, row.getYMin(), xPosition + shapeWidth, row.getYMax());
		ResourceProvider resourceProvider = _chartConfig.getResourceProvider();
		entity.setName(resourceProvider.getLabel(responsible));
		entity.setTooltip(resourceProvider.getTooltip(responsible));
		entity.setGoto(resourceProvider, responsible);
		cf.getEntities().add(entity);
	}

	/**
	 * Given a {@link String} this method cuts it to fit to responsible column.
	 */
	protected String cutResponsibleName(String responsibleName) {
		return ExportUtil.cutText(responsibleName, 1, 20, ProportionalCharSizeMap.INSTANCE);
	}

	/**
	 * Gets the responsible of the given business object.
	 */
	protected abstract Object getResponsibleFromBusinessObject(Object businessObject);

	/**
	 * Draws the state column of the tree.
	 */
	protected void drawStateColumn(ContentContext context, GanttChartCreatorFields cf, int xPos, int colWidth) {
		Map<String, Image> imageCache = new HashMap<>();
		for (GanttRow row : cf.getRows()) {
			if (row.isProgrammatic()) {
				continue;
			}
			String imagePath = getStateImagePathFromBusinessObject(row.getBusinessObject());
			if (imagePath != null) {
				Image image = imageCache.get(imagePath);
				if (image == null) {
					image = ThemeFactory.getTheme().getImageByPath(imagePath);
					imageCache.put(imagePath, image);
				}
				int width = image.getWidth(null);
				if (width <= 0)
					width = 16;
				int height = image.getHeight(null);
				if (height <= 0)
					height = 16;

				// draw state
				int x = xPos + colWidth / 2 - width / 2;
				int y = row.yMid() - height / 2;
				context.content().setColor(getTreeTextColor(cf, row));
				context.content().drawImage(image, x, y, null);

				// create tooltips and goto links
				createStateLinkAndTooltip(cf, row, x, y, x + width - 1, y + width - 1);
			}
		}
	}

	/**
	 * Gets the state image path of the given business object.
	 *
	 * @return the beacon state image path of the businessObject or <code>null</code>, if no state
	 *         is available
	 */
	protected String getStateImagePathFromBusinessObject(Object businessObject) {
		return getBeaconImageKey(getStateFromBusinessObject(businessObject));
	}

	/**
	 * Returns the URL to image for the given state fast list element.
	 */
	protected String getBeaconImageKey(TLClassifier state) {
		String key = "empty";
		if (state != null) {
			key = state.getName();
			key = key.substring(key.lastIndexOf('.') + 1);
		}
		return "/beacon/" + key + ".png";
	}

	/**
	 * Creates a DrawingData link for the given node for the image map.
	 */
	protected void createStateLinkAndTooltip(GanttChartCreatorFields cf, GanttRow row, int xmin, int ymin, int xmax,
			int ymax) {
		Object linkObject = getStateLinkObjectFromBusinessObject(row.getBusinessObject());
		if (linkObject != null) {
			GanttObject entity = new GanttObject(xmin, ymin, xmax, ymax);
			ResourceProvider resourceProvider = _chartConfig.getResourceProvider();
			entity.setName(resourceProvider.getLabel(linkObject));
			entity.setTooltip(resourceProvider.getTooltip(linkObject));
			entity.setGoto(resourceProvider, linkObject);
			cf.getEntities().add(entity);
		}
	}

	/**
	 * Draws the date ranges (process lines of the nodes) into the chart.
	 */
	protected void drawNodeDateRanges(ContentContext context, GanttChartCreatorFields cf) {
		if (cf.hideNodeDateRanges()) {
			return;
		}
		for (GanttRow row : cf.getRows()) {
			if (row.getStartDate() != null && row.getEndDate() != null) {
				int xmin = getXPos(cf, row.getStartDate(), true);
				if (xmin < cf.getTreeWidth()) {
					xmin = cf.getTreeWidth();
				}
				int xmax = getXPos(cf, row.getEndDate(), true);

				// Draw rectangle
				if (xmax > xmin) {
					int nodeDateRangeHeight = _chartConfig.getNodeDateRangeHeight();
					int halfNodeRangeHeight = nodeDateRangeHeight / 2;
					Rectangle rect = new Rectangle(xmin, row.yMid() - halfNodeRangeHeight, xmax - xmin, nodeDateRangeHeight);

					// Fill rectangle
					ColorConfig elementColorConfig = getElementColorConfig(cf, row);
					boolean disableFinishedElements = cf.disableFinishedElements();
					Color elementTypeStartColor = elementColorConfig != null ? elementColorConfig.getStartColor()
						: (disableFinishedElements && row.isDisabled() ? new Color(180, 180, 180) : new Color(51, 103, 153));
					Color elementTypeEndColor = elementColorConfig != null ? elementColorConfig.getEndColor()
						: (disableFinishedElements && row.isDisabled() ? new Color(230, 230, 230) : new Color(155, 196, 255));
					GradientPaint paint = new GradientPaint(xmin, row.yMid() - halfNodeRangeHeight,
						elementTypeStartColor, xmin, row.yMid() + halfNodeRangeHeight, elementTypeEndColor, false);
					context.content().setPaint(paint);
					context.content().fill(rect);

					// set border to rectangle
					Color borderColor = elementColorConfig != null ? elementColorConfig.getBorderColor()
						: (disableFinishedElements && row.isDisabled() ? new Color(180, 180, 180) : new Color(51, 103, 153));
					context.content().setColor(borderColor);
					context.content().drawLine(xmin, row.yMid() - halfNodeRangeHeight, xmax - 1,
						row.yMid() - halfNodeRangeHeight);
					context.content().drawLine(xmin, row.yMid() + halfNodeRangeHeight, xmax - 1,
						row.yMid() + halfNodeRangeHeight);
				}
			}
		}
		context.content().setPaint(_chartConfig.getForegroundColor());
	}

	/**
	 * Gets the {@link ColorConfig} for the given node.
	 */
	protected ColorConfig getElementColorConfig(GanttChartCreatorFields cf, GanttRow row) {
		ColorConfig colorConfig = null;
		Map<String, ColorConfig> colorMap = getChartConfig().getElementTypeColors();
		if (colorMap != null) {
			if (cf.disableFinishedElements() && row.isDisabled()) {
				colorConfig = colorMap.get(DISABLED_COLOR_TYPE);
			}
			else {
				String elementType = getElementType(row.getBusinessObject());
				colorConfig = colorMap.get(elementType);
				if (colorConfig == null) {
					colorConfig = colorMap.get(DEFAULT_COLOR_TYPE);
				}
			}
		}
		return colorConfig;
	}

	/**
	 * Gets the Element type of the given business object for color lookup.
	 */
	protected String getElementType(Object businessObject) {
		if (businessObject instanceof WrapperTLElement) {
			return ((WrapperTLElement) businessObject).getElementType();
		}
		else if (businessObject instanceof AbstractWrapper) {
			return ((AbstractWrapper) businessObject).tTable().getName();
		}
		return null;
	}

	/**
	 * Draws the tree part and the odd / even line background into the chart.
	 */
	protected Collection<BlockingInfo> drawTree(ContentContext context, GanttChartCreatorFields cf) {
		int frameSize = _chartConfig.getFrameSize();
		int horizontalTextSpace = _chartConfig.getHorizontalTextSpace();
		int verticalTextSpace = _chartConfig.getVerticalTextSpace();
		int xTreeStart = frameSize + horizontalTextSpace;
		int yTreeStart = 0;
		Map<String, BufferedImage> imageCache = new HashMap<>();

		int i = 0;
		RowColorIndexer indexer = RowColorIndexer.newInstanceToogleRowGroups();
		for (GanttRow row : cf.getRows()) {
			// Text dimensions
			Font font = getNodeFont(row);
			context.content().setFont(font);
			FontMetrics fontMetrics = fontMetrics(font);
			int fontWidth = computeStringWidth(getTreeNodeName(row), fontMetrics);

			yTreeStart += verticalTextSpace;
			row.setXMin(xTreeStart + row.getDepth() * _chartConfig.getIndentWidthPerDepth());
			row.setYMin(yTreeStart + (rowHeight() - fontHeight()) / 2 + 1);
			row.setXMax(row.getXMin() + fontWidth + horizontalTextSpace - 1);
			row.setYMax(row.getYMin() + fontHeight() - 1);

			// Draw odd/even background
			context.content().setPaint(getRowBackgroundColor(row, i, indexer));
			Rectangle rect =
				new Rectangle(frameSize, row.getYMin() - (rowHeight() - fontHeight()) / 2 - verticalTextSpace - 1,
					cf.getChartWidth() - 2 * frameSize, rowHeight() + 2 * verticalTextSpace);
			context.content().fill(rect);
			yTreeStart += verticalTextSpace + rowHeight();

			// Draw tree image
			int xOffset = 0;
			try {
				BufferedImage nodeImg = getImage(row, imageCache);
				if (nodeImg != null) {
					int height = nodeImg.getHeight();
					int width = nodeImg.getWidth();
					int yStart = (row.getYMax() + row.getYMin() - height) / 2;
					context.content().drawImage(nodeImg, row.getXMin(), yStart, null);
					row.setXMax(row.getXMax() + (width + horizontalTextSpace));
					xOffset = width + horizontalTextSpace;
				}

			} catch (IOException e) {
				// ignore
			}

			// Draw text
			drawTreeNode(context, cf, horizontalTextSpace, row, fontMetrics, xOffset);
			i++;
		}

		context.content().setColor(_chartConfig.getForegroundColor());

		// vertical line behind tree
		context.content().fillRect(cf.getTreeColumnWidth() - frameSize, 0, frameSize, getContentHeight(cf));

		drawAdditionalColumns(context, cf);

		// Block tree area
		return Collections.singleton(new BlockingInfo(0, 0, cf.getTreeWidth() - 1, getContentHeight(cf) - 1, null));
	}

	/**
	 * Draws the label for the given tree node.
	 */
	protected void drawTreeNode(ContentContext context, GanttChartCreatorFields cf, int horizontalTextSpace,
			GanttRow row, FontMetrics fontMetrics, int xOffset) {
		context.content().setColor(getTreeTextColor(cf, row));
		context.content().drawString(getTreeNodeName(row),
			row.getXMin() + xOffset + horizontalTextSpace,
			row.getYMax() - fontMetrics.getDescent() + 1); // Strings are drawn upwards from baseline -> ymax!
	}

	/**
	 * Gets the color to use to write text in tree table.
	 */
	protected Color getTreeTextColor(GanttChartCreatorFields cf, GanttRow row) {
		if (row.isNavOnly()) {
			return _chartConfig.getNavigationTextColor();
		}
		else if (row.isDisabled() && cf.disableFinishedElements()) {
			return _chartConfig.getDisabledTextColor();
		}
		else if (row.getNodeColor() != null) {
			return row.getNodeColor();
		}
		return _chartConfig.getForegroundColor();
	}

	/**
	 * Gets the row background color for the given row with the given index.
	 * 
	 * @param row
	 *        {@link GanttRow} for which the background color is to be caluclated
	 * @param rowNum
	 *        the row index of the given row
	 * @param colorIndexer
	 *        a {@link RowColorIndexer} to translate the given row index of the given row into the color
	 *        index to be used to caluclate the background color
	 * @return the calculated {@link Color} for the given row background
	 */
	protected Color getRowBackgroundColor(GanttRow row, int rowNum, RowColorIndexer colorIndexer) {
		int colorIndex = colorIndexer.getColorIndex(row, rowNum);
		return ((colorIndex % 2) == 0) ? _chartConfig.getEvenRowColor() : _chartConfig.getOddRowColor();
	}

	/**
	 * Gets the image for the given node.
	 *
	 * @return <code>null</code>if given node does not have an image path.
	 */
	protected BufferedImage getImage(GanttRow row, Map<String, BufferedImage> imageCache) throws IOException {
		ThemeImage icon = row.getNodeImagePath();
		if (icon == null) {
			return null;
		}
		icon = icon.resolve();
		if (!(icon instanceof Img)) {
			/* A "real" image is needed to create a BufferedImage. If the icon is a "css icon" no
			 * image can be created. */
			return null;
		}
		String key = ((Img) icon).getFileLink();
		BufferedImage image = imageCache.get(key);
		if (image == null) {
			try (InputStream in = FileManager.getInstance().getStream(key)) {
				image = ImageIO.read(in);
			}
			imageCache.put(key, image);
		}
		return image;
	}

	/**
	 * Gets the label of the given (tree) node.
	 */
	protected String getTreeNodeName(GanttRow row) {
		return row.getName();
	}

	/**
	 * Draws the calendar into the header.
	 */
	protected int drawCalendar(HeaderContext context, GanttChartCreatorFields cf) {
		int nextStart = drawCalendarRow(context, cf, cf.gettInterval(), _chartConfig.getFrameSize(), 0);
		if (cf.gettSubInterval() != null) {
			nextStart = drawCalendarRow(context, cf, cf.gettSubInterval(), nextStart, 1);
		}
		if (cf.showDurationToMS()) {
			nextStart = drawCalendarRow(context, cf, cf.gettSubInterval(), nextStart, 2);
		}
		return nextStart;
	}

	/**
	 * Draws a calendar row into the header.
	 *
	 * @param rowNumber
	 *        the number of the row to draw. the first row is 0 and that row determines
	 */
	protected int drawCalendarRow(HeaderContext context, GanttChartCreatorFields cf, TimeGranularity interval,
			int yPosition, int rowNumber) {
		// Symbols on left limiter
		context.header().setFont(getFont());
		Color foregroundColor = _chartConfig.getForegroundColor();
		context.header().setColor(foregroundColor);
		FontMetrics fontMetrics = fontMetrics();
		int fontHeight = fontHeight();
		int frameSize = _chartConfig.getFrameSize();

		// Gradient background
		int verticalTextSpace = _chartConfig.getVerticalTextSpace();
		GradientPaint paint =
			new GradientPaint(0, yPosition, Color.WHITE, 0, yPosition + fontHeight - 1 + 2
				* verticalTextSpace, Color.LIGHT_GRAY, false);
		context.header().setPaint(paint);
		Rectangle rect =
			new Rectangle(cf.getTreeWidth(), yPosition, cf.getChartWidth() - cf.getTreeWidth() - frameSize, fontHeight
				- 1 + 2 * verticalTextSpace);
		context.header().fill(rect);
		context.header().setPaint(foregroundColor);
		context.header().setColor(foregroundColor);

		yPosition += verticalTextSpace + fontHeight - 1;

		for (ColumnIterator column = new ColumnIterator(_chartConfig, cf, interval); column.next();) {
			int start = column.getStart();
			int stop = column.getStop();
			int width = stop - start;

			Date lastDate = column.getDate();

			// Text dimensions
			String periodText = getPeriodText(cf, lastDate, interval, rowNumber);
			int textWidth = fontMetrics.stringWidth(periodText);

			if (textWidth < width) {
				// draw text only if it fits into column
				context.header().drawString(periodText, start + width / 2 - textWidth / 2,
					yPosition - fontMetrics.getDescent());
			}
			context.header().setColor(_chartConfig.getPeriodSeparatorLineColor());
			if (rowNumber == 0) { // first row
				if (stop != cf.getChartWidth() - 1) {
					context.header().drawLine(stop, frameSize, stop,
						getHeaderHeight(cf.getFilterSettings()) - frameSize);
				}
			}
			else { // other rows
				if (stop != cf.getChartWidth() - 1) {
					context.header().drawLine(stop,
						yPosition - verticalTextSpace - fontHeight + 1,
						stop, yPosition + verticalTextSpace);
				}
			}
			context.header().setColor(foregroundColor);
		}

		yPosition += verticalTextSpace;

		context.header().drawLine(cf.getTreeWidth(), yPosition, cf.getChartWidth(), yPosition);

		return yPosition + frameSize;
	}

	public static class ColumnIterator {

		private final GanttChartConfig _chartConfig;

		private final GanttChartCreatorFields _cf;

		private final TimeGranularity _interval;

		private final int _frameSize;

		private boolean _first = true;

		private int _start;

		private int _stop;

		private Date _date;

		private Date _nextDate;

		/**
		 * Creates a new {@link ColumnIterator}.
		 */
		public ColumnIterator(GanttChartConfig config, GanttChartCreatorFields cf, TimeGranularity interval) {
			_chartConfig = config;
			_cf = cf;
			_interval = interval;

			_frameSize = _chartConfig.getFrameSize();

			_start = cf.getTreeWidth();
			_date = cf.getTimeFrom();

			_nextDate = calcNextPeriodBegin(_date, _interval);
			_stop = getXPos(cf, _nextDate, true, _frameSize);
		}

		public Date getDate() {
			return _date;
		}

		public boolean next() {
			if (!hasNext()) {
				return false;
			}

			if (_first) {
				_first = false;
			} else {
				inc();
			}

			return hasNext();
		}

		private boolean hasNext() {
			return _stop != -1;
		}

		private void inc() {
			_date = _nextDate;
			_start = _stop;

			if (_start == _cf.getChartWidth() - _frameSize) {
				_stop = -1;
			} else {
				_nextDate = calcNextPeriodBegin(_date, _interval);
				_stop = getXPos(_cf, _nextDate, true, _frameSize);
			}
		}

		public int getStop() {
			return _stop;
		}

		public int getStart() {
			return _start;
		}

		/**
		 * Computes the begin of the next date period.
		 */
		protected Date calcNextPeriodBegin(Date lastDate, TimeGranularity interval) {
			Calendar cal = new GregorianCalendar(TLContext.getLocale());
			cal.setTime(lastDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			switch (interval) {
				case DAY:
					cal.add(Calendar.DAY_OF_YEAR, 1);
					return cal.getTime();
				case WEEK:
					cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
					cal.add(Calendar.WEEK_OF_YEAR, 1);
					return cal.getTime();
				case MONTH:
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.add(Calendar.MONTH, 1);
					return cal.getTime();
				case QUARTER:
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
					cal.add(Calendar.MONTH, 3);
					return cal.getTime();
				case YEAR:
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.MONTH, 0);
					cal.add(Calendar.YEAR, 1);
					return cal.getTime();
			}
			return null;
		}

	}

	/**
	 * Gets the header text for the given date period.
	 */
	protected String getPeriodText(GanttChartCreatorFields cf, Date periodBegin, TimeGranularity interval, int rowNumber) {
		if (rowNumber == 2) {
			return getDurationToMSPeriodText(cf, periodBegin, interval, rowNumber);
		}
		else {
			return getDatePeriodText(cf, periodBegin, interval, rowNumber);
		}
	}

	/**
	 * Gets the header text for the given date period.
	 */
	protected String getDurationToMSPeriodText(GanttChartCreatorFields cf, Date periodBegin, TimeGranularity interval,
			int rowNumber) {
		Date targetDate = cf.getTargetDate();
		if (targetDate != null)
			switch (interval) {
				case DAY:
					int dayDiff = DateUtil.differenceInDays(targetDate, periodBegin);
					return (dayDiff > 0 ? "+" : "") + Integer.toString(dayDiff);
				case WEEK:
					int weekDiff = DateUtil.differenceInWeeks(targetDate, periodBegin);
					return (weekDiff > 0 ? "+" : "") + Integer.toString(weekDiff);
				case MONTH:
					int monthDiff = DateUtil.differenceInMonths(targetDate, periodBegin);
					return (monthDiff > 0 ? "+" : "") + Integer.toString(monthDiff);
				case QUARTER:
					int quarterDiff = DateUtil.differenceInQuarters(targetDate, periodBegin);
					return (quarterDiff > 0 ? "+" : "") + Integer.toString(quarterDiff);
				case YEAR:
					int yearDiff = DateUtil.differenceInYears(targetDate, periodBegin);
					return (yearDiff > 0 ? "+" : "") + Integer.toString(yearDiff);
			}
		return "";
	}

	/**
	 * Gets the header text for the given date period.
	 */
	protected String getDatePeriodText(GanttChartCreatorFields cf, Date periodBegin, TimeGranularity interval,
			int rowNumber) {
		Calendar cal = CalendarUtil.createCalendar(periodBegin);
		switch (interval) {
			case DAY:
				return Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
			case WEEK:
				return Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
			case MONTH:
				String string = Resources.getInstance().getString(resPrefix().append("month.").key(Integer.toString(cal.get(Calendar.MONTH))));
				return rowNumber > 0 ? string : (string + " " + cal.get(Calendar.YEAR));
			case QUARTER:
				int quarter = cal.get(Calendar.MONTH) / 3;
				return Resources.getInstance().getString(resPrefix().append("quarter.").key(String.valueOf(quarter)));
			case YEAR:
				return Integer.toString(cal.get(Calendar.YEAR));
		}
		return "";
	}

	/**
	 * Computes the x coordinate in the chart which is corresponding to the given date.
	 *
	 * @param date
	 *        the date to get the coordinate for
	 * @param cropToMax
	 *        if <code>true</code> and the date lies after chartWidth, chartWidth will be returned
	 *        instead of -1
	 * @return the x coordinate in the chart which is corresponding to the given date or -1 if the
	 *         date lies out of area.
	 */
	protected int getXPos(GanttChartCreatorFields cf, Date date, boolean cropToMax) {
		return getXPos(cf, date, cropToMax, _chartConfig.getFrameSize());
	}

	static int getXPos(GanttChartCreatorFields cf, Date date, boolean cropToMax, int frameSize) {
		long fullTimeDiff = cf.getTimeTo().getTime() - cf.getTimeFrom().getTime();
		long timeDiff = date.getTime() - cf.getTimeFrom().getTime();

		// Out of area?
		if (timeDiff < 0) {
			return -1;
		}
		if (timeDiff > fullTimeDiff) {
			return cropToMax ? cf.getChartWidth() - frameSize : -1;
		}
		int milestoneAreaWidth = cf.getChartWidth() - frameSize - cf.getTreeWidth();
		return (int) (cf.getTreeWidth() + milestoneAreaWidth * timeDiff / fullTimeDiff);
	}

	/**
	 * Draws vertical date lines (meetings) into the chart.
	 */
	protected void drawDateLines(ContentContext context, GanttChartCreatorFields cf) {
		context.content().setFont(getFont());
		DateLineDrawingState state = new DateLineDrawingState(fontMetrics(), cf.getTreeWidth());
		for (GanttEvent line : cf.getDateLines()) {
			drawDateLine(context, cf, line, state);
		}
	}

	/**
	 * Remember drawing positions during
	 * {@link AbstractGanttChartCreator#drawDateLines(ContentContext, GanttChartCreatorFields)}
	 */
	public static class DateLineDrawingState {

		/** Font metrics to use */
		private FontMetrics _metrics;

		/** Position X of the previous line */
		private int _lastXPos;

		/** Whether the previous line was the second line */
		private boolean _lastLineSecond = false;

		/** Create new {@link DateLineDrawingState} */
		public DateLineDrawingState(FontMetrics metrics, int lastXPos) {
			_metrics = metrics;
			_lastXPos = lastXPos;
		}

		public FontMetrics getMetrics() {
			return _metrics;
		}

		public void setMetrics(FontMetrics metrics) {
			_metrics = metrics;
		}

		public int getLastXPos() {
			return _lastXPos;
		}

		public void setLastXPos(int lastXPos) {
			_lastXPos = lastXPos;
		}

		public boolean isLastLineSecond() {
			return _lastLineSecond;
		}

		public void setLastLineSecond(boolean lastLineSecond) {
			_lastLineSecond = lastLineSecond;
		}
	}

	protected void drawDateLine(ContentContext context, GanttChartCreatorFields cf, GanttEvent line, DateLineDrawingState state) {
		int xPos = getXPos(cf, line.getDate(), false);
		if (xPos >= 0) {
			int textWidth = state.getMetrics().stringWidth(line.getName());

			// Line
			int width = line.getLineWidth() != 0 ? line.getLineWidth() : _chartConfig.getDateLineWidth();
			line.setXMin(xPos - width / 2);
			line.setXMax(line.getXMin() + width - 1);
			line.setYMin(0);
			line.setYMax(getContentHeight(cf));
			Rectangle rect = new Rectangle(line.getXMin(), line.getYMin(), width, line.getHeight() + 1);
			context.content().setColor(
				line.getLineColor() != null ? line.getLineColor() : _chartConfig.getForegroundColor());
			context.content().fill(rect);

			// Text
			int addY = 0;
			line.setXminT(xPos - textWidth / 2);
			if (line.getXminT() < cf.getTreeWidth()) {
				line.setXminT(cf.getTreeWidth());
			}
			if (line.getXminT() < state.getLastXPos() && !state.isLastLineSecond()) {
				// move to second row
				addY = dateRowHeight();
				state.setLastLineSecond(true);
			}
			else {
				state.setLastLineSecond(false);
			}
			line.setXmaxT(line.getXminT() + textWidth);
			line.setYminT(_chartConfig.getVerticalTextSpace() + addY);
			line.setYmaxT(line.getYminT() + dateRowHeight());
			cf.getDateLineDescriptions().drawString(line.getName(), line.getXminT(),
				line.getYmaxT() - state.getMetrics().getDescent());

			state.setLastXPos(line.getXmaxT());

			cf.getBlockingInfo().add(new BlockingInfo(line.getXMin(), line.getYMin(), line.getXMax(), line.getYMax(), false, true, line, line));
		}
	}

	/**
	 * Draws a line representing the current date.
	 */
	protected void drawCurrentDateLine(ContentContext context, GanttChartCreatorFields cf) {
		Date currentDate = new Date();
		int xPos = getXPos(cf, currentDate, false);
		if (xPos >= 0) {
			int width = _chartConfig.getDateLineWidth();
			int xmin = xPos - width / 2;
			int ymin = 0;
			int ymax = getContentHeight(cf);
			Rectangle rect = new Rectangle(xmin, ymin, width, ymax - ymin + 1);

			context.content().setColor(_chartConfig.getCurrentDateColor());
			context.content().fill(rect);
			cf.getBlockingInfo().add(new BlockingInfo(xmin, ymin, xmin + width, ymax, false, true, null, null));

			// Add tooltip to current date line
			String tooltip = getCurrentDateLineText(currentDate);
			if (!StringServices.isEmpty(tooltip)) {
				GanttObject entity = new GanttObject(xmin, ymin, xmin + width, ymax);
				entity.setTooltip(tooltip);
				cf.getEntities().add(entity);
			}
		}
	}

	/**
	 * Gets the tooltip text for the current date line. May be <code>null</code> for no tooltip.
	 */
	protected String getCurrentDateLineText(Date currentDate) {
		return Resources.getInstance().getMessage(resPrefix().key("today"),
			HTMLFormatter.getInstance().getDateFormat().format(currentDate));
	}

	/**
	 * Draws the NodeDatas (milestones) into the chart.
	 */
	protected void drawNodeDatas(ContentContext context, GanttChartCreatorFields cf) {
		Map<String, Image> imageCache = new HashMap<>();
		for (GanttRow row : cf.getRows()) {
			for (GanttNode nodeData : row.getNodes()) {
				int xPos = getXPos(cf, nodeData.getDate(), false);
				if (xPos >= 0) {
					cf.getBlockingInfo().addAll(nodeData.draw(context, cf, this, xPos, imageCache));
				}
			}
		}
	}

	/**
	 * Draws the (milestone) dependencies into the chart.
	 */
	protected void drawDependencies(ContentContext context, GanttChartCreatorFields cf) {
		for (GanttRow row : cf.getRows()) {
			for (GanttNode ms : row.getNodes()) {
				int msPos = getXPos(cf, ms.getDate(), false);
				if (msPos >= 0) {
					for (GanttNode depMS : ms.getNodeDependencies()) {
						cf.setPathCounter(0);
						drawPath(context, cf, ms, depMS, true);
					}
					for (GanttNode depMS : ms.getNodeDependencies()) {
						cf.setPathCounter(0);
						drawPath(context, cf, ms, depMS, false);
					}
					for (GanttEvent date : ms.getEventDependencies()) {
						cf.setPathCounter(0);
						drawPath(context, cf, ms, date);
					}
				}
			}
		}
	}

	/**
	 * For each milestone draw a legend. Interchange between up / down, that means text above or
	 * below milestone.
	 */
	protected void drawMilestonesLegend(ContentContext context, GanttChartCreatorFields cf) {
		int num = 0;
		RowColorIndexer indexer = RowColorIndexer.newInstanceToogleRowGroups();
		boolean addBlockingInfos = getChartConfig().getAddBlockingInfoToLabels();
		for (GanttRow row : cf.getRows()) {
			boolean legendDown = true;
			Color bgColor = getRowBackgroundColor(row, num, indexer);
			for (GanttNode node : row.getNodes()) {
				int xPos = getXPos(cf, node.getDate(), false);
				if (xPos >= 0 && !StringServices.isEmpty(node.getName())) {
					Collection<BlockingInfo> blockingInfos =
						drawMSLegendForDefaultSymbol(context, cf, row, node, legendDown, xPos,
							bgColor);
					if (addBlockingInfos) {
						cf.getBlockingInfo().addAll(blockingInfos);
					}
					legendDown = !legendDown;
				}
			}
			num++;
		}
	}

	/**
	 * Draw a filled rectangle surrounding the legend text in the given bgColor Then draw the legend
	 * text inside this rectangle This is to make sure the legend text overrides any dependencies or
	 * obstacles that might have been drawn before.
	 */
	protected Collection<BlockingInfo> drawMSLegendForDefaultSymbol(ContentContext context, GanttChartCreatorFields cf, GanttRow row,
			GanttNode node, boolean down, int xPos, Color legendBackground) {
		int legendFontSize = 10; // value found by try-out
		String label = node.getName();
		if (xPos >= 0 && !StringServices.isEmpty(label)) {
			context.content().setFont(getFont());
			int stringWidth = fontMetrics().stringWidth(label);
			int textPos = xPos - stringWidth / 2;
			if (textPos - 3 < cf.getTreeWidth()) {
				textPos = cf.getTreeWidth() + 3;
			}
			int textPosY = row.getYMin() - 1; // text-bottom on top of symbol
			if (down) {
				textPosY = row.getYMax() + legendFontSize; // text-top below bottom of symbol
			}

			// calculate a label bounding box from top to bottom of the label, start one pixel lower to not cover the icon
			int rectangleHeight = legendFontSize - 1;
			int rectangleTop = textPosY - rectangleHeight;
			if (!down) {
				rectangleTop -= 1;
			}
			// draw a bounding box in bg color behind the label, to cover dependency lines so they
			// will not cross the text
			context.content().setColor(legendBackground);
			context.content().fillRect(textPos, rectangleTop, stringWidth, rectangleHeight);
			context.content().setColor(Color.BLACK);
			context.content().drawString(label, textPos, textPosY);
			return Collections.singleton(new BlockingInfo(textPos, rectangleTop, textPos + stringWidth, rectangleTop + rectangleHeight, node));
		}
		return Collections.emptySet();
	}

	/**
	 * Draws the blocking information into the chart (for debug purposes).
	 */
	protected void drawBlockingInfos(ContentContext context, GanttChartCreatorFields cf) {
		for (BlockingInfo bi : cf.getBlockingInfo()) {
			boolean horizontal = bi.isHoriz();
			boolean vertical = bi.isVert();
			if (horizontal && vertical) {
				context.content().setColor(Color.MAGENTA);
			} else if (horizontal || vertical) {
				context.content().setColor(Color.CYAN);
			} else {
				context.content().setColor(Color.GREEN);
			}
			context.content().drawRect(bi.getX1(), bi.getY1(), bi.getWidth(), bi.getHeight());
		}
	}


	protected boolean drawPath(ContentContext context, GanttChartCreatorFields cf, GanttNode ms, GanttNode depMS, boolean startEndOnly) {
		// Draw start and end parts and enter them into blockingInfo
		int depMSPos = getXPos(cf, depMS.getDate(), false);
		if (depMSPos >= 0) {
			boolean dependencyConflict = DateUtil.compareDatesByDay(depMS.getDate(), ms.getDate()) >= 0;
			Color color =
				dependencyConflict ? _chartConfig.getDependencyColor() : _chartConfig.getDependencyConflictColor();
			context.content().setColor(color);

			if (startEndOnly) {
				drawStartLine(context, cf, ms, depMS);
				drawEndLine(context, cf, ms, depMS);
			}
			else {
				Tuple result = computeStartLine(cf, ms, depMS);
				GanttObject d1 = (GanttObject) result.get(0);
				GanttObject d2 = (GanttObject) computeEndLine(cf, ms, depMS, true).get(0);

				Direction lastDirection = Direction.RIGHT;
				if (Utils.getbooleanValue(result.get(2))) {
					lastDirection = ms.getYMin() > depMS.getYMin() ? Direction.UP : Direction.DOWN;
				}

				boolean success =
					findPath(context, cf, d1.getXMax(), d1.getYMax(), d2.getXMax(), d2.getYMax(), color, lastDirection,
						true, false, ms, depMS, false);
				if (!success) {
					Logger
						.info(
							"Failed to route from milestone '" + ms.getName() + "' to milestone '" + depMS.getName()
								+ "'.",
							AbstractGanttChartCreator.class);
					findPath(context, cf, d1.getXMax(), d1.getYMax(), d2.getXMax(), d2.getYMax(), color, lastDirection,
						true, false, ms, depMS, true);
				}
			}
		}
		return false;
	}

	protected Tuple computeStartLine(GanttChartCreatorFields cf, GanttNode ms, GanttNode depMS) {
		int x1, y1, x2, y2;
		int lineDim = fontHeight() / 2;

		x1 = ms.getXMax() + 2;
		y1 = (ms.getYMax() + ms.getYMin()) / 2;
		x2 = x1 + lineDim;
		y2 = y1;
		BlockingInfo b = new BlockingInfo(x1, y1, x2, y2, true, false, ms, null);
		CollisionInfo collide = collide(cf, b, cf.getBlockingInfo());
		boolean collision = collide != null && collide.hasCollision();
		if (collision) {
			x1 = (ms.getXMax() + ms.getXMin()) / 2 + 1;
			x2 = x1;
			if (ms.getYMin() > depMS.getYMin()) { // draw line upwards
				y1 = ms.getYMin() - 2;
				y2 = y1 - lineDim + 2;
			}
			else {
				y1 = ms.getYMax() + 2;
				y2 = y1 + lineDim - 2;
			}
			b = new BlockingInfo(x1, y1, x2, y2, false, true, ms, null);
		}
		return TupleFactory.newTuple(new GanttObject(x1, y1, x2, y2), b, Boolean.valueOf(collision));
	}

	protected Tuple computeEndLine(GanttChartCreatorFields cf, GanttNode ms, GanttNode depMS, boolean forDepLine) {
		int x1, y1, x2, y2;
		int lineDim = fontHeight() / 2;

		x1 = depMS.getXMin() - 2;
		y1 = depMS.yMid();
		x2 = x1 - lineDim;
		y2 = y1;
		BlockingInfo b = new BlockingInfo(x1, y1, x2, y2, true, false, null, depMS);
		CollisionInfo collide = collide(cf, b, cf.getBlockingInfo());
		boolean collision = collide != null && collide.hasCollision();
		if (x2 <= cf.getTreeWidth())
			collision = true;
		if (collision) {
			x1 = (depMS.getXMax() + depMS.getXMin()) / 2;
			x2 = x1;
			if (ms.getYMin() > depMS.getYMin()) { // draw line downwards
				y1 = depMS.getYMax() + 2;
				y2 = y1 + lineDim + 2;
			}
			else {
				y1 = depMS.getYMin() - 2;
				y2 = y1 - lineDim - 2;
			}
			b = new BlockingInfo(x1, y1, x2, y2, false, true, null, depMS);
		}

		if (forDepLine && !collision) {
			int offset =
				(depMS.getYMin() < ms.getYMin()) ? depMS.getYMax() - depMS.getYMin() + 2 : -depMS.getYMax()
					+ depMS.getYMin() - 2;
			x1 = depMS.getXMin() - lineDim - 2;
			y1 = depMS.yMid();
			x2 = x1;
			y2 = y1 + offset;
			b = new BlockingInfo(x1, y1, x2, y2, false, true, null, depMS);
		}

		return TupleFactory.newTuple(new GanttObject(x1, y1, x2, y2), b, Boolean.valueOf(collision));
	}

	protected void drawStartLine(ContentContext context, GanttChartCreatorFields cf, GanttNode ms, GanttNode depMS) {
		Tuple result = computeStartLine(cf, ms, depMS);
		GanttObject d = (GanttObject) result.get(0);

		// Start line at MS
		GeneralPath path = new GeneralPath();
		path.moveTo(d.getXMin(), d.getYMin());
		path.lineTo(d.getXMax(), d.getYMax());
		context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
		context.content().draw(path);
		cf.getBlockingInfo().add((BlockingInfo) result.get(1));
	}

	protected void drawEndLine(ContentContext context, GanttChartCreatorFields cf, GanttNode ms, GanttNode depMS) {
		Tuple result = computeEndLine(cf, ms, depMS, false);
		GanttObject d = (GanttObject) result.get(0);
		boolean collision = Utils.getbooleanValue(result.get(2));

		// End line at dep MS
		GeneralPath path = new GeneralPath();
		path.moveTo(d.getXMin(), d.getYMin());
		path.lineTo(d.getXMax(), d.getYMax());
		context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
		context.content().draw(path);
		cf.getBlockingInfo().add((BlockingInfo) result.get(1));

		// Arrow at dep MS
		path = new GeneralPath();
		int arrDim = fontHeight() / 6;
		if (collision) {
			if (ms.getYMin() > depMS.getYMin()) { // downwards
				path.moveTo(depMS.xMid(), depMS.getYMax());
				path.lineTo(depMS.xMid() - arrDim, depMS.getYMax() + arrDim);
				path.lineTo(depMS.xMid() + arrDim, depMS.getYMax() + arrDim);
			}
			else {
				path.moveTo(depMS.xMid(), depMS.getYMin());
				path.lineTo(depMS.xMid() - arrDim, depMS.getYMin() - arrDim);
				path.lineTo(depMS.xMid() + arrDim, depMS.getYMin() - arrDim);
			}
		}
		else {
			path.moveTo(depMS.getXMin(), depMS.yMid());
			path.lineTo(depMS.getXMin() - arrDim, depMS.yMid() - arrDim);
			path.lineTo(depMS.getXMin() - arrDim, depMS.yMid() + arrDim);
		}
		path.closePath();
		context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
		context.content().draw(path);
		context.content().fill(path);

		if (!collision) {
			int lineDim = fontHeight() / 2;
			int offset =
				(depMS.getYMin() < ms.getYMin()) ? depMS.getYMax() - depMS.getYMin() + 2 : -depMS.getYMax()
					+ depMS.getYMin() - 2;
			int x1 = depMS.getXMin() - lineDim - 2;
			int y1 = depMS.yMid();
			int x2 = x1;
			int y2 = y1 + offset;

			// Vertical line at dep MS
			path = new GeneralPath();
			path.moveTo(x1, y1);
			path.lineTo(x2, y2);
			context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
			context.content().draw(path);
			cf.getBlockingInfo().add(new BlockingInfo(x1, y1, x2, y2, false, true, null, depMS));
		}
	}

	protected boolean findPath(ContentContext context, GanttChartCreatorFields cf, int x1, int y1, int x2, int y2,
			Color color, Direction lastDir,
			boolean firstBit, boolean toVertLine, GanttObject source, GanttObject dest, boolean ignoreCollision) {
		cf.setPathCounter(cf.getPathCounter() + 1);

		// Setup possible directions
		Collection<Direction> allowedDirections =
			new ListBuilder<Direction>(4).add(Direction.DOWN).add(Direction.LEFT).add(Direction.UP)
				.add(Direction.RIGHT).toList();

		// Remove last direction except for first bit (may be prolonged)
		if (!firstBit) {
			allowedDirections.remove(lastDir);
		}

		// Remove backward moves
		switch (lastDir) {
			case LEFT:
				allowedDirections.remove(Direction.RIGHT);
				break;
			case RIGHT:
				allowedDirections.remove(Direction.LEFT);
				break;
			case UP:
				allowedDirections.remove(Direction.DOWN);
				break;
			case DOWN:
				allowedDirections.remove(Direction.UP);
				break;
		}

		if (!toVertLine) { // Otherwise allow up and down
			if (y1 < y2) {
				allowedDirections.remove(Direction.UP);
			}
			else {
				allowedDirections.remove(Direction.DOWN);
			}
		}

		if (x1 < x2) {
			allowedDirections.remove(Direction.LEFT);
		}
		else {
			allowedDirections.remove(Direction.RIGHT);
		}

		if (firstBit && !allowedDirections.contains(lastDir)) {
			allowedDirections.add(lastDir);
		}

		boolean foundJoin = false;
		for (Direction allowedDirection : allowedDirections) {
			// Go as far as possible into this direction
			int difference = 0;
			int sign = 1;
			boolean isX = true;
			switch (allowedDirection) {
				case LEFT:
					difference = x1 - x2;
					sign = -1;
					isX = true;
					break;
				case RIGHT:
					difference = x2 - x1;
					sign = 1;
					isX = true;
					break;
				case UP:
					difference = y1 - y2;
					sign = -1;
					isX = false;
					break;
				case DOWN:
					difference = y2 - y1;
					sign = 1;
					isX = false;
					break;
			}

			// Find max length by binary search
			int succ = -1;
			if (allowedDirection == lastDir && difference < 0) { // Nothing else found, try 'wrong'
																	// direction
				// Move into direction
				for (int len = 5; len < 50; len = len + 5) {
					int xnew = isX ? (x1 + sign * len) : x1;
					int ynew = isX ? y1 : (y1 + sign * len);
					BlockingInfo b = new BlockingInfo(x1, y1, xnew, ynew, isX, !isX, source, dest);
					CollisionInfo collide = collide(cf, b, cf.getBlockingInfo());
					if (ignoreCollision || collide == null || !collide.hasCollision()) {
						cf.getBlockingInfo().add(b);
						// Recurse until end point is met
						if (xnew == x2 && ynew == y2
							|| foundJoin
							|| // reached destination
							findPath(context, cf, xnew, ynew, x2, y2, color, allowedDirection, false, toVertLine,
								source, dest, ignoreCollision)) { // or can find path to destination
							// Draw line
							GeneralPath path = new GeneralPath();
							path.moveTo(x1, y1);
							path.lineTo(xnew, ynew);
							context.content().setStroke(
								new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
							context.content().draw(path);

							return true;
						}
						else {
							cf.getBlockingInfo().remove(b);
						}
					}
				}
			}
			else {
				BlockingInfo b = new BlockingInfo(x1, y1, isX ? (x1 + sign * difference) : x1, isX ? y1 : (y1 + sign * difference),
						isX, !isX, source, dest);
				CollisionInfo collide = collide(cf, b, cf.getBlockingInfo());
				if (ignoreCollision || collide == null || !collide.hasCollision()) {
					succ = difference;
				}
				else {
					int minCoord = 0;
					int maxCoord = difference;
					int lastCoord = difference;
					int currCoord = (minCoord + maxCoord) / 2;
					while (minCoord < maxCoord && lastCoord != currCoord) {
						b = new BlockingInfo(x1, y1, isX ? (x1 + sign * currCoord) : x1, isX ? y1 : (y1 + sign
								* currCoord), isX, !isX, source, dest);
						CollisionInfo collideB = collide(cf, b, cf.getBlockingInfo());
						if (ignoreCollision || collideB == null || !collide.hasCollision()) {
							succ = currCoord;
							minCoord = currCoord;
						}
						else {
							if (collideB.getJoin()) {
								foundJoin = true;
								break;
							}
							maxCoord = currCoord;
						}

						lastCoord = currCoord;
						currCoord = (minCoord + maxCoord) / 2;
					}
				}

				// Try all lengths down to 0
				for (int len = succ; len > 0; len = len - 5) {
					int xnew = isX ? (x1 + sign * len) : x1;
					int ynew = isX ? y1 : (y1 + sign * len);
					b = new BlockingInfo(x1, y1, xnew, ynew, isX, !isX, source, dest);
					cf.getBlockingInfo().add(b);
					// Recurse until end point is met
					if (xnew == x2 && ynew == y2
						|| foundJoin
						|| // reached destination
						findPath(context, cf, xnew, ynew, x2, y2, color, allowedDirection, false, toVertLine, source,
							dest, ignoreCollision)) { // or can find path to destination
						// Draw line
						GeneralPath path = new GeneralPath();
						path.moveTo(x1, y1);
						path.lineTo(xnew, ynew);
						context.content().setStroke(
							new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
						context.content().draw(path);
						return true;
					}
					else {
						cf.getBlockingInfo().remove(b);
					}
				}

			}
		}

		return false;
	}

	protected boolean drawPath(ContentContext context, GanttChartCreatorFields cf, GanttNode ms, GanttEvent depLine) {

		// Draw start part and enter it into blockingInfo

		// Draw start and end parts and enter them into blockingInfo
		int msPos = getXPos(cf, ms.getDate(), false);
		int depDatePos = getXPos(cf, depLine.getDate(), false);
		if (depDatePos >= 0) {
			int lineDim = fontHeight() / 2;
			boolean dependencyConflict = DateUtil.compareDatesByDay(depLine.getDate(), ms.getDate()) >= 0;
			Color color =
				dependencyConflict ? _chartConfig.getDependencyColor() : _chartConfig.getDependencyConflictColor();
			context.content().setColor(color);

			// Start line at MS
			GeneralPath path = new GeneralPath();
			int msY = (ms.getYMax() + ms.getYMin()) / 2;
			path.moveTo(ms.getXMax() + 2, msY); // Start at right MS
			path.lineTo(ms.getXMax() + 2 + lineDim, msY); // Draw a little line
			context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
			context.content().draw(path);
			cf.getBlockingInfo().add(new BlockingInfo(ms.getXMax() + 2, msY, ms.getXMax() + lineDim + 2, msY, true, false, ms, null));

			// Find free y area
			int i = 0;
			int depDateY = ms.getYMin();
			boolean conflict = false;
			int contentHeight = getContentHeight(cf);
			while (i < 100
				&& (conflict || Math.abs(ms.getYMin() - depDateY) < 60 || depDateY < 0 || depDateY >= contentHeight)) {
				depDateY = (int) (20 + Math.random() * (contentHeight - 50));
				CollisionInfo collide =
					collide(cf, new BlockingInfo(depLine.getXMin() - lineDim - 2, depDateY, depLine.getXMin() - 2,
						depDateY, true, false, null, null), cf.getBlockingInfo());
				conflict = collide != null && collide.hasCollision();
				int offset = (depDateY > msY) ? -lineDim : lineDim;
				if (!conflict) {
					collide =
						collide(cf, new BlockingInfo(depLine.getXMin() - lineDim - 2, depDateY, depLine.getXMin()
							- lineDim - 2, depDateY + offset, false, true, null, null), cf.getBlockingInfo());
					conflict = collide != null && collide.hasCollision();
				}
				i++;
			}

			// End line at dep date
			int arrDim = fontHeight() / 6;
			path = new GeneralPath();
			path.moveTo(depLine.getXMin() - lineDim - 2, depDateY); // Draw to left of dep date
			path.lineTo(depLine.getXMin() - 2, depDateY);
			context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
			context.content().draw(path);
			cf.getBlockingInfo().add(new BlockingInfo(depLine.getXMin() - lineDim - 2, depDateY,
				depLine.getXMin() - 2, depDateY, true, false, null, null));

			int offset = (depDatePos <= msPos) ? ((depDateY > msY) ? -lineDim : lineDim) : 0;
			if (offset != 0) {
				// Additionally block horizontal area on date line to avoid crossing into arrow
				path.moveTo(depLine.getXMin() - lineDim - 2, depDateY); // Draw to left of dep date
				path.lineTo(depLine.getXMin() - lineDim - 2, depDateY + offset);
				context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
				context.content().draw(path);
				cf.getBlockingInfo().add(new BlockingInfo(depLine.getXMin() - 2, depDateY,
					depLine.getXMin() - 2, depDateY + offset, false, true, null, depLine));
			}

			// Arrow
			path = new GeneralPath();
			path.moveTo(depLine.getXMin(), depDateY);
			path.lineTo(depLine.getXMin() - arrDim, depDateY - arrDim);
			path.lineTo(depLine.getXMin() - arrDim, depDateY + arrDim);
			path.closePath();
			context.content().setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0));
			context.content().draw(path);
			context.content().fill(path);

			boolean success =
				findPath(context, cf, ms.getXMax() + lineDim + 2, msY, depLine.getXMin() - lineDim - 2, depDateY
					+ offset,
					color, Direction.RIGHT, true, true, ms, depLine, false);

			if (!success) {
				Logger.error("Failed to route from MS " + ms.getName() + " to Date " + depLine.getName(), this);
			}
			// Find path between the 2 points
		}

		return false;
	}

	protected CollisionInfo collide(GanttChartCreatorFields cf, BlockingInfo b, Collection<BlockingInfo> bColl) {
		if (cf.getPathCounter() > 100) {
			return null;
		}

		for (BlockingInfo b2 : bColl) {
			CollisionInfo collisionInfo = collide(b, b2);
			if (collisionInfo.hasCollision() || collisionInfo.getJoin()) {
				return collisionInfo;
			}
		}

		return null;
	}

	protected CollisionInfo collide(BlockingInfo b1, BlockingInfo b2) {
		IntersectInfo intersectionInfo = intersect(b1, b2);
		CollisionInfo collisionInfo = new CollisionInfo();
		collisionInfo.setBlockingInfo(b2);
		collisionInfo.setIntersectionInfo(intersectionInfo);
		switch (intersectionInfo) {
			case YES:
				collisionInfo.setCollision(true);
				break;
			case NO:
			case CROSS:
				break;
			case TOUCH_X:
			case TOUCH_Y:
			case CONNECT_ORTH:
				collisionInfo.setCollision(false /** !(b1.source == b2.source || b1.dest == b2.dest) */
				);
				break;
			case X_JOIN:
			case Y_JOIN:
				collisionInfo.setCollision(false);
				/** !(b1.source == b2.source || b1.dest == b2.dest); */
				collisionInfo.setJoin(b1.getDest() == b2.getDest());
				break;
		}

		return collisionInfo;
	}

	protected IntersectInfo intersect(BlockingInfo b1, BlockingInfo b2) {
		if (b1.getX2() < b2.getX1() - 1 || b1.getX1() > b2.getX2() + 1) { // No x overlap
			return IntersectInfo.NO;
		}

		if (b1.getY2() < b2.getY1() - 1 || b1.getY1() > b2.getY2() + 1) { // No y overlap
			return IntersectInfo.NO;
		}

		if (b1.isHoriz() && b2.isHoriz() && b1.isVert() && b2.isVert()) { // Block overlap or touch
			return IntersectInfo.YES;
		}

		if (b1.isHoriz() && !b1.isVert() && b2.isHoriz() && !b2.isVert()) { // Both horizontal lines
			if (b1.getY1() == b2.getY1() && b1.getY2() == b2.getY2()
				&& (b1.getX2() >= b2.getX1() && b1.getX1() <= b2.getX2())) { // Same vertical area
																				// -> X_JOIN
				return IntersectInfo.X_JOIN;
			}
			return IntersectInfo.YES;
		}

		if (!b1.isHoriz() && b1.isVert() && !b2.isHoriz() && b2.isVert()) { // Both vertical lines
			if (b1.getX1() == b2.getX1() && b1.getX2() == b2.getX2()
				&& (b1.getY2() >= b2.getY1() && b1.getY1() <= b2.getY2())) { // Same horizontal area
																				// -> Y_JOIN
				return IntersectInfo.Y_JOIN;
			}
			return IntersectInfo.YES;
		}

		if (b1.isHoriz() && !b1.isVert() && b2.isVert() && !b2.isHoriz()) { // b1 horizontal and b2
																			// vertical
			int b2W = b2.getX2() - b2.getX1() + 1;
			if ((b2.getX1() >= b1.getX2() - b2W) && (b2.getX1() <= b1.getX2())
				&& (b1.getY2() == b2.getY2())
				&& (b1.getX1() <= b1.getX2() - b2W) // right lower corner connect in x range
				|| (b2.getY2() >= b1.getY2() - b2W) && (b2.getY2() <= b1.getY2()) && (b1.getX2() == b2.getX2())
				&& (b1.getX1() <= b1.getX2() - b2W)) { // right lower corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b2.getX1() >= b1.getX2() - b2W) && (b2.getX1() <= b1.getX2())
				&& (b1.getY1() == b2.getY1())
				&& (b1.getX1() <= b1.getX2() - b2W) // right upper corner connect in x range
				|| (b2.getY1() <= b1.getY1() + b2W) && (b2.getY1() >= b1.getY1()) && (b1.getX2() == b2.getX2())
				&& (b1.getX1() <= b1.getX2() - b2W)) { // right upper corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b2.getX1() >= b1.getX1() - b2W) && (b2.getX1() <= b1.getX1())
				&& (b2.getY2() == b1.getY2())
				&& (b1.getX1() <= b1.getX2() - b2W) // left lower corner connect in x range
				|| (b2.getY2() >= b1.getY2() - b2W) && (b2.getY2() <= b1.getY2()) && (b2.getX1() == b1.getX1())
				&& (b1.getX1() <= b1.getX2() - b2W)) { // left lower corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b2.getX1() >= b1.getX1() - b2W) && (b2.getX1() <= b1.getX1())
				&& (b2.getY1() == b1.getY1())
				&& (b1.getX1() <= b1.getX2() - b2W) // left upper corner connect in x range
				|| (b2.getY1() >= b1.getY1() - b2W) && (b2.getY1() <= b1.getY1()) && (b2.getX1() == b1.getX1())
				&& (b1.getX1() <= b1.getX2() - b2W)) { // left upper corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}
		}

		if (!b1.isHoriz() && b1.isVert() && !b2.isVert() && b2.isHoriz()) { // b1 vertical and b2
																			// horizontal
			int b1W = b1.getX2() - b1.getX1() + 1;
			if ((b1.getX1() >= b2.getX1() - b1W) && (b1.getX1() <= b2.getX1())
				&& (b1.getY2() == b2.getY2())
				&& (b2.getX1() <= b2.getX2() - b1W) // left lower corner connect in x range
				|| (b1.getY2() >= b2.getY2() - b1W) && (b1.getY2() <= b2.getY2()) && (b1.getX1() == b2.getX1())
				&& (b2.getX1() <= b2.getX2() - b1W)) { // left lower corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b1.getX1() >= b2.getX1() - b1W) && (b1.getX1() <= b2.getX1())
				&& (b1.getY1() == b2.getY1())
				&& (b2.getX1() <= b2.getX2() - b1W) // left upper corner connect in x range
				|| (b1.getY1() >= b2.getY1() - b1W) && (b1.getY1() <= b2.getY1()) && (b1.getX1() == b2.getX1())
				&& (b2.getX1() <= b2.getX2() - b1W)) { // left upper corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b1.getX1() >= b2.getX2() - b1W) && (b1.getX1() <= b2.getX2())
				&& (b2.getY2() == b1.getY2())
				&& (b2.getX1() <= b2.getX2() - b1W) // right lower corner connect in x range
				|| (b1.getY2() >= b2.getY2() - b1W) && (b1.getY2() <= b2.getY2()) && (b2.getX2() == b1.getX2())
				&& (b2.getX1() <= b2.getX2() - b1W)) { // right lower corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}

			if ((b1.getX1() >= b2.getX2() - b1W) && (b1.getX1() <= b2.getX2())
				&& (b2.getY1() == b1.getY1())
				&& (b2.getX1() <= b2.getX2() - b1W) // right upper corner connect in x range
				|| (b1.getY1() <= b2.getY1() + b1W) && (b1.getY1() >= b2.getY1()) && (b2.getX2() == b1.getX2())
				&& (b2.getX1() <= b2.getX2() - b1W)) { // right upper corner connect in y range
				return IntersectInfo.CONNECT_ORTH;
			}
		}

		// Vertical and horizontal lines
		if (!b1.isHoriz() && b1.isVert() && b2.isHoriz() && !b2.isVert()) {
			int b1Height = b1.getX2() - b1.getX1() + 1;

			// Overlap at more than line width -> CROSS
			if ((b1.getX2() < b2.getX2() - b1Height) || (b1.getX1() > b2.getX1() + b1Height)) {
				return IntersectInfo.CROSS;
			}

			if (((b1.getX1() >= b2.getX1() && b1.getX1() <= b2.getX2() + 1) || (b1.getX2() <= b2.getX2() && b1.getX2() >= b2
				.getX1() - 1)) // Horizontal range match
				&& (b1.getY1() < b2.getY1() && b1.getY2() > b2.getY2())) { // Vertical match
				return IntersectInfo.TOUCH_Y;
			}
		}

		// Horizontal and vertical lines
		if (b1.isHoriz() && !b1.isVert() && !b2.isHoriz() && b2.isVert()) {
			int b1Width = b1.getY2() - b1.getY1() + 1;

			// Overlap at more than line width -> CROSS
			if ((b1.getY2() < b2.getY2() - b1Width) || (b1.getY1() > b2.getY1() + b1Width)) {
				return IntersectInfo.CROSS;
			}

			if (((b2.getX1() >= b1.getX1() && b2.getX1() <= b1.getX2() + 1) || (b2.getX2() <= b1.getX2() && b2.getX2() >= b1
				.getX1() - 1)) // Horizontal range match
				&& (b2.getY1() < b1.getY1() && b2.getY2() > b1.getY2())) { // Vertical match
				return IntersectInfo.TOUCH_X;
			}
		}

		if (b1.isHoriz() && !b1.isVert() && b2.isVert()) { // Horizontal line and vertical line or
															// block
			if (Math.abs(b1.getX2() - b2.getX1()) <= 1 || Math.abs(b1.getX1() - b2.getX2()) <= 1) {
				return IntersectInfo.TOUCH_X; // NOTE: maybe we should test for full y overlap?
			}
		}

		if (!b1.isHoriz() && b1.isVert() && b2.isHoriz()) { // Vertical line and horizontal line or
															// block
			if (Math.abs(b1.getY2() - b2.getY1()) <= 1 || Math.abs(b1.getY1() - b2.getY2()) <= 1) {
				return IntersectInfo.TOUCH_Y; // NOTE: maybe we should test for full y overlap?
			}
		}

		if (b2.isHoriz() && !b2.isVert() && b1.isVert()) { // Vertical line or block and horizontal
															// line
			if (Math.abs(b2.getX2() - b1.getX1()) <= 1 || Math.abs(b2.getX1() - b1.getX2()) <= 1) {
				return IntersectInfo.TOUCH_X; // NOTE: maybe we should test for full y overlap?
			}
		}

		if (!b2.isHoriz() && b2.isVert() && b1.isHoriz()) { // Horizontal line or block and vertical
															// line
			if (Math.abs(b2.getY2() - b1.getY1()) <= 1 || Math.abs(b2.getY1() - b1.getY2()) <= 1) {
				return IntersectInfo.TOUCH_Y; // NOTE: maybe we should test for full y overlap?
			}
		}

		return IntersectInfo.YES;
	}

	/**
	 * Checks whether the given blocking infos overlaps or not.
	 */
	protected boolean overlaps(BlockingInfo b1, BlockingInfo b2) {
		if (b1.getX2() <= b2.getX1() || b1.getX1() >= b2.getX2()) { // No x overlap
			return false;
		}
		if (b1.getY2() <= b2.getY1() || b1.getY1() >= b2.getY2()) { // No y overlap
			return false;
		}
		return true;
	}


	/**
	 * Information about colliding objects.
	 */
	private static class CollisionInfo {
		/** Collision flag. */
		private boolean _collision = false;

		/** Join flag. */
		private boolean _join = false;

		/** Blocking info. */
		private BlockingInfo _blockingInfo;

		/** Intersection info. */
		private IntersectInfo _intersectionInfo;

		public boolean hasCollision() {
			return _collision;
		}

		public void setCollision(boolean collision) {
			_collision = collision;
		}

		public boolean getJoin() {
			return _join;
		}

		public void setJoin(boolean join) {
			_join = join;
		}

		public BlockingInfo getBlockingInfo() {
			return _blockingInfo;
		}

		public void setBlockingInfo(BlockingInfo blockingInfo) {
			_blockingInfo = blockingInfo;
		}

		public IntersectInfo getIntersectionInfo() {
			return _intersectionInfo;
		}

		public void setIntersectionInfo(IntersectInfo intersectionInfo) {
			_intersectionInfo = intersectionInfo;
		}
	}


	/**
	 * Object representing a group of nodes belonging together with their common blocking infos.
	 */
	public static class CollisionNodeGroup {

		private final Object _key;
		private final Set<GanttNode> _nodes;
		private final Set<BlockingInfo> _blockingInfos;

		/**
		 * Creates a new {@link CollisionNodeGroup}.
		 */
		public CollisionNodeGroup(Object key) {
			_key = key;
			_nodes = new LinkedHashSet<>();
			_blockingInfos = new HashSet<>();
		}

		/**
		 * Adds the given node to this {@link CollisionNodeGroup}.
		 */
		public void addNode(GanttNode node) {
			_nodes.add(node);
		}

		/**
		 * Adds the given nodes to this {@link CollisionNodeGroup}.
		 */
		public void addNodes(Collection<GanttNode> nodes) {
			_nodes.addAll(nodes);
		}

		/**
		 * Adds the given blocking info to this {@link CollisionNodeGroup}.
		 */
		public void addBlockingInfo(BlockingInfo info) {
			_blockingInfos.add(info);
		}

		/**
		 * Adds the given blocking infos to this {@link CollisionNodeGroup}.
		 */
		public void addBlockingInfos(Collection<BlockingInfo> infos) {
			_blockingInfos.addAll(infos);
		}

		/**
		 * Gets the key of this {@link CollisionNodeGroup}.
		 */
		public Object getKey() {
			return _key;
		}

		/**
		 * Gets the nodes of this {@link CollisionNodeGroup}.
		 */
		public Set<GanttNode> getNodes() {
			return _nodes;
		}

		/**
		 * Gets the blocking infos of this {@link CollisionNodeGroup}.
		 */
		public Set<BlockingInfo> getBlockingInfos() {
			return _blockingInfos;
		}

	}

	/**
	 * Comparator for {@link CollisionNodeGroup}s which sorts more important
	 * {@link CollisionNodeGroup}s smaller than less important ones. Per default all nodes are equal
	 * important.
	 */
	public static class CollisionNodeGroupImportanceComparator implements Comparator<CollisionNodeGroup> {

		/** Default instance of this class. */
		public static final CollisionNodeGroupImportanceComparator INSTANCE = new CollisionNodeGroupImportanceComparator();

		@Override
		public int compare(CollisionNodeGroup o1, CollisionNodeGroup o2) {
			Object bo1 = o1.getKey();
			if (bo1 instanceof GanttObject) {
				bo1 = ((GanttObject)bo1).getBusinessObject();
			}
			Object bo2 = o2.getKey();
			if (bo2 instanceof GanttObject) {
				bo2 = ((GanttObject)bo2).getBusinessObject();
			}
			return compareBusinessObject(bo1, bo2);
		}

		/**
		 * Compares the given business objects.
		 * @see #compare(CollisionNodeGroup, CollisionNodeGroup)
		 */
		protected int compareBusinessObject(Object bo1, Object bo2) {
			return 0;
		}

	}

}
