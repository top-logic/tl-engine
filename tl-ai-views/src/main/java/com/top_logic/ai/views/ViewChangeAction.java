/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Decides on the view change set it is given: applies, rejects or reverts it in a transaction of
 * its own, and shows the written views in the window the command was run in.
 *
 * <pre>
 * &lt;generic-command input="selectedSet"&gt;
 *   ...
 *   &lt;view-change action="APPLY"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
@InApp
public class ViewChangeAction implements ViewAction {

	/**
	 * What {@link ViewChangeAction} does.
	 */
	public enum Decision {
		/** {@link ViewChanges#apply(TLObject)} */
		APPLY,
		/** {@link ViewChanges#reject(TLObject)} */
		REJECT,
		/** {@link ViewChanges#revert(TLObject)} */
		REVERT;
	}

	/**
	 * Configuration of {@link ViewChangeAction}.
	 */
	@TagName("view-change")
	public interface Config extends PolymorphicConfiguration<ViewChangeAction> {

		/** Configuration name of {@link #getAction()}. */
		String ACTION = "action";

		@Override
		@ClassDefault(ViewChangeAction.class)
		Class<? extends ViewChangeAction> getImplementationClass();

		/**
		 * What is done with the change set.
		 */
		@Name(ACTION)
		@Mandatory
		Decision getAction();
	}

	private final Decision _decision;

	/**
	 * Creates a {@link ViewChangeAction} from configuration.
	 */
	@CalledByReflection
	public ViewChangeAction(InstantiationContext context, Config config) {
		_decision = config.getAction();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject set)) {
			return input;
		}
		List<String> written;
		try {
			written = KBUtils.inTransaction(() -> switch (_decision) {
				case APPLY -> ViewChanges.apply(set);
				case REVERT -> ViewChanges.revert(set);
				case REJECT -> {
					ViewChanges.reject(set);
					yield List.<String> of();
				}
			});
		} catch (IllegalArgumentException ex) {
			throw new TopLogicException(ResKey.text(ex.getMessage()));
		}
		if (!written.isEmpty()) {
			WindowPage.rebuild(context.getSSEQueue().getRootControl(), new LinkedHashSet<>(written));
		}
		return set;
	}
}
