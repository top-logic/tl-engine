/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.i18n;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.layout.wysiwyg.ui.StructuredText.*;
import static com.top_logic.mig.html.HTMLConstants.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.storage.AbstractStorage;
import com.top_logic.element.model.i18n.I18NAttributeStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.StaticItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.wysiwyg.storage.CommonStructuredTextAttributeStorage;
import com.top_logic.tool.boundsec.ObjectNotFound;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;
import com.top_logic.util.TLContextManager;

/**
 * The {@link AbstractStorage} for {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStructuredTextAttributeStorage<C extends I18NStructuredTextAttributeStorage.Config<?>>
		extends CommonStructuredTextAttributeStorage<C> {

	/** Name of the database table storing the sources codes. */
	private static final String SOURCES_CODES_TABLE_NAME = I18NAttributeStorage.I18N_STORAGE_KO_TYPE;

	/** Name of the database table storing the images. */
	private static final String IMAGES_TABLE_NAME = "I18NHTMLAttributeStorage";

	/** Name of the column for the language in which the image is used. */
	private static final String LANGUAGE_ATTRIBUTE_NAME = I18NAttributeStorage.LANGUAGE_ATTRIBUTE_NAME;

	/** Name of the column for the source code. */
	private static final String SOURCE_CODE_ATTRIBUTE_NAME = I18NAttributeStorage.VALUE_ATTRIBUTE_NAME;

	private AssociationSetQuery<? extends KnowledgeItem> _sourceCodesQuery;

	private List<Locale> _supportedLocales;

	/** {@link TypedConfiguration} constructor for {@link I18NStructuredTextAttributeStorage}. */
	public I18NStructuredTextAttributeStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_sourceCodesQuery = this.createQuery(getSourceCodeTableName(), attribute, StaticItem.class);
		
		_supportedLocales = unmodifiableList(list(getSupportedLocales()));
	}

	private List<Locale> getSupportedLocales() {
		return ResourcesModule.getInstance().getSupportedLocales();
	}

	@Override
	public Object getAttributeValue(TLObject tlObject, TLStructuredTypePart attribute) throws AttributeException {
		Map<Locale, StructuredText> structuredTexts = map();
		addSourceCodes(tlObject, structuredTexts);
		addImages(tlObject, structuredTexts);
		return new I18NStructuredText(structuredTexts);
	}

	private void addSourceCodes(TLObject tlObject, Map<Locale, StructuredText> structuredTexts) {
		for (KnowledgeItem sourceCode : fetchSourceCodes(tlObject)) {
			TLSubSessionContext subSession = TLContextManager.getSubSession();
			Locale currentLocale = subSession.set(StructuredTextConfigService.LOCALE, getLanguage(sourceCode));
			try {
				StructuredText structuredText = getOrCreateByLanguage(structuredTexts, sourceCode);
				String oldSourceCode = getSourceCode(sourceCode);
				String newSourceCode = updateLinks(oldSourceCode);
				structuredText.setSourceCode(newSourceCode);
			} finally {
				if (currentLocale == null) {
					subSession.reset(StructuredTextConfigService.LOCALE);
				} else {
					subSession.set(StructuredTextConfigService.LOCALE, currentLocale);
				}
			}
		}
	}

	private StructuredText getOrCreateByLanguage(Map<Locale, StructuredText> structuredTexts,
			KnowledgeItem internationalizedData) {
		return structuredTexts.computeIfAbsent(getLanguage(internationalizedData), language -> new StructuredText());
	}

	private void addImages(TLObject tlObject, Map<Locale, StructuredText> structuredTexts) {
		for (KnowledgeItem image : fetchImages(tlObject)) {
			StructuredText structuredText = getOrCreateByLanguage(structuredTexts, image);
			addImage(image, structuredText);
		}
	}

	private void addImage(KnowledgeItem image, StructuredText structuredText) {
		String filename = getFileName(image);
		String contentType = getContentType(image);
		BinaryData data = getData(image, filename, contentType);
		structuredText.addImage(filename, data);
	}

	@Override
	protected void internalSetAttributeValue(TLObject owner, TLStructuredTypePart attribute, Object newValue) {
		I18NStructuredText newI18nStructuredTexts = (I18NStructuredText) newValue;
		boolean sourceCodeChanged = setSourceCodes(owner, attribute, newI18nStructuredTexts);
		boolean imagesChanged = setImages(owner, attribute, newI18nStructuredTexts);
		if (sourceCodeChanged || imagesChanged) {
			/* As an updated attribute does not affect the TLObject itself, Lucene will not create a
			 * new index. Thats why the owner has to be touched. */
			owner.tTouch();
		}
	}

	private boolean setSourceCodes(TLObject owner, TLStructuredTypePart attribute, I18NStructuredText newValue) {
		boolean changed = false;

		/* Don't iterator over the locales in the I18NStructuredText. That would write one entry per
		 * fallback locale, which is multiple times more than necessary. Writing just one entry per
		 * "supported locale" is correct, as that means effectively one entry is written per
		 * language. */
		Collection<Locale> supportedLocales = new ArrayList<>(_supportedLocales);
		// The collection is copied to avoid ConcurrentModificationExceptions.
		for (KnowledgeItem oldSourceCode : fetchSourceCodes(owner)) {
			Locale language = getLanguage(oldSourceCode);
			String newSourceCode = getSourceCodeNullSafe(getEntryNullsafe(newValue, language));
			if (StringServices.isEmpty(newSourceCode)) {
				oldSourceCode.delete();
				changed = true;
			} else {
				newSourceCode = setCustomLinks(newSourceCode);
				if (changed) {
					/* It is not necessary to check for change of source code, because only the
					 * accumulated change state is required. */
					setSourceCode(oldSourceCode, newSourceCode);
				} else {
					changed |= updateSourceCode(oldSourceCode, newSourceCode);
				}
			}
			supportedLocales.remove(language);
		}

		for (Locale language : supportedLocales) {
			String newSourceCode = getSourceCodeNullSafe(getEntryNullsafe(newValue, language));
			if (!StringServices.isEmpty(newSourceCode)) {
				createSourceCodeTLObject(owner, attribute, language, setCustomLinks(newSourceCode));
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Sets the {@link TLObjectLinkUtil#DATA_CUSTOM} attribute of TLObject links.
	 * 
	 * <p>
	 * When the user edited and saved a new source code this functions checks if the link texts of
	 * TLObject links equal the names of their target TLObject. If not that means that the user
	 * edited the text of the link and {@link TLObjectLinkUtil#DATA_CUSTOM} has to be set to true.
	 * If they equal {@link TLObjectLinkUtil#DATA_CUSTOM} has to be false.
	 * </p>
	 * 
	 * @param sourceCode
	 *        Source code of the {@link I18NStructuredText}.
	 * @return Source code of the {@link I18NStructuredText} with updated TLObject links.
	 */
	private String setCustomLinks(String sourceCode) {
		Document doc = Jsoup.parse(sourceCode);
		Elements links = doc
			.select(ANCHOR + "[" + HREF_ATTR + "][" + TLObjectLinkUtil.DATA_CUSTOM + "]");
		for (Element link : links) {
			String href = link.attr(HREF_ATTR);
			Wrapper object = resolveLinkTaget(href);
			if (object != null) {
				String objectName = MetaLabelProvider.INSTANCE.getLabel(object);
				String linkText = link.text();
				if (!objectName.equals(linkText)) {
					link.attr(TLObjectLinkUtil.DATA_CUSTOM, "true");
				} else {
					link.attr(TLObjectLinkUtil.DATA_CUSTOM, "false");
				}
			}
		}
		return getBodyHTML(doc);
	}

	private Wrapper resolveLinkTaget(String href) {
		try {
			return TLObjectLinkUtil.getObject(href);
		} catch (ObjectNotFound ex) {
			// Link became invalid. Do not fail. Just do not update link
			return null;
		}
	}

	/**
	 * Updates the link text of {@link TLObject} links if the displayed link text or the actual
	 * {@link #HREF_ATTR goto link} of the target {@link TLObject} has changed.
	 * 
	 * <p>
	 * Displayed links will only be updated if {@link TLObjectLinkUtil#DATA_CUSTOM} is false.
	 * </p>
	 * 
	 * @param sourceCode
	 *        Source code of the {@link I18NStructuredText}.
	 * @return Source code of the {@link I18NStructuredText} with updated {@link TLObject} links.
	 */
	private String updateLinks(String sourceCode) {
		if (!BookmarkService.Module.INSTANCE.isActive()) {
			// Sometimes Lucene starts before the BookmarkService and tries to update links. This
			// will lead to an exception because Lucene wants the full text and will automatically
			// update bookmarks if their label changed. As this is not necessary during the Lucene
			// startup the update can be skipped.
			return sourceCode;
		}
		boolean changed = false;
		Document doc = Jsoup.parse(sourceCode);
		Elements links = doc.select(ANCHOR + "[" + CLASS_ATTR + "=" + TLObjectLinkUtil.TL_OBJECT + "]");
		for (Element link : links) {
			String href = link.attr(HREF_ATTR);
			Wrapper object = resolveLinkTaget(href);
			if (object != null) {
				if ("false".equals(link.attr(TLObjectLinkUtil.DATA_CUSTOM))) {
					String objectName = MetaLabelProvider.INSTANCE.getLabel(object);
					if (!objectName.equals(link.text())) {
						link.text(objectName);
						changed = true;
					}
				}
				String section = StringServices.nonEmpty(link.attr(TLObjectLinkUtil.DATA_SECTION));
				String newHref = TLObjectLinkUtil.getLinkDestination(object, section);
				if (!newHref.equals(href)) {
					link.attr(HREF_ATTR, newHref);
					changed = true;
				}
			}
		}
		if (changed) {
			return getBodyHTML(doc);
		} else {
			return sourceCode;
		}
	}

	static String getBodyHTML(Document document) {
		return document.body().html();
	}

	private void createSourceCodeTLObject(TLObject owner, TLStructuredTypePart attribute, Locale language,
			String text) {
		KnowledgeItem sourceCode = createSourceCodeItem();
		fillSourceCodeTLObject(owner, attribute, language, text, sourceCode);
	}

	private KnowledgeItem createSourceCodeItem() {
		return createItem(getSourceCodeTableName());
	}

	private void fillSourceCodeTLObject(TLObject owner, TLStructuredTypePart attribute, Locale language, String text,
			KnowledgeItem sourceCode) {
		setObject(sourceCode, owner);
		setAttribute(sourceCode, attribute);
		setLanguage(sourceCode, language);
		setSourceCode(sourceCode, text);
	}

	private boolean setImages(TLObject owner, TLStructuredTypePart attribute, I18NStructuredText newValue) {
		boolean someImageChanged = false;
		Map<Locale, Set<KnowledgeItem>> oldImagesByLocale = getImagesByLocale(owner);
		/* Don't iterator over the locales in the I18NStructuredText. That would write one entry per
		 * fallback locale, which is multiple times more than necessary. Writing just one entry per
		 * "supported locale" is correct, as that means effectively one entry is written per
		 * language. */
		for (Locale locale : _supportedLocales) {
			StructuredText newLocalizedValue = getEntryNullsafe(newValue, locale);
			Map<String, BinaryData> newImages = getImagesNullSafe(newLocalizedValue);
			Set<String> newFileNames = newImages.keySet();
			Set<KnowledgeItem> oldImages = CollectionUtil.nonNull(oldImagesByLocale.get(locale));
			someImageChanged |= updateImages(oldImages, newImages);
			someImageChanged |= addImages(owner, attribute, locale, oldImages, newLocalizedValue, newFileNames);
			someImageChanged |= removeImages(oldImages, newFileNames);
		}
		return someImageChanged;
	}

	private Map<Locale, Set<KnowledgeItem>> getImagesByLocale(TLObject owner) {
		Map<Locale, Set<KnowledgeItem>> result = map();
		for (KnowledgeItem image : fetchImages(owner)) {
			Locale locale = getLanguage(image);
			result.computeIfAbsent(locale, key -> set());
			result.get(locale).add(image);
		}
		return result;
	}

	private StructuredText getEntryNullsafe(I18NStructuredText newI18nStructuredTexts, Locale locale) {
		if (newI18nStructuredTexts == null) {
			return null;
		}
		return newI18nStructuredTexts.getEntries().get(locale);
	}

	private boolean addImages(TLObject owner, TLStructuredTypePart attribute, Locale locale,
			Set<KnowledgeItem> oldImages, StructuredText newStructuredText, Set<String> newFileNames) {
		Set<String> possibleToBeAdded = set(newFileNames);
		Set<String> oldFileNames = getFileNames(oldImages);
		possibleToBeAdded.removeAll(oldFileNames);
		addImages(owner, attribute, newStructuredText, locale, possibleToBeAdded);
		return !possibleToBeAdded.isEmpty();
	}

	private void addImages(TLObject owner, TLStructuredTypePart attribute, StructuredText structuredText,
			Locale locale, Set<String> fileNames) {
		for (String fileName : fileNames) {
			BinaryData binaryData = structuredText.getImages().get(fileName);
			KnowledgeItem image = createImageItem(owner, attribute, binaryData, fileName);
			setLanguage(image, locale);
		}
	}

	@Override
	protected Class<?> getApplicationValueType() {
		return I18NStructuredText.class;
	}

	private Locale getLanguage(KnowledgeItem object) {
		return readLocale((String) object.getAttributeValue(LANGUAGE_ATTRIBUTE_NAME));
	}

	private void setLanguage(KnowledgeItem image, Locale newLanguage) {
		image.setAttributeValue(LANGUAGE_ATTRIBUTE_NAME, writeLocale(newLanguage));
	}

	private String writeLocale(Locale locale) {
		return locale.toString();
	}

	private Locale readLocale(String localeCode) {
		return ResourcesModule.localeFromString(localeCode);
	}

	private String getSourceCode(KnowledgeItem sourceCodeObject) {
		return (String) sourceCodeObject.getAttributeValue(SOURCE_CODE_ATTRIBUTE_NAME);
	}

	private void setSourceCode(KnowledgeItem sourceCodeObject, String newSourceCode) {
		sourceCodeObject.setAttributeValue(SOURCE_CODE_ATTRIBUTE_NAME, newSourceCode);
	}

	/**
	 * Updates the source code of the given object.
	 * 
	 * @return Whether source code changed.
	 */
	private boolean updateSourceCode(KnowledgeItem sourceCodeObject, String newSourceCode) {
		String sourceCode = getSourceCode(sourceCodeObject);
		if (!Utils.equals(sourceCode, newSourceCode)) {
			setSourceCode(sourceCodeObject, newSourceCode);
			return true;
		} else {
			return false;
		}
	}

	private Set<? extends KnowledgeItem> fetchSourceCodes(TLObject owner) {
		return resolveQuery(owner, _sourceCodesQuery);
	}

	private String getSourceCodeTableName() {
		return SOURCES_CODES_TABLE_NAME;
	}

	@Override
	protected String getImagesTableName() {
		return IMAGES_TABLE_NAME;
	}

}
