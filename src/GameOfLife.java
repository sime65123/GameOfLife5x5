import java.util.Random;

public class GameOfLife {
    private static final int SIZE = 5;
    private boolean[][] grid = new boolean[SIZE][SIZE];

    public GameOfLife(boolean[][] initial) {
        if (initial == null || initial.length != SIZE || initial[0].length != SIZE) {
            throw new IllegalArgumentException("La grille initiale doit être 5x5.");
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = initial[r][c];
            }
        }
    }

    /** Compte les voisines vivantes (8) en ignorant ce qui est hors de la grille (considéré mort). */
    private int countLiveNeighbors(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // pas soi-même
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE && grid[nr][nc]) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Applique les règles pour calculer la génération suivante. */
    public void step() {
        boolean[][] next = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int n = countLiveNeighbors(r, c);
                if (grid[r][c]) {
                    // vivante reste vivante si 2 ou 3 voisines vivantes
                    next[r][c] = (n == 2 || n == 3);
                } else {
                    // morte devient vivante si exactement 3 voisines vivantes
                    next[r][c] = (n == 3);
                }
            }
        }
        grid = next;
    }

    /** Affichage console simple. ■ = vivante, · = morte */
    public void print(int generation) {
        System.out.println("Generation " + generation);
        for (int r = 0; r < SIZE; r++) {
            StringBuilder line = new StringBuilder();
            for (int c = 0; c < SIZE; c++) {
                line.append(grid[r][c] ? "■ " : "· ");
            }
            System.out.println(line);
        }
        System.out.println();
    }

    /** Lance le jeu pendant N générations avec un délai (ms) entre chaque. */
    public void run(int generations, int delayMs) {
        for (int gen = 0; gen <= generations; gen++) {
            print(gen);
            if (gen < generations) {
                step();
                if (delayMs > 0) {
                    try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    // ====== Quelques configurations 5x5 prêtes à l'emploi ======

    /** Oscillateur "blinker" centré. */
    public static boolean[][] blinker() {
        boolean[][] g = empty();
        // ligne horizontale au milieu (ligne 2, colonnes 1..3)
        g[2][1] = true; g[2][2] = true; g[2][3] = true;
        return g;
    }

    /** "Block" (still life) 2x2 au centre-gauche. */
    public static boolean[][] block() {
        boolean[][] g = empty();
        g[1][1] = true; g[1][2] = true;
        g[2][1] = true; g[2][2] = true;
        return g;
    }

    /** Un glider dans le coin haut-gauche. */
    public static boolean[][] glider() {
        boolean[][] g = empty();
        g[0][1] = true;
        g[1][2] = true;
        g[2][0] = true; g[2][1] = true; g[2][2] = true;
        return g;
    }

    /** Grille vide 5x5. */
    public static boolean[][] empty() {
        return new boolean[SIZE][SIZE];
    }

    /**
     * Construit une grille à partir de 5 chaînes de 5 caractères.
     * Caractères acceptés pour "vivante" : 1, X, O, #, *
     * Exemple :
     * of("..X..",
     *    "..X..",
     *    "..X..",
     *    ".....",
     *    ".....");
     * Les autres caractères sont considérés comme morts.
     */
    public static boolean[][] of(String... rows) {
        if (rows == null || rows.length != SIZE) {
            throw new IllegalArgumentException("Il faut exactement 5 lignes.");
        }
        boolean[][] g = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            if (rows[r].length() != SIZE) {
                throw new IllegalArgumentException("Chaque ligne doit faire 5 caractères.");
            }
            for (int c = 0; c < SIZE; c++) {
                char ch = rows[r].charAt(c);
                g[r][c] = (ch == '1' || ch == 'X' || ch == 'O' || ch == '#' || ch == '*');
            }
        }
        return g;
    }

    /** Grille aléatoire reproductible (probabilité de vie entre 0.0 et 1.0). */
    public static boolean[][] random(long seed, double aliveProbability) {
        if (aliveProbability < 0 || aliveProbability > 1) {
            throw new IllegalArgumentException("aliveProbability doit être entre 0.0 et 1.0");
        }
        boolean[][] g = new boolean[SIZE][SIZE];
        Random rnd = new Random(seed);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                g[r][c] = rnd.nextDouble() < aliveProbability;
            }
        }
        return g;
    }

    // ================== Point d'entrée ==================

    /**
     * Utilisation (arguments optionnels) :
     *   pattern generations delayMs
     *   - pattern ∈ {blinker, block, glider, random}
     *   - generations (défaut: 10)
     *   - delayMs    (défaut: 300)
     *
     * Exemples :
     *   (sans arguments)              -> blinker, 10 générations, 300 ms
     *   glider 20 200                 -> glider, 20 générations, 200 ms
     *   random 15 100                 -> aléatoire, 15 générations, 100 ms
     */
    public static void main(String[] args) {
        String pattern = (args.length > 0) ? args[0].toLowerCase() : "blinker";
        int generations = (args.length > 1) ? parseIntOr(args[1], 10) : 10;
        int delayMs = (args.length > 2) ? parseIntOr(args[2], 300) : 300;

        boolean[][] initial;
        switch (pattern) {
            case "block":
                initial = block();
                break;
            case "glider":
                initial = glider();
                break;
            case "random":
                initial = random(System.currentTimeMillis(), 0.35);
                break;
            case "blinker":
            default:
                initial = blinker();
                break;
        }

        GameOfLife game = new GameOfLife(initial);
        game.run(generations, delayMs);
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
