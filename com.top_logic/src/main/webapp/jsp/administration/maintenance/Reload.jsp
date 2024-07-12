<%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<title>
			Reload Modules
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>
	<%@page extends="com.top_logic.util.TopLogicJspBase"
	import="java.util.List,
	java.util.ArrayList,
	java.util.Enumeration,
	com.top_logic.basic.ReloadableManager" %>
	
	<%
	String            theName;
	String            theDesc;
	ReloadableManager theManager = ReloadableManager.getInstance ();
	String []         theArray   = theManager.getKnown ();
	%>
	<layout:body>
		<basic:access>
			<h1>
				Reload Modules
			</h1>
			<%
			out.flush();
			if (request.getParameter ("RELOAD") != null) {
				%>
				<p>
					Reloading....
				</p>
				<%
				out.flush();
				String []   theMap;
				int         theSize;
				Enumeration theEnum  = request.getParameterNames ();
				List        theList  = new ArrayList ();
				
				while (theEnum.hasMoreElements ()) {
					theName = theEnum.nextElement ().toString ();
					
					if (theName.startsWith ("chk_")) {
						theName = theName.substring (4);
						
						theList.add (theName);
					}
				}
				
				theMap  = new String [theList.size ()];
				theMap  = (String []) theList.toArray (theMap);
				theSize = theMap.length;
				theMap  = theManager.reloadKnown (theMap);
				
				if (theMap.length > 0) {
					%>
					<p>
						Failed to reload following modules:
					</p>
					<table>
						<%
						for (int thePos = 0; thePos < theMap.length; thePos++) {
							%>
							<tr>
								<td>
									<%=theMap [thePos] %>
								</td>
							</tr>
							<%
						}
						%>
					</table>
					<%
				}
				else {
					%>
					<p>
						<b>
							Reload succeeds for <%=theSize %> modules
						</b>
					</p>
					<%
				}
			}
			else {
				%>
				<p>
					This page allows you to reload different modules in TopLogic.
				</p>
				<form method="POST">
					<table class="tl-standard-table" summary="List of Things you can reload">
						<tr>
							<th>
								TopLogic Modules
							</th>
							<th>
								Description
							</th>
						</tr>
						<%
						for (int thePos = 0; thePos < theArray.length; thePos++) {
							theName = theArray [thePos];
							theDesc = theManager.getDescription (theName);
							%>
							<tr>
								<td>
								<div class="cChoice-option" style="gap: var(--spacing-02)">
									<input name="chk_<%=theName %>"
										type="checkbox"
									/>
									<span><%=theName %></span>
								</div>
								</td>
								<td>
									<%=theDesc %>
								</td>
							</tr>
							<%
						}
						%>
					</table>
						<div class="cmdButtons">
							<button class="tlButton cButton cmdButton" name="RELOAD" type="submit">
							 	<h4 class="tlButtonLabel">Reload</h4>
							</button>
						</div>
				</form>
				<%
			}
			%>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>