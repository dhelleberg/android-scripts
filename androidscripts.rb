require "formula"

class Androidscripts < Formula

  homepage 'https://github.com/dhelleberg/android-scripts'
  url 'https://github.com/dhelleberg/android-scripts/archive/1.0.2.tar.gz'
  sha1 'ec3247061457088699539f4391c51727ddfe5425'
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