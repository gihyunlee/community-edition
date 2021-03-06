/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.util.List;

/**
 * DAO for Repositories.
 * @author britt
 */
public interface AVMStoreDAO
{
    /**
     * Save an AVMStore, never before saved.
     * @param store The AVMStore
     */
    public void save(AVMStore store);
    
    /**
     * Delete the given AVMStore.
     * @param store The AVMStore.
     */
    public void delete(AVMStore store);
    
    /**
     * Get all AVMStores.
     * @return A List of all the AVMStores.
     */
    public List<AVMStore> getAll();

    /**
     * Get an AVMStore by name.
     * @param name The name of the AVMStore.
     * @return The AVMStore or null if not found.
     */
    public AVMStore getByName(String name);
    
    /**
     * Get the AVM Store that has the given root as HEAD.
     * @param root The root to query.
     * @return The matching store or null.
     */
    public AVMStore getByRoot(AVMNode root);
    
    /**
     * Update the given AVMStore record.
     * @param rep The dirty AVMStore.
     */
    public void update(AVMStore rep);
    
    /**
     * Get A store by primary key.
     * @param id The primary key.
     * @return The store.
     */
    public AVMStore getByID(long id);
    
    /**
     * Invalidate the by name lookup cache.
     */
    public void invalidateCache();
}
