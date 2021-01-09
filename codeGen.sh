mkdir -p j2k/input

mkdir -p tmp && cd tmp && git clone https://github.com/locationtech/jts.git && cd ../

cp -r tmp/jts/modules/core j2k/input/

rm -rf tmp/jts/modules/core/src/test

ls -al ~/intellij-rainbow-brackets/j2k/input/core

./gradlew test

zip -r result.zip j2k/output