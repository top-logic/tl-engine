/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import static com.top_logic.mig.html.HTMLConstants.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;


/**
 * Utilities for implementing drag'n drop of files.
 *
 * @author <a href=mailto:Diana.Pankratz@top-logic.com>dpa</a>
 */
public class DnDFileUtilities {

	/**
	 * The div of the window where a file can be dropped. Gets visible if the user drags a file over
	 * it. Contains the {@link #DROP_TEXT}.
	 */
	private static final String DROP_AREA = "-dropArea";

	/** Text that will be shown if the user drags a file over the {@link #DROP_AREA} */
	private static final String DROP_TEXT = "dropText";

	/** Div laying over the {@link #DROP_AREA} and the {@link #DROP_TEXT} to handle the file drop */
	private static final String DROP_FRAME = "-dropFrame";

	/** Div showing a dialog with the progress of the upload */
	private static final String PROGRESS_DIALOG = "dndProgressDialog";

	/** Wrapper div of the content of {@link #PROGRESS_DIALOG} */
	private static final String UPLOAD_DIALOG_CONTENT = "dndUploadDialogContent";

	/** Text shown in the {@link #PROGRESS_DIALOG} during an upload */
	private static final String UPLOAD_PROGRESS_TEXT = "dndUploadProgressText";

	/** Div containing the frame of the progress bar to show the progress of the upload */
	private static final String PROGRESS_BAR_FRAME = "dndProgressBarFrame";

	/** The progress bar to fill */
	private static final String PROGRESS_BAR = "dndProgressBar";

	/**
	 * The bar showing the progress and filling {@link #PROGRESS_BAR}. The width depends on the
	 * processed data
	 */
	private static final String PROGRESS = "dndProgress";

	/** Div containing the animation of the loader */
	private static final String LOADER = "dndLoader";

	/** Div containing the rotating loader box for loading animations */
	private static final String LOADER_BOX = "dndLoaderBox";

	/** Div containing the shadow of {@link #LOADER_BOX} */
	private static final String LOADER_SHADOW = "dndLoaderShadow";

	/**
	 * Info message containing the key to determine the reason of a failed upload on client side.
	 * 
	 * @see #FOLDER_DROP_NOT_SUPPORTED
	 * @see #MAX_SIZE_EXCEEDED
	 */
	final public static String CLIENT_UPLOAD_FAILED_MESSAGE = "message";

	/**
	 * Key to determine the info message to show if the client failed an upload because of unsupported folder uploads.
	 * 
	 * <p>
	 * This key is also used in /com.top_logic/webapp/script/tl/DragAndDropFile.js.
	 * </p>
	 */
	final public static String FOLDER_DROP_NOT_SUPPORTED = "folderDropNotSupported";

	/**
	 * Key to determine the info message to show if the client failed an upload because the size of
	 * uploaded files exceeded the configured maximum size.
	 * 
	 * <p>
	 * This key is also used in /com.top_logic/webapp/script/tl/DragAndDropFile.js. The allowed
	 * maximum size will be added to this key by the client. It will be separated by ":".
	 * </p>
	 */
	final public static String MAX_SIZE_EXCEEDED = "maxSizeExceeded";

	/**
	 * For implementing {@link ContentHandler} in a {@link Control} that can receive file drops.
	 * 
	 * @param context
	 *        Is not allowed to be null.
	 * @param uploadHandler
	 *        Is not allowed to be null.
	 */
	public static void handleContent(DisplayContext context, Consumer<HttpServletRequest> uploadHandler) {
		final HttpServletRequest request = context.asRequest();
		uploadHandler.accept(request);
		final HttpServletResponse response = context.asResponse();
		response.setContentType(CONTENT_TYPE_TEXT_HTML_UTF_8);
	}

	/**
	 * Extracts the file name of a {@link Part} and cuts out any path names.
	 * 
	 * @param fileItem
	 *        The file to get the name of.
	 * @return file name
	 */
	public static String getFileName(Part fileItem) {
		int splitNameIndex = fileItem.getName().lastIndexOf(':');
		String fileName =
			(splitNameIndex != -1) ? fileItem.getName().substring(splitNameIndex + 1) : fileItem.getName();
		assert fileName != null : "File must have a non-null name.";
		return fileName;
	}

