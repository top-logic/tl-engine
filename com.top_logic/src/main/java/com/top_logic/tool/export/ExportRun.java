/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.label.ObjectLabel;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tool.export.Export.State;

/**
 * <p>
 * The ExportRun implements a basic procedure of executing an export.
 * </p>
 * <p>
 * The procedure includes switching the export states from {@link State#RUNNING} to
 * {@link State#FINISHED} or {@link State#FAILED}. The result of the export will be stored
 * in a {@link Document} which can be obtained through {@link Export#getDocument()}.
 * </p>
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ExportRun /*implements Runnable*/ {

	private static final String WEBFOLDER_NAME = ExportRun.class.getName();

	private static final String WEBFOLDER_LABEL = "class:" + WEBFOLDER_NAME;

    private final Export export;
    private final ExportHandler handler;

    public ExportRun(Export anExport) {
        this.export = anExport;
        this.handler = ExportHandlerRegistry.getInstance().getHandler(anExport.getExportHandlerID());
    }

    public Export getExport() {
        return this.export;
    }

    public void run() {

        if (State.RUNNING != this.export.getState()) {
            Logger.warn("Export " + this.export + " is not in state '" + State.RUNNING + "'. Ignoring this export.", ExportRun.class);
            return;
        }

        String theHandlerID = this.export.getExportHandlerID();
        try {

            ExportHandler theHandler = this.handler;
            Object theModel = this.export.getModel();
            File theTmpFile = File.createTempFile(theHandlerID, "tmpfile", Settings.getInstance().getTempDir());
            FileOutputStream theOut = new FileOutputStream(theTmpFile);
            ExportResult theResult = new DefaultExportResult(theOut);

			{
                theHandler.exportObject(theModel, theResult);
                theOut.close();

                if (this.checkResult(theResult)) {
                    KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
                    
					Document theDocument;
					try (Transaction theTrans = theKB.beginTransaction()) {
						WebFolder theFolder = this.getOrCreateWebfolder(theKB);
						theDocument = this.export.getDocument();

						FileInputStream theIn = new FileInputStream(theTmpFile);
						if (theDocument == null || !theDocument.tValid()) {
							String theDocumentID = this.createDocumentID(theResult.getFileExtension());
							theDocument = (Document) theFolder.getChildByName(theDocumentID);
							if (theDocument == null) {
								theDocument = theFolder.createOrUpdateDocument(theDocumentID, theIn);
							} else {
								theDocument.update(theIn);
							}
						} else {
							theDocument.update(theIn);
						}
						theIn.close();

						try {
							theTrans.commit();
						} catch (KnowledgeBaseException e) {
							Logger.warn("Commit of export " + this.export + " failed.", e, ExportRun.class);
						}
                    }

                    this.export.setStateFinished(theDocument, theResult.getFileDisplaynameKey(), theResult.getFileExtension());
                }
                else {
                    this.export.setStateFailed(theResult.getFailureKey());
                }

            }
            
            theOut.close();
        }
        catch (IOException ex) {
            Logger.warn("Export failed: " + this.export, ex, ExportRun.class);
			this.export.setStateFailed(I18NConstants.ERROR_WRITE_FAILED);
        }
        catch (Throwable ex) {
        	Logger.warn("Export failed: " + this.export, ex, ExportRun.class);
			this.export.setStateFailed(I18NConstants.ERROR_UNKNOWN_ERROR);
        }
    }

    /**
     * Return a generic id without special characters
     */
    private String createDocumentID(String aFileextension) {
		String theID = IdentifierUtil.toExternalForm(StringID.createRandomID());
        theID = theID.replaceAll("[-:^$&%?§]", StringServices.EMPTY_STRING);
        return theID + Export.EXTENSION_SEPARATOR + aFileextension;
    }

    private boolean checkResult(ExportResult aResult) {

        if (aResult.getFailureKey() != null) {
            return false;
        }

        if (StringServices.isEmpty(aResult.getFileExtension())) {
            throw new IllegalArgumentException("File extension in export result must not be null! ");
        }

        if (aResult.getFileDisplaynameKey() == null) {
            throw new IllegalArgumentException("File name in export result must not be null! ");
        }

        return true;
    }

    private WebFolder getOrCreateWebfolder(KnowledgeBase aKB) {
		KnowledgeObject theKO = ObjectLabel.getLabeledObject(WEBFOLDER_LABEL);
        if (theKO != null) {
			return (WebFolder) WrapperFactory.getWrapper(theKO);
        }
		WebFolder newFolder =
			WebFolderFactory.getInstance().createNewWebFolder(WEBFOLDER_NAME, WebFolderFactory.STANDARD_FOLDER);
		ObjectLabel.createLabel(WEBFOLDER_LABEL, newFolder.tHandle());
		return newFolder;
    }
}
