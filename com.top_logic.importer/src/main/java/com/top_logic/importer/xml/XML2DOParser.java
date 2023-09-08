/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.dsa.DataAccessException;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.importer.text.TextFileImportParser;
import com.top_logic.importer.text.TextImportParser;
import com.top_logic.importer.text.TextImportParser.Text;
import com.top_logic.knowledge.wrap.AbstractWrapper;

/**
 * This parser can be configured in the {@link Configuration}. All needed parts can be done there. 
 * A configuration section can look like this:
 * 
 * <pre>
 *      &lt;section name="XML2DOParser" comment="Configuration of XML2DOParser"&gt;
 *            &lt;entry name="someObject.tag"    value="xmlns:tagname" /&gt;             &lt;!-- Name of the XML tag to be parsed as a data object  --&gt;
 *            &lt;entry name="someObject.id"     value="some-xml-id" /&gt;               &lt;!-- XML object attribute to be used as unique ID of DO --&gt;
 *            &lt;entry name="someObject.doType" value="someType" /&gt;                  &lt;!-- Type of the DataObject that will be created  --&gt;
 *      &lt;/section&gt;
 * </pre>
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class XML2DOParser<C extends XMLFileImportParser.Config> extends DefaultHandler {

    /**
     * Configuration of one XML tag.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface TagConfig extends NamedConfiguration {

        @Mandatory
        String getType();

        /** Unique id of the represented object. */
		@Name(TextFileImportParser.Config.UID)
		String getUID();

        /** Name of the attribute to import the values to. */
        String getAttribute();

        /** Mapping for special text parsers for XML attributes in this tag. */
        @InstanceFormat
        @Key(TextImportParser.Config.NAME_ATTRIBUTE)
        Map<String, TextImportParser<?>> getMappings();

        /** Optional filter for skipping tags in the XML file. */
        @InstanceFormat
        @InstanceDefault(TrueFilter.class)
        TypedFilter getFilter();

        /** Return the name space we live in. */
        String getNamespace();
    }

	/** Send character Events to current, too */
	protected final boolean needCharEvent;

	/** The current node during parsing. */
	private Node<TagConfig> currentNode;

	/** The root node. Its only child contains the result of the parsing. */
	private Node<TagConfig> rootNode;

	private int childCount;

    private Map<QName, TagConfig> tagConfigs;

    private Map<TagConfig, Map<String, TextImportParser<?>>> textImportParsers;

	private Set<String> unknownTags;

	private TextImportParser<?> defaultTextParser;

    /**
     * Creates an importer from the configuration.
     * 
     * If the given configuration name is empty or <code>null</code>, the default 
     * configuration section XML2DOParser will be used.
     */
	public XML2DOParser(@SuppressWarnings("unused") InstantiationContext aContext, C aConfig) {
		this.needCharEvent     = aConfig.getNeedsCharacterEvents();
		this.tagConfigs        = this.createTagConfig(aConfig);
		this.defaultTextParser = aConfig.getDefaultTextParser();
		this.textImportParsers = this.createMapping(aConfig.getDefaultMappings(), this.tagConfigs);

		this.initTagConfigs();
    }

    private Map<QName, TagConfig> createTagConfig(C aConfig) {
        Map<QName, TagConfig> theMap       = new HashMap<>();
        String                theDefaultNS = aConfig.getNamespace();

        for (TagConfig theTag : aConfig.getTags().values()) {
            String theNamespace = theTag.getNamespace();

            if (StringServices.isEmpty(theNamespace)) {
                theNamespace = theDefaultNS;
            }

            theMap.put(new QName(theNamespace, theTag.getName()), theTag);
        }
        return theMap;
    }

    private void initTagConfigs() {
        for (TagConfig theTag : this.tagConfigs.values()) {
            TypedFilter theFilter = theTag.getFilter();

            if (theFilter instanceof AbstractConfigurableFilter) {
                XMLImportFilter.initFilter(theFilter, this.textImportParsers.get(theTag));
            }
        }
    }

    private Map<TagConfig, Map<String, TextImportParser<?>>> createMapping(Map<String, TextImportParser<?>> map, Map<QName, TagConfig> someConfigs) {
        Map<TagConfig, Map<String, TextImportParser<?>>> theMap = new HashMap<>();

        for (TagConfig theTag : someConfigs.values()) {
            Map<String, TextImportParser<?>> theInner = this.cloneMap(map);

            theMap.put(theTag, theInner);

            for (Entry<String, TextImportParser<?>> theMapping : theTag.getMappings().entrySet()) {
                theInner.put(theMapping.getKey(), theMapping.getValue());
            }
        }

        return theMap;
    }

    private Map<String, TextImportParser<?>> cloneMap(Map<String, TextImportParser<?>> aMap) {
        Map<String, TextImportParser<?>> theMap = new HashMap<>();

        for (Entry<String, TextImportParser<?>> theEntry : aMap.entrySet()) {
            theMap.put(theEntry.getKey(), theEntry.getValue());
        }

        return theMap;
    }

	/**
	 * the result of the parsing process. <code>null</code> if parsing has not taken place yet or it did not have a result.
	 */
	public GenericDataObjectWithChildren getDataObject() {
		if (this.rootNode != null && !this.rootNode.children.isEmpty()) {
			return rootNode.children.get(0).dataObject;
		}

		return null;
	}

	/**
	 * Invoke the import of DataObject from the given file.
     * 
	 * @param aFile the file that should be converted into a DataObject.
	 * @return true if the import has been completed
	 */
	public boolean parse(File aFile) throws DataAccessException {
		try {
			return this.parse(new FileInputStream(aFile));
		}
        catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Invoke the import of DataObject from the given file.
     * 
	 * @param   aStream    The input stream that should be converted into a data object.
	 * @return  <code>true</code> if the import has been completed.
     * @throws  DataAccessException    If parsing fails for a reason.
	 */
	public boolean parse(InputStream aStream) throws DataAccessException {
		try {
			this.rootNode    = new Node<>();
			this.currentNode = this.rootNode;
			this.childCount  = 0;
			this.unknownTags = new HashSet<>();

			SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();

            theParser.parse(aStream, this);

            return (true);
        } 
        catch (SAXException sax) {
			throw new DataAccessException(sax);
		} 
        catch (ParserConfigurationException pcx) {
            throw new DataAccessException(pcx);
		} 
        catch (IOException iox) {
            throw new DataAccessException(iox);
		}
        finally {
        	try {
				aStream.close();
			} catch (IOException e) {
				Logger.warn("Failed to cloase stream - ignored", XML2DOParser.class);
			}
        }
	}
	
	public int getCreatedCount() {
	    return this.childCount;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Create a new child node
		QName           theName   = new QName(uri, localName);
        TagConfig       theConfig = this.tagConfigs.get(theName);
		Node<TagConfig> theNode;

		if (theConfig != null) {
            if (theConfig.getFilter().matches(attributes) == FilterResult.TRUE) { 
                theNode = this.currentNode.createChild(theConfig);
            }
            else {
                theNode = this.currentNode.createEmptyChild(qName);
            }

            theNode.qName = theName;
        }
        else {
            theNode = this.currentNode.createEmptyChild(qName);
        }

		this.childCount++;

		// Set the new child node as the current node
		this.currentNode = theNode;

		if (theNode.config != null) {
			String theID = theNode.config.getUID();
		    String theAttr = theNode.config.getAttribute();

		    // Fill id and attributes of the new node
    		for (int i = 0; i < attributes.getLength(); i++) {
    			String attName  = attributes.getLocalName(i);
    			String attValue = attributes.getValue(i);
    
    			if (attName.equals(theID) || attName.equals(theAttr)) {
                    if (attName.equals(theID)) {
        				theNode.id = attValue;
        			}

                    if (attName.equals(theAttr)) {
        			    theNode.title = attValue;
        			}
    			}
    			else {
    				this.setValue(theNode, attName, attValue);
    			}
    		}
		}
	}

    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.currentNode.config != null) {
            // Create the DataObject from the node's data
            this.currentNode.dataObject = new GenericDataObjectWithChildren(
                    this.currentNode.attributes, 
                    this.currentNode.config.getType(), 
                    StringID.valueOf(this.currentNode.id), 
                    this.currentNode.getChildrenDOs());
    	
        	if (this.currentNode.title != null) {
        	    this.currentNode.dataObject.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, currentNode.title);
        	}
        }
        else if (this.tagConfigs.containsKey(this.currentNode.qName)) {
            // Filter removed this element;
        }
        else if (!this.unknownTags.contains(this.currentNode.id)) {
            this.unknownTags.add(this.currentNode.id);

            Logger.warn("Unknown tag '" + this.currentNode.id + "', will not be transformed!", XML2DOParser.class);
        }

        // Jump to the parent node
        this.currentNode = this.currentNode.parent;
    }

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// Fill the _cdata field if needed
		if (needCharEvent && start == 0) {
			currentNode.attributes.put("_cdata", String.valueOf(ch, start, length));
		}
	}

	/** 
     * Set the nodes attributes map the given value.
     * 
     * <p>The converting of the given value will be done by the matching
     * {@link TextImportParser} (where the default is the {@link Text}.</p>
     * 
     * @param aNode     The node to get the configuration from and write the value to.
     * @param aName     Name of the requested attribute.
     * @param aValue    String representation of the value.
     * @see   #textImportParsers
     * @see   #defaultTextParser
     */
    protected void setValue(Node<TagConfig> aNode, String aName, String aValue) {
        TextImportParser<?> theMapping = this.textImportParsers.get(aNode.config).get(aName);

        if (theMapping == null) {
            theMapping = this.defaultTextParser;
        }

        aNode.attributes.put(aName, theMapping.map(aValue));
    }

	/**
	 * Holds the node information.
	 * 
	 * <p>The configuration is set upon creation, the rest of the fields is filled during parsing.</p>
	 *
	 * @author    <a href="mailto:kbu@top-logic.com">kbu</a>
	 */
	public static class Node<C extends Object> {

        /** The DataObject generated as the result of the parsing. */
		public GenericDataObjectWithChildren dataObject;
		
		public QName qName;

		/** The id for the DataObject. */
		public String id;

		/** The name for the DataObject. */
		public String title;

		/** The attributes for the DataObject. */
		public Map<String, Object> attributes = new HashMap<>();

		/** The child nodes. */
		public List<Node<C>> children = new ArrayList<>();

		/** The configuration for this node. */
		public C config;

		/** The parent of this node. */
		public Node<C> parent;

		@Override
		public String toString() {
		    return new NameBuilder(this)
		            .add("id",    this.id)
		            .add("title", this.title)
		            .add("qName", this.qName)
		            .build();
		}

		/**
		 * Create a new child node.
		 * 
		 * @param    aConfig    The configuration for the child, must not be <code>null</code>.
		 * @return   The created child.
		 */
		public Node<C> createChild(C aConfig) {
		    if (aConfig == null) {
		        throw new IllegalArgumentException("Child config must be given!");
		    }
		    else {
		        Node<C> theNode = new Node<>();

    		    theNode.config = aConfig;
    		    theNode.parent = this;

    		    this.children.add(theNode);
		    
    		    return theNode;
		    }
		}

        /**
         * Create a new child node for an unknown tag.
         * 
         * @param    aQName    Name of the unknown tag.
         * @return   The created child.
         */
		public Node<C> createEmptyChild(String aQName) {
		    Node<C> theNode = new Node<>();

            theNode.id     = aQName;
            theNode.parent = this;

            children.add(theNode);
            
            return theNode;
		}
		
		/**
		 * Collect the DataObjects from this node's children.
		 * Note: this will work only after parsing of the children is completed!
		 * 
		 * @return the children DataObjects
		 */
		public List<GenericDataObjectWithChildren> getChildrenDOs() {
			List<GenericDataObjectWithChildren> theChildren = new ArrayList<>();
			for (Node<C> theChild : children) {
				theChildren.add(theChild.dataObject);
			}
			
			return theChildren;
		}
	}

}
