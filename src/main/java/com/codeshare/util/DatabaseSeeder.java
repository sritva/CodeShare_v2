package com.codeshare.util;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.SnippetRepository;
import com.codeshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SnippetRepository snippetRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DatabaseSeeder(UserRepository userRepository, SnippetRepository snippetRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.snippetRepository = snippetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User alice = new User();
            alice.setUsername("alice");
            alice.setPasswordHash(passwordEncoder.encode("password123"));
            alice = userRepository.save(alice);

            User bob = new User();
            bob.setUsername("bob");
            bob.setPasswordHash(passwordEncoder.encode("password123"));
            bob = userRepository.save(bob);

            User charlie = new User();
            charlie.setUsername("charlie");
            charlie.setPasswordHash(passwordEncoder.encode("password123"));
            charlie = userRepository.save(charlie);

            User michael = new User();
            michael.setUsername("michael");
            michael.setPasswordHash(passwordEncoder.encode("password123"));
            michael = userRepository.save(michael);

            Snippet s1 = new Snippet();
            s1.setTitle("Binary Search");
            s1.setCode(
                "public int binarySearch(int[] nums, int target) {\n" +
                "    int lo = 0, hi = nums.length - 1;\n" +
                "    while (lo <= hi) {\n" +
                "        int mid = lo + (hi - lo) / 2;\n" +
                "        if (nums[mid] == target) return mid;\n" +
                "        else if (nums[mid] < target) lo = mid + 1;\n" +
                "        else hi = mid - 1;\n" +
                "    }\n" +
                "    return -1;\n" +
                "}"
            );
            s1.setLanguage("java");
            s1.setPublic(true);
            s1.setUser(alice);
            snippetRepository.save(s1);

            Snippet s2 = new Snippet();
            s2.setTitle("Two Sum");
            s2.setCode(
                "public int[] twoSum(int[] nums, int target) {\n" +
                "    Map<Integer, Integer> map = new HashMap<>();\n" +
                "    for (int i = 0; i < nums.length; i++) {\n" +
                "        int complement = target - nums[i];\n" +
                "        if (map.containsKey(complement)) {\n" +
                "            return new int[]{map.get(complement), i};\n" +
                "        }\n" +
                "        map.put(nums[i], i);\n" +
                "    }\n" +
                "    return new int[]{};\n" +
                "}"
            );
            s2.setLanguage("java");
            s2.setPublic(true);
            s2.setUser(alice);
            snippetRepository.save(s2);

            Snippet s3 = new Snippet();
            s3.setTitle("Alice's Interview Prep Notes");
            s3.setCode(
                "Topics to review:\n" +
                "- Sliding window\n" +
                "- Monotonic stack\n" +
                "- Union-Find\n" +
                "- Topological sort\n" +
                "- Segment tree basics"
            );
            s3.setLanguage("text");
            s3.setPublic(false);
            s3.setUser(alice);
            snippetRepository.save(s3);

            Snippet s4 = new Snippet();
            s4.setTitle("Reverse a Linked List");
            s4.setCode(
                "public ListNode reverse(ListNode head) {\n" +
                "    ListNode prev = null, curr = head;\n" +
                "    while (curr != null) {\n" +
                "        ListNode next = curr.next;\n" +
                "        curr.next = prev;\n" +
                "        prev = curr;\n" +
                "        curr = next;\n" +
                "    }\n" +
                "    return prev;\n" +
                "}"
            );
            s4.setLanguage("java");
            s4.setPublic(true);
            s4.setUser(bob);
            snippetRepository.save(s4);

            Snippet s5 = new Snippet();
            s5.setTitle("Valid Parentheses");
            s5.setCode(
                "def is_valid(s: str) -> bool:\n" +
                "    stack = []\n" +
                "    mapping = {')': '(', '}': '{', ']': '['}\n" +
                "    for char in s:\n" +
                "        if char in mapping:\n" +
                "            top = stack.pop() if stack else '#'\n" +
                "            if mapping[char] != top:\n" +
                "                return False\n" +
                "        else:\n" +
                "            stack.append(char)\n" +
                "    return not stack"
            );
            s5.setLanguage("python");
            s5.setPublic(true);
            s5.setUser(bob);
            snippetRepository.save(s5);

            Snippet s6 = new Snippet();
            s6.setTitle("Merge Sort");
            s6.setCode(
                "function mergeSort(arr) {\n" +
                "    if (arr.length <= 1) return arr;\n" +
                "    const mid = Math.floor(arr.length / 2);\n" +
                "    const left = mergeSort(arr.slice(0, mid));\n" +
                "    const right = mergeSort(arr.slice(mid));\n" +
                "    return merge(left, right);\n" +
                "}\n\n" +
                "function merge(l, r) {\n" +
                "    const result = [];\n" +
                "    let i = 0, j = 0;\n" +
                "    while (i < l.length && j < r.length) {\n" +
                "        result.push(l[i] <= r[j] ? l[i++] : r[j++]);\n" +
                "    }\n" +
                "    return result.concat(l.slice(i)).concat(r.slice(j));\n" +
                "}"
            );
            s6.setLanguage("javascript");
            s6.setPublic(true);
            s6.setUser(charlie);
            snippetRepository.save(s6);

            Snippet s7 = new Snippet();
            s7.setTitle("BFS Level Order Traversal");
            s7.setCode(
                "public List<List<Integer>> levelOrder(TreeNode root) {\n" +
                "    List<List<Integer>> result = new ArrayList<>();\n" +
                "    if (root == null) return result;\n" +
                "    Queue<TreeNode> queue = new LinkedList<>();\n" +
                "    queue.offer(root);\n" +
                "    while (!queue.isEmpty()) {\n" +
                "        int size = queue.size();\n" +
                "        List<Integer> level = new ArrayList<>();\n" +
                "        for (int i = 0; i < size; i++) {\n" +
                "            TreeNode node = queue.poll();\n" +
                "            level.add(node.val);\n" +
                "            if (node.left != null) queue.offer(node.left);\n" +
                "            if (node.right != null) queue.offer(node.right);\n" +
                "        }\n" +
                "        result.add(level);\n" +
                "    }\n" +
                "    return result;\n" +
                "}"
            );
            s7.setLanguage("java");
            s7.setPublic(true);
            s7.setUser(charlie);
            snippetRepository.save(s7);

            Snippet s8 = new Snippet();
            s8.setTitle("Charlie's WIP — Dijkstra's");
            s8.setCode(
                "# TODO: finish implementing priority queue version\n" +
                "import heapq\n\n" +
                "def dijkstra(graph, src):\n" +
                "    dist = {node: float('inf') for node in graph}\n" +
                "    dist[src] = 0\n" +
                "    pq = [(0, src)]\n" +
                "    while pq:\n" +
                "        d, u = heapq.heappop(pq)\n" +
                "        # TODO: relax edges\n" +
                "        pass"
            );
            s8.setLanguage("python");
            s8.setPublic(false);
            s8.setUser(charlie);
            snippetRepository.save(s8);

            Snippet s9 = new Snippet();
            s9.setTitle("Longest Common Subsequence");
            s9.setCode(
                "public int lcs(String a, String b) {\n" +
                "    int m = a.length(), n = b.length();\n" +
                "    int[][] dp = new int[m + 1][n + 1];\n" +
                "    for (int i = 1; i <= m; i++) {\n" +
                "        for (int j = 1; j <= n; j++) {\n" +
                "            if (a.charAt(i-1) == b.charAt(j-1))\n" +
                "                dp[i][j] = dp[i-1][j-1] + 1;\n" +
                "            else\n" +
                "                dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);\n" +
                "        }\n" +
                "    }\n" +
                "    return dp[m][n];\n" +
                "}"
            );
            s9.setLanguage("java");
            s9.setPublic(true);
            s9.setUser(michael);
            snippetRepository.save(s9);

            Snippet s10 = new Snippet();
            s10.setTitle("Detect Cycle in Linked List");
            s10.setCode(
                "def has_cycle(head):\n" +
                "    slow, fast = head, head\n" +
                "    while fast and fast.next:\n" +
                "        slow = slow.next\n" +
                "        fast = fast.next.next\n" +
                "        if slow == fast:\n" +
                "            return True\n" +
                "    return False"
            );
            s10.setLanguage("python");
            s10.setPublic(true);
            s10.setUser(michael);
            snippetRepository.save(s10);
        }
    }
}
