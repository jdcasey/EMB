#!/usr/bin/env ruby

require 'yaml'
require 'pp'

version = '1.0-SNAPSHOT'
plugins = ['nexus-autonx-plugin']

config_file = File.expand_path( "~/.nxtools/config" )
puts "attempting to read configuration from: #{config_file}"

if ( File.exists?( config_file ) )
  config = YAML::load_file( config_file )
  puts "read configuration:"
  pp config
else
  config = { 'nexus-home'=>"~/apps/nexus", 'nexus-bin'=>"~/apps/nexus/current/bin/jsw/macosx-universal-32", 'use-sudo'=>false, 'maven-goals'=>'clean install' }
  puts "default configuration:"
  pp config
end

# We may need nexus to be available in order to build...
exit 1 unless system( "mvn #{config['maven-goals']}" )

stop_cmd = ''
stop_cmd << 'sudo ' if config['use-sudo']
stop_cmd << "#{config['nexus-bin']}/nexus stop"

exit 5 unless system( stop_cmd )

plugins.each do |plugin|
  exit 2 unless system( "rm -rf #{config['nexus-home']}/sonatype-work/nexus/plugin-repository/#{plugin}-#{version}" )
end

plugins.each do |plugin|
	exit 3 unless system( "unzip #{plugin}/target/#{plugin}-#{version}-bundle.zip -d  #{config['nexus-home']}/sonatype-work/nexus/plugin-repository" )
end

start_cmd = ''
start_cmd << 'sudo ' if config['use-sudo']
start_cmd << "#{config['nexus-bin']}/nexus start"

exit 4 unless system( start_cmd )

system( "tail -f  #{config['nexus-home']}/current/logs/wrapper.log" )
