/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.StructuredDataImportPerformer;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.importer.excel.ImportPerformerPostProcessor;
import com.top_logic.importer.excel.ImportPerformerPostProcessor.SimpleImportPerformerPostProcessor;
import com.top_logic.importer.handler.structured.AbstractDOImportHandler;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.text.TextImportParser;
import com.top_logic.importer.text.TextImportParser.Text;
import com.top_logic.importer.xml.XML2DOParser.TagConfig;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Upload handler for the import.
 *
 * @author    <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@SuppressWarnings("serial")
public class XMLFileImportParser<C extends XMLFileImportParser.Config> extends AbstractImportParser {

    /**
     * Configuration for parsing the XML file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractImportParser.Config {

        /** Flag if character events have to be inspected too. */
        @BooleanDefault(true)
        boolean getNeedsCharacterEvents();

        /** Return the name space we live in. */
        String getNamespace();

        /** Tags to be imported. */
        @Mandatory
        @Key(TagConfig.NAME_ATTRIBUTE)
        Map<String, TagConfig> getTags();

        /** Default string parser for values read from XML attributes. */
        @InstanceFormat
        @InstanceDefault(Text.class)
        TextImportParser<?> getDefaultTextParser();

        /** Mapping for text parsers for XML attributes, which have the same behavior in the whole XML structure. */
        @InstanceFormat
        @Key(TextImportParser.Config.NAME_ATTRIBUTE)
        Map<String, TextImportParser<?>> getDefaultMappings();

        /** Name of the structure we will create elements for (needed when called without an {@link AssistentComponent}). */
        String getStructure();
    }

    /**
     * Configuration for all DOImportHandler.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MappingConfig extends NamedConfiguration, PolymorphicConfiguration<AbstractDOImportHandler<MappingConfig, Object>> {

		/** Type this handler operates on. */
        @Mandatory
        String getType();

        @InstanceFormat
        @InstanceDefault(SimpleImportPerformerPostProcessor.class)
        ImportPerformerPostProcessor getPostProcessor();
    }

    /** Configuration provided by this import upload handler. */
    protected final C config;

    private XML2DOParser<C> parser;

    /** 
     * Creates a {@link XMLFileImportParser}.
     */
    public XMLFileImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);

        this.config = aConfig;
        this.parser = new XML2DOParser<>(aContext, aConfig);
    }

    @Override
    public C getConfig() {
        return this.config;
    }

    @Override
    protected List<Map<String, Object>> readData(AssistentComponent aComponent, Object aSource, ImportLogger aLogger) {
        List<Map<String,Object>> theList   = new ArrayList<>();
        InputStream              theStream = null;

        try {
            theStream = this.createStream(aSource);

            if (this.parser.parse(theStream)) {
            	GenericDataObjectWithChildren theDO = this.parser.getDataObject();

            	if (theDO != null) {
            		Map<String,Object> theValues = new HashMap<>();
            		int                theCount  = this.parser.getCreatedCount();

            		theValues.put(StructuredDataImportPerformer.VALUE, theDO);
            		theValues.put(StructuredDataImportPerformer.MODEL, this.getModel(aComponent, this.getConfig().getStructure()));
            		theList.add(theValues);
                    aComponent.setData(StructuredDataImportPerformer.COUNTER, theCount);

                    aLogger.info(XMLFileImportParser.class, I18NConstants.DO_READ, new Object[] {theCount});
            	}
            }
        }
        catch (Exception ex) {
            aLogger.error(XMLFileImportParser.class, I18NConstants.EXCEPTION_PARAM, ex);
        }
        finally {
            FileUtilities.close(theStream);
        }

        return theList;
    }
}
