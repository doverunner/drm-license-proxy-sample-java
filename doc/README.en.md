# Doverunner Token Proxy Sample (v3.0)

This sample project is for Doverunner token proxy integration based on Spring boot.

## Getting started

### Test configuration

- If you test the sample player page online(other than localhost), the page URL should be HTTPS. (SSL/TLS is required)
- Java JDK: amazon-corretto-21

### Configuring Application.properties

You need to configure the below values to run the sample project.

- server.port= {server port}
- doverunner.sitekey= {Doverunner Site Key}
- doverunner.accesskey= {Doverunner Access Key}
- doverunner.siteid= {Doverunner Site ID}
- doverunner.url.license= https://drm-license-seoul.doverunner.com/ri/licenseManager.do

### Options for Response types

You can set the type of license response that the Doverunner license server will send to the proxy server and the type of response that the proxy server will send to the client as follows.

```
doverunner.token.response.format = [original|json]
doverunner.response.format = [original|json]
```

- doverunner.token.response.format: Set the license response type of Doverunner license server
    - original: basic license information only (same as the response of v1.0 spec)
    - json: responds in JSON type with additional information such as Device ID

- doverunner.response.format: Set the type of license response to be sent from the proxy server to the client
    - original: basic license information only (same as the response of v1.0 spec)
    - json: response in JSON type with additional information. In order to play DRM content with the response, a function to parse the response additionally must be implemented on the client side.


### Notes
1. At the time of initial authentication, Widevine requests a license to obtain a Widevine certificate, download the certificate, and request a license.
2. NCG calls `mode=getserverinfo` to download a certificate for each device and requests a license.


## Default configuration of this sample

1. url : http://localhost/drm/{drmType}
    - drmType : fairplay, playready, widevine, ncg, wiseplay
2. cid : test
3. userId : proxySample
4. license Rule : license duration is 3600 seconds


## TODO

1. For testing, you need to update the `TODO` items in the `createPallyconCustomData` method.
    - [properties](../src/main/resources/application.properties)
    - [JAVA](../src/main/java/com/doverunner/sample/service/SampleService.java)

2. When the client (SDK, Browser) and proxy server connection, if `user_id` and `content_id` need to connection with the proxy server, the encryption method used by the company shall be applied and communicated.
- Different companies have different encryption methods, so we don't provide separate guides.


3. Specify the policy to be used using `new DoverunnerDrmTokenClient()`

4. The device information header `pallycon-client-meta` allows you to receive information from the client. ( Doverunner SDK sends it by default. )
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

https://doverunner.com | mkt@doverunner.com

Copyright 2025 Doverunner. All Rights Reserved.
