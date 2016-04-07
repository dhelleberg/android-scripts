require "formula"

class Androidscripts < Formula

  homepage 'https://github.com/dhelleberg/android-scripts'
  url 'https://github.com/dhelleberg/android-scripts/archive/1.0.3.tar.gz'
  sha1 'ed5ab64a07b1a760841a1167629da3461b7df96d'
  head 'https://github.com/dhelleberg/android-scripts.git'

  depends_on "groovy"

  def install
    bin.install 'src/devtools.groovy' => 'devtools'
    bin.install 'src/adbwifi.groovy' => 'adbwifi'
  end

  test do
    output = `#{bin}/devtools --help`.strip
    assert_match /^usage: devtools/, output
  end
end
