#!/usr/bin/env ruby

require 'fileutils'
require 'rexml/document'

include FileUtils
include REXML

UNPACK_COMMANDS = ["osascript -e 'tell app \"Stuffit Expander\" to expand POSIX file \"%file\" to POSIX file \"%dir\"'",
                   "osascript -e 'tell app \"Stuffit Expander\" to expand POSIX file \"%dir/%name-bin.tar\" to POSIX file \"%dir\"' && rm %dir/%name-bin.tar"]
                   
OLD_VERSIONS = ['1.0-SNAPSHOT']
                   
VERSION_REFERENCES = ['~/bin/xvn', '~/bin/xvnDebug']
                   
TARGET = File.expand_path( "~/apps/xaven" )
MAIN = 'distro/maven3'
LIBS = ['events/api', 'events/m3-resolver-events', 'integration/autonx-m3-resolver', 'examples/resolution-logger']

class XavenInstaller
  
  def install( modules=[] )
    build( modules )
    version, distro_name = install_distro( modules )
    install_libs( distro_name, modules )
    adjust_versions( version ) unless modules.length > 0
  end #install
  
  private
  
  def build( modules )
    if ( modules.length > 0 )
      modules.each do |mod|
        puts "Rebuilding #{mod}..."
        Dir.chdir( mod ) do
          exit -1 unless system( "mvn clean install" )
        end #chdir
      end #ARGV.each
    else
      puts "Rebuilding everything..."
      exit -1 unless system( "mvn clean install" )
    end #if/else
  end #build
  
  def adjust_versions( version )
    
    VERSION_REFERENCES.each do |file|
      puts "Adjusting version to #{version} for #{file}..."
      path = File.expand_path( file )
      contents = File.read( path )
      OLD_VERSIONS.each {|old_ver| contents = contents.gsub( old_ver, version )}
      
      File.open( path, 'w' ) {|f| f.write( contents )}
    end
    
  end #adjust_versions
  
  def install_libs( distro_name, modules )
    modules = LIBS unless modules && modules.length > 0
    
    modules.each do |lib|
      version, name,path = read_artifact_id( lib )
      ext_dir = File.join( TARGET, distro_name, 'ext' )
      
      puts "Installing #{name} to #{ext_dir}..."
      cp( File.expand_path( File.join( '.', path ) ), ext_dir )
    end
  end #install_libs
  
  def install_distro( modules )
    version, name, path = read_artifact_id( MAIN, '-bin.tar.gz' )
    
    if ( modules.length < 1 )
      puts "Removing old copy of #{name}..."
      exit 2 unless rm_rf( File.join( TARGET, name ) )
    
      puts "Unpacking new copy..."
      UNPACK_COMMANDS.each do |raw_command|
        cmd = raw_command.gsub( '%file', File.expand_path( File.join( '.', path ) ) ).gsub( '%dir', TARGET ).gsub( '%name', name )
        exit 3 unless system( cmd )
      end
    end
    
    return version, name
  end #install_distro
  
  def read_artifact_id( directory, suffix='.jar' )
    pom = File.join( directory, 'pom.xml' )
    
    if ( File.exists?( pom ) )
      doc = Document.new( File.new( pom ) )
      
      aid = XPath.first( doc.root, "/project/artifactId" ).text
      ver_el = XPath.first( doc.root, '/project/version' )
      ver_el = XPath.first( doc.root, '/project/parent/version' ) unless ver_el
      
      return ver_el.text, "#{aid}-#{ver_el.text}", File.join( directory, 'target', "#{aid}-#{ver_el.text}#{suffix}" )
    else
      puts "Cannot read POM: '#{pom}'"
      exit 1
    end
  end #read_artifact_id
  
end #XavenInstaller

XavenInstaller.new.install( ARGV )
