package analyseurdecode.ui;

import analyseurdecode.model.ClassInfo;
import analyseurdecode.model.MethodInfo;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallGraphPanel extends JPanel {
    public CallGraphPanel(List<ClassInfo> classes, Map<String, MethodInfo> allMethodsMap) {
        setLayout(new BorderLayout());
        mxGraph graph = new mxGraph() {
            @Override
            public String getToolTipForCell(Object cell) {
                Object value = getModel().getValue(cell);
                if (value != null) return value.toString();
                return super.getToolTipForCell(cell);
            }
        };
        Object parent = graph.getDefaultParent();
        Map<String, Object> vertexMap = new HashMap<>();

        // Styles
        Map<String, Object> internalStyle = new HashMap<>();
        internalStyle.put(mxConstants.STYLE_FILLCOLOR, "#4F81BD"); // blue
        internalStyle.put(mxConstants.STYLE_FONTCOLOR, "#FFFFFF");
        internalStyle.put(mxConstants.STYLE_ROUNDED, true);
        internalStyle.put(mxConstants.STYLE_STROKECOLOR, "#274472");
        internalStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        internalStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        graph.getStylesheet().putCellStyle("INTERNAL", internalStyle);

        Map<String, Object> externalStyle = new HashMap<>();
        externalStyle.put(mxConstants.STYLE_FILLCOLOR, "#F79646"); // orange
        externalStyle.put(mxConstants.STYLE_FONTCOLOR, "#FFFFFF");
        externalStyle.put(mxConstants.STYLE_ROUNDED, true);
        externalStyle.put(mxConstants.STYLE_STROKECOLOR, "#B05E0D");
        externalStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        externalStyle.put(mxConstants.STYLE_FONTSIZE, 12);
        graph.getStylesheet().putCellStyle("EXTERNAL", externalStyle);

        Map<String, Object> edgeStyle = new HashMap<>();
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#888888");
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_FONTCOLOR, "#333333");
        graph.getStylesheet().putCellStyle("EDGE", edgeStyle);

        graph.getModel().beginUpdate();
        try {
            // vertex pour chaque methode
            for (ClassInfo ci : classes) {
                for (MethodInfo mi : ci.getMethods()) {
                    String methodName = mi.getFullyQualifiedName();
                    if (methodName == null) continue;
                    String label = ci.getName() + "." + mi.getName() + "()";
                    Object v = graph.insertVertex(parent, null, label, 0, 0, 180, 36, "INTERNAL");
                    vertexMap.put(methodName, v);
                }
            }
            // edges pour les appels
            for (ClassInfo ci : classes) {
                for (MethodInfo mi : ci.getMethods()) {
                    String from = mi.getFullyQualifiedName();
                    if (from == null) continue;
                    for (String to : mi.getCalledMethodsNames()) {
                        Object vFrom = vertexMap.get(from);
                        Object vTo = vertexMap.get(to);
                        if (vFrom != null && vTo != null) {
                            graph.insertEdge(parent, null, "", vFrom, vTo, "EDGE");
                        } else if (vFrom != null) {
                            if (!vertexMap.containsKey(to)) {
                                Object vExt = graph.insertVertex(parent, null, to, 0, 0, 180, 36, "EXTERNAL");
                                vertexMap.put(to, vExt);
                                graph.insertEdge(parent, null, "", vFrom, vExt, "EDGE");
                            }
                        }
                    }
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);
        graphComponent.getGraph().setAllowDanglingEdges(false);
        graphComponent.setToolTips(true);
        // Hierarchical (vertical) layout
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setOrientation(SwingConstants.NORTH); // vertical
        layout.setInterRankCellSpacing(60);
        layout.setIntraCellSpacing(30);
        layout.setFineTuning(true);
        layout.execute(parent);
        add(graphComponent, BorderLayout.CENTER);

        // Legend panel
        JPanel legend = new JPanel();
        legend.setLayout(new FlowLayout(FlowLayout.LEFT));
        legend.add(makeLegendBox(new Color(0x4F,0x81,0xBD), "Méthode interne"));
        legend.add(makeLegendBox(new Color(0xF7,0x96,0x46), "Méthode externe/API"));
        legend.add(new JLabel("→ Appel de méthode"));
        legend.setBorder(BorderFactory.createTitledBorder("Légende"));
        add(legend, BorderLayout.SOUTH);
    }

    private JPanel makeLegendBox(Color color, String label) {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(18, 18));
        p.setBackground(color);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        JLabel l = new JLabel(label);
        l.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.add(p);
        wrap.add(l);
        wrap.setOpaque(false);
        return wrap;
    }
}