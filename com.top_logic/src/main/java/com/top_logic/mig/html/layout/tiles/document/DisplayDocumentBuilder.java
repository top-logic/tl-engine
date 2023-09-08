/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.document;

import java.util.Collection;
import java.util.Optional;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DisplayPDFControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a {@link FormContext} displaying a PDF document using the
 * {@link DisplayPDFControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayDocumentBuilder extends AbstractDisplayDocumentBuilder<DisplayDocumentBuilder.Config> {

	/**
	 * Configuration of a {@link DisplayDocumentBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DisplayDocumentBuilder> {

		/** Configuration name of the value of {@link #getDocumentName()}. */
		String DOCUMENT_NAME_ATTRIBUTE = "documentName";

		/**
		 * The name of the document that is displayed in the component.
		 */
		@Mandatory
		@Name(DOCUMENT_NAME_ATTRIBUTE)
		String getDocumentName();

	}

	/**
	 * Creates a new {@link DisplayDocumentBuilder}.
	 */
	public DisplayDocumentBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected FormMember createField(String fieldName, Object businessModel, LayoutComponent component) {
		FolderDefinition folder = getFolder(businessModel);
		if (folder == null) {
			return errorDocumentNotFound(fieldName, getConfig().getDocumentName());
		}
		Optional<BinaryData> document = getFirstMatchingDocument(folder);
		if (!document.isPresent()) {
			return errorDocumentNotFound(fieldName, getConfig().getDocumentName());
		}
		return createDisplayPDFField(fieldName, document.get());
	}

	private Optional<BinaryData> getFirstMatchingDocument(FolderDefinition folder) {
		Collection<Named> contents = folder.getContents();
		if (contents.isEmpty()) {
			return Optional.empty();
		}
		String documentName = getConfig().getDocumentName();
		return contents.stream()
			.filter(named -> documentName.equals(named.getName()) && named instanceof BinaryData)
			.map(BinaryData.class::cast)
			.findFirst();
	}

	/**
	 * Determines the {@link FolderDefinition} for the given context.
	 * 
	 * @param context
	 *        The context model to get {@link FolderDefinition} from. May be <code>null</code>.
	 * 
	 * @return A {@link FolderDefinition} containing the document. May be <code>null</code>.
	 */
	protected FolderDefinition getFolder(Object context) {
		if (context instanceof WebFolderAware) {
			return ((WebFolderAware) context).getWebFolder();
		}
		return null;
	}

	private FormMember errorDocumentNotFound(String fieldName, String documentName) {
		return errorField(fieldName, I18NConstants.ERROR_DOCUMENT_NOT_FOUND__DOCUMENT.fill(documentName));
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		FolderDefinition folder = getFolder(aModel);
		return folder != null && getFirstMatchingDocument(folder).isPresent();
	}

}
