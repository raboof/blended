package blended.updater.internal

import org.scalatest.FreeSpec
import scala.util.Success
import scala.io.Source
import blended.testsupport.TestFile
import blended.testsupport.TestFile.DeleteWhenNoFailure

class PlaceholderProcessorTest extends FreeSpec with TestFile {

  "With a PlaceholderProcesser with failOnMissing=true" - {
    val pp = new PlaceholderProcessor(Map("A" -> "a", "B" -> "b"), "${", "}", failOnMissing = true)

    "process on a single line of text" - {

      "should process an existing variable" in {
        assert(pp.process("Hello ${A}!") === Success("Hello a!"))
      }
      "should process the same variable multipe times" in {
        assert(pp.process("Hello ${A}, ${A}!") === Success("Hello a, a!"))
      }
      "should process two existing variables" in {
        assert(pp.process("Hello ${A}, ${B}!") === Success("Hello a, b!"))
      }
      "should fail on a missing variable" in {
        val ex = intercept[RuntimeException] {
          pp.process("Hello ${A}, ${C}!").get
        }
        assert(ex.getMessage() === "No property found to replace: ${C}")
      }
      "should not process an escaped existing variable" in {
        assert(pp.process("Hello \\${A}!") === Success("Hello ${A}!"))
      }
      "should not process an triple escaped existing variable" in {
        assert(pp.process("Hello \\\\\\${A}!") === Success("Hello \\${A}!"))
      }
      "should process an double escaped existing variable" in {
        assert(pp.process("Hello \\\\${A}!") === Success("Hello \\a!"))
      }

    }

    "process a file" - {
      implicit val deletePolicy = DeleteWhenNoFailure
      "should process existing varibles" in {
        withTestFiles("Hello ${A}!\nThis is me\nRegards, ${B}", "") { (in, out) =>
          pp.process(in, out)
          assert(Source.fromFile(out).getLines().toSeq === Seq("Hello a!", "This is me", "Regards, b"))
        }
      }
    }
  }

  "With a PlaceholderProcessor with failOnMissing=false" - {
    val pp = new PlaceholderProcessor(Map(), "${", "}", failOnMissing = false)
      "should not fail on a missing variable" in {
        assert(pp.process("Hello ${A}!") === Success("Hello ${A}!"))
      }

  }

}