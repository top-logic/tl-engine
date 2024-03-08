<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="@type">
		<xsl:choose>
			<xsl:when test="string(.) = 'tl.tables:ExternalReferenceTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Ticket:ExternalReference'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.legacy.tabletypes:ExternalReferenceTable'"><xsl:attribute name="type"><xsl:value-of select="'Ticket:ExternalReference'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ActivityTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'RiskItems:Activity'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ApproachingCurveTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'ApproachingCurveStructure:ApproachingCurveType'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:AssignedGoalTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'AssignedGoals:AssignedGoal'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CommitteeTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'CommitteeStruct:Committee'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CostTypeTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'costStructure:CostAreaChild'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CostUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'costStructure:CostUnit.internal'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:DecisionMemoTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Decisions:DecisionMemo'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ExportTemplatePlaceholderTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'posElement:ExportTemplatePlaceholder'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ExportTemplateTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'posElement:ExportTemplate'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:FinalReportTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'FinalReports:FinalReport'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:GovernedTaskTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'GovernedObjects:GovernedTask'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:KPGroupTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'KPI:KPI.Group'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:MeetingTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Meetings:Meetings.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:MilestoneTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Milestones:Milestones.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:OrgUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'orgStructure:OrgUnit.base'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProgramTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Programs:Program'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProjectResultTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'FinalReports:ProjectResult'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProjectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Project'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ScenarioTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'ScenarioStruct:Scenario.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:StatusReportDetailTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'StatusReports:StatusReportDetail'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:StatusReportTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'StatusReports:StatusReport'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:SubprojectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Subproject'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TagTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Tag:Tag'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TargetProductTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Programs:TargetProduct'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TemplateActivityTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projectTemplate:Activity'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TemplateMilestoneTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projectTemplate:projectTemplate.Milestone'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:WorkpackageTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Workpackage'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ObjectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>

			<xsl:when test="string(.) = 'tl.legacy.tabletypes:PersonTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Person'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:PersonTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Person'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:GroupTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Group'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:WebFolderTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:WebFolder'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:DocumentTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:Document'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:DocumentVersionTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:DocumentVersion'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:AbstractUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:AbstractUnit'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:CurrencyTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:Currency'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:UnitTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:Unit'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:CommentTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.comments:Comment'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MetaElementTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClass'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:FastListEltTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClassifier'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:FastListEltTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClassifier'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MetaAttributeTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLStructuredTypePart'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:FastListTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLEnumeration'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:AddressTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Address'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:BoundedRoleTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Role'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:UsedSkillProfileTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Resources:UsedSkillProfile'"/></xsl:attribute></xsl:when> 
			<xsl:when test="string(.) = 'tl.tables:ContactTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Contacts:Contact.Person'"/></xsl:attribute></xsl:when>
    
		    <xsl:when test="starts-with(string(.), 'tl.tables:')"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>
		    <xsl:when test="starts-with(string(.), 'tl.legacy.tabletypes:')"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="reference-builder">
		<!-- Remove. -->
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>