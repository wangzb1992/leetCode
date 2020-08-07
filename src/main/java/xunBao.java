import java.util.*;

public class xunBao<psvm> {
    /**
     * 我们得到了一副藏宝图，藏宝图显示，在一个迷宫中存在着未被世人发现的宝藏。
     *
     * 迷宫是一个二维矩阵，用一个字符串数组表示。它标识了唯一的入口（用 'S' 表示），和唯一的宝藏地点（用 'T' 表示）。但是，宝藏被一些隐蔽的机关保护了起来。在地图上有若干个机关点（用 'M' 表示），只有所有机关均被触发，才可以拿到宝藏。
     *
     * 要保持机关的触发，需要把一个重石放在上面。迷宫中有若干个石堆（用 'O' 表示），每个石堆都有无限个足够触发机关的重石。但是由于石头太重，我们一次只能搬一个石头到指定地点。
     *
     * 迷宫中同样有一些墙壁（用 '#' 表示），我们不能走入墙壁。剩余的都是可随意通行的点（用 '.' 表示）。石堆、机关、起点和终点（无论是否能拿到宝藏）也是可以通行的。
     *
     * 我们每步可以选择向上/向下/向左/向右移动一格，并且不能移出迷宫。搬起石头和放下石头不算步数。那么，从起点开始，我们最少需要多少步才能最后拿到宝藏呢？如果无法拿到宝藏，返回 -1 。
     *
     * 示例 1：
     *
     *     输入： ["S#O", "M..", "M.T"]
     *
     *     输出：16
     *
     *     解释：最优路线为： S->O, cost = 4, 去搬石头 O->第二行的M, cost = 3, M机关触发 第二行的M->O, cost = 3, 我们需要继续回去 O 搬石头。 O->第三行的M, cost = 4, 此时所有机关均触发 第三行的M->T, cost = 2，去T点拿宝藏。 总步数为16。
     *
     * 示例 2：
     *
     *     输入： ["S#O", "M.#", "M.T"]
     *
     *     输出：-1
     *
     *     解释：我们无法搬到石头触发机关
     *
     * 示例 3：
     *
     *     输入： ["S#O", "M.T", "M.."]
     *
     *     输出：17
     *
     *     解释：注意终点也是可以通行的。
     *
     * 限制：
     *
     *     1 <= maze.length <= 100
     *     1 <= maze[i].length <= 100
     *     maze[i].length == maze[j].length
     *     S 和 T 有且只有一个
     *     0 <= M的数量 <= 16
     *     0 <= O的数量 <= 40，题目保证当迷宫中存在 M 时，一定存在至少一个 O 。
     *
     */
    class Solution {

        // time complexity O(n * m * M + M * M * 2 ^ M + M * M * O) = O(n * m * M + M * M * 2 ^ M)
        public int minimalSteps(String[] maze) {
            int n = maze.length, m = maze[0].length();
            int sr = 0, sc = 0, stoneNum = 0, er = 0, ec = 0;
            List<int[]> oList = new ArrayList();
            Map<Integer, Integer> oMap = new HashMap();
            List<int[]> mList = new ArrayList();
            Map<Integer, Integer> mMap = new HashMap();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (maze[i].charAt(j) == 'S') {
                        sr = i;
                        sc = j;
                    } else if (maze[i].charAt(j) == 'M') {
                        mList.add(new int[]{i, j});
                        mMap.put(i * m + j, ++stoneNum);
                    } else if (maze[i].charAt(j) == 'O') {
                        oMap.put(i * m + j, oList.size());
                        oList.add(new int[]{i, j});
                    } else if (maze[i].charAt(j) == 'T') {
                        er = i;
                        ec = j;
                    }
                }
            }
            // 将起点作为第一个机关
            mMap.put(sr * m + sc, 0);
            mList.add(0, new int[]{sr, sc});

            // 1. 判断合法性 O(n + m)
            if (!isValid(maze, sr, sc, stoneNum)) {
                return -1;
            }

            // 2. 计算机关和石堆的最小距离 O(M * n * m)
            int[][] dis = new int[mList.size()][oList.size()];
            for (int i = 0; i < mList.size(); i++) {
                Arrays.fill(dis[i], (int) 1e6);
            }
            for (int i = 0; i < mList.size(); i++) {
                Deque<int[]> queue = new ArrayDeque();
                int step = 0;
                boolean[][] seen = new boolean[n][m];
                seen[mList.get(i)[0]][mList.get(i)[1]] = true;
                queue.add(mList.get(i));
                while (!queue.isEmpty()) {
                    step++;
                    int size = queue.size();
                    while (size-- > 0) {
                        int[] cur = queue.poll();
                        for (int[] dir : dirs) {
                            int nr = cur[0] + dir[0];
                            int nc = cur[1] + dir[1];
                            if (nr < 0 || nc < 0 || nr >= n || nc >= m || seen[nr][nc]
                                    || maze[nr].charAt(nc) == '#') {
                                continue;
                            }
                            seen[nr][nc] = true;
                            if (maze[nr].charAt(nc) == 'O') {
                                dis[i][oMap.get(nr * m + nc)] = step;
                            }
                            queue.add(new int[]{nr, nc});
                        }
                    }
                }
            }

