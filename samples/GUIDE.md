# Ruuter

Ruuter is a service that enables the execution of custom "DSL files"

## Basics

### DSL Files

DSL files are `yaml` files detailing a workflow/steps that are to be executed when said DSL is called through Ruuter. More details on writing DSL files
in: [Writing DSL files](./GUIDE.md#Writing-DSL-files)

### Location of DSL files

All of Ruuters DSL files have to be contained in single folder of the service provider's choosing (by default, this is the [`DSL`](../DSL)
folder, but this can be configured using the [config-path](./CONFIGURATION.md#DSL-folder-location) property)

The structure of the `DSL` folder itself looks something like this:

```
DSL
 |--POST
 |   |-- example_dsl_1.yml
 |   |-- example_dsl_2.yml
 |   |-- SUB-DIRECTORY
 |             |-- example_dsl_3.yml
 |             |-- example_dsl_4.yml
 |
 |--GET
 |   |-- example_dsl_1.yml
 |   |-- example_dsl_2.yml
 |   |-- ...
```

Notes:

* It is mandatory to have at least one *main directory* named after an appropriate HTTP method, such as `GET` or `POST` directly in the root `DSL` directory (
  explained in [Request types](./GUIDE.md#Request-types))
* having any level of subdirectories inside *main directories* is allowed
* DSL files can share name's, as long as they are in different *main directories* (i.e. you can have a DSL named `example.yml` in both `GET` and `POST`
  directories)
* DSL files can not share name's, when they are in the same *main directory*, even when on different levels (i.e. you can not have a DSL named `example.yml` in
  both `POST` and `POST/SUB-DIRECTORY`)

## Calling/Querying DSL files

All of Ruuter's DSLs are exposed through the `/{dsl}` endpoint, where `${dsl}` is the name of the DSL without its `yaml` extension.

Therefore to call a desired DSL, one must simply make an HTTP request to ruuter, with the desired DSLs name, e.g: `www.example-ruuter.com/messages`

### Request types

Ruuter supports all HTTP method types - when a DSL request is made, Ruuter tries to find the queried DSL from the directory with the same name of the incoming
HTTP method type.
(to limit the allowed method types, see: [allowedMethodTypes](./CONFIGURATION.md#Incoming-requests) property)

So if a GET query is made to the DSL `messages`, Ruuter will try to execute `messages.yml` from the `GET` directory.

* If `messages.yml` exists in the `GET` directory, it will be executed
* If `messages.yml` does not exist in the `GET` directory, no action will be taken
* If there is a `messages.yml` DSL in another "main directory", for example the `POST` directory, but not in the `GET` directory, then that DSL will not be
  executed.

### Request body

Ruuter supports request object body as formdata or JSON format.
If formdata is used, duplicate keys are ignored so the functionality is 
kept the same as for JSON, which does not allow duplicate keys.

For multipart (file) requests, all file data should be in field `file` which can be duplicate; 
content of these files will be accessible through the array `body.file[]`.  

Ruuter also supports POST requests with Content-type: text/*, in which
case the body data will be put into relevant field in body object, for
example for text/plain it will be `body.plain` etc.

### DSL query responses

A DSL query always returns a fixed response in the form of:

```json
{
    "response": RETURN_VALUE
}
```

Notes:

* *RETURN_VALUE* is either null, if a DSL does not have a return value, or the returnable object of the DSL, if it does

### Optional parameters

Any parameter where the name starts with the word `optional_`, can be omitted from requests. For example, `post-with-optional.yml` has one required parameter 
and one 
`optional_` type parameter.

```yml
return:
  return: ${incoming.body.somethingRequired}${incoming.body.optional_something}
```

A request made to the endpoint for this file will also require `somethingRequired` in the body, but `optional_something` can be omitted, based on the name.

```http request
POST http://localhost:8080/post-with-optional
Content-Type: application/json

{
  "somethingRequired": "Important data",
  "optional_something": "This can be omitted from the request"
}
```

The above request will give the same result as the one below.

```http request
POST http://localhost:8080/post-with-optional
Content-Type: application/json

{
  "somethingRequired": "Important data"
}
```

### Internal services

Requests put into first level subdirectory named `internal` will be 
used for internal requests only. Those DSLs can only be accessed from 
IPs and referrer URLs specified in configuration.


## Writing DSL files

### General functionalities

* [steps](./general/steps.md)
* [jumps](./general/jumps.md)
* [skip](./general/skip.md)
* [sleep](./general/sleep.md)
* [scripting](./general/scripting.md)
* [Passing parameters to DSL](./general/params.md)
* [Reloading DSLs](./general/reload-dsls.md)

### Step types

* [return](steps/return.md)
* [assign](steps/assign-variables.md)
* [mock](steps/mock.md)
* [http-get](steps/http-get.md)
* [http-post](steps/http-post.md)
* [conditional-jump](steps/conditional-jump.md)
* [template](steps/template.md)


### Using javascript in DSLs

Javascript function calls can be used in DSL parameters, for example 
```
    minValue: ${ list.sort( (a,b) => a - b ) }
```
If using anonymous function calls, only lambda syntax should be used, 
as `{` and `}` are reserved as DSL parameter identifiers.
