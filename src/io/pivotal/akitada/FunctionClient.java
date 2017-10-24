package io.pivotal.akitada;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FunctionClient {
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

    System.out.println("Joining data in /" + REGION1 + " and /" + REGION2 + ". Then, putting joined data into /" + JOINED_REGION + ", by calling Function.");

    JoinRegionsFunction function = new JoinRegionsFunction();
    FunctionService.registerFunction(function);
    ArrayList<String> argList = new ArrayList<String>();
    argList.add(REGION1);
    argList.add(REGION2);
    argList.add(JOINED_REGION);
    Execution execution = FunctionService.onServer((RegionService) ccache).setArguments(argList);

    // collect result sets from the target peer for function
    try {
      @SuppressWarnings("rawtypes")
      ResultCollector rc = execution.execute(function);
      List result = (List) rc.getResult();
      System.out.println("Result set=" + result.toString());
    } catch (FunctionException ex) {
      System.out.println("Something wrong with function exection: " + ex.getMessage());
      ex.printStackTrace();
    }

    // confirmation the joined result
    Region<String, String> joinedRegion = ccache.getRegion(JOINED_REGION);
    Set<String> keySet =  joinedRegion.keySetOnServer();
    for(String str : keySet) {
      System.out.println("common key=" + str + ", joined value=" + joinedRegion.get(str));
    }
    System.out.println("Done to execute join function.");

    ccache.close();
  }
}
