/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.DataObject;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.importer.base.StructuredDataImportPerformer;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.importer.excel.extractor.AbstractExcelStructureExtractor.NodeInfo;
import com.top_logic.importer.excel.extractor.ExcelStructureExtractor;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.xml.XML2DOParser.Node;
import com.top_logic.importer.xml.XMLFileImportParser;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Parse excel files in XLSX, XLS or CSV format and provide a structure of read data ({@link DataObject}s).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelStructureImportParser<C extends ExcelStructureImportParser.Config> extends AbstractExcelFileImportParser<C> {

    /**
     * Configuration for parsing the excel file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractExcelFileImportParser.Config {

        /** Name of the structure we are working in (needed for identifying the correct {@link StructuredElementFactory}). */
        @Mandatory
        String getStructure();

		/**
		 * When importing more than one child we need our model to be the root node (must be same
		 * name as
		 * {@link com.top_logic.importer.base.StructuredDataImportPerformer.Config#getRootName()}).
		 */
        String getRootName();
    }

    /** 
     * Creates a {@link ExcelStructureImportParser}.
     */
    public ExcelStructureImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    protected void preRead(AssistentComponent aComponent) {
        aComponent.setData(StructuredDataImportPerformer.COUNTER, null);
    }

    @Override
    protected List<Map<String, Object>> doRead(ExcelContext aContext, List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) throws InvalidFormatException {
        Node<C> theNode = this.parse(aModel, aContext, aLogger);

        if (theNode != null) {
            GenericDataObjectWithChildren theDO     = this.toDataObject(theNode);
            Map<String,Object>            theValues = new HashMap<>();

            theValues.put(StructuredDataImportPerformer.VALUE, theDO);
            theValues.put(StructuredDataImportPerformer.MODEL, aModel);
            aResult.add(theValues);

            aLogger.info(XMLFileImportParser.class, I18NConstants.DO_READ, new Object[] {aContext.row()});
        }

        return aResult;
    }

    @Override
    protected void postRead(AssistentComponent aComponent, ExcelContextProvider<?> aProvider) {
        aComponent.setData(StructuredDataImportPerformer.COUNTER, aProvider.getContext().row());
    }

    @Override
    protected Object getModel(AssistentComponent aComponent) {
        return this.getModel(aComponent, this.getConfig().getStructure());
    }

    /** 
     * Create a {@link Node child} from the given values and push it to the stack.
     * 
     * @param aStack        The stack of {@link Node}s created.
     * @param aParent       Parent node.
     * @param someValues    Values describing the new node.
     * @param aLogger       Logger for messages.
     */
    protected Node<C> createChild(Stack<Node<C>> aStack, Node<C> aParent, Map<String, Object> someValues, ImportLogger aLogger) {
		Node<C> theNode = this.createChild(aParent, someValues);

        if (theNode != null) { 
            aStack.push(theNode);
        }

        return theNode;
    }

    /** 
     * Create a map containing the {@link NodeInfo} representation of the given wrapper based root node.
     * 
     * @param aModel
     *        The (wrapper based) model to be added as root node.
     * @param aType
     *        Type to be used in performer to identify that additional root node.
     * @return
     *        The requested map of values.
     */
    protected Map<String, Object> getRootValues(Object aModel, String aType) {
        String theName = ((Wrapper) aModel).getName();

        return new MapBuilder<String, Object>()
                .put(ExcelStructureExtractor.NODE_INFO, new NodeInfo(0, theName, theName, aType))
                .toMap();
    }

    private GenericDataObjectWithChildren toDataObject(Node<C> aNode) {
        if (aNode != null && !aNode.children.isEmpty()) {
            return aNode.children.get(0).dataObject;
        }
        else {
            return null;
        }
    }

    private Node<C> parse(Object aModel, ExcelContext aContext, ImportLogger aLogger) {
        Stack<Node<C>> theStack = new Stack<>();
        Node<C>        theRoot  = new Node<>();
        String         theName  = this.getConfig().getRootName();

        theStack.push(theRoot);

        if (!StringServices.isEmpty(theName)) {
            this.createChild(theStack, theRoot, this.getRootValues(aModel, theName), aLogger);
        }

        this.parse(aContext, theStack, aLogger);

        return theRoot;
    }

    private void parse(ExcelContext aContext, Stack<Node<C>> aStack, ImportLogger aLogger) {
        while (aContext.hasMoreRows()) {
            int                 theRow    = aContext.row() + 1;
            Map<String, Object> theValues = this.parseRow(aContext, aLogger);

            if (ExcelStructureExtractor.hasNodeInfo(theValues, theRow, aLogger)) {
                Node<C> theParent = this.getParent(aStack, theValues);

                if (theParent != null) {
                    this.createChild(aStack, theParent, theValues, aLogger);
                }
                else {
                    aLogger.error(ExcelStructureImportParser.class, I18NConstants.NO_PARENT_FOUND, theRow, aStack.peek());
                }
            }

            aContext.down();
        }
    }

	private Node<C> createChild(Node<C> aParent, Map<String, Object> someValues) {
        Node<C>   theChild  = aParent.createChild(this.getConfig());
        NodeInfo  theNodeID = null;

        for (Entry<String, Object> theEntry : someValues.entrySet()) {
            Object theValue = theEntry.getValue();

            if (theValue instanceof NodeInfo) {
                theNodeID = (NodeInfo) theValue;

                theChild.id    = theNodeID.id;
                theChild.title = theNodeID.title;
            }
            else {
                theChild.attributes.put(theEntry.getKey(), theValue);
            }
        }

        if (theNodeID != null) {
            theChild.dataObject = new GenericDataObjectWithChildren(
                    theChild.attributes, 
                    theNodeID.type, 
                    StringID.valueOf(theChild.id),
				new ArrayList<>());

            if (theChild.title != null) {
                theChild.dataObject.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, theNodeID.title);
            }

            if (aParent.dataObject != null) { 
                aParent.dataObject.getChildren().add(theChild.dataObject);
            }
        }

        return theChild;
    }

    /**
     * Parse the row contents at the given context's position.
     * 
     * @param aContext
     *        The {@link ExcelContext} to be used for excel data access
     * @param aLogger
     *        Messages for information to the user.
     */
    private Map<String, Object> parseRow(ExcelContext aContext, ImportLogger aLogger) {
        if (!this.isEmptyRow(aContext, aLogger)) {
            Map<String, Object> theMap = this.extractValues(aContext, aLogger);

            this.constraintsAware(aContext, aLogger);

            if ((theMap != null) && !theMap.isEmpty()) {
                int    theRow  = aContext.row();
                Object theName = ExcelStructureExtractor.getNodeInfo(theMap).title;

                aLogger.info(this, I18NConstants.ROWS_PROCESSED, theRow + 1, theName);

                return theMap;
            }
        }

        return null;
    }

    private Node<C> getParent(Stack<Node<C>> aStack, Map<String, Object> someValues) {
        NodeInfo  theNodeInfo = ExcelStructureExtractor.getNodeInfo(someValues);
        Node<C>   theNode     = aStack.peek();
        int       theLevel    = this.getLevel(theNode);

        if ((theLevel + 1) == theNodeInfo.level) {
            return theNode;
        }
        else if (theNodeInfo.level <= theLevel) {
            while (theLevel >= theNodeInfo.level) {
                aStack.pop();
                theNode = aStack.peek();

                theLevel--;
            }

            return theNode;
        }
        else {
            return null;
        }
    }

    private int getLevel(Node<C> aNode) {
        int theLevel = -1;

        while (aNode != null) {
            aNode = aNode.parent;
            theLevel++;
        }

        return theLevel;
    }
}
