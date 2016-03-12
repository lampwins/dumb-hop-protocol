import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sun.xml.internal.ws.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Network {

    private ArrayList<Connection> connections = new ArrayList<Connection>();
    private Multimap<Node, Adjacency> adjacencyMap = HashMultimap.create();

    public void addConnection(Connection c){

        connections.add(c);

        adjacencyMap.put(c.getNode1(), new Adjacency(c.getNode2(), c));
        adjacencyMap.put(c.getNode2(), new Adjacency(c.getNode1(), c));
    }

    public Collection<Adjacency> getAdjacencies(Node n){
        return adjacencyMap.get(n);
    }

    public String printConnections(){

        String s = "";

        for(Connection c: connections){
            Node n1 = c.getNode1();
            Node n2 = c.getNode2();

            s += n1.getClass().toString().replaceAll("class ", "") +
                    " [" + n1.getRow() + "," + n1.getCol() + "] -> " +
                    n2.getClass().toString().replaceAll("class ", "") +
                    " [" + n2.getRow() + "," + n2.getCol() + "]\n";
        }

        return s;
    }
}
