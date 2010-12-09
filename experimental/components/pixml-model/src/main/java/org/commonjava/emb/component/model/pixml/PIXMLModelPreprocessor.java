/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.component.model.pixml;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.commonjava.emb.EMBAdvisor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Component( role = ModelReader.class, hint = "pixml" )
public class PIXMLModelPreprocessor
    implements ModelReader
{

    @Requirement( hint = "#" )
    private ModelReader embedded;

    @Requirement
    private EMBAdvisor advisor;

    private final XMLInputFactory xmlInputFactory;

    public PIXMLModelPreprocessor()
    {
        xmlInputFactory = XMLInputFactory.newFactory();
    }

    @Override
    public Model read( final File input, final Map<String, ?> options )
        throws IOException, ModelParseException
    {
        InputStream stream = null;
        try
        {
            stream = new FileInputStream( input );
            return read( stream, options );
        }
        finally
        {
            IOUtil.close( stream );
        }
    }

    private void preProcess( final XMLStreamReader reader )
        throws IOException
    {
        //        advisor.clearAdvice();

        try
        {
            int evt = -1;
            while ( ( evt = reader.next() ) != XMLStreamConstants.END_DOCUMENT )
            {
                if ( evt == XMLStreamConstants.PROCESSING_INSTRUCTION )
                {
                    final String key = reader.getPITarget();
                    final String value = reader.getPIData();
                    advisor.advise( key, value, true );
                }
            }
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException( "Failed to pre-process POM for advice in processing instructions.\nReason: "
                + e.getMessage(), e );
        }
    }

    @Override
    public Model read( final Reader input, final Map<String, ?> options )
        throws IOException, ModelParseException
    {
        final StringWriter writer = new StringWriter();

        IOUtil.copy( input, writer );

        try
        {
            preProcess( xmlInputFactory.createXMLStreamReader( new StringReader( writer.toString() ) ) );
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException( "Failed to pre-process POM for advice in processing instructions.\nReason: "
                + e.getMessage(), e );
        }

        return embedded.read( new StringReader( writer.toString() ), options );
    }

    @Override
    public Model read( final InputStream input, final Map<String, ?> options )
        throws IOException, ModelParseException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtil.copy( input, baos );

        try
        {
            preProcess( xmlInputFactory.createXMLStreamReader( new ByteArrayInputStream( baos.toByteArray() ) ) );
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException( "Failed to pre-process POM for advice in processing instructions.\nReason: "
                + e.getMessage(), e );
        }

        return embedded.read( new ByteArrayInputStream( baos.toByteArray() ), options );
    }

}
