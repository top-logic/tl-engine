/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.ImportValueProvider.ValueHolder;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;
import com.top_logic.importer.xml.XMLFileImportParser;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Handle data updates of {@link Wrapper} objects.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractAttributedDOImportHandler<C extends AbstractAttributedDOImportHandler.Config, O extends Wrapper> extends AbstractDOImportHandler<C, O> {
 
	/**
     * Configuration for an attributed based DOImportHandler.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends XMLFileImportParser.MappingConfig {

        @StringDefault("yyyy-MM-dd-HH.mm.ss")
        String getDateFormatPattern();
    }

    private static final Property<DateFormat> DATE_FORMATTER = TypedAnnotatable.property(DateFormat.class, "commandDataFormatter");

    private final String dateFormatPattern;

    /** 
     * Creates a {@link AbstractAttributedDOImportHandler}.
     */
    public AbstractAttributedDOImportHandler(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);

        this.dateFormatPattern = aConfig.getDateFormatPattern();
    }

    /**
     * Update a given ProductionBase from a given DataObject.
     * 
     * @param anAttributed   
     *        The current business object. Must not be <code>null</code>.
     * @param valueProvider  
     *        Provides values for the attributes of the knowledge Model. 
     *        In case it is <code>null</code> the default meta-attribute to DO-Attribute matching is used.   
     * @param aResult
     *        Import result to be filled by this method.
     * @param create
     *        <code>true</code> when given object has been created.
     */
    protected ResKey excecuteUpdate(Wrapper anAttributed, ImportValueProvider valueProvider, StructureImportResult aResult, boolean create) {
		boolean hasChanged = create;

		for (TLStructuredTypePart part : anAttributed.tType().getAllParts()) {
			TLStructuredTypePart theMA = (TLStructuredTypePart) part;
            String theMAName        = theMA.getName();
            String normalizedMAName = theMAName.toLowerCase();

            if (valueProvider.hasAttribute(normalizedMAName)) {
                ValueHolder theDOVal  = valueProvider.getValue(normalizedMAName);
				Object      theAttVal = null;

				if (theDOVal.hasValue()) {
				    try {
						switch (AttributeOperations.getMetaAttributeType(theMA)) {
							case LegacyTypeCodes.TYPE_BOOLEAN:
				                theAttVal = this.getBoolean(theDOVal.getValue()); break;
							case LegacyTypeCodes.TYPE_DATE:
				                theAttVal = this.getDate(theDOVal.getValue()); break;
							case LegacyTypeCodes.TYPE_FLOAT:
				                theAttVal = this.getFloat(theDOVal.getValue()); break;
							case LegacyTypeCodes.TYPE_LONG:
				                theAttVal = getLong(theDOVal.getValue()); break;
							case LegacyTypeCodes.TYPE_STRING:
				                theAttVal = this.getString(theDOVal.getValue()); break;
				            default:
				        }
				    }
				    catch (Exception ex) {
				        Logger.error("Cannot parse value \"" + theDOVal.getValue() + "\" for attribute " + theMAName, this);
				    }
				}
				if (theAttVal != null) {
					hasChanged = AbstractImportPerformer.setValue(anAttributed, theMAName, theAttVal) && hasChanged;
				}
            }
        }

		if (hasChanged) {
			if (create) {
				aResult.addCreated(anAttributed, valueProvider.getRawValues());
			} else {
				aResult.addUpdated(anAttributed, valueProvider.getRawValues());
			}

			return I18NConstants.OBJECT_UPDATED.fill(anAttributed.getName());
		} else {
			return null;
		}
    }

    protected String getString(Object aString) {
        return (aString instanceof String) ? ((String) aString).trim() : null;
    }

    protected Long getLong(Object aString) {
        if (aString instanceof Long) {
            return (Long) aString;
        }
        else if (aString instanceof String) { 
            return Long.valueOf((String) aString);
        }
        else {
            return null;
        }
    }

    protected Float getFloat(Object aString) {
        if (aString instanceof Float) {
            return (Float) aString;
        }
        else if (aString instanceof String) { 
            NumberFormat theFormat = HTMLFormatter.getInstance().getDoubleFormat();

            try {
                return theFormat.parse((String) aString).floatValue();
            }
            catch (ParseException e) {
                Logger.error("Failed to parse '" + aString + "'!", e, this);
            }
        }

        return null;
    }

    protected Boolean getBoolean(Object aString) {
        if (aString instanceof Boolean) {
            return (Boolean) aString;
        }
        else if (aString instanceof String) { 
            return Boolean.valueOf((String) aString);
        }
        else {
            return null;
        }
    }

    protected Date getDate(Object aString) throws ParseException {
        if (aString instanceof Date) {
            return (Date) aString;
        }
        else if (aString instanceof String) { 
            return this.getDateFormat().parse((String) aString);
        }
        else {
            return null;
        }
    }

    protected DateFormat getDateFormat() {
        DisplayContext theContext = DefaultDisplayContext.getDisplayContext();

        if (theContext != null) {
            DateFormat theFormat = theContext.get(DATE_FORMATTER);

            if (theFormat == null) {
				theFormat = CalendarUtil.newSimpleDateFormat(this.dateFormatPattern);

                theContext.set(DATE_FORMATTER, theFormat);
            }

            return theFormat;
        }
        else {
			return CalendarUtil.newSimpleDateFormat(this.dateFormatPattern);
        }
    }

    /** 
     * Post processor call for {@link #execute(Map, ImportValueProvider, StructureImportResult)} method.
     * 
     * @param anAttributed
     *        The attributed we are working on.
     * @param aValueProvider
     *        Value provider with data from {@link AbstractImportParser}.
     * @param aModel
     *        Element we are currently working on.
     * @param created
     *        <code>true</code> when attributed has been created.
     * @see   #getValues(ImportValueProvider)
     */
    protected void postProcess(O anAttributed, ImportValueProvider aValueProvider, O aModel, boolean created) {
        Map<String, Object> theValues = this.getValues(aValueProvider);

        this.getConfig().getPostProcessor().postProcessObject(anAttributed, theValues, aModel, created);
    }

    /** 
     * Extract values from the data object in the value provider.
     * 
     * @param aProvider
     *        The value provider to get the data from.
     * @return The requested map of values stored in the data object handled by the value provider.
     * @see    #postProcess(Wrapper, ImportValueProvider, Wrapper, boolean)
     */
    protected Map<String, Object> getValues(ImportValueProvider aProvider) {
        Map<String, Object>           theValues = new HashMap<>();
        GenericDataObjectWithChildren theDO     = aProvider.getDO();

        for (String theKey : theDO.getAttributeNames()) {
            try {
                theValues.put(theKey, theDO.getAttributeValue(theKey));
            }
            catch (NoSuchAttributeException ex) {
                throw new TopLogicException(StructuredElementDOImportHandler.class, "values.get", new Object[] {theKey}, ex);
            }
        }

        return theValues;
    }
}
