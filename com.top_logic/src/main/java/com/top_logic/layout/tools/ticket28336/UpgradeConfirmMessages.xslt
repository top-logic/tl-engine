<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="//confirmMessage[not(parent::confirmation)]">
		<confirmation class="com.top_logic.tool.boundsec.confirm.CustomConfirmation">
			<xsl:copy>
				<xsl:apply-templates select="node()|@*"/>
			</xsl:copy>
		</confirmation>
	</xsl:template>

	<xsl:template match="//*[@confirm='true' and not(@confirmMessage = '') and not(confirmMessage)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<confirmation class="com.top_logic.tool.boundsec.confirm.CustomConfirmation"
				confirmMessage="{@confirmMessage}"
			>
			</confirmation>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*[@confirm='true' and (not(@confirmMessage) or @confirmMessage = '') and not(confirmMessage)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<confirmation class="com.top_logic.tool.boundsec.confirm.DefaultConfirmation"/>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//*[@confirm='false']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="confirmation"></xsl:attribute>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@confirm">
		<!-- Handled above. -->
	</xsl:template>

	<xsl:template match="confirmation/@confirmMessage">
		<!-- Make idempotent. -->
		<xsl:copy/>
	</xsl:template>

	<xsl:template match="@confirmMessage">
		<!-- Handled above. -->
	</xsl:template>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>