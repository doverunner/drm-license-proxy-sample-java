# PallyCon Token Proxy Sample (v2.3)

This sample project is for PallyCon token proxy integration based on Spring boot.

## Getting started

### Test configuration

- If you test the sample player player page online(other than localhost), the page URL should be HTTPS. (SSL/TLS is required)
- Java JDK: amazon-corretto-17

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


### Notes
1. At the time of initial authentication, Widevine requests a license to obtain a Widevine certificate, download the certificate, and request a license.
2. NCG calls `mode=getserverinfo` to download a certificate for each device and requests a license.
3. For the Android SDK, both requests and responses are communicated using AES encryption.


## Default configuration of this sample

1. url : http://localhost/drm/{drmType} 
   - drmType : fairplay, playready, widevine, ncg  
2. AES key : `i9EpyNdKYlW2KPeYLHaWU9nzmEAIcUwn`
3. AES iv : `0123456789abcdef`
4. license Rule : license duration is 3600 seconds


## TODO

1. For testing, you need to update the `TODO` items in the `createPallyConCustomdata` method.
   - [properties](../src/main/resources/application.properties)
   - [JAVA](../src/main/java/com/pallycon/sample/service/SampleService.java)

2. In the sample code, `user_id`, `content_id` is implemented to be checked by passing the hash value through HTTP header when the client (SDK, Browser) and proxy server communicate.
   This is just an example, and you should implement a separate encryption method in the actual production environment.
   - We don't provide a separate guide to encryption because different companies use different methods.


3. Specify the policy to be used using `new PallyConDrmTokenClient()`

4. The device information header `pallycon-client-meta` allows you to receive information from the client. ( Pallycon SDK sends it by default. )
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

https://pallycon.com | obiz@inka.co.kr

Copyright 2022 INKA Entworks. All Rights Reserved.
