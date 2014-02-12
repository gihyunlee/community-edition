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
 * This widget is used for obtaining optimum code coverage results. It places a dependency
 * on all the modules in the package.
 *
 * @module alfresco/testing/UnitTestService
 * @extends module:alfresco/core/Core
 * @mixes module:alfresco/core/CoreXhr
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "alfresco/navigation/PathTree",
         "alfresco/navigation/TreeStore",
         "alfresco/navigation/CategoryTree",
         "alfresco/navigation/Link",
         "alfresco/navigation/Links",
         "alfresco/navigation/Tree",
         "alfresco/header/AlfCascadingMenu",
         "alfresco/header/LicenseWarning",
         "alfresco/header/Title",
         "alfresco/header/CurrentUserStatus",
         "alfresco/header/AlfMenuItem",
         "alfresco/header/AlfMenuBar",
         "alfresco/header/Header",
         "alfresco/header/SetTitle",
         "alfresco/header/AlfSitesMenu",
         "alfresco/header/SearchBox",
         "alfresco/header/AlfMenuBarPopup",
         "alfresco/header/UserStatus",
         "alfresco/services/RatingsService",
         "alfresco/services/LoggingService",
         "alfresco/services/_NavigationServiceTopicMixin",
         "alfresco/services/PreviewService",
         "alfresco/services/PageService",
         "alfresco/services/PreferenceService",
         "alfresco/services/ActionService",
         "alfresco/services/SiteService",
         "alfresco/services/ContentService",
         "alfresco/services/UserService",
         "alfresco/services/_PageServiceTopicMixin",
         "alfresco/services/_PreviewServiceTopicMixin",
         "alfresco/services/_PreferenceServiceTopicMixin",
         "alfresco/services/_TagServiceTopics",
         "alfresco/services/QuickShareService",
         "alfresco/services/DocumentService",
         "alfresco/services/TagService",
         "alfresco/services/NavigationService",
         "alfresco/services/_RatingsServiceTopicMixin",
         "alfresco/services/_QuickShareServiceTopicMixin",
         "alfresco/documentlibrary/AlfViewSelectionGroup",
         "alfresco/documentlibrary/AlfCreateContentMenuBarItem",
         "alfresco/documentlibrary/AlfSelectedItemsMenuBarPopup",
         "alfresco/documentlibrary/_AlfHashMixin",
         "alfresco/documentlibrary/AlfDocumentListPaginator",
         "alfresco/documentlibrary/_AlfDndDocumentUploadMixin",
         "alfresco/documentlibrary/AlfCreateContentMenuItem",
         "alfresco/documentlibrary/AlfDocument",
         "alfresco/documentlibrary/AlfCreateContentMenuBarPopup",
         "alfresco/documentlibrary/AlfBreadcrumbTrail",
         "alfresco/documentlibrary/AlfDocumentFilters",
         "alfresco/documentlibrary/AlfBreadcrumb",
         "alfresco/documentlibrary/AlfCloudSyncFilteredMenuBarItem",
         "alfresco/documentlibrary/AlfGalleryViewSlider",
         "alfresco/documentlibrary/_AlfCreateContentPermissionsMixin",
         "alfresco/documentlibrary/AlfDocumentFilter",
         "alfresco/documentlibrary/_AlfDocumentListTopicMixin",
         "alfresco/documentlibrary/AlfDocumentList",
         "alfresco/documentlibrary/AlfTagFilters",
         "alfresco/documentlibrary/AlfCreateTemplateContentMenu",
         "alfresco/documentlibrary/AlfSelectDocumentListItems",
         "alfresco/documentlibrary/views/AlfDocumentListView",
         "alfresco/documentlibrary/views/_AlfAdditionalViewControlMixin",
         "alfresco/documentlibrary/views/AlfGalleryView",
         "alfresco/documentlibrary/views/layouts/Column",
         "alfresco/documentlibrary/views/layouts/Popup",
         "alfresco/documentlibrary/views/layouts/Cell",
         "alfresco/documentlibrary/views/layouts/Table",
         "alfresco/documentlibrary/views/layouts/Row",
         "alfresco/documentlibrary/views/layouts/Grid",
         "alfresco/documentlibrary/views/layouts/_MultiItemRendererMixin",
         "alfresco/documentlibrary/views/AlfDetailedView",
         "alfresco/documentlibrary/views/AlfSimpleView",
         "alfresco/documentlibrary/AlfToolbar",
         "alfresco/documentlibrary/AlfResultsPerPageGroup",
         "alfresco/documentlibrary/AlfDocumentActionMenuItem",
         "alfresco/dialogs/AlfDialogService",
         "alfresco/dialogs/AlfDialog",
         "alfresco/dialogs/_AlfCreateFormDialogMixin",
         "alfresco/logo/Logo",
         "alfresco/forms/Form",
         "alfresco/forms/PublishForm",
         "alfresco/forms/controls/MultipleEntryFormControl",
         "alfresco/forms/controls/DojoValidationTextBox",
         "alfresco/forms/controls/AceEditor",
         "alfresco/forms/controls/MultipleEntryElementWrapper",
         "alfresco/forms/controls/MultipleKeyValuePairElement",
         "alfresco/forms/controls/DojoSelect",
         "alfresco/forms/controls/MultipleKeyValuePairFormControl",
         "alfresco/forms/controls/MultipleEntryCreator",
         "alfresco/forms/controls/BaseFormControl",
         "alfresco/forms/controls/RandomValueGenerator",
         "alfresco/forms/controls/DojoCheckBox",
         "alfresco/forms/controls/DropZoneControl",
         "alfresco/forms/controls/MultipleKeyValuePairCreator",
         "alfresco/forms/controls/DojoRadioButtons",
         "alfresco/forms/controls/MultipleEntryElement",
         "alfresco/forms/controls/DojoTextarea",
         "alfresco/forms/controls/JsonEditor",
         "alfresco/forms/creation/PublicationConfigControl",
         "alfresco/forms/creation/FormRulesConfigCreatorElement",
         "alfresco/forms/creation/PublicationConfigCreator",
         "alfresco/forms/creation/FormValidationConfigControl",
         "alfresco/forms/creation/FormRulesConfigCreator",
         "alfresco/forms/creation/PublicationConfigCreatorElement",
         "alfresco/forms/creation/FormRulesConfigControl",
         "alfresco/debug/CoreDataDebugger",
         "alfresco/users/SingleUserSelect",
         "alfresco/users/SingleUserSelectMenu",
         "alfresco/accessibility/AccessibilityMenu",
         "alfresco/wrapped/HeaderJsWrapper",
         "alfresco/menus/AlfVerticalMenuBar",
         "alfresco/menus/_AlfPopupCloseMixin",
         "alfresco/menus/AlfMenuBarSelectItems",
         "alfresco/menus/AlfMenuGroup",
         "alfresco/menus/AlfDropDownMenu",
         "alfresco/menus/_AlfDisplayFilterMixin",
         "alfresco/menus/AlfCascadingMenu",
         "alfresco/menus/AlfMenuBarToggle",
         "alfresco/menus/AlfDynamicMenuBar",
         "alfresco/menus/AlfMenuBarSelect",
         "alfresco/menus/AlfCheckableMenuItem",
         "alfresco/menus/AlfFilteringMenuItem",
         "alfresco/menus/AlfMenuItem",
         "alfresco/menus/AlfMenuBarItem",
         "alfresco/menus/AlfMenuBar",
         "alfresco/menus/_AlfMenuItemMixin",
         "alfresco/menus/AlfFormDialogMenuItem",
         "alfresco/menus/AlfMenuItemWrapper",
         "alfresco/menus/AlfMenuGroups",
         "alfresco/menus/AlfMenuBarPopup",
         "alfresco/menus/AlfMenuTextForClipboard",
         "alfresco/footer/AlfStickyFooter",
         "alfresco/node/DraggableNodeMixin",
         "alfresco/node/NodeDropTargetMixin",
         "alfresco/prototyping/Preview",
         "alfresco/html/Label",
         "alfresco/testing/RequireEverything",
         "alfresco/testing/TestCoverageResults",
         "alfresco/testing/SubscriptionLog",
         "alfresco/testing/UnitTestService",
         "alfresco/upload/AlfUploadDisplay",
         "alfresco/upload/AlfUpload",
         "alfresco/upload/AlfFileDrop",
         "alfresco/preview/Video",
         "alfresco/preview/Audio",
         "alfresco/preview/AlfDocumentPreview",
         "alfresco/preview/AlfDocumentPreviewPlugin",
         "alfresco/preview/StrobeMediaPlayback",
         "alfresco/preview/Flash",
         "alfresco/preview/Image",
         "alfresco/preview/WebPreviewer",
         "alfresco/buttons/AlfButton",
         "alfresco/core/FilteredPage",
         "alfresco/core/ObjectTypeUtils",
         "alfresco/core/Page",
         "alfresco/core/PubSubLog",
         "alfresco/core/WidgetsProcessingFilterMixin",
         "alfresco/core/DynamicWidgetProcessingTopics",
         "alfresco/core/TemporalUtils",
         "alfresco/core/NotificationUtils",
         "alfresco/core/UrlUtils",
         "alfresco/core/PathUtils",
         "alfresco/core/CoreXhr",
         "alfresco/core/DomElementUtils",
         "alfresco/core/DynamicWidgetProcessing",
         "alfresco/core/ProcessWidgets",
         "alfresco/core/XhrDependencyService",
         "alfresco/core/JsNode",
         "alfresco/core/NodeUtils",
         "alfresco/core/CoreData",
         "alfresco/core/Core",
         "alfresco/core/CoreRwd",
         "alfresco/pickers/Picker",
         "alfresco/pickers/SingleItemPicker",
         "alfresco/pickers/Explorer",
         "alfresco/renderers/Toggle",
         "alfresco/renderers/Favourite",
         "alfresco/renderers/_ItemLinkMixin",
         "alfresco/renderers/Category",
         "alfresco/renderers/ReadOnlyTag",
         "alfresco/renderers/Date",
         "alfresco/renderers/Size",
         "alfresco/renderers/Thumbnail",
         "alfresco/renderers/Indicators",
         "alfresco/renderers/InlineEditProperty",
         "alfresco/renderers/EditTag",
         "alfresco/renderers/Selector",
         "alfresco/renderers/Separator",
         "alfresco/renderers/MoreInfo",
         "alfresco/renderers/Tags",
         "alfresco/renderers/QuickShare",
         "alfresco/renderers/_JsNodeMixin",
         "alfresco/renderers/GalleryThumbnail",
         "alfresco/renderers/Banner",
         "alfresco/renderers/Like",
         "alfresco/renderers/FileType",
         "alfresco/renderers/Property",
         "alfresco/renderers/SmallThumbnail",
         "alfresco/renderers/Version",
         "alfresco/renderers/Comments",
         "alfresco/renderers/LockedBanner",
         "alfresco/renderers/Actions",
         "alfresco/layout/AlfSideBarContainer",
         "alfresco/layout/TitleDescriptionAndContent",
         "alfresco/layout/SlidingTabs",
         "alfresco/layout/HorizontalWidgets",
         "alfresco/layout/SlideOverlay",
         "alfresco/layout/AlfAccordionContainer",
         "alfresco/layout/ClassicWindow",
         "alfresco/layout/LeftAndRight",
         "alfresco/layout/VerticalWidgets",
         "alfresco/creation/DropAndPreview",
         "alfresco/creation/DragWidgetPalette",
         "alfresco/creation/WidgetDragWrapper",
         "alfresco/creation/DragPalette",
         "alfresco/creation/DropZone",
         "alfresco/creation/PublicationDropZone",
         "alfresco/creation/WidgetConfig",
         "alfresco/creation/DropZoneWrapper"],
        function(declare, AlfCore, CoreXhr, lang, AlfConstants) {
   
   return declare(null, {});
});