## DSL Service Declaration step

Service declaration step allows and possibly forces deveopers to
define and thus document the DSL.

DSL declaration block should be first in DSL file. If found in any
other place in DSL, an ERROR level message is logged and parsing this
DSL is stopped.

If parser finds a DSL without declaration block it logs a WARN
level message about deprecation.
If parser finds a DSL with declaration that misses an required
field or has a misdefined field, it logs an ERROR level message
and stops loading this DSL, continuing to next one.



```yaml
declare:
  call: declare               # this value is hardcoded and necessary
  version: <version no>       # for documentation
  description: <endponint description>
  method: <get | post>                
  accepts: <content type>     # json | file | formdata | etc.
  returns: <content type>    
  namespace:  <module name>   # for example "backoffice" etc. 
  allowlist:
    body:
    - field: <field name>
      type: <field data type>
      description: <field type description>
    header:
    - field: <field name>
      type: <field data type>
      description: <field type description>
    params:
    - field: <field name>
      type: <field data type>
      description: <field type description>
```

Currently only `allowlist` part is used for validation, all
other declaration fields are used for documentation only.

Only fields in `allowlist.body`/`allowlist.params` block can be used in DSL's.
DSL evalutor maps body.response/GET parameters to these fields.
The same filter is run on header fields with `allowlist.header` block as input. 
