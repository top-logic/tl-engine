<%@taglib uri="layout" prefix="layout"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.knowledge.indexing.DefaultIndexingService"
%><%@page import="com.top_logic.knowledge.indexing.IndexingService"
%><%@page import="java.util.Collection"
%><%@page import="java.util.Iterator"
%><%@page import="java.util.List"
%><%@page import="java.util.Arrays"
%><layout:html>
	<layout:head/>
	<%@page language="java" session="true"
	extends="com.top_logic.util.TopLogicJspBase" %>
	<layout:body>
		<basic:access>
			<%
			// extract the perform parameter form the request.
			
			boolean doIndexing   = Boolean.parseBoolean(request.getParameter("doIndexing"));
			boolean rebuildIndex = Boolean.parseBoolean(request.getParameter("rebuildIndex"));
			boolean startIndex   = Boolean.parseBoolean(request.getParameter("startIndex"));
			boolean stopIndex    = Boolean.parseBoolean(request.getParameter("stopIndex"));
			
			IndexingService indexService = DefaultIndexingService.getIndexingService();
			
			if(doIndexing) {
				String[] theTypesArr = request.getParameterValues("types");
				List<String> theTypes = (theTypesArr != null) ? Arrays.asList(theTypesArr) : null;
				boolean onlyNew = Boolean.parseBoolean(request.getParameter("onlyNew"));
				int sum = indexService.reindex(theTypes, null, onlyNew);
				%>
				<h2>
					Indexing of <%= sum %> objects performed!
				</h2>
				<%	} else if (rebuildIndex) {
				indexService.rebuildIndex();
				%>
				<h2>
					Rebuilding index finished!
				</h2>
				<%	} else if (startIndex) {
				indexService.startIndex();
				%>
				<h2>
					Started index!
				</h2>
				<%	} else if (stopIndex) {
				indexService.stopIndex();
				%>
				<h2>
					Stopped index!
				</h2>
				<%	} else {
				%>
				<h2>
					Starting &amp; Stopping
				</h2>
				<form method="post">
					<input name="startIndex"
						type="hidden"
						value="true"
					/>
					<input name="startIndexButton"
						type="submit"
						value="Start Index"
					/>
				</form>
				<form method="post">
					<input name="stopIndex"
						type="hidden"
						value="true"
					/>
					<input name="stopIndexButton"
						type="submit"
						value="Stop Index"
					/>
				</form>
				<h2>
					Rebuilding Search Index
				</h2>
				Please make sure the application is in maintenance mode and no one adds, changes or deletes data while the rebuild is running.
				<br/>
				Also, if the application is running with more than one cluster node, please stop the index on all the other cluster nodes.
				<form method="post">
					<input name="rebuildIndex"
						type="hidden"
						value="true"
					/>
					<input name="rebuildIndexButton"
						type="submit"
						value="Start rebuilding the index"
					/>
					(WARNING: this may take hours!)
				</form>

				<h2>
					Feed all objects of the selected types to the indexing service
				</h2>

				<form method="post">
					<input name="doIndexing"
						type="hidden"
						value="true"
					/>
					<table>
						<%
						Collection<String> theTypes = indexService.getTypesToIndex();
						if (theTypes != null) {
							for (Iterator<String> theTypesIt=theTypes.iterator(); theTypesIt.hasNext(); ) {
								String theType = theTypesIt.next();
								%>
								<tr>
									<td>
										<%= theType %>:
									</td>
									<td>
										<input name="types"
											type="checkbox"
											value="<%= theType %>"
										/>
										<br/>
									</td>
								</tr>
								<%
							}
						}
						%>
						<tr>
							<td>
								Only new objects:
							</td>
							<td>
								<input name="onlyNew"
									type="checkbox"
									value="true"
								/>
							</td>
						</tr>
						<tr>
							<td>
								<input name="startIndexing"
									type="submit"
									value="Start re-indexing"
								/>
								(WARNING: this may take hours!)
							</td>
						</tr>
					</table>
				</form>
			<%  }
			%>
		</basic:access>
	</layout:body>
</layout:html>