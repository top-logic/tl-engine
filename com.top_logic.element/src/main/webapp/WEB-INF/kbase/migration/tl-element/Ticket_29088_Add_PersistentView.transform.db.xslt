<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="metaobject[@object_name='PersBoundComp']/annotations/annotation/binding[@application-type='com.top_logic.tool.boundsec.wrap.PersBoundComp']">
		 <binding application-type="com.top_logic.element.meta.kbbased.PersistentView"/>
	</xsl:template>
	
   <xsl:template match="metaobject[@object_name='PersBoundComp']/annotations">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()[not(self::key-attributes)]"/>
            <key-attributes attributes="tType"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="metaobject[@object_name='PersBoundComp']/attributes">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()[not(self::mo_attribute[@att_name='tType'])]"/>
            <xsl:call-template name="tType"/>
        </xsl:copy>
    </xsl:template>

    <!-- The stored baseline lists the attributes directly in the "metaobject" tag, without an "attributes" container:
    "tType" is added after the last attribute, an existing "tType" attribute is replaced. -->
    <xsl:template match="metaobject[@object_name='PersBoundComp'][not(attributes)]/*[self::mo_attribute or self::reference][not(@att_name='tType')][last()]">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
        <xsl:call-template name="tType"/>
    </xsl:template>

    <xsl:template match="metaobject[@object_name='PersBoundComp'][not(attributes)]/mo_attribute[@att_name='tType']"/>

    <xsl:template name="tType">
        <mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
            att_name="tType"
            att_type="MetaElement"
            mandatory="true">
            <storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
                <value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
                    type="tl.accounts:PersistentView"/>
            </storage>
        </mo_attribute>
    </xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>