# yatspec-lsd-interceptors [![Download](https://api.bintray.com/packages/nickmcdowall/nkm/yatspec-lsd-interceptors/images/download.svg) ](https://bintray.com/nickmcdowall/nkm/yatspec-lsd-interceptors/_latestVersion)

A central library for interceptors that can be used with Yatspec-LSD / [living sequence diagrams](https://github.com/nickmcdowall/yatspec).

## Interceptors

The interceptors are used to capture messages that flow in and out of the App being tested so that they can be displayed 
on the generated sequence diagrams.

### Available Interceptors

- LsdRestTemplateInterceptor

The `LsdRestTemplateInterceptor` is used by projects that use spring's `RestTemplate` to send messages downstream.
This includes the `TestRestTemplate` which uses a RestTemplate internally.

- LsdOkHttpInterceptor

The `LsdOkHttpInterceptor` is useful when your app uses _Feign_ clients with _OkHttp_ as the underlying client implementation.

## Autoconfig

This library is designed to work well with `SpringBootTest` tests by wiring up default bean configurations for the most
 common use-cases to avoid the need for much boilerplate code with the ability to easily supply user defined @Bean overrides
 where necessary or desirable.

#### Interceptors

Some Interceptors will be auto injected based on which beans and classes are available in the application context.

For example when running a `SpringBootTest` with an application context that has `RestTemplate`, `TestRestTemplate`
 and `TestState` beans available, the `LsdRestTemplateInterceptor` will be injected into both the `RestTemplate` and 
 the `TestRestTemplate` beans automatically.
  
For projects that use `Feign` clients and have okHttp enabled, if a `OkHttpClient.Builder` bean is defined it will have
an `LsdOkHttpInterceptor` auto injected into it's list of interceptors.

(Additional interceptors and auto configuration will be added over time).

#### PathToNameMapper (http)

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


