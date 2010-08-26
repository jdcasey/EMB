/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    @Requirement( hint = "default_" )
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
