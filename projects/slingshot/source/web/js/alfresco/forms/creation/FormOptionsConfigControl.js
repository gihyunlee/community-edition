define(["alfresco/forms/controls/MultipleEntryFormControl",
        "dojo/_base/declare",
        "dojo/_base/array"], 
        function(MultipleEntryFormControl, declare, array) {
   
   return declare([MultipleEntryFormControl], {
      
//      getValue: function() {
//         
//         var values = this.inherited(arguments);
//         
//         var options = [];
//         array.forEach(values, function(value, index) {
//            options.push({label: value, value: value});
//         }); 
//         
//         var optionsConfig = { fixed: options};
//         return optionsConfig;
//      },
//      
//      setValue: function(value) {
//         // In this particular instance we are currently only supporting fixed options
//         // configuration for the form control, so even though the data that is returned
//         // from getValue() is the correct definition for rendering the form it is not
//         // quite correct for rendering the form definition.
//         var value = ((value) ? value.fixed : null);
//         this.alfLog("log", "Setting OPTIONS: ", value);
//         if (this.wrappedWidget)
//         {
//            this.wrappedWidget.setValue(value);
//         }
//      }
   });
});