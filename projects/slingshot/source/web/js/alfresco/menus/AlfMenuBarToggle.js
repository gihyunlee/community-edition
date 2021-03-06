/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * <p>This provides a basic on/off state toggle button that can be placed on a
 * [menu bar]{@link module:alfresco/menus/AlfMenuBar}. By default it simply renders
 * a switch with localized "On" and "Off" labels and doesn't actually perform any
 * action beyond rendering it's current state.</p>
 * <p>It can be configured with custom labels and/or icons to represent state. Ideally
 * each state should be given a publish topic and payload so that it can interact
 * with other widgets and perform a useful action. This is done by configuing the "onConfig"
 * and "offConfig" attributes.</p>
 * <p>Example configuration:</p>
 * <p><pre>{
 *    name: "alfresco/menus/AlfMenuBarToggle",
 *    config: {
 *       id: "MyToggle",
 *       checked: true,
 *       onConfig: {
 *          label: "Turned On",
 *          iconClass: "on-icon",
 *          publishTopic: "toggle_changed",
 *          publishPayload: {
 *             value: "ON"
 *          }
 *       },
 *       offConfig: {
 *          label: "Turned Off",
 *          iconClass: "off-icon",
 *          publishTopic: "toggle_changed",
 *          publishPayload: {
 *             value: "OFF"
 *          }
 *       }
 *    }
 * }</pre></p>
 *
 * @module alfresco/menus/AlfMenuBarToggle
 * @extends module:alfresco/menus/AlfMenuBarItem
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "alfresco/menus/AlfMenuBarItem",
        "dojo/dom-construct",
        "dojo/dom-class"], 
        function(declare, AlfMenuBarItem, domConstruct, domClass) {
   
   
   return declare([AlfMenuBarItem], {
      
      /**
       * An array of the CSS files to use with this widget.
       * 
       * @instance
       * @type {object[]}
       * @default [{cssFile:"./css/AlfMenuBarToggle.css"}]
       */
      cssRequirements: [{cssFile:"./css/AlfMenuBarToggle.css"}],
      
      /**
       * An array of the i18n files to use with this widget.
       * 
       * @instance
       * @type {object[]}
       * @default [{i18nFile: "./i18n/AlfMenuBarToggle.properties"}]
       */
      i18nRequirements: [{i18nFile: "./i18n/AlfMenuBarToggle.properties"}],
   
      /**
       * Indicates the on/off state. True is on, false is off.
       * 
       * @instance
       * @type {boolean}
       * @default false
       */
      checked: false,
      
      /**
       * The configuration representing the toggle in it's 'on' state
       *
       * @instance
       * @type {object} 
       * @default null
       */
      onConfig: null,
      
      /**
       * The configuration representing the toggle in it's 'off' state
       *
       * @instance
       * @type {object} 
       * @default null
       */
      offConfig: null,
      
      /**
       * This sets default values for the [onConfig]{@link module:alfresco/menus/AlfMenuBarToggle#onConfig}
       * and [offConfig]{@link module:alfresco/menus/AlfMenuBarToggle#offConfig} attributes if they 
       * have not been configured. The defaults are localised "On" and "Off" labels with no publication
       * data for either state.
       *
       * @instance
       */
      constructor: function alfresco_menus_AlfMenuBarToggle__constructor(args) {
         
         this.alfLog("log", "Create toggle", args);
         
         if (this.onConfig == null)
         {
            this.onConfig = 
            {
               label: "default.on.label"
            };
         }
         
         if (this.offConfig == null)
         {
            this.offConfig = {
               label: "default.off.label"
            }
         }
      },
      
      /**
       * Sets up the initial state of the widget based on the [onConfig]{@link module:alfresco/menus/AlfMenuBarToggle#onConfig}
       * and [offConfig]{@link module:alfresco/menus/AlfMenuBarToggle#offConfig} attributes. The
       * [renderToggle]{@link module:alfresco/menus/AlfMenuBarToggle#renderToggle} function is called
       * to render the starting state.
       *
       * @instance
       */
      postCreate: function alfresco_menus_AlfMenuBarToggle__postCreate() {
         // Set an iconClass attribute if either the off or on configuration
         // provides it (it doesn't matter what gets set initially as it will
         // be overridden by the call to renderToggle). However, what this does
         // is ensure that an iconNode is created if required (this will be
         // done in the call to inherited(arguments)...
         if (this.onConfig && this.onConfig.iconClass)
         {
            this.iconClass = this.onConfig.iconClass;
         }
         else if (this.offConfig && this.offConfig.iconClass)
         {
            this.iconClass = this.offConfig.iconClass;
         }
         this.inherited(arguments);
         if (this.checked)
         {
            this.renderToggle(this.onConfig, this.offConfig);
         }
         else
         {
            this.renderToggle(this.offConfig, this.onConfig);
         }
      },

      /**
       * This performs the rendering of the toggle when the widget is instantiated and 
       * when the state is changed. The labels and icons are updated to reflect the new
       * state.
       * 
       * @instance
       * @param {object} newConfig The new configuration to apply to the toggle button
       * @param {object} oldConfig The old configuration being removed from the toggle button.
       */
      renderToggle: function alfresco_menus_AlfMenuBarToggle__renderToggle(newConfig, oldConfig) {
         if (newConfig && newConfig.label)
         {
            this.label = newConfig.label;
            this.set('label', this.message(newConfig.label));
         }
         if (oldConfig && oldConfig.iconClass)
         {
            domClass.remove(this.iconNode, oldConfig.iconClass);
         }
         if (newConfig && newConfig.iconClass)
         {
            this.iconClass = newConfig.iconClass;
            domClass.add(this.iconNode, newConfig.iconClass);
         }
         
         // Clicking on the check cell will result in the menu item being marked as selected
         // but we want to ensure that this is not the case so always remove the class that 
         // indicates selection...
         domClass.remove(this.domNode, "dijitMenuItemSelected");
      },
      
      /**
       * This handles the user clicking on the toggle. The state is changed (e.g. from OFF on ON)
       * and any data associated with the new state is published on the configured topic. 
       *
       * @instance
       */
      onClick: function alfresco_menus_AlfMenuBarToggle__onClick() {
         this.alfLog("log", "Toggling!");
         this.checked = !this.checked;
         if (this.checked)
         {
            this.renderToggle(this.onConfig, this.offConfig);
            if (this.onConfig.publishTopic)
            {
               this.alfPublish(this.onConfig.publishTopic, this.onConfig.publishPayload);
            }
         }
         else
         {
            this.renderToggle(this.offConfig, this.onConfig);
            if (this.offConfig.publishTopic)
            {
               this.alfPublish(this.offConfig.publishTopic, this.offConfig.publishPayload);
            }
         }
      }
   });
});