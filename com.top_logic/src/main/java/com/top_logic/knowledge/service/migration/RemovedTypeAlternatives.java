/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.AlternativeConfig.TypeChoice;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;

/**
 * Removes references to removed types from the alternatives of a stored schema.
 *
 * <p>
 * An alternative ({@link AlternativeConfig}) lists the types it may stand for. When a migration
 * removes such a type from the stored schema, the alternative must no longer list it, because the
 * type system cannot be built from a schema with an alternative referencing an unknown type. An
 * alternative without remaining specialisations is removed itself, which in turn may empty other
 * alternatives listing it.
 * </p>
 *
 * <p>
 * The cleanup is available for the typed schema ({@link #cleanup(MetaObjectsConfig, Set, Log)})
 * and for the raw XML of the stored schema ({@link #cleanup(Document, Set, Log)}), which is used
 * when the stored schema cannot be loaded as typed configuration.
 * </p>
 *
 * @see DeleteTableProcessor
 * @see com.top_logic.knowledge.service.migration.processors.RemoveApplicationTypesProcessor
 */
public class RemovedTypeAlternatives {

	/**
	 * Name of the element of a specialisation within an alternative in the XML of a schema.
	 */
	private static final String SPECIALISATION_ELEMENT = "specialisation";

	/**
	 * Name of the attribute of a specialisation element referencing the specialised type.
	 */
	private static final String SPECIALISATION_NAME_ATTRIBUTE = TypeChoice.NAME_ATTRIBUTE;

	/**
	 * Removes all references to the given removed types from the alternatives of the given typed
	 * schema.
	 *
	 * @param types
	 *        The type definitions of the schema, see {@link MetaObjectsConfig#getTypes()}.
	 * @param removedTypes
	 *        Names of the types that have been removed from the schema.
	 * @param log
	 *        {@link Log} to report the changes to.
	 * @return The names of the alternatives that were removed, because they had no specialisation
	 *         left.
	 */
	public static Set<String> cleanup(MetaObjectsConfig types, Set<String> removedTypes, Log log) {
		Map<String, MetaObjectName> typeMap = types.getTypes();
		Set<String> removed = new HashSet<>(removedTypes);
		Set<String> removedAlternatives = new HashSet<>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Iterator<MetaObjectName> it = typeMap.values().iterator(); it.hasNext();) {
				MetaObjectName type = it.next();
				if (!(type instanceof AlternativeConfig)) {
					continue;
				}
				AlternativeConfig alternative = (AlternativeConfig) type;
				String alternativeName = alternative.getObjectName();
				List<TypeChoice> specialisations = alternative.getSpecialisations();
				for (Iterator<TypeChoice> choices = specialisations.iterator(); choices.hasNext();) {
					String specialisation = choices.next().getName();
					if (removed.contains(specialisation)) {
						choices.remove();
						logRemovedSpecialisation(log, alternativeName, specialisation);
					}
				}
				if (specialisations.isEmpty()) {
					it.remove();
					removed.add(alternativeName);
					removedAlternatives.add(alternativeName);
					logRemovedAlternative(log, alternativeName);
					changed = true;
				}
			}
		}
		return removedAlternatives;
	}

	/**
	 * Removes all references to the given removed types from the alternatives in the given XML of
	 * a stored schema.
	 *
	 * @param schema
	 *        The XML of the stored schema.
	 * @param removedTypes
	 *        Names of the types that have been removed from the schema.
	 * @param log
	 *        {@link Log} to report the changes to.
	 * @return The names of the alternatives that were removed, because they had no specialisation
	 *         left.
	 */
	public static Set<String> cleanup(Document schema, Set<String> removedTypes, Log log) {
		Set<String> removed = new HashSet<>(removedTypes);
		Set<String> removedAlternatives = new HashSet<>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Element alternative : elements(schema.getElementsByTagName(MetaObjectsConfig.ALTERNATIVE_TAG_NAME))) {
				String alternativeName = alternative.getAttribute(MetaObjectName.OBJECT_NAME_ATTRIBUTE);
				List<Element> specialisations = elements(alternative.getElementsByTagName(SPECIALISATION_ELEMENT));
				int remaining = specialisations.size();
				for (Element specialisation : specialisations) {
					String specialisationName = specialisation.getAttribute(SPECIALISATION_NAME_ATTRIBUTE);
					if (removed.contains(specialisationName)) {
						specialisation.getParentNode().removeChild(specialisation);
						remaining--;
						logRemovedSpecialisation(log, alternativeName, specialisationName);
					}
				}
				if (remaining == 0) {
					alternative.getParentNode().removeChild(alternative);
					removed.add(alternativeName);
					removedAlternatives.add(alternativeName);
					logRemovedAlternative(log, alternativeName);
					changed = true;
				}
			}
		}
		return removedAlternatives;
	}

	/**
	 * Copies the given live {@link NodeList} of elements, so that the elements can be removed from
	 * the document while iterating.
	 */
	private static List<Element> elements(NodeList nodes) {
		List<Element> result = new ArrayList<>(nodes.getLength());
		for (int n = 0, cnt = nodes.getLength(); n < cnt; n++) {
			result.add((Element) nodes.item(n));
		}
		return result;
	}

	private static void logRemovedSpecialisation(Log log, String alternative, String specialisation) {
		log.info("Removed specialisation '" + specialisation + "' of alternative '" + alternative
			+ "' from the stored schema.");
	}

	private static void logRemovedAlternative(Log log, String alternative) {
		log.info("Removed alternative '" + alternative
			+ "' from the stored schema, since none of its specialisations exists anymore.");
	}

}
