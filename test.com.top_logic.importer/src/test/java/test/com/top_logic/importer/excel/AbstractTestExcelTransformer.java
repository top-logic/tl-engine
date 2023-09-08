/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.excel;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import test.com.top_logic.base.office.AbstractPOIExcelTest;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractTestExcelTransformer extends AbstractPOIExcelTest {

    public interface Config<T extends Object> extends PolymorphicConfiguration<AbstractTestExcelTransformer> {

        @Mandatory
        @Key(TransformerConfig.NAME_ATTRIBUTE)
        Map<String, TransformerConfig<T>> getTransformers();
    }

    public interface TransformerConfig<T extends Object> extends PolymorphicConfiguration<AbstractTestExcelTransformer>, NamedConfiguration {

        @Mandatory
        @InstanceFormat
        Transformer<?> getTransformer();
    }

    protected void doTestValue(int aRow, String aColumn, Object aResult, Transformer<?> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        aContext.row(aRow);

        assertEquals("Failed to parse row: " + aRow + ", column: \"" + aColumn + "\"!", aResult, aTransformer.transform(aContext, aColumn, null, aLogger));
    }

    protected void doTestValueFail(int aRow, String aColumn, Transformer<?> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        aContext.row(aRow);

        try {
            Object theValue = aTransformer.transform(aContext, aColumn, null, aLogger);

            fail("Row: " + aRow + ", column: \"" + aColumn + "\" parsed without error (value: '" + theValue + "')!");
        }
        catch (TransformException ex) {
            Logger.info("Transformer '" + aTransformer.toString() + "' failed, expected (message is : '" + ex.getMessage() + "')!", AbstractTestExcelTransformer.class);
        }
    }

    protected Map<String, ExcelContext> getExcelContextMap(String aFile) throws InvalidFormatException, IOException {
        File                      theFile = new File(aFile);
        Workbook                  theBook = WorkbookFactory.create(theFile);
        Map<String, ExcelContext> theMap  = new HashMap<>();

        for (int thePos = 0; thePos < theBook.getNumberOfSheets(); thePos++) {
            Sheet theSheet = theBook.getSheetAt(thePos);

            if (theSheet.getLastRowNum() > 1) {
                ExcelContext theContext = ExcelContext.getInstance(theSheet);

                theContext.prepareHeaderRows();
                theContext.row(1);

                theMap.put(theSheet.getSheetName(), theContext);
            }
        }

        return theMap;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> Map<String, TransformerConfig<T>> getTransformers(String aKey) throws ConfigurationException {
        Config<T> theConfig = (Config<T>) readConfiguration(AbstractTestExcelTransformer.class, this.getDescriptors(), aKey, null);

        return theConfig.getTransformers();
    }

    protected Map<String, ConfigurationDescriptor> getDescriptors() {
        return Collections.singletonMap("transformer", TypedConfiguration.getConfigurationDescriptor(AbstractTestExcelTransformer.Config.class));
    }

    protected ConfigurationItem readConfiguration(Class<?> testClass, Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName, String fileSuffix, ConfigurationItem fallback) throws ConfigurationException {
		BinaryContent binaryContent = ClassRelativeBinaryContent.withSuffix(testClass, fileSuffix);

        return new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, globalDescriptorsByLocalName).setBaseConfig(fallback).setSource(binaryContent).read();
    }

    private String getResourceName(Class<?> testClass, String fileSuffix) {
        return testClass.getSimpleName() + '.' + fileSuffix;
    }
}

