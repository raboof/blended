package blended.security.login.internal

import java.io.File

import blended.akka.internal.BlendedAkkaActivator
import blended.security.PasswordCallbackHandler
import blended.security.internal.SecurityActivator
import blended.security.login.api.{Token, TokenStore}
import blended.security.login.impl.LoginActivator
import blended.testsupport.BlendedTestSupport
import blended.testsupport.pojosr.{PojoSrTestHelper, SimplePojosrBlendedContainer}
import javax.security.auth.Subject
import javax.security.auth.login.LoginContext
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class TokenStoreSpec extends FreeSpec
  with Matchers
  with SimplePojosrBlendedContainer
  with PojoSrTestHelper {

  private[this] val baseDir = new File(BlendedTestSupport.projectTestOutput, "container").getAbsolutePath()

  def withTokenStore[T](f : TokenStore => T): T = {
    withSimpleBlendedContainer(baseDir) { sr =>
      withStartedBundles(sr)(Seq(
        "blended.akka" -> Some(() => new BlendedAkkaActivator()),
        "blended.security" -> Some(() => new SecurityActivator()),
        "blended.security.login" -> Some(() => new LoginActivator())
      )) { sr =>
        val ref = sr.getServiceReference(classOf[TokenStore].getName())
        val store = sr.getService(ref).asInstanceOf[TokenStore]
        f(store)
      }
    }
  }

  def login(user: String, password : String) : Try[Subject] =  Try {
    val lc = new LoginContext("Test", new PasswordCallbackHandler(user, password.toCharArray()))
    lc.login()
    lc.getSubject()
  }

  "The token store" - {

    "should start empty" in {

      withTokenStore { store =>
        store.listTokens() should be (empty)
      }
    }

    "should allow to create a new token" in {

      withTokenStore { store =>
        val subj = login("andreas", "mysecret").get
        val token : Token = store.newToken(subj, None).get

        token.id should startWith("andreas")
        token.expiresAt should be (0)

        token.permissions.granted.size should be (2)
        token.permissions.granted.find(_.permissionClass == Some("admins")) should be (defined)
        token.permissions.granted.find(_.permissionClass == Some("blended")) should be (defined)

        store.listTokens().size should be(1)
      }
    }

    "should allow to get and delete an existing token" in {

      withTokenStore { store =>
        val subj = login("andreas", "mysecret").get
        val token = store.newToken(subj, None).get

        token.id should startWith("andreas")
        token.expiresAt should be (0)

        val token2 = store.getToken(token.id).get

        assert(token === token2)

        token2.permissions.granted.size should be (2)
        token2.permissions.granted.find(_.permissionClass == Some("admins")) should be (defined)
        token2.permissions.granted.find(_.permissionClass == Some("blended")) should be (defined)

        val token3 = store.removeToken(token.id).get
        assert(token === token3)

        store.listTokens() should be(empty)
      }
    }
  }
}
