/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.tool.dataImport.AbstractDataImporter;

/**
 * The TOPSFileDataImportStatusComponent displays the last result of the
 * TOPSFileDataImporter.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DataImportStatusComponent extends FormComponent {

    public interface Config extends FormComponent.Config {
		@Name(DataImportAssistant.XML_ATTR_IMPORTER_CLASS)
		@Mandatory
		@InstanceFormat
		AbstractDataImporter getImporterClass();
	}

	public static final String FIELD_LAST_RUN = "field_last_run";
    public static final String FIELD_LAST_RESULT = "field_last_result";
    public static final String FIELD_LAST_PROTOCOL = "field_last_protocol";
    public static final String FIELD_SYSTEM_DATA_TS = "field_system_data_ts";

    /** Stores the importer. */
    private AbstractDataImporter importer;



    /**
     * Creates a new instance of this class.
     */
    public DataImportStatusComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
        importer = aAtts.getImporterClass();
    }

    @Override
	protected boolean supportsInternalModel(Object object) {
		return object == null;
	}

	@Override
    public FormContext createFormContext() {
        FormContext theContext = new FormContext(this);
		Formatter theFormatter = HTMLFormatter.getInstance();
        StringField theField; String theValue;

        theValue = theFormatter.formatDateTime(importer.getLastImportRun());
        if (StringServices.isEmpty(theValue)) theValue = getResString("noLastRun");
        theField = FormFactory.newStringField(FIELD_LAST_RUN, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        theValue = importer.isInActiveMode() ? getResString("currentlyRunning") : importer.getLastImportResult();
        if (StringServices.isEmpty(theValue)) theValue = getResString("noLastResult");
        theField = FormFactory.newStringField(FIELD_LAST_RESULT, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        theValue = importer.getLastImportProtocol();
        if (StringServices.isEmpty(theValue)) theValue = getResString("noLastProtocol");
        theField = FormFactory.newStringField(FIELD_LAST_PROTOCOL, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        theValue = theFormatter.formatDate(importer.getSystemDataTimestamp());
        if (StringServices.isEmpty(theValue)) theValue = getResString("noSystemDataTS");
        theField = FormFactory.newStringField(FIELD_SYSTEM_DATA_TS, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        return theContext;
    }

    @Override
    protected void becomingVisible() {
        removeFormContext();
        super.becomingVisible();
    }

}
