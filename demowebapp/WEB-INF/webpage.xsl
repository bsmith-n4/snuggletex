<?xml version="1.0"?>
<!--

$Id: header.jspf 2712 2008-03-10 17:01:01Z davemckain $

THIS WILL BE DEPRECATED SHORTLY!!!!

Stylesheet to generate the SnuggleTeX documentation pages
from skeleton HTML of the following form:

<html xmlns="...">
  <head>
    <title>...</title>
  </head>
  <body id="...">
    ... Main Content ...
  </body>
</html>

This adds in the rest of the <head/> stuff, standard headers & footers
and the navigation menu.

Relative links are fixed up so they are relative to the supplied
context-path.

Copyright (c) 2008 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="html">

  <xsl:output method="xhtml"/>

  <!-- Need to pass webapp context path so as to locate images and stuff -->
  <xsl:param name="context-path"/>

  <xsl:template match="html:html">
    <html xml:lang="en" lang="en">
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:lang">en</xsl:attribute>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="html:head">
    <head>
      <meta http-equiv="Content-Type: text/html; charset=UTF-8" />
      <meta name="description" content="SnuggleTeX Documentation" />
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <xsl:apply-templates select="html:title"/>
      <link rel="stylesheet" href="{$context-path}/includes/core.css" />
      <link rel="stylesheet" href="{$context-path}/includes/website.css" />
    </head>
  </xsl:template>

  <xsl:template match="html:body">
    <body id="{@id}">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" id="header">
        <tr>
          <td width="84" align="left" valign="top">
            <a href="http://www.ed.ac.uk" class="headertext"><img
              src="{$context-path}/includes/uoe_logo.jpg"
              alt="The University of Edinburgh" id="logo" name="logo"
              width="84" height="84" border="0" /></a>
          </td>
          <td align="left">
            <h3>THE UNIVERSITY of EDINBURGH</h3>
            <h1>SCHOOL OF PHYSICS AND ASTRONOMY</h1>
          </td>
          <td align="right">
            <table border="0" cellpadding="0" cellspacing="0" style="margin-right:20px;">
              <tr>
                <td>
                  <a href="http://www.ed.ac.uk" class="headertext"><img
                    src="{$context-path}/includes/arrow.gif"
                    width="16" height="11" border="0"
                    alt="" />University&#160;Homepage</a>
                  <br />
                  <a href="http://www.ph.ed.ac.uk" class="headertext"><img
                    src="{$context-path}/includes/arrow.gif"
                    width="16" height="11" border="0"
                    alt="" />School of Physics and Astronomy Homepage</a>
                  <br />
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <h1 id="location">
        <a href="{$context-path}">SnuggleTeX (v1.0.0-beta5)</a>
      </h1>
      <div id="content">
        <div id="skipnavigation">
          <a href="#maincontent">Skip Navigation</a>
        </div>
        <div id="navigation">
          <div id="navinner">
            <!-- Standard Navigation -->
            <h2>About SnuggleTeX</h2>
            <ul>
              <li><a class="overview" href="{$context-path}/">Overview</a></li>
              <li><a class="features" href="{$context-path}/features.html">Features</a></li>
              <li><a class="usecases" href="{$context-path}/use-cases.html">Use Cases</a></li>
              <li><a class="releasenotes" href="{$context-path}/release-notes.html">Release Notes</a></li>
              <li><a href="http://sourceforge.net/project/showfiles.php?group_id=221375">Download from SourceForge.net</a></li>
            </ul>

            <h2>User Guide</h2>
            <ul>
              <li><a class="requirements" href="{$context-path}/requirements.html">Software Requirements</a></li>
              <li><a class="gettingstarted" href="{$context-path}/getting-started.html">Getting Started</a></li>
              <li><a class="minexample" href="{$context-path}/minimal-example.html">Minimal Example</a></li>
              <li><a class="usageoverview" href="{$context-path}/usage-overview.html">Usage Overview</a></li>
              <li><a class="inputs" href="{$context-path}/inputs.html">Inputs</a></li>
              <li><a class="outputs" href="{$context-path}/outputs.html">Outputs</a></li>
              <li><a class="errors" href="{$context-path}/error-reporting.html">Error Reporting</a></li>
              <li><a class="configuration" href="{$context-path}/configuration.html">Configuration</a></li>
              <li><a class="projects" href="{$context-path}/javadoc/">API Documentation</a></li>
            </ul>

            <h2>Demos and Samples</h2>
            <ul>
              <li><a class="samples" href="{$context-path}/latex-samples.html">LaTeX Samples</a></li>
              <li><a class="contacts" href="/elearning/contacts/">Try Out</a></li>
            </ul>

            <h2>External Links</h2>
            <ul>
              <li><a href="http://sourceforge.net/projects/snuggletex/">SnuggleTeX on SourceForge.net</a></li>
              <li><a href="https://www.wiki.ed.ac.uk/display/Physics/SnuggleTeX">SnuggleTeX Wiki</a></li>
            </ul>
          </div>
        </div>
        <div id="maincontent">
          <div id="maininner">
            <xsl:apply-templates/>
          </div>
        </div>
      </div>
      <div id="copyright">
        <p>
          SnuggleTeX Release 1.0.0-beta5 &#x2014;
          <a href="{$context-path}/release-notes.html">Release Notes</a>
          <br />
          Copyright &#xa9; 2008
          <a href="http://www.ph.ed.ac.uk">The School of Physics and Astronomy</a>,
          <a href="http://www.ed.ac.uk">The University of Edinburgh</a>.
          <br />
          For more information, contact
          <a href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">David McKain</a>.
        </p>
        <p>
          The University of Edinburgh is a charitable body, registered in Scotland,
          with registration number SC005336.
        </p>
      </div>
    </body>
  </xsl:template>

  <xsl:template match="html:*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
