# $1: branch name
# $2: new version
# $3: github token
# $4: github repository

echo -e "\n\e[32mUpdating project version to $2 in branch $1\e[0m\n"

git checkout "$1"

./mvnw --no-transfer-progress versions:set -DnewVersion="$2" -DgenerateBackupPoms=false

git add pom.xml
git commit -m "release: Update project version to $2"
git push https://x-access-token:"$3"@github.com/"$4".git "$1"
