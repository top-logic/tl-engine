/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;

import com.top_logic.base.chart.DefaultImageData;
import com.top_logic.base.chart.component.AbstractImageComponent;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.control.CssButtonControlProvider;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutableCommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.reporting.chart.gantt.I18NConstants;
import com.top_logic.reporting.chart.gantt.model.GanttChartConstants;
import com.top_logic.reporting.chart.gantt.model.GanttChartSettings;
import com.top_logic.reporting.chart.gantt.ui.AbstractGanttChartCreator;
import com.top_logic.reporting.chart.gantt.ui.GanttChartExporter;
import com.top_logic.reporting.chart.gantt.ui.GraphicData;
import com.top_logic.reporting.chart.gantt.ui.ImageData;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * Component showing a gantt chart of the selected structure.
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class GanttComponent extends AbstractImageComponent {

	/**
	 * Configuration options for {@link GanttComponent}.
	 */
	public interface Config extends AbstractImageComponent.Config {

		/**
		 * @see #getRowsPerPage()
		 */
		String ROWS_PER_PAGE = "rowsPerPage";

		@Name(XML_CONFIG_CHART_CREATOR)
		PolymorphicConfiguration<AbstractGanttChartCreator> getChartCreator();

		@Name(XML_CONFIG_CHART_EXPORTER)
		@ImplementationClassDefault(GanttChartExporter.class)
		PolymorphicConfiguration<GanttChartExporter> getChartExporter();

		/**
		 * Number of rows displayed on one page.
		 */
		@Name(ROWS_PER_PAGE)
		@IntDefault(100)
		int getRowsPerPage();
	}

	/** Property key for the page count constraint in current page field. */
	public static final Property<Constraint> PAGE_COUNT_CONSTRAINT =
		TypedAnnotatable.property(Constraint.class, "PageCountConstraint");

	/** Command name for first page command. */
	public static final String COMMAND_FIRST_PAGE = "firstPage";

	/** Command name for previous page command. */
	public static final String COMMAND_PREV_PAGE = "prevPage";

	/** Command name for next page command. */
	public static final String COMMAND_NEXT_PAGE = "nextPage";

	/** Command name for last page command. */
	public static final String COMMAND_LAST_PAGE = "lastPage";

	/** Field name for current page field. */
	public static final String FIELD_CURRENT_PAGE = "currentPage";

	/** Field name for max. page field. */
	public static final String FIELD_PAGE_COUNT = "maxPage";

	/** Field name for last updated field. */
	public static final String FIELD_TIMESTAMP = "timeStamp";

	/** Configuration attribute of the {@link AbstractGanttChartCreator} to use. */
	public static final String XML_CONFIG_CHART_CREATOR = "chartCreator";

	/** Configuration attribute of the {@link GanttChartExporter} to use. */
	public static final String XML_CONFIG_CHART_EXPORTER = "chartExporter";

	private static final ControlProvider PAGE_BUTTON_CP = new CssButtonControlProvider("pgButton");

	/** Holds the {@link AbstractGanttChartCreator}. */
	protected final AbstractGanttChartCreator _chartCreator;

	/** Holds the {@link GanttChartExporter}. */
	private final GanttChartExporter _chartExporter;

	/** Holds the created image. */
	private ImageData _imageData = null;

	/** Holds the created graphic. */
	private GraphicData _graphicData = null;

	/** The current selected page. */
	private int _currentPage = 0;

	/** The page count. */
	private int _pageCount = 1;

	/** Stores the current page to restore the page when a new chart gets created. */
	private int _storedCurrentPage = -1;

	private String _message = null;

	private final ResKey _notExecutableReason;

	/**
	 * Creates a {@link GanttComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GanttComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_chartCreator = context.getInstance(config.getChartCreator());
		_chartExporter = context.getInstance(config.getChartExporter());
		_notExecutableReason = getResPrefix().key("notExecutable");
	}

	private Config config() {
		return (Config) _config;
	}
	
	/**
	 * The chart renderer.
	 */
	public final AbstractGanttChartCreator getChartCreator() {
		return _chartCreator;
	}

	@Override
	protected boolean isChangeHandlingDefault() {
		return false;
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof GanttChartSettings;
	}

	/**
	 * Gets the {@link GanttChartSettings}.
	 * 
	 * <p>
	 * Type-safe access to {@link #getModel()}.
	 * </p>
	 */
	public final GanttChartSettings getFilterSettings() {
		return (GanttChartSettings) getModel();
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		setGraphicData(null);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		setGraphicData(null);
	}

	/**
	 * Gets the {@link ImageData}.
	 */
	protected ImageData getImageData() {
		return _imageData;
	}

	/**
	 * Sets the {@link ImageData}.
	 */
	protected void setImageData(ImageData imageData) {
		_imageData = imageData;
	}

	/**
	 * Gets the {@link GraphicData}.
	 */
	protected GraphicData getGraphicData() {
		return _graphicData;
	}

	/**
	 * Sets the {@link GraphicData}.
	 */
	protected void setGraphicData(GraphicData graphicData) {
		if (_graphicData != graphicData) {
			if (_graphicData != null) {
				_storedCurrentPage = getCurrentPage();
			}
			_graphicData = graphicData;
			if (graphicData == null) {
				setPageCount(1);
				setMessage(null);
			}
			else {
				setPageCount(graphicData.getPageCount());
				String message = graphicData.hasMessage() ? null :
					getResMessage(FIELD_TIMESTAMP, formatTimestampValue(new Date()));
				setMessage(message);
				if (_storedCurrentPage >= 0 && _storedCurrentPage < getPageCount()) {
					setCurrentPage(_storedCurrentPage);
				}
			}
			setImageData(null);
		}
	}

	private void setMessage(String message) {
		_message = message;
		if (hasFormContext()) {
			getFormContext().getField(FIELD_TIMESTAMP).setValue(message);
		}
	}

	public String getMessage() {
		return _message;
	}

	/**
	 * Formats the given timestamp date.
	 */
	protected String formatTimestampValue(Date date) {
		return HTMLFormatter.getInstance().getDateTimeFormat().format(date);
	}

	/**
	 * Gets the current page to show.
	 */
	protected int getCurrentPage() {
		return _currentPage;
	}

	/**
	 * Sets the current page to show.
	 */
	protected final void setCurrentPage(int currentPage) {
		setCurrentPage(currentPage, true);
	}

	/**
	 * Sets the current page to show.
	 */
	protected void setCurrentPage(int currentPage, boolean updatePageField) {
		if (_currentPage != currentPage) {
			_currentPage = currentPage;
			setImageData(null);
			if (hasFormContext()) {
				FormContext formContext = getFormContext();
				if (updatePageField) {
					IntField field = (IntField)formContext.getField(FIELD_CURRENT_PAGE);
					field.initializeField(Integer.valueOf(currentPage + 1));
				}
				handleButtonExecutability(formContext);
			}
		}
	}

	/**
	 * Gets the current page to show.
	 */
	protected int getPageCount() {
		return _pageCount;
	}

	/**
	 * Sets the page count.
	 */
	protected void setPageCount(int pageCount) {
		if (_pageCount != pageCount) {
			_pageCount = pageCount;
			checkCurrentPage();
			if (hasFormContext()) {
				FormContext formContext = getFormContext();
				IntField field = (IntField)formContext.getField(FIELD_PAGE_COUNT);
				field.initializeField(Integer.valueOf(pageCount));
				handleButtonExecutability(formContext);
				updateConstraint(formContext.getField(FIELD_CURRENT_PAGE));
			}
		}
	}

	/**
	 * Checks whether the current page number fits in the range 0 to {@link #getPageCount()} - 1.
	 */
	protected void checkCurrentPage() {
		int currentPage = getCurrentPage();
		int pageCount = getPageCount();
		int newPage = currentPage;
		if (currentPage >= pageCount) newPage = pageCount - 1;
		if (currentPage < 0) newPage = 0;
		if (currentPage != newPage) setCurrentPage(newPage);
	}



	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(this);

		formContext.addMember(createButton(COMMAND_FIRST_PAGE, com.top_logic.layout.table.renderer.Icons.TBL_FIRST,
			com.top_logic.layout.table.renderer.Icons.TBL_FIRST_DISABLED, new GanttFirstPageCommand()));
		formContext.addMember(createButton(COMMAND_PREV_PAGE, com.top_logic.layout.table.renderer.Icons.TBL_PREV,
			com.top_logic.layout.table.renderer.Icons.TBL_PREV_DISABLED, new GanttPrevPageCommand()));
		formContext.addMember(createButton(COMMAND_NEXT_PAGE, com.top_logic.layout.table.renderer.Icons.TBL_NEXT,
			com.top_logic.layout.table.renderer.Icons.TBL_NEXT_DISABLED, new GanttNextPageCommand()));
		formContext.addMember(createButton(COMMAND_LAST_PAGE, com.top_logic.layout.table.renderer.Icons.TBL_LAST,
			com.top_logic.layout.table.renderer.Icons.TBL_LAST_DISABLED, new GanttLastPageCommand()));

		IntField field = FormFactory.newIntField(FIELD_CURRENT_PAGE, Integer.valueOf(getCurrentPage() + 1), !IMMUTABLE);
		updateConstraint(field);
		field.addValueListener(new GanttCurrentPageValueListener());
		formContext.addMember(field);
		field = FormFactory.newIntField(FIELD_PAGE_COUNT, Integer.valueOf(getPageCount()), IMMUTABLE);
		formContext.addMember(field);

		StringField timeStampField = FormFactory.newStringField(FIELD_TIMESTAMP, getMessage(), IMMUTABLE);
		formContext.addMember(timeStampField);

		handleButtonExecutability(formContext);
		return formContext;
	}

	/**
	 * Adds or updates the constraint of current page field.
	 *
	 * @param field
	 *        the current page field to add the constraint
	 */
	protected void updateConstraint(FormField field) {
		// Stupid code because RangeConstraint has declared getUpper() as final.
		Constraint oldConstraint = field.get(PAGE_COUNT_CONSTRAINT);
		if (oldConstraint != null) {
			field.removeConstraint(oldConstraint);
		}
		IntRangeConstraint newConstraint = new IntRangeConstraint(1, getPageCount());
		field.addConstraint(newConstraint);
		field.set(PAGE_COUNT_CONSTRAINT, newConstraint);
	}

	/**
	 * Creates a command field representing a page navigation button.
	 */
	protected CommandField createButton(String name, ThemeImage image, ThemeImage disabledImage,
			GanttPageCommand command) {
		CommandField commandField = FormFactory.newCommandField(name, command);
		commandField.setImage(image);
		commandField.setNotExecutableImage(disabledImage);
		String label = getResString("button." + name);
		commandField.setLabel(label);
		commandField.setTooltip(label);
		commandField.setControlProvider(PAGE_BUTTON_CP);
		return commandField;
	}



	/**
	 * Checks the executability of the buttons and enables or disables them.
	 */
	private void handleButtonExecutability(FormContext formContext) {
		for (Iterator<? extends FormMember> it = formContext.getMembers(); it.hasNext();) {
			FormMember formMember = it.next();
			if (formMember instanceof CommandField) {
				ExecutableCommandField field = (ExecutableCommandField) formMember;
				Command command = field.getExecutable();
				if (command instanceof GanttPageCommand) {
					if (((GanttPageCommand) command).isExecutable()) {
						field.setExecutable();
					} else {
						field.setNotExecutable(_notExecutableReason);
					}
				}
			}
		}
	}

	/**
	 * Creates the gantt chart export PDF file.
	 */
	public File generateChartPDF(DisplayContext displayContext, String exportFormat) throws Exception {
		return generateChartPDF(GanttChartExporter.extractContextInfoForExport(displayContext), exportFormat);
	}

	/**
	 * Creates the gantt chart export PDF file.
	 */
	public File generateChartPDF(Pair<String, String> contextInfo, String exportFormat) throws Exception {
		GraphicData graphicData = getGraphicData();
		if (exportFormat.equals(GanttExportDialog.SCALING_OPTION_ONE_PAGE) || exportFormat.equals(GanttExportDialog.SCALING_OPTION_NATIVE)) {
			// 1. compute graphics data
			if (graphicData == null) {
				graphicData = computeChart(getFilterSettings(), 0, false);
			}
			// 2. create image datas
			int pageCount = graphicData.getPageCount();
			List<ImageData> imageDatas = new ArrayList<>();
			if (pageCount == 1) {
				// there is anyway only one page shown at gui --> create ImageData with header and footer
				imageDatas.add(buildChart(graphicData, 0, 0, true, true));
			} else {
				// there is more than one page shown at gui --> create header only for first and footer only for last page
				for (int i = 0; i < pageCount; i++) {
					if (i == 0) {
						imageDatas.add(buildChart(graphicData, i, 0, true, false));
					} else if (i == (pageCount - 1)) {
						imageDatas.add(buildChart(graphicData, i, 0, false, true));
					} else {
						imageDatas.add(buildChart(graphicData, i, 0, false, false));
					}
				}
			}

			// 3. create PDF
			int chartHeight = graphicData.getHeaderHeight() + graphicData.getContentHeight() + graphicData.getFooterHeight();
			
			boolean isNativeFormat = exportFormat.equals(GanttExportDialog.SCALING_OPTION_NATIVE);
			return _chartExporter.generateChartPDF(contextInfo, imageDatas, getFilterSettings(), true, chartHeight, graphicData.getWidth(), isNativeFormat);
		}
		else {
			// 1. compute graphics data
			int graphicWidth = graphicData != null ? graphicData.getWidth() : 0;
			graphicData = computeChart(getFilterSettings(), graphicWidth, true);

			// 2. create image datas
			List<ImageData> imageDatas = new ArrayList<>();
			for (int i = 0; i < graphicData.getPageCount(); i++) {
				imageDatas.add(buildChart(graphicData, i, 0, true, true));
			}

			// 3. create PDF
			int firstPageHeight = CollectionUtil.getFirst(imageDatas).getDimension().height;
			return _chartExporter.generateChartPDF(contextInfo, imageDatas, getFilterSettings(), false, firstPageHeight, graphicData.getWidth(), false);
		}

	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		int width = extractWidth(dimension);
		if (isGraphicDataInvalid(width)) {
			setGraphicData(computeChart(getFilterSettings(), width, false));
		}
	}


	@Override
	public com.top_logic.base.chart.ImageData createImage(DisplayContext context, String imageId, String imageType,
			Dimension dimension) throws IOException {
		int width = extractWidth(dimension);
		ImageData imageData = getImageData();
		if (imageData == null) {
			imageData = buildChart(getGraphicData(), getCurrentPage(), width, true, true);
			setImageData(imageData);
		}
		BinaryData bytes;
		if (!isWriteToTempFile()) {
			bytes = imageData.toBinaryData();
		} else {
			bytes = BinaryDataFactory.createBinaryData(imageData.toImageFile());
		}

		return new DefaultImageData(imageData.getDimension(), bytes,
			imageData.getHeaderHeight(), imageData.getTreeWidth());
	}

	@Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		int width = extractWidth(dimension);
		ImageData imageData = getImageData();
		if (imageData == null) {
			imageData = buildChart(getGraphicData(), getCurrentPage(), width, true, true);
			setImageData(imageData);
		}
		String imageMap = ChartUtil.getImageMapAsString(mapName, new ChartRenderingInfo(imageData.getAreaData()));
		return imageData.replacePlaceholders(imageMap);
	}



	/**
	 * Checks whether the graphic must be recomputed because of window width change.
	 */
	protected boolean isGraphicDataInvalid(int windowWidth) {
		GraphicData graphicData = getGraphicData();
		if (graphicData == null || graphicData.hasMessage()) {
			return true;
		}
		String scalingOption = (String) CollectionUtil.getFirst(
			getFilterSettings().getValue(GanttChartConstants.PROPERTY_SCALING_OPTION));
		boolean automaticScaling = !GanttChartConstants.SCALING_OPTION_MANUAL.equals(scalingOption);
		return (automaticScaling && graphicData.getWidth() != windowWidth);
	}

	/**
	 * Extracts the width from the given view port area;
	 */
	protected int extractWidth(Dimension dimension) {
		return (int) dimension.getWidth() - 4; // avoid horizontal scroll bar
	}



	/**
	 * Creates the gantt chart.
	 */
	protected GraphicData computeChart(GanttChartSettings filterSettings, int windowWidth, boolean isMultipageExport) {
		try {
			if(isMultipageExport){
				int chartWidth = _chartCreator.getImageWidth(filterSettings, windowWidth);
				int elementsPerPage = computeNodesPerPageForMultiPageExport(filterSettings, chartWidth);
				return _chartCreator.createGraphic(filterSettings, windowWidth, elementsPerPage);
			}else{
				return _chartCreator.createGraphic(filterSettings, windowWidth, config().getRowsPerPage());
			}
		}
		catch (Exception e) {
			Logger.error("Error creating Gantt chart. ", e, GanttComponent.class);
			return _chartCreator.drawEmptyGraphic(I18NConstants.ERROR_CREATION_FAILED);
		}
	}

	/**
	 * Computes the number of nodes that fit vertically on one page in multipage export.
	 */
	private int computeNodesPerPageForMultiPageExport(GanttChartSettings filterSettings, int chartWidth) {
		// compute scaling factor (paperHeight/paperWidth) to be able to compute available pixel vertical
		float scalingFactor;
		if (_chartExporter.usePortraitModeMultiPageExport(chartWidth)) {
			scalingFactor = _chartExporter.getMaxHeightPortrait() / _chartExporter.getMaxWidthPortrait();
			if (chartWidth < _chartExporter.getMaxWidthPortrait()) {
				// if chart width is smaller than available space --> use maximal space available
				// this is needed because in chart exporter we never use a scaling < 1
				// so without this code the export would not use the whole page if the chart has a
				// big height and a small width
				chartWidth = (int) _chartExporter.getMaxWidthPortrait();
			}
		} else {
			scalingFactor = _chartExporter.getMaxHeightLandscape() / _chartExporter.getMaxWidthLandscape();
			if (chartWidth < _chartExporter.getMaxWidthLandscape()) {
				// if chart width is smaller than available space --> use maximal space available
				// this is needed because in chart exporter we never use a scaling < 1
				// so without this code the export would not use the whole page if the chart has a
				// big height and a small width
				chartWidth = (int) _chartExporter.getMaxWidthLandscape();
			}
		}
		
		// available pixel vertical is (width of current gantt chart * scalingFactor)
		float verticalPixelAvailablePerPage = chartWidth * scalingFactor;
		return (int) (verticalPixelAvailablePerPage - _chartCreator.getHeaderHeight(filterSettings) - _chartCreator.getFooterHeight(filterSettings)) / _chartCreator.getNodeHeight(filterSettings);
	}

	/**
	 * Creates a image page from a graphic.
	 */
	protected ImageData buildChart(GraphicData graphicData, int pageNumber, int windowWidth, boolean showHeader, boolean showFooter) {
		try {
			return _chartCreator.createPageImage(graphicData, pageNumber, windowWidth, showHeader, showFooter, getFilterSettings());
		}
		catch (Exception e) {
			Logger.error("Error creating Gantt chart page " + pageNumber + ".", e, GanttComponent.class);
			try {
				return _chartCreator.drawEmptyImage(I18NConstants.ERROR_CREATION_FAILED, windowWidth);
			}
			catch (Exception ex) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}


	/**
	 * Abstract class to switch the page to show.
	 */
	public abstract class GanttPageCommand implements Command {

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			doExecute(context);
			invalidate();
			return DefaultHandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Executes the command. This method may assume valid input for its operation. Any checks on
		 * validity of input and whether this operation is allowed in this context should be done in
		 * {@link #isExecutable()} which will cause inapplicable commands to be de-activated.
		 * 
		 * @see Command#executeCommand(DisplayContext)
		 */
		public abstract void doExecute(DisplayContext context);

		/**
		 * Checks whether the command is currently executable. This should include any
		 * checks with respect to validity of the input, current selection and whether the
		 * operation is allowed in its current context. The result of this method will cause
		 * inappropriate commands to be disabled on the GUI.
		 *
		 * @return Returns <code>true</code> if the command may be executed from a
		 *         structural and from a business point of view, <code>false</code>
		 *         otherwise.
		 */
		public boolean isExecutable() {
			return true;
		}

	}


	/**
	 * Shows the first page.
	 */
	public class GanttFirstPageCommand extends GanttPageCommand {

		@Override
		public void doExecute(DisplayContext context) {
			setCurrentPage(0);
		}

		@Override
		public boolean isExecutable() {
			return getCurrentPage() > 0;
		}

	}


	/**
	 * Shows the previous page.
	 */
	public class GanttPrevPageCommand extends GanttPageCommand {

		@Override
		public void doExecute(DisplayContext context) {
			int newPage = getCurrentPage() - 1;
			if (newPage >= 0) {
				setCurrentPage(newPage);
			}
		}

		@Override
		public boolean isExecutable() {
			return getCurrentPage() > 0;
		}
	}


	/**
	 * Shows the next page.
	 */
	public class GanttNextPageCommand extends GanttPageCommand {

		@Override
		public void doExecute(DisplayContext context) {
			int newPage = getCurrentPage() + 1;
			if (newPage < getPageCount()) {
				setCurrentPage(newPage);
			}
		}

		@Override
		public boolean isExecutable() {
			return getCurrentPage() + 1 < getPageCount();
		}

	}


	/**
	 * Shows the last page.
	 */
	public class GanttLastPageCommand extends GanttPageCommand {

		@Override
		public void doExecute(DisplayContext context) {
			int newPage = getPageCount() - 1;
			setCurrentPage(newPage < 0 ? 0 : newPage);
		}

		@Override
		public boolean isExecutable() {
			return getCurrentPage() + 1 < getPageCount();
		}

	}


	/**
	 * Shows the entered page.
	 */
	public class GanttCurrentPageValueListener implements ValueListener {

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			int newPage = Utils.getintValue(newValue) - 1;
			if (newPage != getCurrentPage() && newPage >= 0 && newPage < getPageCount()) {
				setCurrentPage(newPage, false);
				invalidate();
			}
		}

	}

}