	/**
	 * Determines the content type (mime type) of the {@link Part}.
	 * 
	 * @param fileItem
	 *        Content type of this file will be determined.
	 * @param fileName
	 *        Name of the file.
	 * @return content type of the file
	 */
	public static String getContentType(Part fileItem, String fileName) {
		String contentType = fileItem.getContentType();
		if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			contentType = MimeTypes.getInstance().getMimeType(fileName);
		}
		return contentType;
	}

	/**
	 * Checks if the amount of uploaded data is higher than the configured maximum upload size.
	 * 
	 * @param droppedElements
	 *        All elements dropped in the control to upload them. The keys are the folder names (if
	 *        a folder was dropped). The values are the files of the folder in a {@link List}.
	 * @param maxUploadSize
	 *        The maximum size (bytes) of files that can be uploaded with one drag and drop action.
	 * 
	 * @return True if the amount was to high.
	 */
	public static boolean exceededUploadSize(Map<String, List<BinaryData>> droppedElements, long maxUploadSize) {
		if (maxUploadSize == 0) {
			// upload is unlimited
			return false;
		}

		long size = 0;
		// Iterate through folder
		for (String element : droppedElements.keySet()) {
			List<BinaryData> folderFiles = droppedElements.get(element);
			// Iterate through files of folder
			for (BinaryData binaryData : folderFiles) {
				size += binaryData.getSize();
				if (size > maxUploadSize) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates an info message if the maximum upload size was exceeded.
	 * 
	 * @param maxUploadSize
	 *        The maximum size (bytes) of files that can be uploaded with one drag and drop action.
	 */
	public static void showUploadSizeExceededMessage(long maxUploadSize) {
		String memorySize = MemorySizeFormat.INSTANCE.getSpecification(maxUploadSize);
		ResKey message = I18NConstants.UPLOAD_FAILED_MAX_SIZE_EXCEEDED.fill(memorySize);
		showInfoMessage(Collections.singletonList(message));
	}

	/**
	 * Initializes the JS to handle file uploads with drag and drop.
	 * 
	 * @param context
	 *        The current rendering context. Is not allowed to be null.
	 * @param out
	 *        The stream the output is written to. Is not allowed to be null.
	 * @param control
	 *        The control receiving the drop. Is not allowed to be null.
	 * @param contentHandler
	 *        The {@link ContentHandler} which will receiving the drop. Is not allowed to be null.
	 * @param maxUploadSize
	 * 		  The maximum size (bytes) of files that can be uploaded with one drag and drop action. 
	 */
	public static void initFileDragAndDrop(DisplayContext context, TagWriter out, AbstractControlBase control,
			ContentHandler contentHandler, boolean uploadPossible, long maxUploadSize) throws IOException {
		requireNonNull(contentHandler);
		FrameScope urlContext = control.getScope().getFrameScope();
		URLBuilder url =
			urlContext.getURL(context, contentHandler);
		String dropAreaUploadAllowed =
			HTMLUtil.encodeJS(Resources.getInstance().getString(I18NConstants.DROP_AREA_UPLOAD_ALLOWED));
		String dropAreaUploadNotAllowed =
			HTMLUtil.encodeJS(Resources.getInstance().getString(I18NConstants.DROP_AREA_UPLOAD_NOT_ALLOWED));
		String fileDropLoadingText =
			HTMLUtil.encodeJS(Resources.getInstance().getString(I18NConstants.FILE_DROP_LOADING_TEXT));
		String fileDropProgressbarText =
			HTMLUtil.encodeJS(Resources.getInstance().getString(I18NConstants.FILE_DROP_PROGRESSBAR_TEXT));

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("DragAndDropFile.init('");
		out.append(control.getID());
		out.append("', '");
		out.append(url.getURL());
		out.append("', '");
		out.append(Boolean.toString(uploadPossible));
		out.append("', '");
		out.append(Long.toString(maxUploadSize));
		out.append("', '");
		out.append(dropAreaUploadAllowed);
		out.append("', '");
		out.append(dropAreaUploadNotAllowed);
		out.append("', '");
		out.append(fileDropLoadingText);
		out.append("', '");
		out.append(fileDropProgressbarText);
		out.append("')");
		HTMLUtil.endScriptAfterRendering(out);
	}

	/**
	 * Creates a visual drop area and a progressbar for file drop uploads.
	 * 
	 * @param out
	 *        {@link TagWriter} to write to.
	 * @param controlId
	 *        ID of the control that handles the file drop.
	 */
	public static void writeDropArea(TagWriter out, String controlId) {
		// DropArea
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + DROP_AREA);
		out.endBeginTag();
		out.beginBeginTag(H1);
		out.writeAttribute(ID_ATTR, controlId + "-" + DROP_TEXT);
		out.writeAttribute(CLASS_ATTR, DROP_TEXT);
		out.endBeginTag();
		out.endTag(H1);
		out.endTag(DIV);

		// DropFrame
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + DROP_FRAME);
		out.endBeginTag();
		out.endTag(DIV);

		// ProgressDialog
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + PROGRESS_DIALOG);
		out.writeAttribute(CLASS_ATTR, PROGRESS_DIALOG);
		out.endBeginTag();

		// UploadDialogContent
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + UPLOAD_DIALOG_CONTENT);
		out.writeAttribute(CLASS_ATTR, UPLOAD_DIALOG_CONTENT);
		out.endBeginTag();

		// UploadProgressText
		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(ID_ATTR, controlId + "-" + UPLOAD_PROGRESS_TEXT);
		out.writeAttribute(CLASS_ATTR, UPLOAD_PROGRESS_TEXT);
		out.endBeginTag();
		out.endTag(PARAGRAPH);

		// ProgressBarFrame
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + PROGRESS_BAR_FRAME);
		out.writeAttribute(CLASS_ATTR, PROGRESS_BAR_FRAME);
		out.endBeginTag();

		// ProgressBar
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + PROGRESS_BAR);
		out.writeAttribute(CLASS_ATTR, PROGRESS_BAR);
		out.endBeginTag();

		// Progress
		out.beginBeginTag(SPAN);
		out.writeAttribute(ID_ATTR, controlId + "-" + PROGRESS);
		out.writeAttribute(CLASS_ATTR, PROGRESS);
		out.endBeginTag();
		out.endTag(SPAN);

		// end of ProgressBar
		out.endTag(DIV);

		// end of ProgressBarFrame
		out.endTag(DIV);

		// ProgressLoader
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + LOADER);
		out.writeAttribute(CLASS_ATTR, LOADER);
		out.endBeginTag();

		// LoaderShadow
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + LOADER_SHADOW);
		out.writeAttribute(CLASS_ATTR, LOADER_SHADOW);
		out.endBeginTag();
		out.endTag(DIV);

		// LoaderBox
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, controlId + "-" + LOADER_BOX);
		out.writeAttribute(CLASS_ATTR, LOADER_BOX);
		out.endBeginTag();
		out.endTag(DIV);

		// end of ProgressLoader
		out.endTag(DIV);
		// end of UploadDialogContent
		out.endTag(DIV);
		// end of ProgressDialog
		out.endTag(DIV);
	}

	/**
	 * Calls a JS method to close the progress dialog if it was opened during the upload.
	 * 
	 * @param frameScope
	 *        {@link FrameScope} to add the {@link ClientAction} to.
	 * @param controlId
	 *        Control that handles the upload.
	 */
	public static void hideProgressDialog(FrameScope frameScope, String controlId) {
		frameScope.addClientAction(new JSSnipplet(new AbstractDisplayValue() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("DragAndDropFile.hideProgressDialog('");
				out.append(controlId);
				out.append("');");
			}
		}));

	}
	
	/**
	 * Displays info messages of failed uploads.
	 * 
	 * @param infoMessages
	 *        {@link List} of all messages. Must not be <code>null</code>.
	 */
	public static void showInfoMessage(List<ResKey> infoMessages) {
		InfoService.showInfoList(I18NConstants.INFO_UPLOAD_FAILED_DESCRIPTION, infoMessages);
	}

	/**
	 * If the upload failed on client side an info message will be shown and the upload will be
	 * canceled.
	 * 
	 * <p>
	 * The reason of the failed upload can be accessed by the key
	 * {@link #CLIENT_UPLOAD_FAILED_MESSAGE}. If there are additional arguments they are separated
	 * by ":" in the key.
	 * </p>
	 * 
	 * @param arguments
	 *        The maximum size (bytes) of files that can be uploaded with one drag and drop action.
	 */
	public static void showClientInfoMessages(Map<String, Object> arguments) {
		String message = String.valueOf(arguments.get(CLIENT_UPLOAD_FAILED_MESSAGE));
		String[] messageArguments = message.split(":");
		switch (messageArguments[0]) {
			case FOLDER_DROP_NOT_SUPPORTED:
				showInfoMessage(Collections.singletonList(I18NConstants.UPLOAD_FAILED_FOLDER_DROP_NOT_SUPPORTED));
				break;
			case MAX_SIZE_EXCEEDED:
				if (messageArguments.length > 1) {
					showUploadSizeExceededMessage(Long.valueOf(messageArguments[1]));
				} else {
					showUploadSizeExceededMessage(0);
				}
				break;
			default:
				throw new RuntimeException("Can't resolve message key '" + message + "'");
		}
	}


}
