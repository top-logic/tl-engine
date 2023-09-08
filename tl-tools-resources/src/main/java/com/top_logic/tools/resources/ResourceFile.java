/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A {@link ResourceFile} is a list of key-value entries with some additional meta informations.
 * The ResourceFile represents one property file containing resource keys and values. In
 * difference to {@link Properties} a {@link ResourceFile} might contain duplicate keys.
 */
public class ResourceFile {

	private static final String NL;

	static {
		StringWriter nlBuffer = new StringWriter();
		try (PrintWriter nlPrinter = new PrintWriter(nlBuffer)) {
			nlPrinter.println();
			nlPrinter.close();
		}

		NL = nlBuffer.toString();
	}

	private File _file;

	/**
	 * All resources defined in this {@link ResourceFile} indexed by their key.
	 */
	private Map<String, String> _resources;

	/**
	 * Creates a new empty {@link ResourceFile}.
	 */
	public ResourceFile() {
		this((File) null);
	}

	/**
	 * Creates a new {@link ResourceFile} from values of the given {@link Properties}.
	 */
	public ResourceFile(Properties contents) {
		this();
		putAll(contents);
	}

	/**
	 * Sets all key/value pairs in the given {@link Properties} instance.
	 */
	public void putAll(Properties contents) {
		for (Entry<?, ?> entry : contents.entrySet()) {
			setProperty((String) entry.getKey(), (String) entry.getValue());
		}
	}

	/**
	 * Sets all key/value pairs in the given other {@link ResourceFile}.
	 */
	public void putAll(ResourceFile contents) {
		for (Entry<?, ?> entry : contents._resources.entrySet()) {
			setProperty((String) entry.getKey(), (String) entry.getValue());
		}
	}

	/**
	 * Creates a new {@link ResourceFile} based on the given file.
	 */
	public ResourceFile(File file) {
		_file = file;
		_resources = new HashMap<>();

		if (_file != null && _file.exists()) {
			readProperties(file);
		}
	}

