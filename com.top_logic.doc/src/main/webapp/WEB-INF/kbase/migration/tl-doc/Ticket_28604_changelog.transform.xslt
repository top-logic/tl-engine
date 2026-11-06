<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<!-- The stored baseline lists the attributes of a type directly in the "metaobject" tag, without an "attributes" container.
	The "annotations" tag is the first tag in the "metaobject" tag. Existing annotations are merged into it,
	an existing "key-attributes" entry is replaced. -->
	<xsl:template match="/config/meta-objects/metaobject[@object_name='tl.doc.Page']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<annotations>
				<xsl:apply-templates select="annotations/@* | annotations/node()[not(self::key-attributes)]"/>
				<key-attributes attributes="parent"/>
			</annotations>
			<xsl:apply-templates select="node()[not(self::annotations)]"/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>