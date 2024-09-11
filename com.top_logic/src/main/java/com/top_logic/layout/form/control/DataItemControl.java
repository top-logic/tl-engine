/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.json.JSON;
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
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.ReadOnlyListener;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link DataItemControl} is a standard representation of some
 * {@link DataField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataItemControl extends AbstractFormFieldControl implements ContentHandler, IDownloadControl,
		ReadOnlyListener {
	
	/**
	 * Configuration for {@link DataItemControl}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * @see #getForbidEmptyFiles()
		 */
		String FORBID_EMPTY_FILES = "forbidEmptyFiles";

		/**
		 * Whether the upload of empty files is forbidden.
		 * 
		 * @see DataItemControl#forbidEmptyFileUpload(boolean)
		 */
		@Name(FORBID_EMPTY_FILES)
		@BooleanDefault(false)
		boolean getForbidEmptyFiles();
	}

	private class DownloadAndClearImageRenderer extends DownloadImageRenderer {

		private boolean _disabled;

		public DownloadAndClearImageRenderer(boolean downloadAllowed, boolean disabled) {
			super(downloadAllowed);
			_disabled = disabled;
		}

		@Override
		public void renderDownloadImage(DisplayContext context, TagWriter out, IDownloadControl control,
				boolean disabled) throws IOException {
			super.renderDownloadImage(context, out, control, disabled);
			DataItemControl.this.renderClearImage(context, out, control.dataItem(), _disabled || disabled);
		}
	}

	private static final Map<String, ? extends ControlCommand> DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD =
		createCommandMap(new ControlCommand[] {
			FileNameUpdate.INSTANCE, UploadPerformedCommand.INSTANCE, ClearCommand.INSTANCE, FieldInspector.INSTANCE });

	private static final Map<String, ? extends ControlCommand> DATA_ITEM_COMMANDS_WITH_DOWNLOAD =
		createCommandMap(DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD, new ControlCommand[] {
			DownloadCommand.INSTANCE });

	/**
	 * css class of the control tag.
	 */
	private static final String DATA_ITEM_CSS_CLASS = "dataItem";

	/**
	 * Uploaded data. Variable is filled during "post" to the content handler
	 * and is dispatched to the model of this control when executing the ajax
	 * callback.
	 */
	final List<BinaryDataSource> _uploadedFiles = new ArrayList<>();

	/**
	 * variable to store problems during "post" of the upload
	 */
	final List<ResKey> _uploadErrors = new ArrayList<>();

	/** {@link IDBuilder} to get GUI identifier for the single data items. */
	private final IDBuilder _idBuilder = new IDBuilder();

	/**
	 * whether the download should be displayed inline
	 * 
	 * @see #downloadInline(boolean)
	 */
	private boolean _downloadInline = false;

	/**
	 * Whether uploading of empty files is forbidden.
	 * 
	 * @see #forbidEmptyFileUpload(boolean)
	 */
	private Boolean _forbidEmptyFiles = null;

	/**
	 * Creates a new {@link DataItemControl} with the given {@link DataField} as model.
	 * 
	 * @see AbstractFormFieldControl#AbstractFormFieldControl(FormField, Map)
	 */
	public DataItemControl(DataField model) {
		super(model, commands(model));
	}

	private static Map<String, ? extends ControlCommand> commands(DataField model) {
		return model.isDownload() ? DATA_ITEM_COMMANDS_WITH_DOWNLOAD : DATA_ITEM_COMMANDS_WITHOUT_DOWNLOAD;
	}

	void resetTempData() {
		_uploadedFiles.clear();
		_uploadErrors.clear();
	}

	/**
	 * Whether the download should be offered for displaying the content inline in a new browser
	 * window. Default is <code>false</code>.
	 * 
	 * @param inline
	 *        Whether the download should be displayed inline. If <code>false</code>, the contents
	 *        is offered for download.
	 * 
	 * @return a reference to this {@link DataItemControl}
	 * 
	 * @see WindowScope#deliverContent(BinaryDataSource, boolean)
	 */
	public DataItemControl downloadInline(boolean inline) {
		_downloadInline = inline;
		return this;
	}

	/**
	 * Whether uploading of files with size 0 is prevented.
	 * 
	 * <p>
	 * The default value is configured application-wide, see {@link Config#getForbidEmptyFiles()}.
	 * </p>
	 * 
	 * @param value
	 *        if <code>true</code> then uploading empty files results in an error message.
	 * 
	 * @return a reference to this {@link DataItemControl}
	 */
	public DataItemControl forbidEmptyFileUpload(boolean value) {
		_forbidEmptyFiles = value;
		return this;
	}

	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(DataField.READ_ONLY_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(DataField.READ_ONLY_PROPERTY, this);
		super.deregisterListener(member);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (Utils.equals(normalize(oldValue), normalize(newValue))) {
			return;
		}
		requestRepaint();
	}

	private Object normalize(Object oldValue) {
		if (oldValue instanceof List<?>) {
			List<?> oldListValue = (List<?>) oldValue;
			switch (oldListValue.size()) {
				case 0:
					return null;
				case 1:
					return oldListValue.get(0);
				default:
					return oldListValue;
			}
		}
		return oldValue;
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	/**
	 * Determines whether an upload field should be offered
	 * 
	 * @return <code>true</code> iff an upload field should be shown.
	 */
	private boolean showUpload() {
		final DataField dataField = getDataField();
		return dataField.getDataItems().isEmpty() && !dataField.isReadOnly();
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			renderItemsReadOnly(context, out, getDataField().getDataItems());
		}
		out.endTag(SPAN);
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
	protected String getTypeCssClass() {
		return "cDataItem";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		final DataField dataField = getDataField();
		final boolean disabled = dataField.isDisabled();
		final List<BinaryData> items = dataField.getDataItems();

		String tagName = items.size() > 1 ? DIV : SPAN;

		out.beginBeginTag(tagName);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if (dataField.isReadOnly()) {
				renderItemsReadOnly(context, out, items);
			} else {
				ControlRenderer<? super IDownloadControl> renderer =
					new DownloadAndClearImageRenderer(getDataField().isDownload(), disabled);
				renderItems(context, out, renderer, items);

				if (items.isEmpty() || getDataField().isMultiple()) {
					// in case there is currently no data item in the field an
					// upload is rendered
					writeUploadButton(context, out);
				}
			}
		}
		out.endTag(tagName);
	}

	private void renderItemsReadOnly(DisplayContext context, TagWriter out, List<BinaryData> items)
			throws IOException {
		renderItems(context, out, new DownloadImageRenderer(getDataField().isDownload()), items);
	}

	private void renderItems(DisplayContext context, TagWriter out, ControlRenderer<? super IDownloadControl> renderer,
			List<BinaryData> items) throws IOException {
		if (items.size() > 1) {
			out.beginTag(UL);
			for (BinaryDataSource item : items) {
				out.beginTag(LI);
				renderItem(context, out, renderer, item);
				out.endTag(LI);
			}
			out.endTag(UL);
		} else {
			for (BinaryDataSource item : items) {
				renderItem(context, out, renderer, item);
			}
		}
	}

	private void renderItem(DisplayContext context, TagWriter out, ControlRenderer<? super IDownloadControl> renderer,
			BinaryDataSource item) throws IOException {
		DownloadControl control = new DownloadControl(item);
		control.setRenderer(renderer);
		control.write(context, out);
	}

	void renderClearSubmitImage(DisplayContext context, TagWriter out, boolean disabled) throws IOException {
		renderClearImage(context, out, null, disabled);
	}

	/**
	 * Renders the image whose command {@link ClearCommand clears} the content of the field.
	 * 
	 * @param context
	 *        the context in which rendering occurs
	 * @param out
	 *        the stream to write to
	 * @param item
	 *        The item to remove. If <code>null</code> all elements are cleared.
	 * @param disabled
	 *        whether the command should be disabled
	 * @throws IOException
	 *         iff the given {@link TagWriter} throws some
	 */
	void renderClearImage(DisplayContext context, TagWriter out, BinaryDataSource item, boolean disabled)
			throws IOException {
		ButtonWriter buttonWriter =
			new ButtonWriter(this, com.top_logic.layout.form.tag.Icons.DELETE_BUTTON, com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED, ClearCommand.INSTANCE);
		if (item != null) {
			String itemID = _idBuilder.makeId(item);
			buttonWriter.setJSArguments(new JSObject(ClearCommand.ITEM_IDENTIFIER, new JSString(itemID)));
		} else {
			buttonWriter.setID(getClearChooserID());
		}
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);

		if (disabled) {
			buttonWriter.writeDisabledButton(context, out);
		} else {
			buttonWriter.writeButton(context, out);
		}
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(DATA_ITEM_CSS_CLASS);
	}

	/**
	 * Returns the client side id of the element which clears the file chooser
	 * (in case a upload is rendered), and the id of the element which removes
	 * the currently shown file from the server (in case a download is
	 * rendered).
	 */
	String getClearChooserID() {
		return getID() + "-clear";
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		final FrameScope urlContext = getScope().getFrameScope();
		urlContext.registerContentHandler(null, this);
	}

	@Override
	protected void detachInvalidated() {
		resetTempData();
		_idBuilder.clear();
		super.detachInvalidated();
	}

	@Override
	protected void internalDetach() {
		getScope().getFrameScope().deregisterContentHandler(this);
		// Control is detached
		super.internalDetach();
	}

	/**
	 * Performs the actual upload. It takes informations about the uploaded file from the given
	 * {@link HttpServletRequest} and stored it into the local variable {@link #_uploadedFiles}.
	 * That item is stored into the field when the ajax callback is performed.
	 */
	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		final HttpServletRequest request = context.asRequest();

		uploadMulti(nameChecker(), request.getParts());
	}

	private void uploadMulti(Function<String, ResKey> nameChecker, Collection<Part> parts)
			throws IllegalArgumentException, IOException {
		for (Part file : parts) {
			uploadSingle(nameChecker, file);
		}
	}

	private void uploadSingle(Function<String, ResKey> nameChecker, Part file)
			throws IllegalArgumentException, IOException {
		final String name = file.getSubmittedFileName();
		assert name != null : "File must have a non-null name.";
		String fileName = toFileName(name);

		if (Logger.isDebugEnabled(DataItemControl.class)) {
			Logger.debug("File uploaded: " + fileName, DataItemControl.class);
		}

		if (file.getSize() == 0 && getForbidEmptyFiles()) {
			_uploadErrors.add(I18NConstants.ERROR_UPLOAD_EMPTY_FILE__NAME.fill(fileName));
			return;
		}

		long maxUploadSize = getDataField().getMaxUploadSize();
		if (maxUploadSize > 0 && file.getSize() > maxUploadSize) {
			_uploadErrors.add(I18NConstants.ERROR_UPLOAD_SIZE_EXCEEDED__NAME_LIMIT.fill(fileName, maxUploadSize));
			return;
		}

		ResKey error = nameChecker.apply(fileName);
		if (error != null) {
			_uploadErrors.add(error);
			return;
		}

		BinaryData data = BinaryDataFactory.createUploadData(file);
		String contentType = file.getContentType();

		if (BinaryDataSource.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			contentType = MimeTypes.getInstance().getMimeType(fileName);
		}

		_uploadedFiles.add(new DefaultDataItem(fileName, data, contentType));
	}

	private static String toFileName(String pathName) {
		/* Does not work if system runs on UNIX and file is uploaded using IE under Windows: IE
		 * sends the complete path as filename (e.g. C:\foo\bar.java). UnixFileSystem does not
		 * interpret '\' as separator so the filename is the complete path ("C:\foo\bar.java").
		 * 
		 * see #4544. */
		// return new File(name).getName();

		for (int index = pathName.length() - 1; index >= 0; index--) {
			final char potentialSeparator = pathName.charAt(index);
			if ('/' == potentialSeparator || '\\' == potentialSeparator) {
				return pathName.substring(index + 1);
			}
		}
		return pathName;
	}

	/**
	 * Callback invoked when the upload completes.
	 */
	final void uploadPerformed() {
		if (!_uploadErrors.isEmpty()) {
			InfoService.showWarningList(_uploadErrors);
		}

		if (!_uploadedFiles.isEmpty()) {
			List<BinaryDataSource> newFiles = new ArrayList<>();
			DataField fieldModel = getDataField();
			if (fieldModel.isMultiple()) {
				newFiles.addAll(fieldModel.getDataItems());
			}
			newFiles.addAll(_uploadedFiles);
			updateField(newFiles);
		}

		resetTempData();
	}

	/**
	 * Removes the file with the given ID.
	 * 
	 * <p>
	 * Triggered from the UI, when the remove button next to a file name is clicked.
	 * </p>
	 */
	final void removeItem(String itemID) {
		Object dataItem = _idBuilder.getObjectById(itemID);
		if (dataItem == null) {
			throw new TopLogicException(I18NConstants.DATA_ITEM_NOT_FOUND__ITEM_ID.fill(itemID));
		}
	
		List<BinaryDataSource> newItems = new ArrayList<>(getDataField().getDataItems());
		newItems.remove(dataItem);
		updateField(newItems);
	}

	private void updateField(List<BinaryDataSource> newFiles) {
		final FormField field = getFieldModel();
	
		try {
			FormFieldInternals.updateFieldNoClientUpdate(field, newFiles);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {
	
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					try {
						Object parsedValue =
							FormFieldInternals.parseRawValue((AbstractFormField) field, newFiles);
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
			if (this.getDataField().isMultiple()) {
				out.writeAttribute(MULTIPLE_ATTR, MULTIPLE_ATTR);
			}
			out.endEmptyTag();
		}
		out.endTag(SPAN);
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
		Icons.UPLOAD_ICON.writeWithCss(context, out, ButtonComponentButtonRenderer.CSS_CLASS_IMAGE);
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, ButtonComponentButtonRenderer.CSS_CLASS_LABEL);
		out.endBeginTag();
		out.writeText(context.getResources().getString(I18NConstants.UPLOAD_LABEL));
		out.endTag(SPAN);
		out.endTag(LABEL);
	}

	private void writeAcceptedFileTypes(TagWriter out) {
		String acceptedFileTypes = getDataField().getAcceptedTypes();
		if (!isEmpty(acceptedFileTypes)) {
			out.writeAttribute(ACCEPT_ATTR, acceptedFileTypes);
		}
	}

	private void writeOnFileInputChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		out.append("services.form.DataItemControl.fileNameUpdate('");
		out.append(getID());
		out.append("', this.files)");
		out.endAttribute();
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

	/**
	 * Command function invoked by the client, whenever files are selected.
	 * 
	 * @param fileNames
	 *        the names of the files to upload.
	 * @param fileSizes
	 *        The file sizes of the files with the corresponding names.
	 * 
	 * @see DataField#checkFileName(String)
	 */
	void notifyFilesChanged(final List<String> fileNames, List<Number> fileSizes) {
		Function<String, ResKey> nameChecker = nameChecker();
		int fileId = 0;
		List<Integer> validFileIds = new ArrayList<>();
		List<ResKey> errors = new ArrayList<>();

		// Check files.
		long maxUploadSize = getDataField().getMaxUploadSize();
		for (int n = 0, cnt = fileNames.size(); n < cnt; n++) {
			String fileName = fileNames.get(n);
			ResKey error = nameChecker.apply(fileName);
			if (error == null) {
				long size = fileSizes.get(n).longValue();
				if (getForbidEmptyFiles() && size == 0) {
					errors.add(I18NConstants.ERROR_UPLOAD_EMPTY_FILE__NAME.fill(fileName));
				} else {
					if (maxUploadSize > 0 && size > maxUploadSize) {
						errors.add(I18NConstants.ERROR_UPLOAD_SIZE_EXCEEDED__NAME_LIMIT.fill(fileName, maxUploadSize));
					} else {
						validFileIds.add(Integer.valueOf(fileId));
					}
				}
			} else {
				errors.add(error);
			}

			fileId++;
		}

		if (!errors.isEmpty()) {
			InfoService.showWarningList(errors);
		}

		// If everything is OK, trigger upload.
		if (!validFileIds.isEmpty()) {
			addUpdate(new JSSnipplet(new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.DataItemControl.submit(");
					TagUtil.writeJsString(out, getID());
					out.append(",");
					TagUtil.writeJsString(out, uploadId());
					out.append(",");
					TagUtil.writeJsString(out, uploadURL(context).getURL());
					out.append(",");
					JSON.write(out, validFileIds);
					out.append(");");
				}
			}));
		}
	}

	private Function<String, ResKey> nameChecker() {
		DataField fieldModel = getDataField();

		Set<String> existing;
		if (fieldModel.isMultiple()) {
			existing = fieldModel.getDataItems().stream().map(data -> data.getName()).collect(Collectors.toSet());
		} else {
			existing = Collections.emptySet();
		}

		return (fileName) -> {
			if (existing.contains(fileName)) {
				return I18NConstants.ERROR_UPLOAD_DUPLICATE_NAME__NAME.fill(fileName);
			}
			try {
				getDataField().checkFileName(fileName);
			} catch (CheckException ex) {
				return ResKey.text(ex.getMessage());
			}
			return null;
		};
	}

	private String uploadId() {
		return getID() + "-upload";
	}

	private URLBuilder uploadURL(DisplayContext context) {
		return getScope().getFrameScope().getURL(context, this);
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (showUpload()) {
			return repaintOnEvent(sender);
		} else {
			return super.handleMandatoryChanged(sender, oldValue, newValue);
		}
	}

	@Override
	public Bubble handleReadOnlyChanged(DataField sender, Boolean wasReadOnly, Boolean isReadOnly) {
		return repaintOnEvent(sender);
	}

	/**
	 * Simple abstract superclass of {@link ControlCommand}s used within a
	 * {@link DataItemControl}
	 */
	private static abstract class DataItemCommand extends ControlCommand {

		protected DataItemCommand(String id) {
			super(id);
		}

		@Override
		protected final HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			return exec(commandContext, (DataItemControl) control, arguments);
		}

		protected abstract HandlerResult exec(DisplayContext commandContext, DataItemControl control,
				Map<String, Object> arguments);

	}

	/**
	 * Command which is executed if a new file is selected on the client.
	 */
	private static class FileNameUpdate extends DataItemCommand {

		private static final String FILENAME_ATTR = "value";

		private static final String FILESIZE_ATTR = "size";

		public static final FileNameUpdate INSTANCE = new FileNameUpdate("fileNameUpdate");

		protected FileNameUpdate(String id) {
			super(id);
		}

		@Override
		protected HandlerResult exec(DisplayContext commandContext, DataItemControl control,
				Map<String, Object> arguments) {
			@SuppressWarnings("unchecked")
			final List<String> fileNames = (List<String>) arguments.get(FILENAME_ATTR);
			@SuppressWarnings("unchecked")
			final List<Number> fileSizes = (List<Number>) arguments.get(FILESIZE_ATTR);
			control.notifyFilesChanged(fileNames, fileSizes);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_FILE_NAME;
		}
	}

	/**
	 * Either clears the input field to load a file to the server (in case an
	 * upload is demanded) or removes the file currently stored in the
	 * {@link DataField#getDataItem() data item} of the field.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class ClearCommand extends DataItemCommand {

		public static final String ITEM_IDENTIFIER = "item";

		public static final ClearCommand INSTANCE = new ClearCommand("clear");

		protected ClearCommand(String id) {
			super(id);
		}

		@Override
		protected HandlerResult exec(DisplayContext commandContext, DataItemControl control,
				Map<String, Object> arguments) {
			String itemID = (String) arguments.get(ITEM_IDENTIFIER);
			if (itemID != null) {
				control.removeItem(itemID);
			} else {
				// Clear of submit was clicked.
				control.requestRepaint();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_DATA_ITEM;
		}
	}

	@Override
	public boolean downloadInline() {
		return _downloadInline;
	}

	@Override
	public BinaryDataSource dataItem() {
		DataField theField = getDataField();

		return theField.isMultiple() ? null : theField.getDataItem();
	}

	/**
	 * AJAX callback which is triggered when a file was successfully submitted
	 * to the server.
	 */
	private static class UploadPerformedCommand extends DataItemCommand {
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
		protected HandlerResult exec(DisplayContext commandContext, DataItemControl control,
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
	 * Getter for the configuration.
	 */
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Gets {@link Config#FORBID_EMPTY_FILES}, but also respects changes made by
	 * {@link #forbidEmptyFileUpload}.
	 */
	public boolean getForbidEmptyFiles() {
		if (_forbidEmptyFiles == null) {
			return getConfig().getForbidEmptyFiles();
		}
		return _forbidEmptyFiles.booleanValue();
	}

}
