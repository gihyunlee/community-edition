/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.search.adaptor.lucene;

/**
 * This class defines 
 *  1) all the non-property fields available to query
 *  2) all the extensions to properties
 * 
 * @author Andy
 *
 */
public interface QueryConstants
{   
    public static final String FIELD_NO_LOCALE_SUFFIX = ".no_locale";

    public static final String FIELD_SORT_SUFFIX = ".sort";

    public static final String FIELD_LOCALE_SUFFIX = ".locale";

    public static final String FIELD_SIZE_SUFFIX = ".size";

    public static final String FIELD_MIMETYPE_SUFFIX = ".mimetype";

    public static final String FIELD_FTSSTATUS = "FTSSTATUS";

    public static final String FIELD_FTSREF = "FTSREF";

    public static final String FIELD_ISNOTNULL = "ISNOTNULL";

    public static final String FIELD_ISNULL = "ISNULL";

    public static final String FIELD_ISUNSET = "ISUNSET";

    public static final String FIELD_ALL = "ALL";

    public static final String PROPERTY_FIELD_PREFIX = "@";

    public static final String FIELD_EXACTASPECT = "EXACTASPECT";

    public static final String FIELD_EXACTTYPE = "EXACTTYPE";

    public static final String FIELD_TYPE = "TYPE";

    public static final String FIELD_ASPECT = "ASPECT";

    public static final String FIELD_CLASS = "CLASS";

    public static final String FIELD_ASSOCTYPEQNAME = "ASSOCTYPEQNAME";

    public static final String FIELD_PRIMARYASSOCTYPEQNAME = "PRIMARYASSOCTYPEQNAME";

    public static final String FIELD_QNAME = "QNAME";

    public static final String FIELD_PRIMARYPARENT = "PRIMARYPARENT";

    public static final String FIELD_PARENT = "PARENT";

    /**
     * @deprecated This is basically unused - you want TXID
     */
    @Deprecated
    public static final String FIELD_TX = "TX";

    public static final String FIELD_ISNODE = "ISNODE";

    public static final String FIELD_ISCONTAINER = "ISCONTAINER";

    public static final String FIELD_ISROOT = "ISROOT";

    public static final String FIELD_DBID = "DBID";

    public static final String FIELD_ID = "ID";

    public static final String FIELD_TEXT = "TEXT";

    public static final String FIELD_PATHWITHREPEATS = "PATHWITHREPEATS";

    public static final String FIELD_PATH = "PATH";

    public static final String FIELD_TAG = "TAG";

    public static final String FIELD_ACLID = "ACLID";

    public static final String FIELD_OWNER = "OWNER";

    public static final String FIELD_READER = "READER";

    public static final String FIELD_AUTHORITY = "AUTHORITY";

    public static final String FIELD_OWNERSET = "OWNERSET";

    public static final String FIELD_READERSET = "READERSET";

    public static final String FIELD_AUTHORITYSET = "AUTHSET";

    public static final String FIELD_TXID = "TXID";

    public static final String FIELD_INTXID = "INTXID";

    public static final String FIELD_ACLTXID = "ACLTXID";

    public static final String FIELD_INACLTXID = "INACLTXID";

    public static final String FIELD_TXCOMMITTIME = "TXCOMMITTIME";

    public static final String FIELD_ACLTXCOMMITTIME = "ACLTXCOMMITTIME";

    public static final String FIELD_LINKASPECT = "LINKASPECT";

    public static final String FIELD_ANCESTOR = "ANCESTOR";

    public static final String FIELD_ISCATEGORY = "ISCATEGORY";

    public static final String FIELD_ENCODING_SUFFIX = ".encoding";

    public static final String FIELD_CONTENT_DOC_ID_SUFFIX = "contentDocId";

    public static final String FIELD_TRANSFORMATION_EXCEPTION_SUFFIX = ".transformationException";

    public static final String FIELD_TRANSFORMATION_TIME_SUFFIX = ".transformationTime";

    public static final String FIELD_TRANSFORMATION_STATUS_SUFFIX = ".transformationStatus";

    public static final String FIELD_PARENT_ASSOC_CRC = "PARENTASSOCCRC";

    public static final String FIELD_PRIMARYASSOCQNAME = "PRIMARYASSOCQNAME";

    public static final String FIELD_LID = "LID";

    public static final String FIELD_TENANT = "TENANT";

    public static final String FIELD_EXCEPTION_MESSAGE = "EXCEPTIONMESSAGE";

    public static final String FIELD_EXCEPTION_STACK = "EXCEPTIONSTACK";
    
}