<%@page import="com.top_logic.basic.col.TypedAnnotatable"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.basic.col.TypedAnnotatable.Property"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>

<%-- Maintenance template version: 1.0 (1.6) --%>
<%!
// JSP page configuration:
// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Trigger Task";
private static final String SIMULATE_BUTTON = null;
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";

static final Property<Task> TASK_PROPERTY = TypedAnnotatable.property(Task.class, "ADMINJSP_TriggerTask_SelectedTask");
private static final String WAIT_FOR_FINISH = "ADMINJSP_TriggerTask_WaitForFinish";
{
	LOG_PRINTS = true;
	LOG_TIME = false;
	USE_WAITING_ANI = false;
	TITLE = "TriggerTask";
	DESCRIPTION = "Triggers the execution of a task.";
}

%>


<%!
@Override
protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
	try {
		Task task = TLContext.getContext().reset(TASK_PROPERTY);
		if (task == null) {
			print("No task selected.");
		}
		else {
			print("Running Task '" + task.getName() + "'...");
			
			Thread thread = new Thread(task);
			thread.start();
			
			print(); print();
			print("Done. Task was triggered successfully.");
			
			// This is required because of stupid Scheduler / Task implementation.
			RecalcSchedulerThread recalculate = new RecalcSchedulerThread(task);
			recalculate.recalculate();
			recalculate.start();
			
			if (USE_WAITING_ANI) {
				thread.join();
				print(); print();
				print("Task has finished.");
			}
			
			// check whether stupid scheduler has removed the task
			if (!Scheduler.getSchedulerInstance().isKnown(task)) {
				printError("Stupid scheduler has removed the task from its list. Please restart the system to reactivate it.");
			}
		}
	}
	catch (Exception e) {
		printError("Error: Failed to trigger the selected task.");
		printError(e.toString());
		rollback();
	}
}

// This is a hack for stupid Scheduler / Task implementation.
// This sets the last run time to now so that calcNextShed()
// will calculate correct result.
private static class RecalcSchedulerThread extends Thread {
	
	private Task task;
	
	public RecalcSchedulerThread(Task task) {
		this.task = task;
	}
	
	@Override
	public void run() {
		try {
			// wait for task to start
			sleep(5000);
		}
		catch (InterruptedException e) {
			// ignore
		}
		Logger.info("Calculating next schedule for task " + task.getName() + "...", this);
		recalculate();
		if (!Scheduler.getSchedulerInstance().isKnown(task)) {
			Logger.error("Stupid scheduler has removed the task from its list. Please restart the system to reactivate it.", this);
		}
	}
	
