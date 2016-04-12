require "formula"

class Androidscripts < Formula

  homepage 'https://github.com/dhelleberg/android-scripts'
  url 'https://github.com/dhelleberg/android-scripts/archive/1.0.4.tar.gz'
  sha256 '348a8722e8e2b4c3d8781470ff6bf4d4d8ae14fd8a6efc05f6ea26b28a8488b6'
  head 'https://github.com/dhelleberg/android-scripts.git'

  depends_on "groovy"

  def install
    bin.install 'src/devtools.groovy' => 'devtools'
    bin.install 'src/adbwifi.groovy' => 'adbwifi'
    bin.install 'src/adbscreenrecord.groovy' => 'adbscreenrecord'
  end

  test do
    output = `#{bin}/devtools --help`.strip
    assert_match /^usage: devtools/, output
  end
end
