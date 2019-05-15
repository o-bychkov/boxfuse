import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger


/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

project {
    vcsRoot(BoxFuseVCS)
    sequence{
        build(Build)
        build(Deploy)
    }
}

object Build : BuildType({
    name = "Build"
    artifactRules = "%teamcity.build.default.checkoutDir%/target/*.jar => /opt/%teamcity.build.default.checkoutDir%/helo.jar"
    vcs {
        root(BoxFuseVCS)
    }
    steps {
        maven {
            goals = "jar:jar"
            // useOwnLocalRepo = true
        }
    }
    triggers {
        vcs {
        }
    }

    requirements {
        equals("system.agent.name", "web-0")
    }
})

object Deploy : BuildType ({
    name = "Deploy"
    steps {
        step {
            name = "Deploy"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("teamcitySshKey", "id_rsa")
            param("jetbrains.buildServer.deployer.sourcePath", "/usr/share/app/*.jar")
            param("jetbrains.buildServer.deployer.targetUrl", "192.168.0.112:/opt")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
    }
    triggers {
        finishBuildTrigger {
            buildType = "${Build.id}"
            successfulOnly = true
        }
    }
    requirements {
        equals("system.agent.name", "web-0")
    }
})

object BoxFuseVCS : GitVcsRoot ({
    name = "BoxFuseVCS"
    url = "https://github.com/o-bychkov/boxfuse"
})