	public void recalculate() {
		if (task instanceof TaskImpl) {
			// hack to set last scheduled time to now
			((TaskImpl<?>)task).setRunOnStartup(false);
		}
		task.calcNextShed(System.currentTimeMillis());
	}
	
}
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="java.util.List"
%><%@page import="com.top_logic.layout.form.model.SelectField"
%><%@page import="com.top_logic.layout.Control"
%><%@page import="com.top_logic.layout.form.template.SelectionControlProvider"
%><%@page import="com.top_logic.layout.form.template.DefaultFormFieldControlProvider"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.form.ValueListener"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.util.FormFieldHelper"
%><%@page import="com.top_logic.layout.LabelProvider"
%><%@page import="com.top_logic.util.sched.task.Task"
%><%@page import="com.top_logic.layout.LabelComparator"
%><%@page import="java.util.Collections"
%><%@page import="java.util.ArrayList"
%><%@page import="java.util.Calendar"
%><%@page import="com.top_logic.util.sched.task.impl.TaskImpl"
%><%@page import="java.util.Map"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.basic.ArrayUtil"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.sched.Scheduler"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.basic.util.NumberUtil"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.layout.URLBuilder"
%><%@page import="com.top_logic.layout.form.model.FormFactory"
%><layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
		<basic:script>
			services.ajax.ignoreTLAttributes = true;
		</basic:script>
	</layout:head>

	<layout:body>
		<basic:access>
			<%
			LayoutComponent component = MainLayout.getComponent(pageContext);
			
			// waiting slider
			USE_WAITING_ANI = request.getParameter(WAIT_FOR_FINISH) != null;
			
			writeWaitingSlider(pageContext, "SUBMIT", "SIMULATE");
			
			// run work
			if ((RUN_BUTTON == null && SIMULATE_BUTTON == null) ||
				(USE_WAITING_ANI && (request.getParameter("DO_SUBMIT") != null || request.getParameter("DO_SIMULATE") != null)) ||
				(!USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null))) {
				
				boolean doSimulate = request.getParameter(USE_WAITING_ANI ? "DO_SIMULATE" : "SIMULATE") != null;
				if (doSimulate) {
					out.write("<br/><b>Simulating...</b><br/><br/><br/>\n");
				}
				%>
				<table style="margin: 5px">
					<tr>
						<td>
							<code class="normal">
								<%
								try {
									runWork(out, doSimulate, request);
								}
								catch (Throwable t) {
									printError("Error: An error occured while running the JSP page:");
									printError(t.toString());
									rollback();
								}
								%>
							</code>
						</td>
					</tr>
				</table>
				<%
				if (REFRESH_BUTTON != null) {
					%>
					<br/>
					<form method="POST">
						<table>
							<tr>
								<td>
									<p>
										<button class="tlButton cButton cmdButton"
									            name="<%=doSimulate ? "SIMULATE" : "SUBMIT"%>"
									            type="submit">
									        <span class="tlButtonLabel"><%= REFRESH_BUTTON %></span>
									    </button>
									</p>
								</td>
							</tr>
						</table>
					</form>
					<%
				}
				if (RESTART_LINK != null) {
					%>
					<p>
						<button class="tlButton cButton cmdButton" onclick="self.location.href = '<%=component.getComponentURL(pageContext).getURL()%>';">
					        <h4 class="tlButtonLabel"><%= RESTART_LINK %></h4>
					    </button>
					</p>
					<%
				}
			}
			else {
				%>
				<form method="POST">
					<table>
						<tr>
							<td>
								<p>
									Task to trigger:
									<%
									// add field to select a task
									FormContext context = new FormContext(component);
									
									LabelProvider label = new LabelProvider() {
										@Override
										public String getLabel(Object object) {
											return ((Task)object).getName();
										}
									};
									
									List<Task> tasks = new ArrayList<>(Scheduler.getSchedulerInstance().getAllKnownTasks());
									Collections.sort(tasks, LabelComparator.newInstance(label));
									
									SelectField field = FormFactory.newSelectField(TASK_PROPERTY.getName(), tasks);
									field.addValueListener(new ValueListener() {
											@Override
											public void valueChanged(FormField formField, Object oldValue, Object newValue) {
												TLContext.getContext().set(TASK_PROPERTY, (Task)FormFieldHelper.getSingleProperValue(formField));
											}
									});
									field.setOptionLabelProvider(label);
									context.addMember(field);
									
									Control control = DefaultFormFieldControlProvider.INSTANCE.createControl(field, null);
									TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
									control.write(DefaultDisplayContext.getDisplayContext(pageContext), tagWriter);
									tagWriter.flushBuffer();
									%>
								</p>
								<p>
									Wait for task to finish:
									<input name="<%=WAIT_FOR_FINISH%>"
										checked="checked"
										type="checkbox"
										value="<%=WAIT_FOR_FINISH%>"
									/>
								</p>

								<p>
									<%
									if (RUN_BUTTON != null) {
								        %>
								        <button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
								            <h4 class="tlButtonLabel"><%= RUN_BUTTON %></h4>
								        </button>
								        <%
								    }
								    if (SIMULATE_BUTTON != null) {
								        %>
								        <button class="tlButton cButton cmdButton" name="SIMULATE" type="submit">
								            <h4 class="tlButtonLabel"><%= SIMULATE_BUTTON %></h4>
								        </button>
								        <%
								    }
									%>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<%
			}
			%>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>