/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.EffectiveToolAccess;
import com.top_logic.tl.ai.core.security.EffectiveToolAccess.CapabilityAccess;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.core.security.ToolAccessExplanation;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.util.Resources;

/*
 * PR #306 ("Explain the agent tool access policy in an administration view"). No issue tracks it.
 *
 * The class comment below is exported to META-INF/messages_*.properties by the build's doclet and shown
 * as this component's tooltip in the layout editor, so it is written for an administrator and stays
 * short - TestConfigPresentation enforces a length limit on everything under this package prefix.
 * The design notes live here, where the doclet does not export them.
 *
 * Why a component of its own rather than a table with a selection master: the answer depends on two
 * inputs at once (who, and in which run mode), the second of which is not an object any table could
 * select, and the rows are recomputed from both. Keeping the two inputs as component state and
 * rebuilding the form on a change is the simplest thing that keeps the table, the header line and the
 * footer warnings from ever disagreeing about which account they describe.
 *
 * Read-only throughout, and every verdict comes from EffectiveToolAccess - the read-only twin of the
 * enforcement path - so the view cannot claim an access a real run would be refused, and cannot change
 * anything either. The policy itself is still edited in the service editor.
 */

/**
 * Shows what one account or role may call, and which rule of the policy decided.
 *
 * <p>
 * Read-only, and the one view that answers for the <em>running</em> service: what is saved but not yet
 * in effect is not shown here. The policy itself is written on the Richtlinie tab.
 * </p>
 *
 * @see ToolAccessPolicyComponent
 * @see ToolCatalogModelBuilder
 */
public class EffectiveToolAccessComponent extends FormComponent implements ControlRepresentable {

	/** Name of the account/role selection field. */
	public static final String FIELD_SUBJECT = "subject";

	/** Name of the run-mode selection field. */
	public static final String FIELD_MODE = "mode";

	/** Name of the tree table holding the capability rows. */
	public static final String FIELD_ROWS = "rows";

	/**
	 * Configuration options for {@link EffectiveToolAccessComponent}.
	 */
	public interface Config extends FormComponent.Config {

		@Override
		@ClassDefault(EffectiveToolAccessComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/** The account being asked about, or <code>null</code> if a role is. */
	private Person _account;

	/** The role being asked about hypothetically, or <code>null</code> if an account is. */
	private String _role;

	private boolean _background;

	private Control _renderingControl;

	/** The policy the rows were computed from, to notice a service restart. */
	private ToolAccessPolicy _shownPolicy;

	/** The line naming the roles the chosen subject holds; repainted when the subject changes. */
	private DynamicFragment _subjectInfo;

	/** The notes under the table; repainted when the subject or the run mode changes. */
	private DynamicFragment _footer;

	/**
	 * Creates an {@link EffectiveToolAccessComponent} from configuration.
	 */
	@CalledByReflection
	public EffectiveToolAccessComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// The view is about the policy in effect and about a subject chosen inside it; there is no
		// business object it could be opened on.
		return object == null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		context.addMember(createSubjectField());
		context.addMember(createModeField());
		context.addMember(createRowsField());
		return context;
	}

	/**
	 * Recomputes everything below the two inputs after one of them changed.
	 *
	 * <p>
	 * Three things depend on both inputs and on nothing else: the rows, the line naming the roles, and
	 * which cross-layer warnings are worth showing. All three are updated together and from the same
	 * evaluation, so that the table and the sentences under it can never describe two different
	 * accounts.
	 * </p>
	 */
	private void rebuild() {
		FormContext context = getFormContext();
		if (context != null && context.hasMember(FIELD_ROWS)) {
			((TreeTableField) context.getMember(FIELD_ROWS)).setTree(buildTree());
		}
		if (_subjectInfo != null) {
			_subjectInfo.requestRepaint();
		}
		if (_footer != null) {
			_footer.requestRepaint();
		}
	}

