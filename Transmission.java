import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Transmission {

    private Node source;
    private Node destination;
    private int ttl;
    private int totalTtl;
    private ArrayList<Connection> connections = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private Stack<Adjacency> adjacencyStack = new Stack<>();
    private int saturation;
    private double avgSaturationStart = 0;
    private double avgSaturationMiddle = 0;
    private double avgSaturationEnd = 0;

    public Transmission(Node source, Node destination, int ttl, int saturation){
        this.source = source;
        this.destination = destination;
        this.ttl = this.totalTtl = ttl;
        this.saturation = saturation;
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
        adjacencyStack.push(a);
    }

    public Adjacency backTrackLoop(Node n){

        Adjacency current = adjacencyStack.peek();

        while(!adjacencyStack.peek().getNode().equals(n)){
            current = adjacencyStack.pop();
        }

        return current;
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

    public void applySaturation(){
        for(Iterator<Adjacency> i = adjacencyStack.iterator(); i.hasNext(); ){
            i.next().getConnection().increaseSaturation(saturation);
        }
    }

    public void removeSaturation(){
        for(Iterator<Adjacency> i = adjacencyStack.iterator(); i.hasNext(); ){
            i.next().getConnection().decreaseSaturation(saturation);
        }
    }

    public double getAverageSaturation(){
        double totalSaturation = 0;
        for( Connection c: connections){
            totalSaturation += c.getSaturation();
        }
        return totalSaturation / connections.size();
    }

    public double getAvgSaturationStart() {
        return avgSaturationStart;
    }

    public void setAvgSaturationStart(double avgSaturationStart) {
        this.avgSaturationStart = avgSaturationStart;
    }

    public double getAvgSaturationMiddle() {
        return avgSaturationMiddle;
    }

    public void setAvgSaturationMiddle(double avgSaturationMiddle) {
        this.avgSaturationMiddle = avgSaturationMiddle;
    }

    public double getAvgSaturationEnd() {
        return avgSaturationEnd;
    }

    public void setAvgSaturationEnd(double avgSaturationEnd) {
        this.avgSaturationEnd = avgSaturationEnd;
    }

    public int getTotalTtl() {
        return totalTtl;
    }
}
