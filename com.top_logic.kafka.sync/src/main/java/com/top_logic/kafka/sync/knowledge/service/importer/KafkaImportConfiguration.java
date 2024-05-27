/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.importer;

import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.kafka.sync.knowledge.service.KafkaSyncConfig;
import com.top_logic.kafka.sync.knowledge.service.exporter.KafkaExportConfiguration;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;

/**
 * Import configuration for the object import by kafka.
 * 
 * @see DefaultKafkaKBImportRewriter
 * @see KafkaExportConfiguration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KafkaImportConfiguration extends KafkaSyncConfig {

	/**
	 * The association types which holds may hold references with exported objects as source or
	 * destination end.
	 * 
	 * <p>
	 * This set typically holds sub types of
	 * {@link ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}, and types which are used by
	 * reference attributes to store reference.
	 * </p>
	 */
	Set<MetaObject> getAssociationTypes();

	/**
	 * Mapping of the source tType (as string) to the current {@link ObjectKey} of the target type.
	 * 
	 * <p>
	 * When for a source type nothing is returned, the type is not imported and changes ignored.
	 * </p>
	 */
	Mapping<? super String, ObjectKey> getTypeMapping();

	/**
	 * Mapping of the full qualified name of an source attribute to the name of the
	 * {@link MOAttribute} storing the value.
	 * 
	 * <p>
	 * When for an attribute no target attribute is given, the attribute value is not imported and
	 * ignored.
	 * </p>
	 */
	Mapping<? super String, String> getAttributeMapping();

	/**
	 * Variant of {@link #getValueMapping(ObjectKey, String)} which uses the qualified type name
	 * instead of the {@link ObjectKey}.
	 */
	Function<Object, ?> getValueMapping(String qualifiedOwnerTypeName, String tlAttribute);

}