	/**
	 * Converts the contents to a {@link Properties} instance.
	 */
	public Properties toProperties() {
		Properties result = new Properties();
		for (Entry<String, String> entry : _resources.entrySet()) {
			result.setProperty(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * The {@link File} object on which this resource file bases. May be <code>null</code>.
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * Whether no resources are defined in this {@link ResourceFile}.
	 */
	public boolean isEmpty() {
		return _resources.isEmpty();
	}

	/**
	 * All keys in this file.
	 */
	public Collection<String> getKeys() {
		return _resources.keySet();
    }

	/**
	 * The value with the given key locally defined in this {@link ResourceFile}, or
	 * <code>null</code>, if no such value exists.
	 */
	public String getProperty(String key) {
		return _resources.get(key);
	}

	/**
	 * Updates the value associated with the given key.
	 */
	public void setProperty(String key, String value) {
		_resources.put(key, value);
	}

	/**
	 * Drops the value with the given key.
	 * 
	 * @return The value that was associated with the key before.
	 */
	public String removeProperty(String key) {
		return _resources.remove(key);
	}

	private void readProperties(File data) {
		try (InputStream in = stream(data)) {
			load(in);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * This method is mostly taken from {@link Properties#load(InputStream)}
	 */
	public void load(InputStream in) throws IOException {
	    char[] convtBuf = new char[1024];
	    LineReader reader = new LineReader(in);
	
	    int limit;
	    int keyLen;
	    int valueStart;
	    char c;
	    boolean hasSep;
	    boolean precedingBackslash;

	    while ((limit = reader.readLine()) >= 0) {
	        c = 0;
	        keyLen = 0;
	        valueStart = limit;
	        hasSep = false;
	
	        // System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
	        precedingBackslash = false;
	        while (keyLen < limit) {
	            c = reader.lineBuf[keyLen];
	            // need check if escaped.
	            if ((c == '=' || c == ':') && !precedingBackslash) {
	                valueStart = keyLen + 1;
	                hasSep = true;
	                break;
	            } else if (whiteSpace(c) && !precedingBackslash) {
	                valueStart = keyLen + 1;
	                break;
	            }
	            if (c == '\\') {
	                precedingBackslash = !precedingBackslash;
	            } else {
	                precedingBackslash = false;
	            }
	            keyLen++;
	        }
	        while (valueStart < limit) {
	            c = reader.lineBuf[valueStart];
	            if (nonWhiteSpace(c)) {
	                if (!hasSep && (c == '=' || c == ':')) {
	                    hasSep = true;
	                } else {
	                    break;
	                }
	            }
	            valueStart++;
	        }
			String key = loadConvert(reader.lineBuf, 0, keyLen, convtBuf);

			/*
			// Skip unencoded whitespace at the end of the value.
			while (limit > valueStart) {
				c = reader.lineBuf[limit - 1];
				if (nonWhiteSpace(c)) {
					break;
				}
				limit--;
			}
			*/
			String value = loadConvert(reader.lineBuf, valueStart, limit - valueStart, convtBuf);
	
			setProperty(key, value);
	    }
	}

	/**
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved chars to their
	 * original forms
	 */
	public static String loadConvert(char[] in, int off, int len, char[] buffer) {
	    if (buffer.length < len) {
	        int newLen = len * 2;
	        if (newLen < 0) {
	            newLen = Integer.MAX_VALUE;
	        }
	        buffer = new char[newLen];
	    }
	    char aChar;
	    char[] out = buffer;
	    int outLen = 0;
	    int end = off + len;
	
	    while (off < end) {
	        aChar = in[off++];
	        if (aChar == '\\') {
	            aChar = in[off++];
	            if (aChar == 'u') {
	                // Read the xxxx
	                int value = 0;
	                for (int i = 0; i < 4; i++) {
	                    aChar = in[off++];
	                    switch (aChar) {
	                        case '0':
	                        case '1':
	                        case '2':
	                        case '3':
	                        case '4':
	                        case '5':
	                        case '6':
	                        case '7':
	                        case '8':
	                        case '9':
	                            value = (value << 4) + aChar - '0';
	                            break;
	                        case 'a':
	                        case 'b':
	                        case 'c':
	                        case 'd':
	                        case 'e':
	                        case 'f':
	                            value = (value << 4) + 10 + aChar - 'a';
	                            break;
	                        case 'A':
	                        case 'B':
	                        case 'C':
	                        case 'D':
	                        case 'E':
	                        case 'F':
	                            value = (value << 4) + 10 + aChar - 'A';
	                            break;
	                        default:
	                            throw new IllegalArgumentException(
	                                        "Malformed \\uxxxx encoding.");
	                    }
	                }
	                out[outLen++] = (char) value;
	            } else {
	                if (aChar == 't')
	                    aChar = '\t';
	                else if (aChar == 'r')
	                    aChar = '\r';
	                else if (aChar == 'n')
	                    aChar = '\n';
	                else if (aChar == 'f')
	                    aChar = '\f';
	                out[outLen++] = aChar;
	            }
	        } else {
				out[outLen++] = aChar;
	        }
	    }
	    return new String(out, 0, outLen);
	}

	/**
	 * Save the contents of this {@link ResourceFile} back to {@link #getFile()}.
	 * 
	 * <p>
	 * Must only be called when {@link #getFile()} is not <code>null</code>.
	 * </p>
	 */
	public void save() throws IOException {
		if (getFile() == null) {
			throw new UnsupportedOperationException(
				"Resource file " + _file + " does not base on a File object.");
		}
		saveAs(getFile());
	}

	/**
	 * Save the contents of this {@link ResourceFile} back to the given file.
	 * 
	 * @param file
	 *        The new file to write to.
	 */
	public void saveAs(File file) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			writeTo(out);
		}
	}

	/**
	 * Tests whether the current {@link #getFile() source} is in normal form.
	 */
	public boolean isNormalized() {
		if (getFile() == null || !getFile().exists()) {
			return false;
		}
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			writeTo(buffer);
			byte[] current = Files.readAllBytes(getFile().toPath());
			byte[] expected = buffer.toByteArray();
			return Arrays.equals(expected, current);
		} catch (IOException ex) {
			return false;
		}
	}

	/**
	 * Writes the contents to the given stream.
	 */
	public void writeTo(OutputStream out) throws IOException {
		Charset charset = Charset.forName("ISO_8859-1");
		CharsetEncoder encoder = charset.newEncoder();
		OutputStreamWriter writer = new OutputStreamWriter(out, charset);

		ArrayList<String> keyList = new ArrayList<>(getKeys());
		Collections.sort(keyList);

		for (String key : keyList) {
			String translation = getProperty(key);
			if (translation == null) {
				continue;
			}

			writer.write(key);
			writer.write(" = ");
			int start = 0;
			int limit = translation.length();
			while (start < limit) {
				char ch = translation.charAt(start);
				if (!whiteSpace(ch)) {
					break;
				}
				start++;
			}
			while (limit > start) {
				char ch = translation.charAt(limit - 1);
				if (!whiteSpace(ch)) {
					break;
				}
				limit--;
			}
			for (int n = 0, cnt = translation.length(); n < cnt; n++) {
				char ch = translation.charAt(n);
				switch (ch) {
					case '\\': {
						writer.write("\\\\");
						break;
					}
					case '\r': {
						writer.write("\\r");
						break;
					}
					case '\n': {
						writer.write("\\n");
						break;
					}
					case ' ':
						if (n >= start && n < limit) {
							writer.write(ch);
						} else {
							writer.write("\\ ");
						}
						break;
					case '\t':
						if (n >= start && n < limit) {
							writer.write(ch);
						} else {
							writer.write("\\t");
						}
						break;
					case '\f':
						if (n >= start && n < limit) {
							writer.write(ch);
						} else {
							writer.write("\\f");
						}
						break;
					default: {
						if (encoder.canEncode(ch)) {
							writer.write(ch);
						} else {
							encode(writer, ch);
						}
					}
				}
			}
			writer.write(NL);
		}

		writer.flush();
	}

	private static boolean nonWhiteSpace(char ch) {
		return !whiteSpace(ch);
	}

	private static boolean whiteSpace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\f';
	}

	private static void encode(OutputStreamWriter writer, char ch) throws IOException {
		writer.write("\\u" + fill(Integer.toHexString(ch).toUpperCase()));
	}

	private static String fill(String hexString) {
		return "0000".substring(hexString.length()) + hexString;
	}

	private static InputStream stream(File data) throws FileNotFoundException {
		return new FileInputStream(data);
	}

	/**
	 * Normalizes the given {@link File}.
	 * 
	 * @param normalizationOnly
	 *        Whether the save process should make sure that the contents of the file has not been
	 *        altered semantically.
	 */
	public static void normalize(File f, boolean normalizationOnly) throws IOException {
		new ResourceFile(f).save();
	}

}