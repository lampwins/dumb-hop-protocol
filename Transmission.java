import java.util.ArrayList;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Transmission {

    private Node source;
    private Node destination;
    private int ttl;
    private ArrayList<Connection> connections = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private int saturation;

    public Transmission(Node source, Node destination, int ttl, int saturation){
        this.source = source;
        this.destination = destination;
        this.ttl = ttl;

    }

    public Node getSource(){
        return this.source;
    }

    public Node getDestination(){
        return this.destination;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Adjacency a){
        connections.add(a.getConnection());
        nodes.add(a.getNode());
    }

    public boolean hasConnection(Connection c){
        return connections.contains(c);
    }

    public boolean hasNode(Node n){
        return nodes.contains(n);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getSaturation(){
        return saturation;
    }
}
