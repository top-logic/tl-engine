/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.ai.views.source.ViewSource;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Whether a view text works, asked before it is written.
 *
 * <p>
 * The view is read by the engine's own reader with every overlay of the other modules, so a
 * constraint the engine enforces is reported, not restated here; then instantiated, which compiles
 * its TL-Script against the model; then built, attached and rendered once headlessly with the real
 * data, which finds what only shows when the page opens: a channel nobody declared, a view
 * reference that leads nowhere, a script failing on the data.
 * </p>
 */
public final class ViewCheck {

	private static final Pattern LINE = Pattern.compile("line (\\d+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern LOCATION = Pattern.compile("\\S+ line \\d+ column \\d+: ");

	private static final Pattern LOCATION_SUFFIX = Pattern.compile(" at \\S+ line \\d+ column \\d+\\.?");

	private ViewCheck() {
		// Static utility.
	}

	/**
	 * A problem that keeps a view from working.
	 *
	 * @param view
	 *        The view, relative to the views directory.
	 * @param line
	 *        The line in the checked text, 0 when unknown.
	 * @param message
	 *        What is wrong, in the engine's words.
	 */
	public record Finding(String view, int line, String message) {
		@Override
		public String toString() {
			return view + (line > 0 ? ":" + line : "") + ": " + message;
		}
	}

	/**
	 * Checks views as they would be after a change.
	 *
	 * @param candidates
	 *        Resource path to the new text; <code>null</code> for a view the change deletes.
	 * @return What is wrong; empty when every view works.
	 */
	public static List<Finding> check(Map<String, String> candidates) {
		List<Finding> findings = new ArrayList<>();
		for (Map.Entry<String, String> entry : candidates.entrySet()) {
			if (entry.getValue() != null) {
				check(entry.getKey(), entry.getValue(), candidates.keySet(), findings);
			}
		}
		return findings;
	}

	/**
	 * Checks one view as it would be with the given text.
	 */
	public static List<Finding> check(String resource, String text) {
		return check(Collections.singletonMap(resource, text));
	}

	private static void check(String resource, String text, Set<String> together, List<Finding> findings) {
		String view = ViewFiles.relative(resource);
		try {
			ViewSource.parse(text);
		} catch (IllegalArgumentException ex) {
			findings.add(new Finding(view, lineOf(ex.getMessage()), ex.getMessage()));
			return;
		}

		// Read as ViewLoader.parseConfig reads, but into a protocol that keeps every error: the
		// loader stops at the first, and a model repairs a view faster when it learns all of them.
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		ViewElement.Config config = null;
		try {
			ConfigurationReader reader = new ConfigurationReader(context,
				Collections.singletonMap("view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class)));
			reader.setSources(ViewFiles.sourcesWith(resource, text));
			config = (ViewElement.Config) reader.read();
			if (config != null) {
				new ConstraintChecker().check(context, config);
			}
		} catch (Exception ex) {
			log.error(message(ex));
		}
		if (addErrors(view, log, findings) || config == null) {
			return;
		}

		UIElement element = null;
		try {
			element = new DefaultInstantiationContext(log).getInstance(config);
		} catch (RuntimeException ex) {
			log.error(message(ex));
		}
		if (addErrors(view, log, findings) || element == null) {
			return;
		}

		ReactControl control = null;
		try {
			DefaultReactContext react = new HeadlessContext();
			control = (ReactControl) element.createControl(new DefaultViewContext(react, resource));
			control.attach();
			control.stateAsJSON();
		} catch (RuntimeException | LinkageError | AssertionError ex) {
			String message = message(ex);
			Logger.info("Rendering " + view + " failed: " + message, ViewCheck.class);
			if (together.stream().anyMatch(other -> !other.equals(resource) && message.contains(ViewFiles.relative(other)))) {
				// It shows another view of the same change, which is only written with it.
				return;
			}
			findings.add(new Finding(view, lineOf(message), "The page fails when it opens: " + clean(message)));
		} finally {
			if (control != null) {
				try {
					control.detach();
					control.cleanupTree();
				} catch (RuntimeException ex) {
					// Only a throw-away tree.
				}
			}
		}
	}

	/**
	 * A window for a page nobody sees: what the view servlet's window frame provides, without a
	 * browser behind it. An app bar's menu needs the frame's context-menu opener to be built at all.
	 */
	private static final class HeadlessContext extends DefaultReactContext {

		private final ContextMenuOpener _menus = new ContextMenuOpener(new ContextMenuOpener.MenuRenderer() {
			@Override
			public void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
					Runnable closeHandler) {
				// Nobody sees the page.
			}

			@Override
			public void hide() {
				// Nobody sees the page.
			}
		});

		HeadlessContext() {
			super("", "view-check", new SSEUpdateQueue(), new ReactWindowRegistry("view-check"));
		}

		@Override
		public ContextMenuOpener getContextMenuOpener() {
			return _menus;
		}
	}

	private static boolean addErrors(String view, BufferingProtocol log, List<Finding> findings) {
		Set<String> reported = new HashSet<>();
		for (String error : log.getErrors()) {
			// The reader reports an error, then again as part of its summary "Errors in context: ...".
			String message = clean(error).replaceFirst("^Errors in context: ", "");
			if (reported.add(message)) {
				findings.add(new Finding(view, lineOf(error), message));
			}
		}
		return log.hasErrors();
	}

	private static int lineOf(String message) {
		Matcher matcher = LINE.matcher(message == null ? "" : message);
		int line = 0;
		while (matcher.find()) {
			line = Integer.parseInt(matcher.group(1));
		}
		return line;
	}

	/** The engine's message without the location prefix it repeats and without a stack trace. */
	private static String clean(String message) {
		String text = message.strip();
		Matcher matcher = LOCATION.matcher(text);
		int located = -1;
		while (matcher.find()) {
			located = matcher.end();
		}
		if (located > 0 && located < text.length()) {
			text = text.substring(located);
		}
		text = LOCATION_SUFFIX.matcher(text).replaceAll("");
		int trace = text.indexOf("\n\tat ");
		if (trace > 0) {
			text = text.substring(0, trace);
		}
		return text.length() > 600 ? text.substring(0, 600) + " ..." : text;
	}

	/** Every message of a cause chain, once. */
	private static String message(Throwable ex) {
		StringBuilder out = new StringBuilder();
		for (Throwable t = ex; t != null; t = t.getCause() == t ? null : t.getCause()) {
			String message = t.getMessage();
			if (message != null && out.indexOf(message) < 0) {
				out.append(out.length() > 0 ? " Cause: " : "").append(message);
			}
		}
		return out.length() == 0 ? ex.getClass().getSimpleName() : out.toString();
	}
}
