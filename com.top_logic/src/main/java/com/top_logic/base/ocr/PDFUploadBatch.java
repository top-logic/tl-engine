/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.ocr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.Scheduler;

/**
 * After an Upload the given Documents can be sheduled for an OCR.
 * 
 * This will (when using a repository) result in a new Version od
 * the same document. Per default we assume a User named "ocr" and
 * use it to lock() and commit() the Documents. OCR may take some time,
 * assume about 5 seconds per Page. So this Batch will serialize all
 * OCR jobs thus avoiding congestion with such (expensive) Jobs.
 * 
 * @author    <a href="mailto:kha@top-logic.com>kha</a>
 */
public abstract class PDFUploadBatch extends BatchImpl {

    /** A List of file Uploads holding the Data */
    protected static LinkedList     uploads = new LinkedList();

    /** The currently running Batch, null if not running */
    protected static PDFUploadBatch current = null;
    
    /** A Special TLContext with the ocr-user included */
    protected TLContext ocrContext;

    /**
	 * Create a COSUploadBatch with a given aThreadName.
	 * 
	 * @param threadName
	 *        Must not be <code>null</code> or empty. Has to be unique.
	 */
	public PDFUploadBatch(String threadName) {
		super(threadName);
    }

    /**
     * Handle the actual upload here.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
	public void run() {
		// TODO #19482: FindBugs(ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD)
		// Every access to this variable has to be synchronized.
		// But the whole synchronization of this class is broken and has to be fixed.
        current = this;
		runInContext();
		current = null;
	}

	/**
	 * Implements the actual run. Typically {@link #ocrContext} is set and {@link #executeJob()} is
	 * executed in that context.
	 */
	protected abstract void runInContext();

	/**
	 * Executes the upload.
	 */
	protected void executeJob() {
		while (!getShouldStop()) {
			Object theObject;
			synchronized (uploads) {
				if (uploads.isEmpty())
					break; // done we are
				theObject = uploads.removeFirst();
			}
			if (theObject instanceof Document) {
				handleUpload((Document) theObject);
			}
			else {
				Logger.warn("Unexpected type of Object " + theObject, this);
			}
		}
	}

    /** 
     * Handle a single Upload, assuming a single aDocument. 
     *
     * @param aDocument a Document to Upload
     */
    protected void handleUpload(Document aDocument) {
        try {
            this.ocrDocument(aDocument);
        }     
        catch (Exception ex) {
            Logger.info ("ocrDocument failed", ex, this);
        }
    }
    
    /**
     * Do the actual upload after the Parameters where verified.
     */
    protected void ocrDocument(Document aDocument) throws Exception {

        String language = (String) aDocument
                .getValue(DCMetaData.LANGUAGE);

        File tmpIn  = null;
        File tmpOut = null;
        File tmpTxt = null;
        try {
			tmpIn = File.createTempFile("uploadA", ".pdf", Settings.getInstance().getTempDir());
			tmpOut = File.createTempFile("uploadB", ".pdf", Settings.getInstance().getTempDir());
            // This is PDFCompressor specific, mmh
            tmpTxt = new File(tmpOut.getParentFile(), tmpOut.getName() + ".txt");
			InputStream theStream = aDocument.getStream();
            try {
				FileUtilities.copyToFile(theStream, tmpIn);
			} finally {
				theStream.close();
			}
            TLPDFCompress compress = TLPDFCompress.getInstance();
            tmpOut.delete();
            tmpTxt.delete();
            int cResult = compress.run(tmpIn, tmpOut, language);
            if (0 == cResult) {
                if (Logger.isDebugEnabled(this)) {
                    Logger.debug("uploading OCR Part(" + aDocument.getName(),
                            this);
                }
                InputStream theStream1 = new FileInputStream(tmpOut);
                aDocument.update(theStream1);
                theStream1.close();
                if (tmpTxt.exists()) { // Optionally save .txt document, too
                    DataAccessProxy docDAP   = aDocument.getDAP();
                    DataAccessProxy parenDAP = docDAP.getParentProxy();
                    // The origanl idea to index the .txt files does not
                    // work since the Events are sent before we can save
                    // the textfile, so this is not actually used :-(
                    String txtName = docDAP.getName() + ".txt";
                    DataAccessProxy textDAP = parenDAP.getChildProxy(txtName);

					InputStream input = new FileInputStream(tmpTxt);
                    try {
						OutputStream txtOut;
						if (!textDAP.exists())
							txtOut = parenDAP.createEntry(txtName);
						else
							txtOut = textDAP.getEntryOutputStream();
						try {
							FileUtilities.copyStreamContents(input, txtOut);
						} finally {
							txtOut.close();
						}
					}
					finally {
					    input.close();
					}
                    if (!aDocument.getKnowledgeBase().commit()) {
                        Logger.error("Failed commit document " + aDocument, this);
                    }
                    else {
                    	Logger.info("OCRed document " + aDocument, this);
                    }
                }
            } else {
                Logger.error("Failed to OCR " + aDocument, this);
                // uploadFileOrig(parameters,aFile, aParent, aName);
            }
        } finally {
            if (tmpIn != null)
                tmpIn.delete();
            if (tmpOut != null)
                tmpOut.delete();
            if (tmpTxt != null)
                tmpTxt.delete();
        }
    }

