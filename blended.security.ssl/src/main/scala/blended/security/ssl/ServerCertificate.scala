package blended.security.ssl

import java.security.{ KeyPair, Principal }
import java.security.cert.{ Certificate, X509Certificate }

import scala.util.Try

import blended.util.logging.Logger

case class ServerCertificate private (
  keyPair: KeyPair,
  chain: List[X509Certificate]) {

  override def toString: String = chain.map { c=>
    X509CertificateInfo(c).toString
  }.mkString("\n", "\n", "")
}

case object ServerCertificate {

  private[this] val log = Logger[ServerCertificate]

  def create(keyPair: KeyPair, chain: List[Certificate]): Try[ServerCertificate] = Try {

    val sortedChain: List[X509Certificate] = {

      val x509Certs = chain.map(_.asInstanceOf[X509Certificate])

      if (chain.length <= 1) {
        x509Certs
      } else {

        def signedBy(issuer: Principal): (X509Certificate => Boolean) = c =>
          !c.getIssuerDN().equals(c.getSubjectDN()) && c.getIssuerDN().equals(issuer)

        def sort(remaining: List[X509Certificate])(sorted: List[X509Certificate]): List[X509Certificate] = {
          remaining match {
            case Nil => sorted
            case rest =>
              rest.find(signedBy(sorted.head.getSubjectDN())) match {
                case None => sys.error(s"No signed certificate found for certificate [${X509CertificateInfo(sorted.head)}]")
                case Some(next) =>
                  if (sorted.contains(next)) {
                    sys.error("Certificate chain must not contain circular references")
                  }
                  log.debug(s"Next certificate is [${X509CertificateInfo(next)}]")
                  sort(remaining.filter(c => !c.equals(next)))(next :: sorted)
              }
          }
        }

        x509Certs.find { c => c.getSubjectDN().equals(c.getIssuerDN()) } match {
          case None => sys.error("Certificate chain must have a self signed certificate.")
          case Some(root) => {
            log.debug(s"Root Certificate is [${X509CertificateInfo(root)}]")
            sort(x509Certs.filter(c => !c.equals(root)))(root :: Nil)
          }
        }
      }
    }

    ServerCertificate(keyPair, sortedChain)
  }
}

