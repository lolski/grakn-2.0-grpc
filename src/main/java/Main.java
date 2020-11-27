import grakn.client.GraknClient;
import grakn.client.concept.thing.Entity;
import graql.lang.Graql;

import java.util.Arrays;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        GraknClient client = new GraknClient("localhost:48555");

        long[] perf = new long[1];
        for (int p = 0; p < perf.length; p++) {
            try (GraknClient.Session session = client.session("grakn")) {
                try (GraknClient.Transaction tx = session.transaction().write()) {
                    tx.execute(Graql.define(Graql.type("person").sub("entity")));
                    tx.commit();
                }

                long currTime = System.currentTimeMillis();

                try (GraknClient.Transaction tx = session.transaction().write()) {
                    for (int i = 0; i < 32767; i++) {
                        tx.stream(Graql.insert(Graql.var().isa("person")));
                    }
                    tx.commit();
                }

                int[] x = new int[1];
                try (GraknClient.Transaction tx = session.transaction().read()) {
                    Stream<Entity.Remote> persons = tx.getEntityType("person").asRemote(tx).instances();
                    persons.forEach(person -> {
                        x[0]++;
                    });
                }
                System.out.println("ans count = " + x[0]);

                perf[p] = System.currentTimeMillis() - currTime;
            }

            client.keyspaces().delete("grakn");
        }
        long avg = 0;
        for (int p = 0; p < perf.length; p++) {
            System.out.println("p[" + p + "] = " + perf[p]);
            avg += perf[p];
        }
        System.out.println("avg = " + avg / perf.length);
    }
}
