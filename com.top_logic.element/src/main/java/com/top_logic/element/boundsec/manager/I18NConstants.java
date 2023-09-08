/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ABSTRACT_SOURCE_TYPE =
		legacyKey1("admin.security.import.roleRules.problem.abstractSourceType");

	public static ResKey1 ABSTRACT_TYPE_WITHOUT_INHERITANCE =
		legacyKey1("admin.security.import.roleRules.problem.abstractTypeWithoutInheritance");

	public static ResKey2 ATTRIBUTE_AND_ASSOCIATION_GIVEN =
		legacyKey2("admin.security.import.roleRules.problem.attributeAndAssociationGiven");

	public static ResKey1 AUTHORIZATION_PROBLEM_UNKNOWN_CLASSIFIER =
		legacyKey1("admin.security.import.authorization.problem.unknownClassifier");

	public static ResKey2 AUTHORIZATION_PROBLEM_UNKNOWN_ROLE =
		legacyKey2("admin.security.import.authorization.problem.unknownRole");

	public static ResKey3 CLASSIFICATIONS_PROBLEM_UNKNOWN_CLASSIFIER =
		legacyKey3("admin.security.import.classifications.problem.unknownClassifier");

	public static ResKey1 DUPLICATE_META_ELEMENT =
		legacyKey1("admin.security.import.classifications.problem.duplicateMetaElement");

	public static ResKey2 DUPLICATE_ROLE = legacyKey2("admin.security.import.authorization.problem.duplicateRole");

	public static ResKey2 ILLEGAL_META_ELEMENT =
		legacyKey2("admin.security.import.roleRules.problem.illegalMetaElement");


	public static ResKey2 META_ELEMENT_AND_META_OBJECT_DECLARED =
		legacyKey2("admin.security.import.roleRules.problem.metaElementAndMetaObjectDeclared");

	public static ResKey META_ELEMENT_AND_META_OBJECT_NOT_DECLARED = legacyKey("admin.security.import.roleRules.problem.metaElementAndMetaObjectNotDeclared");

	public static ResKey NO_ATTRIBUTE_OR_ASSOCIATION = legacyKey("admin.security.import.roleRules.problem.noAttributeOrAssociation");

	public static ResKey1 ROLE_RULES_PROBLEM_UNKNOWN_ROLE =
		legacyKey1("admin.security.import.roleRules.problem.unknownRole");

	public static ResKey2 UNKNOWN_ATTRIBUTE = legacyKey2("admin.security.import.roleRules.problem.unknownAttribute");

	public static ResKey2 UNKNOWN_META_ATTRIBUTE =
		legacyKey2("admin.security.import.classifications.problem.unknownMetaAttribute");

	public static ResKey1 UNKNOWN_META_ELEMENT =
		legacyKey1("admin.security.import.roleRules.problem.unknownMetaElement");

	public static ResKey1 UNKNOWN_META_OBJECT = legacyKey1("admin.security.import.roleRules.problem.unknownMetaObject");

	static {
		initConstants(I18NConstants.class);
	}
}
