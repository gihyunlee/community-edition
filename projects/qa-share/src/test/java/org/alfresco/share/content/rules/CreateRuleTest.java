package org.alfresco.share.content.rules;


import org.alfresco.po.share.RepositoryPage;
import org.alfresco.po.share.enums.UserRole;
import org.alfresco.po.share.site.contentrule.FolderRulesPage;
import org.alfresco.po.share.site.contentrule.FolderRulesPageWithRules;
import org.alfresco.po.share.site.contentrule.createrules.CreateRulePage;
import org.alfresco.po.share.site.contentrule.createrules.selectors.AbstractIfSelector;
import org.alfresco.po.share.site.contentrule.createrules.selectors.impl.ActionSelectorEnterpImpl;
import org.alfresco.po.share.site.contentrule.createrules.selectors.impl.IfErrorEnterpImpl;
import org.alfresco.po.share.site.contentrule.createrules.selectors.impl.WhenSelectorImpl;
import org.alfresco.po.share.site.document.*;
import org.alfresco.share.util.*;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.webdrone.testng.listener.FailedTestListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Listeners(FailedTestListener.class)
public class CreateRuleTest extends AbstractUtils {

    private static Log logger = LogFactory.getLog(CreateRuleTest.class);

    protected String testUser;
    protected String siteName = "";

    /**
     * Class includes: Tests from TestLink in Area: Site Document Library Tests
     * <ul>
     * <li>Perform an Activity on Library</li>
     * <li>Site Document library shows documents</li>
     * </ul>
     */
    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.setup();
        testName = this.getClass().getSimpleName();

