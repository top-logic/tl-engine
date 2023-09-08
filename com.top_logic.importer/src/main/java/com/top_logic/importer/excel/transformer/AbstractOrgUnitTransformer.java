/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.orgunit.OrgUnitBase;
import com.top_logic.contact.orgunit.OrgUnitFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Base function to get {@link OrgUnitBase}s from an excel cell.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractOrgUnitTransformer<T> implements Transformer<T> {

	public interface Config extends Transformer.Config {
		boolean getGuessUnit();
	}

	protected final boolean guessUnit;
	protected final boolean mandatory;

	/** 
	 * Creates a {@link AbstractOrgUnitTransformer}.
	 */
	public AbstractOrgUnitTransformer(InstantiationContext aContext, Config aConfig) {
		this.guessUnit = aConfig.getGuessUnit();
	    this.mandatory = aConfig.isMandatory();
	}

	public List<OrgUnitBase> getOrgUnits(ExcelContext aContext, String columnName, ImportLogger logger) {
        String theCoord = this.getString(aContext, columnName, logger);

        if (!StringServices.isEmpty(theCoord)) {
            return AbstractOrgUnitTransformer.getOrgUnits(Arrays.asList(StringServices.split(theCoord, '\n')), guessUnit, aContext, columnName, logger, mandatory);
        }
        else { 
            return null;
        }
	}

    protected String getString(ExcelContext aContext, String columnName, ImportLogger logger) {
        return aContext.getString(columnName);
    }

	public static List<OrgUnitBase> getOrgUnits(Collection<String> someUnits, boolean guessUnit, ExcelContext aContext, String columnName, ImportLogger logger, boolean mandatory) {
	    List<OrgUnitBase>        theUnits   = new ArrayList<>();
	    Map<String, OrgUnitBase> theUnitMap = AbstractOrgUnitTransformer.getUnitMap();

	    for (String theOrg : someUnits) {
	    	OrgUnitBase theUnit = AbstractOrgUnitTransformer.getOrgUnit(theUnitMap, theOrg.trim(), guessUnit);

	        if (theUnit != null) {
	            theUnits.add(theUnit);
	        }
	        else {
	            throw new TransformException(I18NConstants.ORG_UNIT_NOT_FOUND, aContext.row() + 1, columnName, theOrg);
	        }
	    }

	    if (mandatory && theUnits.isEmpty()) {
	    	throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, someUnits.toString());
	    }

	    return theUnits;
	}

    protected static OrgUnitBase getOrgUnit(Map<String, OrgUnitBase> aUnitMap, String aName, boolean guessUnit) {
        if (StringServices.isEmpty(aName)) {
            return null;
        }
        else {
        	OrgUnitBase theUnit = aUnitMap.get(aName);

            if ((theUnit == null) && guessUnit) { 
                String theName = aName.substring(0, aName.length() - 1);

                return AbstractOrgUnitTransformer.getOrgUnit(aUnitMap, theName, guessUnit);
            }
            else {
                return theUnit;
            }
        }
    }

	public static Map<String, OrgUnitBase> getUnitMap() {
	    final Map<String, OrgUnitBase> unitMap = new HashMap<>();

	    TraversalFactory.traverse(OrgUnitFactory.getInstance().getRootOrgUnit(), new AllElementVisitor() {
	        @Override
	        public boolean onVisit(StructuredElement anElement, int aDepth) {
	            if (anElement instanceof OrgUnitBase) {
	            	OrgUnitBase theUnit = (OrgUnitBase) anElement;

	                unitMap.put(theUnit.getOrgID(), theUnit);
	            }

	            return true;
	        }
	    }, TraversalFactory.BREADTH_FIRST);

	    return unitMap;
	}
}
