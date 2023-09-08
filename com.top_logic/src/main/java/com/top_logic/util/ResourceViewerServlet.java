/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DAPropertyNames;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.mig.html.ContainerDetector;

/**
 * This servlet allows view/download of data (files) encoded as data sources.
 * 
 * The access to any data source within the application can be send to the 
 * client by this servlet. Depending on the location of the source (within
 * the web context or outside the context) this class uses different mechanism
 * to send the data to the client.
 * 
 * The servlet can be instructed by the following parameters:
 * <ul>
 *     <li><b>command</b>: The command to be executed. There are two different
 *         ways of modes, the {@link #COMMAND_DOWNLOAD download} and the
 *         {@link #COMMAND_VIEW view} mode. If there is no command in the 
 *         request, the view command will be treated as default.</li>
 *     <li><b>url</b>: The physical resource of the element to be processed
 *         (where the physical resource can be either a 
 *         {@link com.top_logic.dsa.DataSourceAdaptor data source} or an
 *         <a href="http://www.w3.org/TR/1998/REC-html40-19980424/intro/intro.html#h-2.1.1">
 *          HTTP based URI</a> relative to the current application context). 
 *          This parameter is mandatory.</li>
 *     <li><b>version</b>: The version of the file to be processed. This 
 *         parameter will only receive attention, if the URL is a data
 *         source. If the source doesn't support version, the handling of
 *         this parameter depends on the underlying implementation of the
 *         data source.</li>
 * </ul>
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author  <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class ResourceViewerServlet extends TopLogicServlet {

    /** command (view/download) encoded in the request. */
    public static final String PARAM_COMMAND             = "command";

    /** Reference (to be resolved into DSN incl. version) encoded in the Request */
    public static final String PARAM_REF                 = "ref";
    
    /** value for command, View the data encoded in PARAM_URL */
    public static final String COMMAND_VIEW              = "view";
    
    /** value for command, inline the data encoded in PARAM_URL */
    public static final String COMMAND_INLINE            = "inline";

    /** value for command, Download the data encoded in PARAM_URL */
    public static final String COMMAND_DOWNLOAD          = "download";

    
    private static final String VERSION_TAG = "_version_";

    /**
     * Set of allowed DataSourceNames, to allow security.
     */
    protected Set allowedDSNs;

    /** 
     * Overridden to extract allowed DSN names.
     */
    @Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String configuredDSNs = config.getInitParameter("allowedDSNs");
        if (configuredDSNs == null) {
            Logger.error("allowedDSNs Paramter Missing", this);
        } else {
            allowedDSNs = new HashSet(StringServices.toList(configuredDSNs, ','));
        }
    }

    /**
     * Do nothing because cache disabling seems to interfere with downloading.
     * (Apache 1.3.14 on Solaris / IE 5 on NT)
     */
    @Override
	protected void setCachePolicy (HttpServletResponse aResponse) {
        // do nothing
    }

    /**
     * Dispatch the command as given by command=view/download.
     */
    @Override
	protected void doGet (HttpServletRequest aReq, HttpServletResponse aRes)
                                    throws IOException, ServletException {
        boolean debug = Logger.isDebugEnabled(this);

        // get the command parameter
        String theCommand = aReq.getParameter(PARAM_COMMAND);
        String theSource  = ResourceViewerServlet.getDSN(aReq.getParameter (PARAM_REF));
        String theDSN;
        String theVersion;
        int    theLength = VERSION_TAG.length();

        if (theSource.endsWith(VERSION_TAG)) {
            theDSN    = theSource.substring(0, theSource.length() - theLength);
            theVersion = null;
        }
        else {
            int theTag = theSource.lastIndexOf(VERSION_TAG);

            theDSN     = theSource.substring(0, theTag);
            theVersion = theSource.substring(theTag + theLength);
        }

        if (debug) {
            Logger.debug ("query string: " + aReq.getQueryString (), this);
        }

        try {
            DataAccessProxy theDAP   = new DataAccessProxy(theDSN);
            String          protocol = theDAP.getProtocol();
            if (allowedDSNs == null || !allowedDSNs.contains(protocol)) {
                throw new ServletException("Protocol '" + protocol + "' not configured");
            }

            if (theCommand == null || theCommand.equals (COMMAND_VIEW)) {
                this.commandView (theDAP, theVersion, aReq, aRes, debug);
            }
            else if (theCommand.equals (COMMAND_DOWNLOAD)) {
            	this.commandDownload (theDAP, theVersion, aReq, aRes, debug);
            }
            else if (theCommand.equals (COMMAND_INLINE)) {
                this.commandInline (theDAP, theVersion, aReq, aRes, debug);
            }
            else {
                throw new ServletException("Unknown command '" + theCommand + "'");
            }
        }
        catch (Exception dax) {
            Logger.error("Unable to process command " + theCommand, dax, this);

            throw new ServletException(dax);
        }
    }

    /**
     * Reacts on an HTTP POST request, performs the method 
     * {@link #doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)}.
     */
    @Override
	protected void doPost (HttpServletRequest request, 
                           HttpServletResponse response) throws IOException, 
                                                                ServletException {
        this.doGet (request, response);
    }

    /**
     * Download the data encoded by url as an <code>application/octet-
     * stream</code>. If the given URL is a W3 URI, the content at the position
     * is returned. Otherwise a DataAccessProxy is created for the URL and it's
     * entry returned.
     *
     * @param    aDAP        The DataAccessProxy specifying the file.
     * @param    aVersion    The version number of the requested file.
     * @param    aReq        The request.
     * @param    aRes        The response.
     */
    private void commandDownload (DataAccessProxy aDAP,  String aVersion, 
                                  HttpServletRequest aReq, 
                                  HttpServletResponse aRes, boolean debug)
                                        throws IOException, ServletException, DatabaseAccessException {
        if (Logger.isDebugEnabled (this)) {
            Logger.debug ("Downloading URL: " + aDAP, this);
        }
        String theName = aDAP.getDisplayName ();
        int    theLen  = getDAPLength(aDAP, aVersion);
        
		InputStream theStream = null; // Stream of data that will be sent.
        if (aVersion == null) {
            theStream = aDAP.getEntry ();
        }
        else {
            theStream = aDAP.getEntry (aVersion);
        }
		try {
			commandDownload(aVersion, aReq, aRes, theName, theLen, theStream);
		} finally {
			theStream.close();
		}
    }

    /** Internal handler to copy a stream to the user.
     * 
     * Set some addition Header info to make the UserAgent (Browser) happy.
     *
     * @param    aVersion    The version number of the requested file.
     * @param    aReq        The request.
     * @param    aRes        The response.
     * @param    aName       Name of the file send by the downloading user.
     * @param    aStream     The data to be transfered.
     */
    protected void commandDownload(
        String aVersion, HttpServletRequest aReq,  HttpServletResponse aRes,
        String aName   , int length, InputStream aStream)
        throws IOException, ServletException {
            
		aRes.setContentType("application/octet-stream");
		aRes.setHeader("Content-Disposition",
						"attachment; filename=\"" + aName + "\"");
		if (length > 0 && ContainerDetector.getInstance().setContentLength()) {
			aRes.setContentLength(length);
        }
		FileUtilities.copyStreamContents(aStream, aRes.getOutputStream());
		aRes.flushBuffer();
    }
    
    /**
     * If the given URL is a W3C URI or the DataAccessProxy created from the
     * URL returns a value in the <code>getURL</code> method the resource at the
     * resulting URL is forwarded to. Otherwise a DataAccessProxy is created
     * with the URL and it's entry returned. It has the the mime-type of the
     * data or <code>application/octet- stream</code>, if the mime-type is
     * unknown. Thus the browsers decides how to display the data.
     *
     * @param    aDAP        The Datasource resource to be displayed.
     * @param    aVersion    The version of the document to be displayed 
     *                         (may be <code>null</code>).
     * @param    aReq        The request.
     * @param    aRes        The response.
     */
    private void commandInline(
    		DataAccessProxy aDAP, String aVersion, 
    		HttpServletRequest aReq, HttpServletResponse aRes, boolean debug)
    throws IOException, ServletException,   DatabaseAccessException {
    	if (debug) {
    		Logger.debug("DSN: " + aDAP, this);
    	}
    	
    	// The resource is not representable by a http URL.
    	// Directly return the data using it's mime type.
    	this.setMimeTypeInResponse(aDAP, aRes);
    	this.setDataInResponse(aDAP, aVersion, aRes);
    }

    /**
     * If the given URL is a W3C URI or the DataAccessProxy created from the
     * URL returns a value in the <code>getURL</code> method the resource at the
     * resulting URL is forwarded to. Otherwise a DataAccessProxy is created
     * with the URL and it's entry returned. It has the the mime-type of the
     * data or <code>application/octet- stream</code>, if the mime-type is
     * unknown. Thus the browsers decides how to display the data.
     *
     * @param    aDAP        The Datasource resource to be displayed.
     * @param    aVersion    The version of the document to be displayed 
     *                         (may be <code>null</code>).
     * @param    aReq        The request.
     * @param    aRes        The response.
     */
    private void commandView(
        DataAccessProxy aDAP, String aVersion, 
        HttpServletRequest aReq, HttpServletResponse aRes, boolean debug)
        throws IOException, ServletException,   DatabaseAccessException {
        if (debug) {
            Logger.debug("DSN: " + aDAP, this);
        }

        // if not versioned file just forward.
        if (aVersion == null && this.forwardToURL(aDAP, aReq, aRes, debug)) {
            return;
        }

        // The resource is not representable by a http URL.
        // Directly return the data using it's mime type.
        this.setMimeTypeInResponse(aDAP, aRes);
        this.setFileNameInResponse(aDAP, aVersion, aRes, debug);
        this.setDataInResponse(aDAP, aVersion, aRes);
    }

    /**
     * Forwards to a http(s) URL.
     * 
     * @param    aURL    A <code>http</code> or <code>https</code> URL. 
     * @param    aReq    A HttpServletRequest.
     * @param    aRes    A HttpServletResponse that has not been committed.
     * @throws   IOException if I/O fails in forwarding.
     * @throws   ServletException if the URL is a Servlet an an error occurs in
     * rendering it.
     */
    private void forwardToURL(String aURL, HttpServletRequest aReq, HttpServletResponse aRes)
                                        throws IOException, ServletException {
//    Not sure about the cache policy with proxies. This has to be checked
//    in a company net with proxy servers.
//    The view of documents will not work, when the next line is active!
//
//        super.setCachePolicy(aRes);
        this.forwardPage(aURL, aReq, aRes);
    }

    /**
     * Forwards to the URL that represents the DataAccessProxy and returns true.
     * If there is no URL representation there is no forwarding and false is
     * returned.
     * 
     * @param aDAP a DataAccessProxy.
     * @param aReq a HttpServletRequest.
     * @param aRes a HttpServletResponse that has not been committed.
     * 
     * @return <code>true</code>, if the forwarding was sucessful.
     * 
     * @throws IOException if I/O fails in forwarding.
     * @throws ServletException if the URL is a Servlet an an error occurs in
     *         rendering it.
     */
    private boolean forwardToURL(DataAccessProxy aDAP,
                                 HttpServletRequest aReq,
                                 HttpServletResponse aRes, boolean debug) throws IOException, ServletException, DatabaseAccessException {
        try {
            String theFWD  = aDAP.getForwardURL();
    
            if (theFWD != null) {
                if (debug) {
                    Logger.debug("Forwarding to: " + theFWD, this);
                }

                this.forwardToURL(theFWD,aReq, aRes);

                return true;
            }
        } 
        catch (NotSupportedException nsox) { 
                // this is ok since getForwardURL() is not always supported
            Logger.debug(
                "Unable to getForwardURL()  for '"
                    + aDAP + "' will try redirect." + nsox,this);
        }
        try {
            String theURL = aDAP.getURL();

            if (theURL != null) {

                if (debug) {
                    Logger.debug("Redirecting to " + theURL, this);
                }
                aRes.sendRedirect(theURL);
                return true;
            }
        } catch (NotSupportedException nsox) { 
                // this is ok since getForwardURL() is not always supported
            Logger.debug(
                "Unable to sendRedirect()  for '"
                    + aDAP + "'." +
                    nsox,this);
        }
        return false;
    }
    
    /**
     * Sets the MIME type of the data from the given DataAccessProxy as the MIME
     * type of the ServletResponse. If the MIME type is unknown
     * <code>application/octect-stream</code> is set.
     * @param aDAP a DataAccessProxy.
     * @param aRes a HttpServletResponse that has not been committed.
     * @throws DatabaseAccessException if there is a problem in the
     * DataAcessProxy.
     */
    private void setMimeTypeInResponse(
        DataAccessProxy aDAP,
        HttpServletResponse aRes)
        throws DatabaseAccessException {
            
        boolean debug = Logger.isDebugEnabled(this);
        String theType = aDAP.getMimeType();
        if (theType == null) {
            if (debug) {
                Logger.debug("No type defined for " + aDAP + ", using default",this);
            }
            theType = "application/octect-stream";
        }
        if (debug) {
            Logger.debug("Content type set to "+ theType+ "for DataAccessProxy "+ aDAP,this);
        }
        aRes.setContentType(theType);
    }
    
    /** Get the name from the given DAP.
     * 
     * Solutions may wish to override this to find a KO for the
     * DAP and take that name. e.g. POS which shortens the DS-names.
     * 
     * @param  aDAP Proxy to some document.
     * @return name of Document to be shown to the user when downloading/viewing it.
     */
    protected String getNameFromDAP(DataAccessProxy aDAP) {
		return aDAP.getDisplayName();
    }

    /**
     * Sets the file name of the given DataAccessProxy's data in the
     * <code>Content-Disposition</code> header of the given ServletResponse. If
     * the DataAccessProxy cannot deliver a file name, the filename will be set
     * to "unknown".
     * @param aDAP a DataAccessProxy.
     * @param aRes a HttpServletResponse that has not been committed.
     */
    private void setFileNameInResponse(
        DataAccessProxy aDAP, String aVersion,
        HttpServletResponse aRes, boolean debug) {
            
        String theName   = getNameFromDAP(aDAP);
        int    theLength = this.getDAPLength(aDAP, aVersion);
        if (debug) {
            Logger.debug("Content filename set to " + theName, this);
        }
        
        aRes.setHeader("Content-Disposition", "filename=\"" + theName + "\"");

        if (theLength > 0 && ContainerDetector.getInstance().setContentLength()) {
            if (debug) {
                Logger.debug("ContentLength " + theLength, this);
            }
            aRes.setContentLength(theLength);
        }
    }

    
    /**
     * Writes the data from the entry stream of the given DataAccessProxy to the
     * OutputStream of the ServletResponse.
     * @param aDAP a DataAccessProxy.
     * @param aVersion the Version of the data to be retrieved. May be
     * <code>null</code>.
     * @param aRes a HttpServletResponse that has not been committed.
     * @throws IOException if a problem occurs reading or writing the data.  
     * @throws DatabaseAccessException if there is a problem in the
     * DataAcessProxy.
     */
    private void setDataInResponse(
        DataAccessProxy aDAP,
        String aVersion,
        HttpServletResponse aRes)
        throws IOException, DatabaseAccessException {
            
        // copy the element into the response
        InputStream theStream =
            (aVersion == null) ? aDAP.getEntry() : aDAP.getEntry(aVersion);
        if (theStream != null) {
			try {
				OutputStream output = aRes.getOutputStream();
				try {
					FileUtilities.copyStreamContents(theStream, output);
				} finally {
					output.close();
				}
			}
			finally {
			    theStream.close();
			}
            aRes.flushBuffer();
        } else {
            Logger.warn("Couldn't get the entry " + aDAP 
                + " (Version " + aVersion+ ") - no stream.",this);
        }
    }
    
    
    /** 
     * Return length of data found in aDAP.
     * 
     * @param aVersion optional name of version to use. 
     */
    protected int getDAPLength(DataAccessProxy aDAP, String aVersion) {
        int theLength = 0;
		{
            DataObject theProps;

            if (aVersion == null) {
                theProps = aDAP.getProperties();
            }
            else {
                theProps = aDAP.getProperties(aVersion);
            }

            Long length = (Long) theProps.getAttributeValue(DAPropertyNames.SIZE);
        
            if (length != null) {
                theLength = length.intValue();
            }
        }
        return theLength;
    }

    public static String getDSN(String aRef) {
        return ReferenceManager.getSessionInstance().getSource(aRef);
    }

    public static String getDocumentReference(String aDSN, String aVersion) {
        if (StringServices.isEmpty(aVersion)) {
            return ReferenceManager.getSessionInstance().getReference(aDSN + VERSION_TAG);
        }
        else {
            return ReferenceManager.getSessionInstance().getReference(aDSN + VERSION_TAG + aVersion);
        }
    }
}
