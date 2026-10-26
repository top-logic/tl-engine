/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.tl.ai.core.security.layout.DraftAccess.Mark;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * One cell of a policy matrix: shows what the row says about the column's name, and edits it.
 *
 * <p>
 * A click cycles between empty and granted. A prohibition is set and cleared through the context menu,
 * because it beats every permission of every other role and must not be one accidental click away. A
 * cell of a row the section does not name shows what the section default gives the role, greyed, and
 * does not react.
 * </p>
 */
/*
 * Issue #404. A control rather than a form field per cell: the cell holds no value of its own - it is a
 * question to the draft and a click on it is an edit of the draft - and fifteen hundred form fields per
 * sub-tab would sit in the form context whether their page is shown or not.
 */
public class PolicyCellControl extends AbstractConstantControl implements ContextMenuOwner {

	/**
	 * What a cell of a row the section does not name shows: what the section default gives that role.
	 */
	public enum Ghost {

		/** The default admits every name. */
		GRANTED,

		/** The default admits only what the agent itself uses. */
		AGENTS_OWN,

		/** The default admits nothing. */
		NONE;
	}

	/**
	 * How one cell looks.
	 *
	 * @param state
	 *        What the row says, or <code>null</code> for a {@link #ghost()} cell.
	 * @param ghost
	 *        What the section default gives a row it does not name, or <code>null</code> for a row that
	 *        speaks for itself.
	 * @param locked
	 *        Whether a click changes nothing.
	 * @param mark
	 *        What the overlay adds.
	 * @param tooltip
	 *        Why the cell looks the way it does, in the acting user's language.
	 */
	public record Look(CellState state, Ghost ghost, boolean locked, Mark mark, String tooltip) {

		/** A cell that shows what its row says. */
		public static Look of(CellState state, boolean locked, Mark mark, String tooltip) {
			return new Look(state, null, locked, mark, tooltip);
		}

		/** A locked cell showing what the section default gives its row. */
		public static Look ghost(Ghost ghost, Mark mark, String tooltip) {
			return new Look(null, ghost, true, mark, tooltip);
		}
	}

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(ContextMenuOpener.INSTANCE, Click.INSTANCE);

	private final ToolAccessPolicyComponent _matrix;

	private final PolicyCell _cell;

	/** How the cell looks in the rendering that is being written, so that it is computed once. */
	private Look _look;

	/**
	 * Creates a {@link PolicyCellControl}.
	 *
	 * @param matrix
	 *        The matrix the cell belongs to, which answers what the cell shows and makes its edits.
	 * @param cell
	 *        The cell.
	 */
	public PolicyCellControl(ToolAccessPolicyComponent matrix, PolicyCell cell) {
		super(COMMANDS);
		_matrix = matrix;
		_cell = cell;
	}

	@Override
	public Object getModel() {
		return _cell;
	}

