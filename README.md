# lib-4642

lib-4642 is a Leiningen plugin for compiling clojure code as clojurescript
into JavaScript with an external JavaScript API.

## Usage

In your `project.clj`, add `lib-4642` as a dev-dependency like so:

```
  :dev-dependencies [...
                     [lib-4642 "0.0.1"]
                     ...]
```

Then add a separate key for configuration, and also the lib-4642.core to the hooks
list

```
  ...
  :hooks [lib-4642.core]
  :lib-4642 {:build {"code.js" [foo.bar.baz foo.bar.bam]},
             :clojurescript-snapshot "329708bdd0f039241b187bc639836d9997d8fbd4"}
  ...
```

The clojurescript-snapshot is necessary because clojurescript does not have a
release yet.

## What's it do?

lib-4642 handles fetching/using the clojurescript compiler (which is not yet available
through maven). It adds a hook to the compile task that compiles the clojurescript into
js files in the classes directory, so that the js files are automatically bundled into
your project's jar.

The other feature is that rather than compiling the clojurescript straight, it generates
API namespaces for your clojurescript functions to be called from JavaScript. This
mainly means it translates the arguments and return value between native JavaScript
data structures and ClojureScript data structures. The purpose is so that you can call
your clojurescript functions in a natural way from JavaScript.

## License

Copyright (C) 2011 Gary Fredericks

Distributed under the Eclipse Public License, the same as Clojure.
