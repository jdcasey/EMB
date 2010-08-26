package org.commonjava.emb.io;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.commonjava.emb.conf.EMBConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import jline.ConsoleReader;

@Component( role = Prompter.class, hint = "emb" )
public class EMBPrompter
    implements Prompter
{
    @Requirement
    private EMBConfiguration embConfig;

    public String prompt( final String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            return readLine();
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    private String readLine()
        throws IOException
    {
        return new BufferedReader( new InputStreamReader( embConfig.getStandardIn() ) ).readLine();
    }

    public String prompt( final String message, final String defaultReply )
        throws PrompterException
    {
        try
        {
            writePrompt( formatMessage( message, null, defaultReply ) );
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            String line = readLine();

            if ( StringUtils.isEmpty( line ) )
            {
                line = defaultReply;
            }

            return line;
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    public String prompt( final String message, @SuppressWarnings( "rawtypes" ) final List possibleValues,
                          final String defaultReply )
        throws PrompterException
    {
        final String formattedMessage = formatMessage( message, possibleValues, defaultReply );

        String line;

        do
        {
            try
            {
                writePrompt( formattedMessage );
            }
            catch ( final IOException e )
            {
                throw new PrompterException( "Failed to present prompt", e );
            }

            try
            {
                line = readLine();
            }
            catch ( final IOException e )
            {
                throw new PrompterException( "Failed to read user response", e );
            }

            if ( StringUtils.isEmpty( line ) )
            {
                line = defaultReply;
            }

            if ( line != null && !possibleValues.contains( line ) )
            {
                writeLine( "Invalid selection." );
            }
        }
        while ( line == null || !possibleValues.contains( line ) );

        return line;
    }

    private void writeLine( final String message )
    {
        embConfig.getStandardOut().println( message );
    }

    public String prompt( final String message, @SuppressWarnings( "rawtypes" ) final List possibleValues )
        throws PrompterException
    {
        return prompt( message, possibleValues, null );
    }

    public String promptForPassword( final String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            return new ConsoleReader( embConfig.getStandardIn(),
                                      new OutputStreamWriter( embConfig.getStandardOut() ) ).readLine( new Character(
                                                                                                                        '*' ) );
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    private String formatMessage( final String message, @SuppressWarnings( "rawtypes" ) final List possibleValues,
                                  final String defaultReply )
    {
        final StringBuffer formatted = new StringBuffer( message.length() * 2 );

        formatted.append( message );

        if ( possibleValues != null && !possibleValues.isEmpty() )
        {
            formatted.append( " (" );

            for ( @SuppressWarnings( "rawtypes" )
            final Iterator it = possibleValues.iterator(); it.hasNext(); )
            {
                final String possibleValue = (String) it.next();

                formatted.append( possibleValue );

                if ( it.hasNext() )
                {
                    formatted.append( '/' );
                }
            }

            formatted.append( ')' );
        }

        if ( defaultReply != null )
        {
            formatted.append( ' ' ).append( defaultReply ).append( ": " );
        }

        return formatted.toString();
    }

    private void writePrompt( final String message )
        throws IOException
    {
        embConfig.getStandardOut().print( message + ": " );
    }

    public void showMessage( final String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }
        catch ( final IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

    }

}
