# yatspec-lsd-interceptors [![Download](https://api.bintray.com/packages/nickmcdowall/nkm/yatspec-lsd-interceptors/images/download.svg) ](https://bintray.com/nickmcdowall/nkm/yatspec-lsd-interceptors/_latestVersion)

A central library for interceptors that can be used with [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) (aka living sequence diagrams).

The interceptors capture interactions that flow in and out of the App during tests so that they can be added
to the `TestState` bean used by the [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) framework to generate sequence diagrams.

## Autoconfig

This library is designed with `@SpringBootTest` in mind and attempts to minimise boilerplate code by wiring up 
default bean configurations based on the beans and classes available in the project. 

The interceptors can be used outside of a spring project but will require some manual setup. The classes in the 
`com/nickmcdowall/lsd/interceptor/autoconfigure` package would be a good starting point for examples on how to configure 
the interceptors when autowiring is not an option.

To disable autoconfig so that the beans can be used in another library add the following property:

```properties
yatspec.lsd.interceptors.autoconfig.enabled=false
```
 
### Available Interceptors

#### LsdRestTemplateInterceptor
If a `TestState` bean exists and a `RestTemplate` class is on the classpath then
a `RestTemplateCustomizer` bean will be loaded into the default `RestTemplateBuilder` bean.

This causes an interceptor to be injected along with a `BufferingClientHttpRequestFactory` to allow for multiple reads of the 
 response stream (to avoid breaking the chain on additional reads).
- Don't instantiate a `RestTemplate` bean using the default constructor (or else you won't get the interceptor and factory out the box), avoid: 
```java
    // Wrong
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
```
instead use a `RestTemplateBuilder` bean which will provide a correctly configured bean:
```java
    // Correct
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
```
- `TestRestTemplate` beans just need to be `@Autowired` into your tests and will be instantiated and configured for you.

#### LsdFeignLoggerInterceptor
- For `Feign` clients
- Auto configured if a `TestState` bean exists and both `FeignClientBuilder` and `Logger.Level` classes are on the classpath.
Note that if no feign `Logger.Level` bean exists one will be created (`Logger.Level.BASIC`) to enable the interceptor 
to work. If one exists it will not be replaced. 

#### LsdOkHttpInterceptor
- For `OkHttpClient` clients.
- Auto configured if `TestState` and `OkHttpClient.Builder` beans exists *and* has spring property `yatspec.lsd.interceptors.autoconfig.okhttp.enabled=true` 
(requires explicit property to prevent clashing with `LsdFeignLoggerInterceptor` - as it is a popular client implementation 
for `Feign` and the former interceptor should work across all Feign client implementations).

(Additional interceptors and auto configuration will be added over time).

### Naming

Capturing interactions is the first piece of the puzzle, the second is determining the context of the interaction 
so that we can get the naming correct for the entities involved in the interaction. We need to know 
whether the interaction originated from within the application or externally. 

Getting the source or destination names wrong results in confusing sequence diagrams. It is easy when manually logging 
the interactions since the context is clear when writing the log statement but when using runtime interceptors it requires
 a little more work by the library but it should have you covered.

#### http interactions
To determine if the application (`App`) is the destination, the target path is compared against a list 
of known application path prefixes. This list includes common paths prefixes including `/actuator` as well as  dynamic 
paths obtained by querying the `RequestMappingHandlerMapping` bean for request mappings that have been declared within 
the application. In the case that a path matches a known application path the source of the interaction will be called 
`User` and the destination will be called `App`.

If the interaction path does not a match against any known application paths then the source name will be `App` and the 
destination name will be derived from the first section of the path. For example a path value of `/pricing/5` would result
 in a destination name of `pricing`. (_Note that diagram display names can be changed/`aliased`  by 
 implementing `WithParticipants` to provide details about the `Participant`s in the sequence diagram_).
 
If for whatever reason the list of application path prefixes is incomplete you can append additional entries to the 
collection by autowiring the `ApplicationPaths` bean in your test and then updating the list, e.g.
 
 ```java
@Autowire
private ApplicationPaths applicationPaths; 

//Call applicationPaths.addAll(additionalPaths) to append additional path prefixes;
``` 

Or override the bean completely and supply your own paths, e.g.
 
 ```java
    @Bean
    public ApplicationPaths applicationPaths() {
        return new ApplicationPaths(yourCollectionOfPaths);
    }
```

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