package blended.security.cert.internal

import java.io.{File, FileInputStream, FileOutputStream}
import java.security.KeyStore
import java.security.cert.X509Certificate

import blended.security.cert.CertificateProvider
import org.log4s._

import scala.util.{Success, Try}
import scala.concurrent.duration._

class CertificateController(cfg: CertControllerConfig, provider: CertificateProvider) {

  private[this] val log = getLogger
  private[this] lazy val keyStore = initKeyStore().get

  private[this] val millisPerDay : Long = 1.day.toMillis

  def checkCertificate() : Try[KeyStore] = {
    log.info(s"Checking Server Certificate for key store [${cfg.keyStore}]")
    checkAndUpdateCertificate(keyStore)
  }

  def serverKeyStore() : KeyStore = keyStore

  private[this] def checkAndUpdateCertificate(ks: KeyStore) : Try[KeyStore] = {

    if (ks.containsAlias(cfg.alias)) {
      log.info(s"Checking existing certificate with alias [${cfg.alias}]")
      val certInfo = X509CertificateInfo(ks.getCertificate(cfg.alias).asInstanceOf[X509Certificate])

      val remaining = certInfo.notAfter.getTime() - System.currentTimeMillis()

      if (remaining <= cfg.minValidDays * millisPerDay) {
        log.info(s"Certificate [${cfg.alias}] is about to expire in [${remaining % millisPerDay}] days...refreshing certificate.")
        throw new Exception("not implemented")
      } else {
        log.info(s"Server certificate [${cfg.alias}] is still vaild.")
        Success(ks)
      }
    } else {
      log.info(s"Certificate with alias [${cfg.alias}] does not yet exist")
      updateKeystore(ks)
    }
  }

  private[this] def updateKeystore(ks: KeyStore) : Try[KeyStore] = {
    log.info("Aquiring new certificate from certificate provider ...")
    val existing = Option(ks.getCertificate(cfg.alias).asInstanceOf[X509Certificate])
    val cert = provider.refreshCertificate(existing)
    log.info("Successfully obtained certificate from certificate provider.")
    ks.setKeyEntry(cfg.alias, cert.keyPair.getPrivate(), cfg.keyPass, cert.chain)

    val fos = new FileOutputStream(cfg.keyStore)
    try {
      ks.store(fos, cfg.storePass)
      log.info(s"Successfully written modified key store to [${cfg.keyStore}]")
    } finally {
      fos.close()
    }

    Success(ks)
  }

  private[this] def initKeyStore() : Try[KeyStore] = {

    log.debug("Initializing key store for server certificate ...")

    val ks = KeyStore.getInstance("PKCS12")
    val f = new File(cfg.keyStore)

    if (f.exists()) {
      val fis = new FileInputStream(f)
      try {
        ks.load(fis, cfg.storePass)
      } finally {
        fis.close()
      }
    } else {
      log.info(s"Creating empty key store  ...")
      ks.load(null, cfg.storePass)
    }

    Success(ks)
  }
}
