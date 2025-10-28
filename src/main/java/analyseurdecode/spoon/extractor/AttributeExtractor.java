package analyseurdecode.spoon.extractor;


import analyseurdecode.model.AttributeInfo;
import spoon.reflect.declaration.CtField;

public class AttributeExtractor {

    public AttributeInfo extractAttributeInfo(CtField<?> field) {
        try {
            return new AttributeInfo(field.getSimpleName(), field.getType().getSimpleName());
        } catch (Exception e) {
            return null;
        }
    }
}
