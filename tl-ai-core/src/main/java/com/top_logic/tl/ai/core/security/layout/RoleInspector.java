/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessRule;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix.RowState;
import com.top_logic.util.Resources;

/**
 * The pane next to a policy matrix: what the selected role may use on its own, across all four
 * dimensions, and why.
 *
 * <p>
 * Computed from the draft, and says so: what is shown is what the policy on the screen would do, not
 * what the running service does.
 * </p>
 */
/*
 * Issue #404. One line per reason rather than one per name: "seven agents, all through the section
 * default" is one sentence, and a list of seven identical sentences hides the one that differs. A name
 * the run refuses for a reason no cell shows - an infrastructure agent, an agent whose own model the role
 * may not use - gets a line of its own, because that is the line an administrator is looking for.
 */
final class RoleInspector {

	private final DraftAccess _access;

	private final boolean _unattended;

	/**
	 * Creates a {@link RoleInspector}.
	 *
	 * @param access
	 *        The evaluation of the draft.
	 * @param unattended
	 *        Whether the capabilities are asked about for an unattended run.
	 */
	RoleInspector(DraftAccess access, boolean unattended) {
		_access = access;
		_unattended = unattended;
	}

	/** The pane for one role. */
	HTMLFragment view(String roleName, String label) {
		PolicyDraft draft = _access.draft();
		List<HTMLFragment> parts = new ArrayList<>();
		parts.add(Fragments.div("aiPolicyInspectorHead",
			Fragments.span("aiPolicyInspectorTitle", Fragments.text(label)),
			Fragments.span("aiPolicyInspectorName", Fragments.text(roleName))));
		parts.add(Fragments.div("aiPolicyInspectorBasis", Fragments.message(draft.isModified()
			? I18NConstants.INSPECTOR_BASIS_UNSAVED : I18NConstants.INSPECTOR_BASIS_SAVED)));
		for (Dimension dimension : Dimension.values()) {
			parts.add(runSection(roleName, dimension));
		}
		parts.add(capabilitySection(roleName));
		return Fragments.div("aiPolicyInspectorBody", parts.toArray(new HTMLFragment[parts.size()]));
	}

	private HTMLFragment runSection(String roleName, Dimension dimension) {
		PolicyMatrix matrix = new PolicyMatrix(_access.draft(), MatrixDimension.of(dimension));
		List<String> names = matrix.columns(_access.knownNames(dimension));

		Map<String, List<String>> allowedBy = new LinkedHashMap<>();
		List<HTMLFragment> ownLines = new ArrayList<>();
		List<HTMLFragment> refusedLines = new ArrayList<>();
		List<String> deniedByCell = new ArrayList<>();
		int rest = 0;
		int allowed = 0;
		for (String name : names) {
			DraftAccess.RunVerdict verdict = _access.run(roleName, dimension, name);
			if (verdict.allowed()) {
				allowed++;
				if (verdict.mark() == DraftAccess.Mark.AGENTS_OWN) {
					ownLines.add(line("aiPolicyInspectorOwn", "A", name, resources().getString(
						I18NConstants.INSPECTOR_AGENTS_OWN__AGENTS.fill(
							String.join(", ", _access.agentsUsing(roleName, dimension, name))))));
				} else {
					allowedBy.computeIfAbsent(ToolAccessLabels.sentence(verdict.decision()), k -> new ArrayList<>())
						.add(name);
				}
				continue;
			}
			Decision.Denied denied = (Decision.Denied) verdict.decision();
			if (verdict.mark() == DraftAccess.Mark.REFUSED) {
				refusedLines.add(line("aiPolicyInspectorRefused", "⊘", name,
					ToolAccessLabels.sentence(denied)));
			} else if (denied.cause() == Cause.ROLE_DENY) {
				deniedByCell.add(name);
			} else {
				rest++;
			}
		}

		List<HTMLFragment> lines = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : allowedBy.entrySet()) {
			lines.add(line("aiPolicyInspectorAllowed", "✓", String.join(", ", entry.getValue()),
				entry.getKey()));
		}
		lines.addAll(ownLines);
		lines.addAll(refusedLines);
		if (!deniedByCell.isEmpty()) {
			lines.add(line("aiPolicyInspectorDenied", "✕", String.join(", ", deniedByCell),
				resources().getString(I18NConstants.INSPECTOR_DENIED_BY_CELL)));
		}
		if (rest > 0) {
			boolean named = matrix.rowState(roleName) == RowState.NAMED;
			lines.add(line("aiPolicyInspectorRest", "–",
				resources().getString(I18NConstants.INSPECTOR_REST__COUNT.fill(Integer.valueOf(rest))),
				resources().getString(named ? I18NConstants.INSPECTOR_REST_RESTRICTED
					: I18NConstants.INSPECTOR_REST_DEFAULT)));
		}

