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
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CliqueRegistry;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.tool.boundsec.HandlerResult;
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
 * Beside its formatting buttons, the editor's toolbar carries the commands it was created with.
 * They run in a context of their own, derived from the one the editor is displayed in: they see
 * the channels of that view, plus the editor's insertion channel where one is named. Text written
 * to that channel is inserted at the cursor.
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
	 * State holding the toolbar of configured commands, a child control the client renders beside
	 * the editor's formatting buttons.
	 *
	 * <p>
	 * Absent while the editor has no command placed in a toolbar, which is how the client knows
	 * to render its own buttons alone.
	 * </p>
	 */
	private static final String TOOLBAR = "toolbar";

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
	 * The channel whose text is inserted at the cursor, or {@code null} where the configuration
	 * named none.
	 */
	private final DefaultViewChannel _insertChannel;

	/** Number of insertions requested so far, the value of {@link #INSERT_SEQ}. */
	private int _insertions;

	/**
	 * Creates a {@link ReactWysiwygControl} that formats text and offers no commands of its own.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model) {
		this(context, model, List.of(), List.of(), null);
	}

	/**
	 * Creates a {@link ReactWysiwygControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 * @param commands
	 *        The commands the toolbar offers beside the formatting buttons; those placed in a
	 *        toolbar are rendered.
	 * @param commandConfigs
	 *        The configurations the commands were created from, in the same order.
	 * @param insertChannel
	 *        Name of the channel whose text is inserted at the cursor, or {@code null} for an
	 *        editor whose commands write no text.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model, List<ViewCommand> commands,
			List<ViewCommand.Config> commandConfigs, String insertChannel) {
		super(context, model, "TLWysiwygEditor");

		_imageUrlPrefix = context.getContextPath() + "/react-api/data?controlId=" + getID()
			+ "&windowName=" + context.getWindowName() + KEY_PARAM;

		initShadowCopy();
		putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));

		_insertChannel = insertChannel == null ? null : new DefaultViewChannel(insertChannel);
		if (_insertChannel != null) {
			_insertChannel.addListener((sender, oldValue, newValue) -> textWritten(newValue));
		}

		if (commands.isEmpty()) {
			return;
		}
		if (!(context instanceof ViewContext viewContext)) {
			// The commands name the channels they work on, and outside a view there are none to
			// name. Nothing can be built that would run.
			Logger.warn("Editor commands are configured outside a view and are therefore not offered.",
				ReactWysiwygControl.class);
			return;
		}

		createToolbar(_insertChannel == null ? viewContext
			: viewContext.withLocalChannel(insertChannel, _insertChannel), commands, commandConfigs);
	}

	/**
	 * Offers the given commands in the editor's own toolbar, as far as they are placed in one.
	 */
	private void createToolbar(ViewContext context, List<ViewCommand> commands,
			List<ViewCommand.Config> commandConfigs) {
		List<ViewCommandModel> models = ViewCommands.buildCommandModels(context, commands, commandConfigs);
		ReactToolbarControl toolbar = ToolbarBuilder.build(context, new CommandScope(models),
			CommandPlacement.TOOLBAR, new CliqueRegistry(), null);
		if (toolbar == null) {
			return;
		}
		putState(TOOLBAR, toolbar);
		ViewCommands.registerLifecycle(context, models, this);
	}

	/**
	 * The channel whose text the editor inserts at the cursor, {@code null} where the
	 * configuration named none.
	 *
	 * <p>
	 * The editor registers it beside the channels of the view its commands see, so that writing
	 * markup there is the same as a command of the editor producing it.
	 * </p>
	 */
	public ViewChannel getInsertChannel() {
		return _insertChannel;
	}

	/**
	 * Inserts the text a command wrote to the {@link #getInsertChannel() insertion channel}.
	 *
	 * <p>
	 * The channel is cleared afterwards, so that writing the same markup again is another
	 * insertion rather than a value the channel already holds.
	 * </p>
	 */
	private void textWritten(Object written) {
		if (written == null) {
			return;
		}
		if (!(written instanceof String html)) {
			Logger.warn("Insertion channel was written " + written.getClass().getName()
				+ ", which is no markup to insert.", ReactWysiwygControl.class);
			return;
		}
		insertAtCursor(html);
		_insertChannel.set(null);
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
	 * The text is the answer to a pending {@link #INSERT} request: the markup is part of the
	 * content now, so the request is dropped - a client mounting the editor anew would otherwise
	 * carry it out a second time.
	 * </p>
	 */
	@Override
	protected void applyRawClientValue(Object rawValue) {
		super.applyRawClientValue(rawValue);

		if (getState(INSERT) == null) {
			return;
		}
		putState(INSERT, null);
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
