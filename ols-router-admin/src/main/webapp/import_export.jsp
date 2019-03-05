<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ include file="header.jsp"%> 
      <script type="text/javascript">
         <!--
            function getConfirmation(){
               var retVal = confirm("Do you want to continue ?");
               if( retVal == true ){
                  document.write ("User wants to continue!");
                  return true;
               }
               else{
                  document.write ("User does not want to continue!");
                  return false;
               }
            }
         //-->
      </script>
<div class="bodyContent">
<h1>Export Configuration</h1>
<form name="export_form" action="export" method="get">
<p><input name="export" type="submit" value="Download Export"/></p>
</form>
<br/><hr/><br/>
<h1>Validate Configuration</h1>
<form name="validate_form" action="validate" enctype="multipart/form-data" method="post">
<p>
<input type="file" name="file" required />
<input name="import" type="submit" value="Validate"/>
</p>
</form>
<br/><hr/><br/>
<h1>Import Configuration</h1>
<form name="import_form" action="import" enctype="multipart/form-data" method="post"
onsubmit="return confirm('Are you sure you want to import this configuration, overwriting your existing configuration?');">
<p>
<input type="file" name="file" required />
<input name="import" type="submit" value="Import"/>
</p>
</form>
</div>

<%@ include file="footer.jsp" %>
