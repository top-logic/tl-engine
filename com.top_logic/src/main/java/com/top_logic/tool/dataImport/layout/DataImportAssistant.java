/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import java.util.ArrayList;
import java.util.Map;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;
import com.top_logic.tool.boundsec.assistent.CommandChain;
import com.top_logic.tool.boundsec.assistent.CommandHolder;
import com.top_logic.tool.boundsec.assistent.StepAssistantComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.dataImport.AbstractDataImporter;
import com.top_logic.tool.dataImport.layout.DataImportResultStepComponent.DataImportResultStepCommandHandler;
import com.top_logic.tool.dataImport.layout.DataImportStartStepComponent.DataImportStartStepCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * The DataImportAssistant imports data from files.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DataImportAssistant extends BoundAssistentComponent implements HttpSessionBindingListener {

    public interface Config extends BoundAssistentComponent.Config {
		@Name(XML_ATTR_IMPORTER_CLASS)
		@Mandatory
		@InstanceFormat
		AbstractDataImporter getImporterClass();
	}

	/** XML attribute for the importer class. */
    public static final String XML_ATTR_IMPORTER_CLASS = "importerClass";

    /** Data attribute for the token context. */
    public static final String TOKEN_CONTEXT = "token_context";

    // String field constants
    public static final String FIELD_MESSAGE = "field_message";
    public static final String FIELD_INFO_MESSAGE = "field_info_message";
    public static final String FIELD_WARNING_MESSAGE = "field_warning_message";
    public static final String FIELD_ERROR_MESSAGE = "field_error_message";


    // Executable states
	public static final ExecutableState IMPORTER_RUNNING_STATE = ExecutableState.createDisabledState(I18NConstants.ERROR_IMPORTER_RUNNING);

	public static final ExecutableState IMPORTER_BUSY_STATE = ExecutableState.createDisabledState(I18NConstants.ERROR_IMPORTER_BUSY);

	public static final ExecutableState START_PARSING_DISABLED_STATE = ExecutableState.createDisabledState(I18NConstants.START_PARSING_DISABLED);

	public static final ExecutableState START_COMMITTING_DISABLED_STATE = ExecutableState.createDisabledState(I18NConstants.START_COMMITTING_DISABLED);


    /** ExecutabilityRule for starting the parsing. */
    public static final ExecutabilityRule START_PARSING_RULE = new ExecutabilityRule() {
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            AbstractDataImporter theImporter = getImporter(aComponent);
			Lock theToken = getToken(aComponent);
			if (!theToken.check() || theImporter.isInActiveMode()) {
                return IMPORTER_RUNNING_STATE;
            }
            return theImporter.canStartParsing() ? ExecutableState.EXECUTABLE : START_PARSING_DISABLED_STATE;
        }
    };

    /** ExecutabilityRule for starting the committing. */
    public static final ExecutabilityRule START_COMMITING_RULE = new ExecutabilityRule() {
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            AbstractDataImporter theImporter = getImporter(aComponent);
			Lock theToken = getToken(aComponent);
			if (!theToken.check() || theImporter.isInActiveMode()) {
                return IMPORTER_RUNNING_STATE;
            }
            return theImporter.canStartCommitting() ? ExecutableState.EXECUTABLE : START_COMMITTING_DISABLED_STATE;
        }
    };

    /** ExecutabilityRule for parsing progress. */
    public static final ExecutabilityRule PARSED_RULE = new ExecutabilityRule() {
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            return getImporter(aComponent).getMode() >= AbstractDataImporter.MODE_PARSED ? ExecutableState.EXECUTABLE : IMPORTER_BUSY_STATE;
        }
    };

    /** ExecutabilityRule for committing progress. */
    public static final ExecutabilityRule COMMITTED_RULE = new ExecutabilityRule() {
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            return getImporter(aComponent).getMode() >= AbstractDataImporter.MODE_COMMITTED ? ExecutableState.EXECUTABLE : IMPORTER_BUSY_STATE;
        }
    };

    /** ExecutabilityRule for importer progress. */
    public static final ExecutabilityRule NOT_BUSY_RULE = new ExecutabilityRule() {
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            return !getImporter(aComponent).isInActiveMode() ? ExecutableState.EXECUTABLE : IMPORTER_BUSY_STATE;
        }
    };


    /** Stores the importer. */
    private AbstractDataImporter importer;

    /** Holds the SimpleTokenContextHandler for exclusive importer usage. */
	private Lock token;



    /**
     * Creates a new instance of this class.
     */
    public DataImportAssistant(InstantiationContext context, Config aSomeAttr) throws ConfigurationException {
        super(context, aSomeAttr);
        importer = aSomeAttr.getImporterClass();
        token = importer.getNewTokenHandler();
    }

    @Override
	public void valueBound(HttpSessionBindingEvent aHttpsessionbindingevent) {
        // uninteresting
    }

    @Override
	public void valueUnbound(HttpSessionBindingEvent aEvent) {
        importer.cleanUpIfNecessary();
		removeSessionBindingListener();
    }

	@Override
	protected void becomingVisible() {
        super.becomingVisible();
		if (token.tryLock()) {
			addSessionBindingListener();
        }
    }

	void removeSessionBindingListener() {
		TLContext.getContext().getSessionContext().removeHttpSessionBindingListener(this);
	}

	void addSessionBindingListener() {
		TLContext.getContext().getSessionContext().addHttpSessionBindingListener(this);
	}

    /**
     * Gets the TokenContextManager of this assistant.
     *
     * @param aComponent
     *        the stepComponent which requires the TokenContextManager
     * @return the TokenContextManager of this assistant
     */
	public static Lock getToken(LayoutComponent aComponent) {
        if (aComponent == null) return null;
        DataImportAssistant theAss = aComponent instanceof DataImportAssistant ?
            (DataImportAssistant)aComponent : (DataImportAssistant)getEnclosingAssistentComponent(aComponent);
        return theAss.token;
    }

    /**
     * Gets the importer of this assistant.
     *
     * @param aComponent
     *        the stepComponent which requires the importer
     * @return the importer of this assistant
     */
    public static AbstractDataImporter getImporter(LayoutComponent aComponent) {
        if (aComponent == null) return null;
        DataImportAssistant theAss = aComponent instanceof DataImportAssistant ?
            (DataImportAssistant)aComponent : (DataImportAssistant)getEnclosingAssistentComponent(aComponent);
        return theAss.importer;
    }

    /**
	 * Creates up to three StringFields, each one for a message list of the given result for the
	 * given form group.
	 * 
	 * @param theResult
	 *        the progress result to create the StringField from
	 * @param aContext
	 *        the FormGroup to add the StringField to
	 * @param resPrefix
	 *        the resource prefix for message list header line i18n
	 */
	public static void createMessageFieldsFor(ProgressResult theResult, FormGroup aContext, ResPrefix resPrefix) {
        Resources resources = Resources.getInstance();
        if (theResult == null) return;

        if (theResult.hasInfos()) {
			String theValue = resources.getString(resPrefix.key("infos"), null);
			if (theValue == null)
				theValue = resources.getString(I18NConstants.INFOS);
            theValue = theValue + "\n- " + StringServices.toString(theResult.getInfos(), "\n\n- ") + "\n\n ";
            StringField theField = FormFactory.newStringField(FIELD_INFO_MESSAGE, true);
            theField.setValue(theValue);
            aContext.addMember(theField);
        }

        if (theResult.hasWarnings()) {
			String theValue = resources.getString(resPrefix.key("warnings"), null);
			if (theValue == null)
				theValue = resources.getString(I18NConstants.WARNINGS);
            theValue = theValue + "\n- " + StringServices.toString(theResult.getWarnings(), "\n\n- ") + "\n\n ";
            StringField theField = FormFactory.newStringField(FIELD_WARNING_MESSAGE, true);
            theField.setValue(theValue);
            aContext.addMember(theField);
        }

        if (theResult.hasErrors()) {
			String theValue = resources.getString(resPrefix.key("errors"), null);
			if (theValue == null)
				theValue = resources.getString(I18NConstants.ERRORS);
            theValue = theValue + "\n- " + StringServices.toString(theResult.getErrors(), "\n\n- ") + "\n\n ";
            StringField theField = FormFactory.newStringField(FIELD_ERROR_MESSAGE, true);
            theField.setValue(theValue);
            aContext.addMember(theField);
        }

    }



    /**
     * The DataImportProgressStepCommandHandler does nothing. It is for the
     * ExecutabilityRule only.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public static class DataImportProgressStepCommandHandler extends AbstractCommandHandler {

        public final static String COMMAND_ID = "DataImportProgressStepCommand";

		/**
		 * Configuration for {@link DataImportProgressStepCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        public DataImportProgressStepCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			DataImportAssistant.getToken(aComponent).renew();
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return NOT_BUSY_RULE;
        }

		public static CommandHandler newInstance(ExecutabilityRule parsedRule) {
			DataImportProgressStepCommandHandler result =
				newInstance(DataImportProgressStepCommandHandler.class, COMMAND_ID);
			result.setRule(parsedRule);
			return result;
		}

    }



    /**
     * The DataImportCancelCommandHandler does cleanup work.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public static class DataImportCancelCommandHandler extends AbstractCommandHandler {

        public final static String COMMAND_ID = "DataImportCancelCommand";

		/**
		 * Configuration for {@link DataImportCancelCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		public static final CommandHandler INSTANCE = newInstance(DataImportCancelCommandHandler.class, COMMAND_ID);

        

        public DataImportCancelCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            AbstractDataImporter theImporter = getImporter(aComponent);
			Lock theToken = getToken(aComponent);
			if (theToken.check()) {
                theImporter.cleanUpIfNecessary();
            }
			theToken.unlock();
			((DataImportAssistant) getEnclosingAssistentComponent(aComponent)).removeSessionBindingListener();

            LayoutComponent theParent = aComponent.getDialogParent();
            if (theParent instanceof FormComponent) {
                ((FormComponent)theParent).removeFormContext();
                theParent.invalidate();
            }
            return HandlerResult.DEFAULT_RESULT;
        }
    }



    /**
     * The controller for this assistant.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public static class DataImportAssistantController extends StepAssistantComponent {

		/**
		 * Typed configuration interface definition for
		 * {@link DataImportAssistant.DataImportAssistantController}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config extends StepAssistantComponent.Config {

			@Mandatory
			@Name("startStep")
			ComponentName getStartStep();

			@Mandatory
			@Name("parseStep")
			ComponentName getParseStep();

			@Mandatory
			@Name("resultStep")
			ComponentName getResultStep();

			@Mandatory
			@Name("commitStep")
			ComponentName getCommitStep();

			@Mandatory
			@Name("endStep")
			ComponentName getEndStep();
		}

		public DataImportAssistantController(InstantiationContext context, Config config) {
			super(context, config);
        }

		@Override
		public Config getConfig() {
			return (Config) super.getConfig();
		}

        @Override
		protected SimpleStepInfo[] getSteps() {
            return new SimpleStepInfo[] {getStartStep(), getParseStep(), getResultStep(), getCommitStep(), getEndStep()};
        }

        private SimpleStepInfo getStartStep() {
			CommandHolder holder =
				new CommandHolder(DataImportStartStepCommandHandler.INSTANCE,
					assistent.getStepByStepName(getConfig().getStartStep()));
			return new SimpleStepInfo(getConfig().getStartStep(), getConfig().getParseStep(), holder, FIRST_STEP,
				!LAST_STEP, SHOW_CANCEL, !FINISH_STEP);
        }

        private SimpleStepInfo getParseStep() {
			CommandHolder holder =
				new CommandHolder(DataImportProgressStepCommandHandler.newInstance(PARSED_RULE), assistent);
			return new SimpleStepInfo(getConfig().getParseStep(), getConfig().getResultStep(), holder, !FIRST_STEP,
				!LAST_STEP, !SHOW_CANCEL, !FINISH_STEP);
        }

        private SimpleStepInfo getResultStep() {
			CommandHolder holder =
				new CommandHolder(DataImportResultStepCommandHandler.INSTANCE,
					assistent.getStepByStepName(getConfig().getResultStep()));
			return new SimpleStepInfo(getConfig().getResultStep(), getConfig().getCommitStep(), holder, !FIRST_STEP,
				!LAST_STEP, SHOW_CANCEL, !FINISH_STEP);
        }

        private SimpleStepInfo getCommitStep() {
			CommandHolder holder =
				new CommandHolder(DataImportProgressStepCommandHandler.newInstance(COMMITTED_RULE), assistent);
			return new SimpleStepInfo(getConfig().getCommitStep(), getConfig().getEndStep(), holder, !FIRST_STEP,
				!LAST_STEP, !SHOW_CANCEL, !FINISH_STEP);
        }

        private SimpleStepInfo getEndStep() {
            ArrayList theCommands = new ArrayList(2);
			LayoutComponent theEndStep = assistent.getStepByStepName(getConfig().getEndStep());
			theCommands.add(new CommandHolder(DataImportCancelCommandHandler.INSTANCE, theEndStep));
            theCommands.add(new CommandHolder(CommandHandlerFactory.getInstance().getHandler(AssistentComponent.FINISH_COMMAND_KEY), theEndStep));
			CommandHolder holder =
				new CommandHolder(CommandChain.newInstance("TOPSFileDataImportEndStepCommand",
					SimpleBoundCommandGroup.READ, theCommands, theEndStep), theEndStep);
			return new SimpleStepInfo(getConfig().getEndStep(), null, holder, !FIRST_STEP, LAST_STEP, !SHOW_CANCEL,
				FINISH_STEP);
        }


        @Override
		public CommandHolder getAdditionaleCommandForCancel(ComponentName aCurrentStepName) {
            return showCancelButton(aCurrentStepName) ?
				new CommandHolder(DataImportCancelCommandHandler.INSTANCE,
					assistent.getStepByStepName(aCurrentStepName)) : null;
        }

        @Override
		public boolean showBackwardButton(ComponentName aCurrentStepName) {
            return false;
        }

        @Override
		public boolean showCancelButton(ComponentName aCurrentStepName) {
            return !disableCancelButton(aCurrentStepName);
        }

    }

}
