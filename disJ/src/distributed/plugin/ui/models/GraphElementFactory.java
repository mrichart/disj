/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.runtime.Graph;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.topologies.CompletGraph;
import distributed.plugin.ui.models.topologies.GenericGraph;
import distributed.plugin.ui.models.topologies.HyperCube;
import distributed.plugin.ui.models.topologies.ITopology;
import distributed.plugin.ui.models.topologies.Mesh;
import distributed.plugin.ui.models.topologies.Ring;
import distributed.plugin.ui.models.topologies.Torus;
import distributed.plugin.ui.models.topologies.Tree;

/**
 * @author Me
 * 
 * Create an element of graph
 */
public class GraphElementFactory implements CreationFactory {

    private Shell shell;

    private GraphElement graphElement;

    private String template;

    /**
     * 
     * @param GraphElement
     *            a correspoding graph to this factory
     * @param template
     *            a type of object that need to be created
     */
    public GraphElementFactory(GraphElement graphElement, String template) {
        this.shell = null;
        this.graphElement = graphElement;
        this.template = template;
    }


    /*
     * Get a graph corresponding to this factory
     */
    private Graph getGraph() {
        return this.graphElement.getGraph();
    }

    public void setGraphElement(GraphElement element) {
        this.graphElement = element;
        this.shell = this.graphElement.getShell();
    }

    public NodeElement createNodeElement() {
        int id = this.getGraph().getCurrentNodeId();
        return new NodeElement(this.graphElement.getGraphId(), "n" + id);
    }

    public UniLinkElement createUniLinkElement() {
        int id = this.getGraph().getCurrentEdgeId();
        return new UniLinkElement(this.getGraph().getId(),"e" + id);
    }

    public BiLinkElement createBiLinkElement() {
        int id = this.getGraph().getCurrentEdgeId();
        return new BiLinkElement(this.getGraph().getId(), "e" + id);
    }

    /**
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject() {
        if(this.shell == null){
            this.shell = this.graphElement.getShell();
        }
        
        if (template.equals(IGraphEditorConstants.TEMPLATE_NODE))
            return this.createNodeElement();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_UNI_LINK)) {
            return this.createUniLinkElement();

        } else if (template.equals(IGraphEditorConstants.TEMPLATE_BI_LINK)) {
            return this.createBiLinkElement();

        } else if (template.equals(IGraphEditorConstants.TEMPLATE_RING)) {
            return this.createRing();

        } else if (template.equals(IGraphEditorConstants.TEMPLATE_TREE))
            return this.createTree();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_MESH))
            return this.createMesh();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_COMPLETE))
            return this.createComplete();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_HYPER_CUBE))
            return this.createHyperCube();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_TORUS_1))
            return this.createTorus1();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_TORUS_2))
            return this.createTorus2();

        else if (template.equals(IGraphEditorConstants.TEMPLATE_GENERIC))
            return this.createGeneric();
        
        else if (template.equals(IGraphEditorConstants.TEMPLATE_GENERIC_C))
            return this.createGeneric_C();

        else
            return null;

    }

    private ITopology createRing(){
        ITopology ring = new Ring(this, this.shell);
        ring.createTopology();
        return ring;
    }
    
    private ITopology createTree() {
        ITopology tree = new Tree(this, this.shell);
        tree.createTopology();
        return tree;
    }

    private ITopology createMesh() {
        ITopology mesh = new Mesh(this, this.shell);
        mesh.createTopology();
        return mesh;
    }

    private ITopology createComplete() {
        ITopology comp = new CompletGraph(this, this.shell);
        comp.createTopology();
        return comp;
    }

    private ITopology createHyperCube() {
        ITopology hype = new HyperCube(this, this.shell);
        hype.createTopology();
        return hype;
    }

    private ITopology createTorus1() {
        ITopology tor = new Torus(this, this.shell, IGraphEditorConstants.TORUS_1);
        tor.createTopology();
        return tor;
    }

    private ITopology createTorus2() {
        ITopology tor = new Torus(this, this.shell, IGraphEditorConstants.TORUS_2);
        tor.createTopology();
        return tor;
    }

    private ITopology createGeneric() {
        ITopology gen = new GenericGraph(this, this.shell);
        gen.createTopology();
        return gen;
    }
    
    private ITopology createGeneric_C() {
        ITopology gen = new GenericGraph(this, this.shell,IGraphEditorConstants.GENERIC_C);
        gen.createTopology();
        return gen;
    }

    /**
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return this.template;
    }

}
