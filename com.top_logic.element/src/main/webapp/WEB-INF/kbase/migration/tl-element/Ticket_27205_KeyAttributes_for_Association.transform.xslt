<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/association[@object_name='hasWrapperAttValueBaseAssociation']">
		<xsl:copy>
			<!-- The XSLT implementation has a bug which does not copy attributes,
			when the "copy attributes and tags" statement (<xsl:apply-templates select="@* | node()"/>) is after the added tags,
			i.e. after the "annotations" Tag. It is not possible to move the entire copy rule to the top,
			as the "annotations" tag should be the first tag in the "association" tag.
			Therefore, the copy statement has to be split into "copy the attributes" and "copy the tags". -->
			<xsl:apply-templates select="@*"/>
			<annotations>
				<key-attributes attributes="source,dest,metaAttribute"/>
			</annotations>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
