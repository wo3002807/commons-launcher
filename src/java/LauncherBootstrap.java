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
 *    Alternately, this acknowledgement may appear in the software itself,
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class is used as a wrapper for loading the
 * org.apache.commons.launcher.Launcher class and invoking its
 * <code>main(String[])</code> method. This particular
 * class is primary used by the Windows 95, 98, ME, and 2000 platforms to
 * overcome the difficulty of putting a jar file directly into the JVM's
 * classpath when using batch scripts on these platforms.
 * <p>
 * Specifically, the problem on thse platforms is when Windows uses the PATH
 * environment variable to find and run a batch script, %0 will resolve
 * incorrectly in that batch script.
 * <p>
 * The way to work around this Windows limitation is to do the following:
 * <ol>
 * <li>Put this class' class file - LauncherBootstrap.class - in the same
 * directory as the batch script. Do not put this class file in a jar file.
 * <li>Put the jar file containing the launcher's classes in the same
 * directory as the batch script and this class' class file. Be sure that
 * that the jar file is named "commons-launcher.jar".
 * <li>Make the Java command in the batch script invoke Java use the following
 * classpath arguments. Be sure to include the quotes to ensure that paths
 * containing spaces are handled properly:
 * <code>-classpath %0\..;"%PATH%"</code>
 * </ol>
 *
 * @author Patrick Luby
 */
public class LauncherBootstrap {

    //---------------------------------------------------------- Static Fields

    /**
     * Ant classpath property name
     */
    public final static String ANT_CLASSPATH_PROP_NAME = "ant.class.path";

    /**
     * Jar file name
     */
    public final static String LAUNCHER_JAR_FILE_NAME = "commons-launcher.jar";

    /**
     * Properties file name
     */
    public final static String LAUNCHER_PROPS_FILE_NAME = "launcher.properties";

    /**
     * Class name to load
     */
    public final static String LAUNCHER_MAIN_CLASS_NAME = "org.apache.commons.launcher.Launcher";

    /**
     * Cached Laucher class.
     */
    private static Class launcherClass = null;

    //---------------------------------------------------------- Static Methods

    /**
     * The main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try {

            // Try to find the LAUNCHER_JAR_FILE_NAME file in the class
            // loader's and JVM's classpath.
            URL coreURL = LauncherBootstrap.class.getResource("/" + LauncherBootstrap.LAUNCHER_JAR_FILE_NAME);
            if (coreURL == null)
                throw new FileNotFoundException(LauncherBootstrap.LAUNCHER_JAR_FILE_NAME);

            // Coerce the coreURL's directory into a file
            File coreDir = new File(URLDecoder.decode(coreURL.getFile())).getCanonicalFile().getParentFile();

            // Try to find the LAUNCHER_PROPS_FILE_NAME file in the same
            // directory as this class
            File propsFile = new File(coreDir, LauncherBootstrap.LAUNCHER_PROPS_FILE_NAME);
            if (!propsFile.canRead())
                throw new FileNotFoundException(propsFile.getPath());

            // Load the properties in the LAUNCHER_PROPS_FILE_NAME 
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFile);
            props.load(fis);
            fis.close();

            // Create a class loader that contains the Launcher, Ant, and
            // JAXP classes.
            URL[] antURLs = LauncherBootstrap.fileListToURLs((String)props.get(LauncherBootstrap.ANT_CLASSPATH_PROP_NAME));
            URL[] urls = new URL[1 + antURLs.length];
            urls[0] = coreURL;
            for (int i = 0; i < antURLs.length; i++)
                urls[i + 1] = antURLs[i];
            ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader loader = null;
            if (parentLoader != null)
                loader = new URLClassLoader(urls, parentLoader);
            else
                loader = new URLClassLoader(urls);

            // Load the LAUNCHER_MAIN_CLASS_NAME class
            launcherClass = loader.loadClass(LAUNCHER_MAIN_CLASS_NAME);

            // Get the LAUNCHER_MAIN_CLASS_NAME class' getLocalizedString()
            // method as we need it for printing the usage statement
            Method getLocalizedStringMethod = launcherClass.getDeclaredMethod("getLocalizedString", new Class[]{ String.class });

            // Invoke the LAUNCHER_MAIN_CLASS_NAME class' start() method.
            // If the ant.class.path property is not set correctly in the 
            // LAUNCHER_PROPS_FILE_NAME, this will throw an exception.
            Method startMethod = launcherClass.getDeclaredMethod("start", new Class[]{ String[].class });
            int returnValue = ((Integer)startMethod.invoke(null, new Object[]{ args })).intValue();
            // Always exit cleanly after invoking the start() method
            System.exit(returnValue);

       } catch (Throwable t) {

           t.printStackTrace();
           System.exit(1);

        }

    }

    /**
     * Convert a ":" separated list of URL file fragments into an array of URL
     * objects. Note that any all URL file fragments must conform to the format
     * required by the "file" parameter in the
     * {@link URL(String, String, String)} constructor.
     *
     * @param fileList the ":" delimited list of URL file fragments to be
     *  converted
     * @return an array of URL objects
     * @throws MalformedURLException if the fileList parameter contains any
     *  malformed URLs
     */
    private static URL[] fileListToURLs(String fileList)
        throws MalformedURLException
    {

        if (fileList == null || "".equals(fileList))
            return new URL[0];

        // Parse the path string
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(fileList, ":");
        URL bootstrapURL = LauncherBootstrap.class.getResource("/" + LauncherBootstrap.class.getName() + ".class");
        while (tokenizer.hasMoreTokens())
            list.add(new URL(bootstrapURL, tokenizer.nextToken()));

        return (URL[])list.toArray(new URL[list.size()]);

    }

}
