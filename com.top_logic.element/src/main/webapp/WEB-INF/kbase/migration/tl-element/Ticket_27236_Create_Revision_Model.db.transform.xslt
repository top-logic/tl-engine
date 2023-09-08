<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//metaobject[@object_name='Item']">
		<metaobject
			object_name="Revision"
			versioned="false"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.SystemAnnotation"/>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation">
					<implementation-factory class="com.top_logic.knowledge.service.db2.RevisionFactory"/>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.DBAccessFactoryAnnotation">
					<db-access class="com.top_logic.knowledge.service.db2.RevisionDBAccess$Factory"/>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation"
					builder="com.top_logic.knowledge.search.MonomorphicQueryBuilder"
				/>
			</annotations>
			<attributes>
				<mo_attribute att_name="_identifier" db_name="rev" att_type="Long" mandatory="true" immutable="true" system="true">
					<storage class="com.top_logic.knowledge.service.db2.RevisionType$RevisionStorage"/>
				</mo_attribute>
				<mo_attribute att_name="author" db_name="author" att_type="String" db_type="String" db_size="256" mandatory="true" immutable="true" system="false">
					<storage class="com.top_logic.knowledge.service.db2.RevisionType$AuthorStorage"/>
				</mo_attribute>
				<mo_attribute att_name="date" db_name="date" att_type="Long" mandatory="true" immutable="true" system="false">
					<storage class="com.top_logic.knowledge.service.db2.RevisionType$DateStorage"/>
				</mo_attribute>
				<mo_attribute att_name="log" db_name="log" att_type="String" db_type="Clob" db_size="4096" mandatory="true" immutable="true" system="false">
					<storage class="com.top_logic.knowledge.service.db2.RevisionType$LogStorage"/>
				</mo_attribute>
			</attributes>
			<primary-key>
				<index-parts>
					<index_part name="_identifier"/>
				</index-parts>
			</primary-key>
			<index>
				<mo_index name="IDX_DATE" unique="false" custom="true">
					<index-parts>
						<index_part name="date"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
		
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