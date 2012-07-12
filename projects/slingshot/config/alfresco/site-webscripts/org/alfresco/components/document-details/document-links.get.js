<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      model.document = documentDetails.item;
      model.repositoryUrl = AlfrescoUtil.getRepositoryUrl();
   }
   
   // Widget instantiation metadata...
   model.widgets = [];
   var documentActions = {
      name : "Alfresco.DocumentLinks",
      options : {
         nodeRef : model.nodeRef,
         siteId : (model.site != null) ? model.site : null
      }
   };
   model.widgets.push(documentActions);
}

main();

