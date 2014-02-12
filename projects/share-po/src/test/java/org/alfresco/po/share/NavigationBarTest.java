/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.po.share;

import java.util.List;

import org.alfresco.po.share.search.AdvanceSearchContentPage;
import org.alfresco.po.share.site.CreateSitePage;
import org.alfresco.po.share.user.MyProfilePage;
import org.alfresco.po.share.util.FailedTestListener;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Alfresco Share navigation bar integration test.
 * 
 * @author Michael Suzuki
 * @since 1.0
 */
@Listeners(FailedTestListener.class)
public class NavigationBarTest extends AbstractTest
{
    private SharePage page;
    
    @BeforeClass(alwaysRun = true, groups={"alfresco-one"})
    public void setup() throws Exception
    {
        page = loginAs(username, password);
    }
    
    /**
     * Navigate to people finder from the dashboard page
     * and back to dash board page by selecting the 
     * navigation icons.
     * @throws Exception if error
     */
    @Test(groups={"alfresco-one"})
    public void navigateToPeopleFinder() throws Exception
    {
        PeopleFinderPage peoplePage = page.getNav().selectPeople().render();
        Assert.assertEquals("People Finder", peoplePage.getPageTitle());
    }
    
    /**
     * Test navigating to site finder page.
     * @throws Exception if error
     */
    @Test(dependsOnMethods = "navigateToPeopleFinder",groups={"alfresco-one"})
    public void navigateToSearchForSites() throws Exception
    {
        page = page.getNav().selectSearchForSites().render();
        Assert.assertEquals("Site Finder", page.getPageTitle());
    }
    
    /**
     * Test navigating to create site page.
     * @throws Exception if error
     */
    @Test(dependsOnMethods = "navigateToSearchForSites",groups={"alfresco-one"})
    public void navigateToCreateSite() throws Exception
    {
        CreateSitePage createSitePage = page.getNav().selectCreateSite().render();
        Assert.assertTrue(createSitePage.isCreateSiteDialogDisplayed());
        createSitePage.cancel();
    }
    
    /**
     * Test navigating to my profile page.
     * @throws Exception if error
     */
    @Test(dependsOnMethods= "navigateToCreateSite" ,groups={"alfresco-one"})
    public void navigateToMyProfile() throws Exception
    {
        MyProfilePage myProfilePage = page.getNav().selectMyProfile().render();
        Assert.assertTrue(myProfilePage.titlePresent());
    }
    
    /**
     * Test navigating to change password page.
     * @throws Exception if error
     */
    @Test(dependsOnMethods= "navigateToMyProfile",groups={"alfresco-one"})
    public void navigateChangePassword() throws Exception
    {
        ChangePasswordPage changePasswordPage = page.getNav().selectChangePassword().render();
        Assert.assertTrue(changePasswordPage.formPresent());
    }

    /**
     * Test navigating to change password page.
     * @throws Exception if error
     */
    @Test(dependsOnMethods= "navigateChangePassword",groups={"alfresco-one"})
    public void navigateDashBoard() throws Exception
    {
        DashBoardPage dash = page.getNav().selectMyDashBoard().render();
        Assert.assertTrue(dash.titlePresent());
        dash.getTitle().contains("Dashboard");
        Assert.assertTrue(dash.getTitle().contains("Dashboard"));
    }
    
    /**
     * Test repository link, note that this is for non cloud product.
     * @throws Exception if error
     */
    @Test(dependsOnMethods= "navigateDashBoard",groups = "Enterprise-only")
    public void navigateToRepository() throws Exception
    {
        if(alfrescoVersion.isCloud())
        {
            throw new SkipException("This feature is not supported in cloud so skip it");
        }
        page.getNav().selectRepository();
        RepositoryPage repoPage =  drone.getCurrentPage().render();
        Assert.assertTrue(repoPage.isBrowserTitle("Repository"));
    }
    
    /**
     * Test advance search link.
     * Note supported in cloud.
     * @throws Exception if error
     */
    @Test(dependsOnMethods= "navigateToRepository",groups= "Enterprise-only")
    public void advanceSearch() throws Exception
    {
        if(alfrescoVersion.isCloud())
        {
            throw new SkipException("This feature is not supported in cloud so skip it");
        }
        AdvanceSearchContentPage searchPage = page.getNav().selectAdvanceSearch().render();
        Assert.assertEquals("Advanced Search",searchPage.getPageTitle());
    }

    @Test(dependsOnMethods = "advanceSearch", groups = {"Enterprise-only"}, expectedExceptions = UnsupportedOperationException.class)
    public void testSelectNetworkDropdownInEnterprise() throws Exception
    {
        page.getNav().selectNetworkDropdown();
    }
    
    @Test(dependsOnMethods = "testSelectNetworkDropdownInEnterprise", groups = {"Enterprise-only"}, expectedExceptions = UnsupportedOperationException.class)
    public void testSelectNetworkInEnterprise() throws Exception
    {
        String strInvitedUser = username.substring(username.lastIndexOf("@") + 1, username.length());
       page.getNav().selectNetwork(strInvitedUser);
    }
    
    @Test(dependsOnMethods = "navigateDashBoard", groups = "Cloud-only")
    public void testNetworkDropdown()
    {
        Assert.assertNotNull(page.getNav().selectNetworkDropdown());
    }

    @Test(dependsOnMethods = "testNetworkDropdown", groups = "Cloud-only")
    public void testSelectNetwork()
    {
        page = drone.getCurrentPage().render();
        String strInvitedUser = username.substring(username.lastIndexOf("@") + 1, username.length());
    //    Assert.assertNotNull(page.getNav().selectNetworkDropdown());
        Assert.assertNotNull(page.getNav().selectNetwork(strInvitedUser).render());
    }

    @Test(dependsOnMethods = "testSelectNetwork", groups = "Cloud-only", expectedExceptions = IllegalArgumentException.class)
    public void testSelectNetworkWithNull()
    {
        page.getNav().selectNetwork(null);
    }

    @Test(dependsOnMethods = "testSelectNetworkWithNull", groups = "Cloud-only", expectedExceptions = IllegalArgumentException.class)
    public void testSelectNetworkWithEmpty()
    {
        page.getNav().selectNetwork("");
    }

    @Test(dependsOnMethods = "testSelectNetworkWithEmpty", groups = "Cloud-only")
    public void testgetNetworks()
    {
        List<String> userNetworks = page.getNav().getUserNetworks();
        Assert.assertTrue(userNetworks.size() > 0);
    }
 }