import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Main {

    public static void main(String[] args){


        Writer transmissionWriter = new Writer();
        transmissionWriter.append("id,source,destination,ttl,saturation,hops,avg_sat_start,avg_sat_middle,avg_sat_end\n");

        int collums = 5;
        int rows = 5;

        Network network = new Network();

        Node[][] routerTopology = new Node[rows][collums];
        Node[][] hostTopology = new Node[rows - 1][collums - 1];

        //provision routers
        int idCounter = 0;
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < collums; j++){
                Router r = new Router((i == 0 || i == rows - 1));
                r.setId(i, j);
                routerTopology[i][j] = r;
            }
        }

        //provision hosts
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < collums - 1; j++){
                Host h = new Host();
                h.setId(i, j);
                hostTopology[i][j] = h;
            }
        }

        //create router connections
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < collums; j++){

                //not last collum
                if(!(j == collums - 1)) {
                    //this router and the one next to it
                    Connection c = new Connection(routerTopology[i][j], routerTopology[i][j + 1]);
                    network.addConnection(c);
                }

                //not first row
                if(!(i == 0)){
                    //this router and the one above it
                    Connection c = new Connection(routerTopology[i][j], routerTopology[i - 1][j]);
                    network.addConnection(c);
                }

            }
        }

        //create host connections
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < collums - 1; j++){

                //this this host and surrounding routers
                Connection c1 = new Connection(hostTopology[i][j], routerTopology[i][j]);
                Connection c2 = new Connection(hostTopology[i][j], routerTopology[i + 1][j]);
                Connection c3 = new Connection(hostTopology[i][j], routerTopology[i][j + 1]);
                Connection c4 = new Connection(hostTopology[i][j], routerTopology[i + 1][j + 1]);
                network.addConnection(c1);
                network.addConnection(c2);
                network.addConnection(c3);
                network.addConnection(c4);

            }
        }


        //begin simulation
        int iterations = 100,i = 0;

        ArrayList<Transmission> transmissions = new ArrayList<>();

        while(i++ < iterations){

            System.out.println("---Beginning Transmission " + (i - 1) + "---");

            Random r = new Random();

            Node source = hostTopology[r.nextInt(rows - 1)][r.nextInt(collums - 1)];
            Node destination = hostTopology[r.nextInt(rows - 1)][r.nextInt(collums - 1)];

            System.out.println("\tSource: " + source);
            System.out.println("\tDestination: " + destination);

            if(source.equals(destination)){
                //try again if the same
                System.out.println("Aborting transmission; Source equals destination");
                i--;
                continue;
            }

            Node currentNode = source;
            Transmission transmission = new Transmission(source, destination, r.nextInt(iterations / 2) | 1, r.nextInt(100));

            int lateralDif = destination.getCol() - currentNode.getCol();
            int verticalDif = destination.getRow() - currentNode.getRow();

            System.out.println("\t\tlat diff: " + lateralDif);
            System.out.println("\t\tvert diff: " + verticalDif);

            boolean notThereYet = true;

            while(notThereYet) {

                System.out.println("\t\tNext hop selection for: " + currentNode);

                Collection<Adjacency> connectedNodes = network.getAdjacencies(currentNode);
                ArrayList<Adjacency> candidateAdjacencies = new ArrayList<>();

                for(Adjacency a: connectedNodes){
                    //leave out adjacencies that would result in a routing loop
                    if(!transmission.hasNode(a.getNode())){
                        //check if direct connect is available otherwise only add routers
                        if(a.getNode().getClass().equals(Router.class) || a.getNode().equals(destination)) {
                            candidateAdjacencies.add(a);

                            System.out.println("\t\t\tAdding to candidate list: " + a.getNode());
                        }
                    }
                }

                if(candidateAdjacencies.size() == 0){
                    //failed to find a path
                    System.out.println("Failed to select path");
                    break;
                }


                System.out.println("\t\t\tStarting lateral scrubbing");

                //lateral scrubbing
                for(Iterator<Adjacency> iterator = candidateAdjacencies.iterator(); iterator.hasNext();) {

                    Adjacency a = iterator.next();

                    if(!iterator.hasNext()){
                        //size is 1
                        break;
                    }

                    if(a.getNode().equals(destination)){
                        //this is a direct connect, prefer it by skipping
                        continue;
                    }

                    if(lateralDif >= 0){
                        //can move right if needed
                        //remove all connections to the left
                        if(a.getNode().getCol() < currentNode.getCol()){
                            iterator.remove();

                            System.out.println("\t\t\tRemoving from candidate list: " + a.getNode());
                        }
                    }else{
                        //must move left
                        //remove all right moves
                        if(a.getNode().getCol() > currentNode.getCol()){
                            iterator.remove();

                            System.out.println("\t\t\tRemoving from candidate list: " + a.getNode());
                        }
                    }
                }


                System.out.println("\t\t\tStarting vertical scrubbing");

                //vertical scrubbing
                for(Iterator<Adjacency> iterator = candidateAdjacencies.iterator(); iterator.hasNext();) {

                    Adjacency a = iterator.next();

                    if(!iterator.hasNext()){
                        //size is 1
                        break;
                    }

                    if(a.getNode().equals(destination)){
                        //this is a direct connect, prefer it by skipping
                        continue;
                    }

                    if(verticalDif >= 0){
                        //can move down if needed
                        //remove all connections up
                        if(a.getNode().getRow() < currentNode.getRow()){
                            iterator.remove();

                            System.out.println("\t\t\tRemoving from candidate list: " + a.getNode());
                        }
                    }else{
                        //must move up
                        //remove all moves down
                        if(a.getNode().getRow() > currentNode.getRow()){
                            iterator.remove();

                            System.out.println("\t\t\tRemoving from candidate list: " + a.getNode());
                        }
                    }
                }

                Adjacency selectedPath = getAdjacencyWithSmallestSaturation(candidateAdjacencies, destination);
                transmission.addConnection(selectedPath);
                currentNode = selectedPath.getNode();

                if (currentNode.equals(destination)) {
                    notThereYet = false;
                }
            }

            //iteration cleanup
            //iterateTransmissions(transmissions);

            //apply saturation value to each connection (start the transmission)
            transmission.applySaturation();

            //add newest transmission
            transmissions.add(transmission);

            transmission.setAvgSaturationStart(transmission.getAverageSaturation());
        }

        //while(iterateTransmissions(transmissions) > 0);

        int itr = 1;
        for(Transmission t: transmissions){
            //id,source,destination,ttl,saturation,hops,avg_sat_start,avg_sat_middle,avg_sat_end
            transmissionWriter.append(itr++ + ",\"" + t.getSource() + "\",\"" + t.getDestination() +
                    "\"," + t.getTotalTtl() + "," + t.getSaturation() + "," + t.getConnections().size() +
                    "," + t.getAvgSaturationStart() + "," + t.getAvgSaturationMiddle() + "," + t.getAvgSaturationEnd() + "\n");
        }

        transmissionWriter.close();
    }

    public static int iterateTransmissions(ArrayList<Transmission> transmissions){

        int activeTransmissions = 0;
        for(Transmission t: transmissions){
            if(t.getTtl() > 0) {
                if (t.getTtl() == Math.floor(t.getTotalTtl() * 0.5)) {
                    t.setAvgSaturationMiddle(t.getAverageSaturation());
                }
                t.setTtl(t.getTtl() - 1);
                if (t.getTtl() == 0) {
                    t.setAvgSaturationEnd(t.getAverageSaturation());
                    t.removeSaturation();
                    System.out.println("Transmission between " +
                                    t.getSource().getClass().toString().replaceAll("class ", "") +
                                    " " + t.getSource() + " and " +
                                    t.getDestination().getClass().toString().replaceAll("class ", "") +
                                    " " + t.getDestination() + " Complete " +
                                    "- " + t.getConnections().size() + " hops"
                    );
                }else{
                    activeTransmissions++;
                }
            }
        }
        return activeTransmissions;
    }

    public static Adjacency getAdjacencyWithSmallestSaturation(AbstractList<Adjacency> a, Node destination){

        if(a.size() == 1){
            return a.get(0);
        }

        System.out.println("\t\t\tSaturation selection:");

        Adjacency selection = a.get(0);
        for(int i = 0; i < a.size(); i++) {
            for (int j = 0; j < a.size(); j++) {

                System.out.println("\t\t\t\t" + a.get(i).getNode() + ": " + a.get(i).getConnection().getSaturation() +
                        " " + a.get(j).getNode() + ": " + a.get(j).getConnection().getSaturation());

                if (a.get(j).getConnection().getSaturation() < a.get(i).getConnection().getSaturation()){
                    selection = a.get(j);

                    System.out.println("\t\t\t\tChanged selection to " + a.get(j).getNode());
                }
            }
        }

        for(Adjacency b: a){
            if(b.getNode().equals(destination)){
                if(b.getConnection().getSaturation() == selection.getConnection().getSaturation()){
                    //if saturation is equal, choose the direct connect
                    selection = b;

                    System.out.println("\t\t\t\tChanged selection to " + selection.getNode() + " because of direct connect");
                }
            }
        }

        System.out.println("\t\t\t\tFinal selection: " + selection.getNode());

        return selection;
    }
}

class Writer{

    FileWriter writer;

    public Writer(){
        try {
            writer = new FileWriter("transmissions.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void append(String s){
        try {
            writer.append(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
