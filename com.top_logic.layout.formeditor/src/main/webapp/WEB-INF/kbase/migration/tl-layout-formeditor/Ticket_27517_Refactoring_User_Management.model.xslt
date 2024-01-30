<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//module[@name='orgStructure']//interface[@name='OrgUnit.base']//reference[@name='boss']/@type">
		<xsl:attribute name="type">Contacts:PersonContact</xsl:attribute>
	</xsl:template>

	<xsl:template match="/config//module[@name='orgStructure']//interface[@name='OrgUnit.base']//reference[@name='member']/@type">
		<xsl:attribute name="type">Contacts:PersonContact</xsl:attribute>
	</xsl:template>

	<xsl:template match="/config//module[@name='Contacts']//interface[@name='Contact.Person']//reference[@name='boss']/@type">
		<xsl:attribute name="type">Contacts:PersonContact</xsl:attribute>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>