#!/bin/sh

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -eq 1 ] || die "No SourceForge.net user name specified."

./build-website.py

cd dist/website
find . -type f -print0 | xargs -0 chmod a+r
find . -type f -print0 | xargs -0 chmod u+w
find . -type f -print0 | xargs -0 chmod go-w
find . -type f -print0 | xargs -0 chmod a-x
chmod a+x index.php
cd ../..

rsync -avP -e ssh dist/website/ $1@web.sourceforge.net:/home/project-web/docfetcher/htdocs
