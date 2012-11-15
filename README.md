# Form helper module for play 2

This module extends the built-in Form mappings of Play (Scala) framework.

## Add to your project:

Configure resolver:

```scala
resolvers += "blockthirty releases" at "https://raw.github.com/meiwin/m2repo/master/releases/"
```

Add library dependency:

```scala
libraryDepedencies += "blockthirty" %% "mod-forms" % "1.0.0"
```

## How to use

### Keyed mapping

Add supports for `keyed mapping` to Form, example:

```scala
def index = Action { implicit request =>

    // settings is a `Map[String, String]`
    // for request parameters, e.g.
    // - settings[s1] = 'abc'
    // - settings[s2] = 'def'
    // then settings will be a map = { "s1" -> "abc", "s2" -> "def" }
    val settings = Form(
        "settings" -> keyed(nonEmptyText)
    ).bindFromRequest.get

    ...
    ...
}
```