	/**
	 * Recomputes the answer when the policy the running service applies has changed.
	 *
	 * <p>
	 * The rows are a snapshot, and the policy that service applies changes while this view exists - the
	 * Richtlinie tab restarts the service. Without this, an administrator who applies a change, restarts
	 * the service and comes back here reads the answer from before the restart, which is the one thing
	 * this view must never show. Conditional rather than unconditional so that switching tabs does not
	 * collapse the rows a reader had expanded.
	 * </p>
	 */
	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		ToolAccessPolicy running = AiToolAccessService.Module.INSTANCE.isActive()
			? AiToolAccessService.policy() : null;
		if (_shownPolicy != running) {
			// Identity, not equality: restarting the service is what replaces the item, and a policy
			// that was rewritten to the same content answers the same way anyway.
			_shownPolicy = running;
			rebuild();
		}
	}

	@Override
	protected void installFormContext(FormContext newFormContext) {
		super.installFormContext(newFormContext);
		// The rendering control wraps the fields of the form context that is being replaced.
		_renderingControl = null;
	}

	@Override
	public Control getRenderingControl() {
		if (_renderingControl == null) {
			_renderingControl = new FragmentControl(view());
		}
		return _renderingControl;
	}

	/**
	 * The whole view: what it answers, the two inputs, the rows, and the notes that keep the rows from
	 * being over-read.
	 */
	private HTMLFragment view() {
		FormContext context = getFormContext();
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return div("aiSecurityView", p(message(I18NConstants.SERVICE_NOT_RUNNING)));
		}
		_subjectInfo = new DynamicFragment("aiSecuritySubjectInfo", () -> text(subjectInfo()));
		_footer = new DynamicFragment("aiSecurityFooter", this::footer);
		return div("aiSecurityView",
			div("aiSecurityInputs",
				labelWithColon(context.getMember(FIELD_SUBJECT)),
				value(context.getMember(FIELD_SUBJECT)),
				labelWithColon(context.getMember(FIELD_MODE)),
				value(context.getMember(FIELD_MODE)),
				_subjectInfo),
			div("aiSecurityRows",
				TableTag.createTableControl((TreeTableField) context.getMember(FIELD_ROWS))),
			_footer);
	}

	/** The roles the chosen subject holds, next to the picker. */
	private String subjectInfo() {
		Set<String> roles = new TreeSet<>(access().roleNames());
		if (roles.isEmpty()) {
			return Resources.getInstance().getString(I18NConstants.HELD_ROLES_NONE);
		}
		return Resources.getInstance()
			.getString(I18NConstants.HELD_ROLES__ROLES.fill(String.join(", ", roles)));
	}

	/**
	 * The notes under the table: what this view does not answer, what the chosen mode means, and the
	 * cross-layer warnings that would otherwise only ever reach the boot log.
	 */
	private HTMLFragment footer() {
		List<HTMLFragment> notes = new ArrayList<>();
		if (_account != null && _account.isAdmin()) {
			notes.add(div("aiSecurityWarning",
				message(I18NConstants.ADMIN_BANNER__ACCOUNT.fill(_account.getName()))));
		}
		notes.add(div("aiSecurityNote", message(I18NConstants.LAYER_ONE_ONLY_NOTE)));
		notes.add(div("aiSecurityNote", message(I18NConstants.RUN_SECTIONS_NOTE)));
		if (_background) {
			notes.add(div("aiSecurityNote", message(I18NConstants.BACKGROUND_MODE_NOTE)));
		}
		for (ResKey warning : crossLayerWarnings()) {
			notes.add(div("aiSecurityWarning", message(warning)));
		}
		return concat(notes.toArray(new HTMLFragment[notes.size()]));
	}

	/**
	 * The boot-log warnings of {@link AiToolAccessService}, restricted to the roles this view is
	 * showing.
	 *
	 * <p>
	 * Restricted on purpose: a warning about a role the reader is not looking at is noise, and the two
	 * checks are about a role rather than about an account, so the roles of the chosen subject are
	 * exactly the ones worth surfacing here.
	 * </p>
	 */
	private List<ResKey> crossLayerWarnings() {
		AiToolAccessService service = AiToolAccessService.getInstance();
		Set<String> shownRoles = access().roleNames();
		List<ResKey> result = new ArrayList<>();
		Set<String> withoutSecurity = AiToolAccessService.partsWithoutSecurity();
		for (Map.Entry<String, Set<String>> entry : service.dataRolesWithoutModelRights().entrySet()) {
			if (shownRoles.contains(entry.getKey())) {
				result.add(PolicyChecks.dataRoleWithoutModelRights(entry.getKey(), entry.getValue(),
					withoutSecurity));
			}
		}
		for (String roleName : service.rolesUnreachableOnSecurityRoot()) {
			if (shownRoles.contains(roleName)) {
				result.add(I18NConstants.WARN_ROLE_UNREACHABLE_ON_ROOT__ROLE.fill(roleName));
			}
		}
		return result;
	}

	/** The evaluation for the chosen subject and run mode. */
	private EffectiveToolAccess access() {
		if (_role != null) {
			return AiToolAccessService.effectiveAccessForRoles(Set.of(_role), _background);
		}
		return AiToolAccessService.effectiveAccessFor(_account, _background);
	}

	private SelectField createSubjectField() {
		List<Object> options = new ArrayList<>(accounts());
		options.addAll(hypotheticalRoles());

		SelectField field = FormFactory.newSelectField(FIELD_SUBJECT, options, false,
			List.of(currentSubject(options)), true, false, null);
		field.setOptionLabelProvider(SUBJECT_LABELS);
		field.addValueListener((changed, oldValue, newValue) -> {
			Object subject = newValue instanceof List<?> list && !list.isEmpty() ? list.get(0) : null;
			_account = subject instanceof Person person ? person : null;
			_role = subject instanceof String roleName ? roleName : null;
			rebuild();
		});
		return field;
	}

	/**
	 * The subject to preselect: the one already chosen, or the first option so that the view answers
	 * something the moment it opens.
	 */
	private Object currentSubject(List<Object> options) {
		if (_role != null) {
			return _role;
		}
		if (_account != null) {
			return _account;
		}
		Object first = options.isEmpty() ? null : options.get(0);
		if (first instanceof Person person) {
			_account = person;
		} else if (first instanceof String roleName) {
			_role = roleName;
		}
		return first;
	}

	private SelectField createModeField() {
		SelectField field = FormFactory.newSelectField(FIELD_MODE,
			List.of(Boolean.FALSE, Boolean.TRUE), false, List.of(Boolean.valueOf(_background)),
			true, false, null);
		field.setOptionLabelProvider(MODE_LABELS);
		field.addValueListener((changed, oldValue, newValue) -> {
			_background = newValue instanceof List<?> list && !list.isEmpty()
				&& Boolean.TRUE.equals(list.get(0));
			rebuild();
		});
		return field;
	}

	/**
	 * The rows: one node per capability group, its tools as children, and the unclassified tools as
	 * rows of their own.
	 */
	private TreeTableField createRowsField() {
		return FormFactory.newTreeTableField(FIELD_ROWS, buildTree());
	}

	/** The rows for the currently chosen subject and run mode. */
	private DefaultTreeTableModel buildTree() {
		TableConfiguration config =
			TableConfigurationFactory.build(lookupTableConfigurationBuilder(FIELD_ROWS));
		DefaultTreeTableModel tree = new DefaultTreeTableModel(new DefaultTreeTableBuilder(),
			"aiSecurityRoot", new ArrayList<>(config.getDefaultColumns()), config);
		tree.setRootVisible(false);

		if (AiToolAccessService.Module.INSTANCE.isActive()) {
			fill(tree.getRoot(), access());
		}
		return tree;
	}

	private void fill(DefaultTreeTableNode root, EffectiveToolAccess access) {
		fillRunSections(root);
		for (CapabilityAccess capability : access.capabilities()) {
			DefaultTreeTableNode node = root.createChild(new EffectiveAccessRow.Capability(
				capability.group(), capability.explanation(), capability.tools().size(),
				scriptCommitNote(capability.group(), access)));
			for (Tool tool : capability.tools()) {
				node.createChild(new EffectiveAccessRow.ToolRow(access.qualifiedName(tool),
					access.explain(tool)));
			}
		}
		// The tools nobody classified. Denied to everyone by a mechanism no capability grant can
		// change, which is why they cannot sit under a group and get rows of their own.
		for (Tool tool : access.unclassifiedTools()) {
			root.createChild(new EffectiveAccessRow.ToolRow(access.qualifiedName(tool),
				access.explain(tool)));
		}
	}

	/**
	 * The three sections that decide what a run may <em>be</em>, above the capabilities that decide
	 * what it may do.
	 *
	 * <p>
	 * Asked with the same two inputs the chat's menu is filtered with: the candidates the application
	 * has, and what the agents this account may run use themselves. A model an agent is assigned is
	 * not a choice, and a row that called it one would report a restriction that does not exist.
	 * </p>
	 */
	private void fillRunSections(DefaultTreeTableNode root) {
		RunAuthorization access = _account != null
			? AiToolAccessService.effectiveRunAccessFor(_account)
			: AiToolAccessService.effectiveRunAccessForRoles(_role == null ? Set.of() : Set.of(_role));

		AgentFacts facts = AgentFacts.live();
		List<String> agents = names(Dimension.AGENT);
		// The agent section is not the only one that can stop an agent: one whose own model, or one of
		// whose own servers, a role forbids cannot run at all - the chat's menu, the fallback agent and
		// the delegate catalog all drop it, and a view that counted it as reachable would be the one
		// place in the product that disagrees with the gate. Its row says why instead of vanishing.
		// An infrastructure agent is stopped for a third reason that no role decides: the application
		// runs it for the user and nobody converses with it (#397). Its row says so rather than
		// reporting a grant that the gate would refuse.
		Map<String, Decision.Denied> stopped = new LinkedHashMap<>();
		List<String> allowedAgents = new ArrayList<>();
		for (String agentName : access.allowedAgents(agents)) {
			if (facts.isInfrastructure(agentName)) {
				stopped.put(agentName, new Decision.Denied(Dimension.AGENT, agentName, null,
					RoleAccessPolicy.Cause.INFRASTRUCTURE_AGENT));
				continue;
			}
			Decision.Denied own =
				access.agentsOwnRefusal(facts.assignedModel(agentName), facts.declaredServers(agentName));
			if (own == null) {
				allowedAgents.add(agentName);
			} else {
				stopped.put(agentName, own);
			}
		}
		addSection(root, Dimension.AGENT, agents, allowedAgents, Set.of(), stopped);

		Set<String> assignedModels = new TreeSet<>();
		Set<String> declaredServers = new TreeSet<>();
		for (String agentName : allowedAgents) {
			declaredServers.addAll(facts.declaredServers(agentName));
			String model = facts.assignedModel(agentName);
			if (model != null) {
				assignedModels.add(model);
			}
		}
		List<String> models = names(Dimension.MODEL);
		addSection(root, Dimension.MODEL, models, access.allowedModels(models, assignedModels),
			assignedModels, Map.of());
		List<String> servers = names(Dimension.MCP_SERVER);
		addSection(root, Dimension.MCP_SERVER, servers,
			access.allowedMcpServers(servers, declaredServers), declaredServers, Map.of());
	}

	/**
	 * One heading row and one row per candidate, with the rule that decided each.
	 *
	 * @param stopped
	 *        The candidates its own section permits and something else stops, with the refusal that
	 *        stops them; empty for every dimension but {@link Dimension#AGENT}.
	 */
	private void addSection(DefaultTreeTableNode root, Dimension dimension, List<String> candidates,
			List<String> allowed, Set<String> agentsOwn, Map<String, Decision.Denied> stopped) {
		DefaultTreeTableNode node = root.createChild(
			new EffectiveAccessRow.RunSection(dimension, allowed.size(), candidates.size()));
		RoleAccessPolicy policy = sectionOf(dimension);
		Set<String> roleNames = _account != null
			? AiToolAccessService.heldRoleNames(_account)
			: (_role == null ? Set.of() : Set.of(_role));
		for (String candidate : candidates) {
			Decision.Denied own = stopped.get(candidate);
			node.createChild(own == null
				? new EffectiveAccessRow.RunName(dimension, candidate,
					policy.check(roleNames, candidate, agentsOwn))
				: new EffectiveAccessRow.RunName(dimension, candidate, own, stoppedBecause(candidate, own)));
		}
	}

	/** What the row of an agent says that something other than the agent section stops. */
	private static String stoppedBecause(String agentName, Decision.Denied own) {
		if (own.cause() == RoleAccessPolicy.Cause.INFRASTRUCTURE_AGENT) {
			// No role is involved, so the user-facing sentence is the whole explanation.
			return Resources.getInstance().getString(own.reason());
		}
		return Resources.getInstance().getString(I18NConstants.AGENT_USES_DENIED__AGENT_NAME_ROLE
			.fill(agentName, own.name(), own.byRole()));
	}

	/** The evaluation of one of the three sections of the running policy. */
	private static RoleAccessPolicy sectionOf(Dimension dimension) {
		AiToolAccessService service = AiToolAccessService.getInstance();
		switch (dimension) {
			case MODEL:
				return service.getModelAccess();
			case MCP_SERVER:
				return service.getMcpAccess();
			default:
				return service.getAgentAccess();
		}
	}

	/** The names of one dimension the application has, in a stable order. */
	private static List<String> names(Dimension dimension) {
		Set<String> known = AiToolAccessService.knownNames(dimension);
		return known == null ? List.of() : new ArrayList<>(known);
	}

	/**
	 * The sentence the {@code script} group needs and no other group does.
	 *
	 * <p>
	 * A change with {@code changeData} - every call but a dry run - costs
	 * {@link ToolGroups#DATA_WRITE} on top of the {@link ToolGroups#SCRIPT} the tool is classified as. That price is paid through a tool
	 * <em>argument</em>, so no row of a capability table can carry it and it has to be said in words -
	 * wherever the script capability appears.
	 * </p>
	 *
	 * @return The note, or <code>null</code> for every group but {@link ToolGroups#SCRIPT}.
	 */
	private static String scriptCommitNote(String group, EffectiveToolAccess access) {
		if (!ToolGroups.SCRIPT.equals(group)) {
			return null;
		}
		ToolAccessExplanation commit = access.explainCapability(ToolGroups.DATA_WRITE);
		return Resources.getInstance().getString(commit.allowed()
			? I18NConstants.SCRIPT_COMMIT_ALLOWED : I18NConstants.SCRIPT_COMMIT_DENIED);
	}

	/** Every account of the application, by login. */
	private static List<Person> accounts() {
		List<Person> result = new ArrayList<>(Person.all());
		result.sort((left, right) -> left.getName().compareToIgnoreCase(right.getName()));
		return result;
	}

	/**
	 * The roles worth asking about without an account: the ones the policy configures.
	 *
	 * <p>
	 * Not every role of the application. The hypothetical question an administrator has is "what would
	 * this entry of my policy give somebody", and a role the policy says nothing about reaches exactly
	 * the baseline - which every account in the list above already shows.
	 * </p>
	 */
	private static List<String> hypotheticalRoles() {
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return List.of();
		}
		return new ArrayList<>(new TreeSet<>(AiToolAccessService.policy().getToolAccess().keySet()));
	}

	/** How an account or a role is offered in the picker. */
	private static final LabelProvider SUBJECT_LABELS = model -> {
		if (model instanceof Person person) {
			return person.getName();
		}
		if (model instanceof String roleName) {
			return Resources.getInstance()
				.getString(I18NConstants.SUBJECT_ROLE_OPTION__ROLE.fill(roleName));
		}
		return String.valueOf(model);
	};

	/**
	 * A block of the view that is recomputed and repainted when one of the two inputs changes.
	 *
	 * <p>
	 * Needed because the surrounding view is written once: a plain fragment has no identity in the
	 * page and therefore cannot be replaced. Only the blocks that actually depend on the inputs carry
	 * one.
	 * </p>
	 */
	private static final class DynamicFragment extends AbstractConstantControl {

		private final String _cssClass;

		private final Supplier<HTMLFragment> _content;

		DynamicFragment(String cssClass, Supplier<HTMLFragment> content) {
			_cssClass = cssClass;
			_content = content;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.endBeginTag();
			_content.get().write(context, out);
			out.endTag(DIV);
		}

		@Override
		protected String getTypeCssClass() {
			return _cssClass;
		}
	}

	/** How the two run modes are offered. */
	private static final LabelProvider MODE_LABELS = model -> Resources.getInstance().getString(
		Boolean.TRUE.equals(model) ? I18NConstants.MODE_BACKGROUND : I18NConstants.MODE_INTERACTIVE);

}
