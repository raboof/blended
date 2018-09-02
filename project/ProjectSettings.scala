import sbt._
import sbt.Keys._

case class ProjectSettings(
  prjName: String,
  desc: String,
  osgi : Boolean = true
) {

  def libDependencies : Seq[ModuleID] = Seq()

  def bundle : BlendedBundle = BlendedBundle(
    bundleSymbolicName = prjName,
    exportPackage = Seq(prjName),
    privatePackage = Seq(prjName + ".internal")
  )

  final protected def sbtBundle : Option[BlendedBundle] = if (osgi) {
    Some(bundle)
  } else {
    None
  }

  def settings : Seq[Setting[_]] =  {
    val osgiSettings : Seq[Setting[_]] = sbtBundle.toSeq.flatMap { _.osgiSettings }

    Seq(
      name := prjName,
      description := desc,
      libraryDependencies ++= libDependencies,
    ) ++ osgiSettings
  }
}
