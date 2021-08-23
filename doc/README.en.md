# PallyCon Token Proxy Sample (v2.0)

This sample project is for PallyCon token proxy integration based on Spring boot.

## Getting started

### Test configuration

- If you test the sample player player page online(other than localhost), the page URL should be HTTPS. (SSL/TLS is required)
- Java JDK: amazon-corretto-8  (jdk1.8.0_232)

### Configuring Application.properties

You need to configure the below values to run the sample project.

- server.port= {server port} 
- pallycon.sitekey= {PallyCon Site Key}
- pallycon.accesskey= {PallyCon Access Key}
- pallycon.siteid= {PallyCon Site ID}
- pallycon.url.license= https://license.pallycon.com/ri/licenseManager.do

### Options for Response types

You can set the type of license response that the PallyCon license server will send to the proxy server and the type of response that the proxy server will send to the client as follows.

```
pallycon.token.response.format = [original|json]
pallycon.response.format = [original|json]
```

- pallycon.token.response.format: Set the license response type of PallyCon license server
  - original: basic license information only (same as the response of v1.0 spec)
  - json: responds in JSON type with additional information such as Device ID

- pallycon.response.format: Set the type of license response to be sent from the proxy server to the client
  - original: basic license information only (same as the response of v1.0 spec)
  - json: response in JSON type with additional information. In order to play DRM content with the response, a function to parse the response additionally must be implemented on the client side.

## Default configuration of this sample

1. url : http://localhost/drm/{drmType} 
   - drmType : fairplay, playready, widevine  
2. cid : test  
3. userId : proxySample  
4. license Rule : license duration is 3600 seconds
5. custom header name : sample-data 

## TODO

To test the received sample-data header, you need to update the `TODO` of `createPallyConCustomdata` method.

[JAVA](../src/main/java/com/pallycon/sample/service/SampleService.java)  

***

https://pallycon.com | obiz@inka.co.kr

Copyright 2019 INKA Entworks. All Rights Reserved.
