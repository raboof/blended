object BlendedUtilLogging extends ProjectHelper {

  private[this] val helper = new ProjectSettings(
    "blended.util.logging",
     "Logging utility classes to use in other bundles."
  ) {

    override val libDeps = Seq(
      Dependencies.slf4j
    )
  }
  override val project  = helper.baseProject
}

