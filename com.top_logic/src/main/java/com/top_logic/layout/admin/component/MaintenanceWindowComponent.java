/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.IsPositiveIntegerConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.error.TopLogicException;

/**
 * The MaintenanceWindowComponent manages the maintenance window system mode.
 *
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public class MaintenanceWindowComponent extends FormComponent {

    /** ID of the timer field for the maintenance window timer. */
    public static final String TIME_LEFT_ID = "maintenanceCompTimeLeftField";

    /** Saves the MaintenanceWindowManager instance for faster access. */
    private MaintenanceWindowManager maintenanceWndMgr;

    /** Saves the current mode of the {@link MaintenanceWindowManager} to detect state changes. */
    private int maintenanceModeState;

    public MaintenanceWindowComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

    @Override
    public FormContext createFormContext() {
        return new FormContext(this);
    }

    @Override
	protected boolean supportsInternalModel(Object object) {
		return object == null;
    }

    @Override
    protected void componentsResolved(InstantiationContext context) {
    	super.componentsResolved(context);
    	maintenanceWndMgr = MaintenanceWindowManager.getInstance();
    	maintenanceModeState = maintenanceWndMgr.getMaintenanceModeState();
    }

    @Override
    public boolean isModelValid() {
        return (super.isModelValid() && maintenanceModeState == maintenanceWndMgr.getMaintenanceModeState());
    }

    @Override
    public boolean validateModel(DisplayContext aContext) {
        if (maintenanceModeState != maintenanceWndMgr.getMaintenanceModeState()) {
            maintenanceModeState = maintenanceWndMgr.getMaintenanceModeState();
            invalidate();
            invalidateButtons();
            super.validateModel(aContext);
            return true;
        }
        return super.validateModel(aContext);
    }

    @Override
    protected void writeJSTags(String aPath, TagWriter aOut, HttpServletRequest aRequest) throws IOException {
        super.writeJSTags(aPath, aOut, aRequest);
		HTMLUtil.writeJavascriptRef(aOut, aPath, "/script/tl/countdownTimer.js");
    }

    @Override
    protected void writeInOnload(String aContext, TagWriter aOut, HttpServletRequest aRequest) throws IOException {
        super.writeInOnload(aContext, aOut, aRequest);
        if (maintenanceWndMgr.getMaintenanceModeState() == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
            long current = System.currentTimeMillis();
            long finished = maintenanceWndMgr.getFinishedTime();
            HTMLUtil.writeJavaScriptContent(aOut, "initTimer('" + TIME_LEFT_ID + "', " + current + ", " + finished + ");");
        }
    }

    /**
     * Dialog component for entering the maintenance window mode.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class EnterMaintenanceWindowDialog extends FormComponent {

        /** Saves the name of the minutes field. */
        public static final String MIN_FIELD = "min_field";

        /** the default value for the minutes field. */
        public static final String DEFAULT_MIN_VALUE = "5";

		/**
		 * Configuration for the {@link EnterMaintenanceWindowDialog}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends FormComponent.Config {

			/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
			Lookup LOOKUP = MethodHandles.lookup();

			@Override
			default void modifyIntrinsicCommands(CommandRegistry registry) {
				registry.registerButton(EnterMaintenanceWindowDelayedCommandHandler.COMMAND);
				FormComponent.Config.super.modifyIntrinsicCommands(registry);
			}

		}

        /**
         * Creates a new EnterMaintenanceWindowDialog.
         */
        public EnterMaintenanceWindowDialog(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
            super(context, aSomeAttrs);
        }

        @Override
        public FormContext createFormContext() {
            FormContext theContext = new FormContext("form", this.getResPrefix());
            StringField theField = FormFactory.newStringField(MIN_FIELD);
            theField.setValue(DEFAULT_MIN_VALUE);
            theField.addConstraint(IsPositiveIntegerConstraint.INSTANCE);
            theContext.addMember(theField);
            return theContext;
        }

        @Override
		protected boolean supportsInternalModel(Object object) {
			return object == null;
		}

        @Override
        protected void becomingInvisible() {
            super.becomingInvisible();
            // FormContext shall be recreated each time dialog gets opened because
            // something could have changed
            removeFormContext();
        }

    }



    /**
     * This command sets the system into a maintenance window after a specified amount of
     * time, in which only users within specified groups are allowed to log in.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class EnterMaintenanceWindowDelayedCommandHandler extends CloseModalDialogCommandHandler {

        public static final String COMMAND = "EnterMaintenanceWindowDelayed";

		/**
		 * Configuration for {@link EnterMaintenanceWindowDelayedCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends CloseModalDialogCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		/**
		 * Creates a new EnterMaintenanceWindowDelayedCommandHandler.
		 */
        public EnterMaintenanceWindowDelayedCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return StrictInNormalModeExecutable.INSTANCE;
        }

        @Override
        public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            HandlerResult theResult  = new HandlerResult();
            FormComponent theComp    = (FormComponent) aComponent;
            FormContext   theContext = theComp.getFormContext();

            if (theContext.checkAll()) {
                try {
                    FormField theField = theContext.getField(EnterMaintenanceWindowDialog.MIN_FIELD);
                    String theValue = (String)theField.getValue();
                    int min = StringServices.isEmpty(theValue) ? 0 : Integer.parseInt(theValue);
                    MaintenanceWindowManager.getInstance().enterMaintenanceWindow(min*60*1000);
                    performCloseDialog(theComp, theResult);
                }
                catch (Exception e) {
                    theResult.setException(new TopLogicException(this.getClass(), "noNumber", e));
                    theResult.setCloseDialog(false);
                }
                LayoutComponent theParent = theComp.getDialogParent();
                if (theParent != null) {
                    theParent.invalidate();
                    theParent.invalidateButtons();
                }
            }
            else {
                for (Iterator theIt = theContext.getFields(); theIt.hasNext(); ) {
                    FormField theField = (FormField) theIt.next();
                    if (theField.hasError()) {
                        String theErrorMessage = theField.getError();
						theResult.addErrorText(theField.getLabel() + ": " + theErrorMessage);
                    }
                }
                theResult.setCloseDialog(false);
            }
            return theResult;
        }
    }



    /**
     * This command sets the system into a maintenance window, in which only users within
     * specified groups are allowed to log in.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class EnterMaintenanceWindowCommandHandler extends AbstractCommandHandler {

		/** Config interface for {@link EnterMaintenanceWindowCommandHandler}. */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@BooleanDefault(BoundCommand.NEEDS_CONFIRM)
			boolean getConfirm();

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			CommandGroupReference getGroup();

		}

        public static final String COMMAND = "EnterMaintenanceWindow";

        /**
         * Creates a new EnterMaintenanceWindowCommandHandler.
         */
        public EnterMaintenanceWindowCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return StrictInNormalModeExecutable.INSTANCE;
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			MaintenanceWindowManager.getInstance().enterMaintenanceWindow();
			aComponent.invalidate();
			aComponent.invalidateButtons();
			return HandlerResult.DEFAULT_RESULT;
        }

    }


    /**
     * This command leaves the maintenance window and sets the system into normal mode, in
     * which every user may be allowed to log in.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class LeaveMaintenanceWindowCommandHandler extends AbstractCommandHandler {

		/** Config interface for {@link LeaveMaintenanceWindowCommandHandler}. */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			CommandGroupReference getGroup();

		}

        public static final String COMMAND = "LeaveMaintenanceWindow";

        /**
         * Creates a new LeaveMaintenanceWindowCommandHandler.
         */
        public LeaveMaintenanceWindowCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return InMaintenanceWindowExecutable.INSTANCE;
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			MaintenanceWindowManager.getInstance().leaveMaintenanceWindow();
			aComponent.invalidate();
			aComponent.invalidateButtons();
			HandlerResult result = new HandlerResult();
			result.setCloseDialog(true);
			return result;
        }

    }

    /**
     * This command aborts the entering of the maintenance window and leaves the system in
     * normal mode, in which every user may be allowed to log in.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class AbortEnteringMaintenanceWindowCommandHandler extends AbstractCommandHandler {

		/** Config interface for {@link AbortEnteringMaintenanceWindowCommandHandler}. */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@BooleanDefault(BoundCommand.NEEDS_CONFIRM)
			boolean getConfirm();

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			CommandGroupReference getGroup();

		}

        public static final String COMMAND = "AbortEnteringMaintenanceWindow";

        /**
         * Creates a new AbortEnteringMaintenanceWindowCommandHandler.
         */
        public AbortEnteringMaintenanceWindowCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return AboutToEnterMaintenanceWindowExecutable.INSTANCE;
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			MaintenanceWindowManager.getInstance().abortEnterMaintenanceWindow();
			aComponent.invalidate();
			aComponent.invalidateButtons();
			return HandlerResult.DEFAULT_RESULT;
        }

    }

}
