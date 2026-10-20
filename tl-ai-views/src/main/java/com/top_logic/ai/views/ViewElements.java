/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.top_logic.ai.views.source.ViewSource;
import com.top_logic.ai.views.source.ViewSource.Element;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewExecutabilityRule;

/**
 * The tags a view of this application can use, read from the running application.
 *
 * <p>
 * The view layer has no schema; it has the parts of one. Every element registers its tag with the
 * {@link TypeIndex}, its configuration describes its properties, a list property names the tags it
 * accepts, and the documentation of a property ships as its tooltip. This puts them together - for
 * the engine's elements and for the application's own, which no documentation lists.
 * </p>
 */
public final class ViewElements {

	private static final int MAX_EXAMPLE_LINES = 40;

	private static Map<String, ConfigurationDescriptor> _tags;

	private ViewElements() {
		// Static utility.
	}

	/**
	 * Every element, command and command rule a view can name, one line per kind.
	 */
	public static String summary() {
		TypeIndex index = TypeIndex.getInstance();
		return "Elements: " + String.join(", ", new TreeSet<>(index.configTagNames(UIElement.Config.class).keySet()))
			+ "\nCommands: " + String.join(", ", new TreeSet<>(index.configTagNames(ViewCommand.Config.class).keySet()))
			+ "\nCommand rules: "
			+ String.join(", ", new TreeSet<>(index.configTagNames(ViewExecutabilityRule.Config.class).keySet()));
	}

	/**
	 * One tag: what it is, its properties with their types and the tags each list takes, and how
	 * this application's views use it.
	 *
	 * @return The description, or <code>null</code> for a tag no view can name.
	 */
	public static String describe(String tag) {
		ConfigurationDescriptor descriptor = tags().get(tag);
		if (descriptor == null) {
			return null;
		}
		String type = descriptor.getConfigurationInterface().getName();
		StringBuilder out = new StringBuilder("<").append(tag).append(">");
		String about = text(ResKey.internalCreate(type.replace('$', '.') + ".tooltip"));
		if (about.isEmpty() && descriptor.getConfigurationInterface().getEnclosingClass() != null) {
			about = text(ResKey.internalCreate(descriptor.getConfigurationInterface().getEnclosingClass().getName() + ".tooltip"));
		}
		out.append(about.isEmpty() ? "" : ": " + about).append("\nProperties:\n");
		for (PropertyDescriptor property : descriptor.getProperties()) {
			String name = property.getPropertyName();
			if (name.equals("class") || name.equals("implementation-class") || property.isAbstract()) {
				continue;
			}
			out.append("- ").append(name).append(" (").append(property.kind().name().toLowerCase(Locale.ROOT))
				.append(' ').append(property.getType().getSimpleName()).append(')');
			if (property.isMandatory()) {
				out.append(" required");
			}
			Set<String> children = property.getElementNames();
			if (children != null) {
				TreeSet<String> tags = new TreeSet<>();
				children.stream().filter(Objects::nonNull).forEach(tags::add);
				if (!tags.isEmpty()) {
					out.append(" takes <").append(String.join(">, <", tags)).append('>');
				}
			}
			String doc = text(property.labelKey(null).tooltip());
			out.append(doc.isEmpty() ? "" : ": " + doc).append('\n');
		}
		String example = example(tag);
		if (example != null) {
			out.append("Example from this application:\n").append(example).append('\n');
		}
		return out.toString();
	}

	/** Every tag a view can name, the parts inside elements (columns, tabs, ...) included. */
	private static synchronized Map<String, ConfigurationDescriptor> tags() {
		if (_tags == null) {
			Map<String, ConfigurationDescriptor> tags = new TreeMap<>();
			Deque<ConfigurationDescriptor> pending = new ArrayDeque<>();
			TypeIndex index = TypeIndex.getInstance();
			for (Class<?> kind : List.of(UIElement.Config.class, ViewCommand.Config.class,
				ViewExecutabilityRule.Config.class)) {
				for (Map.Entry<String, Class<?>> entry : index.configTagNames(kind).entrySet()) {
					ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(entry.getValue());
					if (tags.putIfAbsent(entry.getKey(), descriptor) == null) {
						pending.push(descriptor);
					}
				}
			}
			while (!pending.isEmpty()) {
				for (PropertyDescriptor property : pending.pop().getProperties()) {
					Set<String> names = property.getElementNames();
					for (String name : names == null ? Set.<String> of() : names) {
						ConfigurationDescriptor child = name == null ? null : property.getElementDescriptor(name);
						if (child != null && tags.putIfAbsent(name, child) == null) {
							pending.push(child);
						}
					}
				}
			}
			_tags = tags;
		}
		return _tags;
	}

	/** The first use of a tag in the application's views, with the view it is from. */
	private static String example(String tag) {
		for (String view : ViewFiles.list()) {
			try {
				String text = ViewFiles.read(ViewFiles.resource(view));
				if (text == null || !text.contains("<" + tag)) {
					continue;
				}
				Element found = first(ViewSource.parse(text).root(), tag);
				if (found != null) {
					String xml = text.substring(found.start(), found.end());
					List<String> lines = xml.lines().toList();
					return "<!-- " + view + " -->\n" + (lines.size() > MAX_EXAMPLE_LINES
						? String.join("\n", lines.subList(0, MAX_EXAMPLE_LINES)) + "\n..." : xml);
				}
			} catch (RuntimeException ex) {
				// A view that cannot be read has no example to give.
			}
		}
		return null;
	}

	private static Element first(Element element, String tag) {
		if (element.tag().equals(tag)) {
			return element;
		}
		for (Element child : element.children()) {
			Element found = first(child, tag);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/** The English text of a documentation key as plain text; "" when there is none. */
	private static String text(ResKey key) {
		String value = ResourcesModule.getInstance().getBundle(Locale.ENGLISH).getString(key, null);
		return value == null ? "" : ViewSource.unescape(value.replaceAll("<[^>]+>", "")).replaceAll("\\s+", " ").strip();
	}
}
