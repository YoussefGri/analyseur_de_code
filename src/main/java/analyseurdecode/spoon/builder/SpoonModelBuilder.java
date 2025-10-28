package analyseurdecode.spoon.builder;

import spoon.Launcher;
import spoon.reflect.CtModel;

public class SpoonModelBuilder {

    public static CtModel buildModel(Launcher launcher) {
        launcher.buildModel();
        return launcher.getModel();
    }
}
