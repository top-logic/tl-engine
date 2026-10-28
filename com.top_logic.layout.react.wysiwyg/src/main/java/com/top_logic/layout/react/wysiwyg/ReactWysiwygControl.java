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
import java.util.ArrayList;
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
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.form.I18NLocalizedFieldModel;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.model.TLObject;
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
 * An image embedded in the text is stored in its source as a reference {@code <img src="ref:name">}
 * (see {@link I18NStructuredTextUtil#getImageRefID(String)}), where {@code name} is the key of the
 * image in {@link StructuredText#getImages()}. The HTML sent to the client carries a download URL
 * {@code <img src="/react-api/data?controlId=...&key=name">} in place of such a reference, and a
 * download URL in the HTML received from the client is turned back into the reference. A plain
 * {@code <img src="name">} naming an image of the text is shown as well and stored as a reference
 * after the next edit.
 * </p>
 *
 * <p>
 * An editor {@link #setTextOnly(boolean) for text only} edits a model holding the HTML source as
 * {@link String}. Such a text cannot store images, so the editor offers no image upload. An editor
 * {@link #setSourceEditing(boolean) with source editing} offers switching between the formatted
 * text and its HTML source.
 * </p>
 *
 * <p>
 * A text received from the client is stored only if it passes the application's HTML safety check
 * ({@link SafeHTML}), see {@link #setCheckHtml(boolean)}.
 * </p>
 *
 * <p>
 * An editor {@link #setMentions(List) with mentions} tells the client their trigger characters in
 * {@link #MENTIONS}. While a mention is typed, the client asks for the objects matching the text
 * typed after the trigger with the command {@link MentionArguments#COMMAND}, sending nothing but
 * the trigger and that text, and receives them in {@link #MENTION_OPTIONS}.
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

	/**
	 * State announcing the key of an uploaded image, which the client inserts at the cursor as a
	 * download URL.
	 */
	private static final String IMAGE_URL = "imageUrl";

	/**
	 * State telling whether the editor offers uploading images, absent for an editor that does.
	 *
	 * @see #setTextOnly(boolean)
	 */
	private static final String IMAGES = "images";

	/**
	 * State telling whether the editor offers switching to the HTML source of the text.
	 *
	 * @see #setSourceEditing(boolean)
	 */
	public static final String SOURCE_EDITING = "sourceEditing";

	/**
	 * State listing the {@link #setMentions(List) mentions} the editor offers, each an object
	 * holding {@link #MENTION_TRIGGER} and {@link #MENTION_PRESENTATION}; absent for an editor
	 * offering none.
	 */
	public static final String MENTIONS = "mentions";

	/** Field of a {@link #MENTIONS} entry holding the trigger character of the mention. */
	public static final String MENTION_TRIGGER = "trigger";

	/**
	 * Field of a {@link #MENTIONS} entry holding the {@link MentionPresentation#getExternalName()
	 * external name} of the presentation of a reference inserted by the mention.
	 */
	public static final String MENTION_PRESENTATION = "presentation";

	/**
	 * State holding the answer to the last {@link MentionArguments#COMMAND} command, an object
	 * holding {@link #OPTIONS_TRIGGER}, {@link #OPTIONS_QUERY} and {@link #OPTIONS_ENTRIES}.
	 *
	 * <p>
	 * The trigger and the query the answer is computed for are part of it, so that the client can
	 * tell an outdated answer from the one to the text currently typed.
	 * </p>
	 */
	public static final String MENTION_OPTIONS = "mentionOptions";

	/** Field of {@link #MENTION_OPTIONS} holding the trigger character the answer is for. */
	public static final String OPTIONS_TRIGGER = "trigger";

	/** Field of {@link #MENTION_OPTIONS} holding the query the answer is for. */
	public static final String OPTIONS_QUERY = "query";

	/**
	 * Field of {@link #MENTION_OPTIONS} listing the objects offered, each an object holding
	 * {@link #ENTRY_LABEL} and {@link #ENTRY_LINK}.
	 */
	public static final String OPTIONS_ENTRIES = "entries";

	/** Field of an {@link #OPTIONS_ENTRIES} entry holding the label of the object. */
	public static final String ENTRY_LABEL = "label";

	/**
	 * Field of an {@link #OPTIONS_ENTRIES} entry holding the destination of a link to the object,
	 * the {@link TLObjectLinkUtil#getLinkDestination(TLObject, String) href} of an
	 * {@link TLObjectLinkUtil#TL_OBJECT object link}.
	 */
	public static final String ENTRY_LINK = "link";

	/** Path of the download URLs the images are served under by {@link #getDownloadData(String)}. */
	private static final String DATA_PATH = "/react-api/data?";

	/** Parameter of a download URL naming the image, the last parameter of the URL. */
	private static final String KEY_PARAM = "&key=";

	/** Start of every download URL of this editor, up to the query parameters. */
	private final String _dataUrlBase;

	/** Start of the download URL of an image, followed by the image key. */
	private final String _imageUrlPrefix;

	private StructuredText _shadowCopy;

	/**
	 * The channel whose text is inserted at the cursor, or {@code null} where the configuration
	 * named none.
	 */
	private final DefaultViewChannel _insertChannel;

	/** Number of insertions requested so far, the value of {@link #INSERT_SEQ}. */
	private int _insertions;

	/** Whether the model holds the HTML source as {@link String}, see {@link #setTextOnly(boolean)}. */
	private boolean _textOnly;

	/** Whether the text received from the client is checked, see {@link #setCheckHtml(boolean)}. */
	private boolean _checkHtml = true;

	/** The mentions the editor offers by their trigger characters, see {@link #setMentions(List)}. */
	private Map<String, Mention> _mentions = Map.of();

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
	 * Creates a {@link ReactWysiwygControl} {@link #setTextOnly(boolean) for text only}, which
	 * offers no commands of its own.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the HTML source as {@link String}.
	 * @param sourceEditing
	 *        Whether the editor offers switching to the HTML source, see
	 *        {@link #setSourceEditing(boolean)}.
	 */
	public static ReactWysiwygControl forText(ReactContext context, FieldModel model, boolean sourceEditing) {
		ReactWysiwygControl editor = new ReactWysiwygControl(context, model);
		editor.setTextOnly(true);
		editor.setSourceEditing(sourceEditing);
		return editor;
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

		_dataUrlBase = context.getContextPath() + DATA_PATH;
		_imageUrlPrefix = _dataUrlBase + "controlId=" + getID()
			+ "&windowName=" + context.getWindowName() + KEY_PARAM;

		initShadowCopy();
		putState(FieldState.VALUE__PROP, rewriteImageUrls(extractHtml(_shadowCopy)));

		_insertChannel = insertChannel == null ? null : new DefaultViewChannel(insertChannel);
		if (_insertChannel != null) {
			_insertChannel.addListener((sender, oldValue, newValue) -> textWritten(newValue));
			ObservingChannels.observeWhileDisplayed(this, List.of(_insertChannel), context);
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
	 *
	 * <p>
	 * The buttons show their icon alone and their label as tooltip, the presentation of the
	 * formatting buttons they stand beside. A command asking for a presentation of its own keeps
	 * it.
	 * </p>
	 */
	private void createToolbar(ViewContext context, List<ViewCommand> commands,
			List<ViewCommand.Config> commandConfigs) {
		List<ViewCommandModel> models = ViewCommands.buildCommandModels(context, commands, commandConfigs);
		ReactToolbarControl toolbar = ToolbarBuilder.build(context, new CommandScope(models),
			CommandPlacement.TOOLBAR, CommandCliqueService.getInstance(), ButtonDisplayMode.ICON_ONLY);
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

	/**
	 * Whether the edited model holds the HTML source of the text as {@link String} rather than a
	 * {@link StructuredText}.
	 */
	public boolean isTextOnly() {
		return _textOnly;
	}

	/**
	 * Sets whether the edited model holds the HTML source of the text as {@link String} rather
	 * than a {@link StructuredText}.
	 *
	 * <p>
	 * A text held as {@link String} has no place for images, so an editor for text only offers no
	 * image upload.
	 * </p>
	 */
	public void setTextOnly(boolean textOnly) {
		_textOnly = textOnly;
		putState(IMAGES, textOnly ? Boolean.FALSE : null);
	}

	/**
	 * Whether the editor offers switching between the formatted text and its HTML source.
	 */
	public boolean isSourceEditing() {
		return getState(SOURCE_EDITING) == Boolean.TRUE;
	}

	/**
	 * Sets whether the editor offers switching between the formatted text and its HTML source.
	 *
	 * <p>
	 * The source is edited as it is: markup the formatted view cannot show is kept as long as the
	 * text is not changed in the formatted view.
	 * </p>
	 */
	public void setSourceEditing(boolean sourceEditing) {
		putState(SOURCE_EDITING, sourceEditing ? Boolean.TRUE : null);
	}

	/**
	 * Whether a text received from the client is stored only if it passes the application's HTML
	 * safety check ({@link SafeHTML}).
	 */
	public boolean isCheckHtml() {
		return _checkHtml;
	}

	/**
	 * Sets whether a text received from the client is stored only if it passes the application's
	 * HTML safety check ({@link SafeHTML}).
	 *
	 * <p>
	 * A text the check rejects is not stored: the field reports the problem the check finds and
	 * keeps its last accepted value, while the editor still shows the rejected text.
	 * </p>
	 *
	 * <p>
	 * An editor checks the text unless told otherwise.
	 * </p>
	 */
	public void setCheckHtml(boolean checkHtml) {
		_checkHtml = checkHtml;
	}

	/**
	 * The ways of referring to objects the editor offers by typing a trigger character.
	 *
	 * @see #setMentions(List)
	 */
	public Collection<Mention> getMentions() {
		return _mentions.values();
	}

	/**
	 * Sets the ways of referring to objects the editor offers by typing a trigger character.
	 *
	 * @param mentions
	 *        The mentions, each with a trigger character of its own. A mention whose trigger an
	 *        earlier one uses is ignored.
	 */
	public void setMentions(List<Mention> mentions) {
		Map<String, Mention> byTrigger = new LinkedHashMap<>();
		List<Map<String, Object>> announced = new ArrayList<>();
		for (Mention mention : mentions) {
			if (byTrigger.putIfAbsent(mention.getTrigger(), mention) != null) {
				Logger.warn("Ignoring a second mention with the trigger '" + mention.getTrigger() + "'.",
					ReactWysiwygControl.class);
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(MENTION_TRIGGER, mention.getTrigger());
			entry.put(MENTION_PRESENTATION, mention.getPresentation().getExternalName());
			announced.add(entry);
		}
		_mentions = byTrigger;

		Object tx = beginUpdate();
		putState(MENTIONS, announced.isEmpty() ? null : announced);
		putState(MENTION_OPTIONS, null);
		commitUpdate(tx);
	}

	/**
	 * Handles the command {@link MentionArguments#COMMAND}: computes the objects the mention with
	 * the given trigger offers for the given query and sends them to the client in
	 * {@link #MENTION_OPTIONS}, together with the trigger and query they are computed for.
	 *
	 * <p>
	 * Computing the objects leaves the value of the field alone. A trigger of no mention of the
	 * editor is answered with no objects. When the function of the mention fails, the client
	 * receives no objects and the failure is reported.
	 * </p>
	 */
	@ReactCommandHandler(value = MentionArguments.COMMAND, technical = true)
	HandlerResult handleMention(MentionArguments args) {
		String trigger = args.getTrigger();
		String query = args.getQuery() == null ? "" : args.getQuery();

		List<Map<String, Object>> entries = new ArrayList<>();
		HandlerResult result = HandlerResult.DEFAULT_RESULT;
		Mention mention = trigger == null ? null : _mentions.get(trigger);
		if (mention != null) {
			try {
				for (TLObject object : mention.options(editedObject(), query)) {
					Map<String, Object> entry = new LinkedHashMap<>();
					entry.put(ENTRY_LABEL, MetaLabelProvider.INSTANCE.getLabel(object));
					entry.put(ENTRY_LINK, TLObjectLinkUtil.getLinkDestination(object, null));
					entries.add(entry);
				}
			} catch (RuntimeException ex) {
				Logger.error("Failed to compute the objects for the mention '" + trigger + query + "'.", ex,
					ReactWysiwygControl.class);
				entries.clear();
				result = HandlerResult.error(I18NConstants.ERROR_MENTION_FAILED);
			}
		}

		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put(OPTIONS_TRIGGER, trigger);
		answer.put(OPTIONS_QUERY, query);
		answer.put(OPTIONS_ENTRIES, entries);
		putState(MENTION_OPTIONS, answer);
		return result;
	}

	/**
	 * The object the edited text is an attribute of, {@code null} for a field editing no attribute
	 * of an object.
	 */
	private Object editedObject() {
		FieldModel model = getFieldModel();
		if (model instanceof I18NLocalizedFieldModel localized) {
			model = localized.getDelegate();
		}
		return model instanceof AttributeFieldModel attributeModel ? attributeModel.getObject() : null;
	}

	private void initShadowCopy() {
		_shadowCopy = toStructuredText(getFieldModel().getValue());
	}

	/**
	 * A copy of the given model value as {@link StructuredText}, an empty text for no value.
	 */
	private static StructuredText toStructuredText(Object value) {
		if (value instanceof StructuredText text) {
			return text.copy();
		}
		if (value instanceof String source) {
			return new StructuredText(source);
		}
		return new StructuredText();
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		_shadowCopy = toStructuredText(newValue);
		putState(FieldState.VALUE__PROP, rewriteImageUrls(extractHtml(_shadowCopy)));
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
		if (_textOnly) {
			return cleanHtml;
		}
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
	 * Replaces the image references in {@code <img src>} with download URLs for the client.
	 *
	 * <p>
	 * A reference {@code ref:name} and a plain {@code name} both become the download URL of the
	 * image {@code name}, provided the text has such an image. Any other source is left as it is.
	 * </p>
	 */
	private String rewriteImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Map<String, BinaryData> images = _shadowCopy.getImages();
		Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			String key = I18NStructuredTextUtil.isImageReference(src)
				? I18NStructuredTextUtil.getImageID(src)
				: src;
			if (images.containsKey(key)) {
				img.attr("src", _imageUrlPrefix + URLEncoder.encode(key, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	/**
	 * Replaces the download URLs in {@code <img src>} with the image references they stand for.
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
			if (!src.startsWith(_dataUrlBase)) {
				continue;
			}
			int keyIndex = src.indexOf(KEY_PARAM);
			if (keyIndex >= 0) {
				String key = src.substring(keyIndex + KEY_PARAM.length());
				int ampIndex = key.indexOf('&');
				if (ampIndex >= 0) {
					key = key.substring(0, ampIndex);
				}
				img.attr("src",
					I18NStructuredTextUtil.getImageRefID(URLDecoder.decode(key, StandardCharsets.UTF_8)));
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
		if (_checkHtml) {
			ResKey problem = rawValue == null ? null : htmlProblem(stripImageUrls(rawValue.toString()));
			setInputError(problem);
			if (problem == null) {
				super.applyRawClientValue(rawValue);
			}
		} else {
			super.applyRawClientValue(rawValue);
		}

		if (getState(INSERT) == null) {
			return;
		}
		putState(INSERT, null);
	}

	/**
	 * The problem the application's HTML safety check ({@link SafeHTML}) finds in the given text,
	 * {@code null} if it is safe.
	 */
	private static ResKey htmlProblem(String html) {
		try {
			SafeHTML.getInstance().check(html);
			return null;
		} catch (I18NException ex) {
			return I18NConstants.ERROR_UNSAFE_HTML__PROBLEM.fill(ex.getErrorKey());
		}
	}

	/**
	 * Reports the given problem with the text received from the client on the model, so that the
	 * field shows it, or clears a reported problem.
	 */
	private void setInputError(ResKey problem) {
		FieldModel model = getFieldModel();
		if (model instanceof I18NLocalizedFieldModel localized) {
			model = localized.getDelegate();
		}
		if (model instanceof AbstractFieldModel abstractModel) {
			abstractModel.setError(problem);
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
