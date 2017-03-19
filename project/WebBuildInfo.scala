import java.util.Base64

import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.SbtGit
import org.eclipse.jgit.revwalk.RevWalk
import sbt.Keys.{name, version}
import sbt.{AutoPlugin, Plugins, SettingKey}
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoBuildNumber, buildInfoKeys}

/**
  * https://github.com/sbt/sbt-git/pull/114
  */
object WebBuildInfo extends AutoPlugin {

  override def requires: Plugins = BuildInfoPlugin

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] = {
    List(
      autoImport.gitCommitDescription := {
        com.typesafe.sbt.SbtGit.GitKeys.gitReader.value.withGit {
          interface =>
            for {
              sha <- git.gitHeadCommit.value
              interface <- Option(interface).collect {
                case i: com.typesafe.sbt.git.JGit => i
              }
              ref <- Option(interface.repo.resolve(sha))
              message <- {
                val walk = new RevWalk(interface.repo)
                try Option(walk.parseCommit(ref.toObjectId)).flatMap(commit =>
                  Option(commit.getFullMessage))
                finally walk.dispose()
              }
            } yield message
        }
      }.map { str =>
        Base64.getEncoder.encodeToString(str.getBytes("UTF-8"))
      },
      buildInfoKeys := Seq[BuildInfoKey](
        name,
        version,
        buildInfoBuildNumber,
        git.gitHeadCommit,
        autoImport.gitCommitDescription
      )
    )
  }

  object autoImport {

    val gitCommitDescription =
      SettingKey[Option[String]]("gitCommitDescription", "Base64-encoded!")

  }

}
