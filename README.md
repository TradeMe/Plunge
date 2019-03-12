# Deeplinking

The deeplinking module is intended to simplify the matching of URLs to callbacks when a deep link is accepted
by the application. This module defines a new domain-specific way of matching URLS, and extracting useful
information required to link the user to the right place.

### Implementation
There's two parts to matching a URL - checking the host matches, and then checking the path matches.
To define a simple match on a FrEnd path (https://test.nz/marketplace for example), we must define a 
UrlSchemeHandler. An implementation may be as such:

```kotlin
class NewSchemeHandler(private val view: DeepLinkingView): UrlSchemeHandler() {

    override fun hostMatches(host: String): Boolean = host.contains("test.nz")
    
    override val matchers by patterns {
        pattern("/marketplace") { view.launchMarketplace() }
    }
}
```

In this simple example, we've managed to condense the matching on the `/marketplace` path, and invoking
the view into a single line. The pattern language developed for this feature can do more than just match simple paths
however.

To set up and perform matching, all you need to do is construct and instance of `DeepLinkHandler` and call the `processUri` function. An example of this 
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

### The Pattern Language

#### Simple matching
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

#### Matching with flags
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

#### Query String matching
By default, query strings are passed to the handler function via the result map. However, there may be cases where we require certain
query string parameters to be present. An example may be a URL where critical information is conveyed in the query string, such as 
https://www.test.co.nz/login?token=324829hjrehbf239. Here, the token is used to log the user in, so is essentially a required
field for a match to work successfully. We can define our pattern as such:

```kotlin
pattern("/login", requiredQueryParams = listOf("token")) { view.logInMember(token = it["token"]) }
```

In this case, the accepted handler will only be invoked if the `token` query string parameter is found.

### Testing
Due to the strict nature of how matching is done with this new pattern language, all we need to do is ensure
that an input URL (in the form of a Uri) matches our pattern definition. For this, we can instantiate our handlers
and test their matchers against an input URI in just a few lines of code:

##### Checking for a positive match
```kotlin
val handler = NewSchemeHandler(mock())
val testUri = Uri.parseUrl("https://test.nz/browse/something")

// Check that only one matcher catches the URL
assertThat(handler.matchers.filterNotNull { it.performMatch(testUri) }.size, 1) 

```
##### Checking for a negative match
```kotlin
val handler = NewSchemeHandler(mock())
val testUri = Uri.parseUrl("https://test.nz/browse/shouldntbehandled")

// Check that no matchers match
assertTrue(handler.matchers.none { it.performMatch(testUri) != null })
```

##### Checking parameters are extracted properly
```kotlin
val handler = NewSchemeHandler(mock())
val itemId = "1234343"
val testUri = Uri.parseUrl("https://test.nz/browse/item/$itemId")

// Check that the matcher extracts the Item ID correctly
assertThat(handler.matchers.first { it.performMatch(testUri)}?.get("itemId"), itemId)
```


