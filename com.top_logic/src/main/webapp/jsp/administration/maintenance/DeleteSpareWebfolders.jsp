<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@page import="java.util.Collection"
%><%@page import="java.util.Iterator"
%><%@page import="java.util.List"
%><%@page import="java.util.ArrayList"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.dsa.DataAccessProxy"
%><%@page import="com.top_logic.dsa.DatabaseAccessException"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseException"
%><%@page import="com.top_logic.knowledge.service.Transaction"
%><%@page import="com.top_logic.knowledge.objects.KOAttributes"
%><%@page import="com.top_logic.knowledge.wrap.CreatePhysicalResource"
%><%@page import="com.top_logic.knowledge.wrap.WebFolderFactory"
%><%@page import="com.top_logic.knowledge.wrap.WrapperFactory"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.knowledge.wrap.WebFolder"
%><%@taglib uri="basic" 	prefix="basic"
%><%@taglib uri="layout"	prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<basic:access>
			<h1>
				Delete spare physical resources of objects
			</h1>
			<%
			boolean isNew      = !"makeItSo".equals(request.getParameter("done"));
			boolean showDetail = !StringServices.isEmpty(request.getParameter("details"));
			boolean commit     = !StringServices.isEmpty(request.getParameter("commit"));
			
			WebFolderFactory theWFFactory = WebFolderFactory.getInstance();
			
			if (isNew) {
				if (theWFFactory.getCreateMode() == CreatePhysicalResource.IMMEDIATE) {
					out.println("'" + CreatePhysicalResource.IMMEDIATE.getExternalName() + "' create mode is set. Skipping!");
				}
				else {
					%>
					Click start to migration.
					<form method="post">
						<input name="done"
							type="hidden"
							value="makeItSo"
						/>
						<table
							summary="Button to reload selected elements"
							width="100%"
						>
							<tr>
								<td>
									Log details:
									<input name="details"
										type="checkbox"
										value="details"
									/>
								</td>
							</tr>
							<tr>
								<td>
									Commit changes:
									<input name="commit"
										type="checkbox"
										value="commit"
									/>
								</td>
							</tr>
							<tr>
								<td>
									<input name="RELOAD"
										type="submit"
										value="Start"
									/>
								</td>
							</tr>
						</table>
					</form>
				<% 		}
			} else {
				%>
				<pre>
					<%
					try {
						KnowledgeBase theKB = WebFolder.getDefaultKnowledgeBase();
						Collection<?> theWFs = WrapperFactory.getWrappersByType(WebFolder.OBJECT_NAME, theKB);
						//Iterator theIter = theWFs.iterator();
						int counter = 0;
						int theWFSize = theWFs.size();
						int sum = 0;
						out.println("Delete Spare WebFolders - checking " + theWFSize + " folders");
						Transaction theTX = null;
						for (Iterator<?> theIter = theWFs.iterator(); theIter.hasNext();) {
							WebFolder theFolder = (WebFolder)theIter.next();
							DataAccessProxy theDAP = theFolder.getDAP();
							if (theDAP != null && theDAP.exists()) {
								String[] entryNames = theDAP.getEntryNames();
								if (entryNames == null || entryNames.length == 0) {
									if (showDetail) {
										out.println("Folder " + theDAP.getName() + " can be deleted");
									}
									if (theTX == null && commit) {
										theTX = theFolder.getKnowledgeBase().beginTransaction();
									}
									
									try {
										sum++;
										if (commit) {
											theDAP.delete(false);
											theFolder.setValue(KOAttributes.PHYSICAL_RESOURCE, null);
											if(sum % 100 == 0) {
												Thread.sleep(10);
												try {
													theTX.commit();
													theTX = null;
												}
												catch (KnowledgeBaseException kex) {
													out.println("ERROR: Failed to commit deletion: " + kex);
												}
											}
											if (showDetail) {
												out.println("Deleted folder: " + theDAP.getName());
											}
										}
									}
									catch (DatabaseAccessException ex) {
										out.println("ERROR: Failed to delete folder: " + theDAP.getName());
										if (theTX != null) {
											theTX.rollback();
										}
									}
									
								}
							}
							counter ++;
							if (commit && counter % 100 == 0) {
								out.println("Handled " + counter + " of " + theWFSize + " webfolders. Deleted " + sum);
							}
						}
						
						if (commit && theTX != null) {	// Commit the rest
							try {
								theTX.commit();
							}
							catch (KnowledgeBaseException kex) {
								out.println("ERROR: Failed to commit deletion" + kex);
							}
						}
						out.print("Process finished");
						if (commit) {
							out.println(", deleted " + sum + " of " + theWFSize + " folders");
						} else {
							out.println(", no changes. " + sum + " of " + theWFSize + " folders can be deleted.");
						}
					} catch(Exception e){
						out.println("Failed to delete Webfolders "+e);
					}
					%>
				</pre>
				<%
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>