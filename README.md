# Ksoup - Kotlin Multiplatform HTML Parser

Ksoup is a lightweight Kotlin Multiplatform library for parsing HTML, extracting HTML tags, attributes, and text, and encoding and decoding HTML entities.

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![MohamedRejeb](https://raw.githubusercontent.com/MohamedRejeb/MohamedRejeb/main/badges/mohamedrejeb.svg)](https://github.com/MohamedRejeb)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

![Slide 16_9 - 1](https://github.com/MohamedRejeb/ksoup/assets/41842296/7933a478-9d3e-4ee7-baf6-c49be9932481)

## Features

- Parse HTML from String
- Extract HTML tags, attributes, and text
- Encode and decode HTML entities
- Lightweight and does not depend on any other library
- Kotlin Multiplatform support
- Fast and efficient
- Unit tested

## Installation

Add the dependency below to your **module**'s `build.gradle.kts` or `build.gradle` file:

```kotlin
val version = "0.1.0"

// For parsing HTML
implementation("com.mohamedrejeb.ksoup:ksoup-html:$version")

// Only for encoding and decoding HTML entities 
implementation("com.mohamedrejeb.ksoup:ksoup-entites:$version")
```

## Usage

### Parsing HTML

To parse HTML from a String, use the `KsoupHtmlParser` class, and provide an implementation of the `KsoupHtmlHandler` interface, and a `KsoupHtmlOptions` object.
Both of them are optional, you can use the default ones if you want.

#### KsoupHtmlParser

You can create a parser using the `KsoupHtmlParser()`, there are several methods that you can use, for example `write` to parse a String, and `end` to close the parser when you are done:

```kotlin
val ksoupHtmlParser = KsoupHtmlParser()

// String to parse
val html = "<h1>My Heading</h1>"

// Pass the HTML to the parser (It is going to parse the HTML and call the callbacks)
ksoupHtmlParser.write(input)

// Close the parser when you are done
ksoupHtmlParser.end()
```

#### KsoupHtmlHandler

You can directly implement `KsoupHtmlHandler` interface or use `KsoupHtmlHandler.Builder()`:

```kotlin
// Implement `KsoupHtmlHandler` interface
val firstHandler = object : KsoupHtmlHandler {
    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        println("Open tag: $name")
    }
}

// Use `KsoupHtmlHandler.Builder()`
val secondHandler = KsoupHtmlHandler
    .Builder()
    .onOpenTag { name, attributes, isImplied ->
        println("Open tag: $name")
    }
```

There are several methods that you can override, for example is you want to just extract the text from the HTML, you can override the `onText` method:

```kotlin
// String to parse
val html = """
    <html>
        <head>
            <title>My Title</title>
        </head>
        <body>
            <h1>My Heading</h1>
            <p>My paragraph.</p>
        </body>
    </html>
""".trimIndent()

// String to store the extracted text
var string = ""

// Create a handler
val handler = KsoupHtmlHandler
    .Builder()
    .onText { text ->
        string += text
    }

// Create a parser
val ksoupHtmlParser = KsoupHtmlParser(
    handler = handler,
)

// Pass the HTML to the parser (It is going to parse the HTML and call the callbacks)
ksoupHtmlParser.write(input)

// Close the parser when you are done
ksoupHtmlParser.end()
```

You can also use `onOpenTag` and `onCloseTag` to know when a tag is opened or closed, it can be used for scrapping data from a website or powering a rich text editor,
Also you can use `onComment` to know when a comment is found in the HTML and `onAttribute` to know when attributes are found in a tag.

#### KsoupHtmlOptions

You can also pass `KsoupHtmlOptions` to the parser to change the behavior of the parser, you can for example disable the decoding of HTML entities which is enabled by default:

```kotlin
val options = KsoupHtmlOption(
    decodeEntities = false,
)
```

### Encoding and Decoding HTML Entities

You can use the `KsoupEntities` class to encode and decode HTML entities:

```kotlin
// Encode HTML entities
val encoded = KsoupEntities.encodeHtml("Hello & World") // return: Hello &amp; World

// Decode HTML entities
val decoded = KsoupEntities.decodeHtml("Hello &amp; World") // return: Hello & World
```

`KsoupEntities` also provides methods to encode and decode only XML entities or HTML4.
The `KsoupEntities` class is available in the `ksoup-entites` module.

Both `encodeHtml` and `decodeHtml` methods support all HTML5 entities, XML entities, and HTML4 entities.

## Coming Features

- [ ] Add clear documentation
- [ ] Add Markdown parser

## Contribution
If you've found an error in this sample, please file an issue. <br>
Feel free to help out by sending a pull request :heart:.

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/MohamedRejeb/Ksoup/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/MohamedRejeb)__ on GitHub for more libraries! 🤩

## License
```markdown
Copyright 2023 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```