            // 3. DP计算状态 O(M * M * O + M * M * 2 ^ M)
            int[][] minDis = new int[mList.size()][mList.size()];

            // 3.1 预处理机关之间的最短距离 O(M * M * O)
            for (int i = 0; i < mList.size(); i++) {
                for (int j = 0; j < mList.size(); j++) {
                    minDis[i][j] = Integer.MAX_VALUE;
                    for (int p = 0; p < oList.size(); p++) {
                        minDis[i][j] = Math.min(minDis[i][j], dis[i][p] + dis[j][p]);
                    }
                }
            }

            // O(M * M * 2 ^ M)
            int[][] dp = new int[1 << mList.size()][mList.size()];
            for (int i = 0; i < (1 << mList.size()); i++) {
                Arrays.fill(dp[i], -1);
            }
            dp[1][0] = 0;
            for (int i = 1; i < (1 << mList.size()); i++) {
                for (int j = 0; j < mList.size(); j++) {
                    if (dp[i][j] == -1) {
                        continue;
                    }
                    for (int k = 0; k < mList.size(); k++) {
                        if ((i & (1 << k)) == 0 && (dp[i | (1 << k)][k] == -1
                                || dp[i | (1 << k)][k] > minDis[j][k] + dp[i][j])) {
                            dp[i | (1 << k)][k] = minDis[j][k] + dp[i][j];
                        }
                    }
                }
            }

            // 4. 计算最后的距离 O(n * m)
            int[] tDis = new int[mList.size()];  //统计终点到各个机关的距离
            boolean[][] seen = new boolean[n][m];
            Deque<int[]> deque = new ArrayDeque();
            deque.add(new int[]{er, ec});
            seen[er][ec] = true;
            int step = 0;
            while (!deque.isEmpty()) {
                step++;
                int size = deque.size();
                while (size-- > 0) {
                    int[] cur = deque.poll();
                    for (int[] dir : dirs) {
                        int nr = cur[0] + dir[0];
                        int nc = cur[1] + dir[1];
                        if (nr < 0 || nc < 0 || nr >= n || nc >= m || seen[nr][nc]
                                || maze[nr].charAt(nc) == '#') {
                            continue;
                        }
                        seen[nr][nc] = true;
                        if (maze[nr].charAt(nc) == 'M') {
                            tDis[mMap.get(nr * m + nc)] = step;
                        }
                        if (nr == sr && nc == sc && stoneNum == 0) { // 处理没有机关的情况
                            return step;
                        }
                        deque.add(new int[]{nr, nc});
                    }
                }
            }

            // 5 遍历结果 O(M)
            int res = Integer.MAX_VALUE;
            for (int i = 1; i < mList.size(); i++) {
                res = Math.min(res, dp[(1 << mList.size()) - 1][i] + tDis[i]);
            }
            return res;
        }

        private int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        private boolean isValid(String[] maze, int sr, int sc, int stoneNum) {
            int n = maze.length, m = maze[0].length();
            boolean[][] seen = new boolean[n][m];
            Deque<int[]> queue = new ArrayDeque();
            int[] valid = new int[3];
            seen[sr][sc] = true;
            queue.add(new int[]{sr, sc});
            while (!queue.isEmpty()) {
                int[] cur = queue.poll();
                for (int[] dir : dirs) {
                    int nr = cur[0] + dir[0];
                    int nc = cur[1] + dir[1];
                    if (nr < 0 || nc < 0 || nr >= n || nc >= m || seen[nr][nc] || maze[nr].charAt(nc) == '#') {
                        continue;
                    }
                    seen[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                    if (maze[nr].charAt(nc) == 'O') {
                        valid[0]++;
                    } else if (maze[nr].charAt(nc) == 'M') {
                        valid[1]++;
                    } else if (maze[nr].charAt(nc) == 'T') {
                        valid[2]++;
                    }
                }
            }
            if (stoneNum == 0) {
                return valid[2] > 0;
            } else {
                return valid[0] > 0 && valid[2] > 0 && valid[1] == stoneNum;
            }
        }
    }


