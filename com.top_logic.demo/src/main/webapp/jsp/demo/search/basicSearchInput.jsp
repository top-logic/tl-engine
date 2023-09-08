<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.layout.SimpleAccessor"
%><%@page import="com.top_logic.layout.table.renderer.DefaultTableRenderer"
%><%@page import="com.top_logic.element.layout.meta.search.AttributedSearchComponent"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFilterSupport"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta" prefix="meta"
%><%@taglib uri="util" prefix="util"
%><%@taglib uri="basic" prefix="basic"
%><%
AttributedSearchComponent theComp = (AttributedSearchComponent) MainLayout.getComponent(pageContext);
DefaultTableRenderer theRenderer  = DefaultTableRenderer.newInstance();
boolean useRel                    = theComp.getRelevantAndNegate();
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<table width="100%">
				<tr>
					<td style="width:5px">
					</td>
					<util:if condition="<%= theComp.isMultiMetaElementSearch() %>">
						<td class="label"
							style="width: 65px;"
						>
							<form:label name="<%=SearchFieldSupport.META_ELEMENT %>"
								colon="true"
							/>
						</td>
						<td class="content"
							style="width: 120px;"
						>
							<form:select name="<%=SearchFieldSupport.META_ELEMENT %>"/>
						</td>
						<td style="width:5px">
						</td>
					</util:if>
					<td class="label"
						style="width: 50px;"
					>
						<form:label name="<%=SearchFieldSupport.STORED_QUERY %>"
							colon="true"
						/>
					</td>
					<td class="content"
						style="width: 125px;"
					>
						<form:select name="<%=SearchFieldSupport.STORED_QUERY %>"
							width="260px"
						/>
					</td>
					<%@include file="/jsp/element/metaattributes/PublishForm_inline.inc" %>
				</tr>
			</table>
			<form:horizontal>
				<%-- Standard parameter and extended search --%>
				<form:cell width="80%">
					<meta:group object="<%=theComp.getSearchMetaElement() %>">
						<form:vertical>
							<basic:fieldset titleKeySuffix="basic">
								<%
								String theRelAndUseName;
								%>
								<table style="margin-top: 10px;">
									<tr>
										<td class="label">
											<meta:label name="<%= AbstractWrapper.NAME_ATTRIBUTE %>"
												colon="true"
											/>
										</td>
										<td class="content">
											<meta:attribute name="<%= AbstractWrapper.NAME_ATTRIBUTE %>"
												inputSize="35"
											/>
										</td>
									</tr>
									<tr>
										<td class="label">
											<form:label name="<%=SearchFieldSupport.FULL_TEXT %>"
												colon="true"
											/>
										</td>
										<%
										theRelAndUseName = SearchFilterSupport.getRelevantAndNegateMemberName(SearchFieldSupport.FULL_TEXT);
										%>
										<td class="content">
											<% if (useRel) { %>
												<form:tristate name="<%= theRelAndUseName %>"/>
											<% } %>
											<form:input name="<%=SearchFieldSupport.FULL_TEXT %>"
												columns="25"
											/>
											<form:checkbox name="<%= SearchFieldSupport.FULLTEXT_PARAM_MODE %>"/>
											<form:resource key="<%= SearchFieldSupport.FULLTEXT_PARAM_MODE %>"/>
										</td>
									</tr>
								</table>
							</basic:fieldset>
							<util:if condition="<%= theComp.isExtendedSearch() %>">
								<meta:attributes
									exclude="<%= theComp.getExcludeSet() %>"
									legend="additional"
								/>
							</util:if>
						</form:vertical>
					</meta:group>
				</form:cell>
				<form:emptyCell/>
				<%-- column selection --%>
				<form:cell width="20%">
					<basic:fieldset titleKeySuffix="attributes.columns">
						<form:custom name="<%= SearchFieldSupport.TABLE_COLUMNS %>"
							controlProvider="<%= theComp.TABLE_PROVIDER %>"
						/>
						<form:error name="<%= SearchFieldSupport.TABLE_COLUMNS %>"/>
					</basic:fieldset>
				</form:cell>
			</form:horizontal>
		</form:form>
	</layout:body>
</layout:html>