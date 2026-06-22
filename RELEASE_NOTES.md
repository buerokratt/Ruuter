## v1.0.0-alpha.1 (2024-05-15)

> Description

### Upgrade Steps

- (if using k8s branch) 
  Update DSL hierarchy to include project layer

### Breaking Changes

- (currently in k8s branch only) Added support for project-based hierarchical DSLs
- Moved templates to separate folder in hierarchy

### New Features

- Support for other HTTP methods in addititon to GET/POST
- Support for receiving formdata and files
- Support for sending formdata and files with HTTP POST 

- Added heartbeat endpoint ( /healthz )
- Support for external logging to OpenSearch
- Support for meaningful errors 

- Added service declaration block for DSLs
- Added automatic OpenAPI spec generation from service declarations

- Minor adjustments: added configuration fields:
 
   * `application.cors.allowCredentials` to set CORS Access-Control-Allow-Credentials
   * `application.httpResponseSizeLimit` to set maximum allowed response size
    

### Bug Fixes

-

### Performance Improvements

-

### Other Changes

- Upgraded framework to Spring Boot 3