	@Override
	protected String getTypeCssClass() {
		return "aiPolicyCell";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (_cell.isTools()) {
			writeTools(context, out);
			return;
		}
		Look look = _look = _matrix.look(_cell);
		boolean clickable = isClickable(look);

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		if (_matrix.hasMenu(_cell)) {
			out.writeAttribute(TL_CONTEXT_MENU_ATTR, getID());
		}
		if (clickable) {
			out.beginAttribute(ONCLICK_ATTR);
			out.append("return ");
			Click.INSTANCE.writeInvokeExpression(out, this);
			out.endAttribute();
		}
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, look.tooltip());
		out.endBeginTag();

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "aiPolicyBox");
		out.endBeginTag();
		out.writeText(glyph(look));
		out.endTag(SPAN);

		if (look.mark() == Mark.REFUSED) {
			writeMark(out, "aiPolicyMark aiPolicyMarkRefused", "⊘");
		} else if (look.mark() == Mark.AGENTS_OWN && look.ghost() == null) {
			writeMark(out, "aiPolicyMark aiPolicyMarkOwn", "A");
		}
		out.endTag(SPAN);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		if (_cell.isTools()) {
			out.append(" aiPolicyTools");
			if (_matrix.mayEditTools(_cell.row())) {
				out.append(" aiPolicyClickable");
			}
			return;
		}
		Look look = _look != null ? _look : _matrix.look(_cell);
		out.append(' ').append(stateClass(look));
		if (look.mark() == Mark.REFUSED) {
			out.append(" aiPolicyRefused");
		}
		if (isClickable(look)) {
			out.append(" aiPolicyClickable");
		}
	}

	private boolean isClickable(Look look) {
		return !look.locked() && _matrix.mayWrite() && look.state() != CellState.DENIED;
	}

	private static void writeMark(TagWriter out, String cssClass, String text) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, cssClass);
		out.endBeginTag();
		out.writeText(text);
		out.endTag(SPAN);
	}

	private void writeTools(DisplayContext context, TagWriter out) throws IOException {
		boolean editable = _matrix.mayEditTools(_cell.row());
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		if (editable) {
			out.beginAttribute(ONCLICK_ATTR);
			out.append("return ");
			Click.INSTANCE.writeInvokeExpression(out, this);
			out.endAttribute();
		}
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out,
			Resources.getInstance().getString(editable ? I18NConstants.CELL_TOOLS : I18NConstants.CELL_TOOLS_LOCKED));
		out.endBeginTag();
		out.writeText(_matrix.toolsSummary(_cell.row()) + (editable ? " …" : ""));
		out.endTag(SPAN);
	}

	private static String stateClass(Look look) {
		if (look.ghost() != null) {
			return "aiPolicyGhost aiPolicyGhost-" + look.ghost().name();
		}
		String result = "aiPolicyState-" + look.state().name();
		return look.locked() ? result + " aiPolicyLocked" : result;
	}

	private static String glyph(Look look) {
		if (look.ghost() != null) {
			switch (look.ghost()) {
				case GRANTED:
					return "✓";
				case AGENTS_OWN:
					return "A";
				default:
					return "";
			}
		}
		switch (look.state()) {
			case GRANTED:
				return "✓";
			case DENIED:
				return "✕";
			default:
				return "";
		}
	}

	/**
	 * The three states the cell can be put into, minus the one it is in.
	 */
	@Override
	public Menu createContextMenu(String contextInfo) {
		if (!_matrix.hasMenu(_cell)) {
			return Menu.create();
		}
		CellState current = _matrix.state(_cell);
		List<CommandModel> items = new ArrayList<>();
		if (current != CellState.GRANTED) {
			items.add(item(CellState.GRANTED, I18NConstants.CELL_MENU_GRANT));
		}
		if (current != CellState.DENIED) {
			items.add(item(CellState.DENIED, I18NConstants.CELL_MENU_DENY));
		}
		if (current != CellState.ABSENT) {
			items.add(item(CellState.ABSENT, current == CellState.DENIED
				? I18NConstants.CELL_MENU_CLEAR_DENY : I18NConstants.CELL_MENU_CLEAR));
		}
		return Menu.create(items.toArray(new CommandModel[items.size()]));
	}

	private CommandModel item(CellState state, ResKey label) {
		CommandModel result = CommandModelFactory.commandModel(context -> {
			if (_matrix.set(_cell, state)) {
				requestRepaint();
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		Resources resources = Resources.getInstance();
		result.setLabel(resources.getString(label));
		String tooltip = resources.getString(label.tooltipOptional());
		if (tooltip != null) {
			result.setTooltip(tooltip);
		}
		return result;
	}

	/**
	 * A click on a cell: the one edit a click makes, or opening the single tools of the row.
	 */
	private static final class Click extends ControlCommand {

		static final Click INSTANCE = new Click();

		private Click() {
			super("click");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CELL_CLICK;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			PolicyCellControl cell = (PolicyCellControl) control;
			if (cell._cell.isTools()) {
				return cell._matrix.openTools(commandContext, cell._cell.row());
			}
			if (cell._matrix.click(cell._cell)) {
				cell.requestRepaint();
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
