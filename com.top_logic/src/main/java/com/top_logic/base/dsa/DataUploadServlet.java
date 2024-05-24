/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

import com.top_logic.basic.Logger;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet for uploading files into DataSources.
 * 
 * <em>Warning:</em> This Servlet can be a major security risk when used
 * without caution ! (does not extend TopLogicServlet)
 *
 * Use the "cmd" Parmer to trigger the following commands <ul>
 *  <li> update(dataSource) If the dataSource is a container its
 *                          child-entries are tried to update. 
 *                          If the dataSource is an entry, this
 *                          entry will be updated. 
 *  </li> 
 *  <li> create(dataSource) Where dataSource is the conatiner and
 *                          the filename is taken from the client
 *  </li> 
 *  <li> upload(dataSource) Decide whether an update or creation in senseful
 *  </li>
 *  <li> mkdir(dataSource)  Create new directory</li> 
 * </ul>
 * It uses functionality of the apache-commons-fileupload.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:hbo@top-logic.com">Holger Borchard</a>
  */
public abstract class DataUploadServlet extends HttpServlet implements UploadHandler{
    
   /**
    * The name of the data-source. 
    * 
    * private, because it is only used in test-cases (security)
    */
    private static final String DATA_SOURCE = "dataSource";
    
    /**
    * The name of the directory parameter of the POST-Request. 
    * 
    * private, because it is only used in test-cases (security)
    */
    public static final String DIR = "dir";
    
    /**
    * The name of the command parameter in the POST-Request.
    * 
    * This command may be update
    * (invocation of putEntry-method), create (invocation of createEntry-method) or check (
    * invocation of createEntry or putEntry-method). 
    */
    public static final String COMMAND = "cmd";
      
    /**
    * This constants is one of the possible command-values.
    * The putEntry-metod of the DataAccessProxy is invoked.
    */
    public static final String UPDATE = "update";
    
    
    /**
    * This constants is one of the possible command-values.
    * The createEntry-metod of the DataAccessProxy is invoked.
    */
    public static final String CREATE = "create";
    
    /**
    * This constants is one of the possible command-values.
    * The doUpload-method is invoked.
    * 
    */
    public static final String UPLOAD = "upload";
    
    /**
    * This constants is one of the possible command-values.
    * The doMkdir-method is invoked.
    * 
    */
    public static final String MKDIR = "mkdir";
    
    /**
    * Handler for the different kind of uploads.
    * 
    */
    private  UploadHandler uploadHandler;
    
    /**
     * Default Konstruktur. Set the instance-variable uploadHandler.
     */
    public DataUploadServlet(){
        this.uploadHandler = this;
    }
    
    /** 
     *  Override to provide security
     * 
     * @param aRequest  may be used to fetch a TLContext.
     * @param aCommand  Value of COMMAND     as extracted from request, always != null 
     * @param aDsn      Value of DATA_SOURCE as extracted from request, always != null
     * @param extra     depending on command, usually the name of some entry.
     * 
     * 
     * @return true when aCommand is allowed with given parameters
     * @throws ServletException when command is not allowed
     */
    protected abstract boolean allow(HttpServletRequest aRequest, String aCommand, String aDsn, Object extra) 
        throws ServletException;

