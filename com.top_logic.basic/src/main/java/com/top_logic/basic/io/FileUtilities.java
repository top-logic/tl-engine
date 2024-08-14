/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.path.PathCollector;
import com.top_logic.basic.util.RegExpUtil;

/**
 * The FileUtilities contain some useful classes to handle files and directories.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class FileUtilities extends StreamUtilities {

	/**
	 * The characters that are not allowed in a filename on Windows.
	 * <p>
	 * There is no such constant for Linux, as it depends on the file system. But usually, only the
	 * "/" character is not allowed on Linux.
	 * </p>
	 */
	public static final String INVALID_FILENAME_WINDOWS = "\\/:*?\"<>|";

	/** File name ending to detect XML files. */
	public static final String XML_FILE_ENDING = ".xml";

	/**
	 * Error message for a file that is not well XML formatted.
	 */
	public static final String FILE_NOT_XML_FORMATTED = "%s is not well XML formatted.";

	/**
	 * Cascading Stylesheet filename suffix.
	 */
	public static final String CSS_FILE_ENDING = ".css";

	/**
	 * Zip filename suffix.
	 */
	public static final String ZIP_FILE_ENDING = ".zip";

	/**
	 * Path separator for filenames.
	 */
	public static final String PATH_SEPARATOR = "/";

	/**
	 * Token represents the parent of the current directory.
	 */
	public static final String PARENT_PATH_TOKEN = "..";

	/** {@link FileFilter} matching all filed ending with ".xml". */
	public static final FileFilter XML_FILE_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(XML_FILE_ENDING);
		}
	};

	/** {@link FileFilter} matching all {@link File#isDirectory() directories}. */
	public static final FileFilter IS_DIRECTORY_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};

	/** {@link FileFilter} matching all {@link File#isFile() normal files}. */
	public static final FileFilter IS_FILE_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	};

	/**
	 * Makes sure, that a complete directory path will exist. If it does NOT exist, it will be
	 * created.
	 * 
	 * Convenience redirect of {@link #enforceDirectory(File)}
	 * 
	 * @param dirName
	 *        the name of the directory
	 * @throws IOException
	 *         When creation of directory failed, or file is an existing file but not a directory.
	 */
	public static void enforceDirectory(String dirName) throws IOException {
		enforceDirectory(new File(dirName));
	}
	
    /**
	 * Makes sure, that a complete directory path will exist. If it does NOT exist, it will be
	 * created.
	 * 
	 * @see #enforceDirectory(String)
	 *
	 * @param dirName
	 *        the name of the directory
	 * @throws IOException
	 *         When creation of directory failed, or file is an existing file but not a directory.
	 */
	public static void enforceDirectory(File dirName) throws IOException {
        File directory = dirName;
        if (!directory.exists ()) {
            // attention: mkdirs, with an trailing s means
            // create ALL missing directories
            assert System.getProperty("os.name").startsWith("Windows") 
                || dirName.getPath().charAt(1) != ':' :
                    "No Drive Letters with '" + System.getProperty("os.name") + "' please";
            
            boolean success = directory.mkdirs ();
			if (!success) {
				throw new IOException("Unable to create directory: " + dirName);
			}
		} else {
			if (!directory.isDirectory()) {
				throw new IOException(dirName + " already exists, but is not a directory.");
			}
        }
    }

	/**
	 * Utility that converts a potential <code>null</code> result from {@link File#listFiles()} into
	 * an {@link IOException}.
	 * 
	 * @param dir
	 *        The directory to list.
	 * @return The content {@link File}s.
	 * @throws IOException
	 *         If the given {@link File} is not a directory, or it cannot be accessed.
	 * 
	 * @see File#listFiles()
	 */
	public static File[] listFiles(File dir) throws IOException {
		File[] contents = dir.listFiles();
		if (contents == null) {
			throw cannotList(dir);
		}
		return contents;
	}

	/**
	 * Utility that converts a potential <code>null</code> result from {@link File#list()} into an
	 * {@link IOException}.
	 * 
	 * @param dir
	 *        The directory to list.
	 * @return The content {@link File}s.
	 * @throws IOException
	 *         If the given {@link File} is not a directory, or it cannot be accessed.
	 * 
	 * @see File#list()
	 */
	public static String[] list(File dir) throws IOException {
		String[] contents = dir.list();
		if (contents == null) {
			throw cannotList(dir);
		}
		return contents;
	}

	/**
	 * Utility that converts a potential <code>null</code> result from
	 * {@link File#listFiles(FilenameFilter)} into an {@link IOException}.
	 * 
	 * @param dir
	 *        The directory to list.
	 * @param filter
	 *        See {@link File#listFiles(FilenameFilter)}.
	 * @return The content {@link File}s.
	 * @throws IOException
	 *         If the given {@link File} is not a directory, or it cannot be accessed.
	 * 
	 * @see File#listFiles(FilenameFilter)
	 */
	public static File[] listFiles(File dir, FilenameFilter filter) throws IOException {
		File[] contents = dir.listFiles(filter);
		if (contents == null) {
			throw cannotList(dir);
		}
		return contents;
	}

	/**
	 * Utility that converts a potential <code>null</code> result from
	 * {@link File#list(FilenameFilter)} into an {@link IOException}.
	 * 
	 * @param dir
	 *        The directory to list.
	 * @param filter
	 *        See {@link File#list(FilenameFilter)}.
	 * @return The content {@link File}s.
	 * @throws IOException
	 *         If the given {@link File} is not a directory, or it cannot be accessed.
	 * 
	 * @see File#list(FilenameFilter)
	 */
	public static String[] list(File dir, FilenameFilter filter) throws IOException {
		String[] contents = dir.list(filter);
		if (contents == null) {
			throw cannotList(dir);
		}
		return contents;
	}

	/**
	 * Utility that converts a potential <code>null</code> result from {@link File#listFiles()} into
	 * an {@link IOException}.
	 * 
	 * @param dir
	 *        The directory to list.
	 * @param filter
	 *        See {@link File#listFiles(FileFilter)}.
	 * @return The content {@link File}s.
	 * @throws IOException
	 *         If the given {@link File} is not a directory, or it cannot be accessed.
	 * 
	 * @see File#listFiles()
	 */
	public static File[] listFiles(File dir, FileFilter filter) throws IOException {
		File[] contents = dir.listFiles(filter);
		if (contents == null) {
			throw cannotList(dir);
		}
		return contents;
	}

	private static IOException cannotList(File dir) throws IOException {
		if (dir.isDirectory()) {
			throw new IOException("Listing files failed for directory: " + dir.getAbsolutePath());
		} else if (!dir.exists()) {
			throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
		} else {
			throw new IOException("Is not a directory: " + dir.getAbsolutePath());
		}
	}

    /**
	 * Recursively remove all files and folders and the given directory.
	 * 
	 * @param dir
	 *        Directory to remove recursively.
	 * @return <code>true</code> when everything succeeded.
	 */
	public static boolean deleteR(File dir) {
		boolean result = deleteContents(dir);
		return dir.delete() && result;
    }

	/**
	 * Removes all files and folders in the given directory.
	 * 
	 * @param dir
	 *        Directory to remove contents
	 * @return <code>true</code> when everything succeeded.
	 */
	public static boolean deleteContents(File dir) {

		boolean result = true;

		if (dir.isDirectory()) {
			File subs[];
			try {
				subs = FileUtilities.listFiles(dir);
			} catch (IOException ex) {
				return false;
			}
			int size = subs.length;
			for (int i = 0; i < size; i++) {
				result = deleteR(subs[i]) && result;
			}
		}
		return result;
	}

	/**
	 * Recursively copies all files and folders from one directory to another.
	 *
	 * @param from
	 *        Directory or file to copy from.
	 * @param to
	 *        Destination directory or file to copy to.
	 * @param copyLastModified
	 *        If <code>true</code>, sets the last-modified date of destination files to the
	 *        last-modified date of the source.
	 * @return true If no error occurred. Potential errors are logged.
	 */
	public static boolean copyR(File from, File to, boolean copyLastModified) {
        boolean result = true;

        if (from.isFile()) {
            try {
                copyFile(from, to, copyLastModified);
            } catch(IOException e) {
                Logger.error("copyR" , e, FileUtilities.class);
                result = false;
            }
        }
        else if (from.isDirectory()) {
            to.mkdir();
			File subs[];
			try {
				subs = FileUtilities.listFiles(from);
			} catch (IOException ex) {
				Logger.error("Listing directory failed: " + ex.getMessage(), ex, FileUtilities.class);
				return false;
			}
            int size = subs.length;
            for (int i=0; i < size && result; i++) {
                File sub = subs[i];
                result &= copyR(sub, new File(to, sub.getName()));
            }
        }
        return result;
    }

   	/**
	 * Recursively copies all files and folders from one directory to another.
	 * 
	 * <p>
	 * The last-modified date of files is preserved.
	 * </p>
	 * 
	 * @param from
	 *        Directory or File to copy
	 * @param to
	 *        Directory or File to copy to
	 * @return true If no errors occurred. Potential errors are logged.
	 */
	public static final boolean copyR(File from, File to) {
       return copyR(from,to, true);
   }

	/**
	 * Reads lines of text from file and adds them to a StringBuilder,
	 * concatenated by '\n'.
	 * <p>
	 * Since it operates on lines it will convert \n\r or \r to \n . In addition
	 * the String will always end with '\n'.
	 * </p>
	 * 
	 * @see #readFileToString(java.io.File) which is slower but doe not care
	 *      about lines.
	 * 
	 * @param file
	 *        the lines will be retrieved from this file.
	 * @param buf
	 *        a StringBuilder that will be used to insert the lines.
	 * 
	 * @throws FileNotFoundException
	 *         will be raised, if file could not be found
	 * @throws IOException
	 *         will be raised for any IO errors
	 */
	public static void readLinesFromFile(File file, StringBuilder buf) throws FileNotFoundException, IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			readLines(in, buf);
		}
	}

	static void readLines(InputStream in, StringBuilder buf) throws IOException {
		readLines(in, buf, ENCODING);
	}

	static void readLines(InputStream in, StringBuilder buf, String encoding) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding))) {
			readAllLinesFromReader(reader, buf);
		}
	}

	/**
	 * Reads lines of text from file and returns it them a string, concatenated by '\n'.
	 * <p>
	 * Since it operates on lines it will convert \n\r or \r to \n . In addition the String will
	 * always end with '\n'.
	 * </p>
	 * 
	 * @see #readFileToString(java.io.File) which is slower but does not care about lines.
	 * 
	 * @param file
	 *        the File to read the lines from.
	 * @return the contents as a string
	 * @throws FileNotFoundException
	 *         will be raised, if file could not be found
	 * @throws IOException
	 *         will be raised for any IO errors
	 */
	public static String readLinesFromFile(File file) throws FileNotFoundException, IOException {
		StringBuilder buf = new StringBuilder((int) file.length());
		readLinesFromFile(file, buf);
		return buf.toString();
	}

	/**
	 * Reads lines of text from file and returns it them a string, concatenated by '\n'.
	 * <p>
	 * Since it operates on lines it will convert \n\r or \r to \n . In addition the String will
	 * always end with '\n'.
	 * </p>
	 * 
	 * @see #readFileToString(java.io.File) which is slower but does not care about lines.
	 * 
	 * @param file
	 *        the content to read the lines from.
	 * @return the contents as a string
	 * @throws FileNotFoundException
	 *         will be raised, if file could not be found
	 * @throws IOException
	 *         will be raised for any IO errors
	 */
	public static String readLinesFromFile(BinaryContent file) throws FileNotFoundException, IOException {
		StringBuilder buf = new StringBuilder();
		try (InputStream in = file.getStream()) {
			readLines(in, buf, ENCODING);
		}
		return buf.toString();
	}

    /**
     * Reads lines of text from file and returns it them a string, concatenated by '\n'.
     *<p>
     * Since it operates on lines it will convert \n\r or \r to \n .
     * In addition the String will always end with '\n'.
     *</p>
     * @see #readFileToString(java.io.File) which is slower but doe not care about lines.
     *
     * @param   fileName the name of the file
     * @return  the contents as a string
     * @throws  FileNotFoundException will be raised, if file could not be found
     * @throws  IOException will be raised for any IO errors
     */
	public static final String readLinesFromFile(String fileName) throws FileNotFoundException, IOException {
        return readLinesFromFile(new File(fileName));   
    }
    
    /**
     * Reads words from file and return them in a List. 
     * Word separators correspond to the default separators of a
     * {@link java.util.StringTokenizer}.
     *
     * @param   aFileName the name of the file
     * @return  the contents as a List
     * @throws  FileNotFoundException will be raised, if file could not be found
     * @throws  IOException will be raised for any IO errors
     */
	public static final List<String> readWordsFromFile(String aFileName) throws FileNotFoundException, IOException {
        return readWordsFromFile(new File(aFileName));
    }

    /**
     * Reads words from file and return them in a List. 
     * Word separators correspond to the default separators of a
     * {@link java.util.StringTokenizer}.
     *
     * @param   aFile the file
     * @return  the contents as a List
     * @throws  FileNotFoundException will be raised, if file could not be found
     * @throws  IOException will be raised for any IO errors
     */
	public static List<String> readWordsFromFile(File aFile) throws FileNotFoundException, IOException {
		return readWordsFromFile(BinaryDataFactory.createBinaryData(aFile));
	}

	/**
	 * Reads words from file and return them in a List. Word separators correspond to the default
	 * separators of a {@link java.util.StringTokenizer}.
	 *
	 * @param aFile
	 *        the file
	 * @return the contents as a List
	 * @throws FileNotFoundException
	 *         will be raised, if file could not be found
	 * @throws IOException
	 *         will be raised for any IO errors
	 */
	public static List<String> readWordsFromFile(BinaryContent aFile) throws IOException {
		String theFileContent = readLinesFromFile(aFile);
        StringTokenizer theTokenizer = new StringTokenizer(theFileContent);
        ArrayList<String> wordsList = new ArrayList<>(theTokenizer.countTokens());
        while (theTokenizer.hasMoreTokens()) {
            wordsList.add(theTokenizer.nextToken());
        }
        return wordsList;
    }
    
	/**
	 * Reads the {@link File} in {@link StreamUtilities#ENCODING ISO} encoding
	 * as {@link #readFileToString(File, String)} does.
	 * 
	 * @see #readFileToString(File, String)
	 */
    public static String readFileToString (File aFile) throws IOException {
        return readFileToString(aFile, ENCODING);
    }
    
	/**
	 * Reads the {@link File} in {@link StreamUtilities#ENCODING ISO} encoding as
	 * {@link #readFileToString(File, String)} does.
	 * 
	 * @see #readFileToString(File, String)
	 */
	public static String readFileToString(BinaryContent aFile) throws IOException {
		return readFileToString(aFile, ENCODING);
	}

	/**
	 * this methods opens a connection to a File object, reads all characters
	 * and stores them in a string.
	 * 
	 * @see #readLinesFromFile(File) which is faster but modifes the line ends.
	 * 
	 * @param aFile
	 *        a file object
	 * @param anEncoding
	 *        encoding of the file content
	 *        
	 * @return String the content string of the read file.
	 * 
	 * @exception IOException
	 *            when accessing file fails.
	 */
	public static String readFileToString(File aFile, String anEncoding) throws IOException {
		StringBuffer theBuf = new StringBuffer((int) aFile.length());
		readContents(new FileInputStream(aFile), anEncoding, theBuf);
		return theBuf.toString();
	}

	/**
	 * this methods opens a connection to a File object, reads all characters and stores them in a
	 * string.
	 * 
	 * @see #readLinesFromFile(File) which is faster but modifes the line ends.
	 * 
	 * @param aFile
	 *        a file object
	 * @param anEncoding
	 *        encoding of the file content
	 * 
	 * @return String the content string of the read file.
	 * 
	 * @exception IOException
	 *            when accessing file fails.
	 */
	public static String readFileToString(BinaryContent aFile, String anEncoding) throws IOException {
		StringBuffer theBuf = new StringBuffer();
		readContents(aFile.getStream(), anEncoding, theBuf);
		return theBuf.toString();
	}

	private static void readContents(InputStream in, String anEncoding, StringBuffer theBuf)
			throws IOException, UnsupportedEncodingException, FileNotFoundException {
		try (Reader reader = new BufferedReader(new InputStreamReader(in, anEncoding))) {
			int character;

			while ((character = reader.read()) != -1) {
				theBuf.append((char) character);
			}
		}
	}

	/**
	 * This methods opens a connection to a File object, reads all characters and stores them in a
	 * string.
	 * 
	 * @see #readLinesFromFile(File) which is faster but modifes the line ends.
	 * 
	 * @param aFile
	 *        a file object
	 * @param anEncoding
	 *        encoding of the file content
	 * 
	 * @return String the content string of the read file.
	 * 
	 * @exception IOException
	 *            when accessing file fails.
	 */
	public static String readFileToString(File aFile, Charset anEncoding) throws IOException {
		StringBuffer theBuf = new StringBuffer((int) aFile.length());
		try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), anEncoding))) {
			int character;
			while ((character = reader.read()) != -1) {
				theBuf.append((char) character);
			}
		}
		return theBuf.toString();
	}

	/**
	 * Reads the content of the file denoted by the filename to a {@link String} as
	 * {@link #readFileToString(File)} does.
	 * 
	 * @see #readFileToString(File)
	 */
	public static String readFileToString(String aFileName) throws IOException {
		return readFileToString(new File(aFileName));
	}

	/**
	 * Writes the given string to the file.
	 * 
	 * Will always use {@link StreamUtilities#ENCODING}, will not work with non
	 * ISO-Strings.
	 * 
	 * @param content
	 *        a string with the content to be saved
	 * @param aFile
	 *        while that will be written to.
	 * 
	 * @return File same as aFile
	 * 
	 * @exception IOException
	 *            from the underlying Streams/Writes
	 * 
	 * @see #writeStringToFile(String, File, String)
	 */
	public static File writeStringToFile(String content, File aFile) throws IOException {
		return writeStringToFile(content, aFile, ENCODING);
	}
    
	/**
	 * Writes the given string to the file.
	 * 
	 * @param content
	 *        A string with the content to be saved.
	 * @param file
	 *        File to write to.
	 * @param encoding
	 *        Encoding of the file.
	 * 
	 * @return The given {@link File}.
	 * 
	 * @exception IOException
	 *            from the underlying Streams/Writes
	 */
	public static File writeStringToFile(String content, File file, String encoding) throws IOException {

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
			writer.write(content);
		}

		return file;
	}

	/**
	 * Writes the given string to the file.
	 * 
	 * @param content
	 *        a string with the content to be saved
	 * @param aFile
	 *        File to write to.
	 * @param anEncoding
	 *        encoding of the string
	 * 
	 * @return the given {@link File}
	 * 
	 * @exception IOException
	 *            from the underlying Streams/Writes
	 */
	public static File writeStringToFile(String content, File aFile, Charset anEncoding) throws IOException {
		try (Writer theWriter = new OutputStreamWriter(new FileOutputStream(aFile), anEncoding)) {
			theWriter.write(content);
		}
		return aFile;
	}

	/**
	 * Writes the content to the file denoted by the given file name as
	 * {@link #writeStringToFile(String, File)} does.
	 * 
	 * @see #writeStringToFile (String, File)
	 */
	public static File writeStringToFile(String content, String abstractFileName) throws IOException {
		File theFile = new File(abstractFileName);

		return writeStringToFile(content, theFile);
	}

	/**
	 * Copies a stream to file defined by the given name as
	 * {@link #copyFile(InputStream, File)} does.
	 * 
	 * @see #copyFile(InputStream, File)
	 */
	public static void copyFile(InputStream aSource, String aName) throws IOException {
		try {
			copyToFile(aSource, aName);
		} finally {
			aSource.close();
		}
	}

	/**
	 * Copies a stream to file defined by the given name as
	 * {@link #copyFile(InputStream, File)} does.
	 * 
	 * @see #copyFile(InputStream, File)
	 */
	public static void copyToFile(InputStream aSource, String aName) throws IOException {
		copyToFile(aSource, new File(aName));
	}
	
    /**
	 * Copies a on File to another keeping the lastModified Date.
	 *
	 * The destination file will be overridden. The new file will be writable even when the source
	 * file was locked, or read-only. The function will set the LastModified date of aDest to the
	 * LastModified date of aSource, this may however fail, which is not checked.
	 *
	 * @param aSource
	 *        The Source file to copy.
	 * @param aDest
	 *        The new file.
	 *
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static final void copyFile(File aSource, File aDest) throws IOException {
        copyFile(aSource, aDest, true);
    }

	/**
	 * Copies a on File to another keeping the lastModified Date.
	 *
	 * The destination file will be overridden. The new file will be writable even when the source
	 * file was locked, or read-only. The function will set the LastModified date of aDest to the
	 * LastModified date of aSource, this may however fail, which is not checked.
	 *
	 * @param aSource
	 *        The Source file to copy.
	 * @param aDest
	 *        The new file.
	 *
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static final void copyFile(Path aSource, Path aDest) throws IOException {
		copyFile(aSource, aDest, true);
	}

	/**
	 * Copies a on File to another keeping the lastModified Date.
	 * 
	 * The destination file will be overridden. The new file will be writable
	 * even when the source file was locked, or read-only.
	 * 
	 * @param aSource
	 *        The Source file to copy.
	 * @param aDest
	 *        The new file.
	 * @param copyLastModified
	 *        will set the LastModified date of aDest to the LastModified date
	 *        of aSource, this may however fail, which is not checked.
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static final void copyFile(File aSource, File aDest, boolean copyLastModified) throws IOException {
		try (FileInputStream input = new FileInputStream(aSource)) {
			try (FileOutputStream output = new FileOutputStream(aDest)) {
				copyStreamContents(input, output);
			}
		}
		if (copyLastModified)
			aDest.setLastModified(aSource.lastModified());
	}

	/**
	 * Copies a on File to another keeping the lastModified Date.
	 * 
	 * The destination file will be overridden. The new file will be writable even when the source
	 * file was locked, or read-only.
	 * 
	 * @param aSource
	 *        The Source file to copy.
	 * @param aDest
	 *        The new file.
	 * @param copyLastModified
	 *        will set the LastModified date of aDest to the LastModified date of aSource, this may
	 *        however fail, which is not checked.
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static final void copyFile(Path aSource, Path aDest, boolean copyLastModified) throws IOException {
		try (InputStream input = Files.newInputStream(aSource)) {
			try (OutputStream output = Files.newOutputStream(aDest)) {
				copyStreamContents(input, output);
			}
		}
		if (copyLastModified) {
			Files.setLastModifiedTime(aDest, Files.getLastModifiedTime(aSource));
		}
	}

	/**
	 * Copies a stream to a file defined by the given name.
	 * 
	 * The destination file will be overridden,
	 * 
	 * @param input
	 *        The content for the new file. Is closed after copying.
	 * @param aDest
	 *        The new file.
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 * 
	 * @deprecated use {@link #copyToFile(InputStream, File)} and close stream after calling
	 */
	@Deprecated
	public static void copyFile(InputStream input, File aDest) throws IOException {
		try {
			copyToFile(input, aDest);
		} finally {
			input.close();
		}
	}

	/**
	 * Copies a stream to a file defined by the given name. The input stream is not closed after
	 * invocation.
	 * 
	 * @param content
	 *        The content for the new file.
	 * @param destination
	 *        The new file.
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static void copyToFile(BinaryContent content, File destination) throws IOException {
		try (InputStream in = content.getStream()) {
			copyToFile(in, destination);
		}
	}

	/**
	 * Copies a stream to a file defined by the given name. The input stream is not closed after
	 * invocation.
	 * 
	 * @param input
	 *        The content for the new file. Is closed after copying.
	 * @param destination
	 *        The new file.
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static void copyToFile(InputStream input, File destination) throws IOException {
		try (FileOutputStream output = new FileOutputStream(destination)) {
			copyStreamContents(input, output);
		}
	}
	
	/**
	 * Compares the <em>contents</em> of two files,
	 * 
	 * @param file1
	 *        The first {@link File} to compare
	 * @param file2
	 *        The second {@link File} to compare
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static boolean equalsFile(File file1, File file2) throws IOException {
		try (final FileInputStream in1 = new FileInputStream(file1)) {
			try (final FileInputStream in2 = new FileInputStream(file2)) {
				return equalsStreamContents(in1, in2);
			}
		}
	}

    /** Return an Array of Strings with files with a common tail */
    public static String[] getFilesEndingWith(File parent, String extension) {
        return parent.list( getExtensionFilter(extension));   
    }
    
    /** Helper function to create a FileNameFilter matching Extensions */
    public static FilenameFilter getExtensionFilter(final String extension) {
        return new FilenameFilter() {
            @Override
			public boolean accept(File aFile, String aName) {    
                return aName.endsWith(extension);
            }
        };
    }

    /**
     * Selects the first entry with a desired ending in a zipped input stream.
     * 
     * @param endsWith A ZipEntry ending with this name will be considered. 
     * 
     * @return null when no such ZipEntry with was found
     */
    public static ZipInputStream getEntryStream(ZipInputStream aZipInputStream, String endsWith) throws IOException {
        ZipEntry theCurrentEntry = aZipInputStream.getNextEntry();
        while (theCurrentEntry != null) {
            if (theCurrentEntry.getName().endsWith(endsWith)) {
                return aZipInputStream;
            } 
            theCurrentEntry = aZipInputStream.getNextEntry();
        }
        return null;
    }
    
    /**
     * reads the given File as a ByteArray
     * @return byteArray from File
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            return null;
        }
    
		try (InputStream is = new FileInputStream(file)) {
        	// Create the byte array to hold the data
        	byte[] bytes = new byte[(int)length];
        	
        	// Read in the bytes
        	int offset = 0;
        	int numRead = 0;
        	while (offset < bytes.length
        			&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        		offset += numRead;
        	}
        	
        	// Ensure all the bytes have been read in
        	if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
        	}
        	return bytes;
		}
    }

	/**
	 * Reads all data from the given {@link Reader} and returns it as
	 * {@link String}.
	 */
	public static String fetchStringContent(Reader reader) throws IOException {
		StringWriter writer = new StringWriter(512);
		copyReaderWriterContents(reader, writer);
		return writer.getBuffer().toString();
	}

	/**
	 * Creates a {@link File} from the given {@link URL} by chaining {@link URL#toURI()} and
	 * {@link File#File(java.net.URI)}.
	 * 
	 * @param url
	 *        Must not be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static File urlToFile(URL url) {
		try {
			return new File(url.toURI());
		} catch (URISyntaxException exception) {
			throw new RuntimeException("Could not convert URL to URI!", exception);
		}
	}

	/**
	 * Ensures that the given {@link File} does not exist.
	 * <p>
	 * If the file exists, it will be deleted. If the file still exists after that,
	 * a {@link RuntimeException} is thrown.
	 * </p>
	 */
	public static void ensureNotExisting(File file) {
		if (file.exists()) {
			file.delete();
			if (file.exists()) {
				throw new RuntimeException("Deleting file failed, it still exists! File: '" + file + "'");
			}
		}
	}

	/**
	 * Ensures that the given file exists and all directories on the file path.
	 */
	public static void ensureFileExisting(File file) {
		file.getParentFile().mkdirs();

		try {
			file.createNewFile();
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	/**
	 * Builds a path with all the names separated by {@link File#separatorChar}.
	 * <p>
	 * The separator char is only inserted between the names, not at the beginning and not at the
	 * end of the path. <br/>
	 * The parameter must not be <code>null</code> and not empty. The individual names must not be
	 * <code>null</code>, too.
	 * </p>
	 */
	public static String buildPath(String... names) {
		if (names.length == 0) {
			throw new IllegalArgumentException("No directory names given!");
		}
		StringBuilder path = new StringBuilder();
		for (String name : names) {
			if (name == null) {
				String errorMessage = "Directory name must not be null! All names: " + Arrays.toString(names);
				throw new NullPointerException(errorMessage);
			}
			path.append(name).append(File.separatorChar);
		}
		path.deleteCharAt(path.length() - 1);
		return path.toString();
	}

	/**
	 * Replaces the file separator ('/' on UNIX, '\' on Windows, by the given character.
	 * 
	 * @param name
	 *        The value in which the separator should be replaced.
	 * @param replacement
	 *        The replacement character.
	 * 
	 * @return A string in which the file separator is replaced by the replacement character.
	 * 
	 * @see File#separatorChar
	 */
	public static String replaceSeparator(String name, char replacement) {
		if (name.isEmpty()) {
			return name;
		}
		int length = name.length();
		StringBuilder buffer = new StringBuilder(length);
		boolean changed = false;
		for (int i = 0; i < length; i++) {
			char c = name.charAt(i);
			switch (c) {
				case '/':
				case '\\':
					buffer.append(replacement);
					changed |= (c != replacement);
					break;
				default:
					buffer.append(c);
					break;
			}
		}
		if (changed) {
			return buffer.toString();
		} else {
			// No changes.
			return name;
		}
	}

	/**
	 * Convenience redirect to {@link RegExpUtil#buildFileContentMatcher(File, Pattern, Charset)} to
	 * make it easier to find.
	 */
	public static Matcher buildFileContentMatcher(File file, Pattern pattern, Charset encoding) {
		return RegExpUtil.buildFileContentMatcher(file, pattern, encoding);
	}

	/** Variant of {@link #getSafeDetailedPath(File)} that accepts a {@link Path}. */
	public static String getSafeDetailedPath(Path path) {
		if (path == null) {
			return "[null]";
		}
		try {
			return getSafeDetailedPath(path.toFile());
		} catch (RuntimeException ex) {
			return path.toString();
		}
	}

	/**
	 * Returns the most detailed path information which can be accessed without throwing an
	 * exception.
	 * <p>
	 * Tries to return the {@link File#getCanonicalPath() canonical path}. If that fails, <br/>
	 * tries to return the {@link File#getAbsolutePath() absolute path}. If that fails, <br/>
	 * returns the {@link File#getPath() simple path}. <br/>
	 * This method is intended to provide debugging or logging information.
	 * </p>
	 * 
	 * @param file
	 *        Is allowed to be <code>null</code>, which will result in "[null]".
	 * @return Might be <code>null</code>. (It is undocumented whether the canonical or absolute
	 *         paths can be <code>null</code>.)
	 */
	public static String getSafeDetailedPath(File file) {
		return file == null ? "[null]" : getCanonicalPath(file);
	}

	private static String getCanonicalPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (RuntimeException ex) {
			return getAbsolutePath(file);
		} catch (IOException ex) {
			return getAbsolutePath(file);
		}
	}

	private static String getAbsolutePath(File file) {
		try {
			return file.getAbsolutePath();
		} catch (RuntimeException ex) {
			return file.getName();
		}
	}

	/**
	 * Calls {@link File#getCanonicalFile()} but converts {@link IOException}s to
	 * {@link RuntimeException}s.
	 */
	public static File canonicalize(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			String message = "Failed to canonicalize '" + getSafeDetailedPath(file) + "'. Cause: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

	/**
	 * Check that the given path matches a suffix of the path name of the given file.
	 * 
	 * <p>
	 * This check especially fails, if the canonical name of the given file differs in case to the
	 * given path name. If this check succeeds, the given path can be used to locate the given file
	 * in a system-independent manner.
	 * </p>
	 * 
	 * @param origPath
	 *        The path name to check.
	 * @param origFile
	 *        The file that should be identified by the given path.
	 * @throws IOException
	 *         If the check fails.
	 */
	public static void checkCase(String origPath, File origFile) throws IOException {
		File canonicalFile = origFile.getCanonicalFile();
	
		File file = canonicalFile;
		String path = origPath.replace('\\', '/');
		while (true) {
			int sepIndex;
			String pathPart;
			do {
				sepIndex = path.lastIndexOf('/');
				if (sepIndex < 0) {
					pathPart = path;
					path = "";
				} else {
					pathPart = path.substring(sepIndex + 1);
					path = path.substring(0, sepIndex);
				}
			} while (".".equals(pathPart));

			if (pathPart.isEmpty() || "..".equals(pathPart)) {
				break;
			}

			if (!pathPart.equals(file.getName())) {
				throw new IOException("Incorrect file name '" + origPath + "' for file '" + canonicalFile.getPath()
					+ "', expecting '" + pathPart + "', found '" + file.getName() + "', case missmatch?.");
			}
	
			if (sepIndex < 0) {
				break;
			}
	
			file = file.getParentFile();
	
			if (file == null) {
				break;
			}
		}
	}

	/**
	 * Returns all {@link File}s in the given directory, recursively.
	 * <p>
	 * Supports (i.e. follows) symbolic links. A loop in the file-system caused by symbolic links
	 * causes a {@link RuntimeException}.
	 * </p>
	 * <p>
	 * The given {@link File} itself is part of the result. If the given file is not a directory,
	 * only the given file itself is returned.
	 * </p>
	 * 
	 * @param file
	 *        Is not allowed to be null.
	 * @return A mutable, resizable {@link List}.
	 */
	public static List<File> getFilesRecursively(File file) {
		List<Path> paths = getFilesRecursively(file.toPath());
		List<File> files = CollectionFactory.list();
		for (Path path : paths) {
			files.add(path.toFile());
		}
		return files;
	}

	/**
	 * Convenience variant of {@link #getFilesRecursively(File)} that accepts and returns
	 * {@link Path}s instead of {@link File}s.
	 */
	public static List<Path> getFilesRecursively(Path file) {
		PathCollector visitor = new PathCollector();
		walkFileTree(file, visitor);
		return visitor.getPaths();
	}

	/**
	 * Convenience variant of {@link Files#walkFileTree(Path, Set, int, FileVisitor)} that supports
	 * (i.e. follows) symbolic links and descends without depth limit.
	 * 
	 * @param path
	 *        Is not allowed to be null.
	 * @param visitor
	 *        Is not allowed to be null.
	 */
	public static void walkFileTree(Path path, FileVisitor<Path> visitor) {
		try {
			Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
		} catch (IOException | IOError ex) {
			throw createWalkError(path, ex);
		}
	}

	/**
	 * Convenience variant of {@link Files#walk(Path, FileVisitOption[])}.
	 * 
	 * @param path
	 *        Is not allowed to be null.
	 * @param options
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Stream<Path> walk(Path path, FileVisitOption... options) {
		try {
			return Files.walk(path, options);
		} catch (IOException | IOError exception) {
			throw createWalkError(path, exception);
		}
	}

	private static RuntimeException createWalkError(Path path, Throwable cause) {
		String message = "Failed to visit the directory tree recursively. Start of descend: '"
			+ FileUtilities.getSafeDetailedPath(path) + "'. Cause: " + cause.getMessage();
		return new RuntimeException(message, cause);
	}

	/**
	 * The name parts defined in {@link Path#getName(int)} as a {@link List}.
	 * 
	 * @param file
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static List<String> getNameParts(Path file) {
		List<String> result = CollectionFactory.list();
		for (int i = 0; i < file.getNameCount(); i++) {
			result.add(file.getName(i).toString());
		}
		return result;
	}

	/**
	 * Searches the first parent with the given name for the given file.
	 * 
	 * @param file
	 *        Source file.
	 * @param name
	 *        Filename
	 * @return First parent with the given filename.
	 */
	public static Optional<File> getFirstParent(File file, String name) {
		File parent = file.getParentFile();

		while (parent != null) {
			if (parent.getName().equals(name)) {
				return Optional.of(parent);
			}

			parent = parent.getParentFile();
		}

		return Optional.empty();
	}

	/**
	 * Returns a compatible filename part replacing special characters that are invalid on linux or
	 * windows with an underscore.
	 * 
	 * @see #INVALID_FILENAME_WINDOWS
	 */
	public static String getCompatibleFilenamePart(String filenamePart) {
		return filenamePart.replaceAll("[" + FileUtilities.INVALID_FILENAME_WINDOWS + "]", "_");
	}

	/**
	 * Returns the {@link String} representation of the normalized relativized path to the given
	 * base.
	 */
	public static String getRelativizedPath(Path base, Path path) {
		String relativizedPath = base.relativize(path).toString();

		return FileUtilities.replaceSeparator(relativizedPath, '/');
	}

	/**
	 * Returns the path starting from the given suffix.
	 * 
	 * <pre>
	 * path = a/b/c/d/e
	 * suffix = b/c
	 * result = d/e
	 * </pre>
	 */
	public static Path getPathFrom(Path path, Path suffix) {
		Path prefix = FileUtilities.getPathTo(path, suffix);
		if (prefix == null) {
			return null;
		}

		return prefix.relativize(path);
	}

	/**
	 * Return the path up to the given suffix.
	 * 
	 * <pre>
	 * path = a/b/c/d/e
	 * suffix = b/c
	 * result = a/b/c
	 * </pre>
	 */
	public static Path getPathTo(Path path, String suffix) {
		return getPathTo(path, Paths.get(suffix));
	}

	/**
	 * Return the path up to the given suffix.
	 * 
	 * <pre>
	 * path = a/b/c/d/e
	 * suffix = b/c
	 * result = a/b/c
	 * </pre>
	 */
	public static Path getPathTo(Path path, Path suffix) {
		int endIndex = path.getNameCount();

		while (!machtes(path, endIndex, suffix)) {
			endIndex--;
			if (endIndex < suffix.getNameCount()) {
				return null;
			}
		}
		Path result = path.subpath(0, endIndex);
		Path root = path.getRoot();
		if (root != null) {
			result = root.resolve(result);
		}
		return result;
	}

	private static boolean machtes(Path path, int endIndex, Path suffix) {
		for (int n = 1, cnt = suffix.getNameCount(); n <= cnt; n++) {
			if (!suffix.getName(cnt - n).equals(path.getName(endIndex - n))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Joins the {@link Path} parts with {@link FileUtilities#PATH_SEPARATOR} as separator into a
	 * {@link String}.
	 * 
	 * @see #getCombinedPath(Path, CharSequence)
	 */
	public static String getCombinedPath(Path path) {
		return getCombinedPath(path, FileUtilities.PATH_SEPARATOR);
	}

	/**
	 * Joins the {@link Path} parts with the given separator into a {@link String}.
	 * 
	 * <pre>
	 * path = a\b\c
	 * separator = /
	 * result = a/b/c
	 * </pre>
	 */
	public static String getCombinedPath(Path path, CharSequence separator) {
		StringJoiner joiner = new StringJoiner(separator);

		path.iterator().forEachRemaining(segment -> joiner.add(String.valueOf(segment)));

		return joiner.toString();
	}

	/**
	 * All resource names of resources with the given common prefix.
	 * 
	 * <p>
	 * Performs the search of resource paths recursive.
	 * </p>
	 * 
	 * @see FileManager#getResourcePaths(String)
	 */
	public static Set<String> getAllResourcePaths(String resourcePrefix) {
		if (resourcePrefix.charAt(resourcePrefix.length() - 1) != '/') {
			return Collections.emptySet();
		}

		Set<String> allResourcePaths = new HashSet<>();
		Set<String> resourcePaths = FileManager.getInstance().getResourcePaths(resourcePrefix);
		allResourcePaths.addAll(resourcePaths);

		for (String resourcePath : resourcePaths) {
			allResourcePaths.addAll(getAllResourcePaths(resourcePath));
		}

		return allResourcePaths;
	}

	/**
	 * Prepends the given prefix to all filenames and returning the resulting paths.
	 * 
	 * <pre>
	 * file = a/b/c.xml
	 * prefix = foo/bar
	 * result = foo/bar/c.xml
	 * </pre>
	 */
	public static Set<String> getPrefixedFilenames(Collection<File> files, String prefix) {
		return files
			.stream()
			.map(f -> {
				StringBuilder result = new StringBuilder(prefix);
				result.append(PATH_SEPARATOR);
				String fileName = f.getName();
				result.append(fileName);
				if (f.isDirectory() && !fileName.endsWith(PATH_SEPARATOR)) {
					result.append(PATH_SEPARATOR);
				}
				return result.toString();
			})
			.collect(Collectors.toSet());
	}

	/**
	 * Returns a collection of {@link File}s located in the specified directory and matching the
	 * specified filter.
	 * 
	 * @param directory
	 *        The directory to list.
	 * @param filter
	 *        Filters the directory listing.
	 * 
	 * @see FileUtilities#listFiles(File, FilenameFilter)
	 * @see File#listFiles(FilenameFilter)
	 */
	public static Collection<File> listFilesSafe(File directory, FilenameFilter filter) {
		try {
			return Arrays.asList(listFiles(directory, filter));
		} catch (IOException exception) {
			return Collections.emptySet();
		}
	}

	/**
	 * Gets the file name, the full filename minus its path, of a {@link FileManager}s resource.
	 * 
	 * @param path
	 *        Path to get the base name from.
	 * 
	 * @see #getFilename(String, String)
	 */
	public static String getFilenameOfResource(String path) {
		return getFilename(path, PATH_SEPARATOR);
	}

	/**
	 * Gets the file name, the full filename minus its path, with respect to the given path
	 * separator.
	 * 
	 * <p>
	 * 
	 * <pre>
	 * path = a/b/c.txt
	 * separator = /
	 * result = c.txt
	 * 
	 * path = a.txt
	 * separator = /
	 * result = a.txt
	 * 
	 * path = a/b
	 * separator = /
	 * result = b
	 * 
	 * path = a/b/
	 * separator = /
	 * result = b
	 * </pre>
	 * </p>
	 * 
	 * @param path
	 *        Path to get the base name from.
	 * @param separator
	 *        Path separator.
	 */
	public static String getFilename(String path, String separator) {
		String normalizedPath = StringUtils.removeEnd(path, separator);

		int nameStartIndex = normalizedPath.lastIndexOf(separator) + 1;
		if (nameStartIndex > 0) {
			if (normalizedPath.length() > nameStartIndex) {
				return normalizedPath.substring(nameStartIndex);
			} else {
				return StringServices.EMPTY_STRING;
			}
		} else {
			return normalizedPath;
		}
	}

	/**
	 * Gets the base name, the full filename minus its path and extension, with respect to the given
	 * path separator.
	 * 
	 * <p>
	 * <pre>
	 * path = a/b/c.txt
	 * separator = /
	 * result = c
	 * 
	 * path = a.txt
	 * separator = /
	 * result = a
	 * 
	 * path = a/b
	 * separator = /
	 * result = b
	 * 
	 * path = a/b/
	 * separator = /
	 * result = b
	 * </pre>
	 * </p>
	 * 
	 * @param path
	 *        Path to get the base name from.
	 */
	public static String getBasename(String path, String separator) {
		return removeFileExtension(getFilename(path, separator));
	}

	/**
	 * Removes the files type extension.
	 * 
	 * <p>
	 * <pre>
	 * path = a.txt
	 * result = a
	 * 
	 * path = a
	 * result = a
	 * </pre>
	 * </p>
	 * 
	 * @param filename
	 *        Name to remove the extension for.
	 */
	public static String removeFileExtension(String filename) {
		if (StringServices.isEmpty(filename)) {
			return filename;
		}

		int extensionStartIndex = filename.lastIndexOf('.');
		if (extensionStartIndex >= 0) {
			return filename.substring(0, extensionStartIndex);
		} else {
			return filename;
		}
	}

	/**
	 * Whether the file by the given path is an empty directory.
	 * 
	 * @param path
	 *        The path to the file to test if its an empty directory.
	 */
	public static boolean isEmptyDirectory(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (Stream<Path> entries = Files.list(path)) {
				return !entries.findFirst().isPresent();
			}
		}

		return false;
	}

}
