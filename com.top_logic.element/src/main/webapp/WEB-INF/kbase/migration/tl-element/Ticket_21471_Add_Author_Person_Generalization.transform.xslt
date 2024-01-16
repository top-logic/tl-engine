<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>

	<xsl:template match="/config//module[@name='tl.accounts']//class[@name='Person' and not(generalizations)]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="node()"/>
			<generalizations>
				<generalization type="tl.core:Author"/>
			</generalizations>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/config//module[@name='tl.accounts']//class[@name='Person']/generalizations">
		<xsl:copy-of select="generalization[@type != 'tl.core:Author']"/>
		<generalization type="tl.core:Author"/>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>