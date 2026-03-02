/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("admin.security.import.roleRules.problem.abstractSourceType")
	public static ResKey1 ABSTRACT_SOURCE_TYPE;

	@CustomKey("admin.security.import.roleRules.problem.abstractTypeWithoutInheritance")
	public static ResKey1 ABSTRACT_TYPE_WITHOUT_INHERITANCE;

	@CustomKey("admin.security.import.authorization.problem.unknownClassifier")
	public static ResKey1 AUTHORIZATION_PROBLEM_UNKNOWN_CLASSIFIER;

	/**
	 * @en Role {1} for classification {0} not found (available roles: {2}). The rule is not used.
	 */
	public static ResKey3 AUTHORIZATION_PROBLEM_UNKNOWN_ROLE;

	@CustomKey("admin.security.import.classifications.problem.unknownClassifier")
	public static ResKey3 CLASSIFICATIONS_PROBLEM_UNKNOWN_CLASSIFIER;

	@CustomKey("admin.security.import.classifications.problem.duplicateMetaElement")
	public static ResKey1 DUPLICATE_META_ELEMENT;

	@CustomKey("admin.security.import.authorization.problem.duplicateRole")
	public static ResKey2 DUPLICATE_ROLE;

	@CustomKey("admin.security.import.roleRules.problem.illegalMetaElement")
	public static ResKey2 ILLEGAL_META_ELEMENT;


	/**
	 * @en No type is given.
	 */
	public static ResKey NO_META_ELEMENT_DECLARED;

	/**
	 * @en No attribute given in path.
	 */
	public static ResKey NO_ATTRIBUTE_DECLARED;

	/**
	 * @en The role {0} does not exist. Available application roles are {1}, available global roles
	 *     are {2}. The corresponding rule is ignored.
	 */
	public static ResKey3 ROLE_RULES_PROBLEM_UNKNOWN_ROLE;

	@CustomKey("admin.security.import.roleRules.problem.unknownAttribute")
	public static ResKey2 UNKNOWN_ATTRIBUTE;

	@CustomKey("admin.security.import.classifications.problem.unknownMetaAttribute")
	public static ResKey2 UNKNOWN_META_ATTRIBUTE;

	@CustomKey("admin.security.import.roleRules.problem.unknownMetaElement")
	public static ResKey1 UNKNOWN_META_ELEMENT;

	static {
		initConstants(I18NConstants.class);
	}
}
