/*
 * $Source: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//launcher/src/java/org/apache/commons/launcher/types/ConditionalArgument.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/30 15:21:29 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation - http://www.apache.org/"
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * http://www.apache.org/
 *
 */

package org.apache.commons.launcher.types;

import java.io.File;

import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Path;

/**
 * A class that represents nested <arg> or <jvmarg> elements. This class
 * provides the same functionality as the class that represents these same
 * elements in a "java" task. In addition, this class supports conditional "if"
 * and "unless" attributes.
 *
 * @author Patrick Luby
 */
public class ConditionalArgument extends DataType {

    //------------------------------------------------------------------ Fields

    /**
     * Cached "if" condition flag.
     */
    private String ifCondition = null;

    /**
     * Cached "unless" condition flag.
     */
    private String unlessCondition = null;

    /**
     * Cached command line arguments.
     */
    private String[] parts = null;

    //----------------------------------------------------------------- Methods

    /**
     * Get the "if" condition flag.
     *
     * @return the "if" condition flag
     */
    public String getIf() {
 
        return ProjectHelper.replaceProperties(project, ifCondition, project.getProperties());

    }

    /**
     * Get a single command line argument.
     *
     * @return a single command line argument
     */
    public String[] getParts() {

        String[] list = new String[parts.length];
        for (int i = 0; i < parts.length; i++)
            list[i] = ProjectHelper.replaceProperties(project, parts[i], project.getProperties());
        return list;

    }

    /**
     * Get the "unless" condition flag.
     *
     * @return the "unless" condition flag
     */
    public String getUnless() {
 
        return ProjectHelper.replaceProperties(project, unlessCondition, project.getProperties());

    }

    /**
     * Set a single command line argument to the absolute
     * filename of the specified file.
     *
     * @param file a single command line argument
     */
    public void setFile(File file) {

        this.parts = new String[]{ file.getAbsolutePath() };

    }

    /**
     * Set the "if" condition. Tasks that nest this class as an element
     * should evaluate this flag in their {@link Task#execute()} method. If the
     * following conditions are true, the task should process this element:
     * <ul>
     * <ol>The flag is neither null nor a empty string
     * <ol>The property that the flag resolves to after macro substitution
     *  is defined
     * </ul>
     *
     * @param property a property name or macro
     */
    public void setIf(String property) {
 
        this.ifCondition = property;

    }

    /**
     * Set a line to split into several command line arguments.
     *
     * @param line line to split into several commandline arguments
     */
    public void setLine(String line) {

        parts = Commandline.translateCommandline(line);

    }

    /**
     * Set a single command line argument and treat it like a path. The
     * correct path separator for the platform is used.
     *
     * @param path a single command line argument
     */
    public void setPath(Path path) {

        this.parts = new String[]{ path.toString() };

    }

    /**
     * Set the "unless" condition. Tasks that nest this class as an element
     * should evaluate this flag in their {@link Task#execute()} method. If the
     * following conditions are true, the task should ignore this element:
     * <ul>
     * <ol>The flag is neither null nor a empty string
     * <ol>The property that the flag resolves to after macro substitution
     *  is defined
     * </ul>
     *
     * @param property a property name or macro
     */
    public void setUnless(String property) {
 
        this.unlessCondition = property;

    }

    /**
     * Set a single command line argument.
     *
     * @param value a single command line argument
     */
    public void setValue(String value) {

        this.parts = new String[]{ value };

    }

}
