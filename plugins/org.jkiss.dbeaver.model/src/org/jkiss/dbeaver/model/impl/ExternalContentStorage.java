/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.model.impl;

import org.jkiss.dbeaver.Log;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jkiss.dbeaver.model.DBPApplication;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.*;

/**
 * File content storage
 */
public class ExternalContentStorage implements DBDContentStorage {

    static final Log log = Log.getLog(ExternalContentStorage.class);

    private final DBPApplication application;
    private File file;
    private String charset;

    public ExternalContentStorage(DBPApplication application, File file)
    {
        this(application, file, null);
    }

    public ExternalContentStorage(DBPApplication application, File file, String charset)
    {
        this.application = application;
        this.file = file;
        this.charset = charset;
    }

    @Override
    public InputStream getContentStream()
        throws IOException
    {
        return new FileInputStream(file);
    }

    @Override
    public Reader getContentReader()
        throws IOException
    {
        return new InputStreamReader(new FileInputStream(file), charset);
    }

    @Override
    public long getContentLength()
    {
        return file.length();
    }

    @Override
    public String getCharset()
    {
        return charset;
    }

    @Override
    public DBDContentStorage cloneStorage(DBRProgressMonitor monitor)
        throws IOException
    {
        // Create new local storage
        IFile tempFile = ContentUtils.createTempContentFile(monitor, application, "copy" + this.hashCode());
        try {
            InputStream is = new FileInputStream(file);
            try {
                tempFile.setContents(is, true, false, monitor.getNestedMonitor());
            }
            finally {
                ContentUtils.close(is);
            }
        } catch (CoreException e) {
            ContentUtils.deleteTempFile(monitor, tempFile);
            throw new IOException(e);
        }
        return new TemporaryContentStorage(application, tempFile);
    }

    @Override
    public void release()
    {
        // Do nothing
/*
        if (!file.delete()) {
            log.warn("Could not delete temporary file");
        }
*/
    }
}