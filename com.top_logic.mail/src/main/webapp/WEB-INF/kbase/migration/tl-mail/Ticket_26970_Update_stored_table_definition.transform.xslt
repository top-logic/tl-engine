<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:variable name="mail_type"
		select="/config/meta-objects/metaobject[@object_name='Mail']/mo_attribute[@att_name='tType']"
	/>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Mail']/mo_attribute[@att_name='name']">
		<xsl:if test="count($mail_type) = 0">
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.mail:Mail"
					/>
				</storage>
			</mo_attribute>
		</xsl:if>
		<xsl:copy-of select="."/>
	</xsl:template>

	<xsl:variable name="mailfolder_type"
		select="/config/meta-objects/metaobject[@object_name='MailFolder']/mo_attribute[@att_name='tType']"
	/>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='MailFolder']/mo_attribute[@att_name='name']">
		<xsl:if test="count($mailfolder_type) = 0">
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.mail:MailFolder"
					/>
				</storage>
			</mo_attribute>
		</xsl:if>
		<xsl:copy-of select="."/>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>