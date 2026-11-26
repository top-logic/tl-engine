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
import com.top_logic.model.TLClass;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Message key used when the source {@link TLClass} of a rule is abstract.
	 */
	@CustomKey("admin.security.import.roleRules.problem.abstractSourceType")
	public static ResKey1 ABSTRACT_SOURCE_TYPE;

	/**
	 * Message key used when the {@link TLClass} of a rule is abstract and the rule is no "inherit"
	 * rule. In such case the rule is useless.
	 */
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

	/**
	 * @en No type is given.
	 */
	public static ResKey NO_META_ELEMENT_DECLARED;

	/**
	 * @en The role {0} does not exist. Available roles are {1}. The corresponding rule is ignored.
	 */
	public static ResKey2 ROLE_RULES_PROBLEM_UNKNOWN_ROLE;

	/**
	 * @en The part {0} is not a reference.
	 */
	public static ResKey1 NOT_A_REFERENCE__PART;

	@CustomKey("admin.security.import.classifications.problem.unknownMetaAttribute")
	public static ResKey2 UNKNOWN_META_ATTRIBUTE;

	/**
	 * Message key used when configured {@link TLClass} is unknown.
	 */
	@CustomKey("admin.security.import.roleRules.problem.unknownMetaElement")
	public static ResKey1 UNKNOWN_META_ELEMENT;

	/**
	 * @en The singleton {0} can not be resolved.
	 */
	public static ResKey1 INVALID_SINGLETON__NAME;

	/**
	 * @en There are two rules with the same id: {0}
	 */
	public static ResKey1 ERROR_DUPLICATE_RULE_ID__ID;

	/**
	 * @en The rule {0} navigates a historic reference backwards and therefore always starts at the
	 *     checked version. It must not set the attribute "history".
	 */
	public static ResKey1 ERROR_HISTORY_WITH_ANCHOR_STEP__RULE;

	/**
	 * @en The singleton rule {0} must not set the attribute "history".
	 */
	public static ResKey1 ERROR_HISTORY_AT_SINGLETON_RULE__RULE;

	/**
	 * @en The rule {0} has no path. It must not use history="anchored", because the object would
	 *     lose the role in its current version.
	 */
	public static ResKey1 ERROR_ANCHORED_WITHOUT_PATH__RULE;

	/**
	 * @en The rule {0} uses history="anchored" but does not name a reference in the attribute
	 *     "anchors".
	 */
	public static ResKey1 ERROR_ANCHORED_WITHOUT_ANCHORS__RULE;

	/**
	 * @en The rule {0} names "{1}" in the attribute "anchors", which is not a reference with
	 *     history type "historic".
	 */
	public static ResKey2 ERROR_ANCHOR_NOT_HISTORIC_REFERENCE__RULE_NAME;

	/**
	 * @en The rule {0} sets the attribute "anchors" without using history="anchored".
	 */
	public static ResKey1 ERROR_ANCHORS_WITHOUT_ANCHORED__RULE;

	/**
	 * @en The rule {0} uses the historic reference "{1}" as anchor. The reference is stored in a way
	 *     that the objects referring to a version cannot be found, so the rule would never find an
	 *     anchor. Store the reference in a link table.
	 */
	public static ResKey2 ERROR_ANCHOR_NOT_NAVIGABLE__RULE_REFERENCE;

	static {
		initConstants(I18NConstants.class);
	}
}
