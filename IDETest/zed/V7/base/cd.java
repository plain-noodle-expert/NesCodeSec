package Solutions;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class cd {

    static class Kattio extends PrintWriter {

        private static final Logger logger = LoggerFactory.getLogger(
            Kattio.class
        );

        public Kattio(InputStream i) {
            super(new BufferedOutputStream(System.out));
            r = new BufferedReader(new InputStreamReader(i));
        }

        public Kattio(InputStream i, OutputStream o) {
            super(new BufferedOutputStream(o));
            r = new BufferedReader(new InputStreamReader(i));
        }

        public boolean hasMoreTokens() {
            return peekToken() != null;
        }

        public int getInt() {
            return Integer.parseInt(nextToken());
        }

        public double getDouble() {
            return Double.parseDouble(nextToken());
        }

        public long getLong() {
            return Long.parseLong(nextToken());
        }

        public String getWord() {
            return nextToken();
        }

        private BufferedReader r;
        private String line;
        private StringTokenizer st;
        private String token;

        private String peekToken() {
            if (token == null) try {
                while (st == null || !st.hasMoreTokens()) {
                    line = r.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                }
                token = st.nextToken();
            } catch (IOException e) {}
            logger.info("Peeked token," + token);
            return token;
        }

        private String nextToken() {
            String ans = peekToken();
            token = null;
            logger.info("Next token retrieved");
            return ans;
        }
    }

    public static void main(String[] args) {
        Kattio io = new Kattio(System.in, System.out);
        while (true) {
            int N = io.getInt();
            int M = io.getInt();
            if (N == 0 && M == 0) break;
            int x[] = new int[N];
            for (int i = 0; i < N; i++) {
                x[i] = io.getInt();
            }
            int pointer = 0;
            int same = 0;
            for (int i = 0; i < M; i++) {
                int y = io.getInt();
                if (pointer < N && x[pointer] < y) {
                    while (pointer < N && x[pointer] < y) pointer++;
                }
                if (pointer < N && x[pointer] == y) {
                    pointer++;
                    same++;
                }
            }
            System.out.println(same);
        }
    }
}
