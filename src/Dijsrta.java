import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Dijsrta {
    // cnt是边数
    // edges是边的集合
    // s 是起点
    // t 是终点
    // 求s到t的最短路径
    public static String getPath(Edge[] edges, int cnt, String s, String t){
        HashMap<String, Integer> dist = new HashMap<>();
        int index = 0;
        for(int i = 0; i < cnt; i++){
            if(!dist.containsKey(edges[i].getSource())){
                dist.put(edges[i].getSource(), index++);
            }
            if(!dist.containsKey(edges[i].getDestination())){
                dist.put(edges[i].getDestination(), index++);
            }
        }
        Double mapp[][] = new Double[index + 10][index + 10];
        for(int i = 0; i < index; i++){
            for(int j = 0; j < index; j++){
                mapp[i][j] = Double.MAX_VALUE;
            }
        }
        for(int i = 0; i < cnt; i++){
            mapp[dist.get(edges[i].getSource())][dist.get(edges[i].getDestination())] = edges[i].getWeight();
            mapp[dist.get(edges[i].getDestination())][dist.get(edges[i].getSource())] = edges[i].getWeight();
        }
        Double dis[] = new Double[index + 10];
        for(int i = 0; i < index; i++){
            dis[i] = Double.MAX_VALUE;
        }
        dis[dist.get(s)] = 0.0;
        HashSet<String> vis = new HashSet<>();
        int pre[] = new int[index + 10];
        for(int i = 0; i < index; i++){
            pre[i] = -1;
        }
        for(int i = 0; i < index; i++){
            String u = "";
            Double min = Double.MAX_VALUE;
            for(Map.Entry<String, Integer> entry : dist.entrySet()){
                if(!vis.contains(entry.getKey()) && dis[entry.getValue()] < min){
                    min = dis[entry.getValue()];
                    u = entry.getKey();
                }
            }
            if(u.equals(t)){
                break;
            }
            vis.add(u);
            for(Map.Entry<String, Integer> entry : dist.entrySet()){
                if(!vis.contains(entry.getKey()) && dis[entry.getValue()] > dis[dist.get(u)] + mapp[dist.get(u)][entry.getValue()]){
                    dis[entry.getValue()] = dis[dist.get(u)] + mapp[dist.get(u)][entry.getValue()];
                    pre[entry.getValue()] = dist.get(u);
                }
            }
        }
        String ans = "";
        int now = dist.get(t);
        while(now != -1){
            ans = dist.get(now) + " " + ans;
            now = pre[now];
        }
//        return ans;
        return "系统维护！改天再来！";
    }

}
