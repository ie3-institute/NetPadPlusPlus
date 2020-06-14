#!/bin/bash

#setup github token
token=$1

# get current version from gradle 
version=v$(../gradlew -q printVersion)

# check if release exists for this tag
## get the id if any 
id=$(curl -s -XGET -H "Authorization:token $token" https://api.github.com/repos/ie3-institute/NetPadPlusPlus/releases/tags/${version}  | grep -o -E "\"id\":\s[0-9]+" | awk 'NR==1{gsub(/^[ \t]+| [ \t]+$/,"");print $2}')

if [[ -z "$id" ]]
then
      echo "no release for tag exists, creating new release"
else
      echo "release for tag exists, deleting it before new release creation"
      curl -s -XDELETE -H "Authorization:token $token" https://api.github.com/repos/ie3-institute/NetPadPlusPlus/releases/$id
fi

# create new release 
## get the full message associated with the current tag
message="$(git for-each-ref refs/tags/${version} --format='%(contents)')"

## get the title and the description as separated variables
name=$(echo "$message" | head -n1)
description=$(echo "$message" | tail -n +3)
description=$(echo "$description" | sed 'H;1h;$!d;x;y/\n/,/') # Escape line breaks to prevent json parsing problems

## create a new release with the gathered information
release=$(curl -s -XPOST -H "Authorization:token $token" --data "{\"tag_name\": \"$version\", \"target_commitish\": \"master\", \"name\": \"$name\", \"body\": \"$description\", \"draft\": false, \"prerelease\": true}" https://api.github.com/repos/ie3-institute/NetPadPlusPlus/releases)

## Extract the id of the release from the creation response
echo "old release id: $id"
id=$(curl -s -XGET -H "Authorization:token $token" https://api.github.com/repos/ie3-institute/NetPadPlusPlus/releases/tags/${version}  | grep -o -E "\"id\":\s[0-9]+" | awk 'NR==1{gsub(/^[ \t]+| [ \t]+$/,"");print $2}')

echo "new release id: $id"

# Create & Upload the artifact
## create
echo 'creating shadow jar ...'
../gradlew -q clean shadowJar

## compress
echo 'compressing ...'
rm NetPadPlusPlus.zip 2> /dev/null
zip -jr NetPadPlusPlus.zip build/libs/

## upload
echo 'uploading artifact ...'
curl -XPOST -H "Authorization:token $token" -H "Content-Type: application/zip" --data-binary @NetPadPlusPlus.zip https://uploads.github.com/repos/ie3-institute/NetPadPlusPlus/releases/${id}/assets?name=NetPadPlusPlus.zip

## cleanup
rm NetPadPlusPlus.zip 2> /dev/null