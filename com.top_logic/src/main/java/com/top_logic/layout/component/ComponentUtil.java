/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Utilities for handling common problems with persistent objects in components.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentUtil {

	/**
	 * Checks, whether the given object can still be displayed.
	 * 
	 * <p>
	 * If this method returns <code>false</code>, the user should be informed
	 * through an error message produced by
	 * {@link #errorObjectDeleted(DisplayContext)}.
	 * </p>
	 * 
	 * @param obj
	 *        The object to check.
	 * @return Whether the object still can be processed.
	 */
	public static boolean isValid(Object obj) {
		if (obj instanceof Wrapper) {
			return ((Wrapper) obj).tValid();
		} else {
			return true;
		}
	}

	/**
	 * Creates an error result that informs the user about an action performed
	 * on a concurrently deleted object.
	 */
	public static HandlerResult errorObjectDeleted(DisplayContext context) {
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(I18NConstants.ERROR_SELECTED_OBJECT_DELETED);
		return result;
	}

	/**
	 * Whether the given object is from a stable revision.
	 */
	public static boolean isHistoric(Object anObject) {
		return anObject instanceof Wrapper && !WrapperHistoryUtils.isCurrent((Wrapper) anObject);
	}

	/**
	 * The current object for the given one, or <code>null</code>, if the given
	 * stable version was deleted.
	 */
	public static Object getCurrent(Object anObject) {
		return WrapperHistoryUtils.getWrapper(Revision.CURRENT, (Wrapper) anObject);
	}

	/**
	 * Removes all invalid objects from the given selection.
	 * 
	 * @param selection
	 *        A selection of objects (either <code>null</code>, a single object, or a collection of
	 *        objects).
	 * @return The objects from the given selection that are still valid. If the given selection is
	 *         a collection, a collection is returned, null or a single instance, otherwise.
	 */
	public static Object filterValid(Object selection) {
		if (selection instanceof Collection) {
			Collection<?> collection = (Collection<?>) selection;
			if (allValid(collection)) {
				return selection;
			}

			ArrayList<Object> filtered = new ArrayList<>();
			for (Object element : collection) {
				if (isValid(element)) {
					filtered.add(element);
				}
			}
			return filtered;
		} else {
			return isValid(selection) ? selection : null;
		}
	}

	/**
	 * Whether all entries in the given collection are {@link #isValid(Object) valid}.
	 */
	public static boolean allValid(Collection<?> currentSelection) {
		for (Object element : currentSelection) {
			if (!isValid(element)) {
				return false;
			}
		}
		return true;
	}

}
