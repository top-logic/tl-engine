/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.excel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.orgunit.OrgHead;
import com.top_logic.contact.orgunit.OrgRoot;
import com.top_logic.contact.orgunit.OrgUnit;
import com.top_logic.contact.orgunit.OrgUnitBase;
import com.top_logic.contact.orgunit.OrgUnitFactory;
import com.top_logic.element.core.TLElementVisitor;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.util.NumberHandlerFactory;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;
import com.top_logic.util.model.ModelService;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
@SuppressWarnings("javadoc")
public class TestExcelTransformerWrappers extends AbstractTestExcelTransformer {

    private static final String TEST_FAST_LIST = "tl.beacon.three";

	Map<String, OrgUnitBase> orgMap;

    public void testWrappers() throws Exception {
        Map<String, TransformerConfig<Object>> theConfig   = this.getTransformers("wrappers.xml");
		Map<String, ExcelContext> theMap = this.getExcelContextMap(
			"./" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/importer/excel/TestExcelTransformer.xlsx");
        List<ImportMessage>                    theMessages = new ArrayList<>();
        ImportLogger                           theLogger   = new ImportMessageLogger(theMessages);
        ExcelContext                   theContext  = theMap.get("wrappers");

        this.testFastListElements(theConfig, theContext, theLogger);
        this.testPersonContacts(theConfig, theContext, theLogger);
        this.testOrgUnits(theConfig, theContext, theLogger);
    }

    // Test FastListElementTransformer

    @SuppressWarnings("unchecked")
    protected void testFastListElements(Map<String, TransformerConfig<Object>> aConfig, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestFastListElement((Transformer<Collection<FastListElement>>) aConfig.get("fast-list-element").getTransformer(),           false, false, aContext, aLogger);
        this.doTestFastListElement((Transformer<Collection<FastListElement>>) aConfig.get("fast-list-element-mandatory").getTransformer(), true,  false, aContext, aLogger);
        this.doTestFastListElement((Transformer<Collection<FastListElement>>) aConfig.get("fast-list-element-create").getTransformer(),    false, true,  aContext, aLogger);
    }

    protected void doTestFastListElement(Transformer<Collection<FastListElement>> aTransformer, boolean isMandatory, boolean isCreate, ExcelContext aContext, ImportLogger aLogger) {
        FastListElement theGreen  = this.getFastListElement(TEST_FAST_LIST + ".green");
        FastListElement theYellow = this.getFastListElement(TEST_FAST_LIST + ".yellow");
        FastListElement theRed    = this.getFastListElement(TEST_FAST_LIST + ".red");

        this.doTestFastListElement(1, "Ampel", theGreen,  aTransformer, aContext, aLogger);
        this.doTestFastListElement(2, "Ampel", theYellow, aTransformer, aContext, aLogger);
        this.doTestFastListElement(3, "Ampel", theRed,    aTransformer, aContext, aLogger);

        if (!isMandatory) {
            this.doTestFastListElement(5, "Ampel", Collections.emptyList(), aTransformer, aContext, aLogger); // empty cell

            if (!isCreate) {
                // Defined fall back in this configuration
                this.doTestFastListElement(4, "Ampel", theGreen, aTransformer, aContext, aLogger); // Orange
            }
            else {
                // Defined create in this configuration
                this.doTestFastListElement(4, "Ampel", TEST_FAST_LIST + ".3", aTransformer, aContext, aLogger); // Orange
            }
        }
        else { 
            this.doTestValueFail(4, "Ampel", aTransformer, aContext, aLogger); // Orange
            this.doTestValueFail(5, "Ampel", aTransformer, aContext, aLogger); // empty cell
        }
    }

    @SuppressWarnings("unchecked")
    protected void testPersonContacts(Map<String, TransformerConfig<Object>> aConfig, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestPersonContact((Transformer<PersonContact>) aConfig.get("person-contact").getTransformer(),           false, aContext, aLogger);
        this.doTestPersonContact((Transformer<PersonContact>) aConfig.get("person-contact-mandatory").getTransformer(), true,  aContext, aLogger);
    }

    protected void doTestPersonContact(Transformer<PersonContact> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestPersonContact(1, "Personen", "Aumann", aTransformer, aContext, aLogger);
		this.doTestPersonContact(2, "Personen", "Dugar", aTransformer, aContext, aLogger);

        this.doTestValueFail(3, "Personen", aTransformer, aContext, aLogger);
        this.doTestValueFail(4, "Personen", aTransformer, aContext, aLogger);

        if (!isMandatory) { 
            this.doTestPersonContact(5, "Unknown",  null, aTransformer, aContext, aLogger);
        }
        else { 
            this.doTestValueFail(5, "Unknown", aTransformer, aContext, aLogger);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void testOrgUnits(Map<String, TransformerConfig<Object>> aConfig, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestOrgUnit((Transformer<OrgUnitBase>) aConfig.get("org-unit").getTransformer(),           false, aContext, aLogger);
        this.doTestOrgUnit((Transformer<OrgUnitBase>) aConfig.get("org-unit-mandatory").getTransformer(), true,  aContext, aLogger);
    }
    
    protected void doTestOrgUnit(Transformer<OrgUnitBase> aTransformer, boolean isMandatory, ExcelContext aContext, ImportLogger aLogger) {
        this.doTestOrgUnit(1, "Orga", "Abt1", aTransformer, aContext, aLogger);
        this.doTestOrgUnit(2, "Orga", "Abt2", aTransformer, aContext, aLogger);
        
        this.doTestValueFail(3, "Orga", aTransformer, aContext, aLogger);
        this.doTestValueFail(4, "Orga", aTransformer, aContext, aLogger);
        
        if (!isMandatory) { 
            this.doTestOrgUnit(5, "Unknown",  null, aTransformer, aContext, aLogger);
        }
        else { 
            this.doTestValueFail(5, "Unknown", aTransformer, aContext, aLogger);
        }
    }

    protected void doTestFastListElement(int aRow, String aColumn, Object anExpected, Transformer<?> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        aContext.row(aRow);

        FastListElement theResult = this.getFastListElement(aTransformer.transform(aContext, aColumn, null, aLogger));

        assertEquals("Failed to parse row: " + aRow + ", column: \"" + aColumn + "\"!", this.getFastListElement(anExpected), theResult); 
    }

    protected void doTestPersonContact(int aRow, String aColumn, String anExpected, Transformer<PersonContact> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        aContext.row(aRow);

        PersonContact theResult = aTransformer.transform(aContext, aColumn, null, aLogger);

        assertEquals("Failed to parse row: " + aRow + ", column: \"" + aColumn + "\"!", this.getPersonContact(anExpected), theResult); 
    }
    
    protected void doTestOrgUnit(int aRow, String aColumn, String anExpected, Transformer<OrgUnitBase> aTransformer, ExcelContext aContext, ImportLogger aLogger) {
        aContext.row(aRow);
        
        OrgUnitBase theResult = aTransformer.transform(aContext, aColumn, null, aLogger);
        
        assertEquals("Failed to parse row: " + aRow + ", column: \"" + aColumn + "\"!", this.getOrgUnit(anExpected), theResult); 
    }

    protected OrgUnitBase getOrgUnit(Object anObject) {
        if (anObject instanceof OrgUnitBase) {
            return (OrgUnitBase) anObject;
        }
        else if (anObject instanceof Collection) {
            return (OrgUnitBase) CollectionUtil.getSingleValueFrom(anObject);
        }
        else if (anObject instanceof String) {
            return this.orgMap.get(anObject);
        }
        else { 
            return null;
        }
    }

    protected PersonContact getPersonContact(Object anObject) {
        if (anObject instanceof PersonContact) {
            return (PersonContact) anObject;
        }
        else if (anObject instanceof Collection) {
            return (PersonContact) CollectionUtil.getSingleValueFrom(anObject);
        }
        else if (anObject instanceof String) {
            return (PersonContact) PersonContact.getByName((String) anObject);
        }
		else if (anObject == null) {
			return null;
		}
        else { 
			throw new IllegalArgumentException("Not a person: " + anObject);
        }
    }

    protected FastListElement getFastListElement(Object anObject) {
        if (anObject instanceof FastListElement) {
            return (FastListElement) anObject;
        }
        else if (anObject instanceof Collection) {
            return (FastListElement) CollectionUtil.getSingleValueFrom(anObject);
        }
        else if (anObject instanceof String) {
            return FastListElement.getElementByName((String) anObject);
        }
        else {
            return null;
        }
    }

    protected void createOrgUnitMap() {
        this.orgMap = new HashMap<>();

        TLElementVisitor theVisitor = new AllElementVisitor() {
            @Override
            public boolean onVisit(StructuredElement anElement, int aDepth) {
                if (anElement instanceof OrgUnitBase) {
                    OrgUnitBase theUnit = (OrgUnitBase) anElement;

                    TestExcelTransformerWrappers.this.orgMap.put(theUnit.getName(), theUnit);
                }

                return super.onVisit(anElement, aDepth);
            }
        };

        OrgRoot theRoot   = OrgUnitFactory.getInstance().getRootOrgUnit();
        OrgHead theHead   = (OrgHead) theRoot.createChild("Company", OrgUnitFactory.ELEMENT_NAME_ORG_HEAD);
        OrgUnit theChild1 = (OrgUnit) theHead.createChild("Abt1", OrgUnitFactory.ELEMENT_NAME_ORG_UNIT);
        OrgUnit theChild2 = (OrgUnit) theHead.createChild("Abt2", OrgUnitFactory.ELEMENT_NAME_ORG_UNIT);

        theHead.setValue(OrgHead.ATTRIBUTE_ORG_ID, "Company");
        theChild1.setValue(OrgUnit.ATTRIBUTE_ORG_ID, "Abt1");
        theChild2.setValue(OrgUnit.ATTRIBUTE_ORG_ID, "Abt2");

        TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.DEPTH_FIRST);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.createOrgUnitMap();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        Test theTest = ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE, TestExcelTransformerWrappers.class,
			LabelProviderService.Module.INSTANCE,
			NumberHandlerFactory.Module.INSTANCE,
			ModelService.Module.INSTANCE);

        return PersonManagerSetup.createPersonManagerSetup(theTest);
    }
}