//
//
//    const int INF = 0x3f3f3f / 2;
//    class Solution {
//        public:
//        int cnt = 0;
//        int id_cnt = 0;
//        unordered_map<int, int> id;
//        int n, m;
//
//        //bfs过程，求解某一个M到其他所有位置的最短距离
//        void bfs(vector<string>& maze, vector<vector<int>>& dist, int x, int y)
//        {
//            //需要存储一个当前点位置，和到当前点为止的最短距离
//            queue<pair<int, pair<int, int>>> q;
//            dist[x][y] = 0;
//            q.push({0, {x, y}});
//
//            while (q.size())
//            {
//                int dx[] = {0, 1, 0, -1}, dy[] = {1, 0, -1, 0};
//                auto t = q.front();
//                q.pop();
//
//                int len = t.first;
//                int xx = t.second.first, yy = t.second.second;
//                //遍历上下左右四个方向
//                for (int i = 0; i < 4; i ++ )
//                {
//                    int a = xx + dx[i], b = yy + dy[i];
//                    if (a < 0 || a >= n || b < 0 || b >= m) continue;
//                    if (maze[a][b] == '#') continue;
//                    //如果遍历过了就直接下一个位置，不重复遍历
//                    if (dist[a][b] != INF) continue;
//                    //最短距离 + 1
//                    dist[a][b] = len + 1;
//                    q.push({len + 1, {a, b}});
//                }
//            }
//        }
//
//        int minimalSteps(vector<string>& maze) {
//            n = maze.size(), m = maze[0].size();
//            //这里要记录一下T的位置，因为最终答案要从最后一个M走到T，不需要再去O了
//            //因此应该是所有的遍历完全的情况，加上到T的距离之和的最短是答案
//            int fi = 0, fj = 0;
//            //因为后边要用状压DP求哈密尔顿路径，所以要记录S点在{S, M}矩阵中的编号
//            int id_s = 0;
//            //为了方便起见，将所有方格内的点编上编号，其中编号从0开始
//            //这样可以将二维坐标变成1维，并且可以还原回去
//            vector<vector<int>> g(n, vector<int>(m));
//            for (int i = 0; i < n; i ++ )
//                for (int j = 0; j < m; j ++ )
//                {
//                    //编上编号
//                    g[i][j] = cnt;
//                    //如果是S或者M，那么要加到哈密尔顿路径中需要遍历的点的集合中
//                    if (maze[i][j] == 'S' || maze[i][j] == 'M')
//                    {
//                        if (maze[i][j] == 'S') id_s = id_cnt;
//                        //用哈希表进行一个映射，从而可以用邻接矩阵中的id_cnt查找g中的编号
//                        id[id_cnt ++ ] = cnt;
//                    }
//                    else if (maze[i][j] == 'T')
//                        fi = i, fj = j;
//                    cnt ++ ;
//                }
//
//            //这里是邻接矩阵，将所有{S,M}依次编号，之后方便进行状压DP求解
//            vector<vector<int>> d(id_cnt, vector<int>(id_cnt, INF));
//
//            //自己到自己的距离为0
//            for (int i = 0; i < id_cnt; i ++ )
//                d[i][i] = 0;
//
//            for (int i = 0; i < n; i ++ )
//                for (int j = 0; j < m; j ++ )
//                    //如果是O，则需要求O到所有{S, M}集合的距离，M -> O -> M的距离对需要枚举
//                    if (maze[i][j] == 'O')
//                    {
//                        vector<vector<int>> dist(n, vector<int>(m, INF));
//                        bfs(maze, dist, i, j);
//
//                        //枚举所有M -> O -> M的距离，其中每对的最小值作为哈密尔顿路径权重值
//                        for (int u = 0; u < id_cnt; u ++ )
//                            for (int v = u + 1; v < id_cnt; v ++ )
//                            {
//                                int p1 = id[u], p2 = id[v];
//                                //将g中的坐标还原回去，这样才能得到对应距离下标x, y
//                                int x1 = p1 / m, y1 = p1 % m;
//                                int x2 = p2 / m, y2 = p2 % m;
//
//                                d[v][u] = d[u][v] = min(d[u][v], dist[x1][y1] + dist[x2][y2]);
//                            }
//                    }
//
//            //计算所有点到终点T的距离，这里T是需要特殊区别于S和M的，因为T不需要去搬石头
//            vector<vector<int>> dist_T(n, vector<int>(m, INF));
//            bfs(maze, dist_T, fi, fj);
//            int ans = INF;
//
//            //状压DP求解哈密尔顿路径
//            vector<vector<int>> f(1 << id_cnt, vector<int>(id_cnt, INF));
//
//            //id_s是编号，f[i][j]表示状态为i的情况下，处于j时，所走过的最短路径
//            f[1 << id_s][id_s] = 0;
//
//            //枚举所有状态
//            for (int i = 0; i < 1 << id_cnt; i ++ )
//                //枚举所有停靠点
//                for (int j = 0; j < id_cnt; j ++ )
//                    //如果当前点是被走到的
//                    if (i >> j & 1)
//                        //枚举停靠点
//                        for (int k = 0; k < id_cnt; k ++ )
//                            //停靠点不能被走到
//                            if (!(i >> k & 1))
//                                //那么当前点可以走到停靠点，状态为f[i | (1 << k)][k]
//                                f[i | (1 << k)][k] = min(f[i | (1 << k)][k], f[i][j] + d[j][k]);
//
//            //枚举所有可以停靠的点，且状态为全部为1，即都走过一遍
//            for (int j = 0; j < id_cnt; j ++ )
//            {
//                int p = id[j];
//                int x = p / m, y = p % m;
//                //从某个停靠点走到T终点的最小值就是答案
//                ans = min(ans, f[(1 << id_cnt) - 1][j] + dist_T[x][y]);
//            }
//            //如果答案太大，说明是不可达
//            //这里INF设置为 0x3f3f3f3f / 2防止溢出
//            if (ans >= INF) return -1;
//            else return ans;
//        }
//    };


}
