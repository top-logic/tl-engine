/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityParentsGenerator;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;

/**
 * {@link ViewAction} serving the security coverage display: it analyses the model based access
 * definition and lists the security parent rules proposed by that analysis.
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the coverage view rather than claiming a
 * global {@code @TagName}. What the action does is decided by its {@link Config#getMode() mode}: it
 * either returns the analyzed types ({@code List<TypeCoverage>}) to write to the channel feeding
 * the {@link SecurityCoverageTable}, or one entry per proposed rule, as the dictionary a table
 * shows and the command accepting the picked proposals works on. The rule of a single type is
 * rendered as XML for the table's detail channel.
 * </p>
 *
 * <p>
 * The types to list the proposals of are read from the configured {@link Config#getRows() rows
 * channel}, so that the display shows the proposals of the analysis the user is looking at. A
 * fresh analysis is made when that channel is unset or holds nothing yet.
 * </p>
 *
 * @implNote Analysis and generation are delegated to {@link SecurityCoverageCheck#analyze()} and
 *           {@link SecurityParentsGenerator}, so that the display, the startup log and the
 *           application test see the same findings.
 */
public class SecurityCoverageAction implements ViewAction {

	/**
	 * What a {@link SecurityCoverageAction} does.
	 */
	public enum Mode {
		/** Analyze the access definition and return the result. */
		REFRESH,

		/** Return the security parent rules proposed for the analyzed types, one entry each. */
		PROPOSALS;

	}

	/** Key of the type a proposal entry defines the security parent of, a {@link TLClass}. */
	public static final String PROPOSAL_TYPE = "type";

	/**
	 * Key of the type of the container that becomes the security parent, a {@link TLClass}: the
	 * owner of the composition the type is contained in.
	 */
	public static final String PROPOSAL_CONTAINER = "container";

	/**
	 * Key of the composition the proposed rule navigates backwards, a {@link TLReference} of the
	 * {@link #PROPOSAL_CONTAINER container}.
	 */
	public static final String PROPOSAL_REFERENCE = "reference";

	/** Key of the id the proposed rule is stored under. */
	public static final String PROPOSAL_ID = "id";

	/** Key of the {@link TypeCoverage analyzed type} the proposal belongs to. */
	public static final String PROPOSAL_COVERAGE = "coverage";

	/**
	 * Configuration for {@link SecurityCoverageAction}.
	 */
	public interface Config extends PolymorphicConfiguration<SecurityCoverageAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getRows()}. */
		String ROWS = "rows";

		@Override
		@ClassDefault(SecurityCoverageAction.class)
		Class<? extends SecurityCoverageAction> getImplementationClass();

		/**
		 * What the action does.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the analyzed types to work with; a fresh analysis is made
		 * when it is unset or holds nothing yet.
		 */
		@Name(ROWS)
		@Nullable
		String getRows();
	}

	private final Mode _mode;

	private final String _rowsChannel;

	/**
	 * Creates a new {@link SecurityCoverageAction} from configuration.
	 */
	@CalledByReflection
	public SecurityCoverageAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_rowsChannel = config.getRows();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return switch (_mode) {
			case REFRESH -> analyze();
			case PROPOSALS -> proposals(rows(context));
		};
	}

	/**
	 * The security parent rule proposed for the given type, as the HTML a display shows.
	 *
	 * @param coverage
	 *        The analyzed type to render the proposed rule of.
	 * @return The rendered rule, empty when no rule is proposed for the type.
	 */
	public static String ruleHtml(TypeCoverage coverage) {
		SecurityParentsGenerator generator = new SecurityParentsGenerator();
		SecurityParentsConfig rules = generator.collect(List.of(coverage));
		if (rules.getRules().isEmpty()) {
			return "";
		}
		return html(generator.toXml(rules));
	}

	/**
	 * The security parent rules proposed for the given types, one entry per proposal in the order
	 * of the types, each the dictionary keyed by {@link #PROPOSAL_TYPE}, {@link #PROPOSAL_CONTAINER},
	 * {@link #PROPOSAL_REFERENCE}, {@link #PROPOSAL_ID} and {@link #PROPOSAL_COVERAGE}.
	 */
	private static List<Map<String, Object>> proposals(List<TypeCoverage> coverage) {
		List<Map<String, Object>> entries = new ArrayList<>();
		for (TypeCoverage typeCoverage : coverage) {
			for (CoverageFinding proposal : typeCoverage.findings(FindingKind.SUGGESTED_PARENT)) {
				TLReference reference = proposal.getContainerReferences().get(0);
				Map<String, Object> entry = new HashMap<>();
				entry.put(PROPOSAL_TYPE, typeCoverage.type());
				entry.put(PROPOSAL_CONTAINER, reference.getOwner());
				entry.put(PROPOSAL_REFERENCE, reference);
				entry.put(PROPOSAL_ID, proposal.getSuggestedRule().getId());
				entry.put(PROPOSAL_COVERAGE, typeCoverage);
				entries.add(entry);
			}
		}
		return entries;
	}

	/**
	 * The given configuration as HTML that keeps its line breaks and indentation.
	 */
	private static String html(String xml) {
		return "<pre><code>" + TagUtil.encodeXML(xml) + "</code></pre>";
	}

	/**
	 * The analyzed types held by the configured rows channel, or a fresh analysis when that channel
	 * is unset or holds nothing yet.
	 */
	@SuppressWarnings("unchecked")
	private List<TypeCoverage> rows(ReactContext context) {
		if (_rowsChannel != null && context instanceof ViewContext viewContext
			&& viewContext.hasChannel(_rowsChannel)) {
			Object value = viewContext.resolveChannel(new ChannelRef(_rowsChannel)).get();
			if (value instanceof List<?> list) {
				return (List<TypeCoverage>) list;
			}
		}
		return analyze();
	}

	/**
	 * A fresh analysis of the access definition.
	 */
	private static List<TypeCoverage> analyze() {
		return SecurityCoverageCheck.getInstance().analyze();
	}
}
