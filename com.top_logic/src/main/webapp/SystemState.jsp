<!DOCTYPE html>
<%@page import="com.top_logic.basic.util.ResKey"
%><%@page extends="com.top_logic.util.NoContextJspBase"%>
<html>
	<%@page language="java" session="false"
	%><%@page import="java.net.InetAddress"
	%><%@page import="java.util.Iterator"
	%><%@page import="java.util.Date"
	%><%@page import="com.top_logic.mig.html.HTMLFormatter"
	%><%@page import="com.top_logic.basic.version.Version"
	%><%@page import="com.top_logic.util.Resources"
	%><%@page import="com.top_logic.util.monitor.ApplicationMonitor"
	%><%@page import="com.top_logic.util.monitor.MonitorResult"
	%><%@page import="com.top_logic.util.monitor.MonitorMessage"
	%><%@page import="com.top_logic.util.monitor.MonitorComponent"
	%><%@page import="com.top_logic.layout.servlet.CacheControl"
	%><%@taglib uri="basic" prefix="basic"
	%>
	
	<%--
	* Check the Status of the System
	*
	* @history   2005-01-26    KHA   Re-Added to main TopLogic repository (from TPM2)
	* @history   2004-05-15    MGA   Created
	*
	* @author    MGA
	--%>
	<head>
		<title>
			Application Monitor
		</title>
		<basic:favicon/>
	</head>
	<body>
		<%
		CacheControl.setNoCache(response);
		
		ApplicationMonitor theMonitor = ApplicationMonitor.getInstance();
		MonitorResult      theResult  = theMonitor.checkApplication();
		
		String  versText  = "No Version found";
		Version theVersion = (Version) application.getAttribute(Version.class.getName());
		if (theVersion != null) {
			versText = theVersion.toString();
		}
		
		if (theResult.isOK() && (null == request.getParameter("showInfo"))) {
			
			%>OK<%
		}
		else {
			MonitorMessage     theMessage;
			Object             theComp;
			MonitorMessage.Status theType;
			String             theImage;
			String             theStateX;
			String             theName;
			String             theText;
			int                theError   = 0;
			int                theFatal   = 0;
			String             theContext = request.getContextPath();
			Date               theDate    = new Date();
			InetAddress        theHost    = InetAddress.getLocalHost();
			String             theVM;
			String             theVendor  = "?";
			String             theRuntime = "?";
			String             theInfo    = "?";
			Resources		   theRes     = Resources.getInstance();
			
			try {
				String theVMVersion = System.getProperty("java.vm.version");
				String theVMName = System.getProperty("java.vm.name");
				
				theVM      = theVMVersion + " (" + theVMName + ')';
				theRuntime = System.getProperty("java.runtime.name");
				theVendor  = System.getProperty("java.vm.vendor");
				theInfo    = System.getProperty("java.vm.info");
			}
			catch (Exception ex) {
				theVM = "Unable to verify used java (reason is: " + ex + ")...";
			}
			%>
			<table
				summary="application state"
				width="95%"
			>
				<tr>
					<th>
					</th>
					<th colspan="2">
						<%=versText %>
					</th>
				</tr>
				<tr>
					<th colspan="3">
						<hr/>
					</th>
				</tr>
				<tr>
					<th>
					</th>
					<th>
						<%= theRes.getString(ResKey.legacy("admin.sys.monitor.monitor")) %>
					</th>
					<th>
						<%= theRes.getString(ResKey.legacy("admin.sys.monitor.message")) %>
					</th>
				</tr>
				<%
				for (Iterator<MonitorMessage> theIt = theResult.getMessages().iterator(); theIt.hasNext(); ) {
					theMessage = theIt.next();
					theType    = theMessage.getType();
					theComp    = theMessage.getComponent();
					
					if (theComp instanceof MonitorComponent) {
						theName = ((MonitorComponent) theComp).getName();
					}
					else if (theComp instanceof String) {
						theName = (String) theComp;
					}
					else {
						theName = theComp.getClass().getName();
					}
					
					if (MonitorMessage.Status.INFO.equals(theType)) {
						theImage = "green.png";
						theStateX = "OK";
					}
					else if (MonitorMessage.Status.ERROR.equals(theType)) {
						theError++;
						theImage = "yellow.png";
						theStateX = "Warning";
					}
					else {
						theFatal++;
						theImage = "red.png";
						theStateX = "Failure";
					}
					
					theImage = theContext + "/images/icons/systemstate/" + theImage;
					%>
					<tr>
						<%
						try {
							%>
							<td valign="top">
								<img
									alt="<%=theStateX %>"
									src="<%=theImage %>"
								/>
							</td>
							<td
								style="white-space: nowrap"
								valign="top"
							>
								<%=theName %>
							</td>
							<td valign="top">
								<%=theMessage.getMessage() %>
							</td>
							<%
						}
						catch (Exception ex) {
							%>
							<td>
								<%=ex %>
							</td>
							<%
						}
						%>
					</tr>
					<%
				}
				
				ResKey theState;
				if (theResult.isOK()) {
					theImage = "/images/icons/systemstate/green.png";
					theState = ResKey.legacy("admin.sys.monitor.ok");
					theText  = "";
				}
				else if (theResult.isAlive()) {
					theImage = "/images/icons/systemstate/yellow.png";
					theState = ResKey.legacy("admin.sys.monitor.alive");
					
					if (theError == 1) {
						theText = "admin.sys.monitor.message.alive1/i" + theError;
					}
					else {
						theText = "admin.sys.monitor.message.alive/i" + theError;
					}
				}
				else {
					theImage = "/images/icons/systemstate/red.png";
					theState = ResKey.legacy("admin.sys.monitor.down");
					
					if (theFatal == 1) {
						theText = "admin.sys.monitor.message.fatal1/i" + theFatal;
					}
					else {
						theText = "admin.sys.monitor.message.fatal/i" + theFatal;
					}
				}
				
				if (theText != "") {
					theText = Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(theText);
				}
				else {
					theText = "&nbsp;";
				}
				
				theImage = theContext + theImage;
				%>
				<tr>
					<td colspan="3">
						<hr/>
					</td>
				</tr>
				<tr>
					<th>
						&#xA0;
					</th>
					<th colspan="2">
						<%= theRes.getString(ResKey.legacy("admin.sys.monitor.state")) %>
					</th>
				</tr>
				<tr>
					<td>
						<img
							alt="<%=theRes.getString(theState) %>"
							src="<%=theImage %>"
						/>
					</td>
					<td>
						<%= theRes.getString(theState) %>
					</td>
					<td>
						<%=theText %>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td>
						&#xA0;
					</td>
					<td align="right">
						<em>
							<%= theRes.getString(ResKey.legacy("admin.sys.monitor.time")) %>:
						</em>
					</td>
					<td>
						<em>
							<%= HTMLFormatter.getInstance().formatDateTime(theDate) %>
						</em>
					</td>
				</tr>
				<tr>
					<td>
						&#xA0;
					</td>
					<td align="right">
						<em>
							<%= theRes.getString(ResKey.legacy("admin.sys.monitor.server")) %>:
						</em>
					</td>
					<td>
						<em>
							<%=theHost.getHostName() %> (IP: <%=theHost.getHostAddress() %>)
						</em>
					</td>
				</tr>
				<tr>
					<td>
						&#xA0;
					</td>
					<td
						align="right"
						valign="top"
					>
						<em>
							<%= theRes.getString(ResKey.legacy("admin.sys.monitor.vm")) %>:
						</em>
					</td>
					<td>
						<table>
							<tr>
								<td align="right">
									<em>
										Version:
									</em>
								</td>
								<td>
									<em>
										<%=theVM %>
									</em>
								</td>
							</tr>
							<tr>
								<td align="right">
								</td>
								<td>
									<em>
										<%=theInfo%>
									</em>
								</td>
							</tr>
							<tr>
								<td align="right">
									<em>
										Runtime:
									</em>
								</td>
								<td>
									<em>
										<%=theRuntime %>
									</em>
								</td>
							</tr>
							<tr>
								<td align="right">
									<em>
										Vendor:
									</em>
								</td>
								<td>
									<em>
										<%=theVendor %>
									</em>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<%
		} // if !OK
		%>
	</body>
</html>