		String state = matrix.rowState(roleName) == RowState.NAMED
			? resources().getString(I18NConstants.ROW_STATE_RESTRICTED)
			: resources().getString(I18NConstants.INSPECTOR_NOT_NAMED__DEFAULT.fill(
				MetaLabelProvider.INSTANCE.getLabel(_access.draft().sectionDefault(dimension))));
		String heading = resources().getString(I18NConstants.INSPECTOR_SECTION__DIMENSION_STATE_ALLOWED_TOTAL
			.fill(ToolAccessLabels.dimensionLabel(dimension), state, Integer.valueOf(allowed),
				Integer.valueOf(names.size())));
		return section(heading, lines);
	}

	private HTMLFragment capabilitySection(String roleName) {
		PolicyMatrix matrix = new PolicyMatrix(_access.draft(), MatrixDimension.CAPABILITIES);
		Map<String, List<String>> allowedBy = new LinkedHashMap<>();
		List<String> deniedByCell = new ArrayList<>();
		List<String> capped = new ArrayList<>();
		int granted = 0;
		int denied = 0;
		int rest = 0;
		for (String group : matrix.columns(List.of())) {
			CellState cell = matrix.cell(roleName, group);
			if (cell == CellState.GRANTED) {
				granted++;
			} else if (cell == CellState.DENIED) {
				denied++;
			}
			DraftAccess.CapabilityVerdict verdict = _access.capability(roleName, group);
			String label = ToolAccessLabels.groupLabel(group);
			if (verdict.allowed(_unattended)) {
				allowedBy.computeIfAbsent(ToolAccessLabels.sentence(verdict.explanation()), k -> new ArrayList<>())
					.add(label);
			} else if (verdict.explanation().allowed()) {
				capped.add(label);
			} else if (verdict.explanation().kind() == ToolAccessRule.Kind.ROLE_DENY) {
				deniedByCell.add(label);
			} else {
				rest++;
			}
		}

		List<HTMLFragment> lines = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : allowedBy.entrySet()) {
			lines.add(line("aiPolicyInspectorAllowed", "✓", String.join(", ", entry.getValue()),
				entry.getKey()));
		}
		if (!capped.isEmpty()) {
			lines.add(line("aiPolicyInspectorRefused", "⊘", String.join(", ", capped),
				resources().getString(I18NConstants.RULE_BACKGROUND_CEILING)));
		}
		if (!deniedByCell.isEmpty()) {
			lines.add(line("aiPolicyInspectorDenied", "✕", String.join(", ", deniedByCell),
				resources().getString(I18NConstants.INSPECTOR_DENIED_BY_CELL)));
		}
		if (rest > 0) {
			lines.add(line("aiPolicyInspectorRest", "–",
				resources().getString(I18NConstants.INSPECTOR_REST__COUNT.fill(Integer.valueOf(rest))),
				resources().getString(I18NConstants.RULE_NOT_GRANTED)));
		}

		ResKey mode = _unattended ? I18NConstants.MODE_BACKGROUND : I18NConstants.MODE_INTERACTIVE;
		String heading = resources().getString(
			I18NConstants.INSPECTOR_CAPABILITIES__GRANTED_DENIED_BASELINE_MODE.fill(
				Integer.valueOf(granted), Integer.valueOf(denied),
				resources().getString(ToolAccessLabels.baselineChoice(_access.draft().baselineChoice())),
				resources().getString(mode)));
		return section(heading, lines);
	}

	private static HTMLFragment section(String heading, List<HTMLFragment> lines) {
		List<HTMLFragment> parts = new ArrayList<>();
		parts.add(Fragments.div("aiPolicyInspectorSection", Fragments.text(heading)));
		parts.addAll(lines);
		return Fragments.div("aiPolicyInspectorBlock", parts.toArray(new HTMLFragment[parts.size()]));
	}

	private static HTMLFragment line(String cssClass, String icon, String names, String reason) {
		return Fragments.div("aiPolicyInspectorLine " + cssClass,
			Fragments.span("aiPolicyInspectorIcon", Fragments.text(icon)),
			Fragments.span("aiPolicyInspectorNames", Fragments.text(names)),
			Fragments.span("aiPolicyInspectorReason", Fragments.text(" – " + reason)));
	}

	private static Resources resources() {
		return Resources.getInstance();
	}

}
