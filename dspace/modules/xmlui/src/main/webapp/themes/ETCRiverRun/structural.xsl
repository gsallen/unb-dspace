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
	
	<xsl:template name="buildHead">
		<head>
			<!-- @custom begin -->
			<!-- @todo: messages.xml reference -->
			<!-- @todo: make this an aspect, not a theme customization -->
			<title>RiverRun | UNB</title>
			<!-- @custom end -->
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
			<meta name="Generator">
				<xsl:attribute name="content">
					<xsl:text>DSpace</xsl:text>
					<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']">
						<xsl:text> </xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']"/>
					</xsl:if>
				</xsl:attribute>
			</meta>
			
			<!-- Add stylsheets -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="media">
						<xsl:value-of select="@qualifier"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
						<xsl:text>/themes/</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
						<xsl:text>/</xsl:text>
						<xsl:value-of select="."/>
					</xsl:attribute>
				</link>
			</xsl:for-each>
			
			<!-- Add syndication feeds -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
				<link rel="alternate" type="application">
					<xsl:attribute name="type">
						<xsl:text>application/</xsl:text>
						<xsl:value-of select="@qualifier"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="."/>
					</xsl:attribute>
				</link>
			</xsl:for-each>
			
			<!--  Add OpenSearch auto-discovery link -->
			<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']">
				<link rel="search" type="application/opensearchdescription+xml">
					<xsl:attribute name="href">
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='scheme']"/>
						<xsl:text>://</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverName']"/>
						<xsl:text>:</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverPort']"/>
						<xsl:value-of select="$context-path"/>
						<xsl:text>/</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='context']"/>
						<xsl:text>description.xml</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="title" >
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']"/>
					</xsl:attribute>
				</link>
			</xsl:if>
			
			<!-- The following javascript removes the default text of empty text areas when they are focused on or submitted -->
			<!-- There is also javascript to disable submitting a form when the 'enter' key is pressed. -->
			<script type="text/javascript">
                                //Clear default text of emty text areas on focus
                                function tFocus(element)
                                {
                                        if (element.value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){element.value='';}
                                }
                                //Clear default text of emty text areas on submit
                                function tSubmit(form)
                                {
                                        var defaultedElements = document.getElementsByTagName("textarea");
                                        for (var i=0; i != defaultedElements.length; i++){
                                                if (defaultedElements[i].value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){
                                                        defaultedElements[i].value='';}}
                                }
                                //Disable pressing 'enter' key to submit a form (otherwise pressing 'enter' causes a submission to start over)
                                function disableEnterKey(e)
                                {
                                     var key;
                                
                                     if(window.event)
                                          key = window.event.keyCode;     //Internet Explorer
                                     else
                                          key = e.which;     //Firefox and Netscape
                                
                                     if(key == 13)  //if "Enter" pressed, then disable!
                                          return false;
                                     else
                                          return true;
                                }
            </script>
			
			<!-- Add theme javascipt  -->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][not(@qualifier)]">
				<script type="text/javascript">
                    <xsl:attribute name="src">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/themes/</xsl:text>
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="."/>
                    </xsl:attribute>&#160;</script>
			</xsl:for-each>
			
			<!-- add "shared" javascript from static, path is relative to webapp root-->
			<xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='static']">
				<script type="text/javascript">
                    <xsl:attribute name="src">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="."/>
                    </xsl:attribute>&#160;</script>
			</xsl:for-each>
			
			
			<!-- Add a google analytics script if the key is present -->
			<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']">
				<script type="text/javascript">
                                        <xsl:text>var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");</xsl:text>
                                        <xsl:text>document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));</xsl:text>
                                </script>
				
				<script type="text/javascript">
                                        <xsl:text>try {</xsl:text>
                                                <xsl:text>var pageTracker = _gat._getTracker("</xsl:text><xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']"/><xsl:text>");</xsl:text>
                                                <xsl:text>pageTracker._trackPageview();</xsl:text>
                                        <xsl:text>} catch(err) {}</xsl:text>
                                </script>
			</xsl:if>
			
			
			<!-- Add the title in -->
			<xsl:variable name="page_title" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']" />
			<title>
				<xsl:choose>
					<xsl:when test="not($page_title)">
						<xsl:text>  </xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="$page_title/node()" />
					</xsl:otherwise>
				</xsl:choose>
			</title>
			
			<!-- Head metadata in item pages -->
			<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']">
				<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']"
					disable-output-escaping="yes"/>
			</xsl:if>
			
		</head>
	</xsl:template>
	
  <xsl:template name="buildFooter">
      <div id="ds-footer">
    		<div id="ds-footer-logo-block">
    		  <div id="ds-footer-logo-lib" class="ds-footer-logo">
    			  <a href="http://lib.unb.ca" title="UNB Libraries">
    			    <img src="/xmlui/themes/ETCRiverRun/images/unb-libraries-logo.gif" alt="UNB Libraries"/>
    			  </a>
			    </div>
			    <div id="ds-footer-logo-etc" class="ds-footer-logo">
    			  <a href="http://lib.unb.ca/etc" title="Electronic Text Centre">
    			    <img src="/xmlui/themes/ETCRiverRun/images/etc-logo-small.jpg" alt="Electronic Text Centre"/>
    			  </a>
			    </div>
    		</div>
          <div id="ds-footer-text"><i18n:text>xmlui.dri2xhtml.structural.footer-promotional</i18n:text></div>
          <div id="ds-footer-links">
              <a>
                  <xsl:attribute name="href">
                      <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                      <xsl:text>/contact</xsl:text>
                  </xsl:attribute>
                  <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
              </a>
              <xsl:text> | </xsl:text>
              <a>
                  <xsl:attribute name="href">
                      <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                      <xsl:text>/feedback</xsl:text>
                  </xsl:attribute>
                  <i18n:text>xmlui.dri2xhtml.structural.feedback-link</i18n:text>
              </a>
          </div>
          <!--Invisible link to HTML sitemap (for search engines) -->
          <a>
              <xsl:attribute name="href">
                  <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                  <xsl:text>/htmlmap</xsl:text>
              </xsl:attribute>
          </a>
      </div>
  </xsl:template>
</xsl:stylesheet>
