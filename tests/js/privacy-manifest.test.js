const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..', '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');

// Upstream PR #770: RNCallKeep.m reads and writes NSUserDefaults, a
// required-reason API, so the pod ships a privacy manifest declaring it.
describe('iOS privacy manifest', () => {
  it('declares every required-reason API RNCallKeep.m uses, and nothing it collects', () => {
    const source = read('ios/RNCallKeep/RNCallKeep.m');
    const manifest = read('ios/PrivacyInfo.xcprivacy');
    expect(source).toMatch(/NSUserDefaults/);
    expect(manifest).toMatch(
      /<string>NSPrivacyAccessedAPICategoryUserDefaults<\/string>\s*<key>NSPrivacyAccessedAPITypeReasons<\/key>\s*<array>\s*<string>CA92\.1<\/string>/
    );
    expect(manifest).toMatch(/<key>NSPrivacyTracking<\/key>\s*<false\/>/);
    expect(manifest).toMatch(/<key>NSPrivacyCollectedDataTypes<\/key>\s*<array\/>/);
  });

  it('is bundled by the podspec', () => {
    expect(read('RNCallKeep.podspec')).toMatch(/resource_bundles\s*=\s*\{\s*'RNCallKeepPrivacyInfo'\s*=>\s*\['ios\/PrivacyInfo\.xcprivacy'\]/);
  });
});
