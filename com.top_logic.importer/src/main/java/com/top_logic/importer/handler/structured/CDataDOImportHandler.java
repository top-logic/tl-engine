/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Set the description of the given {@link Wrapper} from the data of the given {@link DataObject}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CDataDOImportHandler<C extends CDataDOImportHandler.Config, O extends Wrapper> extends AbstractAttributedDOImportHandler<C, O> {

    public interface Config extends AbstractAttributedDOImportHandler.Config {

        @Mandatory
        String getKey();

        @StringDefault("Language")
        String getLanguageKey();
        
        @StringDefault("DE")
        String getLanguage();
    }

    private String key;

    private String languageKey;

    private String language;

    /** 
     * Creates a {@link CDataDOImportHandler}.
     */
    public CDataDOImportHandler(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);

        this.key         = aConfig.getKey();
        this.languageKey = aConfig.getLanguageKey();
        this.language    = aConfig.getLanguage();
    }

    @Override
    public ResKey execute(Map<String, Object> someObjects, ImportValueProvider valueProvider, StructureImportResult aResult) {
        try {
            O theObject = this.getCurrentObject(someObjects);

            this.setLanguageString(theObject, valueProvider.getDO(), this.key, this.language, aResult);

			return I18NConstants.OBJECT_UPDATED.fill(theObject.getName());
        }
        catch (NoSuchAttributeException ex) {
            throw new IllegalArgumentException("Cannot find language '" + this.language +  "' or _cdata attribute in setName");
        }
    }

    protected void setLanguageString(O anAttributed, DataObject aDO, String aKey, String aLanguage, StructureImportResult aResult) throws NoSuchAttributeException {
        String theLang = (String) aDO.getAttributeValue(this.languageKey);

        if (aLanguage.equalsIgnoreCase(theLang)) {
            anAttributed.setValue(aKey, this.toString(aDO));
            aResult.addUpdated(anAttributed, null);
        }
    }

    protected String toString(DataObject aDO) throws NoSuchAttributeException {
        String theString = (String) aDO.getAttributeValue("_cdata");

        return (theString != null) ? theString.trim() : null;
    }
}

