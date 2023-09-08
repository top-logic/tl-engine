<%@page import="com.top_logic.layout.ResPrefix"%><%@page
import="java.text.DateFormat"%><%@page import="java.util.Date"%><%@page
language="java" extends="com.top_logic.util.TopLogicJspBase"%><%@page
import="java.util.Arrays"%><%@page
import="com.top_logic.mig.html.HTMLFormatter"%><%@page
import="com.top_logic.basic.StringServices"%><%@page
import="com.top_logic.util.sched.Scheduler"%><%@page
import="com.top_logic.basic.DebugHelper"%><%@page
import="com.top_logic.util.Resources"%><%@page
import="com.top_logic.mig.html.layout.LayoutComponent"%><%@page
import="com.top_logic.mig.html.layout.MainLayout"%><%@taglib
uri="layout" prefix="layout"%><%@taglib uri="ajaxform" prefix="form"%>
<%
Resources res = Resources.getInstance();
LayoutComponent theComp = MainLayout.getComponent(pageContext);
ResPrefix resPrefix = theComp.getResPrefix();
Scheduler sched = Scheduler.getSchedulerInstance();
String theStatus = sched.getStatus();
Date startTime = sched.getStartTime();
DateFormat timeFormat = HTMLFormatter.getInstance().getTimeFormat();
String theStart = startTime == null ? "---" : timeFormat.format(startTime);

if (theStatus != null) {
	StringBuffer theResult = new StringBuffer("<ol>\n");
	
	for (String theLine : Arrays
		.asList(StringServices.toArray(StringServices.replace(theStatus, "]", "]\n"), '\n'))) {
		theResult.append("<li>").append(theLine).append("</li>\n");
	}
	theResult.append("</ol>\n");
	
	theStatus = theResult.toString();
}
%><layout:html>
	<layout:head>
		<title>
			<%=res.getString(resPrefix.key("title"))%>
		</title>
	</layout:head>
	<layout:body>
		<form:horizontal>
			<form:columns count="2">
				<form:descriptionContainer>
					<form:groupCell columns="1">
						<form:descriptionCell labelAbove="true">
							<form:description>
								<%=res.getString(resPrefix.key("threadName"))%>:
							</form:description>
							<%=sched.getName()%>
						</form:descriptionCell>
						<form:descriptionCell labelAbove="true">
							<form:description>
								<%=res.getString(resPrefix.key("maxTasktime"))%>:
							</form:description>
							<%=DebugHelper.getTime(sched.getMaxTasktime())%>
						</form:descriptionCell>
					</form:groupCell>
					<form:groupCell
						columns="1"
						titleKeyConst="<%=resPrefix.key(\"details\")%>"
					>
						<form:descriptionCell style="text-align: right">
							<form:description>
								<%=res.getString(resPrefix.key("max"))%>:
							</form:description>
							<%=sched.getMaxTask()%>
						</form:descriptionCell>
						<form:descriptionCell style="text-align: right">
							<form:description>
								<%=res.getString(resPrefix.key("waiting"))%>:
							</form:description>
							<%=sched.getNumTasks()%>
						</form:descriptionCell>
						<form:descriptionCell style="text-align: right">
							<form:description>
								<%=res.getString(resPrefix.key("active"))%>:
							</form:description>
							<%=sched.getNumRunningTasks()%>
						</form:descriptionCell>
					</form:groupCell>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:groupCell
						columns="1"
						style="height: calc(100% - 10px)"
						titleText="<%=res.getString(resPrefix.key(\"lastRun\")) +\" (\" + theStart + ')'%>"
					>
						<form:cell>
							<%=theStatus%>
						</form:cell>
					</form:groupCell>
				</form:descriptionContainer>
			</form:columns>
		</form:horizontal>
	</layout:body>
</layout:html>