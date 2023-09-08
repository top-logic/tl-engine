/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;

/**
 * Base class for parsing an external file and generate an in memory model of it. 
 * 
 * <p>This class will normally be used by an {@link AbstractImportPerformer}.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractImportParser implements ConfiguredInstance<AbstractImportParser.Config> {

    /**
     * Base configuration of import parsers. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public interface Config extends PolymorphicConfiguration<AbstractImportParser> {
//
//        @Container
//        ImporterConfig getImporter();

        /** List of extensions supported by this parser. */
        @Mandatory
        @Format(CommaSeparatedStrings.class)
        List<String> getExtensions();
    }

    private String name;

	private Config config;

    /** 
     * Creates a {@link AbstractImportParser}.
     */
    public AbstractImportParser(InstantiationContext context, Config aConfig) {
		this.config = aConfig;
    }

    /**
	 * Read the given source (input stream or file, assuming it has any excel or CSV format) and
	 * return the values read.
	 * 
	 * @param aComponent
	 *        Component calling, may be <code>null</code>.
	 * @param aSource
	 *        The source to read the data, must not be <code>null</code>.
	 * @param aLogger
	 *        Some importer messages to be filled, must not be <code>null</code>.
	 * @return The requested list (representing the read rows from the excel file, never
	 *         <code>null</code>.
	 * @see #validateFile(AssistentComponent, BinaryData)
	 */
    protected abstract List<Map<String, Object>> readData(AssistentComponent aComponent, Object aSource, ImportLogger aLogger);

	@Override
	public Config getConfig() {
		return this.config;
	}

	public List<ImportMessage> validateFile(AssistentComponent aComponent, BinaryData aFile) {
        ImportLogger theLogger = this.createLogger(aComponent);

        if (this.isCorrectFile(aFile)) {
            List<Map<String, Object>> theList = this.readData(aComponent, aFile, theLogger);

            if (aComponent != null) {
                if (!CollectionUtil.isEmptyOrNull(theList)) {
                    aComponent.setData(AbstractImportPerformer.VALUE_MAP, theList);
                }

                aComponent.setData(AbstractImportPerformer.IMPORT_LOGGER, theLogger);
            }
        }
        else if (aFile != null) {
            theLogger.error(AbstractImportParser.class, I18NConstants.WRONG_FILE_EXT, aFile.getName(), this.getExtensions());
        }            
        else {
            theLogger.error(AbstractImportParser.class, I18NConstants.FILE_NULL);
        }

        return this.getMessagesFromLogger(theLogger);
    }

    /** 
     * Return the extensions configured to be supported by this import parser.
     * 
     * @return    The requested list of extensions.
     */
    public List<String> getExtensions() {
        return this.getConfig().getExtensions();
    }

    /** 
     * Read the given input stream (assuming it has an excel format) and return the values read.
     * 
     * @param    aStream         The stream to read the data, must not be <code>null</code>.
     * @param    aLogger         Some importer messages to be filled, must not be <code>null</code>.
     * @return   The requested list (representing the read rows from the excel file, never <code>null</code>.
     */
    public List<Map<String, Object>> validateStream(InputStream aStream, ImportLogger aLogger) {
        return this.readData(null, aStream, aLogger);
    }

    /** 
     * Return the name of the importer this parser lives in.
     * 
     * @return   The requested name.
     */
    public String getName() {
        return this.name;
    }

    /** 
     * @see #getName()
     */
    public void setName(String aName) {
        this.name = aName;
    }

    /** 
     * Check, if the given file has the correct suffix.
     * 
     * @param    aFile    The file to be checked.
     * @return   <code>true</code> when files extension is correct.
     */
	protected boolean isCorrectFile(BinaryData aFile) {
        if (aFile != null) {
            String theFile = aFile.getName().toLowerCase();

            for (String theExt : this.getExtensions()) {
                if (theFile.endsWith(theExt.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /** 
     * Create the logger to be used during this upload.
     * 
     * @param    aComponent      Component calling this upload handler.
     * @return   The requested logger.
     */
    protected ImportLogger createLogger(AssistentComponent aComponent) {
		return new ImportMessageLogger(new ArrayList<>());
    }

    /**
	 * Return the import messages from the logger created in
	 * {@link #createLogger(AssistentComponent)}.
	 * 
	 * @param aLogger
	 *        The logger to get the messages from.
	 * @return The requested list of messages.
	 * @see #validateFile(AssistentComponent, BinaryData)
	 */
    protected List<ImportMessage> getMessagesFromLogger(ImportLogger aLogger) {
        ImportMessageLogger theMessLogger = ImportUtils.INSTANCE.getImportMessageLogger(aLogger);

        return (theMessLogger != null) ? theMessLogger.getImportMessages() : Collections.<ImportMessage>emptyList();
    }

    /**
	 * Provide a matching reader for the given source.
	 * 
	 * @param aSource
	 *        The source to get the reader for.
	 * @return The requested reader, never <code>null</code>.
	 * @throws IOException
	 *         When accessing stream fails.
	 * @throws IllegalArgumentException
	 *         When given source is no {@link File}, {@link InputStream}, or {@link BinaryContent}.
	 */
	protected Reader createReader(Object aSource) throws IOException {
		return new InputStreamReader(createStream(aSource));
    }

    /**
	 * Provide a matching input stream for the given source.
	 * 
	 * @param aSource
	 *        The source to get the input stream for.
	 * @return The requested input stream, never <code>null</code>.
	 * @throws IOException
	 *         When accessing stream fails.
	 * @throws IllegalArgumentException
	 *         When given source is no {@link File}, {@link InputStream}, or {@link BinaryContent}.
	 */
	protected InputStream createStream(Object aSource) throws IOException {
        if (aSource instanceof File) {
            return new FileInputStream((File) aSource);
        }
        else if (aSource instanceof InputStream) {
            return (InputStream) aSource;
        }
		else if (aSource instanceof BinaryContent) {
			return ((BinaryContent) aSource).getStream();
		}
        else {
			if (aSource == null) {
				throw new IllegalArgumentException("No source given!");
			} else {
				throw new IllegalArgumentException("Given source must be " + File.class.getName() + ", "
					+ InputStream.class.getName() + ", or " + BinaryContent.class.getName() + ", but was " + aSource
					+ " (" + aSource.getClass().getName() + ")");
			}
        }
    }

    /** 
     * Return the model defined by the given component (or it's dialog parent).
     * 
     * @param    aComponent    The component to get the model from.
     * @return   The requested model, may be <code>null</code>.
     */
    protected Object getModel(AssistentComponent aComponent) {
        return this.getModel(aComponent, null);
    }

    /** 
     * Return the model defined by the given component (or it's dialog parent).
     * 
     * @param    aComponent    The component to get the model from.
     * @param    aStructure    Optional name of the structure defined by a {@link StructuredElementFactory}.
     * @return   The requested model, may be <code>null</code>.
     */
    protected Object getModel(AssistentComponent aComponent, String aStructure) {
        Object theModel = null;

        if (aComponent != null) {
            LayoutComponent theParent = aComponent.getDialogParent();

            theModel = ((theParent != null) ? theParent.getModel() : aComponent.getModel());
        }

        if ((theModel == null) && !StringServices.isEmpty(aStructure)) { 
			theModel = StructuredElementFactory.getInstanceForStructure(aStructure).getRoot();
        }

        return theModel;
    }
}

