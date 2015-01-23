require "formula"

class Androidscripts < Formula

  homepage 'https://github.com/dhelleberg/android-scripts'
  url 'https://github.com/dhelleberg/android-scripts/archive/1.0.1.tar.gz'
  sha1 '2cb79aa61036b83399cc718033d2883c97b2347f'
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