    /**
     * Add a Job and and schedule a new batch if not already running.
     * 
     * @param aDocument
     *            should be a PDF Document but nos additiaol checkes are done
     */
    public static synchronized void addUpload(Document aDocument, String aPersonName) {
    	addUpload(aDocument, aPersonName, true);
    }
    
    /**
     * Add a Job and and schedule a new batch if not already running.
     * 
     * @param aDocument
     *            should be a PDF Document but nos additiaol checkes are done
     * @param aPersonName the name of the person in whose context the batch should run
     * @param startBatch if true the batch will be started immediately.
     * @return the created batch
     */
    public static synchronized PDFUploadBatch addUpload(Document aDocument, String aPersonName, boolean startBatch) {
        PDFUploadBatch theBatch = null;
        
        try {
            ThreadContext.pushSuperUser();
            TLContext ocrContext; 
            if (current != null && aPersonName.equals(current.ocrContext.getCurrentUserName())) {
                ocrContext = current.ocrContext;
            }
            else { // must create new Context 
				ocrContext = null;
            }
    
            DataAccessProxy theDap = aDocument.getDAP();
            if (theDap.isRepository()) {
				if (!theDap.lock()) // Lock in repository with correct context
					throw new DataObjectException("Cannot lock '" + theDap + "' for PDF Upload");
            }
            boolean start = current == null || uploads.isEmpty() 
				|| null == ocrContext; // must start with a different context.
            uploads.addLast(aDocument);
            if (start) {
				if (ocrContext != null) {
					theBatch = new InContextPDFUploadBatch(ocrContext);
				} else {
					theBatch = new InPersonPDFUploadBatch(aPersonName);
				}
            	if (startBatch) {
            		Scheduler r = Scheduler.getSchedulerInstance();
					SchedulerService.getInstance().execute(theBatch);
            	}
            }
        } finally {
            ThreadContext.popSuperUser();
        }
        
    	return theBatch;
    }
    
    /** 
     * Add a Job and and schedule a new batch if not already running.
     * 
     * Assume some "ocr" user to be configured.
     * 
     * @param aDocument should be a PDF Document but no additional checks are done
     */
    public static void addUpload(Document aDocument) {
    	addUpload(aDocument, true);
    }
    
    /** 
     * Add a Job and and schedule a new batch if not already running.
     * 
     * Assume some "ocr" user to be configured.
     * 
     * @param aDocument should be a PDF Document but no additional checks are done
     * @param startBatch if true the batch will be started immediately.
     */
    public static PDFUploadBatch addUpload(Document aDocument, boolean startBatch) {
        return addUpload(aDocument, "ocr", startBatch);
    }
}
