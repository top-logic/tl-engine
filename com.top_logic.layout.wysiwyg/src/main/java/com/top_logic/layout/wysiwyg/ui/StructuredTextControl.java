/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.mig.util.net.URLUtilities.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.top_logic.base.multipart.MultipartRequest;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.binary.FileItemBinaryData;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.ErrorPage;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.KeyCodeHandler;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.DeliverContentHandler;
import com.top_logic.layout.form.control.FieldInspector;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.layout.wysiwyg.ui.scripting.ImageUploadAction;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Editor for {@link StructuredText} in a WYSIWYG style.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTextControl extends AbstractFormFieldControl implements ContentHandler {

	private static final char JSON_NODE_SEPARATOR = ',';

	private static final char CLOSING_CURLY_BRACKET = '}';

	private static final String READ_ONLY_EDITOR_CONFIG = "readOnly";

	private static final String CONTENTS_CSS_EDITOR_CONFIG = "contentsCss";
	
	private static final String LANGUAGE_EDITOR_CONFIG = "language";

	private static final String TEMPLATE_FALLBACK_LANGUAGE_EDITOR_CONFIG = "fallback_language";

	private static final String FILENAME_SEPARATOR = ".";

	private static final String ID_SEPARATOR = "_";

	private static final String REF_ID_PREFIX = I18NStructuredTextUtil.REF_ID_PREFIX;

	/** Selector for images. */
	public static final String IMAGE_CSS_SELECTOR = getImageCSSSelector(REF_ID_PREFIX);

	private static final String TL_OBJECT_LINK_CSS_SELECTOR = "a.tlObject";

	private static final String TL_OBJECT_WRAPPER_CSS_CLASS = "tlObjectWrapper";

	private static final String RESOURCE_SEPARATOR = "/";

	private static final String RESPONSE_IMG_HEIGHT_NAME = "height";

	private static final String RESPONSE_IMG_WIDTH_NAME = "width";

	private static final String RESPONSE_URL_NAME = "url";

	private static final String RESPONSE_FILENAME_NAME = "fileName";

	private static final String RESPONSE_UPLOADED_NAME = "uploaded";

	private static final String UPLOAD = "upload";

	private static final String DOWNLOAD = "download";

	private static final String DATA_UPLOAD_ATTRIBUTE = "data-upload";

	private static final String HIGHLIGHTING_PLUGIN_NAME = "codesnippet";

	private static final String IMMUTABLE_SOURCE_CODE_FRAME = "immutableSourceCodeFrame";

	private static final String WYSIWYG_INIT_METHOD_BEGIN = "services.wysiwyg.StructuredText.init(";

	private static final String WYSIWYG_INIT_VIEW_METHOD_BEGIN = "services.wysiwyg.StructuredText.initView(";

	private static final String WYSIWYG_REPLACE_METHOD_BEGIN = "services.wysiwyg.StructuredText.replaceContent(";

	private static final String WYSIWYG_CLEANUP_METHOD_BEGIN = "services.wysiwyg.StructuredText.cleanup(";

	private static final String METHOD_END = ");";

	private static final String METHOD_ARGUMENT_SEPARATOR = ",";

	private static final String CONTENT_SUFFIX = "-content";

	private static final String STRUCTURED_TEXT_STYLE_CLASS = "cStructuredText";

	private final boolean _containsTextOnly;

	private boolean _useComponentScrollPosition;

	/**
	 * Overridden control commands for {@link StructuredTextControl}.
	 */
	@SuppressWarnings("hiding")
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] { new DataChanged(), KeyCodeHandler.INSTANCE, FieldInspector.INSTANCE,
			CreateTLObjectLink.INSTANCE, OpenTLObjectLink.INSTANCE });

	private String _editorConfig;

	private String _disabledEditorConfig;

	/**
	 * Transient object for storing the current state of the {@link StructuredText}.
	 * <p>
	 * The reason for this shadow copy is a two step transmission, when a new image is added: First,
	 * the image is uploaded by the JavaScript library for the WYSIWYG editing. Second, the text,
	 * which contains the link to the image, is uploaded by the normal {@link FormField} logic, as a
	 * value change.
	 * </p>
	 * <p>
	 * The {@link FormField} is not informed about the new image, until all the information are on
	 * the server. Only then is the {@link StructuredText} consistent again. Between these two
	 * requests, there is a new image, but the text does not use it, yet. Because the text is
	 * transmitted in the second request. Because of that, this control has to store the new image
	 * itself, until the second request arrives and a consistent {@link StructuredText} can be
	 * created and set as the new {@link FormField} value. This field is used for that: To store the
	 * new image, until it can be set to the {@link FormField} as part of its new value.
	 * </p>
	 * <p>
	 * The problem is, that some time after the first request, the image is downloaded from the
	 * server. But that request can come before or after the second request. That means, this
	 * control cannot just ask the {@link FormField} for the new image: The request might come
	 * before the second request, and therefore before the {@link FormField} is updated with the new
	 * image. Therefore, the control has to ask its cache for the image. This field is this cache.
	 * </p>
	 */
	protected StructuredText _shadowCopy;

	/**
	 * Creates a {@link StructuredTextControl} for the given {@link FormField}.
	 * 
	 * @param model
	 *        The field model that has the structured text as content.
	 */
	public StructuredTextControl(FormField model) {
		this(model, model instanceof StringField || model instanceof ComplexField, null, null);
	}

	/**
	 * Creates a {@link StructuredTextControl} for the given {@link FormField}.
	 * 
	 * @param model
	 *        The field model that has the structured text as content.
	 * @param templateFiles
	 *        Locations of template files see
	 *        {@link StructuredTextConfigService.Config#getTemplateFiles()}.
	 * @param templates
	 *        Names of templates that are defined in the template files see
	 *        {@link StructuredTextConfigService.Config#getTemplates()}
	 */
	public StructuredTextControl(FormField model, List<String> templateFiles, String templates) {
		this(model, model instanceof StringField || model instanceof ComplexField, templateFiles, templates);
	}

	private StructuredTextControl(FormField model, boolean containsTextOnly, List<String> templateFiles, String templates) {
		this(model, getEditorConfig(containsTextOnly, templateFiles, templates), containsTextOnly);
	}

	/**
	 * Creates a {@link StructuredTextControl} for the given {@link FormField}.
	 * 
	 * @param model
	 *        The field model that has the structured text as content.
	 * @param editorConfig
	 *        HTML editor configuration, see
	 *        {@link StructuredTextConfigService#getEditorConfig(String, String, List, String)}.
	 */
	public StructuredTextControl(FormField model, String editorConfig) {
		this(model, editorConfig, model instanceof StringField || model instanceof ComplexField);
	}

	/**
	 * Creates a {@link StructuredTextControl} for the given {@link FormField}.
	 * 
	 * @param model
	 *        The field model that has the structured text as content.
	 * @param editorConfig
	 *        {@link StructuredText} editor configuration.
	 * @param containsTextOnly
	 *        Whether the editor contains only text and no images.
	 */
	public StructuredTextControl(FormField model, String editorConfig, boolean containsTextOnly) {
		super(model, COMMANDS);

		_editorConfig = getThemedEditorConfig(editorConfig);
		_disabledEditorConfig = getDisabledEditorConfig(_editorConfig);

		_containsTextOnly = containsTextOnly;
		_useComponentScrollPosition = false;
	}

	@Override
	protected String getTypeCssClass() {
		return STRUCTURED_TEXT_STYLE_CLASS;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		FrameScope frameScope = getScope().getFrameScope();
		frameScope.registerContentHandler(getID(), this);
	}

	@Override
	protected void internalDetach() {
		FrameScope frameScope = getScope().getFrameScope();
		frameScope.deregisterContentHandler(this);
		super.internalDetach();
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		String controlId = getID();
		String contentId = controlId + CONTENT_SUFFIX;

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);

		out.writeAttribute(DATA_UPLOAD_ATTRIBUTE, createUploadURL(context));

		out.endBeginTag();
		{
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, contentId);
			out.endBeginTag();
			{
				writeContents(context, out, false);
			}
			out.endTag(DIV);

			HTMLUtil.beginScriptAfterRendering(out);

			out.append(WYSIWYG_INIT_METHOD_BEGIN);
			out.writeJsString(controlId);
			out.append(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsString(contentId);
			out.append(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsString(getEditorModeConfig());
			out.append(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsString(TLObjectLinkUtil.TL_OBJECT);
			out.append(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsString(TLObjectLinkUtil.TL_OBJECT_WRAPPER);
			out.append(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsLiteral(_useComponentScrollPosition);
			out.append(METHOD_END);

			HTMLUtil.endScriptAfterRendering(out);
		}

		out.endTag(DIV);
	}

	/**
	 * Get editor config depending on the disabled state of the field.
	 * 
	 * @return The editor config of the WYSIWYG editor.
	 */
	public String getEditorModeConfig() {
		if (getFieldModel().isDisabled()) {
			return _disabledEditorConfig;
		} else {
			return _editorConfig;
		}
	}

	private String createUploadURL(DisplayContext context) {
		URLBuilder uploadUrl = getScope().getFrameScope().getURL(context, this);
		uploadUrl.addResource(UPLOAD);

		return uploadUrl.getURL();
	}

	private String createDownloadURL(DisplayContext context, String imageId) {
		URLBuilder uploadUrl = getScope().getFrameScope().getURL(context, this);
		uploadUrl.addResource(DOWNLOAD);
		uploadUrl.addResource(imageId);

		return uploadUrl.getURL();
	}

	private static String getEditorConfig(boolean containsTextOnly, List<String> templateFiles, String templates) {
		String config;
		StructuredTextConfigService instance = StructuredTextConfigService.getInstance();
		if (containsTextOnly) {
			config = instance.getI18nHTMLConfig(templateFiles, templates);
		} else {
			config = instance.getHtmlConfig(templateFiles, templates);
		}
		return config;
	}

	private String getThemedEditorConfig(String editorConfig) {
		List<String> jsonOptions = new ArrayList<>();
		jsonOptions.add(jsonObject(CONTENTS_CSS_EDITOR_CONFIG, getStylePath()));
		jsonOptions.add(jsonObject(LANGUAGE_EDITOR_CONFIG, TLContext.getLocale().getLanguage()));
		jsonOptions.add(jsonObject(TEMPLATE_FALLBACK_LANGUAGE_EDITOR_CONFIG,
			ResourcesModule.getInstance().getFallbackLocale().getLanguage()));
		return addEditorConfigOptions(editorConfig, jsonOptions);
	}

	private String getDisabledEditorConfig(String editorConfig) {
		String jsonOption = jsonObject(READ_ONLY_EDITOR_CONFIG, true);

		return addEditorConfigOption(editorConfig, jsonOption);
	}

	private String addEditorConfigOptions(String editorConfig, List<String> jsonOptions) {
		for (String jsonOption : jsonOptions) {
			editorConfig = addEditorConfigOption(editorConfig, jsonOption);
		}
		return editorConfig;
	}

	private String addEditorConfigOption(String editorConfig, String jsonOption) {
		return getOpenEditorConfig(editorConfig) + JSON_NODE_SEPARATOR + jsonOption.substring(1);
	}

	private String getOpenEditorConfig(String editorConfig) {
		return editorConfig.substring(0, editorConfig.lastIndexOf(CLOSING_CURLY_BRACKET));
	}

	private String jsonObject(String key, Object value) {
		Map<String, Object> jsonObject = new HashMap<>();
		jsonObject.put(key, value);
		return JSON.toString(jsonObject);
	}

	private String getStylePath() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		Theme currentTheme = ThemeFactory.getInstance().getCurrentTheme(context.getSubSessionContext());
		return context.getContextPath().concat(currentTheme.getStyleSheet());
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		out.beginBeginTag(DIV);
		out.beginCssClasses();
		out.append(IMMUTABLE_SOURCE_CODE_FRAME);
		out.endCssClasses();
		out.endBeginTag();
		
		writeContents(context, out, true);
		out.endTag(DIV);
		out.endTag(DIV);

		HTMLUtil.beginScriptAfterRendering(out);

		out.append(WYSIWYG_INIT_VIEW_METHOD_BEGIN);
		out.writeJsString(getID());
		out.append(METHOD_ARGUMENT_SEPARATOR);
		out.append(Boolean.toString(isHighlightingSupported(_editorConfig)));
		out.append(METHOD_ARGUMENT_SEPARATOR);
		out.writeJsString(TLObjectLinkUtil.TL_OBJECT);
		out.append(METHOD_END);

		HTMLUtil.endScriptAfterRendering(out);
	}

	private boolean isHighlightingSupported(String editorConfig) {
		return editorConfig.contains(HIGHLIGHTING_PLUGIN_NAME);
	}

	/**
	 * Writes the contents of the control.
	 * 
	 * @param context
	 *        {@link DisplayContext} of the control.
	 * @param out
	 *        {@link TagWriter} to write to.
	 * @param tlObjectIcons
	 *        If the icon of the {@link TLObject} link target should be rendered.
	 */
	@SuppressWarnings("deprecation")
	private void writeContents(DisplayContext context, TagWriter out, boolean tlObjectIcons) throws IOException {
		String newSourceCode = resolveSourceCode(context, getSourceCode());

		out.writeContent(newSourceCode);
	}
	
	/**
	 * Change all resolved source attribute values for the appropriate image tags with the
	 * corresponding linked source and remove all unused images.
	 * 
	 * @param newValue
	 *        HTML document to be changed.
	 * @param images
	 *        The images of a {@link StructuredText}. <em>Important:</em> As a side effect, unused
	 *        images are removed from this {@link Map}.
	 * @param urlPrefix
	 *        URL prefix for local images.
	 */
	public static void linkImageSources(Document newValue, Map<String, BinaryData> images, String urlPrefix) {
		Elements localImages = newValue.select(getImageCSSSelector(urlPrefix));
		
		Set<String> linkImageIDs = linkImageSources(urlPrefix, localImages);
		I18NStructuredTextUtil.removeUnusedImages(images, linkImageIDs);
	}

	/**
	 * Resolves placeholders of the source code like images or icons.
	 * 
	 * @param context
	 *        {@link DisplayContext} of this control..
	 * @param sourceCode
	 *        The source code to resolve.
	 * @return Resolved source code as {@link String}.
	 */
	public String resolveSourceCode(DisplayContext context, String sourceCode) {
		if (sourceCode.isEmpty()) {
			return sourceCode;
		}
		Document document = parseDocument(sourceCode);
		resolveImageSources(context, document);
		if (getFieldModel().isImmutable()) {
			addTLObjectLinkIcons(context, document);
		}
		return formatDocument(document);
	}

	/**
	 * Change all source attribute values for the appropriate image tags with the corresponding
	 * resolved source.
	 * 
	 * @param context
	 *        {@link DisplayContext} of this control, to create the download url.
	 * @param document
	 *        {@link Document} containing the HTML code to be changed.
	 * @return {@link String} containing the new HTML code with resolved image sources.
	 */
	private Document resolveImageSources(DisplayContext context, Document document) {
		Elements localImages = document.select(IMAGE_CSS_SELECTOR);

		resolveImageSources(context, localImages);

		return document;
	}

	private void resolveImageSources(DisplayContext context, Elements localImages) {
		localImages.forEach(element -> {
			resolveImageSource(context, element);
		});
	}

	private void resolveImageSource(DisplayContext context, Element element) {
		String attr = I18NStructuredTextUtil.getSrcValue(element);

		I18NStructuredTextUtil.setSrcValue(element,
			createDownloadURL(context, I18NStructuredTextUtil.getImageID(attr)));
	}

	/**
	 * Adds the icon of the link target to the {@link TLObject} link.
	 * 
	 * @param context
	 *        The {@link DisplayContext} of this control to write the {@link ThemeImage} icon.
	 * @param document
	 *        {@link Document} containing the HTML code to be changed.
	 * @return {@link String} containing the new HTML code with the {@link TLObject} links and their
	 *         icons.
	 */
	private Document addTLObjectLinkIcons(DisplayContext context, Document document) {
		Elements tlObjects = document.select(TL_OBJECT_LINK_CSS_SELECTOR);

		addTLObjectLinkIcons(context, tlObjects);
		return document;
	}

	private void addTLObjectLinkIcons(DisplayContext context, Elements tlObjects) {
		tlObjects.forEach(element -> {
			addTlObjectLinkIcon(context, element);
		});
	}

	private void addTlObjectLinkIcon(DisplayContext context, Element tlObject) {
		String targetArguments = tlObject.attr(HREF_ATTR);
		if (targetArguments == null) {
			return;
		}
		Wrapper target = TLObjectLinkUtil.getObject(targetArguments);
		ThemeImage icon = MetaResourceProvider.INSTANCE.getImage(target, Flavor.DEFAULT);
		if (icon == null || icon.equals(ThemeImage.none())) {
			return;
		}
		try (TagWriter writer = new TagWriter()) {
			writer.beginBeginTag(SPAN);
			writer.beginCssClasses();
			writer.append(TL_OBJECT_WRAPPER_CSS_CLASS);
			writer.endCssClasses();
			writer.endBeginTag();
			icon.write(context, writer);
			writer.writeText(NBSP);
			writer.writeContent(tlObject.toString());
			writer.endTag(SPAN);
			tlObject.before(writer.toString());
			tlObject.remove();
		} catch (IOException exception) {
			throw new TopLogicException(I18NConstants.WRITE_LINK_ERROR, exception);
		}
	}

	/**
	 * @see #formatDocument(Document)
	 */
	static Document parseDocument(String sourceCode) {
		return Jsoup.parse(sourceCode, "", Parser.xmlParser());
	}

	/**
	 * @see #parseDocument(String)
	 */
	static String formatDocument(Document document) {
		return document.html();
	}

	private static Set<String> linkImageSources(String urlPrefix, Elements localImages) {
		Set<String> linkedImageIDs = new HashSet<>();

		localImages.forEach(element -> {
			String imageID = linkImageSource(urlPrefix, element);

			linkedImageIDs.add(imageID);
		});

		return linkedImageIDs;
	}

	private static String linkImageSource(String urlPrefix, Element element) {
		String attr = I18NStructuredTextUtil.getSrcValue(element);
		String imageID = getImageID(urlPrefix, attr);

		I18NStructuredTextUtil.setSrcValue(element, I18NStructuredTextUtil.getImageRefID(imageID));

		return imageID;
	}

	private static String getImageID(String urlPrefix, String attr) {
		int index = attr.indexOf(urlPrefix);

		return urlDecode(attr.substring(index + urlPrefix.length() + RESOURCE_SEPARATOR.length()));
	}

	private static String getImageCSSSelector(String srcPrefix) {
		return "*:not(code) img[src^=\"" + srcPrefix + "\"]";
	}

	private String getSourceCode() {
		StructuredText structuredText = getStructuredText();
		if (structuredText != null) {
			return structuredText.getSourceCode();
		}

		// Note: The value could also be an expression
		return StringServices.nonNull((String) getFieldModel().getRawValue());
	}

	private Map<String, BinaryData> getImages() {
		StructuredText structuredText = getStructuredText();
		if (structuredText != null) {
			return structuredText.getImages();
		}

		return Collections.emptyMap();
	}

	private StructuredText getStructuredText() {
		if (_shadowCopy != null) {
			return _shadowCopy;
		}
		Object fieldValue = getFieldModel().getValue();
		if (fieldValue instanceof StructuredText) {
			return (StructuredText) fieldValue;
		}
		return null;
	}

	@Override
	protected void detachInvalidated() {
		if (!getFieldModel().isImmutable()) {
			getScope().getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {

				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					String contentId = getID() + CONTENT_SUFFIX;

					out.append(WYSIWYG_CLEANUP_METHOD_BEGIN);
					TagUtil.writeJsString(out, contentId);
					out.append(METHOD_END);
				}

			}));
		}
		super.detachInvalidated();
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		String id = getID();

		if (Objects.equals(oldValue, newValue)) {
			return;
		}
		_shadowCopy = null;
		addUpdate(new JSSnipplet(new DynamicText() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				String controlId = id;
				String contentId = controlId + CONTENT_SUFFIX;

				out.append(WYSIWYG_REPLACE_METHOD_BEGIN);
				TagUtil.writeJsString(out, controlId);
				out.append(METHOD_ARGUMENT_SEPARATOR);
				TagUtil.writeJsString(out, contentId);
				out.append(METHOD_ARGUMENT_SEPARATOR);
				TagUtil.writeJsString(out, getContent(context));
				out.append(METHOD_END);
			}

			private String getContent(DisplayContext context) {
				return resolveSourceCode(context, getSourceCode());
			}

			private String getSourceCode() {
				if (newValue instanceof StructuredText) {
					return ((StructuredText) newValue).getSourceCode();
				} else {
					// new value may be null.
					return StringServices.nonNull((String) newValue);
				}
			}
		}));
	}

	/**
	 * {@link com.top_logic.layout.form.control.AbstractFormFieldControlBase.ValueChanged} that
	 * additionally checks the transmitted HTML for safety.
	 */
	protected static class DataChanged extends ValueChanged {

		/**
		 * Creates a {@link DataChanged} command.
		 */
		protected DataChanged() {
			super();
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		protected void updateValue(DisplayContext commandContext, AbstractFormFieldControlBase formFieldControl,
				Object newValue, Map<String, Object> arguments) {
			Object clientValue = transformClientVlaue(formFieldControl, newValue);
			super.updateValue(commandContext, formFieldControl, clientValue, arguments);
		}

		private Object transformClientVlaue(AbstractFormFieldControlBase formFieldControl, Object newValue) {
			String newHtml = (String) newValue;
			try {
				SafeHTML.getInstance().check(newHtml);
			} catch (I18NException ex) {
				throw new I18NRuntimeException(I18NConstants.UNSAFE_HTML_ERROR, ex);
			}
			FormField fieldModel = formFieldControl.getFieldModel();
			Document newGUIValue = parseDocument(newHtml);

			StructuredText structuredText =
				getUpdatedStructuredText(formFieldControl, newGUIValue, fieldModel);
			Object clientValue;
			if (((StructuredTextControl) formFieldControl).containsTextOnly()) {
				clientValue = structuredText.getSourceCode();
			} else {
				clientValue = structuredText;
			}
			return clientValue;
		}

		private StructuredText getUpdatedStructuredText(AbstractFormFieldControlBase formFieldControl,
				Document newValue, FormField fieldModel) {
			StructuredText shadowCopy = ((StructuredTextControl) formFieldControl)._shadowCopy;

			if (shadowCopy != null) {
				return getStructuredTextFromShadowCopy(formFieldControl, newValue, shadowCopy);
			} else {
				return getStructuredText(formFieldControl, newValue, fieldModel);
			}

		}

		private StructuredText getStructuredText(AbstractFormFieldControlBase formFieldControl, Document newValue,
				FormField fieldModel) {
			if (((StructuredTextControl) formFieldControl).containsTextOnly()) {
				return new StructuredText(formatDocument(newValue));
			} else {
				StructuredText oldText = (StructuredText) fieldModel.getValue();
				if (oldText == null) {
					return new StructuredText(formatDocument(newValue));
				} else {
					Map<String, BinaryData> images = map(oldText.getImages());
					linkImageSources(newValue, images, getURLPrefix(formFieldControl));
					return new StructuredText(formatDocument(newValue), images);
				}
			}
		}

		private StructuredText getStructuredTextFromShadowCopy(AbstractFormFieldControlBase formFieldControl,
				Document newValue, StructuredText shadowCopy) {
			String urlPrefix = getURLPrefix(formFieldControl);
			linkImageSources(newValue, shadowCopy.getImages(), urlPrefix);
			shadowCopy.setSourceCode(formatDocument(newValue));

			resetShadowCopy(formFieldControl);

			return shadowCopy;
		}

		private void resetShadowCopy(AbstractFormFieldControlBase formFieldControl) {
			((StructuredTextControl) formFieldControl)._shadowCopy = null;
		}

		private String getURLPrefix(AbstractFormFieldControlBase control) {
			FrameScope frameScope = control.getFrameScope();
			DisplayContext context = DefaultDisplayContext.getDisplayContext();

			URLBuilder url = frameScope.getURL(context, (StructuredTextControl) control);
			url.addResource(DOWNLOAD);

			return url.getURL();
		}

	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		String command = url.removeResource();

		if (UPLOAD.equals(command)) {
			uploadFile(context);
		} else if (DOWNLOAD.equals(command)) {
			downloadFile(context, url);
		}
	}

	private void downloadFile(DisplayContext context, URLParser url) throws IOException {
		String imageId = url.removeResource();
		BinaryData image = getImages().get(imageId);

		if (image != null) {
			DeliverContentHandler.deliverContent(context, image);
		} else {
			ErrorPage.showPage(context, "dynamicContentNotFoundErrorPage");
		}
	}

	private void uploadFile(DisplayContext context) throws IOException {
		List<FileItem> files = getFiles(context);

		if (files.size() == 1) {
			FileItemBinaryData fileItemBinaryData = getBinaryData(files.get(0));

			String imageId = saveFile(fileItemBinaryData);
			if (Logger.isDebugEnabled(StructuredTextControl.class)) {
				Logger.debug(imageId, StructuredTextControl.class);
			}

			sendResponse(context, fileItemBinaryData, imageId);

			if (ScriptingRecorder.isRecordingActive()) {
				recordScriptingAction(context, fileItemBinaryData);
			}
		}
	}

	private FileItemBinaryData getBinaryData(FileItem file) {
		return new FileItemBinaryData(file);
	}

	private void recordScriptingAction(DisplayContext context, FileItemBinaryData fileItemBinaryData) {
		LayoutUtils.setWindowScope(context, getWindowScope());

		ScriptingRecorder.recordAction(getImageUploadAction(fileItemBinaryData));
	}

	private ImageUploadAction getImageUploadAction(FileItemBinaryData fileItemBinaryData) {
		ImageUploadAction action = TypedConfiguration.newConfigItem(ImageUploadAction.class);

		action.setField(ModelResolver.buildModelName(getFieldModel()));
		action.setImage(ModelResolver.buildModelName(getFieldModel(), fileItemBinaryData));

		return action;
	}

	private List<FileItem> getFiles(DisplayContext context) {
		HttpServletRequest asRequest = context.asRequest();
		MultipartRequest request = (MultipartRequest) asRequest;

		return request.getFiles();
	}

	private String saveFile(FileItemBinaryData data) {
		if (_shadowCopy == null) {
			initShadowCopy();
		}

		String filename = getID(_shadowCopy, data);
		Map<String, BinaryData> images = _shadowCopy.getImages();
		images.put(filename, data);

		return filename;
	}

	private void initShadowCopy() {
		StructuredText structuredText = (StructuredText) getFieldModel().getValue();

		if (structuredText == null) {
			structuredText = new StructuredText();
		} else {
			structuredText = new StructuredText(structuredText.getSourceCode(), map(structuredText.getImages()));
		}

		_shadowCopy = structuredText;
	}

	private String getID(StructuredText structuredText, FileItemBinaryData data) {
		String name = data.getName();
		Map<String, BinaryData> images = structuredText.getImages();

		return getID(name, images);
	}

	private String getID(String name, Map<String, BinaryData> images) {
		int counter = 0;
		String possibleName = name;

		while (images.containsKey(possibleName)) {
			counter++;

			possibleName = createID(name, counter);
		}

		return possibleName;
	}

	private String createID(String name, int counter) {
		String suffix = getFilenameSuffix(name);

		return createID(name, counter, suffix);
	}

	private String getFilenameSuffix(String name) {
		int index = name.lastIndexOf(FILENAME_SEPARATOR);

		return name.substring(index);
	}

	private String createID(String name, int counter, String suffix) {
		return name.substring(0, name.lastIndexOf(suffix)) + ID_SEPARATOR + counter + suffix;
	}

	private void sendResponse(DisplayContext context, FileItemBinaryData data, String imageId) throws IOException {
		Map<String, Object> fileResponse = getFileResponse(context, data, imageId);
		String response = JSON.toString(fileResponse);

		HttpServletResponse asResponse = context.asResponse();
		asResponse.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		try (PrintWriter out = asResponse.getWriter()) {
			out.print(response);
		}
	}

	private Map<String, Object> getFileResponse(DisplayContext context, FileItemBinaryData data, String imageId) {
		Map<String, Object> fileResponse = new HashMap<>();

		fileResponse.put(RESPONSE_UPLOADED_NAME, 1);
		fileResponse.put(RESPONSE_FILENAME_NAME, data.getName());
		fileResponse.put(RESPONSE_URL_NAME, createDownloadURL(context, imageId));
		fileResponse.put(RESPONSE_IMG_WIDTH_NAME, 300);
		fileResponse.put(RESPONSE_IMG_HEIGHT_NAME, 200);

		return fileResponse;
	}

	/**
	 * Whether the {@link FormField} stores only text, but no images.
	 */
	public boolean containsTextOnly() {
		return _containsTextOnly;
	}
	
	/**
	 * Whether the scroll position of the WYSIWYG editor should be stored in the component.
	 */
	public boolean getUseComponentScrollPosition() {
		return _useComponentScrollPosition;
	}

	/** @see #getUseComponentScrollPosition() */
	public void setUseComponentScrollPosition(boolean useComponentScrollPosition) {
		_useComponentScrollPosition = useComponentScrollPosition;
	}

}
