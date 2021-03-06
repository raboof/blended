#!/bin/sh

function setenv() {

  if [ -f "$BLENDED_HOME/bin/setenv" ] ; then
    . "$BLENDED_HOME/bin/setenv"
  fi
}

function blended_home() {

  home=$1

  if [ "x$BLENDED_HOME" == "x" ] ; then
    OLDDIR=$(pwd)
    dir="$(dirname $0)/.."
    cd $dir
    home=$(pwd)
    cd $OLDDIR
  fi

  echo "$home"
}

export BLENDED_HOME=$(blended_home $BLENDED_HOME)
setenv

cd $BLENDED_HOME

# JVM Restart Delay in seconds
if [ -z "${RESTART_DELAY}" ]; then
  # we provide 2-minute default so that remote servers can clean up as well, 
  # when no such settings was defined before
  RESTART_DELAY=120
fi

# Whether to start the container in interactive mode
if [ -z "${INTERACTIVE}" ]; then
  INTERACTIVE=true
fi

LAUNCHER_OPTS="--profile-lookup $BLENDED_HOME/launch.conf --init-container-id"
if [ "x$BLENDED_STRICT" != "x" ] ; then
  LAUNCHER_OPTS="$LAUNCHER_OPTS --strict"
fi

# Options for the service daemen JVM (outer) with controls the container JVM
JAVA_OPTS="${JAVA_OPTS} -Xmx24m"
JAVA_OPTS="${JAVA_OPTS} -Dlogback.configurationFile=${BLENDED_HOME}/etc/logback.xml"
JAVA_OPTS="${JAVA_OPTS} -Dblended.home=${BLENDED_HOME}"
JAVA_OPTS="${JAVA_OPTS} -Dsun.net.client.defaultConnectTimeout=500 -Dsun.net.client.defaultReadTimeout=500"

# Options for the container JVM (inner) started/managed by the service daemon JVM
# Use prefix "-jvmOpt=" to mark JVM options for the container JVM
#CONTAINER_JAVA_OPTS="${CONTAINER_JAVA_OPTS} -jvmOpt=-Xmx1024m"

if [ -n "$DEBUG_PORT" ] ; then
  if [ -n "$DEBUG_WAIT" ] ; then
    MY_DEBUG_WAIT="y"
  else
    MY_DEBUG_WAIT="n"
  fi
  CONTAINER_JAVA_OPTS="${CONTAINER_JAVA_OPTS} -jvmOpt=-agentlib:jdwp=server=y,transport=dt_socket,address=${DEBUG_PORT},suspend=${MY_DEBUG_WAIT}"
  unset MY_DEBUG_WAIT
fi

if [ -n "$PROFILE_PORT" ] ; then
 if [ -n "$PROFILE_WAIT" ] ; then
   MY_PROFILE_WAIT="wait"
  else
   MY_PROFILE_WAIT="nowait"
 fi
 CONTAINER_JAVA_OPTS="${CONTAINER_JAVA_OPTS} -jvmOpt=-agentpath:/opt/jprofiler10/bin/linux-x64/libjprofilerti.so=port=${PROFILE_PORT},${MY_PROFILE_WAIT}"
 UNSET MY_DEBUG_WAIT
fi

# colun-separated
OUTER_CP="${BLENDED_HOME}/lib/*"
# semicolon-separated
INNER_CP="\
${BLENDED_HOME}/etc;\
${BLENDED_HOME}/lib/blended.launcher-@blended.launcher.version@.jar;\
${BLENDED_HOME}/lib/config-@typesafe.config.version@.jar;\
${BLENDED_HOME}/lib/org.osgi.core-@org.osgi.core.version@.jar;\
${BLENDED_HOME}/lib/blended.updater.config-@blended.updater.config.version@.jar;\
${BLENDED_HOME}/lib/blended.util.logging-@blended.util.logging.version@.jar;\
${BLENDED_HOME}/lib/de.tototec.cmdoption-@cmdoption.version@.jar;\
${BLENDED_HOME}/lib/scala-library-@scala.library.version@.jar;\
${BLENDED_HOME}/lib/slf4j-api-@slf4j.version@.jar;\
${BLENDED_HOME}/lib/logback-core-@logback.version@.jar;\
${BLENDED_HOME}/lib/logback-classic-@logback.version@.jar;\
"

$JAVA_HOME/bin/java -version

exec ${JAVA_HOME}/bin/java\
 $JAVA_OPTS\
 -cp\
 "${OUTER_CP}"\
 blended.launcher.jvmrunner.JvmLauncher\
 start\
 ${CONTAINER_JAVA_OPTS}\
 "-cp=${INNER_CP}"\
 "-restartDelay=${RESTART_DELAY}"\
 "-interactive=${INTERACTIVE}"\
 -- \
 blended.launcher.Launcher \
 --framework-restart 0\
 ${LAUNCHER_OPTS}\
 "$@"
