<%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.base.taglibs.basic.TextTag"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="java.util.Collection"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.Enumeration,
com.top_logic.basic.Logger,
com.top_logic.base.accesscontrol.SessionService,
com.top_logic.util.Resources"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="util" prefix="tl"
%>
<basic:html>
	<head>
		<title>
			<tl:label name="administration.sessions.title"/>
		</title>
		<meta
			content="10; URL=deleteSession.jsp"
			http-equiv="refresh"
		/>
		<basic:cssLink/>
	</head>

	<body>
		<basic:access refusedMessage="Sorry, you are not allowed to access this page. Access is restricted to administrators.">
			<%
			String   delete         = request.getParameter 		 ("delete");
			String[] sessions       = request.getParameterValues ("sessionsToDelete");
			SessionService sessionService    = SessionService.getInstance();
			
			if (delete != null && sessions != null)
			{
				int len = sessions.length;
				for(int i=0;i<len;i++)
				{
					String sess = sessions[i];
					%>
					Trying to delete: " <%= session %>
					<br/>
					<%
					Collection<String> sessionIDs= sessionService.getSessionIDs();
					for (String theSession : sessionIDs) {
						if (theSession.equals(sess) )
						{
							%> Now deleting : " <%= sess %>
							<br/>
							<%
							sessionService.invalidateSession(sess);
						}
					}
				}
			}
			%>
			<form name="deleteSession"
				method="POST"
			>
				<%
				Collection<String> sessionIDs = sessionService.getSessionIDs ();
				%>
				<table
					border="0"
					cellspacing="10"
					summary="Active Sessions"
				>
					<tr>
						<td>
							<i>
								&#xA0;
							</i>
						</td>
						<td>
							<i>
								<tl:label name="administration.sessions.heading.sessionid"/>
							</i>
						</td>
						<td>
							<i>
								<tl:label name="administration.sessions.heading.username"/>
							</i>
						</td>
						<td>
							<i>
								<tl:label name="administration.sessions.heading.userid"/>
							</i>
						</td>
						<td>
							<i>
								<tl:label name="administration.sessions.heading.userip"/>
							</i>
						</td>
						<td>
							<i>
								<tl:label name="administration.sessions.heading.lastaccess"/>
							</i>
						</td>
					</tr>

					<tr>
						<td colspan="6">
							<hr noshade="noshade"/>
						</td>
					</tr>
					<%
					for (String theSession : sessionIDs) {
						Person theUser    = null;
						String        theName;
						String        theId;
						String        theIp="unknown";
						String        lastAccessTime="unknown";
						
						try {
							theUser         = sessionService.getUser (theSession);
							theIp           = sessionService.getClientIP (theSession);
							lastAccessTime  = String.valueOf(sessionService.getLastAccessedTime(theSession));
						}
						catch(Exception ex) {
							Logger.info ("Unable to get user for session " + theSession, ex, this);
						}
						
						if (theUser != null) {
							theName = theUser.getFullName();
							theId   = theUser.getName();
						}
						else {
							theName = "unknown";
							theId   = "";
						}
						%>
						<tr>
							<td>
								<input name="sessionsToDelete"
									type="checkbox"
									value="<%=theSession%>"
								/>
							</td>
							<td>
								<%=theSession %>
							</td>
							<td>
								<%=theName %>
							</td>
							<td>
								<%=theId %>
							</td>
							<td>
								<%=theIp %>
							</td>
							<td>
								<%=lastAccessTime%>
							</td>
						</tr>
					<%   }   %>
					<tr>
						<td colspan="6">
							<hr noshade="noshade"/>
						</td>
					</tr>
					<tr>
						<td>
							&#xA0;
						</td>
					</tr>
					<tr>
						<td colspan="6">
							<input name="delete"
								type="submit"
								value="<%= TextTag.attributeText(ResKey.legacy("administration.sessions.button.delete")) %>"
							/>
						</td>
					</tr>
				</table>
			</form>
		</basic:access>
	</body>
</basic:html>