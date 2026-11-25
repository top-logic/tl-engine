/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.boundsec.manager.I18NConstants;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.SingletonRuleConfig;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.util.TLModelUtil;

/**
 * How a role rule or role parent rule treats historic versions of objects.
 *
 * @see NavigationRuleConfig#getHistory()
 * @see NavigationRuleConfig#getAnchors()
 */
public final class RuleHistory {

	/** {@link RuleHistory} of a role rule without configuration. */
	public static final RuleHistory CURRENT =
		new RuleHistory(HistoryMode.CURRENT, false, false, false, Collections.emptyList());

	/** {@link RuleHistory} of a role parent rule without configuration. */
	public static final RuleHistory HISTORIC =
		new RuleHistory(HistoryMode.HISTORIC, false, false, false, Collections.emptyList());

	private final HistoryMode _mode;

	private final boolean _explicit;

	private final boolean _anchorStep;

	private final boolean _historicForwardStep;

	private final List<TLReference> _anchors;

	private RuleHistory(HistoryMode mode, boolean explicit, boolean anchorStep, boolean historicForwardStep,
			List<TLReference> anchors) {
		_mode = mode;
		_explicit = explicit;
		_anchorStep = anchorStep;
		_historicForwardStep = historicForwardStep;
		_anchors = anchors;
	}

	/**
	 * The data the path of the rule is navigated in.
	 *
	 * <p>
	 * A rule with an {@link #hasAnchorStep() anchor step} always starts at the checked version,
	 * which is the behavior of {@link HistoryMode#HISTORIC}.
	 * </p>
	 */
	public HistoryMode getMode() {
		return _mode;
	}

	/**
	 * Whether the {@link #getMode() mode} was set in the configuration of the rule.
	 */
	public boolean isExplicit() {
		return _explicit;
	}

	/**
	 * Whether the path of the rule navigates a historic reference backwards.
	 *
	 * @see PathNavigation#isAnchorStep()
	 */
	public boolean hasAnchorStep() {
		return _anchorStep;
	}

	/**
	 * Whether the path of the rule navigates a historic reference in forward direction.
	 *
	 * <p>
	 * The roles such a rule grants depend on a historic version of another object, whose roles
	 * change with the current data. The result of the rule is therefore never stored.
	 * </p>
	 */
	public boolean hasHistoricForwardStep() {
		return _historicForwardStep;
	}

	/**
	 * The references that open the gate of a rule with mode {@link HistoryMode#ANCHORED}.
	 */
	public List<TLReference> getAnchors() {
		return _anchors;
	}

	/**
	 * Whether a role this rule grants on an object depends on the version of the object that is
	 * checked.
	 *
	 * @param roleRule
	 *        Whether the rule is a role rule. A role parent rule navigating in the checked version
	 *        is the behavior without configuration and does not count.
	 */
	public boolean isVersionDependent(boolean roleRule) {
		if (_anchorStep || _mode == HistoryMode.ANCHORED) {
			return true;
		}
		return roleRule && _explicit && _mode == HistoryMode.HISTORIC;
	}

	/**
	 * Whether the given object is a historic version.
	 */
	public static boolean isHistoric(TLObject object) {
		return object.tHistoryContext() != Revision.CURRENT_REV;
	}

	/**
	 * Resolves the configuration of a rule.
	 *
	 * @param config
	 *        The configuration of the rule.
	 * @param path
	 *        The path of the rule. It is empty, if the rule has no path.
	 * @param roleRule
	 *        Whether the rule is a role rule, in contrast to a role parent rule.
	 * @param problems
	 *        Receives the errors in the configuration.
	 * @return The resolved configuration. If problems are reported, the result must not be used.
	 */
	public static RuleHistory resolve(NavigationRuleConfig config, List<PathElement> path, boolean roleRule,
			Consumer<ResKey> problems) {
		String id = config.getId();
		boolean anchorStep = false;
		boolean historicForwardStep = false;
		for (PathElement step : path) {
			if (step instanceof PathNavigation navigation) {
				if (navigation.isAnchorStep()) {
					anchorStep = true;
					checkNavigable(id, navigation.getReference(), problems);
				} else if (!navigation.isInverse()
					&& navigation.getReference().getHistoryType() != HistoryType.CURRENT) {
					historicForwardStep = true;
				}
			}
		}
		HistoryMode configured = config.getHistory();
		if (configured != null) {
			if (anchorStep) {
				problems.accept(I18NConstants.ERROR_HISTORY_WITH_ANCHOR_STEP__RULE.fill(id));
			}
			if (config instanceof SingletonRuleConfig) {
				problems.accept(I18NConstants.ERROR_HISTORY_AT_SINGLETON_RULE__RULE.fill(id));
			}
		}
		boolean anchored = configured == HistoryMode.ANCHORED;
		List<TLReference> anchors = new ArrayList<>();
		if (anchored) {
			if (path.isEmpty()) {
				problems.accept(I18NConstants.ERROR_ANCHORED_WITHOUT_PATH__RULE.fill(id));
			}
			if (config.getAnchors().isEmpty()) {
				problems.accept(I18NConstants.ERROR_ANCHORED_WITHOUT_ANCHORS__RULE.fill(id));
			}
			for (String name : config.getAnchors()) {
				TLReference anchor = resolveAnchor(name);
				if (anchor == null) {
					problems.accept(I18NConstants.ERROR_ANCHOR_NOT_HISTORIC_REFERENCE__RULE_NAME.fill(id, name));
				} else {
					checkNavigable(id, anchor, problems);
					anchors.add(anchor);
				}
			}
		} else if (!config.getAnchors().isEmpty()) {
			problems.accept(I18NConstants.ERROR_ANCHORS_WITHOUT_ANCHORED__RULE.fill(id));
		}

		HistoryMode mode;
		if (anchorStep) {
			mode = HistoryMode.HISTORIC;
		} else if (configured != null) {
			mode = configured;
		} else {
			mode = roleRule ? HistoryMode.CURRENT : HistoryMode.HISTORIC;
		}
		return new RuleHistory(mode, configured != null, anchorStep, historicForwardStep,
			Collections.unmodifiableList(anchors));
	}

	/**
	 * Reports a reference used as anchor whose referring objects cannot be found: a rule using it
	 * would silently never find an anchor.
	 */
	private static void checkNavigable(String ruleId, TLReference anchor, Consumer<ResKey> problems) {
		if (!PathNavigation.isBackwardsNavigable(anchor)) {
			problems.accept(I18NConstants.ERROR_ANCHOR_NOT_NAVIGABLE__RULE_REFERENCE.fill(ruleId,
				TLModelUtil.qualifiedName(anchor)));
		}
	}

	private static TLReference resolveAnchor(String name) {
		Object part;
		try {
			part = TLModelUtil.resolveQualifiedName(name);
		} catch (RuntimeException ex) {
			return null;
		}
		if (part instanceof TLReference reference && reference.getHistoryType() == HistoryType.HISTORIC) {
			return reference;
		}
		return null;
	}

}
