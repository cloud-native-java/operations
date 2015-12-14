# bootiful-rest-basics

This example demonstrates some of the basic usages of Spring Boot with Spring REST.

## Documenting REST APIs

In this example you'll find two methods for documenting REST APIs.

* [Swagger](http://java.dzone.com/articles/spring-boot-swagger-ui)
* [Spring REST Docs](https://github.com/spring-projects/spring-restdocs)

### Swagger

Swagger is enabled using the `SwaggerSpringMvcPlugin` bean and configured through the `SwaggerConfig` class. To enable the auto-generation of Swagger meta data as an API endpoint, the following configuration is added to the package of the Sprint Boot `Application` class in the `demo` package. Spring will automatically scan for this configuration and enable Swagger at runtime.

    @Configuration
    @EnableSwagger
    @EnableAutoConfiguration
    public class SwaggerConfig {
    
        private SpringSwaggerConfig springSwaggerConfig;
    
        @Autowired
        public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
            this.springSwaggerConfig = springSwaggerConfig;
        }
    
        @Bean
        public SwaggerSpringMvcPlugin customImplementation() {
            return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                    .apiInfo(new ApiInfo(
                            "Bootiful Customers API",
                            "This app documents the Bootiful Customers API. " +
                                    "Get information about your customers using Spring Boot and Swagger.",
                            null,
                            null,
                            null,
                            null
                    ))
                    .useDefaultResponseMessages(false)
                    .includePatterns("/v1/customers.*");
        }
    
    }
    
#### Documenting REST Controllers

In order to see meaningful documentation on REST API methods, meta-data must be added as annotations to REST controller methods, as shown below.

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get all Customers", notes = "Gets all Customers")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Fields are with validation errors"),
    @ApiResponse(code = 200, message = "") })
    ResponseEntity<Collection<Customer>> getCollection() {
        return ResponseEntity.ok(this.customerRepository.findAll());
    }
    
By using the `@ApiOperation` and `@ApiResponse` annotations, Swagger will automatically document HTTP methods in your REST controller and describe them at runtime, as shown below.

> GET http://localhost:8080/api-docs

    {
       "apiVersion":"1.0",
       "apis":[
          {
             "description":"Customer Rest Controller",
             "path":"/default/customer-rest-controller",
             "position":0
          }
       ],
       "authorizations":{
    
       },
       "info":{
          "description":"This app documents the Bootiful Customers API. Get information about your customers using Spring Boot and Swagger.",
          "title":"Bootiful Customers API"
       },
       "swaggerVersion":"1.2"
    }

As you see in the response, the property `apis` contains our documented `CustomerRestController` class. We can navigate to the listed `path` and get the following response.

> GET http://localhost:8080/api-docs/default/customer-rest-controller

    {
       "apiVersion":"1.0",
       "apis":[
          {
             "description":"getCollection",
             "operations":[
                {
                   "method":"GET",
                   "summary":"Get all Customers",
                   "notes":"Gets all Customers",
                   "nickname":"getCollection",
                   "produces":[
                      "*/*"
                   ],
                   "consumes":[
                      "application/json"
                   ],
                   "parameters":[

                   ],
                   "responseMessages":[
                      {
                         "code":200,
                         "message":"OK",
                         "responseModel":"Collection«Customer»"
                      },
                      {
                         "code":400,
                         "message":"Fields are with validation errors",
                         "responseModel":"Void"
                      }
                   ],
                   "deprecated":"false",
                   "type":"Collection«Customer»"
                }
             ],
             "path":"/v1/customers"
          },
          ...