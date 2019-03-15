# Plunge

The Plunge library is intended to simplify the matching of URLs to callbacks when a deep link is accepted
by the application. This module defines a new domain-specific way of matching URLS, and extracting useful
information required to link the user to the right place.

Plunge is a library for simplifying the handling of Deep Links in your Android app! It provides a 
simple method of defining which URLs you want to handle, and which information you want to extract. It then (optionally)
includes a unified method of testing your deep linking logic, as well as if your app successfully catches URLs from a system level.

## Download

```groovy
implementation 'nz.co.trademe.plunge:plunge:current_version'
```

## Getting Started
There's two parts to matching a URL - checking the host matches, and then checking the path matches.
To define a simple match on a URL (https://test.nz/marketplace for example), we must define a 
UrlSchemeHandler. An implementation may look like this:

```kotlin
class NewSchemeHandler(private val view: DeepLinkingView): UrlSchemeHandler() {

    override fun hostMatches(host: String): Boolean = host.contains("test.nz")
    
    override val matchers by patterns {
        pattern("/marketplace") { view.launchMarketplace() }
    }
}
```

In this simple example, we've managed to condense the matching on the `/marketplace` path, and invoking
the view into a single line. The pattern matching language this library implements can do more than just
match simple paths however - more on this in [The Pattern Language](#the-pattern-language) section.

To set up and perform matching, all you need to do is construct an instance of `DeepLinkHandler` and call the `processUri` function. An example of this 
can be seen below:

```kotlin
val linkHandler = DeepLinkHandler.withSchemeHandlers(NewSchemeHandler(this))

fun onDeepLinkCaught(link: Uri) {
    val handled = linkHandler.processUri(link)
    
    if (!handled) {
        // ... Some kind of default fallback
    }
}
```

If the `DeepLinkHandler` finds a match in any of the Scheme Handlers you've defined, it'll call the associated lambda and return true.

## The Pattern Language

### Simple matching
When defining a pattern, you may want to match certain groups of the path and extract the information found within
to use for routing the user to specific places. An example may be this URL https://www.test.co.nz/1971883591
Here, all we care about from the apps perspective is the item ID, as that's what we use to launch the Item Details screen. We want to capture the 
final part of the path, and use that to move the user somewhere new. We might define our pattern as such

```kotlin
pattern("/{itemId}") { /* Launch View Item Screen */ }
```

Here we're matching on a group using the curly brace syntax `{..}`. This will catch the Item ID path segment and label it as "itemId".
We can use this extracted group in our handler:

```kotlin
pattern("/{itemId}") { view.launchViewItemScreen(itemId = it["itemId"]) }
```

### Matching with flags
This pattern may be catching too much however. We know a item ID should only ever be numeric, but our pattern catches any string.
We can add _flags_ to our pattern to specify which type of characters we should match on. Our above example now becomes this:

```kotlin
pattern("/{d|itemId}") { view.launchViewItemScreen(itemId = it["itemId"]) }
```

Here, we define our flags before the name of the group to capture. We use the `d` flag to specify that this path should only match when it's entirely
digits. 

Currently we only support the digits flag, but more may be added in future if required.

| Flag | Description       |
|------|-------------------|
|  `d` | Match only digits |

### Query String matching
By default, query strings are passed to the handler function via the result map. However, there may be cases where we require certain
query string parameters to be present. An example may be a URL where critical information is conveyed in the query string, such as 
https://www.test.co.nz/login?token=324829hjrehbf239. Here, the token is used to log the user in, so is essentially a required
field for a match to work successfully. We can define our pattern as such:

```kotlin
pattern("/login", requiredQueryParams = listOf("token")) { view.logInMember(token = it["token"]) }
```

In this case, the accepted handler will only be invoked if the `token` query string parameter is found.

## Testing
Due to the strict nature of how matching is done with this new pattern language, all we need to do is ensure
that an input URL (in the form of a Uri) matches our pattern definition. For this, we can instantiate our handlers
and test their matchers against an input URI in just a few lines of code:

### Using Plunge to test deep link handling
Plunge includes two optional, though recommended, dependencies for helping you test your apps deep link handling. These modules allow you to not only test your `pattern` implementation, but also check to make sure your app actually handles the correct URLs from an `intent-filter` level.

#### Defining test cases
Test cases for Plunge are defined in JSON for ease of parsing and human readibility. It also allows Plunge to access these test cases in a unified way. A sample test case may look like this:
```json
{
  "url": "https://www.test.co.nz/login?token=a1b2c3d4",
  "description": "The login page of the co scheme, extracts a token",
  "params": [
    {
      "name": "token",
      "value": "a1b2c3d4"
    }
  ]
}
```

Here we define the URL to test, with a human readible description. We also define params, which is what we expect our `pattern`s to match on and extract.

These test cases should be added to a new directory in the `test` source directory in your app module, and example might be `src/test/test-cases`. See the sample for an example setup!

#### Using `plunge-test` for testing patterns
You'll first need to add a dependency on our test module to you app's test configuration.
```groovy
testImplementation 'nz.co.trademe.plunge:plunge-test:current_version'
```

// TODO - @hndmrsh to fill this out

#### Use `plunge-gradle-plugin` for testing your `intent-filter`'s
You'll first need to add the plugin as a dependency in your build script:
```groovy
classpath 'nz.co.trademe.plunge:plunge-gradle-plugin:current_version'
```
Next, apply the plugin and provide a path to your test cases.
```
apply plugin: nz.co.trademe.plunge

plunge {
    testDirectory = file("$projectDir/src/test/test-cases")
}
```
After a Gradle Sync you notice a few new tasks pop up in the `verification` section for your app, starting with `plungeTest`. Running these tasks will build and deploy that variant of your app to the device attached, and then run the test cases against that installation of the app to ensure the app handles the URLs you want it to. Note, this also supports _negative_ tests, where you can specify which URLs you don't want your app to handle!

There are a couple of subtleties to be aware of:
* The release variant of the `plungeTest` task may not build and deploy your application first. If this is the case, simply ensure your have the release variant installed.
* For the verification to work, you must ensure only one device is connected in debug mode before running the test. This is due to the fact that it queries a specific device for URLs handled. 

### Manually testing your handlers

#### Checking for a positive match
```kotlin
val handler = NewSchemeHandler(mock())
val testUri = Uri.parseUrl("https://test.nz/browse/something")

// Check that only one matcher catches the URL
assertThat(handler.matchers.filterNotNull { it.performMatch(testUri) }.size, 1) 

```
#### Checking for a negative match
```kotlin
val handler = NewSchemeHandler(mock())
val testUri = Uri.parseUrl("https://test.nz/browse/shouldntbehandled")

// Check that no matchers match
assertTrue(handler.matchers.none { it.performMatch(testUri) != null })
```

#### Checking parameters are extracted properly
```kotlin
val handler = NewSchemeHandler(mock())
val itemId = "1234343"
val testUri = Uri.parseUrl("https://test.nz/browse/item/$itemId")

// Check that the matcher extracts the Item ID correctly
assertThat(handler.matchers.first { it.performMatch(testUri)}?.get("itemId"), itemId)
```
## License
Plunge is licensed as MIT.

## Contributing

We love contributions, but make sure to checkout `CONTRIBUTING.MD` first!


