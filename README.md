# yatspec-lsd-interceptors [![Download](https://api.bintray.com/packages/nickmcdowall/nkm/yatspec-lsd-interceptors/images/download.svg) ](https://bintray.com/nickmcdowall/nkm/yatspec-lsd-interceptors/_latestVersion)

A central library for interceptors that can be used with Yatspec-LSD / [living sequence diagrams](https://github.com/nickmcdowall/yatspec).

## Interceptors

The interceptors are used to capture messages that flow in and out of the App being tested so that they can be displayed 
on the generated sequence diagrams.

### Available Interceptors

- LsdRestTemplateInterceptor
The `LsdRestTemplateInterceptor` is used by projects that use spring's `RestTemplate` to send messages downstream.
This includes the `TestRestTemplate` which uses a RestTemplate internally.

- OkHttpInterceptor
The `OkHttpInterceptor` is useful when your app uses Feign clients and you choose to use OkHttp as the underlying client implementation.

## Autoconfig

This library is designed to work well with `SpringBootTest` tests by wiring up default bean configurations for the most
 common use-cases to avoid the need for much boilerplate code with the ability to easily supply user defined @Bean overrides
 where necessary or desirable.

#### Interceptors

For example when running a `SpringBootTest` with an application context that has both a `RestTemplate`
 and a `TestState` bean available then the `LsdRestTemplateInterceptor` will be injected automatically to capture interactions
 between the RestTemplate and downstream endpoints.

If a `TestRestTemplate` bean is also available it too will have an `LsdRestTemplateInterceptor` configured to intercept
 interactions and it will be assumed to represent the calls from the `User` to the `App` being tested.

(Additional autowiring will eventually be introduced to cover each of the available interceptors.)

#### DestinationNamesMapper
By default a `DestinationNamesMapper` which attempts to infer the downstream target names to use in the sequence diagrams based on the first part of the path.
If you prefer to specify your own mappings simply create a `@Bean` of type `DestinationNamesMapper` named 
`restTemplateDestinationMappings` to override the provided mapping. You may find the `UserSuppliedMappings` 
implementation useful - this takes a map of path prefixes to allow more flexibility on the names used 
(alternatively look into implementing `WithParticipants` and using alias names to change the names used in the diagrams).

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


