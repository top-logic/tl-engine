<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<!-- A color annotation that already names a role stays as it is. -->
	<xsl:template match="//annotations/color[@role]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- A support token of the theme becomes the meaning of the same name. -->
	<xsl:template match="//annotations/color[not(@role) and not(@value) and (@token='support-error' or @token='support-warning' or @token='support-success' or @token='support-info')]"
		priority="1"
	>
		<xsl:copy>
			<xsl:apply-templates select="@*[local-name() != 'token']"/>
			<xsl:attribute name="role">
				<xsl:value-of select="substring-after(@token, 'support-')"/>
			</xsl:attribute>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- The interactive token of the theme is the color of the brand. -->
	<xsl:template match="//annotations/color[not(@role) and not(@value) and @token='interactive']"
		priority="1"
	>
		<xsl:copy>
			<xsl:apply-templates select="@*[local-name() != 'token']"/>
			<xsl:attribute name="role">
				<xsl:value-of select="'brand'"/>
			</xsl:attribute>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- A literal color value or any other token names no role, the literal is displayed without a color. -->
	<xsl:template match="//annotations/color[not(@role)]">
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>