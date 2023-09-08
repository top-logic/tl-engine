/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.equal.CustomEqualitySpecification;
import com.top_logic.basic.col.equal.EqualityRedirect;
import com.top_logic.basic.io.binary.ByteArrayStream;

/**
 * Utility functions around Streams, Readers and Writers.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class StreamUtilities {
    
	/** Buffer size used in copy methods for temporary buffers. */
    private static final int BUF_SIZE = 4096;

    /** Default Encoding that is used to convert files to Strings. */
    public static final String ENCODING = "ISO-8859-1";
    
	/** {@link Charset} for name "IS8-8859-1". */
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /** 
     * Null resistant variant of close, will catch and log the IOException.
     * 
     * Most useful when used in a finally block. 
     * 
     * @param  anIStream may be null which will be ignored.
     * @return always null for reassignment.
     */
    public static InputStream close(InputStream anIStream) {
        if (anIStream != null) try {
            anIStream.close();
        } catch (IOException iox) {
            Logger.warn("Failed to close()" , iox, StreamUtilities.class);
        }
        return null;
    }
    
    /** 
     * Null resistant variant of close, will catch and log the IOException.
     * 
     * Most useful when used in a finally block. 
     * 
     * @param  anOStream may be null which will be ignored.
     * @return always null for reassignment..
     */
    public static OutputStream close(OutputStream anOStream) {
        if (anOStream != null) try {
            anOStream.close();
        } catch (IOException iox) {
            Logger.warn("Failed to close()" , iox, StreamUtilities.class);
        }
        return null;
    }

    /** 
     * Null resistant variant of close, will catch and log the IOException.
     * 
     * Most useful when used in a finally block. 
     * 
     * @param  aReader may be null which will be ignored.
     * @return always null for reassignment..
     */
    public static Reader close(Reader aReader) {
        if (aReader != null) try {
            aReader.close();
        } catch (IOException iox) {
            Logger.warn("Failed to close()" , iox, StreamUtilities.class);
        }
        return null;
    }

    /** 
     * Null resistant variant of close, will catch and log the IOException.
     * 
     * Most useful when used in a finally block. 
     * 
     * @param  aWriter may be null which will be ignored.
     * @return always null for reassignment..
     */
    public static Writer close(Writer aWriter) {
        if (aWriter != null) try {
            aWriter.close();
        } catch (IOException iox) {
            Logger.warn("Failed to close()" , iox, StreamUtilities.class);
        }
        return null;
    }

	/**
	 * Reads lines of text from the given reader and adds them to a given
	 * {@link StringBuilder}, concatenated by '\n'.
	 * <p>
	 * Since it operates on lines it will convert \n\r or \r to \n . In addition
	 * the String will always end with '\n'.
	 * </p>
	 * 
	 * @see FileUtilities#readFileToString(File) which is slower but doe not
	 *      care about lines.
	 * 
	 * @param reader
	 *        a reader used to retrieve the lines, will be close()d.
	 * @param buffer
	 *        a StringBuilder that will be used to insert the lines.
	 * 
	 * @throws IOException
	 *         will be raised for any IO errors
	 */
	public static void readAllLinesFromReader(BufferedReader reader, StringBuilder buffer) throws IOException {
		String line = reader.readLine();
		while (line != null) {
			// if line is read, append to buffer
			// first line is an empty string, so no problem
			buffer.append(line);
			buffer.append('\n');
			line = reader.readLine();
		}
	}

	/**
	 * Reads the all lines from the given {@link InputStream} in the given
	 * encoding into a {@link String} as
	 * {@link #readAllLinesFromReader(BufferedReader, StringBuilder)} does.
	 * 
	 * @see #readAllLinesFromReader(BufferedReader, StringBuilder)
	 */
	public static String readAllLinesFromStream(InputStream input, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, encoding));
		StringBuilder buf = new StringBuilder(input.available());
		readAllLinesFromReader(reader, buf);
		return buf.toString();
	}
	
	/**
	 * Reads all lines from the given {@link InputStream} in the given encoding into a
	 * {@link String} as {@link #readAllLinesFromReader(BufferedReader, StringBuilder)} does.
	 * 
	 * @see #readAllLinesFromReader(BufferedReader, StringBuilder)
	 */
	public static String readAllLinesFromStream(InputStream input, Charset encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, encoding));
		StringBuilder buf = new StringBuilder(input.available());
		readAllLinesFromReader(reader, buf);
		return buf.toString();
	}

    /**
     * Reads the stream with {@link #ENCODING ISO} encoding as
     * {@link #readAllLinesFromStream(InputStream, String)} does.
     * 
     * @see #readAllLinesFromStream(InputStream, String)
     */
    public static String readAllLinesFromStream(InputStream aStream) throws IOException {
    	return readAllLinesFromStream(aStream, ENCODING);
    }
    
    /**
     * Same as {@link #readAllFromStream(InputStream, String)} with {@link #ENCODING ISO} encoding.
     * 
     * @see #readAllFromStream(InputStream, String)
     */
    public static String readAllFromStream(InputStream is) throws IOException {
    	return readAllFromStream(is, ENCODING);
    }
    
	/**
	 * Reads the content with {@link #ENCODING ISO} encoding.
	 */
	public static String readAllFromStream(BinaryContent content) throws IOException {
		return readAllFromStream(content, ENCODING);
	}

	/**
	 * Reads the content with the given character encoding.
	 */
	public static String readAllFromStream(BinaryContent content, String encoding) throws IOException {
		try (InputStream in = content.getStream()) {
			return readAllFromStream(in, encoding);
		}
	}

	/**
	 * Convert the contents of the given {@link InputStream} to a string using
	 * the given encoding.
	 * 
	 * @param in
	 *        an arbitrary {@link InputStream}. must not be <code>null</code>
	 * 
	 * @return the contents of the given stream as string.
	 */
	public static String readAllFromStream(InputStream in, String encoding) throws IOException, UnsupportedEncodingException {
		Reader reader = new BufferedReader(new InputStreamReader(in, encoding));
		StringBuilder theBuf = new StringBuilder(in.available());
		int character;
		
		while ((character = reader.read()) != -1) {
			theBuf.append((char) character);
		}
		
		return theBuf.toString();
	}

	/**
	 * Convert the contents of the given {@link InputStream} to a string using the given encoding.
	 * 
	 * @param in
	 *        an arbitrary {@link InputStream}. must not be <code>null</code>
	 * 
	 * @return the contents of the given stream as string.
	 */
	public static String readAllFromStream(InputStream in, Charset encoding) throws IOException,
			UnsupportedEncodingException {
		Reader reader = new BufferedReader(new InputStreamReader(in, encoding));
		StringBuilder theBuf = new StringBuilder(in.available());
		int character;

		while ((character = reader.read()) != -1) {
			theBuf.append((char) character);
		}

		return theBuf.toString();
	}

    /**
	 * Copies the contents of one stream to another stream without closing the
	 * streams afterwards.
	 *
	 * @param aSource
	 *        The source stream.
	 * @param aDest
	 *        The destination stream.
	 * 
	 * @throws IOException
	 *         in case one either reading or writing fails.
	 */
	public static void copyStreamContents(InputStream aSource, OutputStream aDest) throws IOException {
		byte[] theData = new byte[BUF_SIZE];
		int    theSize;
   
		while ((theSize = aSource.read(theData)) >= 0) {
			if (theSize > 0) {
				aDest.write (theData, 0, theSize);
			}
		}
	}

	/**
	 * Compares the bytes of two streams.
	 * 
	 * @param in1
	 *        The first {@link InputStream} to compare.
	 * @param in2
	 *        The second {@link InputStream} to compare.
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static boolean equalsStreamContents(InputStream in1, InputStream in2) throws IOException {
		byte[] data1 = new byte[BUF_SIZE];
		byte[] data2 = new byte[BUF_SIZE];
		while (true) {
			int size1 = readFully(in1, data1);
			int size2 = readFully(in2, data2);
		    if (size1 != size2) { // not the same length -> not equal
		        return false;
		    }
		    if (! Arrays.equals(data1, data2)) {
		        return false;
		    }
		    
		    if (size1 < BUF_SIZE) {
		    	// Both streams are exhausted.
		    	return true;
		    }
		}
	}

	/**
	 * Fill the given buffer from the given {@link InputStream}.
	 * 
	 * <p>
	 * As much data is read as the given stream can provide or as much fits in
	 * the given buffer (whichever is less).
	 * </p>
	 * 
	 * <p>
	 * Note: This method has neither the same semantics as
	 * {@link InputStream#read(byte[])}, nor {@link DataInput#readFully(byte[])}.
	 * </p>
	 * 
	 * @param in
	 *        The stream to read data from.
	 * @param buffer
	 *        the buffer to fill with data.
	 * @return The number of bytes read. If the result is less than the buffer
	 *         size, the given stream is exhausted. A further call will provide
	 *         the value <code>0</code>. If the given stream has reached its end
	 *         of file condition at the time this method is called,
	 *         <code>0</code> is returned.
	 * @throws IOException
	 *         If reading from the given stream fails.
	 */
	public static int readFully(InputStream in, byte[] buffer) throws IOException {
		int stop = buffer.length;
		int bytesRead = 0;
		while (bytesRead < stop) {
			int direct = in.read(buffer, bytesRead, stop - bytesRead);
			if (direct > 0) {
				bytesRead += direct;
			}
			else if (direct < 0) {
				break;
			}
		}
		
		return bytesRead;
	}

	/**
	 * Compares the characters of two readers.
	 * 
	 * @param r1
	 *        The first {@link Reader} to compare.
	 * @param r2
	 *        The second {@link Reader} to compare.
	 * 
	 * @throws IOException
	 *         in case one of the operations fails.
	 */
	public static boolean equalsReaderContent(Reader r1, Reader r2) throws IOException {
		char[] data1 = new char[BUF_SIZE];
		char[] data2 = new char[BUF_SIZE];
		while (true) {
			int size1 = readFully(r1, data1);
			int size2 = readFully(r2, data2);
		    if (size1 != size2) { // not the same length -> not equal
		        return false;
		    }
		    if (! Arrays.equals(data1, data2)) {
		        return false;
		    }
		    
		    if (size1 < BUF_SIZE) {
		    	// Both readers are exhausted.
		    	return true;
		    }
		}
	}

	/**
	 * Fill the given buffer from the given {@link Reader}.
	 * 
	 * <p>
	 * As much data is read as the given reader can provide or as much fits in
	 * the given buffer (whichever is less).
	 * </p>
	 * 
	 * <p>
	 * Note: This method has not the same semantics as
	 * {@link Reader#read(char[])}.
	 * </p>
	 * 
	 * @param reader
	 *        The reader to read data from.
	 * @param buffer
	 *        the buffer to fill with data.
	 * @return The number of <code>char</code>s read. If the result is less than
	 *         the buffer size, the given reader is exhausted. Any further call
	 *         will provide the value <code>0</code>. If the given reader has
	 *         reached its end of file condition at the time this method is
	 *         called, <code>0</code> is returned.
	 * @throws IOException
	 *         If reading from the given reader fails.
	 */
	private static int readFully(Reader reader, char[] buffer) throws IOException {
		int stop = buffer.length;
		int bytesRead = 0;
		while (bytesRead < stop) {
			int direct = reader.read(buffer, bytesRead, stop - bytesRead);
			if (direct > 0) {
				bytesRead += direct;
			}
			else if (direct < 0) {
				break;
			}
		}
		
		return bytesRead;
	}

    /**
     * Copies a Reader to a Writer without closing on of them.
     */
    public static void copyReaderWriterContents(Reader aSource, Writer aDest)
            throws IOException {
        char[] theData = new char[BUF_SIZE];
        int    theSize;
   
        while ((theSize = aSource.read (theData)) >= 0) {
        	if (theSize > 0) {
        		aDest.write (theData, 0, theSize);
        	}
        }
    }
    
	/**
	 * Reads the given stream to a byte array and returns it.
	 * 
	 * @param stream
	 *        the stream to read. must not be <code>null</code>.
	 * 
	 * @return a byte array containing the contents of the stream
	 * 
	 * @throws IOException
	 *         iff the given {@link InputStream} throws some
	 */
	public static byte[] readStreamContents(InputStream stream) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUF_SIZE);
		copyStreamContents(stream, out);
		return out.toByteArray();
	}

	/**
	 * Reads the given content to a byte array and returns it.
	 * 
	 * @param content
	 *        the content to read. must not be <code>null</code>.
	 * 
	 * @return a byte array containing the contents of the stream
	 * 
	 * @throws IOException
	 *         iff the given {@link InputStream} throws some
	 */
	public static byte[] readStreamContents(BinaryContent content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUF_SIZE);
		try (InputStream in = content.getStream()) {
			copyStreamContents(in, out);
		}
		return out.toByteArray();
	}

	/**
	 * Computes the size of the given {@link InputStream}.
	 * 
	 * <p>
	 * This method reads the data from the given {@link InputStream} and returns the number of
	 * totally read bytes.
	 * </p>
	 * 
	 * @param stream
	 *        The {@link InputStream} to compute size of.
	 * @return The number of bytes in the given stream.
	 * 
	 * @throws IOException
	 *         iff the given {@link InputStream} throws some
	 */
	public static long size(InputStream stream) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];

		long result = 0;
		while (true) {
			int direct = stream.read(buffer);
			if (direct >= 0) {
				result += direct;
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * Distinction of a {@link Stream} by a given {@link Function}.
	 * 
	 * @see Stream#distinct()
	 */
	public static <T> Stream<T> distinctBy(Stream<T> stream, Function<T, ?> distinctBy) {
		Stream<EqualityRedirect<T>> distinctStream = mapToEqualityRedirect(stream, distinctBy).distinct();

		return distinctStream.map(equalityRedirect -> equalityRedirect.getValue());
	}

	private static <T> Stream<EqualityRedirect<T>> mapToEqualityRedirect(Stream<T> stream, Function<T, ?> distinctBy) {
		CustomEqualitySpecification<T> equalsSpec = new CustomEqualitySpecification<>(distinctBy);

		return stream.map(value -> new EqualityRedirect<>(value, equalsSpec));
	}

	/**
	 * Concatenates the {@link Stream streams}.
	 * 
	 * @param streams
	 *        If the array is null, the empty stream is returned. Streams that are null are replaced
	 *        by the empty stream. Nulls in the streams are retained untouched.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> Stream<T> concat(Stream<? extends T>... streams) {
		if (streams == null || streams.length == 0) {
			return Stream.empty();
		}
		if (streams.length == 1) {
			/* The cast is safe, as it is not possible to add something to a stream. */
			@SuppressWarnings("unchecked")
			Stream<T> singletonStream = (Stream<T>) streams[0];
			return nonNull(singletonStream);
		}
		if (streams.length == 2) {
			return Stream.concat(nonNull(streams[0]), nonNull(streams[1]));
		}
		return Arrays.stream(streams).flatMap(Function.identity());
	}

	/** Returns the {@link Stream}, unless it is null, in which case the empty stream is returned. */
	public static <T> Stream<T> nonNull(Stream<T> stream) {
		if (stream == null) {
			return Stream.empty();
		}
		return stream;
	}

	/**
	 * Writes the given {@link Properties} in {@link #ISO_8859_1} encoding to the given output.
	 * 
	 * <p>
	 * Output is normalized in following sense:
	 * <ul>
	 * <li>No "current date" is contained in the output.</li>
	 * <li>Lines are sorted in natural order.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * After the entries have been written, the output stream is flushed. The output stream remains
	 * open after this method returns.
	 * </p>
	 * 
	 * @param out
	 *        Stream to write content to.
	 * @param props
	 *        The {@link Properties} to write.
	 */
	public static void storeNormalized(OutputStream out, Properties props) throws IOException {
		ByteArrayStream buffer = new ByteArrayStream();
		props.store(buffer, null);

		// Properties are written ISO-8859-1 encoded.
		Charset cs = ISO_8859_1;

		List<String> allLines;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(buffer.getStream(), cs))) {

			// Remove first line containing the current date!
			String firstLine = br.readLine();
			if (firstLine == null) {
				// Does actually not occur, but is complained by FindBugs.
				// If first line is null, nothing must be written.
				return;
			}
			assert firstLine.charAt(0) == '#' : "First line is a comment containing the current date.";

			// Sort all lines
			allLines = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				allLines.add(line);
			}
			allLines.sort(null);
		}

		// Dump lines
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, cs));
		for (String tmp : allLines) {
			bw.write(tmp);
			bw.newLine();
		}
		bw.flush();
	}

}

