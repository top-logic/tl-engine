/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.multipart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * This class Wraps a multi-part Request created by special HTML-forms.
 * 
 * <p>
 * These forms are typically used to download files:
 * </p>
 * 
 * <pre>
 * &lt;form method="POST" enctype="multipart/form-data" action="..."&gt;
 *     &lt;input name="file"      type="file" /&gt; 
 *     &lt;input name="submit"    type="submit" /&gt;
 * &lt;/form&gt;
 * </pre>
 * 
 * <p>
 * You can use multiple input tags of type "file", but using the same name twice is not supported by
 * this class. The Encoding works by looking at and encoding the InputStream found in the Request.
 * </p>
 * 
 * <p>
 * In case you want to decode the Stream on the fly (to conserve DiskSpace use
 * {@link ServletFileUpload#getItemIterator(HttpServletRequest)} directly.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class MultipartRequest extends HttpServletRequestWrapper {

    /* Copy of obfuscated HTML-Code above 
        <form method="POST" enctype="multipart/form-data" action="save.jsp">
            <input name="file"      type="file" /> <br />
            <input name="submit"    type="submit" />
        </form>
    */

    /** Default Maximum size of a file to be loaded. used as protection 4 GB */
    public static final long    DEFAULT_MAX_POST_SIZE = 4L*1024L*1024L*1024L;
                                                     
    /** Fixed Buffer length for a line of Text read */
    public static final int     BUF_LEN = 1024;
    
    /**
     * Directory used to temporary save the files.
     * may be null indicating the System tmp-area.
     */
    private File                         dir; // TODO KHA create a 

    /** Map of the "normal" parameters found in the wrapped request. */
	private Map<String, String[]> parameters;

    /** List with all fileItems. */
    private List<FileItem> fileItems;

    /**
     * Constructs a new MultipartRequest to handle the specified request. 
     * 
     * Will save any uploaded files in the given directory. 
     * Limiting the upload size to the given size. 
     * If the content is too large, an IOException is thrown. This constructor actually parses 
     * the multipart/form-data and throws an IOException if there's any problem reading or parsing the request.
     *
     * @param       aDir the directory in which to save any uploaded files.
     * @param       aSize the maximum size of the Content to extract
     * @exception   IOException if the uploaded content is larger than the given size or other problems occur.
     */
     public MultipartRequest (HttpServletRequest aRequest, File aDir, long aSize) 
                                    throws IOException {
        super(aRequest);
         
        int length = aRequest.getContentLength ();
        if (length > aSize)  {
            throw new IOException ("Content length (" + length + 
                                   ") bigger than allowed (" + aSize + ")");
        }

        // null is ok, will use system directory ...
        if (aDir != null) {
            if (!aDir.mkdirs () || !aDir.isDirectory () || aDir.canWrite ()) {
                throw new IOException ("Invalid Directory " + aDir);
            }
            this.dir = aDir;
        }
    }

    /**
     * Constructs MultipartRequest with DEFAULT_MAX_POST_SIZE.
     * 
     * Will save any uploaded files in the given directory. Limiting the 
     * upload size to the given size. If the content is too large, an 
     * IOException is thrown. This constructor actually parses the 
     * multipart/form-data and throws an IOException if there's any 
     * problem reading or parsing the request.
     *
     * @param       aDir        The directory in which to save any 
     *                          uploaded files.
     * @exception   IOException if the uploaded content is larger 
     *                          than the given size or other problems occure.
     */
    public MultipartRequest (HttpServletRequest aRequest, File aDir) 
                                 throws IOException {
        this (aRequest, aDir, DEFAULT_MAX_POST_SIZE );
    }

    /**
     * Constructs MultipartRequest with DEFAULT_MAX_POST_SIZE and system tmp-dir.
     * 
     * Will save any uploaded files in a system temporary area.
     * Limiting the upload size to the given size. 
     * If the content is too large, an IOException is thrown. This constructor actually parses 
     * the multipart/form-data and throws an IOException if there's any problem reading or parsing the request.
     *
     * @param       aRequest the servlet request
     * @exception   IOException if the uploaded content is larger than the given size or other problems occur.
     */
    public MultipartRequest (HttpServletRequest aRequest) 
                                throws IOException  {
        this (aRequest, null, DEFAULT_MAX_POST_SIZE);                             
    }

    public synchronized List<FileItem> getFiles() {
        
        /* remove all the filed parameters */
        if(this.parameters == null) {
            this.initParameterMap();
        }
        
        return this.fileItems;
    }

    /**
     * Creates the directory to be used for temporary storage of the
     * uploaded files.
     *
     * This method will normally be called once, so the execution can take
     * some more time (if needed).
     *
	 * @return The directory to be used, using {@link Settings#getTempDir()}.
	 */
    protected File createTempDir () {
        return Settings.getInstance().getTempDir();
    }

    /**
     * Create a new {@link ServletFileUpload} to handle a MultiPart-Request.
     * 
     * Subclasses may use other parameters than the ones shown here.
     */
    private ServletFileUpload createFileUpload() {
       ServletFileUpload theResult = new ServletFileUpload(
               new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, dir));
       
       return theResult;
    }
    
    /**
     * Extract all FileItems that represent simple form-field (not uploaded files).
     * The fieldName and the corresponding value of this value are put as name/values-
     * pairs into a Map: e.g. key = cmd; value = create.
     * String -> List of Strings 
     * The extract item is removed from the list.
     * 
     * @param aFileItems a list of FileItem all non form fields are removed
     * @return theFormFieldMap --gt; key: name of form fiels, value: value of form-field
     * 
     * ?deprecated not longer used. usage of transparent MultiPartRequest here.
     */ 
     private Map<String, String[]>  extractFormFields(List<FileItem> aFileItems){
         int size   = aFileItems.size();
		HashMap<String, Object> theMap = new HashMap<>(size);
         int i      = 0;
         while( i<size ) {
             FileItem theItem = aFileItems.get(i);
             if (theItem.isFormField()){
                String theFieldName = theItem.getFieldName();
                String theStringValue = theItem.getString();
                /* multiple parameters with the same name (e.g. radio-buttons) */
                insertValue(theMap, theFieldName, theStringValue);

                aFileItems.remove(i);
                size --;
                continue;
             }
             i++;
         }
		return cast(theMap);
     }

	/**
	 * Ensures that the values ins the given map are string arrays, and casts the map.
	 * 
	 * @param tmp
	 *        Map to cast. Expects that each value is either a list of {@link String}s or a
	 *        {@link String} array.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String[]> cast(HashMap<String, Object> tmp) {
		for (Entry entry : tmp.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof List<?>) {
				List<String> listValue = (List<String>) value;
				entry.setValue(listValue.toArray(new String[listValue.size()]));
			}
		}
		return (Map) tmp;
	}

	/**
	 * Assigns the given string value to the given field name.
	 * 
	 * @param tmp
	 *        Map to fill. Expects that each value is either a list of {@link String}s or a
	 *        {@link String} array. If there is already an entry for the given field name the string
	 *        value is "appended" to the former value.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void insertValue(Map<String, Object> tmp, String fieldName, String stringValue) {
		Object values = tmp.get(fieldName);
		if(values == null) {
		    values = new String[] {stringValue}; // usually only one element
			tmp.put(fieldName, values);
		} else if (values.getClass().isArray()) {
			List<String> listValue = new ArrayList<>(2);
			listValue.add(((String[]) values)[0]);
			listValue.add(stringValue);
			tmp.put(fieldName, listValue);
		} else {
			((List) values).add(stringValue);
		}
	}

    /**
     * Is called to extract all the parameters from the wrapped request.
     */
    private void initParameterMap() {

        try {
            ServletFileUpload theUpload = this.createFileUpload(); 
            String theEncoding = this.getRequest().getCharacterEncoding();
            if (StringServices.isEmpty(theEncoding)) {
            	theEncoding = LayoutConstants.UTF_8;
            }
            theUpload.setHeaderEncoding(theEncoding); 
            this.fileItems  = parseRequest(theUpload, (HttpServletRequest)super.getRequest());
            this.parameters = this.extractFormFields(this.fileItems);
        } catch (FileUploadException e) {
            Logger.error("problem parsing multi-part request", e, this);
        }
    }

    /** 
     * Extracted to use @SuppressWarnings("unchecked") here.
     * 
     * Can be removed when commons file Upload becomes JDK1.5 aware.
     */
    @SuppressWarnings("unchecked")
    private static List<FileItem> parseRequest(ServletFileUpload theUpload, HttpServletRequest request) throws FileUploadException {
        return theUpload.parseRequest(request);
    }

    /**
     * Returns the names of all the parameters as an Enumeration of Strings. 
     * It returns an empty Enumeration if there are no parameters.
     *
     * @return the names of all the parameters as an Enumeration of Strings.
     */
    @Override
	public synchronized Enumeration<String> getParameterNames () {

        if(this.parameters == null) {
            this.initParameterMap();
        }
        
        return Collections.enumeration(parameters.keySet());
    }

    /**
     * Returns the value of the named parameter as a String, or null if the parameter was not 
     * sent or was sent without a value. 
     *
     * The value is guaranteed to be in its normal, decoded form. If the parameter has multiple 
     * values, only the last one is returned (for backward compatibility). For parameters with 
     * multiple values, it's possible the last "value" may be null.
     *
     * @param   aName the parameter name.
     * @return the parameter value
     */
    @Override
	public synchronized String getParameter (String aName) {
    
        if(this.parameters == null) {
            this.initParameterMap();
        }
        
		String[] values = this.parameters.get(aName);
		if (values == null) {
			return null;
		}
		return values[values.length - 1];
    }

    /**
     * The map of parameters.
     */
    @Override
	public synchronized Map<String, String[]> getParameterMap() {

        if(this.parameters == null) {
            this.initParameterMap();
        }

        return this.parameters;
    }

    /**
     * Returns the values of the named parameter as List of Strings, or null if the parameter was not sent. 
     * The List has one entry for each parameter field sent. If any field was sent without a 
     * value that entry is stored in the array as a null. The values are guaranteed to be 
     * in their normal, decoded form. A single value is returned as a one-element List.
     *
     * @param   aName the parameter name.
     * @return  the parameter values
     */
    @Override
	public synchronized String[] getParameterValues(String aName) {

        if(this.parameters == null) {
            this.initParameterMap();
        }
        
		String[] theList = this.parameters.get(aName);

         if (theList != null) {
			return theList;
         }
		return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    /**
     * Returns the directory to be used for temporary storage of the
     * uploaded files.
     *
     * @return    The directory to be used.
     */
    File getTempDir () {
        if (this.dir == null) {
            this.dir = this.createTempDir ();
        }

        return (this.dir);
    }

    /**
     * @param    aType    The type of the request to be inspected.
     * @return   <code>true</code>, if request is multi-part.
     */
    public static boolean isMultipart(String aType) {
        return ((aType != null) && aType.toLowerCase().startsWith("multipart/form-data"));
    }

    /**
     * @param    aRequest    The request to be inspected.
     * @return   <code>true</code>, if request is multi-part.
     */
    public static boolean isMultipart(HttpServletRequest aRequest) {
        return (MultipartRequest.isMultipart(aRequest.getHeader("Content-Type")));
    }

    /**
	 * Checks, whether the given request is a multi-part one.
	 * 
	 * @return true if request is a multi-part request.
	 */
	public static boolean isMultipartContent(HttpServletRequest req) {
	    String theType = req.getHeader("Content-type");
	    return theType != null 
	        && theType.startsWith("multipart/");
	}
	
}
