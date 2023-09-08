<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.text.Format"
%><%@page import="java.util.*"
%><%@page import="java.lang.management.*"
%><%@page import="com.top_logic.mig.html.HTMLFormatter"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%!

public String convertMemoryUsage(MemoryUsage aMem, Format aFormat) {
	if (aMem == null) {
		return "null";
	}
	else {
		String a = "init: " + toMb(aMem.getInit(), aFormat);
		String b = "used: " + toMb(aMem.getUsed(), aFormat);
		String c = "max: " + toMb(aMem.getMax(), aFormat);
		String d = "committed: " + toMb(aMem.getCommitted(), aFormat);
		
		return a + ", " + b + ", " + c + ", " + d;
	}
}

private String toMb(long aLong, Format theFormat) {
	double theValue = aLong / 1024.0 / 1024.0;
	
	return theFormat.format(theValue) + " MB";
}
%><%
Format theFormat = HTMLFormatter.getInstance().getDoubleFormat();
%><layout:html>
	<layout:head>
		<title>
			JVM Memory Monitor
		</title>
	</layout:head>

	<layout:body>
		<basic:access>
			<table style="width: 100%; border: 0px;">
				<tr>
					<td
						align="center"
						colspan="2"
					>
						<h3>
							Memory MXBean
						</h3>
					</td>
				</tr>
				<tr>
					<td width="200">
						Heap Memory Usage
					</td>
					<td>
						<%=convertMemoryUsage(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), theFormat) %>
					</td>
				</tr>

				<tr>
					<td>
						Non-Heap Memory Usage
					</td>
					<td>
						<%=convertMemoryUsage(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), theFormat) %>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td
						align="center"
						colspan="2"
					>
						<h3>
							Memory Pool MXBeans
						</h3>
					</td>
				</tr>
				<%
				for (Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator(); iter.hasNext(); ) {
					MemoryPoolMXBean theItem = (MemoryPoolMXBean) iter.next();
					%>
					<tr>
						<td colspan="2">
							<table style="width: 100%; border: 1px #98AAB1 solid;">
								<tr>
									<td
										align="center"
										colspan="2"
									>
										<b>
											<%= theItem.getName() %>
										</b>
									</td>
								</tr>
								<tr>
									<td width="200">
										Type
									</td>
									<td>
										<%=theItem.getType() %>
									</td>
								</tr>
								<tr>
									<td>
										Usage
									</td>
									<td>
										<%=convertMemoryUsage(theItem.getUsage(), theFormat) %>
									</td>
								</tr>
								<tr>
									<td>
										Peak Usage
									</td>
									<td>
										<%=convertMemoryUsage(theItem.getPeakUsage(), theFormat) %>
									</td>
								</tr>
								<tr>
									<td>
										Collection Usage
									</td>
									<td>
										<%=convertMemoryUsage(theItem.getCollectionUsage(), theFormat) %>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							&#xA0;
						</td>
					</tr>
					<%
				}
				%>
			</table>
		</basic:access>
	</layout:body>
</layout:html>