/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * One command per language the application supports, each switching the current user to that
 * language.
 *
 * <p>
 * The set follows the supported locales of {@link ResourcesModule}, and an entry is labelled with
 * the language's own name in that language - "Deutsch", "English" - which is what a reader looking
 * for their language recognises, and needs no translation of its own. The entry for the language
 * currently in effect is offered as disabled, the way {@link ThemeCommands} marks the active theme.
 * </p>
 *
 * <p>
 * Nothing is offered while the session is anonymous: the choice is stored on the account, and an
 * anonymous session has none to store it on. A single supported language likewise yields no
 * entries.
 * </p>
 *
 * @implNote Unlike a theme, a language cannot be applied to the running page: labels are resolved
 *           on the server, so everything already rendered carries the old language. The switch
 *           therefore reloads the page, see {@link #applyLanguage(ReactContext, Locale)}.
 */
@InApp
public class LanguageCommands implements ViewCommandSource {

	/**
	 * Configuration for {@link LanguageCommands}.
	 */
	@TagName("language-commands")
	public interface Config extends ViewCommandSource.Config<LanguageCommands> {

		@Override
		@ClassDefault(LanguageCommands.class)
		Class<? extends LanguageCommands> getImplementationClass();
	}

	/**
	 * Creates a new {@link LanguageCommands} from configuration.
	 */
	@CalledByReflection
	public LanguageCommands(InstantiationContext context, Config config) {
		// No configuration needed: the languages come from the resources module.
	}

	@Override
	public List<CommandModel> getCommands(ViewContext context) {
		List<Locale> supported = ResourcesModule.getInstance().getSupportedLocales();
		if (supported.size() < 2 || TLContext.currentUser() == null) {
			// Nothing to switch to, or nobody to remember the choice for.
			return List.of();
		}

		List<CommandModel> result = new ArrayList<>();
		for (Locale locale : supported) {
			result.add(SimpleCommandModel
				.create(locale.toString(), locale.getDisplayLanguage(locale),
					ctx -> applyLanguage(ctx, locale))
				// Read on every display, since the models outlive a switch.
				.setExecutable(() -> !isActive(locale)));
		}
		return result;
	}

	/**
	 * Whether the given language is the one the session currently renders in.
	 */
	private static boolean isActive(Locale locale) {
		Person account = TLContext.currentUser();
		Locale active = account == null ? null : account.getLanguage();
		return active != null && active.getLanguage().equals(locale.getLanguage());
	}

	/**
	 * Stores the given language as the current user's preference, applies it to the running session
	 * and reloads the page in it.
	 *
	 * @param context
	 *        The context whose update queue carries the reload to the browser.
	 * @param locale
	 *        The language to activate.
	 * @return The result of the activation.
	 */
	public static HandlerResult applyLanguage(ReactContext context, Locale locale) {
		Person account = TLContext.currentUser();
		if (account == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		try (Transaction tx = account.tKnowledgeBase()
			.beginTransaction(I18NConstants.CHANGED_LANGUAGE__USER.fill(account.getName()))) {
			account.setLanguage(locale);
			tx.commit();
		}

		// The session renders in the locale it was entered with, so it has to be told as well.
		TLContext session = TLContext.getContext();
		if (session != null) {
			session.setCurrentLocale(Resources.findBestLocale(account));
		}

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
