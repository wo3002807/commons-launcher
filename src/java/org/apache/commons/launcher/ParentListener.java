/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *             Copyright (c) 2002 The Apache Software Foundation.            *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The Jakarta  Project",  and  "Apache  Software Foundation" *
 *    must not  be used  to endorse  or promote  products derived  from this *
 *    software without  prior written  permission.  For written  permission, *
 *    please contact <apache@apache.org>.                                    *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package org.apache.commons.launcher;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * A class for detecting if the parent JVM that launched this process has
 * terminated.
 *
 * @author Patrick Luby
 */
public class ParentListener extends Thread {

    //------------------------------------------------------------------ Fields

    /**
     * Cached heartbeat file.
     */
    private File heartbeatFile = null;

    //------------------------------------------------------------ Constructors

    /**
     * Validates and caches a lock file created by the parent JVM.
     *
     * @param path the lock file that the parent JVM has an open
     *  FileOutputStream
     * @throws IOException if the heartbeat cannot be converted into a valid
     *  File object
     */
    public ParentListener(String path) throws IOException {

        if (path == null)
            throw new IOException();

        // Make sure we have a valid path
        heartbeatFile = new File(path);
        heartbeatFile.getCanonicalPath();

    }

    //----------------------------------------------------------------- Methods

    /**
     * Periodically check that the parent JVM has not terminated. On all
     * platforms other than Windows, this method will check that System.in has
     * not been closed. On Windows NT, 2000, and XP the lock file specified in
     * the {@link #ParentListener(String)} constructor is monitored as reading
     * System.in will block the entire process on Windows machines that use
     * some versions of Unix shells such as MKS, etc. No monitoring is done
     * on Window 95, 98, and ME.
     */ 
    public void run() {

        String osname = System.getProperty("os.name").toLowerCase();

        // We need to use file locking on Windows since reading System.in
        // will block the entire process on some Windows machines.
        if (osname.indexOf("windows") >= 0) {

            // Do nothing if this is a Windows 9x platform since our file
            // locking mechanism does not work on the early versions of
            // Windows
            if (osname.indexOf("nt") == -1 && osname.indexOf("2000") == -1 && osname.indexOf("xp") == -1)
                return;

            // If we can delete the heartbeatFile on Windows, it means that
            // the parent JVM has closed its FileOutputStream on the file.
            // Note that the parent JVM's stream should only be closed when
            // it exits.
            for ( ; ; ) {
                if (heartbeatFile.delete())
                    break;
                // Wait awhile before we try again
                yield();
                try {
                    sleep(5000);
                } catch (Exception e) {}
            }

        } else {

            // Cache System.in in case the application redirects
            InputStream is = System.in;
            int bytesAvailable = 0;
            int bytesRead = 0;
            byte[] buf = new byte[1024];
            try {
                while (true) {
                    synchronized (is) {
                        // Mark the stream position so that other threads can
                        // reread the strea
                        is.mark(buf.length);
                        // Read one more byte than has already been read to
                        // force the stream to wait for input
                        bytesAvailable = is.available();
                        if (bytesAvailable < buf.length) {
                            bytesRead = is.read(buf, 0, bytesAvailable + 1);
                            // Reset so that we "unread" the bytes that we read
                            is.reset();
                            if (bytesRead == -1)
                                break;
                        } else {
                            // Make the buffer larger
                            if (buf.length < Integer.MAX_VALUE / 2)
                                buf = new byte[buf.length * 2];
                        }
                    }
                    yield();
                }
            } catch (IOException ioe) {}

        }

        // Clean up before exiting
        if (heartbeatFile != null)
            heartbeatFile.delete();

        // Exit this process since the parent JVM has exited
        System.exit(0);

    }

}
