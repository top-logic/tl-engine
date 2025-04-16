<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.knowledge.wrap.WrapperFactory"
%><%@page import="java.util.Collection"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.knowledge.wrap.WebFolder"
%><%@page import="java.util.List"
%><%@page import="com.top_logic.dsa.DataAccessProxy"
%><%@page import="java.util.ArrayList"
%><layout:html>
	<layout:head/>
	<layout:body>
		<basic:access>
			<h1>
				Re-generate missing repository folders
			</h1>
			<%
			boolean display = request.getParameter("action") == null;
			if (display) {
				%>
				<p>
					This page is able to reconstruct missing folders in the repository filesystem, it is not able  recover
					lost files. However, if you accidentally deleted empty folders from the repository filesystem, executing
					this function can reconstruct the missing folders and fixes errors during uploading to folders that no
					longer exist in the filesystem.
				</p>
				<form method="post">
					<button class="tlButton cButton cmdButton"
						name="action"
						type="submit"
					>
						Recreate folders
					</button>
				</form>
				<%
			} else {
				%>
				<pre><![CDATA[]]><%
					out.println("Starting...");
					try {
						KnowledgeBase theKB = WebFolder.getDefaultKnowledgeBase();
						Collection folders = WrapperFactory.getWrappersByType(WebFolder.OBJECT_NAME, theKB);
						int batch = 0;
						int count = 0;
						for (Object folder : folders) {
							WebFolder webFolder = (WebFolder)folder;
							DataAccessProxy proxy = webFolder.getDAP();
							DataAccessProxy parent = proxy.getParentProxy();
							List<DataAccessProxy> missing = new ArrayList<>();
							while(!proxy.exists() && parent != null) {
								missing.add(proxy);
								proxy = parent;
								parent = parent.getParentProxy();
							}
							
							DataAccessProxy outer = proxy;
							for (int i = missing.size() - 1; i >= 0; i--) {
								DataAccessProxy inner = (DataAccessProxy)missing.get(i);
								out.println("Creating folder: " + inner.getPath());
								outer.createContainer(inner.getName());
								batch++;
								count++;
								outer = inner;
								if (batch % 100 == 0) {
									Thread.sleep(10);
									batch = 0;
								}
							}
						}
						out.println("Process finished, created " + count + " folders");
					} catch (Exception e){
						out.println("Failed to create folders: " + e.getMessage());
					}
					%></pre>
				<%
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>