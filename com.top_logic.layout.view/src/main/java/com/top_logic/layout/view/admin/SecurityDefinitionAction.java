/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.logging.Level;
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
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.security.SecurityConfigurationService.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} editing the model based access definition from the security coverage display:
 * it creates, prefills, validates and stores the rules and the access rights the analysis reports
 * about, and makes the stored definition effective.
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the coverage views rather than claiming a
 * global {@code @TagName}. What the action does is decided by its {@link Config#getMode() mode}: it
 * produces the rule or the access rights a dialog edits, stores what the dialog hands back, flips
 * one of the marks a type carries, or applies everything stored so far. A mode that stores
 * something returns the marker a channel carries to tell the user that the files and the running
 * definition differ; applying clears that marker and returns the rows of a fresh analysis.
 * </p>
 *
 * <p>
 * What a dialog edits are the additions the application makes for a type, not the rights that are
 * in effect: the stored grants are appended to the grants of the underlying configuration layers,
 * and the stored marks override the ones those layers set.
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

		/**
		 * Store the security parent rules of the proposal entries given as input, a single entry or
		 * a collection of them as a table's selection holds them.
		 */
		ACCEPT_PROPOSALS,

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

		/** Drop the security parent rule with the input id from the stored configuration. */
		REMOVE_SECURITY_PARENT_RULE,

		/** Drop the role rule with the input id from the stored configuration. */
		REMOVE_ROLE_RULE,

		/** Fetch the access rights of the selected type for editing. */
		EDIT_ACCESS_RIGHTS,

		/** Fetch the access rights of the module of the selected type for editing. */
		EDIT_MODULE_ACCESS_RIGHTS,

		/** Store the edited access rights. */
		SAVE_ACCESS_RIGHTS,

		/** Mark the selected type as used by the application code alone. */
		MARK_INTERNAL,

		/** Drop the internal mark of the selected type. */
		UNMARK_INTERNAL,

		/** Exclude the selected type from access control. */
		MARK_WITHOUT_SECURITY,

		/** Put the selected type under access control again. */
		UNMARK_WITHOUT_SECURITY,

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
			case ACCEPT_PROPOSALS -> acceptProposals(input);
			case NEW_SECURITY_PARENT_RULE -> newSecurityParentRule(coverage(context, input));
			case NEW_ROLE_RULE -> newRoleRule(coverage(context, input));
			case EDIT_SECURITY_PARENT_RULE -> editSecurityParentRule(ruleId(input));
			case EDIT_ROLE_RULE -> editRoleRule(ruleId(input));
			case SAVE_SECURITY_PARENT_RULE -> saveSecurityParentRule(rule(input, NavigationRuleConfig.class));
			case SAVE_ROLE_RULE -> saveRoleRule(rule(input, RoleRuleConfig.class));
			case REMOVE_SECURITY_PARENT_RULE -> removeSecurityParentRule(ruleId(input));
			case REMOVE_ROLE_RULE -> removeRoleRule(ruleId(input));
			case EDIT_ACCESS_RIGHTS -> editAccessRights(coverage(context, input));
			case EDIT_MODULE_ACCESS_RIGHTS -> editModuleAccessRights(coverage(context, input));
			case SAVE_ACCESS_RIGHTS -> saveAccessRights(accessRights(input));
			case MARK_INTERNAL -> setInternal(coverage(context, input), true);
			case UNMARK_INTERNAL -> setInternal(coverage(context, input), false);
			case MARK_WITHOUT_SECURITY -> setWithoutSecurity(coverage(context, input), true);
			case UNMARK_WITHOUT_SECURITY -> setWithoutSecurity(coverage(context, input), false);
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
	 * Stores the proposed rules of the given proposal entries, see
	 * {@link SecurityCoverageAction#PROPOSAL_COVERAGE}.
	 */
	private Object acceptProposals(Object input) {
		Collection<?> entries = input instanceof Collection<?> collection ? collection : List.of(input);
		List<TypeCoverage> coverage = entries.stream()
			.filter(Map.class::isInstance)
			.map(entry -> ((Map<?, ?>) entry).get(SecurityCoverageAction.PROPOSAL_COVERAGE))
			.filter(TypeCoverage.class::isInstance)
			.map(TypeCoverage.class::cast)
			.toList();
		if (coverage.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_NO_PROPOSED_RULE);
		}
		store(() -> editor().acceptProposals(coverage));
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
	 * Drops the security parent rule with the given id from the stored configuration.
	 */
	private Object removeSecurityParentRule(String id) {
		remove(() -> editor().removeSecurityParentRule(id), id);
		return Boolean.TRUE;
	}

	/**
	 * Drops the role rule with the given id from the stored configuration.
	 */
	private Object removeRoleRule(String id) {
		remove(() -> editor().removeRoleRule(id), id);
		return Boolean.TRUE;
	}

	/**
	 * Runs the given removal, reporting a rule the stored configuration does not define as one the
	 * base configuration declares.
	 *
	 * <p>
	 * Layering a file onto the configuration underneath can add a rule and replace one, but it
	 * cannot take one away: a rule the application's own file does not define is removed where it
	 * is declared, or overridden by an edited rule of the same id.
	 * </p>
	 */
	private void remove(FileRemoval removal, String id) {
		boolean removed;
		try {
			removed = removal.run();
		} catch (IOException | ConfigurationException ex) {
			throw new TopLogicException(errorWritingFile(editor().getAccessManagerFile()), ex);
		}
		if (!removed) {
			throw new TopLogicException(I18NConstants.ERROR_BASE_RULE_NOT_REMOVABLE__ID.fill(id));
		}
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
	 * The access rights stored for the given type, as a copy to edit.
	 *
	 * <p>
	 * The copy holds the additions the application makes for the type, not the rights that are in
	 * effect: its grants are appended to the grants of the underlying configuration layers when it
	 * is applied.
	 * </p>
	 */
	private Object editAccessRights(TypeCoverage coverage) {
		return load(() -> editor().editableAccessRights(coverage.type()));
	}

	/**
	 * The access rights stored for the module of the given type, as a copy to edit.
	 *
	 * <p>
	 * They apply to every class of that module, in the same additive way the rights of a single
	 * type do.
	 * </p>
	 */
	private Object editModuleAccessRights(TypeCoverage coverage) {
		return load(() -> editor().editableAccessRights(coverage.type().getModule()));
	}

	/**
	 * Stores the given access rights after checking that they name the model element they apply to
	 * and that every rule of them names an operation.
	 */
	private Object saveAccessRights(ModelAccessRights entry) {
		String name = entry.getName();
		if (name == null || name.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_ACCESS_RIGHTS_NAME);
		}
		for (AccessRule grant : entry.getGrants()) {
			if (grant.getOperation() == null) {
				throw new TopLogicException(I18NConstants.ERROR_MISSING_GRANT_OPERATION);
			}
		}
		checkConstraints(entry);
		store(() -> editor().putAccessRights(entry), grantsFile());
		return Boolean.TRUE;
	}

	/**
	 * Rejects access rights violating a constraint of their configuration, such as the two marks
	 * of a type set together.
	 */
	private static void checkConstraints(ModelAccessRights entry) {
		BufferingI18NLog log = new BufferingI18NLog();
		new ConstraintChecker().check(log, entry);
		ResKey[] errors = log.getEntries().stream()
			.filter(event -> event.getLevel() == Level.ERROR)
			.map(BufferingI18NLog.Entry::getMessage)
			.toArray(ResKey[]::new);
		if (errors.length > 0) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_RIGHTS_INVALID__ERRORS.fill(errors));
		}
	}

	/**
	 * Marks the given type as used by the application's own code only, or drops that mark.
	 *
	 * <p>
	 * The value is stored as given, whatever the files held before: the command offering the mark
	 * is chosen by what the analysis reports for the type, so the user asks for the state the
	 * table does not show yet.
	 * </p>
	 */
	private Object setInternal(TypeCoverage coverage, boolean value) {
		TLClass type = coverage.type();
		store(() -> editor().setInternal(type, value), grantsFile());
		return Boolean.TRUE;
	}

	/**
	 * Excludes the given type from access control, or drops that exclusion.
	 *
	 * @see #setInternal(TypeCoverage, boolean)
	 */
	private Object setWithoutSecurity(TypeCoverage coverage, boolean value) {
		TLClass type = coverage.type();
		store(() -> editor().setWithoutSecurity(type, value), grantsFile());
		return Boolean.TRUE;
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
	 * The edited access rights the dialog hands over.
	 */
	private static ModelAccessRights accessRights(Object input) {
		if (input instanceof ModelAccessRights entry) {
			return entry;
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_ACCESS_RIGHTS_SELECTED);
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
	 * Runs the given operation on the file holding the rules, reporting its failure as a message
	 * naming that file.
	 */
	private void store(FileOperation operation) {
		store(operation, editor().getAccessManagerFile());
	}

	/**
	 * Runs the given operation on the given file, reporting its failure as a message naming that
	 * file.
	 */
	private static void store(FileOperation operation, File file) {
		try {
			operation.run();
		} catch (IOException | ConfigurationException ex) {
			throw new TopLogicException(errorWritingFile(file), ex);
		}
	}

	/**
	 * Answers the given query about the file holding the grants, reporting its failure as a message
	 * naming that file.
	 */
	private <T> T load(FileQuery<T> query) {
		try {
			return query.run();
		} catch (ConfigurationException ex) {
			throw new TopLogicException(errorReadingFile(grantsFile()), ex);
		}
	}

	/**
	 * The file holding the grants.
	 */
	private File grantsFile() {
		return editor().getGrantsFile();
	}

	/**
	 * The message telling the user which file could not be written.
	 */
	private static ResKey errorWritingFile(File file) {
		return I18NConstants.ERROR_WRITING_ACCESS_DEFINITION__FILE.fill(file.getAbsolutePath());
	}

	/**
	 * The message telling the user which file could not be read.
	 */
	private static ResKey errorReadingFile(File file) {
		return I18NConstants.ERROR_READING_ACCESS_DEFINITION__FILE.fill(file.getAbsolutePath());
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

	/**
	 * An operation of the editor that drops something from a file.
	 */
	private interface FileRemoval {

		/**
		 * Performs the operation.
		 *
		 * @return Whether the stored configuration defined what was to be dropped.
		 */
		boolean run() throws IOException, ConfigurationException;
	}

	/**
	 * An operation of the editor that reads a file.
	 */
	private interface FileQuery<T> {

		/**
		 * Performs the operation.
		 */
		T run() throws ConfigurationException;
	}
}
