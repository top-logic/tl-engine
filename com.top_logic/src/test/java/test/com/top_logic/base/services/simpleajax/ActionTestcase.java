/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Common superclass for tescases around ClientAction.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ActionTestcase extends AbstractLayoutTest {

    public ActionTestcase() {
        super();
    }

    public ActionTestcase(String aName) {
        super(aName);
    }
    
    /** 
     * TODO JDK 1.5 validate against xsd.
     * 
     * @param expected may be null in this case only the XML will be checked.
     */
    void assertXMLOutput(ClientAction action, String expected) 
        throws Exception 
    {
        TagWriter out  = new TagWriter();
        action.writeAsXML(displayContext(), out);
        String result = out.toString();
        
        validate(result);
      
        // System.out.println(result);
        if (expected != null)
            assertEquals(expected, result);
    }
    
    /**
     * Validate the given String.
     */
    public static void validate (String aString) throws Exception 
    {
       // TODO JDK 1.5 validate against DTD ?
		SAXParser theParser = SAXUtil.newSAXParserValidatingNamespaceAware();
       
       TagWriter responseWriter = new TagWriter();
       
       responseWriter.beginBeginTag("env:Envelope");
       responseWriter.writeAttribute("xmlns", HTMLConstants.XHTML_NS);
       responseWriter.writeAttribute("xmlns:env", "http://www.w3.org/2001/12/soap-envelope");
       responseWriter.writeAttribute(AJAXConstants.AJAX_XMLNS_ATTRIBUTE, AJAXConstants.AJAX_NAMESPACE);
       responseWriter.endBeginTag();
       responseWriter.beginTag("env:Body");

       responseWriter.beginBeginTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);
       responseWriter.endBeginTag();
       
       responseWriter.writeContent(aString);

       responseWriter.endTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);

       responseWriter.endTag("env:Body");
       responseWriter.endTag("env:Envelope");
       
       responseWriter.flush();
       
       InputSource theInput = new InputSource(new StringReader(responseWriter.toString()));
       
       theParser.parse (theInput, new DefaultHandler ());
    }

}

