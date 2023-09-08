/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols.file;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.layout.LayoutConstants;



/**
 * Writer for file based Symbols.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FileSymbolWriter {
	
	private static String ENCODING = LayoutConstants.UTF_8;

    /** The writer to be used for writing. */
    private TagWriter writer;
    private FileSymbolFactory theFactory;
    
    FileSymbolWriter(FileSymbolFactory fact){
    	this.theFactory = fact;
    }

    /**
     * Write all symbols contained in the given map to the symbol file.
     *
     * @param    aMap   The map containing the symbols.
     * @return   The number of written symbols or -1, if writing fails.
     */
    public synchronized int write (Map aMap) {
        int theResult = 0;

        try {
            if (this.writeHeader ()) {
                Iterator theIt = aMap.keySet ().iterator ();

                while (theIt.hasNext ()) {
                    this.writeSymbol ((Symbol) aMap.get (theIt.next ()));

                    theResult++;
                }

                this.writeFooter ();
            }

			getWriter().flush();
        }
        catch (Exception ex) {
            Logger.error ("Error in writing, file destroyed!", ex, this);

            theResult = -1;
        }

        return (theResult);
    }

    protected void writeSymbol (Symbol aSymbol) throws IOException {
        TagWriter theWriter = this.getWriter ();

        theWriter.beginBeginTag("section");
        theWriter.writeAttribute("name", aSymbol.getSymbolName());
        theWriter.endBeginTag();

        theWriter.beginBeginTag("entry");
		theWriter.writeAttribute("name", "allow");
		theWriter.writeAttribute("value", aSymbol.getAllow().getACLString());
		theWriter.endEmptyTag();
        
		theWriter.beginBeginTag("entry");
		theWriter.writeAttribute("name", "deny");
		theWriter.writeAttribute("value", aSymbol.getDeny().getACLString());
		theWriter.endEmptyTag();
		
        theWriter.endTag ("section");
    }

    /**
     * false when current witer is null.
     */
    protected boolean writeHeader () throws IOException {
        TagWriter theWriter = this.getWriter ();

        if (theWriter != null) {
			theWriter.writeXMLHeader(ENCODING);
            theWriter.beginTag ("root");
            theWriter.beginTag ("properties");
            return true;
        }

        return false;
    }

    protected void writeFooter () throws IOException {
        TagWriter theWriter = this.getWriter ();

        theWriter.endTag ("properties");
        theWriter.endTag ("root");

        theWriter.flush ();
		theWriter.close();
    }

    /**
     * Returns the writer to be used for writing data.
     *
     * @return    The writer or null, if file not found.
     */
    protected TagWriter getWriter () {
        if (this.writer == null)  {
            try {
				String path = AliasManager.getInstance().replace(this.theFactory.getSymbolPath());
				this.writer = TagWriter.newTagWriter(new File(path), ENCODING);
			} catch (IOException ex) {
                Logger.error ("Unable to instanciate writer", ex, this);
            }
        }

        return (this.writer);
    }

}
