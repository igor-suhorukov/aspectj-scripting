AspectJ scripting agent
=================

Scripting extension for [AspectJ][1] java agent. Allow java bytecode [instrumentation at jvm startup][2] by using [MVEL][3] expression and execute code from [maven artifact repositories][4]

Any AspectJ [pointcut expression][5] available with aspect types: AROUND, BEFORE, AFTER, AFTER_RETURNING, AFTER_THROWING

For [example][6]:

```json
    {
      "name": "org.aspect.testing.Aspect6",
      "type": "AROUND",
      "pointcut": "execution(public static void main(String[]))",
      "init": {
        "expression": "System.out.println(\"Start jvm\")"
      },
      "dispose": {
        "expression": "System.out.println(\"Stop jvm\")"
      }
    },
```

Thanks [Steve Reed][7] for maven classloader

[1]: http://en.wikipedia.org/wiki/AspectJ "AspectJ"
[2]: https://eclipse.org/aspectj/doc/next/devguide/ltw.html "AspectJ Load-Time Weaving"
[3]: http://en.wikipedia.org/wiki/MVEL "MVEL expression language"
[4]: http://maven.apache.org/guides/introduction/introduction-to-repositories.html "Artifact Repositories"
[5]: https://eclipse.org/aspectj/doc/released/progguide/semantics-pointcuts.html "Pointcuts: Language Semantics"
[6]: https://github.com/igor-suhorukov/aspectj-scripting/blob/master/aspectj-scripting/src/test/resources/instr.json
[7]: https://github.com/smreed "Steve Reed"