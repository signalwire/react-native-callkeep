#!/usr/bin/env bash
# Compiles the Android sources with javac against android.jar and the
# React Native / AndroidX classes, then runs the plain-Java unit tests in
# tests/android/java. No Gradle, no device.
#
#   ANDROID_HOME          SDK with platforms/android-$ANDROID_API (default 35)
#   REACT_ANDROID_VERSION react-android version from the Gradle cache (default 0.81.5)
#   CALLKEEP_CLASSPATH    extra jars/AARs (colon-separated), searched first
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
work="$root/.tmp/android-compile"
api="${ANDROID_API:-35}"
rn="${REACT_ANDROID_VERSION:-0.81.5}"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
cache="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
android_jar="$sdk/platforms/android-$api/android.jar"
[[ -f "$android_jar" ]] || { echo "missing $android_jar" >&2; exit 1; }

rm -rf "$work"; mkdir -p "$work/jars" "$work/classes" "$work/test-classes"

# newest cached artifact for group/name (or an exact version)
find_artifact() {
  local group="$1" name="$2" version="${3:-}" dir
  dir="$cache/$group/$name"
  [[ -d "$dir" ]] || return 1
  if [[ -z "$version" ]]; then version="$(ls "$dir" | sort -V | tail -1)"; fi
  find "$dir/$version" \( -name "$name-$version.aar" -o -name "$name-$version-release.aar" -o -name "$name-$version.jar" \) | head -1
}

cp_entries=("$android_jar")
add() {
  local f="$1" base
  base="$(basename "$f")"
  if [[ "$f" == *.aar ]]; then
    unzip -qo "$f" classes.jar -d "$work/jars/$base" && cp_entries+=("$work/jars/$base/classes.jar")
  else
    cp_entries+=("$f")
  fi
}

extra=(); [[ -n "${CALLKEEP_CLASSPATH:-}" ]] && IFS=: read -r -a extra <<< "$CALLKEEP_CLASSPATH"
for f in "${extra[@]+"${extra[@]}"}"; do [[ -n "$f" ]] && add "$f"; done
for spec in "com.facebook.react react-android $rn" \
            "androidx.core core" \
            "androidx.localbroadcastmanager localbroadcastmanager" \
            "androidx.annotation annotation-jvm" \
            "androidx.annotation annotation" \
            "androidx.lifecycle lifecycle-common" \
            "com.facebook.fbjni fbjni" \
            "com.facebook.infer.annotation infer-annotation" \
            "com.google.code.findbugs jsr305"; do
  # shellcheck disable=SC2086
  if f="$(find_artifact $spec)" && [[ -n "$f" ]]; then add "$f"; fi
done
cp="$(IFS=:; echo "${cp_entries[*]}")"

javac -nowarn -Xlint:none -source 17 -target 17 -cp "$cp" -d "$work/classes" \
  $(find "$root/android/src/main/java" -name '*.java')
echo "android sources compile (android-$api, react-android $rn)"

tests=$(find "$root/tests/android/java" -name '*Test.java' 2>/dev/null || true)
if [[ -n "$tests" ]]; then
  javac -nowarn -source 17 -target 17 -cp "$cp:$work/classes" -d "$work/test-classes" $tests
  for t in $tests; do
    cls="$(basename "$t" .java)"
    # android.jar is a stub: a test may load classes and read constants and annotations, not run Android code.
    java -cp "$work/test-classes:$work/classes:$cp" "io.wazo.callkeep.$cls"
  done
fi
