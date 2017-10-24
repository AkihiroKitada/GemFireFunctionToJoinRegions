package io.pivotal.akitada;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.Struct;

import java.util.ArrayList;
import java.util.List;

public class TestQueryClient {
  private static final String REGION1 = "Region1";
  private static final String REGION2 = "Region2";
  private static final String JOINED_REGION = "JoinedRegion";

  public static void main(String[] argv)  throws Exception {
    ClientCache ccache = new ClientCacheFactory()
        .set("cache-xml-file", "client-cache.xml")
        .create();

    System.out.println("Putting sample data to be joined into /" + REGION1 + " and /" + REGION2 + ".");
    Region<String, String> region1 = ccache.getRegion(REGION1);
    Region<String, String> region2 = ccache.getRegion(REGION2);

    for (int i = 0; i < 100; i++) {
      region1.put("Key-" + String.valueOf(i), REGION1 + "-Value-" + String.valueOf(i));
      region2.put("Key-" + String.valueOf(i*2), REGION2 + "-Value-" + String.valueOf(i*2));
    }
    System.out.println("Done to put sample data.");

    System.out.println("Joining data in /" + REGION1 + " and /" + REGION2 + ". Then, putting joined data into /" + JOINED_REGION + ", by executing query.");
    Region<String, String> joinedRegion = ccache.getRegion(JOINED_REGION);
    QueryService queryService = ccache.getQueryService();
    String queryString = "SELECT DISTINCT e1.key, e1.value, e2.value FROM /" + REGION1 + ".entries e1, /" + REGION2 + ".entries e2 WHERE e1.key = e2.key";
    //Execution execution = FunctionService.onServer((RegionService) ccache).setArguments(argList);

    // collect result sets from each peers for function
    try {
      Query query = queryService.newQuery(queryString);
      SelectResults result = (SelectResults) query.execute();

      for(Object joinedValue: result) {
          Object[] kvs = ((Struct)joinedValue).getFieldValues();
          System.out.println("common key=" + kvs[0] + ", value1=" + kvs[1].toString() + ", value2=" + kvs[2].toString());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println("Done to execute join query.");

    ccache.close();
  }
}
