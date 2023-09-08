/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import static java.util.Collections.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import com.top_logic.base.multipart.MultipartRequest;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.service.binary.FileItemBinaryData;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.CommandModelNaming;
import com.top_logic.layout.basic.CommandModelOwner;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.GenericCommandModelOwner;
import com.top_logic.layout.dnd.DnDFileUtilities;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DeliverContentHandler;
import com.top_logic.layout.form.model.DefaultModeModel;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;
import com.top_logic.layout.image.gallery.scripting.GalleryEditButtonProvider;
import com.top_logic.layout.image.gallery.scripting.GalleryInspectorCommand;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link Control}, that displays a {@link GalleryModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryControl extends AbstractControl implements GalleryModelListener, ModeModelListener, ContentHandler {

	/** Property name for open edit dialog command */
	public static final Property<CommandModel> OPEN_EDIT_DIALOG_PROPERTY =
		TypedAnnotatable.property(CommandModel.class, "OpenEditDialog");

	private static final String JS_CONTROL_CLASS = "fotorama";

	private static final String AUTO_INITIALIZATION_ATTR = DATA_ATTRIBUTE_PREFIX + "auto";
	private static final String ALLOW_FULLSCREEN_ATTR = DATA_ATTRIBUTE_PREFIX + "allowfullscreen";
	private static final String NAVIGATION_ATTR = DATA_ATTRIBUTE_PREFIX + "nav";
	private static final String CAPTIONS_ATTR = DATA_ATTRIBUTE_PREFIX + "captions";

	private static final String THUMB_NAVIGATION_VALUE = "thumbs";

	private static final Map<String, ControlCommand> GALLERY_COMMANDS = createCommandMap(
		new ControlCommand[] {
			GalleryInspectorCommand.INSTANCE,
			DropFile.INSTANCE
		});

	private GalleryModel _galleryModel;
	private ModeModel _modeModel;
	private CommandModel _editGalleryCommandModel;
	private Map<GalleryImage, Pair<ContentHandler, ContentHandler>> _contentHandlers = new HashMap<>();

	private final List<BinaryData> _uploadedItems = new ArrayList<>();

	private boolean _uploadPossible;

	/**
	 * Create a new {@link GalleryControl}.
	 */
	public GalleryControl(GalleryModel galleryModel) {
		this(galleryModel, new DefaultModeModel(), true);
	}

	/**
	 * Create a new {@link GalleryControl}, that do respect display modes (e.g. edit mode, view
	 * mode, etc.).
	 */
	public GalleryControl(GalleryModel galleryModel, ModeModel modeModel, boolean uploadPossible) {
		super(GALLERY_COMMANDS);
		_galleryModel = galleryModel;
		_modeModel = modeModel;
		_uploadPossible = uploadPossible;
	}

	@Override
	public GalleryModel getModel() {
		return _galleryModel;
	}

	/**
	 * {@link GalleryModel}, which this {@link GalleryControl} displays.
	 */
	public GalleryModel getGalleryModel() {
		return _galleryModel;
	}

	private void registerContentHandlers(List<GalleryImage> images) {
		FrameScope urlContext = getURLContext();
		for (GalleryImage image : images) {
			ContentHandler regularHandler = createImageHandler(image.getName(), image.getImage());
			urlContext.registerContentHandler(urlContext.createNewID(), regularHandler);
			ContentHandler thumbNailHandler;
			if (hasThumbnail(image)) {
				thumbNailHandler = createImageHandler(image.getName(), image.getThumbnail());
				urlContext.registerContentHandler(urlContext.createNewID(), thumbNailHandler);
			} else {
				thumbNailHandler = null;
			}
			_contentHandlers.put(image, new Pair<>(regularHandler, thumbNailHandler));
		}
	}

	private boolean hasThumbnail(GalleryImage image) {
		return image.getThumbnail() != null;
	}

	private DeliverContentHandler createImageHandler(String name, BinaryDataSource imageData) {
		DeliverContentHandler imageHandler = new DeliverContentHandler();
		imageHandler.setData(ImageDataUtil.castMimeType(name, imageData));
		return imageHandler;
	}

	@Override
	public boolean isVisible() {
		return _modeModel.getMode() != ModeModel.INVISIBLE_MODE;
	}
	
	@Override
	protected void internalAttach() {
		super.internalAttach();
		registerContentHandlers(_galleryModel.getImages());
		getURLContext().registerContentHandler(getID(), this);
		_editGalleryCommandModel = CommandModelFactory.commandModel(new ManageGalleryCommand(_galleryModel));
		if (ScriptingRecorder.isEnabled()) {
			CommandModelNaming.setOwner(_editGalleryCommandModel,
				new GenericCommandModelOwner(_galleryModel, GalleryEditButtonProvider.INSTANCE));
			_galleryModel.set(OPEN_EDIT_DIALOG_PROPERTY, _editGalleryCommandModel);
		}
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		_galleryModel.addListener(GalleryModel.IMAGES_PROPERTY, this);
		_modeModel.addModeModelListener(this);
		setEditGalleryButtonVisible(isButtonVisible(_modeModel.getMode()));
		setEditGalleryButtonActive(isButtonActive(_modeModel.getMode()));
	}

	@Override
	protected void detachInvalidated() {
		_modeModel.removeModeModelListener(this);
		_galleryModel.removeListener(GalleryModel.IMAGES_PROPERTY, this);
		super.detachInvalidated();
	}

	@Override
	protected void internalDetach() {
		unregisterContentHandlers();
		getURLContext().deregisterContentHandler(this);
		if (ScriptingRecorder.isEnabled()) {
			_galleryModel.reset(OPEN_EDIT_DIALOG_PROPERTY);
			CommandModelNaming.setOwner(_editGalleryCommandModel, CommandModelOwner.NO_OWNER);
		}
		_editGalleryCommandModel = null;
		super.internalDetach();
	}

	private void unregisterContentHandlers() {
		FrameScope urlContext = getURLContext();
		for (Pair<ContentHandler, ContentHandler> handler : _contentHandlers.values()) {
			urlContext.deregisterContentHandler(handler.getFirst());
			urlContext.deregisterContentHandler(handler.getSecond());
		}
		_contentHandlers.clear();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeImageContainer(out);
		writeManageGalleryButton(context, out);
		DnDFileUtilities.writeDropArea(out, getID());
		writeJSGalleryInit(context, out);
		DnDFileUtilities.initFileDragAndDrop(context, out, this, this, _uploadPossible,
			GalleryUploadListener.getMaxFileSize());
		out.endTag(DIV);
	}

	private void writeImageContainer(TagWriter out) {
		GalleryViewConfiguration viewConfiguration = _galleryModel.getViewConfiguration();
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, getImageContainerId());
		out.writeAttribute(CLASS_ATTR, JS_CONTROL_CLASS);
		out.writeAttribute(AUTO_INITIALIZATION_ATTR, "false");
		out.writeAttribute(NAVIGATION_ATTR, THUMB_NAVIGATION_VALUE);
		out.writeAttribute(ALLOW_FULLSCREEN_ATTR, "true");
		out.writeAttribute(CAPTIONS_ATTR, showCaptions(_modeModel.getMode()));

		// Note: The attributes "data-width" and "data-height" are used by the Fotorama library
		// displaying the images.
		out.writeAttribute(TL_WIDTH_ATTR, viewConfiguration.getGalleryWidth().toString());
		out.writeAttribute(TL_HEIGHT_ATTR, viewConfiguration.getGalleryHeight().toString());
		out.endBeginTag();
		out.endTag(DIV);
	}

	private CharSequence getImageContainerId() {
		return getID() + "-imageContainer";
	}

	private void writeManageGalleryButton(DisplayContext context, TagWriter out) throws IOException {
		ButtonControl buttonControl =
			new ButtonControl(_editGalleryCommandModel,
				GalleryButtonRenderer.newGalleryButtonRenderer(_galleryModel.getViewConfiguration().getGalleryWidth()));
		buttonControl.write(context, out);
	}

	private void writeImages(DisplayContext context, Appendable out) throws IOException {
		out.append("[");
		if (!_galleryModel.getImages().isEmpty()) {
			writeRegularImages(context, out);
		} else {
			writeNoPreviewImage(context, out);
		}
		out.append("]");
	}

	private void writeNoPreviewImage(DisplayContext context, Appendable out) throws IOException {
		String imageLink = Icons.NO_PREVIEW_IMAGE.get().get(context);
		writeImage(out, Resources.getInstance().getString(I18NConstants.NO_IMAGES_AVAILABLE), imageLink, "");
	}

	private void writeRegularImages(DisplayContext context, Appendable out) throws IOException {
		List<GalleryImage> images = _galleryModel.getImages();
		for (int i = 0; i < images.size(); i++) {
			GalleryImage image = images.get(i);
			if (i > 0) {
				out.append(", ");
			}
			writeRegularImage(context, out, image);
		}
	}

	private void writeRegularImage(DisplayContext context, Appendable out, GalleryImage image)
			throws IOException {
		String imageUrl = getRegularImageUrl(context, image);
		String thumbnailUrl = getThumbnailUrl(context, image);
		String imageName = image.getName();
		writeImage(out, imageName, imageUrl, thumbnailUrl);
	}

	private void writeImage(Appendable out, String imageName, String imageUrl, String thumbnailUrl) throws IOException {
		out.append("{ img: '");
		out.append(imageUrl);
		out.append("', ");
		if (!StringServices.isEmpty(thumbnailUrl)) {
			out.append("thumb: '");
			out.append(thumbnailUrl);
			out.append("', ");
		}
		out.append("caption: '");
		out.append(imageName);
		out.append("'}");
	}

	private String getRegularImageUrl(DisplayContext context, GalleryImage image) {
		ContentHandler imageDataHandler = _contentHandlers.get(image).getFirst();
		return getImageUrl(context, imageDataHandler);
	}

	private String getThumbnailUrl(DisplayContext context, GalleryImage image) {
		ContentHandler imageDataHandler = _contentHandlers.get(image).getSecond();
		if (imageDataHandler != null) {
			return getImageUrl(context, imageDataHandler);
		} else {
			return "";
		}
	}

	private String getImageUrl(DisplayContext context, ContentHandler imageDataHandler) {
		return getURLContext().getURL(context, imageDataHandler).getURL();
	}

	private void writeJSGalleryInit(DisplayContext context, TagWriter out) throws IOException {
		HTMLUtil.beginScriptAfterRendering(out);
		out.append("services.form.GalleryControl.init('");
		out.append(getImageContainerId());
		out.append("', ");
		writeImages(context, out);
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);
	}

	private void writeJSGalleryReload(DisplayContext context, Appendable out) throws IOException {
		out.append("services.form.GalleryControl.reload('");
		out.append(getImageContainerId());
		out.append("', ");
		writeImages(context, out);
		out.append(");");
	}

	private void showJSGalleryImageCaptions(Appendable out, boolean showCaptions) throws IOException {
		out.append("services.form.GalleryControl.showCaptions('");
		out.append(getImageContainerId());
		out.append("', ");
		out.append(String.valueOf(showCaptions));
		out.append(");");
	}

	final FrameScope getURLContext() {
		return getScope().getFrameScope();
	}

	@Override
	public void notifyImagesChanged(GalleryModel source, final List<GalleryImage> oldValue,
			final List<GalleryImage> newValue) {
		if (!CollectionUtil.equals(oldValue, newValue)) {
			renewImageHandlers(newValue);
			addUpdate(new JSSnipplet(new AbstractDisplayValue() {

				@SuppressWarnings("synthetic-access")
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					writeJSGalleryReload(context, out);
				}
			}));
		}
	}

	private void renewImageHandlers(List<GalleryImage> newValue) {
		if (isAttached()) {
			unregisterContentHandlers();
			registerContentHandlers(newValue);
		}
	}

	@Override
	public void handleModeChange(Object sender, int oldMode, int newMode) {
		if (hasVisibilityChanged(oldMode, newMode)) {
			requestRepaint();
		} else {
			showGalleryCaptions(showCaptions(newMode));
			setEditGalleryButtonVisible(isButtonVisible(newMode));
			setEditGalleryButtonActive(isButtonActive(newMode));
		}
	}

	private boolean hasVisibilityChanged(int oldMode, int newMode) {
		return (oldMode == ModeModel.INVISIBLE_MODE) ^ (newMode == ModeModel.INVISIBLE_MODE);
	}

	private void showGalleryCaptions(final boolean showCaptions) {
		addUpdate(new JSSnipplet(new AbstractDisplayValue() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				showJSGalleryImageCaptions(out, showCaptions);
			}
		}));
	}

	private void setEditGalleryButtonVisible(boolean isVisible) {
		if (_editGalleryCommandModel != null) {
			_editGalleryCommandModel.setVisible(isVisible);
		}
	}

	private void setEditGalleryButtonActive(boolean isActive) {
		if (_editGalleryCommandModel != null) {
			if (isActive) {
				_editGalleryCommandModel.setExecutable();
				_editGalleryCommandModel.setTooltip(
					Resources.getInstance().getString(I18NConstants.TOOLTIP_OPEN_IMAGE_MANAGEMENT_DIALOG_BUTTON));
			} else {
				_editGalleryCommandModel.setNotExecutable(I18NConstants.DISABLED_OPEN_IMAGE_MANAGEMENT_DIALOG_BUTTON);
			}
		}
	}

	private boolean showCaptions(int newMode) {
		return newMode == ModeModel.EDIT_MODE;
	}

	private boolean isButtonVisible(int mode) {
		return mode == ModeModel.EDIT_MODE || mode == ModeModel.DISABLED_MODE;
	}

	private boolean isButtonActive(int mode) {
		return mode == ModeModel.EDIT_MODE;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) {
		DnDFileUtilities.handleContent(context, this::performUpload);
	}

	/**
	 * Receives files when they are dropped in the drop area and stores them in
	 * {@link #_uploadedItems}.
	 * 
	 * @param request
	 *        Containing the files that where posted.
	 */
	private void performUpload(MultipartRequest request) {
		clearUploadedItems();
		final List<FileItem> receivedFiles = request.getFiles();
		if (receivedFiles != null) {
			for (int i = 0; i < receivedFiles.size(); i++) {
				final FileItem fileItem = receivedFiles.get(i);

				String fileName = DnDFileUtilities.getFileName(fileItem);
				if (Logger.isDebugEnabled(GalleryControl.class)) {
					Logger.debug("Upload file named '" + fileName + "'", GalleryControl.class);
				}

				BinaryData data = new FileItemBinaryData(fileItem);
				String contentType = DnDFileUtilities.getContentType(fileItem, fileName);
				addUploadedItem(new DefaultDataItem(fileName, data, contentType));
			}
		}
	}

	List<BinaryData> getUploadedItems() {
		return unmodifiableList(_uploadedItems);
	}

	private void addUploadedItem(BinaryData item) {
		_uploadedItems.add(item);
	}

	void clearUploadedItems() {
		_uploadedItems.clear();
	}

	/**
	 * Command to trigger upload of dropped files.
	 *
	 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
	 */
	protected static class DropFile extends ControlCommand {
		/**
		 * ID of the command.
		 */
		protected static final String COMMAND_ID = "dropFile";

		/**
		 * Single instance of the {@link DropFile}.
		 */
		public static final ControlCommand INSTANCE = new DropFile();

		/**
		 * Creates a {@link DropFile} Command
		 */
		public DropFile() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			GalleryControl galleryControl = (GalleryControl) control;
			try {
				if (arguments.get(DnDFileUtilities.CLIENT_UPLOAD_FAILED_MESSAGE) != null) {
					DnDFileUtilities.showClientInfoMessages(arguments);
				} else {
					List<GalleryImage> galleryFiles = new ArrayList<>(galleryControl.getGalleryModel().getImages());
					List<String> galleryNames = new ArrayList<>();
					for (GalleryImage image : galleryFiles) {
						galleryNames.add(image.getName());
					}

					List<ResKey> infoMessages = new ArrayList<>();
					List<String> duplicateImages = new ArrayList<>();
					for (BinaryData binaryData : galleryControl.getUploadedItems()) {
						String fileName = binaryData.getName();
						if (galleryNames.contains(fileName)) {
							duplicateImages.add(fileName);
						} else {
							GalleryImage galleryImage;
							try {
								galleryImage = GalleryUploadListener.createGalleryImage(binaryData);
								galleryNames.add(fileName);
								galleryFiles.add(galleryImage);

							} catch (TopLogicException ex) {
								infoMessages.add(ex.getErrorKey());
							}
						}
					}

					addDuplicatesInfoMessage(infoMessages, duplicateImages);
					if (!infoMessages.isEmpty()) {
						DnDFileUtilities.showInfoMessage(infoMessages);
					}
					galleryControl.getGalleryModel().setImages(galleryFiles);
				}
			} finally {
				galleryControl.clearUploadedItems();
				DnDFileUtilities.hideProgressDialog(galleryControl.getURLContext(), galleryControl.getID());
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Creates an info message if duplicated images were detected.
		 * 
		 * <p>
		 * If there are multiple duplicates they will be represented in a comma separated String in
		 * one message.
		 * </p>
		 * 
		 * @param infoMessages
		 *        List of all info messages. The new message will be added to it.
		 * @param duplicateImages
		 *        List of image duplicates.
		 */
		private void addDuplicatesInfoMessage(List<ResKey> infoMessages, List<String> duplicateImages) {
			if (!duplicateImages.isEmpty()) {
				ResKey formattedMessage =
					I18NConstants.IMAGE_NAME_ALREADY_EXISTS.fill(String.join(", ", duplicateImages));
				infoMessages.add(formattedMessage);
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DROP_IMAGE_UPLOAD;
		}
	}

}
