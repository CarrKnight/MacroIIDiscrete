/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities;

import agents.EconomicAgent;
import agents.people.Person;
import agents.firm.Firm;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import goods.GoodType;
import model.MacroII;
import org.apache.commons.collections15.Transformer;
import sim.portrayal.Inspector;
import sim.util.Bag;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> This is another component of a market inspector gui.
 * <p/> The idea is that this will show all exchanges in a network
 * <p/> I assume that if gui is off this doesn't even get instantiated!
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-08
 * @see
 */
public class ExchangeNetwork {

    //this chooses the vertex color of the network's vertices
    Transformer<EconomicAgent,Paint> vertexPainter = new Transformer<EconomicAgent, Paint>() {
        @Override
        public Paint transform(EconomicAgent hasInventory) {

            if(hasInventory instanceof Person)
                return Color.blue;
            if(hasInventory instanceof DummyBuyer || hasInventory instanceof DummySeller)
                return Color.BLACK;
            if(hasInventory instanceof Firm)
                return Color.red;
            else{
                assert false : hasInventory.getClass().getSimpleName(); //this shouldn't happen
                return Color.black;
            }

        }
    };


    /**
     * This is the graph that contains the information on the exchanges!.
     */
    private Graph<EconomicAgent,InventoryEdge> inventoryGraph;

    /**
     * The object that draws/visualize the graph
     */
    private VisualizationViewer<EconomicAgent,InventoryEdge> vv; //does the graph


    /**
     * The good type you are modeling
     */
    private final GoodType type;

    /**
     * Whenever this is true we have queued a "repaint" call to the
     */
    private boolean repaintFlag = false;

