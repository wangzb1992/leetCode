import java.util.*;

public class xunBaoJava {
        //用于走步
        private int[] steps = {1, -1, 0, 0};
        //用于记录地图大小和地图，方便后续使用
        int m, n;
        char[][] graph;
        public int minimalSteps(String[] maze) {
            //先对地图做下处理，后续方便使用
            m = maze.length;
            n = maze[0].length();
            graph = new char[m][n];
            for (int i = 0; i < m; i++) graph[i] = maze[i].toCharArray();
            //先统计出所有石头和机关的位置，以及起、终点的位置
            List<int[]> stones = new ArrayList();
            List<int[]> buttons = new ArrayList();
            int x_start = -1, y_start = -1, x_target = -1, y_target = -1;
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    char c = graph[i][j];
                    if (c == 'M') buttons.add(new int[]{i, j});
                    else if (c == 'O') stones.add(new int[]{i, j});
                    else if (c == 'S') {
                        x_start = i;
                        y_start = j;
                    }else if (c == 'T') {
                        x_target = i;
                        y_target = j;
                    }
                }
            }
            int size_b = buttons.size();
            int size_s = stones.size();
            //先从起点bfs一下，得到起点到所有石头和终点的最短距离,加个计数表示需要找到的目标数
            // 这里需要找到起点到所有石头和终点的距离，所以cnt = stones.size() + 1；
            int[][] start_dis = bfs(x_start, y_start, size_s + 1);
            //剪个枝，如果没有机关,直接返回起点到终点的最短距离
            if (size_b == 0) return start_dis[x_target][y_target];
            //解决了从起点出发的最短距离问题，下面需要解决的是从各机关出发的问题，因为机关有多个，所以需要用三维数组
            int[][][] dis = new int[size_b][m][n];
            //遍历每一个机关并进行bfs
            for (int i = 0; i < dis.length; i++) {
                int[][] dis_i = bfs(buttons.get(i)[0], buttons.get(i)[1], stones.size() + 1);
                dis[i] = dis_i;
            }
            //最后一步准备工作，是将所有机关间的，以及起点到各机关的，各机关到终点的最短距离穷举出来并保存;
            //其中d[i][buttons.size()]为机关到起点最短距离
            //d[i][buttons.size() + 1]为机关到终点最短距离
            int[][] d = new int[size_b][size_b + 2];
            for (int[] row : d) {
                Arrays.fill(row, -1);
            }
            //遍历时记得排除特殊情况，即有从起点或者终点到达不了的机关，则永远无法寻宝成功
            for (int i = 0; i < d.length; i++) {
                //先填充机关到终点的最短距离,如果距离为-1，即不可达，则直接返回
                d[i][size_b + 1] = dis[i][x_target][y_target];
                if (d[i][size_b + 1] == -1) return -1;
                //再填充机关到起点的最短距离,起点和机关已确定，只需要遍历所有石头取最小值
                int x_stone, y_stone;
                for (int j = 0; j < size_s; j++) {
                    x_stone = stones.get(j)[0];
                    y_stone = stones.get(j)[1];
                    int d1 = dis[i][x_stone][y_stone];
                    int d2 = start_dis[x_stone][y_stone];
                    if (d1 != -1 && d2 != -1) {
                        if (d[i][size_b] == -1 || d[i][size_b] > d1 + d2) d[i][size_b] = d1 + d2;
                    }
                }
                if (d[i][size_b] == -1) return -1;
                //最后填充机关到机关间的最短距离,这里需要两层循环，第一层遍历目标机关，第二层改变中间石头
                //注意只需要遍历j > i就行，因为j到i和i到j是同一距离
                for (int j = i + 1; j < size_b; j++) {
                    for (int k = 0; k < size_s; k++) {
                        x_stone = stones.get(k)[0];
                        y_stone = stones.get(k)[1];
                        int d1 = dis[i][x_stone][y_stone];
                        int d2 = dis[j][x_stone][y_stone];
                        if (d1 != -1 && d2 != -1) {
                            if (d[i][j] == -1 || d[i][j] > d1 + d2) d[i][j] = d1 + d2;
                        }
                    }
                    d[j][i] = d[i][j];
                }
            }
            //准备工作做完，下面开始动态规划，具体思路参考官方答案
            int[][] dp = new int[1 << size_b][size_b];
            for (int[] row : dp) {
                Arrays.fill(row, -1);
            }
            //定义初始状态，即从没有机关被触发到触发一个
            for (int i = 0; i < size_b; i++) dp[1 << i][i] = d[i][size_b];
            //开始动态规划,mask表示当前状态
            for (int mask = 1; mask < dp.length - 1; mask++) {
                //需要遍历mask的每一位，把每一位‘1’作为当前状态所触发的那个机关
                for (int i = 0; i < size_b; i++) {
                    if ((mask & (1 << i)) != 0) {
                        //下一个可能的状态是任何一个没有被出发的机关被出发
                        for (int j = 0; j < size_b; j++) {
                            if ((mask & (1 << j)) == 0) {
                                int next = mask | (1 << j);
                                if (dp[next][j] == -1 || dp[next][j] > dp[mask][i] + d[i][j])
                                    dp[next][j] = dp[mask][i] + d[i][j];
                            }
                        }
                    }
                }
            }
            //所有机关都被触发完成，此刻最后一步就是从对应最后被触发的机关走到终点
            int res = Integer.MAX_VALUE;
            int final_state = (1 << size_b) - 1;
            for (int i = 0; i < size_b; i++) {
                res = Math.min(res, dp[final_state][i] + d[i][size_b + 1]);
            }
            return res;
        }
        //具体的bfs函数
        private int[][] bfs(int i, int j, int cnt) {
            int[][] res = new int[m][n];
            for (int[] row : res) {
                Arrays.fill(row, -1);
            }
            res[i][j] = 0;
            Queue<int[]> queue = new ArrayDeque();
            queue.add(new int[]{i, j});
            int  i_to, j_to, i_from, j_from;
            while (!queue.isEmpty() && cnt != 0) {
                int[] from = queue.poll();
                i_from = from[0];
                j_from = from[1];
                for (int k = 0; k < 4; k++) {
                    i_to = i_from + steps[k];
                    j_to = j_from + steps[3 - k];
                    if (isLegal(i_to, j_to) && graph[i_to][j_to] != '#' && res[i_to][j_to] == -1) {
                        res[i_to][j_to] = res[i_from][j_from] + 1;
                        queue.offer(new int[]{i_to, j_to});
                        if (graph[i_to][j_to] == 'T' || graph[i_to][j_to] == 'O') cnt--;
                    }
                }
            }
            return res;
        }

        private boolean isLegal(int i, int j) {
            if (i < 0 || i >= m || j < 0 || j >= n) return false;
            return true;
        }

}
