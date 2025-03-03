TAG_NAME=$1

if [[ $TAG_NAME =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    VERSION=${TAG_NAME:1}
else
    VERSION=${TAG_NAME//-SNAPSHOT/}
fi

NEXT_MINOR_VERSION=$(echo "$VERSION" | awk -F. '{print $1"."$2+1".0"}')
NEXT_PATCH_VERSION=$(echo "$VERSION" | awk -F. '{print $1"."$2"."$3+1}')
PATCH_VERSION=$(echo "$VERSION" | awk -F. '{print $1"."$2}')

{
  echo "VERSION=$VERSION";
  echo "NEXT_MINOR_VERSION=$NEXT_MINOR_VERSION";
  echo "NEXT_PATCH_VERSION=$NEXT_PATCH_VERSION";
  echo "PATCH_VERSION=$PATCH_VERSION";
} >> "$GITHUB_ENV"