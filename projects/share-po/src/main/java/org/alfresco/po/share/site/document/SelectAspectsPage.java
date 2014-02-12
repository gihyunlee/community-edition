/*
 * Copyright (C) 200ou5-2013 Alfresco Software Limited.
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
package org.alfresco.po.share.site.document;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.alfresco.webdrone.RenderElement.getVisibleRenderElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.alfresco.po.share.ShareLink;
import org.alfresco.po.share.SharePage;
import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.sun.jna.platform.unix.X11.XSizeHints.Aspect;

/**
 * Select Aspects page object, this page comes from Document Detail Page's Manage Aspects.
 *
 * @author Shan Nagarajan
 * @since  1.6.1
 */
public class SelectAspectsPage extends SharePage
{
    
    private static final By AVAILABLE_ASPECT_TABLE = By.cssSelector("div[id$='aspects-left']>table>tbody.yui-dt-data>tr");
    private static final By CURRENTLY_ADDED_ASPECT_TABLE = By.cssSelector("div[id$='aspects-right']>table>tbody.yui-dt-data>tr");
    private static final By HEADER_ASPECT_TABLE = By.cssSelector("td>div>h3");
    private static final By ADD_REMOVE_LINK = By.cssSelector("td>div>a");
    private static final By APPLY_CHANGE = By.cssSelector("button[id$='aspects-ok-button']");
    private static final By CANCEL = By.cssSelector("button[id$='aspects-cancel-button']");
    private static Log logger = LogFactory.getLog(SelectAspectsPage.class);
    private static final By TITLE  = By.cssSelector("div[id$='aspects-title']");

    /**
     * Constructor.
     */
    protected SelectAspectsPage(WebDrone drone)
    {
        super(drone);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SelectAspectsPage render(RenderTime timer)
    {
        elementRender(timer, getVisibleRenderElement(TITLE));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SelectAspectsPage render(long time)
    {
        return render(new RenderTime(time));
    }

    @SuppressWarnings("unchecked")
    @Override
    public SelectAspectsPage render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }
    
    /**
     * Add the {@link Aspect} if it is available to add.
     * 
     * @param aspects {@link List} of {@link Aspect} to added.
     * @return {@link DocumentDetailsPage}
     */
    public DocumentDetailsPage remove(List<DocumentAspect> aspects)
    {
        return addRemoveAspects(aspects, CURRENTLY_ADDED_ASPECT_TABLE);
    }
    
    /**
     * Add the {@link Aspect} if it is available to add.
     * 
     * @param aspects {@link List} of {@link Aspect} to added.
     * @return {@link DocumentDetailsPage}
     */
    public DocumentDetailsPage add(List<DocumentAspect> aspects)
    {
        return addRemoveAspects(aspects, AVAILABLE_ASPECT_TABLE);
    }
    
    /**
     * Add the {@link Aspect} if it is available to add.
     * 
     * @param aspects {@link List} of {@link Aspect} to added.
     * @return {@link DocumentDetailsPage}
     */
    private DocumentDetailsPage addRemoveAspects(List<DocumentAspect> aspects, By by)
    {
        if(aspects == null || aspects.isEmpty())
        {
           throw new UnsupportedOperationException("Aspets can't be empty or null."); 
        }
        List<WebElement> availableElements = null;
        Map<DocumentAspect, ShareLink> availableAspectMap = null;
        int aspectCount = 0;
        try
        {
            availableElements = drone.findAndWaitForElements(by);
        }
        catch (TimeoutException exception){ }
        
        if(availableElements != null && !availableElements.isEmpty())
        {
            //Convert List into Map
            availableAspectMap = new HashMap<DocumentAspect, ShareLink>();
            for (WebElement webElement : availableElements)
            {
                try
                {
                    WebElement header = webElement.findElement(HEADER_ASPECT_TABLE);
                    WebElement addLink = webElement.findElement(ADD_REMOVE_LINK);
                    ShareLink addShareLink = new ShareLink(addLink, drone);
                    availableAspectMap.put(DocumentAspect.getAspect(header.getText()), addShareLink);
                }
                catch (NoSuchElementException e)
                {
                    logger.error("Not able to find the header or link element on this row.");
                }
                catch (Exception e) 
                {
                	logger.error("Exception while finding & adding aspects : " + e.getMessage());
				}
            }
        }
        
        if(availableAspectMap != null && !availableAspectMap.isEmpty()) 
        {
            for (DocumentAspect aspect : aspects)
            {
                ShareLink link = availableAspectMap.get(aspect);
                if(link != null )
                {
                    try
                    {
                        link.click();
                        if(logger.isTraceEnabled())
                        {
                            logger.trace( aspect + "Aspect Added.");
                        }
                    }
                    catch (StaleElementReferenceException exception)
                    {
                        drone.find(CANCEL).click();
                        throw new PageException("Unexpected Refresh on Page lost reference to the Aspects." + exception);
                    }
                    aspectCount++;
                } 
                else
                {
                    logger.error("Not able to find in the available aspects bucket " + aspect.toString());
                }
            }
        }
        
        if(aspectCount == 0)
        {
            drone.find(CANCEL).click();
        }
        else
        {
            drone.find(APPLY_CHANGE).click();
            drone.waitForElement(By.cssSelector("div.bd>span.message"), SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
            drone.waitUntilNotVisible(By.cssSelector("div.bd>span.message"), "Successfully updated aspects", SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        }
            
        return new DocumentDetailsPage(drone);
    }
    
    @Override
    public String getTitle()
    {
        return drone.find(TITLE).getText();
    }

}