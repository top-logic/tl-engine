/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * What an answer may cite in this application, declared once.
 *
 * <p>
 * A kind is a vocabulary the application owns. The framework knows two, {@link #WEB} and
 * {@link #OBJECT}; an application declares its own here. The model never names a kind: the tool that
 * read a source decides it - a page is {@code web}, an object is the first kind whose
 * {@link KindConfig#getTypes() types} cover its type, or {@code object} - and the validator keeps a
 * citation only of a declared kind. What a kind is for is the reader: the {@link KindConfig#getLabel()
 * label} the sources list and the popover show, and whether a source of it has a sentence to quote.
 * </p>
 *
 * <p>
 * An application whose agents point at sources another way than with a link registers the syntax
 * here, in {@code <marker-syntaxes>} - see {@link CitationMarkerSyntax}.
 * </p>
 *
 * @see CitationValidator
 */
public class CitationKinds extends ConfiguredManagedClass<CitationKinds.Config> {

	/** A web page {@code webFetch} or the browser read. */
	public static final String WEB = "web";

	/** An object of the application a read tool showed, of a type no declared kind claims. */
	public static final String OBJECT = "object";

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

		/** Configuration property name for {@link #getMarkerSyntaxes()}. */
		String MARKER_SYNTAXES = "marker-syntaxes";

		/**
		 * Further ways an answer of this application points at its sources, beside the link.
		 *
		 * <p>
		 * The link ({@link LinkMarkers}) is always understood and is not configured here.
		 * </p>
		 *
		 * @see #getMarkerSyntaxes()
		 */
		@Name(MARKER_SYNTAXES)
		List<PolymorphicConfiguration<? extends CitationMarkerSyntax>> getMarkerSyntaxes();
	}

	/** One declared kind. */
	public interface KindConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration property name for {@link #getTypes()}. */
		String TYPES = "types";

		/** Configuration property name for {@link #hasPassage()}. */
		String PASSAGE = "passage";

		/**
		 * One line saying what a source of this kind is, for whoever maintains the configuration.
		 *
		 * @see #getDescription()
		 */
		@Name(DESCRIPTION)
		String getDescription();

		/**
		 * What to call this kind in front of a reader, in the reader's language.
		 *
		 * <p>
		 * The name is the configuration's vocabulary and stays the same word in every language. This
		 * is the word the sources rail and the marker popover show instead; without it they show the
		 * name.
		 * </p>
		 *
		 * @see #getLabel()
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The qualified names of the model types whose objects are sources of this kind, e.g.
		 * {@code demo.events:Event}. A subtype counts as its type.
		 *
		 * <p>
		 * An object a read tool showed the model is put on the reading list with the first declared
		 * kind that claims its type, and with {@link CitationKinds#OBJECT} when none does. A kind
		 * without types is one only an application's own tool registers.
		 * </p>
		 *
		 * @see #getTypes()
		 */
		@Name(TYPES)
		@Format(CommaSeparatedStrings.class)
		List<String> getTypes();

		/**
		 * Whether a source of this kind has a sentence to quote.
		 *
		 * <p>
		 * True for text - a web page, a document. False for a structured object such as an invoice,
		 * which has fields rather than prose: its popover shows the fields the tool read instead of a
		 * window of text around a sentence.
		 * </p>
		 *
		 * @see #hasPassage()
		 */
		@Name(PASSAGE)
		@BooleanDefault(true)
		boolean hasPassage();
	}

	private final Map<String, KindConfig> _kinds = new LinkedHashMap<>();

	private final List<CitationMarkerSyntax> _syntaxes = new ArrayList<>();

	/** Whether any kind claims model types; every object is then an {@link #OBJECT} without asking. */
	private final boolean _claimsTypes;

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
		_claimsTypes = _kinds.values().stream().anyMatch(kind -> !kind.getTypes().isEmpty());
		_syntaxes.add(LinkMarkers.INSTANCE);
		for (PolymorphicConfiguration<? extends CitationMarkerSyntax> syntax : config.getMarkerSyntaxes()) {
			_syntaxes.add(context.getInstance(syntax));
		}
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

	/**
	 * What to call {@code kind} in front of a reader, in the current account's language.
	 *
	 * @return the declared label, or empty when the kind is undeclared or declared without one - the
	 *         caller then shows the kind name, which is what every installation showed before.
	 */
	public String label(String kind) {
		KindConfig declared = _kinds.get(kind == null || kind.isBlank() ? WEB : kind.trim());
		ResKey key = declared == null ? null : declared.getLabel();
		if (key == null) {
			return "";
		}
		String text = Resources.getInstance().getString(key, null);
		return text == null ? "" : text;
	}

	/** Whether a source of this kind has a sentence to quote; true for an undeclared one. */
	public boolean hasPassage(String kind) {
		KindConfig declared = _kinds.get(kind == null || kind.isBlank() ? WEB : kind.trim());
		return declared == null || declared.hasPassage();
	}

	/**
	 * The ways an answer of this application points at its sources: the link first, then the
	 * application's own syntaxes in configuration order.
	 */
	public List<CitationMarkerSyntax> markerSyntaxes() {
		return List.copyOf(_syntaxes);
	}

	/**
	 * The kind an object a read tool showed the model is a source of: the first declared kind whose
	 * {@link KindConfig#getTypes() types} cover the object's type, else {@link #OBJECT}.
	 */
	public String kindOf(TLObject object) {
		TLStructuredType type = object == null ? null : object.tType();
		if (type == null || !_claimsTypes) {
			return OBJECT;
		}
		List<String> names = new ArrayList<>();
		if (type instanceof TLClass clazz) {
			for (TLClass general : TLModelUtil.getReflexiveTransitiveGeneralizations(clazz)) {
				names.add(TLModelUtil.qualifiedName(general));
			}
		} else {
			names.add(TLModelUtil.qualifiedName(type));
		}
		for (KindConfig kind : _kinds.values()) {
			for (String claimed : kind.getTypes()) {
				if (names.contains(claimed.trim())) {
					return kind.getName();
				}
			}
		}
		return OBJECT;
	}

	/**
	 * {@link #kindOf(TLObject)} of the running instance, or {@link #OBJECT} outside an application.
	 */
	public static String objectKind(TLObject object) {
		return isActive() ? getInstance().kindOf(object) : OBJECT;
	}

	/**
	 * {@link #markerSyntaxes()} of the running instance, or the link alone outside an application.
	 */
	public static List<CitationMarkerSyntax> syntaxes() {
		return isActive() ? getInstance().markerSyntaxes() : List.of(LinkMarkers.INSTANCE);
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
