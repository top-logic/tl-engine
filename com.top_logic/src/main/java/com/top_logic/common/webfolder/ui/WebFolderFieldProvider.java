/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.common.folder.FolderFieldProvider;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.common.webfolder.model.ProxyFolderContent;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.clipboard.ModifyClipboardExecutable;
import com.top_logic.common.webfolder.ui.commands.UpdateExecutable;
import com.top_logic.common.webfolder.ui.commands.VersionExecutable;
import com.top_logic.common.webfolder.ui.commands.WebFolderDeleteExecutable;
import com.top_logic.common.webfolder.ui.keywords.ShowKeywordExecutable;
import com.top_logic.common.webfolder.ui.similar.ShowSimilarExecutable;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderFieldProvider extends FolderFieldProvider {

	/**
	 * Creates a {@link WebFolderFieldProvider}.
	 */
	public WebFolderFieldProvider(ExecutableState allowClipboard, ExecutableState allowWrite,
			ExecutableState allowDelete) {
		super(allowClipboard, allowWrite, allowDelete);
	}

    @Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
		String fieldName = getFieldName(aModel, anAccessor, aProperty);
    	final Object businessObject;
    	if (aModel instanceof FolderNode) {

			FolderNode node = (FolderNode) aModel;
			if (WebFolderAccessor.UPDATE.equals(aProperty)) {
				return this.createUpdateGroup(fieldName, node);
			} else if (WebFolderAccessor.DELETE.equals(aProperty)) {
				return this.createDeleteField(fieldName, node);
			} else if (WebFolderAccessor.KEYWORDS.equals(aProperty)) {
				return this.createKeywordsField(fieldName, node);
			} else if (WebFolderAccessor.SIMILAR_DOCUMENTS.equals(aProperty)) {
				return this.createSimilarDocsField(fieldName, node);
			}

			businessObject = ((FolderNode) aModel).getBusinessObject();
		} else {
			businessObject = aModel;
    	}

        if (WebFolderAccessor.DOWNLOAD.equals(aProperty)) {
			return this.createDownloadField(fieldName, retrieveDocument(businessObject));
        }
        else if (WebFolderAccessor.CLIPBOARD.equals(aProperty)) {
			return this.createAdd2ClipboardField(fieldName, businessObject, getAllowClipboard());
        }
        else if (WebFolderAccessor.VERSION.equals(aProperty)) {
			return this.createVersionField(fieldName, retrieveDocument(businessObject));
        }
        else { 
            return null;
        }
    }

	private Object retrieveDocument(final Object businessObject) {
		return DocumentAccessor.DOCUMENT_MAPPING.map(businessObject);
	}

	protected FormMember createKeywordsField(String fieldName, FolderNode node) {
		Object userObject = node.getBusinessObject();
		if (userObject instanceof Document) {
			return createShowKeywordsCommand(fieldName, node);
		} else if (userObject instanceof DocumentVersion) {
			return createShowKeywordsCommand(fieldName, folderContentForDocumentVersion(node));
		} else {
			return this.createHiddenField(fieldName);
		}
	}

	private FormMember createShowKeywordsCommand(String fieldName, FolderContent content) {
		Command theExecutable = new ShowKeywordExecutable(content);

		CommandField showKeywordsCommand =
			WebFolderFieldProvider.createField(fieldName, theExecutable,
				com.top_logic.common.webfolder.ui.keywords.Icons.KEYWORDS,
				com.top_logic.common.webfolder.ui.keywords.Icons.KEYWORDS_DISABLED);
		showKeywordsCommand.setNotExecutableReasonKey(I18NConstants.KEYWORDS_SEARCH_NOT_POSSIBLE);

		return showKeywordsCommand;
	}

	protected FormMember createSimilarDocsField(String fieldName, FolderNode node) {
		Object userObject = node.getBusinessObject();
		if (userObject instanceof Document) {
			return createShowSimilarCommand(fieldName, node);
		} else if (userObject instanceof DocumentVersion) {
			return createShowSimilarCommand(fieldName, folderContentForDocumentVersion(node));
		} else {
			return this.createHiddenField(fieldName);
		}
	}

	private FormMember createShowSimilarCommand(String fieldName, FolderContent content) {
		Command theExecutable = new ShowSimilarExecutable(content);

		CommandField showSimilarCommand =
			WebFolderFieldProvider.createField(fieldName, theExecutable,
				com.top_logic.common.webfolder.ui.similar.Icons.SIMILAR_OBJECTS,
				com.top_logic.common.webfolder.ui.similar.Icons.SIMILAR_OBJECTS_DISABLED);
		showSimilarCommand.setNotExecutableReasonKey(I18NConstants.SIMILAR_DOCUMENT_SEARCH_NOT_POSSIBLE);

		return showSimilarCommand;
	}

	private FolderContent folderContentForDocumentVersion(final FolderContent content) {
		return new ProxyFolderContent() {

			@Override
			public Object getContent() {
				return ((DocumentVersion) super.getContent()).getDocument();
			}

			@Override
			protected FolderContent getImplementation() {
				return content;
			}
		};
	}



	@Override
	protected FormMember folderDownload(String name, Object userObject) {
		if ((userObject instanceof WebFolder) && WebFolderUIFactory.getInstance().getProvideZipDownload()) {
			WebFolder theFolder = (WebFolder) userObject;
			SingleSelectionModel theModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);

			theModel.setSingleSelection(theFolder);

			return getUIFactory().createZipDownloadField(theModel, name, com.top_logic.tool.export.Icons.DOWNLOAD,
				com.top_logic.tool.export.Icons.DOWNLOAD_DISABLED);
		}
		return this.createHiddenField(name);
	}

	protected FormMember createAdd2ClipboardField(String name, final Object businessObject, ExecutableState allowAdd) {
		CommandField field = ModifyClipboardExecutable.createField((Wrapper) businessObject, name, allowAdd);
		field.setInheritDeactivation(false);
		return field;
    }

	/**
	 * Get factory for UI
	 * 
	 * @return the Factory for creating the UI
	 */
	protected WebFolderUIFactory getUIFactory() {
		return WebFolderUIFactory.getInstance();
	}
    
	/**
	 * Creates the group holding the "update" button of a document.
	 * 
	 * @param name
	 *        The name of the group.
	 * @param node
	 *        Never null.
	 */
	protected FormMember createUpdateGroup(String name, FolderNode node) {
		if (!(node.getBusinessObject() instanceof Document)) {
			return createHiddenField(name);
		}
		FormGroup group = new FormGroup(name, I18NConstants.TABLE);
		if (getAllowWrite().isExecutable()) {
			CommandField updateField = createUpdateField(node);
			group.addMember(updateField);
			NotExecutableListener.createNotExecutableReasonKey(I18NConstants.FIELD_DISABLED, updateField)
				.addAsListener(group);
		}
		return group;
	}

	/**
	 * Creates the "update" button.
	 * 
	 * @param node
	 *        Never null.
	 * @return Is not allowed to be null.
	 */
	protected CommandField createUpdateField(FolderNode node) {
		Command command = new UpdateExecutable(node);
		return createField("updateField", command, com.top_logic.common.webfolder.ui.commands.Icons.DOC_COMMIT,
			com.top_logic.common.webfolder.ui.commands.Icons.DOC_COMMIT_DISABLED);
	}

	protected FormMember createVersionField(String name, Object userObject) {
		if (userObject instanceof Document) {
			Document document = (Document) userObject;
			Command theExecutable = new VersionExecutable(document);
			CommandField theField = WebFolderFieldProvider.createField(name, theExecutable, null, null);
			theField.setInheritDeactivation(false);
			theField.setLabel(String.valueOf(document.getVersionNumber()));
            return theField;
        }
        else {
			return this.createHiddenField(name);
        }
    }

	/**
	 * Workaround for {@link FormTableModel} not accepting null as value of one cell.
	 * 
	 * @param aName
	 *        The name of the field, must not be <code>null</code>.
	 * @return The hidden field without label.
	 */
	protected HiddenField createHiddenField(String aName) {
		HiddenField theField = FormFactory.newHiddenField(aName);

		theField.setLabel("");

		return theField;
	}

    public static CommandField createField(String aName, Command anExecutable, ThemeImage anImage, ThemeImage aDisabled) {
        CommandField theField = FormFactory.newCommandField(aName, anExecutable);
        
        theField.setImage(anImage);
        theField.setNotExecutableImage(aDisabled);

        return theField;
    }

	@Override
	protected Command getDeleteExecutable(String name, Object aModel) {
		if (aModel instanceof FolderNode) {
			return new WebFolderDeleteExecutable((FolderNode) aModel);
		}
		return null;
	}

}

