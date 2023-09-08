<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.meta.search.AttributedSearchComponent"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFilterSupport"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta" prefix="meta"
%><%@taglib uri="util" prefix="util"
%><%
AttributedSearchComponent theComp = (AttributedSearchComponent) MainLayout.getComponent(pageContext);
boolean                   useRel  = theComp.getRelevantAndNegate();
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<form:columns count="3">
				<util:if condition="<%= theComp.isMultiMetaElementSearch() %>">
					<form:descriptionCell>
						<form:description>
							<form:label name="<%=SearchFieldSupport.META_ELEMENT %>"/>
						</form:description>
						<form:select name="<%=SearchFieldSupport.META_ELEMENT %>"/>
					</form:descriptionCell>
				</util:if>
				<form:descriptionCell>
					<form:description>
						<form:label name="<%=SearchFieldSupport.STORED_QUERY %>"/>
					</form:description>
					<form:select name="<%=SearchFieldSupport.STORED_QUERY %>"
						width="300px"
					/>
				</form:descriptionCell>
				<%@include file="/jsp/element/metaattributes/PublishForm_inline.inc" %>
			</form:columns>
			<form:columns count="2">
				<%-- Standard parameter and extended search --%>
				<form:cell width="80%">
					<meta:group object="<%=theComp.getSearchMetaElement() %>">
						<form:groupCell
							columns="2"
							labelAbove="true"
							titleKeySuffix="basic"
						>
							<%
							String theRelAndUseName;
							%>
							<form:descriptionCell
								cssClass="sizeSet"
								splitControls="true"
							>
								<form:description>
									<meta:label name="<%= AbstractWrapper.NAME_ATTRIBUTE %>"/>
								</form:description>
								<meta:attribute name="<%= AbstractWrapper.NAME_ATTRIBUTE %>"
									inputSize="35"
								/>
							</form:descriptionCell>

							<form:descriptionCell
								cssClass="sizeSet"
								splitControls="true"
							>
								<form:description>
									<form:label name="<%=SearchFieldSupport.FULL_TEXT %>"/>
								</form:description>
								<%
								theRelAndUseName = SearchFilterSupport.getRelevantAndNegateMemberName(SearchFieldSupport.FULL_TEXT);
								if (useRel) { %>
									<form:tristate name="<%= theRelAndUseName %>"/>
								<% } %>
								<form:input name="<%=SearchFieldSupport.FULL_TEXT %>"
									columns="25"
								/>
								<form:checkbox name="<%= SearchFieldSupport.FULLTEXT_PARAM_MODE %>"/>
								<form:resource key="<%= SearchFieldSupport.FULLTEXT_PARAM_MODE %>"/>
							</form:descriptionCell>
						</form:groupCell>
						<util:if condition="<%= theComp.isExtendedSearch() %>">
							<meta:attributes
								columns="2"
								exclude="<%= theComp.getExcludeSet() %>"
								firstColumnWidth="17em"
								labelAbove="true"
								legend="additional"
								splitControls="true"
							/>
						</util:if>
					</meta:group>
				</form:cell>
				<%-- column selection --%>
				<form:cell width="20%">
					<basic:fieldset titleKeySuffix="attributes.columns">
						<form:cell wholeLine="true">
							<form:custom name="<%= SearchFieldSupport.TABLE_COLUMNS %>"
								controlProvider="<%= theComp.TABLE_PROVIDER %>"
							/>
							<form:error name="<%= SearchFieldSupport.TABLE_COLUMNS %>"/>
						</form:cell>
					</basic:fieldset>
				</form:cell>
			</form:columns>
		</form:form>
	</layout:body>
</layout:html>