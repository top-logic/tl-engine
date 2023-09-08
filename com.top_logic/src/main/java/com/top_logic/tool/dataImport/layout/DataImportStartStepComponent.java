/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import java.util.Date;
import java.util.Map;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.dataImport.AbstractDataImporter;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * The DataImportStartStepComponent is the first step of the DataImportAssistant.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DataImportStartStepComponent extends FormComponent {

    public static final String FIELD_LAST_RUN = "field_last_run";
    public static final String FIELD_SYSTEM_DATA_TS = "field_system_data_ts";
    public static final String FIELD_IMPORT_DATA_TS = "field_import_data_ts";


    /**
     * Creates a new instance of this class.
     */
    public DataImportStartStepComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext(this);
		Formatter theFormatter = HTMLFormatter.getInstance();
        StringField theField; String theValue, theMessage;

        AbstractDataImporter theImporter = DataImportAssistant.getImporter(this);
        theValue = theFormatter.formatDateTime(theImporter.getLastImportRun());
        if (StringServices.isEmpty(theValue)) theValue = getResString("noLastRun");
        theField = FormFactory.newStringField(FIELD_LAST_RUN, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        theValue = theFormatter.formatDate(theImporter.getSystemDataTimestamp());
        if (StringServices.isEmpty(theValue)) theValue = getResString("noSystemDataTS");
        theField = FormFactory.newStringField(FIELD_SYSTEM_DATA_TS, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

		Lock theToken = DataImportAssistant.getToken(this);
		if (!theToken.check() || theImporter.isInActiveMode()) {
            theMessage = getResString("importerRunning");
            theValue = StringServices.EMPTY_STRING;
        }
        else {
            ProgressResult theResult = theImporter.prepareImport();
            if (theResult.isSuccess()) {
                theMessage = getResString("importerReady");
                theValue = theFormatter.formatDate((Date)theResult.getValue(AbstractDataImporter.RESULT_IMPORT_DATE));
            }
            else {
                theMessage = getResString("importerNotReady");
                theValue = getResString("noImportDataTS");
            }
            DataImportAssistant.createMessageFieldsFor(theResult, theContext, getResPrefix());
        }

        theField = FormFactory.newStringField(FIELD_IMPORT_DATA_TS, IMMUTABLE);
        theField.setValue(theValue);
        theContext.addMember(theField);

        theField = FormFactory.newStringField(DataImportAssistant.FIELD_MESSAGE, IMMUTABLE);
        theField.setValue(theMessage);
        theContext.addMember(theField);

        return theContext;
    }



    /**
     * Overridden to ensure importer was prepared before executability rules
     * of buttons get called.
     */
    @Override
	protected void becomingVisible() {
        super.becomingVisible();
        getFormContext();
    }

    /**
     * The DataImportStartStepCommandHandler starts the data import parsing.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
	public static class DataImportStartStepCommandHandler extends AbstractCommandHandler {

        public final static String COMMAND_ID = "DataImportStartStepCommand";

		/**
		 * Configuration for {@link DataImportStartStepCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		public static final CommandHandler INSTANCE = newInstance(DataImportStartStepCommandHandler.class, COMMAND_ID);

        public DataImportStartStepCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			if (DataImportAssistant.getToken(aComponent).renew()) {
				final AbstractDataImporter importer = DataImportAssistant.getImporter(aComponent);

				Thread thread = new ImportThread(AbstractDataImporter.PARSE_THREAD, importer) {
					@Override
					protected void doImport() {
						_importer.parseImport();
					}
				};
                thread.start();
                return HandlerResult.DEFAULT_RESULT;
            }
            HandlerResult theResult = new HandlerResult();
			theResult.addErrorText(aComponent.getResString("tokenExpired"));
            return theResult;
        }

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return DataImportAssistant.START_PARSING_RULE;
        }

    }

}
