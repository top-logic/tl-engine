/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.xml.DOXMLHandler;

/**
 * This class is used to import ExampleDataObjects from an XML-File. 
 * 
 * The DataObjects may contain nested DataObjects. 
 * The type of the ExampleDataObjects has to be "DataObject".
 *
 * @author Holger Borchard
 */
class DOImporter extends DispatchingHandler{

    /** The List of object finally impoted */
	List doList;
    
    /** A Stack of Dataobjects to supported nestend Objects */
	Stack doStack;
	
	/**
	 * Default Constructor: It invokes the register of MyDOXMLHandler.
	 *
	 */
	public DOImporter(){
		super();
		this.registerHandlers();
	}

	/**
	 * Return a list of all imported DataObjects.
	 */
	public List getDoList(){
		return this.doList;
		 
	}
    
	/**
	 * Invoke the import of DataObject from the given file.
     * 
	 * @param aFile the file that should be converted into a DataObject.
	 * @return true if the import has be completed
	 */
	public boolean doImport(File aFile) {
			boolean result = false;
        
            doList  = new ArrayList((int) (aFile.length() >> 8));   // 256
            doStack = new Stack();

            try {
			SAXParser parser = SAXUtil.newSAXParser();
				parser.parse(aFile, this);
				result = true;
			} catch (SAXException sax) {
				sax.printStackTrace();
			} catch (ParserConfigurationException pcx) {
				pcx.printStackTrace();
			} catch (IOException iox) {
				iox.printStackTrace();
			}
			
			return result;
		}
	/**
	 * Register the MyDOXMLHandler to the name dataobject.
	 *
	 */
	private void registerHandlers(){
		this.registerHandler("dataobject", new MyDOXMLHandler());
		
	}
	
	/**
	 * This class is an extension of the DOXMLHandler.
	 * It add the imported DataObject to the collection doColl of the outer class.
	 * It is allowed to import DataObjects that contains other DataObjects.
	 * 
	 * @author Holger Borchard
	 *
	 * 
	 */
	
	/*package protected*/ class MyDOXMLHandler extends DOXMLHandler{

		/** Name of attribute to put a child into a parent */
		String  atrrName;
		
		/**
		 * Overwrite the method end element of the superclass.
		 * It gets the last element from stack. This last element is the
		 * parent-object of the actual DataObject (child). The child is assigned
		 * to the parent.
		 * If the stack is empty, there is no parent of the current DataObject. So the
		 * current DataObject is the Root-Object and will be added to the doColl of 
		 * the outer class.
		 * 
		 */
		@Override
		public void endElement(String anUri, String anLocalName, String aQName) 
		            throws SAXException {
			super.endElement(anUri, anLocalName, aQName);
			if ("dataobject".equals(aQName)) {
				if (!doStack.isEmpty()){
			    	ExampleDataObject theResult = (ExampleDataObject) doStack.pop();
                    theResult.getMap().put(atrrName, result);  // save away the (now) child
                    result = theResult; // continue processing theResult
			    } else {
			        doList.add(result);
                }
			}
		}	

		/** Overwrite the method getValue of the superclass.
		 * If a type of an element is DataObject, assign the parameter name to instance-
		 * variable attrName. This attrName will be assigned to a child-object when the parsing 
		 * of the element is finished.
		 * 
		 * @see com.top_logic.dob.xml.DOXMLHandler#getValue(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		protected Object getValue(String name, String value, String type)
			throws SAXException {
			if ("DataObject".equals(type)){
				atrrName = name;
				return null; // cannot do that , yet
			} else {
				return super.getValue(name, value, type);
			}
		}

		/** Overwrite the method handleDataObject of the superclass. If the current dataObject is a child-object,
		 * the instance-variable result of the superclass is not null. It contains the parent
		 * object of the current object. This parent-object is pushed onto the stack doStack
		 * (instane-variable of the outer class).
		 * @see com.top_logic.dob.xml.DOXMLHandler#handleDataObject(org.xml.sax.Attributes)
		 */
		@Override
		protected void handleDataObject(Attributes attributes)
			throws SAXException {
			if (result != null) // push parent to stack
				doStack.push(result);
			result = new ExampleDataObject(16);
		}

	}
}
