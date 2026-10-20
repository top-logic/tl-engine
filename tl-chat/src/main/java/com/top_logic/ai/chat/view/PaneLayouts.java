/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * Which attributes the pane shows for a type it was never configured for, and in which order.
 *
 * <p>
 * The pane has no view per type, so it asks the model. Two rules carry it:
 * </p>
 *
 * <ul>
 * <li><b>Inherited first.</b> An attribute a type inherits is usually the one that names the object -
 * {@code name} of a common base type - and a form or a table that started with the type's own
 * details and ended with its name would read backwards.</li>
 * <li><b>A composition is a table, not a value.</b> The engine's {@code <field>} has no view of a
 * composition and prints its parts as text; the form shows each - and each computed list of objects,
 * which is usually a selection of such parts - as a {@code <composition-table>} below the fields, and a table that chooses its own columns leaves compositions and other
 * multi-valued attributes to the column selection - and long text, which a column cuts to one
 * line.</li>
 * </ul>
 *
 * <p>
 * A type that names its main properties ({@code <main-properties>}) needs none of this for its table:
 * the engine's {@code <table>} shows exactly those, and the pane leaves the choice to it.
 * </p>
 */
public final class PaneLayouts {

	/** How many columns a table chooses for a type that names no main properties. */
	public static final int MAX_CHOSEN_COLUMNS = 6;

	private PaneLayouts() {
		// Static utility.
	}

	/**
	 * Every attribute of the type that is not hidden from display: those of its generalizations
	 * first, in their order, then its own.
	 */
	public static List<TLStructuredTypePart> ordered(TLStructuredType type) {
		if (type == null) {
			return List.of();
		}
		Map<String, TLStructuredTypePart> parts = new LinkedHashMap<>();
		collect(type, type, parts);
		List<TLStructuredTypePart> result = new ArrayList<>(parts.size());
		for (TLStructuredTypePart part : parts.values()) {
			if (!DisplayAnnotations.isHidden(part)) {
				result.add(part);
			}
		}
		return result;
	}

	private static void collect(TLStructuredType type, TLStructuredType declaring,
			Map<String, TLStructuredTypePart> parts) {
		if (declaring instanceof TLClass clazz) {
			for (TLClass generalization : clazz.getGeneralizations()) {
				collect(type, generalization, parts);
			}
		}
		for (TLStructuredTypePart local : declaring.getLocalParts()) {
			// The part as the shown type has it: an override there is the one that counts.
			TLStructuredTypePart part = type.getPart(local.getName());
			if (part != null) {
				parts.putIfAbsent(part.getName(), part);
			}
		}
	}

	/** Whether the attribute holds parts of the object rather than a value of it. */
	public static boolean isComposition(TLStructuredTypePart part) {
		return part instanceof TLReference reference && reference.isComposite();
	}

	/**
	 * Whether a form shows the attribute as a table of its own: a composition, or a computed list of
	 * objects - a selection of the object's parts, such as the sources a research run cites out of
	 * all it read.
	 */
	public static boolean isTable(TLStructuredTypePart part) {
		return isComposition(part)
			|| (part instanceof TLReference reference && reference.isMultiple() && reference.isDerived());
	}

	/**
	 * Whether the attribute holds text that is read rather than scanned - a description, a note - and
	 * would take a table's width for one cut-off line.
	 */
	public static boolean isLongText(TLStructuredTypePart part) {
		return TLAnnotations.getAnnotation(part, MultiLine.class) != null
			|| TLAnnotations.getAnnotation(part.getType(), MultiLine.class) != null;
	}

	/**
	 * The columns of a table that must name its columns - a {@code <composition-table>} derives
	 * none: the main properties of the type, otherwise the ones {@link #chosenColumns} picks.
	 */
	public static List<String> columns(TLStructuredType type) {
		if (type == null) {
			return List.of();
		}
		List<String> main = new ArrayList<>();
		for (String name : DisplayAnnotations.getMainProperties(type)) {
			if (type.getPart(name) != null) {
				main.add(name);
			}
		}
		return main.isEmpty() ? chosenColumns(type) : main;
	}

	/**
	 * The columns a table chooses itself, or an empty list when the type names its main properties
	 * and the table is to show those.
	 */
	public static List<String> chosenColumns(TLStructuredType type) {
		if (type == null || !DisplayAnnotations.getMainProperties(type).isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		for (TLStructuredTypePart part : ordered(type)) {
			if (isComposition(part) || part.isMultiple() || isLongText(part)) {
				continue;
			}
			result.add(part.getName());
			if (result.size() == MAX_CHOSEN_COLUMNS) {
				break;
			}
		}
		return result;
	}
}
