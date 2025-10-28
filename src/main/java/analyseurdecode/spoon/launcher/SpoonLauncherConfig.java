package analyseurdecode.spoon.launcher;

import spoon.Launcher;

public class SpoonLauncherConfig {

    public static Launcher createConfiguredLauncher(String projectPath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(projectPath);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setCommentEnabled(false);
        return launcher;
    }
}
