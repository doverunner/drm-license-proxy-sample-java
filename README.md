# PallyCon Token Proxy Sample
Spring boot 를 이용한 PallyCon Token Proxy Sample Project.
## Getting started
### Test 환경 세팅
- player 페이지가 http://localhost가 아닌경우 https 설정이 필수.
1. Application.properties 세팅
해당 값들은 Sample project를 동작시키기 위한 설정 값들입니다.

**Required**
```
server.port= {server port} 
pallycon.sitekey= {PallyCon Site Key}
pallycon.accesskey= {PallyCon Access Key}
pallycon.siteid= {PallyCon Site ID}
pallycon.url.license= https://license.pallycon.com/ri/licenseManager.do
```

**Option**
```
pallycon.token.response.format=custom
pallycon.response.format=original
```
- pallycon.token.response.format : license 서버에 요청할 license response type.
    - custom: json type으로 응답
    - original : license 정보만 응답.
- pallycon.response.format : proxy 서버에서 응답 할 license response type.
    - custom: json type으로 응답. client 에서 추가로 처리하는 기능이 개발되어야 합니다.
    - original : license 정보만 응답.

### 기본 sample 구성
1. url : http://localhost/drm/{drmType} 
    - drmType : fairplay, playready, widevine  
2. cid : test  
3. userId : proxySample  
4. license Rule : 3600초 라이센스.
5. custom header name : sample-data 

#### TODO
전달 받은 sample-data header를 이용한 테스트를 위해서는 createPallyConCustomdata method 에 TODO 사항들을 update.  
[JAVA](/src/main/java/com/pallycon/sample/service/SampleService.java)  

