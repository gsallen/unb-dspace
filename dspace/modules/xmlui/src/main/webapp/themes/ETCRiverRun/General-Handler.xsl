<?xml version="1.0" encoding="UTF-8"?>
<!-- @todo describe me -->    

<xsl:stylesheet 
	xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim" 
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	
	<xsl:output indent="yes"/>
	
	<!-- Generate the bitstream information from the file section -->
	<xsl:template match="mets:fileGrp[@USE='CONTENT']">
		<xsl:param name="context"/>
		<xsl:param name="primaryBitstream" select="-1"/>
		<!-- @custom begin -->		
		<!-- @todo: move inline styles to style.css -->
		<h2 class='collapseclass' style='width:550px; padding-left:20px; padding-top:0px;'><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
		<div style='overflow: hidden; margin:0px; padding:0px; display:none;'>
		<!-- @custom end -->
			<table class="ds-table file-list">
				<tr class="ds-table-header-row">
					<th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text></th>
					<th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text></th>
					<th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text></th>
					<th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-view</i18n:text></th>
					<!-- Display header for 'Description' only if at least one bitstream contains a description -->
					<xsl:if test="mets:file/mets:FLocat/@xlink:label != ''">
						<th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-description</i18n:text></th>
					</xsl:if>
				</tr>
				
				<xsl:choose>
					<!-- If one exists and it's of text/html MIME type, only display the primary bitstream -->
					<xsl:when test="mets:file[@ID=$primaryBitstream]/@MIMETYPE='text/html'">
						<xsl:apply-templates select="mets:file[@ID=$primaryBitstream]">
							<xsl:with-param name="context" select="$context"/>
						</xsl:apply-templates>
					</xsl:when>
					<!-- Otherwise, iterate over and display all of them -->
					<xsl:otherwise>
						<xsl:apply-templates select="mets:file">
							<xsl:sort data-type="number" select="boolean(./@ID=$primaryBitstream)" order="descending" />
							<xsl:sort select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/> 
							<xsl:with-param name="context" select="$context"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
			</table>
		<!-- @custom begin -->
		</div>
		<!-- @custom end -->
	</xsl:template>   
	
</xsl:stylesheet>
