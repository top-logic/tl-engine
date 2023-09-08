/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.excel;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
@SuppressWarnings("javadoc")
public class TestExcelTransformerPrimitives extends AbstractTestExcelTransformer {

    @SuppressWarnings("unchecked")
    public void testPrimitives() throws Exception {
        Map<String, TransformerConfig<Object>> theConfig   = this.getTransformers("primitives.xml");
		Map<String, ExcelContext> theMap = this.getExcelContextMap(
			"./" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/importer/excel/TestExcelTransformer.xlsx");
        List<ImportMessage>                    theMessages = new ArrayList<>();
        ImportLogger                           theLogger   = new ImportMessageLogger(theMessages);
        ExcelContext                           theContext  = theMap.get("primitives");

        this.doTestString    ((Transformer<String>)  theConfig.get("string")    .getTransformer(), false, theContext, theLogger);
        this.doTestLong      ((Transformer<Long>)    theConfig.get("long")      .getTransformer(), false, theContext, theLogger);
        this.doTestDouble    ((Transformer<Double>)  theConfig.get("double")    .getTransformer(), false, theContext, theLogger);
		this.doTestBoolean((Transformer<Boolean>) theConfig.get("boolean").getTransformer(), false, false,
			theContext, theLogger);
		this.doTestBoolean((Transformer<Boolean>) theConfig.get("boolean-no-trim").getTransformer(), false, true,
			theContext, theLogger);
        this.doTestDate      ((Transformer<Date>)    theConfig.get("date")      .getTransformer(), false, theContext, theLogger);
        this.doTestDateDoW   ((Transformer<Date>)    theConfig.get("dateDoW")   .getTransformer(), false, theContext, theLogger);
        this.doTestInt2String((Transformer<String>)  theConfig.get("int2string").getTransformer(), false, theContext, theLogger);
        this.doTestURL2String((Transformer<String>)  theConfig.get("url")       .getTransformer(), false, theContext, theLogger);

        this.doTestString    ((Transformer<String>)  theConfig.get("string-mandatory")    .getTransformer(), true, theContext, theLogger);
        this.doTestLong      ((Transformer<Long>)    theConfig.get("long-mandatory")      .getTransformer(), true, theContext, theLogger);
        this.doTestDouble    ((Transformer<Double>)  theConfig.get("double-mandatory")    .getTransformer(), true, theContext, theLogger);
		this.doTestBoolean((Transformer<Boolean>) theConfig.get("boolean-mandatory").getTransformer(), true, false,
			theContext, theLogger);
        this.doTestDate      ((Transformer<Date>)    theConfig.get("date-mandatory")      .getTransformer(), true, theContext, theLogger);
        this.doTestDateDoW   ((Transformer<Date>)    theConfig.get("dateDoW-mandatory")   .getTransformer(), true, theContext, theLogger);
        this.doTestInt2String((Transformer<String>)  theConfig.get("int2string-mandatory").getTransformer(), true, theContext, theLogger);

        this.doTestSubString    ((Transformer<String>) theConfig.get("sub-string")   .getTransformer(), theContext, theLogger);
        this.doTestString2Number((Transformer<String>) theConfig.get("string2number").getTransformer(), theContext, theLogger);
    }

    protected void doTestString(Transformer<String> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "String", "Hallo",  aTransformer, aContext, aLogger);
        this.doTestValue(2, "String", "Huhu",   aTransformer, aContext, aLogger);

        if (!isMandatory) { 
            this.doTestValue(3, "String",  "!\"§$%&/()=", aTransformer, aContext, aLogger);
            this.doTestValue(4, "String",  null,          aTransformer, aContext, aLogger);
            this.doTestValue(6, "Unknown", null,          aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(3, "String",  aTransformer, aContext, aLogger);
            this.doTestValueFail(4, "String",  aTransformer, aContext, aLogger);
            this.doTestValueFail(6, "Unknown", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestLong(Transformer<Long> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "Long", Long.valueOf(1l),     aTransformer, aContext, aLogger);
        this.doTestValue(2, "Long", Long.valueOf(10000l), aTransformer, aContext, aLogger);
        this.doTestValue(3, "Long", Long.valueOf(10000l), aTransformer, aContext, aLogger);

        this.doTestValueFail(4, "Long", aTransformer, aContext, aLogger);

        if (!isMandatory) { 
            this.doTestValue(5, "Long",    null, aTransformer, aContext, aLogger);
            this.doTestValue(6, "Unknown", null, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(5, "Long",    aTransformer, aContext, aLogger);
            this.doTestValueFail(6, "Unknown", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestDouble(Transformer<Double> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "Double",  Double.valueOf(1d),       aTransformer, aContext, aLogger);
        this.doTestValue(2, "Double",  Double.valueOf(10000d),   aTransformer, aContext, aLogger);
        this.doTestValue(3, "Double",  Double.valueOf(10000.1d), aTransformer, aContext, aLogger);

        this.doTestValueFail(4, "Double", aTransformer, aContext, aLogger);

        if (!isMandatory) { 
            this.doTestValue(5, "Double",  null, aTransformer, aContext, aLogger);
            this.doTestValue(6, "Unknown", null, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(5, "Double",  aTransformer, aContext, aLogger);
            this.doTestValueFail(6, "Unknown", aTransformer, aContext, aLogger);
        }
    }

	protected void doTestBoolean(Transformer<Boolean> aTransformer, boolean isMandatory, boolean noTrim,
			ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "Boolean", Boolean.TRUE,  aTransformer, aContext, aLogger);
        this.doTestValue(2, "Boolean", Boolean.FALSE, aTransformer, aContext, aLogger);
		// Test trim of value
		if (noTrim) {
			doTestValueFail(5, "Boolean", aTransformer, aContext, aLogger);
		} else {
			doTestValue(5, "Boolean", Boolean.TRUE, aTransformer, aContext, aLogger);
		}

        this.doTestValueFail(4, "Boolean", aTransformer, aContext, aLogger);

        if (!isMandatory) { 
            this.doTestValue(3, "Boolean", null,          aTransformer, aContext, aLogger);
			this.doTestValue(6, "Boolean", Boolean.FALSE, aTransformer, aContext, aLogger);
			this.doTestValue(7, "Unknown", Boolean.FALSE, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(3, "Boolean", aTransformer, aContext, aLogger);
			this.doTestValueFail(6, "Boolean", aTransformer, aContext, aLogger);
			this.doTestValueFail(7, "Unknown", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestDate(Transformer<Date> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) throws ParseException {
        Date theDate = CalendarUtil.newSimpleDateFormat("yyyyMMdd-HH:mm:ss").parse("20160101-14:00:00");

        this.doTestDate(aTransformer, theDate, isMandatory, aContext, aLogger);
    }

    protected void doTestDateDoW(Transformer<Date> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) throws ParseException {
        DateFormat theFormat = CalendarUtil.newSimpleDateFormat("yyyyMMdd-HH:mm:ss");

        this.doTestDate(aTransformer, theFormat.parse("20160102-14:00:00"), isMandatory, aContext, aLogger);

        // Test calendar weeks (without time).
        Date theDate2 = theFormat.parse("20160102-00:00:00");

        this.doTestValue(5, "Date", theDate2, aTransformer, aContext, aLogger);
        this.doTestValue(6, "Date", theDate2, aTransformer, aContext, aLogger);
        this.doTestValue(8, "Date", theDate2, aTransformer, aContext, aLogger);
    }

    protected void doTestInt2String(Transformer<String> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "Integer", "Hallo", aTransformer, aContext, aLogger);
        this.doTestValue(2, "Integer", "12345", aTransformer, aContext, aLogger);

        this.doTestValueFail(3, "Integer", aTransformer, aContext, aLogger);

        if (!isMandatory) {
            this.doTestValue(4, "Integer", null, aTransformer, aContext, aLogger);
            this.doTestValue(5, "Unknown", null, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(4, "Integer", aTransformer, aContext, aLogger);
            this.doTestValueFail(5, "Unknown", aTransformer, aContext, aLogger);
        }
    }
    
    protected void doTestURL2String(Transformer<String> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "URL", "http://www.google.de", aTransformer, aContext, aLogger);
        this.doTestValue(2, "URL", "file://somewhere/over/the/rainbow", aTransformer, aContext, aLogger);

        this.doTestValueFail(3, "URL", aTransformer, aContext, aLogger);
        this.doTestValueFail(4, "URL", aTransformer, aContext, aLogger);
        this.doTestValueFail(5, "URL", aTransformer, aContext, aLogger);

        if (!isMandatory) {
            this.doTestValue(6, "URL", null, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(6, "URL", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestDate(Transformer<Date> aTransformer, Date aDate, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "Date", aDate, aTransformer, aContext, aLogger);
        this.doTestValue(2, "Date", aDate, aTransformer, aContext, aLogger);
        this.doTestValue(7, "Date", aDate, aTransformer, aContext, aLogger);

        this.doTestValueFail(3, "Date", aTransformer, aContext, aLogger);

        if (!isMandatory) {
            this.doTestValue(4, "Date",    null, aTransformer, aContext, aLogger);
            this.doTestValue(5, "Unknown", null, aTransformer, aContext, aLogger);
        }
        else {
            this.doTestValueFail(4, "Date",    aTransformer, aContext, aLogger);
            this.doTestValueFail(5, "Unknown", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestSubString(Transformer<String> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "String", "Hallo", aTransformer, aContext, aLogger);
        this.doTestValue(2, "String", "Huhu",  aTransformer, aContext, aLogger);
        this.doTestValue(3, "String", "!\"§$", aTransformer, aContext, aLogger);
        this.doTestValue(4, "Unknown", null,   aTransformer, aContext, aLogger);
    }
    
    protected void doTestString2Number(Transformer<String> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestValue(1, "String",  Long.valueOf(23l), aTransformer, aContext, aLogger);
        this.doTestValue(2, "String",  Long.valueOf(42l), aTransformer, aContext, aLogger);
        this.doTestValue(4, "String",  null,              aTransformer, aContext, aLogger);
        this.doTestValue(3, "Unknown", null,              aTransformer, aContext, aLogger);

        this.doTestValueFail(3, "String", aTransformer, aContext, aLogger);
    }

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestExcelTransformerPrimitives.class);
    }
}

