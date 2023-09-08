/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * Implementation of Unit as Wrapper.
 * 
 * UnitWrappers do not care about any Security.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class UnitWrapper extends AbstractWrapper implements Unit {

    /** Name of underlying KnowledgeObject */
    public static final String OBJECT_NAME = "Unit";

	/** Full qualified name of the {@link TLType} of a {@link Unit}. */
	public static final String UNIT_TYPE = "tl.units:Unit";

	/**
	 * Resolves {@link #UNIT_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link Unit}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #UNIT_TYPE} could not be resolved.
	 */
	public static TLStructuredType getUnitType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(UNIT_TYPE);
	}

    /** Name of Attribute for Identifier of base ID */
	public static final String BASE_FORMAT_REF = "baseUnit";
    
    /** Name of Attribute for the conversion factor */
    public static final String FACTOR       = "factor";
    
    /** Name of Attribute for the sort order*/
    public static final String SORT_ORDER   = "sortOrder";

    /** Name of Attribute for the format String */
	public static final String FORMAT = "format";

    /** Comparator to sort by SORT_ORDER attribute */
	public static final Comparator<? super Unit> COMPARATOR = new WrapperComparator(SORT_ORDER);

    /**
     * Default CTor as of contract with the WrapperFactory.
     */
	public UnitWrapper(KnowledgeObject ko) {
        super(ko);
	}

    @Override
	public void setName(String aName) {
    	setValue(UnitWrapper.NAME_ATTRIBUTE, aName);
    }

    /** 
     * true when BASE_ID attrbute is null.
     */
    @Override
	public boolean isBaseUnit() {
		return getBaseUnit() == null;
    }

    /** 
     * Return a Unit this one is based on.
     * 
     * @return null for base units (isBaseUnit())
     */
    @Override
	public Unit getBaseUnit() {
		return tGetDataReference(Unit.class, BASE_FORMAT_REF);
    }

    @Override
	public void setBaseUnit(Unit aUnit) {
		this.tSetDataReference(UnitWrapper.BASE_FORMAT_REF, aUnit);
    }

    @Override
	public Integer getSortOrder() {
		return tGetDataInteger(SORT_ORDER);
    }

    @Override
	public void setSortOrder(Integer aSortOrder) {
    	this.setValue(UnitWrapper.SORT_ORDER,  aSortOrder);
    }

    /**
     * Return the conversion factor to the base unit.
     * 
     * @return 0.0 in case Unit cannot be converted via a simple Factor (Celsius/Farenheit)
     *         1.0 in case of a basUnit (getBaseUnit() == null)
     */
    @Override
	public double getConversionFactor() throws IllegalStateException {
		return tGetDataDoubleValue(FACTOR, 1.0);
    }

    /** 
     * This method sets the conversion factor.
     */
    @Override
	public void setConversionFactor(Double aFactor) throws IllegalStateException {
        this.setValue(Currency.FACTOR,  aFactor);
    }

    /** 
     * This method sets the conversion factor.
     */
    public void setConversionFactor(double aFactor) throws IllegalStateException {
        this.setValue(Currency.FACTOR,  Double.valueOf(aFactor));
    }

    /**
     * Return the Format to display a Number as String.
     * 
     * The format is based on the FORMAT attribute and the locale.
     * 
     * @param aLocale defines the language to use.
     */ 
    @Override
	public NumberFormat getFormat(Locale aLocale) {
		return new DecimalFormat(getFormatSpec(), new DecimalFormatSymbols(aLocale));
    }

    /**
     * Return the NumberFormat using the TLContext and fallback to default Locale.
     * 
     * The Format will NOT include the unit symbol.
     */ 
    @Override
	public NumberFormat getFormat() {
        return getFormat(TLContext.getLocale());
    }

    @Override
	public String getFormatSpec() {
		return (String) getValue(FORMAT);
	}

	@Override
	public void setFormatSpec(String formatSpec) {
		setValue(FORMAT, formatSpec);
    }

    /**
     * Divide by the conversion factor format the number.
     */
    @Override
	public String format(Number aNumber) {
		if (isBaseUnit()) {
            return getFormat().format(aNumber);
        }
        else {
			double d = aNumber.doubleValue() / getConversionFactor();
            return getFormat().format(d);
        }
    }

    /**
     * Parses the given String and converts and multiplies it with the conversion factor.
     * 
     * @throws ParseException based on the formatter used.
     */
    @Override
	public Number parse(String aNumber) throws ParseException {
		if (isBaseUnit()) {
            return getFormat().parse(aNumber);
        }
        else {
            Number num = getFormat().parse(aNumber);
			return Double.valueOf(getConversionFactor() * num.doubleValue());
        }
    }
    
    /**
	 * The unit for values without unit and precision 0 decimal digits.
	 */
    public static UnitWrapper getPiece0() {
		return constant("piece0");
	}

	/**
	 * The unit for values without unit and precision 1 decimal digit.
	 */
	public static UnitWrapper getPiece1() {
		return constant("piece1");
	}

	/**
	 * The unit for values without unit and precision 2 decimal digits.
	 */
	public static UnitWrapper getPiece2() {
		return constant("piece2");
	}

	/**
	 * The unit for values without unit and precision 3 decimal digits.
	 */
	public static UnitWrapper getPiece3() {
		return constant("piece3");
	}

	private static UnitWrapper constant(String name) {
		UnitWrapper result = getInstance(name);
		if (result == null) {
			throw new UnsupportedOperationException("System not configured to support unit constant '" + name + "'.");
		}
		return result;
	}

	/**
	 * Return the wrapper for some identifier in default KnowlwdgeBase.
	 * 
	 * @param name
	 *        The name of the {@link Unit}.
	 * @return The requested UnitWrapper.
	 */
	public static UnitWrapper getInstance(String name) {
		return getInstance(PersistencyLayer.getKnowledgeBase(), name);
    }

    /**
	 * Return the wrapper for some identifier in given KnowlwdgeBase.
	 * 
	 * @param name
	 *        The name of the {@link Unit}.
	 * @return The requested UnitWrapper.
	 */
	public static UnitWrapper getInstance(KnowledgeBase aBase, String name) {
		DataObject unitItem = aBase.getObjectByAttribute(OBJECT_NAME, NAME_ATTRIBUTE, name);
		if (unitItem == null) {
            // Evil hack, should be configurable ...
        	// Fallback mechanism because Currency is a sub type
			unitItem = aBase.getObjectByAttribute(Currency.OBJECT_NAME, Currency.NAME_ATTRIBUTE, name);
        }
		return (UnitWrapper) WrapperFactory.getWrapper((KnowledgeObject) unitItem);
    }

    /**
     * Return a List of all Units in given KnowledgeBase.
     * 
     * @return a List, ordered by sortOrder, never null.
     */
	public static List<Unit> getAllUnits(KnowledgeBase aBase) {
        Collection<KnowledgeObject> theUnitKOs = aBase.getAllKnowledgeObjects(OBJECT_NAME);
        List<Unit>                  result     = new ArrayList<>(theUnitKOs.size());
        Iterator<KnowledgeObject>   iter       = theUnitKOs.iterator();
        while (iter.hasNext()) {
			result.add((Unit) WrapperFactory.getWrapper(iter.next()));
        }
        Collections.sort(result, COMPARATOR);
        return result;
    }

    /**
     * Return a List of all Units in default KnowledgeBase.
     * 
     * @return a List, ordered by sortOrder, never null.
     */
	public static List<Unit> getAllUnits() {
        return getAllUnits(getDefaultKnowledgeBase());
    }


    /**
     * Create a new Unit
     * 
     * @param aName The name (identifier) of the unit to create. No consistency checks are made.
     * @param aSortOrder The sort order on the GUI.
     * 
     * @see java.util.Currency#getInstance(String)
     */
    public static UnitWrapper createUnit(KnowledgeBase aBase, String aName, int aSortOrder) {
		{
			KnowledgeObject theKO = aBase.createKnowledgeObject(UnitWrapper.OBJECT_NAME);

            theKO.setAttributeValue(NAME_ATTRIBUTE , aName);
			theKO.setAttributeValue(FORMAT, "fixed2"); // ###,###,###,##0.00"
            theKO.setAttributeValue(SORT_ORDER     , Integer.valueOf(aSortOrder));

            return (UnitWrapper) WrapperFactory.getWrapper(theKO);
        }
    }
}
