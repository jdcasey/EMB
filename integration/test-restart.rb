#!/usr/bin/env ruby

require 'yaml'
require 'pp'

config_file = File.expand_path( "~/.nxtools/config" )
puts "attempting to read configuration from: #{config_file}"

if ( File.exists?( config_file ) )
  config = YAML::load_file( config_file )
  puts "read configuration:"
  pp config
else
  config = { 'nexus-home'=>"~/apps/nexus", 'nexus-bin'=>"~/apps/nexus/current/bin/jsw/macosx-universal-32", 'use-sudo'=>false }
  puts "default configuration:"
  pp config
end

restart_cmd = ''
restart_cmd << 'sudo ' if config['use-sudo']
restart_cmd << "#{config['nexus-bin']}/nexus restart"

exit 4 unless system( restart_cmd )

system( "tail -f #{config['nexus-home']}/current/logs/wrapper.log" )
