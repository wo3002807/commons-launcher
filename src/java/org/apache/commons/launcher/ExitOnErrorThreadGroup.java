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

/**
 * A class that subclasses the {@link ThreadGroup} class. This class is used
 * by {@link ChildMain#main(String[])} to run the target application. By using
 * this class, any {@link Error} other than {@link ThreadDeath} thrown by
 * threads created by the target application will be caught the process
 * terminated. By default, the JVM will only print a stack trace of the
 * {@link Error} and destroy the thread. However, when an uncaught
 * {@link Error} occurs, it normally means that the JVM has encountered a
 * severe problem. Hence, an orderly shutdown is a reasonable approach.
 * <p>
 * Note: not all threads created by the target application are guaranteed to
 * use this class. Target application's may bypass this class by creating a
 * thread using the {@link Thread#Thread(ThreadGroup, String)} or other similar
 * constructors.
 *
 * @author Patrick Luby
 */
public class ExitOnErrorThreadGroup extends ThreadGroup {

    //------------------------------------------------------------ Constructors

    /**
     * Constructs a new thread group. The parent of this new group is the
     * thread group of the currently running thread.
     *
     * @param name the name of the new thread group
     */
    public ExitOnErrorThreadGroup(String name) {

        super(name);

    }

    //----------------------------------------------------------------- Methods

    /**
     * Trap any uncaught {@link Error} other than {@link ThreadDeath} and exit.
     *
     * @param t the thread that is about to exit
     * @param e the uncaught exception
     */
    public void uncaughtException(Thread t, Throwable e) {

        if (e instanceof ThreadDeath)
            return;

        Launcher.error(e);
        System.exit(1);

    }

}
