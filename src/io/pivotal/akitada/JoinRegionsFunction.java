package io.pivotal.akitada;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultSender;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.Struct;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

public class JoinRegionsFunction implements Function, Declarable {

  private static final long serialVersionUID = 5444789932775728927L;

  @Override
  public boolean hasResult() {
    return true;
  }

  @Override
  public String getId() {
    return "JoinRegionsFunction";
  }

  @Override
  public boolean optimizeForWrite() {
    return true;
  }

  @Override
  public boolean isHA() {
    return true;
  }

  @Override
  public void init(Properties properties) {
  }

  @Override
  public void execute(FunctionContext context) {
    // create a result sender to return some values to the function caller
    ResultSender resultSender = context.getResultSender();

    // get a cache to get regions and execute a query
    Cache cache = CacheFactory.getAnyInstance();

    // prepare the query service
    QueryService queryService = cache.getQueryService();

    // get arguments from the function caller: region name1 (to be joined), region name2 (to be joined), region name3 (store joined result)
    ArrayList argList = (ArrayList) (context.getArguments());

    // prepare query string to join two region
    String queryString = "SELECT DISTINCT e1.key, e1.value, e2.value FROM /" + (String) argList.get(0)
        + ".entries e1, /" + (String) argList.get(1) + ".entries e2 WHERE e1.key = e2.key";

    try {
      // prepare the query
      Query query = queryService.newQuery(queryString);

      // exeucte query and get result set
      SelectResults result = (SelectResults) query.execute();

      // get region to store the joined results
      Region<String, String> joinedRegion = cache.getRegion((String)argList.get(2));

      // logic to join values from two regions
      for(Object joinedValue: result) {
        Object[] kvs = ((Struct)joinedValue).getFieldValues();
        //System.out.println("common key=" + kvs[0] + ", value1=" + kvs[1].toString() + ", value2=" + kvs[2].toString());
        joinedRegion.put(kvs[0].toString(), kvs[1].toString()+":"+kvs[2].toString());
      }

      // send query resut - actually, this is not required but for just confirmation
      resultSender.lastResult((ArrayList) result.asList());
    } catch (Exception e) {
      ArrayList<String> errInfo = new ArrayList();
      errInfo.add(queryString);
      errInfo.add(e.getClass().getSimpleName());
      //errInfo.add(e.getStackTrace().toString());
      resultSender.lastResult(errInfo);
    }
  }
}