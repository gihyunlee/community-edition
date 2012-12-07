<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/images/task-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      <#if args.workflowPooled == true>
                                                         Nieuwe gepoolde taak
                                                      <#else>
                                                         Er is een taak aan u toegewezen
                                                      </#if>
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hi,</p>

                                             <p>
                                                <#if args.workflowPooled == true>
                                                   De volgende gepoolde taak is beschikbaar om te worden opgehaald:
                                                <#else>
                                                   De volgende taak is aan u toegewezen:
                                                </#if>
                                             </p>
                                             
                                             <p><b>"${args.workflowTitle}"</b></p>
                                             
                                             <#if (args.workflowDescription)??>                                             
                                             	<p>${args.workflowDescription}</p>                                             
                                             </#if>
                                             
                                             <p>
                                                <#if (args.workflowDueDate)??>Due:&nbsp;&nbsp;<b>${args.workflowDueDate?date?string.full}</b><br></#if>
                                                <#if (args.workflowPriority)??>
                                                   Prioriteit:&nbsp;&nbsp;
                                                   <b>
                                                   <#if args.workflowPriority == 3>
                                                      Laag
                                                   <#elseif args.workflowPriority == 2>
                                                      Gemiddeld
                                                   <#else>
                                                      Hoog
                                                   </#if>
                                                   </b>
                                                </#if>
                                             </p>
                                             
                                             <#if (args.workflowDocuments)??>
                                                <table cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
                                                   <#list args.workflowDocuments as doc>
                                                      <tr>
                                                         <td>
                                                            <table cellpadding="0" cellspacing="0" border="0">
                                                               <tr>
                                                                  <td valign="top">
                                                                     <img src="${shareUrl}/res/components/images/generic-file.png" alt="" width="64" height="64" border="0" style="padding-right: 10px;" />
                                                                  </td>
                                                                  <td>
                                                                     <table cellpadding="2" cellspacing="0" border="0">
                                                                        <tr>
                                                                           <td><b>${doc.name}</b></td>
                                                                        </tr>
                                                                        <tr>
                                                                           <td>Klik op deze koppeling om het document te downloaden:</td>
                                                                        </tr>
                                                                        <tr>
                                                                           <td>
                                                                              <a href="${shareUrl}/proxy/alfresco/api/node/content/workspace/SpacesStore/${doc.id}/${doc.name}?a=true">
                                                                              ${shareUrl}/proxy/alfresco/api/node/content/workspace/SpacesStore/${doc.id}/${doc.name}?a=true</a>
                                                                           </td>
                                                                        </tr>
                                                                     </table>
                                                                  </td>
                                                               </tr>
                                                            </table>
                                                         </td>
                                                      </tr>
                                                      <#if doc_has_next>
                                                         <tr><td><div style="border-top: 1px solid #aaaaaa; margin:12px;"></div></td></tr>
                                                      </#if>
                                                   </#list>
                                                </table>
                                             </#if>
                                             
                                             <#if args.workflowPooled == true>
                                                <p>Klik op deze koppeling om de taak te bekijken:</p>
                                                <p><a href="${shareUrl}/page/task-details?taskId=${args.workflowId}">${shareUrl}/page/task-details?taskId=${args.workflowId}</a>
                                             <#else>
                                                <p>Klik op deze koppeling om de taak te bewerken:</p>
                                                <p><a href="${shareUrl}/page/task-edit?taskId=${args.workflowId}">${shareUrl}/page/task-edit?taskId=${args.workflowId}</a>
                                             </#if>
                                             
                                             <p>Hoogachtend,<br />
                                             Alfresco ${productName!""}</p>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 Ga voor meer informatie over Alfresco ${productName!""} naar <a href="http://www.alfresco.com">http://www.alfresco.com</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>