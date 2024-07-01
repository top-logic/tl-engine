/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.BasicFileLog;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.component.AJAXSupport;
import com.top_logic.layout.basic.component.BasicAJAXSupport;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.IFrameLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A generic component to show the progress of a long lasting process provided by an
 * {@link com.top_logic.layout.progress.ProgressInfo} .
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 * 
 * @deprecated Use {@link ProgressDialog}.
 */
@Deprecated
public abstract class AJAXProgressComponent extends BoundComponent implements HTMLConstants {

	public interface Config extends BoundComponent.Config {

		String FINISH_COMMAND_HANDLERS = WriteFinishedProgressCommand.COMMAND_NAME;
		
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(SCROLL_UP)
		@BooleanDefault(SCROLL_UP_DEFAULT)
		boolean getScrollUp();

		void setScrollUp(boolean value);

		@Name(CLOSE_ON_FINISH)
		@BooleanDefault(CLOSE_ON_FINISH_DEFAULT)
		boolean getCloseOnFinish();

		void setCloseOnFinish(boolean value);

		@Name(LOG_MESSAGES)
		@BooleanDefault(LOG_MESSAGES_DEFAULT)
		boolean getLogMessages();

		@Name(LOG_FILE_PREFIX)
		@StringDefault(LOG_FILE_PREFIX_DEFAULT)
		String getLogFilePrefix();

		@Name(USE_INTERVALL_FROM_PROGRESS_PROVIDER)
		@BooleanDefault(USE_INTERVAL_FROM_PROGRESS_PROVIDER_DEFAULT)
		boolean getUseIntervallFromProgressProvider();

		@Name(REFRESH_INTERVAL)
		@IntDefault(REFRESH_INTERVAL_DEFAULT)
		int getRefreshInterval();

		@Name(PROGRESS_COMPONENT_WIDTH)
		@StringDefault(PROGRESS_COMPONENT_WIDTH_DEFAULT)
		String getProgressComponentWidth();

		@Name(PROGRESS_COMPONENT_HEIGHT)
		@StringDefault(PROGRESS_COMPONENT_HEIGHT_DEFAULT)
		String getProgressComponentHeight();

		// Use legacy rendering since this component does not controls for rendering and depends on
		// IFRAME rendering.
		@Override
		@ItemDefault(IFrameLayoutControlProvider.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@FormattedDefault(FINISH_COMMAND_HANDLERS)
		@Format(CommaSeparatedStrings.class)
		List<String> getFinishCommandHandlers();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(ProgressReplacement.COMMAND_NAME);
			for (String finishHandler : getFinishCommandHandlers()) {
				registry.registerCommand(finishHandler);
			}
		}
	}

	/**
	 * ID of the DOM element which is used to store the timeout variable
	 */
	private static final String TIMEOUT_ANCHOR_ID = "timeoutAnchor";

	/**
	 * Indicates if you want to use the refreshIntervall given by the ProgressInfo-Component or set
	 * an own interval in the XML-File
	 */
    private static final String USE_INTERVALL_FROM_PROGRESS_PROVIDER = "useIntervallFromProgressProvider";

    /** true if the messages during the progress should be logged. */
    private static final String LOG_MESSAGES = "logMessages";

    /** true if the dialog should close when the progress is finished */
    private static final String CLOSE_ON_FINISH = "closeOnFinish";

    /** customizes the Message-Window. */
    private static final String SCROLL_UP = "scrollUp";

    /** Constant for "messages" */
    private static final String MESSAGES = "messages";

    /** Constant for "progressTable" */
    private static final String PROGRESS_TABLE = "progressTable";

    /** Constant for "progressText" */
    private static final String PROGRESS_TEXT = "progressText";

    /** Constant for "refreshInterval" */
    private static final String REFRESH_INTERVAL = "refreshInterval";

    /** SCROLL_UP_DEFAULT = true */
    private static final boolean SCROLL_UP_DEFAULT = true;

