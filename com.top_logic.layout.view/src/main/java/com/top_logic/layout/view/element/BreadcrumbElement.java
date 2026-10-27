/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.nav.ReactBreadcrumbControl;
import com.top_logic.layout.react.control.nav.ReactBreadcrumbControl.BreadcrumbEntry;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.InputsOf;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.Resources;

/**
 * Declarative {@link UIElement} rendering a navigation trail over the list of places held by a
 * channel.
 *
 * <p>
 * The {@link Config#getPath() path} channel holds the places from the outermost one to the current
 * one, which is the last entry. Every entry before the current one is a link: clicking it writes the
 * path up to that entry back to the channel, so whatever shows the place of the path follows by
 * listening on the same channel.
 * </p>
 *
 * <pre>
 * &lt;breadcrumb path="location" label="$entry.get(`my:Folder#name`)"/&gt;
 * </pre>
 */
@InApp
public class BreadcrumbElement implements UIElement {

	/**
	 * Configuration for {@link BreadcrumbElement}.
	 */
	@TagName("breadcrumb")
	@TypeIcon("css:bi bi-chevron-double-right")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Name of the variable the {@link #getLabel() label} sees an entry of the path as. */
		String ENTRY = "entry";

		@Override
		@ClassDefault(BreadcrumbElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel holding the path: the list of places from the outermost one to
		 * the current one.
		 */
		@Name(PATH)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getPath();

		/**
		 * TL-Script expression computing the text of an entry of the path.
		 *
		 * <p>
		 * The expression sees the entry as the variable {@value #ENTRY}. Unset, or where the
		 * expression yields nothing, the entry shows its own label.
		 * </p>
		 */
		@Name(LABEL)
		@Supplies(ENTRY)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();
	}

	private final Config _config;

	private final QueryExecutor _label;

	/**
	 * Creates a new {@link BreadcrumbElement} from configuration.
	 */
	@CalledByReflection
	public BreadcrumbElement(InstantiationContext context, Config config) {
		_config = config;
		_label = InputScript.compile(config, Config.LABEL);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel pathChannel = context.resolveChannel(_config.getPath());

		ReactBreadcrumbControl control = new ReactBreadcrumbControl(context, entries(pathChannel.get()),
			id -> navigate(pathChannel, id));
		control.setCssClass(_config.getCssClass());

		Registration registration =
			pathChannel.addListener((sender, oldValue, newValue) -> control.updateItems(entries(newValue)));
		control.addCleanupAction(registration::dispose);
		return control;
	}

	/**
	 * The entries of the trail for the given path, each identified by its index in the path.
	 */
	private List<BreadcrumbEntry> entries(Object path) {
		List<?> places = path instanceof List<?> list ? list : List.of();
		List<BreadcrumbEntry> result = new ArrayList<>(places.size());
		for (int index = 0; index < places.size(); index++) {
			result.add(new BreadcrumbEntry(Integer.toString(index), label(places.get(index))));
		}
		return result;
	}

	private String label(Object place) {
		if (_label != null && place != null) {
			Object result = _label.execute(place);
			if (result != null) {
				return result instanceof ResKey key ? Resources.getInstance().getString(key)
					: SearchExpression.asString(result);
			}
		}
		return MetaLabelProvider.INSTANCE.getLabel(place);
	}

	/**
	 * Goes back to the entry with the given id: the path up to that entry is written to the
	 * channel.
	 */
	private static void navigate(ViewChannel pathChannel, String id) {
		int index;
		try {
			index = Integer.parseInt(id);
		} catch (NumberFormatException ex) {
			Logger.warn("Not an entry of the breadcrumb: " + id, BreadcrumbElement.class);
			return;
		}
		List<?> path = pathChannel.get() instanceof List<?> list ? list : List.of();
		if (index < 0 || index >= path.size() - 1) {
			// Not an entry before the current one.
			return;
		}
		pathChannel.set(Collections.unmodifiableList(new ArrayList<>(path.subList(0, index + 1))));
	}
}
