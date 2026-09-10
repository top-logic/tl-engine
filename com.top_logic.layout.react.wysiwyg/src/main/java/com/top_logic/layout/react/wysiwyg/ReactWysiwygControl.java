/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * React WYSIWYG editor control for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * Renders the {@code TLWysiwygEditor} React component backed by {@code TipTap}. Converts between
 * {@link StructuredText} (server model) and HTML strings (client). Serves embedded images via
 * {@link DataProvider} and accepts image uploads via {@link UploadHandler}.
 * </p>
 *
 * <p>
 * Where an {@link ObjectLinkConfig} says which dialog the object is picked in, the editor offers
 * a button inserting a link to an application object. The picked object arrives on that
 * configuration's {@link ObjectLinkConfig#getResultChannel() result channel}, and the link
 * leading to it is inserted at the cursor.
 * </p>
 *
 * <p>
 * Image URLs are rewritten when sending HTML to the client: bare filenames in
 * {@code <img src="file.png">} become full download URLs
 * {@code <img src="/react-api/data?controlId=...&key=file.png">}. The reverse transformation is
 * applied when receiving HTML back from the client.
 * </p>
 */
public class ReactWysiwygControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	/**
	 * Command sent when the user follows an {@link TLObjectLinkUtil#TL_OBJECT object link} in
	 * displayed content.
	 */
	private static final String CMD_SHOW_OBJECT_LINK = "showObjectLink";

	/**
	 * The {@link #CMD_SHOW_OBJECT_LINK} argument naming the object to display.
	 */
	private static final String ARG_HREF = "href";

	/**
	 * Command sent when the user asks for a link to an application object.
	 *
	 * <p>
	 * Takes no arguments: what is picked and where is decided by the {@link ObjectLinkConfig} the
	 * editor was created with, and the picked object arrives on that configuration's
	 * {@link ObjectLinkConfig#getResultChannel() result channel}, not through the command.
	 * </p>
	 */
	private static final String CMD_INSERT_OBJECT_LINK = "insertObjectLink";

	/**
	 * State offering the button that sends {@link #CMD_INSERT_OBJECT_LINK}, an object holding
	 * {@link #BUTTON_LABEL} and {@link #BUTTON_ICON}.
	 *
	 * <p>
	 * Absent while the editor has no {@link ObjectLinkConfig}, which is how the client knows not
	 * to offer the button at all.
	 * </p>
	 */
	private static final String OBJECT_LINK = "objectLink";

	/** Field of {@link #OBJECT_LINK} holding the text of the button. */
	private static final String BUTTON_LABEL = "label";

	/** Field of {@link #OBJECT_LINK} holding the icon class of the button. */
	private static final String BUTTON_ICON = "icon";

	/** The icon of the {@link #OBJECT_LINK} button. */
	private static final String OBJECT_LINK_ICON = "ri-links-line";

	/**
	 * State asking the client to insert markup at the cursor, an object holding {@link #INSERT_SEQ}
	 * and {@link #INSERT_HTML}.
	 *
	 * <p>
	 * A request, not a value: it is set for one insertion and taken back as soon as the client
	 * reports the text it produced. See {@link #applyRawClientValue(Object)}.
	 * </p>
	 */
	private static final String INSERT = "insert";

	/**
	 * Field of {@link #INSERT} counting the insertions, so that inserting the same markup twice is
	 * two requests and not one.
	 */
	private static final String INSERT_SEQ = "seq";

	/** Field of {@link #INSERT} holding the markup to insert at the cursor. */
	private static final String INSERT_HTML = "html";

	private static final String IMAGE_URL = "imageUrl";

	private static final String KEY_PARAM = "&key=";

	private final String _imageUrlPrefix;

	private StructuredText _shadowCopy;

	/**
	 * Where the object a link is inserted for is picked, or {@code null} if this editor inserts no
	 * object links.
	 */
	private final ObjectLinkConfig _objectLink;

	/**
	 * The channel the object selection publishes the picked object on, or {@code null} if this
	 * editor inserts no object links.
	 */
	private final DefaultViewChannel _objectLinkResult;

	/**
	 * The open object selection, or {@code null} while none is open or the dialog closed itself
	 * already.
	 */
	private DialogHandle _objectLinkDialog;

	/** Number of insertions requested so far, the value of {@link #INSERT_SEQ}. */
	private int _insertions;

	/**
	 * Creates a {@link ReactWysiwygControl} that formats text and inserts no object links.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model) {
		this(context, model, null);
	}

	/**
	 * Creates a {@link ReactWysiwygControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 * @param objectLink
	 *        Where the object a link is inserted for is picked, or {@code null} to offer no such
	 *        button.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model, ObjectLinkConfig objectLink) {
		super(context, model, "TLWysiwygEditor");

		_imageUrlPrefix = context.getContextPath() + "/react-api/data?controlId=" + getID()
			+ "&windowName=" + context.getWindowName() + KEY_PARAM;

		initShadowCopy();
		putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));

		_objectLink = objectLink;
		if (objectLink == null) {
			_objectLinkResult = null;
		} else {
			_objectLinkResult = new DefaultViewChannel(objectLink.getResultChannel());
			_objectLinkResult.addListener((sender, oldValue, newValue) -> objectPicked(newValue));
			putState(OBJECT_LINK, buttonState());
		}
	}

	/**
	 * The channel the object selection publishes the picked object on, {@code null} if this editor
	 * inserts no object links.
	 *
	 * <p>
	 * The editor hands this channel to the dialog it opens, under the name the
	 * {@link ObjectLinkConfig#getResultChannel()} gives it, and inserts a link to whatever object
	 * appears on it. Writing an object here is therefore the same as picking it in the dialog.
	 * </p>
	 */
	public ViewChannel getObjectLinkResult() {
		return _objectLinkResult;
	}

	/**
	 * The {@link #OBJECT_LINK} state describing the button inserting an object link.
	 */
	private static Map<String, Object> buttonState() {
		Map<String, Object> button = new LinkedHashMap<>();
		button.put(BUTTON_LABEL, Resources.getInstance().getString(I18NConstants.INSERT_OBJECT_LINK));
		button.put(BUTTON_ICON, OBJECT_LINK_ICON);
		return button;
	}

	private void initShadowCopy() {
		StructuredText value = (StructuredText) getFieldModel().getValue();
		if (value != null) {
			_shadowCopy = value.copy();
		} else {
			_shadowCopy = new StructuredText();
		}
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		if (newValue instanceof StructuredText) {
			_shadowCopy = ((StructuredText) newValue).copy();
			putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));
		} else {
			_shadowCopy = new StructuredText();
			putState(VALUE, "");
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		String html = rawValue != null ? rawValue.toString() : "";
		String cleanHtml = stripImageUrls(html);
		if (isVisuallyEmpty(cleanHtml)) {
			// An emptied editor means "no value": normalize to null so emptiness checks (e.g. a
			// mandatory constraint) treat the field like a cleared text input rather than
			// accepting markup skeletons such as an empty paragraph.
			_shadowCopy.setSourceCode("");
			return null;
		}
		_shadowCopy.setSourceCode(cleanHtml);
		return _shadowCopy.copy();
	}

	/**
	 * Whether the given HTML renders without any visible content: no text and no embedded media
	 * (the editor represents an empty document as markup skeletons like {@code <p></p>}).
	 */
	private static boolean isVisuallyEmpty(String html) {
		if (html.isEmpty()) {
			return true;
		}
		Document document = Jsoup.parseBodyFragment(html);
		return document.text().trim().isEmpty()
			&& document.select("img, table, iframe, video, audio, hr").isEmpty();
	}

	@Override
	public BinaryData getDownloadData(String key) {
		if (_shadowCopy == null || key == null) {
			return null;
		}
		return _shadowCopy.getImages().get(key);
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		for (Part part : parts) {
			String submittedFileName = part.getSubmittedFileName();
			if (submittedFileName == null || submittedFileName.isEmpty()) {
				// Skip non-file parts (controlId, windowName).
				continue;
			}
			try {
				String fileName = uniqueImageKey(submittedFileName);
				BinaryData imageData = BinaryDataFactory.createUploadData(part);
				_shadowCopy.addImage(fileName, imageData);
				putState(IMAGE_URL, fileName);
			} catch (IOException ex) {
				throw new TopLogicException(I18NConstants.ERROR_IMAGE_UPLOAD_FAILED, ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Rewrites bare image filenames in {@code <img src>} to full download URLs for the client.
	 */
	private String rewriteImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			if (!src.contains("react-api/data") && _shadowCopy.getImages().containsKey(src)) {
				img.attr("src", _imageUrlPrefix + URLEncoder.encode(src, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	/**
	 * Strips download URLs from {@code <img src>} back to bare image keys for storage.
	 */
	private String stripImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			int keyIndex = src.indexOf(KEY_PARAM);
			if (keyIndex >= 0) {
				String key = src.substring(keyIndex + KEY_PARAM.length());
				int ampIndex = key.indexOf('&');
				if (ampIndex >= 0) {
					key = key.substring(0, ampIndex);
				}
				img.attr("src", URLDecoder.decode(key, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	private String extractHtml(StructuredText text) {
		if (text == null) {
			return "";
		}
		return text.getSourceCode();
	}

	/**
	 * Displays the object an {@link TLObjectLinkUtil#TL_OBJECT object link} in the shown content
	 * points at.
	 */
	@ReactCommandHandler(value = CMD_SHOW_OBJECT_LINK,
		params = @ReactParam(name = ARG_HREF, required = true,
			description = "The link destination naming the object to display."))
	void handleShowObjectLink(ReactContext context, Map<String, Object> arguments) {
		ObjectLinks.follow(context, (String) arguments.get(ARG_HREF));
	}

	/**
	 * Opens the selection the object a link is inserted for is picked in.
	 *
	 * <p>
	 * The link is not inserted here: the selection takes as long as the user needs and publishes
	 * the picked object on the {@link #getObjectLinkResult() result channel}, which is where the
	 * insertion follows from.
	 * </p>
	 *
	 * <p>
	 * An editor that offers no such button - one without an {@link ObjectLinkConfig} - and a
	 * context that displays no dialogs both leave the command with nowhere to go; the user is told
	 * so instead of the click doing nothing.
	 * </p>
	 */
	@ReactCommandHandler(CMD_INSERT_OBJECT_LINK)
	void handleInsertObjectLink(ReactContext context) {
		if (_objectLink == null) {
			Logger.warn("Asked for an object link, but the editor is configured with no selection.",
				ReactWysiwygControl.class);
			ViewMessages.error(context, I18NConstants.ERROR_OBJECT_SELECTION_UNAVAILABLE);
			return;
		}

		// The object picked last time is still on the channel, and a channel reports a change
		// only. Without clearing it, picking the same object twice would insert it once.
		_objectLinkResult.set(null);

		DialogHandle dialog = OpenDialogAction.openDialogWithChannels(context,
			ViewLoader.fullPath(_objectLink.getDialogView()),
			OpenDialogAction.Config.CLOSE_ON_BACKDROP_DEFAULT,
			Map.of(_objectLink.getResultChannel(), _objectLinkResult), List.of());
		if (dialog == null) {
			Logger.warn("Asked for an object link outside a window that displays dialogs.",
				ReactWysiwygControl.class);
			ViewMessages.error(context, I18NConstants.ERROR_OBJECT_SELECTION_UNAVAILABLE);
			return;
		}
		_objectLinkDialog = dialog;
	}

	/**
	 * Inserts a link to what the object selection published, ignoring a channel that was merely
	 * cleared.
	 */
	private void objectPicked(Object picked) {
		if (picked == null) {
			return;
		}
		if (!(picked instanceof TLObject object)) {
			Logger.warn("Object selection published " + picked.getClass().getName()
				+ ", which no link can lead to.", ReactWysiwygControl.class);
			return;
		}
		insertObjectLink(object);
	}

	/**
	 * Asks the client to insert a link to the given object at the cursor.
	 */
	private void insertObjectLink(TLObject object) {
		insertAtCursor(TLObjectLinkUtil.getLink(object, MetaLabelProvider.INSTANCE.getLabel(object), null));
	}

	/**
	 * Asks the client to insert the given markup at the cursor of the editor.
	 *
	 * <p>
	 * The request stands until the client has carried it out and reported the text it produced,
	 * whereupon it is taken back. Requesting an insertion of markup already inserted is therefore
	 * a second insertion and not a repetition of the first.
	 * </p>
	 *
	 * @param html
	 *        The markup to insert.
	 */
	public void insertAtCursor(String html) {
		Map<String, Object> insert = new LinkedHashMap<>();
		insert.put(INSERT_SEQ, Integer.valueOf(++_insertions));
		insert.put(INSERT_HTML, html);
		putState(INSERT, insert);
	}

	/**
	 * Applies the text the client sends and takes back what was asked of it.
	 *
	 * <p>
	 * The text is the answer to a pending {@link #INSERT} request: the link is part of the content
	 * now, so the request is dropped - a client mounting the editor anew would otherwise carry it
	 * out a second time. The selection it came from has served its purpose and is closed, unless
	 * it closed itself already.
	 * </p>
	 *
	 * @implNote The selection is closed here rather than when the object arrives on the channel:
	 *           closing it while its own publishing command is still running would take it out
	 *           from under the {@code close-dialog} that usually follows, and that one closes
	 *           whichever dialog is topmost - by then the one underneath.
	 */
	@Override
	protected void applyRawClientValue(Object rawValue) {
		super.applyRawClientValue(rawValue);

		if (getState(INSERT) == null) {
			return;
		}
		putState(INSERT, null);

		DialogHandle dialog = _objectLinkDialog;
		_objectLinkDialog = null;
		if (dialog != null) {
			dialog.close(DialogResult.ok(null));
		}
	}

	private String uniqueImageKey(String name) {
		if (name == null) {
			name = "image";
		}
		Map<String, BinaryData> images = _shadowCopy.getImages();
		if (!images.containsKey(name)) {
			return name;
		}
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String ext = dot > 0 ? name.substring(dot) : "";
		int counter = 1;
		String candidate;
		do {
			candidate = base + "_" + counter + ext;
			counter++;
		} while (images.containsKey(candidate));
		return candidate;
	}

}