    /** CLOSE_ON_DEFAULT = false */
    private static final boolean CLOSE_ON_FINISH_DEFAULT = false;

    /** USE_INTERVAL_FROM_PROGRESS_PROVIDER_DEFAULT = false */
    private static final boolean USE_INTERVAL_FROM_PROGRESS_PROVIDER_DEFAULT = false;

    /** LOG_MESSAGES_DEFAULT = false */
    private static final boolean LOG_MESSAGES_DEFAULT = false;

    /** Constant for "logMessagePrefix" */
    private static final String LOG_FILE_PREFIX = "logFilePrefix";

    /** LOG_MESSAGE_PREFIX_DEFAULT = "ProgressLog_" */
    private static final String LOG_FILE_PREFIX_DEFAULT = "ProgressLog_";

    /** REFRESH_INTERVAL_DEFAULT = 1 */
    private static final int REFRESH_INTERVAL_DEFAULT = 1;

    /** PROGRESS_COMPONENT_WIDTH_DEFAULT = "300px" */
    private static final String PROGRESS_COMPONENT_WIDTH_DEFAULT = "300px";

    /** PROGRESS_COMPONENT_HEIGHT_DEFAULT = "100px" */
    private static final String PROGRESS_COMPONENT_HEIGHT_DEFAULT = "100px";

    /** PROGRESS_COMPONENT_WIDTH */
    private static final String PROGRESS_COMPONENT_WIDTH = "progressComponentWidth";

    /** PROGRESS_COMPONENT_HEIGHT */
    private static final String PROGRESS_COMPONENT_HEIGHT = "progressComponentHeight";

	private static final String CSS_CLASS_PROGRESS_BAR_TABLE = "progressBarTable";

    /** DateFomat to create new files */
    DateFormat FILE_DATE_FORMAT = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /** ProgressComponent width - default 300 px */
    private String progressComponentWidth;

    /** ProgressComponent height - default 100 px (applies to the message-box) */
    private String progressComponentHeight;

    /** Constant for state */
    public static final int NO_PROGRESS = 0;

    /** Constant for state */
    public static final int IN_PROGRESS = 1;

    /** Constant for state */
    public static final int FINISHING = 2;

    /** Constant for state */
    public static final int FINISHED = 3;

    /** if scrollUp is true new Messages will appear on top of previous messages. */
    private boolean scrollUp;

    /**
     * if the component is a dialog this will force the component to close when
     * finished
     */
    private boolean closeOnFinish;

    /**
     * if you want to use an own refresh interval instead of the one given from
     * ProgressInfo
     */
    private boolean useIntervalFromProgressProvider;

    /** enables file-logging into a file logMessagePrefixyyyy-dd-mm_hh-MM-ss.log */
    private boolean logMessages;

    /** the refreshInterval to use instead of the one from the ProgressInfo */
    private int refreshInterval;

	/** the state of the component. See {@link #checkState()} */
	int state;

    /** counter for the finish-commands */
	byte finishCount;

    /** the prefix for the logfile */
    private String logFilePrefix;

    /** the suffix of the logfile. Will be set to the current timestamp */
    private String logFileSuffix;

    /** will be set to the system-property lineSeparator */
    private String lineSeparator;

    /**
     * startPosition is set to AJAX_POSITION_START_VALUE or
     * AJAX_POSITION_END_VALUE depending on scrollUp *
     */
	String startPosition;

	BasicAJAXSupport ajaxSupport = new BasicAJAXSupport();

    /**
     * TODO CCA this will break when the component is reused with a different
     * model
     */
    ProgressInfo progressInfo;

    /** Message accumulated from {@link ProgressInfo#getMessage()} for the GUI */
	StringBuffer wholeMessageGUI = new StringBuffer();

    /** Message accumulated from {@link ProgressInfo#getMessage()} for the LogFile */
	StringBuffer wholeMessageFileLog = new StringBuffer();

    /**
	 * @see #getFinishCommands()
	 */
	private final List<CommandHandler> _finishCommands;

