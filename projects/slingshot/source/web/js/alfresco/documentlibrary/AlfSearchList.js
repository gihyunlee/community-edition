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
 * Extends the default [AlfDocumentList]{@link module:alfresco/documentlibrary/AlfDocumentList} to 
 * make search specific requests.
 * 
 * @module alfresco/documentlibrary/AlfSearchList
 * @extends alfresco/documentlibrary/AlfDocumentList
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "alfresco/documentlibrary/AlfDocumentList", 
        "alfresco/core/PathUtils",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/dom-construct",
        "dojo/dom-class"], 
        function(declare, AlfDocumentList, PathUtils, array, lang, domConstruct, domClass) {
   
   return declare([AlfDocumentList], {
      

      /**
       * Subscribe the document list topics.
       * 
       * @instance
       */
      postMixInProperties: function alfresco_documentlibrary_AlfDocumentList__postMixInProperties() {
         // this.alfPublish(this.getPreferenceTopic, {
         //    preference: "org.alfresco.share.documentList.documentsPerPage",
         //    callback: this.setPageSize,
         //    callbackScope: this
         // });

         // Only subscribe to filter changes if 'useHash' is set to true. This is because multiple DocLists might
         // be required on the same page and they can't all feed off the hash to drive the location.
         // if (this.useHash)
         // {
         //    this.alfSubscribe(this.filterChangeTopic, lang.hitch(this, "onChangeFilter"));
         // }
         
         // this.alfSubscribe(this.viewSelectionTopic, lang.hitch(this, "onViewSelected"));
         // this.alfSubscribe(this.documentSelectionTopic, lang.hitch(this, "onDocumentSelection"));
         // this.alfSubscribe(this.sortRequestTopic, lang.hitch(this, "onSortRequest"));
         // this.alfSubscribe(this.sortFieldSelectionTopic, lang.hitch(this, "onSortFieldSelection"));
         // this.alfSubscribe(this.showFoldersTopic, lang.hitch(this, "onShowFolders"));
         // this.alfSubscribe(this.pageSelectionTopic, lang.hitch(this, "onPageChange"));
         // this.alfSubscribe(this.docsPerpageSelectionTopic, lang.hitch(this, "onDocsPerPageChange"));
         // this.alfSubscribe(this.reloadDataTopic, lang.hitch(this, "loadData"));
         
         // Subscribe to the topics that will be published on by the DocumentService when retrieving documents
         // that this widget requests...
         this.alfSubscribe("ALF_RETRIEVE_DOCUMENTS_REQUEST_SUCCESS", lang.hitch(this, "onSearchLoadSuccess"));
         this.alfSubscribe("ALF_RETRIEVE_DOCUMENTS_REQUEST_FAILURE", lang.hitch(this, "onDataLoadFailure"));
         // this.alfSubscribe("ALF_SEARCH_REQUEST_SUCCESS", lang.hitch(this, "onSearchLoadSuccess"));
         // this.alfSubscribe("ALF_SEARCH_REQUEST_FAILURE", lang.hitch(this, "onDataLoadFailure"));
         this.alfSubscribe("ALF_SEARCH_RESULT_CLICKED", lang.hitch(this, "onSearchResultClicked"));

         this.alfSubscribe("ALF_SET_SEARCH_TERM", lang.hitch(this, "onSearchTermRequest"));
         this.alfSubscribe("ALF_INCLUDE_FACET", lang.hitch(this, "onIncludeFacetRequest"));
         this.alfSubscribe("ALF_APPLY_FACET_FILTER", lang.hitch(this, "onApplyFacetFilter"));
         this.alfSubscribe("ALF_REMOVE_FACET_FILTER", lang.hitch(this, "onRemoveFacetFilter"));

         // Get the messages for the template...
         this.noViewSelectedMessage = this.message("doclist.no.view.message");
         this.noDataMessage = this.message("doclist.no.data.message");
         this.fetchingDataMessage = this.message("doclist.loading.data.message");
         this.renderingViewMessage = this.message("doclist.rendering.data.message");

         this.facetFilters = {};
      },
      
      searchTerm: "",

      /**
       * Updates the current search term
       *
       * @instance
       * @param {object} payload The details of the search term to set
       */
      onSearchTermRequest: function alfresco_documentlibrary_AlfSearchList__onSearchTermRequest(payload) {
         this.alfLog("log", "Setting search term", payload, this);
         var searchTerm = lang.getObject("term", false, payload);
         if (searchTerm == null)
         {
            this.alfLog("warn", "No term provided on request", payload, this);
         }
         else
         {
            this.searchTerm = searchTerm;
            this.loadData();
         }
      },

      /**
       * The facet fields to include in searches. This is updated by the onIncludeFacetRequest function.
       * 
       * @instance
       * @type {string}
       * @default ""
       */
      facetFields: "",

      /**
       * 
       * @instance
       * @param {object} payload The details of the facet to include
       */
      onIncludeFacetRequest: function alfresco_services_SearchService__onIncludeFacetRequest(payload) {
         this.alfLog("log", "Adding facet filter", payload, this);
         var qname = lang.getObject("qname", false, payload);
         if (qname == null)
         {
            this.alfLog("warn", "No qname provided when adding facet field", payload, this);
         }
         else
         {
            this.facetFields = (this.facetFields != "") ? this.facetFields + "," + qname : qname;
         }
      },

      facetFilters: null,

      onApplyFacetFilter: function alfresco_documentlibrary_AlfSearchList__onApplyFacetFilter(payload) {
         this.alfLog("log", "Filtering on facet", payload, this);
         var filter = lang.getObject("filter", false, payload);
         if (filter == null)
         {
            this.alfLog("warn", "No filter provided when filtering by facet", payload, this);
         }
         else
         {
            this.facetFilters[filter] = true;
            this.loadData();
         }
      },

      onRemoveFacetFilter: function alfresco_documentlibrary_AlfSearchList__onRemoveFacetFilter(payload) {
         this.alfLog("log", "Removing facet filter", payload, this);
         delete this.facetFilters[payload.filter];
         this.loadData();
      },

      loadData: function alfresco_documentlibrary_AlfDocumentList__loadData() {
         this.showLoadingMessage(); // Commented out because of timing issues...

         var filters = "";
         for (var key in this.facetFilters)
         {
            filters = filters + key.replace(/\.__/g, "") + ",";
         }
         filters = filters.substring(0, filters.length - 1);

         var searchPayload = {
            term: this.searchTerm,
            facetFields: this.facetFields,
            filters: filters
         };

         // Set a response topic that is scoped to this widget...
         searchPayload.alfResponseTopic = this.pubSubScope + "ALF_RETRIEVE_DOCUMENTS_REQUEST";

         this.alfPublish("ALF_SEARCH_REQUEST", searchPayload, true);
      },


      /**
       * Handles successful calls to get data from the repository.
       * 
       * @instance
       * @param {object} response The response object
       * @param {object} originalRequestConfig The configuration that was passed to the the [serviceXhr]{@link module:alfresco/core/CoreXhr#serviceXhr} function
       */
      onSearchLoadSuccess: function alfresco_documentlibrary_AlfDocumentList__onSearchLoadSuccess(payload) {
         this.alfLog("log", "Search Results Loaded", payload, this);
         
         this._currentData = payload.response;
         
         // TODO: This should probably be in the SearchService... but will leave here for now...
         var facets = lang.getObject("response.facets", false, payload);
         var filters = lang.getObject("requestConfig.query.filters", false, payload);
         if (facets != null)
         {
            for (var key in facets)
            {
               this.alfPublish("ALF_FACET_RESULTS_" + key, {
                  facetResults: facets[key],
                  activeFilters: filters
               });
            }
         }

         // Re-render the current view with the new data...
         var view = this.viewMap[this._currentlySelectedView];
         if (view != null)
         {
            this.showRenderingMessage();
            view.setData(this._currentData);
            view.renderView();
            this.showView(view);
            
            // Force a resize of the sidebar container to take the new height of the view into account...
            this.alfPublish("ALF_RESIZE_SIDEBAR", {});
         }
      },

      

      /**
       * @instance
       */
      onSearchResultClicked: function alfresco_documentlibrary_AlfDocumentList__onSearchResultClicked(payload) {
         this.alfLog("log", "Search Result Clicked");

         var isContainer = lang.getObject("item.node.isContainer", false, payload);
         if (isContainer == true)
         {
            this.currentPath = "/";
            this.nodeRef = lang.getObject("item.nodeRef", false, payload);
            this.rootNode = lang.getObject("item.nodeRef", false, payload);
            this.loadData();
         }
         else
         {
            this.alfPublish("ALF_NAVIGATE_TO_PAGE", payload);
         }
      }
   });
});