/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The ZipUtil contains useful methods for zip.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ZipUtil {

    // Contants
    
    /** A constant for the root dir of the zip file. */
    public static final String DIR_ROOT = "";

    private ZipUtil() {
        // Use the static methods.
    }

    /**
     * This method writes all webfolder and documents of the given attributed to
     * the zip output stream. The documents are stored in to the directory (e.g.
     * 'documents/'). Should the documents stored into the zip root dir use the
     * {@link #DIR_ROOT} constant. NOTE, the given directory must end with a
     * slash. The slash is the indicator that the name is an directory. The map
     * contains the zip entry names of all zip entries which are already write
     * out to the output stream. Check for all new entries if the given entry
     * already exist otherwise an exception will thrown. For all new entries the
     * map must be updated.
     * 
     * Note, this method closes NOT the output stream!
     * 
     * @param aZipOut
     *        A zip output stream. Must not be <code>null</code>.
     * @param someZipEntryNames
     *        A map with zip entry names. Key: the entry name (e.g. 'documents/help/' 
     *        or 'documents/help/bla.doc'. Value: an arbitrary Object. If the map
     *        not contains an object for an key that means the entry does not exist. 
     *        Must not be <code>null</code>.
     * @param aDirectoryName
     *        A directory to store the documents of the given attributed. Must
     *        not be <code>null</code>.
     * @param anAttributed
     *        An attributed. Must not be <code>null</code>.
     */
	public static void write(ZipOutputStream aZipOut, Map someZipEntryNames, String aDirectoryName,
			Wrapper anAttributed) throws IOException {
        // Write root directory for the attributed but the root dir for the
        // attributed must not be the root dir of the zip file.
        ZipEntry directoryEntry = new ZipEntry(aDirectoryName);
        if(notExists(someZipEntryNames, aDirectoryName)) {
            aZipOut.putNextEntry(directoryEntry);
            aZipOut.closeEntry();
            someZipEntryNames.put(aDirectoryName, Boolean.TRUE);
        }
        
        // Get the document meta attributes und webfolder meta attributes
		for (TLStructuredTypePart part : anAttributed.tType().getAllParts()) {
			if (AttributeOperations.isWebFolderAttribute(part)) {
				WebFolder webFolder = (WebFolder) anAttributed.getValue(part.getName());
                if (webFolder != null) {
                    write(aZipOut, someZipEntryNames, aDirectoryName, webFolder);
                }
            }
        }
    }
 
    /** This methods writes a web folder to the given zip output stream.
     *  See {@link #write(ZipOutputStream, Map, String, Wrapper)}. */
	public static void write(ZipOutputStream aZipOut, Map someZipEntryNames, String aDirectoryName,
			WebFolder aWebFolder) throws IOException {
        ZipEntry directoryEntry = new ZipEntry(aDirectoryName);
        if(notExists(someZipEntryNames, aDirectoryName)) {
            aZipOut.putNextEntry(directoryEntry);
            aZipOut.closeEntry();
            someZipEntryNames.put(aDirectoryName, Boolean.TRUE);
        }
        
        String   webFolderName  = directoryEntry.getName() + MetaLabelProvider.INSTANCE.getLabel(aWebFolder) + "/";
        ZipEntry webFolderEntry = new ZipEntry(webFolderName);
        if(notExists(someZipEntryNames, webFolderName)) {
            aZipOut.putNextEntry(webFolderEntry);
            aZipOut.closeEntry();
            someZipEntryNames.put(webFolderName, Boolean.TRUE);
        }
        
		Collection<? extends TLObject> childs = aWebFolder.getContent();
        for (Iterator childIter = childs.iterator(); childIter.hasNext();) {
			TLObject child = (TLObject) childIter.next();
            
            if (child instanceof Document) {
                write(aZipOut, someZipEntryNames, webFolderName, (Document) child);
            } else if (child instanceof WebFolder) {
                write(aZipOut, someZipEntryNames, webFolderName, (WebFolder) child);
            }
        }
    }
    
    /** This methods writes a document to the given zip output stream.
     *  See {@link #write(ZipOutputStream, Map, String, Wrapper)}. */
	public static void write(ZipOutputStream aZipOut, Map someZipEntryNames, String aDirectoryName, Document aDocument)
			throws IOException {
        ZipEntry directoryEntry = new ZipEntry(aDirectoryName);
        if(notExists(someZipEntryNames, aDirectoryName)) {
            aZipOut.putNextEntry(directoryEntry);
            aZipOut.closeEntry();
            someZipEntryNames.put(aDirectoryName, Boolean.TRUE);
        }
        
        String   documentName  = directoryEntry.getName() + aDocument.getName();
        ZipEntry documentEntry = new ZipEntry(documentName);
        if(notExists(someZipEntryNames, documentName)) {
            aZipOut.putNextEntry(documentEntry);
			InputStream content = aDocument.getStream();
			try {
				writeInput(aZipOut, content);
			} finally {
				content.close();
			}
            aZipOut.closeEntry();
            someZipEntryNames.put(documentName, Boolean.TRUE);
        }
    }
    
    private static boolean notExists(Map someZipEntryNames, String anEntryName) {
        // The root directory always exists.
        if (anEntryName.equals(DIR_ROOT)) {
            return false;
        }
        
        Object value = someZipEntryNames.get(anEntryName);
        
        return value == null;
    }
    
    private static void writeInput(ZipOutputStream aOut, InputStream aIn) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = aIn.read(buffer)) > 0) {
            aOut.write(buffer, 0, length);
        }
    }
    
}

