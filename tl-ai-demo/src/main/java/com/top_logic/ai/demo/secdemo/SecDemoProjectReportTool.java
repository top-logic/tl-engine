/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.secdemo;

import java.util.List;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * The declared-types pair of the security test bed (issue #258).
 *
 * <p>
 * Two tools that do exactly the same thing - list the projects the acting account may read, honestly
 * read-filtered - and differ in one respect only: {@link #listProjects} declares
 * {@code types = "pm:Project"} and {@link #listProjectsUndeclared} declares nothing. The pair
 * isolates the mechanism exactly, the way {@code countTickets} and {@code countTicketsUnclassified}
 * isolate the {@code <tool-groups>} classification.
 * </p>
 *
 * <p>
 * What the pair makes visible is the reason {@link ToolMethod#types()} exists. {@code ticketReader}
 * is granted {@code data.read} and holds no Read right on {@code pm:Project}, so the undeclared tool
 * runs and answers "no projects" - a true sentence about what that account may see, and one a model
 * reports to the user as "there are no projects". The declared tool is not even offered to the model,
 * and a call to it is refused with a sentence naming the type. Both tools are honest; only one of
 * them can be understood.
 * </p>
 *
 * <p>
 * {@code pm:Project} is declarable because its Read grant goes to {@code projectPartner}, a role the
 * test bed assigns on the security root as well as per project - see the {@code <role-rules>} of
 * {@code secdemo.config.xml}. A type whose read right existed only per object must not be declared;
 * {@link ToolMethod#types()} says why.
 * </p>
 */
public class SecDemoProjectReportTool {

	/**
	 * Lists the readable projects, declaring the type it works on.
	 */
	@ToolMethod(description = "Lists the projects the acting account may read. Declares the type it"
		+ " works on, so an account without read access to pm:Project is refused before the tool runs"
		+ " rather than told that there are no projects.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ, types = "pm:Project")
	public String listProjects(ToolExecutionContext context) {
		return report(context);
	}

	/**
	 * The same listing, declaring nothing - the state of every tool before issue #258.
	 */
	@ToolMethod(description = "Lists the projects the acting account may read. Declares no type, so an"
		+ " account without read access to pm:Project gets an empty list instead of a refusal.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ)
	public String listProjectsUndeclared(ToolExecutionContext context) {
		return report(context);
	}

	/**
	 * The names of the projects the run's principal may read, filtered per object - which is the check
	 * that protects the data whether or not a type was declared.
	 */
	private static String report(ToolExecutionContext context) {
		if (!(context instanceof ToolContext toolContext)) {
			throw new IllegalStateException("Listing projects is checked against the run's principal,"
				+ " and this call has no run to ask.");
		}
		TLClass projectType = (TLClass) TLModelUtil.findType("pm:Project");
		List<TLObject> all = MetaElementUtil.getAllInstancesOf(projectType, TLObject.class);
		List<TLObject> readable = toolContext.security().modelSecurity().filterReadable(all);
		if (readable.isEmpty()) {
			return "No projects.";
		}
		StringBuilder answer = new StringBuilder("Projects:");
		for (TLObject project : readable) {
			answer.append(' ').append(project.tValueByName("name"));
		}
		return answer.toString();
	}

}
