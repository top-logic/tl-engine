/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dob.DataObject;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.ExcelStructureImportParser;
import com.top_logic.importer.excel.transformer.I18NConstants;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.text.TextFileImportParser;

/**
 * Create the unique {@link NodeInfo} for building up a structure in {@link ExcelStructureImportParser}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractExcelStructureExtractor<C extends AbstractExcelStructureExtractor.ColumnConfig>
		implements Extractor {

    public interface Config<C extends AbstractExcelStructureExtractor.ColumnConfig> extends Extractor.Config {

        @Mandatory
		@Name(TextFileImportParser.Config.UID)
        String getUID();

    }

    public interface ColumnConfig extends NamedConfiguration {
        
        public static final String LEVEL_ATTRIBUTE = "level";

        @Mandatory
        String getType();

		@Name(TextFileImportParser.Config.UID)
		String getUID();

        @Name(LEVEL_ATTRIBUTE)
        @Mandatory
        Integer getLevel();

        @Mandatory
        String getTitle();
    }

    public static final String NODE_INFO = "_nodeInfo";

    private final String id;

    /** 
     * Creates a {@link AbstractExcelStructureExtractor}.
     */
    public AbstractExcelStructureExtractor(InstantiationContext aContext, Config<C> aConfig) {
        this.id      = aConfig.getUID();
    }

    @Override
    public Map<String, Object> extract(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
        return new MapBuilder<String, Object>()
                .put(NODE_INFO, this.getNodeInfo(aContext, aParser, aLogger))
                .toMap();
    }

	public abstract Map<Object, C> getColumns();

    /** 
     * Find the correct column configuration for creating a {@link NodeInfo}.
     * 
     * <p>The returned column configuration will be handed over to 
     * {@link #getNodeID(ColumnConfig, ExcelContext, ImportLogger)} and
     * {@link #getNodeName(ColumnConfig, ExcelContext, ImportLogger)} when creating
     * a new {@link NodeInfo}. The creating method itself is private to avoid miss 
     * configuration.</p>
     * <p>When this method returns <code>null</code>, the row will be ignored during
     * import.</p>
     * 
     * @param    aContext    The excel context in the current row to get the {@link NodeInfo} for.
     * @return   The requested node information, may be <code>null</code>.
     */
    protected C getColumnConfig(ExcelContext aContext) {
        for (Entry<Object, C> theEntry : this.getColumns().entrySet()) {
            String theName = aContext.getString(theEntry.getKey().toString());

            if (!StringServices.isEmpty(theName)) {
                return theEntry.getValue();
            }
        }

        return null;
    }

    /** 
     * Return the unique ID for identifying the {@link DataObject} and later on the
     * {@link StructuredElement} the {@link DataObject} belongs to.
     * 
     * @param    aColumn     The configuration for the {@link NodeInfo}.
     * @param    aContext    The excel context in the current row to get the ID for.
     * @param    aLogger     When reading values fails for a reason.
     * @return   The requested unique ID.
     * @throws   TransformException   When unique ID is <code>null</code> or empty.
     */
    protected String getNodeID(ColumnConfig aColumn, ExcelContext aContext, ImportLogger aLogger) {
		String theKey = aColumn.getUID();
        String theID  = aContext.getString(StringServices.isEmpty(theKey) ? this.id : theKey);

        if (!StringServices.isEmpty(theID)) {
            return theID;
        }
        else { 
            throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, theKey, theID);
        }
    }

    /** 
     * Return the name of the {@link DataObject}.
     * 
     * @param    aColumn     The configuration for the {@link NodeInfo}.
     * @param    aContext    The excel context in the current row to get the name for.
     * @param    aLogger     When reading values fails for a reason.
     * @return   The requested name.
     */
    protected String getNodeName(ColumnConfig aColumn, ExcelContext aContext, ImportLogger aLogger) {
        return aContext.getString(aColumn.getTitle());
    }

    /** 
     * Creation of a node info depends on {@link #getColumnConfig(ExcelContext)}.
     * 
     * @see       #getColumnConfig(ExcelContext)
     */
    private NodeInfo getNodeInfo(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
        ColumnConfig theColumn = this.getColumnConfig(aContext);

        if (theColumn != null) {
            int    theLevel = theColumn.getLevel();
            String theID    = this.getNodeID(theColumn, aContext, aLogger);
            String theTitle = this.getNodeName(theColumn, aContext, aLogger);
            String theType  = theColumn.getType();
            
            return new NodeInfo(theLevel, theID, theTitle, theType);
        }
        else {
            return null;
        }
    }

    public static NodeInfo getNodeInfo(Map<String, Object> someValues) {
        return (NodeInfo) someValues.get(AbstractExcelStructureExtractor.NODE_INFO);
    }

    public static boolean hasNodeInfo(Map<String, Object> someValues, int aRow, ImportLogger aLogger) {
        if (someValues != null) { 
            if (someValues.get(AbstractExcelStructureExtractor.NODE_INFO) instanceof NodeInfo) {
                return true;
            }
            else { 
                aLogger.warn(ExcelStructureImportParser.class, I18NConstants.NO_NODE_LEVEL, aRow);
            }
        }

        return false;
    }

    /**
     * ID describing the node and all relevant things needed to create a {@link GenericDataObjectWithChildren} later on. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class NodeInfo {

        // Attributes

        public final int level;
        public final String id;
        public final String title;
        public final String type;

        // Constructors
        
        /** 
         * Creates a {@link NodeInfo}.
         */
        public NodeInfo(int aLevel, String anID, String aTitle, String aType) {
            this.level = aLevel;
            this.title = aTitle;
            this.id    = anID;
            this.type  = aType;
        }

        // Overridden methods from Object

        @Override
        public String toString() {
            return new NameBuilder(this)
                    .add("level", this.level)
                    .add("id", this.id)
                    .add("type", this.type)
                    .add("title", this.title)
                    .build();
        }
    }
}

