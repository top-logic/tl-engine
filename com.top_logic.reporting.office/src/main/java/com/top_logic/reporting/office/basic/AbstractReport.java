/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionEngine;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.ReportHandler;
import com.top_logic.reporting.office.ReportReaderWriter;
import com.top_logic.reporting.office.ReportResult;
import com.top_logic.reporting.office.ReportToken;
import com.top_logic.reporting.office.StaticSymbolResolver;
import com.top_logic.util.TLContext;


/**
 * Base construct for generating reports with office.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractReport implements ReportHandler, StaticSymbolResolver {

    protected boolean processingComplete;
    protected ReportResult reportResult;
    protected UserInterface contextUser;
    protected ExpansionContext context;
    protected ExpansionEngine engine;
    protected ReportReaderWriter readerWriter;
    protected StaticSymbolResolver resolver;

    /**
     * Default constructor, which is calling {@link #initReportReaderWriter()} to
     * get the needed writer.
     */
    public AbstractReport() {
        this.readerWriter = this.initReportReaderWriter();
    }

    /**
     * Return the writer to be used for generating the report.
     * 
     * @return    The writer to be used for generating the report, never <code>null</code>.
     */
    protected abstract ReportReaderWriter initReportReaderWriter();

    /**
     * Return the name of the template file.
     * 
     * The returned name will later be resolved by the {@link FileManager}.
     * 
     * @return    The name of the template file, never <code>null</code>.
     */
    protected abstract String getTemplateFileName();

    /**
     * Return the empty result file to be filled by this report.
     * 
     * @return   The requested file, never <code>null</code>.
     */
    protected abstract File getResultFile();

    /**
     * Initialize the resolver for this reporter.
     * 
     * @return    The new resolver, never <code>null</code>.
     */
    protected abstract StaticSymbolResolver initResolver();

    /**
     * @see com.top_logic.reporting.office.ReportHandler#reportFinished()
     */
    @Override
	public boolean reportFinished() {
        return this.processingComplete;
    }

    /**
     * initialize the ReaderWriter with the right file locations!
     * 
     * @see com.top_logic.reporting.office.ReportHandler#prepare()
     */
    @Override
	public void prepare() {
        try {            
            File theFile = this.getResultFile();
    
            // Get the template file via the FileManager 
            this.readerWriter.init(this.getTemplateFileName(), this.context.getReportLocale(), theFile);
    
            // set the locale for the context!
            TLContext theContext = TLContext.getContext();
    
            if (theContext != null) {
                theContext.setCurrentLocale(this.context.getReportLocale());
            }
        }
        catch (Exception exp) {
            Logger.error ("Template access was impossible du to fatal error",exp,this);
        }       
    }

    /**
     * The following process is executed:
     * <ol>
     *   <li>read the expansion objects / symbols from the template</li>
     *   <li>expand the expansion object with the expansion engine</li>
     *   <li>create the result file and write the expanded objects to the result file</li>
     * </ol>
     * @see com.top_logic.reporting.office.ReportHandler#processReport()
     */
    @Override
	public void processReport() {
        List theObjects       = null;
        List theListOfSymbols = this.readerWriter.getSymbols();
        int  theSize          = (theListOfSymbols == null) ? 0 : theListOfSymbols.size();
    
        if (theSize > 0) {
            ExpansionEngine  theExpansionEngine  = this.getExpansionEngine();
            ExpansionContext theExpansionContext = this.getExpansionContext();
            ExpansionObject  theCurrent;
    
            theObjects = new ArrayList(theSize);
    
            for (Iterator theIt = theListOfSymbols.iterator(); theIt.hasNext(); ) {
                theCurrent = (ExpansionObject) theIt.next();
    
                theObjects.add(theCurrent);
    
                if (!theCurrent.isExpanded()) {
                    theCurrent.expand(theExpansionEngine, theExpansionContext);
                }
            }
        }
        else {
            theObjects = new ArrayList(0);
        }
    
        this.readerWriter.writeExpansionObjects(theObjects);        
    }

    /**
     * finishing just sets the flag.
     * @see com.top_logic.reporting.office.ReportHandler#finishReport()
     */
    @Override
	public void finishReport() {
        this.processingComplete = true;
        this.reportResult       = new ReportResult(true, this.readerWriter.getResultFilePath(), null);
    }

    /**
     * In this case everything is done in this  
     * @see com.top_logic.reporting.office.ReportHandler#getReportResult()
     */
    @Override
	public ReportResult getReportResult() {
        return this.reportResult;
    }

    /**
     * @see com.top_logic.reporting.office.ReportHandler#setContextUser(com.top_logic.base.user.UserInterface)
     */
    @Override
	public void setContextUser(UserInterface aUser) {
        this.contextUser = aUser;
    }

    /**
     * In this special case we create a SimpleExpansionContext capable of storing a list of 
     * business objects, as taken from the ReportToken and defining the output file path for
     * the generated report. 
     * 
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionContext(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionContext initializeExpansionContext(ReportToken aToken) {
        this.context = new BasicExpansionContext(aToken.getEnvironment());
    
        return (this.context);
    }

    /**
     * The standard ExpansionEngine is capable of interpreting the basic symbol syntax.
     * 
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionEngine(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionEngine initializeExpansionEngine(ReportToken aToken) {
        this.engine = new BasicExpansionEngine();
    
        this.engine.setStaticSymbolResolver(this);
    
        return this.engine;
    }

    /**
     * We resolve the stuff for ourself, so the handler is its own static resolver.
     * 
     * @see com.top_logic.reporting.office.StaticSymbolResolver#resolveSymbol(com.top_logic.reporting.office.ExpansionContext, com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	public Object resolveSymbol(ExpansionContext aContext, ExpansionObject aSymbol) {
        return this.getResolver().resolveSymbol(aContext, aSymbol);
    }

    /**
     * Return the resolver, which will be initialized lazy.
     * 
     * @return    The requested resolver, never <code>null</code>.
     */
    protected StaticSymbolResolver getResolver() {
        if (this.resolver == null) {
            this.resolver = this.initResolver();
        }

        return this.resolver;
    }

    /**
     * Accessor for member.
     */
    protected ExpansionEngine getExpansionEngine() {
        return this.engine;
    }

    /**
     * Accessor for member.
     */
    protected ExpansionContext getExpansionContext() {
        return this.context;
    }
}
