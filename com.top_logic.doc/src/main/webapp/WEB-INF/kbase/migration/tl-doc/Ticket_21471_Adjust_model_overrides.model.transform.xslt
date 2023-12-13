<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<!--  remove history override attribute -->
	<xsl:template match="/config//module[@name='tl.model']//class[@name='TLReference']//property[@name='historyType']/@override"/>

	<!-- add backwards kind to parent reference -->
	<xsl:template match="/config//module[@name='tl.doc']//class[@name='Page']//reference[@name='parent']">
		<reference kind="backwards">
			<xsl:apply-templates select="@*|node()"/>
		</reference>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>