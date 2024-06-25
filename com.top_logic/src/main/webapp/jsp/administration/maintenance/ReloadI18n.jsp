<%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<title>
			Reload Translations
		</title>
		<basic:cssLink/>
	</layout:head>
	<%--
	* Page for the ReloadableManager.
	*
	* @history    2002.03.18    MGA    Created
	--%>
	
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
				Reload Translations
			</h1>
			<p>
				Reload the system translations (i18n). The new values will be in effect
				after your next login.
			</p>
			<%
			if (request.getParameter ("RELOAD") != null) {
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
					if(theSize>0){
						%>
						<p>
							<strong>
								Translations where reloaded.
							</strong>
						</p>
						<%
					}
				}
			}
			else{
				%>
				<form method="POST">
					<table summary="List of Things you can reload">
						<%
						for (int thePos = 0; thePos < theArray.length; thePos++) {
							theName = theArray [thePos];
							theDesc = theManager.getDescription (theName);
							if(theName.startsWith("Resources") ){
								%>
								<tr>
									<td>
										<div class="cChoice-option" style="gap: var(--spacing-02)">
											<input name="chk_<%=theName %>"
												checked="checked"
												type="hidden"
											/>
										</div>
									</td>
									<%-- = theDesc not needed here --%>
								</tr>
								<%
							}
						}
						%>
					</table>
					<p>
						<button class="tlButton cButton cmdButton" name="RELOAD" type="submit">
						 	<h4 class="tlButtonLabel">Reload</h4>
						</button>
					</p>
				</form>
			<%  } %>
		</basic:access>
	</layout:body>
</layout:html>