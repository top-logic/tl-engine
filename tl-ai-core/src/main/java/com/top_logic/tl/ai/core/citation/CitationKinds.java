/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;

/**
 * What an answer may cite in this application, declared once.
 *
 * <p>
 * {@code kind} used to be a free string on the cite tool, which named no kinds: asked to cite an
 * invoice the model invented {@code "model"}, nothing could resolve it, and its popover stayed
 * empty. A kind is a vocabulary the application owns - the framework only knows {@code web} - so
 * hosts declare theirs here, the framework tells the model what they are, and the validator refuses
 * one it was never told about, exactly as it refuses an invented id.
 * </p>
 *
 * @see #vocabulary()
 * @see CitationValidator
 */
public class CitationKinds extends ConfiguredManagedClass<CitationKinds.Config> {

	/** The one kind the framework itself knows: a page a browser tool fetched. */
	public static final String WEB = "web";

	/** Configuration of {@link CitationKinds}. */
	public interface Config extends ConfiguredManagedClass.Config<CitationKinds> {

		/** Configuration property name for {@link #getKinds()}. */
		String KINDS = "kinds";

		/**
		 * What answers in this application may cite, by {@link KindConfig#getName() kind}.
		 *
		 * @see #getKinds()
		 */
		@Name(KINDS)
		@Key(KindConfig.NAME_ATTRIBUTE)
		Map<String, KindConfig> getKinds();
	}

	/** One declared kind. */
	public interface KindConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getIdSource()}. */
		String ID_SOURCE = "id-source";

		/** Configuration property name for {@link #hasPassage()}. */
		String PASSAGE = "passage";

		/**
		 * One line saying what a source of this kind is, addressed to the model.
		 *
		 * @see #getDescription()
		 */
		@Name(DESCRIPTION)
		String getDescription();

		/**
		 * Where the id of such a source comes from, e.g. "the id field an invoice tool returned".
		 *
		 * @see #getIdSource()
		 */
		@Name(ID_SOURCE)
		String getIdSource();

		/**
		 * Whether a source of this kind has a sentence to quote.
		 *
		 * <p>
		 * True for text - a web page, a document. False for a structured object such as an invoice,
		 * which has fields rather than prose: asking for a verbatim sentence there made the model
		 * serialise the object as JSON, which no tool result then matched.
		 * </p>
		 *
		 * @see #hasPassage()
		 */
		@Name(PASSAGE)
		@BooleanDefault(true)
		boolean hasPassage();
	}

	private final Map<String, KindConfig> _kinds = new LinkedHashMap<>();

	/**
	 * Creates a {@link CitationKinds} from configuration.
	 *
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration.
	 */
	public CitationKinds(InstantiationContext context, Config config) {
		super(context, config);
		_kinds.putAll(config.getKinds());
	}

	/** The declared kinds, in declaration order. */
	public List<KindConfig> kinds() {
		return List.copyOf(_kinds.values());
	}

	/** Whether {@code kind} was declared. The empty kind is treated as {@link #WEB}. */
	public boolean isDeclared(String kind) {
		String name = kind == null || kind.isBlank() ? WEB : kind.trim();
		return _kinds.containsKey(name);
	}

	/** Whether a source of this kind has a sentence to quote; true for an undeclared one. */
	public boolean hasPassage(String kind) {
		KindConfig declared = _kinds.get(kind == null || kind.isBlank() ? WEB : kind.trim());
		return declared == null || declared.hasPassage();
	}

	/**
	 * What to tell the model it may cite, appended to the system prompt of a run whose agent has the
	 * cite tool.
	 */
	public String vocabulary() {
		if (_kinds.isEmpty()) {
			return "";
		}
		StringBuilder text = new StringBuilder("### Kinds you may cite\n\n");
		for (KindConfig kind : _kinds.values()) {
			text.append("- `").append(kind.getName()).append("`");
			if (!isBlank(kind.getDescription())) {
				text.append(" - ").append(kind.getDescription().trim());
			}
			if (!isBlank(kind.getIdSource())) {
				text.append(". id: ").append(kind.getIdSource().trim());
			}
			text.append(kind.hasPassage()
				? ". Pass the sentence you read as `passage`."
				: ". A structured object - do NOT pass a `passage`.");
			text.append("\n");
		}
		text.append("\nUse one of these kinds exactly as written. A citation with any other kind is"
			+ " dropped, because nothing in this application can resolve it.");
		return text.toString();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	/** The running instance. */
	public static CitationKinds getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Whether the service is available.
	 *
	 * <p>
	 * A unit test validates citations without an application around it; there the declaration is
	 * unknown and no kind is refused for being undeclared.
	 * </p>
	 */
	public static boolean isActive() {
		return Module.INSTANCE.isActive();
	}

	/** Module of {@link CitationKinds}. */
	public static final class Module extends TypedRuntimeModule<CitationKinds> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton
		}

		@Override
		public Class<CitationKinds> getImplementation() {
			return CitationKinds.class;
		}
	}
}
