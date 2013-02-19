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
package org.alfresco.repo.content.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Provides access to transformer configuration and current performance data.
 * 
 * @author Alan Davis
 */
public interface TransformerConfig
{
    /**
     * Wild card mimetype and mimetype extension.
     */
    public static final String ANY = "*";
    
    /**
     * Prefix before the transformer name of all property names that contain transformer
     * information
     */
    static final String CONTENT = "content.";
    
    /**
     * Prefix for all transformer names
     */
    static final String TRANSFORMER = "transformer.";
    
    /**
     * The combined content and transformer name prefix of for all property names that contain
     * transformer information
     */
    static final String PREFIX = CONTENT+TRANSFORMER;
    
    /**
     * The 'transformer name' for system wide defaults for all transformers
     */
    static final String DEFAULT_TRANSFORMER = TRANSFORMER+"default";
    
    /**
     * Name given to the 'SUMMARY' dummy (does not exist) transformer that gathers data
     * from all root level transformations 
     */
    static final String SUMMARY_TRANSFORMER_NAME = "SUMMARY";
    
    /**
     * The separator between the transformer name and two mimetype extensions in a property name.
     */
    static final String MIMETYPES_SEPARATOR = ".mimetypes.";
    
    /**
     * The suffix to property names for supported and unsupported combinations.
     */
    static final String SUPPORTED = ".supported";
    
    /**
     * The suffix to property names for the priority.
     */
    static final String PRIORITY = ".priority";
    
    /**
     * The suffix to property names for the threshold count.
     */
    static final String THRESHOLD_COUNT = ".thresholdCount";
    
    /**
     * The suffix to property names for the error time.
     */
    static final String ERROR_TIME = ".errorTime";
    
    /**
     * The suffix to property names for the the average time. Only used in the initial setup of
     * TransformerData. This is a historical property used by the 'Transformation Server' to set
     * an effective priority.
     */
    static final String INITIAL_TIME = ".time";
    
    /**
     * The suffix to property names for the the average count. Only used in the initial setup of
     * TransformerData. This is a historical property used by the 'Transformation Server' to set
     * an effective priority.
     */
    static final String INITIAL_COUNT = ".count";
    
    /**
     * To support the historical concept of EXPLICIT transformers, all such transformers
     * are given a {@link PRIORITY_EXPLICIT} (5). By default transformers have a default of 10.
     * A value of 5 allows better transformers to be added later.
     */
    public int PRIORITY_EXPLICIT = 5;
    
    /**
     * Suffixes to property names used to define transformation limits 
     */
    static final Collection<String> LIMIT_SUFFIXES = Arrays.asList(new String [] {
            '.'+TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES,
            '.'+TransformationOptionLimits.OPT_TIMEOUT_MS,
            '.'+TransformationOptionLimits.OPT_MAX_PAGES,
            '.'+TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES,
            '.'+TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS,
            '.'+TransformationOptionLimits.OPT_PAGE_LIMIT
    });

    /**
     * No suffixes to property names used to define transformer settings. 
     */
    public static final Collection<String> NO_SUFFIXES = Collections.singletonList("");

    /**
     * Returns a transformer property value. 
     * @param name of the property.
     * @return a transformer property or {@code null} if not set.
     */
    String getProperty(String name);

    /**
     * Sets a transformer property value. This will be stored in the database but on an MBean
     * reset would be cleared.
     * 
     * @param propertyNameAndValue
     */
    void setProperty(String propertyNameAndValue);

    /**
     * Returns and creates if needed the {@link TransformerStatistics} object for the combination of
     * transformer, sourceMimetype and targetMimetype. When transformer is null this is the
     * system wide summary object for a combination of sourceMimetype and targetMimetype.
     * When both sourceMimetype and targetMimetype are null this is the transformer's summary
     * object. When all three parameters are null this is the system wide summary for all
     * transformers.
     * @param transformer the transformer for which data is being recorded.
     * @param sourceMimetype the source mimetype.
     * @param targetMimetype the source mimetype.
     * @return the requested {@link TransformerStatistics}.
     */
    public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype);

    /**
     * Returns the limits defined for the combination of transformer, sourceMimetype and targetMimetype.
     * When the transformer is null, this is a default value. When both sourceMimetype and targetMimetype
     * are null this is a default for the specified transformer.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @return the combined (takes into account defaults from higher levels) limits for the combination.
     */
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype, String targetMimetype);

    /**
     * Returns true if the supplied mimetype transformation pair is allowed by the list of supported
     * and unsupported transformations.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @param options not currently used
     */
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options);

    /**
     * Returns the priority of the specified transformer for the the combination of source and target mimetype.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @return the priority. To support the historical concept of EXPLICIT transformers, all such transformers
     *         are given a {@link PRIORITY_EXPLICIT} (5). By default transformers have a default of 10.
     */
    public int getPriority(ContentTransformer contentTransformerHelper,
            String sourceMimetype, String targetMimetype);

    /**
     * Returns the threshold of the transformer. It is only after this number of transformation attempts
     * that the average time is used.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @return the threshold.
     */

    public int getThresholdCount(ContentTransformer contentTransformerHelper, String sourceMimetype,
            String targetMimetype);
}