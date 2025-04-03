<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="//*[@confirm='true']/confirmMessage">
		<confirmation class="com.top_logic.tool.boundsec.confirm.CustomConfirmation">
			<xsl:copy>
				<xsl:apply-templates select="node()|@*"/>
			</xsl:copy>
		</confirmation>
	</xsl:template>

	<xsl:template match="@confirm[.='false']">
		<xsl:attribute name="confirmation"></xsl:attribute>
	</xsl:template>

	<xsl:template match="@confirm[.='true']">
	</xsl:template>

	<xsl:template match="//*[@confirm='true'][not(confirmMessage)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<confirmation class="com.top_logic.tool.boundsec.confirm.DefaultConfirmation"/>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="confirmMessage[parent::*/@confirm='false']">
	</xsl:template>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>