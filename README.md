# JDisKo
JDisKo is a cross-platform JDK installer using
[foojay's discoapi](https://github.com/foojayio/discoapi) and [discoclient](https://github.com/foojayio/discoclient).

* [foojay DiscoAPI swagger UI](https://api.foojay.io/swagger-ui)

## Install
* Download an executable file which fits your OS from [JDisKo Releases](https://github.com/kwon37xi/jdisko/releases).

## Build native binary
* references
  * https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
  * https://picocli.info/picocli-on-graalvm.html
  * https://github.com/remkop/picocli-native-image-demo

* requirements
  * GraalVM 22.1.0 Java 17 CE
  * `gcc`
```
./gradlew nativeCompile
```
  * `jdisko` native executable will be generated on `app/build/native/nativeCompile`

## TODO
* disco client 를 추상화할것. 옵션이 너무 많음.
* 각 메이저 버전별 최신 버전 목록 리스팅
* 각 배포판별 리스팅
* 다운로드 및 설치
* 최신 버전 체크
* 환경변수로 기본 JDK 배포판 지정
* 환경변수로 API endpoint 지정
* upgrade