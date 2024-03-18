/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.infoservice;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Central service, to present messages in info area.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class InfoService {
	
	private static final Property<Boolean> ERROR_DISPLAYED = TypedAnnotatable.property(Boolean.class, "errorDisplayed");

	/** Class for the case when it is an information message. */
	public static final String INFO_CSS = "tl-info-service-item__info";

	/** Class for the case when it is a warning message. */
	public static final String WARNING_CSS = "tl-info-service-item__warning";

	/** Class for the case when it is an error message. */
	public static final String ERROR_CSS = "tl-info-service-item__error";

	/** Configuration for the {@link InfoService} */
	public interface Config extends ConfigurationItem {

		/**
		 * seconds, until info service will be fade out, if it has not been pinned.
		 */
		@IntDefault(6)
		@Name("fade-out-timer-seconds")
		int getFadeOutTimerSeconds();
	}

	/**
	 * Property, which refers to list of entries, that shall be displayed at GUI.
	 */
	@SuppressWarnings("rawtypes")
	public static final Property<List> INFO_SERVICE_ENTRIES =
		TypedAnnotatable.property(List.class, "infoServiceEntries");

	/**
	 * Sends the given {@link Throwable} as error to {@link Logger} and the given {@link ResKey} to
	 * {@link InfoService}. Thereby only the very first call of this method will produce any output.
	 */
	public static void logError(DisplayContext context, ResKey userErrorMessage,
			Throwable throwable, Object caller) {
		logError(context, userErrorMessage, throwable.getMessage(), throwable, caller);
	}

	/**
	 * Shows a given user message as error in info area and also writes the error to log file.
	 */
	public static void logError(ResKey userMessage, String logMessage, Throwable throwable, Object caller) {
		logError(DefaultDisplayContext.getDisplayContext(), userMessage, logMessage, throwable, caller);
	}

	/**
	 * Sends the given log message with the given optional {@link Throwable} as error to
	 * {@link Logger} and the given {@link ResKey} to {@link InfoService}. Thereby only the very
	 * first call of this method will produce any output.
	 */
	public static void logError(DisplayContext context, ResKey userMessage, String logMessage,
			Throwable throwable, Object caller) {
		if (!context.isSet(InfoService.ERROR_DISPLAYED)) {
			String errorDetail = errorDetail(logMessage, throwable);
			Logger.error(errorDetail, throwable, caller);

			showError(new InfoServiceItemMessageFragment(userMessage, ResKey.text(errorDetail)));
			context.set(InfoService.ERROR_DISPLAYED, true);
		}
	}

	/**
	 * Given log message appended with details from the given optional {@link Throwable}.
	 * 
	 * @param logMessage
	 *        The log message to extend.
	 * @param throwable
	 *        Optional {@link Throwable}.
	 * @return Extended log message.
	 */
	public static String errorDetail(String logMessage, Throwable throwable) {
		String errorDetail;
		if (throwable == null) {
			errorDetail = logMessage;
		} else {
			String exceptionMessage = throwable.getMessage();
			if (Utils.equals(exceptionMessage, logMessage)) {
				errorDetail = logMessage;
			} else {
				errorDetail =
					logMessage + " (" + throwable.getClass().getName()
						+ (exceptionMessage == null ? "" : ": " + exceptionMessage) + ")";
			}
		}
		return errorDetail;
	}

	/**
	 * Shows given messages as error in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the error reason
	 * @param detailMessage
	 *        - optional message, that describes some background information.
	 */
	public static void showError(ResKey summaryMessage, HTMLFragment detailMessage) {
		showError(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as error in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the error reason
	 * @param detailMessage
	 *        - optional message, that describes some background information, maybe
	 *        {@link ResKey#NONE}.
	 */
	public static void showError(ResKey summaryMessage, ResKey detailMessage) {
		showError(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as error in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the error reason
	 */
	public static void showError(ResKey summaryMessage) {
		showError(summaryMessage, ResKey.NONE);
	}

	/**
	 * Shows given {@link HTMLFragment} as error in info area.
	 * 
	 * @param messageFragment
	 *        - fragment, that describes the error reason
	 */
	public static void showError(HTMLFragment messageFragment) {
		show(new DefaultInfoServiceItem(Icons.INFOSERVICE_ERROR, I18NConstants.ERROR_MESSAGE_HEADER, messageFragment,
			ERROR_CSS));
	}

	/**
	 * Shows given messages as list of errors in info area.
	 * 
	 * @param messages
	 *        Messages describing the error reasons
	 */
	public static void showErrorList(List<ResKey> messages) {
		showErrorList(null, messages);
	}

	/**
	 * Shows given messages as list of errors in info area.
	 * @param description
	 *        Message shown above the list. Maybe <code>null</code> if no description should be
	 *        shown.
	 * @param messages
	 *        Messages describing the error reasons
	 */
	public static void showErrorList(ResKey description, List<ResKey> messages) {
		InfoService.showError(getMessageList(messages, description));
	}

	/**
	 * Shows given messages as warning in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the warning reason
	 * @param detailMessage
	 *        - optional message, that describes some background information.
	 */
	public static void showWarning(ResKey summaryMessage, HTMLFragment detailMessage) {
		showWarning(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as warning in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the warning reason
	 * @param detailMessage
	 *        - optional message, that describes some background information, maybe
	 *        {@link ResKey#NONE}.
	 */
	public static void showWarning(ResKey summaryMessage, ResKey detailMessage) {
		showWarning(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as warning in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the warning reason
	 */
	public static void showWarning(ResKey summaryMessage) {
		showWarning(summaryMessage, ResKey.NONE);
	}

	/**
	 * Shows given {@link HTMLFragment} as warning in info area.
	 * 
	 * @param messageFragment
	 *        - fragment, that describes the warning reason
	 */
	public static void showWarning(HTMLFragment messageFragment) {
		show(
			new DefaultInfoServiceItem(Icons.INFOSERVICE_WARNING, I18NConstants.WARNING_MESSAGE_HEADER, messageFragment,
			WARNING_CSS));
	}

	/**
	 * Shows given messages as list of warnings in info area.
	 * 
	 * @param messages
	 *        Messages describing the warning reasons
	 */
	public static void showWarningList(List<ResKey> messages) {
		showWarningList(null, messages);
	}

	/**
	 * Shows given messages as list of warnings in info area.
	 * @param description
	 *        Message shown above the list. Maybe <code>null</code> if no description should be
	 *        shown.
	 * @param messages
	 *        Messages describing the warning reasons
	 */
	public static void showWarningList(ResKey description, List<ResKey> messages) {
		InfoService.showWarning(getMessageList(messages, description));
	}

	/**
	 * Shows given messages as info in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the info reason
	 * @param detailMessage
	 *        - optional message, that describes some background information
	 */
	public static void showInfo(ResKey summaryMessage, HTMLFragment detailMessage) {
		showInfo(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as info in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the info reason
	 * @param detailMessage
	 *        - optional message, that describes some background information, maybe
	 *        {@link ResKey#NONE}.
	 */
	public static void showInfo(ResKey summaryMessage, ResKey detailMessage) {
		showInfo(new InfoServiceItemMessageFragment(summaryMessage, detailMessage));
	}

	/**
	 * Shows given messages as info in info area.
	 * 
	 * @param summaryMessage
	 *        - message, that describes the info reason
	 */
	public static void showInfo(ResKey summaryMessage) {
		showInfo(summaryMessage, ResKey.NONE);
	}

	/**
	 * Shows given {@link HTMLFragment} as info in info area.
	 * 
	 * @param messageFragment
	 *        - fragment, that describes the info reason
	 */
	public static void showInfo(HTMLFragment messageFragment) {
		show(new DefaultInfoServiceItem(Icons.INFOSERVICE_INFO, I18NConstants.INFO_MESSAGE_HEADER, messageFragment,
			INFO_CSS));
	}

	/**
	 * Shows given messages as list of infos in info area.
	 * 
	 * @param messages
	 *        Messages describing the info reasons
	 */
	public static void showInfoList(List<ResKey> messages) {
		showInfoList(null, messages);
	}

	/**
	 * Shows given messages as list of infos in info area.
	 * @param description
	 *        Message shown above the list. Maybe <code>null</code> if no description should be
	 *        shown.
	 * @param messages
	 *        Messages describing the info reasons
	 */
	public static void showInfoList(ResKey description, List<ResKey> messages) {
		InfoService.showInfo(getMessageList(messages, description));
	}

	/**
	 * Shows a {@link HTMLFragment} as item in info area.
	 */
	public static void show(HTMLFragment infoItem) {
		if (!DefaultDisplayContext.hasDisplayContext()) {
			return;
		}
		getLogEntries().add(infoItem);
	}

	@SuppressWarnings("unchecked")
	private static List<HTMLFragment> getLogEntries() {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		if (!displayContext.isSet(INFO_SERVICE_ENTRIES)) {
			displayContext.set(INFO_SERVICE_ENTRIES, new ArrayList<>());
		}
		List<HTMLFragment> logEntries = displayContext.get(INFO_SERVICE_ENTRIES);
		return logEntries;
	}

	/**
	 * {@link HTMLFragment} displaying the message of the given {@link I18NFailure} and all causes.
	 *
	 * @see #messages(ResKey, Throwable)
	 */
	public static HTMLFragment messages(I18NFailure ex) {
		Throwable cause = ((Throwable) ex).getCause();
		ResKey summaryKey = ex.getErrorKey();
		if (cause == null) {
			return new InfoServiceItemMessageFragment(summaryKey, ResKey.NONE);
		} else {
			return messages(summaryKey, cause);
		}
	}

	/**
	 * {@link HTMLFragment} displaying the given summary key and the messages of the given
	 * {@link Throwable} and all causes.
	 * 
	 * <p>
	 * The returned {@link HTMLFragment} displays the given key as summary and as detail an
	 * {@link HTMLConstants#UL unordered} list containing the messages of the given
	 * {@link Throwable} and all causes.
	 * </p>
	 * 
	 * @param summaryKey
	 *        The summary of the message
	 * @param ex
	 *        The {@link Throwable} to get actual messages from.
	 * 
	 * @see InfoService#show(HTMLFragment)
	 */
	public static HTMLFragment messages(ResKey summaryKey, Throwable ex) {
		HTMLFragment detail = new HTMLFragment() {
			
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.beginTag(HTMLConstants.UL);
				Throwable cause = ex;
				do {
					out.beginTag(HTMLConstants.LI);
					String message;
					if (cause instanceof I18NFailure) {
						message = context.getResources().getString(((I18NFailure) cause).getErrorKey());
					} else {
						message = cause.getLocalizedMessage();
					}
					out.writeText(message);
					out.endTag(HTMLConstants.LI);
					cause = cause.getCause();
				}
				while (cause != null);
				out.endTag(HTMLConstants.UL);
			}
		};

		return new InfoServiceItemMessageFragment(summaryKey, detail);
	}

	/**
	 * List of {@link ResKey} messages with a describing text above.
	 * 
	 * @param messages
	 *        Messages to be shown in the list
	 * @param description
	 *        Message shown above the list. Maybe <code>null</code> if no description should be
	 *        shown.
	 * @return {@link HTMLFragment} containing a describing text and a list of messages
	 */
	private static HTMLFragment getMessageList(List<ResKey> messages, ResKey description) {
		HTMLFragment list = messageList(messages);
		if (description != null) {
			HTMLFragment text = p(message(description));
			return div(text, list);
		} else {
			return list;
		}
	}

}
