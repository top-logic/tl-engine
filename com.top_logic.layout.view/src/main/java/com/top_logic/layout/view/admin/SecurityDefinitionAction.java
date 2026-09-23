/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} editing the model based access definition from the security coverage display:
 * it creates, prefills, validates and stores the rules the analysis reports about, and makes the
 * stored definition effective.
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the coverage views rather than claiming a
 * global {@code @TagName}. What the action does is decided by its {@link Config#getMode() mode}: it
 * either produces the rule a dialog edits, stores the edited rule, or applies everything stored so
 * far. A mode that stores something returns the marker a channel carries to tell the user that the
 * files and the running definition differ; applying clears that marker and returns the rows of a
 * fresh analysis.
 * </p>
 *
 * <p>
 * The analyzed type to work on is the action's input. A command whose input is another channel -
 * because an executability rule inspects that channel - names the channel holding the selected type
 * in {@link Config#getSelection() selection} instead.
 * </p>
 *
 * @implNote All file and service operations are delegated to {@link SecurityDefinitionEditor}, so
 *           that this action only maps between the view layer and that editor: it resolves the
 *           input, reports a failure of the editor as a {@link TopLogicException} and hands the
 *           result back to the chain. The marker of the stored but unapplied changes is
 *           {@link Boolean#TRUE}, so that a view displays it with the plain predicate
 *           {@code p -> $p != null}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityDefinitionAction implements ViewAction {

	/**
	 * Suffix of the id proposed for a new role rule, appended to the name of the type it applies
	 * to.
	 *
	 * @see SecurityCoverageAnalysis#SUGGESTED_RULE_ID_SUFFIX The counterpart for a security parent
	 *      rule.
	 */
	public static final String ROLE_RULE_ID_SUFFIX = "_roleRule";

	/**
	 * What a {@link SecurityDefinitionAction} does.
	 */
	public enum Mode {
		/** Store the security parent rule the analysis proposes for the selected type. */
		ACCEPT_PROPOSAL,

		/** Create the security parent rule to edit for the selected type. */
		NEW_SECURITY_PARENT_RULE,

		/** Create the role rule to edit for the selected type. */
		NEW_ROLE_RULE,

		/** Fetch the security parent rule with the input id for editing. */
		EDIT_SECURITY_PARENT_RULE,

		/** Fetch the role rule with the input id for editing. */
		EDIT_ROLE_RULE,

		/** Store the edited security parent rule. */
		SAVE_SECURITY_PARENT_RULE,

		/** Store the edited role rule. */
		SAVE_ROLE_RULE,

		/** Make the stored definition effective and analyze it again. */
		APPLY;
	}

	/**
	 * Configuration for {@link SecurityDefinitionAction}.
	 */
	public interface Config extends PolymorphicConfiguration<SecurityDefinitionAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(SecurityDefinitionAction.class)
		Class<? extends SecurityDefinitionAction> getImplementationClass();

		/**
		 * What the action does.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the analyzed type to work on, for a command whose input is
		 * another channel.
		 */
		@Name(SELECTION)
		@Nullable
		String getSelection();
	}

	private final Mode _mode;

	private final String _selectionChannel;

	private SecurityDefinitionEditor _editor;

	/**
	 * Creates a new {@link SecurityDefinitionAction} from configuration.
	 */
	@CalledByReflection
	public SecurityDefinitionAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_selectionChannel = config.getSelection();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return switch (_mode) {
			case ACCEPT_PROPOSAL -> acceptProposal(coverage(context, input));
			case NEW_SECURITY_PARENT_RULE -> newSecurityParentRule(coverage(context, input));
			case NEW_ROLE_RULE -> newRoleRule(coverage(context, input));
			case EDIT_SECURITY_PARENT_RULE -> editSecurityParentRule(ruleId(input));
			case EDIT_ROLE_RULE -> editRoleRule(ruleId(input));
			case SAVE_SECURITY_PARENT_RULE -> saveSecurityParentRule(rule(input, NavigationRuleConfig.class));
			case SAVE_ROLE_RULE -> saveRoleRule(rule(input, RoleRuleConfig.class));
			case APPLY -> apply();
		};
	}

	/**
	 * Stores the security parent rule proposed for the given type.
	 */
	private Object acceptProposal(TypeCoverage coverage) {
		if (editor().proposedRule(coverage) == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_PROPOSED_RULE);
		}
		store(() -> editor().acceptProposal(coverage));
		return Boolean.TRUE;
	}

	/**
	 * The security parent rule to edit for the given type: the proposed one where the analysis has
	 * a proposal, an empty rule for the type otherwise.
	 */
	private Object newSecurityParentRule(TypeCoverage coverage) {
		NavigationRuleConfig proposal = editor().proposedRule(coverage);
		if (proposal != null) {
			return proposal;
		}
		NavigationRuleConfig rule = TypedConfiguration.newConfigItem(NavigationRuleConfig.class);
		prefill(rule, coverage, SecurityCoverageAnalysis.SUGGESTED_RULE_ID_SUFFIX);
		return rule;
	}

	/**
	 * An empty role rule for the given type.
	 */
	private Object newRoleRule(TypeCoverage coverage) {
		RoleRuleConfig rule = TypedConfiguration.newConfigItem(RoleRuleConfig.class);
		prefill(rule, coverage, ROLE_RULE_ID_SUFFIX);
		return rule;
	}

	/**
	 * Names the given rule after the given type and lets it apply to the sub types as well.
	 */
	private static void prefill(NavigationRuleConfig rule, TypeCoverage coverage, String idSuffix) {
		rule.setId(coverage.type().getName() + idSuffix);
		rule.setMetaElement(TLModelUtil.qualifiedName(coverage.type()));
		rule.setInherit(true);
	}

	/**
	 * The security parent rule with the given id, as a copy to edit.
	 */
	private Object editSecurityParentRule(String id) {
		NavigationRuleConfig rule = editor().editableSecurityParentRule(id);
		if (rule == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_SECURITY_PARENT_RULE__ID.fill(id));
		}
		return rule;
	}

	/**
	 * The role rule with the given id, as a copy to edit.
	 */
	private Object editRoleRule(String id) {
		RoleRuleConfig rule = editor().editableRoleRule(id);
		if (rule == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_ROLE_RULE__ID.fill(id));
		}
		return rule;
	}

	/**
	 * Stores the given security parent rule after checking that it can navigate anywhere.
	 */
	private Object saveSecurityParentRule(NavigationRuleConfig rule) {
		checkCommon(rule);
		if (rule.getPathElements().isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_RULE_PATH);
		}
		store(() -> editor().putSecurityParentRule(rule));
		return Boolean.TRUE;
	}

	/**
	 * Stores the given role rule after checking that it delivers a role.
	 */
	private Object saveRoleRule(RoleRuleConfig rule) {
		checkCommon(rule);
		if (rule.getRole().isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_RULE_ROLE);
		}
		store(() -> editor().putRoleRule(rule));
		return Boolean.TRUE;
	}

	/**
	 * Checks what every rule needs: a name to be stored under and a type to apply to.
	 */
	private static void checkCommon(NavigationRuleConfig rule) {
		String id = rule.getId();
		if (id == null || id.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_RULE_ID);
		}
		String type = rule.getMetaElement();
		if (type == null || type.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_RULE_TYPE);
		}
	}

	/**
	 * Makes the stored definition effective and returns the rows of a fresh analysis.
	 */
	private Object apply() {
		editor().apply();
		return analyze();
	}

	/**
	 * The analyzed type to work on: the input where it is one, the value of the configured
	 * selection channel otherwise.
	 */
	private TypeCoverage coverage(ReactContext context, Object input) {
		if (input instanceof TypeCoverage coverage) {
			return coverage;
		}
		Object selected = channelValue(context, _selectionChannel);
		if (selected instanceof TypeCoverage coverage) {
			return coverage;
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_TYPE_SELECTED);
	}

	/**
	 * The id of the rule to edit.
	 */
	private static String ruleId(Object input) {
		if (input instanceof String id && !id.isBlank()) {
			return id;
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_RULE_SELECTED);
	}

	/**
	 * The edited rule the dialog hands over.
	 */
	private static <R extends NavigationRuleConfig> R rule(Object input, Class<R> ruleType) {
		if (ruleType.isInstance(input)) {
			return ruleType.cast(input);
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_RULE_SELECTED);
	}

	/**
	 * The value of the channel with the given name, or <code>null</code> when the view has no such
	 * channel.
	 */
	private static Object channelValue(ReactContext context, String channelName) {
		if (channelName == null || !(context instanceof ViewContext viewContext)
			|| !viewContext.hasChannel(channelName)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(channelName)).get();
	}

	/**
	 * The editor of the access definition, created when it is first used.
	 *
	 * @implNote The editor resolves the files of the running application, which a view being
	 *           loaded must not depend on, so it is not created in the constructor.
	 */
	private SecurityDefinitionEditor editor() {
		if (_editor == null) {
			_editor = new SecurityDefinitionEditor();
		}
		return _editor;
	}

	/**
	 * Runs the given file operation, reporting its failure as a message naming the file.
	 */
	private void store(FileOperation operation) {
		try {
			operation.run();
		} catch (IOException | ConfigurationException ex) {
			throw new TopLogicException(errorWritingFile(), ex);
		}
	}

	/**
	 * The message telling the user which file could not be written.
	 */
	private ResKey errorWritingFile() {
		return I18NConstants.ERROR_WRITING_ACCESS_DEFINITION__FILE
			.fill(editor().getAccessManagerFile().getAbsolutePath());
	}

	/**
	 * A fresh analysis of the access definition.
	 */
	private static List<TypeCoverage> analyze() {
		return SecurityCoverageCheck.getInstance().analyze();
	}

	/**
	 * An operation of the editor that writes a file.
	 */
	private interface FileOperation {

		/**
		 * Performs the operation.
		 */
		void run() throws IOException, ConfigurationException;
	}
}