    /**
     * Create the network: both the structure and the visualization
     */
    public ExchangeNetwork(GoodType type) {

        assert MacroII.hasGUI(); //don't bother with any of this if there is no inventory
        //create the graph!
        inventoryGraph = new DirectedSparseGraph<>();
        //record your good type
        this.type = type;


        //create the layout used for vertices
        Layout<EconomicAgent,InventoryEdge> layout = new SpringLayout<EconomicAgent, InventoryEdge>(inventoryGraph);
        //new DAGLayout<EconomicAgent, InventoryEdge>(inventoryGraph);
        layout.setSize(new Dimension(600,600)); //set size
        //create the viewer
        vv = new VisualizationViewer<>(layout);
        //tell it how to color the vertices
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPainter);
        //tell it to print the name of the vertices
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<EconomicAgent>());
        //call to string to label edges
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<InventoryEdge>()); //edges will have name
        //have the thickness of lines grow
        vv.getRenderContext().setEdgeStrokeTransformer(InventoryEdge.edgeStrokeTransformer);


        //now set up the visualization so that clicking on an agent gives you info about him
        //set up the mouse to select
        ModalGraphMouse mouse = new DefaultModalGraphMouse<>();
        mouse.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(mouse);
        //make selection communicate to Mason through inspectors
        vv.getPickedVertexState().addItemListener(new ItemListener() {
            @Override
            //when you have a selection, this logEvent is fired
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) //if e is selected
                {
                    EconomicAgent agent = (EconomicAgent) e.getItem();             //cast it to agent
                    Inspector inspector = agent.getInspector(MacroII.getGUI()); //notice how you get back the outer instance

                    //now for the moronic, let's talk to MASON in java 1.3 language because computer science people are too smart
                    //to use generics or arraylists
                    Bag inspectors = new Bag(1); inspectors.add(inspector);
                    Bag names = new Bag(1); names.add(agent.toString());
                    MacroII.getGUI().controller.setInspectors(inspectors, names);
                }
            }
        }
        );

    }

    /**
     * Add an agent (a vertex) to the graph.
     * @param agent a new agent
     * @return true if the agent was correctly added (usually it's false if it was already there)
     */
    public boolean addAgent(EconomicAgent agent){

        return inventoryGraph.addVertex(agent);

    }



    /**
     * The receiver should call this in its receive method to show the exchange in the graph
     * @param sender the agent giving up the goods
     * @param receiver the agent receiving the goods
     * @param g what kind of good?
     * @param quantity how much is exchanged?
     */
    public void registerInventoryDelivery(EconomicAgent sender, EconomicAgent receiver,
                                          GoodType g, int quantity)
    {
        //if graphing is not active, don't bother
        assert MacroII.hasGUI();
        assert g == type;


        //the thing here is this: if there is no link between the two we need to create it, otherwise we need to increase its weight
        InventoryEdge alreadyExistingLink = null;

        //get all the out edges going to the receiver
        Collection<InventoryEdge> outEdges = inventoryGraph.getInEdges(receiver);


        //we need to find the edge that: comes from the sender and has the same goodType!
        for(InventoryEdge e : outEdges)
        {
            //if right source and right good type
            if(inventoryGraph.isSource(sender,e))
            {
                assert e.getType() == type;
                alreadyExistingLink = e;
                break;
            }


        }

        //if the link already exists, increase its quantity
        if(alreadyExistingLink != null)
        {
            alreadyExistingLink.setHowMuch(alreadyExistingLink.getHowMuch() + quantity);
            assert !type.isLabor(); //labor in general is just one good
        }
        //otherwise create a new link
        else
        {
            inventoryGraph.addEdge(new InventoryEdge(g,quantity),sender,
                    receiver, EdgeType.DIRECTED);
        }

        queueRepaint();

    }


    /**
     * At the end of the week the graph is cleared of all edges (unless the good type is labor!!)
     */
    public void weekEnd(){

        if(!type.isLabor())
        {
            //create a new list
            Collection<InventoryEdge> edges = new LinkedList<>();
            edges.addAll(inventoryGraph.getEdges()); //add all edges to this list
            //for all inventory edges
            for(InventoryEdge e :edges )                                          //remove them!
                //remove!
                inventoryGraph.removeEdge(e);
            //repaint so we can see that they disappeared!
            queueRepaint();

        }
    }

    /**
     * Queues up a repaint call. Notice that it is synchronized.
     */
    private synchronized void queueRepaint(){

        if(repaintFlag) //if it's already queued ignore the call
            return;
        //set repaint flag true
        repaintFlag = true;
        //get in the gui thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //repaint!
                vv.repaint();
                //clear the flag!
                repaintFlag = false;
            }
        });


    }


    /**
     * Removes one <code>agent</code> from this graph.
     * As a side effect, removes any edges <code>e</code> incident to <code>vertex</code> if the
     * removal of <code>vertex</code> would cause <code>e</code> to be incident to an illegal
     * number of vertices.  (Thus, for example, incident hyperedges are not removed, but
     * incident edges--which must be connected to a vertex at both endpoints--are removed.)
     *
     * <p>Fails under the following circumstances:
     * <ul>
     * <li/><code>vertex</code> is not an element of this graph
     * <li/><code>vertex</code> is <code>null</code>
     * </ul>
     *
     * @param agent the vertex to remove
     * @return <code>true</code> if the removal is successful, <code>false</code> otherwise
     */
    public boolean removeAgent(EconomicAgent agent) {
        return inventoryGraph.removeVertex(agent);
    }

    /**
     * Returns true if this graph's vertex collection contains <code>vertex</code>.
     * @param agent the agent whose presence is being queried
     * @return true iff this graph contains a vertex <code>vertex</code>
     */
    public boolean containsAgent(EconomicAgent agent) {
        return inventoryGraph.containsVertex(agent);
    }

    public VisualizationViewer<EconomicAgent, InventoryEdge> getVisualization() {
        return vv;
    }

    /**
     * Call this if all the edges between two nodes to go
     * @param node1 the first vertex
     * @param node2 the second vertex
     * @return true if any edge was removed
     */
    public boolean removeEdges(EconomicAgent node1, EconomicAgent node2)
    {
        boolean anyEdgeRemoved = false;

        //remove all edges between two nodes
        Iterable<InventoryEdge> inEdges =  new ArrayList<>(inventoryGraph.getInEdges(node1)); //of all the edges that go in node1
        for(InventoryEdge e : inEdges)
        {
            //if it goes there, destroy it!
            if(inventoryGraph.isSource(node2,e))  //if they originate from node2
            {
                inventoryGraph.removeEdge(e);
                anyEdgeRemoved=true;
            }
        }

        //do it again for the other side
        inEdges =  new ArrayList<>(inventoryGraph.getInEdges(node2));  //of all the edges that go in node2
        for(InventoryEdge e : inEdges)
        {
            //if it goes there, destroy it!
            if(inventoryGraph.isSource(node1,e))  //if they originate from node1
            {
                inventoryGraph.removeEdge(e);
                anyEdgeRemoved = true;
            }
        }
        //if any edge was removed, repaint when possible
        if(anyEdgeRemoved)
            queueRepaint();
        return anyEdgeRemoved;

    }
}
