<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta"     prefix="meta"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.tool.boundsec.AbstractCommandHandler"%>


<%
AdminElementComponent theComponent  = (AdminElementComponent) MainLayout.getComponent(pageContext);
FormContext           theContext    = theComponent.getFormContext();
Object                theModel      = theComponent.getModel();
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<p>
				Diese Sicht zeigt Informationen zum Elternelement des gewählten Knoten.
				D.h. die Berechtigungen werden auch auf dem Elternknoten geprüft. Darf der Elternknoten nicht betrachtet werden, wird der Tabber nicht angezeigt.
				Dafür wurde in der Komponente die Methode EditComponent#receiveModelSelectedEvent() überschrieben so das nicht das selektierte Element als Modell gesetzt werden soll, sondern dessen Elternelement.
			</p>
			<p>
				<b>
					Achtung:
				</b>
				Das Standardverhalten in der EditComponent führt dazu, dass beim Setzen eines Modells, dies an den Master propagiert wird. Reagiert dieser darauf (z.B. HTMLTree) führt dies zu einem Event-Pingpong bei
				dem die Baum-Auswahl bis zum Root-Element hoch (und darüber hinaus) wechselt.
				Deshalb wurde hier als Master eine Komponente gewählt, die den Baum als Master hat aber selbst nicht auf ModelSet eines Slave reagiert.
			</p>
			<%
			if (theModel == null) {
				%>
				<form:resource key="noModel"/>
				<%
			}
			else if (!(theModel instanceof Wrapper)) {
				%>
				<basic:fieldset titleKeySuffix="basic">
					<table class="frm"
						border="0"
						summary="Basic information"
					>
						<colgroup>
							<col width="10%"/>
							<col width="*"/>
						</colgroup>
						<tr>
							<td class="label">
								<form:label name="<%=AdminElementComponent.ELEMENT_TYPE%>"/>
								:
							</td>
							<th class="content">
								<form:input name="<%=AdminElementComponent.ELEMENT_TYPE%>"/>
								<form:error name="<%=AdminElementComponent.ELEMENT_TYPE%>"/>
							</th>
						</tr>
						<form:ifExists name="<%=AdminElementComponent.ELEMENT_NAME%>">
							<tr>
								<td class="label">
									<form:label name="<%=AdminElementComponent.ELEMENT_NAME%>"/>
									:
								</td>
								<td class="content">
									<form:input name="<%=AdminElementComponent.ELEMENT_NAME%>"/>
									<form:error name="<%=AdminElementComponent.ELEMENT_NAME%>"/>
								</td>
							</tr>
						</form:ifExists>
					</table>
				</basic:fieldset>
				<%
			}
			else {
				%>
				<meta:group object="<%=(Wrapper) theModel%>">
					<table style="width:100%;">
						<tr>
							<td>
								<basic:fieldset titleKeySuffix="basic">
									<br/>
									<table class="frm"
										border="0"
										bordercolor="black"
										summary="Basic element information"
									>
										<form:ifExists name="<%=AbstractWrapper.NAME_ATTRIBUTE %>">
											<tr>
												<td class="label">
													<meta:label name="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
														colon="true"
													/>
												</td>
												<td class="content"
													width="100%"
												>
													<meta:attribute name="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
														preferTextArea="false"
													/>
												</td>
											</tr>
										</form:ifExists>
										<tr>
											<td class="label">
												<form:label name="<%=AdminElementComponent.ELEMENT_TYPE %>"/>
												:
											</td>
											<td class="content">
												<form:input name="<%=AdminElementComponent.ELEMENT_TYPE %>"/>
												<form:error name="<%=AdminElementComponent.ELEMENT_TYPE %>"/>
											</td>
										</tr>
										<tr>
											<td class="label">
												<form:label name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
												:
											</td>
											<td class="content">
												<form:select name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
												<form:error name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
											</td>
										</tr>
										<meta:exists name="priorityTable">
											<tr>
												<td class="label">
													<meta:label name="priorityTable"/>
													:
												</td>
												<td class="content">
													<meta:table name="priorityTable"
														columnNames="name boolean string float date"
														rowMove="true"
													/>
													<meta:error name="priorityTable"/>
												</td>
											</tr>
										</meta:exists>
									</table>
								</basic:fieldset>
							</td>
						</tr>
					</table>
					<meta:attributes legend="additional"/>
				</meta:group>
				<%
			}
			%>
		</form:form>
	</layout:body>
</layout:html>