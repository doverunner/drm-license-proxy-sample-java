# PallyCon Token Proxy Sample

## Getting started
### Test 환경 세팅
- player 페이지가 http://localhost가 아닌경우 https 설정이 필수.
### Application.properties 세팅
해당 값들은 Sample project를 동작시키기 위한 필수 설정 값들입니다.

- server.port= {server port} 
- pallycon.sitekey= {PallyCon Site Key}
- pallycon.accesskey= {PallyCon Access Key}
- pallycon.siteid= {PallyCon Site ID}
- pallycon.url.license= https://license.pallycon.com/ri/licenseManager.do

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

