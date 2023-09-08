/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.shared.string.StringServicesShared.isEmpty;
import static com.top_logic.layout.tree.model.TLTreeModelUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.LocationImpl;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.component.ComponentNamingScheme;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.util.ActionResourceProvider;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Utility methods for {@link ApplicationActionOp} or for working with {@link ApplicationAction}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ActionUtil {

	/** Separator between the comments of a parent action and its child action. */
	public static final String COMMENT_SEPARATOR = " > ";

	/** Separator between the failure-messages of a parent action and its child action. */
	public static final String FAILURE_MESSAGE_SEPARATOR = "; Cause: ";

	/**
	 * The instance of the {@link ActionUtil}. This is not a singleton, as (potential) subclasses
	 * can create further instances.
	 */
	public static final ActionUtil INSTANCE = new ActionUtil();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected ActionUtil() {
		// See JavaDoc above.
	}

	/**
	 * Searches the {@link LayoutComponent} with the given name.
	 */
	public static LayoutComponent getComponentByName(ComponentName componentName, MainLayout mainLayout, ApplicationAction action) {
		if (componentName == null || Utils.equals(mainLayout.getName(), componentName)) {
			return mainLayout;
		} else {
			return ComponentNamingScheme.locateComponent(action, mainLayout, componentName);
		}

	}

	/**
	 * Propagates the {@link ApplicationAction#getComment()} and
	 * {@link ApplicationAction#getFailureMessage()} from the {@link ActionChain} to its direct
	 * children.
	 * <p>
	 * Does not propagate recursive.
	 * </p>
	 * 
	 * @param action
	 *        Must not be <code>null</code>.
	 */
	public static void propagateDebugInfo(ActionChain action) {
		List<ApplicationAction> children = action.getActions();
		propagateDebugInfo(action, children);
	}

	/**
	 * Propagates the {@link ApplicationAction#getComment()} and
	 * {@link ApplicationAction#getFailureMessage()} from the given {@link DynamicAction} to the
	 * given list of actions.
	 * <p>
	 * Does not propagate recursive.
	 * </p>
	 * 
	 * @param action
	 *        Must not be <code>null</code>.
	 * @param targets
	 *        The actions to update.
	 */
	public static void propagateDebugInfo(ApplicationAction action, List<ApplicationAction> targets) {
		propagateComment(action, targets);
		propagateFailureMessage(action, targets);
	}

	private static void propagateComment(ApplicationAction parent,
			Collection<? extends ApplicationAction> children) {
		if (isEmpty(parent.getComment())) {
			return;
		}
		for (ApplicationAction child : children) {
			if (isEmpty(child.getComment())) {
				child.setComment(parent.getComment());
			} else {
				child.setComment(parent.getComment() + COMMENT_SEPARATOR + child.getComment());
			}
		}
	}

	private static void propagateFailureMessage(ApplicationAction parent,
			Collection<? extends ApplicationAction> children) {
		if (isEmpty(parent.getFailureMessage())) {
			return;
		}
		for (ApplicationAction child : children) {
			if (isEmpty(child.getFailureMessage())) {
				child.setFailureMessage(parent.getFailureMessage());
			} else {
				child.setFailureMessage(
					parent.getFailureMessage() + FAILURE_MESSAGE_SEPARATOR + child.getFailureMessage());
			}
		}
	}

	/**
	 * Returns a {@link String} containing the {@link ApplicationAction} itself, the given
	 * {@link ApplicationAction#getComment() comment} and
	 * {@link ApplicationAction#getFailureMessage() failure message}, the
	 * {@link Throwable#getMessage()} and the {@link Location}.
	 */
	public static ApplicationAssertion enhanceThrowable(
			ApplicationAction action, Throwable ex, String comment, String failureMessage) {
		String enhancedMessage = enhanceMessage(ex, action, comment, failureMessage);
		return new ApplicationAssertion(enhancedMessage, ex);
	}

	/**
	 * Returns a {@link String} containing the {@link ApplicationAction} itself, its
	 * {@link ApplicationAction#getComment() comment} and
	 * {@link ApplicationAction#getFailureMessage() failure message}, the
	 * {@link Throwable#getMessage()} and the {@link Location}.
	 */
	public static String enhanceMessage(Throwable ex, ApplicationAction action) {
		return enhanceMessage(ex, action, actionName(action), action.getFailureMessage());
	}

	/**
	 * Readable name of the given {@link ApplicationAction}.
	 */
	public static String actionName(ApplicationAction action) {
		String comment = nonEmpty(action.getComment());
		if (comment != null) {
			return comment;
		} else {
			return ActionResourceProvider.newInstance().getLabel(action);
		}
	}

	/**
	 * Concatenates the {@link ApplicationAction#getComment()}s from the parent actions, including
	 * the given action itself.
	 */
	public static String joinParentComments(TLTreeNode<?> failedAction) {
		List<String> comments = getParentComments(failedAction);
		// From ascending to descending order.
		Collections.reverse(comments);
		return StringServices.join(comments, ActionUtil.COMMENT_SEPARATOR);
	}

	private static List<String> getParentComments(TLTreeNode<?> failedAction) {
		List<String> comments = new ArrayList<>();
		List<ApplicationAction> actionHierarchy = createPathToRootUserObject(failedAction, ApplicationAction.class);
		ResourceProvider resourceProvider = ActionResourceProvider.newInstance();
		for (ApplicationAction ancestor : actionHierarchy) {
			String comment = ancestor.getComment();
			if (StringServices.isEmpty(comment)) {
				comments.add(resourceProvider.getLabel(ancestor));
			} else {
				comments.add(comment);
			}
		}
		return comments;
	}

	/**
	 * Concatenates the {@link ApplicationAction#getFailureMessage()}s from the parent actions,
	 * including the given action itself.
	 */
	public static String joinParentFailureMessages(TLTreeNode<?> failedAction) {
		List<String> failureMessages = getParentFailureMessages(failedAction);
		// From ascending to descending order.
		Collections.reverse(failureMessages);
		return StringServices.join(failureMessages, ActionUtil.FAILURE_MESSAGE_SEPARATOR);
	}

	private static List<String> getParentFailureMessages(TLTreeNode<?> failedAction) {
		List<String> failureMessages = new ArrayList<>();
		List<ApplicationAction> actionHierarchy = createPathToRootUserObject(failedAction, ApplicationAction.class);
		for (ApplicationAction ancestor : actionHierarchy) {
			String failureMessage = ancestor.getFailureMessage();
			if (!StringServices.isEmpty(failureMessage)) {
				failureMessages.add(failureMessage);
			}
		}
		return failureMessages;
	}

	private static String enhanceMessage(Throwable ex, ConfigurationItem config, String comment,
			String failureMessage) {
		String message = getMessage(ex);
		List<String> parts = Arrays.asList(message.split("\\n"));

		StringBuilder result = new StringBuilder();

		if (!StringServices.isEmpty(failureMessage) && !parts.contains(failureMessage)) {
			result.append(failureMessage);
			result.append("\n");
		}

		result.append(message);

		if (!StringServices.isEmpty(comment)) {
			String actionName = "Action: " + comment;
			if (!parts.contains(actionName)) {
				result.append("\n");
				result.append(actionName);
			}
		}

		Location location = config.location();
		if (!LocationImpl.NONE.equals(location)) {
			String locationLine = "At " + location;
			if (!parts.contains(locationLine)) {
				result.append("\n");
				result.append(locationLine);
			}
		}

		return result.toString();
	}

	private static String getMessage(Throwable exception) {
		if (exception.getMessage() == null) {
			return exception.getClass().getName();
		}
		return exception.getMessage();
	}

	private static String removeSentenceEnd(String actionComment) {
		Pattern p = Pattern.compile(".*([\\.\\!\\:\\;]\\s*)");
		Matcher m = p.matcher(actionComment);
		if (m.matches()) {
			actionComment = actionComment.substring(0, actionComment.length() - m.group(1).length());
		}
		return actionComment;
	}

}
