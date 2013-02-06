define(["alfresco/forms/controls/BaseFormControl",
        "dojo/_base/declare",
        "dijit/_WidgetBase", 
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/RadioButton.html",
        "dojo/text!./templates/RadioButtons.html",
        "alfresco/core/Core",
        "dijit/form/RadioButton",
        "dojo/_base/array",
        "dojo/dom-construct"], 
        function(BaseFormControl, declare, _Widget, _Templated, RadioButtonTemplate, RadioButtonsTemplate, AlfCore, DojoRadioButton, array, domConstruct) {
   
   var RadioButton = declare([_Widget, _Templated, AlfCore], {
      cssRequirements: [{cssFile:"./css/RadioButton.css"}],
      templateString: RadioButtonTemplate,
      _radioButton: null,
      postCreate: function() {
         this._radioButton = new DojoRadioButton({name: this.name, value: this.value});
         this._radioButton.placeAt(this._radioButtonNode);
         this._labelNode.innerHTML = this.message(this.label);
      }
   });

   var RadioButtons = declare([_Widget, _Templated, AlfCore], {
      
      cssRequirements: [{cssFile:"./css/RadioButtons.css"}],
      templateString: RadioButtonsTemplate,
      options: null,
      optionToWidget: null,
      control: null,
      currentValue: null,
      lastValue: null,
      postCreate: function() {
         // Create an object to map each option value to the RadioButton that represents it...
         this.optionToWidget = {};
         if (this.options != null && this.options instanceof Array)
         {
            array.forEach(this.options, function(option, index) {
               if (typeof option.label == "string" && typeof option.value == "string")
               {
                  this.addOption(option);
               }
               else
               {
                  this.alfLog("log", "An option provided for a RadioButton was either missing a value or label", option);
               }
            }, this);
         }
      },
      addOption: function(option) {
         // Create and add a new RadioButton and record a reference to it...
         option.name = this.name; // Add the name to create the radio button "group" (TODO: Is this necessary for our purpose?)
         var rb = new RadioButton(option);
         rb.placeAt(this.containerNode);
         this.options.push(option);
         this.optionToWidget[option.value] = rb;
         
         var _this = this; 
         var watchHandle = rb._radioButton.on("change", function(isChecked) {
            if (isChecked)
            {
               _this.currentValue = this.value;
               _this.control.formControlValueChange(name, _this.lastValue, _this.currentValue);
               _this.control.validate();
            }
            else
            {
               _this.lastValue = this.value;
            }
         }, true);
      },
      removeOption: function(option) {
         if (typeof option.value == "string")
         {
            // Destroy the widget...
            var rb = this.optionToWidget[option.value];
            // TODO: Need to connect various events - such as when the radiobutton is selected
            //       We will need to capture the overall value of the widget.
            rb.destroy();
         }
      },
      getValue: function() {
         for (var key in this.optionToWidget) {
            var selected =  this.optionToWidget[key]._radioButton.get("checked");
            if (selected)
            {
               return this.optionToWidget[key]._radioButton.getValue();
            }
         }
      }
   });
   
   return declare([BaseFormControl], {
      
      getWidgetConfig: function() {
         // Return the configuration for the widget
         return {
            id : this.generateUuid(),
            name: this.name,
            value: this.value,
            options: (this.options != null) ? this.options : [],
            control: this
         };
      },
      
      createFormControl: function(config) {
         // Create the inner class that we've defined above. The reason for doing this is that it
         // provides the standard widget API that the BaseFormControl is expecting...
         return new RadioButtons(config);
      }
   });
});