    /**
     * is set to true once the process is finished even if the ProgressInfo
     * decides to continue wrongly.
     */
    private boolean alreadyFinished;

    private CommandHandler closeCommand;

    /**
     * Create a new AJAXProgressComponent from XML.
     */
    public AJAXProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        resetLocalVariables();
        resetState();

        lineSeparator = System.getProperty("line.separator");

        scrollUp = atts.getScrollUp();
        startPosition = scrollUp ? AJAXConstants.AJAX_POSITION_START_VALUE
                                : AJAXConstants.AJAX_POSITION_END_VALUE;

        closeOnFinish = atts.getCloseOnFinish();

        logMessages = atts.getLogMessages();
        logFilePrefix = atts.getLogFilePrefix();

        useIntervalFromProgressProvider = atts.getUseIntervallFromProgressProvider();

        refreshInterval = atts.getRefreshInterval();
        if (refreshInterval < 1) {
            refreshInterval = 1;
        } else if (refreshInterval > 60) {
            refreshInterval = 60;
        }

        progressComponentWidth = atts.getProgressComponentWidth();
        progressComponentHeight = atts.getProgressComponentHeight();

		ArrayList<CommandHandler> finishCommands = new ArrayList<>();
		atts.getFinishCommandHandlers().forEach(handlerName -> {
			finishCommands.add(CommandHandlerFactory.getInstance().getHandler(handlerName));
		});
		_finishCommands = Collections.unmodifiableList(finishCommands);

    }

    /**
     * Check our configuration when Layout is correctly set up.
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
        super.componentsResolved(context);

        if (closeOnFinish) {
			CommandHandler cmdch = getCommandById(CloseModalDialogCommandHandler.HANDLER_NAME);
            if (cmdch != null) {
                closeCommand = cmdch;
            }
            else {
                Logger.warn("closeOnFinish is set but this is not a Dialog",
                            this);
            }
        }
    }

    /**
	 * List of the finish-Commands which will be fired after the progress is finished
	 */
	protected List<CommandHandler> getFinishCommands() {
		return _finishCommands;
    }

    /**
     * @see com.top_logic.layout.basic.component.AJAXComponent#ajaxSupport()
     */
    @Override
	protected AJAXSupport ajaxSupport() {
        return ajaxSupport;
    }

    /**
     * Check if model implements ProgressInfo, if it does the model is returned.
     * 
     * Override this method if you can derive a ProgressInfo in some other way.
     * 
     * @return the model if the model implements ProgressInfo, otherwise null
     */
    protected ProgressInfo getProgressInfo() {
        Object theModel = getModel();
        if (theModel instanceof ProgressInfo) {
            return (ProgressInfo) theModel;
        }
        return null;
    }

	/**
	 * Hook for subclasses so they are called when the import is finished.
	 * 
	 * Please call super so {@link #getFinishCommands()} can be executed.
	 * 
	 * @param someContext
	 *        the context in which command was executed
	 * @param someArguments
	 *        the arguments for the command
	 */
    protected void progressFinished(DisplayContext someContext,
            Map someArguments) {
		// nothing to do here
    }

	/**
	 * Writes the body initially / on reload.
	 */
    @Override
	public void writeBody(ServletContext aContext, HttpServletRequest aReq,
            HttpServletResponse aResp, TagWriter aOut) throws IOException,
                                                       ServletException {

        super.writeBody(aContext, aReq, aResp, aOut);

        progressInfo = getProgressInfo();
        checkState();
        if (useIntervalFromProgressProvider) {
            refreshInterval = progressInfo.getRefreshSeconds();
            if (refreshInterval < 1) {
                refreshInterval = 1;
            } else if (refreshInterval > 60) {
                refreshInterval = 60;
            }
        }

        writeTableInit(aOut, Resources.getInstance());
    }

    /**
     * Writes the Progress-Component initially including all the DIV-Tags for
     * the AJAX-Updates. The progressHeader written by writeTableInit is not
     * changed during AJAX-Updates
     * 
     * Also starts the JS setTimeout to call the IncProgressBar-Command
     */
    protected void writeTableInit(TagWriter aOut, Resources res)
                                                                 throws IOException {

        if (logMessages && logFileSuffix == null) {
            logFileSuffix = FILE_DATE_FORMAT.format(new Date());
            Logger.info("Logging progress into file " + logFilePrefix
                        + logFileSuffix, this);
			BasicFileLog.getInstance().appendIntoLogFile(logFilePrefix + logFileSuffix, "Start logging " + new Date().toString() + lineSeparator);
        }

        /*
         * reseting the wholeMessage to null will force the
         * getMessageDelta-Method to send the whole message again after a page
         * reload (in case the whole message is a concatenation of all messages)
         */
        wholeMessageGUI.setLength(0);

		aOut.beginBeginTag(DIV);
		aOut.writeAttribute(ID_ATTR, TIMEOUT_ANCHOR_ID);
		aOut.endBeginTag();
		aOut.endTag(DIV);

        aOut.beginTag("h2");
        aOut.writeContent(res.getString(getResPrefix().key("progressHeader")));
        aOut.endTag("h2");

        // Setzt das DIV für den Fortschrits-Text
        aOut.writeContent(lineSeparator + "<div id=\"" + PROGRESS_TEXT
                          + "\" class=\"" + PROGRESS_TEXT + "\" style=\"width: "
                          + progressComponentWidth + ";\">");
        aOut.writeContent(lineSeparator + "</div>");
       
        // Zeichnet die Tabelle im DIV ProgressTable
        HTMLUtil.beginDiv(aOut, null, PROGRESS_TABLE);
        
        if (progressInfo != null) {
            if (!progressInfo.isFinished()) {

                double progress = progressInfo.getProgress();

				aOut.beginBeginTag(TABLE);
				aOut.writeAttribute(CLASS_ATTR, CSS_CLASS_PROGRESS_BAR_TABLE);
				aOut.writeAttribute(STYLE_ATTR, "width:" + progressComponentWidth);
				aOut.endBeginTag();

                HTMLUtil.beginTr(aOut);

                if (progress == 0.0) {
					aOut.beginBeginTag(TD);
					aOut.writeAttribute(CLASS_ATTR, "progressCellEmpty");
					aOut.writeAttribute(WIDTH_ATTR, "100%");
					aOut.endBeginTag();
					aOut.writeText("0%");
                    HTMLUtil.endTd(aOut);

                }
                else if (progress < 10) {
					aOut.beginBeginTag(TD);
					aOut.writeAttribute(CLASS_ATTR, "progressCellFilled");
					aOut.writeAttribute(WIDTH_ATTR, (int) progress + "%");
					aOut.endBeginTag();
                    HTMLUtil.endTd(aOut);
					aOut.beginBeginTag(TD);
					aOut.writeAttribute(CLASS_ATTR, "progressCellEmpty");
					aOut.writeAttribute(WIDTH_ATTR, (100 - (int) progress) + "%");
					aOut.endBeginTag();
					aOut.writeText((int) progress + "%");
                    HTMLUtil.endTd(aOut);
                }
                else {
					aOut.beginBeginTag(TD);
					aOut.writeAttribute(CLASS_ATTR, "progressCellFilled");
					aOut.writeAttribute(WIDTH_ATTR, (int) progress + "%");
					aOut.endBeginTag();
					aOut.writeText((int) progress + "%");
                    HTMLUtil.endTd(aOut);
					aOut.beginBeginTag(TD);
					aOut.writeAttribute(CLASS_ATTR, "progressCellEmpty");
					aOut.writeAttribute(WIDTH_ATTR, (100 - (int) progress) + "%");
					aOut.endBeginTag();
                    HTMLUtil.endTd(aOut);
                }

                HTMLUtil.endTr(aOut);
                HTMLUtil.endTable(aOut);

            }
            else {
				aOut.beginBeginTag(TABLE);
				aOut.writeAttribute(CLASS_ATTR, CSS_CLASS_PROGRESS_BAR_TABLE);
				aOut.writeAttribute(STYLE_ATTR, "width:" + progressComponentWidth);
				aOut.endBeginTag();
                HTMLUtil.beginTr(aOut);
				aOut.beginBeginTag(TD);
				aOut.writeAttribute(CLASS_ATTR, "progressCellFilled");
				aOut.writeAttribute(WIDTH_ATTR, "100%");
				aOut.endBeginTag();
                HTMLUtil.endTd(aOut);

                HTMLUtil.endTr(aOut);
                HTMLUtil.endTable(aOut);
            }
        }
        
        HTMLUtil.endDiv(aOut);

        // Setzt das DIV Messages für die Meldungen
        aOut.writeContent(lineSeparator + "<div id=\"" + MESSAGES
                          + "\" class=\"" + MESSAGES + "\" style=\"width: "
                          + progressComponentWidth + "; height: "
                          + progressComponentHeight + ";\">");
        aOut.writeContent(lineSeparator + "</div>");

        // Setzt die Timeout-Funktion entsprechend des übergebenen
        // Refresh-Intervalls
        aOut.beginScript();
		aOut.append(getRefreshCommand((refreshInterval * 1000)));
		aOut.append("var clearFunction = function() { window.clearTimeout(domElem.timeout);};\n");
		aOut.append("BAL.addEventListener(domElem, 'DOMNodeRemovedFromDocument', clearFunction);");
        aOut.endScript();
    }

    /**
     * Depending on the current state this method
     * <ul>
     * <li>replaces the content of the DIV-Tags step by step and set a new
     * timeout if the Progress is not yet finished.</li>
     * <li>sends JSSnipplet with the finishing-commands if there are any
     * registered</li>
     * <li>calls the progressFinished-method and reset local variables</li>
     * </ul>
     * 
     * @author cca
     * 
     */
	public static class ProgressReplacement extends AbstractSystemAjaxCommand {

		public static final String COMMAND_NAME = "IncProgressBar";

		public interface Config extends AbstractSystemAjaxCommand.Config {

			@StringDefault(COMMAND_NAME)
			@Override
			String getId();

		}

		public ProgressReplacement(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {

			AJAXProgressComponent progressComponent = (AJAXProgressComponent) aComponent;
			progressComponent.checkState();

			if (progressComponent.getState() == IN_PROGRESS) {
				progressComponent.ajaxSupport.add(new ContentReplacement(PROGRESS_TEXT, new ProgressTextReplacement(
					progressComponent)));
				progressComponent.ajaxSupport
					.add(new ContentReplacement(PROGRESS_TABLE, new ProgressTableReplacement(progressComponent)));
				progressComponent.ajaxSupport.add(new FragmentInsertion(MESSAGES, progressComponent.startPosition,
					new ProgressMessageInsertion(progressComponent)));
				progressComponent.ajaxSupport.add(new JSSnipplet(
					progressComponent.getRefreshCommand((progressComponent.refreshInterval * 1000))));
                
			} else if (progressComponent.getState() == FINISHING) {
				if (progressComponent.finishCount == 0) {
					progressComponent.ajaxSupport.add(new ContentReplacement(PROGRESS_TEXT,
						new ProgressTextReplacement(progressComponent)));
					progressComponent.ajaxSupport.add(new ContentReplacement(PROGRESS_TABLE,
						new ProgressTableReplacement(progressComponent)));
					progressComponent.ajaxSupport.add(new FragmentInsertion(MESSAGES, progressComponent.startPosition,
						new ProgressMessageInsertion(progressComponent)));
                    
                }

				if (progressComponent.finishCount < progressComponent.getFinishCommands().size()) {
					CommandHandler command = progressComponent.getFinishCommands().get(progressComponent.finishCount);
					progressComponent.ajaxSupport.add(new JSSnipplet(command.getID() + "();"));
                }

				progressComponent.finishCount++;
                
				progressComponent.ajaxSupport.add(new JSSnipplet(progressComponent.getRefreshCommand(500)));
                
                
			} else if (progressComponent.getState() == FINISHED) {
                
				if (progressComponent.logMessages) {
					BasicFileLog.getInstance().appendIntoLogFile(progressComponent.logFilePrefix
						+ progressComponent.logFileSuffix, "End logging " + new Date().toString());
                }
                
				if (progressComponent.closeCommand != null) {
					progressComponent.ajaxSupport.add(new JSSnipplet(progressComponent.closeCommand.getID() + "();"));
                }

				progressComponent.progressFinished(aContext, someArguments);
				progressComponent.resetLocalVariables();
            }

			progressComponent.markAsValid();

            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return true;
		}

		protected static final class ProgressMessageInsertion implements HTMLFragment {

			private final AJAXProgressComponent _progressComponent;

			public ProgressMessageInsertion(AJAXProgressComponent progressComponent) {
				_progressComponent = progressComponent;
			}

			@Override
			public void write(DisplayContext context, TagWriter out)
                                                                            throws IOException {
                TagWriter writer = (TagWriter) out;

				if (_progressComponent.progressInfo != null) {
					String currentMessage = _progressComponent.progressInfo.getMessage();
                    
					String currentMessageGUI =
						_progressComponent.getMessageDelta(currentMessage, _progressComponent.wholeMessageGUI);
					String currentMessageFileLog =
						_progressComponent.getMessageDelta(currentMessage, _progressComponent.wholeMessageFileLog);

                    if (!StringServices.isEmpty(currentMessage)) {
						if (_progressComponent.logMessages) {
							BasicFileLog.getInstance().appendIntoLogFile(_progressComponent.logFilePrefix
								+ _progressComponent.logFileSuffix,
                                                       currentMessageFileLog
									+ _progressComponent.lineSeparator);
                        }

                        // Das <DIV> ist notwendig damit der Content auch im IE
                        // angezeigt wird
                        String[] theArray = StringServices
                                .toArray(currentMessageGUI, '\n');
                        if(theArray!=null){
	                        for (int thePos = 0; thePos < theArray.length; thePos++) {
	                            writer.beginTag(HTMLConstants.DIV);
	                            writer
									.writeText(theArray[_progressComponent.scrollUp ? theArray.length
	                                                                   - 1 - thePos
	                                                                : thePos]);
	                            writer.endTag(HTMLConstants.DIV);
	
	                            writer.flush();
	                        }
                        }
                    }
                }
            }
        }

		protected static final class ProgressTextReplacement implements HTMLFragment {

			private final AJAXProgressComponent _progressComponent;

			public ProgressTextReplacement(AJAXProgressComponent progressComponent) {
				_progressComponent = progressComponent;
			}

			@Override
			public void write(DisplayContext context, TagWriter out)
                                                                            throws IOException {
                TagWriter writer = (TagWriter) out;
                // Das <span> ist notwendig damit der Content auch im IE
                // angezeigt wird
				Resources res = context.getResources();
				if (_progressComponent.progressInfo == null) {
                    writer.writeContent("<span align=\"left\">"
						+ res.getString(_progressComponent.getResPrefix().key("progessNotAvailable"))
                                          + "</span>");
                } else {
					float progress = _progressComponent.progressInfo.getProgress();

					if (_progressComponent.progressInfo.isFinished()) {
                        writer
                                .writeContent("<span>"
								+ res.getString(_progressComponent.getResPrefix().key("progressFinished"))
                                              + "</span>");
                    } else {
                        Object params[] = new Object[] {
							Float.valueOf(progress),
							Long.valueOf(_progressComponent.progressInfo
                                                                .getCurrent()),
							Long.valueOf(_progressComponent.progressInfo
                                                                .getExpected()), };
                        writer.writeContent("<span align=\"left\">"
							+ res.getMessage(_progressComponent.getResPrefix().key("progressInfo"), params)
                                            + "</span>");
                    }
                }
            }
        }

		protected static final class ProgressTableReplacement implements HTMLFragment {

			private final AJAXProgressComponent _progressComponent;

			public ProgressTableReplacement(AJAXProgressComponent progressComponent) {
				_progressComponent = progressComponent;
			}

			@Override
			public void write(DisplayContext context, TagWriter out)
                                                                            throws IOException {
                TagWriter writer = (TagWriter) out;

				if (_progressComponent.progressInfo != null) {
					if (!_progressComponent.progressInfo.isFinished()) {

						double progress = _progressComponent.progressInfo.getProgress();
						writer.beginBeginTag(TABLE);
						writer.writeAttribute(CLASS_ATTR, CSS_CLASS_PROGRESS_BAR_TABLE);
						writer.writeAttribute(STYLE_ATTR, "width:" + _progressComponent.progressComponentWidth);
						writer.endBeginTag();
                        HTMLUtil.beginTr(writer);

                        if (progress == 0.0) {
							writer.beginBeginTag(TD);
							writer.writeAttribute(CLASS_ATTR, "progressCellEmpty");
							writer.writeAttribute(WIDTH_ATTR, "100%");
							writer.endBeginTag();
							writer.writeText("0%");
                            HTMLUtil.endTd(writer);

                        }
                        else if (progress < 10) {
							writer.beginBeginTag(TD);
							writer.writeAttribute(CLASS_ATTR, "progressCellFilled");
							writer.writeAttribute(WIDTH_ATTR, (int) progress + "%");
							writer.endBeginTag();
                            HTMLUtil.endTd(writer);
							writer.beginBeginTag(TD);
							writer.writeAttribute(CLASS_ATTR, "progressCellEmpty");
							writer.writeAttribute(WIDTH_ATTR, (100 - (int) progress) + "%");
							writer.endBeginTag();
							writer.writeText((int) progress + "%");
                            HTMLUtil.endTd(writer);
                        }
                        else {
                            if (progress > 100) {
                                progress = 100;
                            }
							writer.beginBeginTag(TD);
							writer.writeAttribute(CLASS_ATTR, "progressCellFilled");
							writer.writeAttribute(WIDTH_ATTR, (int) progress + "%");
							writer.endBeginTag();
							writer.writeText((int) progress + "%");
                            HTMLUtil.endTd(writer);
							writer.beginBeginTag(TD);
							writer.writeAttribute(CLASS_ATTR, "progressCellEmpty");
							writer.writeAttribute(WIDTH_ATTR, (100 - (int) progress) + "%");
							writer.endBeginTag();
                            HTMLUtil.endTd(writer);
                        }

                        HTMLUtil.endTr(writer);
                        HTMLUtil.endTable(writer);

                    }
                    else {
						writer.beginBeginTag(TABLE);
						writer.writeAttribute(CLASS_ATTR, CSS_CLASS_PROGRESS_BAR_TABLE);
						writer.writeAttribute(STYLE_ATTR, "width:" + _progressComponent.progressComponentWidth);
						writer.endBeginTag();
                        HTMLUtil.beginTr(writer);
						writer.beginBeginTag(TD);
						writer.writeAttribute(CLASS_ATTR, "progressCellFilled");
						writer.writeAttribute(WIDTH_ATTR, "100%");
						writer.endBeginTag();
						writer.writeText("100%");
                        HTMLUtil.endTd(writer);

                        HTMLUtil.endTr(writer);
                        HTMLUtil.endTable(writer);
                    }
                }
            }
        }
    }

    /**
	 * Returns the delta of the new ProgressMessage newMessage and the Message already written to
	 * keep the traffic as small as possible.
	 * 
	 * If there is no delta, the whole newMessage will be returned.
	 * 
	 * Several successive messages with exactly the same content will only be returned once because
	 * the delta of two equal strings is empty. Therefore messages should be unique (e.g. containing
	 * a timestamp)
	 * 
	 * empty and null-messages will be returned null.
	 * 
	 * @param newMessage
	 *        current message
	 * @param wholeMessageToUse
	 *        MessageBuffer for GUI or FileLog
	 * @return the difference of the new message to the prior message
	 */
    protected String getMessageDelta(String newMessage, StringBuffer wholeMessageToUse) {
        if (newMessage == null) {
            return newMessage;
        } else if (newMessage.startsWith(wholeMessageToUse.toString())) {
            int i = wholeMessageToUse.length();
            wholeMessageToUse.setLength(0);
            wholeMessageToUse.append(newMessage);
            return (StringServices.isEmpty(newMessage.substring(i))) ? null
                                                                    : newMessage.substring(i);
        }
        wholeMessageToUse.setLength(0);
        wholeMessageToUse.append(newMessage);
        return newMessage;
    }

    

    /**
     * method called after the whole progress is finished to reset the variables
     */
    protected void resetLocalVariables() {
        progressInfo = null;
        finishCount = 0;
        alreadyFinished = false;
        wholeMessageGUI.setLength(0);
        wholeMessageFileLog.setLength(0);
        logFileSuffix = null;
    }

    /**
     * call this to reset the state to NO_PROGRESS
     */
    protected void resetState() {
        state = NO_PROGRESS;
    }

    /**
	 * Checks the current state of the progress. Possible values for state are
	 * <ul>
	 * <li>NO_PROGRESS</li>
	 * <li>IN_PROGRESS</li>
	 * <li>FINISHING</li>
	 * <li>FINISHED</li>
	 * </ul>
	 */
    protected void checkState() {
        if (state == NO_PROGRESS) {
            state = progressInfo != null ? progressInfo.isFinished() ? FINISHING : IN_PROGRESS : NO_PROGRESS;
            return;
        }

        if (state == IN_PROGRESS) {
            alreadyFinished = alreadyFinished || progressInfo.isFinished();
            state = alreadyFinished ? FINISHING : IN_PROGRESS;
            return;
        }

        if (state == FINISHING) {
            state = finishCount < getFinishCommands().size() ? FINISHING
                                                            : FINISHED;
            return;
        }
        if (state == FINISHED) {
            return;
        }

    }

    /**
     * getter for state
     * 
     * @return an int 0, 1, 2 or 3 representing the state
     */
    public int getState() {
        return state;
    }

	String getRefreshCommand(final int refreshInMillis) {
		String domElem = "var domElem = document.getElementById('" + TIMEOUT_ANCHOR_ID + "');\n";
		String setTimeout =
			"domElem.timeout = setTimeout(\"IncProgressBar()\", " + String.valueOf(refreshInMillis) + ");\n";
		return domElem + setTimeout;
	}

	/**
	 * A dummy command handler used as default finish command. This forces the component to write
	 * the last message of progress infos, that were finished before the first reload.
	 * 
	 * @author <a href=mailto:tsa@top-logic.com>tsa</a>
	 */
	public static class WriteFinishedProgressCommand extends AbstractSystemAjaxCommand {

		public static final String COMMAND_NAME = "writeFinishedProgress";

		public interface Config extends AbstractSystemAjaxCommand.Config {

			@StringDefault(COMMAND_NAME)
			@Override
			String getId();

		}

		public WriteFinishedProgressCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordAwaitProgress(aComponent);
			}

            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return true;
		}
    }

	/**
	 * {@link CommandHandler} {@link ProgressInfo#signalStop() stopping} a currently running
	 * progress.
	 */
	public static class SignalStopProgress extends AbstractCommandHandler {

    	/** ID as registered with the CoamnndHAndlerFactory */
		public static final String COMMAND_ID = "signalStop";

		public SignalStopProgress(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext,
				LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			if (model instanceof ProgressInfo) {
				if (!((ProgressInfo) model).signalStop()) {
					Logger.warn("Model of '" + aComponent + "' will not stop", SignalStopProgress.class);
				}
			} else {
				Logger.warn("Model of '" + aComponent + "' is not a ProgressInfo but '" + model,
					SignalStopProgress.class);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

	}

}
