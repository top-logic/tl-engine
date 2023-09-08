/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.DBTypeFormat;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DOCollection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;

/** 
 * Utility class to write Data(and Meta-) Objects to XML.
 *
 * The main Utility functions are static and may be used whenever
 * suiteable.
 *
 *                                    for other writers.
 * @author   <a href="mailto:dierk.kogge@top-logic.com">Dierk Kogge</a>
 * @author   <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DOXMLWriter implements DOXMLConstants {

    /** The value for indenting values in the writer. */
    private static final int DEFAULT_INDENT = 2;

    /** Strings that need no special encoding when written as XML */
    protected static final String SPACES = 
    "                                                                                ";

    /** Strings that need no special encoding when written as XML */
    protected static final String DONT_ENCODE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()+-*/.,;: =_";

    /**
     * Convert the given data object to an XML representation.
     *
     * @param    anObject    The object to be converted.
     * @return   The converted object.
     */
    public String convertDO2XML (DataObject anObject) {
        
        StringWriter theWriter = new StringWriter ();
        try {
			writeDO2XML (anObject, theWriter);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
        return theWriter.toString();
    }

    /**
     * Write the given data object as XML representation to a stream and
     * returns this one.
     *
     * Dont use this method for large Object since it uses a byte arrays
     * to buffer thr resullt.
     *
     * @param    anObject    The object to be converted.
     * @return   The stream containing the converted object, please
     *           close() as soon as possible.
     */
    public InputStream getDOasXMLStream (DataObject anObject) {
        
        ByteArrayOutputStream theOut = new ByteArrayOutputStream ();
        try {
			this.writeDO2XML (anObject, theOut);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
        
        ByteArrayInputStream theIn = 
            new ByteArrayInputStream(theOut.toByteArray());
          
        return theIn;
    }

    /**
     * Serialize the given data object as XML to the given stream.
     *
     * First the system tries to use a writer, which encodes with "ISO-8859-1".
     * If this fails, a writer with "UTF-8" will be used. If this fails also,
     * we are using a writer without encoding.
     *
     * @param    anObject    The object to be serialized.
     * @param    aStream     The stream to be used for writing.
     */
    public void writeDO2XML (DataObject anObject, OutputStream aStream) throws IOException {
        Writer theWriter;

        try {
            theWriter = new OutputStreamWriter (aStream, "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
        	throw new AssertionError(ex);
        }

        this.writeDO2XML (anObject, theWriter);
        // flush contents to the actual output stream
        theWriter.flush();
    }

    /**
     * Use the given writer to serialize the given data object to him.
     *
     * The result is a valid XML document, this means, that it contains
     * an XML header! If the given writer is an OutputStreamWriter, we 
     * try to find the used encoding. If not, no encoding information 
     * will be written.
     *
     * @param    anObject    The object to be serialized.
     * @param    out     The stream to be used for writing.
     */
    public void writeDO2XML (DataObject anObject, Writer out) throws IOException {
        String      theEncoding = null;
        if (out instanceof OutputStreamWriter) {
            theEncoding = ((OutputStreamWriter) out).getEncoding ();

            if ("ISO8859_1".equals (theEncoding)) {
                theEncoding = "ISO-8859-1";
            }
        }
        if (theEncoding == null) {
        	theEncoding = "UTF-8";
        }

        out.write ("<?xml version=\"1.0");

        if (theEncoding != null) {
            out.write ("\" encoding=\"");
            out.write (theEncoding);
        }

        out.write ("\"?>");
        nl(out);
        nl(out);

        this.writeDataObject (out, anObject);
    }

    /**
     * Use the given writer to serialize the given data object to him.
     *
     * This method writes all information of the data object into the
     * writer. It is expected, that there has been a preprocessing on the
     * writer to write the header for XML!
     *
     * @param    aWriter     The writer to be used for writing.
     * @param    anObject    The object to be serialized.
     */
    public void writeDataObject (Writer aWriter, DataObject anObject) throws IOException {
        this.writeDataObject (0, aWriter, anObject, null);
    }

    /**
     * Use the given writer to serialize the given data object to it.
     *
     * This method writes all information of the data object into the
     * writer. It is expected, that there has been a preprocessing on the
     * writer to write the header for XML!
	 * If an attribute name is given the DO will be written in an attribute
	 * tag and in a dataobject tag otherwise.
     *
     * @param    anIndent    The indent to be used when writing.
     * @param    out         The writer to be used for writing.
     * @param    anObject    The object to be serialized.
	 * @param	 aName		 An optional attribute name.
     */
    public void writeDataObject (int         anIndent,  Writer out, 
                                 DataObject  anObject, String aName) throws IOException {
        if (anObject instanceof DOCollection) {
            DOCollection theColl     = (DOCollection) anObject;
            boolean      isPrimitive = MetaObjectUtils.isPrimitive(theColl.getCollectionType());

            if (isPrimitive) {
            	if (aName != null) {
	            	this.writeSimpleAttributeTag (anIndent, 
	            			aName, 
	            			"String", 
	            			null, 
	            			out);
            	}
            	else {
            		this.writeStartDOTag(anIndent, anObject, out);
            	}
            	
                for (Iterator<String> theIt = theColl.iterator (); theIt.hasNext(); ) {
                	String theValue = theIt.next ();
                    
                	out.write(SPACES, 0, anIndent + DEFAULT_INDENT);
                	out.write("<item>");
                	out.write(theValue);
                	out.write("</item>\n");
                }
            }
            else {
            	if (aName != null) {
            		this.writeSimpleAttributeTag (anIndent, aName, anObject.tTable().getName(), null, out);
            	}
        		else {
        			this.writeStartDOTag(anIndent, anObject, out);
        		}

            	for (Iterator<DataObject> theIt = theColl.iterator (); theIt.hasNext(); ) {
                    DataObject theValue = theIt.next();
                    
					this.writeDataObject(anIndent + DEFAULT_INDENT, out, theValue, null);
                }
            }

            if (aName != null) {
            	this.writeEndAttrTag (anIndent, out);
            }
            else {
            	this.writeEndDOTag(anIndent, out);
            }
        }
        else {
        	if (aName != null) {
        		this.writeSimpleAttributeTag (anIndent, aName, anObject.tTable().getName(), null, out);
        	}
        	
            this.writeStartDOTag (anIndent, anObject, out);
            this.writeAttributes (anIndent + DEFAULT_INDENT, anObject, out);
            this.writeEndDOTag   (anIndent, out);

            if (aName != null) {
            	this.writeEndAttrTag (anIndent, out);
            }
        }
    }

    /** 
     * Helper function to write out an int value for SQL-Type.
    *
    * The attribut will only be written when it is not the same
    * as its default value.
    *
    * @param   type  Type of the attribute's column.
    * @param   def   default value of the Attribute to write.
    * @param   out   The output is written here.
    */
   private void witeTypeAttr(DBType type, DBType def, String name, Writer out) throws IOException {
       if (type != def) {
			out.write(" ");
           out.write(name);
           out.write("=\"");
           out.write(DBTypeFormat.INSTANCE.getSpecification(type));
           out.write('"');
       }
   }

   /** Helper function to write out an int value for DBAttributes.
     *
     * The attribut will only be written when it is not the same
     * as its default value.
     *
     * @param   val       value of the Attribute to write.
     * @param   def       defualt value of the Attribute to write.
     * @param   out   The output is written here.
     */
    private void witeIntAttr(int val, int def, String name, Writer out) throws IOException {
        if (val != def) {
			out.write(" ");
            out.write(name);
            out.write("=\"");
            out.write(Integer.toString(val));
            out.write('"');
        }
    }
    
    /** Helper function to write out a boolean Attribute.
     *
     * The attribut will only be written when it is not the same
     * as its default value.
     *
     * @param   val       value of the Attribute to write.
     * @param   def       defualt value of the Attribute to write.
     * @param   out   The output is written here.
     */
    private void witeBooleanAttr(boolean val, boolean def, String name, Writer out) throws IOException {
        if (val != def) {
			out.write(" ");
            out.write(name);
            out.write("=\"");
            out.write(Boolean.toString(val));
            out.write('"');
        }
    }

	private void writeMandatoryBooleanAttr(Writer out, String name, boolean val) throws IOException {
		out.write(" ");
		out.write(name);
		out.write("=\"");
		out.write(Boolean.toString(val));
		out.write('"');
	}

	private void writeExternallyNamedAttr(Writer out, String name, ExternallyNamed val)
			throws IOException {
		out.write(" ");
		out.write(name);
		out.write("=\"");
		out.write(val.getExternalName());
		out.write('"');
	}

    /**
     * Write a single MOAttribute as XML, DBAttributes will be honored.
     *
     * @param     aMOA      The attribute to be written.
     * @param     out   The output is written here.
     */
    protected void writeMOAttribute (MOAttribute aMOA, Writer out) throws IOException {
		if (aMOA instanceof MOReference) {
			writeReference((MOReference) aMOA, out);
		} else {
			writePlainAttribute(aMOA, out);
    	}
	}

	private void writePlainAttribute(MOAttribute attribute, Writer out) throws IOException {
		MetaObject meta = attribute.getMetaObject();
		String typeName = meta.getName();
		String attrName = attribute.getName();
		if (typeName.startsWith("java.lang.") ||
			typeName.startsWith("java.util.")) {
			typeName = typeName.substring(10);
		}
		out.write("\t<");
		out.write(MO_ATTRIBUTE_ELEMENT);
		out.write(' ');
		out.write(ATT_NAME_ATTRIBUTE);
		out.write("=\"");
		out.write(attrName);
		out.write("\" ");
		out.write(ATT_TYPE_ATTRIBUTE);
		out.write("=\"");
		out.write(TagUtil.encodeXML(typeName));
		out.write('"');
		writeMandatoryBooleanAttr(out, MANDATORY_ATTRIBUTE, attribute.isMandatory());
		// default false
		witeBooleanAttr(attribute.isImmutable(), false, IMMUTABLE_ATTRIBUTE, out);
		// default false
		witeBooleanAttr(attribute.isInitial(), false, INITIAL_ATTRIBUTE, out);

		if (attribute instanceof DBAttribute) { // write extra attributes here
			DBAttribute dba = (DBAttribute) attribute;
			// a DBAttribute with non DBMetaObject shout not happen ...
			DBMetaObject dbMO = (DBMetaObject) meta;
			String dbName = dba.getDBName();
			if (!attrName.equals(dbName)) {
				out.write(' ');
				out.write(DB_NAME_ATTRIBUTE);
				out.write("=\"");
				out.write(dbName);
				out.write('"');
			}
			witeTypeAttr(dba.getSQLType(), dbMO.getDefaultSQLType(), DB_TYPE_ATTRIBUTE, out);
			witeIntAttr(dba.getSQLSize(), dbMO.getDefaultSQLSize(), DB_SIZE_ATTRIBUTE, out);
			witeIntAttr(dba.getSQLPrecision(), dbMO.getDefaultSQLPrecision(), DB_PREC_ATTRIBUTE, out);
		}

		out.write("/>");
		nl(out);
	}
    
	private void writeReference(MOReference attribute, Writer out) throws IOException {
		out.write("\t<");
		out.write(REFERENCE_ELEMENT);
		out.write(' ');
		out.write(ATT_NAME_ATTRIBUTE);
		out.write("=\"");
		out.write(attribute.getName());
		out.write("\"");
		writeExternallyNamedAttr(out, HISTORIC_ATTRIBUTE, attribute.getHistoryType());
		writeExternallyNamedAttr(out, DELETION_POLICY_ATTRIBUTE, attribute.getDeletionPolicy());
		writeMandatoryBooleanAttr(out, BRANCH_GLOBAL_ATTRIBUTE, attribute.isBranchGlobal());
		writeMandatoryBooleanAttr(out, MONOMORPHIC_ATTRIBUTE, attribute.isMonomorphic());
		writeMandatoryBooleanAttr(out, CONTAINER_ATTRIBUTE, attribute.isContainer());
		out.write(" ");
		out.write(TARGET_TYPE_ATTRIBUTE);
		out.write("=\"");
		out.write(attribute.getMetaObject().getName());
		out.write('"');
		writeMandatoryBooleanAttr(out, MANDATORY_ATTRIBUTE, attribute.isMandatory());
		// default false
		witeBooleanAttr(attribute.isImmutable(), false, IMMUTABLE_ATTRIBUTE, out);
		// default false
		witeBooleanAttr(attribute.isInitial(), false, INITIAL_ATTRIBUTE, out);

		out.write("/>");
		nl(out);

	}

    /**
     * Write List of attributes  as XML.
     *
     * @param     attribs   List of MOAttributes.
     * @param     aWriter   The output is written here
     */
    protected void writeMOAttributes (List<? extends MOAttribute> attribs, Writer aWriter) throws IOException {
    	for (int i = 0, size = attribs.size(); i < size; i++) {
    		MOAttribute theAttribute = attribs.get (i);
  		    writeMOAttribute(theAttribute, aWriter);
    	}
    }

    /**
     * Write attributes of a MOStructure as XML.
     *
     * @param     aMOS      The meta data of object to be used.
     * @param     aWriter   The output is written here
     */
    protected void writeMOAttributes (MOStructure aMOS, Writer aWriter) throws IOException {
    	writeMOAttributes(aMOS.getAttributes(), aWriter);
    }
    

    /**
     * Write attributes of a MOClass as XML.
     *
     * @param     aMOC      The meta data of object to be used.
     * @param     aWriter   The output is written here
     */
    protected void writeMOAttributes (MOClass aMOC, Writer aWriter) throws IOException {
        // Do not write Attributes of superclass, too
    	writeMOAttributes(aMOC.getDeclaredAttributes (), aWriter);
    }

    /**
     * Write a single MOIndex as XML, DBAttributes will be honored.
     *
     * @param     anIndex   The attribute to be written.
     * @param     out   The output is written here.
     */
    protected void writeMOIndex (MOIndex anIndex, Writer out) throws IOException {
		out.write("\t<");
        out.write(MO_INDEX_ELEMENT);
        out.write(' ');
        out.write(NAME_ATTRIBUTE);
        out.write("=\"");
        out.write(anIndex.getName ());
        out.write('"');
                            // default true
        witeBooleanAttr(anIndex.isUnique(), true , UNIQUE_ATTRIBUTE, out); 

        if (anIndex instanceof DBIndex)  { // write extra attributes here
    
            DBIndex  dbi = (DBIndex)  anIndex;
            // aWriter.writeln();
			// aWriter.write(" ");
			out.write(' ');
            out.write(DB_NAME_ATTRIBUTE);
            out.write("=\"");
            out.write(dbi.getDBName());
            out.write('"');
                    // default false
            witeBooleanAttr(dbi.isInMemory(), false , IN_MEMORY_ATTRIBUTE, out); 
        }
        out.write(">");
        nl(out);

		Iterable<DBAttribute> cols = anIndex.getKeyAttributes();
		for (DBAttribute dbAttr : cols) {
			MOAttribute attr = dbAttr.getAttribute();
			out.write("\t\t<");
            out.write(INDEX_PART_ELEMENT);
            out.write(" ");
            out.write(NAME_ATTRIBUTE);
            out.write("=\"");
			out.write(attr.getName());
			out.write('"');
			if (attr instanceof MOReference) {
				writeExternallyNamedAttr(out, PART_ATTRIBUTE, ((MOReference) attr).getPart(dbAttr));
			}
			out.write("/>");
            nl(out);
        }
		out.write("\t</");
        out.write(MO_INDEX_ELEMENT);
        out.write(">");
		nl(out);
    }

    /**
     * Write List of indexes as XML.
     *
     * @param     indexes   List of MOIndex
     * @param     aWriter   The output is written here
     */
    protected void writeMOIndexes (List<? extends MOIndex> indexes, Writer aWriter) throws IOException {
        if (indexes == null) {
            return; // nothing to do here ...
        }

        int  size    = indexes.size ();

        for (int i=0; i < size; i++) {
            MOIndex theIndex = indexes.get (i);
			writeMOIndex(theIndex, aWriter);
        }
    }

    /**
     * Write Indexes for a given Structure of indexes as XML.
     *
     * @param     aMOS      The Structure to write the indexes for
     * @param     aWriter   The output is written here
     */
    protected void writeMOIndexes (MOStructure aMOS, Writer aWriter) throws IOException {
        writeMOIndexes(aMOS.getIndexes(), aWriter);
    }

    /** 
     * Helper function to create the correct Type String for a MetaObject.
     */
    protected String typeOf(MetaObject aMO) {
    	switch (aMO.getKind()) {
		case item:
    		return "MOClass";
		case struct:
    		return "MOStructure";
		case primitive:
    		return "MOPrimitive";
		case collection:
    		return "MOCollection";
			case alternative:
				return "MOAlternative";
		case tuple:
		case ANY:
		case INVALID:
		case NULL:
		case function:
			throw new AssertionError("Unsupported meta object kind: " + aMO.getKind());
		}
    	throw new UnreachableAssertion("No such meta object kind: " + aMO.getKind());
    }

    /** Write Attributes specific for a DBTableMetaObject */
    protected void writeDBTableHeader(DBTableMetaObject aDBMO, Writer out) throws IOException {
        String dbName = aDBMO.getDBName();
        if (dbName != null) {
            out.write (' ');
            out.write (DB_NAME_ATTRIBUTE);
            out.write ("=\"");
            out.write (dbName);
            out.write ('"');
        }
    }

    /** Hook into writeMetaObjectObject(), called <em>before</em> the Attributes are written. */
    protected void preWriteAttributes (MetaObject aMO, Writer aWriter) {
    }

    /** Hook into writeMetaObjectObject(), called <em>after</em> the Attributes are written. */
    protected void postWriteAttributes (MetaObject aMO, Writer aWriter) {
    }

    /**
     * Write string representation of the given meta data object.
     *
     * @param     aMO       The meta data of objects to be used.
     * @param     out   The output is written here.
     */
    protected void writeMetaObject (
        MetaObject aMO, Writer out) throws IOException {
        
    	out.write  ('<');
        out.write  (META_OBJECT_ELEMENT);        // <metaobject 
        out.write  (' ');
        out.write  (MO_TYPE_ATTRIBUTE);    // object_type="..." 
        out.write  ("=\"");
        out.write  (typeOf(aMO));
        out.write  ("\" ");
        out.write  (MO_NAME_ATTRIB);    // object_name="..."
        out.write  ("=\"");
    	out.write  (TagUtil.encodeXML(aMO.getName()));
        out.write  ('"');
        if (MetaObjectUtils.isAbstract(aMO)) {
            out.write  (' ');
            out.write  (ABSTRACT_ATTRIBUTE);    // abstract="..."
            out.write  ("=\"true\"");
        }
        MOClass aSuper = MetaObjectUtils.isClass(aMO) ? ((MOClass) aMO).getSuperclass() : null;
        if (aSuper != null) {
            out.write (' ');
            out.write (SUPERCLASS_ATTRIBUTE);
            out.write ("=\"");
            out.write (TagUtil.encodeXML(aSuper.getName()));
            out.write ('"');
        }
        if (aMO instanceof DBTableMetaObject) {
            writeDBTableHeader((DBTableMetaObject) aMO, out);
        }
        out.write('>');
        nl(out);

        preWriteAttributes(aMO, out);
        if (MetaObjectUtils.isClass(aMO))  {
            MOClass moc = (MOClass) aMO;
            writeMOAttributes(moc, out);
            writeMOIndexes   (moc, out);
        }
        else if (aMO instanceof MOStructure) {
    	    MOStructure mos = (MOStructure) aMO;
    	    writeMOAttributes(mos, out);
    	    writeMOIndexes   (mos, out);
		} else if (aMO instanceof MOAlternative) {
			writeSpecialisations((MOAlternative) aMO, out);
        }

    	postWriteAttributes(aMO, out);
    	out.write("</");
        out.write(META_OBJECT_ELEMENT);
    	out.write('>');
        nl(out);
        nl(out);
    }

	private void writeSpecialisations(MOAlternative alternative, Writer out) throws IOException {
		Set<? extends MetaObject> specialisations = alternative.getSpecialisations();
		out.write("<specialisations>");
		nl(out);
		for (MetaObject specialisation : specialisations) {
			out.write("<specialisation name=\"");
			out.write(specialisation.getName());
			out.write("\" />");
			nl(out);
		}
		out.write("</specialisations>");
		nl(out);
	}

	/**
	 * Write all meta objects of the given knowledgebase to the given writer.
	 * 
	 * @param aRepository
	 *        The knowledgebase containing the meta objects to be written.
	 * @param out
	 *        The writer to be used.
	 */
	public void writeMetaObjects(MORepository aRepository, Writer out) throws IOException {
		// Fetch all types.
		List<? extends MetaObject> types = new ArrayList<>(aRepository.getMetaObjects());
		
		// Order according to type hierarchy (super types first).
		Collections.sort(types, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				MetaObject type1 = (MetaObject) o1;
				MetaObject type2 = (MetaObject) o2;
				
				if (type1.equals(type2)) {
					return 0;
				}
				
				// Super classes first.
				if (type1.isSubtypeOf(type2)) {
					// type1 is a subclass of type2, type1 > type2
					return 1;
				}
				if (type2.isSubtypeOf(type1)) {
					// type2 is a subclass of type1, type1 < type2
					return -1;
				}
				
				// Stable alphabetical order.
				return type1.getName().compareTo(type2.getName());
			}
		});
		
		out.write("<!-- MetaObjects -->");
        nl(out);
        nl(out);
		for (int n = 0, cnt = types.size(); n < cnt; n++) {
			writeMetaObject(types.get(n), out);
		}
	}

    /**
     * Hook for writing additional information of the given data object to
     * the writer.
     *
     * @param    anObject    The object to be serialized.
     * @param    aWriter     The writer to be used for writing.
     */
    protected void postWriteStartDOTag (DataObject anObject, 
                                        Writer aWriter) {
    }

    /**
     * Convert the given object to a matching string representation.
     *
     * @param    anObject    The object to be converted.
     * @return   The string representation of the given object.
     */
    protected String getStringValue (Object anObject) {
        String theValue = null;

        if (anObject instanceof Date) {
            theValue = Long.toString (((Date) anObject).getTime ());
        }
        else if (anObject instanceof String) {
            theValue = getEncoded ((String) anObject);
        }
		else if (anObject instanceof TLID) {
			theValue = IdentifierUtil.toExternalForm((TLID) anObject);
		}
        else {
            theValue = anObject.toString ();
        }

        return (theValue);
    }

    /**
     * Hook for changing the tag name "dataobject".
     *
     * @return    The name for the tag.
     */
    protected String getDataObjectTag () {
        return ("dataobject");
    }

    /**
     * Hook for changing the tag name "attribute".
     *
     * @return    The name for the tag.
     */
    protected String getAttributeTag () {
        return ("attribute");
    }

    /**
     * Hook for changing attribute name "type".
     *
     * @return    The name for the tag.
     */
    protected String getTypeTag () {
        return ("type");
    }

    /**
     * Hook for changing attribute name "id".
     *
     * @return    The name for the tag.
     */
    protected String getIDTag () {
        return ("id");
    }

    /**
     * Hook for changing attribute name "name".
     *
     * @return    The name for the tag.
     */
    protected String getNameTag () {
        return ("name");
    }

    /**
     * Hook for changing attribute name "value".
     *
     * @return    The name for the tag.
     */
    protected String getValueTag () {
        return ("value");
    }

    /**
     * Write the start tag for the data object.
     *
     * After writing the tag name and the type of the data object, this method
     * calls a hook to append additional information (such as name). This hook
     * is called {@link #postWriteStartDOTag(com.top_logic.dob.DataObject,java.io.Writer)}.
     *
     * @param    anIndent    The indent to be used when writing.
     * @param    out     The writer to be used for writing.
     * @param    anObject    The object to be serialized.
     *
     * @see #postWriteStartDOTag(com.top_logic.dob.DataObject,java.io.Writer)
     */
    protected void writeStartDOTag (int         anIndent,
                                    DataObject  anObject, 
                                    Writer out) throws IOException {
        out.write (SPACES,0,anIndent);
        out.write ('<');
        out.write (this.getDataObjectTag ());
        out.write (' ');
        out.write (this.getTypeTag ());
        out.write ("=\"");
        out.write (TagUtil.encodeXML(anObject.tTable().getName()));
        out.write ("\" ");
        out.write (this.getIDTag ());
        out.write ("=\"");
		out.write(IdentifierUtil.toExternalForm(anObject.getIdentifier()));
        out.write ('\"');
        this.postWriteStartDOTag (anObject, out);
        out.write ('>');
        nl(out);
    }

    /**
     * Write the end tag for the data object.
     *
     * @param    anIndent    The indent to be used when writing.
     * @param    out     The writer to be used for writing.
     */
    protected void writeEndDOTag (int anIndent, Writer out) throws IOException {
        out.write (SPACES, 0, anIndent);
        out.write ("</");
        out.write (this.getDataObjectTag ());
        out.write ('>');
        nl(out);
    }

    /**
     * Write all existing attributes of the given data object to the writer.
     *
     * This method uses the given indent to format the outout. If there is
     * an attribute, which itself is an data object, this method will make
     * a recursive call to write this one to the writer. If it is a simple
     * attribute, we use the simple output.
     *
     * @param    anIndent    The indent to be used when writing.
     * @param    anObject    The object to be serialized.
     * @param    aWriter     The writer to be used for writing.
     */
    protected void writeAttributes (int         anIndent,
                                    DataObject  anObject, 
                                    Writer aWriter) {
        String [] theNames = anObject.getAttributeNames ();

        for (int thePos = 0; thePos < theNames.length; thePos++) {
            try {
                String theName  = theNames [thePos];
                Object theValue = anObject.getAttributeValue (theName);

                if (theValue instanceof DataObject) {
                	this.writeDataObject(anIndent, aWriter, (DataObject) theValue, theName);
                }
                else if (theValue != null) {
                    this.writeSimpleAttributeTag (anIndent, 
                                                  theName, 
                                                  theValue.getClass ().getName (),
                                                  this.getStringValue (theValue), 
                                                  aWriter);
                }
            }
            catch (Exception ex) {
                Logger.warn ("Unable to write attribute " + theNames [thePos], 
                             ex, this);
            }
        }
    }

    /**
     * Write the attribute tag to the writer.
     *
     * Depending on the parameter "aValue", the tag is closed (aValue != null)
     * or open (aValue == null). The rest of the information will be written
     * as they are provided. The creation of the string representation of the
     * value has been done in the method {@link #getStringValue(java.lang.Object)}.
     * Feel free to overwrite that method to provide your special string 
     * display.
     *
     * @param    anIndent    The indent to be used for writing.
     * @param    aName       The name of the attribute.
     * @param    aType       The type of the value to be written.
     * @param    aValue      The string representation of the value.
     * @param    out     The writer to be used for writing the information.
     *
     * @see #getStringValue(java.lang.Object)
     */
    protected void writeSimpleAttributeTag (int    anIndent,
                                            String aName, 
                                            String aType, 
                                            String aValue, 
                                            Writer out) throws IOException {
        out.write (SPACES, 0, anIndent);
        out.write ('<');
        out.write (this.getAttributeTag ());
        out.write (' ');
        out.write (this.getNameTag ());
        out.write ("=\"");
        out.write (aName);
        out.write ("\" ");
        out.write (this.getTypeTag ());
        out.write ("=\"");
        out.write (TagUtil.encodeXML(aType));

        if (aValue != null) {
            out.write ("\" ");
            out.write (this.getValueTag ());
            out.write ("=\"");
            out.write (aValue);
            out.write ("\" />");
            nl(out);
        }
        else {
            out.write ("\">");
            nl(out);
        }
    }

    /**
     * Write the end tag for an attribute.
     *
     * @param    anIndent    The indent to be used for writing.
     * @param    aWriter     The writer to be used for writing.
     */
    protected void writeEndAttrTag (int anIndent, Writer aWriter) throws IOException {
        aWriter.write (SPACES, 0, anIndent);
        aWriter.write ("</");
        aWriter.write (this.getAttributeTag ());
        aWriter.write ('>');
        nl(aWriter);
    }

    protected static void writeEncoded (String aString, Writer aWriter) throws IOException {
        aWriter.write (getEncoded (aString));
    }

    /**
     * Replace all non-valid characters by unicodes.
     *
     * @param    aString    The string to be encoded.
     */
    protected static String getEncoded (String aString) {
        char         theChar;
        int          theLength = aString.length ();
        StringBuffer theBuffer = new StringBuffer (theLength);

        for (int thePos = 0; thePos < theLength; thePos++) {
            theChar = aString.charAt (thePos);

            if (DONT_ENCODE.indexOf (theChar) > -1) {
                theBuffer.append (theChar);
            }
            else {
                theBuffer.append (getUnicodeStringFor (theChar));
            }
        }

        return (theBuffer.toString ());
    }

    /**
     * Returns a unicode string for the given character.
     *
     * Because the toString() method of Integer is slow, we encode some known
     * values with constants.
     *
     * @param    aChar    The character to be encoded.
     * @return   The unicode for the character (HTML conform).
     */
    protected static String getUnicodeStringFor (char aChar) {
        String theEncoding;

        switch (aChar) {
            case '&'  : theEncoding = TagUtil.AMP_CHAR;
                        break;
            case '\"' : theEncoding = TagUtil.QUOT_CHAR;
                        break;
            case '\'' : theEncoding = TagUtil.APOS_CHAR;
                        break;
            case '<'  : theEncoding = TagUtil.LT_CHAR;
                        break;
            case '>'  : theEncoding = TagUtil.GT_CHAR;
                        break;
            default   : theEncoding = "&#" + Integer.toString (aChar) + ';';
                        break;
        }

        return (theEncoding);
    }
    
    /** Platform dependent new line string. */
	private static final String NL;
	
	static {
		StringWriter buffer = new StringWriter();
		new PrintWriter(buffer).println();
		NL = buffer.toString();
	}
	
    protected static void nl(Writer writer) throws IOException {
    	writer.write(NL);
	}


}    
