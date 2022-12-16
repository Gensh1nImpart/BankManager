public class Edge {
    String source;
    String destination;
    double weight;
    Edge(String source, String destination, double weight){
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public String getSource(){
        return source;
    }

    public String getDestination(){
        return destination;
    }

    public double getWeight(){
        return weight;
    }

    public String toString(){
        return source + " " + destination + " " + weight;
    }
}