	@Override
	public final void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		/* The type parameters are necessary here. Without them, Eclipse reports an error. */
		TopLogicServlet.<IOException, ServletException> withSessionIdLogMark(aRequest,
			() -> doPostWithLogMark(aRequest, aResponse));
	}

    /**
    * Dispatches the cmd-parameter and extract the files.
    *
    * @param    aRequest     The send request. This may be a multipart-request
    *                        or a normal request. If it is a normal request, only
    *                        the mkdir-command will be execute.
    * @param    aResponse    The send response.
    *
    * @throws   ServletException    If the request for the POST could not 
    *                               be handled.
    * @throws   IOException         If an input or output error is detected 
    *                               when the servlet handles the POST request. 
    */   
	protected void doPostWithLogMark(HttpServletRequest aRequest, HttpServletResponse aResponse)
            throws ServletException, IOException {
        if (FileUploadBase.isMultipartContent(aRequest)){
            try{
                DiskFileUpload theUpload = new DiskFileUpload();
                this.initializeFileUpload(theUpload);       
                List theFileItems = theUpload.parseRequest(aRequest);
                Map theFormFieldMap = this.extractFormFields(theFileItems);
                this.uploadFileItems(aRequest, theFileItems, theFormFieldMap);
            }
            catch(FileUploadException fue){
                throw new ServletException(fue);
            }
        }
        else{
			{
                String theCommand = aRequest.getParameter(COMMAND);
                if (MKDIR.equals(theCommand)){
                    doMkdir(aRequest);
                }
            }
        }
        
        
    }
    
    /**
    * Create all entries of the given list of FileItems with the given DataAccessProxy.
    * 
    *
    * @param    aProxy       the DataAccessProxy
    * @param    aFileItems   a list of instances of org.apache.commons.fileupload.FileItem
    *
    * @throws   ServletException        if the aProxy does not exists or is no container
    * @throws   DatabaseAccessException if the createEntry-method failed
    * @throws   IOException             if the building of InputStream from the FileItem failed 
    *                                
    */            
     public DataAccessProxy doCreate(DataAccessProxy aProxy, List aFileItems)
                                   throws ServletException, DatabaseAccessException, IOException{
        DataAccessProxy theNewProxy = null;                                                            
        if (aProxy.exists()){
             throw new ServletException("'" + aProxy + "' must not exist");
        }
        Iterator theIter = aFileItems.iterator();
        while (theIter.hasNext()){
            FileItem theItem = (FileItem)theIter.next();
            if (theItem != null && !theItem.isFormField()){
                theNewProxy = uploadHandler.createEntry(aProxy, theItem);   
            }
        }                          
        return theNewProxy;            
    }

    /**
    * Create a new entry with the given DataAccessProxy that represents the given FileItem.
    * The name of this new entry is the name of the uploaded client-file.
    *
    * @param    aProxy       the DataAccessProxy
    * @param    anItem       an Instances of FileItem, that contains the uploaded client-file.
    * @return   the DataAccessProxy of the uploaded file
    *
    *
    * @throws   DatabaseAccessException if the createEntry failed
    * @throws   IOException             if the createEntry failed 
    *                                
    */     
    @Override
	public DataAccessProxy createEntry(DataAccessProxy aProxy, FileItem anItem)
		                          throws IOException, DatabaseAccessException {
        String theFileName;
        if (aProxy.isContainer()) { // create entry in container                                      
            theFileName = anItem.getName();  
        }
        else // does not yet exists, create the entry 
        {
            theFileName = aProxy.getName();
            aProxy = aProxy.getParentProxy();
        }
        InputStream theStream = anItem.getInputStream();
		try {
			return createEntry(aProxy, theStream, theFileName);
		} finally {
			theStream.close();
		}
                                      
    }
    
	@Override
	public DataAccessProxy createEntry(DataAccessProxy aProxy, InputStream aStream,
						String aFileName) throws DatabaseAccessException, IOException {
		DataAccessProxy theNewProxy = null;
		if (aFileName != null && aFileName.length() > 0) {
			aFileName = new File(aFileName).getName();
			theNewProxy = aProxy.createEntryProxy(aFileName, aStream);
			if (theNewProxy.isRepository()) {
				theNewProxy.unlock();
			}
		}
		return theNewProxy;
	}
    
    /**
    * Update the entry of the given proxy by using the FileItems of the given list.
    * If aProxy is an entry this entry will be updated regarding the first FileItem
    * of the given list, that has a file-name that contains an inputStream that is not
    * null.
    * If a proxy is a container, it is tried to update an entries with the same name
    * as the file-names in this container. 
    *
    * @param    aProxy       the DataAccessProxy
    * @param    aFileItems   a list of instances of org.apache.commons.fileupload.FileItem
    *
    * @throws   ServletException        if the aProxy does not exists
    * @throws   DatabaseAccessException if the putEntry-method failed
    * @throws   IOException             if the putEntry-method failed 
    *                                
    */
    public DataAccessProxy doUpdate(DataAccessProxy aProxy, List aFileItems)
                                    throws ServletException, DatabaseAccessException, IOException{
        Iterator theIter = aFileItems.iterator();
        if (aProxy.exists()&& aProxy.isEntry()){
            boolean isPut = false;
            while (theIter.hasNext() && !isPut){
                FileItem theItem = (FileItem) theIter.next();
                if (theItem != null && !theItem.isFormField()){
                    isPut = uploadHandler.putEntry(aProxy, theItem);
                    if (isPut && theIter.hasNext()){
                        Logger.info("Unexpected more than one file!", this);
                    }
                }
            
            }                                                  
        }
        else if (aProxy.exists()&& aProxy.isContainer()){
            while (theIter.hasNext()){
                FileItem theItem = (FileItem) theIter.next();
                if (theItem != null && !theItem.isFormField()){
                    uploadHandler.putEntryInContainer(aProxy, theItem);
                }
            }
        }
        return aProxy;
    }
    
    /**
     * Put an entry with the given proxy.
     * The content of the entry is determine by the given FileItem.
     * The update takes place if the name of the given FileItem is not null 
     * and consists of at least one sign. This is a criterion for an exsiting
     * client-file.
     * 
     * @param   aProxy  the DataAccessProxy
     * @param   anItem a FileItem that represents the content of the entry.
     * 
     * @return  true, if the update takes place
     * 
     * @throws   DatabaseAccessException if the putEntry-method failed
     * @throws   IOException             if the putEntry-method failed 
     */
    @Override
	public boolean putEntry(DataAccessProxy aProxy, FileItem anItem)
                           throws IOException, DatabaseAccessException {
        InputStream theStream = anItem.getInputStream();
		try {
			String theFileName = anItem.getName();
			// check for empty filename
			if (theFileName != null && theFileName.length() != 0) {
				return putEntry(aProxy, theStream);
			}
		} finally {
			theStream.close();
		}
        return false;
    }

	/**
	 * Put an entry with the given proxy. The content of the entry is determine by the given
	 * InputStream.
	 * 
	 * @param aProxy
	 *        the DataAccessProxy
	 * @param aStream
	 *        some InputStream that represents the content of the entry. must not be
	 *        <code>null</code> .
	 * 
	 * @return true, if the update takes place
	 * 
	 * @throws DatabaseAccessException
	 *         if the putEntry-method failed
	 * @throws IOException
	 *         if the putEntry-method failed
	 */
	@Override
	public boolean putEntry(DataAccessProxy aProxy, InputStream aStream)
									throws DatabaseAccessException, IOException {
		if (aProxy.isRepository()) {
			aProxy.lock();
		}
		aProxy.putEntry(aStream);
		if (aProxy.isRepository()) {
			aProxy.unlock();
		}
		return true;
	}
    
    /**
     * Try to update a child entry of the given DataAccessProxy.
     * It is checked if an entry with the same name as the
     * name of the client-files exists. If so, this entry will be updated.
     * 
     * @param   aProxy the DataAccessProxy
     * @param   anItem aFileItem the contains the uploaded File
     * 
     * @return  true if an entry is uploaded
     * 
     * @throws   DatabaseAccessException if the putEntry-method failed
     * @throws   IOException             if the putEntry-method failed  
     */
    @Override
	public  boolean putEntryInContainer(DataAccessProxy aProxy, FileItem anItem) 
                                          throws DatabaseAccessException, IOException{
        String theFileName = anItem.getName();
        boolean isPut = false;
        if (theFileName != null){
            theFileName = new File(theFileName).getName();
            DataAccessProxy theChildProxy = aProxy.getChildProxy(theFileName);
            if (theChildProxy.exists()){
                uploadHandler.putEntry(theChildProxy, anItem);
                isPut = true;
            }
            else{
                Logger.info("Update nicht möglich, da DataSource nicht existiert", this);
            }
        }
        return isPut;
        
    }
     
    /**
    * Check the given DataAccessProxy.
    * If the proxy exists and is an entry, invoke the doUpdate-method of this class.
    * If the proxy exists and is a container, invoke the putEntryInContainer-Method
    * for every FileItem of the given list. If this method failed, because no entry
    * with the given name exists, invoke the createEntry-method for that item.
    * If both conditions are not satisfied throw a ServletException.
    *
    * @param    aProxy       the DataAccessProxy
    * @param    aFileItems   a list of instances of org.apache.commons.fileupload.FileItem
    *
    */  
    public DataAccessProxy doUpload(DataAccessProxy aProxy, List aFileItems)
                                    throws ServletException, DatabaseAccessException, IOException{
        if (!aProxy.exists()){
            throw new ServletException("Data-Source does not exists!");
        }
        else if (aProxy.isEntry()){
            return doUpdate(aProxy, aFileItems);
        }
        else if (aProxy.isContainer()){
            Iterator theIter = aFileItems.iterator();
            while (theIter.hasNext()){
                FileItem theItem = (FileItem) theIter.next();
                if(!uploadHandler.putEntryInContainer(aProxy, theItem)){
                    uploadHandler.createEntry(aProxy, theItem);
                }
            }
        }
        return aProxy;
     }
     
    /**
     * Invoke the creation of a new directory.
     * 
     * @param   aRequest that contains a string that described a datasource 
     *          (Parameter-Name dataSource) and the name of the new directory
     *          (Parameter-Name dir)
     * @throws DatabaseAccessException if creation failed
     */
     public void doMkdir(HttpServletRequest aRequest) throws DatabaseAccessException, ServletException {
         String theDataSource = aRequest.getParameter(DATA_SOURCE);
         String theDir       = aRequest.getParameter(DIR);
         if (theDataSource != null && theDir != null 
          && allow(aRequest, MKDIR, theDataSource, theDir)) {
             DataAccessProxy theProxy = new DataAccessProxy(theDataSource);
             uploadHandler.mkdir(theProxy, theDir);
         }
     }
     
    /**
    * Invoke the creation of a new directory.
    * 
    * @param   aProxy the DataAccessProxy
    * @param   aDirName the name of new directory 
    * @throws DatabaseAccessException if creation failed
    */
    @Override
	public void mkdir(DataAccessProxy aProxy, String aDirName)
                            throws DatabaseAccessException {
        if (aProxy.exists() && aProxy.isContainer()){
            aProxy.createContainer(aDirName);
        }
    }
    
    /**
     * Extract all FileItems that represent simple form-field (not uploaded files).
     * The fieldName and the corresponding value of this value are put as name/values-
     * pairs into a Map: e.g. key = cmd; value = create. 
     * The extract item is removed from the list.
     * @param aFileItems a list of FileItem
     * 
     * @return theFormFieldMap --> key: name of form fiels, value: value of form-field
     */ 
     public Map extractFormFields(List aFileItems){
         Map theFormFieldMap = new HashMap();
         Iterator theIter = aFileItems.iterator();
         while (theIter.hasNext()){
             FileItem theItem = (FileItem) theIter.next();
             if (theItem.isFormField()){
                String theFieldName = theItem.getFieldName();
                String theValue = theItem.getString();
                theFormFieldMap.put(theFieldName, theValue);
                theIter.remove();
             }
         }
         return theFormFieldMap;                           
     }
     
     /**
      * Decide whether the doCreate, doUpdate or doUpload-method is invoked.
      * 
      * @param  aFileItems a list of FileItems
      * @param  aFormFieldMap a map the contains a name of a form-field as key and its 
      *         value as value. This class uses only the keys cmd and dataSource.
      * @throws ServletException if upload failed
      * @throws DatabaseAccessException if upload failed
      * @throws IOException if upload failed
      */
     public void uploadFileItems(HttpServletRequest aRequest, List aFileItems, Map aFormFieldMap) 
                                throws ServletException, DatabaseAccessException, IOException{
        String theCommand = (String)aFormFieldMap.get(COMMAND);
        String theDataSource = (String)aFormFieldMap.get(DATA_SOURCE);
        if (theCommand == null || theDataSource == null){
            throw new ServletException("Incomplete Request: Command or DataSource is missing!");
        }
        if (!allow(aRequest, theCommand, theDataSource, aFileItems))
            return;
        if (CREATE.equals(theCommand)){
            doCreate(new DataAccessProxy(theDataSource), aFileItems);            
        }
        else if (UPDATE.equals(theCommand)){
            doUpdate(new DataAccessProxy(theDataSource), aFileItems);
        }
        else if (UPLOAD.equals(theCommand)){
             doUpload(new DataAccessProxy(theDataSource), aFileItems);
        }
         
     }
     
     /**
      * Initialize the multipart-fileupload.
      * Only the repository-path is needed.
      */
     public  void initializeFileUpload(DiskFileUpload anUpload){
        // maximum size before a FileUploadException will be thrown
        //anUpload.setSizeMax(1000000);
        // maximum size that will be stored in memory
        //anUpload.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        anUpload.setRepositoryPath(System.getProperty("java.io.tmpdir"));
     }
  
}
