import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Old {

    public static void main(String[] args){


        Writer transmissionWriter = new Writer();
        transmissionWriter.append("id,source,destination,ttl,saturation,hops,avg_sat_start,avg_sat_middle,avg_sat_end\n");

        Network network = new Network();

        Node[] routerTopology = new Node[6];
        Node[] hostTopology = new Node[25];

        //provision routers
        int idCounter = 0;
        Router core = new Router(true);
        routerTopology[0] = core;
        core.setId(0, 0);

        for(int i = 1; i <= 5; i++){
            routerTopology[i] = new Router(false);
            routerTopology[i].setId(0,i);
            network.addConnection(new Connection(core, routerTopology[i]));
            for(int j = 0; j < 5; j++){
                hostTopology[(i - 1) * 5 + j] = new Host();
                hostTopology[(i - 1) * 5 + j].setId(0, (i - 1) * 5 + j);
                network.addConnection(new Connection(routerTopology[i], hostTopology[(i - 1) * 5 + j]));
            }
        }

        //System.out.print(network);
        //System.exit(1);

        //begin simulation
        int iterations = 100,i = 0;

        ArrayList<Transmission> transmissions = new ArrayList<>();

        while(i++ < iterations){

            System.out.println("---Beginning Transmission " + (i - 1) + "---");

            Random r = new Random();

            Node source = hostTopology[r.nextInt(25 - 1)];
            Node destination = hostTopology[r.nextInt(25 - 1)];

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

            boolean notThereYet = true;

            while(notThereYet) {

                System.out.println("\t\tNext hop selection for: " + currentNode);

                Collection<Adjacency> connectedNodes = network.getAdjacencies(currentNode);
                ArrayList<Adjacency> candidateAdjacencies = new ArrayList<>();

                if(currentNode.equals(core)){
                    int group = destination.getCol() / 5;
                    Iterator<Adjacency> itr = network.getAdjacencies(core).iterator();
                    while(itr.hasNext()){
                        Adjacency a = itr.next();
                        if(a.getNode().equals(routerTopology[group + 1])){
                            candidateAdjacencies.add(a);
                        }
                    }
                }else {

                    for (Adjacency a : connectedNodes) {
                        //leave out adjacencies that would result in a routing loop
                        if (!transmission.hasNode(a.getNode())) {
                            //check if direct connect is available otherwise only add routers
                            if (a.getNode().getClass().equals(Router.class) || a.getNode().equals(destination)) {
                                candidateAdjacencies.add(a);

                                System.out.println("\t\t\tAdding to candidate list: " + a.getNode());
                            }
                        }
                    }
                }

                if(candidateAdjacencies.size() == 0){
                    //failed to find a path
                    System.out.println("Failed to select path");
                    break;
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
