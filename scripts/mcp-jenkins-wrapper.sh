#!/bin/bash
# Wrapper script that reads Jenkins credentials from ~/.netrc
# netrc format: machine tl.bos.local login <username> password <api-token>

NETRC_MACHINE="tl.bos.local"

# Parse credentials from netrc
if [[ -f ~/.netrc ]]; then
    CREDS=$(awk -v host="$NETRC_MACHINE" '
        $1 == "machine" && $2 == host { found=1 }
        found && $1 == "login" { user=$2 }
        found && $1 == "password" { pass=$2; print user " " pass; exit }
    ' ~/.netrc)

    JENKINS_USERNAME=$(echo "$CREDS" | cut -d' ' -f1)
    JENKINS_PASSWORD=$(echo "$CREDS" | cut -d' ' -f2)
fi

exec mcp-jenkins \
    --jenkins-url "http://jenkins:8090/" \
    --jenkins-username "${JENKINS_USERNAME}" \
    --jenkins-password "${JENKINS_PASSWORD}" \
    "$@"
