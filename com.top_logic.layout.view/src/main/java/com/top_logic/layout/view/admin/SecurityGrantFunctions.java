/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants.Cell;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants.Row;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions presenting and editing the grants on a type: which role holds which operation
 * on the objects of the type and on their attributes.
 *
 * <p>
 * The grants of a type are computed once by {@link #of(TLClass)} and handed to the other functions,
 * so that a table showing them as a matrix - a row per role, a column per operation - asks one
 * analysis for all of its cells. A changed grant is stored in the application configuration and
 * takes effect when the stored access definition is applied, see {@link TypeGrants}.
 * </p>
 *
 * <p>
 * The functions write the access definition of the application, so they are reserved for an
 * administrator when called interactively.
 * </p>
 */
@ScriptPrefix("securityGrants")
@AdminOnly
public class SecurityGrantFunctions extends TLScriptFunctions {

	/**
	 * CSS class of a cell whose state is stored but not yet in effect, and of a row whose grants
	 * never take effect.
	 */
	public static final String PENDING_CLASS = "tl-status-warning";

	/** Separator between the places listed in a tooltip. */
	private static final String SEPARATOR = ", ";

	/** Separator between the paragraphs of a tooltip. */
	private static final String PARAGRAPH = "\n";

	/** The number of places listed in the tooltip of an operation before the list is cut. */
	private static final int MAX_LISTED_USES = 20;

	/** Starts the paragraph of {@link #info(TypeGrants)}. */
	private static final String HTML_PARAGRAPH_START = "<p>";

	/** Ends the paragraph of {@link #info(TypeGrants)}. */
	private static final String HTML_PARAGRAPH_END = "</p>";

	/** Marks a list of places that was cut. */
	private static final String MORE = "…";

	/**
	 * The grants on the given type, as an editor of them presents them.
	 *
	 * <p>
	 * They are computed from the access definition in effect and the changes stored but not yet
	 * applied. Compute them again to see a change made elsewhere.
	 * </p>
	 *
	 * @param type
	 *        The type whose grants are edited, typically the selected one of the security coverage.
	 * @return The grants, <code>null</code> for no type.
	 */
	@Label("Grants on a type")
	public static TypeGrants of(TLClass type) {
		if (type == null) {
			return null;
		}
		return new TypeGrants(SecurityCoverageCheck.getInstance().newAnalysis(), ModelAccessRights.getInstance(),
			new SecurityDefinitionEditor(), type);
	}

	/**
	 * Why the given type has no grants of its own, as a paragraph of HTML.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @return The explanation in the language of the user, <code>null</code> for a type with
	 *         grants of its own.
	 */
	@Label("Explanation of a type without grants")
	@SideEffectFree
	public static String info(TypeGrants grants) {
		if (grants == null) {
			return null;
		}
		ResKey explanation;
		AccessParentFunction parent = grants.getAccessParent();
		if (parent != null) {
			explanation = I18NConstants.GRANTS_DELEGATED__PARENT.fill(parent.getLabel());
		} else if (grants.isExempt()) {
			explanation = I18NConstants.GRANTS_EXEMPT;
		} else {
			return null;
		}
		return HTML_PARAGRAPH_START + TagUtil.encodeXML(Resources.getInstance().getString(explanation))
			+ HTML_PARAGRAPH_END;
	}

	/**
	 * The rows of the grants: one per role a user can hold on the objects of the type, followed by
	 * one per such role on each attribute.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @return The rows, empty for no grants or a type without grants of its own.
	 */
	@Label("Rows of the grants on a type")
	@SideEffectFree
	public static List<Row> rows(TypeGrants grants) {
		return grants == null ? List.of() : grants.getRows();
	}

	/**
	 * The operations applying to the type: the built-in operations first, the declared ones after
	 * them.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @return The operations, empty for no grants or a type without grants of its own.
	 */
	@Label("Operations of the grants on a type")
	@SideEffectFree
	public static List<BoundCommandGroup> operations(TypeGrants grants) {
		return grants == null ? List.of() : grants.getOperations();
	}

	/**
	 * The name of the given operation, which stays the same across sessions.
	 *
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return The name of the command group of the operation.
	 */
	@Label("Name of an operation")
	@SideEffectFree
	public static String operationName(@Mandatory BoundCommandGroup operation) {
		return operation.getID();
	}

	/**
	 * The label of the given operation, with the places of the application checking it on the
	 * type as its tooltip.
	 *
	 * <p>
	 * The places listed are those checking the operation on the type or on one of its
	 * generalizations. A place not declaring the type it checks is listed for an operation an
	 * application declares only; a built-in operation is checked by too many such places to tell
	 * something about the type.
	 * </p>
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return An internationalized label in all supported languages.
	 */
	@Label("Label of an operation with its users")
	@SideEffectFree
	public static ResKey operationLabel(@Mandatory TypeGrants grants, @Mandatory BoundCommandGroup operation) {
		List<OperationUse> uses = grants.getUses(operation);
		List<String> declared = new ArrayList<>();
		List<String> undeclared = new ArrayList<>();
		for (OperationUse use : uses) {
			if (use.target() == OperationUse.Target.TYPE) {
				declared.add(use.user());
			} else if (!ApplicableOperations.isBuiltIn(operation)) {
				undeclared.add(use.user());
			}
		}
		ResKey.Builder builder = ResKey.builder();
		ResKey.Builder tooltip = builder.suffix(ResKey.TOOLTIP);
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			Resources resources = Resources.getInstance(locale);
			builder.add(locale, resources.getString(labelKey(operation)));
			tooltip.add(locale, operationTooltip(resources, operation, declared, undeclared));
		}
		return builder.build();
	}

	private static String operationTooltip(Resources resources, BoundCommandGroup operation, List<String> declared,
			List<String> undeclared) {
		StringBuilder result = new StringBuilder();
		String description = resources.getString(labelKey(operation).tooltipOptional());
		if (description != null && !description.isEmpty()) {
			result.append(description).append(PARAGRAPH);
		}
		if (declared.isEmpty() && undeclared.isEmpty()) {
			result.append(resources.getString(I18NConstants.GRANTS_NOT_CHECKED));
		}
		if (!declared.isEmpty()) {
			result.append(resources.getString(I18NConstants.GRANTS_CHECKED_BY__USERS.fill(list(declared))));
		}
		if (!undeclared.isEmpty()) {
			if (!declared.isEmpty()) {
				result.append(PARAGRAPH);
			}
			result.append(resources.getString(I18NConstants.GRANTS_UNDECLARED__USERS.fill(list(undeclared))));
		}
		return result.toString();
	}

	/**
	 * The key of the label of the given operation, the one of its command group.
	 */
	private static ResKey labelKey(BoundCommandGroup operation) {
		return com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(operation.getID());
	}

	private static String list(List<String> users) {
		if (users.size() <= MAX_LISTED_USES) {
			return String.join(SEPARATOR, users);
		}
		return users.stream().limit(MAX_LISTED_USES).collect(Collectors.joining(SEPARATOR)) + SEPARATOR + MORE;
	}

	/**
	 * The role of the given row.
	 *
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @return The role the row is about.
	 */
	@Label("Role of a row of the grants")
	@SideEffectFree
	public static BoundedRole role(@Mandatory Row row) {
		return row.role();
	}

	/**
	 * What the grants of the given row are on: the attribute, or the type for a row about the
	 * objects of the type.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @return The attribute of the row, or the type for a row about the objects of the type.
	 */
	@Label("Model part of a row of the grants")
	@SideEffectFree
	public static TLModelPart part(@Mandatory TypeGrants grants, @Mandatory Row row) {
		return row.attribute() != null ? row.attribute() : grants.getType();
	}

	/**
	 * Whether the role of the given row holds the given operation, directly or through an implying
	 * operation, once the stored changes are applied.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return <code>null</code> where the operation cannot be granted, and for an attribute that is
	 *         not restricted.
	 */
	@Label("Whether a grant is given")
	@SideEffectFree
	public static Boolean granted(@Mandatory TypeGrants grants, @Mandatory Row row,
			@Mandatory BoundCommandGroup operation) {
		Cell cell = cell(grants, row, operation);
		if (!cell.applies() || !cell.restricted()) {
			return null;
		}
		return Boolean.valueOf(cell.granted());
	}

	/**
	 * Whether the grant of the given operation can be changed in the given row: the operation
	 * applies there, and the role does not hold it through an implying operation.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return Whether a click on the cell changes the grant.
	 */
	@Label("Whether a grant can be changed")
	@SideEffectFree
	public static boolean editable(@Mandatory TypeGrants grants, @Mandatory Row row,
			@Mandatory BoundCommandGroup operation) {
		return cell(grants, row, operation).editable();
	}

	/**
	 * Grants the given operation in the given row, or revokes it.
	 *
	 * <p>
	 * The change is stored in the application configuration and takes effect with "Apply
	 * configuration".
	 * </p>
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @param granted
	 *        Whether to grant the operation.
	 */
	@Label("Change a grant")
	public static void set(@Mandatory TypeGrants grants, @Mandatory Row row, @Mandatory BoundCommandGroup operation,
			boolean granted) {
		try {
			grants.setGranted(row, operation, granted);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_WRITING_ACCESS_DEFINITION__FILE
				.fill(new SecurityDefinitionEditor().getGrantsFile().getPath()), ex);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ERROR_READING_ACCESS_DEFINITION__FILE
				.fill(new SecurityDefinitionEditor().getGrantsFile().getPath()), ex);
		}
	}

	/**
	 * What a cell of the given grant tells beside its check mark: through which operation the role
	 * holds it - on the type, or on which attributes -, whether the shown state is still to be applied, whether the attribute is
	 * restricted at all.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return The tooltip in the language of the user, <code>null</code> for nothing to tell.
	 */
	@Label("Tooltip of a grant")
	@SideEffectFree
	public static String tooltip(@Mandatory TypeGrants grants, @Mandatory Row row,
			@Mandatory BoundCommandGroup operation) {
		Cell cell = cell(grants, row, operation);
		if (!cell.applies()) {
			return null;
		}
		Resources resources = Resources.getInstance();
		List<String> parts = new ArrayList<>();
		if (!cell.impliedBy().isEmpty()) {
			String implying = cell.impliedBy().stream()
				.map(implied -> resources.getString(labelKey(implied)))
				.collect(Collectors.joining(SEPARATOR));
			parts.add(resources.getString(I18NConstants.GRANTS_IMPLIED__OPERATIONS.fill(implying)));
		}
		for (Map.Entry<BoundCommandGroup, List<TLStructuredTypePart>> entry : cell.impliedByAttributes().entrySet()) {
			String attributes = entry.getValue().stream()
				.map(MetaLabelProvider.INSTANCE::getLabel)
				.collect(Collectors.joining(SEPARATOR));
			parts.add(resources.getString(I18NConstants.GRANTS_IMPLIED_BY_ATTRIBUTES__OPERATION_ATTRIBUTES
				.fill(resources.getString(labelKey(entry.getKey())), attributes)));
		}
		if (!cell.restricted()) {
			parts.add(resources.getString(I18NConstants.GRANTS_UNRESTRICTED));
		}
		if (cell.pending()) {
			parts.add(resources.getString(I18NConstants.GRANTS_PENDING));
		}
		return parts.isEmpty() ? null : String.join(PARAGRAPH, parts);
	}

	/**
	 * The CSS class marking a cell of the given grant: {@value #PENDING_CLASS} for a state that is
	 * stored but not yet in effect.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @param operation
	 *        An operation, see {@link #operations(TypeGrants)}.
	 * @return The class, <code>null</code> for a cell in effect as it is shown.
	 */
	@Label("CSS class of a grant")
	@SideEffectFree
	public static String cellClass(@Mandatory TypeGrants grants, @Mandatory Row row,
			@Mandatory BoundCommandGroup operation) {
		return cell(grants, row, operation).pending() ? PENDING_CLASS : null;
	}

	/**
	 * The CSS class marking the given row: {@value #PENDING_CLASS} for a role that cannot be held
	 * on the type, whose grants never take effect.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @return The class, <code>null</code> for a row of a role that can be held.
	 */
	@Label("CSS class of a row of the grants")
	@SideEffectFree
	public static String rowClass(@Mandatory TypeGrants grants, @Mandatory Row row) {
		return grants.isIneffective(row) ? PENDING_CLASS : null;
	}

	/**
	 * What the role cell of the given row tells: that the role cannot be held on the type, so that
	 * its grants never take effect and can only be revoked.
	 *
	 * @param grants
	 *        The grants, see {@link #of(TLClass)}.
	 * @param row
	 *        A row, see {@link #rows(TypeGrants)}.
	 * @return The tooltip in the language of the user, <code>null</code> for a role that can be
	 *         held.
	 */
	@Label("Tooltip of the role of a row of the grants")
	@SideEffectFree
	public static String roleTooltip(@Mandatory TypeGrants grants, @Mandatory Row row) {
		if (!grants.isIneffective(row)) {
			return null;
		}
		return Resources.getInstance().getString(I18NConstants.GRANTS_INEFFECTIVE);
	}

	private static Cell cell(TypeGrants grants, Row row, BoundCommandGroup operation) {
		try {
			return grants.getCell(row, operation);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ERROR_READING_ACCESS_DEFINITION__FILE
				.fill(new SecurityDefinitionEditor().getGrantsFile().getPath()), ex);
		}
	}

}
