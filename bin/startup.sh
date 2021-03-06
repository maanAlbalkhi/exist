#!/bin/bash
# -----------------------------------------------------------------------------
# startup.sh - Start Script for Jetty + eXist
#
# $Id$
# -----------------------------------------------------------------------------

## @UNIX_INSTALLER_1@

#
# In addition to the other parameter options for the jetty container
# pass -j or --jmx to enable JMX agent. The port for it can be specified
# with optional port number e.g. -j1099 or --jmx=1099.
#
usage="startup.sh [-j[jmx-port]|--jmx[=jmx-port]]\n"

# This will enable Java debugging via JDWP on port 4000 in Server mode
# DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=4000"

#DEBUG_OPTS="-Dexist.start.debug=true"

case "$0" in
	/*)
		SCRIPTPATH=$(dirname "$0")
		;;
	*)
		SCRIPTPATH=$(dirname "$PWD/$0")
		;;
esac

# source common functions and settings
source "${SCRIPTPATH}"/functions.d/eXist-settings.sh
source "${SCRIPTPATH}"/functions.d/jmx-settings.sh
source "${SCRIPTPATH}"/functions.d/getopt-settings.sh

get_opts "$@";

check_exist_home "$0";

set_exist_options;
set_jetty_home;

check_java_home;

# set java options
set_java_options;

# save LANG
set_locale_lang;

# enable the JMX agent? If so, concat to $JAVA_OPTIONS:
check_jmx_status;

if [ $FORKING == "1" ]; then
    JAVA_RUN="exec ${JAVA_RUN}"
fi

if [ $PIDFILE ]; then
    echo $$ > $PIDFILE
fi

${JAVA_RUN} ${JAVA_OPTIONS} ${OPTIONS} \
	${DEBUG_OPTS} -jar "$EXIST_HOME/start.jar" \
	jetty ${JAVA_OPTS[@]}

restore_locale_lang;
