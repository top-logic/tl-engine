package com.meterware.httpunit.parsing;

import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Element;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.SAXException;

import com.meterware.httpunit.dom.HTMLDocumentImpl;


/**
 * Using JSoup as HTML parser to create HTML documents.
 * 
 * @author <a href="mailto:maziar.behdju@top-logic.com">Maziar Behdju</a>
 **/
class JSoupHTMLParser implements HTMLParser {
	static final int MAX_ERRORS = 128;
	
    public void parse( URL pageURL, String pageText, HTMLParserListener listener, DocumentAdapter adapter ) throws IOException, SAXException {
    	Parser htmlParser = Parser.htmlParser();
    	Parser xmlParser = Parser.xmlParser();
    	org.jsoup.nodes.Document jsoupDocument;
    	try {
        	try {
				jsoupDocument = tryParserToEncode(htmlParser, pageURL, pageText);
	        	if (htmlParser.getErrors().size() > 0) {
	        		jsoupDocument = tryParserToEncode(xmlParser, pageURL, pageText);
	        	}
	        	createHTMLDocument(jsoupDocument, adapter);
        	} catch (RuntimeException | SAXException e) {
        		jsoupDocument = tryParserToEncode(xmlParser, pageURL, pageText);
        		createHTMLDocument(jsoupDocument, adapter);
        	}
        	if (listener != null) {
        		for (ParseError error : htmlParser.getErrors()) {
        			String[] indices = error.getCursorPos().split(":");
        			listener.error(pageURL, error.getErrorMessage(), Integer.valueOf(indices[0]), Integer.valueOf(indices[1]));
        		}
        		htmlParser.getErrors().clear();
        		
        		for (ParseError error : xmlParser.getErrors()) {
        			String[] indices = error.getCursorPos().split(":");
        			listener.error(pageURL, error.getErrorMessage(), Integer.valueOf(indices[0]), Integer.valueOf(indices[1]));
        		}
        		xmlParser.getErrors().clear();
        	}
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "UTF-8 encoding failed" );
        }
    }

    private org.jsoup.nodes.Document tryParserToEncode(Parser jsoupParser, URL pageURL, String pageText) throws IllegalStateException, IOException, SAXException {
    	jsoupParser.setTrackErrors(MAX_ERRORS);
    	org.jsoup.nodes.Document jsoupDocument = Jsoup.parse( new ByteArrayInputStream( pageText.getBytes( UTF_ENCODING ) ), UTF_ENCODING, pageURL.getPath(), jsoupParser); 
    	
    	return (org.jsoup.nodes.Document) jsoupDocument;
    }
    
    private void createHTMLDocument(org.jsoup.nodes.Document jsoupDocument, DocumentAdapter adapter) throws IllegalStateException, IOException, SAXException {
    	W3CDom w3cDom = new W3CDom();
		Document w3cDoc = w3cDom.fromJsoup(jsoupDocument);
		HTMLDocument htmlDocument = new HTMLDocumentImpl();
        NodeList nl = w3cDoc.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node importedNode = nl.item(i);
            if ((importedNode.getNodeType() != Node.DOCUMENT_TYPE_NODE) && (importedNode.getNodeType() != -1)) {
            	htmlDocument.appendChild( htmlDocument.importNode( importedNode, true ) );
            }
        }
        adapter.setDocument( htmlDocument );
    }
    
    

    public String getCleanedText( String string ) {
        return (string == null) ? "" : string.replace( NBSP, ' ' );
    }


    public boolean supportsPreserveTagCase() {
        return false;
    }

    public boolean supportsForceTagCase() {
      return false;
    }
    
    public boolean supportsReturnHTMLDocument() {
        return true;
    }


    public boolean supportsParserWarnings() {
        return false;
    }


    final private static char NBSP = (char) 160;   // non-breaking space, defined by JTidy

    final private static String UTF_ENCODING = "UTF-8";
}
