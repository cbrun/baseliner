#!/bin/bash
#Start of user code protected
#TODO set here the Hudson or Jenkins URL of your server.
HUDSON_URL="http://obeo-products.ci.obeo.fr:8180/jenkins/"
#End of user code
JOBNAME="baseliner--master"

USERNAME=$1
PASSWORD=$2
if [  -z $USERNAME ]; then
    echo "Please specify a username"
    exit
fi
if [ -z $PASSWORD ]; then
    echo "Please specify a password"
    exit
fi



CURL="curl --user $USERNAME:$PASSWORD -s "

# if exist
`$CURL -f -o /dev/null "$HUDSON_URL/job/$JOBNAME/config.xml"`
if [ $? -eq 0 ]; then
    echo "INFO: $JOBNAME already exists on the hudson server. Updating Config."
    $CURL -H "Content-Type: text/xml" --data-binary "@config.xml" "$HUDSON_URL/job/$JOBNAME/config.xml"
else
    echo "INFO: $JOBNAME does not exists on the hudson server. creating it."
    $CURL -H "Content-Type: text/xml" --data-binary "@config.xml" "$HUDSON_URL/createItem?name=$JOBNAME"
fi
