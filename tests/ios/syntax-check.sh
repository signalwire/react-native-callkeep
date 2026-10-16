#!/usr/bin/env bash
# Type-checks ios/RNCallKeep/RNCallKeep.m with clang -fsyntax-only against the
# iOS Simulator SDK and React Native's own headers (no Pods, no Xcode project).
#
#   REACT_NATIVE_DIR  a react-native package (default: ../react-native, then node_modules/react-native)
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
work="$root/.tmp/ios-headers"
rn="${REACT_NATIVE_DIR:-}"
for candidate in "$rn" "$root/../react-native" "$root/node_modules/react-native"; do
  if [[ -n "$candidate" && -f "$candidate/React/Base/RCTBridgeModule.h" ]]; then rn="$candidate"; break; fi
done
[[ -f "$rn/React/Base/RCTBridgeModule.h" ]] || { echo "set REACT_NATIVE_DIR to a react-native package" >&2; exit 1; }

# <React/X.h> resolves through a flat directory of links, as CocoaPods' public headers do.
rm -rf "$work"; mkdir -p "$work/React"
find "$rn/React" "$rn/Libraries" -name '*.h' -not -path '*/Tests/*' | while read -r h; do
  ln -sf "$h" "$work/React/$(basename "$h")"
done
mkdir -p "$work/RCTDeprecation"
ln -sf "$rn/ReactApple/Libraries/RCTFoundation/RCTDeprecation/Exported/RCTDeprecation.h" "$work/RCTDeprecation/RCTDeprecation.h"

sdk="$(xcrun --sdk iphonesimulator --show-sdk-path)"
xcrun --sdk iphonesimulator clang -fsyntax-only -fobjc-arc -fmodules \
  -target arm64-apple-ios15.1-simulator -isysroot "$sdk" \
  -Werror=objc-method-access -Wno-nullability-completeness \
  -I "$work" -I "$rn/ReactCommon/yoga" -I "$root/ios/RNCallKeep" \
  "$root/ios/RNCallKeep/RNCallKeep.m"
echo "RNCallKeep.m type-checks ($(basename "$sdk"), react-native $(node -p "require('$rn/package.json').version"))"
