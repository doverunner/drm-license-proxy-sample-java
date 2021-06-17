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

```java
    //sample source
    private String createPallyConCustomdata(String sampleData, String drmType) throws Exception {
            String siteKey = env.getProperty("pallycon.sitekey");
            String accessKey = env.getProperty("pallycon.accesskey");
            String siteId = env.getProperty("pallycon.siteid");
    
            ...
    
            //TODO 1.
            // Add sample data processing
            // ....
            // cid, userId is required
            String cid = "test";
            String userId = "proxySample";
            //-----------------------------
    
            pallyConDrmTokenClient.userId(userId);
            pallyConDrmTokenClient.cId(cid);
    
            //TODO 2.
            // Create license rule
            // https://pallycon.com/docs/en/multidrm/license/license-token/#license-policy-json
            
            // option
            // create playback_policy
            // this sample rule : limit 3600 seconds license.
            PlaybackPolicy playbackPolicy = new PlaybackPolicy();
            playbackPolicy.licenseDuration(3000);
            playbackPolicy.persistent(true);
            
            // option
            // create security_policy
            //this sample rule: ALL Track, Widevine Security Level 5
            SecurityPolicyWidevine securityPolicyWidevine = 
                    new SecurityPolicyWidevine().securityLevel(WidevineSecurityLevel.HW_SECURE_ALL);
            
            SecurityPolicy securityPolicy = 
                    new SecurityPolicy().trackType(TrackType.ALL).widevine(securityPolicyWidevine);
    
            
            // option
            //create external_key
            // this sampe rule : mpeg_cenc ALL TRACK keyid : 01234567890123450123456789012345, key: 01234567890123450123456789012345
            ExternalKeyPolicyMpegCenc externalKeyPolicyMpegCenc = 
                    new ExternalKeyPolicyMpegCenc(
                            TrackType.ALL
                            ,"01234567890123450123456789012345"
                            ,"01234567890123450123456789012345");
            
            ExternalKeyPolicy externalKeyPolicy = new ExternalKeyPolicy();
            externalKeyPolicy.mpegCenc(externalKeyPolicyMpegCenc);
            
    
            //TODO 3.
            // create PallyConDrmTokenPolicy
            // Set the created playbackpolicy, securitypolicy, and externalkey.
            PallyConDrmTokenPolicy pallyConDrmTokenPolicy= new PallyConDrmTokenPolicy.PolicyBuilder()
                    .playbackPolicy(playbackPolicy)
                    .securityPolicy(securityPolicy)
                    .externalKey(externalKeyPolicy)
                    .build();
    
            // Token is created with the created token policy.
            return pallyConDrmTokenClient.policy(pallyConDrmTokenPolicy).execute();
    
        }

```

