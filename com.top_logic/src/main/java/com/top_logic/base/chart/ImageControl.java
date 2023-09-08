/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.buttonbar.Icons;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.control.DeliverContentHandler;
import com.top_logic.layout.form.model.DefaultImageModel;
import com.top_logic.layout.form.model.DefaultModeModel;
import com.top_logic.layout.form.model.DimensionListener;
import com.top_logic.layout.form.model.ImageComponentChangeListener;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.model.ImageModel;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * The ImageControl is the control object for {@link com.top_logic.base.chart.ImageComponent}s.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ImageControl extends AbstractControl implements ModeModelListener, DimensionListener,
		ImageComponentChangeListener, ContentHandler {

	/**
	 * Header area of a sync-scroll image containing {@link #SYNCSCROLL_EDGE_CSS_CLASS}, and
	 * {@link #SYNCSCROLL_TOP_CSS_CLASS}.
	 */
	private static final String SYNCSCROLL_HEADER_CSS_CLASS = "ssiHeader";

	/**
	 * Top-left area of a sync-scroll image (that is fixed in x and y dimension).
	 */
	private static final String SYNCSCROLL_EDGE_CSS_CLASS = "ssiEdge";

	/**
	 * Scrolling top area of a sync-scroll image (scrolling horizontally with the
	 * {@link #SYNCSCROLL_CONTENT_CSS_CLASS} area).
	 */
	private static final String SYNCSCROLL_TOP_CSS_CLASS = "ssiTop";

	/**
	 * Body area of a sync-scroll image.
	 */
	private static final String SYNCSCROLL_BODY_CSS_CLASS = "ssiBody";

	/**
	 * Left area of the {@value #SYNCSCROLL_BODY_CSS_CLASS} that is only scrolled vertically
	 * together with the {@link #SYNCSCROLL_CONTENT_CSS_CLASS} area.
	 */
	private static final String SYNCSCROLL_LEFT_CSS_CLASS = "ssiLeft";

	/**
	 * Content scroll container of a sync-scroll image that has scroll bars to be scrolled
	 * horizontally and vertically by the user. The area {@link #SYNCSCROLL_LEFT_CSS_CLASS} follows
	 * this scroll movement vertically, and the area {@link #SYNCSCROLL_TOP_CSS_CLASS} follows
	 * horizontally.
	 */
	private static final String SYNCSCROLL_CONTENT_CSS_CLASS = "ssiContent";

	/**
	 * Technical clipping area within {@value #SYNCSCROLL_CONTENT_CSS_CLASS} to establish a with and
	 * height for the content image.
	 */
	private static final String SYNCSCROLL_CONTENT_AREA_CSS_CLASS = "ssiContentArea";

	/**
	 * The actual image data within {@link #SYNCSCROLL_EDGE_CSS_CLASS},
	 * {@link #SYNCSCROLL_TOP_CSS_CLASS}, {@link #SYNCSCROLL_LEFT_CSS_CLASS}, and
	 * {@value #SYNCSCROLL_CONTENT_AREA_CSS_CLASS}.
	 */
	private static final String SYNCSCROLL_IMAGE_CSS_CLASS = "ssiImage";

	private static final String SIZE_CONTAINER_CLASS = "sizeContainer";
	private static final String IMAGE_CONTAINER_CLASS = "imageContainer";
	private static final String WAITING_SLIDER_STRUCTURE_CLASS = "waitingSliderStructure";

	/**
	 * Commands for {@link ImageControl}.
	 */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ControlCommand[] {
		RequestImageCommand.INSTANCE
	});

	/** The identifier of the image. */
	/* package protected */ String imageId;

	private boolean _useImageMap = true;

	/** Flag whether to show a waiting slider while loading the image. */
	private boolean useWaitingSlider = false;

	/** Flag whether to subtract scroll bar size from window height when computing window size. */
	private boolean respectHorizontalScrollbar = true;

	/** Flag whether to subtract scroll bar size from window width when computing window size. */
	private boolean respectVerticalScrollbar = true;

	private ImageModel imageModel;

	private DeliverContentHandler _imageHandler;

	/**
	 * The {@link ModeModel} that control the visibility of this control.
	 */
	private final ModeModel modeModel;

	private String _cssClasses = null;
	
	boolean _hasFlexibleDimension = true;

	private Dimension _dimension = null;

	/**
	 * Information from last complete rendering, used for decision whether an incremental update is
	 * possible.
	 */
	private int _lastHeaderHeight;

	/**
	 * Information from last complete rendering, used for decision whether an incremental update is
	 * possible.
	 */
	private int _lastLeftWidth;

	/**
	 * Information from last complete rendering, used for decision whether an incremental update is
	 * possible.
	 */
	private boolean _lastSyncScroll;

	/**
	 * Information from last complete rendering, used for decision whether an incremental update is
	 * possible.
	 */
	private Dimension _lastDimension;

	/**
	 * Creates an {@link ImageControl} for the given {@link ImageField}.
	 * 
	 * @param field
	 *        the field to create a control for
	 */
	public ImageControl(ImageField field) {
		this(field, null, new FormModeModelAdapter(field), field.getCssClasses());
		setUseImageMap(false);
	}

	/**
	 * Creates a {@link ImageControl} with a {@link DefaultModeModel}.
	 *
	 * @see ImageControl#ImageControl(ImageComponent, Dimension, String, ModeModel, String)
	 */
	public ImageControl(ImageComponent component, Dimension dimension, String imageId, String cssClasses) {
		this(component, dimension, imageId, new DefaultModeModel(), cssClasses);
	}

	/**
	 * Creates an {@link ImageControl} with a {@link DefaultImageModel} build from the given
	 * {@link ImageComponent} and {@link Dimension}.
	 * 
	 * @param component
	 *        The {@link ImageComponent} to display.
	 * @param dimension
	 *        The {@link Dimension} of the image. <code>null</code> indicates that the image should
	 *        be adjusted to the dimension of the browser window.
	 * @see ImageControl#ImageControl(ImageModel, String, ModeModel, String)
	 */
	public ImageControl(ImageComponent component, Dimension dimension, String imageId, ModeModel modeModel, String cssClasses) {
		this(new DefaultImageModel(component, dimension), imageId, modeModel, cssClasses);
	}

	/**
	 * Creates an {@link ImageControl} with arbitrary {@link ImageModel}.
	 *
	 * @param model
	 *        The {@link ImageModel} to use. Must not be <code>null</code>.
	 * @param imageId
	 *        The image identifier.
	 * @param modeModel
	 *        The model that controls the visibility of the image represented by this control.
	 */
	public ImageControl(ImageModel model, String imageId, ModeModel modeModel, String cssClasses) {
		this(model, imageId, modeModel, COMMANDS, cssClasses);
	}

	/**
	 * {@link ImageControl} constructor for subclasses with custom command map.
	 */
	protected ImageControl(ImageModel model, String imageId, ModeModel modeModel,
			Map<String, ControlCommand> commandsByName, String cssClasses) {
		super(commandsByName);
		this.imageModel = model;
		this.imageId = imageId;
		this.modeModel = modeModel;
		_imageHandler = new DeliverContentHandler();
		_cssClasses = cssClasses;
	}

	@Override
	public ImageModel getModel() {
		return imageModel;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		init(imageModel.getDimension());
		this.modeModel.addModeModelListener(this);
		this.imageModel.addListener(ImageModel.DIMENSION_PROPERTY, this);
		this.imageModel.addListener(ImageModel.IMAGE_PROPERTY, this);
		FrameScope urlContext = getURLContext();
		urlContext.registerContentHandler(getID(), this);
	}

	@Override
	protected void internalDetach() {
		getURLContext().deregisterContentHandler(this);
		this.imageModel.removeListener(ImageModel.IMAGE_PROPERTY, this);
		this.imageModel.removeListener(ImageModel.DIMENSION_PROPERTY, this);
		this.modeModel.removeModeModelListener(this);
		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		beginControlTag(context, out);
		if (isVisible()) {
			writeSizeMeasurementContainer(context, out);
			writeImageAnchor(context, out);
		}
		endControlTag(out);
	}

	private void endControlTag(TagWriter out) {
		out.endTag(DIV);
	}

	private void beginControlTag(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
	}

	private void writeSizeMeasurementContainer(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, getSizeMeasurementContainerId());
		out.writeAttribute(CLASS_ATTR, SIZE_CONTAINER_CLASS);
		out.endBeginTag();
		writeSizeComputingJavaScript(context, out);
		out.endTag(DIV);
	}

	private void writeDimensionStyle(TagWriter out, Dimension dimension) throws IOException {
		double width = dimension.getWidth();
		double height = dimension.getHeight();
		if (width > 0 && height > 0) {
			out.beginAttribute(STYLE_ATTR);
			out.append("width: ");
			out.append(Double.toString(width));
			out.append("px; ");
			out.append("height: ");
			out.append(Double.toString(height));
			out.append("px;");
			out.endAttribute();
		}
	}

	private String getSizeMeasurementContainerId() {
		return getID() + "-sizeMeasurementContainer";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, _cssClasses);
	}

	private void writeImageAnchor(DisplayContext context, TagWriter out) throws IOException {
		beginImageAnchor(out);
		if (isUseWaitingSlider()) {
			writeWaitingSliderStructure(context, out);
		}
		endImageAnchor(out);
	}

	private void beginImageAnchor(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, getImageAnchorId());
		out.writeAttribute(CLASS_ATTR, IMAGE_CONTAINER_CLASS);
		Dimension dimension = getDimension();
		if (dimension != null) {
			writeDimensionStyle(out, dimension);
		}
		out.endBeginTag();
	}

	private void endImageAnchor(TagWriter out) {
		out.endTag(DIV);
	}

	private String getImageAnchorId() {
		return getID() + "-imageAnchor";
	}

	private void writeWaitingSliderStructure(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(TABLE);
		out.writeAttribute(CLASS_ATTR, WAITING_SLIDER_STRUCTURE_CLASS);
		out.endBeginTag();
		out.beginTag(TBODY);
		out.beginTag(TR);
		out.beginTag(TD);
		writeWaitingSlider(context, out);
		out.endTag(TD);
		out.endTag(TR);
		out.endTag(TBODY);
		out.endTag(TABLE);
	}

	/**
	 * Writes a waiting slider, computes the area dimension and start an update to replace the slider with an image.
	 */
	protected void writeWaitingSlider(DisplayContext aContext, TagWriter out) throws IOException {
		Icons.SLIDER_IMG.get().writeWithCss(aContext, out, "fullProgress");
	}

	/**
	 * write JavaScript to compute dimension and reload control.
	 */
	protected void writeSizeComputingJavaScript(DisplayContext aContext, TagWriter out) throws IOException {
		// write JavaScript to compute dimension and reload control
		HTMLUtil.beginScriptAfterRendering(out);

		out.append("services.form.ImageControl.init('");
		out.append(getID());
		out.append("', '");
		out.append(getSizeMeasurementContainerId());
		out.append("', ");
		out.append(Boolean.toString(isRespectVerticalScrollbar()));
		out.append(", ");
		out.append(Boolean.toString(isRespectHorizontalScrollbar()));
		out.append(", ");
		out.append(Boolean.toString(_hasFlexibleDimension));
		out.append(");");

		HTMLUtil.endScriptAfterRendering(out);
	}

	private void writeCheckForUpdateJavaScript(DisplayContext aContext, TagWriter out) throws IOException {
		// write JavaScript to compute dimension and reload control
		HTMLUtil.beginScriptAfterRendering(out);

		out.append("services.form.ImageControl.checkForBufferedUpdateRequests('");
		out.append(getID());
		out.append("', '");
		out.append(getSizeMeasurementContainerId());
		out.append("', ");
		out.append(Boolean.toString(isRespectVerticalScrollbar()));
		out.append(", ");
		out.append(Boolean.toString(isRespectHorizontalScrollbar()));
		out.append(");");

		HTMLUtil.endScriptAfterRendering(out);
	}

	void writeImageContent(DisplayContext context, TagWriter out) throws IOException {
		beginImageAnchor(out);

		ImageData image = createImage(context);
		if (image != null) {
			String url = createImageUrl(context, image);

			_lastDimension = image.getDimension();
			_lastHeaderHeight = image.getHeaderHeight();
			_lastLeftWidth = image.getDescriptionWidth();
			_lastSyncScroll = syncScroll(_lastHeaderHeight, _lastLeftWidth);
			if (_lastSyncScroll) {
				writeSyncScrollArea(context, out, _lastDimension, _lastHeaderHeight, _lastLeftWidth, url);
			} else {
				writeImageTag(out, url, contentId(), null, null, _lastDimension);
				writeImageMap(context, out);
			}
		}

		writeCheckForUpdateJavaScript(context, out);
		endImageAnchor(out);
	}

	private boolean syncScroll(int headerHeight, int leftWidth) {
		return headerHeight > 0 || leftWidth > 0;
	}

	private void writeSyncScrollArea(DisplayContext context, TagWriter out, Dimension imageDimension, 
			int headerHeight, int leftWidth, String url) throws IOException {
		int contentWidth = imageDimension.width - leftWidth;
		int contentHeight = imageDimension.height - headerHeight;

		String topId = topId();
		String leftId = leftId();
		String contentDivId = contentDivId();
		{
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, SYNCSCROLL_HEADER_CSS_CLASS);
			out.writeAttribute(STYLE_ATTR, "height: " + headerHeight + "px");
			out.endBeginTag();
			{
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, SYNCSCROLL_EDGE_CSS_CLASS);
				out.writeAttribute(STYLE_ATTR, "width: " + leftWidth + "px");
				out.endBeginTag();
				{
					writeImageTag(out, url, edgeId(), SYNCSCROLL_IMAGE_CSS_CLASS, null, imageDimension);
				}
				out.endTag(DIV);

				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, SYNCSCROLL_TOP_CSS_CLASS);
				out.writeAttribute(STYLE_ATTR, "left: " + leftWidth + "px");
				out.endBeginTag();
				{
					writeImageTag(out, url, topId, SYNCSCROLL_IMAGE_CSS_CLASS, "left: -" + leftWidth + "px",
						imageDimension);
				}
				out.endTag(DIV);
			}
			out.endTag(DIV);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, SYNCSCROLL_BODY_CSS_CLASS);
			out.writeAttribute(STYLE_ATTR, "top: " + headerHeight + "px");
			out.endBeginTag();
			{
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, SYNCSCROLL_LEFT_CSS_CLASS);
				out.writeAttribute(STYLE_ATTR, "width: " + leftWidth + "px");
				out.endBeginTag();
				{
					writeImageTag(out, url, leftId, SYNCSCROLL_IMAGE_CSS_CLASS, "top: -" + headerHeight + "px",
						imageDimension);
				}
				out.endTag(DIV);

				out.beginBeginTag(DIV);
				out.writeAttribute(ID_ATTR, contentDivId);
				out.writeAttribute(CLASS_ATTR, SYNCSCROLL_CONTENT_CSS_CLASS);
				out.writeAttribute(STYLE_ATTR, "left: " + leftWidth + "px");
				out.endBeginTag();
				{
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, SYNCSCROLL_CONTENT_AREA_CSS_CLASS);
					out.writeAttribute(STYLE_ATTR, "width: " + contentWidth + "px; height: " + contentHeight
						+ "px");
					out.endBeginTag();
					{
						writeImageTag(out, url, contentId(), SYNCSCROLL_IMAGE_CSS_CLASS,
							"top: -" + headerHeight + "px; left: -" + leftWidth
							+ "px", imageDimension);
					}
					out.endTag(DIV);

					// Note: The image map must be placed inside a wheel-event-monitored region to
					// get the event even if the mouse cursor is currently over an active area
					// described in the image map.
					writeImageMap(context, out);
				}
				out.endTag(DIV);
			}
			out.endTag(DIV);
		}

		out.beginScript();
		out.append("services.form.syncScroll(" +
			headerHeight + ", " + leftWidth + ", '" + topId + "', '" + leftId + "', '" + contentDivId + "');");
		out.endScript();
	}

	private void writeImageTag(TagWriter out, String url, String id, String cssClass, String style, Dimension dimension) {
		out.beginBeginTag(IMG);
		out.writeAttribute(ID_ATTR, id);
		out.writeAttribute(STYLE_ATTR, style);
		out.writeAttribute(CLASS_ATTR, cssClass);
		out.writeAttribute(BORDER_ATTR, 0);
		out.writeAttribute(WIDTH_ATTR, dimension.width);
		out.writeAttribute(HEIGHT_ATTR, dimension.height);
		out.writeAttribute(SRC_ATTR, url);

		if (useImageMap()) {
			out.writeAttribute(USEMAP_ATTR, '#' + mapId());
		}
		out.endEmptyTag();
	}

	final String createImageUrl(DisplayContext context, ImageData image) {
		_imageHandler.setData(image.getBytes());

		FrameScope urlContext = getURLContext();
		return urlContext.getURL(context, this).appendParameter("t", Long.toString(System.nanoTime())).getURL();
	}

	final ImageData createImage(DisplayContext aContext) {
		try {
			return imageComponent().createImage(aContext, imageId, ImageComponent.IMAGE_TYPE_PNG, getDimension());
		} catch (Exception e) {
			Logger.error("Could not write the chart because an exception has occurred for the image id '"
				+ this.imageId +
				"' in scope '" + getScope() + "'.", e, this);
			return null;
		}
	}

	private ImageComponent imageComponent() {
		return this.imageModel.getImageComponent();
	}

	private void writeImageMap(DisplayContext context, TagWriter out) throws IOException {
		Dimension dimension = getDimension();

		/* Write the image map if there is one. */
		if (useImageMap()) {
			getImageMap(dimension, mapId()).write(context, out);
		}
	}

	private HTMLFragment getImageMap(Dimension dimension, String mapId) {
		HTMLFragment map;
		try {
			map = imageComponent().getImageMap(imageId, mapId, dimension);
		} catch (Exception e) {
			errorImageMapCreation(e);
			return emptyMap(mapId);
		}
		if (map != null) {
			return map;
		}
		return emptyMap(mapId);
	}

	private static HTMLFragment emptyMap(String mapId) {
		return Fragments.map(Fragments.attributes(Fragments.name(mapId), Fragments.id(mapId)));
	}

	private void errorImageMapCreation(Exception ex) {
		Logger.error("Could not get the image map for the image ('" + this.imageId + "').", ex, this);
	}

	private FrameScope getURLContext() {
		return getScope().getFrameScope();
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		_imageHandler.handleContent(context, id, url);
	}

	@Override
	public void handleModeChange(Object sender, int oldMode, int newMode) {
		boolean change = (oldMode == ModeModel.INVISIBLE_MODE) ^ (newMode == ModeModel.INVISIBLE_MODE);
		if (change) {
			update();
		}
	}

	@Override
	public void handleDimensionChanged(Object sender, Dimension oldDimension, Dimension newDimension) {
		if (sender != this.imageModel) {
			return;
		}
		init(newDimension);
		requestRepaint();
	}

	@Override
	public void handleImageChanged(ImageModel sender, ImageComponent oldValue, ImageComponent newValue) {
		if (sender != this.imageModel) {
			return;
		}
		update();
	}

	private void init(Dimension dimension) {
		setDimension(dimension);
		_hasFlexibleDimension = (getDimension() == null);
	}

	/**
	 * This method updates the {@link ImageControl}.
	 */
	public void update() {
		if (!isAttached() || isRepaintRequested()) {
			return;
		}

		HandlerResult result = inControlScope(DefaultDisplayContext.getDisplayContext(), context -> {
			ImageData image = createImage(context);
			if (image != null) {
				String url = createImageUrl(context, image);

				Dimension dimension = image.getDimension();
				if (_lastDimension != null && _lastDimension.width == dimension.width
					&& _lastDimension.height == dimension.height) {
					int headerHeight = image.getHeaderHeight();
					int leftWidth = image.getDescriptionWidth();
					if (_lastHeaderHeight == headerHeight && _lastLeftWidth == leftWidth) {
						boolean syncScroll = syncScroll(headerHeight, leftWidth);
						ConstantDisplayValue urlValue = new ConstantDisplayValue(url);
						if (syncScroll == _lastSyncScroll) {
							if (syncScroll) {
								addUpdate(new PropertyUpdate(edgeId(), SRC_ATTR, urlValue));
								addUpdate(new PropertyUpdate(leftId(), SRC_ATTR, urlValue));
								addUpdate(new PropertyUpdate(topId(), SRC_ATTR, urlValue));
								addUpdate(new PropertyUpdate(contentId(), SRC_ATTR, urlValue));
							} else {
								addUpdate(new PropertyUpdate(contentId(), SRC_ATTR, urlValue));
								if (useImageMap()) {
									String mapId = mapId();
									addUpdate(new ElementReplacement(mapId, getImageMap(dimension, mapId)));
								}
							}
							return HandlerResult.DEFAULT_RESULT;
						}
					}
				}
			}
			return null;
		});

		if (result == null) {
			// Redraw of internal structure, produces minimal flicker.
			addUpdate(new ElementReplacement(getImageAnchorId(), new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					writeImageContent(context, out);
				}
			}));
		}
	}

	/**
	 * Setter for {@link #useImageMap()}.
	 */
	public void setUseImageMap(boolean useImageMap) {
		if (useImageMap == useImageMap()) {
			return;
		}
		_useImageMap = useImageMap;
		requestRepaint();
	}

	/**
	 * Whether an image map for the written image must be used.
	 */
	public boolean useImageMap() {
		return _useImageMap;
	}

	private String mapId() {
		return getID() + "-map";
	}

	private String edgeId() {
		return getID() + "-edge";
	}

	private String leftId() {
		return getID() + "-left";
	}

	private String topId() {
		return getID() + "-top";
	}

	private String contentId() {
		return getID() + "-content";
	}

	private String contentDivId() {
		return getID() + "-contentDiv";
	}

	@Override
	public boolean isVisible() {
		return this.modeModel.getMode() != ModeModel.INVISIBLE_MODE;
	}

	/**
	 * This method returns the dimension.
	 *
	 * @return Returns the dimension.
	 */
	public Dimension getDimension() {
		return _dimension;
	}

	/**
	 * This method sets the dimension.
	 * 
	 * @param dimension
	 *        The dimension to set.
	 */
	void setDimension(Dimension dimension) {
		_dimension = dimension;
	}

	/**
	 * This method returns the useWaitingSlider.
	 *
	 * @return Returns the useWaitingSlider.
	 */
	public boolean isUseWaitingSlider() {
		return useWaitingSlider;
	}

	/**
	 * This method sets the useWaitingSlider.
	 *
	 * @param useWaitingSlider The useWaitingSlider to set.
	 */
	public void setUseWaitingSlider(boolean useWaitingSlider) {
		this.useWaitingSlider = useWaitingSlider;
	}

	/**
	 * This method returns the respectHorizontalScrollbar.
	 *
	 * @return Returns the respectHorizontalScrollbar.
	 */
	public boolean isRespectHorizontalScrollbar() {
		return respectHorizontalScrollbar;
	}

	/**
	 * This method sets the respectHorizontalScrollbar.
	 *
	 * @param respectHorizontalScrollbar
	 *        The respectHorizontalScrollbar to set.
	 */
	public void setRespectHorizontalScrollbar(boolean respectHorizontalScrollbar) {
		this.respectHorizontalScrollbar = respectHorizontalScrollbar;
	}

	/**
	 * This method returns the respectVerticalScrollbar.
	 *
	 * @return Returns the respectVerticalScrollbar.
	 */
	public boolean isRespectVerticalScrollbar() {
		return respectVerticalScrollbar;
	}

	/**
	 * This method sets the respectVerticalScrollbar.
	 *
	 * @param respectVerticalScrollbar
	 *        The respectVerticalScrollbar to set.
	 */
	public void setRespectVerticalScrollbar(boolean respectVerticalScrollbar) {
		this.respectVerticalScrollbar = respectVerticalScrollbar;
	}

	/**
	 * Executes the given command in the control scope of this control.
	 * 
	 * <p>
	 * Ensures that the scope of this control is installed in the command context. This is necessary
	 * because the chart may contain links which cannot be created during command execution. This is
	 * not a problem in this case, because the chart is displayed in this control and not in a
	 * foreign one.
	 * </p>
	 */
	HandlerResult inControlScope(DisplayContext commandContext, Command command) {
		return commandContext.executeScoped(getScope(), command);
	}

	/**
	 * This command computes the dimension of the image according to the component frame size in the
	 * browser and updates the image afterwards.
	 */
	public static class RequestImageCommand extends ControlCommand {

		/** The unique command id. */
		public static final String COMMAND = "requestImage";

		/** The single instance of this class. */
		public static final RequestImageCommand INSTANCE = new RequestImageCommand();

		/** Creates a {@link RequestImageCommand}. */
		private RequestImageCommand() {
			super(COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final ImageControl imageControl = (ImageControl) control;
			final Dimension dimension = parseDimension(arguments);

			return imageControl.inControlScope(commandContext, new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					generateImage(context, dimension, imageControl);
					return HandlerResult.DEFAULT_RESULT;
				}
			});
		}

		private Dimension parseDimension(Map<String, Object> arguments) {
			return new Dimension(Utils.getintValue(arguments.get(WIDTH_ATTR)), Utils.getintValue(arguments.get(HEIGHT_ATTR)));
		}

		void generateImage(DisplayContext commandContext, Dimension dimension, ImageControl imageControl) {
			prepareImage(commandContext, dimension, imageControl);
			generateControlUpdate(imageControl);
		}

		private void prepareImage(DisplayContext commandContext, Dimension dimension, ImageControl imageControl) {
			try {
				if (imageControl._hasFlexibleDimension) {
					imageControl.setDimension(dimension);
				}
				imageControl.imageModel.getImageComponent().prepareImage(commandContext, imageControl.imageId,
					imageControl.getDimension());
			} catch (Exception ex) {
				Logger.error("Could not prepare the chart because an exception has occurred for the image id '" +
					imageControl.imageId + "' in scope '" + imageControl.getScope() + "'.", ex, ImageControl.class);
			}
		}

		private void generateControlUpdate(final ImageControl imageControl) {
			imageControl.addUpdate(new ElementReplacement(imageControl.getImageAnchorId(),
				new HTMLFragment() {

				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
						imageControl.writeImageContent(context, out);
				}
			}));
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REQUEST_IMAGE;
		}
	}

}
