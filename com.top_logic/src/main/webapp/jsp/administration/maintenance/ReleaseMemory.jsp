<%@page language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@page import="java.util.*"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.tool.boundsec.wrap.Group"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
%><layout:html>
	<layout:head/>
	<%
	boolean imported = "started".equals(request.getParameter("State"));
	boolean doDump = "yes".equals(request.getParameter("dump"));
	
	LayoutComponent theComp = MainLayout.getComponent(pageContext);
	String          startURL  = theComp.getComponentURL(pageContext).appendParameter("State", "started").getURL();
	String          endURL    = theComp.getComponentURL(pageContext).appendParameter("State", "started").appendParameter("dump", "yes").getURL();
	
	%>
	<layout:body>
		<basic:access>
			<h1>
				Garbage Collector manuell ausf�hren
			</h1>
			<%
			if (!imported) {
				%>
				<p>
					Durch Klicken den Vorgang
					<a href="<%=startURL %>">
						<b>
							starten
						</b>
					</a>
				</p>
				<p>
					Out Of Memory Error erzeugen, um Dump zu provozieren
					<a href="<%=endURL %>">
						<b>
							Out of Memory Error
						</b>
					</a>
				</p>

				<table class="tl-standard-table">
					<tr>
						<th colspan="2">
							Current Memory Status
						</th>
					</tr>
					<tr>
						<td>
							Max VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().maxMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Current VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().totalMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Available to VM
						</td>
						<td>
							<%=((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Used Memory in VM
						</td>
						<td>
							<%=((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Free Memory in VM
						</td>
						<td>
							<%=(Runtime.getRuntime().freeMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
				</table>
				<%
			}
			else {
				
				if(doDump) {
					
					try{
						ArrayList theList = new ArrayList();
						while(true){
							theList.add(new long[250000000]); //8 byte x 250 Million => 2GB
						}
					}catch(OutOfMemoryError err){
						out.write("Out of Memory Error created. <br/><br/>");
						out.flush();
					}
				}
				
				
				out.print("Bitte warten...<br/>");
				out.flush();
				%>
				<table class="tl-standard-table">
					<tr>
						<th colspan="2">
							Memory Status before GC
						</th>
					</tr>
					<tr>
						<td>
							Max VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().maxMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Current VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().totalMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Available to VM
						</td>
						<td>
							<%=((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Used Memory in VM
						</td>
						<td>
							<%=((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Free Memory in VM
						</td>
						<td>
							<%=(Runtime.getRuntime().freeMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
				</table>
				<hr/>
				<%
				System.runFinalization();
				System.gc();
				Thread.yield();
				%>
				<table class="tl-standard-table">
					<tr>
						<th colspan="2">
							Memory Status After GC
						</th>
					</tr>
					<tr>
						<td>
							Max VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().maxMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Current VM Size
						</td>
						<td>
							<%=(Runtime.getRuntime().totalMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Available to VM
						</td>
						<td>
							<%=((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Used Memory in VM
						</td>
						<td>
							<%=((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)/1024+" MB" %>
						</td>
					</tr>
					<tr>
						<td>
							Free Memory in VM
						</td>
						<td>
							<%=(Runtime.getRuntime().freeMemory()/1024)/1024+" MB" %>
						</td>
					</tr>
				</table>
				<%
				out.flush();
				%>
				<%
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>