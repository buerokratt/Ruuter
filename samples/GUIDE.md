# Ruuter
Ruuter is a service that enables the execution of custom "DSL files"

## Basics

### DSL's
DSL files are `yaml` files detailing a workflow/steps that are to be executed when said DSL is called. More details on writing DSL files in: [Writing DSL files](./GUIDE.md#Writing-DSL-files)

### Location of DSL files
All of Ruuters DSL's have to be contained in single folder of the service provider's choosing (by default, this is the [`DSL`](../DSL) 
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
* It is mandatory to have at least one *main directory* named after an appropriate HTTP method, such as `GET` or `POST` directly in the root `DSL` directory (explained in [Request types](./GUIDE.md#Request-types))
* having any level of subdirectories inside *main directories* is allowed
* DSL's can share name's, as long as they are in different *main directories* (i.e. you can have a DSL named `example.yml` in both `GET` and `POST` directories)
* DSL's can not share name's, when they are in the same *main directory*, even when on different levels (i.e. you can not have a DSL named `example.yml` in both `POST` and `POST/SUB-DIRECTORY`)

## Calling/Querying DSL files
All of Ruuter's DSL's are exposed through the `/{configuration}` endpoint, where `${configuration}` is the name of the DSL without its `yaml` extension. 

Therefore to call a desired DSL, one must simply make an HTTP request to ruuter, 
with the desired DSL's name, e.g: `www.example-ruuter.com/messages`

### Request types
Ruuter support's all HTTP method types - when a DSL request is made, Ruuter tries to find the queried DSL from the directory with the same name of the incoming HTTP method type.
(to limit the allowed method types, see: [allowedMethodTypes](./CONFIGURATION.md#Incoming-requests) property)

So if a GET query is made to the DSL `messages`, Ruuter will try to execute `messages.yml` from the `GET` directory. 
* If `messages.yml` exists in the `GET` directory, it will be executed
* If `messages.yml` does not exist in the `GET` directory, no action will be taken
* If there is a `messages.yml` DSL in another "main directory", for example the `POST` directory, but not in the `GET` directory, then that DSL will not be executed.

### DSL query responses
A DSL query always returns a fixed response in the form of:
```
{
    "response": RETURN_VALUE
}
```
Notes:
* *RETURN_VALUE* is either null, if a DSL does not have a return value, or the returnable object of the DSL, if it does


## Writing DSL files
### General functionalities
* [steps](./general/steps.md)
* [jumps](./general/jumps.md)
* [skip](./general/skip.md)
* [sleep](./general/sleep.md)
* [scripting](./general/scripting.md)
* [Passing parameters to DSL](./general/params.md)

### Step types
* [http-get](steps/http-get.md)
* [http-post](steps/http-post.md)
* [assign](steps/assign-variables.md)
* [conditional-jump](steps/conditional-jump.md)
* [return](steps/return.md)
* [template](steps/template.md)
* [mock](steps/mock.md)
