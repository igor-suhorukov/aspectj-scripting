include Java
require "../target/maven-classloader-1.0-SNAPSHOT.jar"
include_class Java::com.github.smreed.classloader.MavenClassLoader

@cl = MavenClassLoader.forGAV("joda-time:joda-time:1.6.2");

def dateTime()
  newInstanceOf("org.joda.time.DateTime")
end

def newInstanceOf(clsName)
  @cl.loadClass(clsName).newInstance()
end

# ruby date
ruby_now = Time.now
# joda date
joda_now = dateTime()

puts "Ruby says it is #{ruby_now} and joda-time says it is #{joda_now}"

old_joda = MavenClassLoader.forGAV("joda-time:joda-time:1.6.1").loadClass("org.joda.time.DateTime").newInstance()

puts "Old joda-time says it is #{old_joda}"

puts "Are old joda-time and new joda-time classes equal? #{old_joda.getClass() == joda_now.getClass()}"
puts "Are old joda-time and new joda-time classes equal according to Java? #{old_joda.getClass().equals(joda_now.getClass())}"
puts "Are old joda-time and new joda-time classes identical? #{old_joda.getClass().object_id == joda_now.getClass().object_id}"
