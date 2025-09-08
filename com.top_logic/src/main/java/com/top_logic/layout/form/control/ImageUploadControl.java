/**
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.ReadOnlyListener;
import com.top_logic.layout.image.gallery.ImageDataUtil;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;

/**
 * Control for an image upload view of a {@link DataField} model.
 * 
 * <p>
 * An image upload can have . TODO
 * </p>
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class ImageUploadControl extends AbstractFormFieldControl implements ContentHandler, IDownloadControl,
		ReadOnlyListener {

	/**
	 * Uploaded Image.
	 */
	private BinaryDataSource _uploadedImage = null;

	/**
	 * Variable to store problems during "post" of the upload.
	 */
	final List<ResKey> _uploadErrors = new ArrayList<>();

	/** {@link IDBuilder} to get GUI identifier for the single data items. */
	private final IDBuilder _idBuilder = new IDBuilder();

	private final DeliverContentHandler imageHandler;

	private int _urlSuffix = 0;

	private static final Map<String, ? extends ControlCommand> DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD =
		createCommandMap(new ControlCommand[] {
			ImageUpdate.INSTANCE, UploadPerformedCommand.INSTANCE, ClearCommand.INSTANCE, FieldInspector.INSTANCE });
//			FileNameUpdate.INSTANCE, UploadPerformedCommand.INSTANCE, ClearCommand.INSTANCE, FieldInspector.INSTANCE });

	private static final Map<String, ? extends ControlCommand> DATA_ITEM_COMMANDS_WITH_DOWNLOAD =
		createCommandMap(DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD, new ControlCommand[] {
			DownloadCommand.INSTANCE });

	/**
	 * Creates a {@link ImageUploadControl}.
	 * 
	 * @param model
	 *        The {@link DataField} model.
	 */
	public ImageUploadControl(DataField model) {
		super(model, commands(model));
		this.imageHandler = new DeliverContentHandler();
		if (!model.getDataItems().isEmpty()) {
			imageHandler.setData(model.getDataItem());
		}
	}

	private static Map<String, ? extends ControlCommand> commands(DataField model) {
		return model.isDownload() ? DATA_ITEM_COMMANDS_WITH_DOWNLOAD : DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD;
	}

	@Override
	protected String getTypeCssClass() {
		return "cImgUpload";
	}
	
	/**
	 * type safe getter for the model of this control, i.e. actually calls
	 * {@link #getModel()}
	 * 
	 * @see #getModel()
	 */
	public final DataField getDataField() {
		return (DataField) getModel();
	}

	void resetTempData() {
		_uploadedImage = null;
		_uploadErrors.clear();
//		imageHandler.setData(_uploadedImage);
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		final DataField dataField = getDataField();
		final boolean disabled = dataField.isDisabled();
		final BinaryData image = dataField.getDataItem();

		// TODO: PDF ok? Bilder können ja auch als PDF gespeichert werden.
//		dataField.setAcceptedTypes("image/*,.pdf");
		dataField.addConstraint(new AbstractConstraint() {

			@Override
			public boolean check(Object value) throws CheckException {
				if (!isPictureOrNull(value)) {
					throw new CheckException(ImageDataUtil.noValidImageExtension());
				}
				return true;
			}
		});

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if (dataField.isReadOnly()) {
//				renderImageReadOnly(context, out, image);
			} else {
				if (image == null) {
					// in case there is currently no image in the field an
					// upload is rendered
					writeUploadButton(context, out);
				} else {
					renderImage(context, out, image);
				}
			}
		}
		out.endTag(DIV);
	}

	boolean isPictureOrNull(Object value) {
		BinaryData bdata = (BinaryData) value;

		return bdata == null || ImageDataUtil.isSupportedImageFilename(bdata.getName());
	}

	/**
	 * Writes the button for selecting files to upload.
	 */
	private void writeUploadButton(DisplayContext context, TagWriter out) throws IOException {
		/* add additional span to set control classes. needed to get mandatory style for example. */
		out.beginBeginTag(SPAN);
		writeControlClasses(out);
		out.endBeginTag();
		{
			writeUploadButtonLabel(context, out);

			out.beginBeginTag(INPUT);
			out.writeAttribute(TYPE_ATTR, FILE_TYPE_VALUE);
			out.writeAttribute(ID_ATTR, uploadId());
			out.writeAttribute(STYLE_ATTR, "display: none;");
			out.writeAttribute(CLASS_ATTR, FormConstants.IS_UPLOAD_CSS_CLASS);

			// Prevent a click that opens the file chooser to also select a table row (if displayed
			// in a table cell). Otherwise the row might get redrawn an the underlying control
			// removed. Note: This hack must be done at the label element forwarding the event to
			// the
			// file input and the file input itself to "really" prevent the event from bubbling.
			out.writeAttribute(ONCLICK_ATTR, "var e = BAL.getEvent(event); e.stopPropagation(); return true;");

			if (!getModel().isActive()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}
			writeOnFileInputChange(out);
			writeAcceptedFileTypes(out);
			out.endEmptyTag();
		}
		out.endTag(SPAN);
	}

	private void writeOnFileInputChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		out.append("services.form.ImageUploadControl.updateImage('");
		out.append(getID());
		out.append("', this.files)");
		out.endAttribute();
	}

	private void writeAcceptedFileTypes(TagWriter out) {
		String acceptedFileTypes = getDataField().getAcceptedTypes();
		if (!isEmpty(acceptedFileTypes)) {
			out.writeAttribute(ACCEPT_ATTR, acceptedFileTypes);
		}
	}

	private void writeUploadButtonLabel(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(LABEL);
		out.writeAttribute(FOR_ATTR, uploadId());

		// Prevent a click that opens the file chooser to also select a table row (if displayed
		// in a table cell). Otherwise the row might get redrawn an the underlying control
		// removed. Note: This hack must be done at the label element forwarding the event to the
		// file input and the file input itself to "really" prevent the event from bubbling.
		out.writeAttribute(ONCLICK_ATTR, "var e = BAL.getEvent(event); e.stopPropagation(); return true;");

		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
			context.getResources().getString(I18NConstants.UPLOAD_LABEL.tooltip()));
		if (getModel().isActive()) {
			out.writeAttribute(CLASS_ATTR,
				CssUtil.joinCssClasses(FormConstants.IS_UPLOAD_CSS_CLASS,
					ButtonComponentButtonRenderer.CSS_CLASS_ENABLED_BUTTON));
		} else {
			out.writeAttribute(CLASS_ATTR,
				CssUtil.joinCssClasses(FormConstants.IS_UPLOAD_CSS_CLASS,
					ButtonComponentButtonRenderer.CSS_CLASS_DISABLED_BUTTON));
		}
		out.endBeginTag();
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "centerLabel");
		out.endBeginTag();
		Icons.UPLOAD_ICON.writeWithCss(context, out, ButtonComponentButtonRenderer.CSS_CLASS_IMAGE);
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, ButtonComponentButtonRenderer.CSS_CLASS_LABEL);
		out.endBeginTag();
		String label = context.getResources().getString(I18NConstants.UPLOAD_LABEL);
		// TODO: anderes Label (z.B. "Bild hochladen")
		final BinaryData image = getDataField().getDataItem();
		if (image != null) {
			label = image.getName();
		}
		out.writeText(label);
		out.endTag(SPAN);
		out.endTag(SPAN);
		out.endTag(LABEL);
	}

	private void renderImage(DisplayContext context, TagWriter out, BinaryDataSource image) throws IOException {
		out.beginBeginTag(IMG);
		out.writeAttribute(ALT_ATTR, image.getName());
		out.writeAttribute(SRC_ATTR,
			getURLContext().getURL(context, imageHandler).appendParameter("itemVersion", _urlSuffix).getURL());
		out.endEmptyTag();
		renderClearImage(context, out, null, false);
	}

	/**
	 * Renders the image whose command {@link ClearCommand clears} the content of the field.
	 * 
	 * @param context
	 *        the context in which rendering occurs
	 * @param out
	 *        the stream to write to
	 * @param img
	 *        The image to replace the current one. If <code>null</code> just the current image is
	 *        cleared.
	 * @param disabled
	 *        whether the command should be disabled
	 * @throws IOException
	 *         iff the given {@link TagWriter} throws some
	 */
	void renderClearImage(DisplayContext context, TagWriter out, BinaryDataSource img, boolean disabled)
			throws IOException {
		ButtonWriter buttonWriter =
			new ButtonWriter(this, com.top_logic.layout.form.tag.Icons.DELETE_BUTTON,
				com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED, ClearCommand.INSTANCE);
		if (img != null) {
//			buttonWriter.setJSArguments();
		} else {
			buttonWriter.setID(getClearID());
		}
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);

		if (disabled) {
			buttonWriter.writeDisabledButton(context, out);
		} else {
			buttonWriter.writeButton(context, out);
		}
	}

