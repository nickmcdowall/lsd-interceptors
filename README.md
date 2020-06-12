# yatspec-lsd-interceptors [![Download](https://api.bintray.com/packages/nickmcdowall/nkm/yatspec-lsd-interceptors/images/download.svg) ](https://bintray.com/nickmcdowall/nkm/yatspec-lsd-interceptors/_latestVersion)

A central library for interceptors that can be used with [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) (aka living sequence diagrams).

The interceptors capture messages (requests/responses) that flow in and out of the App during tests so that they can be added
to the TestState bean used by the [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) framework to generate sequence diagrams.

## Autoconfig

This library is designed with `@SpringBootTest` in mind and attempts to minimise boilerplate code by wiring up 
default bean configurations based on the beans and classes available in the project. The interceptors can be used outside of
a spring project but will require 'manual' injection.
 
### Available Interceptors

#### LsdRestTemplateInterceptor
- For `RestTemplate` and `TestRestTemplate` clients.
- Auto configured if a `TestState` bean exists and a `RestTemplate` class is on the classpath.
- Uses a `RestTemplateCustomizer` to add an interceptor and set a `BufferingClientHttpRequestFactory` to preserve the response stream after reads

#### LsdFeignLoggerInterceptor
- For `Feign` clients
- Auto configured if a `TestState` bean exists and both `FeignClientBuilder` and `Logger.Level` classes are on the classpath.
Note that if no feign `Logger.Level` bean exists one will be created (`Logger.Level.BASIC`) to enable the interceptor 
to work. If one exists it will not be replaced. 

#### LsdOkHttpInterceptor
- For `OkHttpClient` clients.
- Auto configured if `TestState` and `OkHttpClient.Builder` beans exists *and* has spring property `com.lsd.intercept.okhttp=true` 
(requires explicit property to prevent clashing with `LsdFeignLoggerInterceptor` - as it is a popular client implementation 
for `Feign` and the former interceptor should work across all Feign client implementations).

(Additional interceptors and auto configuration will be added over time).

### PathToNameMapper (http)

Each http interceptor will be provided with default `PathToNameMapper` beans for resolving _source_ and _destination_ 
names based on the endpoint path being invoked. The defaults are based on assumptions about how the client might be used which
may not always match the reality. Therefore, it is possible to override the mappings by providing user defined path to name 
mappings via a bean that matches the default bean name that would otherwise be provided. See LsdNameMappingConfiguration 
class for further details.
 
 For example it is assumed that TestRestTemplates are typically used in tests to invoke the application API for testing:
 
    TestRestTemplate (`User`) --> Application API (`App`)
 
 Within the application a RestTemplate is typically used to invoke downstream services:
 
    RestTemplate (`App`) -->  (`OtherService`) - name derived by path or user supplied mappings.
 
## Build/Release

### Requirements

JDK
* Java 11

IDE
* Lombok plugin and enable annotation processing

```
./gradlew clean build
```

### Release

Run script

```
./release.sh
```


