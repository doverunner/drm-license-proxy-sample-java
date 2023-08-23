# PallyCon Token Proxy Sample (v2.3)

Spring boot를 이용한 PallyCon Token Proxy 샘플 프로젝트입니다.

## Getting started

### 테스트 환경 세팅

- 재생 테스트용 플레이어 웹페이지가 로컬호스트(http://localhost)가 아닌 경우, 해당 URL에 HTTPS 설정이 필수입니다. (테스트용 웹 서버에 SSL/TLS 적용 필요)
- Java JDK: amazon-corretto-17

### Application.properties 세팅

샘플 프로젝트를 실행하려면 아래와 같은 값들을 설정해야 합니다.

- server.port= {server port}
- pallycon.sitekey= {PallyCon Site Key}
- pallycon.accesskey= {PallyCon Access Key}
- pallycon.siteid= {PallyCon Site ID}
- pallycon.url.license= https://license.pallycon.com/ri/licenseManager.do

### 응답 유형에 대한 옵션

PallyCon 라이선스 서버에서 Proxy 서버에 보내줄 라이선스 응답의 유형과 Proxy 서버에서 클라이언트에 보내줄 응답의 유형을 다음과 같이 설정할 수 있습니다.

```
pallycon.token.response.format=[original|json]
pallycon.response.format=[original|json]
```

- pallycon.token.response.format : PallyCon 라이선스 서버의 license response 유형 설정
    - original: 기본적인 라이선스 정보만 응답
    - json: 라이선스 정보와 Device ID 등의 추가 정보가 포함된 JSON type으로 응답

- pallycon.response.format : proxy 서버에서 클라이언트에 전송할 license response 유형 설정
    - original: 기본적인 라이선스 정보만 응답
    - json: 추가 정보가 포함된 JSON type으로 응답. 해당 응답으로 DRM 콘텐츠를 재생하기 위해서는 클라이언트에서 추가로 응답을 파싱 처리하는 기능이 개발되어야 합니다.


### 참고 사항
1. Widevine 은 최초 인증시 Widevine 인증서를 받기 위해 라이센스 요청을 하여 인증서를 다운 받은 후 라이센스 요청을 한다.
2. NCG는 최초 라이센스 인증시 `mode=getserverinfo`를 호출하여 기기별 인증서를 다운받은 후 라이센스 요청을 한다.



## 샘플 프로젝트 기본 설정

1. url : http://localhost/drm/{drmType}
    - drmType : fairplay, playready, widevine , ncg
2. cid : test
3. userId : proxySample
4. license Rule : 라이선스 만료 시간 3600초


## TODO

1. 테스트를 위해서는 기본 설정 완료 후 `createPallyConCustomdata` 메소드의 `TODO` 사항들을 업데이트해야 합니다.
    - [properties](../src/main/resources/application.properties)
    - [JAVA](../src/main/java/com/pallycon/sample/service/SampleService.java)


2. Client( SDK, Browser ) 와 Proxy Server가 통신 할때 `user_id`, `content_id`를 Proxy Server와 통신이 필요 할 경우 당사에서 사용하고 있는 암호화 방식을 적용하여 통신하여야 한다.
    - 회사 마다 암호화 방식이 다르므로 별도로 가이드를 제공하지는 않습니다.


3. 사용하고자 하는 Policy를 `new PallyConDrmTokenClient()` 를 사용하여 지정한다.


4. 디바이스 정보 Header `pallycon-client-meta` 를 통해 Client의 정보를 받을수 있다. ( Pallycon SDK에서는 기본으로 보내줌. )
- Original Value String : `ewoJImRldmljZV9pbmZvIjogewoJCSJkZXZpY2VfbW9kZWwiOiAiaVBob25lIFNFIChpUGhvbmU4LDQpIiwKCQkib3NfdmVyc2lvbiI6IjE1LjcuMiIKCX0KfQ==`
- Base64 Decoding :
```JSON
{
    "device_info": {
        "device_model": "iPhone SE (iPhone8,4)",
        "os_version":"15.7.2"
    }
}
```

***

https://pallycon.com | cbiz@inka.co.kr

Copyright 2022 INKA Entworks. All Rights Reserved.
