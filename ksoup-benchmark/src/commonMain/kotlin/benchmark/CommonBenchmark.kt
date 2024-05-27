package benchmark

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
class CommonBenchmark {
    private lateinit var handler: KsoupHtmlHandler
    private lateinit var parser: KsoupHtmlParser

    @Setup
    fun setUp() {
        handler = KsoupHtmlHandler
            .Builder()
            .build()
        parser = KsoupHtmlParser(handler = handler)
    }

    @TearDown
    fun teardown() {
        parser.end()
    }

    @Benchmark
    fun parseHtml() {
        parser.write("<html><head><title>Test</title></head><body><h1>Test</h1></body></html>")
    }

    @Benchmark
    fun parseComplexAndLongHtml() {
        parser.write("<html><head><title>Test</title><style>body { color: red; }</style><script>console.log('test')</script></head><body><!-- comment --><h1 class='title' id='test'>Test</h1></body></html>".repeat(100))
    }
}