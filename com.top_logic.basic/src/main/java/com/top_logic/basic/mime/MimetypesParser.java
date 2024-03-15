/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
 package com.top_logic.basic.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.activation.FileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.sax.SAXUtil;


/** Helper to extract the mimetypes from a web.xml file.
 * 
 * This class comes in handy in case you want to parse
 * a web.xml file but there is no Servlet context around.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class MimetypesParser extends ElementHandler {

    /** Parse a web.xml file and extract the Mimetypes 
     *
     * @param  filename name of the file to parse, usually web.xml
     *
     * @return the map of Mimetypes.
     */
	public static FileTypeMap parse(String filename) throws SAXException, ParserConfigurationException, IOException {
		return parse(BinaryDataFactory.createBinaryData(new File(filename)));
    }

    /**
	 * Parse a web.xml file and extract the Mimetypes
	 *
	 * @param file
	 *        the file to parse, usually web.xml
	 *
	 * @return the map of Mimetypes.
	 */
	public static FileTypeMap parse(BinaryContent file) throws SAXException, ParserConfigurationException, IOException {
		return parse(file, createStorage());
	}

	private static HashMap<String, String> createStorage() {
		return new HashMap<>();
	}

	/**
	 * Parse a web.xml file and extract the Mimetypes
	 *
	 * @param file
	 *        the file to parse, usually web.xml
	 *
	 * @return the map of Mimetypes.
	 */
	private static FileTypeMap parse(BinaryContent file, Map<String, String> storage)
			throws ParserConfigurationException, SAXException, IOException {
		try (InputStream in = file.getStream()) {
			return parse(in, storage);
		}
	}

	/**
	 * Parses the given web.xml files and extract their Mimetypes.
	 * 
	 * <p>
	 * The given files is a fallback chain in which entries of a file with a higher index are
	 * overriden by entries for files with lower index.
	 * </p>
	 * 
	 * @param files
	 *        The fallback chain of files to parse.
	 * @return The accumulated {@link FileTypeMap}.
	 */
	public static FileTypeMap parse(BinaryContent... files)
			throws SAXException, ParserConfigurationException, IOException {
		int numberFiles = files.length;
		switch(numberFiles) {
    		case 0: return new HashedFileTypeMap();
			case 1:
				return parse(files[0]);
    		default: {
				HashMap<String, String> storage = new HashMap<>();
				int index = numberFiles - 1;
				while (index > 0) {
					parse(files[index], storage);
					index--;
				}
				return parse(files[0], storage);
    		}
    	}
    }

	/**
	 * Parses the given web.xml content.
	 */
	public static FileTypeMap parse(InputStream in) throws ParserConfigurationException, SAXException {
		return parse(in, createStorage());
	}

	/**
	 * Parses the given web.xml content.
	 */
	public static FileTypeMap parse(InputStream in, Map<String, String> storage)
			throws ParserConfigurationException, SAXException {
		SAXParser parser = SAXUtil.newSAXParser();
		MimetypesParser handler = new MimetypesParser(storage);

        try {
			parser.parse(in, handler);
            return handler.getTypeMap();
        }
        catch (Exception ex) {
			Logger.warn("Unable to parse mime types from '" + in + "'!", MimetypesParser.class);

            return (FileTypeMap.getDefaultFileTypeMap());
        }
	}
    
    /** The typemap used to accumulate the types */
    protected HashedFileTypeMap typeMap;
    
    /** The extension currently parsed */
    protected String extension;
    
    /** The mime-type currently parsed */
    protected String mimeType;

    /** Constructor, call just before parsing */
	protected MimetypesParser(Map<String, String> storage)
    {
		typeMap = new HashedFileTypeMap(storage);
    }
    
    /** Accssor to the accumulated MimetypesFileTypeMap */
    protected FileTypeMap getTypeMap()
    {
    	return typeMap;
    }
    
    /* actual code to parse the following ...
     	<mime-mapping>
     		<extension>ico</extension> 
		<mime-type>image/x-icon</mime-type> 
	</mime-mapping>
     */
    
    /** Care about the mime-mapping, extension and mime-type tags.
     * <pre>
     *   mime-mapping
     *   extension
     *   mime-type</pre> 
     */
    @Override
	public void endElement(String uri, String localName, String qName)
        throws SAXException {
        if ("mime-mapping".equals(qName)
                && extension != null
                && mimeType != null) {
            typeMap.addType(extension, mimeType);

            extension = null;
            mimeType  = null;
        } 
        else if ("extension".equals(qName)) {
            extension = this.getString().trim();
        }
        else if ("mime-type".equals(qName)) {
            mimeType = this.getString().trim();
        }
    }

    /** take care that we resolve the web-app dtd locally (if possible) */
    @Override
	public InputSource resolveEntity(String publicId, String systemId)
                             throws SAXException {
        if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".
            equals(publicId)) {
            InputSource result = new InputSource(
                getClass().getResourceAsStream("web-app_2_3.dtd"));
            // Parser is not happy without SystemId and PublicId
            result.setPublicId(publicId);
            result.setSystemId("http://java.sun.com/dtd/web-app_2_3.dtd");
            return result;
        }
        if ("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".
            equals(publicId)) {
            InputSource result = new InputSource(
                getClass().getResourceAsStream("web-app_2_2.dtd"));
            // Parser is not happy without SystemId and PublicId
            result.setPublicId(publicId);
            result.setSystemId("http://java.sun.com/j2ee/dtds/web-app_2_2.dtd");
            return result;
        }
        Logger.warn("Don know how to resolveEntity publicId: '" 
                + publicId + "' systemId: ' " + systemId + "'" , this);
        // fallback to default 
        return null; 
    }
  
}
