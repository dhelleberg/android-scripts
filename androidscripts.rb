require "formula"

class Androidscripts < Formula

  homepage 'https://github.com/thefabulous/android-scripts'
  url 'https://github.com/thefabulous/android-scripts/archive/master.tar.gz'
  sha256 'fd6b541c647c3fce103d7844e62e7161a769cd4900fe449c5ce74d7a5354dcb1'
  head 'https://github.com/thefabulous/android-scripts.git'

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