//	private void renderImageReadOnly(DisplayContext context, TagWriter out, BinaryData image)
//			throws IOException {
//		renderImage(context, out, new DownloadImageRenderer(getDataField().isDownload()), image);
//	}

//	private void renderImage(DisplayContext context, TagWriter out, ControlRenderer<? super IDownloadControl> renderer,
//			BinaryDataSource image) throws IOException {
//		DownloadControl control = new DownloadControl(image);
//		control.setRenderer(renderer);
//		control.write(context, out);
//	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
//			renderImageReadOnly(context, out, getDataField().getDataItem());
		}
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (Utils.equals(oldValue, newValue)) {
			return;
		}
		requestRepaint();
	}

	private static String toFileName(String pathName) {
		// return new File(name).getName();

		for (int index = pathName.length() - 1; index >= 0; index--) {
			final char potentialSeparator = pathName.charAt(index);
			if ('/' == potentialSeparator || '\\' == potentialSeparator) {
				return pathName.substring(index + 1);
			}
		}
		return pathName;
	}

	@Override
	public Bubble handleReadOnlyChanged(DataField sender, Boolean wasReadOnly, Boolean isReadOnly) {
		return repaintOnEvent(sender);
	}

	@Override
	public BinaryDataSource dataItem() {
		return _uploadedImage;
	}

	@Override
	public ResKey noValueKey() {
		final ResKey noValueResKey = getDataField().get(DataField.NO_VALUE_I18N_KEY_PROPERTY);
		if (noValueResKey != null) {
			return noValueResKey;
		} else {
			return I18NConstants.NO_DOWNLOAD_AVAILABLE;
		}
	}

	@Override
	public boolean downloadInline() {
		return false;
	}

	private String uploadId() {
		return getID() + "-upload";
	}

	String getClearID() {
		return getID() + "-clear";
	}

	private URLBuilder uploadURL(DisplayContext context) {
		return getURLContext().getURL(context, this);
	}

	/**
	 * Command function invoked by the client, whenever files are selected.
	 * 
	 * @param fileName
	 *        the names of the files to upload.
	 * @param fileSize
	 *        The file sizes of the files with the corresponding names.
	 * 
	 * @see DataField#checkFileName(String)
	 */
	void notifyImageChanged(final String fileName, Number fileSize) {
		Function<String, ResKey> nameChecker = nameChecker();
		List<ResKey> errors = new ArrayList<>();
		boolean valid = false;

		// Check files.
		long maxUploadSize = getDataField().getMaxUploadSize();
		ResKey error = nameChecker.apply(fileName);
		if (error == null) {
			long size = fileSize.longValue();
			if (size == 0) {
				errors.add(I18NConstants.ERROR_UPLOAD_EMPTY_FILE__NAME.fill(fileName));
			} else {
				if (maxUploadSize > 0 && size > maxUploadSize) {
					errors.add(I18NConstants.ERROR_UPLOAD_SIZE_EXCEEDED__NAME_LIMIT.fill(fileName, maxUploadSize));
				} else {
					valid = true;
				}
			}
		} else {
			errors.add(error);
		}

		if (!errors.isEmpty()) {
			InfoService.showWarningList(errors);
		}

		// If everything is OK, trigger upload.
		if (valid) {
			addUpdate(new JSSnipplet(new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.ImageUploadControl.submit(");
					TagUtil.writeJsString(out, getID());
					out.append(",");
					TagUtil.writeJsString(out, uploadId());
					out.append(",");
					TagUtil.writeJsString(out, uploadURL(context).getURL());
					out.append(");");
				}
			}));
		}
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		final FrameScope urlContext = getURLContext();
		urlContext.registerContentHandler(null, this);
		urlContext.registerContentHandler(urlContext.createNewID(), imageHandler);
	}

	@Override
	protected void detachInvalidated() {
		resetTempData();
		_idBuilder.clear();
		super.detachInvalidated();
	}

	@Override
	protected void internalDetach() {
		getURLContext().deregisterContentHandler(this);
		getURLContext().deregisterContentHandler(imageHandler);
		// Control is detached
		super.internalDetach();
	}

	private FrameScope getURLContext() {
		return getScope().getFrameScope();
	}

	/**
	 * Performs the actual upload. It takes informations about the uploaded image from the given
	 * {@link HttpServletRequest} and stores it into the local variable {@link #_uploadedImage}.
	 * That item is stored into the field when the ajax callback is performed.
	 */
	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		final HttpServletRequest request = context.asRequest();

		uploadImage(nameChecker(), (Part) request.getParts().toArray()[0]);
	}

	private void uploadImage(Function<String, ResKey> nameChecker, Part image)
			throws IllegalArgumentException, IOException {
		final String name = image.getSubmittedFileName();
		assert name != null : "File must have a non-null name.";
		String fileName = toFileName(name);

		if (Logger.isDebugEnabled(DataItemControl.class)) {
			Logger.debug("File uploaded: " + fileName, DataItemControl.class);
		}

		// if (image.getSize() == 0 && getForbidEmptyFiles()) {
//			_uploadErrors.add(I18NConstants.ERROR_UPLOAD_EMPTY_FILE__NAME.fill(fileName));
//			return;
//		}

		long maxUploadSize = getDataField().getMaxUploadSize();
		if (maxUploadSize > 0 && image.getSize() > maxUploadSize) {
			_uploadErrors.add(I18NConstants.ERROR_UPLOAD_SIZE_EXCEEDED__NAME_LIMIT.fill(fileName, maxUploadSize));
			return;
		}

		ResKey error = nameChecker.apply(fileName);
		if (error != null) {
			_uploadErrors.add(error);
			return;
		}

		BinaryData data = BinaryDataFactory.createUploadData(image);
		String contentType = image.getContentType();

		if (BinaryDataSource.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			contentType = MimeTypes.getInstance().getMimeType(fileName);
		}

		_uploadedImage = new DefaultDataItem(fileName, data, contentType);
		imageHandler.setData(_uploadedImage);
		_urlSuffix++;
//		requestRepaint();
	}

	/**
	 * Callback invoked when the upload completes.
	 */
	final void uploadPerformed() {
		if (!_uploadErrors.isEmpty()) {
			InfoService.showWarningList(_uploadErrors);
		}

		if (_uploadedImage != null) {
			BinaryDataSource newImage = null;
			DataField fieldModel = getDataField();
			newImage = _uploadedImage;
			updateField(newImage);
		}

//		resetTempData();
	}

	private void updateField(BinaryDataSource newImage) {
		final FormField field = getFieldModel();

		try {
			FormFieldInternals.updateFieldNoClientUpdate(field, newImage);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					try {
						Object parsedValue =
							FormFieldInternals.parseRawValue((AbstractFormField) field, newImage);
						field.setValue(parsedValue);
					} catch (CheckException checkEx) {
						// Ignore
					}

					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(getWindowScope());
		}
		requestRepaint();
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	private Function<String, ResKey> nameChecker() {
		return (fileName) -> {
			try {
				getDataField().checkFileName(fileName);
			} catch (CheckException ex) {
				return ResKey.text(ex.getMessage());
			}
			return null;
		};
	}

	/**
	 * Simple abstract superclass of {@link ControlCommand}s used within a
	 * {@link ImageUploadControl}
	 */
	private static abstract class ImageUploadCommand extends ControlCommand {

		protected ImageUploadCommand(String id) {
			super(id);
		}

		@Override
		protected final HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			return exec(commandContext, (ImageUploadControl) control, arguments);
		}

		protected abstract HandlerResult exec(DisplayContext commandContext, ImageUploadControl control,
				Map<String, Object> arguments);

	}

	/**
	 * Command which is executed if a new file is selected on the client.
	 */
	private static class ImageUpdate extends ImageUploadCommand {

		private static final String FILENAME_ATTR = "value";

		private static final String FILESIZE_ATTR = "size";

		public static final ImageUpdate INSTANCE = new ImageUpdate("imageUpdate");

		protected ImageUpdate(String id) {
			super(id);
		}

		@Override
		protected HandlerResult exec(DisplayContext commandContext, ImageUploadControl control,
				Map<String, Object> arguments) {
			@SuppressWarnings("unchecked")
			final String fileName = (String) arguments.get(FILENAME_ATTR);
			@SuppressWarnings("unchecked")
			final Number fileSize = (Number) arguments.get(FILESIZE_ATTR);
			control.notifyImageChanged(fileName, fileSize);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_IMAGE;
		}
	}

	/**
	 * AJAX callback which is triggered when a file was successfully submitted to the server.
	 */
	private static class UploadPerformedCommand extends ImageUploadCommand {
		/**
		 * Singleton {@link DataItemControl.UploadPerformedCommand} instance.
		 */
		public static final UploadPerformedCommand INSTANCE = new UploadPerformedCommand();

		/**
		 * Creates a {@link UploadPerformedCommand}.
		 */
		protected UploadPerformedCommand() {
			super("uploadPerformed");
		}

		@Override
		protected HandlerResult exec(DisplayContext commandContext, ImageUploadControl control,
				Map<String, Object> arguments) {
			control.uploadPerformed();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPLOAD_COMPLETED_COMMAND;
		}
	}

	/**
	 * Command which is executed, either when the delete button is pressed or when a new image is
	 * uploaded while there is currently one.
	 */
	private static class ClearCommand extends ImageUploadCommand {

		public static final ClearCommand INSTANCE = new ClearCommand("clear");

		protected ClearCommand(String id) {
			super(id);
		}

		@Override
		protected HandlerResult exec(DisplayContext commandContext, ImageUploadControl control,
				Map<String, Object> arguments) {
			BinaryDataSource image = (BinaryDataSource) arguments.get("");
			if (image != null) {

			} else {
				// Clear of submit was clicked.
				control.updateField(null);
//				control.requestRepaint();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_IMAGE;
		}
	}

}
