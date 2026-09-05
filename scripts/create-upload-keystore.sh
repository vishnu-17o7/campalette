#!/usr/bin/env bash
# Creates the local Play upload keystore and keystore.properties.
# Never commit the JKS or the properties file.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool was not found on PATH. Install JDK 17 and retry." >&2
  exit 1
fi

keystore_path="$root/release/campalette-upload.jks"
properties_path="$root/keystore.properties"

if [[ -e "$keystore_path" ]]; then
  echo "Refusing to overwrite existing $keystore_path" >&2
  exit 1
fi
if [[ -e "$properties_path" ]]; then
  echo "Refusing to overwrite existing $properties_path" >&2
  exit 1
fi

mkdir -p "$root/release"

echo "You will be asked for a keystore password, key password, and certificate name."
echo "Use a unique password and store it in a password manager."
echo "Play requires the certificate to remain valid after 22 October 2033; this key is valid 10,000 days."
echo

keytool -genkeypair -v \
  -keystore "$keystore_path" \
  -alias campalette-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS

python3 - <<'PY'
from getpass import getpass
from pathlib import Path
store = getpass("Re-enter the keystore password to write keystore.properties: ")
key = getpass("Re-enter the key password (same as store password unless you set a different one): ")
Path("keystore.properties").write_text(
    "storeFile=release/campalette-upload.jks\n"
    f"storePassword={store}\n"
    "keyAlias=campalette-upload\n"
    f"keyPassword={key}\n",
    encoding="ascii",
)
PY

echo
echo "Wrote $keystore_path"
echo "Wrote $properties_path"
echo "Back these up. They are gitignored and required for every Play upload."
