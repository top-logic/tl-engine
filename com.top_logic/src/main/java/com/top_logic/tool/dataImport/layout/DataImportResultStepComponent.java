/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.dataImport.AbstractDataImporter;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * The DataImportResultStepComponent is the step after the parsing step of the
 * DataImportAssistant, showing the results of the parsing.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DataImportResultStepComponent extends FormComponent {

    /**
     * Creates a new instance of this class.
     */
    public DataImportResultStepComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext(this);
        AbstractDataImporter theImporter = DataImportAssistant.getImporter(this);
        ProgressResult theResult = theImporter.getParseResult();
        DataImportAssistant.createMessageFieldsFor(theResult, theContext, getResPrefix());

        String theMessage;
        if (theImporter.isInActiveMode()) {
            theMessage = getResString("importerRunning");
        }
        else {
            theMessage = getResString(theResult != null && theResult.isSuccess() ? "commitReady" : "commitNotReady");
        }

        StringField theField = FormFactory.newStringField(DataImportAssistant.FIELD_MESSAGE, IMMUTABLE);
        theField.setValue(theMessage);
        theContext.addMember(theField);

        return theContext;
    }

    /**
     * The DataImportResultStepCommandHandler starts the data import committing.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
	public static class DataImportResultStepCommandHandler extends AbstractCommandHandler {

        public final static String COMMAND_ID = "DataImportResultStepCommand";

		/**
		 * Configuration for {@link DataImportResultStepCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		public static final CommandHandler INSTANCE = newInstance(DataImportResultStepCommandHandler.class, COMMAND_ID);

        public DataImportResultStepCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			if (DataImportAssistant.getToken(aComponent).renew()) {
				AbstractDataImporter importer = DataImportAssistant.getImporter(aComponent);
				Thread thread = new ImportThread(AbstractDataImporter.COMMIT_THREAD, importer) {
					@Override
					protected void doImport() {
						_importer.commitImport();
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
            return DataImportAssistant.START_COMMITING_RULE;
        }
    }

}
