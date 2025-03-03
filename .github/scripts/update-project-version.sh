# $1: branch name
# $2: new version
# $3: github token
# $4: github repository

echo -e "\n\e[32mUpdating project version to $2 in branch $1\e[0m\n"

git checkout "$1"

sed -i "s/version = \"[0-9.]*-[a-z]*\"/version = \"$2\"/" build.gradle.kts
sed -i "s/ARG VERSION=[0-9.]*-[a-z]*/ARG VERSION=$2/" Dockerfile

git add build.gradle.kts Dockerfile
git commit -m "release: Update project version to $2"
git push https://x-access-token:"$3"@github.com/"$4".git "$1"