        logger.info("[Suite ] : Start Tests in: " + testName);
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14018() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Classifiable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14018() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Classifiable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.CLASSIFIABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("Categories")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Classifiable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.CLASSIFIABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Categories")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Classifiable'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.CLASSIFIABLE));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14019() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Effectivity
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14019() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Effectivity aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.EFFECTIVITY);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify Effectivity is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("EffectiveFrom")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Effectivity' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.EFFECTIVITY.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Effective From")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Effectivity'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.EFFECTIVITY));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14020() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Templatable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14020() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Templatable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.TEMPLATABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Templatable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.TEMPLATABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Templatable'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.TEMPLATABLE));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14021() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Emailed
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14021() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Emailed aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.EMAILED);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("Subject")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Emailed' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.EMAILED.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Subject")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Emailed" aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.EMAILED));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14022() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Email Alias
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14022() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String aspect_rule = "Email Alias";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Aliasable (Email) aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.ALIASABLE_EMAIL);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Aliasable (Email)' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(aspect_rule);

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Aliasable (Email)'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.ALIASABLE_EMAIL));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14023() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Summarizable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14023() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Summarizable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.SUMMARIZABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("Summary")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Summarizable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.SUMMARIZABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Summary")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Classifiable'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.SUMMARIZABLE));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14024() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Dublin Core
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14024() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Dublin Core aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.DUBLIN_CORE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("Publisher")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Dublin Core' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.DUBLIN_CORE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Publisher")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Dublin Core'' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.DUBLIN_CORE));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14025() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Complianceable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14025() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Complianceable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.COMPLIANCEABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("RemoveAfter")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Complianceable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.COMPLIANCEABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Remove After")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Complianceable" aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.COMPLIANCEABLE));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14026() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Versionable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14026() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Versionable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.VERSIONABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Versionable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.VERSIONABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Versionable" aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.VERSIONABLE));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14028() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect -Inline Editable
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14028() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Inline Editable aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.INLINE_EDITABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Inline Editable' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.INLINE_EDITABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Inline Editable" aspect has been removed
        Assert.assertTrue(aspectsPage.getAvailableAspects().contains(DocumentAspect.INLINE_EDITABLE));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14029() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Taggeble
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14029() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Taggeble aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.TAGGABLE);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Taggeble' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.TAGGABLE.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Taggeble" aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.TAGGABLE));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14030() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-EXIF
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14030() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // EXIF aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.EXIF);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("ImageWidth")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'EXIF' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.EXIF.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Image Width")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "EXIF' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.EXIF));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14031() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Remove Aspect-Geographic
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14031() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName1 = getFileName(testName) + ".txt";
        String[] moveFolderPath = {folderName};

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);
        ShareUser.openDocumentDetailPage(drone, fileName1);

        // Geographic aspect is added
        List<DocumentAspect> aspects = Arrays.asList(DocumentAspect.GEOGRAPHIC);
        DocumentDetailsPage detailsPage = (DocumentDetailsPage) ShareUser.addAspects(drone, aspects);

        // Open document details page and verify category is present in document's metadata
        Map<String, Object> properties = detailsPage.getProperties();
        Assert.assertTrue("(None)".equalsIgnoreCase((String) properties.get("Latitude")));

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Latitude' from drop-down select control
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.GEOGRAPHIC.getValue());

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.openDocumentLibrary(drone);
        // Move the document to the folder
        docLibPage = ShareUserSitePage.copyOrMoveToFolder(drone, siteName, fileName1, moveFolderPath, false);

        // Navigating to movefolder
        docLibPage.selectFolder(folderName);

        // Verifying that file is moved successfully.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        // View 'Manage Aspects' page for the added item
        properties = detailsPage.getProperties();
        Assert.assertFalse(properties.containsValue(("Latitude")));

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "GEOGRAPHIC' aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.GEOGRAPHIC));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_14034() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Specialize Type
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14034() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String subFolderName = getFolderName(testName) + "_sub";

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Rule Name");
        createRulePage.fillDescriptionField("Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select 'Specialize Type' action
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectSpecialiseType("File Transfer Target");

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        // Execute the rule. Create any subfolder in the folder
        ShareUserSitePage.createFolder(drone, subFolderName, null);

        FolderDetailsPage folderDetailsPage = docLibPage.getFileDirectoryInfo(subFolderName).selectViewFolderDetails().render();

        //The type of the subfolder is File Transfer Target
        Assert.assertTrue(folderDetailsPage.getProperties().containsKey("EndpointHost"));

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_15271() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Create combined rule - copy action
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_15271() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName) + "First";
        String folderName2 = getFolderName(testName) + "Second";
        String fileName1 = getFileName(testName) + ".txt";
        String newCopyName1 = getFileName(testName) + "_newName.txt";
        String newOriginalTitle1 = getFileName(testName) + "_newTitle.txt";
        String newOriginalDescription1 = getFileName(testName) + "_newDescription.txt";

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Any site is created, e.g. test-site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Any two folders are created in the site, e.g. folder1 and folder2
        ShareUserSitePage.createFolder(drone, folderName, null);
        ShareUserSitePage.createFolder(drone, folderName2, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Rule Name");
        createRulePage.fillDescriptionField("Rule Description");

        // Select several values in the When section, e.g.
        // 'Items are created or enter this folder' and 'Items are deleted or leave this folder'.
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();
        createRulePage.addOrRemoveOptionsFieldsToBlock(CreateRulePage.Block.WHEN_BLOCK, CreateRulePage.AddRemoveAction.ADD);
        whenSelectorIml.selectOutbound();

        //Select 'Copy' value in the 'Perform Action' section.
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectCopy(siteName, folderName2);

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        contentDetails.setContent(fileName1);
        docLibPage = ShareUser.createContentInCurrentFolder(drone, contentDetails, ContentType.PLAINTEXT, docLibPage);

        docLibPage.selectFolder(folderName2);

        //Verify the copy presence in the destination directory, e.g. in folder2
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        //Edit the copy, e.g. change the name
        docLibPage = ShareUser.editProperties(drone, fileName1, newCopyName1, null, null);
        Assert.assertTrue(docLibPage.isFileVisible(newCopyName1));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        //Edit the original, e.g. specify title and description.
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        //Edit the original, e.g. specify title and description.
        docLibPage = ShareUser.editProperties(drone, fileName1, null, newOriginalTitle1, newOriginalDescription1);
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));
        Assert.assertEquals(docLibPage.getFileDirectoryInfo(fileName1).getTitle(), "(" + newOriginalTitle1 + ")");
        Assert.assertEquals(docLibPage.getFileDirectoryInfo(fileName1).getDescription(), newOriginalDescription1);

        //Remove the original document
        ShareUser.selectContentCheckBox(drone, fileName1);
        docLibPage = ShareUser.deleteSelectedContent(drone);
        Assert.assertFalse(docLibPage.isFileVisible(fileName1));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName2);

        // Verify the copy in the destination folder, e.g. folder2
        Assert.assertFalse(docLibPage.isFileVisible(newCopyName1));
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));
        Assert.assertEquals(docLibPage.getFileDirectoryInfo(fileName1).getTitle(), "(" + newOriginalTitle1 + ")");
        Assert.assertEquals(docLibPage.getFileDirectoryInfo(fileName1).getDescription(), newOriginalDescription1);
    }

    //If error Execute script
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_14017() throws Exception {

        // upload .js file to Data Dictionary/Web Scripts Extensions
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String fileName = getFileName(testName) + ".txt";
        String scriptName = "script" + System.currentTimeMillis() + ".js";
        String fileNameForScript = "executed_script_" + System.currentTimeMillis() + ".txt";
        String scriptContent = "var doc = userhome.createFile(\"" + fileNameForScript + "\");";
        File jsFile = SiteUtil.newFile(scriptName, scriptContent).getAbsoluteFile();

        // Login
        ShareUser.login(drone, ADMIN_USERNAME, ADMIN_PASSWORD);

        RepositoryPage repositoryPage;

        String folderPath = REPO + SLASH + "Data Dictionary" + SLASH + "Scripts";
        ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);
        repositoryPage = ShareUserRepositoryPage.uploadFileInRepository(drone, jsFile);
        Assert.assertTrue(repositoryPage.isFileVisible(jsFile.getName()));


        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

        // Creating folder
        ShareUserSitePage.createFolder(drone, folderName, null);

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Rule Name");
        createRulePage.fillDescriptionField("Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectUpdate();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select 'Specialize Type' action
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectMove(siteName, "Documents");

        createRulePage.selectRunRuleInBackgroundCheckbox();

        IfErrorEnterpImpl ifErrorEnterpImpl = createRulePage.getIfErrorObj();
        ifErrorEnterpImpl.selectScript(scriptName);

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage = ShareUserSitePage.navigateToFolder(drone, folderName);

        // Create File
        contentDetails = new ContentDetails();
        contentDetails.setName(fileName);
        ShareUser.createContentInCurrentFolder(drone, contentDetails, ContentType.PLAINTEXT, docLibPage);

        ShareUserSitePage.navigateToFolder(drone, folderName);
        docLibPage = ShareUser.editProperties(drone, fileName, fileName);
        Assert.assertTrue(docLibPage.isFileVisible(fileName));

        ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO);

        Assert.assertTrue(docLibPage.isFileVisible(fileNameForScript));
        jsFile.delete();
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_8483() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Deleting a folder with Rule set
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_8483() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String folderName = getFolderName(testName) + System.currentTimeMillis();
        String subFolderName = getFolderName(testName) + System.currentTimeMillis() + "_sub";
        String Title = "Manage Permissions";
        String GroupName = "EVERYONE";

        // Login
        ShareUser.login(drone, ADMIN_USERNAME, ADMIN_PASSWORD);

        RepositoryPage repositoryPage;

        // go to Repository Browser,
        String folderPath = REPO;
        ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        // create Folder1 and sub folder Folder2 inside Folder1
        repositoryPage = ShareUserRepositoryPage.createFolderInRepository(drone, folderName, folderName, folderName);

        Assert.assertTrue(repositoryPage.isFileVisible(folderName));
        repositoryPage.selectFolder(folderName);

        ShareUserRepositoryPage.createFolderInRepository(drone, subFolderName, subFolderName, subFolderName);

        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        // verify created folders are present in the main repository
        Assert.assertTrue(repositoryPage.isFileVisible(folderName), "verifying folder present in repository");

        // Set inherit permissions to false on Folder1. Set Coordinator permission for "Everyone" in Folder
        // Select more options in folder1 and click on Manage permissions
        ManagePermissionsPage managePermissionsPage = repositoryPage.getFileDirectoryInfo(folderName).selectManagePermission().render();

        // Verify Manage Permissions page is displayed
        Assert.assertTrue(managePermissionsPage.render().isTitlePresent(Title), "Verify manage permissions page is displayed");

        // Verify Inherit permissions options in Manage Permissions page
        Assert.assertTrue(managePermissionsPage.isInheritPermissionEnabled());

        // Add group, save and return to repository page
        ShareUserMembers.addUserOrGroupIntoInheritedPermissions(drone, GroupName, false, UserRole.COORDINATOR, false);

        // Verify Inherit permissions options in Manage Permissions page
        Assert.assertFalse(managePermissionsPage.isInheritPermissionEnabled());

        Assert.assertEquals(UserRole.COORDINATOR, ShareUserMembers.getContentPermission(drone, folderName, GroupName));

        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        // create the rule for folder
        FolderRulesPage folderRulesPage = repositoryPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Rule Name");
        createRulePage.fillDescriptionField("Rule Description");

        // Select several values in the When section, e.g.
        // 'Items are created or enter this folder' and 'Items are deleted or leave this folder'.
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        //Select 'Copy' value in the 'Perform Action' section.
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectAddAspect(DocumentAspect.VERSIONABLE.getValue());

        createRulePage.addOrRemoveOptionsFieldsToBlock(CreateRulePage.Block.ACTION_BLOCK, CreateRulePage.AddRemoveAction.ADD);
        actionSelectorEnterpImpl.selectAddAspect(DocumentAspect.CLASSIFIABLE.getValue());

        createRulePage.addOrRemoveOptionsFieldsToBlock(CreateRulePage.Block.ACTION_BLOCK, CreateRulePage.AddRemoveAction.ADD);
        actionSelectorEnterpImpl.selectAddAspect(DocumentAspect.TAGGABLE.getValue());

        // Rule applies to subfolders = true
        createRulePage.selectApplyToSubfolderCheckbox();

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        ShareUser.logout(drone);

        // Login as user "test" and try to delete Folder2
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        folderPath = REPO + SLASH + folderName;
        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        Assert.assertTrue(repositoryPage.isFileVisible(subFolderName));

        //Remove the original document
        ShareUser.selectContentCheckBox(drone, subFolderName);
        ShareUser.deleteSelectedContent(drone);

        // check the file is deleted
        Assert.assertFalse(repositoryPage.isFileVisible(subFolderName));

    }

    //Running rule script to move documents
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_8497() throws Exception {

        // upload .js file to Data Dictionary/Web Scripts Extensions
        String sourceFolder = "Source Folder_" + System.currentTimeMillis();
        String targetFolder = "TargetFolder_" + System.currentTimeMillis();
        String fileName = getFileName(testName) + System.currentTimeMillis() + ".txt";

        // Login
        ShareUser.login(drone, ADMIN_USERNAME, ADMIN_PASSWORD);

        RepositoryPage repositoryPage;

        // go to Repository Browser,
        String folderPath = REPO;
        ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        // create Folder1
        repositoryPage = ShareUserRepositoryPage.createFolderInRepository(drone, targetFolder, null, null);

        Assert.assertTrue(repositoryPage.isFileVisible(targetFolder));

        //create Folder2
        repositoryPage = ShareUserRepositoryPage.createFolderInRepository(drone, sourceFolder, null, null);

        Assert.assertTrue(repositoryPage.isFileVisible(targetFolder));

        //Add a test document to the "Source Folder"
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName);

        repositoryPage = ShareUserRepositoryPage.createContentInFolder(drone, contentDetails, ContentType.PLAINTEXT, sourceFolder);

        Assert.assertTrue(repositoryPage.isFileVisible(fileName));

        String nodeRef = repositoryPage.getFileDirectoryInfo(fileName).getNodeRef();

        String scriptName = "moveAndRefresh_" + System.currentTimeMillis() + ".js";
        String scriptContent = "var docNodeRef = \"" + nodeRef + "\";" + "\r\n" + "var doc = search.findNode( docNodeRef );" +
                "\r\n" + "var folder = companyhome.childByNamePath(\"" + targetFolder + "\");" + "\r\n" + "doc.move(folder);";
        File jsFile = SiteUtil.newFile(scriptName, scriptContent).getAbsoluteFile();

        //Get the noderef for the test document and modify the docNodeRef variable in the attached moveAndRefresh.js file
        folderPath = REPO + SLASH + "Data Dictionary" + SLASH + "Scripts";
        ShareUserRepositoryPage.navigateToFolderInRepository(drone, folderPath);

        //Add the moveAndRefresh.js file to the Data Diction/Scripts directory
        repositoryPage = ShareUserRepositoryPage.uploadFileInRepository(drone, jsFile);
        Assert.assertTrue(repositoryPage.isFileVisible(jsFile.getName()));

        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO);

        Assert.assertTrue(repositoryPage.isFileVisible(sourceFolder));

        // Start to create a rule on the "Source Folder"
        FolderRulesPage folderRulesPage = repositoryPage.getFileDirectoryInfo(sourceFolder).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(sourceFolder));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField("Rule Name");
        createRulePage.fillDescriptionField("Rule Description");

        // Select "Update" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectUpdate();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Execute Script - moveAndRefresh
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectExecuteScript(scriptName);

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(sourceFolder));

        // Navigate to the test file located in the "Source Folder" and edit metadata
        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + sourceFolder);

        DocumentDetailsPage detailsPage = repositoryPage.selectFile(fileName).render();
        EditDocumentPropertiesPage editDocumentProperties = detailsPage.selectEditProperties().render();
        editDocumentProperties.setName(fileName);
        editDocumentProperties.setDocumentTitle(fileName);
        editDocumentProperties.selectSave().render();

        //it is moved to Target folder
        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + targetFolder);
        Assert.assertTrue(repositoryPage.isFileVisible(fileName));

        repositoryPage = ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + sourceFolder);
        Assert.assertFalse(repositoryPage.isFileVisible(fileName));
        jsFile.delete();

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_ALF_8499() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    // Two corresponding inverse rules
    @Test(groups = {"EnterpriseOnly"})
    public void ALF_8499() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName + System.currentTimeMillis());
        String folderName = getFolderName(testName);
        String subFolderName = getFolderName(testName) + "_sub";
        String ruleName = "Rule Name First";
        String fileName1 = getFileName(testName) + ".txt";

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        // Creating folder
        docLibPage = ShareUserSitePage.createFolder(drone, folderName, null);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        // Create any subfolder in the folder
        ShareUserSitePage.createFolder(drone, subFolderName, null);

        docLibPage = ShareUser.openDocumentLibrary(drone);

        Assert.assertTrue(docLibPage.isFileVisible(folderName));

        // create the rule for folder
        FolderRulesPage folderRulesPage = docLibPage.getFileDirectoryInfo(folderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(folderName));

        // Fill "Name" field with correct data
        CreateRulePage createRulePage = folderRulesPage.openCreateRulePage().render();
        createRulePage.fillNameField(ruleName);
        createRulePage.fillDescriptionField("Rule Description First");

        // Select "Inbound" value from "When" drop-down select control
        WhenSelectorImpl whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        AbstractIfSelector ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Create a rule of adding versionable aspects to parent folder with 'Rule applied to subfolder' enabled
        ActionSelectorEnterpImpl actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectAddAspect(DocumentAspect.VERSIONABLE.getValue());

        // Rule applies to subfolders = true
        createRulePage.selectApplyToSubfolderCheckbox();

        // Click "Create" button
        FolderRulesPageWithRules folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(folderName));

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        Assert.assertTrue(docLibPage.isFileVisible(subFolderName));

        //3  Create a rule of removing versionable aspect to subfolder
        // create the rule for folder
        folderRulesPage = docLibPage.getFileDirectoryInfo(subFolderName).selectManageRules().render();
        Assert.assertTrue(folderRulesPage.isPageCorrect(subFolderName));

        // verify the message
        Assert.assertTrue(folderRulesPage.isInheritRulesMessageDisplayed());

        // Fill "Name" field with correct data
        createRulePage = folderRulesPage.openCreateRulePage().render();

        createRulePage.fillNameField("Remove Aspect Rule Name");
        createRulePage.fillDescriptionField("Remove Aspect Rule Description");

        // Select "Inbound" value from "When" drop-down select control
        whenSelectorIml = createRulePage.getWhenOptionObj();
        whenSelectorIml.selectInbound();

        // Select 'All items' from "If" drop-down select control
        ifSelector = createRulePage.getIfOptionObj();
        ifSelector.selectIFOption(0);

        // Select Remove an aspect" from "Perform Action" drop-down select control
        // Select 'Versionable' from drop-down select control
        actionSelectorEnterpImpl = createRulePage.getActionOptionsObj();
        actionSelectorEnterpImpl.selectRemoveAspect(DocumentAspect.VERSIONABLE.getValue());

        // Click "Create" button
        folderRulesPageWithRules = createRulePage.clickCreate().render();
        Assert.assertTrue(folderRulesPageWithRules.isPageCorrect(subFolderName));

        // The rule is created. Note: Subfolder rwill have two rules
        Assert.assertEquals(folderRulesPageWithRules.getInheritedRulesFolderName(ruleName), folderName);

        docLibPage = ShareUser.openDocumentLibrary(drone);

        // Navigating to folder
        docLibPage.selectFolder(folderName);

        Assert.assertTrue(docLibPage.isFileVisible(subFolderName));

        docLibPage.selectFolder(subFolderName);

        // Upload a file into the Subfolder.
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName1);
        contentDetails.setContent(fileName1);
        docLibPage = ShareUser.createContentInCurrentFolder(drone, contentDetails, ContentType.PLAINTEXT, docLibPage);

        docLibPage.selectFolder(folderName);
        docLibPage.selectFolder(subFolderName);

        //Verify the file presence in the destination directory, e.g. in sub folder
        Assert.assertTrue(docLibPage.isFileVisible(fileName1));

        DocumentDetailsPage detailsPage = ShareUser.openDocumentDetailPage(drone, fileName1);

        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();

        // "Versionable" aspect has been removed
        Assert.assertFalse(aspectsPage.getSelectedAspects().contains(DocumentAspect.VERSIONABLE));

    }

}
