/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.Template;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link SingletonLinkStorage} for {@link Document} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DocumentStorage<C extends DocumentStorage.Config> extends SingletonLinkStorage<C> {

	/**
	 * Configuration options for {@link DocumentStorage}.
	 */
	@TagName("document-storage")
	public interface Config extends SingletonLinkStorage.Config<DocumentStorage<?>> {

		/** @see #getFolderLocator() */
		String FOLDER_PATH_PROPERTY = "folder-path";

		/**
		 * Algorithm to find the {@link WebFolder} for document storage.
		 * 
		 * @deprecated Use {@link TLFolderLocation} annotation instead of customizing the storage
		 *             implementation.
		 */
		@Name(FOLDER_PATH_PROPERTY)
		// Note: There is a format for AttributeValueLocator. Therefore, it cannot be replaced with
		// a configuration.
		@Deprecated
		AttributeValueLocator getFolderLocator();

	}

	/**
	 * {@link TLAttributeAnnotation} that provides an algorithm for looking up the folder in which
	 * to store an uploaded document.
	 */
	@TagName("folder")
	@TargetType(value = TLTypeKind.REF, name = Document.DOCUMENT_TYPE)
	@InApp
	public interface TLFolderLocation extends TLAttributeAnnotation {
		/**
		 * Algorithm that resolves the folder to store an uploaded document in.
		 */
		@Name("value")
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		@DefaultContainer
		PolymorphicConfiguration<? extends AttributeValueLocator> getValue();
	}

	private AttributeValueLocator _folderLocator;

	private AttributeValueLocator _templateLocator;

	/**
	 * Creates a {@link DocumentStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DocumentStorage(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		initFolderLocator(attribute);
		initTemplateLocator(attribute);
	}

	private void initFolderLocator(TLStructuredTypePart attribute) {
		AttributeValueLocator locator;
		TLFolderLocation annotation = attribute.getAnnotation(TLFolderLocation.class);
		if (annotation != null) {
			locator = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(annotation.getValue());
		} else {
			locator = ((Config) getConfig()).getFolderLocator();
		}

		_folderLocator = nonNull(locator);
	}

	private void initTemplateLocator(TLStructuredTypePart attribute) {
		AttributeValueLocator locator;
		Template annotation = attribute.getAnnotation(Template.class);
		if (annotation != null) {
			locator = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(annotation.getValue());
		} else {
			locator = null;
		}

		_templateLocator = nonNull(locator);
	}

	private AttributeValueLocator nonNull(AttributeValueLocator locator) {
		return locator == null ? x -> null : locator;
	}

	@Override
	protected void checkType(TLStructuredTypePart attribute, Object simpleValue) {
		if ((simpleValue != null) && (!(simpleValue instanceof Document)) && (!(simpleValue instanceof BinaryData))) {
			throw new IllegalArgumentException("Document attribute must be set to document or data item.");
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		if (aValue == null) {
			super.internalSetAttributeValue(aMetaAttributed, attribute, null);
			return;
		}

		Document document;
		if (aValue instanceof Document) {
			document = (Document) aValue;
		} else {
			BinaryData data = BinaryData.cast(aValue);
			WebFolder folder = getFolder(aMetaAttributed);
			document = folder.createOrUpdateDocument(data.getName(), data);
		}

		super.internalSetAttributeValue(aMetaAttributed, attribute, document);
	}

	/**
	 * @param object
	 *        the attributed object to locate the upload WebFolder
	 * @return the WebFolder where the Document should be uploaded
	 */
	public WebFolder getFolder(TLObject object) {
		return (WebFolder) this.getFolderLocator().locateAttributeValue(object);
	}

	/**
	 * @see Config#getFolderLocator()
	 */
	public AttributeValueLocator getFolderLocator() {
		return _folderLocator;
	}

	/**
	 * The {@link AttributeValueLocator} resolving a template document for this document attribute.
	 */
	public AttributeValueLocator getTemplateLocator() {
		return _templateLocator;
	}

	/**
	 * Resolves the template {@link Document} that should be used to create this document
	 * 
	 * @param baseObject
	 *        The object defining the attribute this storage implementation is responsible for.
	 * @return The {@link Document} that should be used as template for creating new documents to
	 *         store in the attribute this storage implementation is responsible for.
	 */
	public Document locateTemplate(TLObject baseObject) {
		return (Document) getTemplateLocator().locateAttributeValue(baseObject);
	}

	/**
	 * Resolves the {@link WebFolder} to store new documents in.
	 * 
	 * @param baseObject
	 *        The object defining the attribute this storage implementation is responsible for.
	 * @return The {@link WebFolder} to store documents in.
	 */
	public WebFolder locateFolder(TLObject baseObject) {
		return (WebFolder) getFolderLocator().locateAttributeValue(baseObject);
	}
}