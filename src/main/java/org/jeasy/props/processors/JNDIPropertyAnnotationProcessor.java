/**
 * The MIT License
 *
 *   Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props.processors;

import org.jeasy.props.annotations.JNDIProperty;
import org.jeasy.props.api.AnnotationProcessingException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * An annotation processor that loads properties from a JNDI context.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class JNDIPropertyAnnotationProcessor extends AbstractAnnotationProcessor<JNDIProperty> {

    private static final Logger LOGGER = Logger.getLogger(JNDIPropertyAnnotationProcessor.class.getName());

    /**
     * The JNDI context.
     */
    private Context context;

    @Override
    public Object processAnnotation(final JNDIProperty jndiPropertyAnnotation, final Field field) throws AnnotationProcessingException {

        if (context == null) {
            try {
                context = new InitialContext(); // not in constructor cause throw NamingException
            } catch (NamingException e) {
                throw new AnnotationProcessingException("Unable to initialize JNDI context", e);
            }
        }

        String name = jndiPropertyAnnotation.value().trim();

        //check attributes
        rejectIfEmpty(name, missingAttributeValue("name", JNDIProperty.class.getName(), field));

        //get object from JNDI context
        Object value = getObjectFromJndiContext(name);

        //check object obtained from JNDI context
        if (value == null) {
            LOGGER.log(Level.WARNING, "Object ''{0}'' not found in JNDI context", name);
        }

        return value;

    }

    private Object getObjectFromJndiContext(String name) throws AnnotationProcessingException {
        Object value;
        try {
            value = context.lookup(name);
            return value;
        } catch (NamingException e) {
            throw new AnnotationProcessingException(format("Unable to lookup object '%s' from JNDI context", name), e);
        }
    }

}
