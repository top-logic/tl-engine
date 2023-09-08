/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.EmptyBinaryData;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * Allows Downloading of (versioned) physical Resources from Wrappers.
 * 
 * Used mostly to download Documents from Webfolders, but
 * should be useable for other Tasks as well.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DownloadHandler extends AbstractDownloadHandler {

    /** The command provided by this instance. */
    public static final String COMMAND = "download";

    /** The flag for the version in the request. */
    public static final String VERSION = "v";

    /** The flag for the version in the request. */
    public static final String[] ATTRIBUTES = { 
            AbstractCommandHandler.OBJECT_ID,  
            AbstractCommandHandler.TYPE,
            DownloadHandler.VERSION };    

    public DownloadHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#getAttributeNames()
     */
    @Override
	public String[] getAttributeNames() {
        String[] theAttr = super.getAttributeNames();
        return (String[]) ArrayUtil.join(theAttr, ATTRIBUTES);
    }

    /**
     * Prepare the download by fetching the Wrapper
     * 
     * @param aComponent the component initiating the download
     * @param arguments the actual request
     * @return the return Object will be given back to you in all following methods,
     *         may be null.
     */
    @Override
	protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo, Map<String, Object> arguments)
			throws IOException {
		TLID theID =
			IdentifierUtil.fromExternalForm(LayoutComponent.getParameter(arguments, AbstractCommandHandler.OBJECT_ID));
        String theType    = LayoutComponent.getParameter(arguments, AbstractCommandHandler.TYPE);
        String theVersion = LayoutComponent.getParameter(arguments, DownloadHandler.VERSION);

		return (new DownloadPackage(WrapperFactory.getWrapper(theID, theType), theVersion));
    }
    
    /** 
     * Extract the download data from the given download pack.
     */
    @Override
	public BinaryDataSource getDownloadData(Object aPrepareResult) throws IOException {
        if (aPrepareResult instanceof File) {
			return BinaryDataFactory.createBinaryData((File) aPrepareResult);
    	}
    	else {
	        DownloadPackage thePack = (DownloadPackage) aPrepareResult;
	        Wrapper         theWrap = thePack.wrapper;
	        File            theFile = thePack.file;
	
	        if (theFile != null) {
				return BinaryDataFactory.createBinaryData(theFile);
	        }
	        
	        InputStream theData;
	        if (theWrap.tHandle().isInstanceOf("Link")) {
	            theData = this.getLinkData(thePack.wrapper);
				return BinaryDataFactory.createFileBasedBinaryData(theData);
	        }
	        else {
	            if (theWrap instanceof DocumentVersion) {
	                theWrap = ((DocumentVersion) theWrap).getDocument();
	            } 
	            // And a Wrap and a DAP and DibbeDabbeDub ;-)
	            DataAccessProxy theDAP = theWrap.getDAP();
	
	            if (theDAP != null) {
	                String theVersion = thePack.version;
	    
	                if (Logger.isDebugEnabled (this)) {
	                    Logger.debug ("Downloading from " + theDAP, this);
	                }
	    
	                if (StringServices.isEmpty(theVersion)) {
	                    theData = theDAP.getEntry();
	                }
	                else {
	                    theData = theDAP.getEntry(theVersion);
	                }      
	    			return BinaryDataFactory.createFileBasedBinaryData(theData);
				} else {
					return EmptyBinaryData.INSTANCE;
	            }
	        }
    	}
    }

    /** 
     * Extract the name from the Wrapper / context
     */
    @Override
	public String getDownloadName(LayoutComponent aComponent, Object aPrepareResult) {
        if (aPrepareResult instanceof DownloadPackage) {
    		return ((DownloadPackage) aPrepareResult).wrapper.getName();
    	}
    	else if (aPrepareResult instanceof File) {
    		return ((File) aPrepareResult).getName();
    	}
    	else {
    		return null;
    	}
    }
    
    /**
     * Special For dataObejcts of type "LINK".
     */
	protected final InputStream getLinkData(Wrapper theWrap) throws IOException {
        String theDSN = theWrap.getDSN();

        if (theDSN != null) {
            if (theDSN.startsWith("http://")) {
                URL theURL = new URL(theDSN);

                Logger.info("Trying download from " + theDSN, this);

                return theURL.openStream();
            }
            else {
                return theWrap.getDAP().getEntry();
            } 
        }
        return null;
    }

    /**
     * Nothing to cleanup here, thanks for asking.
     */
    @Override
	public void cleanupDownload(Object model, Object aContext) {
    
    }
    
    public class DownloadPackage {

    	public final Wrapper wrapper;

    	public File file;	// Used if the document is modified before download (e.g. watermark inserted in PDF document)

        public final String version;

        public DownloadPackage(Wrapper aWrapper, String aVersion) {
            this.wrapper = aWrapper;
            this.version = aVersion;
        }
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
        return InViewModeExecutable.INSTANCE;
    }

}
