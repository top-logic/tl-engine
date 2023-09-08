/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;

/**
 * Class for zipping files and folders.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Zipper implements Closeable {

	private static final char ZIP_ENTRY_PATH_SEPARATOR = '/';

	private final static int BUFFER = 2048;

    /** The ZIP stream to write the zipped data to. */
    private ZipOutputStream stream;

    /** The number of added files. */
    private int files;

    /**
     * @param    aName    The name of the ZIP file to be created.
     */
    public Zipper(String aName) throws FileNotFoundException {
		this(new File(aName));
    }

    /**
	 * @param file
	 *        The file to write zip content to.
	 */
	public Zipper(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}

	/**
	 * @param out
	 *        The outputStream to write content to.
	 */
	public Zipper(OutputStream out) {
		super();
		stream = new ZipOutputStream(new BufferedOutputStream(out));
	}

	/**
	 * Return the string representation of this instance.
	 * 
	 * @return The string representation.
	 */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    ", open: " + (this.stream != null) +
                    ", #files: " + this.files +
                    ')');
    }

    /**
     * Add the folder defined by the given name to this ZIP file.
     * 
     * This method will add all files in this folder and also all sub folders.
     * 
     * @param    aPath    The path of the folder to be added.
     * @return   The total number of files added.
     * 
     * @throws   FileNotFoundException    If there is no folder with the given name.
     * @throws   IOException              If operation fails for a reason.
     */
    public int addFolder(String aPath) throws FileNotFoundException, IOException {
		return addFolder(new File(aPath));
    }

	/**
	 * Add the given folder to this ZIP file.
	 * 
	 * This method will add all files in this folder and also all sub folders.
	 * 
	 * @param aDir
	 *        The folder to be added.
	 * @return The total number of files added.
	 * 
	 * @throws FileNotFoundException
	 *         If there is no folder with the given name.
	 * @throws IOException
	 *         If operation fails for a reason.
	 */
	public int addFolder(File aDir) throws FileNotFoundException, IOException {
		return addFolder(aDir, aDir.getName());
	}

    /**
     * Add the folder defined by the given name to this ZIP file.
     * 
     * This method will add all files in this folder and also all sub folders.
     * 
     * @param    aPath      The path of the folder to be added.
     * @param    aParent    The name of the parent folder to be used in the ZIP file.
     * @return   The total number of files added.
     * 
     * @throws   FileNotFoundException    If there is no folder with the given name.
     * @throws   IOException              If operation fails for a reason.
     */
    public int addFolder(String aPath, String aParent) throws FileNotFoundException, IOException {
        return (this.addFolder(new File(aPath), aParent));
    }

    /**
     * Add the given folder to this ZIP file.
     * 
     * This method will add all files in this folder and also all sub folders.
     * 
     * @param    aDir       The folder to be added.
     * @param    aParent    The name of the parent folder to be used in the ZIP file.
     * @return   The total number of files added.
     * 
     * @throws   FileNotFoundException    If the given file is no folder.
     * @throws   IOException              If operation fails for a reason.
     */
    public int addFolder(File aDir, String aParent) throws FileNotFoundException, IOException {
        if (!aDir.isDirectory()) {
            throw new FileNotFoundException("Given file is no folder (was: '" + 
                                            aDir + "')!");
        }

        int    theCount;
        String theName;
        File   theChild;
		File theFiles[] = FileUtilities.listFiles(aDir);

		if (theFiles.length > 0) {
			for (int thePos = 0; thePos < theFiles.length; thePos++) {
				theChild = theFiles[thePos];
				theName = aParent + ZIP_ENTRY_PATH_SEPARATOR + theChild.getName();

				if (theChild.isFile()) {
					theCount = this.addFile(theChild, theName);

					Logger.info("Added: '" + theFiles[thePos] + "' (" +
						theCount + " bytes)", this);
				} else {
					this.addFolder(theChild, theName);
				}
			}
		} else {
			getStream().putNextEntry(new ZipEntry(aParent + ZIP_ENTRY_PATH_SEPARATOR));
		}

        return (this.files);
    }

	/**
	 * Adds the folder given by this resource path to the zipped file.
	 * 
	 * @param path
	 *        Resource path to the folder that should be added to the ZIP file.
	 * @param name
	 *        The name of the folder to be used in the ZIP file.
	 */
	public int addFolderByResourcePath(String path, String name) throws FileNotFoundException, IOException {
		FileManager fileManager = FileManager.getInstance();
		if (!fileManager.isDirectory(path)) {
			throw new FileNotFoundException("Given file is no folder (was: '" + path + "')!");
		}

		Set<String> subPaths = fileManager.getResourcePaths(path);

		if (!subPaths.isEmpty()) {
			for (String subPath : subPaths) {
				String filename = FileUtilities.getFilenameOfResource(subPath);
				String zipName = name + ZIP_ENTRY_PATH_SEPARATOR + filename;
				if (fileManager.isDirectory(subPath)) {
					this.addFolderByResourcePath(subPath, zipName);
				} else {
					this.addFile(fileManager.getStream(subPath), zipName);
				}
			}
		} else {
			getStream().putNextEntry(new ZipEntry(name + ZIP_ENTRY_PATH_SEPARATOR));
		}

		return (this.files);
	}

    /**
     * Append the given file to the ZIP file.
     * 
     * @param    aFile    The file to be zipped.
     * @return   The number of bytes written to the ZIP file.
     * 
     * @throws   FileNotFoundException    If the file to be written cannot be found. 
     * @throws   IOException              If writing or reading fails.
     */
    public int addFile(File aFile) throws FileNotFoundException, IOException {
        if ((aFile == null) || (!aFile.isFile())) {
            throw new FileNotFoundException("Given file is no normal file (was: " + 
                                            aFile + ")!");
        }

        return (this.addFile(aFile, aFile.getName()));
    }

    /**
     * Append the given file to the ZIP file.
     * 
     * @param    aFile    The file to be zipped.
     * @param    aName    The name of the entry in the ZIP file.
     * @return   The number of bytes written to the ZIP file.
     * 
     * @throws   FileNotFoundException    If the file to be written cannot be found. 
     * @throws   IOException              If writing or reading fails.
     */
    public int addFile(File aFile, String aName) throws FileNotFoundException, IOException {
        return (this.addFile(new FileInputStream(aFile), aName));
    }

    /**
     * Append the given stream to the ZIP file.
     * 
     * @param    aStream    The stream containing the data to be zipped.
     * @param    aName      The name of the entry in the ZIP file.
     * @return   The number of bytes written to the ZIP file.
     * 
     * @throws   FileNotFoundException    If the file to be written cannot be found. 
     * @throws   IOException              If writing or reading fails.
     */
    public int addFile(InputStream aStream, String aName) throws IOException {
        int                 theCount;
        int                 theSum    = 0;
        byte                theData[] = new byte[BUFFER];
        ZipEntry            theEntry  = new ZipEntry(aName);
		try (BufferedInputStream theIn = new BufferedInputStream(aStream, BUFFER)) {
			ZipOutputStream theZip = this.getStream();

			if (theZip == null) {
				throw new IOException("ZIP file is closed for this instance.");
			}

			theZip.putNextEntry(theEntry);

			while ((theCount = theIn.read(theData, 0, BUFFER)) != -1) {
				theZip.write(theData, 0, theCount);

				theSum += theCount;
			}

        }

        this.files++;

        return (theSum);
    }

    /**
     * Close the ZIP file.
     * 
     * After closing it, this instance will no longer pack ZIP files.
     * 
     * @throws    IOException    If closing the file fails.
     */
	@Override
	public void close() throws IOException {
		this.stream.close();
    }

    /**
     * Return the ZIP stream to write data to.
     * 
     * @return    The requested ZIP stream for writing.
     */
	public final ZipOutputStream getStream() {
		return this.stream;
    }

    /** 
     * Zip the given file and return the resulting ZIP stream.
     * 
     * @param    aFile    The file to be zipped, must not be <code>null</code>.
     * @return   The requested ZIP stream, never <code>null</code>.
     * @throws   IOException    If zipping fails for a reason.
     */
    public static InputStream zip(File aFile) throws IOException {
    	return Zipper.zip(aFile, aFile.getName());
    }

    /** 
     * Zip the given file and return the resulting ZIP stream.
     * 
     * @param    aFile    The file to be zipped, must not be <code>null</code>.
     * @param    aName    The name of the file to be zipped, may be <code>null</code>.
     * @return   The requested ZIP stream, never <code>null</code>.
     * @throws   IOException    If zipping fails for a reason.
     */
    public static InputStream zip(File aFile, String aName) throws IOException {
        if (aFile == null) {
            throw new IllegalArgumentException("Given file is null!");
        }
        else if (!aFile.exists()) {
            throw new IllegalArgumentException("Given file '" + aFile.getName() + "' doesn't exist!");
        }
        else {
            return Zipper.zip(new File[] {aFile}, new String[] {aName});
        }
    }
    
    /** 
     * Zip the given files and return the resulting ZIP stream.
     * 
     * @param    someFiles    The files to be zipped, must not be <code>null</code>.
     * @return   The requested ZIP stream, may be <code>null</code> if given array is empty.
     * @throws   IOException    If zipping fails for a reason.
     */
    public static InputStream zip(File[] someFiles) throws IOException {
        if (someFiles == null) {
            throw new IllegalArgumentException("Given file array is null!");
        }

        return Zipper.zip(someFiles, new String[someFiles.length]);
    }        

    /** 
     * Zip the given files and return the resulting ZIP stream.
     * 
     * @param    someFiles    The files to be zipped, must not be <code>null</code>.
     * @param    someNames    The names of the files to be zipped, must not be <code>null</code> 
     *                        and be of same length of "someFiles".
     * @return   The requested ZIP stream, may be <code>null</code> if given array is empty.
     * @throws   IOException    If zipping fails for a reason.
     */
    public static InputStream zip(File[] someFiles, String[] someNames) throws IOException {
    	if (someFiles == null) {
    		throw new IllegalArgumentException("Given file array is null!");
    	}
    	else if (someFiles.length != someNames.length) {
        	throw new IllegalArgumentException("Length of file name array doesn`t match to length of file array!");
        }

        File   theTemp   = File.createTempFile("tlzip", ".tlzip", Settings.getInstance().getTempDir());
		try (Zipper theZipper = new Zipper(theTemp)) {
			for (int thePos = 0; thePos < someFiles.length; thePos++) {
				File theFile = someFiles[thePos];

				if (theFile != null) {
					if (StringServices.isEmpty(someNames[thePos])) {
						theZipper.addFile(theFile);
					} else {
						theZipper.addFile(theFile, someNames[thePos]);
					}
				}
			}
        }

        return new FileInputStream(theTemp);
    }
}
